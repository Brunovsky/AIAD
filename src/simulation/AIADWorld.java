package simulation;

public class AIADWorld extends World {
    public AIADWorld() {
        numberDays = 100;

        T = 160;
        technicians = new TechniciansDesc[] {
            new TechniciansDesc(160, TechniciansDesc.Strategy.SIMPLE)  //
        };

        Cl = 20;
        clients = new ClientsDesc[] {
            new ClientsDesc(20, ClientsDesc.Strategy.UNIFORM)  //
        };

        S = 4;
        stations = new StationsDesc[] {
            new StationsDesc(4, 40, 5)  //
        };

        Co = 5;
        companies = new CompaniesDesc[] {
            new CompaniesDesc(3, CompaniesDesc.Strategy.PREFER_EASY, 32),  //
            new CompaniesDesc(2, CompaniesDesc.Strategy.PREFER_HARD, 32)   //
        };

        assertValid();
    }
}
