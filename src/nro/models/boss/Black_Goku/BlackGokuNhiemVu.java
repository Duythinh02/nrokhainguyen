package nro.models.boss.Black_Goku;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstPlayer;
import nro.models.map.service.ChangeMapService;
import nro.models.player.Player;
import nro.models.services.NpcService;
import nro.models.services.TaskService;
import nro.models.skill.Skill;
import nro.models.utils.Util;

// NV 33 "Tương lai đen tối": Black Goku riêng cho từng người chơi ở Sân sau siêu thị (map 104).
// Xuất hiện -> con chim nhắc đánh yếu hắn (xong bước "Chạm trán");
// bị đánh xuống dưới 30% máu -> "Ta đợi ngươi ở tương lai!" rồi biến mất (xong bước "Đánh yếu"). Không chết, không rơi đồ.
public class BlackGokuNhiemVu extends Boss {

    private static final Map<Long, BlackGokuNhiemVu> ACTIVE = new ConcurrentHashMap<>();
    private static final int BIRD_AVATAR = 4433; // icon "Quỷ Chim"
    private static final int ESCAPE_PERCENT = 30;
    private static final long MAX_LIFE_MS = 10 * 60 * 1000L;

    private final Player owner;
    private boolean joined;
    private boolean escaped;
    private boolean removed;
    private long joinTime;
    private long leaveAt;

    // Gọi khi người chơi vào map 104 ở bước "Chạm trán" hoặc "Đánh yếu" của NV 33; mỗi người chỉ 1 con
    public static void trySpawn(Player pl) {
        if (pl == null || pl.zone == null || ACTIVE.containsKey(pl.id)) {
            return;
        }
        try {
            new BlackGokuNhiemVu(pl);
        } catch (Exception e) {
            ACTIVE.remove(pl.id);
        }
    }

    private BlackGokuNhiemVu(Player pl) throws Exception {
        super((int) (-3_000_000L - pl.id), new BossData(
                "Black Goku", // name
                ConstPlayer.TRAI_DAT, // gender
                new short[]{550, 551, 552, -1, -1, -1}, // outfit {head, body, leg, bag, aura, eff}
                50000, // dame
                new int[]{500_000_000}, // hp
                new int[]{104}, // map join
                new int[][]{
                    {Skill.KAMEJOKO, 7, 1500},
                    {Skill.GALICK, 7, 1500},},
                new String[]{"|-1|Ngươi là ai mà dám cản đường ta?",
                    "|-1|Cơ thể này, sức mạnh này... thật tuyệt vời"}, // text chat 1
                new String[]{"|-1|Chỉ có vậy thôi sao?",
                    "|-1|Loài người thật thấp kém"}, // text chat 2
                new String[]{}, // text chat 3
                1 // second rest
        ));
        this.owner = pl;
        this.zone = pl.zone;
        this.location.x = pl.location.x + (Util.isTrue(1, 2) ? 120 : -120);
        this.location.y = pl.location.y;
        ACTIVE.put(pl.id, this);
    }

    @Override
    public void joinMap() {
        if (this.zone == null || owner.zone != this.zone) {
            leaveMap();
            return;
        }
        int y = this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24);
        ChangeMapService.gI().changeMap(this, this.zone, this.location.x, y);
        this.joined = true;
        this.joinTime = System.currentTimeMillis();
        this.changeStatus(BossStatus.CHAT_S);
        TaskService.gI().checkDoneTaskGapBlackGoku(owner);
        NpcService.gI().createTutorial(owner, BIRD_AVATAR,
                "Chíp chíp! Kẻ đó là Black Goku, hắn mạnh khủng khiếp!\n"
                + "Ngươi chưa thể hạ hắn bây giờ đâu.\n"
                + "Hãy đánh cho hắn yếu đi (dưới 30% máu)!");
    }

    @Override
    public void update() {
        if (removed) {
            return;
        }
        super.update();
        if (removed || !joined) {
            return;
        }
        long now = System.currentTimeMillis();
        if (escaped) {
            if (now >= leaveAt) {
                leaveMap();
            }
            return;
        }
        // Người chơi rời khu / thoát game / quá lâu thì biến mất
        if (owner.zone != this.zone || owner.isDie() || now - joinTime > MAX_LIFE_MS) {
            leaveMap();
        }
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (removed || escaped || this.isDie()) {
            return 0;
        }
        long dmg = (long) this.nPoint.subDameInjureWithDeff(damage);
        long threshold = this.nPoint.hpMax * ESCAPE_PERCENT / 100;
        long hp = this.nPoint.hp;
        if (hp - dmg <= threshold) {
            dmg = Math.max(0, hp - threshold);
            this.nPoint.subHP(dmg);
            escape(plAtt);
            return dmg;
        }
        this.nPoint.subHP(dmg);
        return dmg;
    }

    private void escape(Player plAtt) {
        escaped = true;
        leaveAt = System.currentTimeMillis() + 2500;
        this.chat("Ta đợi ngươi ở tương lai!");
        TaskService.gI().checkDoneTaskDanhYeuBlackGoku(owner);
        if (plAtt != null && plAtt != owner && !plAtt.isBoss) {
            TaskService.gI().checkDoneTaskDanhYeuBlackGoku(plAtt);
        }
        if (owner.zone == this.zone) {
            NpcService.gI().createTutorial(owner, BIRD_AVATAR,
                    "Chíp chíp! Hắn đã chạy về tương lai rồi!\n"
                    + "Hãy đến tương lai, tiêu diệt Super Black Goku và bọn Zamasu!");
        }
    }

    // Không bao giờ chết thật: nếu có đòn kết liễu thì coi như bỏ chạy
    @Override
    public void die(Player plKill) {
        if (!escaped) {
            escape(plKill);
        }
    }

    @Override
    public void reward(Player plKill) {
    }

    @Override
    public void autoLeaveMap() {
    }

    @Override
    public void leaveMap() {
        if (removed) {
            return;
        }
        removed = true;
        ACTIVE.remove(owner.id);
        ChangeMapService.gI().exitMap(this);
        BossManager.gI().removeBoss(this);
        this.dispose();
    }
}
