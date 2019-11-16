package utils;

public enum MalfunctionType {
    // Malfunction type defines repair duration and price

    HARD(0),
    MEDIUM(1),
    EASY(2);

    private final int value;

    MalfunctionType(final int value) {
        this.value = value;
    }

    public static MalfunctionType make(int value){
        switch (value){
            case 0:
                return HARD;
            case 1:
                return MEDIUM;
            case 2:
                return EASY;
        }
        return EASY;
    }

    public int getValue() {
        return value;
    }
}
