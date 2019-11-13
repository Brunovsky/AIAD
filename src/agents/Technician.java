package agents;

import agentbehaviours.WaitRepairRequests;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import message.ClientMessage;
import message.TechnicianMessage;
import utils.*;


import static java.lang.System.exit;

public class Technician extends Agent {

    private Location location;
    private double repairPriceMultiplier; //p.e. 1.2 ou 1.5 - definined in arguments
    TimeBoard timeBoard;
    FinancialAccount financialAccount;

    protected void setup() {
        
        timeBoard = new TimeBoard();

        Logger.info(getLocalName(), "Setup Technician Agent");

        String serviceName = "TechRepairs";
        String serviceType = "tech-repairs";

        // Read the name of the service to register as an argument
        String[] args = (String[]) getArguments();
        if (args != null && args.length == 2) {
            location = new Location(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } else {
            Logger.error(getLocalName(), "Wrong arguments");
            exit(0);
        }

        financialAccount = new FinancialAccount();

        Logger.info(getLocalName(), "Registering service \"" + serviceName + "\" of type "+serviceType);

        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName(serviceName);
            sd.setType(serviceType);

            // Agents that want to use this service need to "know" the weather-forecast-ontology
//            sd.addOntologies("tech-repairs-ontology");
            // Agents that want to use this service need to "speak" the FIPA-SL language
//            sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
            //sd.addProperties(new Property("country", "Portugal"));

            dfd.addServices(sd);

            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new WaitRepairRequests());
    }

    public RepairSlot handleReceivedClientCfp(ACLMessage cfp) {
        try {
            ClientMessage receivedClientMessage = (ClientMessage) cfp.getContentObject();
            // verificar timeboard se dá ou não e retornar startSlotTime
            receivedClientMessage.getRequestSendTime();

            double startSlotTime = 0;
            double repairPrice = 0;
            String clientId = cfp.getSender().getName();


            RepairSlot repairSlot = new RepairSlot(receivedClientMessage.getType(), startSlotTime, receivedClientMessage.getLocation(), repairPrice, clientId, this.location);

            return repairSlot;
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

        return null; // or return null
    }

    public boolean handleReceivedClientAcceptProposal(ACLMessage cfp, ACLMessage propose){
        //  TODO:
        // Handle Accept Proposal

        // Perform action
        // Update account and timeboard

        return true;
    }

    /*
    Agent Termination
 */
    protected void takeDown() {

        // Removing the registration in the yellow pages
        try {

            DFService.deregister(this);
        } catch (FIPAException fe) {

            fe.printStackTrace();
        }

        Logger.warn(getLocalName(), "Terminated!");
    }

    public Location getLocation() {
        return location;
    }
}

