package nro.models.Bot;

import java.util.ArrayList;
import java.util.List;
import nro.models.consts.ConstItem;
import nro.models.map.Map;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.MapService;
import nro.models.mob.Mob;
import nro.models.player_system.Template.SkillTemplate;
import nro.models.server.Manager;
import nro.models.services.ItemService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.services.SkillService;
import nro.models.skill.NClass;
import nro.models.skill.Skill;
import nro.models.utils.Logger;
import nro.models.utils.Util;

/**
 * Bot mô phỏng người chơi thật: mặc đồ cơ bản theo hành tinh, đi bộ tới quái,
 * làm lần lượt chuỗi nhiệm vụ đánh quái đầu game rồi đi luyện tập.
 */
public class BotNguoiChoi extends Bot {

    // Quái theo chặng nhiệm vụ (cộng thêm hành tinh cho các chặng có quái riêng từng hành tinh)
    private static final int[] STAGE_MOB = {0, 1, 4, 7, 13, 16};
    private static final boolean[] STAGE_BY_PLANET = {false, true, true, true, true, true};
    private static final int[] STAGE_KILLS = {5, 10, 10, 15, 15, 20};
    public static final int STAGE_FREE = 6; // đã xong chuỗi nhiệm vụ, đi luyện tập

    private static final long[] STAGE_POWER = {1_500, 12_000, 60_000, 250_000, 1_200_000, 5_000_000, 20_000_000};
    private static final int[] STAGE_HP = {150, 500, 1_500, 3_000, 8_000, 15_000, 30_000};
    private static final int[] STAGE_DAME = {10, 70, 160, 220, 900, 600, 1_500};
    private static final int[] STAGE_CLOTHES_LEVEL = {0, 1, 2, 3, 4, 5, 6};

    private static final String[] CHATS = {
        "ai đi nv cùng k", "map này đông quá", "up đệ k ae", "lag thế nhỉ",
        "hôm nay vào đông ghê", "sv ngon ae", "đi up sm thôi", "ae hành tinh nào",
        "pt k ae", "quái hồi lâu vãi", "sắp xong nv rồi", "ai biết boss ra đâu k"
    };

    private int stage;
    private int kills;
    private Mob target;
    private long lastAttack;
    private long nextAttackDelay = 600;
    private long restUntil;
    private long nextChat;
    private long lastChangeZone;
    private long arriveTime;
    private int errors;

    public BotNguoiChoi(String name, int gender, int stage) {
        super(headOf(gender), clothesPart(gender, 0, stage), clothesPart(gender, 1, stage), 4, name, null, (short) -1);
        this.gender = (byte) gender;
        this.stage = Math.max(0, Math.min(STAGE_FREE, stage));
        this.nextChat = System.currentTimeMillis() + Util.nextInt(60_000, 300_000);
        applyStageStats();
    }

    private static short headOf(int gender) {
        int[][] heads = {{64, 31, 30}, {9, 29, 32}, {28, 27, 6}};
        int[] h = heads[gender];
        return (short) h[Util.nextInt(0, h.length - 1)];
    }

    // Lấy part áo (type 0 -> body) hoặc quần (type 1 -> leg) cơ bản theo hành tinh và chặng
    private static short clothesPart(int gender, int type, int stage) {
        try {
            int[] list = ConstItem.LIST_ITEM_CLOTHES[gender][type];
            int level = Math.min(STAGE_CLOTHES_LEVEL[Math.max(0, Math.min(STAGE_FREE, stage))], list.length - 1);
            return (short) ItemService.gI().getTemplate(list[level]).part;
        } catch (Exception e) {
            return (short) (type == 0 ? 6 : 7);
        }
    }

    private void applyStageStats() {
        int s = this.stage;
        this.nPoint.power = STAGE_POWER[s] + Util.nextInt(0, (int) Math.min(Integer.MAX_VALUE, STAGE_POWER[s] / 2));
        this.nPoint.tiemNang = 0;
        this.nPoint.hpg = STAGE_HP[s];
        this.nPoint.hpMax = STAGE_HP[s];
        this.nPoint.hp = STAGE_HP[s];
        this.nPoint.mpg = 1_000 + s * 2_000;
        this.nPoint.mpMax = this.nPoint.mpg;
        this.nPoint.mp = this.nPoint.mpg;
        this.nPoint.dameg = STAGE_DAME[s];
        this.nPoint.defg = s * 5;
        this.nPoint.critg = 1 + s;
        this.nPoint.maxStamina = 20_000;
        this.nPoint.stamina = 20_000;
        learnSkills();
    }

    // Kỹ năng cấp thấp theo chặng, giống người chơi mới (không lấy cấp 7 như bot cũ)
    private void learnSkills() {
        this.playerSkill.skills.clear();
        for (NClass n : Manager.gI().NCLASS) {
            if (n.classId != this.gender) {
                continue;
            }
            List<SkillTemplate> templates = n.skillTemplatess;
            addSkill(templates, 0, Math.min(7, 1 + this.stage));
            if (this.stage >= 3) {
                addSkill(templates, 1, Math.min(7, this.stage - 1));
            }
            break;
        }
        if (!this.playerSkill.skills.isEmpty()) {
            this.playerSkill.skillSelect = this.playerSkill.skills.get(0);
        }
    }

    private void addSkill(List<SkillTemplate> templates, int index, int point) {
        if (index >= templates.size()) {
            return;
        }
        SkillTemplate t = templates.get(index);
        for (Skill skill : t.skillss) {
            if (skill.point == point) {
                this.playerSkill.skills.add(new Skill(skill));
                return;
            }
        }
    }

    private int currentMobTemp() {
        if (this.stage >= STAGE_FREE) {
            int s = Util.nextInt(3, 5);
            return STAGE_MOB[s] + (STAGE_BY_PLANET[s] ? this.gender : 0);
        }
        return STAGE_MOB[this.stage] + (STAGE_BY_PLANET[this.stage] ? this.gender : 0);
    }

    // Vào map có quái của chặng hiện tại, ưu tiên map cùng hành tinh
    public boolean goToQuestMap() {
        int mobTemp = currentMobTemp();
        List<Map> same = new ArrayList<>();
        List<Map> other = new ArrayList<>();
        for (Map map : Manager.MAPS) {
            if (map == null || map.zones == null || map.zones.isEmpty() || map.mapId > 50
                    || MapService.gI().isMapOffline(map.mapId)) {
                continue;
            }
            boolean has = false;
            for (Mob m : map.zones.get(0).mobs) {
                if (m.tempId == mobTemp) {
                    has = true;
                    break;
                }
            }
            if (has) {
                (map.planetId == this.gender ? same : other).add(map);
            }
        }
        List<Map> pick = !same.isEmpty() ? same : other;
        if (pick.isEmpty()) {
            return false;
        }
        Map map = pick.get(Util.nextInt(0, pick.size() - 1));
        Zone zone = pickZone(map);
        if (zone == null) {
            return false;
        }
        int x = Util.nextInt(60, Math.max(61, map.mapWidth - 60));
        int y = map.yPhysicInTop(x, 100);
        if (this.zone == null) {
            this.location.x = x;
            this.location.y = y;
            ChangeMapService.gI().goToMap(this, zone);
            this.zone.load_Me_To_Another(this);
        } else {
            ChangeMapService.gI().changeMap(this, zone, x, y);
        }
        this.target = null;
        this.arriveTime = System.currentTimeMillis();
        this.lastChangeZone = this.arriveTime;
        return true;
    }

    private Zone pickZone(Map map) {
        List<Zone> ok = new ArrayList<>();
        for (Zone z : map.zones) {
            if (!z.isFullPlayer()) {
                ok.add(z);
            }
        }
        return ok.isEmpty() ? null : ok.get(Util.nextInt(0, ok.size() - 1));
    }

    @Override
    public void update() {
        try {
            super.update();
            if (this.zone == null || this.isDie()) {
                return;
            }
            long now = System.currentTimeMillis();
            maybeChat(now);
            if (now < this.restUntil) {
                return;
            }
            // Thỉnh thoảng đứng nghỉ như người thật
            if (Util.isTrue(1, 1500)) {
                this.restUntil = now + Util.nextInt(5_000, 20_000);
                return;
            }
            // Đổi khu sau một lúc ở map
            if (now - this.lastChangeZone > Util.nextInt(240_000, 480_000)) {
                goToQuestMap();
                return;
            }
            huntMob(now);
        } catch (Exception e) {
            if (this.errors++ < 3) {
                Logger.logException(BotNguoiChoi.class, e, "Bot " + this.name);
            }
        }
    }

    private void huntMob(long now) {
        if (this.target == null || this.target.isDie() || this.target.zone != this.zone) {
            this.target = findMob();
            if (this.target == null) {
                if (now - this.arriveTime > 20_000) {
                    goToQuestMap();
                }
                return;
            }
        }
        int dx = this.target.location.x - this.location.x;
        int dy = this.target.location.y - this.location.y;
        int dist = (int) Math.sqrt((double) dx * dx + (double) dy * dy);
        if (dist > 50) {
            // Đi bộ tới quái, mỗi nhịp một đoạn
            int step = Math.min(dist - 30, Util.nextInt(35, 55));
            int nx = this.location.x + (int) Math.round((double) dx * step / dist);
            int ny = this.location.y + (int) Math.round((double) dy * step / dist);
            PlayerService.gI().playerMove(this, nx, ny);
            return;
        }
        if (now - this.lastAttack < this.nextAttackDelay) {
            return;
        }
        this.lastAttack = now;
        this.nextAttackDelay = Util.nextInt(500, 1_100);
        if (this.nPoint.mp < this.nPoint.mpMax / 5) {
            this.nPoint.mp = this.nPoint.mpMax; // như người chơi ăn đậu
        }
        if (!this.playerSkill.skills.isEmpty()) {
            this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
        }
        SkillService.gI().useSkill(this, null, this.target, -1, null);
        if (this.target.isDie()) {
            onKill();
        }
    }

    private Mob findMob() {
        int want = currentMobTemp();
        Mob best = null;
        int bestDist = Integer.MAX_VALUE;
        for (Mob m : new ArrayList<>(this.zone.mobs)) {
            if (m == null || m.isDie()) {
                continue;
            }
            boolean match = this.stage >= STAGE_FREE || m.tempId == want;
            if (!match) {
                continue;
            }
            int d = Math.abs(m.location.x - this.location.x);
            if (d < bestDist) {
                bestDist = d;
                best = m;
            }
        }
        return best;
    }

    private void onKill() {
        this.target = null;
        if (this.stage >= STAGE_FREE) {
            return;
        }
        this.kills++;
        if (this.kills >= STAGE_KILLS[this.stage]) {
            this.kills = 0;
            this.stage++;
            applyStageStats();
            if (Util.isTrue(1, 3)) {
                Service.gI().chat(this, "xong nv rồi");
            }
            goToQuestMap();
        }
    }

    private void maybeChat(long now) {
        if (now >= this.nextChat) {
            this.nextChat = now + Util.nextInt(120_000, 600_000);
            if (Util.isTrue(1, 3)) {
                Service.gI().chat(this, CHATS[Util.nextInt(0, CHATS.length - 1)]);
            }
        }
    }

    public int getStage() {
        return this.stage;
    }
}
