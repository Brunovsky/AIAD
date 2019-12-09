package types;

import jade.core.AID;
import java.util.ArrayList;
import utils.Logger.Format;

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

    public String formatWorkdays(Format format) {
        StringBuilder builder = new StringBuilder();

        builder.append(WorkdayFinance.header(format)).append('\n');
        for (WorkdayFinance workday : workdays) {
            builder.append(workday.format(format)).append('\n');
        }

        return builder.toString();
    }

    public String formatFinance(Format format) {
        return finance.format(format) + '\n';
    }

    public String format(Format format) {
        return Finance.header(format) + '\n' + formatFinance(format) + formatWorkdays(format);
    }
}
