package simulation;

public abstract class World {
    private static World world;

    static void set(World newWorld) {
        world = newWorld;
    }

    public static World get() {
        assert world != null : "Called get on null world";
        return world;
    }

    public int getDay() {
        return 1;  // TODO
    }
}
