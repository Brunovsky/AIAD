package agents;

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
import strategies.company.AllocStrategy1;
import strategies.company.AllocStrategy2;
import strategies.company.PaymentStrategy;
import strategies.company.PaymentStrategy1;
import strategies.company.PaymentStrategy2;
import strategies.company.TechDistributionStrategy;
import utils.Logger;

public class Company extends Agent {

    private String serviceName = "Company_";
    private String serviceType = "companyservice";  

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

    public Company(String companyName, TechDistributionStrategy techDistributionStrategy, PaymentStrategy paymentStrategyParam, AllocStrategy allocStrategyParam) {
        technicians = new HashMap<AID, String>();
        stations = new HashMap<AID, String>();

        serviceName += companyName;
        
        this.techDistributionStrategy = techDistributionStrategy;
        this.paymentStrategy = paymentStrategy; 
        this.allocStrategy = allocStrategy;
    }

    @Override
    protected void setup(){

        Logger.info(getLocalName(), "Setup Company Agent");

        //  Register Company in yellow pages
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName(serviceName);                                               // Necessary?????
            sd.setType(serviceType);

            dfd.addServices(sd);

            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        //  Search for Stations and notify them
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType("repairs-station");
            template.addServices(templateSd);

            // DFAgentDescription[] results = DFService.search(this, template, sc);
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
    
    class SubscriptionListener extends CyclicBehaviour {
        private static final long serialVersionUID = 9068977292715279066L;

        private final MessageTemplate mt;
        private final String ontology;
        private final HashMap<AID, String> agentsMap;

        SubscriptionListener(Agent a, String ontology, HashMap<AID, String> agentsMap) {
            super(a);
            this.ontology = ontology;
            this.agentsMap = agentsMap;

            MessageTemplate subscribe = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
            MessageTemplate unsubscribe = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
            MessageTemplate tp = MessageTemplate.MatchOntology(ontology);
            this.mt = MessageTemplate.and(MessageTemplate.or(subscribe, unsubscribe), tp);
        }

        @Override
        public void action() {  // Deviamos por block() em vez de tar sempre a retornar null -> performance
            ACLMessage message = receive(mt);
            if (message == null) return;

            if (message.getPerformative() == ACLMessage.SUBSCRIBE) {
                this.agentsMap.put(message.getSender(), "");
            } else {
                this.agentsMap.remove(message.getSender());
            }

            message.createReply();
            message.setPerformative(ACLMessage.CONFIRM);
            message.setContent(salary + " ; " + contractBonus + " ; " + contractTime);
            send(message);
        }
    }
}
