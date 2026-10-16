package nro.models.matches;

import lombok.Builder;
import lombok.Data;
import nro.models.player.Player;

@Data
@Builder
public class TOP {

    private String name;
    private byte gender;
    private short head;
    private short body;
    private short leg;
    @lombok.Getter @lombok.Setter private long power;
    @lombok.Getter @lombok.Setter private long ki;
    @lombok.Getter @lombok.Setter private long hp;
    @lombok.Getter @lombok.Setter private long sd;
    @lombok.Getter @lombok.Setter private byte nv;
    @lombok.Getter @lombok.Setter private byte subnv;
    private int sk;
    private int pvp;
    private int nhs;
    private int dicanh;
    private int divdst;
    private int juventus;
    private long lasttime;
    @lombok.Getter @lombok.Setter private long time;
    private int level;
    private int cash;
    @lombok.Getter @lombok.Setter private int thoivang;
    @lombok.Getter @lombok.Setter private int id_player;
    @lombok.Getter @lombok.Setter private String info1;
    @lombok.Getter @lombok.Setter private String info2;
    private long paramCompare;
    
    public void setId_player(int id_player) {
    this.id_player = id_player;
}


    @lombok.Getter @lombok.Setter private long LastTimeLogin;

    @lombok.Getter @lombok.Setter private int top;

    @lombok.Getter @lombok.Setter private int vnd;

    @lombok.Getter @lombok.Setter private int coin;

    @lombok.Getter @lombok.Setter private int danap;

    @lombok.Getter @lombok.Setter private int hongngoc;

    @lombok.Getter @lombok.Setter private long power_pet;

    @lombok.Getter @lombok.Setter private long hp_pet;

    @lombok.Getter @lombok.Setter private long ki_pet;

    @lombok.Getter @lombok.Setter private long sd_pet;

    @lombok.Getter @lombok.Setter private int PointNamekWar;

    @lombok.Getter @lombok.Setter private int TangLixi;

    @lombok.Getter @lombok.Setter private int MoLixi;

    @lombok.Getter @lombok.Setter private int BanPhaoHoa;

    @lombok.Getter @lombok.Setter private int BanPhaoHoaVIP;

    @lombok.Getter @lombok.Setter private int TrangtricayNoel;

    @lombok.Getter @lombok.Setter private int Chetaonguoituyet;

    @lombok.Getter @lombok.Setter private int Chetaonguoituyetbanggia;

    @lombok.Getter @lombok.Setter private int Dotdiem;

    @lombok.Getter @lombok.Setter private int BanPhaoHoaVuLan;

    @lombok.Getter @lombok.Setter private int HoaDang;

    @lombok.Getter @lombok.Setter private int HoaDangLoiChuc;

    @lombok.Getter @lombok.Setter private int MoHopMaQuy;

    @lombok.Getter @lombok.Setter private int ThiepHalloween;

    @lombok.Getter @lombok.Setter private int DuaTopMoThiep_83;

    @lombok.Getter @lombok.Setter private int DuaTopTangBongHoaHong;

    @lombok.Getter @lombok.Setter private int DuaTopLamBanhTrungThu;

    @lombok.Getter @lombok.Setter private int DuaTopMoBanhTrungThuDacBiet;

    @lombok.Getter @lombok.Setter private int DuaTopMoTrungRongVang;

    @lombok.Getter @lombok.Setter private int DuaTopMoHopQuaGioTo;

    @lombok.Getter @lombok.Setter private int DuaTopDangBanhHungVuong;

    @lombok.Getter @lombok.Setter private int DuaTopDoiDuaHau;

    @lombok.Getter @lombok.Setter private int DuaTopHopQuaBlackFriday;

    @lombok.Getter @lombok.Setter private int DuaTopMuaSam;

    @lombok.Getter @lombok.Setter private long TopMayDamTraiDat;

    @lombok.Getter @lombok.Setter private long TopMayDamNamec;

    @lombok.Getter @lombok.Setter private long TopMayDamXayda;
}
