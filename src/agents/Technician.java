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
import utils.*;
import utils.constants.Constants;


import static java.lang.System.exit;

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

        String serviceName = Constants.SERVICE_NAME;
        String serviceType = Constants.SERVICE_TYPE;

//        // Read the name of the service to register as an argument
//        Object[] args = getArguments();
//        if (args != null && args.length == 3) {
//            TechnicianType technicianType = (TechnicianType) args[0];
//            location = new Location((int)args[1], (int)args[2]);
//        } else {
//            Logger.error(getLocalName(), "Wrong arguments");
//            exit(0);
//        }

        Logger.info(getLocalName(), "Registering service \"" + serviceName + "\" of type " + serviceType);

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
            double startSlotTime = this.timeBoard.getNextAvailableSlotStartTime(receivedClientMessage.getRequestSendTime());
            double repairPrice = getRepairPrice(receivedClientMessage, startSlotTime);

            if (repairPrice == 0) {
                // It won't send a proposal message to client because it isn't worth the trip
                return null;
            }

            String clientId = cfp.getSender().getName();

            RepairSlot repairSlot = new RepairSlot(receivedClientMessage.getType(), startSlotTime, receivedClientMessage.getLocation(), repairPrice, clientId, this.location);

            return repairSlot;
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

        return null;
    }

    public double getRepairPrice(ClientMessage receivedClientMessage, double startSlotTime) {
        switch (this.technicianType) {
            case TECHNICIAN_TYPE_1:
                // Cares about distance
                // Malfunction price depends on type
                return 2 * Constants.calculateDistance(receivedClientMessage.getLocation(), this.location) * Constants.PRICE_PER_UNIT_OF_DISTANCE + Constants.getMalFunctionPrice(receivedClientMessage.getType());
            case TECHNICIAN_TYPE_2:
                // Cares about distance
                // Malfunction price is default
                return 2 * Constants.calculateDistance(receivedClientMessage.getLocation(), this.location) * Constants.PRICE_PER_UNIT_OF_DISTANCE + Constants.PRICE_DEFAULT_MALFUNCTION;
            case TECHNICIAN_TYPE_3:
                // Doesn't care about distance
                // Malfunction price depends on type
                // If the distance is not worth he refuses
                if (2 * Constants.calculateDistance(receivedClientMessage.getLocation(), this.location) * Constants.PRICE_PER_UNIT_OF_DISTANCE < Constants.getMalFunctionPrice(receivedClientMessage.getType())) {
                    return Constants.getMalFunctionPrice(receivedClientMessage.getType());
                } else {
                    return 0;
                }
            case TECHNICIAN_TYPE_4:
                // Doesn't care about distance
                // Malfunction price is default
                // If the distance is not worth he refuses
                if (2 * Constants.calculateDistance(receivedClientMessage.getLocation(), this.location) * Constants.PRICE_PER_UNIT_OF_DISTANCE < Constants.PRICE_DEFAULT_MALFUNCTION) {
                    return Constants.PRICE_DEFAULT_MALFUNCTION;
                } else {
                    return 0;
                }
            case TECHNICIAN_TYPE_5:
                // Cares about distance
                // Malfunction price depends on type
                // Cares about the time between client request time and technician repair proposed time
                return 2 * Constants.calculateDistance(receivedClientMessage.getLocation(), this.location) * Constants.PRICE_PER_UNIT_OF_DISTANCE + Constants.getMalFunctionPrice(receivedClientMessage.getType()) + Math.exp(-0.004*(startSlotTime - receivedClientMessage.getRequestSendTime()))*20;

            case TECHNICIAN_TYPE_6:
                // Doesn't care about distance
                // Malfunction price depends on type
                // If the distance is not worth he refuses
                // Cares about the time between client request time and technician repair proposed time
                if (2 * Constants.calculateDistance(receivedClientMessage.getLocation(), this.location) * Constants.PRICE_PER_UNIT_OF_DISTANCE < Constants.PRICE_DEFAULT_MALFUNCTION) {
                    return Constants.getMalFunctionPrice(receivedClientMessage.getType()) + Math.exp(-0.004*(startSlotTime - receivedClientMessage.getRequestSendTime()))*20;
                } else {
                    return 0;
                }

            case TECHNICIAN_TYPE_7:
                // Cares about distance
                // Malfunction price is default
                // Cares about the time between client request time and technician repair proposed time
                return 2 * Constants.calculateDistance(receivedClientMessage.getLocation(), this.location) * Constants.PRICE_PER_UNIT_OF_DISTANCE + Constants.PRICE_DEFAULT_MALFUNCTION + Math.exp(-0.004*(startSlotTime - receivedClientMessage.getRequestSendTime()))*20;

            case TECHNICIAN_TYPE_8:
                // Doesn't care about distance
                // If the distance is not worth he refuses
                // Malfunction price is default
                // Cares about the time between client request time and technician repair proposed time
                if (2 * Constants.calculateDistance(receivedClientMessage.getLocation(), this.location) * Constants.PRICE_PER_UNIT_OF_DISTANCE < Constants.PRICE_DEFAULT_MALFUNCTION) {
                    return Constants.PRICE_DEFAULT_MALFUNCTION + Math.exp(-0.004*(startSlotTime - receivedClientMessage.getRequestSendTime()))*20;
                } else {
                    return 0;
                }

            default:
                break;
        }
        return 0;
    }

    public boolean handleReceivedClientAcceptProposal(RepairSlot slot) {
        this.timeBoard.addRepairSlot(slot);
        return true;
    }

    // Agent Termination
    protected void takeDown() {
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

