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
import strategies.ClientStrategy;
import strategies.CompanyStrategy;
import strategies.TechnicianStrategy;

public class Simulation {
    private ArrayList<Technician> technicianAgents;
    private ArrayList<Client> clientAgents;
    private ArrayList<Station> stationAgents;
    private ArrayList<Company> companyAgents;

    private Map<ClientStrategy, ArrayList<Client>> clientMap;
    private Map<CompanyStrategy, ArrayList<Company>> companyMap;
    private Map<TechnicianStrategy, ArrayList<Technician>> technicianMap;

    private Runtime runtime;
    private Profile profile;
    private ContainerController container;

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

        launchStations();
        launchClients();
        launchTechnicians();
        launchCompanies();

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

    // shuffle array of ints
    private void shuffle(int[] array) {
        final Random rng = ThreadLocalRandom.current();
        for (int i = array.length - 1; i > 0; --i) {
            int j = rng.nextInt(i + 1);
            int a = array[j];
            array[j] = array[i];
            array[i] = a;
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
        for (int i = 0; i < World.get().S; ++i) {
            String id = "station_" + (i + 1);

            Station station = new Station(id);

            launchAgent(id, station);

            stationAgents.add(station);
        }
    }

    /**
     * Launch all world clients according to the world specification with randomly ordered
     * locations.
     */
    private void launchClients() {
        ClientStrategy strategies[] = new ClientStrategy[World.get().Cl];
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

            Client client = new Client(id, strategies[i], stations[i]);

            clientMap.get(strategies[i]).add(client);

            launchAgent(id, client);

            clientAgents.add(client);
        }
    }

    /**
     * Launch all world companies
     */
    private void launchCompanies() {
        CompanyStrategy[] strategies = new CompanyStrategy[World.get().Co];

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

            Company company = new Company(id, strategies[i]);

            companyMap.get(strategies[i]).add(company);

            launchAgent(id, company);

            companyAgents.add(company);
        }
    }

    private void launchTechnicians() {
        TechnicianStrategy[] strategies = new TechnicianStrategy[World.get().T];
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

        for (int i = 0; i < World.get().Co; ++i) {
            String id = "technician_" + (i + 1);

            Technician technician = new Technician(id, stations[i], companies[i], strategies[i]);

            technicianMap.get(strategies[i]).add(technician);

            launchAgent(id, technician);

            technicianAgents.add(technician);
        }
    }

    //    public class ClientWaiter implements Client.Callback {
    //        private Lock lock;
    //        private Condition condition;
    //
    //        private ClientWaiter() {
    //            lock = new ReentrantLock();
    //            condition = lock.newCondition();
    //        }
    //
    //        private boolean await() {
    //            try {
    //                lock.lock();
    //                condition.await();
    //                return true;
    //            } catch (InterruptedException e) {
    //                e.printStackTrace();
    //                return false;
    //            } finally {
    //                lock.unlock();
    //            }
    //        }
    //
    //        private void signal() {
    //            try {
    //                lock.lock();
    //                condition.signal();
    //            } finally {
    //                lock.unlock();
    //            }
    //        }
    //
    //        public void run() {
    //            signal();
    //        }
    //    }
}
