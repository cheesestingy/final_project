package final_project;

public class GameConfig {
    public static final int WINDOW_WIDTH = 850;
    public static final int WINDOW_HEIGHT = 800;

    public static final int PLAYFIELD_WIDTH = 450;
    public static final int PLAYFIELD_HEIGHT = 700;

    public static final double PLAYFIELD_MIN_X = (WINDOW_WIDTH - PLAYFIELD_WIDTH) / 2.0;
    public static final double PLAYFIELD_MIN_Y = 50;
    public static final double PLAYFIELD_MAX_X = PLAYFIELD_MIN_X + PLAYFIELD_WIDTH;
    public static final double PLAYFIELD_MAX_Y = PLAYFIELD_MIN_Y + PLAYFIELD_HEIGHT;

    public static final int BLOCK_HEALTH_LINEAR_END_WAVE = 10;
    public static final double BLOCK_HEALTH_GROWTH_RATE = 1.3;

    public static final double BLOCK_SIZE = PLAYFIELD_WIDTH / 8.0;
    public static final int BALL_RADIUS = 11;
    public static final double BALL_SPEED = 8.0;

    public static final int BOSS_INTERVAL = 10;
    // Boss1 分裂數量
    public static final int BOSS1_SPLIT_COUNT = 6;

    // 小Boss血量倍率
    public static final double BOSS1_SPLIT_HP_RATIO = 0.2;

    public static final int BOSS2_STONE_COUNT = 3;

    public static final double BOSS_HP_MULTIPLIER_MIN = 20.0;
    public static final double BOSS_HP_MULTIPLIER_MID = 30.0;
    public static final double BOSS_HP_MULTIPLIER_MAX = 40.0;



    public static final double WARNING_LINE_Y = PLAYFIELD_MAX_Y - BLOCK_SIZE - 20;

    public static final double FIRE_EXPLOSION_RADIUS = BLOCK_SIZE * 3;
    public static final double BURN_PERCENT = 0.42;

    public static final double ICE_EXPLOSION_RADIUS = BLOCK_SIZE * 2.0;

    public static final long PIERCE_DAMAGE_COOLDOWN_MS = 210;

    public static final double SHRINK_BALL_SIZE = 0.3;

    public static final double BALL_DAMAGE_COST_MULTIPLIER = 1.2;
    public static final double POWER_UP_COST_MULTIPLIER = 1.45;
    public static final double COIN_REWARD_COST_MULTIPLIER = 1.4;

    public static final int BALL_DAMAGE_START = 1;
    public static final double BALL_DAMAGE_UPGRADE_MULTIPLIER = 1.05;
    public static final int BALL_DAMAGE_MAX = 67676767;

    public static final int BALL_DAMAGE_UPGRADE_COST_START = 25;


    public static final double POWER_UP_SPAWN_CHANCE_START = 0.2;
    public static final double POWER_UP_SPAWN_CHANCE_UPGRADE_AMOUNT = 0.04;
    public static final double POWER_UP_SPAWN_CHANCE_MAX = 0.60;

    public static final int POWER_UP_UPGRADE_COST_START = 80;

    public static final double COIN_REWARD_MULTIPLIER_START = 2.0;
    public static final double COIN_REWARD_MULTIPLIER_UPGRADE_AMOUNT = 0.2;
    public static final double COIN_REWARD_MULTIPLIER_MAX = 4.0;

    public static final int COIN_REWARD_UPGRADE_COST_START = 100;

}