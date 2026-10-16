package nro.models.boss;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.consts.BossType;
import nro.models.player.NewPet;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.player.SetClothes;

/**
 * Cân bằng dame 3 hành tinh khi đánh boss (chỉ boss, không ảnh hưởng đánh quái / PvP).
 *
 * - Mỗi người (đệ tử tính chung với chủ) chỉ gây được tối đa X% hpMax boss mỗi giây,
 *   X tùy theo máu boss; tính theo cửa sổ 2 giây.
 * - Trần nhân theo trang bị: không set x0.6, set kích hoạt x1.0, set + đồ Thần x1.5, set + đồ Hủy Diệt x2.0.
 * - Tự sát không bị trần trên, thay bằng sàn/trần % hpMax riêng theo trang bị; mỗi người 1 lần / boss / 60 giây.
 * - Boss bị đánh bởi người chơi từ 2 hành tinh khác nhau: tỷ lệ rơi đồ Thần x1.10, đủ 3 hành tinh: x1.15.
 */
public final class BossDamageLimit {

    private static final long WINDOW_MS = 2000;
    private static final long TU_SAT_COOLDOWN_MS = 60_000;
    private static final long PLANET_WINDOW_MS = 60_000;

    // [hpMax từ mức này trở lên, % hpMax mỗi giây]; dưới mức thấp nhất thì không giới hạn
    private static final long[] HP_STEPS = {2_000_000_000L, 500_000_000L, 50_000_000L, 20_000_000L};
    private static final double[] PERCENT_PER_SEC = {1.5, 2.5, 5, 8};

    // Theo bậc trang bị: 0 = không set, 1 = set kích hoạt, 2 = set + đồ Thần, 3 = set + đồ Hủy Diệt
    private static final double[] TIER_MULTIPLIER = {0.6, 1.0, 1.5, 2.0};
    private static final double[] TU_SAT_MIN_PERCENT = {3, 5, 6.5, 8};
    private static final double[] TU_SAT_MAX_PERCENT = {9, 15, 20, 25};

    private static final double DROP_BONUS_2_PLANETS = 1.10;
    private static final double DROP_BONUS_3_PLANETS = 1.15;

    public static final class State {

        // id chủ -> {thời điểm bắt đầu cửa sổ, dame đã gây trong cửa sổ}
        private final Map<Long, long[]> windows = new ConcurrentHashMap<>();
        // id chủ -> lần tự sát gần nhất
        private final Map<Long, Long> lastTuSat = new ConcurrentHashMap<>();
        // hành tinh (gender) -> lần cuối có người hành tinh đó đánh boss
        private final long[] lastHitByPlanet = new long[3];
        // null = chưa xét; loại boss không đổi nên chỉ xét 1 lần
        private Boolean limited;

        // Gọi khi boss hồi sinh / sang form mới để không giữ dữ liệu cũ
        public void reset() {
            windows.clear();
            lastTuSat.clear();
            java.util.Arrays.fill(lastHitByPlanet, 0);
        }
    }

    private BossDamageLimit() {
    }

    // Đánh thường / skill thường lên boss
    public static long limitAttack(Player plAtt, Player target, long dame) {
        Boss boss = asLimitedBoss(target);
        Player owner = owner(plAtt);
        if (boss == null || owner == null || dame <= 0) {
            return dame;
        }
        long now = System.currentTimeMillis();
        recordPlanet(boss, owner, now);
        if (!isLimitedBoss(boss)) {
            return dame;
        }
        double percentPerSec = percentPerSecond(boss);
        if (percentPerSec <= 0) {
            return dame;
        }
        long budget = Math.max(1, (long) (boss.nPoint.hpMax * percentPerSec / 100 * (WINDOW_MS / 1000.0)
                * TIER_MULTIPLIER[tier(owner)]));
        synchronized (boss.damageLimit) {
            long[] w = boss.damageLimit.windows.computeIfAbsent(owner.id, k -> new long[2]);
            if (now - w[0] >= WINDOW_MS) {
                w[0] = now;
                w[1] = 0;
            }
            long remain = budget - w[1];
            if (remain <= 0) {
                return 0;
            }
            long dealt = Math.min(dame, remain);
            w[1] += dealt;
            return dealt;
        }
    }

    // Tự sát lên boss: sàn/trần theo % hpMax, mỗi người 1 lần / boss / 60 giây
    public static long limitTuSat(Player plAtt, Player target, long dame) {
        Boss boss = asLimitedBoss(target);
        Player owner = owner(plAtt);
        if (boss == null || owner == null) {
            return dame;
        }
        long now = System.currentTimeMillis();
        recordPlanet(boss, owner, now);
        if (!isLimitedBoss(boss) || percentPerSecond(boss) <= 0) {
            return dame;
        }
        Long last = boss.damageLimit.lastTuSat.get(owner.id);
        if (last != null && now - last < TU_SAT_COOLDOWN_MS) {
            return 0;
        }
        boss.damageLimit.lastTuSat.put(owner.id, now);
        int t = tier(owner);
        // Sàn không quá gấp đôi dame gốc, tránh nick yếu tự sát hàng loạt ăn sàn % máu boss
        long min = Math.min((long) (boss.nPoint.hpMax * TU_SAT_MIN_PERCENT[t] / 100), dame * 2);
        long max = (long) (boss.nPoint.hpMax * TU_SAT_MAX_PERCENT[t] / 100);
        return Math.max(min, Math.min(max, dame));
    }

    // Hệ số tỷ lệ rơi đồ Thần theo số hành tinh cùng đánh boss trong 60 giây gần nhất
    public static double godDropMultiplier(Player bossPlayer) {
        if (!(bossPlayer instanceof Boss boss)) {
            return 1;
        }
        long now = System.currentTimeMillis();
        int planets = 0;
        for (long t : boss.damageLimit.lastHitByPlanet) {
            if (t > 0 && now - t <= PLANET_WINDOW_MS) {
                planets++;
            }
        }
        return planets >= 3 ? DROP_BONUS_3_PLANETS : planets == 2 ? DROP_BONUS_2_PLANETS : 1;
    }

    private static Boss asLimitedBoss(Player target) {
        return target instanceof Boss boss && target.nPoint != null ? boss : null;
    }

    private static boolean isLimitedBoss(Boss boss) {
        Boolean limited = boss.damageLimit.limited;
        if (limited == null) {
            limited = computeLimited(boss);
            boss.damageLimit.limited = limited;
        }
        return limited;
    }

    private static boolean computeLimited(Boss boss) {
        if (boss.bossType != null) {
            switch (boss.bossType) {
                case PHOBAN, PHOBANBBH, PHOBANDT, PHOBANBDKB, PHOBANCDRD, PHOBANKGHD, YARDART, SKILLSUMMONED:
                    return false;
                default:
                    break;
            }
        }
        // Boss mô phỏng người chơi / luyện tập / võ đài: không giới hạn
        String pkg = boss.getClass().getPackageName();
        return !(pkg.endsWith(".sieu_hang") || pkg.endsWith(".luyen_tap_tu_dong")
                || pkg.endsWith(".vo_dai_hat_mit") || pkg.endsWith(".dai_hoi_vo_thuat"));
    }

    private static double percentPerSecond(Boss boss) {
        long hpMax = boss.nPoint.hpMax;
        for (int i = 0; i < HP_STEPS.length; i++) {
            if (hpMax >= HP_STEPS[i]) {
                return PERCENT_PER_SEC[i];
            }
        }
        return 0;
    }

    private static Player owner(Player pl) {
        if (pl instanceof Pet pet) {
            return pet.master;
        }
        if (pl instanceof NewPet pet) {
            return pet.master;
        }
        if (pl == null || pl.isBoss) {
            return null;
        }
        return pl;
    }

    private static void recordPlanet(Boss boss, Player owner, long now) {
        if (owner.isBot || owner.gender < 0 || owner.gender > 2) {
            return;
        }
        boss.damageLimit.lastHitByPlanet[owner.gender] = now;
    }

    private static int tier(Player owner) {
        SetClothes s = owner.setClothes;
        if (s == null || !hasActivatedSet(s)) {
            return 0;
        }
        if (s.checkSetDes()) {
            return 3;
        }
        if (s.godClothes) {
            return 2;
        }
        return 1;
    }

    private static boolean hasActivatedSet(SetClothes s) {
        return s.songoku >= 5 || s.thienXinHang >= 5 || s.kirin >= 5 || s.kaioken >= 5 || s.thanVuTruKaio >= 5
                || s.ocTieu >= 5 || s.pikkoroDaimao >= 5 || s.picolo >= 5 || s.lienHoan >= 5 || s.nail >= 5
                || s.kakarot >= 5 || s.cadic >= 5 || s.nappa >= 5 || s.giamSatThuong >= 5 || s.cadicM >= 5;
    }
}
