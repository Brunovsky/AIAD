package simulation;

import strategies.TechnicianStrategy;

public class TechniciansDesc {
    public int number;
    public TechnicianStrategy strategy;

    public TechniciansDesc(int number, TechnicianStrategy strategy) {
        this.number = number;
        this.strategy = strategy;
    }
}
