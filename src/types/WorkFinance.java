package types;

import jade.lang.acl.ACLMessage;

public class WorkFinance {
    public int days = 0;
    public int jobs = 0;
    public double salary = 0.0;
    public double cut = 0.0;
    public double earned = 0.0;

    public WorkFinance() {}

    public WorkFinance(int days) {
        this.days = 1;
    }

    public WorkFinance(int days, int jobs, double salary, double cut, double earned) {
        this.days = days;
        this.jobs = jobs;
        this.salary = salary;
        this.cut = cut;
        this.earned = earned;
    }

    public void add(WorkFinance finance) {
        days += finance.days;
        jobs += finance.jobs;
        salary += finance.salary;
        cut += finance.cut;
        earned += finance.cut;
    }

    public static WorkFinance empty() {
        WorkFinance finance = new WorkFinance();
        finance.days = 1;
        return finance;
    }

    public String make() {
        return String.format("%d:%d:%f:%f:%f", days, jobs, salary, cut, earned);
    }

    public static WorkFinance from(ACLMessage message) {
        String[] parts = message.getContent().split(":");
        WorkFinance finance = new WorkFinance();
        finance.days = Integer.parseInt(parts[0]);
        finance.jobs = Integer.parseInt(parts[1]);
        finance.salary = Double.parseDouble(parts[2]);
        finance.cut = Double.parseDouble(parts[3]);
        finance.earned = Double.parseDouble(parts[4]);
        return finance;
    }
}
