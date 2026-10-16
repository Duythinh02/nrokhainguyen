package nro.models.boss.Boss_Manager;

/*
 * @Author: Anwin
 */

public class BossNomalManager extends BossManager {

    private static BossNomalManager instance;

    public static BossNomalManager gI() {
        if (instance == null) {
            instance = new BossNomalManager();
        }
        return instance;
    }

}






