package strategies;

import jade.core.AID;
import simulation.World;
import types.Contract;
import types.JobList;
import types.Proposal;

public class PreferEasyCompanyStrategy extends CompanyStrategy {
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
        Proposal proposal = new Proposal(company.getAID());

        if (technicians == 0) return null;

        proposal.easyPrice = 10;
        proposal.mediumPrice = 20;
        proposal.hardPrice = 30;

        if (technicians >= jobList.easy) {
            technicians -= jobList.easy;
            proposal.easy = jobList.easy;
        } else {
            proposal.easy = technicians;
            return proposal;
        }

        if (technicians >= jobList.medium) {
            technicians -= jobList.medium;
            proposal.medium = jobList.medium;
        } else {
            proposal.medium = technicians;
            return proposal;
        }

        if (technicians >= jobList.hard) {
            technicians -= jobList.hard;
            proposal.hard = jobList.hard;
        } else {
            proposal.hard = technicians;
            return proposal;
        }

        return proposal;
    }
}
