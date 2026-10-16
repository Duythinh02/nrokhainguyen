package nro.models.combine;

import nro.models.services.InventoryService;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.utils.FormatStyle;
import nro.models.utils.Util;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.services.ItemService;
import nro.models.combine.CombineService;

public class TinhThachHoa {
 private static int getDaNangcapTinhThach(int star) {
        switch (star) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
            case 4:
                return 5;
            case 5:
                return 6;
            case 6:
                return 7;
            case 7:
                return 8;
        }
        return 0;
    }

    private static float getTiLeNangcapTinhThach(int star) {
        switch (star) {
            case 0:
                return 90f;
            case 1:
                return 50f;
            case 2:
                return 30f;
            case 3:
                return 25f;
            case 4:
                return 15f;
            case 5:
                return 8f;
            case 6:
                return 5f;
            case 7:
                return 2f;
        }
        return 0;
    }
    public static void showInfoCombine(Player player) {
    if (player.combineNew.itemsCombine.size() == 2) {
                    Item caiTrang = null;
                    Item manhVo = null;
                    int star = 0;
                    for (Item item : player.combineNew.itemsCombine) {
                       if (item.template.type == 21 || item.template.type == 72 || item.template.type == 11) {
                            caiTrang = item;
                        } else if (item.template.type == 93 && item.template.id >= 2452 && item.template.id <= 2458) {
                            manhVo = item;
                        }
                    }
                    if (caiTrang != null) {
                        for (ItemOption io : caiTrang.itemOptions) {
                            if (io.optionTemplate.id == 114||io.optionTemplate.id == 80||io.optionTemplate.id == 81||io.optionTemplate.id == 153||io.optionTemplate.id == 155||io.optionTemplate.id == 156||io.optionTemplate.id == 162) {
                                if (io.param >= CombineService.MAX_LEVEL_ITEM) {
                                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                            "Vật phẩm đã nâng tinh thạch cấp tối đa", "Đóng");
                                    return;
                                }
                                star = io.param;
                                break;
                            }
                        }

                    }
                    player.combineNew.DaNangcap = getDaNangcapTinhThach(star);
                    player.combineNew.TileNangcap = getTiLeNangcapTinhThach(star);
                    if (caiTrang != null && manhVo != null) {
                        String npcSay = caiTrang.template.name + "\n|2|";
                        for (ItemOption io : caiTrang.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|7|Tỉ lệ thành công: " + getTiLeNangcapTinhThach(star) + "%" + "\n";
                        npcSay += "|1|Cần " + Util.formatNumber(getDaNangcapTinhThach(star), FormatStyle.VIETNAMESE) + " Tinh Thạch ";
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Nâng cấp");

                    } else {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 Pet, Linh Thú hoặc Vật phẩm đeo lưng và loại đá tinh thạch", "Đóng");
                    }
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 Pet, Linh Thú hoặc Vật phẩm đeo lưng và loại đá tinh thạch", "Đóng");
                } }

    public static void startCombine(Player player) {
      float tiLe = player.combineNew.TileNangcap;

        if (player.combineNew.itemsCombine.size() == 2) {
            long gold = player.combineNew.goldCombine;
            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            }
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gem < gem) {
                Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }

            Item caiTrang = null;
            Item manhCaiTrang = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.template.type == 21 || item.template.type == 72 || item.template.type == 11) {
                    caiTrang = item;
                } else if (item.template.type == 93 && item.template.id >= 2452 && item.template.id <= 2458) {
                    manhCaiTrang = item;
                }
            }

            int star = 0;
            if (caiTrang != null) {
                for (ItemOption io : caiTrang.itemOptions) {
                    if (io.optionTemplate.id == 114||io.optionTemplate.id == 80||io.optionTemplate.id == 81||io.optionTemplate.id == 153||io.optionTemplate.id == 155||io.optionTemplate.id == 156||io.optionTemplate.id == 162) {
                        star = Math.max(star, io.param);
                    }
                }
            }
            if (star >= 10) {
                Service.gI().sendThongBao(player, "Đã max cấp");
                return;
            }
            if (caiTrang != null && manhCaiTrang != null && manhCaiTrang.quantity > player.combineNew.DaNangcap) {
                player.inventory.gold -= gold;
                player.inventory.gem -= gem;
                InventoryService.gI().subQuantityItemsBag(player, manhCaiTrang, player.combineNew.DaNangcap);
                if (Util.isTrue(tiLe, 100)) {
                    Item newCaiTrang = ItemService.gI().createNewItem((short) (caiTrang.template.id));

                    // Copy các chỉ số từ bông tai cũ sang bông tai mới
                    for (ItemOption io : caiTrang.itemOptions) {
                        newCaiTrang.itemOptions.add(new ItemOption(io.optionTemplate.id, io.param));
                    }

                    // Khai báo randomOptionId ở ngoài
                    int randomOptionId = -1;

                    // Xác định giá trị randomOptionId dựa trên manhCaiTrang.template.id
                    switch (manhCaiTrang.template.id) {
                        case 2452:
                            randomOptionId = 114;
                            break;
                        case 2453:
                            randomOptionId = 80;
                            break;
                        case 2454:
                            randomOptionId = 81;
                            break;
                        case 2455:
                            randomOptionId = 153;
                            break;
                        case 2456:
                            randomOptionId = 155;
                            break;
                        case 2457:
                            randomOptionId = 156;
                            break;
                        case 2458:
                            randomOptionId = 162;
                            break;
                        default:
                            break;
                    }

                    // Kiểm tra nếu newCaiTrang đã có loại tinh thạch khác
                    for (ItemOption io : newCaiTrang.itemOptions) {
                        if (io.optionTemplate.id != randomOptionId && (io.optionTemplate.id == 114||io.optionTemplate.id == 80||io.optionTemplate.id == 81||io.optionTemplate.id == 153||io.optionTemplate.id == 155||io.optionTemplate.id == 156||io.optionTemplate.id == 162)) {
                            Service.gI().sendThongBao(player, "Đã nâng một loại tinh thạch khác rồi, không thể nâng tiếp");
                            return;
                        }
                    }

                    // Nếu không có loại tinh thạch khác, tiếp tục thêm hoặc nâng cấp
                    boolean hasOption = false;
                    for (ItemOption io : newCaiTrang.itemOptions) {
                        if (io.optionTemplate.id == randomOptionId) {
                            io.param = Math.min(io.param + 1, CombineService.MAX_LEVEL_ITEM);
                            hasOption = true;
                            break;
                        }
                    }

                    if (!hasOption) {
                        newCaiTrang.itemOptions.add(new ItemOption(randomOptionId, 1));
                    }

                    CombineService.gI().sendEffectSuccessCombine(player);
                    InventoryService.gI().subQuantityItemsBag(player, caiTrang, 1);
                    InventoryService.gI().addItemBag(player, newCaiTrang);

                } else {
                    CombineService.gI().sendEffectFailCombine(player);
                }
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendMoney(player);
                CombineService.gI().reOpenItemCombine(player);
            } else {
                Service.gI().sendThongBao(player, "Thiếu vật phẩm cần để nâng");
            }
        }
    }
    
}
