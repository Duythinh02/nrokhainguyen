package nro.models.boss.doanh_trai;

/*
 * @Author: Anwin
 */

import nro.models.services.EffectSkillService;
import nro.models.player.Player;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossID;
import nro.models.consts.BossStatus;
import static nro.models.consts.BossType.PHOBANDT;
import nro.models.boss.Boss_Manager.RedRibbonHQManager;
import nro.models.map.service.ChangeMapService;
import nro.models.services.Service;
import nro.models.skill.Skill;
import nro.models.utils.Util;
import nro.models.consts.ConstPlayer;
import nro.models.map.ItemMap;
import nro.models.map.Zone;

public class RobotVeSi2 extends Boss {

    public RobotVeSi2(Zone zone, long dame, long hp) throws Exception {
        super(PHOBANDT, BossID.ROBOT_VE_SI_2, new BossData(
                "Rôbốt Vệ Sĩ 1",
                ConstPlayer.TRAI_DAT,
                new short[]{138, 139, 140, -1, -1, -1},
                dame,
                new long[]{hp},
                new int[]{57},
                new int[][]{
                    {Skill.KAMEJOKO, Util.nextInt(6, 7), Util.nextInt(700, 1200)}
                },
                new String[]{},
                new String[]{},
                new String[]{},
                60
        ));

        this.zone = zone;
    }

    @Override
    public void reward(Player plKill) {
        if (plKill != null && Util.isTrue(100, 100)) {
            ItemMap it = new ItemMap(
                    this.zone,
                    Util.nextInt(14, 16),
                    1,
                    this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id
            );
            Service.gI().dropItemMap(this.zone, it);
        }
    }

    @Override
    public void joinMap() {
        ChangeMapService.gI().changeMap(this, this.zone, 300, 312);
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
        Service.gI().setPos(this, 300, 312);
    }

    @Override
    public void afk() {
        Player pl = getPlayerAttack();

        if (pl == null || pl.isDie()) {
            return;
        }

        Service.gI().setPos(this, pl.location.x + Util.nextInt(-100, 100), 0);
        this.changeStatus(BossStatus.ACTIVE);
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }

            damage = this.nPoint.subDameInjureWithDeff(damage);

            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }

            this.nPoint.subHP(damage);

            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }

            return damage;
        } else {
            return 0;
        }
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            reward(plKill);
        }

        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        RedRibbonHQManager.gI().removeBoss(this);
        this.dispose();
    }
}