package strategies;

import jade.core.AID;
import java.util.Map;
import simulation.World;
import types.JobList;
import types.Proposal;
import types.StationHistory;
import types.TypedFinance;

/**
 * Company that prefers easy jobs with low pay that require less technicians
 */
public class EasyCommonCompanyStrategy extends CompanyStrategy {
    private double easyPrice = World.get().getEasyBase();
    private double mediumPrice = World.get().getMediumBase();
    private double hardPrice = World.get().getHardBase();

    private final int memory = 18;
    private final int rhythm = 1;

    private final double EASY_GOAL = 0.200;
    private final double MEDIUM_GOAL = 0.375;
    private final double HARD_GOAL = 0.550;

    private final double EASY_INC = 0.030 * World.get().getEasyBase();
    private final double MEDIUM_INC = 0.040 * World.get().getMediumBase();
    private final double HARD_INC = 0.050 * World.get().getHardBase();

    @Override
    public void adjustPrices() {
        if (World.get().getDay() % rhythm != 0) return;

        TypedFinance finance = new TypedFinance();
        for (StationHistory history : company.getStationHistory().values()) {
            finance.add(history.lastDaysAggregate(memory));
        }

        double rejected, price;
        final double time = rhythm / (double) (World.get().getDay() + 1);

        // EASY
        rejected = finance.easy.rejectedShare();
        price = easyPrice + (EASY_GOAL - rejected) * EASY_INC * time;
        easyPrice = price;

        // MEDIUM
        rejected = finance.medium.rejectedShare();
        price = mediumPrice + (MEDIUM_GOAL - rejected) * MEDIUM_INC * time;
        mediumPrice = price;

        // EASY
        rejected = finance.hard.rejectedShare();
        price = hardPrice + (HARD_GOAL - rejected) * HARD_INC * time;
        hardPrice = price;
    }

    @Override
    public Proposal makeProposal(int technicians, JobList jobList, AID station) {
        Proposal proposal = new Proposal(company.getAID());
        assert technicians > 0;

        proposal.easyPrice = easyPrice;
        proposal.mediumPrice = mediumPrice;
        proposal.hardPrice = hardPrice;

        int weight = technicians * World.WORKER_WEIGHT;

        // take easy jobs
        if (weight >= World.EASY_WEIGHT * jobList.easy) {
            weight -= World.EASY_WEIGHT * jobList.easy;
            proposal.easy = jobList.easy;
        } else {
            proposal.easy = weight / World.EASY_WEIGHT;
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

        // take hard jobs
        if (weight >= World.HARD_WEIGHT * jobList.hard) {
            weight -= World.HARD_WEIGHT * jobList.hard;
            proposal.hard = jobList.hard;
        } else {
            proposal.hard = weight / World.HARD_WEIGHT;
            return proposal;
        }

        return proposal;
    }

    @Override
    public void populateRow(Map<String, String> row) {
        row.put("easy_price", String.format("%.2f", easyPrice));
        row.put("medium_price", String.format("%.2f", mediumPrice));
        row.put("hard_price", String.format("%.2f", hardPrice));
    }

    @Override
    public void populateRow(Map<String, String> row, AID station) {
        row.put("easy_price", String.format("%.2f", easyPrice));
        row.put("medium_price", String.format("%.2f", mediumPrice));
        row.put("hard_price", String.format("%.2f", hardPrice));
    }

    @Override
    public String getName() {
        return "EASY_COMMON";
    }

    @Override
    public String toString() {
        return "EASY";
    }
}
