package nro.models.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.player.NewPet;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.player.SetClothes;
import nro.models.server.Client;

/**
 * Giới hạn dame lên boss nhiệm vụ: Kuku, Mập Đầu Đinh, Rambo, Tiểu đội sát thủ, Fide,
 * Số 19, Số 20, Android 13/14/15, Pic, Poc, King Kong.
 * Boss khác không giới hạn.
 *
 * - Mỗi người (đệ tử tính chung với chủ) chỉ gây được tối đa 8% hpMax boss mỗi giây; tính theo cửa sổ 2 giây.
 * - Trần nhân theo trang bị: không set x0.6, set kích hoạt x1.0, set + đồ Thần x1.5, set + đồ Hủy Diệt x2.0.
 * - Tự sát không bị trần trên, thay bằng sàn/trần % hpMax riêng theo trang bị; mỗi người 1 lần / boss / 60 giây.
 * - Mọi boss bị đánh bởi người chơi từ 2 hành tinh khác nhau: tỷ lệ rơi đồ Thần x1.10, đủ 3 hành tinh: x1.15.
 */
public final class BossDamageLimit {

    private static final long WINDOW_MS = 2000;
    private static final long TU_SAT_COOLDOWN_MS = 60_000;
    private static final long PLANET_WINDOW_MS = 60_000;

    private static final Set<Integer> LIMITED_BOSS_IDS = Set.of(
            BossID.KUKU, BossID.MAP_DAU_DINH, BossID.RAMBO,
            // Tiểu đội sát thủ (cả 3 bộ id)
            BossID.SO_4, BossID.SO_3, BossID.SO_2, BossID.SO_1, BossID.TIEU_DOI_TRUONG,
            BossID.SO_4_NM, BossID.SO_3_NM, BossID.SO_2_NM, BossID.SO_1_NM, BossID.TIEU_DOI_TRUONG_NM,
            BossID.SO_4_NAMEK, BossID.SO_3_NAMEK, BossID.SO_2_NAMEK, BossID.SO_1_NAMEK, BossID.TIEU_DOI_TRUONG_NAMEK,
            BossID.FIDE,
            BossID.ANDROID_19, BossID.DR_KORE,
            BossID.ANDROID_13, BossID.ANDROID_14, BossID.ANDROID_15,
            BossID.PIC, BossID.POC, BossID.KING_KONG);

    // % hpMax mỗi giây
    private static final double PERCENT_PER_SEC = 8;

    // Theo bậc trang bị: 0 = không set, 1 = set kích hoạt, 2 = set + đồ Thần, 3 = set + đồ Hủy Diệt
    private static final double[] TIER_MULTIPLIER = {0.6, 1.0, 1.5, 2.0};
    private static final double[] TU_SAT_MIN_PERCENT = {3, 5, 6.5, 8};
    private static final double[] TU_SAT_MAX_PERCENT = {9, 15, 20, 25};

    private static final double DROP_BONUS_2_PLANETS = 1.10;
    private static final double DROP_BONUS_3_PLANETS = 1.15;

    private static final long CREDIT_WINDOW_MS = 60_000;
    private static final long CREDIT_MIN_PERCENT = 1;

    public static final class State {

        // id chủ -> {thời điểm bắt đầu cửa sổ, dame đã gây trong cửa sổ}
        private final Map<Long, long[]> windows = new ConcurrentHashMap<>();
        // id chủ -> lần tự sát gần nhất
        private final Map<Long, Long> lastTuSat = new ConcurrentHashMap<>();
        // hành tinh (gender) -> lần cuối có người hành tinh đó đánh boss
        private final long[] lastHitByPlanet = new long[3];
        // id chủ -> {lần đánh gần nhất, tổng dame đã gây trong đợt đánh liên tục}, dùng chia công nhiệm vụ
        private final Map<Long, long[]> contributors = new ConcurrentHashMap<>();

        // Gọi khi boss hồi sinh / sang form mới để không giữ dữ liệu cũ
        public void reset() {
            windows.clear();
            lastTuSat.clear();
            contributors.clear();
            java.util.Arrays.fill(lastHitByPlanet, 0);
        }
    }

    private BossDamageLimit() {
    }

    // Đánh thường / skill thường lên boss
    public static long limitAttack(Player plAtt, Player target, long dame) {
        Boss boss = asBoss(target);
        Player owner = owner(plAtt);
        if (boss == null || owner == null || dame <= 0) {
            return dame;
        }
        long now = System.currentTimeMillis();
        recordPlanet(boss, owner, now);
        if (!isLimitedBoss(boss)) {
            recordHit(boss, owner, dame, now);
            return dame;
        }
        long budget = Math.max(1, (long) (boss.nPoint.hpMax * PERCENT_PER_SEC / 100 * (WINDOW_MS / 1000.0)
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
            recordHit(boss, owner, dealt, now);
            return dealt;
        }
    }

    // Tự sát lên boss: sàn/trần theo % hpMax, mỗi người 1 lần / boss / 60 giây
    public static long limitTuSat(Player plAtt, Player target, long dame) {
        Boss boss = asBoss(target);
        Player owner = owner(plAtt);
        if (boss == null || owner == null) {
            return dame;
        }
        long now = System.currentTimeMillis();
        recordPlanet(boss, owner, now);
        if (!isLimitedBoss(boss)) {
            recordHit(boss, owner, dame, now);
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
        long dealt = Math.max(min, Math.min(max, dame));
        recordHit(boss, owner, dealt, now);
        return dealt;
    }

    /**
     * Người được tính nhiệm vụ khi boss chết: người kết liễu (đệ tính cho chủ) + những ai cùng khu,
     * đánh boss trong 60 giây gần nhất và đã gây ít nhất 1% hpMax trong đợt đánh đó.
     */
    public static List<Player> questCreditPlayers(Boss boss, Player plKill) {
        List<Player> result = new ArrayList<>();
        Player killer = ownerOrSelf(plKill);
        if (isRealPlayer(killer)) {
            result.add(killer);
        }
        if (boss == null || boss.nPoint == null) {
            return result;
        }
        long now = System.currentTimeMillis();
        long minDame = Math.max(1, boss.nPoint.hpMax * CREDIT_MIN_PERCENT / 100);
        for (Map.Entry<Long, long[]> e : boss.damageLimit.contributors.entrySet()) {
            long[] c = e.getValue();
            if (now - c[0] > CREDIT_WINDOW_MS || c[1] < minDame) {
                continue;
            }
            if (killer != null && killer.id == e.getKey()) {
                continue;
            }
            Player pl = Client.gI().getPlayer(e.getKey());
            if (isRealPlayer(pl) && pl.zone != null && pl.zone.equals(boss.zone)) {
                result.add(pl);
            }
        }
        return result;
    }

    // Đệ tử / đệ mới -> chủ; còn lại giữ nguyên
    public static Player ownerOrSelf(Player pl) {
        if (pl instanceof Pet pet && pet.master != null) {
            return pet.master;
        }
        if (pl instanceof NewPet pet && pet.master != null) {
            return pet.master;
        }
        return pl;
    }

    private static boolean isRealPlayer(Player pl) {
        return pl != null && !pl.isBot && !pl.isBoss && !pl.isPet && !pl.isNewPet;
    }

    private static void recordHit(Boss boss, Player owner, long dealt, long now) {
        if (dealt <= 0 || owner.isBot || boss.isDie()) {
            return;
        }
        boss.lastTimeHitByPlayer = now;
        long[] c = boss.damageLimit.contributors.computeIfAbsent(owner.id, k -> new long[2]);
        synchronized (c) {
            if (now - c[0] > CREDIT_WINDOW_MS) {
                c[1] = 0; // ngưng đánh quá lâu thì tính lại từ đầu
            }
            c[0] = now;
            c[1] += dealt;
        }
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

    private static Boss asBoss(Player target) {
        return target instanceof Boss boss && target.nPoint != null ? boss : null;
    }

    private static boolean isLimitedBoss(Boss boss) {
        return LIMITED_BOSS_IDS.contains((int) boss.id);
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
