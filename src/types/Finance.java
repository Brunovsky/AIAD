package types;

import java.util.Map;

public class Finance {
    public double cost = 0.0;
    public double revenue = 0.0;
    public int proposalWeight = 0;
    public int assignedWeight = 0;
    public int workerWeight = 0;

    public int rejectedWeight() {
        return proposalWeight - assignedWeight;
    }

    public int wastedWeight() {
        return workerWeight - proposalWeight;
    }

    public int lostWeight() {
        return workerWeight - assignedWeight;
    }

    // Share of jobs accepted by the station, [0,1]
    public double acceptedShare() {
        if (proposalWeight == 0) return 0.5;
        return (double) assignedWeight / proposalWeight;
    }

    // Share of jobs rejected by the station, [0,1]
    public double rejectedShare() {
        if (proposalWeight == 0) return 0.5;
        return (double) rejectedWeight() / proposalWeight;
    }

    // Share of workers not assigned to a proposal (not enough jobs), [0,1]
    public double wastedShare() {
        if (workerWeight == 0) return 0;
        return (double) wastedWeight() / workerWeight;
    }

    // Share of workers not assigned to a job (not enough (accepted) jobs), [0,1]
    public double lostShare() {
        if (workerWeight == 0) return 0;
        return (double) lostWeight() / workerWeight;
    }

    public Finance() {}

    public void add(Workday workday) {
        this.cost += workday.cost;
        this.revenue += workday.revenue;
        this.proposalWeight += workday.proposalWeight();
        this.assignedWeight += workday.assignedWeight();
        this.workerWeight += workday.workerWeight();
    }

    public void add(Finance finance) {
        this.cost += finance.cost;
        this.revenue += finance.revenue;
        this.proposalWeight += finance.proposalWeight;
        this.assignedWeight += finance.assignedWeight;
        this.workerWeight += finance.workerWeight;
    }

    // ***** FORMATTING

    public void populateRow(Map<String, String> row) {
        row.put("cost", String.format("%.1f", cost));
        row.put("revenue", String.format("%.1f", revenue));
        row.put("proposal", String.format("%d", proposalWeight));
        row.put("assigned", String.format("%d", assignedWeight));
        row.put("worker", String.format("%d", workerWeight));
    }
}
