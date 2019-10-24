package utils;

public class Utils {

    public static ClientType getClientType(String type) {
        switch (type) {
            case "reasonable":
                return ClientType.REASONABLE_UNAVAILABLE;
            case "urgent":
                return ClientType.URGENT_AVAILABLE;
            case "selfish_available":
                return ClientType.SELFISH_AVAILABLE;
            case "selfish_unavailable":
                return ClientType.SELFISH_UNAVAILABLE;
            default:
                return null;
        }
    }
}
