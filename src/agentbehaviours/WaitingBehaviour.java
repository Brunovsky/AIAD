package agentbehaviours;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public abstract class WaitingBehaviour extends Behaviour {
    private static final long serialVersionUID = -1539040812100225266L;

    private boolean finalized = false;

    public WaitingBehaviour(Agent a) {
        super(a);
    }

    protected final void finalize() {
        finalized = true;
    }

    @Override
    public boolean done() {
        return finalized;
    }
}
