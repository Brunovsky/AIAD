package simulation;

public class AIADWorld extends World {
    public AIADWorld() {
        numberDays = 100;

        Cl = 200;
        clients = new ClientsDesc[] {
            new ClientsDesc(200, ClientsDesc.Strategy.UNIFORM)  //
        };

        S = 5;
        stations = new StationsDesc[] {
            new StationsDesc(5, 40)  //
        };

        Co = 6;
        companies = new CompaniesDesc[] {
            new CompaniesDesc(3, CompaniesDesc.Strategy.PREFER_EASY, 70),  //
            new CompaniesDesc(3, CompaniesDesc.Strategy.PREFER_HARD, 70),  //
        };

        salary = 30.0;
        easyBase = 25.0;
        mediumBase = 80.0;
        hardBase = 256.0;

        assertValid();
    }
}
