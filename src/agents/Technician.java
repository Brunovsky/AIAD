package agents;

import static utils.constants.Constants.PRICE_DEFAULT_MALFUNCTION;
import static utils.constants.Constants.SERVICE_NAME;
import static utils.constants.Constants.SERVICE_TYPE;
import static utils.constants.Constants.calculateDistance;
import static utils.constants.Constants.getMalFunctionPrice;

import agentbehaviours.WaitRepairRequests;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import message.ClientMessage;
import utils.Location;
import utils.Logger;
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

        String serviceName = SERVICE_NAME;
        String serviceType = SERVICE_TYPE;

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
        double distancePrice = 2
                               * calculateDistance(receivedClientMessage.getLocation(),
                                                   this.location);
        double fixPrice;

        switch (this.technicianType) {
        case TECHNICIAN_TYPE_1:
            // Cares about distance
            // Malfunction price depends on type
            return distancePrice + getMalFunctionPrice(receivedClientMessage.getType());
        case TECHNICIAN_TYPE_2:
            // Cares about distance
            // Malfunction price is default
            return distancePrice + PRICE_DEFAULT_MALFUNCTION;
        case TECHNICIAN_TYPE_3:
            // Doesn't care about distance
            // Malfunction price depends on type
            // If the distance is not worth he refuses
            if (distancePrice < getMalFunctionPrice(receivedClientMessage.getType())) {
                return getMalFunctionPrice(receivedClientMessage.getType());
            } else {
                return 0;
            }
        case TECHNICIAN_TYPE_4:
            // Doesn't care about distance
            // Malfunction price is default
            // If the distance is not worth he refuses
            if (distancePrice < PRICE_DEFAULT_MALFUNCTION) {
                return PRICE_DEFAULT_MALFUNCTION;
            } else {
                return 0;
            }
        case TECHNICIAN_TYPE_5:
            // Cares about distance
            // Malfunction price depends on type
            // Cares about the time between client request time and technician repair proposed time
            return distancePrice + getMalFunctionPrice(receivedClientMessage.getType())
                + Math.exp(-0.004 * (startSlotTime - receivedClientMessage.getRequestSendTime()))
                      * 20;

        case TECHNICIAN_TYPE_6:
            // Doesn't care about distance
            // Malfunction price depends on type
            // If the distance is not worth he refuses
            // Cares about the time between client request time and technician repair proposed time
            fixPrice = getMalFunctionPrice(receivedClientMessage.getType())
                       + Math.exp(-0.004
                                  * (startSlotTime - receivedClientMessage.getRequestSendTime()))
                             * 20;
            if (distancePrice < PRICE_DEFAULT_MALFUNCTION) {
                return fixPrice;
            } else {
                return 0;
            }

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

        default:
            break;
        }

        return 0;
    }
}
