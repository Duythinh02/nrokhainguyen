package nro.models.boss.Golden_fireza;

import nro.models.boss.Boss;
import nro.models.boss.BossesData;
import static nro.models.consts.BossType.SKILLSUMMONED;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstTask;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.service.ChangeMapService;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.utils.Util;

/**
 * Death Beam 1 - 5: lính Fide Vàng gọi ra ở Đông Karin (21h - 22h), mục tiêu NV 37 bước 1 - 5 (hạ đúng thứ tự).
 * Trước đây là "chưởng tự đuổi" bất tử (injured = 0, chạm là gây 2,1 tỷ sát thương, tự biến mất sau 15 giây) nên
 * không ai hạ được. Nay là boss đánh được như Xên con (máu / chiêu trong BossesData.DEATH_BEAM), ở lại tới khi bị hạ
 * hoặc Fide Vàng rời map. Chỉ mất máu khi trúng Bom / Laze / Quả cầu kênh khí, chiêu khác "Xí hụt". Không có Tái tạo năng lượng. Fide Vàng gọi lại cả 5 khi tất cả đã nghỉ (GoldenFrieza.attack).
 */
public abstract class DeathBeamBase extends Boss {

    // Ở lại tối đa (giống Xên con), Fide Vàng rời map thì đi theo
    private static final long MAX_STAY_MS = 900_000;
    private static final short CT_FIDE_VANG = 629;

    private final int soThuTu;
    private long st;

    protected DeathBeamBase(int bossId, int soThuTu) throws Exception {
        super(SKILLSUMMONED, bossId, BossesData.DEATH_BEAM);
        this.soThuTu = soThuTu;
    }

    @Override
    public void joinMap() {
        this.name = "Death Beam " + soThuTu; // BossesData.DEATH_BEAM tên "$" bị client ẩn
        st = System.currentTimeMillis();
        this.zone = this.parentBoss.zone;
        ChangeMapService.gI().changeMap(this, this.zone,
                this.parentBoss.location.x + Util.nextInt(-100, 100), this.parentBoss.location.y);
        Service.gI().sendFlagBag(this);
        this.changeStatus(BossStatus.CHAT_S);
    }

    private long lastTimeChatHut;

    // Chiêu đặc biệt của 3 hành tinh: Quả cầu kênh khí (Trái Đất), Laze (Namếc), Bom - Tự sát (Xayda)
    private static boolean isChieuDacBiet(Player plAtt) {
        if (plAtt == null || plAtt.playerSkill == null || plAtt.playerSkill.skillSelect == null) {
            return false;
        }
        int skillId = plAtt.playerSkill.skillSelect.template.id;
        return skillId == nro.models.skill.Skill.QUA_CAU_KENH_KHI || skillId == nro.models.skill.Skill.MAKANKOSAPPO
                || skillId == nro.models.skill.Skill.TU_SAT;
    }

    // Chỉ chiêu đặc biệt mới mất máu; chiêu khác, quái đánh: "Xí hụt", 0 sát thương
    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (isMobAttack || !isChieuDacBiet(plAtt)) {
            if (!this.isDie() && Util.canDoWithTime(lastTimeChatHut, 3000)) {
                lastTimeChatHut = System.currentTimeMillis();
                this.chat("Xí hụt");
            }
            return 0;
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

    // Đang làm bước hạ Death Beam của NV 37
    private static boolean dangLamDeathBeam(Player pl) {
        int task = TaskService.gI().getIdTask(pl);
        return task >= ConstTask.TASK_37_1 && task <= ConstTask.TASK_37_5;
    }

    @Override
    public void reward(Player plKill) {
        nro.models.services.BossHuntService.gI().rewardBossPoint(plKill, 5, this);
        // Fide Vàng gọi lại liên tục: cải trang chỉ rơi cho người đang làm bước Death Beam, tránh rơi tràn lan
        if (dangLamDeathBeam(plKill)) {
            ItemMap ct = new ItemMap(zone, CT_FIDE_VANG, 1, this.location.x + Util.nextInt(-50, 50),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
            ct.options.add(new Item.ItemOption(30, 1));
            ct.options.add(new Item.ItemOption(50, 20));
            ct.options.add(new Item.ItemOption(77, 20));
            ct.options.add(new Item.ItemOption(103, 20));
            ct.options.add(new Item.ItemOption(93, 20));
            Service.gI().dropItemMap(this.zone, ct);
        }
    }

    @Override
    public void autoLeaveMap() {
        boolean fideDaDi = this.parentBoss == null || this.parentBoss.zone == null || !this.parentBoss.zone.equals(this.zone);
        if (fideDaDi || Util.canDoWithTime(st, MAX_STAY_MS)) {
            this.leaveMap();
        }
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }
}
