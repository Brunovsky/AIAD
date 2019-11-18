package agentbehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import simulation.God;
import utils.Logger;

public class AwaitNight extends OneShotBehaviour {
    private static final long serialVersionUID = 1447889012925933759L;

    public AwaitNight(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        Logger.info(myAgent.getLocalName(), "Waiting for night...");
        God.get().awaitNight();
    }
}
