package agents;

import java.util.HashMap;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
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
    private String serviceType = "company";  

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
    private double repairCommission;
    
    /**
     * € Technicians receive per joining the Company
     */
    private double contractBonus;
    
    /**
     * Time Technicians have to be in the Company
     */
    private double contractTime;

    // TODO: Decide which ones are public to stranger Technicians

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
        

        //this.addBehaviour();

    } 
}
