package agentbehaviours;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;

public class SequentialLoopBehaviour extends SequentialBehaviour {
    private static final long serialVersionUID = 5140712999282253776L;

    public SequentialLoopBehaviour(Agent a) {
        super(a);
    }

    @Override
    public int onEnd() {
        reset();
        myAgent.addBehaviour(this);
        return super.onEnd();
    }
}
