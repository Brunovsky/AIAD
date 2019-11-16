package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;
import static jade.lang.acl.MessageTemplate.or;

import java.util.HashMap;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import strategies.company.AllocStrategy;
import strategies.company.PaymentStrategy;
import strategies.company.TechDistributionStrategy;
import utils.Logger;

public class Company extends Agent {
    private static final String serviceName = "Company_";
    private static final String serviceType = "companyservice";

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

        addBehaviour(new SubscriptionListener(this, "company-subscription", technicians));
    }

    @Override
    protected void takeDown() {
        Logger.warn(getLocalName(), "Company Terminated!");
    }

    class SubscriptionListener extends CyclicBehaviour {
        private static final long serialVersionUID = 9068977292715279066L;

        private final MessageTemplate mt;
        private final HashMap<AID, String> subscribers;

        SubscriptionListener(Agent a, String ontology, HashMap<AID, String> subscribers) {
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
                this.subscribers.put(message.getSender(), "");
            } else /* ACLMessage.CANCEL */ {
                this.subscribers.remove(message.getSender());
            }

            message.createReply();
            message.setPerformative(ACLMessage.CONFIRM);
            message.setContent(salary + " ; " + contractBonus + " ; " + contractTime);
            send(message);
        }
    }
}
