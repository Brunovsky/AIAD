package utils.constants;

import utils.MalfunctionType;

public class Constants {

    public static int PRICE_EASY_MALFUNCTION_REPAIR = 10; // €

    public static int PRICE_MEDIUM_MALFUNCTION_REPAIR = 20; // €

    public static int PRICE_HARD_MALFUNCTION_REPAIR = 30; // €

    public static double PRICE_PER_UNIT_OF_DISTANCE = 0.5;

    public static int DURATION_EASY_MALFUNCTION_REPAIR = 20; // minutes

    public static int DURATION_MEDIUM_MALFUNCTION_REPAIR = 40; // minutes

    public static int DURATION_HARD_MALFUNCTION_REPAIR = 60; // minutes

    public static int getMalfunctionDuration(MalfunctionType type) {
        switch (type) {
            case EASY:
                return DURATION_EASY_MALFUNCTION_REPAIR;
            case MEDIUM:
                return DURATION_MEDIUM_MALFUNCTION_REPAIR;
            case HARD:
                return DURATION_HARD_MALFUNCTION_REPAIR;
            default:
                break;
        }
        return 0;
    }

    public static int getMalfunctionPrice(MalfunctionType type) {
        switch (type) {
            case EASY:
                return PRICE_EASY_MALFUNCTION_REPAIR;
            case MEDIUM:
                return PRICE_MEDIUM_MALFUNCTION_REPAIR;
            case HARD:
                return PRICE_HARD_MALFUNCTION_REPAIR;
            default:
                break;
        }
        return 0;
    }

}
