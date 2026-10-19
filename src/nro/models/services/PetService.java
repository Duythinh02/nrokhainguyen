package nro.models.services;

import nro.models.consts.ConstDetu;
import nro.models.utils.Logger;
import nro.models.consts.ConstPlayer;
import nro.models.item.Item;
import nro.models.player.NewPet;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.map.service.ChangeMapService;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 *
 */
public class PetService {

    private static PetService instance;

    public static PetService gI() {
        if (instance == null) {
            instance = new PetService();
        }
        return instance;
    }

    // Đệ tử thường chỉ nhận được khi hạ Super Broly; đệ của người chơi hạ boss thì tính cho chủ
    public void rewardNormalPetFromBoss(Player plKill, String bossName, int gender) {
        Player owner = plKill;
        if (plKill instanceof Pet) {
            owner = ((Pet) plKill).master;
        }
        if (owner == null || owner.isBot || owner instanceof Pet) {
            return;
        }
        if (owner.pet == null) {
            createNormalPet(owner, gender);
            Service.gI().sendThongBao(owner, "Bạn đã thu phục được đệ tử của " + bossName);
        }
    }

    // Đệ tử VIP (Uub, Kid Beer, Jiren): bật bằng pet.vip.enabled=true trong Config.properties
    public static boolean isVipPetEnabled() {
        return readPetFlag("pet.vip.enabled");
    }

    // Đệ tử Mabư (nở trứng): bật bằng pet.mabu.enabled=true trong Config.properties
    public static boolean isMabuPetEnabled() {
        return readPetFlag("pet.mabu.enabled");
    }

    private static boolean readPetFlag(String key) {
        try (java.io.FileInputStream f = new java.io.FileInputStream("Config.properties")) {
            java.util.Properties pr = new java.util.Properties();
            pr.load(f);
            return "true".equalsIgnoreCase(pr.getProperty(key, "false").trim());
        } catch (Exception e) {
            return false;
        }
    }

    public void createNormalPet(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, false, false, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận tao làm đệ tử");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createNormalPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, false, false);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận tao làm đệ tử");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createMabuPet(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, true, false, false, false, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Oa oa oa...");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createMabuPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, true, false, false, false);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Oa oa oa...");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createUubPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, true, false, false, (byte) player.gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận tao làm đệ tử");
            } catch (Exception e) {
            }
        }).start();
    }
    
    public void createJirenPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, false,true, (byte) player.gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận tao làm đệ tử");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createKidBeerPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, true, false, (byte) player.gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Hãy hợp tác với ta, Kakarot!");
            } catch (Exception e) {
            }
        }).start();
    }

    public void changeNormalPet(Player player, int gender) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createNormalPet(player, gender, limitPower);
    }

    public void changeNormalPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createNormalPet(player, limitPower);
    }

    public void changeMabuPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createMabuPet(player, limitPower);
    }

    public void changeUubPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createUubPet(player, player.pet.gender, limitPower);
    }

    public void changeKidBeerPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createKidBeerPet(player, player.pet.gender, limitPower);
    }
    
    public void changeJirenPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createKidBeerPet(player, player.pet.gender, limitPower);
    }

    public void changeMabuPet(Player player, int gender) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createMabuPet(player, gender, limitPower);
    }

    public void changeNamePet(Player player, String name) {
        try {
            if (!InventoryService.gI().isExistItemBag(player, 400)) {
                Service.gI().sendThongBao(player, "Bạn cần thẻ đặt tên đệ tử, mua tại Santa");
                return;
            } else if (Util.haveSpecialCharacter(name)) {
                Service.gI().sendThongBao(player, "Tên không được chứa ký tự đặc biệt");
                return;
            } else if (name.length() > 10) {
                Service.gI().sendThongBao(player, "Tên quá dài");
                return;
            }
            ChangeMapService.gI().exitMap(player.pet);
            player.pet.name = "$" + name.toLowerCase().trim();
            InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, 400), 1);
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Service.gI().chatJustForMe(player, player.pet, "Cảm ơn sư phụ đã đặt cho con tên " + name);
                } catch (Exception e) {
                }
            }).start();
        } catch (Exception ex) {

        }
    }

    private int[] getDataPetNormal() {
        int[] petData = new int[5];
        petData[0] = Util.nextInt(40, 105) * 20; //hp
        petData[1] = Util.nextInt(40, 105) * 20; //mp
        petData[2] = Util.nextInt(20, 45); //dame
        petData[3] = Util.nextInt(9, 50); //def
        petData[4] = Util.nextInt(0, 2); //crit
        return petData;
    }

    private int[] getDataPetMabu() {
        int[] petData = new int[5];
        petData[0] = Util.nextInt(40, 105) * 20; //hp
        petData[1] = Util.nextInt(40, 105) * 20; //mp
        petData[2] = Util.nextInt(50, 120); //dame
        petData[3] = Util.nextInt(9, 50); //def
        petData[4] = Util.nextInt(0, 2); //crit
        return petData;
    }

    private int[] getDataPetUub() {
        int[] petData = new int[5];
        petData[0] = 400_000; // hp
        petData[1] = 400_000; // mp
        petData[2] = 20_000;  // dame
        petData[3] = Util.nextInt(9, 50); //def
        petData[4] = Util.nextInt(0, 2); //crit
        return petData;
    }

    private int[] getDataPetKidBeer() {
        int[] petData = new int[5];
        petData[0] = 400_000; // hp
        petData[1] = 400_000; // mp
        petData[2] = 20_000;  // dame
        petData[3] = Util.nextInt(9, 50); //def
        petData[4] = Util.nextInt(0, 2); //crit
        return petData;
    }
    
    private int[] getDataPetJiren() {
        int[] petData = new int[5];
        petData[0] = 400_000; // hp
        petData[1] = 400_000; // mp
        petData[2] = 20_000;  // dame
        petData[3] = Util.nextInt(9, 50); //def
        petData[4] = Util.nextInt(0, 2); //crit
        return petData;
    }

    private void createNewPet(Player player, boolean isMabu, boolean isUub, boolean isKidBeer, boolean isJiren, byte... gender) {
        int[] data;

        if (isMabu) {
            data = getDataPetMabu();
        } else if (isUub) {
            data = getDataPetUub();
        } else if (isKidBeer) {
            data = getDataPetKidBeer();
        } else if (isJiren) {
            data = getDataPetJiren();
        } else {
            data = getDataPetNormal();
        }

        Pet pet = new Pet(player);

        pet.name = "$" + (isMabu ? "Mabư" : isUub ? "Uub" : isKidBeer ? "Kid Beer" : isJiren ? "Kid Jiren" : "Đệ tử");

        pet.gender = (gender != null && gender.length != 0) ? gender[0] : (byte) Util.nextInt(0, 2);

        pet.id = player.isPl() ? -player.id : -Math.abs(player.id) - 100000;

        pet.nPoint.power = isUub ? 40000000000L : isMabu ? 1500000L : isKidBeer ? 40000000000L : isJiren ? 40000000000L : 2000L;

        pet.typePet = (byte) (isMabu ? 1 : isUub ? 2 : isKidBeer ? 3 : isJiren ? 4 : 0);

        pet.nPoint.stamina = 1000;
        pet.nPoint.maxStamina = 1000;
        pet.nPoint.hpg = data[0];
        pet.nPoint.mpg = data[1];
        pet.nPoint.dameg = data[2];
        pet.nPoint.defg = data[3];
        pet.nPoint.critg = data[4];

        int itemBodySize = 7;
        if (pet.typePet == 2 || pet.typePet == 3 || pet.typePet == 4) {
            itemBodySize = 9;
        }
        for (int i = 0; i < itemBodySize; i++) {
            pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }

        pet.playerSkill.skills.add(SkillUtil.createSkill(Util.nextInt(0, 2) * 2, 1));
        for (int i = 0; i < 6; i++) {
            pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
        }

        pet.nPoint.setFullHpMp();
        player.pet = pet;
    }

    public static void Pet2(Player pl, int h, int b, int l) {
        if (pl.newPet != null) {
            pl.newPet.dispose();
        }
        pl.newPet = new NewPet(pl, (short) h, (short) b, (short) l);
        pl.newPet.name = "$";
        pl.newPet.gender = pl.gender;
        pl.newPet.nPoint.tiemNang = 1;
        pl.newPet.nPoint.power = 1;
        pl.newPet.nPoint.limitPower = 1;
        pl.newPet.nPoint.hpg = 500000000;
        pl.newPet.nPoint.mpg = 500000000;
        pl.newPet.nPoint.hp = 500000000;
        pl.newPet.nPoint.mp = 500000000;
        pl.newPet.nPoint.dameg = 1;
        pl.newPet.nPoint.defg = 1;
        pl.newPet.nPoint.critg = 1;
        pl.newPet.nPoint.stamina = 1;
        pl.newPet.nPoint.setBasePoint();
        pl.newPet.nPoint.setFullHpMp();
    }

    // Pet đi theo type 21: hình lấy thẳng từ head/body/leg của item template
    public static boolean petFollowFromTemplate(Player pl, Item item) {
        if (item == null || !item.isNotNullItem() || item.template.type != 21 || item.template.head < 0) {
            return false;
        }
        Pet2(pl, item.template.head, item.template.body, item.template.leg);
        Service.gI().point(pl);
        return true;
    }

    public void createNormalBot(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewBot(player, false);
                if (limitPower != null && limitPower.length == 1) {
                    player.Pet.nPoint.limitPower = limitPower[0];
                    player.Pet.nPoint.initPowerLimit();
                }
                CheckPlayer(player);
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Pet, "Xin hãy thu nhận con làm đệ tử");
            } catch (Exception e) {
                Logger.logException(PetService.class, e);
            }
        }).start();
    }

    public void createMabuBot(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewBot(player, true);
                if (limitPower != null && limitPower.length == 1) {
                    player.Pet.nPoint.limitPower = limitPower[0];
                    player.Pet.nPoint.initPowerLimit();
                }
                CheckPlayer(player);
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Pet, "Oa oa oa...");
            } catch (Exception e) {
                Logger.logException(PetService.class, e);
            }
        }).start();
    }

    private void createNewBot(Player player, boolean isMabu, byte... gender) {
        int[] data = isMabu ?  getDataPetMabu(): getDataPetNormal();
        Pet pet = new Pet(player);
        pet.name = "$" + (isMabu ? "Mabư" : "Đệ tử");
        pet.gender = (gender != null && gender.length != 0) ? gender[0] : (byte) Util.nextInt(0, 2);
        pet.id = player.isPl() ? -player.id : -Math.abs(player.id) - 1_000_000;
        pet.nPoint.power = isMabu ? 1500000 : 2000;
        pet.typeDeTu = (byte) (isMabu ? 1 :0);
        pet.thuctinh = (byte) 0;
        pet.nPoint.stamina = 1000;
        pet.nPoint.maxStamina = 1000;
        pet.nPoint.hpg = data[0];
        pet.nPoint.mpg = data[1];
        pet.nPoint.dameg = data[2];
        pet.nPoint.defg = data[3];
        pet.nPoint.critg = data[4];
        for (int j = 0; j < ConstPlayer.QTY_MAX_ITEM_BODY_PET ; j++) {
            pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }
        pet.playerSkill.skills.add(SkillUtil.createSkill(Util.nextInt(0, 2) * 2, 1));
        for (int j = 0; j < ConstPlayer.QTY_MAX_SKILL_PET; j++) {
            pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
        }
        pet.nPoint.setFullHpMp();
        player.Pet = pet;
    }

    private void CheckPlayer(Player player) {
        player.Pet.nPoint.defg = Util.nextInt(1, 200);
        player.Pet.nPoint.def = player.Pet.nPoint.defg;
        player.Pet.nPoint.critg = Util.nextInt(0, 5);
        player.Pet.nPoint.crit = player.Pet.nPoint.critg;
        player.Pet.nPoint.stamina = 1000;
        player.Pet.nPoint.maxStamina = 1000;
        if (player.zone != null) {
            int MapId = player.zone.map.mapId;
            switch (MapId) {
                case 30:
                case 6:
                case 34:
                case 10:
                case 38:
                case 19:
                    player.Pet.nPoint.power = Util.nextInt(2_000_000, 100_000_000);
                    player.Pet.nPoint.hpg = Util.nextInt(5_000, 20_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.hp = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.hpMax = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.mpg = Util.nextInt(5_000, 20_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.mp = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.mpMax = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.dameg = Util.nextInt(300, 1_500) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.dameg * 10 / 100) : 1);
                    player.Pet.nPoint.dame = player.Pet.nPoint.dameg;
                    break;
                case 68:
                case 69:
                    player.Pet.nPoint.power = Util.nextInt(50_000_000, 140_000_000);
                    player.Pet.nPoint.hpg = Util.nextInt(20_000, 50_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.hp = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.hpMax = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.mpg = Util.nextInt(20_000, 50_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.mp = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.mpMax = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.dameg = Util.nextInt(1_000, 5_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.dameg * 10 / 100) : 1);
                    player.Pet.nPoint.dame = player.Pet.nPoint.dameg;
                    break;
                case 70:
                case 71:
                case 72:
                case 63:
                case 64:
                case 65:
                    player.Pet.nPoint.power = Util.nextInt(150_000_000, 1_500_000_000);
                    player.Pet.nPoint.hpg = Util.nextInt(50_000, 100_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.hp = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.hpMax = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.mpg = Util.nextInt(50_000, 100_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.mp = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.mpMax = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.dameg = Util.nextInt(3_000, 8_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.dameg * 10 / 100) : 1);
                    player.Pet.nPoint.dame = player.Pet.nPoint.dameg;
                    break;
                case 66:
                case 67:
                case 73:
                case 74:
                case 75:
                case 76:
                    player.Pet.nPoint.power = Util.nextLong(1_500_000_000L, 10_000_000_000L);
                    player.Pet.nPoint.hpg = Util.nextInt(100_000, 150_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.hp = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.hpMax = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.mpg = Util.nextInt(100_000, 150_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.mp = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.mpMax = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.dameg = Util.nextInt(5_000, 20_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.dameg * 10 / 100) : 1);
                    player.Pet.nPoint.dame = player.Pet.nPoint.dameg;
                    break;
                case 77:
                case 81:
                case 82:
                case 83:
                case 79:
                case 80:
                case 92:
                case 93:
                    player.Pet.nPoint.power = Util.nextLong(10_000_000_000L, 20_000_000_000L);
                    player.Pet.nPoint.hpg = Util.nextInt(150_000, 220_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.hp = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.hpMax = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.mpg = Util.nextInt(150_000, 220_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.mp = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.mpMax = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.dameg = Util.nextInt(10_000, 25_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.dameg * 10 / 100) : 1);
                    player.Pet.nPoint.dame = player.Pet.nPoint.dameg;
                    break;
                case 94:
                case 97:
                case 98:
                case 100:
                    player.Pet.nPoint.power = Util.nextLong(20_000_000_000L, 30_000_000_000L);
                    player.Pet.nPoint.hpg = Util.nextInt(220_000, 300_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.hp = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.hpMax = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.mpg = Util.nextInt(220_000, 300_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.mp = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.mpMax = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.dameg = Util.nextInt(15_000, 29_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.dameg * 10 / 100) : 1);
                    player.Pet.nPoint.dame = player.Pet.nPoint.dameg;
                    break;
                case 105:
                case 106:
                case 107:
                case 108:
                case 109:
                case 110:
                    player.Pet.nPoint.power = Util.nextLong(30_000_000_000L, 60_000_000_000L);
                    player.Pet.nPoint.hpg = Util.nextInt(700_000, 1_000_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.hp = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.hpMax = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.mpg = Util.nextInt(700_000, 1_000_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.mp = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.mpMax = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.dameg = Util.nextInt(40_000, 70_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.dameg * 10 / 100) : 1);
                    player.Pet.nPoint.dame = player.Pet.nPoint.dameg;
                    break;
                case 123:
                case 192:
                case 197:
                case 200:
                    player.Pet.nPoint.power = Util.nextLong(30_000_000_000L, 50_000_000_000L);
                    player.Pet.nPoint.hpg = Util.nextInt(300_000, 450_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.hp = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.hpMax = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.mpg = Util.nextInt(300_000, 450_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.mp = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.mpMax = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.dameg = Util.nextInt(19_000, 30_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.dameg * 10 / 100) : 1);
                    player.Pet.nPoint.dame = player.Pet.nPoint.dameg;
                    break;
                default:
                    player.Pet.nPoint.power = Util.nextInt(2_000, 100_000);
                    player.Pet.nPoint.hpg = Util.nextInt(1_000, 3_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.hp = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.hpMax = player.Pet.nPoint.hpg;
                    player.Pet.nPoint.mpg = Util.nextInt(1_000, 3_000) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.hpg * 10 / 100) : 1);
                    player.Pet.nPoint.mp = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.mpMax = player.Pet.nPoint.mpg;
                    player.Pet.nPoint.dameg = Util.nextInt(29, 100) 
                            + (player.Pet.typeDeTu == ConstDetu.MABU ? (player.Pet.nPoint.dameg * 10 / 100) : 1);
                    player.Pet.nPoint.dame = player.Pet.nPoint.dameg;
                    break;
            }
        }
    }
}
