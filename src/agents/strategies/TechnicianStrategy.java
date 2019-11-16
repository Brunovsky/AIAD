package agents.strategies;

import agents.Technician;
import types.Contract;

public abstract class TechnicianStrategy {
    protected Technician technician = null;

    public void setTechnician(Technician technician) {
        this.technician = technician;
    }

    /**
     * Compare two job offers put forward by two (not necessarily different) companies.
     * Return the best one.
     */
    public abstract Contract bestJobOffer(Contract cp1, Contract cp2);

    /**
     * Decide if the job offer (next contract) proposed should be accepted.
     */
    public abstract boolean acceptJobOffer(Contract cp);

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
