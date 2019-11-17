package types;

import jade.core.AID;
import java.util.ArrayList;

// TODO LOGIC
public class StationHistory {
    public final AID station;
    public final ArrayList<WorkFinance> finances;
    public final WorkFinance total;

    public StationHistory(AID station) {
        this.station = station;
        this.finances = new ArrayList<>();
        this.total = new WorkFinance();
    }

    public void add(WorkFinance finance) {
        finances.add(finance);
        total.add(finance);
    }
}
