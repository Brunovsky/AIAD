package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;
import static jade.lang.acl.MessageTemplate.or;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashMap;
import java.util.Map;
import strategies.company.AllocStrategy;
import strategies.company.PaymentStrategy;
import strategies.company.TechDistributionStrategy;
import utils.Logger;

public class Company extends Agent {
    private static final String serviceName = "Company_";
    private static final String serviceType = "companyservice";
    private static final String companyOnto = "company-subscription";

    private final String id;
    private HashMap<AID, String> technicians;
    private HashMap<AID, String> stations;

    private PaymentStrategy paymentStrategy;
    private AllocStrategy allocStrategy;
    private TechDistributionStrategy techDistributionStrategy;

    /**
     * € Technicians receive per month
     */
    private double salary;

    /**
     * € Technicians receive per Repair
     */
    public double repairCommission;

    /**
     * € Technicians receive per joining the Company
     */
    private double contractBonus;

    /**
     * Time Technicians have to be in the Company
     */
    private double contractTime;

    public Company(String id, String companyName, TechDistributionStrategy techDistributionStrategy,
                   PaymentStrategy paymentStrategyParam, AllocStrategy allocStrategyParam) {
        assert id != null && companyName != null;
        technicians = new HashMap<AID, String>();
        stations = new HashMap<AID, String>();

        this.id = id;
        this.techDistributionStrategy = techDistributionStrategy;
        this.paymentStrategy = paymentStrategy;
        this.allocStrategy = allocStrategy;
    }

    @Override
    protected void setup() {
        Logger.info(getLocalName(), "Setup");

        registerDFService();
        findStations();

        addBehaviour(new SubscriptionListener(this, companyOnto, technicians));
    }

    private void registerDFService() {
        //  Register Company in yellow pages
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName("company" + id);  // Necessary?????
            sd.setType("company");

            dfd.addServices(sd);

            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void findStations() {
        //  Search for Stations and notify them
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType("station");
        template.addServices(templateSd);

        try {
            DFAgentDescription[] stations = DFService.search(this, template);
            for (DFAgentDescription stationDescriptor : stations) {
                this.stations.put(stationDescriptor.getName(), "");
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        Logger.warn(getLocalName(), "Company Terminated!");
    }

    private class CompanyNight extends Behaviour {
        private static final long serialVersionUID = 6059838822925652797L;

        private void replyStation(ACLMessage message) {
            AID station = message.getSender();
            assert stations.containsKey(station);

            // TODO LOGIC: Generate all proposals
            String proposals = new String();

            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(proposals);
            send(reply);
        }

        private void informTechnicians(ACLMessage message) {
            AID station = message.getSender();
            assert stations.containsKey(station);

            String accepted = message.getContent();
            // TODO LOGIC: Process all accepted proposals

            // TODO COMMS: repeat for each technician
            {
                // TODO LOGIC: find payment for technician.
                String payment = "";
                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                inform.setOntology("client-payment");
                inform.setContent(payment);
                send(inform);
            }
        }

        @Override
        public void action() {
            MessageTemplate acl, onto;

            // TODO COMMS: repeat for each station.
            {
                onto = MatchOntology("inform-company-jobs");
                acl = MatchPerformative(ACLMessage.REQUEST);
                ACLMessage request = receive(and(onto, acl));  // Protocol A
                while (request == null) {
                    block();
                    request = receive(and(onto, acl));
                }
                replyStation(request);  // Protocol B
            }

            // TODO COMMS: repeat for each station.
            {
                onto = MatchOntology("inform-company-assignment");
                acl = MatchPerformative(ACLMessage.INFORM);
                ACLMessage inform = receive(and(onto, acl));  // Protocol C
                while (inform == null) {
                    block();
                    inform = receive(and(onto, acl));
                }
                informTechnicians(inform);  // Protocol D
            }
        }

        @Override
        public boolean done() {
            return false;
            // return true on the final day
        }
    }

    private class SubscriptionListener extends CyclicBehaviour {
        private static final long serialVersionUID = 9068977292715279066L;

        private final MessageTemplate mt;
        private final Map<AID, String> subscribers;

        SubscriptionListener(Agent a, String ontology, Map<AID, String> subscribers) {
            super(a);
            this.subscribers = subscribers;

            MessageTemplate subscribe = MatchPerformative(ACLMessage.SUBSCRIBE);
            MessageTemplate unsubscribe = MatchPerformative(ACLMessage.CANCEL);
            MessageTemplate onto = MatchOntology(ontology);
            this.mt = and(or(subscribe, unsubscribe), onto);
        }

        @Override
        public void action() {
            ACLMessage message = myAgent.receive(mt);
            while (message == null) {
                block();
                return;
            }

            if (message.getPerformative() == ACLMessage.SUBSCRIBE) {
                this.subscribers.putIfAbsent(message.getSender(), new String());
            } else /* ACLMessage.CANCEL */ {
                this.subscribers.remove(message.getSender());
            }

            message.createReply();
            message.setPerformative(ACLMessage.CONFIRM);
            myAgent.send(message);
        }
    }
}
