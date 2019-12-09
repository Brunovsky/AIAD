package simulation;

public class AIADWorld extends World {
    public AIADWorld() {
        numberDays = 3;
        salary = 30.0;

        Cl = 150;
        clients = new ClientsDesc[] {
            new ClientsDesc(150, ClientsDesc.Strategy.UNIFORM)  //
        };

        S = 5;
        stations = new StationsDesc[] {
            new StationsDesc(5, 30)  //
        };

        Co = 6;
        companies = new CompaniesDesc[] {
            new CompaniesDesc(3, CompaniesDesc.Strategy.PREFER_EASY, 80),  //
            new CompaniesDesc(3, CompaniesDesc.Strategy.PREFER_HARD, 90),  //
        };

        assertValid();
    }
}
