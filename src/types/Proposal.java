package types;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class Proposal {
    public final AID company;
    public int easy, medium, hard;
    public double easyPrice, mediumPrice, hardPrice;

    public Proposal(AID company) {
        this.company = company;
    }

    public int totalJobs() {
        return easy + medium + hard;
    }

    public double totalEarnings() {
        return easy * easyPrice + medium * mediumPrice + hard * hardPrice;
    }

    public String make() {
        return String.format("%d=%f:%d=%f:%d=%f", easy, easyPrice, medium, mediumPrice, hard,
                             hardPrice);
    }

    public static Proposal from(AID company, ACLMessage message) {
        String[] parts = message.getContent().split(":|=");
        Proposal proposal = new Proposal(company);
        proposal.easy = Integer.parseInt(parts[0]);
        proposal.easyPrice = Double.parseDouble(parts[1]);
        proposal.medium = Integer.parseInt(parts[2]);
        proposal.mediumPrice = Double.parseDouble(parts[3]);
        proposal.hard = Integer.parseInt(parts[4]);
        proposal.hardPrice = Double.parseDouble(parts[5]);
        return proposal;
    }
}