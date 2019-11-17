package strategies.company;

import types.Contract;
import types.Proposal;

public class SimpleCompanyStrategy extends CompanyStrategy {
    @Override
    public Contract initialContract() {
        return null;
    }

    @Override
    public Proposal makeProposal(int technicians, String message) {
        return null;
    }
}
