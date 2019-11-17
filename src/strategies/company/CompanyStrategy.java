package strategies.company;

import agents.Company;
import types.Contract;
import types.Proposal;

public abstract class CompanyStrategy {
    private final Company company;

    public CompanyStrategy(Company company) {
        this.company = company;
    }

    public abstract Contract initialContract();

    public abstract Proposal makeProposal(int technicians, String message);
}
