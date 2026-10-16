package nro.models.event;

import nro.models.services.Service;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.event_list.TopUp;
import nro.models.event_list.TrungThu;
import nro.models.event_list.HungVuong;
import nro.models.event_list.Christmas;
import nro.models.event_list.Halloween;
import nro.models.event_list.LunarNewYear;
import nro.models.event_list.Default;
import nro.models.event_list.InternationalWomensDay;

public class EventManager {

    private static EventManager instance;

    public static boolean LUNNAR_NEW_YEAR = true;

    public static boolean INTERNATIONAL_WOMANS_DAY = true;

    public static boolean CHRISTMAS = true;

    public static boolean HALLOWEEN = true;

    public static boolean HUNG_VUONG = true;

    public static boolean TRUNG_THU = true;

    public static boolean TOP_UP = true;

    public static EventManager gI() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public void init() {
        new Default().init();
        if (LUNNAR_NEW_YEAR) {
           // new LunarNewYear().init();
        }
        if (INTERNATIONAL_WOMANS_DAY) {
           // new InternationalWomensDay().init();
        }
        if (HALLOWEEN) {
          //  new Halloween().init();
        }
        if (CHRISTMAS) {
          //  new Christmas().init();
        }
        if (HUNG_VUONG) {
            new HungVuong().init();
        }
        if (TRUNG_THU) {
           // new TrungThu().init();
        }
        if (TOP_UP) {
            new TopUp().init();
        }
        // ---- sự kiện bổ sung từ AWN_Version (mặc định tắt, bật trong Config.properties)
        VU_LAN_FESTIVAL = configOn("event.vulan");
        VALENTINE_DAY = configOn("event.valentine");
        BLACK_FRIDAY = configOn("event.blackfriday");
        DAY_20_10 = configOn("event.day2010");
        if (VU_LAN_FESTIVAL) {
            new nro.models.event.VuLanFestival.VuLanFestival().init();
        }
        if (VALENTINE_DAY) {
            new nro.models.event.ValentineDay.ValentineDay().init();
        }
        if (BLACK_FRIDAY) {
            new nro.models.event.BlackFriday.BlackFriday().init();
        }
        if (DAY_20_10) {
            new nro.models.event_list.WomensDay.WomensDay().init();
        }
    }

    public static boolean VU_LAN_FESTIVAL = false;

    public static boolean VALENTINE_DAY = false;

    public static boolean BLACK_FRIDAY = false;

    public static boolean DAY_20_10 = false;

    /**
     * Công tắc cho các sự kiện bổ sung từ AWN_Version, đặt trong Config.properties
     * (event.vulan, event.valentine, event.blackfriday, event.day2010 = true/false).
     */
    private static boolean configOn(String key) {
        try (java.io.FileInputStream f = new java.io.FileInputStream("Config.properties")) {
            java.util.Properties pr = new java.util.Properties();
            pr.load(f);
            return "true".equalsIgnoreCase(pr.getProperty(key, "false").trim());
        } catch (Exception e) {
            return false;
        }
    }

    public void setCurrentEvent(int eventId) {
        LUNNAR_NEW_YEAR = false;
        CHRISTMAS = false;
        VU_LAN_FESTIVAL = false;
        HALLOWEEN = false;
        INTERNATIONAL_WOMANS_DAY = false;
        TRUNG_THU = false;
        HUNG_VUONG = false;
        BLACK_FRIDAY = false;
        VALENTINE_DAY = false;
        DAY_20_10 = false;
        TOP_UP = false;

        switch (eventId) {
            case 1:
                LUNNAR_NEW_YEAR = true;
                nro.models.utils.Logger.success("Sự kiện Tết Nguyên Đán đã khởi động!\n");
                for (Player p : Client.gI().getPlayers()) {
                    Service.gI().sendThongBao(p, "|7|Sự kiện Tết Nguyên Đán đang diễn ra, nhanh tay cày quốc thôi nào!");
                }
                break;

            case 2:
                TRUNG_THU = true;
                nro.models.utils.Logger.success("Sự kiện Trung Thu đã khởi động!\n");
                for (Player p : Client.gI().getPlayers()) {
                    Service.gI().sendThongBao(p, "|7|Sự kiện Trung Thu đang diễn ra, nhanh tay cày quốc thôi nào!");
                }
                break;

            case 3:
                HALLOWEEN = true;
                nro.models.utils.Logger.success("Sự kiện Halloween đã khởi động!\n");
                for (Player p : Client.gI().getPlayers()) {
                    Service.gI().sendThongBao(p, "|7|Sự kiện Halloween đang diễn ra, nhanh tay cày quốc thôi nào!");
                }
                break;

            case 4:
                CHRISTMAS = true;
                nro.models.utils.Logger.success("Sự kiện Giáng Sinh đã khởi động!\n");
                for (Player p : Client.gI().getPlayers()) {
                    Service.gI().sendThongBao(p, "|7|Sự kiện Giáng Sinh đang diễn ra, nhanh tay cày quốc thôi nào!");
                }
                break;

            case 5:
                VU_LAN_FESTIVAL = true;
                nro.models.utils.Logger.success("Sự kiện Vu Lan Báo Hiếu đã khởi động!\n");
                for (Player p : Client.gI().getPlayers()) {
                    Service.gI().sendThongBao(p, "|7|Sự kiện Vu Lan đang diễn ra, nhanh tay cày quốc thôi nào!");
                }
                break;

            case 6:
                INTERNATIONAL_WOMANS_DAY = true;
                nro.models.utils.Logger.success("Sự kiện Quốc tế 8-3 đã khởi động!\n");
                for (Player p : Client.gI().getPlayers()) {
                    Service.gI().sendThongBao(p, "|7|Sự kiện Quốc tế 8-3 đang diễn ra, nhanh tay cày quốc thôi nào!");
                }
                break;

            case 7:
                HUNG_VUONG = true;
                nro.models.utils.Logger.success("Sự kiện Hùng Vương đã khởi động!\n");
                for (Player p : Client.gI().getPlayers()) {
                    Service.gI().sendThongBao(p, "|7|Sự kiện Hùng Vương đang diễn ra, nhanh tay cày quốc thôi nào!");
                }
                break;

            case 8:
                BLACK_FRIDAY = true;
                nro.models.utils.Logger.success("Sự kiện BlackFriday đã khởi động!\n");
                for (Player p : Client.gI().getPlayers()) {
                    Service.gI().sendThongBao(p, "|7|Sự kiện BlackFriday đang diễn ra, nhanh tay cày quốc thôi nào!");
                }
                break;

            case 9:
                VALENTINE_DAY = true;
                nro.models.utils.Logger.success("Sự kiện Valentine đã khởi động!\n");
                for (Player p : Client.gI().getPlayers()) {
                    Service.gI().sendThongBao(p, "|7|Sự kiện Valentine đang diễn ra, nhanh tay cày quốc thôi nào!");
                }
                break;

            case 10:
                DAY_20_10 = true;
                nro.models.utils.Logger.success("Sự kiện 20-10 đã khởi động!\n");
                for (Player p : Client.gI().getPlayers()) {
                    Service.gI().sendThongBao(p, "|7|Sự kiện 20/10 đang diễn ra, nhanh tay cày quốc thôi nào!");
                }
                break;

            case 11:
                TOP_UP = true;
                nro.models.utils.Logger.success("Sự kiện Top Up đã khởi động!\n");
                break;

            default:
                break;
        }

        init();
    }
}
