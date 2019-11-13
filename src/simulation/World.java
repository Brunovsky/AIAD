package simulation;

public class World {
    // * World
    double radius;  // world radius
    double speed;   // technician speed

    // * Technicians
    int T;                    // number of technicians
    double technicianRadius;  // technician circle radius
    TechniciansDesc[] technicians;

    // * Clients
    int C;                  // number of clients
    double period;          // period in which clients are launched
    double[] clientRadius;  // client circles radii
    int[] clientNumbers;    // client circles numbers
    ClientsDesc[] clients;
}
