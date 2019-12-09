package simulation;

public abstract class World {
    // Services
    static public final String CLIENT_STATION_SERVICE = "client-station-subscription";
    static public final String COMPANY_STATION_SERVICE = "company-station-subscription";

    // Types
    static public final String STATION_TYPE = "station";
    static public final String COMPANY_TYPE = "company";

    // Ontologies
    static public final String PROMPT_CLIENT = "prompt-client-malfunctions";
    static public final String INFORM_CLIENT = "inform-client-assignment";
    static public final String COMPANY_PAYMENT = "company-payment";
    static public final String INITIAL_EMPLOYMENT = "initial-employment";
    static public final String INFORM_COMPANY_JOBS = "inform-company-jobs";
    static public final String INFORM_COMPANY_ASSIGNMENT = "inform-company-assignment";
    static public final String TECHNICIAN_OFFER_CONTRACT = "technician-offer-contract";
    static public final String COMPANY_SUBSCRIPTION = "company-subscription";

    public static final String companyName(String id) {
        return "company-" + id;
    }

    public static final String stationName(String id) {
        return "station-" + id;
    }

    public static final String clientName(String id) {
        return "client-" + id;
    }

    public static final int MILLI_DELAY = 1000;
    public static final int MILLI_PERIOD = 1300;
    public static final int MILLI_WAIT = 30;

    // *****

    public static final int EASY_WEIGHT = 1;
    public static final int MEDIUM_WEIGHT = 3;
    public static final int HARD_WEIGHT = 9;
    public static final int WORKER_WEIGHT = 3;

    // Clients
    int Cl;
    ClientsDesc[] clients;

    // Stations
    int S;
    StationsDesc[] stations;

    // Companies
    int Co;
    CompaniesDesc[] companies;

    double salary;

    // * State
    int numberDays;
    int currentDay;

    private static World world;

    static void set(World newWorld) {
        world = newWorld;
    }

    public static World get() {
        assert world != null : "Called get on null world";
        return world;
    }

    public double getSalary() {
        return salary;
    }

    public int getDay() {
        return currentDay;
    }

    public void setDay(int currentDay) {
        this.currentDay = currentDay;
    }

    public int getNumberDays() {
        return numberDays;
    }

    public int getTotalAgents() {
        return S + Cl + Co;
    }

    void assertValid() {
        assert Cl > 0 && S > 0 && Co > 0;
        assert numberDays > 0 && salary > 0.0;

        assert clients != null && stations != null && companies != null;

        int cl = 0, s = 0, co = 0;
        for (ClientsDesc client : clients) cl += client.numberClients;
        for (StationsDesc station : stations) s += station.numberStations;
        for (CompaniesDesc company : companies) co += company.numberCompanies;
        assert Cl == cl && S == s && Co == co;

        assert currentDay <= numberDays;
    }
}
