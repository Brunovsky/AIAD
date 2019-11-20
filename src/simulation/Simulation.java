package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import agents.Client;
import agents.Company;
import agents.Station;
import agents.Technician;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Simulation {
    private final ArrayList<Technician> technicianAgents;
    private final ArrayList<Client> clientAgents;
    private final ArrayList<Station> stationAgents;
    private final ArrayList<Company> companyAgents;

    private final Map<ClientsDesc.Strategy, ArrayList<Client>> clientMap;
    private final Map<CompaniesDesc.Strategy, ArrayList<Company>> companyMap;
    private final Map<TechniciansDesc.Strategy, ArrayList<Technician>> technicianMap;

    private final Runtime runtime;
    private final Profile profile;
    private final ContainerController container;

    public static void main(String[] args) {
        System.out.print("\033[H\033[2J");
        World.set(new AIADWorld());
        new Simulation();
    }

    public Simulation() {
        technicianAgents = new ArrayList<>();
        clientAgents = new ArrayList<>();
        stationAgents = new ArrayList<>();
        companyAgents = new ArrayList<>();

        clientMap = new HashMap<>();
        companyMap = new HashMap<>();
        technicianMap = new HashMap<>();

        runtime = Runtime.instance();
        profile = new ProfileImpl(true);
        container = runtime.createAgentContainer(profile);

        God.renew();
        launchStations();
        launchClients();
        launchCompanies();
        launchTechnicians();
        launchSimulation();

        try {
            for (Technician technician : technicianAgents) technician.doDelete();
            for (Company company : companyAgents) company.doDelete();
            for (Client client : clientAgents) client.doDelete();
            for (Station station : stationAgents) station.doDelete();
            container.kill();
            runtime.shutDown();
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

            launchAgent(id, station);

            stationAgents.add(station);

            try {
                Thread.sleep(World.get().MILLI_WAIT);
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
        ClientsDesc.Strategy strategies[] = new ClientsDesc.Strategy[World.get().Cl];
        AID[] stations = new AID[World.get().Cl];

        int z = 0;
        for (ClientsDesc entry : World.get().clients) {
            clientMap.putIfAbsent(entry.strategy, new ArrayList<>());
            for (int k = 0; k < entry.number; ++k, ++z) {
                strategies[z] = entry.strategy;
            }
        }

        z = 0;
        int n = 0;
        for (StationsDesc entry : World.get().stations) {
            for (int i = 0; i < entry.number; i++, n++) {
                for (int k = 0; k < entry.numberClients; k++, z++) {
                    stations[z] = stationAgents.get(n).getAID();
                }
            }
        }

        shuffle(strategies);
        shuffle(stations);

        for (int i = 0; i < World.get().Cl; ++i) {
            String id = "client_" + (i + 1);

            Client client = new Client(id, strategies[i].make(), stations[i]);

            clientMap.get(strategies[i]).add(client);

            launchAgent(id, client);

            clientAgents.add(client);
            try {
                Thread.sleep(World.get().MILLI_WAIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Launch all world companies
     */
    private void launchCompanies() {
        CompaniesDesc.Strategy[] strategies = new CompaniesDesc.Strategy[World.get().Co];

        int z = 0;
        for (CompaniesDesc entry : World.get().companies) {
            companyMap.putIfAbsent(entry.strategy, new ArrayList<>());
            for (int i = 0; i < entry.number; i++, z++) {
                strategies[z] = entry.strategy;
            }
        }

        shuffle(strategies);

        for (int i = 0; i < World.get().Co; ++i) {
            String id = "company_" + (i + 1);

            Company company = new Company(id, strategies[i].make());

            companyMap.get(strategies[i]).add(company);

            launchAgent(id, company);

            companyAgents.add(company);

            try {
                Thread.sleep(World.get().MILLI_WAIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Launch all world technicians
     */
    private void launchTechnicians() {
        TechniciansDesc.Strategy[] strategies = new TechniciansDesc.Strategy[World.get().T];
        AID[] stations = new AID[World.get().T];
        AID[] companies = new AID[World.get().T];

        int z = 0;
        for (TechniciansDesc entry : World.get().technicians) {
            technicianMap.putIfAbsent(entry.strategy, new ArrayList<>());
            for (int i = 0; i < entry.number; i++, z++) {
                strategies[z] = entry.strategy;
            }
        }

        z = 0;
        int n = 0;
        for (StationsDesc entry : World.get().stations) {
            for (int i = 0; i < entry.number; i++, n++) {
                for (int k = 0; k < entry.numberTechnicians; k++, z++) {
                    stations[z] = stationAgents.get(n).getAID();
                }
            }
        }

        z = 0;
        n = 0;
        for (CompaniesDesc entry : World.get().companies) {
            for (int i = 0; i < entry.number; i++, n++) {
                for (int k = 0; k < entry.numberTechnicians; k++, z++) {
                    companies[z] = companyAgents.get(n).getAID();
                }
            }
        }

        shuffle(strategies);
        shuffle(stations);
        shuffle(companies);

        for (int i = 0; i < World.get().T; ++i) {
            String id = "technician_" + (i + 1);

            Technician technician = new Technician(id, stations[i], companies[i],
                                                   strategies[i].make());

            technicianMap.get(strategies[i]).add(technician);

            launchAgent(id, technician);

            technicianAgents.add(technician);

            try {
                Thread.sleep(World.get().MILLI_WAIT);
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
