package simulation;

public class TechniciansDesc {
    public enum TechnicianBehaviour { BrunoT, NunoT, MiguelT }

    public int number;
    public TechnicianBehaviour behaviour;

    public TechniciansDesc(int number, TechnicianBehaviour behaviour) {
        assert behaviour != null;
        this.number = number;
        this.behaviour = behaviour;
    }
}
