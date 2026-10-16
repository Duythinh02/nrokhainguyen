package nro.models.npc_list;
import nro.models.clan.ClanMember;
import nro.models.consts.ConstNpc;
import nro.models.map.phoban.RedRibbonHQ;
import nro.models.services_dungeon.RedRibbonHQService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.map.service.NpcService;
import nro.models.services.Service;
import nro.models.utils.TimeUtil;
import nro.models.utils.Util;

public class LinhCanh extends Npc {

    public LinhCanh(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (player.clan == null) {
                NpcService.gI().createTutorial(player, tempId, this.avartar,
                        "Chỉ tiếp các bang hội, miễn tiếp khách vãng lai");
                return;
            }
            if (!player.getSession().actived) {
                    Service.gI().sendThongBao(player, "Vui lòng mở thành viên trước");
                return;
            }
            if (player.clan.getMembers().size() < RedRibbonHQ.N_PLAYER_CLAN) {
                NpcService.gI().createTutorial(player, tempId, this.avartar,
                        "Bang hội phải có ít nhất 5 thành viên mới có thể tham gia");
                return;
            }
            int countToday = RedRibbonHQ.getTimesToday(player);
            if (!RedRibbonHQ.canJoin(player, player.clan.doanhTrai)) {
                createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Hôm nay ngươi đã vào trại " + countToday + " lần rồi\n"
                        + "Nên ngươi không thể vào được nữa.\n"
                        + "Hãy chờ đến ngày mai để có thể vào miễn phí", "OK", "Hướng\ndẫn\nthêm");
                return;
            }
            if (player.clan.doanhTrai != null) {
                createOtherMenu(player, ConstNpc.MENU_JOIN_DOANH_TRAI,
                        "Bang hội của ngươi đang đánh trại độc nhãn\nThời gian còn lại là "
                        + TimeUtil.getTimeLeft(player.clan.doanhTrai.getLastTimeOpen(), RedRibbonHQ.TIME_DOANH_TRAI / 1000)
                        + ". Ngươi có muốn tham gia không?",
                        "Tham gia", "Không", "Hướng\ndẫn\nthêm");
                return;
            }
            int nPlSameClan = 0;
            for (Player pl : player.zone.getPlayers()) {
                if (!pl.equals(player) && pl.clan != null
                        && pl.clan.equals(player.clan) && pl.location.x >= 1285
                        && pl.location.x <= 1645) {
                    nPlSameClan++;
                }
            }
            if (nPlSameClan < RedRibbonHQ.N_PLAYER_MAP) {
                createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Ngươi phải có ít nhất " + RedRibbonHQ.N_PLAYER_MAP + " đồng đội cùng bang đứng gần mới có thể vào\n"
                        + "tuy nhiên ta khuyên ngươi nên đi cùng với 3-4 người để khỏi chết. "
                        + "Hahaha.", "OK", "Hướng\ndẫn\nthêm");
                return;
            }
            createOtherMenu(player, ConstNpc.MENU_JOIN_DOANH_TRAI,
                    "Hôm nay ngươi đã vào trại " + countToday + "/" + RedRibbonHQ.MAX_TIMES_PER_DAY + " lần. Ngươi có muốn vào\nkhông?\nĐể vào, ta khuyên ngươi nên có 3-4 người cùng bang đi cùng.",
                    "Vào\n(miễn phí)", "Không", "Hướng\ndẫn\nthêm");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (player.idMark.getIndexMenu()) {
                case ConstNpc.MENU_JOIN_DOANH_TRAI -> {
                    if (select == 0) {
                        RedRibbonHQService.gI().joinDoanhTrai(player);
                    } else if (select == 2) {
                        NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_DOANH_TRAI);
                    }
                }
                case ConstNpc.IGNORE_MENU -> {
                    if (select == 1) {
                        NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_DOANH_TRAI);
                    }
                }
            }
        }
    }
}
