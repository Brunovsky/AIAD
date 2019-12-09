package types;

import utils.Logger.Format;

public abstract class Record {
    public abstract String csv();
    public abstract String table();

    public final String format(Format format) {
        switch (format) {
        case CSV:
            return csv();
        case TABLE:
        default:
            return table();
        }
    }

    public static final String join(Format format, String a, String b) {
        switch (format) {
        case CSV:
            return a + "," + b;
        case TABLE:
        default:
            return a + "  " + b;
        }
    }

    public static final String join(Format format, Record a, Record b) {
        return join(format, a.format(format), b.format(format));
    }

    public static final String line(Format format, Record a, Record b) {
        return join(format, a, b) + '\n';
    }

    public static final String line(Format format, String a, String b) {
        return join(format, a, b) + '\n';
    }
}
