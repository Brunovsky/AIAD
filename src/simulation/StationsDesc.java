package simulation;

public class StationsDesc {
    public final int numberStations;
    public final int numberTechnicians;
    public final int numberClients;

    public StationsDesc(int number, int numberTechnicians, int numberClients) {
        this.numberStations = number;
        this.numberTechnicians = numberTechnicians;
        this.numberClients = numberClients;
    }
}
