package agentbehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import simulation.God;

public class AwaitDay extends OneShotBehaviour {
    private static final long serialVersionUID = -6431251470507539025L;

    public AwaitDay(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        God.get().awaitDay();
    }
}
