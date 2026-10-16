package nro.models.boss.Boss_Manager;


import nro.models.boss.Boss;
import nro.models.utils.Logger;
import nro.models.server.Maintenance;

/*
 * @Author: Anwin
 */

public class BossOfTheGangsManager extends BossManager {

    private static BossOfTheGangsManager instance;

    public static BossOfTheGangsManager gI() {
        if (instance == null) {
            instance = new BossOfTheGangsManager();
        }
        return instance;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                long st = System.currentTimeMillis();
                for (int i = this.bosses.size() - 1; i >= 0; i--) {
                    if (i < this.bosses.size()) {
                        Boss boss = this.bosses.get(i);
                        try {
                            boss.update();
                        } catch (Exception e) {
                            Logger.logException(BossManager.class, e);
                            try {
                                removeBoss(boss);
                            } catch (Exception ex) {
                                Logger.logException(BossManager.class, ex);
                            }
                        }
                    }
                }
                if (150 - (System.currentTimeMillis() - st) > 0) {
                    Thread.sleep(150 - (System.currentTimeMillis() - st));
                }
            } catch (Exception e) {
                Logger.logException(BossManager.class, e);
            }
        }
    }
}






