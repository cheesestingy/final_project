package final_project;

public class GameConfig {
    public static final int WINDOW_WIDTH = 750;
    public static final int WINDOW_HEIGHT = 800;

    public static final int PLAYFIELD_WIDTH = 450;
    public static final int PLAYFIELD_HEIGHT = 700;

    public static final double PLAYFIELD_MIN_X = (WINDOW_WIDTH - PLAYFIELD_WIDTH) / 2.0;
    public static final double PLAYFIELD_MIN_Y = 50;
    public static final double PLAYFIELD_MAX_X = PLAYFIELD_MIN_X + PLAYFIELD_WIDTH;
    public static final double PLAYFIELD_MAX_Y = PLAYFIELD_MIN_Y + PLAYFIELD_HEIGHT;

    public static final double BLOCK_SIZE = PLAYFIELD_WIDTH / 8.0;
    public static final int BALL_RADIUS = 11;
    public static final double BALL_SPEED = 8.0;

    public static final double WARNING_LINE_Y = PLAYFIELD_MAX_Y - BLOCK_SIZE - 20;

    public static final double FIRE_EXPLOSION_RADIUS = BLOCK_SIZE * 3;
    public static final double BURN_PERCENT = 0.42;

    public static final double ICE_EXPLOSION_RADIUS = BLOCK_SIZE * 2.0;

    public static final long PIERCE_DAMAGE_COOLDOWN_MS = 210;
}