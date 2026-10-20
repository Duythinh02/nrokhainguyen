package nro.models.boss.ta_long;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.consts.ConstTaskBadges;
import nro.models.map.ItemMap;
import nro.models.map.service.MapService;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.task.BadgesTaskService;
import nro.models.utils.Util;

// Boss Tà Long (Dragon Ball GT)
public class TaLong extends Boss {

    private long st;

    public TaLong() throws Exception {
        super(BossID.TA_LONG, BossesData.TA_LONG);
    }

    @Override
    public void reward(Player plKill) {
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);
        nro.models.services.BossHuntService.gI().rewardBossPoint(plKill, 5);
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
        // TODO: chưa có danh sách đồ rơi, tạm thời chỉ rơi vàng
        ItemMap vang = new ItemMap(this.zone, 190, Util.nextInt(20000, 30000), x, y, plKill.id);
        Service.gI().dropItemMap(this.zone, vang);
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(10, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return (int) damage;
        } else {
            return 0;
        }
    }

    @Override
    public void joinMap() {
        // Luôn xuất hiện ở khu 0 Làng Kakarot (map 14)
        if (this.zoneFinal == null) {
            this.zoneFinal = MapService.gI().getZoneByMapIDAndZoneID(14, 0);
        }
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        // Không có người chơi trong 15 phút thì rời map
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }
}
