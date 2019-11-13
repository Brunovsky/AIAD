package agents;

import agentbehaviours.RequestRepair;
import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.util.leap.Iterator;
import message.TechnicianMessage;
import utils.Location;
import utils.Logger;
import utils.MalfunctionType;
import utils.ClientType;

import static java.lang.System.exit;

public abstract class Client extends Agent {

    private Location location;
    private MalfunctionType malfunctionType;
    private double requestSendTime;
    private ClientType clientType;

    protected void setup() {
        Logger.info(getLocalName(), "Setup Client Agent");
        String serviceType = "tech-repairs";

        Logger.WARN(getLocalName(), "Setup Client Agent");

        String[] args = (String[]) getArguments();
        if (args != null && args.length == 3) {
            switch(args[0]) {
                case "RU":
                    clientType = ClientType.REASONABLE_UNAVAILABLE;
                case "SA":
                    clientType = ClientType.SELFISH_AVAILABLE;
                case "SU":
                    clientType = ClientType.SELFISH_UNAVAILABLE;
                case "UA":
                    clientType = ClientType.URGENT_AVAILABLE;
            }
            location = new Location(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        } else {
            Logger.error(getLocalName(), "Wrong arguments");
            exit(0);
        }

        // Use myAgent to access Client private variables

        Logger.info(getLocalName(), "Searching for services of type " + serviceType);

        try {
            // Build the description used as template for the search
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType(serviceType);
            template.addServices(templateSd);

            // Constraint for search
            //SearchConstraints sc = new SearchConstraints();
            //sc.setMaxResults(new Long(100));

            //DFAgentDescription[] results = DFService.search(this, template, sc);
            DFAgentDescription[] results = DFService.search(this, template);

            if (results.length > 0) {
                Logger.info(getLocalName(), "Found the following " + serviceType + " services:");

                for (int i = 0; i < results.length; ++i) {

                    DFAgentDescription dfd = results[i];
                    AID provider = dfd.getName();
                    Iterator it = dfd.getAllServices();

                    while (it.hasNext()) {
                        ServiceDescription sd = (ServiceDescription) it.next();
                        if (sd.getType().equals(serviceType)) {
                            Logger.info(getLocalName(), "- Service \"" + sd.getName() + "\" provided by agent " + provider.getName());
                        }
                    }
                }
            } else {
                Logger.warn(getLocalName(), "No " + serviceType + " service found");
            }

            Logger.info(getLocalName(), "Starting Contract with Technicians...");
            this.addBehaviour(new RequestRepair(results));
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }


    public boolean compareTechnicianMessages(TechnicianMessage msg1, TechnicianMessage msg2){
        switch(clientType) {    
            case REASONABLE_UNAVAILABLE:
            //  TODO
            case SELFISH_AVAILABLE:
            //  TODO
            case SELFISH_UNAVAILABLE:
            //  TODO
            case URGENT_AVAILABLE:
            //  TODO
        }

        return true;
    }

    public Location getLocation() {
        return location;
    }

    public MalfunctionType getMalfunctionType() {
        return malfunctionType;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public double getRequestSendTime() {
        return requestSendTime;
    }
}

