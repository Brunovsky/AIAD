package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;

import agentbehaviours.AwaitDayBehaviour;
import agentbehaviours.AwaitNightBehaviour;
import agentbehaviours.SequentialLoopBehaviour;
import agentbehaviours.SubscribeBehaviour;
import agentbehaviours.WaitingBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import simulation.World;
import strategies.CompanyStrategy;
import types.Finance;
import types.JobList;
import types.Proposal;
import types.Record;
import types.StationHistory;
import types.WorkdayFinance;
import utils.Logger;
import utils.Logger.Format;

public class Company extends Agent {
    private static final long serialVersionUID = -4840612670786798770L;

    private final String id;

    private final Set<AID> stations;
    private final Map<AID, StationHistory> stationHistory;
    private final Map<AID, Integer> stationTechnicians;

    private final int numTechnicians;
    private final CompanyStrategy strategy;

    public Company(String id, int numTechnicians, CompanyStrategy strategy) {
        assert id != null && strategy != null;
        this.id = id;

        this.stations = ConcurrentHashMap.newKeySet();
        this.stationHistory = new ConcurrentHashMap<>();
        this.stationTechnicians = new ConcurrentHashMap<>();

        this.numTechnicians = numTechnicians;
        this.strategy = strategy;
        strategy.setCompany(this);
    }

    @Override
    protected void setup() {
        Logger.company(id, "Setup " + id);

        registerDFService();
        findStations();

        // Background
        SequentialLoopBehaviour loop = new SequentialLoopBehaviour(this);
        // Night
        loop.addSubBehaviour(new AwaitNightBehaviour(this));
        loop.addSubBehaviour(new CompanyNight(this));
        // Day
        loop.addSubBehaviour(new AwaitDayBehaviour(this));
        addBehaviour(loop);
    }

    @Override
    protected void takeDown() {
        Logger.company(id, "Company Terminated!");
        logAggregate();
        logSingle();
    }

    // Register the company in yellow pages
    private void registerDFService() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName(World.companyName(id));
            sd.setType(World.COMPANY_TYPE);

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
        templateSd.setType(World.STATION_TYPE);
        template.addServices(templateSd);

        String companySub = World.COMPANY_STATION_SERVICE;

        try {
            DFAgentDescription[] descriptions = DFService.search(this, template);
            for (DFAgentDescription stationDescriptor : descriptions) {
                AID station = stationDescriptor.getName();
                stations.add(station);
                stationHistory.put(station, new StationHistory(station));
                addBehaviour(new SubscribeBehaviour(this, station, companySub));
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        int each = numTechnicians / stations.size();
        int extra = numTechnicians % stations.size();

        for (AID station : stations) {
            if (extra > 0) {
                stationTechnicians.put(station, each + 1);
                --extra;
            } else {
                stationTechnicians.put(station, each);
            }
        }
    }

    // ***** UTILITIES

    private Finance totalFinance() {
        Finance finance = new Finance();
        for (StationHistory history : stationHistory.values()) {
            finance.add(history.finance);
        }
        return finance;
    }

    // ***** BEHAVIOURS

    /**
     * Behaviour: Receive a list of Job offers from a subscribed station.
     * Reply with a list of proposals.
     */
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
            MessageTemplate onto = MatchOntology(World.INFORM_COMPANY_JOBS);
            MessageTemplate acl = MatchPerformative(ACLMessage.REQUEST);
            MessageTemplate mt = and(and(onto, acl), MatchSender(station));
            ACLMessage message = receive(mt);
            if (message == null) {
                block();
                return;
            }

            int technicians = stationTechnicians.get(station);

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
                stationHistory.get(station).add(WorkdayFinance.empty());

                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                send(reply);
            }

            finalize();
        }
    }

    /**
     * Behaviour: Receive list of accepted jobs from the station.
     */
    private class ReceiveAcceptedJobs extends WaitingBehaviour {
        private static final long serialVersionUID = 239544247798867648L;

        private final AID station;
        private final Proposal proposal;

        ReceiveAcceptedJobs(Agent a, AID station, Proposal proposal) {
            super(a);
            this.station = station;
            this.proposal = proposal;
        }

        @Override
        public void action() {
            // Protocol C
            MessageTemplate onto = MatchOntology(World.INFORM_COMPANY_ASSIGNMENT);
            MessageTemplate acl = MatchPerformative(ACLMessage.INFORM);
            MessageTemplate mt = and(and(onto, acl), MatchSender(station));
            ACLMessage message = receive(mt);
            if (message == null) {
                block();
                return;
            }

            Logger.company(id, "Received accepted jobs from station " + station.getLocalName());

            int technicians = stationTechnicians.get(station);

            Proposal accepted = Proposal.from(myAgent.getAID(), message);
            accepted.copyPrices(proposal);

            WorkdayFinance workday = new WorkdayFinance(technicians, proposal, accepted);
            stationHistory.get(station).add(workday);

            finalize();
        }
    }

    /**
     * Behaviour: Expect list of job offers from each subscribed station.
     */
    private class CompanyNight extends OneShotBehaviour {
        private static final long serialVersionUID = 6059838822925652797L;

        CompanyNight(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            for (AID station : stations) {
                addBehaviour(new ReceiveJobList(myAgent, station));
            }
        }
    }

    // ***** LOGGING

    private void logAggregate() {
        CompanyRecord record = new CompanyRecord(id, strategy.toString(), numTechnicians);
        Finance finance = totalFinance();
        String line = Record.line(Logger.AGGREGATE_FORMAT, record, finance);
        Logger.appendCompany(line);
    }

    private void logSingle() {
        StringBuilder builder = new StringBuilder();

        if (Logger.RECORD_DEBUG) {
            builder.append("Strategy:    ").append(strategy).append('\n');
            builder.append("Technicians: ").append(numTechnicians).append('\n');
        }

        String headerStation = StationRecord.header(Logger.COMPANY_FORMAT);
        String headerFinance = Finance.header(Logger.COMPANY_FORMAT);
        String header = Record.line(Logger.COMPANY_FORMAT, headerStation, headerFinance);
        builder.append(header);

        TreeMap<String, String> lines = new TreeMap<>();
        TreeMap<String, String> blocks = new TreeMap<>();

        for (AID station : stations) {
            String name = station.getLocalName();
            int technicians = stationTechnicians.get(station);
            StationHistory history = stationHistory.get(station);
            StationRecord record = new StationRecord(name, technicians);
            String line = Record.line(Logger.COMPANY_FORMAT, record, history.finance);
            lines.put(name, line);

            if (Logger.RECORD_DEBUG) {
                String block = history.formatWorkdays(Logger.COMPANY_FORMAT);
                blocks.put(name, String.format("\n==> Station: %s\n%s", name, block));
            }
        }

        for (String line : lines.values()) builder.append(line);
        for (String block : blocks.values()) builder.append(block);

        String string = builder.toString();
        Logger.single(id, string);
    }

    public static class CompanyRecord extends Record {
        private final String name;
        private final String strategy;
        private final int technicians;

        public CompanyRecord(String name, String strategy, int technicians) {
            this.name = name;
            this.strategy = strategy;
            this.technicians = technicians;
        }

        public static String csvHeader() {
            return "company,strategy,techns";
        }

        public static String tableHeader() {
            return String.format("%10s  %15s  %6s", "station", "strategy", "techns");
        }

        public static String header(Format format) {
            switch (format) {
            case CSV:
                return csvHeader();
            case TABLE:
            default:
                return tableHeader();
            }
        }

        @Override
        public String csv() {
            return String.format("%s,%s,%d", name, strategy, technicians);
        }

        @Override
        public String table() {
            return String.format("%10s  %15s  %6d", name, strategy, technicians);
        }
    }

    public static class StationRecord extends Record {
        private final String name;
        private final int technicians;

        public StationRecord(String name, int technicians) {
            this.name = name;
            this.technicians = technicians;
        }

        public static String csvHeader() {
            return "station,techns";
        }

        public static String tableHeader() {
            return String.format("%10s  %6s", "station", "techns");
        }

        public static String header(Format format) {
            switch (format) {
            case CSV:
                return csvHeader();
            case TABLE:
            default:
                return tableHeader();
            }
        }

        @Override
        public String csv() {
            return String.format("%s,%d", name, technicians);
        }

        @Override
        public String table() {
            return String.format("%10s  %6d", name, technicians);
        }
    }
}
