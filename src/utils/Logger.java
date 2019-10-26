package utils;

public class Logger {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    public static void info(String agentName, String info){
        System.out.println(ANSI_GREEN + "[" + agentName + "]: " + ANSI_RESET + info);
    }

    public static void warn(String agentName, String warn){
        System.out.println(ANSI_YELLOW + "[" + agentName + "]: " + ANSI_RESET + warn);
    }

    public static void error(String agentName, String error){
        System.out.println(ANSI_RED + "[" + agentName + "]: " + ANSI_RESET + error);
    }

    public static void INFO(String agentName, String info){
        System.out.println(ANSI_GREEN_BACKGROUND + ANSI_BLACK + "[" + agentName + "]:" + ANSI_RESET + " " + info);
    }

    public static void WARN(String agentName, String warn){
        System.out.println(ANSI_YELLOW_BACKGROUND + ANSI_BLACK + "[" + agentName + "]:" + ANSI_RESET + " " + warn);
    }

    public static void ERROR(String agentName, String error){
        System.out.println(ANSI_RED_BACKGROUND + "[" + agentName + "]:" + ANSI_RESET + " " + error);
    }
}
