package nro.models.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.data.LocalManager;
import nro.models.map.service.NpcService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.services_func.Input;
import nro.models.utils.Logger;
import nro.models.utils.Util;

/**
 * Sư đồ (menu ở Ông Gohan / Ông Moori / Ông Paragus):
 * - Sư phụ từ MIN_POWER_SU_PHU sức mạnh, tối đa MAX_DE_TU đệ tử; đệ tử bái sư khi dưới MAX_POWER_DE_TU.
 * - Đệ tử đạt mốc sức mạnh (MOC) thì sư phụ nhận điểm săn boss (DIEM_MOC); mốc cuối là xuất sư.
 *   Mốc đã vượt trước lúc bái sư không được tính. Sư phụ offline thì điểm chờ đến lần đăng nhập sau.
 * - Sư phụ và đệ tử cùng khu: cả hai được +2% sức đánh, +5% HP, KI (NPoint, Player.suDoBuff).
 * Dữ liệu ở bảng su_do (1 dòng / đệ tử), cache trên Player khi đăng nhập.
 */
public final class SuDoService {

    public static final long MIN_POWER_SU_PHU = 60_000_000_000L;
    public static final long MAX_POWER_DE_TU = 10_000_000_000L;
    public static final int MAX_DE_TU = 3;
    private static final long[] MOC = {1_000_000_000L, 5_000_000_000L, 10_000_000_000L, 20_000_000_000L, 40_000_000_000L};
    private static final int[] DIEM_MOC = {20, 40, 60, 80, 100};
    public static final int BUFF_DAME = 2, BUFF_HP_KI = 5;

    // Menu Ông Gohan / Moori / Paragus
    public static final int MENU_SU_DO = 7901;
    // Hộp thoại xác nhận gửi cho sư phụ (Con mèo, xử lý ở NpcFactory)
    public static final int MENU_XAC_NHAN_BAI_SU = 7902;
    private static final long REQUEST_TIMEOUT_MS = 60_000;

    // id sư phụ -> {id người xin bái, thời điểm}
    private static final Map<Long, long[]> requests = new ConcurrentHashMap<>();
    // Người đang online có quan hệ sư đồ, quét buff mỗi vài giây
    private static final Set<Long> active = ConcurrentHashMap.newKeySet();

    private SuDoService() {
    }

    private static String ty(long power) {
        return Util.numberToMoney(power);
    }

    // Tải sư đồ lúc đăng nhập chạy nền, không làm chậm luồng đăng nhập
    private static final java.util.concurrent.ExecutorService LOADER = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
        Thread th = new Thread(r, "SuDoLoader");
        th.setDaemon(true);
        return th;
    });

    // ===================== Đăng nhập / cache =====================
    public static void onLoginAsync(Player player) {
        if (player != null && player.isPl()) {
            LOADER.execute(() -> onLogin(player));
        }
    }

    public static void onLogin(Player player) {
        if (player == null || !player.isPl()) {
            return;
        }
        player.suPhuId = -1;
        player.suDoMilestone = 0;
        player.deTuIds.clear();
        int pending = 0;
        List<Long> xuatSu = new ArrayList<>();
        try (Connection con = LocalManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT master_id, milestone FROM su_do WHERE disciple_id = ?")) {
                ps.setLong(1, player.id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(2) < MOC.length) {
                        player.suPhuId = rs.getLong(1);
                        player.suDoMilestone = rs.getInt(2);
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT disciple_id, milestone, pending_points FROM su_do WHERE master_id = ?")) {
                ps.setLong(1, player.id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        pending += rs.getInt(3);
                        if (rs.getInt(2) >= MOC.length) {
                            xuatSu.add(rs.getLong(1));
                        } else {
                            player.deTuIds.add(rs.getLong(1));
                        }
                    }
                }
            }
            if (pending > 0) {
                try (PreparedStatement ps = con.prepareStatement("UPDATE su_do SET pending_points = 0 WHERE master_id = ?")) {
                    ps.setLong(1, player.id);
                    ps.executeUpdate();
                }
            }
            for (Long id : xuatSu) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM su_do WHERE disciple_id = ? AND master_id = ? AND milestone >= ?")) {
                    ps.setLong(1, id);
                    ps.setLong(2, player.id);
                    ps.setInt(3, MOC.length);
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            Logger.logException(SuDoService.class, e, "Lỗi tải sư đồ: " + player.name);
            return;
        }
        if (pending > 0) {
            player.event.addEventPoint(pending);
            Service.gI().sendThongBao(player, "Đệ tử của bạn đạt mốc sức mạnh khi bạn vắng mặt: nhận " + pending + " điểm săn boss");
        }
        if (player.suPhuId > 0 || !player.deTuIds.isEmpty()) {
            active.add(player.id);
        }
    }

    // ===================== Buff cùng khu =====================
    // Gọi định kỳ (ServerManager): bật / tắt buff khi sư phụ và đệ tử cùng khu
    public static void updateBuffs() {
        for (Long id : active) {
            Player pl = Client.gI().getPlayer(id);
            if (pl == null) {
                active.remove(id);
                continue;
            }
            boolean buff = hasPartnerInZone(pl);
            if (!buff && !pl.suDoBuff && pl.suPhuId <= 0 && pl.deTuIds.isEmpty()) {
                active.remove(id); // đã rời / xuất sư
                continue;
            }
            if (buff != pl.suDoBuff) {
                pl.suDoBuff = buff;
                Service.gI().point(pl);
                Service.gI().sendThongBao(pl, buff
                        ? "Sư đồ cùng luyện tập: +" + BUFF_DAME + "% sức đánh, +" + BUFF_HP_KI + "% HP, KI"
                        : "Đã rời xa sư đồ, mất hiệu ứng sư đồ");
            }
        }
    }

    private static boolean hasPartnerInZone(Player pl) {
        if (pl.zone == null) {
            return false;
        }
        if (pl.suPhuId > 0 && sameZone(pl, Client.gI().getPlayer(pl.suPhuId))) {
            return true;
        }
        for (Long id : pl.deTuIds) {
            if (sameZone(pl, Client.gI().getPlayer(id))) {
                return true;
            }
        }
        return false;
    }

    private static boolean sameZone(Player a, Player b) {
        return b != null && b.zone != null && b.zone.equals(a.zone);
    }

    // ===================== Mốc sức mạnh =====================
    // Gọi từ NPoint.powerUp
    public static void onPowerUp(Player player) {
        if (player.suPhuId <= 0 || player.suDoMilestone >= MOC.length || player.nPoint.power < MOC[player.suDoMilestone]) {
            return;
        }
        int diem = 0;
        int from = player.suDoMilestone;
        while (player.suDoMilestone < MOC.length && player.nPoint.power >= MOC[player.suDoMilestone]) {
            diem += DIEM_MOC[player.suDoMilestone];
            player.suDoMilestone++;
        }
        long masterId = player.suPhuId;
        boolean xuatSu = player.suDoMilestone >= MOC.length;
        Player master = Client.gI().getPlayer(masterId);
        try {
            if (master != null) {
                master.event.addEventPoint(diem);
                LocalManager.executeUpdate("UPDATE su_do SET milestone = ? WHERE disciple_id = ?", player.suDoMilestone, player.id);
                Service.gI().sendThongBao(master, "Đệ tử " + player.name + " đạt " + ty(MOC[player.suDoMilestone - 1])
                        + " sức mạnh, bạn nhận " + diem + " điểm săn boss");
                if (xuatSu) {
                    LocalManager.executeUpdate("DELETE FROM su_do WHERE disciple_id = ?", player.id);
                    master.deTuIds.remove(player.id);
                }
            } else {
                LocalManager.executeUpdate("UPDATE su_do SET milestone = ?, pending_points = pending_points + ? WHERE disciple_id = ?",
                        player.suDoMilestone, diem, player.id);
            }
        } catch (Exception e) {
            Logger.logException(SuDoService.class, e, "Lỗi thưởng mốc sư đồ: " + player.name);
            player.suDoMilestone = from;
            return;
        }
        Logger.fileLog("su_do", player.name + " (" + player.id + ") đạt mốc " + player.suDoMilestone
                + ", sư phụ " + masterId + " nhận " + diem + " điểm");
        if (xuatSu) {
            player.suPhuId = -1;
            Service.gI().sendThongBaoOK(player, "Chúc mừng! Bạn đã đạt " + ty(MOC[MOC.length - 1])
                    + " sức mạnh và xuất sư.");
        }
    }

    // ===================== Menu =====================
    public static void openMenu(Npc npc, Player player) {
        StringBuilder say = new StringBuilder("Sư đồ: sư phụ (từ " + ty(MIN_POWER_SU_PHU) + " sức mạnh) nhận tối đa "
                + MAX_DE_TU + " đệ tử (dưới " + ty(MAX_POWER_DE_TU) + ").\n"
                + "Cùng khu: cả hai +" + BUFF_DAME + "% sức đánh, +" + BUFF_HP_KI + "% HP, KI.\n"
                + "Đệ tử đạt mốc, sư phụ nhận điểm săn boss:");
        for (int i = 0; i < MOC.length; i++) {
            say.append(i % 2 == 0 ? "\n" : " | ").append(ty(MOC[i])).append(": ").append(DIEM_MOC[i]).append(" điểm");
        }
        say.append(" (xuất sư)\n");
        if (player.suPhuId > 0) {
            Player master = Client.gI().getPlayer(player.suPhuId);
            say.append("\nSư phụ của bạn: ").append(master != null ? master.name + " (online)" : tenTuDb(player.suPhuId))
                    .append("\nMốc tiếp theo: ").append(ty(MOC[player.suDoMilestone]));
            npc.createOtherMenu(player, MENU_SU_DO, say.toString(), "Rời\nsư phụ", "Đóng");
        } else if (!player.deTuIds.isEmpty()) {
            say.append("\nĐệ tử (").append(player.deTuIds.size()).append("/").append(MAX_DE_TU).append("):");
            for (Long id : player.deTuIds) {
                Player dt = Client.gI().getPlayer(id);
                say.append("\n- ").append(dt != null ? dt.name + " " + ty(dt.nPoint.power) + " SM (online)" : tenTuDb(id));
            }
            npc.createOtherMenu(player, MENU_SU_DO, say.toString(), "Đuổi\nđệ tử", "Đóng");
        } else {
            npc.createOtherMenu(player, MENU_SU_DO, say.toString(), "Bái sư", "Đóng");
        }
    }

    // select 0 ở MENU_SU_DO
    public static void confirmMenu(Player player, int select) {
        if (select != 0) {
            return;
        }
        if (player.suPhuId > 0) {
            roiSuPhu(player);
        } else if (!player.deTuIds.isEmpty()) {
            Input.gI().createForm(player, Input.SU_DO_DUOI, "Đuổi đệ tử", new Input.SubInput("Tên đệ tử", Input.ANY));
        } else {
            if (player.nPoint.power >= MAX_POWER_DE_TU) {
                Service.gI().sendThongBao(player, "Chỉ bái sư được khi dưới " + ty(MAX_POWER_DE_TU) + " sức mạnh");
                return;
            }
            Input.gI().createForm(player, Input.SU_DO_BAI_SU, "Bái sư (sư phụ phải đang online)",
                    new Input.SubInput("Tên sư phụ", Input.ANY));
        }
    }

    private static String tenTuDb(long id) {
        try (Connection con = LocalManager.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT name FROM player WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) + " (offline)" : "?";
            }
        } catch (Exception e) {
            return "?";
        }
    }

    // ===================== Bái sư =====================
    public static void requestBaiSu(Player player, String tenSuPhu) {
        Player master = Client.gI().getPlayer(tenSuPhu == null ? "" : tenSuPhu.trim());
        String loi = checkBaiSu(player, master);
        if (loi != null) {
            Service.gI().sendThongBao(player, loi);
            return;
        }
        requests.put(master.id, new long[]{player.id, System.currentTimeMillis()});
        NpcService.gI().createMenuConMeo(master, MENU_XAC_NHAN_BAI_SU, -1,
                player.name + " (" + ty(player.nPoint.power) + " sức mạnh) muốn bái bạn làm sư phụ.\n"
                + "Đệ tử đạt mốc sức mạnh bạn sẽ nhận điểm săn boss, cùng khu cả hai được buff.",
                "Nhận\nđệ tử", "Từ chối");
        Service.gI().sendThongBao(player, "Đã gửi lời bái sư tới " + master.name + ", chờ sư phụ đồng ý");
    }

    private static String checkBaiSu(Player deTu, Player master) {
        if (master == null || !master.isPl()) {
            return "Sư phụ không tồn tại hoặc đang offline";
        }
        if (master.id == deTu.id) {
            return "Không thể tự bái mình làm sư phụ";
        }
        if (deTu.suPhuId > 0) {
            return "Bạn đã có sư phụ";
        }
        if (!deTu.deTuIds.isEmpty()) {
            return "Bạn đang là sư phụ, không thể bái sư";
        }
        if (deTu.nPoint.power >= MAX_POWER_DE_TU) {
            return "Chỉ bái sư được khi dưới " + ty(MAX_POWER_DE_TU) + " sức mạnh";
        }
        if (master.nPoint.power < MIN_POWER_SU_PHU) {
            return master.name + " chưa đủ " + ty(MIN_POWER_SU_PHU) + " sức mạnh để làm sư phụ";
        }
        if (master.suPhuId > 0) {
            return master.name + " đang là đệ tử của người khác";
        }
        if (master.deTuIds.size() >= MAX_DE_TU) {
            return master.name + " đã đủ " + MAX_DE_TU + " đệ tử";
        }
        return null;
    }

    // Sư phụ trả lời hộp thoại xác nhận (NpcFactory, Con mèo)
    public static void confirmBaiSu(Player master, int select) {
        long[] req = requests.remove(master.id);
        if (req == null || System.currentTimeMillis() - req[1] > REQUEST_TIMEOUT_MS) {
            Service.gI().sendThongBao(master, "Lời bái sư đã hết hạn");
            return;
        }
        Player deTu = Client.gI().getPlayer(req[0]);
        if (select != 0) {
            if (deTu != null) {
                Service.gI().sendThongBao(deTu, master.name + " đã từ chối nhận bạn làm đệ tử");
            }
            return;
        }
        if (deTu == null) {
            Service.gI().sendThongBao(master, "Người xin bái sư đã offline");
            return;
        }
        String loi = checkBaiSu(deTu, master);
        if (loi != null) {
            Service.gI().sendThongBao(master, loi);
            return;
        }
        // Mốc đã vượt trước khi bái sư không được thưởng
        int milestone = 0;
        while (milestone < MOC.length && deTu.nPoint.power >= MOC[milestone]) {
            milestone++;
        }
        try {
            // disciple_id là khoá chính: nếu đã có sư phụ (2 lời mời đồng ý cùng lúc) thì lỗi, không ghi đè
            LocalManager.executeUpdate("INSERT INTO su_do (disciple_id, master_id, milestone) VALUES (?, ?, ?)",
                    deTu.id, master.id, milestone);
        } catch (Exception e) {
            Service.gI().sendThongBao(master, "Không thể nhận đệ tử, vui lòng thử lại");
            return;
        }
        deTu.suPhuId = master.id;
        deTu.suDoMilestone = milestone;
        master.deTuIds.add(deTu.id);
        active.add(deTu.id);
        active.add(master.id);
        Logger.fileLog("su_do", master.name + " (" + master.id + ") nhận đệ tử " + deTu.name + " (" + deTu.id + ")");
        Service.gI().sendThongBao(master, "Đã nhận " + deTu.name + " làm đệ tử");
        Service.gI().sendThongBao(deTu, "Bái sư thành công! Sư phụ của bạn là " + master.name);
    }

    // ===================== Rời / đuổi =====================
    private static void roiSuPhu(Player deTu) {
        long masterId = deTu.suPhuId;
        try {
            LocalManager.executeUpdate("DELETE FROM su_do WHERE disciple_id = ?", deTu.id);
        } catch (Exception e) {
            Service.gI().sendThongBao(deTu, "Có lỗi, vui lòng thử lại");
            return;
        }
        deTu.suPhuId = -1;
        Player master = Client.gI().getPlayer(masterId);
        if (master != null) {
            master.deTuIds.remove(deTu.id);
            Service.gI().sendThongBao(master, deTu.name + " đã rời sư môn");
        }
        Logger.fileLog("su_do", deTu.name + " (" + deTu.id + ") rời sư phụ " + masterId);
        Service.gI().sendThongBao(deTu, "Bạn đã rời sư phụ");
    }

    public static void duoiDeTu(Player master, String tenDeTu) {
        String ten = tenDeTu == null ? "" : tenDeTu.trim();
        Long deTuId = null;
        for (Long id : master.deTuIds) {
            Player dt = Client.gI().getPlayer(id);
            String name = dt != null ? dt.name : tenTuDb(id).replace(" (offline)", "");
            if (name.equalsIgnoreCase(ten)) {
                deTuId = id;
                break;
            }
        }
        if (deTuId == null) {
            Service.gI().sendThongBao(master, "Không có đệ tử tên " + ten);
            return;
        }
        try {
            LocalManager.executeUpdate("DELETE FROM su_do WHERE disciple_id = ? AND master_id = ?", deTuId, master.id);
        } catch (Exception e) {
            Service.gI().sendThongBao(master, "Có lỗi, vui lòng thử lại");
            return;
        }
        master.deTuIds.remove(deTuId);
        Player dt = Client.gI().getPlayer(deTuId);
        if (dt != null) {
            dt.suPhuId = -1;
            Service.gI().sendThongBao(dt, master.name + " đã cho bạn rời sư môn");
        }
        Logger.fileLog("su_do", master.name + " (" + master.id + ") đuổi đệ tử " + deTuId);
        Service.gI().sendThongBao(master, "Đã đuổi " + ten + " khỏi sư môn");
    }
}
