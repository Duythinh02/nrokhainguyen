package nro.models.shop_ky_gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nro.models.consts.ConstItem;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.map.service.NpcService;
import nro.models.network.Message;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.CoinKhaiNguyenService;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 * 
 */

public class ConsignShopService {

    private static ConsignShopService instance;

    public static ConsignShopService gI() {
        if (instance == null) {
            instance = new ConsignShopService();
        }
        return instance;
    }

    private List<ConsignItem> getItemKyGui2(Player pl, byte tab, byte to, byte max) {
        List<ConsignItem> filtered = ConsignShopManager.gI().listItem.stream()
                .filter(it -> it != null && it.tab == tab && !it.isBuy)
                .sorted(Comparator.comparingInt((ConsignItem it) -> it.isUpTop).reversed())
                .collect(Collectors.toList());

        List<ConsignItem> result = new ArrayList<>();
        for (int i = to; i <= max && i < filtered.size(); i++) {
            result.add(filtered.get(i));
        }
        return result;
    }

    private List<ConsignItem> getItemKyGui(Player pl, byte tab, byte... max) {
        List<ConsignItem> filtered = ConsignShopManager.gI().listItem.stream()
                .filter(it -> it != null && it.tab == tab && !it.isBuy && it.player_sell != pl.id)
                .sorted(Comparator.comparingInt((ConsignItem it) -> it.isUpTop).reversed())
                .collect(Collectors.toList());

        List<ConsignItem> result = new ArrayList<>();

        if (max.length == 2) {
            int from = max[0], to = max[1];
            for (int i = from; i < to && i < filtered.size(); i++) {
                result.add(filtered.get(i));
            }
        } else if (max.length == 1) {
            int limit = max[0];
            for (int i = 0; i < limit && i < filtered.size(); i++) {
                result.add(filtered.get(i));
            }
        } else {
            return filtered;
        }

        return result;
    }

    private boolean isKyGui(Item item) {
        switch (item.template.type) {
            case 27:
                switch (item.template.id) {
                    case 921:
                    case 1155:
                    case 1156:
                    case 568:
                        return true;
                }
                return false;
            case 21:
            case 72:
                return true;
        }
        for (int i = 0; i < item.itemOptions.size(); i++) {
            if (item.itemOptions.get(i).optionTemplate.id == 86) {
                return true;
            }
        }
        return false;
    }

    private boolean SubThoiVang(Player pl, int quatity) {
        for (Item item : pl.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 457 && item.quantity >= quatity) {
                nro.models.services.InventoryService.gI().subQuantityItemsBag(pl, item, quatity);
                return true;
            }
        }
        return false;
    }

    // synchronized: tránh 2 người cùng mua 1 món
    public synchronized void buyItem(Player pl, int id) {
        if (pl.nPoint.power < 17000000000L) {
            Service.gI().sendThongBao(pl, "Yêu cầu sức mạnh lớn hơn 17 tỷ");
            openShopKyGui(pl);
            return;
        }
        ConsignItem it = getItemBuy(id);
        if (it == null || it.isBuy) {
            Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
            return;
        }
        if (it.player_sell == pl.id) {
            Service.gI().sendThongBao(pl, "Không thể mua vật phẩm bản thân đăng bán");
            openShopKyGui(pl);
            return;
        }
        if (!it.isCoin()) {
            Service.gI().sendThongBao(pl, "Ký gửi bằng vàng/ngọc đã ngừng, chỉ mua được món đăng bán bằng " + CoinKhaiNguyenService.NAME);
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy");
            return;
        }
        if (!CoinKhaiNguyenService.sub(pl, it.coinSell)) {
            Service.gI().sendThongBao(pl, "Bạn không đủ " + it.coinSell + " " + CoinKhaiNguyenService.NAME
                    + " (đang có " + CoinKhaiNguyenService.count(pl) + ")");
            return;
        }
        Item item = toItem(it);
        it.isBuy = true;
        InventoryService.gI().addItemBag(pl, item);
        InventoryService.gI().sendItemBags(pl);
        Service.gI().sendThongBao(pl, "Bạn đã nhận được " + item.template.name);
        ConsignShopManager.gI().save();
        openShopKyGui(pl);
    }

    // Tạo item trả cho người chơi; copy option để item trong túi không dùng chung object với tin ký gửi
    private Item toItem(ConsignItem it) {
        Item item = ItemService.gI().createNewItem(it.itemId);
        item.quantity = it.quantity;
        for (ItemOption io : it.options) {
            item.itemOptions.add(new ItemOption(io));
        }
        return item;
    }

    // Tìm món chưa bán theo id (duyệt thẳng, không cần lọc + sắp xếp cả shop)
    public ConsignItem getItemBuy(int id) {
        for (ConsignItem it : ConsignShopManager.gI().listItem) {
            if (it != null && it.id == id && !it.isBuy) {
                return it;
            }
        }
        return null;
    }

    public ConsignItem getItemBuy(Player pl, int id) {
        for (ConsignItem it : ConsignShopManager.gI().listItem) {
            if (it != null && it.id == id && it.player_sell == pl.id) {
                return it;
            }
        }
        return null;
    }

    public void openShopKyGui(Player pl, byte index, int page) {
        if (page > getItemKyGui(pl, index).size()) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-100);
            msg.writer().writeByte(index);
            List<ConsignItem> items = getItemKyGui(pl, index);
            List<ConsignItem> itemsSend = getItemKyGui2(pl, index, (byte) (page * 20), (byte) (page * 20 + 20));
            byte tab = (byte) (items.size() / 20 > 0 ? (items.size() / 20) + 1 : 1);
            msg.writer().writeByte(tab); // max page
            msg.writer().writeByte(page);
            msg.writer().writeByte(itemsSend.size());
            for (int j = 0; j < itemsSend.size(); j++) {
                ConsignItem itk = itemsSend.get(j);
                Item it = ItemService.gI().createNewItem(itk.itemId);
                it.itemOptions.clear();
                if (itk.options.isEmpty()) {
                    it.itemOptions.add(new ItemOption(73, 0));
                } else {
                    it.itemOptions.addAll(itk.options);
                }
                msg.writer().writeShort(it.template.id);
                msg.writer().writeShort(itk.id);
                msg.writer().writeInt(itk.displayGold());
                msg.writer().writeInt(itk.displayGem());
                msg.writer().writeByte(0); // buy type
                if (pl.getSession().version >= 222) {
                    msg.writer().writeInt(itk.quantity);
                } else {
                    msg.writer().writeByte(itk.quantity);
                }
                msg.writer().writeByte(itk.player_sell == pl.id ? 1 : 0); // isMe
                msg.writer().writeByte(it.itemOptions.size());
                for (int a = 0; a < it.itemOptions.size(); a++) {
                    msg.writer().writeByte(it.itemOptions.get(a).optionTemplate.id);
                    msg.writer().writeShort(it.itemOptions.get(a).param);
                }
                msg.writer().writeByte(0);
            }
            pl.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void upItemToTop(Player pl, int id) {
        ConsignItem it = getItemBuy(id);
        if (it == null || it.isBuy) {
            Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
            return;
        }
        if (it.player_sell != pl.id) {
            Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
            openShopKyGui(pl);
            return;
        }
        pl.idMark.setIdItemUpTop(id);
        NpcService.gI().createMenuConMeo(pl, ConstNpc.UP_TOP_ITEM, -1, "Bạn có muốn đưa vật phẩm ['" + ItemService.gI().createNewItem(it.itemId).template.name + "'] của bản thân lên trang đầu?\nYêu cầu 5 Ngọc Xanh.", "Đồng ý", "Từ Chối");
    }

    public void StartupItemToTop(Player pl) {
        if (!SubThoiVang(pl, 2)) {
            Service.gI().sendThongBao(pl, "Bạn cần có ít nhất 2 thỏi vàng đưa vật phẩm lên trang đầu");
            return;
        }

        for (ConsignItem its : ConsignShopManager.gI().listItem) {
            if (its.id == pl.idMark.getIdItemUpTop()) {
                its.isUpTop = 1;
                Service.gI().sendThongBao(pl, "Đưa vật phẩm lên trang đầu thành công");
                break;
            }
        }

        ConsignShopManager.gI().listItem.sort(Comparator.comparingInt((ConsignItem it) -> it.isUpTop).reversed());

        ConsignShopManager.gI().save();
        openShopKyGui(pl);
    }

    public synchronized void claimOrDel(Player pl, byte action, int id) {
        ConsignItem it = getItemBuy(pl, id);
        switch (action) {
            case 1: // hủy vật phẩm
                if (it == null || it.isBuy) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
                    return;
                }
                if (it.player_sell != pl.id) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
                    openShopKyGui(pl);
                    return;
                }
                if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
                    Service.gI().sendThongBao(pl, "Hành trang đã đầy");
                    return;
                }
                Item item = toItem(it);
                if (ConsignShopManager.gI().listItem.remove(it)) {
                    InventoryService.gI().addItemBag(pl, item);
                    InventoryService.gI().sendItemBags(pl);
                    Service.gI().sendMoney(pl);
                    ConsignShopManager.gI().save();
                    Service.gI().sendThongBao(pl, "Hủy bán vật phẩm thành công");
                    openShopKyGui(pl);
                }
                break;
            case 2: // nhận tiền
                if (it == null || !it.isBuy) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc chưa được bán");
                    return;
                }
                if (it.player_sell != pl.id) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
                    openShopKyGui(pl);
                    return;
                }
                if (it.isCoin()) {
                    if (!CoinKhaiNguyenService.add(pl, it.coinSell - it.coinSell * 10 / 100)) {
                        Service.gI().sendThongBao(pl, "Hành trang đã đầy");
                        return;
                    }
                    InventoryService.gI().sendItemBags(pl);
                } else if (it.goldSell > 0) {
                    // tin đăng cũ: trả như trước
                    Item tvAdd = ItemService.gI().createNewItem((short) 457);
                    tvAdd.quantity = it.goldSell - it.goldSell * 10 / 100;
                    InventoryService.gI().addItemBag(pl, tvAdd);
                } else if (it.gemSell > 0) {
                    pl.inventory.gem += it.gemSell - it.gemSell * 10 / 100;
                }
                if (ConsignShopManager.gI().listItem.remove(it)) {
                    Service.gI().sendMoney(pl);
                    ConsignShopManager.gI().save();
                    Service.gI().sendThongBao(pl, "Bạn đã bán vật phẩm thành công");
                    openShopKyGui(pl);

                }
                break;
        }
    }

    public List<ConsignItem> getItemCanKiGui(Player pl) {
        List<ConsignItem> its = new ArrayList<>();
        ConsignShopManager.gI().listItem.stream().filter((it) -> (it != null && it.player_sell == pl.id)).forEachOrdered((it) -> {
            its.add(it);
        });
        pl.inventory.itemsBag.stream().filter((it) -> (itemCanConsign(it))).forEachOrdered((it) -> {
            its.add(new ConsignItem(InventoryService.gI().getIndexBag(pl, it), it.template.id, (int) pl.id, (byte) 4, -1, -1, it.quantity, (byte) -1, it.itemOptions, false));
        });
        return its;
    }

    // Ký gửi được mọi vật phẩm, trừ: không thể giao dịch (option 30), đồ nhiệm vụ (type 8),
    // vàng/ngọc nhặt rơi (type 9, 10) và chính Coin Khai Nguyên
    public boolean itemCanConsign(Item it) {
        if (it == null || !it.isNotNullItem()) {
            return false;
        }
        switch (it.template.type) {
            case 8, 9, 10:
                return false;
        }
        if (it.template.id == ConstItem.COIN_KHAI_NGUYEN) {
            return false;
        }
        return it.itemOptions.stream().noneMatch(op -> op.optionTemplate.id == 30);
    }

    public int getMaxId() {
        int max = 0;
        for (ConsignItem it : ConsignShopManager.gI().listItem) {
            if (it != null && it.id > max) {
                max = it.id;
            }
        }
        return max;
    }

    public byte getTabKiGui(Item it) {
        if (it.template.type >= 0 && it.template.type <= 2) {
            return 0;
        } else if ((it.template.type >= 3 && it.template.type <= 4)) {
            return 1;
        } else if (it.template.type == 29) {
            return 2;
        } else {
            return 3;
        }
    }

    private static final int MAX_COIN_PRICE = 1_000_000;

    // Chỉ ký gửi bằng Coin Khai Nguyên: client chỉ có ô vàng/ngọc nên giá nhập ở ô Vàng được tính là coin
    public synchronized void KiGui(Player pl, int id, int money, byte moneyType, int quantity) {
        try {
            if (moneyType != 0) {
                Service.gI().sendThongBao(pl, "Chỉ ký gửi bằng " + CoinKhaiNguyenService.NAME + ", hãy nhập giá ở ô Vàng");
                openShopKyGui(pl);
                return;
            }
            if (id < 0 || id >= pl.inventory.itemsBag.size()) {
                return;
            }
            Item bagItem = pl.inventory.itemsBag.get(id);
            if (!bagItem.isNotNullItem() || !itemCanConsign(bagItem)) {
                Service.gI().sendThongBao(pl, "Vật phẩm không thể kí gửi");
                openShopKyGui(pl);
                return;
            }
            Item it = ItemService.gI().copyItem(bagItem);
            if (quantity <= 0 || quantity > it.quantity) {
                openShopKyGui(pl);
                return;
            }
            if (money <= 0 || money > MAX_COIN_PRICE) {
                Service.gI().sendThongBao(pl, "Giá từ 1 đến " + Util.numberToMoney(MAX_COIN_PRICE) + " " + CoinKhaiNguyenService.NAME);
                openShopKyGui(pl);
                return;
            }
            // Thu phí sau khi đã kiểm tra hợp lệ, tránh mất phí oan
            if (!SubThoiVang(pl, 1)) {
                Service.gI().sendThongBao(pl, "Bạn cần có ít nhất 1 thỏi vàng để làm phí đăng bán");
                return;
            }
            InventoryService.gI().subQuantityItemsBag(pl, bagItem, quantity);
            ConsignItem consign = new ConsignItem(getMaxId() + 1, it.template.id, (int) pl.id, getTabKiGui(it), -1, -1, quantity, (byte) 0, it.itemOptions, false);
            consign.coinSell = money;
            ConsignShopManager.gI().listItem.add(consign);
            InventoryService.gI().sendItemBags(pl);
            openShopKyGui(pl);
            Service.gI().sendMoney(pl);
            Service.gI().sendThongBao(pl, "Đăng bán thành công với giá " + money + " " + CoinKhaiNguyenService.NAME);
            ConsignShopManager.gI().save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openShopKyGui(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(2);
            msg.writer().writeByte(5);
            for (byte i = 0; i < 5; i++) {
                if (i == 4) {
                    msg.writer().writeUTF(ConsignShopManager.gI().tabName[i]);
                    msg.writer().writeByte(0);
                    // Dựng danh sách 1 lần (trước đây dựng lại mỗi vòng lặp)
                    List<ConsignItem> canKiGui = getItemCanKiGui(pl);
                    // Số lượng gửi bằng 1 byte: tối đa 127 món (tin đã đăng đứng trước nên không bị cắt)
                    if (canKiGui.size() > 127) {
                        canKiGui = canKiGui.subList(0, 127);
                    }
                    msg.writer().writeByte(canKiGui.size());
                    for (ConsignItem itk : canKiGui) {
                        if (itk == null) {
                            continue;
                        }
                        Item it = ItemService.gI().createNewItem(itk.itemId);
                        it.itemOptions.clear();
                        if (itk.options.isEmpty()) {
                            it.itemOptions.add(new ItemOption(73, 0));
                        } else {
                            it.itemOptions.addAll(itk.options);
                        }
                        msg.writer().writeShort(it.template.id);
                        msg.writer().writeShort(itk.id);
                        msg.writer().writeInt(itk.displayGold());
                        msg.writer().writeInt(itk.displayGem());
                        if (getItemBuy(pl, itk.id) == null) {
                            msg.writer().writeByte(0); // buy type
                        } else if (itk.isBuy) {
                            msg.writer().writeByte(2);
                        } else {
                            msg.writer().writeByte(1);
                        }
                        msg.writer().writeInt(itk.quantity);
                        msg.writer().writeByte(1); // isMe
                        msg.writer().writeByte(it.itemOptions.size());
                        for (int a = 0; a < it.itemOptions.size(); a++) {
                            msg.writer().writeByte(it.itemOptions.get(a).optionTemplate.id);
                            msg.writer().writeShort(it.itemOptions.get(a).param);
                        }
                        msg.writer().writeByte(0);
                        msg.writer().writeByte(0);
                    }
                } else {
                    List<ConsignItem> items = getItemKyGui(pl, i);
                    List<ConsignItem> itemsSend = getItemKyGui2(pl, i, (byte) 0, (byte) 20);
                    msg.writer().writeUTF(ConsignShopManager.gI().tabName[i]);
                    byte tab = (byte) (items.size() / 20 > 0 ? (items.size() / 20) + 1 : 1);
                    msg.writer().writeByte(tab); // max page
                    msg.writer().writeByte(itemsSend.size());
                    for (int j = 0; j < itemsSend.size(); j++) {
                        ConsignItem itk = itemsSend.get(j);
                        Item it = ItemService.gI().createNewItem(itk.itemId);
                        it.itemOptions.clear();
                        if (itk.options.isEmpty()) {
                            it.itemOptions.add(new ItemOption(73, 0));
                        } else {
                            it.itemOptions.addAll(itk.options);
                        }
                        msg.writer().writeShort(it.template.id);
                        msg.writer().writeShort(itk.id);
                        msg.writer().writeInt(itk.displayGold());
                        msg.writer().writeInt(itk.displayGem());
                        msg.writer().writeByte(0); // buy type
                        msg.writer().writeInt(itk.quantity);
                        msg.writer().writeByte(itk.player_sell == pl.id ? 1 : 0); // isMe     
                        msg.writer().writeByte(it.itemOptions.size());
                        for (int a = 0; a < it.itemOptions.size(); a++) {
                            msg.writer().writeByte(it.itemOptions.get(a).optionTemplate.id);
                            msg.writer().writeShort(it.itemOptions.get(a).param);
                        }
                        msg.writer().writeByte(0);
                        msg.writer().writeByte(0);
                    }
                }
            }
            pl.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }
}
