package utils;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class SimulationTables {
    private static final Map<String, Map<String, Table>> daily = new ConcurrentSkipListMap<>();

    public static Map<String, Table> getCompany(String company) {
        return daily.computeIfAbsent(company, s -> new ConcurrentSkipListMap<>());
    }

    public static Table getDaily(String company, String station) {
        return daily.computeIfAbsent(company, s -> new ConcurrentSkipListMap<>())
            .computeIfAbsent(station, s -> new Table(company + " - " + station));
    }

    public enum Format { CSV, TABLE }
    // public static final Format AGGREGATE_FORMAT = Format.TABLE;
    // public static final Format COMPANY_FORMAT = Format.TABLE;
    public static final Format AGGREGATE_FORMAT = Format.CSV;
    public static final Format COMPANY_FORMAT = Format.CSV;

    public static void writeGlobal(Table table) {
        String output = table.output(AGGREGATE_FORMAT, WORLDS_KEYS);
        Logger.write(Logger.COMPANIES_AGGREGATE_FILE, output);
    }

    // GLOBAL TABLE

    public static final String[] WORLDS_KEYS = new String[] {
        "world",      // Simulation
        "days",       // World[i]
        "company",    // World[i] > Company[c]
        "strategy",   // World[i] > Company[c]
        "techns",     // World[i] > Company[c]
        "salary",     // World[i]
        "cost",       // World[i] > Company[c].totalFinance()
        "revenue",    // World[i] > Company[c].totalFinance()
        "proposal",   // World[i] > Company[c].totalFinance()
        "assigned",   // World[i] > Company[c].totalFinance()
        "worker",     // World[i] > Company[c].totalFinance()
        "companies",  // World[i]
        "clients",    // World[i]
        "alltechns",  // World[i]
        "stations",   // World[i]
    };

    public static final String[] DAILY_KEYS = new String[] {
        "day",           // Company[c].StationHistory[s].WorkdayFinance[d]
        "techns",        // Company[c].StationHistory[s].WorkdayFinance[d]
        "cost",          // Company[c].StationHistory[s].WorkdayFinance[d]
        "revenue",       // Company[c].StationHistory[s].WorkdayFinance[d]
        "proposal",      // Company[c].StationHistory[s].WorkdayFinance[d]
        "assigned",      // Company[c].StationHistory[s].WorkdayFinance[d]
        "worker",        // Company[c].StationHistory[s].WorkdayFinance[d]
        "easy_price",    // Company[c].Strategy
        "medium_price",  // Company[c].Strategy
        "hard_price",    // Company[c].Strategy
    };
}
