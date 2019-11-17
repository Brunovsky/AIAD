package types;

import jade.lang.acl.ACLMessage;

public class JobList {
    public int easy, medium, hard;

    public JobList(int easy, int medium, int hard) {
        this.easy = easy;
        this.medium = medium;
        this.hard = hard;
    }

    public String make() {
        return String.format("%d:%d:%d", easy, medium, hard);
    }

    public static JobList from(ACLMessage message) {
        String[] parts = message.getContent().split(":");
        int easy = Integer.parseInt(parts[0]);
        int medium = Integer.parseInt(parts[1]);
        int hard = Integer.parseInt(parts[2]);
        return new JobList(easy, medium, hard);
    }
}
