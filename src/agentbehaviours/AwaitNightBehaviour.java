package agentbehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import simulation.God;
import utils.Logger;

public class AwaitNightBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 1447889012925933759L;

    public AwaitNightBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        Logger.info(myAgent.getLocalName(), "Waiting for night...");

        God.get().awaitNight(myAgent.getAID());

        MessageTemplate mt = MessageTemplate.MatchOntology("simulation-night");
        ACLMessage message = myAgent.receive(mt);
        while (message == null) {
            block();
            message = myAgent.receive(mt);
        }
    }
}
