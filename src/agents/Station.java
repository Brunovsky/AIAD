package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;
import static jade.lang.acl.MessageTemplate.or;

import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import utils.Logger;

public class Station extends Agent {
    private static final long serialVersionUID = 3322670743911601747L;

    private String id;
    private final Set<AID> clients;
    private final Set<AID> technicians;

    public Station(String id) {
        assert id != null;
        this.id = id;
        this.clients = ConcurrentHashMap.newKeySet(20);
        this.technicians = ConcurrentHashMap.newKeySet(20);
    }

    @Override
    public void setup() {
        Logger.info(getLocalName(), "Setup");

        registerDFService();

        addBehaviour(new SubscriptionListener(this, "client-subscription", clients));
        addBehaviour(new SubscriptionListener(this, "technician-subscription", technicians));

        // in a loop, in this order (during the night)
        addBehaviour(new FetchNewMalfunctions(this));
        addBehaviour(new AssignTechnicians(this));
    }

    private void registerDFService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName("station-" + id);
        sd.setType("station");

        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private ACLMessage prepareClientPromptMessage() {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        message.setOntology("prompt-client-malfunctions");
        for (AID client : clients) message.addReceiver(client);
        return message;
    }

    private ACLMessage prepareTechnicianInformMessage() {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        message.setOntology("inform-technician-jobs");
        for (AID technician : technicians) message.addReceiver(technician);
        return message;
    }

    class FetchNewMalfunctions extends AchieveREInitiator {
        private static final long serialVersionUID = 8662470226125479639L;

        public FetchNewMalfunctions(Agent a) {
            super(a, prepareClientPromptMessage());
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            AID client = inform.getSender();
            assert clients.contains(client);
            Object content = inform.getContent();

            // ...
            // Client answered with an inform message.
            // Read the content and update the repair requests for this client.
            // Message is REPAIRS\nADJUSTMENTS
        }
    }

    class AssignTechnicians extends AchieveREInitiator {
        private static final long serialVersionUID = -6775360046825661442L;

        AssignTechnicians(Agent a) {
            super(a, prepareTechnicianInformMessage());
        }

        private void informClient(AID client, String content) {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setOntology("inform-client-assignment");
            message.addReceiver(client);
            send(message);
        }

        @Override
        protected void handleAllResultNotifications(Vector resultNotifications) {
            Map<Long, AID> assignment;

            // ...
            // Technicians answered with an inform message, saying which repairs they will handle in
            // the next day. Sort them out so the smallest invoices get assigned and the rest
            // rejected. Ignore any invoice not respecting the maximum set price by the client.

            // once then, inform each client. Some contents will be empty (no assignments).
            // informClient(client, content)
        }
    }

    class SubscriptionListener extends CyclicBehaviour {
        private static final long serialVersionUID = 9068977292715279066L;

        private final MessageTemplate mt;
        private final String ontology;
        private final Set<AID> agentsSet;

        SubscriptionListener(Agent a, String ontology, Set<AID> agentsSet) {
            super(a);
            this.ontology = ontology;
            this.agentsSet = agentsSet;

            MessageTemplate subscribe = MatchPerformative(ACLMessage.SUBSCRIBE);
            MessageTemplate unsubscribe = MatchPerformative(ACLMessage.CANCEL);
            MessageTemplate onto = MatchOntology(ontology);
            this.mt = and(or(subscribe, unsubscribe), onto);
        }

        @Override
        public void action() {
            ACLMessage message = receive(mt);
            if (message == null) return;

            if (message.getPerformative() == ACLMessage.SUBSCRIBE) {
                this.agentsSet.add(message.getSender());
            } else {
                this.agentsSet.remove(message.getSender());
            }

            message.createReply();
            message.setPerformative(ACLMessage.CONFIRM);
            message.setOntology(ontology);
            send(message);
        }
    }
}
