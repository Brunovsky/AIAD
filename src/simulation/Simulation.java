package simulation;

import agents.Client;
import agents.Company;
import agents.Station;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import utils.Logger;
import utils.Table;

public class Simulation {
    private boolean ran = false;
    private final ArrayList<Client> clientAgents;
    private final ArrayList<Station> stationAgents;
    private final ArrayList<Company> companyAgents;

    private final Map<ClientsDesc.Strategy, ArrayList<Client>> clientMap;
    private final Map<CompaniesDesc.Strategy, ArrayList<Company>> companyMap;

    private static Runtime runtime;
    private ContainerController container;

    private static Table worldTable;

    public static boolean SIMULATION_DEBUG_MODE = false;
    public static boolean EXECUTION_MODE_MULTI = true;
    public static int NUM_WORLDS = 5;

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        System.out.print("\033[H\033[2J");
        Logger.clearLogFolder();

        runtime = Runtime.instance();

        worldTable = new Table("World Table");

        if (EXECUTION_MODE_MULTI) {
            runSeveral();
        } else {
            runOnce();
        }

        runtime.setCloseVM(true);
        runtime.shutDown();
    }

    // ***** EXECUTION MODES

    private static void runOnce() {
        Table table = new Simulation().run(1, new AIADWorld());
        worldTable.merge(table);
        String output = table.output(Logger.AGGREGATE_FORMAT, WORLDS_KEYS);
        Logger.write(Logger.COMPANIES_AGGREGATE_FILE, output);
    }

    private static void runSeveral() {
        for (int n = 1; n <= NUM_WORLDS; ++n) {
            // Table table = new Simulation().run(n, new AIADWorld());
            Table table = new Simulation().run(n, new MiguelWorld());
            worldTable.merge(table);
        }
        String output = worldTable.output(Logger.AGGREGATE_FORMAT, WORLDS_KEYS);
        Logger.write(Logger.COMPANIES_AGGREGATE_FILE, output);
    }

    // ***** SIMULATION

    public Simulation() {
        clientAgents = new ArrayList<>();
        stationAgents = new ArrayList<>();
        companyAgents = new ArrayList<>();

        clientMap = new HashMap<>();
        companyMap = new HashMap<>();
    }

    private Table run(int n, World world) {
        World.set(world);
        assert !ran;
        ran = true;

        Profile profile = new ProfileImpl();
        container = runtime.createMainContainer(profile);

        Logger.simulation("BEGIN: WORLD " + n);

        God.renew();
        launchStations();
        launchClients();
        launchCompanies();
        launchSimulation();

        Table table = tableCompanies();
        table.setAll("world", String.format("%d", n));
        world.extend(table);

        for (Company company : companyAgents) company.doDelete();
        for (Client client : clientAgents) client.doDelete();
        for (Station station : stationAgents) station.doDelete();

        Logger.simulation("END: WORLD " + n);

        try {
            container.kill();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        return table;
    }

    // ***** TABLES

    private static final String[] WORLDS_KEYS = new String[] {
        "world",      // Simulation
        "days",       // World[i]
        "company",    // World[i] > Company[c]
        "strategy",   // World[i] > Company[c]
        "techns",     // World[i] > Company[c]
        "cost",       // World[i] > Company[c].totalFinance()
        "revenue",    // World[i] > Company[c].totalFinance()
        "proposal",   // World[i] > Company[c].totalFinance()
        "assigned",   // World[i] > Company[c].totalFinance()
        "worker",     // World[i] > Company[c].totalFinance()
        "companies",  // World[i]
        "clients",    // World[i]
        "stations",   // World[i]
        "salary",     // World[i]
    };

    // COMPANIES TABLE

    private static final String[] COMPANIES_KEYS = new String[] {
        "company",   // Company[c]
        "strategy",  // Company[c]
        "techns",    // Company[c]
        "cost",      // Company[c].totalFinance()
        "revenue",   // Company[c].totalFinance()
        "proposal",  // Company[c].totalFinance()
        "assigned",  // Company[c].totalFinance()
        "worker",    // Company[c].totalFinance()
    };

    private Table tableCompanies() {
        Table table = new Table();
        for (Company company : companyAgents) company.populateRow(table.addRow());
        return table;
    }

    @SuppressWarnings("unused")
    private void writeCompanies() {
        Table table = tableCompanies();
        String output = table.output(Logger.AGGREGATE_FORMAT, COMPANIES_KEYS);
        Logger.write(Logger.COMPANIES_AGGREGATE_FILE, output);
    }

    // INDIVIDUAL COMPANY TABLES

    private static final String[] COMPANY_STATIONS_KEYS = new String[] {
        "station",   // Company[c]
        "techns",    // Company[c]
        "cost",      // Company[c].StationHistory[s].Finance
        "revenue",   // Company[c].StationHistory[s].Finance
        "proposal",  // Company[c].StationHistory[s].Finance
        "assigned",  // Company[c].StationHistory[s].Finance
        "worker",    // Company[c].StationHistory[s].Finance
    };

    private Table tableCompanyStations(Company company) {
        return company.makeTableStations();
    }

    private void writeCompanyStation(Company company) {
        Table table = tableCompanyStations(company);
        String output = table.output(Logger.COMPANY_FORMAT, COMPANY_STATIONS_KEYS);
        Logger.write(company.getId(), output);
    }

    // INDIVIDUAL COMPANY STATION HISTORY TABLES

    private static final String[] COMPANY_STATION_HISTORY_KEYS = new String[] {
        "day",       // Company[c].StationHistory[s].WorkdayFinance[d]
        "techns",    // Company[c].StationHistory[s].WorkdayFinance[d]
        "cost",      // Company[c].StationHistory[s].WorkdayFinance[d]
        "revenue",   // Company[c].StationHistory[s].WorkdayFinance[d]
        "proposal",  // Company[c].StationHistory[s].WorkdayFinance[d]
        "assigned",  // Company[c].StationHistory[s].WorkdayFinance[d]
        "worker",    // Company[c].StationHistory[s].WorkdayFinance[d]
    };

    private Table[] tablesCompanyStationHistories(Company company) {
        return company.makeTablesStationHistory();
    }

    private void writeCompanyStationHistory(Company company) {
        Table[] tables = tablesCompanyStationHistories(company);
        String[] outputs = new String[tables.length];
        for (int i = 0; i < tables.length; ++i) {
            outputs[i] = "Station: " + tables[i].getTitle() + "\n";
            outputs[i] += tables[i].output(Logger.COMPANY_FORMAT, COMPANY_STATION_HISTORY_KEYS);
        }
        String output = String.join("\n", outputs);
        Logger.write(company.getId(), output);
    }

    // Write all company station tables

    @SuppressWarnings("unused")
    private void writeCompanyStationAll() {
        for (Company company : companyAgents) {
            writeCompanyStation(company);
            Logger.write(company.getId(), "\n");
            writeCompanyStationHistory(company);
        }
    }

    // ***** UTILITIES

    // shuffle array of objects
    private void shuffle(Object[] array) {
        final Random rng = ThreadLocalRandom.current();
        for (int i = array.length - 1; i > 0; --i) {
            int j = rng.nextInt(i + 1);
            Object a = array[j];
            array[j] = array[i];
            array[i] = a;
        }
    }

    // ***** LAUNCHERS

    private void launchAgent(String id, Agent agent) {
        try {
            AgentController ac = container.acceptNewAgent(id, agent);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Launch all world stations
     */
    private void launchStations() {
        for (int i = World.get().S - 1; i >= 0; --i) {
            String id = "station_" + (i + 1);

            Station station = new Station(id);
            stationAgents.add(station);

            launchAgent(id, station);

            try {
                Thread.sleep(World.MILLI_WAIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Launch all world clients according to the world specification with randomly
     * ordered locations.
     */
    private void launchClients() {
        ClientsDesc[] configs = new ClientsDesc[World.get().Cl];
        AID[] stations = new AID[World.get().Cl];

        int z = 0;
        for (ClientsDesc entry : World.get().clients) {
            clientMap.putIfAbsent(entry.strategy, new ArrayList<>());
            for (int k = 0; k < entry.numberClients; ++k, ++z) {
                configs[z] = entry;
            }
        }

        z = 0;
        int n = 0;
        for (StationsDesc entry : World.get().stations) {
            for (int i = 0; i < entry.numberStations; i++, n++) {
                for (int k = 0; k < entry.numberClients; k++, z++) {
                    stations[z] = stationAgents.get(n).getAID();
                }
            }
        }

        shuffle(configs);
        shuffle(stations);

        for (int i = 0; i < World.get().Cl; ++i) {
            String id = "client_" + (i + 1);
            ClientsDesc config = configs[i];
            AID station = stations[i];

            Client client = new Client(id, config.strategy.make(), station);

            clientMap.get(config.strategy).add(client);
            clientAgents.add(client);

            launchAgent(id, client);

            try {
                Thread.sleep(World.MILLI_WAIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Launch all world companies
     */
    private void launchCompanies() {
        CompaniesDesc[] configs = new CompaniesDesc[World.get().Co];

        int z = 0;
        for (CompaniesDesc entry : World.get().companies) {
            companyMap.putIfAbsent(entry.strategy, new ArrayList<>());
            for (int i = 0; i < entry.numberCompanies; i++, z++) {
                configs[z] = entry;
            }
        }

        shuffle(configs);

        for (int i = 0; i < World.get().Co; ++i) {
            String id = "company_" + (i + 1);
            CompaniesDesc config = configs[i];

            Company company = new Company(id, config.numberTechnicians, config.strategy.make());

            companyMap.get(config.strategy).add(company);
            companyAgents.add(company);

            launchAgent(id, company);

            try {
                Thread.sleep(World.MILLI_WAIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Schedule the world orchestrator runnables to control the progress of days.
     * Await for the simulation to finish.
     */
    private void launchSimulation() {
        try {
            AgentController ac = container.acceptNewAgent("god", God.get());
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
            System.exit(3);
        }
        God.get().runSimulation();
        God.get().awaitWorld();
    }
}
