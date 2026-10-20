package nro.models.data;

import nro.models.player_system.Template.HeadAvatar;
import nro.models.player_system.Template.MapTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import nro.models.utils.FileIO;
import nro.models.services.Service;
import nro.models.skill.NClass;
import nro.models.skill.Skill;
import nro.models.player_system.Template.MobTemplate;
import nro.models.player_system.Template.NpcTemplate;
import nro.models.player_system.Template.SkillTemplate;
import nro.models.interfaces.ISession;
import java.io.ByteArrayOutputStream;
import nro.models.network.Message;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nro.models.server.Manager;
import nro.models.network.MySession;
import nro.models.utils.Logger;

import nro.models.player_system.Template.BgItem;

public class DataGame {

    // Tự động tăng khi data/update_data thay đổi (xem autoVersionData), không cần sửa tay.
    // Lưu ý: client đọc part theo số khung cố định (đầu 3, thân 17, chân 14), sai số khung sẽ lệch mọi part phía sau
    public static byte vsData = 15;
    public static byte vsMap = 3;
    public static byte vsSkill = 1;
    public static byte vsItem = 13; // 13: thêm Bông tai VIP (2693)
    public static int vsRes = 1;
    public static short maxSmallVersion = 32767;

    public static String LINK_IP_PORT = "Ngọc Rồng Online:36.50.134.190:14445:0";
    public static Map<Object, Object> MAP_MOUNT_NUM = new HashMap<>();

    public static void sendVersionGame(MySession session) {
        Message msg;
        try {
            msg = Service.gI().messageNotMap((byte) 4);
            msg.writer().writeByte(vsData);
            msg.writer().writeByte(vsMap);
            msg.writer().writeByte(vsSkill);
            msg.writer().writeByte(vsItem);
            msg.writer().writeByte(0);

            long[] smtieuchuan = {1000L, 3000L, 15000L, 40000L, 90000L, 170000L, 340000L, 700000L,
                1500000L, 15000000L, 150000000L, 1500000000L, 5000000000L, 10000000000L, 40000000000L,
                50010000000L, 60010000000L, 70010000000L, 80010000000L, 100010000000L, 1000010000000L, 10000010000000L};
            msg.writer().writeByte(smtieuchuan.length);
            for (int i = 0; i < smtieuchuan.length; i++) {
                msg.writer().writeLong(smtieuchuan[i]);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    //vData
    public static void updateData(MySession session) {
        final byte[] dart = FileIO.readFile("data/update_data/dart");
        final byte[] arrow = FileIO.readFile("data/update_data/arrow");
        final byte[] effect = FileIO.readFile("data/update_data/effect");
        final byte[] image = FileIO.readFile("data/update_data/image");
        final byte[] part = FileIO.readFile("data/update_data/part");
        final byte[] skill = FileIO.readFile("data/update_data/skill");

        Message msg;
        try {
            msg = new Message(-87);
            msg.writer().writeByte(vsData);
            msg.writer().writeInt(dart.length);
            msg.writer().write(dart);
            msg.writer().writeInt(arrow.length);
            msg.writer().write(arrow);
            msg.writer().writeInt(effect.length);
            msg.writer().write(effect);
            msg.writer().writeInt(image.length);
            msg.writer().write(image);
            msg.writer().writeInt(part.length);
            msg.writer().write(part);
            msg.writer().writeInt(skill.length);
            msg.writer().write(skill);

            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //vMap
    public static void updateMap(MySession session) {
        Message msg;
        try {
            msg = Service.gI().messageNotMap((byte) 6);
            msg.writer().writeByte(vsMap);
            msg.writer().writeByte(Manager.MAP_TEMPLATES.length);
            for (MapTemplate temp : Manager.MAP_TEMPLATES) {
                msg.writer().writeUTF(temp.name);
            }
            msg.writer().writeByte(Manager.NPC_TEMPLATES.size());
            for (NpcTemplate temp : Manager.NPC_TEMPLATES) {
                msg.writer().writeUTF(temp.name);
                msg.writer().writeShort(temp.head);
                msg.writer().writeShort(temp.body);
                msg.writer().writeShort(temp.leg);
                msg.writer().writeByte(0);
            }
            msg.writer().writeByte(Manager.MOB_TEMPLATES.size());
            for (MobTemplate temp : Manager.MOB_TEMPLATES) {
                msg.writer().writeByte(temp.type);
                msg.writer().writeUTF(temp.name);
                msg.writer().writeInt(temp.hp);
                msg.writer().writeByte(temp.rangeMove);
                msg.writer().writeByte(temp.speed);
                msg.writer().writeByte(temp.dartType);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    //vSkill
    public static void updateSkill(MySession session) {
        Message msg;
        try {
            msg = new Message(-28);

            msg.writer().writeByte(7);
            msg.writer().writeByte(vsSkill);
            msg.writer().writeByte(0); //count skill option

            msg.writer().writeByte(Manager.NCLASS.size());
            for (NClass nClass : Manager.NCLASS) {
                msg.writer().writeUTF(nClass.name);

                msg.writer().writeByte(nClass.skillTemplatess.size());
                for (SkillTemplate skillTemp : nClass.skillTemplatess) {
                    msg.writer().writeByte(skillTemp.id);
                    msg.writer().writeUTF(skillTemp.name);
                    msg.writer().writeByte(skillTemp.maxPoint);
                    msg.writer().writeByte(skillTemp.manaUseType);
                    msg.writer().writeByte(skillTemp.type);
                    msg.writer().writeShort(skillTemp.iconId);
                    msg.writer().writeUTF(skillTemp.damInfo);
                    msg.writer().writeUTF("null");
                    if (skillTemp.id != 0) {
                        msg.writer().writeByte(skillTemp.skillss.size());
                        for (Skill skill : skillTemp.skillss) {
                            msg.writer().writeShort(skill.skillId);
                            msg.writer().writeByte(skill.point);
                            msg.writer().writeLong(skill.powRequire);
                            msg.writer().writeShort(skill.manaUse);
                            msg.writer().writeInt(skill.coolDown);
                            msg.writer().writeShort(skill.dx);
                            msg.writer().writeShort(skill.dy);
                            msg.writer().writeByte(skill.maxFight);
                            msg.writer().writeShort(skill.damage);
                            msg.writer().writeShort(skill.price);
                            msg.writer().writeUTF(skill.moreInfo);
                        }
                    } else {
                        //Thêm 2 skill trống 105, 106
                        msg.writer().writeByte(skillTemp.skillss.size() + 2);
                        for (Skill skill : skillTemp.skillss) {
                            msg.writer().writeShort(skill.skillId);
                            msg.writer().writeByte(skill.point);
                            msg.writer().writeLong(skill.powRequire);
                            msg.writer().writeShort(skill.manaUse);
                            msg.writer().writeInt(skill.coolDown);
                            msg.writer().writeShort(skill.dx);
                            msg.writer().writeShort(skill.dy);
                            msg.writer().writeByte(skill.maxFight);
                            msg.writer().writeShort(skill.damage);
                            msg.writer().writeShort(skill.price);
                            msg.writer().writeUTF(skill.moreInfo);
                        }
                        for (int i = 105; i <= 106; i++) {
                            msg.writer().writeShort(i);
                            msg.writer().writeByte(0);
                            msg.writer().writeLong(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeInt(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeByte(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeUTF("");
                        }
                    }
                }
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    public static void sendDataImageVersion(MySession session) {
        Message msg;
        try {
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    public static void sendEffectTemplate(MySession session, int id, int... idtemp) {
        int idT = id;
        if (idtemp.length > 0 && idtemp[0] != 0) {
            idT = idtemp[0];
        }
        Message msg;
        try {
            final byte[] effData = FileIO.readFile("data/effdata/DataEffect_" + idT);
            final byte[] effImg = FileIO.readFile("data/effect/x" + session.zoomLevel + "/ImgEffect_" + idT + ".png");
            if (effData == null || effImg == null) {
                return;
            }
            msg = new Message(-66);
            msg.writer().writeShort(id);
            msg.writer().writeInt(effData.length);
            msg.writer().write(effData);
            if (session.version > 220) {
                msg.writer().write(idT == 60 ? 2 : 0);
            }
            msg.writer().writeInt(effImg.length);
            msg.writer().write(effImg);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendBgItemVersion(MySession session) {
        Message msg;
        try {
            msg = new Message(-93);
            msg.writer().writeShort(Manager.BG_ITEMS.size());
            for (BgItem bgItem : Manager.BG_ITEMS) {
                msg.writer().writeByte(bgItem.id);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    public static void sendItemBGTemplate(MySession session, int id) {
        Message msg;
        try {
            final byte[] bg_temp = FileIO.readFile("data/item_bg_temp/x" + session.zoomLevel + "/" + id + ".png");
            if (bg_temp == null) {
                return;
            }
            msg = new Message(-32);
            msg.writer().writeShort(id);
            msg.writer().writeInt(bg_temp.length);
            msg.writer().write(bg_temp);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    public static void sendDataItemBG(MySession session) {
        Message msg;
        try {
            msg = new Message(-31);
            msg.writer().writeShort(Manager.BG_ITEMS.size());
            for (BgItem bgItem : Manager.BG_ITEMS) {
                msg.writer().writeShort(bgItem.idImage);
                msg.writer().writeByte(bgItem.layer);
                msg.writer().writeShort(bgItem.dx);
                msg.writer().writeShort(bgItem.dy);
                msg.writer().writeByte(0);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final java.util.Map<String, byte[]> ICON_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final byte[] NO_ICON = new byte[0];

    // Icon theo zoom; thiếu thì lấy zoom khác, thiếu hẳn thì dùng icon thay thế (id 0)
    // để client không xin lại liên tục (gây lag hành trang khi có item thiếu icon)
    private static byte[] getIconBytes(int zoom, int id) {
        String key = zoom + "/" + id;
        byte[] cached = ICON_CACHE.get(key);
        if (cached != null) {
            return cached.length == 0 ? null : cached;
        }
        byte[] icon = FileIO.readFile("data/icon/x" + zoom + "/" + id + ".png");
        int[] order = {zoom, 2, 3, 1, 4};
        for (int z : order) {
            if (icon != null) {
                break;
            }
            icon = FileIO.readFile("data/icon/x" + z + "/" + id + ".png");
        }
        if (icon == null && id != 0) {
            icon = getIconBytes(zoom, 0);
        }
        ICON_CACHE.put(key, icon == null ? NO_ICON : icon);
        return icon;
    }

    public static void sendIcon(MySession session, int id) {
        Message msg;
        try {
            final byte[] icon = getIconBytes(session.zoomLevel, id);

            if (icon == null) {
                return;
            }

            msg = new Message(-67);
            msg.writer().writeInt(id);
            msg.writer().writeInt(icon.length);
            msg.writer().write(icon);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String VS_DATA_FILE = "data/update_data/vsdata.txt";
    private static final String[] UPDATE_DATA_FILES = {"dart", "arrow", "effect", "image", "part", "skill"};

    // Gọi sau khi tạo lại data/update_data/part lúc khởi động: nội dung đổi so với lần trước thì tự tăng vsData
    // để client tải lại (không cần sửa tay vsData nữa). Lưu "version:crc" vào VS_DATA_FILE.
    public static void autoVersionData() {
        try {
            java.util.zip.CRC32 crc = new java.util.zip.CRC32();
            for (String f : UPDATE_DATA_FILES) {
                byte[] b = FileIO.readFile("data/update_data/" + f);
                if (b != null) {
                    crc.update(b);
                }
            }
            long hash = crc.getValue();
            File file = new File(VS_DATA_FILE);
            if (file.exists()) {
                String[] s = new String(java.nio.file.Files.readAllBytes(file.toPath())).trim().split(":");
                byte saved = Byte.parseByte(s[0].trim());
                long savedHash = Long.parseLong(s[1].trim());
                vsData = saved;
                if (savedHash != hash) {
                    vsData = (byte) (saved >= 127 ? 1 : saved + 1);
                }
            }
            java.nio.file.Files.write(file.toPath(), (vsData + ":" + hash).getBytes());
            Logger.success("Data version (vsData): " + vsData + "\n");
        } catch (Exception e) {
            Logger.logException(DataGame.class, e, "Lỗi tự đánh version data");
        }
    }

    // Icon sửa từ mốc này trở đi được đánh version theo thời gian sửa file để client tải lại;
    // icon cũ giữ version 0 để người chơi không phải tải lại toàn bộ.
    private static final long ICON_VERSION_SINCE = java.time.LocalDate.of(2026, 10, 7)
            .atStartOfDay(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli();
    private static byte[] smallVersions;

    private static synchronized byte[] getSmallVersions() {
        if (smallVersions == null) {
            long[] latest = new long[maxSmallVersion];
            for (int z = 1; z <= 4; z++) {
                File[] files = new File("data/icon/x" + z).listFiles((d, n) -> n.endsWith(".png"));
                if (files == null) {
                    continue;
                }
                for (File f : files) {
                    try {
                        int id = Integer.parseInt(f.getName().substring(0, f.getName().length() - 4));
                        if (id >= 0 && id < maxSmallVersion) {
                            latest[id] = Math.max(latest[id], f.lastModified());
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            byte[] v = new byte[maxSmallVersion];
            int changed = 0;
            for (int i = 0; i < maxSmallVersion; i++) {
                if (latest[i] >= ICON_VERSION_SINCE) {
                    v[i] = (byte) ((latest[i] / 1000) % 127 + 1);
                    changed++;
                }
            }
            smallVersions = v;
            Logger.success("Icon có version mới: " + changed + "\n");
        }
        return smallVersions;
    }

    public static void sendSmallVersion(MySession session) {
        Message msg;
        try {
            byte[] versions = getSmallVersions();
            msg = new Message(-77);
            msg.writer().writeShort(maxSmallVersion);
            for (int i = 0; i < maxSmallVersion; i++) {
                msg.writer().writeByte(versions[i]);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void requestMobTemplate(MySession session, int id) {
        Message msg;
        try {
//            if (!session.check && id > 106) {
//                byte[] mob = FileIO.readFile("data/mob/x" + session.zoomLevel + "/" + 0);
//                msg = new Message(11);
//                msg.writer().writeByte(id);
//                msg.writer().write(mob);
//                session.sendMessage(msg);
//                msg.cleanup();
//                return;
//            }
            final byte[] mob = FileIO.readFile("data/mob/x" + session.zoomLevel + "/" + id);
            msg = new Message(11);
            msg.writer().writeByte(id);
            msg.writer().write(mob);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void sendTileSetInfo(MySession session) {
        Message msg;
        try {
            final byte[] data = FileIO.readFile("data/map/tile_set_info");
            msg = new Message(-82);
            msg.writer().write(data);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //data vẽ map
    public static void sendMapTemp(MySession session, int id) {
        Message msg;
        try {
            final byte[] data = FileIO.readFile("data/map/tile_map_data/" + id);
            if (data == null) {
                return;
            }
            msg = new Message(-28);
            msg.writer().writeByte(10);
            msg.writer().write(data);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    //head-avatar
    public static void sendHeadAvatar(Message msg) {
        try {
            msg.writer().writeShort(Manager.HEAD_AVATARS.size());
            for (HeadAvatar ha : Manager.HEAD_AVATARS) {
                msg.writer().writeShort(ha.headId);
                msg.writer().writeShort(ha.avatarId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendImageByName(MySession session, String imgName) {
        Message msg;
        try {
            msg = new Message(66);
            msg.writer().writeUTF(imgName);
            msg.writer().writeByte(Manager.getNFrameImageByName(imgName));

            final byte[] data = FileIO.readFile("data/img_by_name/x" + session.zoomLevel + "/" + imgName + ".png");

            if (data == null) {
                msg.writer().writeInt(0);
            } else {
                msg.writer().writeInt(data.length);
                msg.writer().write(data);
            }

            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendVersionRes(ISession session) {
        Message msg;
        try {
            msg = new Message(-74);
            msg.writer().writeByte(0);
            msg.writer().writeInt(vsRes);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendSizeRes(MySession session) {
        Message msg;
        try {
            msg = new Message(-74);
            msg.writer().writeByte(1);
            final File[] files = new File("data/res/x" + session.zoomLevel).listFiles();
            if (files != null) {
                msg.writer().writeShort(files.length);
            } else {
                msg.writer().writeShort(0);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendRes(MySession session) {
        Message msg;
        try {
            File dir = new File("data/res/x" + session.zoomLevel);
            File[] files = dir.listFiles();
            if (files != null) {
                for (final File fileEntry : files) {
                    String original = fileEntry.getName();
                    try (FileChannel fileChannel = FileChannel.open(fileEntry.toPath(), StandardOpenOption.READ)) {
                        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        int bytesRead;
                        while ((bytesRead = fileChannel.read(buffer)) > 0) {
                            buffer.flip();
                            byteArrayOutputStream.write(buffer.array(), 0, bytesRead);
                            buffer.clear();
                        }
                        byte[] res = byteArrayOutputStream.toByteArray();
                        msg = new Message(-74);
                        msg.writer().writeByte(2);
                        msg.writer().writeUTF(original);
                        msg.writer().writeInt(res.length);
                        msg.writer().write(res);
                        session.sendMessage(msg);
                        msg.cleanup();
                    } catch (IOException e) {
                        Logger.logException(DataGame.class, e);
                    }
                }
            }
            msg = new Message(-74);
            msg.writer().writeByte(3);
            msg.writer().writeInt(vsRes);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    public static void sendLinkIP(MySession session) {
        Message msg;
        try {
            msg = new Message(-29);
            msg.writer().writeByte(2);
            msg.writer().writeUTF(LINK_IP_PORT + ",0,0");
            msg.writer().writeByte(1);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

public static File getIconFile(int iconId) {
    int[] zoomTry = {4, 3, 2, 1};
    for (int z : zoomTry) {
        File f = getIconFile(iconId, z);
        if (f.exists() && f.isFile()) return f;
    }
    return getIconFile(iconId, 1);
}

public static File getIconFile(int iconId, int zoomLevel) {
    return new File("data/icon/x" + zoomLevel + "/" + iconId + ".png");
}
}
