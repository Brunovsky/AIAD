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


    // Technicians
    int T;
    TechniciansDesc[] technicians;

    // Clients
    int Cl;
    ClientsDesc[] clients;

    // Stations
    int S;

    // Companies
    int Co;
    //CompaniesDesc[] companies;

    private static World world;

    static void set(World newWorld) {
        world = newWorld;
    }

    public static World get() {
        assert world != null : "Called get on null world";
        return world;
    }

    public int getDay() {
        return 1;  // TODO
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

}
