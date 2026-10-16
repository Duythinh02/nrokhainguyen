package nro.models.boss.BossFunction;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.services.TaskService;
import nro.models.map.service.ChangeMapService;

public class TaskTauPayPay extends Boss {

    public TaskTauPayPay(int bossID, BossData bossData, Zone zone, int x, int y) throws Exception {
        super(bossID, bossData);
        this.zone = zone;
        this.location.x = x;
        this.location.y = y;
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public void update() {
        super.update();
        if (this.zone.getNumOfPlayers() != 1) {
            leaveMap();
        }
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void joinMap() {
        super.joinMap();
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        BossManager.gI().removeBoss(this);
        this.dispose();
    }
}





