package simulation;

public class World {
    // World
    double radius;  // world radius
    double speed;   // technician speed

    // Technicians
    int T;                    // number of technicians
    double technicianRadius;  // technician circle radius
    TechniciansDesc[] technicians;

    // Clients
    int C;                  // number of clients
    double period;          // time period (regular interval) in which clients are launched
    double[] clientRadius;  // client circles radii
    int[] clientNumbers;    // number of clients in the circles
    int[] malfunctions;     // absolute frequency of each malfunction
    ClientsDesc[] clients;
}
