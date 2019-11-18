package strategies;

import jade.core.AID;
import simulation.World;
import types.Contract;
import types.JobList;
import types.Proposal;

public class SimpleCompanyStrategy extends CompanyStrategy {
    @Override
    public Contract initialContract(AID technician, AID station) {
        String stationName = station.getLocalName();
        int day = World.get().getDay();
        int start = day, end = day + 30;
        double salary = 20;
        double percentage = 0.15;
        return new Contract(company.getAID(), technician, stationName, salary, percentage, start,
                            end);
    }

    @Override
    public Proposal makeProposal(int technicians, JobList jobList) {
        // TODO STRATEGY
        return null;
    }
}
