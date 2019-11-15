package simulation;

import utils.MalfunctionType;

public abstract class World {
    // World
    double radius;   // world radius
    double speed;    // technician speed
    double gasCost;  // gas cost per unit of distance

    // Technicians
    int T;                    // number of technicians
    double technicianRadius;  // technician circle radius
    TechniciansDesc[] technicians;
    final String serviceType = "tech-repairs";
    final String serviceName = "TechRepairs";

    // Clients
    int C;                  // number of clients
    double period;          // time period (regular interval) in which clients are launched
    double[] clientRadius;  // client circles radii
    int[] clientNumbers;    // number of clients in the circles
    ClientsDesc[] clients;

    // Malfunctions [0,1,2] = [HARD,MEDIUM,EASY]
    int[] malfunctions;                                          // malfunction frequencies
    double[] repairDurations = new double[] {20.0, 40.0, 60.0};  // malfunction repair durations
    double[] repairBasePrices = new double[] {90, 65, 30};       // malfunction base prices

    private static World world;

    static void set(World newWorld) {
        world = newWorld;
    }

    public static World get() {
        assert world != null : "Called get on null world";
        return world;
    }

    public double getWorldRadius() {
        return radius;
    }

    public double getSpeed() {
        return speed;
    }

    public double getGasCost() {
        return gasCost;
    }

    public int numTechnicians() {
        return T;
    }

    public int numClients() {
        return C;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double malfunctionDuration(MalfunctionType type) {
        return repairDurations[type.index()];
    }

    public double malfunctionPrice(MalfunctionType type) {
        return repairBasePrices[type.index()];
    }

    public double travelDuration(double distance) {
        return distance / speed;
    }

    public double travelCost(double distance) {
        return distance * gasCost;
    }

    public double workDuration(double distance, MalfunctionType type) {
        return malfunctionDuration(type) + 2 * travelDuration(distance);
    }

    void assertValid() {
        assert T > 0 && C > 0 && radius > 0 && speed > 0 && gasCost > 0;
        assert technicianRadius > 0 && radius > technicianRadius && period > 0;
        assert technicians != null && clients != null && clientRadius != null
            && clientNumbers != null;

        int t = 0, c = 0, d = 0;
        for (TechniciansDesc tech : technicians) t += tech.number;
        for (ClientsDesc client : clients) c += client.number;
        for (int num : clientNumbers) d += num;
        assert T == t && C == c && C == d;

        for (double r : clientRadius) assert r > 0 && radius > r;
        assert malfunctions[0] >= 0 && malfunctions[1] >= 0 && malfunctions[2] >= 0;
        assert malfunctions[0] + malfunctions[1] + malfunctions[2] >= C;
        assert repairDurations[0] >= 0 && repairDurations[1] >= 0 && repairDurations[2] >= 0;
    }
}
