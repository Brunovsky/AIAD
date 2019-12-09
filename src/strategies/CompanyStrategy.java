package strategies;

import agents.Company;
import types.JobList;
import types.Proposal;

public abstract class CompanyStrategy {
    protected Company company = null;

    public void setCompany(Company company) {
        this.company = company;
    }

    public abstract Proposal makeProposal(int technicians, JobList jobList);
}
