package nro.models.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nro.models.consts.ConstTaskBadges;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.task.BadgesTaskService;

/**
 *
 * @author By Mr Blue
 *
 */
public class Inventory {

    public static final long LIMIT_GOLD = 1_000_000_000_000L;
    public static final int MAX_ITEMS_BAG = 80;
    public static final int MAX_ITEMS_BOX = 100;
    public Item trainArmor;
    public List<String> giftCode;
    public List<Item> itemsBody;
    public List<Item> itemsBag;
    public List<Item> itemsBox;

    public List<Item> itemsBoxCrackBall;
    public List<Item> itemsDaBan;

    public long gold;
    public int gem;
    public int ruby;
    public int coupon;
    public int event;
    public Iterable<Item> items;

    public Inventory() {
        itemsBody = new ArrayList<>();
        itemsBag = new ArrayList<>();
        itemsBox = new ArrayList<>();
        itemsMailBox = new ArrayList<>();
        itemsBoxCrackBall = new ArrayList<>();
        itemsDaBan = new ArrayList<>();
        giftCode = new ArrayList<>();
    }

    public int getGem() {
        return this.gem;
    }

    public int getParam(Item it, int id) {
        for (ItemOption op : it.itemOptions) {
            if (op != null && op.optionTemplate.id == id) {
                return op.param;
            }
        }
        return 0;
    }

    public boolean haveOption(List<Item> l, int index, int id) {
        Item it = l.get(index);
        if (it != null && it.isNotNullItem()) {
            return it.itemOptions.stream().anyMatch(op -> op != null && op.optionTemplate.id == id);
        }
        return false;
    }

    public void subGem(int num) {
        this.gem -= num;
    }

    public void subGold(int num) {
        this.gold -= num;
    }

    public void addGold(int gold) {
        this.gold += gold;
        if (this.gold > LIMIT_GOLD) {
            this.gold = LIMIT_GOLD;
        }
    }

    public void dispose() {
        if (this.trainArmor != null) {
            this.trainArmor.dispose();
        }
        this.trainArmor = null;
        if (this.itemsBody != null) {
            for (Item it : this.itemsBody) {
                it.dispose();
            }
            this.itemsBody.clear();
        }
        if (this.itemsBag != null) {
            for (Item it : this.itemsBag) {
                it.dispose();
            }
            this.itemsBag.clear();
        }
        if (this.itemsBox != null) {
            for (Item it : this.itemsBox) {
                it.dispose();
            }
            this.itemsBox.clear();
        }
        if (this.itemsBoxCrackBall != null) {
            for (Item it : this.itemsBoxCrackBall) {
                it.dispose();
            }
            this.itemsBoxCrackBall.clear();
        }
        if (this.itemsDaBan != null) {
            for (Item it : this.itemsDaBan) {
                it.dispose();
            }
            this.itemsDaBan.clear();
        }
        this.itemsBody = null;
        this.itemsBag = null;
        this.itemsBox = null;
        this.itemsBoxCrackBall = null;
        this.itemsDaBan = null;
    }

    public void checkAndUpdateMeRongBadges(Player player) {
        Set<Integer> checkedItemIds = new HashSet<>();

        List<List<Item>> inventories = Arrays.asList(
                this.itemsBag,
                this.itemsBox,
                this.itemsBody
        );

        for (List<Item> inventory : inventories) {
            for (Item item : inventory) {
                if (item != null && isPermanent(item)) {
                    int itemId = item.template.id;
                    if (itemId >= 1765 && itemId <= 1771 && !checkedItemIds.contains(itemId)) {
                        BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ME_RONG, 1);
                        checkedItemIds.add(itemId);
                    }
                }
            }
        }
    }

    private boolean isPermanent(Item item) {
        return item != null && item.getOptionById(93) == null;
    }

    public List<Item> itemsMailBox;

    public List<Item> itemsBoxCollection;

    public long getGold() {
        return this.gold;
    }

    public int getGemAndRuby() {
        return this.gem + this.ruby;
    }

    public void subGemAndRuby(int num) {
        this.ruby -= num;
        if (this.ruby < 0) {
            this.gem += this.ruby;
            this.ruby = 0;
        }
    }

    public int getRuby() {
        return this.ruby;
    }

    public void subRuby(int num) {
        this.ruby -= num;
    }

    public void addRuby(int ruby) {
        this.ruby += ruby;
        if (this.ruby > LIMIT_RUBY) {
            this.ruby = LIMIT_RUBY;
        }
    }
    public static final int LIMIT_RUBY = 2_000_000_000;

    public void addExpVip(int vip) {
        this.Exp_Vip += vip;
        if (this.Exp_Vip > LIMIT_EXP_VIP) {
            this.Exp_Vip = LIMIT_EXP_VIP;
        }
    }

    public int Exp_Vip;

    public void subExpVip(int num) {
        this.Exp_Vip -= num;
    }

    public static final int LIMIT_EXP_VIP = 2_000_000_000;
}
