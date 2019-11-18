package agentbehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import simulation.God;
import utils.Logger;

public class AwaitDayBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = -6431251470507539025L;

    public AwaitDayBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        Logger.info(myAgent.getLocalName(), "Waiting for day...");

        God.get().awaitDay(myAgent.getAID());

        MessageTemplate mt = MessageTemplate.MatchOntology("simulation-day");
        ACLMessage message = myAgent.receive(mt);
        while (message == null) {
            block();
            message = myAgent.receive(mt);
        }
    }
}
