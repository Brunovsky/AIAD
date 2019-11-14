package simulation;

import utils.ClientType;
import utils.TechnicianType;

public class WorldBuilder {
    World select(String name) {
        switch (name) {
        case "simple_small":
            return simpleSmall();
        case "disks_small":
            return disksSmall();
        case "simple_large":
            return simpleLarge();
        default:
            throw new IllegalArgumentException("Invalid world name: " + name);
        }
    }

    World simpleSmall() {
        World world = new World();

        world.radius = 100.0;
        world.speed = 1.0;

        world.T = 20;
        world.technicianRadius = 50.0;
        world.technicians = new TechniciansDesc[] {
            new TechniciansDesc(10, TechnicianType.TECHNICIAN_TYPE_2),  //
            new TechniciansDesc(10, TechnicianType.TECHNICIAN_TYPE_4)   //
        };

        world.C = 200;
        world.clientRadius = new double[] {30.0, 65.0};
        world.clientNumbers = new int[] {40, 60};
        world.period = 2.0;
        world.clients = new ClientsDesc[] {
            new ClientsDesc(45, ClientType.CLIENT_TYPE_1),  //
            new ClientsDesc(155, ClientType.CLIENT_TYPE_4)        //
        };

        return world;
    }

    World disksSmall() {
        World world = new World();

        // ...

        return world;
    }

    World simpleLarge() {
        World world = new World();

        // ...

        return world;
    }
}
