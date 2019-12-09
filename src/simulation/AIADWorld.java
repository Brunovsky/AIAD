package simulation;

public class AIADWorld extends World {
    public AIADWorld() {
        numberDays = 20;
        salary = 30.0;

        Cl = 24;
        clients = new ClientsDesc[] {
            new ClientsDesc(24, ClientsDesc.Strategy.UNIFORM)  //
        };

        S = 4;
        stations = new StationsDesc[] {
            new StationsDesc(4, 15, 6)  //
        };

        Co = 2;
        companies = new CompaniesDesc[] {
            new CompaniesDesc(1, CompaniesDesc.Strategy.PREFER_EASY, 30),  //
            new CompaniesDesc(1, CompaniesDesc.Strategy.PREFER_HARD, 30)   //
        };

        assertValid();
    }
}
