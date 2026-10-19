package nro.models.player;

import lombok.Getter;
import lombok.Setter;
import nro.models.player.Player;

/**
 * @author By Mr Blue
 */
@Setter
@Getter
public class PlayerEvent {
    private int eventPoint;
    private Player player;
    public int luotNhanNgocMienPhi = 1;
    public int luotNhanCapsuleBang = 1;

    public PlayerEvent(Player player) {
        this.player = player;
    }
    
    public void addEventPoint(int num) {
        eventPoint += num;
    }
    
    // Ngày hiện tại dạng yyyyMMdd (rẻ hơn SimpleDateFormat + parseInt)
    private static int today() {
        java.time.LocalDate d = java.time.LocalDate.now();
        return d.getYear() * 10000 + d.getMonthValue() * 100 + d.getDayOfMonth();
    }

    // Giới hạn điểm săn boss mỗi ngày (chỉ tính điểm từ diệt boss, điểm nhiệm vụ không bị giới hạn)
    public static final int BOSS_POINT_DAILY_LIMIT = 700;
    private int bossPointToday;
    private int bossPointDay; // ngày dạng yyyyMMdd

    private void resetBossPointIfNewDay() {
        int today = today();
        if (bossPointDay != today) {
            bossPointDay = today;
            bossPointToday = 0;
        }
    }

    /**
     * Cộng điểm săn boss có giới hạn theo ngày
     *
     * @return số điểm thực tế được cộng (0 nếu đã đủ giới hạn)
     */
    public int addBossPointDaily(int num) {
        resetBossPointIfNewDay();
        int add = Math.max(0, Math.min(num, BOSS_POINT_DAILY_LIMIT - bossPointToday));
        bossPointToday += add;
        eventPoint += add;
        return add;
    }

    public int getBossPointToday() {
        resetBossPointIfNewDay();
        return bossPointToday;
    }

    public void setBossPointToday(int bossPointToday) {
        this.bossPointToday = bossPointToday;
    }

    public int getBossPointDay() {
        return bossPointDay;
    }

    public void setBossPointDay(int bossPointDay) {
        this.bossPointDay = bossPointDay;
    }

    // Giới hạn tặng điểm săn boss mỗi ngày: mỗi tài khoản gửi tối đa và nhận tối đa TRANSFER_POINT_DAILY_LIMIT điểm/ngày
    public static final int TRANSFER_POINT_DAILY_LIMIT = 100;
    private int transferPointDay; // ngày dạng yyyyMMdd
    private int transferPointSent;
    private int transferPointReceived;

    private void resetTransferPointIfNewDay() {
        int today = today();
        if (transferPointDay != today) {
            transferPointDay = today;
            transferPointSent = 0;
            transferPointReceived = 0;
        }
    }

    public int getTransferPointSendRemaining() {
        resetTransferPointIfNewDay();
        return Math.max(0, TRANSFER_POINT_DAILY_LIMIT - transferPointSent);
    }

    public int getTransferPointReceiveRemaining() {
        resetTransferPointIfNewDay();
        return Math.max(0, TRANSFER_POINT_DAILY_LIMIT - transferPointReceived);
    }

    public void addTransferPointSent(int num) {
        resetTransferPointIfNewDay();
        transferPointSent += num;
    }

    public void addTransferPointReceived(int num) {
        resetTransferPointIfNewDay();
        transferPointReceived += num;
    }

    // Giới hạn đổi điểm săn boss sang Coin Khai Nguyên mỗi ngày
    public static final int COIN_EXCHANGE_DAILY_LIMIT = 100;
    private int coinExchangeDay; // ngày dạng yyyyMMdd
    private int coinExchangeToday;

    private void resetCoinExchangeIfNewDay() {
        int today = today();
        if (coinExchangeDay != today) {
            coinExchangeDay = today;
            coinExchangeToday = 0;
        }
    }

    public int getCoinExchangeRemaining() {
        resetCoinExchangeIfNewDay();
        return Math.max(0, COIN_EXCHANGE_DAILY_LIMIT - coinExchangeToday);
    }

    public void addCoinExchangeToday(int num) {
        resetCoinExchangeIfNewDay();
        coinExchangeToday += num;
    }

    public void subEventPoint(int num) {
        eventPoint -= num;
    }

    public void update() {
       
    }


    public int getNamekWarPoint() {
        return NamekWarPoint;
    }

    public void subNamekWarPoint(int num) {
        NamekWarPoint -= num;
    }

    public void addNamekWarPoint(int num) {
        NamekWarPoint += num;
    }

    public int NamekWarPoint;

    public int getEventPoint() {
        return eventPoint;
    }

    public void setEventPoint(int eventPoint) {
        this.eventPoint = eventPoint;
    }

    public int getTrungThuPoint() {
        return TrungThuPoint;
    }
    public int TrungThuPoint;

    public void addTrungThuPoint(int num) {
        TrungThuPoint += num;
    }

    public int getBlackFridayPoint() {
        return BlackFridayPoint;
    }

    public int getInternationalWomensDayPoint() {
        return InternationalWomensDayPoint;
    }

    public void addInternationalWomensDayPoint(int num) {
        InternationalWomensDayPoint += num;
    }

    public int get20_10Point() {
        return Day20_10Point;
    }

    public void set20_10Point(int num) {
        Day20_10Point = num;
    }

    public void add20_10Point(int num) {
        Day20_10Point += num;
    }

    public void sub20_10Point(int num) {
        Day20_10Point -= num;
    }

    // Số bông hoa đã tặng sự kiện 20/10 (dùng cho đua top)
    private int flower20_10;

    public int getFlower20_10() {
        return flower20_10;
    }

    public void setFlower20_10(int num) {
        flower20_10 = num;
    }

    public void addFlower20_10(int num) {
        flower20_10 += num;
    }

    public void addChristMasPoint(int num) {
        ChristMasPoint += num;
    }

    public int getLunaNewYearPoint() {
        return LunaNewYearPoint;
    }

    public void addLunaNewYearPoint(int num) {
        LunaNewYearPoint += num;
    }

    public int getChristMasPoint() {
        return ChristMasPoint;
    }

    public int getHalloweenPoint() {
        return HalloweenPoint;
    }

    public void addHalloweenPoint(int num) {
        HalloweenPoint += num;
    }

    private int BlackFridayPoint;

    private int InternationalWomensDayPoint;

    private int Day20_10Point;

    private int ChristMasPoint;

    private int LunaNewYearPoint;

    private int HalloweenPoint;
}
