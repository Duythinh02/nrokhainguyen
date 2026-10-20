package nro.models.npc_list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import nro.models.combine.CombineService;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.npc.Npc;
import nro.models.player.NPoint;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.map.service.NpcService;
import nro.models.services.TaskService;
import nro.models.map.service.ChangeMapService;
import nro.models.services.ItemService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.shop.ShopService;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 *
 */
public class Bill extends Npc {

    public Bill(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            TaskService.gI().checkDoneTaskTalkNpc(player, this);
            player.idMark.setIndexMenu(ConstNpc.BASE_MENU);

            if (mapId == 154) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "...",
                        "Về\nthánh địa\nKaio", "Từ chối");
            } else if (player.nPoint.power >= POWER_NANG_CAP_SUC_MANH) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Chưa tới giờ thi đấu, xem hướng dẫn để biết thêm chi tiết",
                        "Nói\nchuyện", "Hướng\ndẫn\nthêm", "Đổi thức ăn\nlấy phiếu ăn", "Đổi phiếu ăn\nlấy quà", "Phân rã\nHuỷ Diệt", "Nâng cấp\nsức mạnh", "Từ chối");
            } else {
                // Chưa đủ 120 tỷ: ẩn nút Nâng cấp sức mạnh
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Chưa tới giờ thi đấu, xem hướng dẫn để biết thêm chi tiết",
                        "Nói\nchuyện", "Hướng\ndẫn\nthêm", "Đổi thức ăn\nlấy phiếu ăn", "Đổi phiếu ăn\nlấy quà", "Phân rã\nHuỷ Diệt", "Từ chối");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        switch (mapId) {
            case 48 -> {
                switch (player.idMark.getIndexMenu()) {
                    case ConstNpc.BASE_MENU -> {
                        switch (select) {
                            case 0 -> {
                                if (InventoryService.gI().canOpenBillShop(player)) {
                                    createOtherMenu(player, 2,
                                            "Đói bụng quá... mỗi món đồ Hủy Diệt ngươi cần đưa ta\n"
                                            + "99 phần đồ ăn và 1 món Thần Linh cùng loại, cùng hành tinh.\n"
                                            + "Nếu tâm trạng ta vui ngươi có thể nhận trang bị tăng đến 15%",
                                            "OK", "Từ chối");
                                    player.idMark.setIndexMenu(100);
                                } else {
                                    createOtherMenu(player, 2,
                                            "Ngươi mang 99 phần đồ ăn và món Thần Linh\n"
                                            + "muốn đổi (để trong hành trang) tới đây...\n"
                                            + "rồi ta nói chuyện tiếp.",
                                            "OK");
                                    player.idMark.setIndexMenu(101);
                                }
                            }
                            case 1 ->
                                NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_BILL);
                            case 2 -> {
                                createOtherMenu(player, 10, "Ngươi có muốn đổi thức ăn thành phiếu ăn nhanh không?",
                                        "Đồng ý",
                                        "Từ chối");
                                player.idMark.setIndexMenu(10);
                            }
                            case 3 -> {
                                ShopService.gI().opendShop(player, "SHOP_SU_KIEN_VL", true);
                            }
                            case 4 ->
                                CombineService.gI().openTabCombine(player, CombineService.PHAN_RA_DO_HUY_DIET);
                            case 5 -> {
                                // Dưới 120 tỷ thì ô 5 là "Từ chối"
                                if (player.nPoint.power >= POWER_NANG_CAP_SUC_MANH) {
                                    menuNangCapSucManh(player);
                                }
                            }
                        }
                    }
                    case ConstNpc.MENU_START_COMBINE -> {
                        if (select == 0 && player.combineNew != null
                                && player.combineNew.typeCombine == CombineService.PHAN_RA_DO_HUY_DIET) {
                            CombineService.gI().startCombine(player);
                        }
                    }
                    case MENU_NANG_CAP_SUC_MANH -> {
                        if (select == 0) {
                            nangCapSucManh(player);
                        }
                    }
                    case 10 -> {
                        if (select == 0) {
                            doiNhanhItem(player);
                        }
                    }
                    case 100 -> {
                        if (select == 0 && InventoryService.gI().canOpenBillShop(player)) {
                            ShopService.gI().opendShop(player, "BILL", true);
                        }
                    }
                }
            }

            case 154 -> {
                if (select == 0) {
                    ChangeMapService.gI().changeMap(player, 50, -1, 318, 336);
                }
            }
        }
    }

    // Nâng cấp sức mạnh: mở giới hạn mốc 10 -> NPoint.MAX_LIMIT_BILL, cần 120 tỷ sức mạnh, mỗi mốc 100 điểm săn boss
    private static final int MENU_NANG_CAP_SUC_MANH = 20;
    private static final long POWER_NANG_CAP_SUC_MANH = 120_000_000_000L;
    private static final int COST_NANG_CAP_SUC_MANH = 100;

    // Trả về lý do không nâng cấp được, null nếu nâng cấp được
    private String checkNangCapSucManh(Player player) {
        if (player.nPoint.power < POWER_NANG_CAP_SUC_MANH) {
            return "Ngươi cần đạt " + Util.numberToMoney(POWER_NANG_CAP_SUC_MANH) + " sức mạnh mới được nâng cấp sức mạnh";
        }
        if (player.nPoint.limitPower >= NPoint.MAX_LIMIT_BILL) {
            return "Sức mạnh của ngươi đã đạt tới mức tối đa";
        }
        if (player.nPoint.limitPower < NPoint.MAX_LIMIT) {
            return "Ngươi cần mở hết giới hạn sức mạnh ở Tổ sư Kaio trước";
        }
        if (!player.nPoint.canOpenPower()) {
            return "Ngươi cần đạt " + Util.numberToMoney(player.nPoint.getPowerLimit())
                    + " sức mạnh (giới hạn hiện tại) mới nâng cấp tiếp được";
        }
        if (player.event.getEventPoint() < COST_NANG_CAP_SUC_MANH) {
            return "Ngươi cần " + COST_NANG_CAP_SUC_MANH + " điểm săn boss, hiện có " + player.event.getEventPoint();
        }
        return null;
    }

    private void menuNangCapSucManh(Player player) {
        String err = checkNangCapSucManh(player);
        if (err != null) {
            Service.gI().sendThongBao(player, err);
            return;
        }
        createOtherMenu(player, MENU_NANG_CAP_SUC_MANH,
                "Ta sẽ mở giới hạn sức mạnh của ngươi lên "
                + Util.numberToMoney(player.nPoint.getPowerNextLimit()) + "\n"
                + "Phí: " + COST_NANG_CAP_SUC_MANH + " điểm săn boss (đang có " + player.event.getEventPoint() + ")",
                "Nâng cấp", "Từ chối");
    }

    private void nangCapSucManh(Player player) {
        String err = checkNangCapSucManh(player);
        if (err != null) {
            Service.gI().sendThongBao(player, err);
            return;
        }
        player.event.setEventPoint(player.event.getEventPoint() - COST_NANG_CAP_SUC_MANH);
        player.nPoint.limitPower++;
        Service.gI().sendThongBao(player, "Giới hạn sức mạnh của ngươi đã tăng lên "
                + Util.numberToMoney(player.nPoint.getPowerLimit()));
    }

    private void doiNhanhItem(Player player) {
        int[] itemIds = {1798, 1799, 1800, 1801, 1802};
        Set<Integer> itemIdSet = Arrays.stream(itemIds).boxed().collect(Collectors.toSet());
        int total1805 = 0;

        for (int id : itemIds) {
            int count = 0;
            List<Item> itemsToSubtract = new ArrayList<>();

            for (Item item : player.inventory.itemsBag) {
                if (item != null && item.template != null && item.template.id == id) {
                    count += item.quantity;
                    itemsToSubtract.add(item);
                }
            }

            int soLanDoi = count / 99;
            int soLuongTru = soLanDoi * 99;

            if (soLanDoi > 0) {
                total1805 += soLanDoi;

                int remaining = soLuongTru;
                for (Item item : itemsToSubtract) {
                    if (remaining <= 0) {
                        break;
                    }

                    int toSub = Math.min(remaining, item.quantity);
                    InventoryService.gI().subQuantityItemsBag(player, item, toSub);
                    remaining -= toSub;
                }
            }
        }

        if (total1805 > 0) {
            Item item1805 = ItemService.gI().createNewItem((short) 1805);
            item1805.quantity = total1805;
            InventoryService.gI().addItemBag(player, item1805);

            PlayerService.gI().sendInfoHpMpMoney(player);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn đã đổi thành công " + total1805 + " x Phiếu ăn.");
        } else {
            Service.gI().sendThongBao(player, "Bạn không có đủ 99 phần của bất kỳ vật phẩm nào để đổi.");
        }
    }

}
