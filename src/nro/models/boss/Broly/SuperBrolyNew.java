package nro.models.boss.Broly;

import nro.models.boss.BossData;
import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.consts.BossStatus;
import static nro.models.consts.BossType.BROLY;
import nro.models.boss.BossesData;
import nro.models.services.EffectSkillService;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import nro.models.map.Zone;
import nro.models.consts.ConstPlayer;
import nro.models.consts.ConstRatio;
import nro.models.player.Player;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.services.PetService;
import nro.models.services.Service;
import nro.models.map.service.ChangeMapService;
import nro.models.skill.Skill;
import nro.models.services.SkillService;
import nro.models.utils.Logger;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;
import nro.models.map.ItemMap;

public class SuperBrolyNew extends Boss {

    private long st;
    private int timeLeaveMap;
    private long spawnTime;
    private static final int RESET_AFTER = 30 * 60 * 1000;

    public SuperBrolyNew() throws Exception {
        super(BossID.SUPER_BROLY_NEW, false, true, false, false, BossesData.SUPPER_BROLY_NEW);
    }

    // Hành tinh của đệ tử đi theo, người hạ boss nhận đệ cùng hành tinh
    private int petGender;

    @Override
    public void reward(Player plKill) {
        // Super Broly không rơi trứng, chỉ cho đệ tử thường
        PetService.gI().rewardNormalPetFromBoss(plKill, this.name, this.petGender);
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }

            if (this.currentLevel != 0) {
                damage /= 2;
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
        }

        return 0;
    }

    @Override
    public void autoLeaveMap() {
        if (this.zone != null && !this.isDie() && Util.canDoWithTime(spawnTime, RESET_AFTER)) {
            this.chat("Ta sẽ quay lại mạnh hơn!");
            this.leaveMapNew();
            return;
        }

        if (Util.canDoWithTime(st, timeLeaveMap)) {
            if (Util.isTrue(1, 2)) {
                this.leaveMap();
            } else {
                this.leaveMapNew();
            }
        }

        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
            timeLeaveMap = Util.nextInt(1800000, 3600000);
        }
    }

    @Override
    public void joinMap() {
        this.name = this.data[this.currentLevel].getName() + " " + Util.nextInt(1, 100);
        super.joinMap();
        st = System.currentTimeMillis();
        timeLeaveMap = Util.nextInt(1800000, 3600000);
        spawnTime = System.currentTimeMillis();
        if (this.zone != null) {
            // Mỗi Super Broly dẫn theo 1 đệ tử thường thuộc hành tinh ngẫu nhiên
            removePet();
            this.petGender = Util.nextInt(0, 2);
            PetService.gI().createNormalPet(this, this.petGender);
        }
    }

    @Override
    public void leaveMap() {
        removePet();
        super.leaveMap();
    }

    private void removePet() {
        if (this.pet != null) {
            if (this.pet.zone != null) {
                ChangeMapService.gI().exitMap(this.pet);
            }
            this.pet.dispose();
            this.pet = null;
        }
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();

            try {
                Player pl = getPlayerAttack();

                if (pl == null || pl.isDie()) {
                    return;
                }

                this.playerSkill.skillSelect = this.playerSkill.skills.get(
                        Util.nextInt(0, this.playerSkill.skills.size() - 1)
                );

                int dis = Util.getDistance(this, pl);

                if (dis > 450) {
                    move(pl.location.x - 24, pl.location.y);
                } else if (dis > 100) {
                    int dir = (this.location.x - pl.location.x < 0 ? 1 : -1);
                    int move = Util.nextInt(50, 100);
                    move(this.location.x + (dir == 1 ? move : -move), pl.location.y);
                } else {
                    if (Util.isTrue(30, 100)) {
                        int move = Util.nextInt(50);
                        move(pl.location.x + (Util.nextInt(0, 1) == 1 ? move : -move), this.location.y);
                    }

                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                }
            } catch (Exception ex) {
                Logger.logException(Boss.class, ex);
            }
        }
    }
}