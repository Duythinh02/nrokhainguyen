package nro.models.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nro.models.data.LocalManager;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.player.PlayerEvent;
import nro.models.utils.Logger;

/**
 * Ghi nhận săn boss cho bảng xếp hạng trên web: số boss đã diệt (player.boss_kill)
 * và điểm săn boss (player.event_point_boss, cộng dồn, không bị tiêu).
 * Ghi thẳng vào DB nên không bị lần lưu người chơi hay admin panel ghi đè.
 */
public class BossHuntService {

    private static BossHuntService instance;

    private final ExecutorService writer = Executors.newSingleThreadExecutor(r -> {
        Thread th = new Thread(r, "BossHuntWriter");
        th.setDaemon(true);
        return th;
    });

    public static BossHuntService gI() {
        if (instance == null) {
            instance = new BossHuntService();
        }
        return instance;
    }

    /**
     * Cộng điểm săn boss cho người diệt (giới hạn PlayerEvent.BOSS_POINT_DAILY_LIMIT điểm/ngày),
     * gửi thông báo và ghi nhận bảng xếp hạng
     *
     * @param plKill người (hoặc đệ tử) diệt boss
     * @param point số điểm boss thưởng
     */
    public void rewardBossPoint(Player plKill, int point) {
        rewardBossPoint(plKill, point, true);
    }

    // Boss trên 1 tỷ HP được BIG_BOSS_POINT điểm thay cho điểm thường
    public static final long BIG_BOSS_HP = 1_000_000_000L;
    public static final int BIG_BOSS_POINT = 10;

    /**
     * Gọi từ boss khi bị hạ: boss trên BIG_BOSS_HP máu (máu thật của boss NV 36 - 39 lấy từ QuestBossHp,
     * có thể vượt giới hạn int) nhận BIG_BOSS_POINT điểm
     */
    public void rewardBossPoint(Player plKill, int point, nro.models.boss.Boss boss) {
        if (boss != null && boss.nPoint != null && point > 0) {
            long hp = Math.max(boss.nPoint.hpMax, nro.models.boss.QuestBossHp.get(boss));
            if (hp > BIG_BOSS_HP) {
                point = Math.max(point, BIG_BOSS_POINT);
            }
        }
        rewardBossPoint(plKill, point, true);
    }

    /**
     * @param countBossKill false khi điểm không đến từ boss (vd quái trên 1 tỷ HP): chỉ cộng điểm, không tính
     * thêm 1 boss đã diệt trên bảng xếp hạng
     */
    public void rewardBossPoint(Player plKill, int point, boolean countBossKill) {
        Player owner = plKill;
        if (owner != null && owner.isPet) {
            owner = ((Pet) owner).master;
        }
        if (owner == null || owner.event == null) {
            return;
        }
        // Hợp thể Gogeta với người chơi khác: chia đôi điểm, người dẫn nhận phần lẻ
        Player partner = PlayerFusionService.isLeader(owner) ? owner.fusionPartner : null;
        if (partner != null && partner.event != null && point > 0) {
            int partnerPoint = point / 2;
            point -= partnerPoint;
            int addedPartner = partner.event.addBossPointDaily(partnerPoint);
            Service.gI().sendThongBao(partner, addedPartner > 0
                    ? "+" + addedPartner + " Point chia từ hợp thể (hôm nay " + partner.event.getBossPointToday()
                    + "/" + PlayerEvent.BOSS_POINT_DAILY_LIMIT + ")"
                    : "Hôm nay đã đủ " + PlayerEvent.BOSS_POINT_DAILY_LIMIT + " điểm săn boss");
            addRankPoint(partner, addedPartner);
        }
        int added = owner.event.addBossPointDaily(point);
        if (added > 0) {
            Service.gI().sendThongBao(owner, "+" + added + " Point (hôm nay " + owner.event.getBossPointToday()
                    + "/" + PlayerEvent.BOSS_POINT_DAILY_LIMIT + ")");
        } else {
            Service.gI().sendThongBao(owner, "Hôm nay đã đủ " + PlayerEvent.BOSS_POINT_DAILY_LIMIT
                    + " điểm săn boss, có cái nịt điểm!");
        }
        if (countBossKill) {
            onKillBoss(owner, added);
        } else {
            addRankPoint(owner, added);
        }
    }

    // Cộng điểm bảng xếp hạng săn boss mà không tính thêm 1 boss đã diệt (người được chia điểm khi hợp thể)
    private void addRankPoint(Player pl, int point) {
        if (pl == null || pl.isBot || !pl.isPl() || point <= 0) {
            return;
        }
        final long playerId = pl.id;
        writer.execute(() -> {
            try {
                LocalManager.executeUpdate("update player set event_point_boss = event_point_boss + ? where id = ?", point, playerId);
            } catch (Exception e) {
                Logger.logException(BossHuntService.class, e, "Lỗi ghi điểm săn boss " + playerId);
            }
        });
    }

    /**
     * Gọi khi boss thưởng điểm cho người diệt
     *
     * @param plKill người (hoặc đệ tử) diệt boss
     * @param point số điểm boss thưởng
     */
    public void onKillBoss(Player plKill, int point) {
        Player owner = plKill;
        if (owner != null && owner.isPet) {
            owner = ((Pet) owner).master;
        }
        if (owner == null || owner.isBot || !owner.isPl()) {
            return;
        }
        final long playerId = owner.id;
        final int addPoint = Math.max(point, 0);
        writer.execute(() -> {
            try {
                LocalManager.executeUpdate("update player set boss_kill = boss_kill + 1, event_point_boss = event_point_boss + ? where id = ?",
                        addPoint, playerId);
            } catch (Exception e) {
                Logger.logException(BossHuntService.class, e, "Lỗi ghi điểm săn boss " + playerId);
            }
        });
    }
}
