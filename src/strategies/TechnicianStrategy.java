package strategies;

import agents.Technician;
import types.Contract;

public abstract class TechnicianStrategy {
    protected Technician technician = null;

    public void setTechnician(Technician technician) {
        this.technician = technician;
    }

    /**
     * Decide whether the technician's contract-seeking behaviour should be run today.
     * Return true to run the behaviour.
     */
    public abstract boolean lookForContracts();

    /**
     * Return a new Contract to propose as the technician's renewal contract.
     */
    public abstract Contract renewalContract();
}
