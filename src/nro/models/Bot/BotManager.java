package nro.models.Bot;

import nro.models.map.service.ChangeMapService;
import java.util.ArrayList;
import java.util.List;
import nro.models.server.ServerManager;

public class BotManager implements Runnable {

    public static BotManager i;

    public List<Bot> bot = new ArrayList<>();

    public static BotManager gI() {
        if (i == null) {
            i = new BotManager();
        }
        return i;
    }

    @Override
    public void run() {
        while (ServerManager.isRunning) {
            try {
                long st = System.currentTimeMillis();

                for (Bot bot : new ArrayList<>(this.bot)) {
                    if (bot != null) {
                        bot.update();
                    }
                }

                long timeLeft = 150 - (System.currentTimeMillis() - st);
                if (timeLeft > 0) {
                    Thread.sleep(timeLeft);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static volatile boolean BOT_SYSTEM_ENABLED = false;

    public static boolean ALLOW_CREATE_BOT = false;

    public static volatile int TARGET_MOB_BOT  = 0;

    public static volatile int TARGET_SHOP_BOT = 0;

    public static volatile int TARGET_BOSS_BOT = 0;

    public static volatile int TARGET_SELL_BOT = 0;

    public void stopAllBots() {
        List<Bot> snapshot;
        synchronized (bot) {
            snapshot = new ArrayList<>(bot);
            bot.clear();
        }

        for (int i = snapshot.size() - 1; i >= 0; i--) {
            Bot b = snapshot.get(i);
            try {
                if (b != null) {
                    ChangeMapService.gI().exitMap(b);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static volatile int SHOP_ITEM_ID = 441;

    public static volatile int SHOP_TRADE_ID = 14;

    public static volatile int SHOP_TRADE_NEED = 1;
}
