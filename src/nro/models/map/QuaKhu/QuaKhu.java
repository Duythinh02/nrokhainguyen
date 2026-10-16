package nro.models.map.QuaKhu;

import nro.models.player.Player;
import nro.models.map.service.MapService;
import nro.models.services.Service;
import nro.models.map.service.ChangeMapService;
import nro.models.utils.TimeUtil;
import java.util.List;

public class QuaKhu {
    
//DEV by Anwin
//Zalo: 0982542412

    public static final byte HOUR_OPEN_MAP_QUAKHU = 0;
    public static final byte MIN_OPEN_MAP_QUAKHU = 0;
    public static final byte SECOND_OPEN_MAP_QUAKHU = 0;

    public static final byte HOUR_CLOSE_MAP_QUAKHU = 0;
    public static final byte MIN_CLOSE_MAP_QUAKHU = 0;
    public static final byte SECOND_CLOSE_MAP_QUAKHU = 0;

    public static final int AVAILABLE = 10000;
    
    private static QuaKhu i;

    public static long TIME_OPEN_QUAKHU;
    public static long TIME_CLOSE_QUAKHU;

    private int day = -1;

    public static QuaKhu gI() {
        if (i == null) {
            i = new QuaKhu();
        }
        i.setTimeJoinMapQuaKhu();
        return i;
    }

    public void setTimeJoinMapQuaKhu() {
        if (i.day == -1 || i.day != TimeUtil.getCurrDay()) {
            i.day = TimeUtil.getCurrDay();
            try {
                TIME_OPEN_QUAKHU = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " 
                        + String.format("%02d:%02d:%02d", HOUR_OPEN_MAP_QUAKHU, MIN_OPEN_MAP_QUAKHU, SECOND_OPEN_MAP_QUAKHU), "dd/MM/yyyy HH:mm:ss");
                TIME_CLOSE_QUAKHU = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " 
                        + String.format("%02d:%02d:%02d", HOUR_CLOSE_MAP_QUAKHU, MIN_CLOSE_MAP_QUAKHU, SECOND_CLOSE_MAP_QUAKHU), "dd/MM/yyyy HH:mm:ss");
            } catch (Exception ignored) {
            }
        }
    }

    private void kickOutOfMapQuaKhu(Player player) {
        if (MapService.gI().isMap5000NamTruoc(player.zone.map.mapId)) {
            Service.getInstance().sendThongBao(player, "Thời gian Du Hành đã hết, bạn sẽ được đưa về Hiện Tại!");
            ChangeMapService.gI().TroVeThoiGian2(player);
            player.nPoint.power *= 100;
            Service.gI().point(player);
        }
    }

    private void ketthucQuaKhu(Player player) {
        List<Player> playersMap = player.zone.getPlayers();
        for (int i = playersMap.size() - 1; i >= 0; i--) {
            Player pl = playersMap.get(i);
            kickOutOfMapQuaKhu(player);
        }
    }

    public void joinMapQuaKhu(Player player) {
        boolean changed = false;
        if (player.clan != null) {
            List<Player> players = player.zone.getPlayers();
            for (Player pl : players) {
            }
        }
    }
    
    public void update(Player player) {
        if (player.zone == null || !MapService.gI().isMapBlackBallWar(player.zone.map.mapId)) {
            try {
                if (player.thoigianduhanh <= 0) {
                    ketthucQuaKhu(player);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
