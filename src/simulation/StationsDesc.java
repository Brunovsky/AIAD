package simulation;

public class StationsDesc {
    private int numberStations;
    private int numberTechnicians;
    private int numberClients;

    public StationsDesc(int numberStations, int numberTechnicians, int numberClients) {
        this.numberStations = numberStations;
        this.numberTechnicians = numberTechnicians;
        this.numberClients = numberClients;
    }

    public int getNumberStations() {
        return numberStations;
    }

    public int getNumberTechnicians() {
        return numberTechnicians;
    }

    public int getNumberClients() {
        return numberClients;
    }
}
