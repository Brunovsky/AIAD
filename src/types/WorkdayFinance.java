package types;

import simulation.World;
import utils.Logger.Format;

/**
 * Report for one day's work at a station or accross all stations for a given company.
 */
public class WorkdayFinance extends Record {
    public final int day;
    public final int technicians;
    public final Proposal proposal;
    public final Proposal assignment;
    public final double cost;
    public final double revenue;

    public WorkdayFinance(int technicians, Proposal proposal, Proposal assignment) {
        this.day = World.get().getDay();
        this.technicians = technicians;
        this.proposal = proposal;
        this.assignment = assignment;
        this.cost = World.get().getSalary() * technicians;
        this.revenue = assignment.totalRevenue();
    }

    public static WorkdayFinance empty() {
        return new WorkdayFinance(0, null, null);
    }

    public int proposalWeight() {
        return proposal.weight();
    }

    public int assignedWeight() {
        return assignment.weight();
    }

    public int workerWeight() {
        return World.WORKER_WEIGHT * technicians;
    }

    public int wastedWeight() {
        return workerWeight() - proposalWeight();
    }

    public int lostWeight() {
        return workerWeight() - assignedWeight();
    }

    // ***** FORMATTING

    public static String csvHeader() {
        return "day,techns,cost,revenue,proposal,assigned,worker";
    }

    public static String tableHeader() {
        return String.format("%3s  %6s  %10s  %10s  %8s  %8s  %8s", "day", "techns", "cost",
                             "revenue", "proposal", "assigned", "worker");
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
        return String.format("%d,%d,%f,%f,%d,%d,%d", day, technicians, cost, revenue,
                             proposalWeight(), assignedWeight(), workerWeight());
    }

    @Override
    public String table() {
        return String.format("%3d  %6d  %10.2f  %10.2f  %8d  %8d  %8d", day, technicians, cost,
                             revenue, proposalWeight(), assignedWeight(), workerWeight());
    }
}
