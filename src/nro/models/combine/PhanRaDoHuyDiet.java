package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.map.service.NpcManager;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * Berus (Hành tinh Kaio) - Phân rã Huỷ Diệt: 1 món Huỷ Diệt (650 - 662) đổi lấy 30 - 60 phần
 * của 1 loại thức ăn ngẫu nhiên (663 - 667). Đồ Huỷ Diệt mất sau khi phân rã.
 */
public class PhanRaDoHuyDiet {

    private static final int THUC_AN_MIN_ID = 663, THUC_AN_MAX_ID = 667;
    private static final int SO_LUONG_MIN = 30, SO_LUONG_MAX = 60;

    public static boolean isDoHuyDiet(Item item) {
        return item != null && item.template != null && item.template.id >= 650 && item.template.id <= 662;
    }

    private static Item getItem(Player player) {
        if (player.combineNew == null || player.combineNew.itemsCombine.size() != 1) {
            return null;
        }
        Item item = player.combineNew.itemsCombine.get(0);
        // Phải còn trong hành trang: đặt vào ô ghép rồi cất rương / mặc lên người thì subQuantityItemsBag
        // không trừ được gì mà vẫn nhận thức ăn
        return isDoHuyDiet(item) && item.quantity > 0 && player.inventory.itemsBag.contains(item) ? item : null;
    }

    public static void showInfoCombine(Player player) {
        Item item = getItem(player);
        if (item == null) {
            Service.gI().sendThongBaoOK(player, "Cần đúng 1 món Huỷ Diệt để phân rã");
            return;
        }
        String info = "Phân rã " + item.template.name + "\n"
                + "nhận " + SO_LUONG_MIN + " - " + SO_LUONG_MAX + " phần thức ăn ngẫu nhiên.\n"
                + "Món Huỷ Diệt sẽ mất sau khi phân rã!";
        NpcManager.getNpc(ConstNpc.BILL).createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                info, "Phân rã", "Huỷ");
    }

    public static void startCombine(Player player) {
        Item item = getItem(player);
        if (item == null) {
            Service.gI().sendThongBao(player, "Cần đúng 1 món Huỷ Diệt để phân rã");
            return;
        }
        // Đồ Huỷ Diệt bị trừ trước nên luôn còn ít nhất 1 ô trống cho thức ăn
        String tenDo = item.template.name;
        InventoryService.gI().subQuantityItemsBag(player, item, 1);

        Item thucAn = ItemService.gI().createNewItem((short) Util.nextInt(THUC_AN_MIN_ID, THUC_AN_MAX_ID));
        int soLuong = Util.nextInt(SO_LUONG_MIN, SO_LUONG_MAX);
        thucAn.quantity = soLuong;
        InventoryService.gI().addItemBag(player, thucAn);
        InventoryService.gI().sendItemBags(player);

        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendThongBao(player, "Phân rã " + tenDo + " nhận được " + soLuong + " " + thucAn.template.name);
        player.combineNew.clearItemCombine();
        CombineService.gI().reOpenItemCombine(player);
    }
}
