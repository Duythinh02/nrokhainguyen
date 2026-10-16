package nro.models.services;

import nro.models.managers.GiftCodeManager;
import nro.models.player_system.GiftCode;
import nro.models.item.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nro.models.player.Player;
import nro.models.map.service.NpcService;
import nro.models.shop.ItemShop;
import nro.models.shop.Shop;

/**
 *
 * @author By Mr Blue
 * 
 */

public class GiftCodeService {

    private static GiftCodeService instance;

    public static GiftCodeService gI() {
        if (instance == null) {
            instance = new GiftCodeService();
        }
        return instance;
    }

    public void giftCode(Player player, String code) {
        // checkUseGiftCode tự báo lý do (sai code, hết hạn, hết lượt, đã nhập, thiếu ô) khi trả về null
        GiftCode giftcode = GiftCodeManager.gI().checkUseGiftCode(player, code);
        if (giftcode != null) {
            Set<Integer> keySet = giftcode.detail.keySet();
            String textGift = "|0|Bạn vừa nhận được:\b";
            for (Integer key : keySet) {
                int idItem = key;
                int quantity = giftcode.detail.get(key);

                switch (idItem) {
                    case -1 -> {
                        player.inventory.gold = Math.min(player.inventory.gold + (long) quantity, 2000000000L);
                        textGift += "|2|" + quantity + " vàng\b";
                    }
                    case -2 -> {
                        player.inventory.gem = Math.min(player.inventory.gem + quantity, 200000000);
                        textGift += "|3|" + quantity + " ngọc\b";
                    }
                    case -3 -> {
                        player.inventory.ruby = Math.min(player.inventory.ruby + quantity, 200000000);
                        textGift += "|4|" + quantity + " ngọc khóa\b";
                    }
                    default -> {
                        Item itemGiftTemplate = ItemService.gI().createNewItem((short) idItem);
                        if (itemGiftTemplate != null) {
                            Item itemGift = new Item((short) idItem);
                            // Bản sao options riêng cho từng người nhận; không có options thì dùng list rỗng (null làm lỗi khi thêm vào hành trang)
                            List<Item.ItemOption> giftOptions = new ArrayList<>();
                            if (giftcode.option.get(key) != null) {
                                for (Item.ItemOption io : giftcode.option.get(key)) {
                                    giftOptions.add(new Item.ItemOption(io.optionTemplate.id, io.param));
                                }
                            }

                            if (itemGift.template.type == 0 || itemGift.template.type == 1 || itemGift.template.type == 2 || itemGift.template.type == 3
                                    || itemGift.template.type == 4 || itemGift.template.type == 5) {
                                if (itemGift.template.id == 457) {
                                    itemGift.itemOptions.add(new Item.ItemOption(30, 0));
                                } else {
                                    itemGift.itemOptions = giftOptions;
                                    itemGift.quantity = quantity;
                                    InventoryService.gI().addItemBag(player, itemGift);
                                }
                            } else {
                                itemGift.itemOptions = giftOptions;
                                itemGift.quantity = quantity;
                                InventoryService.gI().addItemBag(player, itemGift);
                            }
                            textGift += "|1|x" + quantity + " " + itemGift.template.name + "\b";
                        }
                    }
                }
            }
            InventoryService.gI().sendItemBags(player);
            NpcService.gI().createTutorial(player, 1139, textGift);
        }
    }

}
