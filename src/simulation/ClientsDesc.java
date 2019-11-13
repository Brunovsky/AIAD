package simulation;

public class ClientsDesc {
    public enum ClientBehaviour { BrunoC, NunoC, MiguelC }

    public int number;
    public ClientBehaviour behaviour;

    public ClientsDesc(int number, ClientBehaviour behaviour) {
        assert behaviour != null;
        this.number = number;
        this.behaviour = behaviour;
    }
}
