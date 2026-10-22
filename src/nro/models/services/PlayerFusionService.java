package nro.models.services;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.consts.ConstPlayer;
import nro.models.item.Item;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.NpcService;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.services_func.TransactionService;
import nro.models.utils.Logger;
import nro.models.utils.Util;

/**
 * Hợp thể Gogeta giữa 2 người chơi (Bông tai VIP).
 * - A bấm vào B -> "Hợp thể" (menu phụ SubMenuService), B đồng ý (hộp thoại Con mèo, NpcFactory).
 * - Yêu cầu: cả 2 người có Bông tai VIP trong hành trang; 1 người mặc CT Goku SSJ4, người kia CT Cađíc SSJ4; cùng khu.
 * - A (người dẫn) mang ngoại hình Gogeta, cộng toàn bộ HP / KI / sức đánh của B (giống hợp thể với đệ tử),
 *   điều khiển bình thường ở mọi map. Điểm săn boss A nhận khi hợp thể chia đôi cho B (BossHuntService).
 * - B (người xem) vẫn hiện nhân vật, đi theo A, không điều khiển được (Controller chặn lệnh), không nhận sát thương.
 * - Tách: A hoặc B bấm vào người kia -> "Tách hợp thể"; tự tách khi 1 người thoát game, A chết, cởi cải trang
 *   hoặc A hợp thể với đệ tử.
 */
public final class PlayerFusionService {

    public static final int ITEM_BONG_TAI_VIP = 2693;
    private static final int CT_GOKU_SSJ4 = 1553;
    private static final int CT_CADIC_SSJ4 = 1693;
    public static final short GOGETA_HEAD = 1578, GOGETA_BODY = 1581, GOGETA_LEG = 1582;

    // Hộp thoại Con mèo gửi cho người được mời
    public static final int MENU_XAC_NHAN_HOP_THE = 7903;
    // Hộp thoại Tách hợp thể khi bấm nút Đệ tử lúc đang hợp thể
    public static final int MENU_TACH_HOP_THE = 7904;
    private static final long REQUEST_TIMEOUT_MS = 60_000;
    private static final long COOLDOWN_MS = 30_000;
    // B cách A quá khoảng này thì kéo lại cạnh A
    private static final int FOLLOW_DISTANCE = 40;

    // id người được mời -> {id người mời, thời điểm}
    private static final Map<Long, long[]> requests = new ConcurrentHashMap<>();
    // id người dẫn (A) đang hợp thể
    private static final Set<Long> leaders = ConcurrentHashMap.newKeySet();

    private PlayerFusionService() {
    }

    public static boolean isWatcher(Player pl) {
        return pl != null && pl.fusionPartner != null && !pl.fusionLeader;
    }

    public static boolean isLeader(Player pl) {
        return pl != null && pl.fusionPartner != null && pl.fusionLeader;
    }

    // Đang hợp thể (A hoặc B) thì không được thay / cởi cải trang của bản thân lẫn đệ tử (InventoryService)
    public static boolean blockCostumeChange(Player pl, Item item) {
        if (pl == null || pl.fusionPartner == null || item == null || !item.isNotNullItem() || item.template.type != 5) {
            return false;
        }
        Service.gI().sendThongBao(pl, "Đang hợp thể, không thể thay cải trang của sư phụ lẫn đệ tử");
        return true;
    }

    // Người xem (và đệ tử của họ) không hiện với người chơi khác (Zone.infoPlayer)
    public static boolean isHiddenFrom(Player plInfo, Player plReceive) {
        if (isWatcher(plInfo)) {
            return true;
        }
        return plInfo.isPet && plInfo instanceof nro.models.player.Pet pet && isWatcher(pet.master) && plReceive != pet.master;
    }

    // Xoá người xem khỏi màn hình những người khác trong khu
    private static void hideFromOthers(Player b) {
        if (b.zone == null) {
            return;
        }
        for (Player pl : new java.util.ArrayList<>(b.zone.getPlayers())) {
            if (pl != null && pl != b) {
                Service.gI().exitMap(pl, b.id);
                if (b.pet != null) {
                    Service.gI().exitMap(pl, b.pet.id);
                }
            }
        }
    }

    // Hiệu ứng hợp thể / tách giống hợp thể Porata với đệ tử (Pet.fusionEffect): type = HOP_THE_PORATA khi hợp, 0 khi tách
    private static void fusionEffect(Player a, int type) {
        if (a.zone == null) {
            return;
        }
        nro.models.network.Message msg = null;
        try {
            msg = new nro.models.network.Message(125);
            msg.writer().writeByte(type);
            msg.writer().writeInt((int) a.id);
            Service.gI().sendMessAllPlayerInMap(a, msg);
        } catch (Exception e) {
            Logger.logException(PlayerFusionService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    // Chỉ gửi vị trí cho chính người xem (người khác không thấy họ)
    private static void setPosSelf(Player b, int x, int y) {
        b.location.x = x;
        b.location.y = y;
        nro.models.network.Message msg = null;
        try {
            msg = new nro.models.network.Message(123);
            msg.writer().writeInt((int) b.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeByte(1);
            b.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(PlayerFusionService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private static int caiTrang(Player pl) {
        if (pl.inventory == null || pl.inventory.itemsBody.size() < 6) {
            return -1;
        }
        Item ct = pl.inventory.itemsBody.get(5);
        return ct != null && ct.isNotNullItem() ? ct.template.id : -1;
    }

    private static String tenCaiTrang(Player pl) {
        Item ct = pl.inventory != null && pl.inventory.itemsBody.size() > 5 ? pl.inventory.itemsBody.get(5) : null;
        return ct != null && ct.isNotNullItem() ? ct.template.name : "chưa mặc cải trang";
    }

    // Một người Goku SSJ4, người kia Cađíc SSJ4
    private static boolean dungCaiTrang(Player a, Player b) {
        int ca = caiTrang(a), cb = caiTrang(b);
        return (ca == CT_GOKU_SSJ4 && cb == CT_CADIC_SSJ4) || (ca == CT_CADIC_SSJ4 && cb == CT_GOKU_SSJ4);
    }

    // Lý do không hợp thể được, null nếu được
    private static String check(Player a, Player b) {
        if (a == null || b == null || !a.isPl() || !b.isPl()) {
            return "Người chơi không tồn tại hoặc đang offline";
        }
        if (a.id == b.id) {
            return "Không thể tự hợp thể với chính mình";
        }
        if (a.fusionPartner != null || b.fusionPartner != null) {
            return "Một trong hai người đang hợp thể";
        }
        if (a.fusion.typeFusion != ConstPlayer.NON_FUSION || b.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            return "Hãy tách hợp thể với đệ tử trước";
        }
        if (a.zone == null || !a.zone.equals(b.zone)) {
            return "Hai người phải đứng cùng khu";
        }
        if (a.isDie() || b.isDie()) {
            return "Không thể hợp thể khi đang chết";
        }
        if (InventoryService.gI().findItemBag(a, ITEM_BONG_TAI_VIP) == null) {
            return a.name + " cần có Bông tai VIP trong hành trang (shop Uron, 500 Coin Khai Nguyên)";
        }
        if (InventoryService.gI().findItemBag(b, ITEM_BONG_TAI_VIP) == null) {
            return b.name + " cần có Bông tai VIP trong hành trang (shop Uron, 500 Coin Khai Nguyên)";
        }
        if (!dungCaiTrang(a, b)) {
            return "Một người mặc CT Goku SSJ4, người kia mặc CT Cađíc SSJ4 (" + a.name + ": " + tenCaiTrang(a)
                    + ", " + b.name + ": " + tenCaiTrang(b) + ")";
        }
        long now = System.currentTimeMillis();
        long conLai = COOLDOWN_MS - (now - Math.max(a.lastPlayerFusionEnd, b.lastPlayerFusionEnd));
        if (conLai > 0) {
            return "Vừa tách hợp thể, chờ " + ((conLai + 999) / 1000) + " giây nữa rồi thử lại";
        }
        if (TransactionService.gI().check(a) || TransactionService.gI().check(b)) {
            return "Không thể hợp thể khi đang giao dịch";
        }
        return null;
    }

    // ===================== Menu phụ khi bấm vào người chơi =====================
    // Gọi từ Service.getPlayerMenu: thêm nút Hợp thể / Tách hợp thể
    public static void showSubMenu(Player player, Player target) {
        if (player == null || target == null || !player.isPl() || !target.isPl()) {
            return;
        }
        if (player.fusionPartner != null) {
            if (player.fusionPartner == target) {
                SubMenuService.gI().showSubMenu(player, new SubMenuService.SubMenu(SubMenuService.TACH_HOP_THE,
                        "Tách hợp thể", "Kết thúc hợp thể Gogeta"));
            }
            return;
        }
        if (target.fusionPartner == null && InventoryService.gI().findItemBag(player, ITEM_BONG_TAI_VIP) != null) {
            SubMenuService.gI().showSubMenu(player, new SubMenuService.SubMenu(SubMenuService.HOP_THE,
                    "Hợp thể", "Hợp thể Gogeta (Bông tai VIP)"));
        }
    }

    // Nút Đệ tử khi đang hợp thể (Controller -107): hộp thoại Tách hợp thể
    public static void showMenuTach(Player pl) {
        Player partner = pl.fusionPartner;
        if (partner == null) {
            return;
        }
        Service.gI().hideWaitDialog(pl);
        NpcService.gI().createMenuConMeo(pl, MENU_TACH_HOP_THE, -1,
                "Đang hợp thể Gogeta với " + partner.name + ".\nBạn có muốn tách hợp thể?",
                "Tách\nhợp thể", "Đóng");
    }

    // Trả lời hộp thoại Tách hợp thể (NpcFactory, Con mèo)
    public static void confirmTach(Player pl, int select) {
        if (select == 0) {
            separate(pl, pl.name + " tách hợp thể");
        }
    }

    // ===================== Mời / đồng ý =====================
    public static void request(Player a, Player b) {
        String loi = check(a, b);
        if (loi != null) {
            Service.gI().sendThongBao(a, loi);
            return;
        }
        long[] dangCho = requests.get(b.id);
        if (dangCho != null && System.currentTimeMillis() - dangCho[1] <= REQUEST_TIMEOUT_MS) {
            Service.gI().sendThongBao(a, b.name + " đang có lời mời hợp thể chưa trả lời, thử lại sau");
            return;
        }
        requests.put(b.id, new long[]{a.id, System.currentTimeMillis()});
        NpcService.gI().createMenuConMeo(b, MENU_XAC_NHAN_HOP_THE, -1,
                a.name + " muốn hợp thể Gogeta với bạn.\n"
                + a.name + " sẽ điều khiển, bạn đi theo và chỉ xem, không điều khiển được nhân vật.\n"
                + "Điểm săn boss khi hợp thể chia đôi cho 2 người.\n"
                + "Bấm vào " + a.name + " chọn \"Tách hợp thể\" để dừng bất cứ lúc nào.",
                "Đồng ý", "Từ chối");
        Service.gI().sendThongBao(a, "Đã gửi lời mời hợp thể tới " + b.name);
    }

    // B trả lời hộp thoại (NpcFactory, Con mèo)
    public static void confirm(Player b, int select) {
        long[] req = requests.remove(b.id);
        if (req == null || System.currentTimeMillis() - req[1] > REQUEST_TIMEOUT_MS) {
            Service.gI().sendThongBao(b, "Lời mời hợp thể đã hết hạn");
            return;
        }
        Player a = Client.gI().getPlayer(req[0]);
        if (select != 0) {
            if (a != null) {
                Service.gI().sendThongBao(a, b.name + " đã từ chối hợp thể");
            }
            return;
        }
        synchronized (PlayerFusionService.class) {
            String loi = check(a, b);
            if (loi != null) {
                Service.gI().sendThongBao(b, loi);
                if (a != null) {
                    Service.gI().sendThongBao(a, loi);
                }
                return;
            }
            start(a, b);
        }
    }

    private static void start(Player a, Player b) {
        a.fusionPartner = b;
        a.fusionLeader = true;
        b.fusionPartner = a;
        b.fusionLeader = false;
        leaders.add(a.id);
        // Đệ tử của người xem về nhà, người xem biến mất khỏi màn hình người khác
        if (b.pet != null && b.pet.status != nro.models.player.Pet.GOHOME && b.pet.status != nro.models.player.Pet.FUSION) {
            b.pet.goHome();
        }
        hideFromOthers(b);
        setPosSelf(b, a.location.x, a.location.y);
        fusionEffect(a, ConstPlayer.HOP_THE_PORATA);
        Service.gI().Send_Caitrang(a);
        Service.gI().point(a);
        Logger.fileLog("hop_the", a.name + " (" + a.id + ") hợp thể với " + b.name + " (" + b.id + ")");
        Service.gI().sendThongBao(a, "Hợp thể Gogeta thành công với " + b.name + "!");
        Service.gI().sendThongBao(b, "Đã hợp thể với " + a.name + ", bạn đang ở chế độ xem");
    }

    // ===================== Tách =====================
    public static void separate(Player pl, String lyDo) {
        separate(pl, lyDo, null);
    }

    // B được đi theo A bỏ qua điều kiện vào map: tách ra mà B chưa tới được map / khu này thì cho bay về nhà
    private static boolean canStayAfterSeparate(Player b, Player a) {
        if (b.zone == null || a.zone == null || !b.zone.equals(a.zone)) {
            return false; // chưa vào được cùng khu với người dẫn
        }
        int mapId = b.zone.map.mapId;
        if (nro.models.map.service.MapService.gI().isMapPhoBan(mapId)) {
            return b.clan != null && b.clan == a.clan; // phó bản của bang khác
        }
        return ChangeMapService.gI().checkMapCanJoin(b, b.zone) != null; // map chưa mở theo nhiệm vụ
    }

    private static void flyHome(Player pl) {
        Service.gI().sendThongBao(pl, "Bạn chưa đến được khu vực này, bay về nhà");
        ChangeMapService.gI().changeMapBySpaceShip(pl, 21 + pl.gender, -1, -1);
    }

    // leaving: người đang thoát game / mất kết nối (không dịch chuyển người này)
    private static void separate(Player pl, String lyDo, Player leaving) {
        if (pl == null || pl.fusionPartner == null) {
            return;
        }
        Player a, b;
        synchronized (PlayerFusionService.class) {
            if (pl.fusionPartner == null) {
                return;
            }
            a = pl.fusionLeader ? pl : pl.fusionPartner;
            b = pl.fusionLeader ? pl.fusionPartner : pl;
            a.fusionPartner = null;
            a.fusionLeader = false;
            b.fusionPartner = null;
            b.fusionLeader = false;
            leaders.remove(a.id);
            long now = System.currentTimeMillis();
            a.lastPlayerFusionEnd = now;
            b.lastPlayerFusionEnd = now;
        }
        if (a.zone != null) {
            fusionEffect(a, ConstPlayer.NON_FUSION);
            Service.gI().Send_Caitrang(a);
            Service.gI().point(a);
        }
        Logger.fileLog("hop_the", a.name + " tách hợp thể với " + b.name + ": " + lyDo);
        Service.gI().sendThongBao(a, "Đã tách hợp thể: " + lyDo);
        Service.gI().sendThongBao(b, "Đã tách hợp thể: " + lyDo);
        if (b != leaving && b.zone != null && Client.gI().getPlayer(b.id) != null) {
            if (canStayAfterSeparate(b, a)) {
                b.zone.load_Me_To_Another(b); // hiện lại người xem với mọi người trong khu
            } else {
                flyHome(b);
            }
        }
    }

    public static void onLogout(Player pl) {
        if (pl != null && pl.fusionPartner != null) {
            separate(pl, pl.name + " đã thoát game", pl);
        }
        if (pl != null) {
            requests.remove(pl.id);
        }
    }

    // ===================== Mỗi giây: kéo B theo A, kiểm tra điều kiện =====================
    public static void update() {
        for (Long id : leaders) {
            Player a = Client.gI().getPlayer(id);
            Player b = a != null ? a.fusionPartner : null;
            if (a == null || b == null || !a.fusionLeader) {
                leaders.remove(id);
                continue;
            }
            try {
                if (Client.gI().getPlayer(b.id) == null || !isConnected(b)) {
                    separate(a, b.name + " đã thoát game hoặc mất kết nối", b);
                } else if (!isConnected(a)) {
                    separate(a, a.name + " đã mất kết nối", a);
                } else if (a.isDie()) {
                    separate(a, a.name + " đã bị hạ gục");
                } else if (!dungCaiTrang(a, b)) {
                    separate(a, "cởi cải trang SSJ4");
                } else if (a.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                    separate(a, "hợp thể với đệ tử");
                } else {
                    follow(a, b);
                }
            } catch (Exception e) {
                Logger.logException(PlayerFusionService.class, e);
            }
        }
    }

    private static boolean isConnected(Player pl) {
        return pl.getSession() != null && pl.getSession().isConnected();
    }

    private static void follow(Player a, Player b) {
        if (a.zone == null) {
            return;
        }
        if (b.isDie()) {
            b.nPoint.setFullHpMp();
            PlayerService.gI().hoiSinh(b);
        }
        // Đệ tử của người xem không được ra ngoài (vô hình với người khác mà vẫn đánh quái / boss)
        if (b.pet != null && b.pet.status != nro.models.player.Pet.GOHOME && !b.pet.isGoingHome()) {
            b.pet.goHome();
        }
        if (b.zone == null || !b.zone.equals(a.zone)) {
            ChangeMapService.gI().changeMap(b, a.zone, a.location.x, a.location.y);
        } else if (Util.getDistance(a, b) > FOLLOW_DISTANCE) {
            setPosSelf(b, a.location.x, a.location.y);
        }
    }
}
