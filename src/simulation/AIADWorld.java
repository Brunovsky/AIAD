package simulation;

import strategies.ClientStrategy1;
import strategies.SimpleTechnicianStrategy;
import strategies.company.SimpleCompanyStrategy;

public class AIADWorld extends World{
    public AIADWorld() {
        numberDays = 100;

        T=160;
        technicians = new TechniciansDesc[] {
                new TechniciansDesc(160, new SimpleTechnicianStrategy())
        };

        Cl = 20;
        clients = new ClientsDesc[] {
                new ClientsDesc(20, new ClientStrategy1())
        };

        S = 4;
        stations = new StationsDesc[] {
                new StationsDesc(4, 40, 5)
        };

        Co = 5;
        companies = new CompaniesDesc[] {
                new CompaniesDesc(5, new SimpleCompanyStrategy(), 32)
        };

        assertValid();
    }
}
