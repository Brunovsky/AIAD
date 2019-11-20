package utils;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    // private static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    private static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    private static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    private static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    // private static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    // private static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    // private static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    // private static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    private static final boolean useColours = true;
    public static final String logfolder = "./log/";

    public static void print(String pre, String name, String text) {
        if (useColours) {
            System.out.println(pre + "[" + name + "]:" + ANSI_RESET + " " + text);
        } else {
            System.out.println("[" + name + "]: " + text);
        }
    }

    public static void write(String id, String text) {
        Path path = Paths.get(logfolder + id);
        try {
            Files.write(path, text.getBytes(), APPEND, CREATE);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // * debug

    public static void info(String agentName, String info) {
        print(ANSI_GREEN, agentName, info);
    }

    public static void warn(String agentName, String warn) {
        print(ANSI_YELLOW, agentName, warn);
    }

    public static void error(String agentName, String error) {
        print(ANSI_RED, agentName, error);
    }

    public static void INFO(String agentName, String info) {
        print(ANSI_GREEN_BACKGROUND + ANSI_BLACK, agentName, info);
    }

    public static void WARN(String agentName, String warn) {
        print(ANSI_YELLOW_BACKGROUND + ANSI_BLACK, agentName, warn);
    }

    public static void ERROR(String agentName, String error) {
        print(ANSI_RED_BACKGROUND + ANSI_BLACK, agentName, error);
    }

    // * colors

    public static void red(String agentName, String text) {
        print(ANSI_RED, agentName, text);
    }

    public static void green(String agentName, String text) {
        print(ANSI_GREEN, agentName, text);
    }

    public static void yellow(String agentName, String text) {
        print(ANSI_YELLOW, agentName, text);
    }

    public static void blue(String agentName, String text) {
        print(ANSI_BLUE, agentName, text);
    }

    public static void purple(String agentName, String text) {
        print(ANSI_PURPLE, agentName, text);
    }

    public static void cyan(String agentName, String text) {
        print(ANSI_CYAN, agentName, text);
    }

    public static void white(String agentName, String text) {
        print(ANSI_WHITE, agentName, text);
    }

    // * agents

    private static final boolean showClient = false;
    private static final boolean showStation = true;
    private static final boolean showCompany = true;
    private static final boolean showTechnician = false;
    private static final boolean showGod = true;

    public static void client(String agentName, String text) {
        if (!showClient) return;
        print(ANSI_WHITE, agentName, text);
    }

    public static void station(String agentName, String text) {
        if (!showStation) return;
        print(ANSI_RED, agentName, text);
    }

    public static void company(String agentName, String text) {
        if (!showCompany) return;
        print(ANSI_PURPLE, agentName, text);
    }

    public static void technician(String agentName, String text) {
        if (!showTechnician) return;
        print(ANSI_RED, agentName, text);
    }

    public static void god(String agentName, String text) {
        if (!showGod) return;
        print(ANSI_YELLOW, agentName, text);
    }
}
