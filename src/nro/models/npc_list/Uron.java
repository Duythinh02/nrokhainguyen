package nro.models.npc_list;
import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.shop.ShopService;

public class Uron extends Npc {

    public Uron(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player pl) {
        if (canOpenNpc(pl)) {
            createOtherMenu(pl, ConstNpc.BASE_MENU, "Cậu muốn mua gì nào?",
                    "Cửa hàng", "Cửa hàng\nCoin\nKhai Nguyên", "Đóng");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player) && player.idMark.isBaseMenu()) {
            switch (select) {
                case 0 ->
                    ShopService.gI().opendShop(player, "URON", false);
                case 1 -> // Shop đặc biệt: giá hiện kèm icon Coin Khai Nguyên (Bông tai VIP)
                    ShopService.gI().opendShop(player, "URON_COIN", false);
            }
        }
    }
}
