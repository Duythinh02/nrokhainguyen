package nro.models.utils;

/**
 * Đọc công tắc true/false trong Config.properties (đọc mỗi lần gọi, đổi file không cần khởi động lại).
 */
public class GameConfig {

    public static boolean isOn(String key, boolean defaultValue) {
        String v = get(key);
        return v == null ? defaultValue : "true".equalsIgnoreCase(v.trim());
    }

    public static String get(String key) {
        try (java.io.FileInputStream f = new java.io.FileInputStream("Config.properties")) {
            java.util.Properties pr = new java.util.Properties();
            pr.load(f);
            return pr.getProperty(key);
        } catch (Exception e) {
            return null;
        }
    }

    // Tài khoản không hiện trên các bảng xếp hạng (top.hidden_accounts=tk1,tk2)
    public static java.util.List<String> topHiddenAccounts() {
        java.util.List<String> accounts = new java.util.ArrayList<>();
        String v = get("top.hidden_accounts");
        if (v != null) {
            for (String s : v.split(",")) {
                if (!s.isBlank()) {
                    accounts.add(s.trim());
                }
            }
        }
        return accounts;
    }

    // Tài xỉu (Mèo Đỏ Đen) và Chẵn lẻ (Vua Cờ Bạc)
    public static boolean taiXiuChanLeOn() {
        return isOn("minigame.taixiu_chanle.enabled", false);
    }
}
