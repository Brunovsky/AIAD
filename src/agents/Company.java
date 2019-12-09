package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import agentbehaviours.AwaitDayBehaviour;
import agentbehaviours.AwaitNightBehaviour;
import agentbehaviours.SequentialLoopBehaviour;
import agentbehaviours.SubscribeBehaviour;
import agentbehaviours.WaitingBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import simulation.World;
import strategies.CompanyStrategy;
import types.Finance;
import types.JobList;
import types.Proposal;
import types.StationHistory;
import types.WorkdayFinance;
import utils.Logger;
import utils.Table;

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

    public String getId() {
        return id;
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

    public void populateRow(Map<String, String> row) {
        row.put("company", id);
        row.put("strategy", strategy.toString());
        row.put("techns", String.format("%d", numTechnicians));
        totalFinance().populateRow(row);
    }

    public Table makeTableStations() {
        Table table = new Table(id);
        TreeMap<String, AID> map = new TreeMap<>();
        for (AID station : stations) map.put(station.getLocalName(), station);
        for (AID station : map.values()) {
            Map<String, String> row = table.addRow();
            row.put("station", station.getLocalName());
            row.put("techns", String.format("%d", stationTechnicians.get(station)));
            stationHistory.get(station).finance.populateRow(row);
        }
        return table;
    }

    public Table[] makeTablesStationHistory() {
        TreeMap<String, Table> map = new TreeMap<>();
        for (AID station : stations) {
            map.put(station.getLocalName(), stationHistory.get(station).makeTable());
        }

        Table[] tables = new Table[stations.size()];
        int i = 0;
        for (Table table : map.values()) tables[i++] = table;
        return tables;
    }
}
