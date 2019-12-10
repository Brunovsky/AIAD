package strategies;

import agents.Company;
import jade.core.AID;
import java.util.Map;
import types.JobList;
import types.Proposal;

public abstract class CompanyStrategy {
    protected Company company = null;

    public void setCompany(Company company) {
        this.company = company;
    }

    public abstract void adjustPrices();

    public abstract Proposal makeProposal(int technicians, JobList jobList, AID station);

    public abstract void populateRow(Map<String, String> row);

    public abstract void populateRow(Map<String, String> row, AID station);

    public abstract String getName();
}
