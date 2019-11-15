package agents;

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
import utils.Logger;

public class Company extends Agent {

    private String serviceName = "Company_";
    private String serviceType = "company";  

    private PaymentStrategy paymentStrategy;
    private AllocStrategy allocStrategy;

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


    public Company(String companyName, int paymentStrategyParam, int allocStrategyParam) {
        serviceName += companyName; 
        savePaymentStrategy(paymentStrategyParam);
        saveAllocStrategy(allocStrategyParam);
    }

    private void savePaymentStrategy(int paymentStrategyParam) {
        switch(paymentStrategyParam) {
            case 1:
            this.paymentStrategy = new PaymentStrategy1();
                break;
            case 2:
            this.paymentStrategy = new PaymentStrategy2();
                break;
            default:
            Logger.ERROR(this.getLocalName(), "Wrong company payment strategy");
        }
    }

    private void saveAllocStrategy(int allocStrategyParam) {
        switch(allocStrategyParam) {
            case 1:
            this.allocStrategy = new AllocStrategy1();
                break;
            case 2:
            this.allocStrategy = new AllocStrategy2();
                break;
            default:
            Logger.ERROR(this.getLocalName(), "Wrong company allocation strategy");
        }
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
            DFAgentDescription[] results = DFService.search(this, template);
        } catch (FIPAException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

        //this.addBehaviour();

    } 
}
