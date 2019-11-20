package types;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import utils.MalfunctionType;

public class Proposal {
    public final AID company;
    public int easy = 0, medium = 0, hard = 0;
    public double easyPrice = 0.0, mediumPrice = 0.0, hardPrice = 0.0;

    public Proposal(AID company) {
        this.company = company;
    }

    public int totalJobs() {
        return easy + medium + hard;
    }

    public double totalEarnings() {
        return easy * easyPrice + medium * mediumPrice + hard * hardPrice;
    }

    public int get(MalfunctionType type) {
        switch (type) {
        case EASY:
            return easy;
        case MEDIUM:
            return medium;
        case HARD:
            return hard;
        }
        return -1;
    }

    public void add(MalfunctionType type, int num) {
        switch (type) {
        case EASY:
            easy += num;
            break;
        case MEDIUM:
            medium += num;
            break;
        case HARD:
            hard += num;
            break;
        }
    }

    public double getPrice(MalfunctionType type) {
        switch (type) {
        case EASY:
            return easyPrice;
        case MEDIUM:
            return mediumPrice;
        case HARD:
            return hardPrice;
        }
        return -1;
    }

    public String make() {
        return String.format("%d:%f:%d:%f:%d:%f", easy, easyPrice, medium, mediumPrice, hard,
                             hardPrice);
    }

    public static Proposal from(AID company, ACLMessage message) {
        String[] parts = message.getContent().split(":");
        Proposal proposal = new Proposal(company);
        proposal.easy = Integer.parseInt(parts[0]);
        proposal.easyPrice = Double.parseDouble(parts[1]);
        proposal.medium = Integer.parseInt(parts[2]);
        proposal.mediumPrice = Double.parseDouble(parts[3]);
        proposal.hard = Integer.parseInt(parts[4]);
        proposal.hardPrice = Double.parseDouble(parts[5]);
        return proposal;
    }

    @Override
    public String toString() {
        return String.format("%d %f %d %f %d %f", easy, easyPrice, medium, mediumPrice, hard,
                             hardPrice);
    }
}
