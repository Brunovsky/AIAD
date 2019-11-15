package agents;

import agentbehaviours.client.QueryListeningBehaviour;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class NewClient extends Agent {
    private Station station = null;

    public void setup() {
        addBehaviour(new QueryListeningBehaviour());
    }

    public class NewClientBehaviour extends OneShotBehaviour {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);

        @Override
        public void action() {
            // Investigate new malfunctions, generating new stack of repairs.
            // Define repair time for each malfunction (between 9H and 18H, say).
            String repairs = "1,2,3";
            String priceAdjusts = "4,5,6";

            // send list of repairs and price adjustments.
            ACLMessage request = new ACLMessage(ACLMessage.INFORM);
            request.setContent(repairs + priceAdjusts);
            request.addReceiver(station.getAID());
            send(request);

            ACLMessage reply = receive(mt);
        }
    }
}
