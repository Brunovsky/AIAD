package types;

import utils.Logger.Format;

public class Finance extends Record {
    public double cost = 0.0;
    public double revenue = 0.0;
    public int proposalWeight = 0;
    public int assignedWeight = 0;
    public int workerWeight = 0;

    public int wastedWeight() {
        return workerWeight - proposalWeight;
    }

    public int lostWeight() {
        return workerWeight - assignedWeight;
    }

    public Finance() {}

    public void add(WorkdayFinance workday) {
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

    public static String csvHeader() {
        return "cost,revenue,proposal,assigned,worker";
    }

    public static String tableHeader() {
        return String.format("%10s  %10s  %8s  %8s  %8s", "cost", "revenue", "proposal", "assigned",
                             "worker");
    }

    public static String header(Format format) {
        switch (format) {
        case CSV:
            return csvHeader();
        case TABLE:
        default:
            return tableHeader();
        }
    }

    @Override
    public String csv() {
        return String.format("%f,%f,%d,%d,%d", cost, revenue, proposalWeight, assignedWeight,
                             workerWeight);
    }

    @Override
    public String table() {
        return String.format("%10.2f  %10.2f  %8d  %8d  %8d", cost, revenue, proposalWeight,
                             assignedWeight, workerWeight);
    }
}
