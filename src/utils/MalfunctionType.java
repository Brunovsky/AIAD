package utils;

public enum MalfunctionType {
    // Malfunction type defines repair duration and price

    HARD,
    MEDIUM,
    EASY;

    public int index() {
        switch (this) {
        case HARD:
            return 0;
        case MEDIUM:
            return 1;
        case EASY:
            return 2;
        }
        return 0;
    }
}
