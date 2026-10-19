package nro.models.combine;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;

/**
 *
 * @author By Mr Blue
 */

public class NhapNgocRong {

    // Ngọc rồng 2-7 sao (id 15-20), đủ ít nhất 7 viên
    private static boolean nhapDuoc(Item item) {
        return item != null && item.isNotNullItem() && item.template.id > 14 && item.template.id <= 20 && item.quantity >= 7;
    }

    private static Item itemNhap(Player player) {
        return player.combineNew.itemsCombine.size() == 1 ? player.combineNew.itemsCombine.get(0) : null;
    }

    public static void showInfoCombine(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
            return;
        }
        Item item = itemNhap(player);
        if (!nhapDuoc(item)) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần 7 viên ngọc rồng 2 sao trở lên", "Đóng");
            return;
        }
        String tenMoi = ItemService.gI().getTemplate((short) (item.template.id - 1)).name;
        int soLan = item.quantity / 7;
        String npcSay = "|2|Con có muốn biến 7 " + item.template.name + " thành\n"
                + "1 viên " + tenMoi + "\n"
                + "|7|Cần 7 " + item.template.name;
        if (soLan > 1) {
            npcSay += "\n|1|Nhập nhanh: " + (soLan * 7) + " viên -> " + soLan + " " + tenMoi;
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    "Làm phép", "Nhập nhanh\nx" + soLan, "Từ chối");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép", "Từ chối");
        }
    }

    public static void nhapNgocRong(Player player) {
        nhap(player, false);
    }

    // Nhập hết 1 lần: N viên -> N/7 viên ngọc rồng sao thấp hơn, phần dư giữ lại
    public static void nhapNgocRongNhanh(Player player) {
        nhap(player, true);
    }

    private static void nhap(Player player, boolean nhapHet) {
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
            return;
        }
        Item item = itemNhap(player);
        // Nút "Nhập nhanh" chỉ có khi >= 14 viên; dưới mức đó nút thứ 2 là "Từ chối"
        if (!nhapDuoc(item) || (nhapHet && item.quantity < 14)) {
            return;
        }
        int soLan = nhapHet ? item.quantity / 7 : 1;
        CombineService.gI().sendEffectCombineDB(player, item.template.iconID);
        Item nr = ItemService.gI().createNewItem((short) (item.template.id - 1), soLan);
        String tenMoi = nr.template.name;
        InventoryService.gI().addItemBag(player, nr);
        InventoryService.gI().subQuantityItemsBag(player, item, soLan * 7);
        InventoryService.gI().sendItemBags(player);
        if (nhapHet) {
            Service.gI().sendThongBao(player, "Đã nhập " + (soLan * 7) + " viên thành " + soLan + " " + tenMoi);
        }
        CombineService.gI().reOpenItemCombine(player);
    }

}
