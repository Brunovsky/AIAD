package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
import utils.Logger;
import utils.SimulationTables;
import utils.Table;

public class Simulation {
    private boolean ran = false;
    private final ArrayList<Client> clientAgents;
    private final ArrayList<Station> stationAgents;
    private final ArrayList<Company> companyAgents;
    private final Map<ClientsDesc.Strategy, ArrayList<Client>> clientMap;
    private final Map<CompaniesDesc.Strategy, ArrayList<Company>> companyMap;

    private ContainerController container;

    private static Runtime runtime;

    public static boolean SIMULATION_DEBUG_MODE = true;
    public static boolean EXECUTION_MODE_MULTI = true;
    public static int NUM_WORLDS = 40;

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        System.out.print("\033[H\033[2J");
        Logger.clearLogFolder();

        runtime = Runtime.instance();

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
        Simulation simulation = new Simulation();
        World world = new MiguelWorld();

        simulation.setup(world);

        Logger.simulation("BEGIN");

        simulation.launch();

        Logger.simulation("END");

        Table table = simulation.tableCompanies();
        world.extend(table);

        simulation.writeCompanyStationAll();

        simulation.terminate();

        String output = table.output(SimulationTables.AGGREGATE_FORMAT,
                                     SimulationTables.WORLDS_KEYS);
        Logger.write(Logger.COMPANIES_AGGREGATE_FILE, output);
    }

    private static void runSeveral() {
        Table worldTable = new Table("World Table");

        String output = worldTable.outputHeader(SimulationTables.AGGREGATE_FORMAT,
                                                SimulationTables.WORLDS_KEYS);
        Logger.write(Logger.COMPANIES_AGGREGATE_FILE, output);

        for (int n = 1; n <= NUM_WORLDS; ++n) {
            Simulation simulation = new Simulation();
            World world = new MiguelWorld();

            simulation.setup(world);

            Logger.simulation("BEGIN " + n);

            simulation.launch();

            Logger.simulation("END " + n);

            Table table = simulation.tableCompanies("world_" + n);
            worldTable.merge(table);
            world.extend(table);
            table.setAll("world", String.format("%d", n));

            output = table.outputBody(SimulationTables.AGGREGATE_FORMAT,
                                      SimulationTables.WORLDS_KEYS);
            Logger.write(Logger.COMPANIES_AGGREGATE_FILE, output);

            simulation.terminate();
        }

        // Do something with worldTable?
    }

    // ***** SIMULATION

    public Simulation() {
        clientAgents = new ArrayList<>();
        stationAgents = new ArrayList<>();
        companyAgents = new ArrayList<>();
        clientMap = new HashMap<>();
        companyMap = new HashMap<>();
    }

    private void setup(World world) {
        World.set(world);
        assert !ran;
        ran = true;

        Profile profile = new ProfileImpl();
        container = runtime.createMainContainer(profile);
    }

    private void launch() {
        God.renew();
        launchStations();
        launchClients();
        launchCompanies();
        launchSimulation();
    }

    private void terminate() {
        for (Company company : companyAgents) company.doDelete();
        for (Client client : clientAgents) client.doDelete();
        for (Station station : stationAgents) station.doDelete();

        try {
            container.kill();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    // ***** TABLES

    // COMPANIES TABLE

    private Table tableCompanies(String prefix) {
        Table table = new Table("Global Performance");
        for (Company company : companyAgents) {
            company.populateRow(table.addRow(prefix + company.getId()));
        }
        return table;
    }

    private Table tableCompanies() {
        return tableCompanies("");
    }

    // INDIVIDUAL COMPANY STATION HISTORY TABLES

    private void writeCompanyStationHistory(Company company) {
        Map<String, Table> map = SimulationTables.getCompany(company.getId());
        String[] outputs = new String[map.size()];
        int i = 0;
        for (Table table : map.values()) {
            outputs[i] = "Station: " + table.getTitle() + "\n";
            outputs[i++] += table.output(SimulationTables.COMPANY_FORMAT,
                                         SimulationTables.DAILY_KEYS);
        }
        String header = String.format("Company: %s\nStrategy: %s\n\n", company.getId(),
                                      company.getStrategy().getName());
        String output = header + String.join("\n", outputs);
        Logger.write(company.getId(), output);
    }

    // Write all company station tables

    private void writeCompanyStationAll() {
        for (Company company : companyAgents) {
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
            String id = "station_" + (i < 9 ? "0" : "") + (i + 1);

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
            String id = "client_" + (i < 9 ? "00" : i < 99 ? "0" : "") + (i + 1);
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
            String id = "company_" + (i < 9 ? "0" : "") + (i + 1);
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
