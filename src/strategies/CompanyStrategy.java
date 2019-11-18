package strategies;

import agents.Company;
import jade.core.AID;
import types.Contract;
import types.JobList;
import types.Proposal;

public abstract class CompanyStrategy {
    protected Company company = null;

    public enum Performance { VERY_GOOD, GOOD, NEUTRAL, BAD, VERY_BAD }

    public void setCompany(Company company) {
        this.company = company;
    }

    public abstract Contract initialContract(AID technician, AID station);

    public abstract Proposal makeProposal(int technicians, JobList jobList);
}
