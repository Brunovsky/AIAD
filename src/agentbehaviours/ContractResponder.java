package agentbehaviours;

import agents.Technician;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import message.ClientMessage;
import message.TechnicianMessage;
import utils.Logger;
import utils.RepairSlot;

import java.io.IOException;

public class ContractResponder extends ContractNetResponder {

    RepairSlot repairSlot;

    public ContractResponder(Agent a, MessageTemplate mt) {
        super(a, mt);
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException {
        try {
            Logger.info(myAgent.getLocalName(), "CFP received from "+cfp.getSender().getName());

            RepairSlot slot =((Technician) myAgent).handleReceivedClientCfp(cfp);
            if(slot != null){
                // Send propose to Client


                TechnicianMessage proposal = new TechnicianMessage();
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContentObject(proposal);
                return propose;
            } else {
                // Don't send propose to Client
                Logger.warn(myAgent.getLocalName(), "Refuse");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new RefuseException("evaluation-failed");
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
        Logger.info(myAgent.getLocalName(), "Proposal accepted");
        if (((Technician) myAgent).handleReceivedClientAcceptProposal(cfp, propose)) {
            Logger.info(myAgent.getLocalName(), "Action successfully performed");
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        }
        else {
            Logger.error(myAgent.getLocalName(), " Action execution failed");
            throw new FailureException("unexpected-error");
        }
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        Logger.warn(myAgent.getLocalName(), "Proposal rejected");
        //matar repairslot
    }
}
