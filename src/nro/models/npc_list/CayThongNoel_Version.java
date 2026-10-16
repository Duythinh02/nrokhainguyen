package nro.models.npc_list;

/**
 * @author Anwin
 */

import nro.models.utils.Functions;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.consts.ConstNpc;
import nro.models.event.EventManager;
import java.io.IOException;
import nro.models.item.Item;
import nro.models.services.ItemService;
import nro.models.network.Message;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.shop.ShopService;

public class CayThongNoel_Version extends Npc {
    
    public CayThongNoel_Version(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }    

    @Override
    public void openBaseMenu(Player player) {
        if (EventManager.CHRISTMAS) {
            if (this.mapId == 174) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, 
                        "Noel lặng lẽ rơi ngoài ngõ,\n" +
                        "Gió buồn lay nhẹ cánh thông xanh.\n" +
                        "Chuông ngân vọng lại miền xa nhớ,\n" +
                        "Một bóng người xưa vẫn chưa thành.",
                        "Chế tạo\nngười tuyết", "Chế tạo\nngười tuyết\nbăng giá", 
                        "Top\nngười tuyết", "Top\nngười tuyết\nbăng giá", "Đổi điểm\nsự kiện\n[" + player.event.getChristMasPoint() + "]", "Đóng");

            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (EventManager.CHRISTMAS) {
                Item MuiCaRot = InventoryService.gI().findItemBag(player, 1444);
                Item TangTuyet = InventoryService.gI().findItemBag(player, 1445);
                Item TayGo = InventoryService.gI().findItemBag(player, 1446);
                Item KhanChoang = InventoryService.gI().findItemBag(player, 1447);
                Item NonNoel = InventoryService.gI().findItemBag(player, 1451);
                boolean MCR = false;
                boolean TT = false;
                boolean TG = false;
                boolean KC = false;
                boolean NN = false;
                if (MuiCaRot != null && MuiCaRot.quantity >= 1) {
                    MCR = true;
                }
                if (TangTuyet != null && TangTuyet.quantity >= 10) {
                    TT = true;
                }
                if (TayGo != null && TayGo.quantity >= 2) {
                    TG = true;
                }
                if (KhanChoang != null && KhanChoang.quantity >= 5) {
                    KC = true;
                }
                if (NonNoel != null && NonNoel.quantity >= 1) {
                    NN = true;
                }
                if (this.mapId == 174) {
                    if (player.idMark.isBaseMenu()) {
                        switch (select) {
                            case 0: {
                                if (MCR && TT && TG && KC) {
                                    this.createOtherMenu(player, 0,
                                        "|1|Để chế tạo Người Tuyết cần\n"
                                        + "|2|Mũi cà rốt " + MuiCaRot.quantity + "/1\n"
                                        + "|2|Tảng tuyết " + TangTuyet.quantity + "/10\n"
                                        + "|2|Tay gỗ " + TayGo.quantity + "/2\n"
                                        + "|2|Khăn choàng " + KhanChoang.quantity + "/5\n"
                                        + "|2|Giá vàng : 500tr vàng",
                                        "Đồng ý", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "|1|Để chế tạo Người Tuyết cần\n"
                                        + (MuiCaRot == null ? "|7|" : MuiCaRot.quantity >= 1 ? "|2|" : "|7|") + "Mũi cà rốt " + (MuiCaRot == null ? "0" : MuiCaRot.quantity) + "/1\n"
                                        + (TangTuyet == null ? "|7|" : TangTuyet.quantity >= 10 ? "|2|" : "|7|") + "Tảng tuyết " + (TangTuyet == null ? "0" : TangTuyet.quantity) + "/10\n"
                                        + (TayGo == null ? "|7|" : TayGo.quantity >= 2 ? "|2|" : "|7|") + "Tay gỗ " + (TayGo == null ? "0" : TayGo.quantity) + "/2\n"
                                        + (KhanChoang == null ? "|7|" : KhanChoang.quantity >= 5 ? "|2|" : "|7|") + "Khăn choàng " + (KhanChoang == null ? "0" : KhanChoang.quantity) + "/5\n"
                                        + "|2|Giá vàng : 500tr vàng",
                                        "Từ chối");
                                }
                                break;
                            }
                            case 1: {
                                if (MCR && TT && TG && KC && NN) {
                                    this.createOtherMenu(player, 1,
                                        "|1|Để chế tạo Người Tuyết cần\n"
                                        + "|2|Mũi cà rốt " + MuiCaRot.quantity + "/1\n"
                                        + "|2|Tảng tuyết " + TangTuyet.quantity + "/10\n"
                                        + "|2|Tay gỗ " + TayGo.quantity + "/2\n"
                                        + "|2|Khăn choàng " + KhanChoang.quantity + "/5\n"
                                        + "|2|Nón noel " + NonNoel.quantity + "/1\n"
                                        + "|2|Giá vàng : 500tr vàng\n"
                                        + "|2|Giá ngọc : 10 ngọc",
                                        "Đồng ý", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "|1|Để chế tạo Người Tuyết cần\n"
                                        + (MuiCaRot == null ? "|7|" : MuiCaRot.quantity >= 1 ? "|2|" : "|7|") + "Mũi cà rốt " + (MuiCaRot == null ? "0" : MuiCaRot.quantity) + "/1\n"
                                        + (TangTuyet == null ? "|7|" : TangTuyet.quantity >= 10 ? "|2|" : "|7|") + "Tảng tuyết " + (TangTuyet == null ? "0" : TangTuyet.quantity) + "/10\n"
                                        + (TayGo == null ? "|7|" : TayGo.quantity >= 2 ? "|2|" : "|7|") + "Tay gỗ " + (TayGo == null ? "0" : TayGo.quantity) + "/2\n"
                                        + (KhanChoang == null ? "|7|" : KhanChoang.quantity >= 5 ? "|2|" : "|7|") + "Khăn choàng " + (KhanChoang == null ? "0" : KhanChoang.quantity) + "/5\n"
                                        + (NonNoel == null ? "|7|" : NonNoel.quantity >= 1 ? "|2|" : "|7|") + "Nón noel " + (NonNoel == null ? "0" : NonNoel.quantity) + "/1\n"
                                        + "|2|Giá vàng : 500tr vàng\n"
                                        + "|2|Giá ngọc : 10 ngọc",
                                        "Từ chối");
                                }
                                break;
                            }
                            case 2: {
                                Service.gI().sendThongBao(player, "Bảng xếp hạng đang được cập nhật");
                                break;
                            }
                            case 3: {
                                Service.gI().sendThongBao(player, "Bảng xếp hạng đang được cập nhật");
                                break;
                            }
                            case 4: {
                                ShopService.gI().opendShop(player, "CHRIST_MAS_EVENT_2", true);
                                break;
                            }
                        }
                    } else if (player.idMark.getIndexMenu() == 0) {
                        switch (select) {
                            case 0: {
                                if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                                    Service.gI().sendThongBao(player, "Hành trang của bạn không đủ chỗ trống");
                                    return;
                                }
                                if (player.inventory.getGold() < 500_000_000) {
                                    Service.gI().sendThongBao(player, "Bạn không đủ vàng để thực hiện");
                                    return;
                                }
                                try {
                                    Message msgg;
                                    msgg = new Message(-81);
                                    msgg.writer().writeByte(0);
                                    msgg.writer().writeUTF("MaiTienDung");
                                    msgg.writer().writeUTF("MaiTienDung");
                                    msgg.writer().writeShort(tempId);
                                    player.sendMessage(msgg);
                                    msgg.cleanup();
                                    msgg = new Message(-81);
                                    msgg.writer().writeByte(1);
                                    msgg.writer().writeByte(4);
                                    msgg.writer().writeByte(InventoryService.gI().getIndexBag(player, MuiCaRot));
                                    msgg.writer().writeByte(InventoryService.gI().getIndexBag(player, TangTuyet));
                                    msgg.writer().writeByte(InventoryService.gI().getIndexBag(player, TayGo));
                                    msgg.writer().writeByte(InventoryService.gI().getIndexBag(player, KhanChoang));
                                    player.sendMessage(msgg);
                                    msgg.cleanup();
                                    msgg = new Message(-81);
                                    msgg.writer().writeByte(7);
                                    msgg.writer().writeShort(12178);
                                    player.sendMessage(msgg);
                                    msgg.cleanup();
                                    InventoryService.gI().subQuantityItemsBag(player, MuiCaRot, 1);
                                    InventoryService.gI().subQuantityItemsBag(player, TangTuyet, 10);
                                    InventoryService.gI().subQuantityItemsBag(player, TayGo, 2);
                                    InventoryService.gI().subQuantityItemsBag(player, KhanChoang, 5);
                                    Item NguoiTuyet = ItemService.gI().createNewItem((short)1448);
                                    NguoiTuyet.addOptionParam(30, 0);
                                    NguoiTuyet.addOptionParam(93, 30);
                                    InventoryService.gI().addItemBag(player, NguoiTuyet);
                                    player.inventory.gold -= 500_000_000;
                                    player.DuaTopCheTaoNguoiTuyet++;
                                    InventoryService.gI().sendItemBags(player);
                                    Service.gI().sendMoney(player);
                                } catch (IOException e) {
                                }
                                new Thread(() -> {
                                    Functions.sleep(2000);
                                    player.event.addChristMasPoint(1);
                                    Service.gI().sendThongBao(player, "Bạn nhận được 1 điểm sự kiện");
                                }).start();
                                InventoryService.gI().sendItemBags(player);
                                Service.gI().sendMoney(player);
                                break;
                            }
                        }
                    } else if (player.idMark.getIndexMenu() == 1) {
                        switch (select) {
                            case 0: {
                                if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                                    Service.gI().sendThongBao(player, "Hành trang của bạn không đủ chỗ trống");
                                    return;
                                }
                                if (player.inventory.getGold() < 500_000_000) {
                                    Service.gI().sendThongBao(player, "Bạn không đủ vàng để thực hiện");
                                    return;
                                }
                                if (player.inventory.getRuby() < 10) {
                                    Service.gI().sendThongBao(player, "Bạn không đủ hồng ngọc để thực hiện");
                                    return;
                                }
                                try {
                                    Message msgg;
                                    msgg = new Message(-81);
                                    msgg.writer().writeByte(0);
                                    msgg.writer().writeUTF("MaiTienDung");
                                    msgg.writer().writeUTF("MaiTienDung");
                                    msgg.writer().writeShort(tempId);
                                    player.sendMessage(msgg);
                                    msgg.cleanup();
                                    msgg = new Message(-81);
                                    msgg.writer().writeByte(1);
                                    msgg.writer().writeByte(5);
                                    msgg.writer().writeByte(InventoryService.gI().getIndexBag(player, MuiCaRot));
                                    msgg.writer().writeByte(InventoryService.gI().getIndexBag(player, TangTuyet));
                                    msgg.writer().writeByte(InventoryService.gI().getIndexBag(player, TayGo));
                                    msgg.writer().writeByte(InventoryService.gI().getIndexBag(player, KhanChoang));
                                    msgg.writer().writeByte(InventoryService.gI().getIndexBag(player, NonNoel));
                                    player.sendMessage(msgg);
                                    msgg.cleanup();
                                    msgg = new Message(-81);
                                    msgg.writer().writeByte(7);
                                    msgg.writer().writeShort(12179);
                                    player.sendMessage(msgg);
                                    msgg.cleanup();
                                    InventoryService.gI().subQuantityItemsBag(player, MuiCaRot, 1);
                                    InventoryService.gI().subQuantityItemsBag(player, TangTuyet, 10);
                                    InventoryService.gI().subQuantityItemsBag(player, TayGo, 2);
                                    InventoryService.gI().subQuantityItemsBag(player, KhanChoang, 5);
                                    InventoryService.gI().subQuantityItemsBag(player, NonNoel, 1);
                                    Item NguoiTuyetBangGia = ItemService.gI().createNewItem((short)1449);
                                    NguoiTuyetBangGia.addOptionParam(30, 0);
                                    NguoiTuyetBangGia.addOptionParam(93, 30);
                                    InventoryService.gI().addItemBag(player, NguoiTuyetBangGia);
                                    player.inventory.gold -= 500_000_000;
                                    player.inventory.ruby -= 10;
                                    player.DuaTopCheTaoNguoiTuyetBangGia++;
                                    InventoryService.gI().sendItemBags(player);
                                    Service.gI().sendMoney(player);
                                } catch (IOException e) {
                                }
                                new Thread(() -> {
                                    Functions.sleep(2000);
                                    player.event.addChristMasPoint(1);
                                    Service.gI().sendThongBao(player, "Bạn nhận được 1 điểm sự kiện");
                                }).start();
                                InventoryService.gI().sendItemBags(player);
                                Service.gI().sendMoney(player);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
