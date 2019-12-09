package utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import utils.Logger.Format;

public class Table {
    private final String title;
    private final List<Map<String, String>> rows;

    public Table() {
        this(null);
    }

    public Table(String title) {
        this.title = title;
        this.rows = new LinkedList<>();
    }

    public String getTitle() {
        return title;
    }

    public Map<String, String> addRow() {
        HashMap<String, String> map = new HashMap<>();
        rows.add(map);
        return map;
    }

    public int numRows() {
        return rows.size();
    }

    // Merge the given table's rows into this one. This does NOT clone the rows
    public void merge(Table table) {
        rows.addAll(table.rows);
    }

    // Add the given (key,value) pair to every row in this table
    public void setAll(String key, String value) {
        for (Map<String, String> map : rows) map.put(key, value);
    }

    // Build a CSV
    public String csv(String[] keys) {
        StringBuilder builder = new StringBuilder();
        String[] elems = new String[keys.length];

        builder.append(String.join(",", keys)).append('\n');

        for (Map<String, String> map : rows) {
            for (int i = 0; i < keys.length; ++i) {
                assert map.containsKey(keys[i]);
                elems[i] = map.get(keys[i]);
            }
            builder.append(String.join(",", elems)).append('\n');
        }

        return builder.toString();
    }

    // Build a pretty table
    public String table(String[] keys, String columnDelimiter) {
        StringBuilder builder = new StringBuilder();
        int[] width = new int[keys.length];
        String[] elems = new String[keys.length];

        for (int i = 0; i < keys.length; ++i) width[i] = keys[i].length();
        for (Map<String, String> map : rows) {
            for (int i = 0; i < keys.length; ++i) {
                assert map.containsKey(keys[i]);
                String value = map.get(keys[i]);
                if (width[i] < value.length()) {
                    width[i] = value.length();
                }
            }
        }

        for (int i = 0; i < keys.length; ++i) {
            elems[i] = " ".repeat(width[i] - keys[i].length()) + keys[i];
        }
        builder.append(String.join(columnDelimiter, elems)).append('\n');

        for (Map<String, String> map : rows) {
            for (int i = 0; i < keys.length; ++i) {
                String value = map.get(keys[i]);
                elems[i] = " ".repeat(width[i] - value.length()) + value;
            }
            builder.append(String.join(columnDelimiter, elems)).append('\n');
        }

        return builder.toString();
    }

    public String table(String[] keys) {
        return table(keys, "  ");
    }

    public String output(Format format, String[] keys) {
        switch (format) {
        case CSV:
            return csv(keys);
        case TABLE:
        default:
            return table(keys);
        }
    }
}
