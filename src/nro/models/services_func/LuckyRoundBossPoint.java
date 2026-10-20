package nro.models.services_func;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * Vòng quay may mắn bằng điểm săn boss (Thượng Đế -> Quay ngọc May mắn -> Quay bằng điểm săn boss).
 * Mỗi lượt tốn PRICE điểm săn boss, đồ nhận vào rương phụ (itemsBoxCrackBall) như vòng quay có sẵn.
 * Mọi phần thưởng đều khoá giao dịch (option 30).
 */
public final class LuckyRoundBossPoint {

    public static final int PRICE = 50;
    private static final int MAX_ITEM_IN_BOX = 100;

    // {tỉ lệ (phần nghìn), loại}: tổng = 1000
    private static final int THOI_VANG_20 = 0, THOI_VANG_50 = 1, DA_NANG_CAP = 2, THUOC_2 = 3, DA_BAO_VE = 4,
            THUC_AN = 5, NGOC_RONG = 6, THOI_VANG_200 = 7, NGOC_RONG_1_SAO = 8, DO_THAN_LINH = 9;
    private static final int[][] RATE = {
        {300, THOI_VANG_20},
        {150, THOI_VANG_50},
        {150, DA_NANG_CAP},
        {120, THUOC_2},
        {80, DA_BAO_VE},
        {80, THUC_AN},
        {50, NGOC_RONG},
        {40, THOI_VANG_200},
        {15, NGOC_RONG_1_SAO},
        {15, DO_THAN_LINH}
    };

    // Đồ Thần Linh theo hành tinh: áo, quần, găng, giày + nhẫn (dùng chung)
    private static final int[][] DO_THAN_LINH_IDS = {
        {555, 556, 562, 563, 561},
        {557, 558, 564, 565, 561},
        {559, 560, 566, 567, 561}
    };

    private LuckyRoundBossPoint() {
    }

    // Tên từng loại thưởng (theo chỉ số loại) để hiện bảng tỉ lệ, luôn khớp với RATE
    private static final String[] TEN_LOAI = {"20 thỏi vàng", "50 thỏi vàng", "5 đá nâng cấp", "thuốc cấp 2", "Đá bảo vệ",
        "10 thức ăn", "Ngọc rồng 3-7 sao", "200 thỏi vàng", "Ngọc rồng 1 sao", "Đồ Thần Linh"};
    private static final String BANG_TI_LE = buildBangTiLe();

    private static String buildBangTiLe() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < RATE.length; i++) {
            int phanNghin = RATE[i][0];
            String tiLe = phanNghin % 10 == 0 ? String.valueOf(phanNghin / 10) : (phanNghin / 10) + "," + (phanNghin % 10);
            sb.append(i % 2 == 0 ? "\n" : " | ").append(tiLe).append("% ").append(TEN_LOAI[RATE[i][1]]);
        }
        return sb.toString();
    }

    public static String info(Player player) {
        return "Mỗi lượt quay tốn " + PRICE + " điểm săn boss (đang có " + player.event.getEventPoint() + ")\n"
                + "Đồ nhận được vào Rương phụ, khoá giao dịch." + BANG_TI_LE;
    }

    private static int countItems(Player player) {
        List<Item> box = player.inventory.itemsBoxCrackBall;
        return box.size() - InventoryService.gI().getCountEmptyListItem(box);
    }

    public static void spin(Player player, int count) {
        if (count <= 0) {
            return;
        }
        int need = PRICE * count;
        if (player.event.getEventPoint() < need) {
            Service.gI().sendThongBao(player, "Cần " + need + " điểm săn boss để quay " + count + " lần, hiện có "
                    + player.event.getEventPoint());
            return;
        }
        if (countItems(player) + count > MAX_ITEM_IN_BOX) {
            Service.gI().sendThongBao(player, "Rương phụ không đủ chỗ, hãy nhận bớt đồ trong rương phụ trước");
            return;
        }
        player.event.setEventPoint(player.event.getEventPoint() - need);
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            items.add(randomReward(player));
        }
        player.inventory.itemsBoxCrackBall.addAll(items);

        // Gộp tên + số lượng để thông báo gọn
        Map<String, Integer> tong = new LinkedHashMap<>();
        for (Item it : items) {
            tong.merge(it.template.name, it.quantity, Integer::sum);
        }
        StringBuilder sb = new StringBuilder("Quay " + count + " lần nhận được (đã vào Rương phụ):");
        tong.forEach((ten, sl) -> sb.append("\n").append(sl).append(" ").append(ten));
        sb.append("\n\nCòn ").append(player.event.getEventPoint()).append(" điểm săn boss");
        Service.gI().sendThongBaoOK(player, sb.toString());
    }

    private static int randomLoai() {
        int r = Util.nextInt(0, 999);
        for (int[] rate : RATE) {
            r -= rate[0];
            if (r < 0) {
                return rate[1];
            }
        }
        return THOI_VANG_20;
    }

    private static Item create(int id, int quantity) {
        Item it = ItemService.gI().createNewItem((short) id);
        it.quantity = quantity;
        it.itemOptions.add(new Item.ItemOption(30, 0));
        return it;
    }

    private static Item randomReward(Player player) {
        switch (randomLoai()) {
            case THOI_VANG_50:
                return create(457, 50);
            case DA_NANG_CAP:
                return create(Util.nextInt(220, 224), 5);
            case THUOC_2:
                return create(Util.nextInt(1150, 1154), 1);
            case DA_BAO_VE:
                return create(987, 1);
            case THUC_AN:
                return create(Util.nextInt(663, 667), 10);
            case NGOC_RONG:
                return create(Util.nextInt(16, 20), 1);
            case THOI_VANG_200:
                return create(457, 200);
            case NGOC_RONG_1_SAO:
                return create(14, 1);
            case DO_THAN_LINH: {
                int g = Math.max(0, Math.min(DO_THAN_LINH_IDS.length - 1, player.gender));
                int id = DO_THAN_LINH_IDS[g][Util.nextInt(0, DO_THAN_LINH_IDS[g].length - 1)];
                Item it = ItemService.gI().createNewItem((short) id);
                it.itemOptions.addAll(ItemService.gI().getDefaultOptionTL(id));
                it.itemOptions.add(new Item.ItemOption(30, 0));
                return it;
            }
            default:
                return create(457, 20);
        }
    }
}
