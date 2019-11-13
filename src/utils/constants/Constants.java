package utils.constants;

import utils.Location;
import utils.MalfunctionType;

public class Constants {

    public static String SERVICE_TYPE = "tech-repairs";

    public static String SERVICE_NAME = "TechRepairs";

    public static double PRICE_PER_UNIT_OF_DISTANCE = 0.1;

    // ------------- TO DELETE? ------------------
    public static double PRICE_EASY_MALFUNCTION = 20;

    public static double PRICE_MEDIUM_MALFUNCTION = 40;

    public static double PRICE_HARD_MALFUNCTION = 60;

    public static double PRICE_DEFAULT_MALFUNCTION = 30;

    public static double getMalFunctionPrice(MalfunctionType type){
        switch (type) {
            case EASY:
                return PRICE_EASY_MALFUNCTION;
            case MEDIUM:
                return PRICE_MEDIUM_MALFUNCTION;
            case HARD:
                return PRICE_HARD_MALFUNCTION;
            default:
                break;
        }
        return 0;
    }
    // ------------------------------------------


    public static int DURATION_EASY_MALFUNCTION_REPAIR = 20; // minutes

    public static int DURATION_MEDIUM_MALFUNCTION_REPAIR = 40; // minutes

    public static int DURATION_HARD_MALFUNCTION_REPAIR = 60; // minutes

    public static double TECHNICIAN_DURATION_PER_DISTANCE = 1; //inverso da velocidade

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

    public static double calculateDistance(Location clientLocation, Location technicianLocation) {
        return Math.sqrt(Math.pow(technicianLocation.getX() - clientLocation.getX(), 2) + Math.pow(technicianLocation.getY() - clientLocation.getY(), 2));
    }

    public static double calculateSlotDuration(double distance, MalfunctionType type) {
        return getMalfunctionDuration(type) + 2 * distance * TECHNICIAN_DURATION_PER_DISTANCE;
    }


}
