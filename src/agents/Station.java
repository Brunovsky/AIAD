package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;
import static jade.lang.acl.MessageTemplate.or;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

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

    private final String id;
    private final HashMap<AID, Integer> clients;
    private final HashMap<AID, Integer> technicians;

    public Station(String id) {
        assert id != null;
        this.id = id;
        this.clients = new HashMap<>();
        this.technicians = new HashMap<>();

        // TODO LOGIC: replace Integer with a proper data structure for state tracking
        // TODO LOGIC: technicians probably don't need state tracking, but clients do
    }

    @Override
    protected void setup() {
        Logger.info(getLocalName(), "Setup");

        registerDFService();

        addBehaviour(new SubscriptionListener(this, "client-subscription", clients));
        addBehaviour(new SubscriptionListener(this, "technician-subscription", technicians));

        // TODO COMMS: in a loop, in this order (during the night)
        addBehaviour(new FetchNewMalfunctions(this));
        addBehaviour(new AssignJobs(this));
    }

    @Override
    protected void takeDown() {
        Logger.warn(getLocalName(), "Station Terminated!");
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
        for (AID client : clients.keySet()) message.addReceiver(client);
        return message;
    }

    private ACLMessage prepareTechnicianInformMessage() {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        message.setOntology("inform-technician-jobs");
        for (AID technician : technicians.keySet()) message.addReceiver(technician);
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
            assert clients.containsKey(client);
            String content = inform.getContent();

            // TODO LOGIC: read 'content' and update the repairs cache.
            // Content is "REPAIRS\nADJUSTMENTS" but this can be changed.
            // TODO COMMS: verify this does indeed finish when all clients respond.
        }
    }

    class AssignJobs extends AchieveREInitiator {
        private static final long serialVersionUID = -6775360046825661442L;

        AssignJobs(Agent a) {
            super(a, prepareTechnicianInformMessage());
        }

        private void informClient(AID client, String content) {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setOntology("inform-client-assignment");
            message.addReceiver(client);
            send(message);
        }

        private void informTechnician(AID technician, String content) {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setOntology("inform-technician-assignment");
            message.addReceiver(technician);
            send(message);
        }

        @Override
        protected void handleAllResultNotifications(Vector resultNotifications) {
            Map<Long, AID> assignment;

            // TODO LOGIC: read 'resultNotifications' contents, compare with the repair cache,
            // TODO LOGIC: and assign technicians to jobs.

            // TODO COMMS: then inform each client. Some contents will be empty (no assignments).
            // informClient(client, content)

            // TODO COMMS: then inform each technician. Some contents will be empty.
            // informTechnician(technician, content)
        }
    }

    class SubscriptionListener extends CyclicBehaviour {
        private static final long serialVersionUID = 9068977292715279066L;

        private final MessageTemplate mt;
        private final Map<AID, Integer> subscribers;

        SubscriptionListener(Agent a, String ontology, Map<AID, Integer> subscribers) {
            super(a);
            this.subscribers = subscribers;

            MessageTemplate subscribe = MatchPerformative(ACLMessage.SUBSCRIBE);
            MessageTemplate unsubscribe = MatchPerformative(ACLMessage.CANCEL);
            MessageTemplate onto = MatchOntology(ontology);
            this.mt = and(or(subscribe, unsubscribe), onto);
        }

        @Override
        public void action() {
            ACLMessage message = receive(mt);
            while (message == null) {
                block();
                return;
            }

            if (message.getPerformative() == ACLMessage.SUBSCRIBE) {
                this.subscribers.putIfAbsent(message.getSender(), 0);
            } else /* ACLMessage.CANCEL */ {
                this.subscribers.remove(message.getSender());
            }

            message.createReply();
            message.setPerformative(ACLMessage.CONFIRM);
            send(message);
        }
    }
}
