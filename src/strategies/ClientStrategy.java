package strategies;

import java.util.Map;

import agents.Client;
import types.Repair;

public abstract class ClientStrategy {
    protected Client client = null;

    public void setClient(Client client) {
        this.client = client;
    }

    public abstract void evaluateAdjustments(Map<Integer, Repair> dayRequestRepairs);

    public abstract int generateNewRepairs(Map<Integer, Repair> dayRequestRepairs, int repairId);
}
