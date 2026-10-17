package nro.models.shop_ky_gui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import nro.models.data.LocalManager;
import org.json.simple.JSONValue;

/**
 *
 * @author By Mr Blue
 *
 */

public class ConsignShopManager {

    private static ConsignShopManager instance;

    public static ConsignShopManager gI() {
        if (instance == null) {
            instance = new ConsignShopManager();
        }
        return instance;
    }

    public long lastTimeUpdate;

    public String[] tabName = {"Áo Quần", "Găng Tay", "Phụ Kiện", "Linh tinh", ""};

    // CopyOnWrite: người chơi mở shop (duyệt list) cùng lúc người khác mua/đăng bán không bị ConcurrentModificationException
    public List<ConsignItem> listItem = new CopyOnWriteArrayList<>();

    // Ghi trong 1 transaction: lỗi giữa chừng thì rollback, không để bảng ký gửi bị trống
    public synchronized void save() {
        try (Connection con = LocalManager.getConnection()) {
            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try (Statement s = con.createStatement();
                    PreparedStatement ps = con.prepareStatement("INSERT INTO `shop_ky_gui`(`id`, `player_id`, `tab`, `item_id`, `gold`, `gem`, `coin`, `quantity`, `itemOption`, `isUpTop`, `isBuy`) VALUES (?,?,?,?,?,?,?,?,?,?,?)")) {
                s.execute("DELETE FROM shop_ky_gui");
                for (ConsignItem it : this.listItem) {
                    if (it == null) {
                        continue;
                    }
                    String options = JSONValue.toJSONString(it.options);
                    ps.setInt(1, it.id);
                    ps.setInt(2, it.player_sell);
                    ps.setInt(3, it.tab);
                    ps.setInt(4, it.itemId);
                    ps.setInt(5, it.goldSell);
                    ps.setInt(6, it.gemSell);
                    ps.setInt(7, it.coinSell);
                    ps.setInt(8, it.quantity);
                    ps.setString(9, options == null || options.equals("null") ? "[]" : options);
                    ps.setInt(10, it.isUpTop);
                    ps.setInt(11, it.isBuy ? 1 : 0);
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(autoCommit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

