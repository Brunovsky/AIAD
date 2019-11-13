package agents;

import agentbehaviours.RequestRepair;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import message.TechnicianMessage;
import utils.ClientType;
import utils.Location;
import utils.Logger;
import utils.MalfunctionType;
import utils.constants.Constants;

public class Client extends Agent {
    private Location location;
    private MalfunctionType malfunctionType;
    private double requestSendTime;
    private ClientType clientType;

    public Client(Location location, MalfunctionType malfunctionType, double requestSendTime,
                  ClientType clientType) {
        this.location = location;
        this.malfunctionType = malfunctionType;
        this.requestSendTime = requestSendTime;
        this.clientType = clientType;
    }

    /**
     * Arguments:
     * ClientType
     * MalfunctionType
     * int
     * int
     */
    protected void setup() {
        Logger.info(getLocalName(), "Setup Client Agent");
        String serviceType = Constants.SERVICE_TYPE;

        Logger.WARN(getLocalName(), "Setup Client Agent");

        //        Object[] args = getArguments();
        //        if (args != null && args.length == 3) {
        //            this.clientType = (ClientType) args[0];
        //            this.malfunctionType = (MalfunctionType) args[1];
        //            this.location = new Location((int)args[2], (int)args[3]);
        //        } else {
        //            Logger.error(getLocalName(), "Wrong arguments");
        //            exit(0);
        //        }



        // Use myAgent to access Client private variables

        Logger.info(getLocalName(), "Searching for services of type " + serviceType);

        try {
            // Build the description used as template for the search
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType(serviceType);
            template.addServices(templateSd);

            // Constraint for search
            // SearchConstraints sc = new SearchConstraints();
            // sc.setMaxResults(new Long(100));

            // DFAgentDescription[] results = DFService.search(this, template, sc);
            DFAgentDescription[] results = DFService.search(this, template);

//            if (results.length > 0) {
//                Logger.info(getLocalName(), "Found the following " + serviceType + " services:");
//
//                for (int i = 0; i < results.length; ++i) {
//
//                    DFAgentDescription dfd = results[i];
//                    AID provider = dfd.getName();
//                    Iterator it = dfd.getAllServices();
//
//                    while (it.hasNext()) {
//                        ServiceDescription sd = (ServiceDescription) it.next();
//                        if (sd.getType().equals(serviceType)) {
//                            Logger.info(getLocalName(), "- Service \"" + sd.getName() + "\" provided by agent " + provider.getName());
//                        }
//                    }
//                }
//            } else {
//                Logger.warn(getLocalName(), "No " + serviceType + " service found");
//            }

            Logger.info(getLocalName(), "Starting Contract with Technicians...");
            this.addBehaviour(new RequestRepair(results));

        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public boolean compareTechnicianMessages(TechnicianMessage msg1, TechnicianMessage msg2) {
        switch (clientType) {
        case REASONABLE_UNAVAILABLE:
            //  TODO
        case SELFISH_AVAILABLE:
            //  TODO
        case SELFISH_UNAVAILABLE:
            //  TODO
        case URGENT_AVAILABLE:
            //  TODO
        }
        // return true if msg1 it's better than msg2
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
