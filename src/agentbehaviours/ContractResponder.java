package agentbehaviours;

import java.io.IOException;

import agents.Technician;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import message.TechnicianMessage;
import utils.Logger;
import utils.RepairSlot;

public class ContractResponder extends ContractNetResponder {
    RepairSlot repairSlot;

    public ContractResponder(Agent a, MessageTemplate mt) {
        super(a, mt);
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException {
        String clientName = cfp.getSender().getLocalName();
        try {
            Logger.info(myAgent.getLocalName(), "CFP received from " + clientName);

            this.repairSlot = ((Technician) myAgent).handleReceivedClientCfp(cfp);
            if (this.repairSlot != null) {
                // Send propose to Client
                TechnicianMessage proposal = new TechnicianMessage(this.repairSlot.getRepairPrice(),
                                                                   this.repairSlot
                                                                       .getStartRepairTime());
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContentObject(proposal);
                return propose;
            } else {
                // Don't send propose to Client
                ACLMessage refuse = cfp.createReply();
                refuse.setPerformative(ACLMessage.REFUSE);
                Logger.warn(myAgent.getLocalName(), "Refuse");
                return refuse;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new RefuseException("evaluation-failed");
    }

    // TODO:
    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
        throws FailureException {
        Logger.info(myAgent.getLocalName(), "Proposal accepted");
        if (((Technician) myAgent).handleReceivedClientAcceptProposal(this.repairSlot)) {
            Logger.info(myAgent.getLocalName(), "Action successfully performed");
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        } else {
            Logger.error(myAgent.getLocalName(), " Action execution failed");
            throw new FailureException("unexpected-error");
        }
    }

    // TODO:
    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        Logger.warn(myAgent.getLocalName(), "Proposal rejected");
    }
}
