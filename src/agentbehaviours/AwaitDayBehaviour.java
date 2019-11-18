package agentbehaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import simulation.God;

public class AwaitDayBehaviour extends WaitingBehaviour {
    private static final long serialVersionUID = -6431251470507539025L;

    public AwaitDayBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        God.get().awaitDay(myAgent.getAID());

        MessageTemplate mt = MessageTemplate.MatchOntology("simulation-day");
        ACLMessage message = myAgent.receive(mt);
        if (message != null) {
            finalize();
        } else {
            block();
        }
    }
}
