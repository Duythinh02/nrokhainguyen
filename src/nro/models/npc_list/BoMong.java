package nro.models.npc_list;

import java.time.LocalDate;
import java.time.LocalDateTime;
import nro.models.clan.ClanMember;
import nro.models.consts.ConstNpc;
import nro.models.server.Client;
import nro.models.services.ClanService;
import nro.models.consts.ConstTask;
import nro.models.item.Item;
import nro.models.services.AchievementService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.services_func.Input;

/**
 *
 * @author By Mr Blue
 *
 */
public class BoMong extends Npc {

    // Quà điểm danh mỗi ngày
    private static final int DIEM_DANH_DIEM_BOSS = 50;
    private static final int DIEM_DANH_THOI_VANG = 200;
    private static final int DIEM_DANH_CAPSULE_BANG = 20;

    // Cộng Capsule Bang cho bang của người chơi (giống điểm danh bang ở Giữ Ma Đậu Bò), false nếu chưa có bang
    private static boolean addCapsuleBang(Player player, int soLuong) {
        if (player.clan == null) {
            return false;
        }
        player.clan.capsuleClan += soLuong;
        for (ClanMember cm : player.clan.getMembers()) {
            if (cm.id == player.id) {
                cm.memberPoint += soLuong;
                cm.clanPoint += soLuong;
                break;
            }
        }
        for (ClanMember cm : player.clan.getMembers()) {
            Player pl = Client.gI().getPlayer(cm.id);
            if (pl != null) {
                ClanService.gI().sendMyClan(pl);
            }
        }
        return true;
    }

    public BoMong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            if (canOpenNpc(player)) {
                if (this.mapId == 47 || this.mapId == 84) {
                    if (Input.NAP_TIEN_ENABLED) {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Ngươi muốn có thêm ngọc thì chịu khó làm vài nhiệm vụ sẽ được ngọc thưởng", "Nhiệm vụ\nhàng ngày", "Nhiệm vụ\nthành tích", "Nạp Ngọc", "Điểm danh", "Từ chối");
                    } else {
                        // Đã đóng nạp tiền: ẩn nút "Nạp Ngọc"
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Ngươi muốn có thêm ngọc thì chịu khó làm vài nhiệm vụ sẽ được ngọc thưởng", "Nhiệm vụ\nhàng ngày", "Nhiệm vụ\nthành tích", "Điểm danh", "Từ chối");
                    }
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 47 || this.mapId == 84) {
                if (player.idMark.isBaseMenu()) {
                    // Menu ẩn nút "Nạp Ngọc" (vị trí 2) thì các nút sau lùi 1: đưa về đúng chỉ số cũ
                    if (!Input.NAP_TIEN_ENABLED && select >= 2) {
                        select++;
                    }
                    switch (select) {
                        case 0 -> {
                            if (player.playerTask.sideTask.template != null) {
                                String npcSay = "Nhiệm vụ hiện tại: " + player.playerTask.sideTask.getName() + " ("
                                        + player.playerTask.sideTask.getLevel() + ")"
                                        + "\nHiện tại đã hoàn thành: " + player.playerTask.sideTask.count + "/"
                                        + player.playerTask.sideTask.maxCount + " ("
                                        + player.playerTask.sideTask.getPercentProcess() + "%)\nSố nhiệm vụ còn lại trong ngày: "
                                        + player.playerTask.sideTask.leftTask + "/" + ConstTask.MAX_SIDE_TASK;
                                this.createOtherMenu(player, ConstNpc.MENU_OPTION_PAY_SIDE_TASK,
                                        npcSay, "Trả nhiệm\nvụ", "Hủy nhiệm\nvụ");
                            } else {
                                this.createOtherMenu(player, ConstNpc.MENU_OPTION_LEVEL_SIDE_TASK,
                                        "Tôi có vài nhiệm vụ theo cấp bậc, "
                                        + "sức cậu có thể làm được cái nào?",
                                        "Dễ", "Bình thường", "Khó", "Từ chối");
                            }
                        }
                        case 1 -> {
                            AchievementService.gI().openAchievementUI(player);
                        }
                        case 2 -> {
                            Input.gI().createFormTradeGem(player);
                        }
                        case 3 -> {
                            if (player.lastCheckIn != null) {
                                LocalDate last = player.lastCheckIn.toLocalDate();
                                LocalDate today = LocalDate.now();
                                if (last.isEqual(today)) {
                                    Service.gI().sendThongBao(player, "Bạn đã điểm danh hôm nay rồi!");
                                    return;
                                }
                            }
                            if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                                Service.gI().sendThongBao(player, "Hành trang cần ít nhất 1 ô trống để điểm danh");
                                return;
                            }
                            player.lastCheckIn = LocalDateTime.now();
                            player.event.addEventPoint(DIEM_DANH_DIEM_BOSS);
                            Item item457 = ItemService.gI().createNewItem((short) 457);
                            item457.quantity = DIEM_DANH_THOI_VANG;
                            item457.itemOptions.add(new Item.ItemOption(30, 0));
                            InventoryService.gI().addItemBag(player, item457);
                            boolean coBang = addCapsuleBang(player, DIEM_DANH_CAPSULE_BANG);
                            InventoryService.gI().sendItemBags(player);

                            Service.gI().sendThongBaoOK(player, "Điểm danh thành công! Nhận được:\n"
                                    + DIEM_DANH_DIEM_BOSS + " điểm săn boss\n"
                                    + DIEM_DANH_THOI_VANG + " thỏi vàng\n"
                                    + (coBang ? DIEM_DANH_CAPSULE_BANG + " Capsule Bang"
                                            : "(Chưa có bang hội nên không nhận được " + DIEM_DANH_CAPSULE_BANG + " Capsule Bang)"));
                        }

                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPTION_LEVEL_SIDE_TASK) {
                    switch (select) {
                        case 0, 1, 2 ->
                            TaskService.gI().changeSideTask(player, (byte) select);
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPTION_PAY_SIDE_TASK) {
                    switch (select) {
                        case 0 ->
                            TaskService.gI().paySideTask(player);
                        case 1 ->
                            TaskService.gI().removeSideTask(player);
                    }
                }
            }
        }
    }
}
