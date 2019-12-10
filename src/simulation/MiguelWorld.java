package simulation;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import utils.Logger;

public class MiguelWorld extends World {
    public MiguelWorld() {
        final Random random = ThreadLocalRandom.current();

        easyBase = 25.0;
        mediumBase = 80.0;
        hardBase = 256.0;

        // Days: 1...20
        numberDays = random.nextInt(20 - 1) + 1;

        // Salary: 15...51
        salary = random.nextInt(51 - 15) + 15;

        // Stations: 4...10
        S = (random.nextInt(10 - 4) + 4);

        // Clients: 5...30 per station
        Cl = (random.nextInt(30 - 5) + 5) * S;

        clients = new ClientsDesc[] {
            new ClientsDesc(Cl, ClientsDesc.Strategy.UNIFORM)  //
        };

        // Same number of clients per station
        stations = new StationsDesc[] {
            new StationsDesc(S, Cl / S),
        };

        // Companies: 1...4 for each strategy +
        int Co1 = random.nextInt(4 - 1) + 1;
        int Co2 = random.nextInt(4 - 1) + 1;
        int Co3 = random.nextInt(4 - 1) + 1;
        int Co4 = random.nextInt(4 - 1) + 1;
        Co = Co1 + Co2 + Co3 + Co4;

        companies = new CompaniesDesc[] {
            new CompaniesDesc(Co1, CompaniesDesc.Strategy.PREFER_EASY,  //
                              random.nextInt(200 - 20) + 20),
            new CompaniesDesc(Co2, CompaniesDesc.Strategy.PREFER_EASY,  //
                              random.nextInt(200 - 20) + 20),
            new CompaniesDesc(Co3, CompaniesDesc.Strategy.PREFER_HARD,  //
                              random.nextInt(200 - 20) + 20),
            new CompaniesDesc(Co4, CompaniesDesc.Strategy.PREFER_HARD,  //
                              random.nextInt(200 - 20) + 20),
        };

        assertValid();
    }

    @Override
    void assertValid() {
        super.assertValid();
        Logger.white("World", String.format("Companies: %d", Co));
        Logger.white("World", String.format("Stations: %d", S));
        Logger.white("World", String.format("Clients: %d", Cl));
        Logger.white("World", String.format("Salary: %.2f", salary));
    }
}
