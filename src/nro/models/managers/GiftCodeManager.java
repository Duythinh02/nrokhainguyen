package nro.models.managers;
import nro.models.data.LocalManager;
import nro.models.player_system.GiftCode;
import nro.models.player.Player;
import nro.models.map.service.NpcService;
import nro.models.services.Service;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import nro.models.item.Item;
import nro.models.services.InventoryService;
import nro.models.server.Manager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class GiftCodeManager {

    public String name;
    public final ArrayList<GiftCode> listGiftCode = new ArrayList<>();

    private static GiftCodeManager instance;

    public static GiftCodeManager gI() {
        if (instance == null) {
            instance = new GiftCodeManager();
        }
        return instance;
    }

    /**
     * Đọc 1 dòng bảng giftcode (dùng chung cho lúc khởi động và nút Reload ở admin panel)
     */
    public static GiftCode fromResultSet(ResultSet rs) throws SQLException {
        GiftCode giftcode = new GiftCode();
        giftcode.code = rs.getString("code").trim();
        giftcode.id = rs.getInt("id");
        giftcode.countLeft = rs.getInt("count_left");
        if (giftcode.countLeft == -1) {
            giftcode.countLeft = 999999999;
        }
        giftcode.datecreate = rs.getTimestamp("datecreate");
        giftcode.dateexpired = rs.getTimestamp("expired");
        JSONArray jar = (JSONArray) JSONValue.parse(rs.getString("detail"));
        if (jar != null) {
            for (int i = 0; i < jar.size(); ++i) {
                JSONObject jsonObj = (JSONObject) jar.get(i);

                int id = Integer.parseInt(jsonObj.get("id").toString());
                int quantity = Integer.parseInt(jsonObj.get("quantity").toString());

                JSONArray option = (JSONArray) jsonObj.get("options");
                ArrayList<Item.ItemOption> optionList = new ArrayList<>();

                if (option != null) {
                    for (int u = 0; u < option.size(); u++) {
                        JSONObject jsonobject = (JSONObject) option.get(u);
                        int optionId = Integer.parseInt(jsonobject.get("id").toString());
                        int param = Integer.parseInt(jsonobject.get("param").toString());
                        optionList.add(new Item.ItemOption(optionId, param));
                    }
                }

                giftcode.option.put(id, optionList);
                giftcode.detail.put(id, quantity);
            }
        }
        return giftcode;
    }

    /**
     * Tải lại toàn bộ giftcode từ DB, trả về số code đã tải
     */
    public int reload() throws Exception {
        ArrayList<GiftCode> list = new ArrayList<>();
        try (Connection con = LocalManager.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM giftcode");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(fromResultSet(rs));
            }
        }
        synchronized (listGiftCode) {
            listGiftCode.clear();
            listGiftCode.addAll(list);
        }
        return list.size();
    }

    public GiftCode checkUseGiftCode(Player player, String code) {
        if (code == null) {
            return null;
        }
        code = code.trim();
        synchronized (listGiftCode) {
            for (GiftCode giftCode : listGiftCode) {
                if (giftCode.code.equalsIgnoreCase(code)) {
                    // Kiểm tra hết các điều kiện TRƯỚC khi trừ lượt, để người chơi không bị mất lượt oan
                    if (giftCode.timeCode()) {
                        Service.gI().sendThongBaoOK(player, "Code đã hết hạn");
                        return null;
                    }
                    for (int id : giftCode.detail.keySet()) {
                        if (id >= 0 && (id >= Manager.ITEM_TEMPLATES.size() || Manager.ITEM_TEMPLATES.get(id) == null)) {
                            Service.gI().sendThongBaoOK(player, "Giftcode đang lỗi vật phẩm, vui lòng báo admin");
                            return null;
                        }
                    }
                    if (giftCode.countLeft <= 0) {
                        Service.gI().sendThongBaoOK(player, "Giftcode đã hết");
                        return null;
                    } else if (giftCode.isUsedGiftCode(player)) {
                        Service.gI().sendThongBaoOK(player, "Tham lam!");
                        return null;
                    }
                    // vàng/ngọc/hồng ngọc (id âm) cộng thẳng vào tiền, không chiếm ô hành trang
                    int slotNeed = (int) giftCode.detail.keySet().stream().filter(id -> id >= 0).count();
                    if (InventoryService.gI().getCountEmptyBag(player) < slotNeed) {
                        Service.gI().sendThongBaoOK(player, "Cần tối thiểu " + slotNeed + " ô hành trang trống");
                        return null;
                    }
                    giftCode.countLeft -= 1;
                    player.giftCode.add(giftCode.code);
                    updateGiftCode(giftCode);
                    return giftCode;
                }
            }
        }
        Service.gI().sendThongBao(player, "Code không chính xác!");
        return null;
    }

    public void updateGiftCode(GiftCode giftcode) {
        try {
            LocalManager.executeUpdate("update giftcode set count_left = ? where id = ?", giftcode.countLeft, giftcode.id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkInfomationGiftCode(Player p) {
        StringBuilder sb = new StringBuilder();
        for (GiftCode giftCode : listGiftCode) {
            sb.append("Code: ").append(giftCode.code).append(", Số lượng còn lại: ").append(giftCode.countLeft).append("\b")
                    .append("Ngày tạo: ")
                    .append(giftCode.datecreate).append(", Ngày hết hạn: ").append(giftCode.dateexpired)
                    .append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        NpcService.gI().createTutorial(p, 5073, sb.toString());
    }


    public static String removeCharAt(String s, int pos) {
        return s.substring(0, pos) + s.substring(pos + 1);
    }
}
