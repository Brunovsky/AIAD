package agents;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
    private Callback callback;

    public Client(Location location, MalfunctionType malfunctionType, double requestSendTime,
                  ClientType clientType, Callback callback) {
        this.location = location;
        this.malfunctionType = malfunctionType;
        this.requestSendTime = requestSendTime;
        this.clientType = clientType;
        this.callback = callback;
    }

    @Override
    protected void setup() {
        Logger.info(getLocalName(), "Setup Client Agent");
        String serviceType = Constants.SERVICE_TYPE;

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

            Logger.info(getLocalName(), "Starting Contract with Technicians...");
            this.addBehaviour(new RequestRepair(results));
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        callback.run();
    }

    public boolean compareTechnicianMessages(TechnicianMessage msg1, TechnicianMessage msg2) {
        Random rng = ThreadLocalRandom.current();
        switch (clientType) {
        case REASONABLE_UNAVAILABLE:
            //  TODO
        case SELFISH_AVAILABLE:
            //  TODO
        case SELFISH_UNAVAILABLE:
            //  TODO
        case URGENT_AVAILABLE:
            //  TODO
        default:
            return rng.nextBoolean();
        }
        // return true if msg1 it's better than msg2
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

    public interface Callback { public void run(); }
}
