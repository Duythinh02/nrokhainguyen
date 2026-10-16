package nro.models.event.VuLanFestival;

/*
 * @Author: Anwin
 */

import nro.models.boss.BossID;
import nro.models.event.Event;

import nro.models.boss.BossID;

import nro.models.boss.BossID;

import nro.models.boss.BossID;

public class VuLanFestival extends Event {
    
    @Override
    public void init() {
        super.init();
    }

    @Override
    public void npc() {
        createNpc(5, 83, 900, 408);
        createNpc(13, 83, 950, 384);
        createNpc(20, 83, 1100, 360);
    }

    @Override
    public void boss() {
        createBoss(BossID.PIKKON, 3);
    }
}






