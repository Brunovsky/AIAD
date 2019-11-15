package agents;

import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;

import agentbehaviours.client.QueryListeningBehaviour;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Logger;

public class Client extends Agent {
    private Station station;

    public Client(Station station) {
        assert station != null;
        this.station = station;
    }

    @Override
    public void setup() {
        addBehaviour(new QueryListeningBehaviour());
    }

    public class ClientBehaviour extends CyclicBehaviour {
        private final MessageTemplate inform = MatchPerformative(ACLMessage.INFORM);

        @Override
        public void action() {
            Logger.info(getLocalName(), "Starting behaviour...");

            MessageTemplate fromStation = MatchSender(station.getAID());
            MessageTemplate mt = and(inform, fromStation);

            String repairs = "1 2 3";
            String priceAdjusts = "4 5 6";

            // send list of repairs and price adjustments.
            ACLMessage request = new ACLMessage(ACLMessage.INFORM);
            request.setContent(repairs + " ; " + priceAdjusts);
            request.addReceiver(station.getAID());
            send(request);

            // wait for reply...
            ACLMessage reply = receive(mt);

            // Remove informed malfunctions, which will be solved in the next day.
            // Process the informed prices: consider increasing/decreasing maximum prices set.

            // Investigate new malfunctions, generating new stack of repairs.
            // Define repair time for each malfunction (between 9H and 18H, say).

            Logger.info(getLocalName(), "Finished behaviour...");
        }
    }
}
