package simulation;

import strategies.ClientStrategy;
import strategies.PoissonClientStrategy;
import strategies.UniformClientStrategy;

public class ClientsDesc {
    public final int numberClients;
    public final Strategy strategy;

    public enum Strategy {
        UNIFORM,
        POISSON;

        public ClientStrategy make() {
            switch (this) {
            case UNIFORM:
                return new UniformClientStrategy();
            case POISSON:
                return new PoissonClientStrategy();
            }
            return null;
        }
    }

    public ClientsDesc(int number, Strategy strategy) {
        this.numberClients = number;
        this.strategy = strategy;
    }
}
