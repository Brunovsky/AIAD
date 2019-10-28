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

import static java.lang.System.exit;

public abstract class Client extends Agent {

    private Location location;
    private MalfunctionType malfunctionType;

    protected void setup() {
        Logger.info(getLocalName(), "Setup Client Agent");
        String serviceType = "tech-repairs";

        Logger.WARN(getLocalName(), "Setup Client Agent");

        // TODO: get args from console

        String[] args = (String[]) getArguments();
        if (args != null && args.length == 2) {
            location = new Location(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
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


    public abstract boolean compareTechnicianMessages(TechnicianMessage msg1, TechnicianMessage msg2);

    public Location getLocation() {
        return location;
    }
}

