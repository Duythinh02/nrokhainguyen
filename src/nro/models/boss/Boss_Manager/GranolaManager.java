package nro.models.boss.Boss_Manager;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.player.Player;
import nro.models.services.SkillService;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;

public class GranolaManager extends Boss {

    private long lastTimeBlame;
    private int blameDelay = 15000;

    public GranolaManager() throws Exception {
        super(BossID.GranolaManager, BossesData.GranolaManager);
    }

    @Override
    public void active() {
        // NV 39: Granola là boss đánh được (trước đây đứng canh đường ở chế độ không PK)
        super.active();

        if (Util.canDoWithTime(lastTimeBlame, blameDelay)) {
            this.chat("Ta là kẻ sống sót cuối cùng của tộc Cereal!");
            lastTimeBlame = System.currentTimeMillis();
            blameDelay = Util.nextInt(10000, 20000);
        }
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100)) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }

                this.playerSkill.skillSelect = this.playerSkill.skills
                        .get(Util.nextInt(0, this.playerSkill.skills.size() - 1));

                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                        } else {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50));
                        }
                    }

                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void joinMap() {
        super.joinMap();
    }
}