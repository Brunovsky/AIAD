package types;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Internal data of Technician
 * Internal data of Company
 * Data negotiated between Technician and Company
 */
public class Contract {
    public final AID company;
    public final AID technician;
    public final String station;

    public final double salary;
    public final double percentage;
    public final int start, end;

    public final WorkFinance history;

    public Contract(AID company, AID technician, String station, double salary, double percentage,
                    int start, int end) {
        this.company = company;
        this.technician = technician;
        this.station = station;

        this.salary = salary;
        this.percentage = percentage;
        this.start = start;
        this.end = end;
        this.history = new WorkFinance();
    }

    public Contract renew(int nextStart, int nextEnd) {
        return new Contract(company, technician, station, salary, percentage, nextStart, nextEnd);
    }

    public int numDays() {
        return end - start + 1;
    }

    public String make() {
        return String.format("%s:%f:%f:%d:%d", station, salary, percentage, start, end);
    }

    public static Contract from(AID company, AID technician, ACLMessage message) {
        String[] parts = message.getContent().split(":");
        String station = parts[0];
        double salary = Double.parseDouble(parts[1]);
        double percentage = Double.parseDouble(parts[2]);
        int start = Integer.parseInt(parts[3]);
        int end = Integer.parseInt(parts[4]);
        return new Contract(company, technician, station, salary, percentage, start, end);
    }
}
