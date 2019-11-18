package agentbehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import simulation.God;
import utils.Logger;

public class AwaitDay extends OneShotBehaviour {
    private static final long serialVersionUID = -6431251470507539025L;

    public AwaitDay(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        Logger.info(myAgent.getLocalName(), "Waiting for day...");
        God.get().awaitDay();
    }
}
