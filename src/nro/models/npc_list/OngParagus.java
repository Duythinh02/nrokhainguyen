package nro.models.npc_list;

public class OngParagus extends OngGohan {

    public OngParagus(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    protected String loiChao() {
        return "Con cố gắng theo Vua Vegeta học thành tài, đừng lo lắng cho ta.";
    }
}
