package simulation;

import utils.ClientType;
import utils.TechnicianType;

public class PortoWorld extends World {
    public PortoWorld() {
        radius = 100.0;
        speed = 1.0;
        gasCost = 2.0;

        T = 20;
        technicianRadius = 50.0;
        technicians = new TechniciansDesc[] {
            new TechniciansDesc(5, TechnicianType.TECHNICIAN_TYPE_1),  //
            new TechniciansDesc(5, TechnicianType.TECHNICIAN_TYPE_2),  //
            new TechniciansDesc(5, TechnicianType.TECHNICIAN_TYPE_3),  //
            new TechniciansDesc(5, TechnicianType.TECHNICIAN_TYPE_4)   //
        };

        C = 200;
        period = 1.0;
        clientRadius = new double[] {30.0, 65.0};
        clientNumbers = new int[] {80, 120};
        clients = new ClientsDesc[] {
            new ClientsDesc(45, ClientType.CLIENT_TYPE_1),  //
            new ClientsDesc(155, ClientType.CLIENT_TYPE_4)  //
        };

        malfunctions = new int[] {30, 70, 100};
        repairDurations = new double[] {20.0, 40.0, 60.0};

        assertValid();
    }
}
