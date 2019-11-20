
package strategies;

import jade.core.AID;
import simulation.World;
import types.Contract;
import types.JobList;
import types.Proposal;

/**
 * Company that prefers hard jobs with high pay that require more technicians
 *
 * Since hard jobs are less common, the company reduces risk by giving technicians a more
 * substantial percentage of each job's pay but a lower salary.
 *
 * Company with 20 employees pays base salary of 20 * 20 = 400.
 */

public class PreferHardCompanyStrategy extends CompanyStrategy {
    @Override
    public Contract initialContract(AID technician, AID station) {
        String stationName = station.getLocalName();
        int day = World.get().getDay();
        int start = day, end = day + 3;
        double salary = 20;
        double percentage = 0.25;
        return new Contract(company.getAID(), technician, stationName, salary, percentage, start,
                            end);
    }

    @Override
    public Proposal makeProposal(int technicians, JobList jobList) {
        Proposal proposal = new Proposal(company.getAID());
        assert technicians > 0;

        proposal.easyPrice = 30;
        proposal.mediumPrice = 85;
        proposal.hardPrice = 270;

        // take hard jobs
        if (technicians >= 3 * jobList.hard) {
            technicians -= 3 * jobList.hard;
            proposal.hard = jobList.hard;
        } else {
            proposal.hard = technicians / 3;
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

        // take easy jobs
        if (3 * technicians >= jobList.easy) {
            technicians -= (jobList.easy + 2) / 3;
            proposal.easy = jobList.easy;
        } else {
            proposal.easy = 3 * technicians;
            return proposal;
        }

        return proposal;
    }
}
