package agents;

import agentbehaviours.WaitRepairRequests;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.lang.acl.ACLMessage;
import message.ClientMessage;
import message.TechnicianMessage;
import utils.Location;
import utils.Logger;

public class Technician extends Agent {

    private Location location;
    private int repairPrice;

    protected void setup() {
        Logger.info(getLocalName(), "Setup Technician Agent");

        String serviceName = "TechRepairs";
        String serviceType = "tech-repairs";

        // Read the name of the service to register as an argument
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            serviceName = (String) args[0];
        }

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

    public TechnicianMessage handleReceivedClientCfp(ClientMessage contentObject) {
        TechnicianMessage response = new TechnicianMessage();

        // TODO: make response here

        return response; // or return null
    }

    public boolean handleReceivedClientAcceptProposal(ACLMessage cfp, ACLMessage propose){
        //  TODO:
        // Handle Accept Proposal

        // Perform action

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
}

