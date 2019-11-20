package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import agentbehaviours.AwaitDayBehaviour;
import agentbehaviours.AwaitNightBehaviour;
import agentbehaviours.SequentialLoopBehaviour;
import agentbehaviours.SubscribeBehaviour;
import agentbehaviours.WaitingBehaviour;
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
    private final Map<String, AID> stationNames;

    private final CompanyStrategy strategy;

    public Company(String id, CompanyStrategy strategy) {
        assert id != null && strategy != null;
        this.id = id;

        this.activeTechnicians = ConcurrentHashMap.newKeySet();
        this.technicianHistory = new ConcurrentHashMap<>();

        this.activeStations = ConcurrentHashMap.newKeySet();
        this.stationHistory = new ConcurrentHashMap<>();
        this.stationTechnicians = new ConcurrentHashMap<>();
        this.stationNames = new ConcurrentHashMap<>();

        this.strategy = strategy;
        strategy.setCompany(this);
    }

    @Override
    protected void setup() {
        Logger.company(id, "Setup " + id);

        registerDFService();
        findStations();

        // Background
        addBehaviour(new SubscriptionListener(this));
        addBehaviour(new ReceiveContractProposals(this));

        SequentialLoopBehaviour loop = new SequentialLoopBehaviour(this);
        // Night
        loop.addSubBehaviour(new AwaitNightBehaviour(this));
        loop.addSubBehaviour(new CompanyNight(this));
        // Day
        loop.addSubBehaviour(new AwaitDayBehaviour(this));
        loop.addSubBehaviour(new UpdateContracts(this));
        addBehaviour(loop);
    }

    @Override
    protected void takeDown() {
        Logger.company(id, "Company Terminated!");
        StringBuilder builder = new StringBuilder();
        builder.append("\tDAY \tSALARY \t\tCUT \tEARNED \t\tACCUMULATED\n");
        for (StationHistory history : stationHistory.values()) {
            double earned = 0.0;
            builder.append(history.station.getLocalName()).append('\n');
            for (int i = 0; i < history.finances.size(); ++i) {
                WorkFinance finance = history.finances.get(i);
                earned += finance.earned;
                builder.append(String.format("\t%d \t%f \t%f \t%f \t%f\n", i, finance.salary,
                                             finance.cut, finance.earned, earned));
            }
        }
        Logger.write(id, builder.toString());
    }

    // Register the company in yellow pages
    private void registerDFService() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName("company-" + id);
            sd.setType(World.get().getCompanyType());

            dfd.addServices(sd);

            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
            System.exit(1);
        }
    }

    // Subscribe to all stations and record a list of them.
    private void findStations() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType(World.get().getStationType());
        template.addServices(templateSd);

        String companySub = World.get().getCompanyStationService();

        try {
            DFAgentDescription[] stations = DFService.search(this, template);
            for (DFAgentDescription stationDescriptor : stations) {
                AID station = stationDescriptor.getName();
                activeStations.add(station);
                stationHistory.put(station, new StationHistory(station));
                stationNames.put(station.getLocalName(), station);
                stationTechnicians.put(station, new HashSet<>());
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

    private class UpdateContracts extends OneShotBehaviour {
        private static final long serialVersionUID = 2769099897492753827L;

        UpdateContracts(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            // Remove contracts ending now
            int today = World.get().getDay();
            for (AID station : stationTechnicians.keySet()) {
                Set<AID> technicians = stationTechnicians.get(station);
                Set<AID> removed = new HashSet<>();
                for (AID technician : technicians) {
                    Contract currentContract = technicianHistory.get(technician).currentContract();
                    assert currentContract != null;
                    if (currentContract.end == today) {
                        removed.add(technician);
                        activeTechnicians.remove(technician);
                    }
                }
                technicians.removeAll(removed);
            }

            // Add contracts starting tomorrow
            int tomorrow = today + 1;
            for (TechnicianHistory history : technicianHistory.values()) {
                Contract contract = history.lastContract();
                if (contract.start == tomorrow) {
                    stationTechnicians.get(history.station).add(history.technician);
                    activeTechnicians.add(history.technician);
                }
            }
        }
    }

    private class ReceiveContractProposals extends CyclicBehaviour {
        private static final long serialVersionUID = -3009146208732453520L;

        ReceiveContractProposals(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            MessageTemplate onto = MatchOntology(World.get().getTechnicianOfferContract());
            MessageTemplate acl = MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage propose = receive(and(onto, acl));
            if (propose == null) {
                block();
                return;
            }

            AID technician = propose.getSender();
            Contract contract = Contract.from(myAgent.getAID(), technician, propose);
            technicianHistory.get(technician).addContract(contract);

            // TODO SIMPLIFICATION
            assert activeTechnicians.contains(technician);

            ACLMessage reply = propose.createReply();
            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            reply.setContent(propose.getContent());
            send(reply);
        }
    }

    private class ReceiveJobList extends WaitingBehaviour {
        private static final long serialVersionUID = -5608877347217729029L;

        private final AID station;

        ReceiveJobList(Agent a, AID station) {
            super(a);
            this.station = station;
        }

        @Override
        public void action() {
            // Protocol A
            MessageTemplate onto = MatchOntology(World.get().getInformCompanyJobs());
            MessageTemplate acl = MatchPerformative(ACLMessage.REQUEST);
            ACLMessage message = receive(and(and(onto, acl), MatchSender(station)));
            if (message == null) {
                block();
                return;
            }

            int technicians = numTechniciansInStation(station);

            if (technicians > 0) {
                JobList jobList = JobList.from(message);
                Proposal proposal = strategy.makeProposal(technicians, jobList);

                // Protocol B
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(proposal.make());
                send(reply);

                addBehaviour(new ReceiveAcceptedJobs(myAgent, station, proposal));
            } else {
                stationHistory.get(station).add(null, null);
                stationHistory.get(station).add(new WorkFinance(1));

                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                send(reply);
            }

            finalize();
        }
    }

    private class ReceiveAcceptedJobs extends WaitingBehaviour {
        private static final long serialVersionUID = 239544247798867648L;

        private final AID station;
        private final Proposal proposed;

        ReceiveAcceptedJobs(Agent a, AID station, Proposal proposed) {
            super(a);
            this.station = station;
            this.proposed = proposed;
        }

        @Override
        public void action() {
            // Protocol C
            MessageTemplate onto = MatchOntology(World.get().getInformCompanyAssignment());
            MessageTemplate acl = MatchPerformative(ACLMessage.INFORM);
            ACLMessage message = receive(and(and(onto, acl), MatchSender(station)));
            if (message == null) {
                block();
                return;
            }

            Logger.company(id, "Received accepted jobs from station " + station.getLocalName());

            int technicians = numTechniciansInStation(station);

            Proposal accepted = Proposal.from(myAgent.getAID(), message);
            accepted.easyPrice = proposed.easyPrice;
            accepted.mediumPrice = proposed.mediumPrice;
            accepted.hardPrice = proposed.hardPrice;

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
                double sum = salary + techCut;
                WorkFinance payment = new WorkFinance(1, haveJobs, salary, techCut, sum);

                history.add(payment);

                totalSalaryPaid += salary;
                totalCutPaid += techCut;

                Logger.company(id, "Sent payment to technician " + technician.getLocalName());

                // Protocol D
                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                inform.setOntology(World.get().getCompanyPayment());
                inform.setContent(payment.make());
                inform.addReceiver(technician);
                send(inform);
            }

            final double earned = accepted.totalEarnings() - totalSalaryPaid - totalCutPaid;
            WorkFinance finance = new WorkFinance(1, jobs, totalSalaryPaid, totalCutPaid, earned);

            stationHistory.get(station).add(proposed, accepted);
            stationHistory.get(station).add(finance);

            finalize();
        }
    }

    private class CompanyNight extends OneShotBehaviour {
        private static final long serialVersionUID = 6059838822925652797L;

        CompanyNight(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            for (AID station : activeStations) {
                addBehaviour(new ReceiveJobList(myAgent, station));
            }
        }
    }

    private class SubscriptionListener extends CyclicBehaviour {
        private static final long serialVersionUID = 9068977292715279066L;

        private final MessageTemplate mt;
        private final String ontology;

        SubscriptionListener(Agent a) {
            super(a);
            this.ontology = World.get().getInitialEmployment();

            MessageTemplate subscribe = MatchPerformative(ACLMessage.SUBSCRIBE);
            MessageTemplate onto = MatchOntology(ontology);
            this.mt = and(subscribe, onto);
        }

        @Override
        public void action() {
            ACLMessage message = receive(mt);
            if (message == null) {
                block();
                return;
            }

            AID technician = message.getSender();
            AID station = stationNames.get(message.getContent());
            Contract initialContract = strategy.initialContract(technician, station);

            TechnicianHistory history = new TechnicianHistory(technician, station);
            activeTechnicians.add(technician);
            technicianHistory.put(technician, history);
            stationTechnicians.get(station).add(technician);
            history.addContract(initialContract);

            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(initialContract.make());
            send(reply);
        }
    }
}
