package nro.models.services;

import nro.models.consts.ConstItem;
import nro.models.item.Item;
import nro.models.player.Player;

// Coin Khai Nguyên (item 2691): tiền tệ ký gửi, đổi từ điểm săn boss tại Tori-Bot
public final class CoinKhaiNguyenService {

    public static final String NAME = "Coin Khai Nguyên";

    private CoinKhaiNguyenService() {
    }

    // Tổng coin trong hành trang (cộng mọi ô)
    public static long count(Player pl) {
        long total = 0;
        for (Item it : pl.inventory.itemsBag) {
            if (it.isNotNullItem() && it.template.id == ConstItem.COIN_KHAI_NGUYEN) {
                total += it.quantity;
            }
        }
        return total;
    }

    // Trừ coin trên nhiều ô; không đủ thì không trừ gì
    public static boolean sub(Player pl, int amount) {
        if (amount <= 0 || count(pl) < amount) {
            return false;
        }
        int remain = amount;
        for (Item it : new java.util.ArrayList<>(pl.inventory.itemsBag)) {
            if (remain <= 0) {
                break;
            }
            if (it.isNotNullItem() && it.template.id == ConstItem.COIN_KHAI_NGUYEN) {
                int take = Math.min(remain, it.quantity);
                InventoryService.gI().subQuantityItemsBag(pl, it, take);
                remain -= take;
            }
        }
        return true;
    }

    // false nếu hành trang đầy (không nhận được coin)
    public static boolean add(Player pl, int amount) {
        if (amount <= 0) {
            return true;
        }
        Item coin = ItemService.gI().createNewItem((short) ConstItem.COIN_KHAI_NGUYEN, amount);
        return InventoryService.gI().addItemBag(pl, coin);
    }
}
