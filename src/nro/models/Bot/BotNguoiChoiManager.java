package nro.models.Bot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nro.models.data.LocalManager;
import nro.models.server.Client;
import nro.models.services.ItemService;
import nro.models.utils.Logger;
import nro.models.utils.Util;

/**
 * Tạo bot giống người chơi thật (BotNguoiChoi). Số lượng đặt bằng bot.player.count trong Config.properties.
 */
public class BotNguoiChoiManager {

    private static final String[] BASES = {
        "tuan", "long", "hieu", "minh", "khoa", "phuc", "quan", "hung", "toan", "vinh",
        "thai", "linh", "ngoc", "kiet", "binh", "dung", "giang", "kien", "manh", "nhat",
        "phat", "sang", "thien", "trung", "viet", "hoang", "duc", "nam", "bao", "huy",
        "son", "dat", "tam", "lam", "hai", "loc", "tai", "an", "kame", "goku",
        "vegi", "songo", "picco", "trunk", "gohan", "cadic", "broly", "namek", "xayda", "ruby"
    };
    private static final String[] SUFFIXES = {
        "99", "98", "97", "2k", "vn", "pk", "xd", "td", "nm", "88", "68", "69", "9x", "kk",
        "zz", "01", "07", "12", "123", "vip", "pro", "1", "7", "9", "x", "z", "dz", "cute", "no1"
    };

    private static final List<BotNguoiChoi> BOTS = new ArrayList<>();

    public static void spawnFromConfig() {
        int count = readCount();
        if (count <= 0) {
            return;
        }
        spawn(count);
    }

    public static void spawn(int count) {
        Set<String> used = new HashSet<>();
        int ok = 0;
        for (int i = 0; i < count; i++) {
            try {
                String name = uniqueName(used);
                if (name == null) {
                    break;
                }
                int gender = Util.nextInt(0, 2);
                int stage = randomStage();
                BotNguoiChoi bot = new BotNguoiChoi(name, gender, stage);
                for (int j = 0; j < 11; j++) {
                    bot.inventory.itemsBody.add(ItemService.gI().createItemNull());
                }
                for (int j = 0; j < 20; j++) {
                    bot.inventory.itemsBag.add(ItemService.gI().createItemNull());
                }
                if (bot.goToQuestMap()) {
                    synchronized (BotManager.gI().bot) {
                        BotManager.gI().bot.add(bot);
                    }
                    BOTS.add(bot);
                    ok++;
                }
                Thread.sleep(Util.nextInt(300, 1200));
            } catch (Exception e) {
                Logger.logException(BotNguoiChoiManager.class, e, "Tạo bot người chơi");
            }
        }
        Logger.success("Đã tạo " + ok + " bot người chơi\n");
    }

    // Nhiều bot mới hơn bot đã lên cấp, giống tỉ lệ người chơi thật
    private static int randomStage() {
        int r = Util.nextInt(0, 99);
        if (r < 30) {
            return 0;
        } else if (r < 50) {
            return 1;
        } else if (r < 65) {
            return 2;
        } else if (r < 77) {
            return 3;
        } else if (r < 87) {
            return 4;
        } else if (r < 94) {
            return 5;
        }
        return BotNguoiChoi.STAGE_FREE;
    }

    // Tên đúng 6 ký tự a-z0-9, không trùng nhân vật trong DB và người đang online
    private static String uniqueName(Set<String> used) {
        for (int tries = 0; tries < 500; tries++) {
            String base = BASES[Util.nextInt(0, BASES.length - 1)];
            String name = base;
            while (name.length() < 6) {
                String suf = SUFFIXES[Util.nextInt(0, SUFFIXES.length - 1)];
                name = name + suf;
            }
            if (name.length() != 6 || !name.matches("[a-z0-9]{6}")) {
                continue;
            }
            if (used.contains(name) || Client.gI().getPlayer(name) != null || existsInDb(name)) {
                continue;
            }
            used.add(name);
            return name;
        }
        return null;
    }

    private static boolean existsInDb(String name) {
        try (java.sql.Connection con = LocalManager.getConnection();
                java.sql.PreparedStatement ps = con.prepareStatement("select 1 from player where name = ? limit 1")) {
            ps.setString(1, name);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            return true; // không kiểm tra được thì bỏ tên này cho an toàn
        }
    }

    private static int readCount() {
        try (java.io.FileInputStream f = new java.io.FileInputStream("Config.properties")) {
            java.util.Properties pr = new java.util.Properties();
            pr.load(f);
            return Integer.parseInt(pr.getProperty("bot.player.count", "20").trim());
        } catch (Exception e) {
            return 20;
        }
    }
}
