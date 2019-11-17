package strategies.company;

import agents.Company;
import types.Contract;
import types.Proposal;

public abstract class CompanyStrategy {
    protected Company company;

    public enum Performance { VERY_GOOD, GOOD, NEUTRAL, BAD, VERY_BAD }

    public void setCompany(Company company) {
        this.company = company;
    }

    public abstract Contract initialContract();

    public abstract boolean acceptContractOffer(Contract contract);

    // possibility: counter-offer

    public abstract Proposal makeProposal(int technicians, String message);

    public abstract Performance evaluatePerformance();
}
