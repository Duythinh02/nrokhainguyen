package nro.models.services_dungeon;

import nro.models.map.phoban.RedRibbonHQ;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.map.service.ChangeMapService;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author By Mr Blue
 * 
 */

public class RedRibbonHQService {

    private static RedRibbonHQService instance;

    public static RedRibbonHQService gI() {
        if (RedRibbonHQService.instance == null) {
            RedRibbonHQService.instance = new RedRibbonHQService();
        }
        return RedRibbonHQService.instance;
    }

    public List<RedRibbonHQ> doanhTrais;

    private RedRibbonHQService() {
        this.doanhTrais = new ArrayList<>();
        for (int i = 0; i < RedRibbonHQ.AVAILABLE; i++) {
            this.doanhTrais.add(new RedRibbonHQ(i));
        }
    }

    public void addMapDoanhTrai(int id, Zone zone) {
        this.doanhTrais.get(id).getZones().add(zone);
    }

    public void joinDoanhTrai(Player pl) {
        if (pl.clan == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        if (!RedRibbonHQ.canJoin(pl, pl.clan.doanhTrai)) {
            Service.gI().sendThongBao(pl, "Hôm nay bạn đã vào doanh trại " + RedRibbonHQ.MAX_TIMES_PER_DAY + " lần rồi, vui lòng chờ đến ngày mai");
            return;
        }
        if (pl.clan.doanhTrai != null) {
            RedRibbonHQ.markJoin(pl, pl.clan.doanhTrai);
            pl.clan.doanhTrai.updateHPDame();
            ChangeMapService.gI().changeMapInYard(pl, 53, -1, 60);
            return;
        }
        RedRibbonHQ doanhTrai = null;
        for (RedRibbonHQ dt : this.doanhTrais) {
            if (dt.getClan() == null) {
                doanhTrai = dt;
                break;
            }
        }
        if (doanhTrai == null) {
            Service.gI().sendThongBao(pl, "Doanh trại đã đầy, hãy quay lại vào lúc khác!");
            return;
        }
        doanhTrai.openDoanhTrai(pl);
    }
}
