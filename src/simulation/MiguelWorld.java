package simulation;

import java.util.Random;

public class MiguelWorld extends World {
  public MiguelWorld() {
    Random random = new Random();
    numberDays = random.nextInt(20) + 1;
    salary = random.nextInt(51 - 15) + 15;

    S = (random.nextInt(5 - 2) + 2) * 3;

    Cl = (random.nextInt(200 - 20) + 20) * S;
    clients = new ClientsDesc[] { new ClientsDesc(Cl, ClientsDesc.Strategy.UNIFORM) //
    };

    stations = new StationsDesc[] { new StationsDesc(S / 3, Cl / S), new StationsDesc(S / 3, Cl / S),
        new StationsDesc(S / 3, Cl / S) };

    Co = (random.nextInt(10) + 1) * 4;
    companies = new CompaniesDesc[] {
        new CompaniesDesc(Co / 4, CompaniesDesc.Strategy.PREFER_EASY, random.nextInt(200 - 20) + 20), //
        new CompaniesDesc(Co / 4, CompaniesDesc.Strategy.PREFER_HARD, random.nextInt(200 - 20) + 20),
        new CompaniesDesc(Co / 4, CompaniesDesc.Strategy.PREFER_HARD, random.nextInt(200 - 20) + 20),
        new CompaniesDesc(Co / 4, CompaniesDesc.Strategy.PREFER_HARD, random.nextInt(200 - 20) + 20) };

    System.out.println(numberDays);
    System.out.println(salary);
    System.out.println(S);
    System.out.println(Co);
    System.out.println(Cl);
    System.out.println("TIME: " + (MILLI_WAIT * (S + Co + Cl) + (2 * numberDays + 2) * MILLI_PERIOD));
    assertValid();
  }
}
