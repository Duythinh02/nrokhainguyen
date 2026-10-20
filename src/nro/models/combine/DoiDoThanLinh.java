package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * Bà Hạt Mít (Đảo Kame) - Đổi đồ Thần Linh: 1 món Thần Linh đổi ngẫu nhiên sang 1 món Thần Linh cùng hành tinh (có thể ra lại đúng loại cũ).
 * Đủ 5 món: áo 25%, quần 20%, găng 15%, giày 25%, nhẫn 15%, cố định cho mọi món đặt vào.
 * Nhẫn Thần Linh (561) dùng chung 3 hành tinh: đổi nhẫn thì ra đồ theo hành tinh của nhân vật.
 * Đồ mới nhận chỉ số gốc mới, giữ số ô sao pha lê (107) và khoá giao dịch (30) của đồ cũ.
 */
public class DoiDoThanLinh {

    // {áo, quần, găng, giày, nhẫn} theo hành tinh: Trái Đất, Namếc, Xayda
    private static final int[][] DO_THAN_LINH = {
        {555, 556, 562, 563, 561},
        {557, 558, 564, 565, 561},
        {559, 560, 566, 567, 561}
    };
    // Tỉ lệ ra theo loại {áo, quần, găng, giày, nhẫn}
    private static final int[] TI_LE = {25, 20, 15, 25, 15};
    private static final String[] TEN_LOAI = {"Áo", "Quần", "Găng", "Giày", "Nhẫn"};
    private static final int NHAN_THAN_LINH = 561, LOAI_NHAN = 4;

    // {hành tinh, loại} của món đồ, null nếu không đổi được
    private static int[] find(Item item, Player player) {
        if (item == null || item.template == null) {
            return null;
        }
        if (item.template.id == NHAN_THAN_LINH) {
            return new int[]{Math.max(0, Math.min(DO_THAN_LINH.length - 1, player.gender)), LOAI_NHAN};
        }
        for (int g = 0; g < DO_THAN_LINH.length; g++) {
            for (int t = 0; t < DO_THAN_LINH[g].length; t++) {
                if (DO_THAN_LINH[g][t] == item.template.id) {
                    return new int[]{g, t};
                }
            }
        }
        return null;
    }

    private static Item getItem(Player player) {
        if (player.combineNew == null || player.combineNew.itemsCombine.size() != 1) {
            return null;
        }
        return player.combineNew.itemsCombine.get(0);
    }

    // Phí mỗi lần đổi
    private static final int PHI_DIEM_BOSS = 20;
    private static final int THOI_VANG_ID = 457, PHI_THOI_VANG = 10;

    private static int demThoiVang(Player player) {
        int count = 0;
        for (Item it : player.inventory.itemsBag) {
            if (it != null && it.isNotNullItem() && it.template.id == THOI_VANG_ID) {
                count += it.quantity;
            }
        }
        return count;
    }

    private static void truThoiVang(Player player, int soLuong) {
        for (int i = player.inventory.itemsBag.size() - 1; i >= 0 && soLuong > 0; i--) {
            Item it = player.inventory.itemsBag.get(i);
            if (it != null && it.isNotNullItem() && it.template.id == THOI_VANG_ID) {
                int sub = Math.min(soLuong, it.quantity);
                InventoryService.gI().subQuantityItemsBag(player, it, sub);
                soLuong -= sub;
            }
        }
    }

    // Lý do không đủ phí, null nếu đủ
    private static String checkPhi(Player player) {
        if (player.event.getEventPoint() < PHI_DIEM_BOSS) {
            return "Cần " + PHI_DIEM_BOSS + " điểm săn boss, hiện có " + player.event.getEventPoint();
        }
        int thoiVang = demThoiVang(player);
        if (thoiVang < PHI_THOI_VANG) {
            return "Cần " + PHI_THOI_VANG + " thỏi vàng, hiện có " + thoiVang;
        }
        return null;
    }

    private static int tongTiLe() {
        int tong = 0;
        for (int tiLe : TI_LE) {
            tong += tiLe;
        }
        return tong;
    }

    private static String tiLeText() {
        int tong = tongTiLe();
        StringBuilder sb = new StringBuilder();
        for (int t = 0; t < TI_LE.length; t++) {
            sb.append(TEN_LOAI[t]).append(": ").append(TI_LE[t] * 100 / tong).append("%\n");
        }
        return sb.toString();
    }

    public static void showInfoCombine(Player player) {
        Item item = getItem(player);
        int[] pos = find(item, player);
        if (pos == null) {
            Service.gI().sendThongBaoOK(player, "Cần 1 món Thần Linh (áo, quần, găng, giày, nhẫn) để đổi");
            return;
        }
        String info = "Đổi " + item.template.name + " lấy ngẫu nhiên 1 món\n"
                + "Thần Linh cùng hành tinh (có thể ra lại món cũ):\n"
                + tiLeText()
                + "Đồ mới nhận chỉ số mới, giữ số ô sao pha lê.\n"
                + "Sao pha lê đã ép trên đồ cũ sẽ mất!\n"
                + "Phí: " + PHI_DIEM_BOSS + " điểm săn boss (có " + player.event.getEventPoint() + ")"
                + " + " + PHI_THOI_VANG + " thỏi vàng (có " + demThoiVang(player) + ")";
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, info, "Đổi", "Huỷ");
    }

    public static void startCombine(Player player) {
        Item item = getItem(player);
        int[] pos = find(item, player);
        if (pos == null || item.quantity < 1 || !player.inventory.itemsBag.contains(item)) {
            Service.gI().sendThongBao(player, "Cần 1 món Thần Linh (áo, quần, găng, giày, nhẫn) để đổi");
            return;
        }
        String loiPhi = checkPhi(player);
        if (loiPhi != null) {
            Service.gI().sendThongBao(player, loiPhi);
            return;
        }
        player.event.setEventPoint(player.event.getEventPoint() - PHI_DIEM_BOSS);
        truThoiVang(player, PHI_THOI_VANG);
        int loaiMoi = randomLoai();
        int idMoi = DO_THAN_LINH[pos[0]][loaiMoi];

        Item moi = ItemService.gI().createNewItem((short) idMoi);
        moi.itemOptions.addAll(ItemService.gI().getDefaultOptionTL(idMoi));
        for (ItemOption io : item.itemOptions) {
            int id = io.optionTemplate.id;
            if (id == 107 || id == 30) {
                moi.itemOptions.add(new ItemOption(id, io.param));
            }
        }

        InventoryService.gI().subQuantityItemsBag(player, item, 1);
        InventoryService.gI().addItemBag(player, moi);
        InventoryService.gI().sendItemBags(player);
        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendThongBao(player, "Bạn nhận được " + moi.template.name);
        player.combineNew.clearItemCombine();
        CombineService.gI().reOpenItemCombine(player);
    }

    // Chọn loại mới theo TI_LE, có thể ra lại đúng loại cũ
    private static int randomLoai() {
        int r = Util.nextInt(0, tongTiLe() - 1);
        for (int t = 0; t < TI_LE.length; t++) {
            r -= TI_LE[t];
            if (r < 0) {
                return t;
            }
        }
        return 0;
    }
}
