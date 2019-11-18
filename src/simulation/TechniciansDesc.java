package simulation;

import strategies.SimpleTechnicianStrategy;
import strategies.TechnicianStrategy;

public class TechniciansDesc {
    public int number;
    public Strategy strategy;

    public enum Strategy {
        SIMPLE;

        public TechnicianStrategy make() {
            switch (this) {
            case SIMPLE:
                return new SimpleTechnicianStrategy();
            }
            return null;
        }
    }

    public TechniciansDesc(int number, Strategy strategy) {
        this.number = number;
        this.strategy = strategy;
    }
}
