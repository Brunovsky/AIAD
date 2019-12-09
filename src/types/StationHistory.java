package types;

import java.util.ArrayList;
import java.util.Map;

import jade.core.AID;
import utils.Table;

public class StationHistory {
    public final AID station;
    public final ArrayList<WorkdayFinance> workdays;
    public final Finance finance;

    public StationHistory(AID station) {
        this.station = station;
        this.workdays = new ArrayList<>();
        this.finance = new Finance();
    }

    public void add(WorkdayFinance workday) {
        workdays.add(workday);
        finance.add(workday);
    }

    public void populateRow(Map<String, String> row) {
        row.put("station", station.getLocalName());
        finance.populateRow(row);
    }

    public Table makeTable() {
        Table table = new Table(station.getLocalName());
        for (WorkdayFinance workday : workdays) workday.rowSet(table.addRow());
        return table;
    }
}
