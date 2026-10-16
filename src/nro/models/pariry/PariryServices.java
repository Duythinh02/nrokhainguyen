package nro.models.pariry;

import nro.models.services.InventoryService;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.services.ItemService;

/*
 * @Author: Anwin
 */

public class PariryServices {

    private static PariryServices instance;

    public static PariryServices gI() {
        if (instance == null) {
            instance = new PariryServices();
        }
        return instance;
    }

    public void addPlayerEven(Player player) {
        if (pariryManager.currentPariry != null) {
            pariryManager.gI().addPlayerEven(player);
        }
    }

    public void addPlayerOdd(Player player) {
        if (pariryManager.currentPariry != null) {
            pariryManager.gI().addPlayerOdd(player);
        }
    }

    public void rewardRuby(Player player) {
        if (player != null) {
            Item tvthang = ItemService.gI().createNewItem((short) 457);
            tvthang.quantity = (int) Math.round(player.rubyWin * 1.5);
            if (tvthang.template.id == 457) {
                tvthang.itemOptions.add(new ItemOption(30, 1));
                tvthang.itemOptions.add(new ItemOption(93, 10));
            }
            InventoryService.gI().addItemBag(player, tvthang);
            InventoryService.gI().sendItemBags(player);
            pariryManager.gI().setAfterPlayerReward(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + tvthang.quantity + " thỏi vàng");
            player.rubyWin = 0;
        }
    }

    // Cược bằng điểm săn boss, cùng mức với Tài Xỉu; thắng nhận về 150% (gồm cả điểm cược)
    public static final int MIN_BET = nro.models.minigame.TaiXiu.MIN_BET;
    public static final int MAX_BET = nro.models.minigame.TaiXiu.MAX_BET;
    public static final int WIN_PERCENT = 150;

    public void bet(Player pl, boolean isOdd, int amount) {
        synchronized (pariryManager.gI()) {
            Pariry cur = pariryManager.currentPariry;
            if (cur == null || pariryManager.time < 3) {
                Service.gI().sendThongBao(pl, "Hết thời gian đặt cược, vui lòng chờ phiên sau");
                return;
            }
            if (cur.checkPlayer(pl) != -1) {
                Service.gI().sendThongBao(pl, "Bạn đã đặt cược phiên này rồi");
                return;
            }
            if (amount < MIN_BET || amount > MAX_BET) {
                Service.gI().sendThongBao(pl, "Cược ít nhất " + MIN_BET + " - nhiều nhất " + MAX_BET + " điểm săn boss");
                return;
            }
            if (pl.event.getEventPoint() < amount) {
                Service.gI().sendThongBao(pl, "Bạn không đủ điểm săn boss! Đang có " + pl.event.getEventPoint() + " điểm.");
                return;
            }
            pl.event.subEventPoint(amount);
            pl.cuoc = amount;
            if (isOdd) {
                cur.addPlayerOdd(pl);
            } else {
                cur.addPlayerEven(pl);
            }
            Service.gI().sendThongBao(pl, "Đã đặt " + (isOdd ? "LẺ " : "CHẴN ") + amount + " điểm săn boss (còn "
                    + pl.event.getEventPoint() + " điểm)");
        }
    }

    public boolean checkHavePariry() {
        return pariryManager.currentPariry != null;
    }

    public String getHistoryPlayer(Player player) {
        return pariryManager.gI().getHistoryPlayer(player);
    }

    public String getHistory() {
        return pariryManager.gI().getHistoryGame();
    }
}
