package nro.models.services;

import nro.models.boss.BossDamageLimit;
import nro.models.item.Item;
import nro.models.player.Pet;
import nro.models.player.Player;

// % may mắn từ trang bị (option 236 "+#% May mắn") tăng tỷ lệ rơi đồ.
// May mắn tối đa 100%; thưởng tỷ lệ = (mayMan/100)^1.5 * MAX_BONUS (100% may mắn => tỷ lệ rơi x1.2).
public class LuckService {

    public static final int OPTION_MAY_MAN = 236;
    public static final int MAX_LUCK = 100;
    public static final double MAX_BONUS = 0.20;

    // Đệ tử hạ thì tính may mắn của chủ
    private static Player owner(Player player) {
        if (player instanceof Pet pet && pet.master != null) {
            return pet.master;
        }
        return player;
    }

    public static int getLuck(Player player) {
        Player pl = owner(player);
        if (pl == null || pl.inventory == null || pl.inventory.itemsBody == null) {
            return 0;
        }
        int total = 0;
        for (Item item : pl.inventory.itemsBody) {
            if (item != null && item.isNotNullItem() && item.itemOptions != null) {
                for (Item.ItemOption op : item.itemOptions) {
                    if (op.optionTemplate != null && op.optionTemplate.id == OPTION_MAY_MAN) {
                        total += op.param;
                    }
                }
            }
        }
        return Math.min(total, MAX_LUCK);
    }

    public static double getMultiplier(Player player) {
        return 1 + Math.pow(getLuck(player) / (double) MAX_LUCK, 1.5) * MAX_BONUS;
    }

    // Thay cho Util.isTrue(rate, total) ở các chỗ rơi đồ có tính may mắn
    public static boolean isTrue(Player player, double rate, double total) {
        return Math.random() * total < rate * getMultiplier(player);
    }

    // Rơi đồ Thần từ boss: thêm thưởng khi boss bị người chơi nhiều hành tinh cùng đánh
    public static boolean isTrueGodDrop(Player player, Player boss, double rate, double total) {
        return isTrue(player, rate * BossDamageLimit.godDropMultiplier(boss), total);
    }
}
