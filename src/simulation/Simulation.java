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
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import utils.Logger;

public class Simulation {
    private final ArrayList<Client> clientAgents;
    private final ArrayList<Station> stationAgents;
    private final ArrayList<Company> companyAgents;

    private final Map<ClientsDesc.Strategy, ArrayList<Client>> clientMap;
    private final Map<CompaniesDesc.Strategy, ArrayList<Company>> companyMap;

    private final Runtime runtime;
    private final Profile profile;
    private final ContainerController container;

    public static void main(String[] args) {
        System.out.print("\033[H\033[2J");
        Logger.clearLogFolder();
        Logger.aggregateHeaders();
        World.set(new AIADWorld());
        new Simulation();
    }

    public Simulation() {
        clientAgents = new ArrayList<>();
        stationAgents = new ArrayList<>();
        companyAgents = new ArrayList<>();

        clientMap = new HashMap<>();
        companyMap = new HashMap<>();

        runtime = Runtime.instance();
        profile = new ProfileImpl();
        container = runtime.createMainContainer(profile);

        God.renew();
        launchStations();
        launchClients();
        launchCompanies();
        launchSimulation();

        try {
            for (Company company : companyAgents) company.doDelete();
            for (Client client : clientAgents) client.doDelete();
            for (Station station : stationAgents) station.doDelete();

            container.kill();
            runtime.shutDown();
            // System.exit(0);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

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
     * Launch all world clients according to the world specification with randomly ordered
     * locations.
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
