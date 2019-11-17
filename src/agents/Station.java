package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;
import static jade.lang.acl.MessageTemplate.or;
import static message.Messages.parseClientAdjustmentMessage;
import static message.Messages.parseClientRequestMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
import types.Repair;
import utils.Logger;

public class Station extends Agent {
    private static final long serialVersionUID = 3322670743911601747L;
    private static final String clientSub = "client-station-subscription";
    private static final String companySub = "company-station-subscription";
    private static final String technicianSub = "technician-station-subscription";

    private final String id;
    private final Set<AID> clients;
    private final Set<AID> companies;
    private HashMap<AID, HashMap<Integer, Repair>> repairsQueue;

    public Station(String id) {
        assert id != null;
        this.id = id;
        this.clients = new HashSet<>();
        this.companies = new HashSet<>();
        this.repairsQueue = new HashMap<>();

        // TODO LOGIC: replace String with a proper data structure for state tracking
    }

    @Override
    protected void setup() {
        Logger.info(getLocalName(), "Setup " + id);

        registerDFService();

        addBehaviour(new SubscriptionListener(this, clientSub, clients));
        addBehaviour(new SubscriptionListener(this, companySub, companies));

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
        for (AID client : clients) message.addReceiver(client);
        return message;
    }

    private ACLMessage prepareCompanyQueryMessage() {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        message.setOntology("inform-company-jobs");
        for (AID company : companies) message.addReceiver(company);
        return message;
    }

    // ***** BEHAVIOURS

    private class FetchNewMalfunctions extends AchieveREInitiator {
        private static final long serialVersionUID = 8662470226125479639L;

        public FetchNewMalfunctions(Agent a) {
            super(a, prepareClientPromptMessage());  // Protocol A
        }

        @Override  // Protocol B
        protected void handleInform(ACLMessage inform) {
            AID client = inform.getSender();
            assert clients.contains(client);
            String content = inform.getContent();

            HashMap<Integer, Double> newAdjustments = parseClientAdjustmentMessage(content);
            if (newAdjustments.size() != 0 && repairsQueue.containsKey(client)) {
                HashMap<Integer, Repair> clientUnresolvedRepairs = repairsQueue.get(client);
                Iterator it = newAdjustments.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    clientUnresolvedRepairs.get(pair.getKey()).setPrice((Double) pair.getValue());
                }
            }

            HashMap<Integer, Repair> newRequests = parseClientRequestMessage(content);
            if (repairsQueue.containsKey(client)) {
                repairsQueue.get(client).putAll(newRequests);
            } else {
                repairsQueue.put(client, newRequests);
            }

            // TODO COMMS: verify this does indeed finish when all clients respond.
        }
    }

    private class AssignJobs extends AchieveREInitiator {
        private static final long serialVersionUID = -6775360046825661442L;

        AssignJobs(Agent a) {
            super(a, prepareCompanyQueryMessage());  // Protocol C
        }

        private void informCompany(AID company, String content) {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setOntology("inform-company-assignment");
            message.addReceiver(company);
            message.setContent(content);
            send(message);  // Protocol E
        }

        private void informClient(AID client, String content) {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setOntology("inform-client-assignment");
            message.addReceiver(client);
            message.setContent(content);
            send(message);  // Protocol F
        }

        @Override  // Protocol D
        protected void handleAllResultNotifications(Vector resultNotifications) {
            // TODO LOGIC: read 'resultNotifications' contents, compare with the repair cache,

            // TODO COMMS: then inform each client. Some contents will be 'empty' (no assignments).
            // DO THIS FOR EACH CLIENT

            // clientsRepairs is a HashMap<Integer, Repair>, Integer it's the clients repair id
            // HashMap<Integer, Repair> clientsRepairs = new HashMap<>();
            // String contentClient = getClientResponseMessage(clientsRepairs);

            // TODO COMMS:
            // informCompany(company, content)
            // informClient(client, content)

            // TODO COMMS: remove from repairsQueue the repairs resolved, just leave the ones with
            // no assignments
        }
    }

    private class SubscriptionListener extends CyclicBehaviour {
        private static final long serialVersionUID = 9068977292715279066L;

        private final MessageTemplate mt;
        private final Set<AID> subscribers;

        SubscriptionListener(Agent a, String ontology, Set<AID> subscribers) {
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
                this.subscribers.add(message.getSender());
            } else /* ACLMessage.CANCEL */ {
                this.subscribers.remove(message.getSender());
            }

            message.createReply();
            message.setPerformative(ACLMessage.CONFIRM);
            send(message);
        }
    }
}
