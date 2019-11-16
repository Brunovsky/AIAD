package types;

import jade.core.AID;

/**
 * Internal data of Technician
 * Internal data of Company
 * Data negotiated between Technician and Company
 */
public class Contract {
    public final AID company;
    public final AID technician;
    public final AID station;

    public final double salary;
    public final double percentage;
    public final int start, end;

    public Contract(AID company, AID technician, AID station, double salary, double percentage,
                    int start, int end) {
        this.company = company;
        this.technician = technician;
        this.station = station;

        this.salary = salary;
        this.percentage = percentage;
        this.start = start;
        this.end = end;
    }

    public Contract renew(int nextStart, int nextEnd) {
        return new Contract(company, technician, station, salary, percentage, nextStart, nextEnd);
    }

    public int numDays() {
        return end - start;
    }
}
