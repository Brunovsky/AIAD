package strategies;

import types.Contract;
import types.JobList;
import types.Proposal;

// TODO STRATEGY
public class SimpleCompanyStrategy extends CompanyStrategy {
    @Override
    public Contract initialContract() {
        return null;
    }

    @Override
    public Proposal makeProposal(int technicians, JobList jobList) {
        return null;
    }
}
