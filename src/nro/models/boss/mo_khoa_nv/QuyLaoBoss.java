package nro.models.boss.mo_khoa_nv;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossDamageLimit;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstPlayer;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.map.service.ChangeMapService;
import nro.models.skill.Skill;
import nro.models.utils.Util;

/**
 * Quy Lão Kame thử thách mở khoá nhiệm vụ: boss riêng cho 1 người, gọi từ menu "Mở khoá nhiệm vụ" của Quy Lão.
 * Chỉ người gọi (và đệ tử) gây được sát thương; hạ được thì rơi Thẻ nhiệm vụ.
 * Người chơi chết, rời khu hoặc quá 5 phút thì boss bỏ đi.
 */
public class QuyLaoBoss extends Boss {

    private static final long TIME_LIMIT = 5 * 60_000;
    private static final Map<Long, QuyLaoBoss> ACTIVE = new ConcurrentHashMap<>();

    private final Player playerAtt;
    private long timeJoinMap;

    private static BossData createData() {
        return new BossData(
                "Quy Lão Kame",
                ConstPlayer.TRAI_DAT,
                new short[]{33, 34, 35, -1, -1, -1}, // giống NPC Quy Lão (npc_template 13)
                100_000, // dame (x2 trong initBase) -> thực tế 200K
                new int[]{500_000_000}, // hp (x2 trong initBase) -> thực tế 1 tỷ
                new int[]{5},
                new int[][]{
                    {Skill.DRAGON, 7, 1000},
                    {Skill.KAMEJOKO, 7, 2000},
                    {Skill.THAI_DUONG_HA_SAN, 7, 30000},
                    {Skill.TAI_TAO_NANG_LUONG, 7, 60000},},
                new String[]{"|-1|Muốn mở khoá nhiệm vụ mới thì phải thắng được ta!",
                    "|-1|Ta sẽ không nương tay đâu, cẩn thận nhé con"},
                new String[]{"|-1|Kamê... Kamê... Ha!", "|-1|Còn non lắm con ơi", "|-1|Hôhôhô"},
                new String[]{"|-1|Giỏi lắm, con đã đủ sức đi tiếp rồi. Cầm lấy Thẻ nhiệm vụ!"},
                3600);
    }

    private QuyLaoBoss(Player player) throws Exception {
        super(Util.createIdBossClone((int) player.id) - 100_000_000, createData());
        this.playerAtt = player;
    }

    public static boolean isFighting(Player player) {
        return ACTIVE.containsKey(player.id);
    }

    public static void spawn(Player player) {
        if (ACTIVE.containsKey(player.id)) {
            return;
        }
        try {
            ACTIVE.put(player.id, new QuyLaoBoss(player));
        } catch (Exception e) {
            ACTIVE.remove(player.id);
            e.printStackTrace();
        }
    }

    @Override
    public void joinMap() {
        if (playerAtt.zone == null) {
            leaveMap();
            return;
        }
        this.zone = playerAtt.zone;
        ChangeMapService.gI().changeMap(this, this.zone,
                playerAtt.location.x + Util.getOne(-1, 1) * 120, playerAtt.location.y);
        this.timeJoinMap = System.currentTimeMillis();
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public Player getPlayerAttack() {
        return playerAtt;
    }

    @Override
    public void autoLeaveMap() {
        if (playerAtt == null || playerAtt.zone == null || this.zone == null || !this.zone.equals(playerAtt.zone)) {
            leaveMap();
        } else if (playerAtt.isDie()) {
            this.chat("Luyện tập thêm rồi quay lại tìm ta nhé");
            Service.gI().sendThongBao(playerAtt, "Bạn đã thua Quy Lão, hãy luyện tập thêm rồi thử lại");
            leaveMap();
        } else if (timeJoinMap > 0 && Util.canDoWithTime(timeJoinMap, TIME_LIMIT)) {
            this.chat("Hết giờ rồi, lần sau nhanh tay hơn nhé");
            Service.gI().sendThongBao(playerAtt, "Hết thời gian thử thách, hãy thử lại");
            leaveMap();
        }
    }

    @Override
    protected synchronized int injuredLong(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (BossDamageLimit.ownerOrSelf(plAtt) != playerAtt) {
            return 0;
        }
        return super.injuredLong(plAtt, damage, piercing, isMobAttack);
    }

    @Override
    public void die(Player plKill) {
        plKill = BossDamageLimit.ownerOrSelf(plKill);
        if (plKill != null && plKill == playerAtt) {
            reward(plKill);
        }
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void reward(Player plKill) {
        Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, TaskService.ITEM_THE_NHIEM_VU, 1,
                this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        Service.gI().sendThongBao(plKill, "Bạn đã hạ Quy Lão, hãy nhặt Thẻ nhiệm vụ và dùng để mở khoá nhiệm vụ mới");
    }

    @Override
    public void leaveMap() {
        ACTIVE.remove(playerAtt.id);
        super.leaveMap(); // 1 dạng: rời map, về REST
        BossManager.gI().removeBoss(this);
        this.dispose();
    }
}
