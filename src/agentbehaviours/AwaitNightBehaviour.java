package agentbehaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import simulation.God;
import utils.Logger;

public class AwaitNightBehaviour extends WaitingBehaviour {
    private static final long serialVersionUID = 1447889012925933759L;

    public AwaitNightBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        Logger.white(myAgent.getLocalName(), "Waiting for night...");

        God.get().awaitNight(myAgent.getAID());

        MessageTemplate mt = MessageTemplate.MatchOntology("simulation-night");
        ACLMessage message = myAgent.receive(mt);
        if (message != null) {
            finalize();
            Logger.white(myAgent.getLocalName(), "Night arrived!");
        } else {
            block();
        }
    }
}
