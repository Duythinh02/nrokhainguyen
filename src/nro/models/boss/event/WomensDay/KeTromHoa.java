package nro.models.boss.event.WomensDay;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.consts.BossStatus;
import nro.models.event.EventManager;
import nro.models.event_list.WomensDay.WomensDayService;
import nro.models.item.Item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * Boss sự kiện 20/10: rơi nguyên liệu trồng hoa và quà cho người hạ gục.
 */
public class KeTromHoa extends Boss {

    private long st;

    public KeTromHoa() throws Exception {
        super(BossID.KE_TROM_HOA, false, true, BossesData.KE_TROM_HOA);
    }

    @Override
    public void reward(Player plKill) {
        int y = this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24);
        drop(plKill, WomensDayService.THUOC_TANG_TRUONG, Util.nextInt(1, 3), y, 0);
        drop(plKill, WomensDayService.HAT_MAM, Util.nextInt(1, 2), y, 20);
        drop(plKill, WomensDayService.LOI_CHUC, 1, y, -20);
        if (Util.isTrue(10, 100)) {
            drop(plKill, WomensDayService.LOI_CHUC_VIP, 1, y, 40);
        }
        if (Util.isTrue(3, 100)) {
            ItemMap it = new ItemMap(zone, 1100, 1, this.location.x - 40, y, plKill.id);
            it.options.add(new ItemOption(50, Util.nextInt(14, 16)));
            it.options.add(new ItemOption(77, Util.nextInt(14, 16)));
            it.options.add(new ItemOption(103, Util.nextInt(14, 16)));
            it.options.add(new ItemOption(30, 0));
            it.options.add(new ItemOption(93, 7));
            Service.gI().dropItemMap(this.zone, it);
        }
    }

    private void drop(Player plKill, short tempId, int quantity, int y, int dx) {
        ItemMap it = new ItemMap(zone, tempId, quantity, this.location.x + dx, y, plKill.id);
        if (tempId == WomensDayService.LOI_CHUC || tempId == WomensDayService.LOI_CHUC_VIP) {
            it.options.add(new ItemOption(30, 0));
            it.options.add(new ItemOption(93, 30));
        }
        Service.gI().dropItemMap(this.zone, it);
    }

    // Mỗi đòn tối đa 2% máu, cần cả nhóm cùng đánh
    @Override
    protected synchronized int injuredLong(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        return super.injuredLong(plAtt, Math.min(damage, Math.max(1, (long) this.nPoint.hpMax / 50)), piercing, isMobAttack);
    }

    @Override
    public void joinMap() {
        if (!EventManager.DAY_20_10) { // sự kiện đã kết thúc: nghỉ, không xuất hiện
            this.lastTimeRest = System.currentTimeMillis();
            this.changeStatus(BossStatus.REST);
            return;
        }
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        if (!EventManager.DAY_20_10 || Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }
}
