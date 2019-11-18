package simulation;

public class AIADWorld extends World {
    public AIADWorld() {
        numberDays = 4;

        T = 20;
        technicians = new TechniciansDesc[] {
            new TechniciansDesc(20, TechniciansDesc.Strategy.SIMPLE)  //
        };

        Cl = 40;
        clients = new ClientsDesc[] {
            new ClientsDesc(40, ClientsDesc.Strategy.UNIFORM)  //
        };

        S = 4;
        stations = new StationsDesc[] {
            new StationsDesc(4, 5, 10)  //
        };

        Co = 5;
        companies = new CompaniesDesc[] {
            new CompaniesDesc(3, CompaniesDesc.Strategy.PREFER_EASY, 4),  //
            new CompaniesDesc(2, CompaniesDesc.Strategy.PREFER_HARD, 4)   //
        };

        assertValid();
    }
}
