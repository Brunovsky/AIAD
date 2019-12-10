package types;

import java.util.Map;
import simulation.World;

/**
 * Report for one day's work at a station or accross all stations for a given company.
 */
public class Workday {
    public final int day;
    public final int technicians;
    public final Proposal proposal;
    public final Proposal assignment;
    public final double cost;
    public final double revenue;

    public Workday(int technicians, Proposal proposal, Proposal assignment) {
        this.day = World.get().getDay();
        this.technicians = technicians;
        this.proposal = proposal;
        this.assignment = assignment;
        this.cost = World.get().getSalary() * technicians;
        this.revenue = assignment.totalRevenue();
    }

    public static Workday empty() {
        return new Workday(0, null, null);
    }

    public int proposalWeight() {
        return proposal.totalWeight();
    }

    public int assignedWeight() {
        return assignment.totalWeight();
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

    public void populateRow(Map<String, String> row) {
        row.put("day", String.format("%d", day));
        row.put("techns", String.format("%d", technicians));
        row.put("cost", String.format("%.1f", cost));
        row.put("revenue", String.format("%.1f", revenue));
        row.put("proposal", String.format("%d", proposalWeight()));
        row.put("assigned", String.format("%d", assignedWeight()));
        row.put("worker", String.format("%d", workerWeight()));
        row.put("easy", String.format("%d/%d", assignment.easy, proposal.easy));
        row.put("medium", String.format("%d/%d", assignment.medium, proposal.medium));
        row.put("hard", String.format("%d/%d", assignment.hard, proposal.hard));
    }
}
