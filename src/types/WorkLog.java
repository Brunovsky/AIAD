package types;

import static agents.Technician.State.*;

import agents.Technician;
import agents.Technician.State;
import jade.core.AID;
import simulation.World;

public class WorkLog {
    private final Technician technician;

    public final int day;
    public final AID company;
    public final AID station;

    public final State state;
    public final int jobs;
    public final double salary;
    public final double cut;

    public WorkLog(Technician technician, int jobs, double cut) {
        this.technician = technician;

        this.day = World.get().getDay();  // get this from world
        this.company = technician.getCompany();
        this.station = technician.getStation();

        this.state = technician.getWorkState();
        this.jobs = jobs;
        this.salary = technician.getSalary();
        this.cut = cut;

        assert state != UNEMPLOYED || (company == null && jobs == 0 && salary == 0 && cut == 0);
    }

    public boolean working() {
        return state == State.WORKING;
    }

    public boolean moving() {
        return state == State.MOVING;
    }

    public boolean employed() {
        return state == State.UNEMPLOYED;
    }

    public boolean atHomeStation() {
        return technician.getHomeStation().equals(station);
    }
}
