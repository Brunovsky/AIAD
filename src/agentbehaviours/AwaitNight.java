package agentbehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import simulation.God;

public class AwaitNight extends OneShotBehaviour {
    private static final long serialVersionUID = 1447889012925933759L;

    public AwaitNight(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        God.get().awaitNight();
    }
}
