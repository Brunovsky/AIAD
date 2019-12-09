
package strategies;

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

    @Override
    public String toString() {
        return "PREFER_HARD";
    }
}
