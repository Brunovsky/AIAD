package agents;

import agentbehaviours.RequestRepair;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import message.TechnicianMessage;
import simulation.World;
import utils.ClientType;
import utils.Location;
import utils.Logger;
import utils.MalfunctionType;

public class OldClient extends Agent {
    private Location location;
    private MalfunctionType malfunctionType;
    private double requestSendTime;
    private ClientType clientType;
    private Callback callback;

    public OldClient(Location location, MalfunctionType malfunctionType, double requestSendTime,
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
        String serviceType = World.get().getServiceType();

        // Use myAgent to access Client private variables

        Logger.info(getLocalName(), "Searching for services " + serviceType);

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
        switch (clientType) {
        case CLIENT_TYPE_1:
            return msg1.getRepairPrice() < msg2.getRepairPrice();
        case CLIENT_TYPE_2:
            return msg1.getStartRepairTime() < msg2.getStartRepairTime();
        case CLIENT_TYPE_3:
            return ((msg1.getStartRepairTime() - this.requestSendTime) * 0.05
                    + msg1.getRepairPrice())
                < ((msg2.getStartRepairTime() - this.requestSendTime) * 0.05
                   + msg2.getRepairPrice());
        case CLIENT_TYPE_4:
            return ((msg1.getStartRepairTime() - this.requestSendTime) + msg1.getRepairPrice())
                < ((msg2.getStartRepairTime() - this.requestSendTime) + msg2.getRepairPrice());
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

    public interface Callback { public void run(); }
}
