package strategies;

import types.JobList;
import types.Proposal;

/**
 * Company that prefers easy jobs with low pay that require less technicians
 */

public class PreferEasyCompanyStrategy extends CompanyStrategy {
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

    @Override
    public String toString() {
        return "PREFER_EASY";
    }
}
