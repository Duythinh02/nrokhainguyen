package nro.models.data;

import nro.models.player_system.Template.ArrHead2Frames;
import nro.models.player_system.Template.ItemOptionTemplate;
import nro.models.server.Manager;
import nro.models.utils.Logger;
import nro.models.network.Message;
import nro.models.network.MySession;
import nro.models.player_system.Template;

public class ItemData {

    // Client chỉ lưu đúng 1 gói "nạp lại" (type 1) và 1 gói "thêm" (type 2), mỗi gói lệnh -28 tối đa 65535 byte
    private static final int MAX_MSG_BYTES = 65500;
    private static final int SRC_ITEM_COUNT = 2000; // số item gốc của SRC, item AWN được thêm từ id 2000
    private static final int ITEM_FIXED_BYTES = 16; // type, gender, 2 độ dài UTF, level, strRequire, icon, part, upToUp
    private static volatile int cachedTemplateCount = -1;
    private static volatile int cachedSplit;
    private static volatile String[] cachedDescriptions;

    public static void updateItem(MySession session) {
        updateItemOptionItemplate(session);
        updateItemArrHead2FTemplate(session);
        prepareItemTemplates();
        int total = Manager.ITEM_TEMPLATES.size();
        updateItemTemplate(session, cachedSplit);
        if (cachedSplit < total) {
            updateItemTemplate(session, cachedSplit, total);
        }
    }

    // Tính điểm chia 2 gói và mô tả gửi cho client (cắt bớt mô tả dài nếu tổng vượt sức chứa của 2 gói)
    private static synchronized void prepareItemTemplates() {
        int total = Manager.ITEM_TEMPLATES.size();
        if (cachedTemplateCount == total && cachedDescriptions != null) {
            return;
        }
        int[] nameBytes = new int[total];
        int[] descBytes = new int[total];
        long sum = 0;
        int maxDesc = 0;
        int maxItem = 0;
        for (int i = 0; i < total; i++) {
            Template.ItemTemplate it = Manager.ITEM_TEMPLATES.get(i);
            nameBytes[i] = utfLength(it.name);
            descBytes[i] = utfLength(it.description);
            maxDesc = Math.max(maxDesc, descBytes[i]);
            sum += ITEM_FIXED_BYTES + nameBytes[i] + descBytes[i];
            maxItem = Math.max(maxItem, ITEM_FIXED_BYTES + nameBytes[i] + descBytes[i]);
        }
        // Chừa thêm 1 item lớn nhất vì gói 1 được lấp đầy theo thứ tự, gói 2 nhận phần còn lại
        long budget = 2L * MAX_MSG_BYTES - 16 - maxItem;
        // Ưu tiên giữ mô tả item gốc SRC: cắt mô tả item AWN (id >= SRC_ITEM_COUNT) trước, thiếu chỗ mới cắt item SRC
        int capAwn = maxDesc;
        int capSrc = maxDesc;
        if (sum > budget) {
            capAwn = findCap(nameBytes, descBytes, total, budget, maxDesc, true, capSrc);
            if (totalBytes(nameBytes, descBytes, total, capSrc, capAwn) > budget) {
                capAwn = 0;
                capSrc = findCap(nameBytes, descBytes, total, budget, maxDesc, false, capAwn);
            }
            Logger.warning("Item template: cắt mô tả gửi client (AWN " + capAwn + " byte, SRC " + capSrc + " byte) để vừa 2 gói\n");
        }
        String[] descs = new String[total];
        int split = 0;
        long bytes = 5;
        for (int i = 0; i < total; i++) {
            Template.ItemTemplate it = Manager.ITEM_TEMPLATES.get(i);
            int cap = i >= SRC_ITEM_COUNT ? capAwn : capSrc;
            descs[i] = descBytes[i] > cap ? truncateUtf(it.description, cap) : it.description;
            int itemBytes = ITEM_FIXED_BYTES + nameBytes[i] + utfLength(descs[i]);
            if (split == i && bytes + itemBytes <= MAX_MSG_BYTES) {
                bytes += itemBytes;
                split = i + 1;
            }
        }
        long rest = 7;
        for (int i = split; i < total; i++) {
            rest += ITEM_FIXED_BYTES + nameBytes[i] + utfLength(descs[i]);
        }
        if (rest > MAX_MSG_BYTES) {
            Logger.error("Item template: gói thứ 2 vẫn vượt giới hạn (" + rest + " byte)\n");
        }
        cachedDescriptions = descs;
        cachedSplit = split;
        cachedTemplateCount = total;
    }

    private static long totalBytes(int[] nameBytes, int[] descBytes, int total, int capSrc, int capAwn) {
        long t = 0;
        for (int i = 0; i < total; i++) {
            t += ITEM_FIXED_BYTES + nameBytes[i] + Math.min(descBytes[i], i >= SRC_ITEM_COUNT ? capAwn : capSrc);
        }
        return t;
    }

    // Tìm giới hạn mô tả lớn nhất cho một nhóm (AWN hoặc SRC) khi nhóm kia giữ giới hạn otherCap
    private static int findCap(int[] nameBytes, int[] descBytes, int total, long budget, int maxDesc, boolean awnGroup, int otherCap) {
        int lo = 0, hi = maxDesc;
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            long t = awnGroup ? totalBytes(nameBytes, descBytes, total, otherCap, mid) : totalBytes(nameBytes, descBytes, total, mid, otherCap);
            if (t <= budget) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        return lo;
    }

    private static String truncateUtf(String s, int maxBytes) {
        if (s == null) {
            return "";
        }
        int len = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int cb = (c >= 0x0001 && c <= 0x007F) ? 1 : (c > 0x07FF ? 3 : 2);
            if (len + cb > maxBytes) {
                return s.substring(0, i);
            }
            len += cb;
        }
        return s;
    }

    private static int utfLength(String s) {
        if (s == null) {
            return 0;
        }
        int len = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            len += (c >= 0x0001 && c <= 0x007F) ? 1 : (c > 0x07FF ? 3 : 2);
        }
        return len;
    }

    private static void updateItemOptionItemplate(MySession session) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);
            msg.writer().writeByte(DataGame.vsItem); //vcitem
            msg.writer().writeByte(0); //update option
            // Client đọc số lượng option bằng 1 byte (tối đa 255), gửi quá sẽ tràn và làm lệch cả gói tin
            int count = Math.min(255, Manager.ITEM_OPTION_TEMPLATES.size());
            msg.writer().writeByte(count);
            for (int i = 0; i < count; i++) {
                ItemOptionTemplate io = Manager.ITEM_OPTION_TEMPLATES.get(i);
                msg.writer().writeUTF(io.name);
                msg.writer().writeByte(io.type);
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateItemTemplate(MySession session, int count) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);

            msg.writer().writeByte(DataGame.vsItem); //vcitem
            msg.writer().writeByte(1); //reload itemtemplate
            msg.writer().writeShort(count);
            for (int i = 0; i < count; i++) {
                Template.ItemTemplate itemTemplate = Manager.ITEM_TEMPLATES.get(i);
                msg.writer().writeByte(itemTemplate.type);
                msg.writer().writeByte(itemTemplate.gender);
                msg.writer().writeUTF(itemTemplate.name);
                msg.writer().writeUTF(cachedDescriptions[i]);
                msg.writer().writeByte(itemTemplate.level);
                msg.writer().writeInt(itemTemplate.strRequire);
                msg.writer().writeShort(itemTemplate.iconID);
                msg.writer().writeShort(itemTemplate.part);
                msg.writer().writeBoolean(itemTemplate.isUpToUp);
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateItemTemplate(MySession session, int start, int end) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);

            msg.writer().writeByte(DataGame.vsItem); //vcitem
            msg.writer().writeByte(2); //add itemtemplate
            msg.writer().writeShort(start);
            msg.writer().writeShort(end);
            for (int i = start; i < end; i++) {
//                System.out.println("start: " + start + " -> " + end + " id " + Manager.ITEM_TEMPLATES.get(i).id);
                msg.writer().writeByte(Manager.ITEM_TEMPLATES.get(i).type);
                msg.writer().writeByte(Manager.ITEM_TEMPLATES.get(i).gender);
                msg.writer().writeUTF(Manager.ITEM_TEMPLATES.get(i).name);
                msg.writer().writeUTF(cachedDescriptions[i]);
                msg.writer().writeByte(Manager.ITEM_TEMPLATES.get(i).level);
                msg.writer().writeInt(Manager.ITEM_TEMPLATES.get(i).strRequire);
                msg.writer().writeShort(Manager.ITEM_TEMPLATES.get(i).iconID);
                msg.writer().writeShort(Manager.ITEM_TEMPLATES.get(i).part);
                msg.writer().writeBoolean(Manager.ITEM_TEMPLATES.get(i).isUpToUp);
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateItemArrHead2FTemplate(MySession session) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);
            msg.writer().writeByte(DataGame.vsItem); //vcitem
            msg.writer().writeByte(100);
            msg.writer().writeShort(Manager.ARR_HEAD_2_FRAMES.size());
            for (ArrHead2Frames io : Manager.ARR_HEAD_2_FRAMES) {
                msg.writer().writeByte(io.frames.size());
                for (int i : io.frames) {
                    msg.writer().writeShort(i);
                }
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
