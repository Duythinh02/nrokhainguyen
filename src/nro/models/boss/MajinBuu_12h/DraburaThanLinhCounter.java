package nro.models.boss.MajinBuu_12h;

import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.models.server.ServerNotify;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * Đếm chung toàn server số Drabura (map Ma Bư) bị tiêu diệt: cứ mỗi 10-15 con
 * thì con đủ số rơi 1 món đồ thần linh ngẫu nhiên có 1-7 sao (option 107).
 */
public class DraburaThanLinhCounter {

    public static final int MIN_KILLS = 10;
    public static final int MAX_KILLS = 15;

    private static int killed;
    private static int target = Util.nextInt(MIN_KILLS, MAX_KILLS);

    public static synchronized void onKilled(Zone zone, int x, int y, Player plKill) {
        if (zone == null || plKill == null) {
            return;
        }
        killed++;
        if (killed < target) {
            return;
        }
        killed = 0;
        target = Util.nextInt(MIN_KILLS, MAX_KILLS);
        ItemMap it = ItemService.gI().randDoTLBoss(zone, 1, x, y, plKill.id);
        if (it == null) {
            return;
        }
        int star = Util.nextInt(1, 7);
        it.options.add(new Item.ItemOption(107, star));
        Service.gI().dropItemMap(zone, it);
        ServerNotify.gI().notify(plKill.name + " vừa hạ Drabura và nhận được đồ thần linh " + star + " sao!");
    }
}
