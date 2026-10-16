package nro.models.event_list.WomensDay;

import nro.models.boss.BossID;
import nro.models.event.Event;

/**
 *
 * @author Anwin
 */
public class WomensDay extends Event {
    
    @Override
    public void init() {
        if (!WomensDayService.scheduleEnd()) {
            return;
        }
        super.init();
        WomensDayService.load();
    }

    @Override
    public void boss() {
        createBoss(BossID.KE_TROM_HOA, 5);
    }

    @Override
    public void npc() {
        createNpc(5, 100, 900, 408);
        createNpc(13, 100, 950, 384);
        createNpc(20, 100, 1100, 360);
    }
}






