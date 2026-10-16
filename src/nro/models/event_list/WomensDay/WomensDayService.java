package nro.models.event_list.WomensDay;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import nro.models.attribute.Attribute;
import nro.models.combine.CombineService;
import nro.models.consts.ConstAttribute;
import nro.models.data.LocalManager;
import nro.models.server.ServerManager;
import nro.models.utils.Logger;
import nro.models.event.EventManager;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.player.Inventory;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * Sự kiện 20/10: rơi nguyên liệu, mở quà, tặng hoa lấy điểm.
 */
public class WomensDayService {

    public static final short DAT_TRONG_CAY = 1093;
    public static final short PHAN_BON = 1094;
    public static final short HAT_MAM = 1095;
    public static final short THUOC_TANG_TRUONG = 1096;
    public static final short HOA_HONG_XANH = 1098;
    public static final short CHAU_HOA_HONG_XANH = 1099;
    public static final short LOI_CHUC = 1101;
    public static final short LOI_CHUC_VIP = 1102;
    public static final short THIEP_CHUC_VIP = 1718;
    public static final short HOP_QUA_20_10 = 1719;

    // Điểm 20/10 nhận khi tặng hoa cho NPC
    public static final int POINT_HOA = 2;
    public static final int POINT_CHAU_HOA = 15;

    private static final int TIER_LOI_CHUC = 0;
    private static final int TIER_HOP_QUA = 1;
    private static final int TIER_VIP = 2;

    private static final short ICON_VANG = 930;
    private static final short ICON_HONG_NGOC = 7743;

    /**
     * Quái ở các map thường (0 - 163) rơi nguyên liệu trồng hoa khi sự kiện bật.
     */
    public static void dropFromMob(Player player, Zone zone, int x, int y, List<ItemMap> list) {
        if (!EventManager.DAY_20_10 || player.isBot) {
            return;
        }
        if (Util.isTrue(15, 100)) {
            list.add(new ItemMap(zone, DAT_TRONG_CAY, 1, x, y, player.id));
        }
        if (Util.isTrue(10, 100)) {
            list.add(new ItemMap(zone, PHAN_BON, 1, x + 10, y, player.id));
        }
        if (Util.isTrue(1, 200)) {
            list.add(new ItemMap(zone, THUOC_TANG_TRUONG, 1, x - 10, y, player.id));
        }
    }

    /**
     * @return true nếu item là quà 20/10 (đã xử lý xong)
     */
    public static boolean useItem(Player pl, Item item) {
        switch (item.template.id) {
            case LOI_CHUC:
                openGift(pl, item, TIER_LOI_CHUC);
                return true;
            case HOP_QUA_20_10:
                openGift(pl, item, TIER_HOP_QUA);
                return true;
            case LOI_CHUC_VIP:
            case THIEP_CHUC_VIP:
                openGift(pl, item, TIER_VIP);
                return true;
            default:
                return false;
        }
    }

    private static void openGift(Player pl, Item item, int tier) {
        if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy");
            return;
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        short iconReward;
        String rewardName;
        int r = Util.nextInt(1, 100);
        switch (tier) {
            case TIER_LOI_CHUC -> {
                if (r <= 40) {
                    int gold = Util.nextInt(1_000_000, 5_000_000);
                    addGold(pl, gold);
                    iconReward = ICON_VANG;
                    rewardName = Util.numberToMoney(gold) + " vàng";
                } else if (r <= 60) {
                    int ruby = Util.nextInt(1, 2);
                    pl.inventory.ruby += ruby;
                    iconReward = ICON_HONG_NGOC;
                    rewardName = ruby + " hồng ngọc";
                } else if (r <= 85) {
                    Item it = createMaterial();
                    InventoryService.gI().addItemBag(pl, it);
                    iconReward = it.template.iconID;
                    rewardName = "x" + it.quantity + " " + it.template.name;
                } else {
                    Item it = createBoTro(1);
                    InventoryService.gI().addItemBag(pl, it);
                    iconReward = it.template.iconID;
                    rewardName = it.template.name;
                }
            }
            case TIER_HOP_QUA -> {
                if (r <= 30) {
                    int gold = Util.nextInt(3_000_000, 10_000_000);
                    addGold(pl, gold);
                    iconReward = ICON_VANG;
                    rewardName = Util.numberToMoney(gold) + " vàng";
                } else if (r <= 50) {
                    int ruby = Util.nextInt(2, 5);
                    pl.inventory.ruby += ruby;
                    iconReward = ICON_HONG_NGOC;
                    rewardName = ruby + " hồng ngọc";
                } else if (r <= 70) {
                    Item it = createBoTro(1);
                    InventoryService.gI().addItemBag(pl, it);
                    iconReward = it.template.iconID;
                    rewardName = it.template.name;
                } else if (r <= 85) {
                    Item it = createMaterial();
                    InventoryService.gI().addItemBag(pl, it);
                    iconReward = it.template.iconID;
                    rewardName = "x" + it.quantity + " " + it.template.name;
                } else if (r <= 95) {
                    Item it = ItemService.gI().createNewItem((short) 1143);
                    it.addOptionParam(30, 0);
                    InventoryService.gI().addItemBag(pl, it);
                    iconReward = it.template.iconID;
                    rewardName = it.template.name;
                } else {
                    Item it = createPhuKien(14, 16);
                    InventoryService.gI().addItemBag(pl, it);
                    iconReward = it.template.iconID;
                    rewardName = it.template.name;
                }
            }
            default -> {
                if (r <= 25) {
                    int ruby = Util.nextInt(10, 30);
                    pl.inventory.ruby += ruby;
                    iconReward = ICON_HONG_NGOC;
                    rewardName = ruby + " hồng ngọc";
                } else if (r <= 45) {
                    int gold = Util.nextInt(20_000_000, 50_000_000);
                    addGold(pl, gold);
                    iconReward = ICON_VANG;
                    rewardName = Util.numberToMoney(gold) + " vàng";
                } else if (r <= 65) {
                    Item it = createBoTro(3);
                    InventoryService.gI().addItemBag(pl, it);
                    iconReward = it.template.iconID;
                    rewardName = "x3 " + it.template.name;
                } else if (r <= 80) {
                    Item it = ItemService.gI().createNewItem((short) 1143, 3);
                    it.addOptionParam(30, 0);
                    InventoryService.gI().addItemBag(pl, it);
                    iconReward = it.template.iconID;
                    rewardName = "x3 " + it.template.name;
                } else if (r <= 95) {
                    Item it = createPhuKien(15, 18);
                    InventoryService.gI().addItemBag(pl, it);
                    iconReward = it.template.iconID;
                    rewardName = it.template.name;
                } else {
                    Item it = createCaiTrang();
                    InventoryService.gI().addItemBag(pl, it);
                    iconReward = it.template.iconID;
                    rewardName = it.template.name;
                }
                Service.gI().sendThongBaoAllPlayer("|7|" + pl.name + " gửi lời chúc 20/10 tới toàn thể chị em và nhận được " + rewardName + "!");
            }
        }
        InventoryService.gI().sendItemBags(pl);
        Service.gI().sendMoney(pl);
        CombineService.gI().sendEffectOpenItem(pl, item.template.iconID, iconReward);
        Service.gI().sendThongBao(pl, "Bạn nhận được " + rewardName);
    }

    /**
     * Tặng hoa cho NPC 20/10 để lấy điểm.
     */
    public static void giveFlower(Player pl, boolean chauHoa) {
        short id = chauHoa ? CHAU_HOA_HONG_XANH : HOA_HONG_XANH;
        Item flower = InventoryService.gI().findItemBag(pl, id);
        if (flower == null || flower.quantity < 1) {
            Service.gI().sendThongBao(pl, "Bạn không có " + ItemService.gI().getTemplate(id).name);
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần một ô trống trong hành trang");
            return;
        }
        InventoryService.gI().subQuantityItemsBag(pl, flower, 1);
        int point = chauHoa ? POINT_CHAU_HOA : POINT_HOA;
        pl.event.add20_10Point(point);
        int bong = chauHoa ? BONG_CHAU_HOA : BONG_HOA;
        pl.event.addFlower20_10(bong);
        addServerFlowers(bong);
        String msg = "Bạn nhận được " + point + " điểm 20/10";
        Item gift = null;
        if (chauHoa) {
            gift = ItemService.gI().createNewItem(HOP_QUA_20_10);
        } else if (Util.isTrue(30, 100)) {
            gift = ItemService.gI().createNewItem(LOI_CHUC);
        }
        if (gift != null) {
            gift.addOptionParam(30, 0);
            gift.addOptionParam(93, 30);
            InventoryService.gI().addItemBag(pl, gift);
            msg += " và " + gift.template.name;
        }
        InventoryService.gI().sendItemBags(pl);
        Service.gI().chat(pl, "Chúc mừng ngày Phụ nữ Việt Nam 20/10!");
        Service.gI().sendThongBao(pl, msg + ". Tổng điểm: " + pl.event.get20_10Point());
    }

    // ===================== Mốc tặng hoa toàn máy chủ =====================
    // Hoa hồng xanh tính 1 bông, chậu hoa tính 5 bông
    public static final int BONG_HOA = 1;
    public static final int BONG_CHAU_HOA = 5;

    // {số bông toàn server, % TNSM cộng thêm, thời gian (giờ)}
    private static final int[][] MILESTONES = {
        {200, 100, 6},
        {500, 100, 12},
        {1000, 200, 12},
        {2000, 200, 24},
        {5000, 300, 24}
    };

    private static final String EVENT_ROW = "day_20_10";
    private static int serverFlowers;
    private static int lastStage;

    // ===================== Hẹn giờ kết thúc =====================
    private static final String END_KEY = "event.day2010.end"; // dạng yyyy-MM-dd HH:mm trong Config.properties

    /**
     * @return thời điểm kết thúc (ms), 0 nếu không đặt
     */
    public static long getEndTime() {
        try (java.io.FileInputStream f = new java.io.FileInputStream("Config.properties")) {
            java.util.Properties pr = new java.util.Properties();
            pr.load(f);
            String s = pr.getProperty(END_KEY, "").trim();
            if (s.isEmpty()) {
                return 0;
            }
            return java.time.LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            Logger.error("Sai định dạng " + END_KEY + " (cần yyyy-MM-dd HH:mm): " + e + "\n");
            return 0;
        }
    }

    /**
     * Hẹn tắt sự kiện đúng giờ kết thúc. Trả về false nếu đã quá giờ.
     */
    public static boolean scheduleEnd() {
        long end = getEndTime();
        if (end <= 0) {
            return true;
        }
        long delay = end - System.currentTimeMillis();
        if (delay <= 0) {
            EventManager.DAY_20_10 = false;
            Logger.success("Sự kiện 20/10 đã quá giờ kết thúc, không bật.\n");
            return false;
        }
        java.util.Timer timer = new java.util.Timer("Event20_10_End", true);
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                EventManager.DAY_20_10 = false;
                Service.gI().sendThongBaoAllPlayer("|7|Sự kiện 20/10 đã kết thúc. Cảm ơn mọi người đã tham gia!");
                Logger.success("Sự kiện 20/10 đã kết thúc.\n");
            }
        }, delay);
        return true;
    }

    public static void load() {
        try (Connection con = LocalManager.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT `data` FROM `event` WHERE `name` = ?")) {
            ps.setString(1, EVENT_ROW);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JsonObject json = new Gson().fromJson(rs.getString("data"), JsonObject.class);
                    serverFlowers = json.get("flowers").getAsInt();
                    lastStage = json.get("stage").getAsInt();
                } else {
                    LocalManager.executeUpdate("INSERT INTO `event` (`name`, `data`) VALUES (?, ?)", EVENT_ROW, toJson());
                }
            }
        } catch (Exception e) {
            Logger.error("Lỗi load sự kiện 20/10: " + e + "\n");
        }
    }

    private static String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("flowers", serverFlowers);
        json.addProperty("stage", lastStage);
        return json.toString();
    }

    private static synchronized void addServerFlowers(int num) {
        serverFlowers += num;
        int stage = 0;
        for (int i = 0; i < MILESTONES.length; i++) {
            if (serverFlowers >= MILESTONES[i][0]) {
                stage = i + 1;
            }
        }
        if (stage > lastStage) {
            lastStage = stage;
            int[] m = MILESTONES[stage - 1];
            Attribute at = ServerManager.gI().getAttributeManager() == null ? null
                    : ServerManager.gI().getAttributeManager().find(ConstAttribute.TNSM);
            if (at != null) {
                // Không hạ mức buff đang chạy (admin có thể đã bật mức cao hơn)
                boolean active = !at.isExpired();
                at.setValue(active ? Math.max(at.getValue(), m[1]) : m[1]);
                if (at.getTime() != -1) { // -1 = buff vĩnh viễn, giữ nguyên
                    at.setTime(Math.max(active ? at.getTime() : 0, m[2] * 3600));
                }
            }
            Service.gI().sendThongBaoAllPlayer("|7|Toàn máy chủ đã tặng " + serverFlowers
                    + " bông hoa 20/10! Cả server được tăng " + m[1] + "% TNSM trong " + m[2] + " giờ!");
        }
        try {
            LocalManager.executeUpdate("UPDATE `event` SET `data` = ? WHERE `name` = ?", toJson(), EVENT_ROW);
        } catch (Exception e) {
            Logger.error("Lỗi lưu sự kiện 20/10: " + e + "\n");
        }
    }

    /**
     * Dòng thông tin mốc toàn server, hiện ở menu NPC.
     */
    public static String getServerStatus() {
        StringBuilder sb = new StringBuilder("Toàn máy chủ đã tặng " + serverFlowers + " bông hoa.\n");
        if (lastStage < MILESTONES.length) {
            int[] next = MILESTONES[lastStage];
            sb.append("Mốc tiếp theo: ").append(next[0]).append(" bông = +").append(next[1])
                    .append("% TNSM toàn server ").append(next[2]).append(" giờ.\n");
        }
        Attribute at = ServerManager.gI().getAttributeManager() == null ? null
                : ServerManager.gI().getAttributeManager().find(ConstAttribute.TNSM);
        if (at != null && !at.isExpired() && at.getValue() > 0) {
            sb.append("|1|Đang tăng ").append(at.getValue()).append("% TNSM, còn ")
                    .append(Util.formatTimeHMS(at.getTime())).append("\n");
        }
        return sb.toString();
    }

    // ===================== Đua top tặng hoa =====================
    private static String topCache = "";
    private static long lastTopLoad;

    public static String getTopText(Player pl) {
        if (System.currentTimeMillis() - lastTopLoad > 60_000) {
            lastTopLoad = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            String query = "SELECT name, bong FROM ("
                    + "SELECT player.name, CAST(JSON_UNQUOTE(JSON_EXTRACT(player.data_event, '$[16]')) AS UNSIGNED) AS bong "
                    + "FROM player INNER JOIN account ON account.id = player.account_id WHERE account.ban = 0"
                    + ") t WHERE bong > 0 ORDER BY bong DESC LIMIT 10";
            try (Connection con = LocalManager.getConnection();
                    PreparedStatement ps = con.prepareStatement(query);
                    ResultSet rs = ps.executeQuery()) {
                int i = 1;
                while (rs.next()) {
                    sb.append(i++).append(". ").append(rs.getString("name")).append(": ")
                            .append(rs.getInt("bong")).append(" bông\n");
                }
            } catch (Exception e) {
                Logger.error("Lỗi load top 20/10: " + e + "\n");
            }
            topCache = sb.length() == 0 ? "Chưa có ai tặng hoa.\n" : sb.toString();
        }
        return "|7|TOP TẶNG HOA 20/10\n|0|" + topCache
                + "|1|Bạn đã tặng " + pl.event.getFlower20_10() + " bông.\n"
                + "|2|(Cập nhật mỗi phút, tính theo dữ liệu đã lưu)";
    }

    private static void addGold(Player pl, int gold) {
        pl.inventory.gold += gold;
        if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
            pl.inventory.gold = Inventory.LIMIT_GOLD;
        }
    }

    private static Item createMaterial() {
        return switch (Util.nextInt(0, 3)) {
            case 0 -> ItemService.gI().createNewItem(DAT_TRONG_CAY, 5);
            case 1 -> ItemService.gI().createNewItem(PHAN_BON, 3);
            case 2 -> ItemService.gI().createNewItem(HAT_MAM, 1);
            default -> ItemService.gI().createNewItem(THUOC_TANG_TRUONG, 1);
        };
    }

    // Cuồng nộ 2, Bổ khí 2, Bổ huyết 2
    private static Item createBoTro(int quantity) {
        short[] list = {1150, 1151, 1152};
        return ItemService.gI().createNewItem(list[Util.nextInt(0, list.length - 1)], quantity);
    }

    // Phụ kiện đeo lưng chủ đề hoa, hạn 7 ngày
    private static Item createPhuKien(int min, int max) {
        short[] list = {1515, 1519, 1520, 1100, 1344};
        Item it = ItemService.gI().createNewItem(list[Util.nextInt(0, list.length - 1)]);
        it.addOptionParam(50, Util.nextInt(min, max));
        it.addOptionParam(77, Util.nextInt(min, max));
        it.addOptionParam(103, Util.nextInt(min, max));
        it.addOptionParam(30, 0);
        it.addOptionParam(93, 7);
        return it;
    }

    // Cải trang, hạn 7 ngày
    private static Item createCaiTrang() {
        short[] list = {675, 676, 677, 678, 679, 680, 681, 1011};
        short id = list[Util.nextInt(0, list.length - 1)];
        Item it = ItemService.gI().createNewItem(id);
        List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(id);
        if (!ops.isEmpty()) {
            it.itemOptions = ops;
        } else {
            it.addOptionParam(50, 15);
            it.addOptionParam(77, 15);
            it.addOptionParam(103, 15);
        }
        it.addOptionParam(30, 0);
        it.addOptionParam(93, 7);
        return it;
    }
}
