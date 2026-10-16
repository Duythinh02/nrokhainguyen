package adminpanel;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import nro.models.clan.Clan;
import nro.models.clan.ClanMember;
import nro.models.data.LocalManager;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.server.Manager;
import nro.models.shop_ky_gui.ConsignShopManager;

// Xóa nhân vật / tài khoản vĩnh viễn, dùng chung cho tab Tài khoản và tab Người chơi.
public final class AccountDeleter {

    private AccountDeleter() {}

    // deleteAccount = false: chỉ xóa nhân vật, tài khoản còn lại, đăng nhập lại sẽ tạo nhân vật mới.
    // deleteAccount = true : xóa cả nhân vật lẫn tài khoản.
    // playerName = null nếu tài khoản chưa có nhân vật. onDone chạy trên EDT sau khi xóa xong.
    public static void confirmAndDelete(Component parent, int accountId, String username, String playerName,
                                        boolean deleteAccount, Runnable onDone) {
        boolean hasPlayer = playerName != null;
        if (!deleteAccount && !hasPlayer) {
            JOptionPane.showMessageDialog(parent, "Tài khoản này chưa có nhân vật.");
            return;
        }

        String what = deleteAccount
                ? "XÓA VĨNH VIỄN TÀI KHOẢN \"" + username + "\" (ID " + accountId + ")" + (hasPlayer ? " và nhân vật \"" + playerName + "\"" : "")
                : "XÓA NHÂN VẬT \"" + playerName + "\" của tài khoản \"" + username + "\" (tài khoản vẫn giữ lại)";
        String typed = JOptionPane.showInputDialog(parent,
                what + "\n\n"
                + "• Người chơi đang online sẽ bị kick.\n"
                + "• Nhân vật được gỡ khỏi bang, đồ đang ký gửi bị xóa.\n"
                + "• Dữ liệu được sao lưu vào thư mục backup_xoa trước khi xóa.\n\n"
                + "Gõ đúng tên tài khoản \"" + username + "\" để xác nhận:",
                deleteAccount ? "Xóa tài khoản" : "Xóa nhân vật", JOptionPane.WARNING_MESSAGE);
        if (typed == null) return;
        if (!typed.trim().equals(username)) {
            JOptionPane.showMessageDialog(parent, "Tên tài khoản không khớp, đã hủy.");
            return;
        }

        new Thread(() -> {
            try {
                // 1) Kick nếu đang online (kick sẽ lưu nhân vật xuống DB trước, nên phải kick rồi mới xóa)
                Player online = Client.gI().getPlayerByUser(accountId);
                if (online != null && online.getSession() != null) {
                    Client.gI().kickSession(online.getSession());
                }

                try (Connection conn = LocalManager.getConnection()) {
                    List<Integer> playerIds = new ArrayList<>();
                    try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM player WHERE account_id = ?")) {
                        ps.setInt(1, accountId);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) playerIds.add(rs.getInt("id"));
                        }
                    }

                    // 2) Sao lưu trước khi xóa
                    File backup = backupBeforeDelete(conn, accountId, playerIds, deleteAccount);

                    // 3) Gỡ khỏi dữ liệu đang chạy trong RAM (bang hội, ký gửi)
                    for (int pid : playerIds) {
                        removeFromClan(pid);
                        ConsignShopManager.gI().listItem.removeIf(it -> it != null && it.player_sell == pid);
                    }

                    // 4) Xóa trong DB
                    boolean hasKyGui = hasTable(conn, "shop_ky_gui");
                    boolean hasSuperRank = hasTable(conn, "super_rank");
                    conn.setAutoCommit(false);
                    try {
                        for (int pid : playerIds) {
                            if (hasKyGui) execDelete(conn, "DELETE FROM shop_ky_gui WHERE player_id = ?", pid);
                            if (hasSuperRank) execDelete(conn, "DELETE FROM super_rank WHERE player_id = ?", pid);
                            execDelete(conn, "DELETE FROM player WHERE id = ?", pid);
                        }
                        if (deleteAccount) execDelete(conn, "DELETE FROM account WHERE id = ?", accountId);
                        conn.commit();
                    } catch (Exception ex) {
                        conn.rollback();
                        throw ex;
                    } finally {
                        conn.setAutoCommit(true);
                    }

                    String msg = (deleteAccount ? "Đã xóa tài khoản \"" + username + "\"" : "Đã xóa nhân vật \"" + playerName + "\"")
                            + "\nSao lưu: " + backup.getAbsolutePath();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(parent, msg);
                        if (onDone != null) onDone.run();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent, "Lỗi khi xóa: " + e.getMessage()));
            }
        }).start();
    }

    // Tìm tài khoản của nhân vật rồi xóa (dùng ở tab Người chơi).
    public static void confirmAndDeleteByPlayer(Component parent, int playerId, boolean deleteAccount, Runnable onDone) {
        try (Connection conn = LocalManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT p.name, a.id AS acc_id, a.username FROM player p LEFT JOIN account a ON a.id = p.account_id WHERE p.id = ?")) {
            ps.setInt(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(parent, "Không tìm thấy nhân vật ID " + playerId + " (có thể đã bị xóa).");
                    return;
                }
                String username = rs.getString("username");
                if (username == null) {
                    JOptionPane.showMessageDialog(parent, "Nhân vật ID " + playerId + " không gắn với tài khoản nào.");
                    return;
                }
                confirmAndDelete(parent, rs.getInt("acc_id"), username, rs.getString("name"), deleteAccount, onDone);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Lỗi: " + e.getMessage());
        }
    }

    // Gỡ nhân vật khỏi bang trong RAM rồi lưu bang xuống DB.
    // Nếu là bang chủ: nhường cho phó bang (hoặc thành viên) có sức mạnh cao nhất; bang không còn ai thì giải tán.
    private static void removeFromClan(int playerId) {
        for (Clan clan : new ArrayList<>(Manager.CLANS)) {
            ClanMember cm = clan.getClanMember(playerId);
            if (cm == null) continue;

            boolean wasLeader = cm.role == Clan.LEADER;
            clan.removeClanMember(cm);
            clan.removeMemberOnline(cm, null);

            if (clan.getMembers().isEmpty()) {
                clan.deleteDB(clan.id);
                Manager.CLANS.remove(clan);
                continue;
            }
            if (wasLeader) {
                ClanMember next = null;
                for (ClanMember m : clan.getMembers()) {
                    if (next == null
                            || (m.role == Clan.DEPUTY && next.role != Clan.DEPUTY)
                            || (m.role == next.role && m.powerPoint > next.powerPoint)) {
                        next = m;
                    }
                }
                next.role = Clan.LEADER;
            }
            clan.sendMyClanForAllMember();
            clan.update();
        }
    }

    // Ghi dữ liệu sắp xóa thành file .sql (INSERT) để có thể khôi phục.
    private static File backupBeforeDelete(Connection conn, int accountId, List<Integer> playerIds, boolean deleteAccount) throws Exception {
        File dir = new File("backup_xoa");
        dir.mkdirs();
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        File f = new File(dir, (deleteAccount ? "taikhoan_" : "nhanvat_") + accountId + "_" + time + ".sql");
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            w.println("-- Sao lưu trước khi xóa, account_id = " + accountId + ", " + time);
            if (deleteAccount) dumpRows(conn, w, "account", "id", accountId);
            for (int pid : playerIds) {
                dumpRows(conn, w, "player", "id", pid);
                if (hasTable(conn, "super_rank")) dumpRows(conn, w, "super_rank", "player_id", pid);
                if (hasTable(conn, "shop_ky_gui")) dumpRows(conn, w, "shop_ky_gui", "player_id", pid);
            }
        }
        return f;
    }

    private static void dumpRows(Connection conn, PrintWriter w, String table, String keyCol, int key) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM `" + table + "` WHERE `" + keyCol + "` = ?")) {
            ps.setInt(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();
                while (rs.next()) {
                    StringBuilder cols = new StringBuilder();
                    StringBuilder vals = new StringBuilder();
                    for (int i = 1; i <= n; i++) {
                        if (i > 1) { cols.append(", "); vals.append(", "); }
                        cols.append('`').append(md.getColumnName(i)).append('`');
                        vals.append(sqlLiteral(rs.getObject(i)));
                    }
                    w.println("INSERT INTO `" + table + "` (" + cols + ") VALUES (" + vals + ");");
                }
            }
        }
    }

    private static String sqlLiteral(Object v) {
        if (v == null) return "NULL";
        if (v instanceof Number) return v.toString();
        if (v instanceof Boolean) return ((Boolean) v) ? "1" : "0";
        if (v instanceof byte[]) {
            StringBuilder sb = new StringBuilder("X'");
            for (byte b : (byte[]) v) sb.append(String.format("%02X", b));
            return sb.append('\'').toString();
        }
        return "'" + v.toString().replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    private static void execDelete(Connection conn, String sql, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static boolean hasTable(Connection conn, String table) {
        try (ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), null, table, null)) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }
}
