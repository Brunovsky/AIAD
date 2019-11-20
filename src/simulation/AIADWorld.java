package simulation;

public class AIADWorld extends World {
    public AIADWorld() {
        numberDays = 4;

        MILLI_DELAY = 1000;
        MILLI_PERIOD = 2000;
        MILLI_WAIT = 30;

        T = 80;
        technicians = new TechniciansDesc[] {
            new TechniciansDesc(80, TechniciansDesc.Strategy.SIMPLE)  //
        };

        Cl = 80;
        clients = new ClientsDesc[] {
            new ClientsDesc(80, ClientsDesc.Strategy.UNIFORM)  //
        };

        S = 4;
        stations = new StationsDesc[] {
            new StationsDesc(4, 20, 20)  //
        };

        Co = 4;
        companies = new CompaniesDesc[] {
            new CompaniesDesc(2, CompaniesDesc.Strategy.PREFER_EASY, 20),  //
            new CompaniesDesc(2, CompaniesDesc.Strategy.PREFER_HARD, 20)   //
        };

        assertValid();
    }
}
