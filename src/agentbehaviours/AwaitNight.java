package agentbehaviours;

import jade.core.behaviours.OneShotBehaviour;
import simulation.God;

public class AwaitNight extends OneShotBehaviour {
    private static final long serialVersionUID = 1447889012925933759L;

    @Override
    public void action() {
        God.get().awaitNight();
    }
}
