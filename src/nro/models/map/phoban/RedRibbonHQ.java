package nro.models.map.phoban;
import nro.models.utils.Functions;
import nro.models.boss.Boss;
import nro.models.boss.doanh_trai.NinjaAoTim;
import nro.models.boss.doanh_trai.RobotVeSi;
import nro.models.boss.doanh_trai.TrungUyThep;
import nro.models.boss.doanh_trai.TrungUyTrang;
import nro.models.boss.doanh_trai.TrungUyXanhLo;
import nro.models.clan.Clan;
import nro.models.map.Zone;
import nro.models.mob.Mob;
import nro.models.player.Player;
import nro.models.map.service.ChangeMapService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import lombok.Data;
import nro.models.map.ItemMap;
import nro.models.server.Maintenance;
import nro.models.map.service.ItemMapService;
import nro.models.services.ItemTimeService;
import nro.models.map.service.MapService;
import nro.models.services.Service;
import nro.models.services_dungeon.RedRibbonHQService;
import nro.models.utils.Util;

@Data
public class RedRibbonHQ implements Runnable {

    //bang hội đủ số người mới đc mở
    public static final int N_PLAYER_CLAN = 5;
    //số người đứng cùng khu
    public static final int N_PLAYER_MAP = 1;
    public static final int AVAILABLE = 50;
    //số lần mỗi người được vào doanh trại mỗi ngày
    public static final int MAX_TIMES_PER_DAY = 5;
    public static final int TIME_DOANH_TRAI = 1800000;
    public static final int TIME_PICK_DOANH_TRAI = 300000;

    public int id;
    public final List<Zone> zones;
    private Clan clan;

    private long lastTimeOpen;
    public boolean isOpened;
    public boolean isTimePicking;
    public long lastTimePick;
    public boolean winDT;
    public List<Boss> bosses = new ArrayList<>();
    //phần thưởng chia cho từng boss: {ngọc 3 sao, thỏi vàng, ngọc}
    private static final int REWARD_NGOC_3S = 0, REWARD_THOI_VANG = 1, REWARD_NGOC = 2;
    private final Map<Boss, int[]> rewardByBoss = new HashMap<>();

    public RedRibbonHQ(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
    }

    public void addZone(Zone zone) {
        this.zones.add(zone);
    }

    //số lần người chơi đã vào doanh trại hôm nay
    public static int getTimesToday(Player pl) {
        return Util.isAfterMidnight(pl.lastTimeJoinDT) ? 0 : pl.timesPerDayDT;
    }

    //ghi nhận lượt vào, vào lại cùng 1 lần mở trại không tính thêm lượt
    public static void markJoin(Player pl, RedRibbonHQ dt) {
        if (pl.lastTimeJoinDT < dt.lastTimeOpen) {
            pl.timesPerDayDT = getTimesToday(pl) + 1;
        }
        pl.lastTimeJoinDT = System.currentTimeMillis();
    }

    public static boolean canJoin(Player pl, RedRibbonHQ dt) {
        return (dt != null && pl.lastTimeJoinDT >= dt.lastTimeOpen) || getTimesToday(pl) < MAX_TIMES_PER_DAY;
    }

    //boss doanh trại chết thì rơi phần thưởng đã chia cho nó
    public static void dropBossReward(Boss boss, Player plKill) {
        for (RedRibbonHQ dt : RedRibbonHQService.gI().doanhTrais) {
            int[] reward = dt.rewardByBoss.remove(boss);
            if (reward != null) {
                int n = 0;
                for (int i = 0; i < reward[REWARD_NGOC_3S]; i++) {
                    dropAround(boss, plKill, 16, 1, n++);
                }
                if (reward[REWARD_THOI_VANG] > 0) {
                    dropAround(boss, plKill, 457, reward[REWARD_THOI_VANG], n++);
                }
                if (reward[REWARD_NGOC] > 0) {
                    dropAround(boss, plKill, 77, reward[REWARD_NGOC], n++);
                }
                return;
            }
        }
    }

    private static void dropAround(Boss boss, Player plKill, int tempId, int quantity, int index) {
        int x = boss.location.x + (index % 2 == 0 ? 1 : -1) * ((index + 1) / 2) * 20;
        Service.gI().dropItemMap(boss.zone, new ItemMap(boss.zone, tempId, quantity, x,
                boss.zone.map.yPhysicInTop(x, boss.location.y - 24), plKill.id));
    }

    public Zone getMapById(int mapId) {
        for (Zone zone : this.zones) {
            if (zone.map.mapId == mapId) {
                return zone;
            }
        }
        return null;
    }

    @Override
    public void run() {
        long lastErrorLog = 0;
        while (!Maintenance.isRunning && isOpened) {
            try {
                long startTime = System.currentTimeMillis();
                update();
                Functions.sleep(Math.max(150 - (System.currentTimeMillis() - startTime), 10));
            } catch (Exception e) {
                // Không sleep sau lỗi thì vòng lặp quay 100% CPU và spam log hàng triệu dòng
                if (System.currentTimeMillis() - lastErrorLog > 10000) {
                    lastErrorLog = System.currentTimeMillis();
                    e.printStackTrace();
                }
                Functions.sleep(150);
            }
        }
    }

    public void openDoanhTrai(Player player) {
        try {
            if (Util.isAfterMidnight(player.clan.lastTimeOpenDoanhTrai)) {
                player.clan.countDoanhTraiToday = 0;
            }
            player.clan.countDoanhTraiToday++;
            this.lastTimeOpen = System.currentTimeMillis();
            this.clan = player.clan;
            player.clan.doanhTrai = this;
            player.clan.playerOpenDoanhTrai = player;
            player.clan.lastTimeOpenDoanhTrai = this.lastTimeOpen;
            player.clan.haveGoneDoanhTrai = false;
            sendTextDoanhTrai();
            //Khởi tạo quái, boss
            this.isOpened = true;
            this.init();
        } catch (Exception e) {
            e.printStackTrace();
            player.clan.lastTimeOpenDoanhTrai = 0;
            player.clan.haveGoneDoanhTrai = false;
            player.clan.countDoanhTraiToday = 0;
            this.dispose();
            return;
        }
        List<Player> plJoinDT = new ArrayList();
        //Đưa thành viên vào doanh trại
        for (Player pl : player.zone.getPlayers()) {
            if (pl != null && !pl.equals(player) && pl.clan != null
                    && pl.clan.equals(player.clan) && pl.location.x >= 1285
                    && pl.location.x <= 1645) {
                plJoinDT.add(pl);
            }
        }
        for (Player pl : plJoinDT) {
            if (pl.isDie()) {
                continue;
            }
            if (!canJoin(pl, this)) {
                Service.gI().sendThongBao(pl, "Hôm nay bạn đã vào doanh trại " + MAX_TIMES_PER_DAY + " lần rồi");
                continue;
            }
            markJoin(pl, this);
            ChangeMapService.gI().changeMapInYard(pl, 53, -1, 60);
        }
        markJoin(player, this);
        ChangeMapService.gI().changeMapInYard(player, 53, -1, 60);
    }

    private void init() {
        long totalDamage = 0;
        long totalHp = 0;

        for (Player player : this.clan.membersInGame) {
            if (player != null && player.nPoint != null) {
                totalDamage += player.nPoint.dame;
                totalHp += player.nPoint.hpMax;
            }
        }

        // Hồi sinh quái
        for (Zone zone : this.zones) {
            for (Mob mob : zone.mobs) {
                long mobTempId = mob.tempId;
                mob.point.dame = (int) ((mobTempId != 0) ? Math.min(totalHp / mobTempId, 2_147_483_647L) : 0);
                mob.point.maxHp = (int) ((mobTempId != 0) ? Math.min(totalDamage * mobTempId, 2_147_483_647) : 0);
                mob.lvMob = 0;
                mob.hoiSinh();
                mob.hoiSinhMobPhoBan();
            }

            long dame = totalHp / 20;
            long hp = totalDamage * 50;

            if (zone.map.mapId == 59) {
                try {
                    long bossDamage = Math.min(dame, 200_000_000L);
                    long bossMaxHealth = Math.min(hp, 2_147_483_647L);
                    bosses.add(new TrungUyTrang(
                            zone,
                            (int) bossDamage,
                            (int) bossMaxHealth
                    ));
                } catch (Exception e) {
                }
            }
            if (zone.map.mapId == 62) {
                try {
                    long bossDamage = Math.min((long) (dame * 1.1), 200_000_000L);
                    long bossMaxHealth = Math.min((long) (hp * 1.1), 2_147_483_647);
                    bosses.add(new TrungUyXanhLo(
                            zone,
                            (int) bossDamage,
                            (int) bossMaxHealth
                    ));
                } catch (Exception e) {
                }
            }
            if (zone.map.mapId == 55) {
                try {
                    long bossDamage = Math.min((long) (dame * 1.15), 200_000_000L);
                    long bossMaxHealth = Math.min((long) (hp * 1.15), 2_147_483_647L);
                    bosses.add(new TrungUyThep(
                            zone,
                            (int) bossDamage,
                            (int) bossMaxHealth
                    ));
                } catch (Exception e) {
                }
            }
            if (zone.map.mapId == 54) {
                try {
                    long bossDamage = Math.min((long) (dame * 1.2), 200_000_000L);
                    long bossMaxHealth = Math.min((long) (hp * 1.2), 2_147_483_647);
                    bosses.add(new NinjaAoTim(
                            zone,
                            clan,
                            (int) bossDamage,
                            (int) bossMaxHealth
                    ));
                } catch (Exception e) {
                }
            }

            if (zone.map.mapId == 57) {
                try {
                    long bossDamage = Math.min((long) (dame * 1.3), 200_000_000L);
                    long bossMaxHealth = Math.min((long) (hp * 1.3), 2_147_483_647);
                    for (int i = 0; i < 4; i++) {
                        bosses.add(new RobotVeSi(
                                zone,
                                i,
                                (int) bossDamage,
                                (int) bossMaxHealth
                        ));
                    }
                } catch (Exception e) {
                }
            }
        }
        // Mỗi lượt rơi tổng 3-7 viên ngọc 3 sao, 400-500 thỏi vàng, 40k-50k ngọc, chia đều cho các boss
        this.rewardByBoss.clear();
        List<Boss> shuffled = new ArrayList<>(this.bosses);
        Collections.shuffle(shuffled);
        for (Boss boss : shuffled) {
            this.rewardByBoss.put(boss, new int[3]);
        }
        splitReward(shuffled, REWARD_NGOC_3S, Util.nextInt(3, 7));
        splitReward(shuffled, REWARD_THOI_VANG, Util.nextInt(400, 500));
        splitReward(shuffled, REWARD_NGOC, Util.nextInt(40_000, 50_000));
        Executors.newSingleThreadExecutor().submit(this, "Doanh Trại: " + this.clan.name);
    }

    //chia đều total cho các boss, phần dư dồn cho các boss đầu danh sách (đã xáo trộn)
    private void splitReward(List<Boss> shuffled, int type, int total) {
        int n = shuffled.size();
        if (n == 0) {
            return;
        }
        for (int i = 0; i < n; i++) {
            this.rewardByBoss.get(shuffled.get(i))[type] = total / n + (i < total % n ? 1 : 0);
        }
    }

    public void update() {
        if (isOpened) {
            if ((!isTimePicking && Util.canDoWithTime(lastTimeOpen, TIME_DOANH_TRAI)) || (isTimePicking && Util.canDoWithTime(lastTimePick, TIME_PICK_DOANH_TRAI))) {
                try {
                    finish();
                } finally {
                    dispose();
                }
                return;
            }

            boolean allCharactersDead = true;
            for (Zone zone : zones) {
                for (Mob mob : zone.mobs) {
                    if (!mob.isDie()) {
                        allCharactersDead = false;
                        break;
                    }
                }

                if (allCharactersDead) {
                    for (Player boss : zone.getBosses()) {
                        if (!boss.isDie()) {
                            allCharactersDead = false;
                            break;
                        }
                    }
                }
            }
            if (allCharactersDead && !winDT) {
                winDT = true;
                for (Zone zone : zones) {
                    List<Player> players = zone.getPlayers();
                    for (Player pl : players) {
                        Service.gI().sendThongBao(pl, "Mau đi tìm Độc Nhãn");
                    }
                }
            }

        }
    }

    public ItemMap NR(Zone zone) {
        int x = Util.nextInt(100, zone.map.mapWidth - 100);
        int y = zone.map.yPhysicInTop(x, 100);
        int nr = Util.isTrue(1, 500) ? Util.nextInt(14, 18) : Util.nextInt(18, 20);
        ItemMap it = new ItemMap(zone, nr, 1, x, y, -1);
        return it;
    }

    public void randomNR() {
        for (Zone zone : zones) {
            Service.gI().dropItemMap(zone, NR(zone));
            Service.gI().dropItemMap(zone, NR(zone));
            Service.gI().dropItemMap(zone, NR(zone));
            if (Util.isTrue(1, 2)) {
                Service.gI().dropItemMap(zone, NR(zone));
            }
            if (Util.isTrue(1, 3)) {
                Service.gI().dropItemMap(zone, NR(zone));
            }
            if (Util.isTrue(1, 4)) {
                Service.gI().dropItemMap(zone, NR(zone));
            }
            if (Util.isTrue(1, 5)) {
                Service.gI().dropItemMap(zone, NR(zone));
            }
        }
    }

    public void finish() {
        for (Zone zone : zones) {
            for (int i = zone.getPlayers().size() - 1; i >= 0; i--) {
                if (i < zone.getPlayers().size()) {
                    Player pl = zone.getPlayers().get(i);
                    kickOutOfDT(pl);
                }
            }
        }
    }

    private void kickOutOfDT(Player player) {
        if (player == null || player.zone == null) {
            return;
        }
        if (MapService.gI().isMapDoanhTrai(player.zone.map.mapId)) {
            Service.gI().sendThongBao(player, "Đã hết thời gian, bạn sẽ được đưa về nhà");
            ChangeMapService.gI().changeMapBySpaceShip(player, 21 + player.gender, -1, -1);
        }
    }

    private void sendTextDoanhTrai() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().sendTextDoanhTrai(pl);
        }
    }

    public void sendTextTimePickDoanhTrai() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().sendTextTimePickDoanhTrai(pl);
        }
    }

    private void removeTextDoanhTrai() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().removeTextDoanhTrai(pl);
        }
    }

    public void updateHPDame() {
        long totalDame = 0;
        long totalHp = 0;
        for (Player pl : this.clan.membersInGame) {
            if (pl != null && pl.nPoint != null) {
                totalDame += pl.nPoint.dame;
                totalHp += pl.nPoint.hpMax;
            }
        }

        //update hp dame quái
        for (Zone zone : this.zones) {
            for (Mob mob : zone.mobs) {
                if (mob.isDie()) {
                    continue;
                }
                mob.point.dame = (int) (totalHp / mob.tempId < 2_147_483_647 ? totalHp / mob.tempId : 2_147_483_647);
                mob.point.maxHp = (int) (totalDame/3 * mob.tempId < 2_147_483_647 ? totalDame * mob.tempId : 2_147_483_647);
                mob.point.hp = mob.point.maxHp;
                mob.setTiemNang();
            }
        }

        long dame = totalHp / 20;
        long hp = totalDame * 50;
        for (Boss boss : bosses) {
            if (boss.isDie() || boss.zone == null) {
                continue;
            }
            if (boss.zone.map.mapId == 59) {
                try {
                    long bossDamage = (dame);
                    long bossMaxHealth = (hp);
                    bossDamage = Math.min(bossDamage, 200000000L);
                    bossMaxHealth = Math.min(bossMaxHealth, 2000000000L);
                    boss.nPoint.hpMax = (int) bossMaxHealth;
                    boss.nPoint.dame = (int) bossDamage;
                    boss.nPoint.hp = boss.nPoint.hpMax;
                } catch (Exception exception) {
                }
            }
            if (boss.zone.map.mapId == 62) {
                try {
                    long bossDamage = (long) (dame * 1.1);
                    long bossMaxHealth = (long) (hp * 1.1);
                    bossDamage = Math.min(bossDamage, 200000000L);
                    bossMaxHealth = Math.min(bossMaxHealth, 2000000000L);
                    boss.nPoint.hpMax = (int) bossMaxHealth;
                    boss.nPoint.dame = (int) bossDamage;
                    boss.nPoint.hp = boss.nPoint.hpMax;
                } catch (Exception exception) {
                }
            }
            if (boss.zone.map.mapId == 55) {
                try {
                    long bossDamage = (long) (dame * 1.15);
                    long bossMaxHealth = (long) (hp * 1.15);
                    bossDamage = Math.min(bossDamage, 200000000L);
                    bossMaxHealth = Math.min(bossMaxHealth, 2000000000L);
                    boss.nPoint.hpMax = (int) bossMaxHealth;
                    boss.nPoint.dame = (int) bossDamage;
                    boss.nPoint.hp = boss.nPoint.hpMax;
                } catch (Exception exception) {
                }
            }
            if (boss.zone.map.mapId == 54) {
                try {
                    long bossDamage = (long) (dame * 1.2);
                    long bossMaxHealth = (long) (hp * 1.2);
                    bossDamage = Math.min(bossDamage, 200000000L);
                    bossMaxHealth = Math.min(bossMaxHealth, 2000000000L);
                    if (boss.id >= -14 && boss.id <= -9) {
                        bossDamage /= 10;
                        bossMaxHealth /= 10;
                    }
                    boss.nPoint.hpMax = (int) bossMaxHealth;
                    boss.nPoint.dame = (int) bossDamage;
                    boss.nPoint.hp = boss.nPoint.hpMax;
                } catch (Exception exception) {
                }
            }

            if (boss.zone.map.mapId == 57) {
                try {
                    long bossDamage = (long) (dame * 1.3);
                    long bossMaxHealth = (long) (hp * 1.3);
                    bossDamage = Math.min(bossDamage, 200000000L);
                    bossMaxHealth = Math.min(bossMaxHealth, 2000000000L);
                    boss.nPoint.hpMax = (int) bossMaxHealth;
                    boss.nPoint.dame = (int) bossDamage;
                    boss.nPoint.hp = boss.nPoint.hpMax;
                } catch (Exception exception) {
                }
            }
        }

    }

    // Mỗi bước bọc try riêng: lỗi giữa chừng không được để isOpened = true (luồng sẽ dispose lại mãi)
    private void dispose() {
        try {
            this.removeTextDoanhTrai();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // remove bosses
        for (Boss boss : bosses) {
            try {
                if (!boss.isDie()) {
                    boss.leaveMap();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // remove itemmap
        for (Zone zone : zones) {
            try {
                for (int i = zone.items.size() - 1; i >= 0; i--) {
                    if (i < zone.items.size()) {
                        ItemMapService.gI().removeItemMap(zone.items.get(i));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.bosses.clear();
        this.rewardByBoss.clear();
        this.winDT = false;
        this.isOpened = false;
        if (this.clan != null) {
            this.clan.haveGoneDoanhTrai = true;
            this.clan.doanhTrai = null;
        }
        this.clan = null;
        this.isTimePicking = false;
    }
}
