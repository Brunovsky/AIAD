package strategies;

import jade.core.AID;
import simulation.World;
import types.Contract;
import types.JobList;
import types.Proposal;

/**
 * Company that prefers easy jobs with low pay that require less technicians
 *
 * Since these jobs are very common, the company has low risk, and therefore can afford to pay
 * technicians a more substancial salary but reduced job cuts.
 *
 * Company with 20 employees pays base salary of 20 * 28 = 560.
 */

public class PreferEasyCompanyStrategy extends CompanyStrategy {
    @Override
    public Contract initialContract(AID technician, AID station) {
        String stationName = station.getLocalName();
        int day = World.get().getDay();
        int start = day, end = day + 10;
        double salary = 28;
        double percentage = 0.05;
        return new Contract(company.getAID(), technician, stationName, salary, percentage, start,
                            end);
    }

    @Override
    public Proposal makeProposal(int technicians, JobList jobList) {
        Proposal proposal = new Proposal(company.getAID());
        assert technicians > 0;

        proposal.easyPrice = 25;
        proposal.mediumPrice = 85;
        proposal.hardPrice = 310;

        // take easy jobs
        if (3 * technicians >= jobList.easy) {
            technicians -= (jobList.easy + 2) / 3;
            proposal.easy = jobList.easy;
        } else {
            proposal.easy = 3 * technicians;
            return proposal;
        }

        // take medium jobs
        if (technicians >= jobList.medium) {
            technicians -= jobList.medium;
            proposal.medium = jobList.medium;
        } else {
            proposal.medium = technicians;
            return proposal;
        }

        // take hard jobs
        if (technicians >= 3 * jobList.hard) {
            technicians -= 3 * jobList.hard;
            proposal.hard = jobList.hard;
        } else {
            proposal.hard = technicians / 3;
            return proposal;
        }

        return proposal;
    }
}
