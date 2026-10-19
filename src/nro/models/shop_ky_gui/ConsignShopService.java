package nro.models.shop_ky_gui;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

    private static final int PAGE_SIZE = 20;
    private static final int MAX_COIN_PRICE = 1_000_000;
    // Số món ở tab "của tôi" gửi bằng 1 byte
    private static final int MAX_MY_ITEMS = 127;
    private static final Comparator<ConsignItem> UP_TOP_FIRST = Comparator.comparingInt((ConsignItem it) -> it.isUpTop).reversed();

    private static ConsignShopService instance;

    public static ConsignShopService gI() {
        if (instance == null) {
            instance = new ConsignShopService();
        }
        return instance;
    }

    // Món đang bán của 1 tab, món được đẩy lên đầu đứng trước
    private List<ConsignItem> getItemsOfTab(int tab) {
        List<ConsignItem> result = new ArrayList<>();
        for (ConsignItem it : ConsignShopManager.gI().listItem) {
            if (it != null && it.tab == tab && !it.isBuy) {
                result.add(it);
            }
        }
        result.sort(UP_TOP_FIRST);
        return result;
    }

    private static int maxPage(int size) {
        return Math.max(1, (size + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    private static List<ConsignItem> page(List<ConsignItem> items, int page) {
        int from = Math.min(page * PAGE_SIZE, items.size());
        return items.subList(from, Math.min(from + PAGE_SIZE, items.size()));
    }

    private static void writeOptions(DataOutputStream w, List<ItemOption> options) throws IOException {
        if (options.isEmpty()) {
            w.writeByte(1);
            w.writeByte(73);
            w.writeShort(0);
            return;
        }
        w.writeByte(options.size());
        for (ItemOption io : options) {
            w.writeByte(io.optionTemplate.id);
            w.writeShort(io.param);
        }
    }

    private boolean subThoiVang(Player pl, int quantity) {
        for (Item item : pl.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 457 && item.quantity >= quantity) {
                InventoryService.gI().subQuantityItemsBag(pl, item, quantity);
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
        if (it == null) {
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

    // Tìm món chưa bán theo id
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
        if (index < 0 || index > 3 || page < 0) {
            return;
        }
        List<ConsignItem> items = getItemsOfTab(index);
        int maxPage = maxPage(items.size());
        if (page >= maxPage) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-100);
            DataOutputStream w = msg.writer();
            w.writeByte(index);
            w.writeByte(maxPage);
            w.writeByte(page);
            List<ConsignItem> itemsSend = page(items, page);
            boolean intQuantity = pl.getSession().version >= 222;
            w.writeByte(itemsSend.size());
            for (ConsignItem itk : itemsSend) {
                w.writeShort(itk.itemId);
                w.writeShort(itk.id);
                w.writeInt(itk.displayGold());
                w.writeInt(itk.displayGem());
                w.writeByte(0); // buy type
                if (intQuantity) {
                    w.writeInt(itk.quantity);
                } else {
                    w.writeByte(itk.quantity);
                }
                w.writeByte(itk.player_sell == pl.id ? 1 : 0); // isMe
                writeOptions(w, itk.options);
                w.writeByte(0);
            }
            pl.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void upItemToTop(Player pl, int id) {
        ConsignItem it = getItemBuy(id);
        if (it == null) {
            Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
            return;
        }
        if (it.player_sell != pl.id) {
            Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
            openShopKyGui(pl);
            return;
        }
        pl.idMark.setIdItemUpTop(id);
        NpcService.gI().createMenuConMeo(pl, ConstNpc.UP_TOP_ITEM, -1, "Bạn có muốn đưa vật phẩm ['" + ItemService.gI().getTemplate(it.itemId).name + "'] của bản thân lên trang đầu?\nYêu cầu 5 Ngọc Xanh.", "Đồng ý", "Từ Chối");
    }

    public synchronized void claimOrDel(Player pl, byte action, int id) {
        ConsignItem it = getItemBuy(pl, id);
        if (it == null) {
            Service.gI().sendThongBao(pl, action == 1 ? "Vật phẩm không tồn tại hoặc đã được bán" : "Vật phẩm không tồn tại hoặc chưa được bán");
            return;
        }
        switch (action) {
            case 1: // hủy vật phẩm
                if (it.isBuy) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
                    return;
                }
                if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
                    Service.gI().sendThongBao(pl, "Hành trang đã đầy");
                    return;
                }
                if (ConsignShopManager.gI().listItem.remove(it)) {
                    InventoryService.gI().addItemBag(pl, toItem(it));
                    InventoryService.gI().sendItemBags(pl);
                    Service.gI().sendMoney(pl);
                    ConsignShopManager.gI().save();
                    Service.gI().sendThongBao(pl, "Hủy bán vật phẩm thành công");
                    openShopKyGui(pl);
                }
                break;
            case 2: // nhận tiền
                if (!it.isBuy) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc chưa được bán");
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

    // Ký gửi được mọi vật phẩm, trừ: không thể giao dịch (option 30), đồ nhiệm vụ (type 8),
    // vàng/ngọc nhặt rơi (type 9, 10) và chính Coin Khai Nguyên.
    // Đồ thần linh luôn ký gửi được, kể cả khi có option 30
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
        if (it.isDTL()) {
            return true;
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
        int type = it.template.type;
        if (type >= 0 && type <= 2) {
            return 0;
        } else if (type == 3 || type == 4) {
            return 1;
        } else if (type == 29) {
            return 2;
        }
        return 3;
    }

    // Chỉ ký gửi bằng Coin Khai Nguyên: client chỉ có ô vàng/ngọc nên giá nhập ở ô Vàng hay ô Ngọc đều tính là coin
    public synchronized void KiGui(Player pl, int id, int money, byte moneyType, int quantity) {
        try {
            if (id < 0 || id >= pl.inventory.itemsBag.size()) {
                return;
            }
            Item bagItem = pl.inventory.itemsBag.get(id);
            if (!itemCanConsign(bagItem)) {
                Service.gI().sendThongBao(pl, "Vật phẩm không thể kí gửi");
                openShopKyGui(pl);
                return;
            }
            if (quantity <= 0 || quantity > bagItem.quantity) {
                openShopKyGui(pl);
                return;
            }
            if (money <= 0 || money > MAX_COIN_PRICE) {
                Service.gI().sendThongBao(pl, "Giá từ 1 đến " + Util.numberToMoney(MAX_COIN_PRICE) + " " + CoinKhaiNguyenService.NAME);
                openShopKyGui(pl);
                return;
            }
            // Thu phí sau khi đã kiểm tra hợp lệ, tránh mất phí oan
            if (!subThoiVang(pl, 1)) {
                Service.gI().sendThongBao(pl, "Bạn cần có ít nhất 1 thỏi vàng để làm phí đăng bán");
                return;
            }
            Item it = ItemService.gI().copyItem(bagItem);
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

    // Tab "của tôi": tin đã đăng (buy type 1 = đang bán, 2 = đã bán) đứng trước, rồi tới đồ trong túi ký gửi được (buy type 0)
    private void writeMyTab(DataOutputStream w, Player pl) throws IOException {
        List<ConsignItem> mine = new ArrayList<>();
        for (ConsignItem it : ConsignShopManager.gI().listItem) {
            if (it != null && it.player_sell == pl.id) {
                mine.add(it);
            }
        }
        List<Item> bag = pl.inventory.itemsBag;
        List<Integer> bagIndexes = new ArrayList<>();
        for (int i = 0; i < bag.size() && mine.size() + bagIndexes.size() < MAX_MY_ITEMS; i++) {
            if (itemCanConsign(bag.get(i))) {
                bagIndexes.add(i);
            }
        }
        int listed = Math.min(mine.size(), MAX_MY_ITEMS);
        w.writeByte(listed + bagIndexes.size());
        for (int i = 0; i < listed; i++) {
            ConsignItem itk = mine.get(i);
            writeMyRow(w, itk.itemId, itk.id, itk.displayGold(), itk.displayGem(), itk.isBuy ? 2 : 1, itk.quantity, itk.options);
        }
        for (int i : bagIndexes) {
            Item it = bag.get(i);
            writeMyRow(w, it.template.id, i, -1, -1, 0, it.quantity, it.itemOptions);
        }
    }

    private static void writeMyRow(DataOutputStream w, int templateId, int id, int gold, int gem, int buyType, int quantity, List<ItemOption> options) throws IOException {
        w.writeShort(templateId);
        w.writeShort(id);
        w.writeInt(gold);
        w.writeInt(gem);
        w.writeByte(buyType);
        w.writeInt(quantity);
        w.writeByte(1); // isMe
        writeOptions(w, options);
        w.writeByte(0);
        w.writeByte(0);
    }

    public void openShopKyGui(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-44);
            DataOutputStream w = msg.writer();
            String[] tabName = ConsignShopManager.gI().tabName;
            w.writeByte(2);
            w.writeByte(5);
            for (int i = 0; i < 4; i++) {
                List<ConsignItem> items = getItemsOfTab(i);
                List<ConsignItem> itemsSend = page(items, 0);
                w.writeUTF(tabName[i]);
                w.writeByte(maxPage(items.size()));
                w.writeByte(itemsSend.size());
                for (ConsignItem itk : itemsSend) {
                    w.writeShort(itk.itemId);
                    w.writeShort(itk.id);
                    w.writeInt(itk.displayGold());
                    w.writeInt(itk.displayGem());
                    w.writeByte(0); // buy type
                    w.writeInt(itk.quantity);
                    w.writeByte(itk.player_sell == pl.id ? 1 : 0); // isMe
                    writeOptions(w, itk.options);
                    w.writeByte(0);
                    w.writeByte(0);
                }
            }
            w.writeUTF(tabName[4]);
            w.writeByte(0);
            writeMyTab(w, pl);
            pl.sendMessage(msg);
            // Client chỉ có icon vàng/ngọc: giá hiện ở ô vàng nhưng thực tế là coin
            Service.gI().sendThongBao(pl, "Siêu thị giao dịch bằng " + CoinKhaiNguyenService.NAME
                    + ". Số hiện cạnh icon vàng là giá " + CoinKhaiNguyenService.NAME + ".");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
}
