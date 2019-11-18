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
    public final WorkFinance finance;

    public WorkLog(Technician technician, Contract contract, WorkFinance finance) {
        this.technician = technician;
        this.contract = contract;  // may be null, when unemployed

        this.day = World.get().getDay();

        this.state = technician.getWorkState();
        this.finance = finance;
    }

    public WorkLog(Technician technician) {
        this(technician, null, new WorkFinance(1));
    }

    public boolean working() {
        return state == State.WORKING;
    }

    public boolean employed() {
        return state == State.UNEMPLOYED;
    }
}
