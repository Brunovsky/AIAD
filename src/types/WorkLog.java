package types;

import agents.Technician;
import agents.Technician.State;
import simulation.World;

/**
 * Internal data of Technician
 */
public class WorkLog {
    public final Technician technician;
    public final Contract contract;
    public final int day;

    public final State state;
    public final int jobs;
    public final double cut;

    public WorkLog(Technician technician, Contract contract, int jobs, double cut) {
        this.technician = technician;
        this.contract = contract;  // may be null, when unemployed

        this.day = World.get().getDay();

        this.state = technician.getWorkState();
        this.jobs = jobs;
        this.cut = cut;
    }

    public boolean working() {
        return state == State.WORKING;
    }

    public boolean employed() {
        return state == State.UNEMPLOYED;
    }

    public boolean atHomeStation() {
        return contract == null || contract.station.equals(technician.getHomeStation());
    }
}
