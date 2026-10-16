package nro.models.map.DragonNamecWar;

import nro.models.player.Player;
import nro.models.map.service.ChangeMapService;
import nro.models.utils.TimeUtil;
import nro.models.utils.Util;
import nro.models.consts.ConstTranhNgocNamek;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @Build Anwin
 */

public class TranhNgoc {

    private static TranhNgoc i;

    private static long TIME_REGISTER;
    private static long TIME_OPEN;
    private static long TIME_CLOSE;

    public static final byte HOUR_REGISTER = 11;
    public static final byte MIN_REGISTER = 50;
    public static final byte HOUR_OPEN = 12;
    public static final byte MIN_OPEN = 00;

    public static final byte HOUR_CLOSE = 12;
    public static final byte MIN_CLOSE = 10;

    private final List<Player> playersFide = new ArrayList<>();
    private final List<Player> playersCadic = new ArrayList<>();

    private int day = -1;

    public static TranhNgoc gI() {
        if (i == null) {
            i = new TranhNgoc();
        }
        i.setTime();
        return i;
    }

    public List<Player> getPlayersCadic() {
        return this.playersCadic;
    }

    public List<Player> getPlayersFide() {
        return this.playersFide;
    }

    public void addPlayersCadic(Player player) {
        synchronized (playersCadic) {
            if (!this.playersCadic.contains(player)) {
                this.playersCadic.add(player);
            }
        }
    }

    public void addPlayersFide(Player player) {
        synchronized (playersFide) {
            if (!this.playersFide.contains(player)) {
                this.playersFide.add(player);
            }
        }
    }

    public void removePlayersCadic(Player player) {
        synchronized (playersCadic) {
            if (this.playersCadic.contains(player)) {
                this.playersCadic.remove(player);
            }
        }
    }

    public void removePlayersFide(Player player) {
        synchronized (playersFide) {
            if (this.playersFide.contains(player)) {
                this.playersFide.remove(player);
            }
        }
    }

    public void setTime() {
        if (i.day == -1 || i.day != TimeUtil.getCurrDay()) {
            i.day = TimeUtil.getCurrDay();
            try {
                TranhNgoc.TIME_OPEN = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + String.format("%02d:%02d:00", HOUR_OPEN, MIN_OPEN), "dd/MM/yyyy HH:mm:ss");
                TranhNgoc.TIME_CLOSE = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + String.format("%02d:%02d:00", HOUR_CLOSE, MIN_CLOSE), "dd/MM/yyyy HH:mm:ss");
                TranhNgoc.TIME_REGISTER = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + String.format("%02d:%02d:00", HOUR_REGISTER, MIN_REGISTER), "dd/MM/yyyy HH:mm:ss");
            } catch (Exception e) {   e.printStackTrace();
            }
        }
    }

    public void update(Player player) {
        try {
            long currentTime = System.currentTimeMillis();
            if (Util.canDoWithTime(player.lastTimeUpdateBallWar, 1000)) {
                player.lastTimeUpdateBallWar = currentTime;
                if (player.zone != null && player.zone.map.mapId == ConstTranhNgocNamek.MAP_ID) {
                    try {
                        if (!isTimeStartWar() || (!player.zone.getPlayersFide().contains(player) && !player.zone.getPlayersCadic().contains(player))) {
                            kickOutOfMap(player);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    if (isTimeStartWar() && playersCadic.contains(player)) {
                        ChangeMapService.gI().changeMap(player, ConstTranhNgocNamek.MAP_ID, -1, Util.nextInt(140, 200), 696);
                    } else if (isTimeStartWar() && playersFide.contains(player)) {
                        ChangeMapService.gI().changeMap(player, ConstTranhNgocNamek.MAP_ID, -1, Util.nextInt(2169, 2240), 1752);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void kickOutOfMap(Player player) {
        player.idMark.setTranhNgoc((byte) -1);
        ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 250);
        player.isHoldNamecBallTranhDoat = false;
        player.tempIdNamecBallHoldTranhDoat = -1;
    }

    public boolean isTimeRegisterWar() {
        long now = System.currentTimeMillis();
        return now > TIME_REGISTER && now < TIME_OPEN;
    }

    public boolean isTimeStartWar() {
        long now = System.currentTimeMillis();
        return now > TIME_OPEN && now < TIME_CLOSE;
    }
}






