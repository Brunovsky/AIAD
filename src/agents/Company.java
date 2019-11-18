package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import agentbehaviours.SubscribeBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import simulation.World;
import strategies.CompanyStrategy;
import types.Contract;
import types.JobList;
import types.Proposal;
import types.StationHistory;
import types.TechnicianHistory;
import types.WorkFinance;
import utils.Logger;

public class Company extends Agent {
    private static final long serialVersionUID = -4840612670786798770L;

    private final String id;

    private final Set<AID> activeTechnicians;
    private final Map<AID, TechnicianHistory> technicianHistory;

    private final Set<AID> activeStations;
    private final Map<AID, StationHistory> stationHistory;
    private final Map<AID, Set<AID>> stationTechnicians;
    private final Map<String, AID> stationNames;  // can't parse AID from message

    private final CompanyStrategy strategy;

    public Company(String id, CompanyStrategy strategy) {
        assert id != null;
        this.id = id;

        this.activeTechnicians = new HashSet<>();
        this.technicianHistory = new HashMap<>();

        this.activeStations = new HashSet<>();
        this.stationHistory = new HashMap<>();
        this.stationTechnicians = new HashMap<>();
        this.stationNames = new HashMap<>();

        this.strategy = strategy;
    }

    @Override
    protected void setup() {
        Logger.info(getLocalName(), "Setup " + id);

        // SETUP
        registerDFService();
        findStations();
        addBehaviour(new SubscriptionListener(this));

        // NIGHT
        addBehaviour(new CompanyNight(this));

        // DAY
        addBehaviour(new ReceiveContractProposals(this));
    }

    @Override
    protected void takeDown() {
        Logger.warn(getLocalName(), "Company Terminated!");
    }

    // Register the company in yellow pages
    private void registerDFService() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName("company" + id);  // Necessary?????
            sd.setType(World.get().getCompanyType());

            dfd.addServices(sd);

            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    // Subscribe to all stations and record a list of them.
    private void findStations() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType(World.get().getStationType());
        template.addServices(templateSd);

        String companySub = World.get().getCompanySubscription();

        try {
            DFAgentDescription[] stations = DFService.search(this, template);
            for (DFAgentDescription stationDescriptor : stations) {
                AID station = stationDescriptor.getName();
                activeStations.add(station);
                stationHistory.put(station, new StationHistory(station));
                stationNames.put(station.getLocalName(), station);
                addBehaviour(new SubscribeBehaviour(this, station, companySub));
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    // ***** UTILITIES

    private int numTechniciansInStation(AID station) {
        return stationTechnicians.get(station).size();
    }

    // ***** BEHAVIOURS

    private class ReceiveContractProposals extends OneShotBehaviour {
        private static final long serialVersionUID = -3009146208732453520L;

        ReceiveContractProposals(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            MessageTemplate onto, acl, mt;

            onto = MatchOntology(World.get().getTechnicianOfferContract());
            acl = MatchPerformative(ACLMessage.PROPOSE);
            mt = and(onto, acl);
            ACLMessage propose = receive(mt);
            while (propose == null) {
                block();
                propose = receive(mt);
            }

            AID technician = propose.getSender();
            Contract contract = Contract.from(myAgent.getAID(), technician, propose);
            technicianHistory.get(technician).addContract(contract);

            // TODO SIMPLIFICATION
            assert activeTechnicians.contains(technician);

            ACLMessage reply = propose.createReply();
            reply.setContent(propose.getContent());
            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            send(reply);
        }
    }

    private class CompanyNight extends OneShotBehaviour {
        private static final long serialVersionUID = 6059838822925652797L;

        private final HashMap<AID, Proposal> proposals;

        CompanyNight(Agent a) {
            super(a);
            this.proposals = new HashMap<>();
        }

        private void replyStation(ACLMessage message) {
            AID station = message.getSender();
            assert activeStations.contains(station);

            int technicians = numTechniciansInStation(station);

            if (technicians > 0) {
                JobList jobList = JobList.from(message);
                Proposal proposal = strategy.makeProposal(technicians, jobList);
                proposals.put(station, proposal);

                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(proposal.make());
                send(reply);
            } else {
                stationHistory.get(station).add(null, null);
                stationHistory.get(station).add(new WorkFinance(1));

                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                send(reply);
            }
        }

        private void informTechnicians(ACLMessage message) {
            AID station = message.getSender();
            assert activeStations.contains(station);

            int technicians = numTechniciansInStation(station);

            Proposal proposed = proposals.get(station);
            Proposal accepted = Proposal.from(myAgent.getAID(), message);

            final int haveJobs = accepted.totalJobs() > 0 ? 1 : 0;
            final int jobs = accepted.totalJobs();
            final double cut = accepted.totalEarnings() / technicians;

            double totalSalaryPaid = 0.0;
            double totalCutPaid = 0.0;

            for (AID technician : stationTechnicians.get(station)) {
                TechnicianHistory history = technicianHistory.get(technician);
                Contract contract = history.currentContract();

                double salary = contract.salary;
                double techCut = contract.percentage * cut;
                WorkFinance payment = new WorkFinance(1, haveJobs, salary, techCut, 0);

                history.add(payment);

                totalSalaryPaid += salary;
                totalCutPaid += techCut;

                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                inform.setOntology(World.get().getCompanyPayment());
                inform.setContent(payment.make());
                send(inform);
            }

            final double earned = accepted.totalEarnings() - totalSalaryPaid - totalCutPaid;
            WorkFinance finance = new WorkFinance(1, jobs, totalSalaryPaid, totalCutPaid, earned);

            stationHistory.get(station).add(proposed, accepted);
            stationHistory.get(station).add(finance);
        }

        @Override
        public void action() {
            proposals.clear();
            MessageTemplate acl, onto, mt;

            onto = MatchOntology(World.get().getInformCompanyJobs());
            acl = MatchPerformative(ACLMessage.REQUEST);
            mt = and(onto, acl);
            for (AID station : activeStations) {
                MessageTemplate from = MatchSender(station);
                ACLMessage request = receive(and(mt, from));  // Protocol A
                while (request == null) {
                    block();
                    request = receive(and(mt, from));
                }
                replyStation(request);  // Protocol B
            }

            onto = MatchOntology(World.get().getInformCompanyAssignment());
            acl = MatchPerformative(ACLMessage.INFORM);
            mt = and(onto, acl);
            for (AID station : activeStations) {
                if (numTechniciansInStation(station) == 0) continue;
                MessageTemplate from = MatchSender(station);
                ACLMessage inform = receive(and(mt, from));  // Protocol C
                while (inform == null) {
                    block();
                    inform = receive(and(mt, from));
                }
                informTechnicians(inform);  // Protocol D
            }
        }
    }

    private class SubscriptionListener extends CyclicBehaviour {
        private static final long serialVersionUID = 9068977292715279066L;

        private final MessageTemplate mt;

        SubscriptionListener(Agent a) {
            super(a);

            MessageTemplate subscribe = MatchPerformative(ACLMessage.SUBSCRIBE);
            MessageTemplate onto = MatchOntology(World.get().getInitialEmployment());
            this.mt = and(subscribe, onto);
        }

        @Override
        public void action() {
            ACLMessage message = receive(mt);
            while (message == null) {
                block();
                return;
            }

            AID technician = message.getSender();
            AID station = stationNames.get(message.getContent());
            Contract initialContract = strategy.initialContract(station);

            if (message.getPerformative() == ACLMessage.SUBSCRIBE) {
                activeTechnicians.add(technician);
                technicianHistory.put(technician, new TechnicianHistory(technician));
                stationTechnicians.get(station).add(technician);
            }

            message.createReply();
            message.setPerformative(ACLMessage.CONFIRM);
            message.setContent(initialContract.make());
            send(message);
        }
    }
}
