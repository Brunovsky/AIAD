package types;

import java.util.ArrayList;

import jade.core.AID;

public class StationHistory {
    public final AID station;
    public final ArrayList<WorkFinance> finances;
    public final WorkFinance total;

    public final ArrayList<Proposal> proposals;
    public final ArrayList<Proposal> assignments;

    public StationHistory(AID station) {
        this.station = station;
        this.finances = new ArrayList<>();
        this.total = new WorkFinance();
        this.proposals = new ArrayList<>();
        this.assignments = new ArrayList<>();
    }

    public void add(WorkFinance finance) {
        finances.add(finance);
        total.add(finance);
    }

    public void add(Proposal proposed, Proposal assigned) {
        proposals.add(proposed);
        assignments.add(assigned);
    }
}
