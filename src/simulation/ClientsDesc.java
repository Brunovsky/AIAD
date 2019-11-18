package simulation;

import strategies.ClientStrategy;

public class ClientsDesc {
    public int number;
    public ClientStrategy strategy;

    public ClientsDesc(int number, ClientStrategy strategy) {
        this.number = number;
        this.strategy = strategy;
    }
}