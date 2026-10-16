package nro.models.Bot.New;

import nro.models.services.PetService;
import nro.models.map.service.ChangeMapService;
import nro.models.services_func.UseItem;
import nro.models.services.PlayerService;
import nro.models.utils.Util;
import nro.models.consts.ConstPlayer;
import java.util.Random;
import nro.models.map.Zone;
import nro.models.player.Pet;
import nro.models.player.NPoint;
import nro.models.skill.Skill;

public class BotDetu_11 {
             
    long lastTimeEatPea;
         
    public Bot_new bot;
    
    private long TimeUseSkillXD;
    
    private long TimeUseSkillNM;
    
    private long TimeUseSkillTD;
    
    private long TimeRunMapxayda;
    private long TimeRunMaptraidat;
    private long TimeRunMapnamec;
    
    boolean isTrue = false;
       
    public BotDetu_11(Bot_new b){
        this.bot = b;
    }
    
    private int[] BuffPea = new int[]{16_000, 32_000, 64_000, 128_000, 256_000};
         
    public void update(){
        Random random = new Random();
        int Opstion = BuffPea[random.nextInt(BuffPea.length)];
        if (this.bot.zone != null) {
            if (this.bot.effectSkill.isStone || this.bot.effectSkin.isDraburaFrost || this.bot.isDie()) {
                return;
            }
            MoveTo();
            FixmoveTo(this.bot.location.y);
        }
        if (!this.bot.isDie() && Util.canDoWithTime(lastTimeEatPea, Util.nextInt(30_000, 120_000))) {
            UseItem.gI().eatPeaBot(this.bot, Opstion);
            lastTimeEatPea = System.currentTimeMillis();
        }
        if (this.bot.Pet == null && this.bot.type == 10) {
            if (Util.isTrue(90, 100)) {
                PetService.gI().createNormalBot(this.bot, NPoint.MAX_LIMIT);
            } else {
                PetService.gI().createMabuBot(this.bot, NPoint.MAX_LIMIT);
            }
        }
        if (this.bot.Pet != null) {
            this.bot.Pet.changeStatus(Pet.PROTECT);
        }
        if (!isTrue && this.bot.Pet != null) {
            if (Util.isTrue(50, 100)) {
                this.bot.Pet.IdBot = 1;
            } else if (Util.isTrue(20, 100)) {
                this.bot.Pet.IdBot = 2;
            } else if (Util.isTrue(20, 100)) {
                this.bot.Pet.IdBot = 3;
            } else if (Util.isTrue(50, 100)) {
                this.bot.Pet.IdBot = 4;
            } else {
                this.bot.Pet.IdBot = 5;
            }
            isTrue = true;
        }
        if (this.bot.Pet != null && this.bot.Pet.IdBot == 1) {
            this.bot.Pet.nPoint.tlHutHp = 70;
            this.bot.Pet.nPoint.tlHutMp = 70;
        } else if (this.bot.Pet != null && this.bot.Pet.IdBot == 2) {
            this.bot.Pet.nPoint.tlHutHp = 30;
            this.bot.Pet.nPoint.tlHutMp = 30;
            this.bot.Pet.nPoint.tlHutHpMpXQ = 5;
        } else if (this.bot.Pet != null && this.bot.Pet.IdBot == 3) {
            this.bot.Pet.nPoint.tlHutHp = 20;
            this.bot.Pet.nPoint.tlHutMp = 20;
            this.bot.Pet.nPoint.tlHutHpMpXQ = 4;
        } else if (this.bot.Pet != null && this.bot.Pet.IdBot == 4) {
            this.bot.Pet.nPoint.tlHutHp = 10;
            this.bot.Pet.nPoint.tlHutMp = 10;
            this.bot.Pet.nPoint.tlHutHpMpXQ = 2;
        } else if (this.bot.Pet != null && this.bot.Pet.IdBot == 5) {
            this.bot.Pet.nPoint.dame = this.bot.Pet.nPoint.dameg + (this.bot.Pet.nPoint.dameg * 25 / 100);
            this.bot.Pet.nPoint.tlHutHp = 20;
            this.bot.Pet.nPoint.tlHutMp = 20;
        }
        switch (this.bot.gender) {
            case ConstPlayer.XAYDA:
                if (this.bot.playerSkill.skillSelect != null && !this.bot.isDie() && Util.canDoWithTime(TimeUseSkillXD, Util.nextInt(30_000, 60_000))) {
                    this.bot.useSkill(Skill.TAI_TAO_NANG_LUONG);
                    TimeUseSkillXD = System.currentTimeMillis();
                }
                break;
            case ConstPlayer.TRAI_DAT:
                if (this.bot.playerSkill.skillSelect != null && !this.bot.isDie() && Util.canDoWithTime(TimeUseSkillTD, Util.nextInt(30_000, 60_000))) {
                    this.bot.useSkill(Skill.THAI_DUONG_HA_SAN);
                    TimeUseSkillTD = System.currentTimeMillis();
                }
                break;
            case ConstPlayer.NAMEC:
                if (this.bot.playerSkill.skillSelect != null && !this.bot.isDie() && Util.canDoWithTime(TimeUseSkillNM, Util.nextInt(30_000, 60_000))) {
                    this.bot.useSkill(Skill.TRI_THUONG);
                    TimeUseSkillNM = System.currentTimeMillis();
                }
                break;
        }
    }
    
    public void MoveTo() {
        switch (this.bot.gender) {
            case 0:
                if (Util.canDoWithTime(TimeRunMaptraidat, Util.nextInt(5_000, 60_000))) {
                    PlayerService.gI().playerMove(this.bot, this.bot.location.x + (Util.isTrue(1, 2) ? 1 : -1), this.bot.location.y);
                    TimeRunMaptraidat = System.currentTimeMillis();
                }
                break;
            case 1:
                if (Util.canDoWithTime(TimeRunMapnamec, Util.nextInt(5_000, 60_000))) {
                    PlayerService.gI().playerMove(this.bot, this.bot.location.x + (Util.isTrue(1, 2) ? 1 : -1), this.bot.location.y);
                    TimeRunMapnamec = System.currentTimeMillis();
                }
                break;
            case 2:
                if (Util.canDoWithTime(TimeRunMapxayda, Util.nextInt(5_000, 60_000))) {
                    PlayerService.gI().playerMove(this.bot, this.bot.location.x + (Util.isTrue(1, 2) ? 1 : -1), this.bot.location.y);
                    TimeRunMapxayda = System.currentTimeMillis();
                }
                break;
            default:
                break;
        }
    }
    
    public void FixmoveTo(int y) {
        if (this.bot.location.x < 200) {
            PlayerService.gI().playerMove(this.bot, Util.nextInt(220, 240), y);
        } else if (this.bot.location.x > 1300) {
            PlayerService.gI().playerMove(this.bot, Util.nextInt(1250, 1290), y);
        }
    }
    
    public void CheckPower() {
        long power = this.bot.nPoint.power;
        if (power >= 40_000_000_000L && power < 50_000_000_000L) {
            this.MapUp_28();
        } else if (power >= 50_000_000_000L && power < 60_000_000_000L) {
            this.MapUp_29();
        }
    }
    
    //--------------------------------------------------------------------------
    private int[] RandomLocation_x_12 = new int[]{1019 + Util.nextInt(0, 60), 1400 + Util.nextInt(-20, 20), 1750 + Util.nextInt(-20, 20)};
    public void MapUp_28(){
        Random random = new Random();
        int location = RandomLocation_x_12[random.nextInt(RandomLocation_x_12.length)];
        if (this.bot.zone != null && this.bot.zone.map.mapId != 197) {
            Zone zone = this.bot.getRandomZone(197);
            if (zone != null) {
                ChangeMapService.gI().goToMap(this.bot, zone);
                this.bot.zone.load_Me_To_Another(this.bot);
                PlayerService.gI().playerMove(this.bot, location, 744);
            }
        }
    }
    //--------------------------------------------------------------------------
    private int[] RandomLocation_x_13 = new int[]{580 + Util.nextInt(-20, 20), 915 + Util.nextInt(-20, 20), 1180 + Util.nextInt(-20, 20), 1430 + Util.nextInt(-20, 20)};
    public void MapUp_29(){
        Random random = new Random();
        int location = RandomLocation_x_13[random.nextInt(RandomLocation_x_13.length)];
        if (this.bot.zone != null && this.bot.zone.map.mapId != 200) {
            Zone zone = this.bot.getRandomZone(200);
            if (zone != null) {
                ChangeMapService.gI().goToMap(this.bot, zone);
                this.bot.zone.load_Me_To_Another(this.bot);
                PlayerService.gI().playerMove(this.bot, location, 576);
            }
        }
    }
}




