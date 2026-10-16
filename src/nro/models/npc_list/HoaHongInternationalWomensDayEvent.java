package nro.models.npc_list;

/**
 * @author Anwin
 */

import nro.models.services.InventoryService;
import nro.models.map.service.MapService;
import nro.models.services.Service;
import nro.models.utils.Util;
import nro.models.consts.ConstNpc;
import java.util.List;
import nro.models.item.Item;
import nro.models.services.ItemService;
import nro.models.map.Map;
import nro.models.npc.Npc;
import nro.models.player.Player;

public class HoaHongInternationalWomensDayEvent extends Npc {

    private final byte COUNT_CHANGE = 1;
    private int count;

    public HoaHongInternationalWomensDayEvent(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    private void haiHoaHong(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Bạn không đủ hành trang để hái bông hoa hồng.");
            return;
        }
        Item hoaHong = ItemService.gI().createNewItem((short) 1530);
        hoaHong.addOptionParam(30, 0);
        hoaHong.addOptionParam(87, 0);
        int quality = Util.nextInt(1, 3);
        hoaHong.quantity = quality;
        InventoryService.gI().addItemBag(player, hoaHong);
        Service.gI().sendThongBao(player, "Bạn đã hái bông hoa hồng, nhận được x" + quality + " Bông hoa hồng");
        InventoryService.gI().sendItemBags(player);
        count++;
        if (this.count >= COUNT_CHANGE) {
            List<Player> playersMap;
            playersMap = player.zone.getPlayers();
            count = 0;
            this.map.npcs.remove(this);
            Map mapHoahong = MapService.gI().getMapForHoaHong();
            this.mapId = mapHoahong.mapId;
            this.cx = Util.nextInt(100, mapHoahong.mapWidth - 100);
            this.cy = mapHoahong.yPhysicInTop(this.cx, 0);
            this.map = mapHoahong;
            this.map.npcs.add(this);
            for (Player pl : playersMap) {
                pl.zone.mapInfo(pl);
            }
            System.out.println("hoa hồng mọc tại map " + mapHoahong.mapName);
        }

    }

    @Override
    public void openBaseMenu(Player player) {
        player.idMark.setIndexMenu(ConstNpc.BASE_MENU);
        if (this.mapId != player.zone.map.mapId) {
            Service.gI().sendThongBao(player, "Hoa hồng không thể hái được nữa, vui lòng đi qua chỗ khác để hái.");
            Service.gI().hideWaitDialog(player);
            return;
        }
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Hoa hồng", 
                "Hái", "Bỏ qua");

    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (player.idMark.isBaseMenu()) {
            switch (select) {
                case 0: {
                    haiHoaHong(player);
                    break;
                }
            }
        }
    }
}
