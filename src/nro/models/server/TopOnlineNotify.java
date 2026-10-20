package nro.models.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nro.models.data.LocalManager;
import nro.models.player.Player;
import nro.models.utils.Logger;

/**
 * Người trong bảng xếp hạng (giống web bxh.php) đăng nhập thì chạy chữ cuối màn:
 * "Top 1 Nhiệm Vụ, Top 2 Sức Mạnh laze1hp đã online".
 * Bảng xếp hạng đọc lại từ DB mỗi REFRESH_MINUTES phút (dữ liệu theo lần lưu gần nhất của người chơi).
 */
public final class TopOnlineNotify {

    // Báo cho top 1 -> TOP_N của mỗi bảng
    private static final int TOP_N = 3;
    public static final int REFRESH_MINUTES = 5;
    // Thoát vào lại liên tục không báo lại trong khoảng này
    private static final long NOTIFY_COOLDOWN_MS = 10 * 60_000L;

    private static final String FILTER = " FROM player INNER JOIN account ON account.id = player.account_id"
            + " WHERE account.is_admin = 0 AND account.ban = 0";

    private static final String[] TEN_BANG = {"Nhiệm Vụ", "Sức Mạnh", "Đại Gia", "Săn Boss", "Sức Mạnh Đệ"};
    private static final String[] QUERY = {
        // Nhiệm vụ: nhiệm vụ chính, bước, số lượng
        "SELECT player.id" + FILTER + " ORDER BY"
        + " CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(data_task, ',', 1), '[', -1) AS UNSIGNED) DESC,"
        + " CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(data_task, ',', 2), ',', -1) AS UNSIGNED) DESC,"
        + " CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(data_task, ',', 3), ',', -1) AS UNSIGNED) DESC LIMIT " + TOP_N,
        // Sức mạnh
        "SELECT player.id" + FILTER
        + " ORDER BY CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(data_point, ',', 2), ',', -1) AS UNSIGNED) DESC LIMIT " + TOP_N,
        // Đại gia: tính Coin Khai Nguyên trong Java (xem topDaiGia)
        null,
        // Săn boss
        "SELECT player.id" + FILTER + " AND player.boss_kill > 0"
        + " ORDER BY player.event_point_boss DESC, player.boss_kill DESC LIMIT " + TOP_N,
        // Sức mạnh đệ tử
        "SELECT player.id" + FILTER + " AND JSON_VALID(player.pet) AND JSON_LENGTH(player.pet) >= 2"
        + " ORDER BY CAST(JSON_VALUE(JSON_UNQUOTE(JSON_EXTRACT(player.pet, '$[1]')), '$[1]') AS UNSIGNED) DESC LIMIT " + TOP_N
    };

    // Coin Khai Nguyên = item 2691; mỗi phần tử items_bag / items_box là chuỗi "[id,số lượng,...]"
    private static final Pattern COIN_KHAI_NGUYEN = Pattern.compile("\"\\[\\s*2691\\s*,\\s*(\\d+)");

    // id người chơi -> danh hiệu, vd "Top 1 Nhiệm Vụ, Top 2 Sức Mạnh"
    private static volatile Map<Long, String> titles = Collections.emptyMap();
    private static final Map<Long, Long> lastNotify = new ConcurrentHashMap<>();

    private TopOnlineNotify() {
    }

    // id top 1 -> TOP_N của từng bảng (chỉ số theo TEN_BANG)
    private static List<List<Long>> computeTop(Connection con) throws Exception {
        List<List<Long>> result = new ArrayList<>();
        for (int b = 0; b < TEN_BANG.length; b++) {
            result.add(QUERY[b] != null ? topIds(con, QUERY[b]) : topDaiGia(con));
        }
        return result;
    }

    public static void refresh() {
        Map<Long, StringBuilder> map = new java.util.LinkedHashMap<>();
        try (Connection con = LocalManager.getConnection()) {
            List<List<Long>> tops = computeTop(con);
            for (int b = 0; b < tops.size(); b++) {
                List<Long> ids = tops.get(b);
                for (int r = 0; r < ids.size(); r++) {
                    StringBuilder sb = map.computeIfAbsent(ids.get(r), k -> new StringBuilder());
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append("Top ").append(r + 1).append(' ').append(TEN_BANG[b]);
                }
            }
        } catch (Exception e) {
            Logger.logException(TopOnlineNotify.class, e, "Lỗi đọc bảng xếp hạng thông báo online");
            return;
        }
        Map<Long, String> result = new ConcurrentHashMap<>();
        map.forEach((id, sb) -> result.put(id, sb.toString()));
        titles = result;
    }

    private static List<Long> topIds(Connection con, String query) throws Exception {
        List<Long> ids = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getLong(1));
            }
        }
        return ids;
    }

    // Nhiều coin nhất trước, bằng coin thì xét điểm săn boss (giống bxh.php)
    private static List<Long> topDaiGia(Connection con) throws Exception {
        List<long[]> rows = new ArrayList<>(); // {id, coin, điểm boss}
        try (PreparedStatement ps = con.prepareStatement("SELECT player.id, player.event_point_boss, player.items_bag, player.items_box"
                + FILTER + " AND (player.items_bag LIKE '%[2691,%' OR player.items_box LIKE '%[2691,%')");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long coin = demCoin(rs.getString(3)) + demCoin(rs.getString(4));
                if (coin > 0) {
                    rows.add(new long[]{rs.getLong(1), coin, rs.getLong(2)});
                }
            }
        }
        rows.sort((x, y) -> x[1] != y[1] ? Long.compare(y[1], x[1]) : Long.compare(y[2], x[2]));
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < rows.size() && i < TOP_N; i++) {
            ids.add(rows.get(i)[0]);
        }
        return ids;
    }

    private static long demCoin(String json) {
        if (json == null) {
            return 0;
        }
        long tong = 0;
        Matcher m = COIN_KHAI_NGUYEN.matcher(json);
        while (m.find()) {
            tong += Long.parseLong(m.group(1));
        }
        return tong;
    }

    public static void onLogin(Player player) {
        if (player == null || player.isBot) {
            return;
        }
        String title = titles.get(player.id);
        if (title == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = lastNotify.get(player.id);
        if (last != null && now - last < NOTIFY_COOLDOWN_MS) {
            return;
        }
        lastNotify.put(player.id, now);
        ServerNotify.gI().notify(title + " " + player.name + " đã online");
    }
}
