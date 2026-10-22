package nro.models.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.map.service.NpcService;
import nro.models.player.NPoint;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.utils.Util;

/**
 * Cộng tiềm năng cho đệ tử giống sư phụ: người chơi tự chọn chỉ số và số lần cộng (x1 / x10 / x100).
 * Mở bằng chat "cong de" hoặc nút "Cộng tiềm năng đệ tử" ở Ông Gohan / Moori / Paragus.
 * Giới hạn chỉ số vẫn theo bậc giới hạn sức mạnh đệ tử đã mở khóa (NPoint.getMax*Goc).
 * Đệ tử không còn tự cộng ngẫu nhiên.
 */
public final class PetPointService {

    // Hộp thoại Con mèo, xử lý ở NpcFactory
    public static final int MENU_CHON_CHI_SO = 7905;
    public static final int MENU_CHON_SO_LAN = 7906;

    private static final byte HP = 0, KI = 1, SUC_DANH = 2, GIAP = 3, CHI_MANG = 4;
    private static final String[] TEN = {"HP", "KI", "Sức đánh", "Giáp", "Chí mạng"};
    private static final int[] SO_LAN = {1, 10, 100};

    // id người chơi -> chỉ số đang chọn (chờ chọn số lần)
    private static final Map<Long, Byte> dangChon = new ConcurrentHashMap<>();

    private PetPointService() {
    }

    private static Pet getPet(Player player) {
        if (player == null || player.pet == null || player.pet.nPoint == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có đệ tử");
            return null;
        }
        return player.pet;
    }

    // Tiềm năng cần cho lần cộng kế tiếp (cùng công thức NPoint.applyPoint với point = 1)
    private static long chiPhi(NPoint np, byte type) {
        switch (type) {
            case HP:
                return np.hpg + 1000L;
            case KI:
                return np.mpg + 1000L;
            case SUC_DANH:
                return np.dameg * 100L;
            case GIAP:
                return (np.defg + 5L) * 100000;
            default:
                long tn = 50000000L;
                for (int i = 0; i < np.critg; i++) {
                    tn *= 5L;
                }
                return tn;
        }
    }

    private static String dong(NPoint np, byte type, long cur, long max) {
        return "\n" + TEN[type] + ": " + Util.numberToMoney(cur) + "/" + Util.numberToMoney(max)
                + (cur >= max ? " (tối đa)" : " - cần " + Util.numberToMoney(chiPhi(np, type)) + " TN");
    }

    public static void openMenu(Player player) {
        Pet pet = getPet(player);
        if (pet == null) {
            return;
        }
        NPoint np = pet.nPoint;
        String say = "Cộng tiềm năng cho đệ tử " + pet.name
                + "\nTiềm năng: " + Util.numberToMoney(np.tiemNang)
                + dong(np, HP, np.hpg, np.getMaxHpMpGoc())
                + dong(np, KI, np.mpg, np.getMaxHpMpGoc())
                + dong(np, SUC_DANH, np.dameg, np.getMaxDameGoc())
                + dong(np, GIAP, np.defg, np.getMaxDefGoc())
                + dong(np, CHI_MANG, np.critg, np.getMaxCritGoc());
        NpcService.gI().createMenuConMeo(player, MENU_CHON_CHI_SO, -1, say,
                "HP\n+20", "KI\n+20", "Sức đánh\n+1", "Giáp\n+1", "Chí mạng\n+1", "Đóng");
    }

    // Trả lời MENU_CHON_CHI_SO
    public static void chonChiSo(Player player, int select) {
        if (select < HP || select > CHI_MANG) {
            return;
        }
        byte type = (byte) select;
        if (type == GIAP || type == CHI_MANG) {
            // Giống sư phụ: giáp / chí mạng mỗi lần chỉ cộng 1
            cong(player, type, 1);
            return;
        }
        Pet pet = getPet(player);
        if (pet == null) {
            return;
        }
        dangChon.put(player.id, type);
        NpcService.gI().createMenuConMeo(player, MENU_CHON_SO_LAN, -1,
                "Cộng " + TEN[type] + " cho đệ tử\nTiềm năng: " + Util.numberToMoney(pet.nPoint.tiemNang),
                "x1", "x10", "x100", "Quay lại");
    }

    // Trả lời MENU_CHON_SO_LAN
    public static void chonSoLan(Player player, int select) {
        Byte type = dangChon.remove(player.id);
        if (type == null) {
            return;
        }
        if (select >= 0 && select < SO_LAN.length) {
            cong(player, type, SO_LAN[select]);
        } else if (select == SO_LAN.length) {
            openMenu(player);
        }
    }

    /**
     * Gói tin cộng tiềm năng đệ tử từ client (sub command 18, cùng dạng với 16 của sư phụ).
     * soLan: 1 / 10 / 100 như bảng chỉ số; cộng từng lần, hết tiềm năng / chạm max thì giữ phần đã cộng.
     * Chỉ báo khi không cộng được để chế độ tự cộng của mod không bị spam thông báo.
     */
    public static void congTuClient(Player player, byte type, int soLan) {
        if (player == null || player.pet == null || player.pet.nPoint == null
                || type < HP || type > CHI_MANG || soLan <= 0 || soLan > 1000) {
            return;
        }
        Pet pet = player.pet;
        NPoint np = pet.nPoint;
        int daCong = 0;
        int ketQua = NPoint.POINT_OK;
        for (int i = 0; i < soLan; i++) {
            ketQua = np.applyPoint(type, (short) 1);
            if (ketQua != NPoint.POINT_OK) {
                break;
            }
            daCong++;
        }
        if (daCong > 0) {
            Service.gI().point(pet);
        }
        // Luôn gửi lại để client cập nhật tiềm năng / chỉ số gốc (tự cộng dựa vào đây để dừng)
        Service.gI().showInfoPet(player);
        if (daCong == 0) {
            Service.gI().sendThongBao(player, ketQua == NPoint.POINT_CAPPED
                    ? "Đệ tử: " + TEN[type] + " đã đạt tối đa của bậc hiện tại"
                    : "Đệ tử không đủ tiềm năng để cộng " + TEN[type]);
        }
    }

    private static void cong(Player player, byte type, int soLan) {
        Pet pet = getPet(player);
        if (pet == null) {
            return;
        }
        NPoint np = pet.nPoint;
        int daCong = 0;
        int ketQua = NPoint.POINT_OK;
        // Cộng từng lần: hết tiềm năng hoặc chạm max giữa chừng thì giữ phần đã cộng
        for (int i = 0; i < soLan; i++) {
            ketQua = np.applyPoint(type, (short) 1);
            if (ketQua != NPoint.POINT_OK) {
                break;
            }
            daCong++;
        }
        if (daCong > 0) {
            Service.gI().point(pet);
            Service.gI().showInfoPet(player);
        }
        String tb = daCong > 0 ? "Đã cộng " + daCong + " lần " + TEN[type] + " cho đệ tử" : "Chưa cộng được " + TEN[type];
        if (ketQua == NPoint.POINT_CAPPED) {
            tb += " (đã đạt tối đa của bậc hiện tại)";
        } else if (ketQua == NPoint.POINT_FAIL) {
            tb += " (đệ tử không đủ tiềm năng)";
        }
        Service.gI().sendThongBao(player, tb);
        openMenu(player);
    }
}
