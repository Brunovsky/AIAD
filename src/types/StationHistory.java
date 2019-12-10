package types;

import jade.core.AID;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class StationHistory {
    public final AID station;
    public final List<Workday> workdays;
    public final Finance finance;

    public StationHistory(AID station) {
        this.station = station;
        this.workdays = new LinkedList<>();
        this.finance = new Finance();
    }

    public void add(Workday workday) {
        workdays.add(workday);
        finance.add(workday);
    }

    public Workday last() {
        return workdays.isEmpty() ? null : workdays.get(workdays.size() - 1);
    }

    public List<Workday> lastDays(int n) {
        List<Workday> last = new LinkedList<>();
        ListIterator<Workday> it = workdays.listIterator(workdays.size());
        while (it.hasPrevious() && n-- > 0) last.add(it.previous());
        return last;
    }

    public TypedFinance lastDaysAggregate(int n) {
        TypedFinance finance = new TypedFinance();
        ListIterator<Workday> it = workdays.listIterator(workdays.size());
        while (it.hasPrevious() && n-- > 0) finance.add(it.previous());
        return finance;
    }
}
