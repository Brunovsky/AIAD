package strategies;

import agents.Company;
import types.Contract;
import types.JobList;
import types.Proposal;

public abstract class CompanyStrategy {
    protected Company company;

    public enum Performance { VERY_GOOD, GOOD, NEUTRAL, BAD, VERY_BAD }

    public void setCompany(Company company) {
        this.company = company;
    }

    public abstract Contract initialContract();

    public abstract Proposal makeProposal(int technicians, JobList jobList);
}
