package simulation;

import utils.TechnicianType;

public class TechniciansDesc {
    public int number;
    public TechnicianType personality;

    public TechniciansDesc(int number, TechnicianType personality) {
        assert personality != null;
        this.number = number;
        this.personality = personality;
    }
}
