package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import java.io.IOException;
import nro.models.player.Player;
import nro.models.network.Message;
import nro.models.npc.Npc;
import nro.models.map.service.NpcManager;
import nro.models.combine.PhanRaTrangBiKichHoat.PhanRaTrangBi;
import nro.models.services.InventoryService;

/**
 *
 * @author By Mr Blue
 */

public class CombineService {
    
    public static final byte MAX_STAR_ITEM = 8; // đồ tối đa 8 sao pha lê
    public static final byte MAX_LEVEL_ITEM = 8;
    public static final int DANG_SINH_LE = 8999;
    
    private static final byte OPEN_TAB_COMBINE = 0;
    private static final byte REOPEN_TAB_COMBINE = 1;
    private static final byte COMBINE_SUCCESS = 2;
    private static final byte COMBINE_FAIL = 3;
    private static final byte COMBINE_DRAGON_BALL = 5;
    public static final byte OPEN_ITEM = 6;
    
    public static final int LAM_PHEP_NHAP_DA = 512;
    public static final int EP_SAO_TRANG_BI = 500;
    public static final int PHA_LE_HOA_TRANG_BI = 501;
    public static final int CHUYEN_HOA_TRANG_BI_VANG = 994;
    public static final int CHUYEN_HOA_TRANG_BI_NGOC = 995;
    public static final int PHAN_RA_TRANG_BI_KH = 996;
    public static final int TAI_TAO_CAPSULE_KH = 997;
    public static final int NANG_CAP_SAO_PHA_LE = 100;
    public static final int DANH_BONG_SAO_PHA_LE = 101;
    public static final int CUONG_HOA_LO_SAO_PHA_LE = 102;
    public static final int TAO_DA_HEMATITE = 103;
    
    public static final int GIAM_DINH_SACH = 104;
    public static final int TAY_SACH = 105;
    public static final int NANG_CAP_SACH_TUYET_KY = 106;
    public static final int HOI_PHUC_SACH = 107;
    public static final int PHAN_RA_SACH = 108;
    public static final int DUI_DUC = 109;
    public static final int DA_MAI = 110;
    public static final int NANG_CAP_VAT_PHAM = 510;
    public static final int NANG_CAP_BONG_TAI = 511;
    public static final int NHAP_NGOC_RONG = 513;
    public static final int PHAN_RA_DO_THAN_LINH = 514;
    public static final int CHE_TAO_TRANG_BI_THIEN_SU = 515;
    public static final int NANG_CHI_SO_BONG_TAI = 517;
    public static final int NANG_CAP_BONG_TAI3 = 455;
    public static final int NANG_CHI_SO_BONG_TAI3 = 457;
    public static final int NANG_CAP_KICH_HOAT = 518;
    public static final int NANG_CAP_KICH_HOAT_VIP = 519;
    public static final int DAP_DO_AO_HOA = 520;
    public static final int PS_HOA_TRANG_BI = 521;
    public static final int TAY_PS_HOA_TRANG_BI = 522;
    public static final int MO_KHOA_ITEM = 523;
    public static final int NANG_CAP_CHAN_MENH = 524;
    public static final int AN_TRANG_BI = 525;
    public static final int GIA_HAN_VAT_PHAM = 526;
    public static final int SIEU_HOA = 527;
    public static final int TINH_THACH_HOA = 528;
    public static final int NANG_GIAP_LUYEN_TAP = 529;
    public static final int NANG_CAP_DO_HD = 601;
    public static final int DOI_DO_THAN_LINH = 602;
    public static final int PHAN_RA_DO_HUY_DIET = 603;
    private static CombineService instance;
    
    public final Npc baHatMit;
    public final Npc whis;
    
    private CombineService() {
        this.baHatMit = NpcManager.getNpc(ConstNpc.BA_HAT_MIT);
        this.whis = NpcManager.getNpc(ConstNpc.WHIS);
    }
    
    public static CombineService gI() {
        if (instance == null) {
            instance = new CombineService();
        }
        return instance;
    }

    /**
     * Hiển thị thông tin đập đồ
     *
     * @param player
     * @param index
     */
    // Đã đóng: chỉ dùng set kích hoạt cấp thấp nhất của 3 hành tinh (không nâng kích hoạt / VIP / hủy diệt)
    // Đóng luôn chế tạo đồ thiên sứ (thường + kích hoạt)
    public static boolean isClosedCombine(int type) {
        return type == NANG_CAP_KICH_HOAT || type == NANG_CAP_KICH_HOAT_VIP || type == NANG_CAP_DO_HD
                || type == CHE_TAO_TRANG_BI_THIEN_SU;
    }

    public static String closedCombineMessage(int type) {
        return type == CHE_TAO_TRANG_BI_THIEN_SU
                ? "Chức năng chế tạo đồ thiên sứ đã đóng"
                : "Chức năng nâng cấp set kích hoạt đã đóng";
    }

    public void showInfoCombine(Player player, int[] index) {
        if (player.combineNew == null) {
            return;
        }
        if (isClosedCombine(player.combineNew.typeCombine)) {
            nro.models.services.Service.gI().sendThongBao(player, closedCombineMessage(player.combineNew.typeCombine));
            return;
        }
        player.combineNew.clearItemCombine();
        if (index.length > 0) {
            for (int i = 0; i < index.length; i++) {
                player.combineNew.itemsCombine.add(player.inventory.itemsBag.get(index[i]));
            }
        }
        switch (player.combineNew.typeCombine) {
            case EP_SAO_TRANG_BI:
                EpSaoTrangBi.showInfoCombine(player);
                break;
            case PHA_LE_HOA_TRANG_BI:
                PhaLeHoaTrangBi.showInfoCombine(player);
                break;
            case CHUYEN_HOA_TRANG_BI_VANG:
                ChuyenHoaTrangBi_Vang.chuyenHoaTrangBiVang(player);
                break;
            case CHUYEN_HOA_TRANG_BI_NGOC:
                ChuyenHoaTrangBi_Ngoc.chuyenHoaTrangBiNgoc(player);
                break;
            case PHAN_RA_TRANG_BI_KH:
                PhanRaTrangBi.showInfoCombine(player);
                break;
            case TAI_TAO_CAPSULE_KH:
                TaiTaoCapsuleKichHoat.showInfoCombine(player);
                break;
            case NHAP_NGOC_RONG:
                NhapNgocRong.showInfoCombine(player);
                break;
            case NANG_CAP_VAT_PHAM:
                NangCapVatPham.showInfoCombine(player);
                break;
            case NANG_CAP_BONG_TAI:
                NangCapBongTai.showInfoCombine(player);
                break;
            case NANG_CHI_SO_BONG_TAI:
                NangChiSoBongTai.showInfoCombine(player);
                break;
            case NANG_CAP_BONG_TAI3:
                NangCapBongTai3.showInfoCombine(player);
                break;
            case NANG_CHI_SO_BONG_TAI3:
                NangChiSoBongTai3.showInfoCombine(player);
                break;    
            case NANG_CAP_SAO_PHA_LE:
                NangCapSaoPhaLe.showInfoCombine(player);
                break;
            case DANH_BONG_SAO_PHA_LE:
                DanhBongSaoPhaLe.showInfoCombine(player);
                break;
            case CUONG_HOA_LO_SAO_PHA_LE:
                CuongHoaLoSaoPhaLe.showInfoCombine(player);
                break;
            case TAO_DA_HEMATITE:
                TaoDaHematite.showInfoCombine(player);
                break;
            case GIAM_DINH_SACH:
                GiamDinhSach.showInfoCombine(player);
                break;
            case TAY_SACH:
                TaySach.showInfoCombine(player);
                break;
            case NANG_CAP_SACH_TUYET_KY:
                NangCapSachTuyetKy.showInfoCombine(player);
                break;
            case HOI_PHUC_SACH:
                HoiPhucSach.showInfoCombine(player);
                break;
            case PHAN_RA_SACH:
                PhanRaSach.showInfoCombine(player);
                break;
            case CHE_TAO_TRANG_BI_THIEN_SU:
                CheTaoTrangBiThienSu.showInfoCombine(player);
                break;
            case DUI_DUC:
                CheTaoDuiDuc.showInfoCombine(player);
                break;
            case DA_MAI:
                TaoDaMai.showInfoCombine(player);
                break;
            case NANG_CAP_KICH_HOAT:
                NangCapKichHoat.showInfoCombine(player);
                break;
            case NANG_CAP_KICH_HOAT_VIP:
                NangCapKichHoatVip.showInfoCombine(player);
                break;
            case DAP_DO_AO_HOA:
                DapDoAoHoa.showInfoCombine(player);
                break;
            case PS_HOA_TRANG_BI:
                PhapSuHoa.showInfoCombine(player);
                break;
            case TAY_PS_HOA_TRANG_BI:
                RemoveOptionItem.showInfoCombine(player);
                break;
            case MO_KHOA_ITEM:
                MoKhoaItem.showInfoCombine(player);
                break;
            case NANG_CAP_CHAN_MENH:
                NangCapChanMenh.showInfoCombine(player);
                break;
            case AN_TRANG_BI:
                TinhAnTrangBi.showInfoCombine(player);
                break;
            case GIA_HAN_VAT_PHAM:
                GiaHanVatPham.showInfoCombine(player);
                break;
            case SIEU_HOA:
                SieuHoaCaiTrang.showInfoCombine(player);
                break;
            case TINH_THACH_HOA:
                TinhThachHoa.showInfoCombine(player);
                break;
            case NANG_GIAP_LUYEN_TAP:
                NangGiapLuyenTap.showInfoCombine(player);
                break;
            case NANG_CAP_DO_HD:
                NangCapDoHuyDiet.showInfoCombine(player);
                break;
            case DOI_DO_THAN_LINH:
                DoiDoThanLinh.showInfoCombine(player);
                break;
            case PHAN_RA_DO_HUY_DIET:
                PhanRaDoHuyDiet.showInfoCombine(player);
                break;
        }
    }

    /**
     * Bắt đầu đập đồ - điều hướng từng loại đập đồ
     *
     * @param player
     */
    public void startCombine(Player player) {
        if (isClosedCombine(player.combineNew.typeCombine)) {
            nro.models.services.Service.gI().sendThongBao(player, closedCombineMessage(player.combineNew.typeCombine));
            return;
        }
        switch (player.combineNew.typeCombine) {
            case EP_SAO_TRANG_BI:
                EpSaoTrangBi.epSaoTrangBi(player);
                break;
            case PHA_LE_HOA_TRANG_BI:
                PhaLeHoaTrangBi.phaLeHoa(player);
                break;
            case CHUYEN_HOA_TRANG_BI_VANG:
                ChuyenHoaTrangBi_Vang.thucHienChuyenHoa(player);
                break;
            case CHUYEN_HOA_TRANG_BI_NGOC:
                ChuyenHoaTrangBi_Ngoc.thucHienChuyenHoa(player);
                break;
            case PHAN_RA_TRANG_BI_KH:
                PhanRaTrangBi.ThucHienPhanRa(player);
                break;
            case TAI_TAO_CAPSULE_KH:
                TaiTaoCapsuleKichHoat.thucHienTaiTao(player);
                break;
            case NHAP_NGOC_RONG:
                NhapNgocRong.nhapNgocRong(player);
                break;
            case NANG_CAP_VAT_PHAM:
                NangCapVatPham.nangCapVatPham(player);
                break;
            case NANG_CAP_BONG_TAI:
                NangCapBongTai.nangCapBongTai(player);
                break;
            case NANG_CHI_SO_BONG_TAI:
                NangChiSoBongTai.nangChiSoBongTai(player);
                break;
            case NANG_CAP_BONG_TAI3:
                NangCapBongTai3.nangCapBongTai(player);
                break;
            case NANG_CHI_SO_BONG_TAI3:
                NangChiSoBongTai3.nangChiSoBongTai(player);
                break;
            case NANG_CAP_SAO_PHA_LE:
                NangCapSaoPhaLe.nangCapSaoPhaLe(player);
                break;
            case DANH_BONG_SAO_PHA_LE:
                DanhBongSaoPhaLe.danhBongSaoPhaLe(player);
                break;
            case CUONG_HOA_LO_SAO_PHA_LE:
                CuongHoaLoSaoPhaLe.cuongHoaLoSaoPhaLe(player);
                break;
            case TAO_DA_HEMATITE:
                TaoDaHematite.taoDaHematite(player);
                break;
            case GIAM_DINH_SACH:
                GiamDinhSach.giamDinhSach(player);
                break;
            case TAY_SACH:
                TaySach.taySach(player);
                break;
            case NANG_CAP_SACH_TUYET_KY:
                NangCapSachTuyetKy.nangCapSachTuyetKy(player);
                break;
            case HOI_PHUC_SACH:
                HoiPhucSach.hoiPhucSach(player);
                break;
            case PHAN_RA_SACH:
                PhanRaSach.phanRaSach(player);
                break;
            case CHE_TAO_TRANG_BI_THIEN_SU:
                CheTaoTrangBiThienSu.CheTaoTS(player);
                break;
            case DUI_DUC:
                CheTaoDuiDuc.CheTaoDuiDuc(player);
                break;
            case DA_MAI:
                TaoDaMai.CheTaoDuiDuc(player);
                break;
            case NANG_CAP_KICH_HOAT:
                NangCapKichHoat.startCombine(player);
                break;
            case NANG_CAP_KICH_HOAT_VIP:
                NangCapKichHoatVip.startCombine(player);
                break;
            case DAP_DO_AO_HOA:
                DapDoAoHoa.startCombine(player);
                break;
            case PS_HOA_TRANG_BI:
                PhapSuHoa.startCombine(player);
                break;
            case TAY_PS_HOA_TRANG_BI:
                RemoveOptionItem.startCombine(player);
                break;
            case MO_KHOA_ITEM:
                MoKhoaItem.startCombine(player);
                break;
            case NANG_CAP_CHAN_MENH:
                NangCapChanMenh.nangCapChanMenh(player);
                break;
            case AN_TRANG_BI:
                TinhAnTrangBi.startCombine(player);
                break;
            case GIA_HAN_VAT_PHAM:
                GiaHanVatPham.startCombine(player);
                break;
            case SIEU_HOA:
                SieuHoaCaiTrang.startCombine(player);
                break;
            case TINH_THACH_HOA:
                TinhThachHoa.startCombine(player);
                break;
            case NANG_GIAP_LUYEN_TAP:
                NangGiapLuyenTap.startCombine(player);
                break;
            case NANG_CAP_DO_HD:
                NangCapDoHuyDiet.startCombine(player);
                break;
            case DOI_DO_THAN_LINH:
                DoiDoThanLinh.startCombine(player);
                break;
            case PHAN_RA_DO_HUY_DIET:
                PhanRaDoHuyDiet.startCombine(player);
                break;
        }
        
        player.idMark.setIndexMenu(ConstNpc.IGNORE_MENU);
        player.combineNew.clearParamCombine();
        player.combineNew.lastTimeCombine = System.currentTimeMillis();
        
    }
    
    public void startCombineVip(Player player, int n) {
        if (isClosedCombine(player.combineNew.typeCombine)) {
            nro.models.services.Service.gI().sendThongBao(player, closedCombineMessage(player.combineNew.typeCombine));
            return;
        }
        switch (player.combineNew.typeCombine) {
            case PHA_LE_HOA_TRANG_BI:
                PhaLeHoaTrangBi.phaLeHoa(player, n);
                break;
        }
        
        player.idMark.setIndexMenu(ConstNpc.IGNORE_MENU);
        player.combineNew.clearParamCombine();
        player.combineNew.lastTimeCombine = System.currentTimeMillis();
        
    }

    /**
     * Mở tab đập đồ
     *
     * @param player
     * @param type kiểu đập đồ
     */
    public void openTabCombine(Player player, int type) {
        if (isClosedCombine(type)) {
            nro.models.services.Service.gI().sendThongBao(player, closedCombineMessage(type));
            return;
        }
        player.combineNew.setTypeCombine(type);
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_TAB_COMBINE);
            msg.writer().writeUTF(getTextInfoTabCombine(type));
            msg.writer().writeUTF(getTextTopTabCombine(type));
            if (player.idMark.getNpcChose() != null) {
                msg.writer().writeShort(player.idMark.getNpcChose().tempId);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng mở item
     *
     * @param player
     * @param icon1
     * @param icon2
     */
    public void sendAddItemCombine(Player player, int npcId, Item... items) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("null");
            msg.writer().writeUTF("null");
            msg.writer().writeShort(npcId);
            player.sendMessage(msg);
            msg.cleanup();
            msg = new Message(-81);
            msg.writer().writeByte(1);
            msg.writer().writeByte(items.length);
            for (Item item : items) {
                msg.writer().writeByte(InventoryService.gI().getIndexItemBag(player, item));
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }
    
    public void sendEffectOpenItem(Player player, short icon1, short icon2) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_ITEM);
            msg.writer().writeShort(icon1);
            msg.writer().writeShort(icon2);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    public void sendEffectCombineItem(Player player, byte type, short icon1, short icon2) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(type);
            switch (type) {
                case 0:
                    msg.writer().writeUTF("");
                    msg.writer().writeUTF("");
                    break;
                case 1:
                    msg.writer().writeByte(0);
                    msg.writer().writeByte(-1);
                    break;
                case 2: // success 0 eff 0
                case 3: // success 1 eff 0
                    break;
                case 4: // success 0 eff 1
                    msg.writer().writeShort(icon1);
                    break;
                case 5: // success 0 eff 2
                    msg.writer().writeShort(icon1);
                    break;
                case 6: // success 0 eff 3
                    msg.writer().writeShort(icon1);
                    msg.writer().writeShort(icon2);
                    break;
                case 7: // success 0 eff 4
                    msg.writer().writeShort(icon1);
                    break;
                case 8: // success 1 eff 4
                    // Lam do ts
                    break;
            }
            msg.writer().writeShort(-1); // id npc
            msg.writer().writeShort(-1); // x
            msg.writer().writeShort(-1); // y
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    private int getTempIdItemC0(int gender, int type) {
        if (type == 4) {
            return 12;
        }
        switch (gender) {
            case 0:
                switch (type) {
                    case 0:
                        return 0;
                    case 1:
                        return 6;
                    case 2:
                        return 21;
                    case 3:
                        return 27;
                }
                break;
            case 1:
                switch (type) {
                    case 0:
                        return 1;
                    case 1:
                        return 7;
                    case 2:
                        return 22;
                    case 3:
                        return 28;
                }
                break;
            case 2:
                switch (type) {
                    case 0:
                        return 2;
                    case 1:
                        return 8;
                    case 2:
                        return 23;
                    case 3:
                        return 29;
                }
                break;
        }
        return -1;
    }
    //Trả về tên đồ c0

    private String getNameItemC0(int gender, int type) {
        if (type == 4) {
            return "Rada cấp 1";
        }
        switch (gender) {
            case 0:
                switch (type) {
                    case 0:
                        return "Áo vải 3 lỗ";
                    case 1:
                        return "Quần vải đen";
                    case 2:
                        return "Găng thun đen";
                    case 3:
                        return "Giầy nhựa";
                }
                break;
            case 1:
                switch (type) {
                    case 0:
                        return "Áo sợi len";
                    case 1:
                        return "Quần sợi len";
                    case 2:
                        return "Găng sợi len";
                    case 3:
                        return "Giầy sợi len";
                }
                break;
            case 2:
                switch (type) {
                    case 0:
                        return "Áo vải thô";
                    case 1:
                        return "Quần vải thô";
                    case 2:
                        return "Găng vải thô";
                    case 3:
                        return "Giầy vải thô";
                }
                break;
        }
        return "";
    }

    /**
     * Hiệu ứng đập đồ thành công
     *
     * @param player
     */
    public boolean CheckSlot(Item trangBi, int starEmpty) {
        if (starEmpty < 8) {
            return true;
        }
        for (ItemOption io : trangBi.itemOptions) {
            if (starEmpty == 8 && io.optionTemplate.id == 228) {
                return io.param >= 8;
            } else if (starEmpty == 9 && io.optionTemplate.id == 228) {
                return io.param >= 9;
            }
        }
        
        return false;
        
    }
    
    public void sendEffSuccessVip(Player player, int iconID) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(7);
            msg.writer().writeShort(iconID);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }
    
    public void sendEffectSuccessCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_SUCCESS);
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng đập đồ thất bại
     *
     * @param player
     */
    public void sendEffFailVip(Player player) {
        try {
            Message msg;
            msg = new Message(-81);
            msg.writer().writeByte(8);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }
    
    public void sendEffectFailCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_FAIL);
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Gửi lại danh sách đồ trong tab combine
     *
     * @param player
     */
    public void reOpenItemCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(REOPEN_TAB_COMBINE);
            msg.writer().writeByte(player.combineNew.itemsCombine.size());
            for (Item it : player.combineNew.itemsCombine) {
                for (int j = 0; j < player.inventory.itemsBag.size(); j++) {
                    if (it == player.inventory.itemsBag.get(j)) {
                        msg.writer().writeByte(j);
                    }
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng ghép ngọc rồng
     *
     * @param player
     * @param icon
     */
    public void sendEffectCombineDB(Player player, short icon) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_DRAGON_BALL);
            msg.writer().writeShort(icon);
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    
    private String getTextTopTabCombine(int type) {
        switch (type) {
            case NANG_CAP_KICH_HOAT:
                return "Ta sẽ phù phép\nchế tạo trang bị Huỷ Diệt\nthành trang bị Kích Hoạt";
            case NANG_CAP_KICH_HOAT_VIP:
                return "Ta sẽ phù phép\ntrang bị Hủy Diệt\nthành trang bị Kích Hoạt VIP";
            case DAP_DO_AO_HOA:
                return "Ta sẽ giúp ngươi ảo hóa trang bị\nđể tăng thuộc tính cao hơn";
            case PS_HOA_TRANG_BI:
                return "Pháp sư hóa Pet ,Đeo Lưng\nVán bay";
            case TAY_PS_HOA_TRANG_BI:
                return "Tẩy đồ";
            case MO_KHOA_ITEM:
                return "Mở khóa giao dịch vật phẩm";
            case NANG_CAP_CHAN_MENH:
                return "Ta sẽ phù phép\ncho chân thiên tử\nthành 1 chân thiên tử cấp cao";
            case AN_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở thành trang bị Ấn";
            case GIA_HAN_VAT_PHAM:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\nthêm hạn sử dụng";
            case SIEU_HOA:
                return "Ta sẽ giúp con siêu hóa\nCải trang";
            case TINH_THACH_HOA:
                return "Ta sẽ giúp con Tinh Thạch hóa vật phẩm";
            case NANG_GIAP_LUYEN_TAP:
                return "Ta sẽ giúp con nâng cấp\nGiáp luyện tập";
            case NANG_CAP_DO_HD:
                return "Ta sẽ phù phép\nnâng cấp đồ Thần Linh\nthành đồ Hủy Diệt";
            case DOI_DO_THAN_LINH:
                return "Ta sẽ đổi đồ Thần Linh\ncủa ngươi sang món khác\ncùng hành tinh";
            case PHAN_RA_DO_HUY_DIET:
                return "Ta sẽ phân rã\nđồ Huỷ Diệt của ngươi\nthành thức ăn";
            case EP_SAO_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở lên mạnh mẽ";
            case PHA_LE_HOA_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở thành trang bị pha lê";
            case CHUYEN_HOA_TRANG_BI_NGOC:
            case CHUYEN_HOA_TRANG_BI_VANG:
                return "Lưu ý trang bị mới\nphải hơn trang bị gốc\n1 bậc";
            case PHAN_RA_TRANG_BI_KH:
                return "Ta sẽ phù phép\nphân rã thành\nkhoáng tái chế cho ngươi";
            case TAI_TAO_CAPSULE_KH:
                return "Ta sẽ phù phép\ntái tạo thành 1 viên\nCapsule kích hoạt tự chọn";
            case NHAP_NGOC_RONG:
                return "Ta sẽ phù phép\ncho 7 viên Ngọc Rồng\nthành 1 viên Ngọc Rồng cấp cao";
            case NANG_CAP_VAT_PHAM:
                return "Ta sẽ phù phép cho trang bị của ngươi trở lên mạnh mẽ";
            case PHAN_RA_DO_THAN_LINH:
                return "Ta sẽ phân rã \n  trang bị của người thành điểm!";
            case NANG_CAP_BONG_TAI:
                return "Ta sẽ phù phép\ncho bông tai Porata của ngươi\nthành cấp 2";
            case NANG_CHI_SO_BONG_TAI:
                return "Ta sẽ phù phép\ncho bông tai Porata cấp 2 của ngươi\ncó 1 chỉ số ngẫu nhiên";
            case NANG_CAP_BONG_TAI3:
                return "Ta sẽ phù phép\ncho bông tai Porata của ngươi\nthành cấp 3";
            case NANG_CHI_SO_BONG_TAI3:
                return "Ta sẽ phù phép\ncho bông tai Porata cấp 3 của ngươi\ncó 2 chỉ số ngẫu nhiên";    
            case NANG_CAP_SAO_PHA_LE:
                return "Ta sẽ phù phép\nnâng cấp Sao Pha Lê\nthành cấp 2";
            case DANH_BONG_SAO_PHA_LE:
                return "Đánh bóng\nSao pha lê cấp 2";
            case CUONG_HOA_LO_SAO_PHA_LE:
                return "Cường hóa\nÔ Sao Pha Lê";
            case TAO_DA_HEMATITE:
                return "Ta sẽ phù phép\ntạo đá hematite";
            case DUI_DUC:
                return "Ta sẽ phù phép\ntạo dùi đục";
            case DA_MAI:
                return "Ta sẽ phù phép\ntạo đá mài";
            case GIAM_DINH_SACH:
                return "Ta sẽ phù phép\ngiám định sách đó cho ngươi";
            case TAY_SACH:
                return "Ta sẽ phù phép\ntẩy sách đó cho ngươi";
            case NANG_CAP_SACH_TUYET_KY:
                return "Ta sẽ phù phép\nnâng cấp Sách Tuyệt Kỹ cho ngươi";
            case HOI_PHUC_SACH:
                return "Ta sẽ phù phép\nphục hồi sách cho ngươi";
            case PHAN_RA_SACH:
                return "Ta sẽ phù phép\nphân rã sách đó cho ngươi";
            case CHE_TAO_TRANG_BI_THIEN_SU:
                return "Chế tạo\ntrang bị thiên sứ";
            case LAM_PHEP_NHAP_DA:
                return "Ta sẽ phù phép\n"
                        + "cho 10 mảnh đá vụn\n"
                        + "trở thành 1 đá nâng cấp";
            default:
                return "";
        }
    }
    
    private String getTextInfoTabCombine(int type) {
        switch (type) {
            case NANG_CAP_KICH_HOAT:
                return "Vào hành trang\nChọn 1 trang bị Huỷ Diệt \nSau đó chọn 'Nâng cấp'";
            case NANG_CAP_KICH_HOAT_VIP:
                return "Vào hành trang\nChọn 3 trang bị Hủy Diệt\nSau đó chọn 'Nâng cấp' để chế tạo trang bị Kích Hoạt VIP";
            case DAP_DO_AO_HOA:
                return "Vào hành trang\nChọn trang bị và loại đá quý nâng cấp\nCó thể thêm đá bảo vệ để tránh tụt cấp\nSau đó chọn 'Nâng cấp'";
            case PS_HOA_TRANG_BI:
                return "Vào hành trang\nChọn trang bị có thể Pháp Sư Hóa\n(Pet,Đeo Lưng,Ván Bay)\nvà Đá Pháp Sư\nSau đó chọn 'Nâng cấp'";
            case TAY_PS_HOA_TRANG_BI:
                return "Vào hành trang\nChọn 1 vật phẩm cần tẩy và Đá Tẩy\nSau đó chọn 'Tẩy'";
            case MO_KHOA_ITEM:
                return "Vào hành trang\nChọn 1 vật phẩm khóa giao dịch (bông tai, vật phẩm sự kiện...)\nvà Đá Hoàng Kim\nSau đó chọn 'Mở khóa'";
            case NANG_CAP_CHAN_MENH:
                return "Vào hành trang\nChọn trang sức chân thiên tử\nChọn nguyên liệu Ma quái và Tinh thể\nSau đó chọn 'Nâng cấp'";
            case AN_TRANG_BI:
                return "Vào hành trang\nChọn 1 trang bị Huỷ Diệt và 99 mảnh Ấn\nSau đó chọn 'Làm phép'\n----\nTinh ấn (+15% HP)\nNhật ấn (+15% KI)\nNguyệt ấn (+15% SD)";
            case GIA_HAN_VAT_PHAM:
                return "Vào hành trang\nChọn 1 trang bị có hạn sử dụng và Đá Hoàng Kim\nSau đó chọn 'Gia hạn'";
            case SIEU_HOA:
                return "Vào hành trang\nChọn 1 Cải trang và Đá Siêu Hóa\nSau đó chọn 'Nâng cấp'";
            case TINH_THACH_HOA:
                return "Vào hành trang\nChọn 1 vật phẩm (Pet, Linh thú, VPDL)\nvà Đá Tinh Thạch\nSau đó chọn 'Nâng cấp'";
            case NANG_GIAP_LUYEN_TAP:
                return "Vào hành trang\nChọn 1 Giáp luyện tập và Đá Hổ Phách\nSau đó chọn 'Nâng cấp'";
            case NANG_CAP_DO_HD:
                return "Cần 1 món thần linh với 2 tỉ vàng\nSau đó chọn 'Nâng cấp'";
            case DOI_DO_THAN_LINH:
                return "Vào hành trang\nChọn 1 món Thần Linh (áo, quần, găng, giày, nhẫn)\nSau đó chọn 'Đổi'\nTỉ lệ: áo 25%, quần 20%, găng 15%,\ngiày 25%, nhẫn 15%\nPhí: 20 điểm săn boss + 10 thỏi vàng";
            case PHAN_RA_DO_HUY_DIET:
                return "Vào hành trang\nChọn 1 món Huỷ Diệt\nSau đó chọn 'Phân rã'\nNhận 30 - 60 phần thức ăn ngẫu nhiên";
            case EP_SAO_TRANG_BI:
                return "Chọn trang bị\n(Áo, quần, găng, giày hoặc rađa) có ô đặt sao pha lê\nChọn loại sao pha lê\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case PHA_LE_HOA_TRANG_BI:
                return "Chọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nSau đó chọn 'Nâng cấp'";
            case PHAN_RA_TRANG_BI_KH:
                return "Vào hành trang\nChọn hay nhiều\nTrang bị kích hoạt cần rã\nSau đó chọn 'Phân rã'";
            case TAI_TAO_CAPSULE_KH:
                return "Vào hành trang\nChọn 3 khoáng tái chế\nChọn 1 Capsule vỡ\nSau đó chọn 'Tái tạo'";
            case CHUYEN_HOA_TRANG_BI_NGOC:
            case CHUYEN_HOA_TRANG_BI_VANG:
                return "Vào hành trang\nChọn trang bị gốc\n(Áo,quần,găng,giày hoặc rada)\ntừ cấp[+4] trở lên\nChọn tiếp trang bị mới\nchưa nâng cấp cần nhập thể\nsau đó chọn 'Nâng cấp'";
            case NHAP_NGOC_RONG:
                return "Vào hành trang\nChọn 7 viên ngọc cùng sao\nSau đó chọn 'Làm phép'";
            case NANG_CAP_VAT_PHAM:
                return "vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nChọn loại đá để nâng cấp\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case PHAN_RA_DO_THAN_LINH:
                return "vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nChọn loại đá để phân rã\n"
                        + "Sau đó chọn 'Phân Rã'";
            case CHE_TAO_TRANG_BI_THIEN_SU:
                return "vào hành trang\nChọn 1 công thức và công thức Vip\nkèm 1 đá nâng, 1 đá may mắn\n và 999 mảnh thiên sứ\n "
                        + "Ta sẽ cho ra đồ thiên sứ từ 0-15% chỉ số\n"
                        + "Sau đó chọn 'Nâng Cấp'";
            case NANG_CAP_BONG_TAI:
                return "Vào hành trang\nChọn bông tai Porata\nChọn mảnh bông tai để nâng cấp, Số lượng 9999 cái"
                        + "\nSau đó chọn 'Nâng cấp'";
            case NANG_CHI_SO_BONG_TAI:
                return "Vào hành trang\nChọn bông tai Porata\nChọn mảnh hồn porata số lượng 99"
                        + "\ncái và đá xanh lam để nâng cấp.\nSau đó chọn 'Nâng cấp chỉ số'";
            case NANG_CAP_BONG_TAI3:
                return "Vào hành trang\nChọn bông tai Porata cấp 2\nChọn mảnh bông tai để nâng cấp, Số lượng 19999 cái"
                        + "\nSau đó chọn 'Nâng cấp'";
            case NANG_CHI_SO_BONG_TAI3:
                return "Vào hành trang\nChọn bông tai Porata Cấp 3\nChọn mảnh hồn porata số lượng 199"
                        + "\ncái và đá xanh lam để nâng cấp.\nSau đó chọn 'Nâng cấp chỉ số'";    
            case NANG_CAP_SAO_PHA_LE:
                return "Vào hành trang\nChọn đá Hematite\nChọn loại sao pha lê (cấp 1)\nSau đó chọn 'Nâng cấp'";
            case DANH_BONG_SAO_PHA_LE:
                return "Vào hành trang\nChọn loại sao pha lê cấp 2 có từ 2 viên trở lên\nChọn 1 đá mài\nSau đó chọn 'Đánh bóng'";
            case CUONG_HOA_LO_SAO_PHA_LE:
                return "Vào hành trang\nChọn trang bị có Ô sao thứ 8 trở lên chưa cường hóa\nChọn đá Hematite\nChọn dùi đục\nSau đó chọn 'Cường hóa'";
            case TAO_DA_HEMATITE:
                return "Vào hành trang\nChọn 5 sao pha lê cấp 1 cùng màu\nChọn 'Tạo đá Hematite'";
            case DUI_DUC:
                return "Vào hành trang\nChọn 5 viên đá Hematite\nChọn 'Tạo dùi đục'";
            case DA_MAI:
                return "Vào hành trang\nChọn 5 Dùi Đục\nChọn 'Tạo đá mài'";
            case GIAM_DINH_SACH:
                return "Vào hành trang chọn\n1 sách cần giám định";
            case TAY_SACH:
                return "Vào hành trang chọn\n1 sách cần tẩy";
            case NANG_CAP_SACH_TUYET_KY:
                return "Vào hành trang chọn\nSách Tuyệt Kỹ 1 cần nâng cấp và 10 Kìm bấm giấy";
            case HOI_PHUC_SACH:
                return "Vào hành trang chọn\nCác Sách Tuyệt Kỹ cần phục hồi";
            case PHAN_RA_SACH:
                return "Vào hành trang chọn\n1 sách cần phân rã";
            case LAM_PHEP_NHAP_DA:
                return "Vào hành trang\n"
                        + "Chọn 10 mảnh đá vụn\n"
                        + "Chọn 1 bình nước phép\n"
                        + "(mua tại Uron ở trạm tàu vũ trụ)\n"
                        + "Sau đó chọn 'Làm phép'";
            default:
                return "";
        }
    }
}
