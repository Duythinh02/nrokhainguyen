package nro.models.combine;
import lombok.Setter;
import nro.models.item.Item;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author By Mr Blue
 */

public class Combine {

    public long lastTimeCombine;

    public List<Item> itemsCombine;
    @Setter
    public int typeCombine;

    public int goldCombine;
    public int gemCombine;
    public float ratioCombine;
    public int countDaNangCap;
    public short countDaBaoVe;
    public int countDaQuy;
    public int DiemNangcap;
    public int DaNangcap;
    public float TileNangcap;
    public int rubyCombine;

    public Combine() {
        this.itemsCombine = new ArrayList<>();
    }

    public void clearItemCombine() {
        this.itemsCombine.clear();
    }

    public void clearParamCombine() {
        this.goldCombine = 0;
        this.gemCombine = 0;
        this.ratioCombine = 0;
        this.countDaNangCap = 0;
        this.countDaBaoVe = 0;
        this.countDaQuy = 0;
        this.DiemNangcap = 0;
        this.DaNangcap = 0;
        this.TileNangcap = 0;
        this.rubyCombine = 0;

    }

    public void dispose() {
        this.itemsCombine = null;
    }

    public void setTypeCombine(int type) {
        this.typeCombine = type;
    }
}
