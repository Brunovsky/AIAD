package agentbehaviours.client;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class QueryListeningBehaviour extends OneShotBehaviour {
    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

    @Override
    public void action() {
        ACLMessage message = myAgent.receive(mt);
        while (message == null) {
            block();
            message = myAgent.receive(mt);
        }
        String content = message.getContent();
        // ^ list of repair requests being attended the next day.
    }
}
