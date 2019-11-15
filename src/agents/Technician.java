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
import simulation.World;
import utils.Location;
import utils.Logger;
import utils.MalfunctionType;
import utils.RepairSlot;
import utils.TechnicianType;
import utils.TimeBoard;

public class Technician extends Agent {
    private Location location;
    TimeBoard timeBoard;
    TechnicianType technicianType;

    public Technician(Location location, TechnicianType technicianType) {
        this.location = location;
        this.timeBoard = new TimeBoard();
        this.technicianType = technicianType;
    }

    protected void setup() {
        timeBoard = new TimeBoard();

        Logger.info(getLocalName(), "Setup Technician Agent");

        String serviceName = World.get().getServiceName();
        String serviceType = World.get().getServiceType();

        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName(serviceName);
            sd.setType(serviceType);

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

            double startSlotTime = this.timeBoard.getNextAvailableSlotStartTime(
                receivedClientMessage.getRequestSendTime());
            double repairPrice = getRepairPrice(receivedClientMessage, startSlotTime);

            if (repairPrice == 0) {
                // It won't send a proposal message to client because it isn't worth the trip
                return null;
            }

            String clientId = cfp.getSender().getName();

            RepairSlot repairSlot = new RepairSlot(receivedClientMessage.getType(), startSlotTime,
                                                   receivedClientMessage.getLocation(), repairPrice,
                                                   clientId, this.location);

            return repairSlot;
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean handleReceivedClientAcceptProposal(RepairSlot slot) {
        this.timeBoard.addRepairSlot(slot);
        return true;
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        Logger.warn(getLocalName(), "Technician Terminated!");
    }

    public Location getLocation() {
        return location;
    }

    public double getRepairPrice(ClientMessage receivedClientMessage, double startSlotTime) {
        MalfunctionType type = receivedClientMessage.getType();
        double distance = Location.distance(receivedClientMessage.getLocation(), this.location);
        double distancePrice = World.get().travelCost(distance);
        double malfPrice = World.get().malfunctionPrice(type);
        double sentTime = receivedClientMessage.getRequestSendTime();

        switch (this.technicianType) {
        case TECHNICIAN_TYPE_1: {
            // Cares about distance
            // Malfunction price depends on type
            return distancePrice + malfPrice;
        }
        case TECHNICIAN_TYPE_2: {
            // Doesn't care about distance
            // Malfunction price depends on type
            // If the distance is not worth he refuses
            if (distancePrice < malfPrice) {
                return malfPrice;
            } else {
                return 0;
            }
        }
        case TECHNICIAN_TYPE_3: {
            // Cares about distance
            // Malfunction price depends on type
            // Cares about the time between client request time and technician repair proposed time
            return distancePrice + malfPrice + Math.exp(-0.004 * (startSlotTime - sentTime)) * 20;
        }
        case TECHNICIAN_TYPE_4: {
            // Doesn't care about distance
            // Malfunction price depends on type
            // If the distance is not worth he refuses
            // Cares about the time between client request time and technician repair proposed time
            double fixPrice = malfPrice + Math.exp(-0.004 * (startSlotTime - sentTime)) * 20;
            if (distancePrice < fixPrice) {
                return fixPrice;
            } else {
                return 0;
            }
        }
        default:
            break;
        }

        return 0;
    }

    public TimeBoard getTimeBoard() {
        return timeBoard;
    }
}

/**
        case TECHNICIAN_TYPE_7:
            // Cares about distance
            // Malfunction price is default
            // Cares about the time between client request time and technician repair proposed time
            return distancePrice + PRICE_DEFAULT_MALFUNCTION
                + Math.exp(-0.004 * (startSlotTime - receivedClientMessage.getRequestSendTime()))
                      * 20;

        case TECHNICIAN_TYPE_8:
            // Doesn't care about distance
            // If the distance is not worth he refuses
            // Malfunction price is default
            // Cares about the time between client request time and technician repair proposed time
            fixPrice = PRICE_DEFAULT_MALFUNCTION
                       + Math.exp(-0.004
                                  * (startSlotTime - receivedClientMessage.getRequestSendTime()))
                             * 20;
            if (distancePrice < fixPrice) {
                return fixPrice;
            } else {
                return 0;
            }
 */
