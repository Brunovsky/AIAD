
package strategies;

import jade.core.AID;
import java.util.HashMap;
import java.util.Map;
import simulation.World;
import types.JobList;
import types.Proposal;
import types.StationHistory;
import types.TypedFinance;

/**
 * Company that prefers hard jobs with high pay that require more technicians
 */
public class HardSpecializedCompanyStrategy extends CompanyStrategy {
    private Map<AID, Double> easyPrice = new HashMap<>();
    private Map<AID, Double> mediumPrice = new HashMap<>();
    private Map<AID, Double> hardPrice = new HashMap<>();

    private final int memory = 12;
    private final int rhythm = 2;

    private final double EASY_GOAL = 0.500;
    private final double MEDIUM_GOAL = 0.375;
    private final double HARD_GOAL = 0.250;

    private final double EASY_INC = 0.050 * World.get().getEasyBase();
    private final double MEDIUM_INC = 0.040 * World.get().getMediumBase();
    private final double HARD_INC = 0.030 * World.get().getHardBase();

    private void insert(AID station) {
        easyPrice.putIfAbsent(station, World.get().getEasyBase());
        mediumPrice.putIfAbsent(station, World.get().getMediumBase());
        hardPrice.putIfAbsent(station, World.get().getHardBase());
    }

    protected void adjustPrice(StationHistory history) {
        AID station = history.station;
        TypedFinance finance = history.lastDaysAggregate(memory);

        insert(station);

        double rejected, price;
        final double time = rhythm / (double) (World.get().getDay() + 1);

        // EASY
        rejected = finance.easy.rejectedShare();
        price = easyPrice.get(station) + (EASY_GOAL - rejected) * EASY_INC * time;
        easyPrice.put(station, price);

        // MEDIUM
        rejected = finance.medium.rejectedShare();
        price = mediumPrice.get(station) + (MEDIUM_GOAL - rejected) * MEDIUM_INC * time;
        mediumPrice.put(station, price);

        // EASY
        rejected = finance.hard.rejectedShare();
        price = hardPrice.get(station) + (HARD_GOAL - rejected) * HARD_INC * time;
        hardPrice.put(station, price);
    }

    @Override
    public void adjustPrices() {
        if (World.get().getDay() % rhythm != 0) return;
        for (StationHistory history : company.getStationHistory().values()) {
            adjustPrice(history);
        }
    }

    @Override
    public Proposal makeProposal(int technicians, JobList jobList, AID station) {
        Proposal proposal = new Proposal(company.getAID());
        assert technicians > 0;

        insert(station);

        proposal.easyPrice = easyPrice.get(station);
        proposal.mediumPrice = mediumPrice.get(station);
        proposal.hardPrice = hardPrice.get(station);

        int weight = technicians * World.WORKER_WEIGHT;

        // take hard jobs
        if (weight >= World.HARD_WEIGHT * jobList.hard) {
            weight -= World.HARD_WEIGHT * jobList.hard;
            proposal.hard = jobList.hard;
        } else {
            proposal.hard = weight / World.HARD_WEIGHT;
            return proposal;
        }

        // take medium jobs
        if (weight >= World.MEDIUM_WEIGHT * jobList.medium) {
            weight -= World.MEDIUM_WEIGHT * jobList.medium;
            proposal.medium = jobList.medium;
        } else {
            proposal.medium = weight / World.MEDIUM_WEIGHT;
            return proposal;
        }

        // take easy jobs
        if (weight >= World.EASY_WEIGHT * jobList.easy) {
            weight -= World.EASY_WEIGHT * jobList.easy;
            proposal.easy = jobList.easy;
        } else {
            proposal.easy = weight / World.EASY_WEIGHT;
            return proposal;
        }

        return proposal;
    }

    private double average(Map<AID, Double> map) {
        double total = 0.0;
        for (double price : map.values()) total += price;
        return total / map.size();
    }

    @Override
    public void populateRow(Map<String, String> row) {
        row.put("easy_price", String.format("%.2f", average(easyPrice)));
        row.put("medium_price", String.format("%.2f", average(mediumPrice)));
        row.put("hard_price", String.format("%.2f", average(hardPrice)));
    }

    @Override
    public void populateRow(Map<String, String> row, AID station) {
        insert(station);
        row.put("easy_price", String.format("%.2f", easyPrice.get(station)));
        row.put("medium_price", String.format("%.2f", mediumPrice.get(station)));
        row.put("hard_price", String.format("%.2f", hardPrice.get(station)));
    }

    @Override
    public String getName() {
        return "HARD_SPECIALIZED";
    }

    @Override
    public String toString() {
        return "HARD";
    }
}
