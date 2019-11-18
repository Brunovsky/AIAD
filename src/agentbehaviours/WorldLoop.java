package agentbehaviours;

import jade.core.behaviours.Behaviour;
import simulation.World;

public class WorldLoop extends Behaviour {
    private static final long serialVersionUID = -4829097119597629938L;

    private final Behaviour child;

    public WorldLoop(Behaviour behaviour) {
        super(behaviour.getAgent());
        this.child = behaviour;
    }

    @Override
    public void action() {
        child.action();
    }

    @Override
    public boolean done() {
        return World.get().getDay() == World.get().getNumberDays();
    }
}
