package simulation;

import strategies.SimpleCompanyStrategy;
import strategies.SimpleTechnicianStrategy;
import strategies.UmNomeParaMeter;
import strategies.UniformClientStrategy;

public class AIADWorld extends World {
    public AIADWorld() {
        numberDays = 100;

        T = 160;
        technicians = new TechniciansDesc[]{
                new TechniciansDesc(160, new SimpleTechnicianStrategy())  //
        };

        Cl = 20;
        clients = new ClientsDesc[]{
                new ClientsDesc(20, new UniformClientStrategy())  //
        };

        S = 4;
        stations = new StationsDesc[]{
                new StationsDesc(4, 40, 5)  //
        };

        Co = 5;
        companies = new CompaniesDesc[]{
                new CompaniesDesc(3, new SimpleCompanyStrategy(), 32),
                new CompaniesDesc(2, new UmNomeParaMeter(), 32)
        };

        assertValid();
    }
}
