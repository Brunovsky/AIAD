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


import static java.lang.System.exit;

public class Technician extends Agent {

    private Location location;
    TimeBoard timeBoard;
    TechnicianType technicianType;

    protected void setup() {
        
        timeBoard = new TimeBoard();

        Logger.info(getLocalName(), "Setup Technician Agent");

        String serviceName = "TechRepairs";
        String serviceType = "tech-repairs";

        // Read the name of the service to register as an argument
        String[] args = (String[]) getArguments();
        if (args != null && args.length == 3) {
            switch(args[0]) {
                case "T1":
                    technicianType = TechnicianType.TECHNICIAN_TYPE_1;
                case "T2":
                    technicianType = TechnicianType.TECHNICIAN_TYPE_2;
                case "T3":
                    technicianType = TechnicianType.TECHNICIAN_TYPE_3;
                case "T4":
                    technicianType = TechnicianType.TECHNICIAN_TYPE_4;
            }
            location = new Location(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        } else {
            Logger.error(getLocalName(), "Wrong arguments");
            exit(0);
        }

        Logger.info(getLocalName(), "Registering service \"" + serviceName + "\" of type "+serviceType);

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
            double repairPrice = getRepairPrice(receivedClientMessage.getLocation(), receivedClientMessage.getType());
            String clientId = cfp.getSender().getName();

            RepairSlot repairSlot = new RepairSlot(receivedClientMessage.getType(), startSlotTime, receivedClientMessage.getLocation(), repairPrice, clientId, this.location);

            return repairSlot;
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

        return null;
    }

    public double getRepairPrice(Location clientLocation, MalfunctionType type){
        // TODO: Miguel faz aí um switch

        // calcular preço gasto para a viagem + preço consoante o type de malfunction
        return 0;
    }

    public boolean handleReceivedClientAcceptProposal(RepairSlot slot){
        //  TODO:
        // Handle Accept Proposal
        this.timeBoard.addRepairSlot(slot);
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

