package nro.models.boss;

/**
 * Máu (trong game, đã tính nhân 2) của boss nhiệm vụ 36 - 39. Ghi đè máu trong BossData.
 * Máu tối đa hiển thị được là Integer.MAX_VALUE (~2,147 tỷ), vượt thì chặn ở mức đó.
 * Boss trên 1,5 tỷ máu chỉ nhận 70% sát thương (Boss.damageTakenScale, áp dụng ở NPoint.subHP).
 */
public final class QuestBossHp {

    private static final long TY = 1_000_000_000L;
    private static final long TR = 1_000_000L;
    private static final long GIAM_SAT_THUONG_TU = 1 * TY + 500 * TR;

    private QuestBossHp() {
    }

    // Siêu Bojack: dạng 1 của Bojack và boss Siêu Bojack riêng dùng chung máu
    private static final long SIEU_BOJACK_HP = 2 * TY + 500 * TR;

    // Trả về -1 nếu boss không nằm trong bảng
    public static long get(Boss boss) {
        return switch ((int) boss.id) {
            // NV 36 Băng hải tặc vũ trụ: từ 1 tỷ, mỗi boss +300 triệu
            case BossID.BUJIN -> 1 * TY;
            case BossID.KOGU -> 1 * TY + 300 * TR;
            case BossID.ZANGYA -> 1 * TY + 600 * TR;
            case BossID.BIDO -> 1 * TY + 900 * TR;
            // Bojack (dạng 0) biến thành Siêu Bojack (dạng 1)
            case BossID.BOJACK -> boss.currentLevel == 0 ? 2 * TY + 200 * TR : SIEU_BOJACK_HP;
            case BossID.SUPER_BOJACK -> SIEU_BOJACK_HP;
            // NV 38 Rồng ác Tà Long: từ 1 tỷ, mỗi boss +300 triệu
            case BossID.KHIDOT -> 1 * TY;
            case BossID.NHATTHAN -> 1 * TY + 300 * TR;
            case BossID.NGUYETTHAN -> 1 * TY + 600 * TR;
            case BossID.TA_LONG -> 1 * TY + 900 * TR;
            // NV 39 Kẻ sống sót cuối cùng: từ 1,5 tỷ, mỗi boss +400 triệu
            case BossID.GasManager -> 1 * TY + 500 * TR;
            case BossID.OilManager -> 1 * TY + 900 * TR;
            case BossID.ElecManager -> 2 * TY + 300 * TR;
            case BossID.MackiManager -> 2 * TY + 700 * TR;
            case BossID.GranolaManager -> 3 * TY + 100 * TR;
            default -> -1;
        };
    }

    public static void apply(Boss boss) {
        long realHp = get(boss);
        if (realHp <= 0) {
            boss.damageTakenScale = 1;
            return;
        }
        boss.nPoint.hpg = (int) Math.min(realHp, Integer.MAX_VALUE);
        boss.damageTakenScale = realHp > GIAM_SAT_THUONG_TU ? 0.7 : 1;
        boss.nPoint.hp = boss.nPoint.hpg;
    }
}
