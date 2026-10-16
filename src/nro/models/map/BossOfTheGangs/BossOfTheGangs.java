package nro.models.map.BossOfTheGangs;

/*
 * @Author: MaiTienDung
 */

import nro.models.player.Player;
import nro.models.boss.Boss;
import nro.models.utils.Functions;
import nro.models.utils.Logger;
import nro.models.server.Maintenance;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.MapService;
import nro.models.services.Service;
import nro.models.utils.TimeUtil;
import nro.models.utils.Util;
import nro.models.clan.Clan;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import nro.models.map.service.ItemMapService;
import nro.models.services.ItemTimeService;
import nro.models.boss.Anw.BossOfTheGangs.FideClan;
import nro.models.boss.Anw.BossOfTheGangs.MabuClan;
import nro.models.boss.Anw.BossOfTheGangs.MiNuongClan;
import nro.models.boss.Anw.BossOfTheGangs.ThoBunmaClan;
import nro.models.boss.Anw.BossOfTheGangs.XenBoHungClan;
import nro.models.map.Zone;

@Data
public class BossOfTheGangs implements Runnable {

    public static final int AVAILABLE = 120;
    public static final int TIME_MAP_BOSS_BANG_HOI = 1800000;

    public int id;
    public final List<Zone> zones;

    public Clan clan;
    public boolean isOpened;
    private long lastTimeOpen;
    private long lastTimeUpdateMessage;
    private boolean kickoutBossOfTheGangs;
    private long timeKickOutBossOfTheGangs;
    public List<Boss> bosses = new ArrayList<>();
    public boolean BossDead;
    private boolean callBoss;

    public BossOfTheGangs(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning && isOpened) {
            try {
                long startTime = System.currentTimeMillis();
                update();
                long elapsedTime = System.currentTimeMillis() - startTime;
                long sleepTime = 150 - elapsedTime;
                if (sleepTime > 0) {
                    Functions.sleep(sleepTime);
                }
            } catch (Exception e) {
                Logger.logException(BossOfTheGangs.class, e);
            }
        }
    }

    public void update() {
        if (isOpened) {
            if (Util.canDoWithTime(lastTimeOpen, TIME_MAP_BOSS_BANG_HOI) || (kickoutBossOfTheGangs && Util.canDoWithTime(timeKickOutBossOfTheGangs, 30000))) {
                finish();
                dispose();
            } 
            if (!callBoss) {
                try {
                    switch (clan.boss_clan_round) {
                        case 2:
                            bosses.add(new FideClan(getMapById(249), clan));
                            break;
                        case 3:
                            bosses.add(new XenBoHungClan(getMapById(249), clan));
                            break;
                        case 4:
                            bosses.add(new MabuClan(getMapById(249), clan));
                            break;
                        case 5:
                            bosses.add(new ThoBunmaClan(getMapById(249), clan));
                            break;
                        case 6:
                            bosses.add(new MiNuongClan(getMapById(249), clan));
                            break;
                        default:
                            break;
                    }
                    callBoss = true;
                } catch (Exception exception) {
                    Logger.logException(Boss.class, exception);
                }
            }
            if (!kickoutBossOfTheGangs && (BossDead || Util.canDoWithTime(lastTimeOpen, TIME_MAP_BOSS_BANG_HOI - 30000))) {
                kickoutBossOfTheGangs = true;
                timeKickOutBossOfTheGangs = System.currentTimeMillis();
            }
            if (kickoutBossOfTheGangs && Util.canDoWithTime(lastTimeUpdateMessage, 5000)) {
                lastTimeUpdateMessage = System.currentTimeMillis();
                for (Zone zone : zones) {
                    List<Player> players = zone.getPlayers();
                    for (Player pl : players) {
                        if (MapService.gI().isMapBossBangHoi(pl.zone.map.mapId)) {
                            Service.gI().sendThongBao(pl, "Về khu vực bang sau " + TimeUtil.getTimeLeft(timeKickOutBossOfTheGangs, 30) + " nữa");
                        }
                    }
                }
            }

        }
    }

    public void openBossOfTheGangs(Clan clan) {
        try {
            this.lastTimeOpen = System.currentTimeMillis();
            this.clan = clan;
            this.clan.lastTimeOpenBossOfTheGangs = this.lastTimeOpen;
            this.clan.BossOfTheGang = this;
            this.isOpened = true;
            this.callBoss = false;
            sendTextBossOfTheGangs();
            new Thread(this, "Map Boss Bang Hội: " + this.clan.name).start();
        } catch (Exception e) {
            this.dispose();
        }
    }
    
    public void finish() {
        for (Zone zone : zones) {
            for (int i = zone.getPlayers().size() - 1; i >= 0; i--) {
                if (i < zone.getPlayers().size()) {
                    Player pl = zone.getPlayers().get(i);
                    kickOutOfBossOfTheGangs(pl);
                }
            }

        }
    }

    private void kickOutOfBossOfTheGangs(Player player) {
        if (MapService.gI().isMapBossBangHoi(player.zone.map.mapId)) {
            ChangeMapService.gI().changeMapBySpaceShip(player, 153, -1, Util.nextInt(300, 400));
        }
    }

    public Zone getMapById(int mapId) {
        for (Zone zone : this.zones) {
            if (zone.map.mapId == mapId) {
                return zone;
            }
        }
        return null;
    }

    private void sendTextBossOfTheGangs() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().sendTextMapBossBangHoi(pl);
        }
    }

    private void removeTextBossOfTheGangs() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().removeTextMapBossBangHoi(pl);
        }
    }

    public void dispose() {
        this.clan.updateClanBasicInfo();
        for (Zone zone : zones) {
            for (int i = zone.items.size() - 1; i >= 0; i--) {
                if (i < zone.items.size()) {
                    ItemMapService.gI().removeItemMap(zone.items.get(i));
                }
            }
        }
        for (Boss boss : bosses) {
            if (!boss.isDie()) {
                boss.leaveMap();
            }
        }
        this.removeTextBossOfTheGangs();
        this.bosses.clear();
        this.isOpened = false;
        this.clan.BossOfTheGang = null;
        this.clan = null;
        this.kickoutBossOfTheGangs = false;
        this.BossDead = false;
    }
}
