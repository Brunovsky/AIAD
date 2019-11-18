package simulation;

public abstract class World {

    // Services
    final String clientStationService = "client-station-subscription";
    final String companyStationService = "company-station-subscription";

    // Types
    final String stationType = "station";
    final String companyType = "company";

    // Ontologies
    final String promptClient = "prompt-client-malfunctions";
    final String informClient = "inform-client-assignment";
    final String companyPayment = "company-payment";
    final String initialEmployment = "initial-employment";
    final String informCompanyJobs = "inform-company-jobs";
    final String informCompanyAssignment = "inform-company-assignment";
    final String technicianOfferContract = "technician-offer-contract";
    final String companySubscription = "company-subscription";


    int numberDays;
    int currentDay = 0;

    // Technicians
    int T;
    TechniciansDesc[] technicians;

    // Clients
    int Cl;
    ClientsDesc[] clients;

    // Stations
    int S;
    StationsDesc[] stations;

    // Companies
    int Co;
    CompaniesDesc[] companies;

    private static World world;

    static void set(World newWorld) {
        world = newWorld;
    }

    public static World get() {
        assert world != null : "Called get on null world";
        return world;
    }

    public int getDay() {
        return currentDay;
    }

    public void setDay(int currentDay) {
        this.currentDay = currentDay;
    }

    public String getClientStationService() {
        return clientStationService;
    }

    public String getPromptClient() {
        return promptClient;
    }

    public String getInformClient() {
        return informClient;
    }

    public String getCompanyPayment() {
        return companyPayment;
    }

    public String getInitialEmployment() {
        return initialEmployment;
    }

    public String getInformCompanyJobs() {
        return informCompanyJobs;
    }

    public String getInformCompanyAssignment() {
        return informCompanyAssignment;
    }

    public String getTechnicianOfferContract() {
        return technicianOfferContract;
    }

    public String getCompanySubscription() {
        return companySubscription;
    }

    public String getStationType() {
        return stationType;
    }

    public String getCompanyType() {
        return companyType;
    }

    public String getCompanyStationService() {
        return companyStationService;
    }

    public int getNumberDays() {
        return numberDays;
    }

    void assertValid(){
        assert T>0 && Cl>0 && S>0 && Co>0;

        assert technicians != null && clients != null && stations != null && companies != null;

        int t = 0, cl = 0, s = 0, co = 0;
        for (TechniciansDesc tech : technicians) t += tech.number;
        for (ClientsDesc client : clients) cl += client.number;
        for (StationsDesc station : stations) s += station.number;
        for (CompaniesDesc company : companies) co += company.number;
        assert T == t && Cl == cl && S == s && Co == co;

        assert currentDay <= numberDays;
    }

}
