package agentbehaviours;

import jade.core.behaviours.Behaviour;
import utils.Logger;

public class WorldLoop extends Behaviour {
    private static final long serialVersionUID = -4829097119597629938L;

    private final Behaviour child;

    public WorldLoop(Behaviour behaviour) {
        super(behaviour.getAgent());
        this.child = behaviour;
    }

    @Override
    public void action() {
        Logger.info(myAgent.getLocalName(), "LOOP");
        child.action();
    }

    @Override
    public boolean done() {
        return false;
    }
}
