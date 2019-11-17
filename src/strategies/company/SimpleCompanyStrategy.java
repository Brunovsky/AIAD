package strategies.company;

import types.Contract;
import types.JobList;
import types.Proposal;

public class SimpleCompanyStrategy extends CompanyStrategy {
    @Override
    public Contract initialContract() {
        return null;
    }

    @Override
    public Proposal makeProposal(int technicians, JobList jobList) {
        // Prefer hard tasks
        return null;
    }

    @Override
    public boolean acceptContractOffer(Contract contract) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Performance evaluatePerformance() {
        // TODO Auto-generated method stub
        return null;
    }
}
