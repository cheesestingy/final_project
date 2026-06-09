package final_project;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class main extends Application {

    // Window and Playfield Dimensions
    private static final int WINDOW_WIDTH = 750;
    private static final int WINDOW_HEIGHT = 800;

    private static final int PLAYFIELD_WIDTH = 450;
    private static final int PLAYFIELD_HEIGHT = 700;

    private static final double PLAYFIELD_MIN_X = (WINDOW_WIDTH - PLAYFIELD_WIDTH) / 2.0;
    private static final double PLAYFIELD_MIN_Y = 50;
    private static final double PLAYFIELD_MAX_X = PLAYFIELD_MIN_X + PLAYFIELD_WIDTH;
    private static final double PLAYFIELD_MAX_Y = PLAYFIELD_MIN_Y + PLAYFIELD_HEIGHT;

    private static final double BLOCK_SIZE = PLAYFIELD_WIDTH / 8.0;
    private static final int BALL_RADIUS = 11;
    private static final double BALL_SPEED = 5.0;

    // Warning line placed roughly one block's height above the floor
    private static final double WARNING_LINE_Y = PLAYFIELD_MAX_Y - BLOCK_SIZE - 20;

    private Pane root;
    private Polyline aimPath;
    private Text waveText;
    private Text highScoreText;
    private Text moneyText;
    private Text remainingBallsText;
    private Button upgradeDamageBtn;

    private VBox gameOverMenu;
    private Text gameOverWaveText;

    private List<Ball> balls = new ArrayList<>();
    private List<Block> blocks = new ArrayList<>();

    // Game Progression Variables
    private int wave = 1;
    private int highestWave = 1;
    private int totalBalls = 1;
    private int money = 0;
    private int ballDamage = 1;
    private int damageUpgradeCost = 50;

    private double startX = PLAYFIELD_MIN_X + (PLAYFIELD_WIDTH / 2.0);
    private final double startY = PLAYFIELD_MAX_Y - BALL_RADIUS;
    private double nextStartX = startX;
    private boolean firstBallLanded = false;

    private enum GameState { AIMING, SHOOTING, WAITING, GAME_OVER }
    private GameState state = GameState.AIMING;

    private double aimVx, aimVy;
    private int ballsFired = 0;
    private int fireDelayCounter = 0;
    private int extraBallsEarned = 0;

    private Random random = new Random();

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();
        root.setStyle("-fx-background-color: #0f0f1a;");

        // Playfield Background
        Rectangle playfield = new Rectangle(PLAYFIELD_MIN_X, PLAYFIELD_MIN_Y, PLAYFIELD_WIDTH, PLAYFIELD_HEIGHT);
        playfield.setFill(Color.web("#1a1a2e"));
        playfield.setStroke(Color.WHITE);
        playfield.setStrokeWidth(2);
        root.getChildren().add(playfield);

        // Visual Warning Line
        Line warningLine = new Line(PLAYFIELD_MIN_X, WARNING_LINE_Y, PLAYFIELD_MAX_X, WARNING_LINE_Y);
        warningLine.setStroke(Color.web("#e74c3c")); // Red
        warningLine.setStrokeWidth(2);
        warningLine.getStrokeDashArray().addAll(8d, 8d); // Dashed effect
        root.getChildren().add(warningLine);

        setupUI();
        setupGameOverMenu();

        aimPath = new Polyline();
        aimPath.setStroke(Color.WHITE);
        aimPath.setStrokeWidth(2);
        aimPath.getStrokeDashArray().addAll(10d, 10d);
        aimPath.setVisible(false);
        root.getChildren().add(aimPath);

        root.setOnMousePressed(this::handleMousePress);
        root.setOnMouseDragged(this::handleMouseDrag);
        root.setOnMouseReleased(this::handleMouseRelease);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("Block Breaker Final Project");
        primaryStage.setScene(scene);
        primaryStage.show();

        balls.add(new Ball(startX, startY));
        spawnRow();
        updateRemainingBallsUI();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
        timer.start();
    }

    private void setupUI() {
        // Left Side UI (Wave & High Score)
        waveText = new Text("Wave: " + wave);
        waveText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        waveText.setFill(Color.WHITE);
        waveText.setX(20);
        waveText.setY(80);

        highScoreText = new Text("Highest: " + highestWave);
        highScoreText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        highScoreText.setFill(Color.LIGHTGRAY);
        highScoreText.setX(20);
        highScoreText.setY(110);

        // Right Side UI (Economy & Shop)
        moneyText = new Text("Money: $0");
        moneyText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        moneyText.setFill(Color.GOLD);
        moneyText.setX(PLAYFIELD_MAX_X + 20);
        moneyText.setY(80);

        Text shopTitle = new Text("SHOP");
        shopTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        shopTitle.setFill(Color.WHITE);
        shopTitle.setX(PLAYFIELD_MAX_X + 20);
        shopTitle.setY(130);

        upgradeDamageBtn = new Button();
        updateShopUI();
        upgradeDamageBtn.setLayoutX(PLAYFIELD_MAX_X + 20);
        upgradeDamageBtn.setLayoutY(150);
        upgradeDamageBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        upgradeDamageBtn.setOnAction(e -> handleUpgradePurchase());

        // Balls Remaining Text
        remainingBallsText = new Text("x" + totalBalls);
        remainingBallsText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        remainingBallsText.setFill(Color.WHITE);

        root.getChildren().addAll(waveText, highScoreText, moneyText, shopTitle, upgradeDamageBtn, remainingBallsText);
    }

    private void setupGameOverMenu() {
        gameOverMenu = new VBox(20);
        gameOverMenu.setAlignment(Pos.CENTER);
        gameOverMenu.setLayoutX(PLAYFIELD_MIN_X);
        gameOverMenu.setLayoutY(PLAYFIELD_MIN_Y);
        gameOverMenu.setPrefSize(PLAYFIELD_WIDTH, PLAYFIELD_HEIGHT);
        gameOverMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        gameOverMenu.setVisible(false);

        Text title = new Text("GAME OVER");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        title.setFill(Color.web("#e74c3c"));

        gameOverWaveText = new Text();
        gameOverWaveText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gameOverWaveText.setFill(Color.WHITE);

        Button retryBtn = new Button("Retry");
        retryBtn.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        retryBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        retryBtn.setOnAction(e -> resetGame());

        gameOverMenu.getChildren().addAll(title, gameOverWaveText, retryBtn);
        root.getChildren().add(gameOverMenu);
    }

    private void handleUpgradePurchase() {
        if (state == GameState.GAME_OVER) return;

        if (money >= damageUpgradeCost) {
            money -= damageUpgradeCost;
            ballDamage++;
            damageUpgradeCost += 50;
            updateShopUI();
        }
    }

    private void updateShopUI() {
        moneyText.setText("Money: $" + money);
        upgradeDamageBtn.setText("Ball Damage +\nCost: $" + damageUpgradeCost + "\nCurrent: " + ballDamage);
        upgradeDamageBtn.setDisable(money < damageUpgradeCost);
    }

    private void updateRemainingBallsUI() {
        if (state == GameState.WAITING || state == GameState.GAME_OVER) {
            remainingBallsText.setVisible(false);
            return;
        }

        remainingBallsText.setVisible(true);
        remainingBallsText.setX(startX - 8);
        remainingBallsText.setY(startY - 15);

        if (state == GameState.AIMING) {
            remainingBallsText.setText("x" + totalBalls);
        } else if (state == GameState.SHOOTING) {
            remainingBallsText.setText("x" + (totalBalls - ballsFired));
        }
    }

    private void handleMousePress(MouseEvent e) {
        if (state == GameState.AIMING) {
            aimPath.setVisible(true);
            updateAim(e.getX(), e.getY());
        }
    }

    private void handleMouseDrag(MouseEvent e) {
        if (state == GameState.AIMING) {
            updateAim(e.getX(), e.getY());
        }
    }

    private void handleMouseRelease(MouseEvent e) {
        if (state == GameState.AIMING && aimPath.isVisible()) {
            aimPath.setVisible(false);
            if (aimVy < -1.0) {
                state = GameState.SHOOTING;
                ballsFired = 0;
                fireDelayCounter = 0;
                firstBallLanded = false;
                extraBallsEarned = 0;
                updateRemainingBallsUI();
            }
        }
    }

    private void updateAim(double mx, double my) {
        double dx = mx - startX;
        double dy = my - startY;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length > 0) {
            aimVx = -(dx / length) * BALL_SPEED;
            aimVy = -(dy / length) * BALL_SPEED;

            aimPath.getPoints().clear();
            aimPath.getPoints().addAll(startX, startY);

            double simX = startX;
            double simY = startY;
            double dirX = -(dx / length);
            double dirY = -(dy / length);

            Circle dummy = new Circle(0, 0, BALL_RADIUS);
            double stepLength = 2.0;
            int maxSteps = 400;

            for (int i = 0; i < maxSteps; i++) {
                simX += dirX * stepLength;
                simY += dirY * stepLength;
                dummy.setCenterX(simX);
                dummy.setCenterY(simY);

                boolean bounced = false;

                if (simX - BALL_RADIUS <= PLAYFIELD_MIN_X) {
                    simX = PLAYFIELD_MIN_X + BALL_RADIUS;
                    dirX = -dirX;
                    bounced = true;
                } else if (simX + BALL_RADIUS >= PLAYFIELD_MAX_X) {
                    simX = PLAYFIELD_MAX_X - BALL_RADIUS;
                    dirX = -dirX;
                    bounced = true;
                }

                if (simY - BALL_RADIUS <= PLAYFIELD_MIN_Y) {
                    simY = PLAYFIELD_MIN_Y + BALL_RADIUS;
                    dirY = -dirY;
                    bounced = true;
                }

                if (bounced) { aimPath.getPoints().addAll(simX, simY); }

                if (simY + BALL_RADIUS >= PLAYFIELD_MAX_Y) {
                    aimPath.getPoints().addAll(simX, simY);
                    break;
                }

                boolean hitBlock = false;
                for (Block block : blocks) {
                    if (isIntersecting(dummy, block.rect)) {
                        hitBlock = true;
                        break;
                    }
                }

                if (hitBlock) {
                    aimPath.getPoints().addAll(simX, simY);
                    break;
                }
            }

            double lastY = aimPath.getPoints().get(aimPath.getPoints().size() - 1);
            double lastX = aimPath.getPoints().get(aimPath.getPoints().size() - 2);
            if (lastX != simX || lastY != simY) {
                aimPath.getPoints().addAll(simX, simY);
            }
        }
    }

    private void update() {
        if (state == GameState.SHOOTING) {
            fireDelayCounter++;
            if (fireDelayCounter >= 12 && ballsFired < totalBalls) {
                Ball b = balls.get(ballsFired);
                b.vx = aimVx;
                b.vy = aimVy;
                b.active = true;
                ballsFired++;
                fireDelayCounter = 0;
                updateRemainingBallsUI();
            }
            if (ballsFired >= totalBalls) {
                state = GameState.WAITING;
                updateRemainingBallsUI();
            }
        }

        if (state == GameState.SHOOTING || state == GameState.WAITING) {
            boolean allBallsStopped = true;

            for (Ball b : balls) {
                if (b.active) {
                    allBallsStopped = false;
                    moveBall(b);
                }
            }

            if (allBallsStopped && state == GameState.WAITING) {
                endWave();
            }
        }
    }

    private void moveBall(Ball b) {
        b.circle.setCenterX(b.circle.getCenterX() + b.vx);
        checkWallCollisions(b);
        checkBlockCollisions(b, true);

        b.circle.setCenterY(b.circle.getCenterY() + b.vy);
        checkWallCollisions(b);
        checkBlockCollisions(b, false);

        if (b.circle.getCenterY() + BALL_RADIUS >= PLAYFIELD_MAX_Y) {
            b.active = false;
            b.circle.setCenterY(startY);

            if (!firstBallLanded) {
                firstBallLanded = true;
                nextStartX = b.circle.getCenterX();
                nextStartX = Math.max(PLAYFIELD_MIN_X + BALL_RADIUS, Math.min(PLAYFIELD_MAX_X - BALL_RADIUS, nextStartX));
            }
            b.circle.setCenterX(nextStartX);
        }
    }

    private void checkWallCollisions(Ball b) {
        if (b.circle.getCenterX() - BALL_RADIUS <= PLAYFIELD_MIN_X) {
            b.circle.setCenterX(PLAYFIELD_MIN_X + BALL_RADIUS);
            if (b.vx < 0) b.vx = -b.vx;
        }
        else if (b.circle.getCenterX() + BALL_RADIUS >= PLAYFIELD_MAX_X) {
            b.circle.setCenterX(PLAYFIELD_MAX_X - BALL_RADIUS);
            if (b.vx > 0) b.vx = -b.vx;
        }

        if (b.circle.getCenterY() - BALL_RADIUS <= PLAYFIELD_MIN_Y) {
            b.circle.setCenterY(PLAYFIELD_MIN_Y + BALL_RADIUS);
            if (b.vy < 0) b.vy = -b.vy;
        }
    }

    private void checkBlockCollisions(Ball b, boolean movingX) {
        Iterator<Block> it = blocks.iterator();
        while (it.hasNext()) {
            Block block = it.next();

            if (isIntersecting(b.circle, block.rect)) {
                if (movingX) {
                    b.vx = -b.vx;
                    b.circle.setCenterX(b.circle.getCenterX() + (b.vx > 0 ? 1 : -1));
                } else {
                    b.vy = -b.vy;
                    b.circle.setCenterY(b.circle.getCenterY() + (b.vy > 0 ? 1 : -1));
                }

                if (block.isSpecial) {
                    extraBallsEarned++;
                    block.remove();
                    it.remove();
                } else {
                    block.health -= ballDamage;
                    if (block.health <= 0) {
                        money += 10;
                        updateShopUI();
                        block.remove();
                        it.remove();
                    } else {
                        block.updateVisuals();
                    }
                }
                break;
            }
        }
    }

    private boolean isIntersecting(Circle c, Rectangle r) {
        double circleDistanceX = Math.abs(c.getCenterX() - r.getX() - r.getWidth() / 2);
        double circleDistanceY = Math.abs(c.getCenterY() - r.getY() - r.getHeight() / 2);

        if (circleDistanceX > (r.getWidth() / 2 + c.getRadius())) { return false; }
        if (circleDistanceY > (r.getHeight() / 2 + c.getRadius())) { return false; }

        if (circleDistanceX <= (r.getWidth() / 2)) { return true; }
        if (circleDistanceY <= (r.getHeight() / 2)) { return true; }

        double cornerDistanceSq = Math.pow(circleDistanceX - r.getWidth() / 2, 2) +
                Math.pow(circleDistanceY - r.getHeight() / 2, 2);

        return (cornerDistanceSq <= Math.pow(c.getRadius(), 2));
    }

    private void endWave() {
        startX = nextStartX;
        wave++;

        if (wave > highestWave) {
            highestWave = wave;
            highScoreText.setText("Highest: " + highestWave);
        }
        waveText.setText("Wave: " + wave);

        totalBalls += extraBallsEarned;
        while (balls.size() < totalBalls) {
            balls.add(new Ball(startX, startY));
        }

        boolean isGameOver = false;
        for (Block block : blocks) {
            block.shiftDown();
            // Trigger Game Over if a block crosses the warning line
            if (block.rect.getY() + BLOCK_SIZE >= WARNING_LINE_Y) {
                isGameOver = true;
            }
        }

        if (isGameOver) {
            triggerGameOver();
        } else {
            spawnRow();
            state = GameState.AIMING;
            updateRemainingBallsUI();
            updateShopUI();
        }
    }

    private void triggerGameOver() {
        state = GameState.GAME_OVER;
        gameOverWaveText.setText("You reached Wave: " + wave);

        // This is the magic line that fixes the Z-Index!
        gameOverMenu.toFront();
        gameOverMenu.setVisible(true);

        updateRemainingBallsUI();
    }

    private void resetGame() {
        for (Block b : blocks) b.remove();
        blocks.clear();

        for (Ball b : balls) root.getChildren().remove(b.circle);
        balls.clear();

        wave = 1;
        totalBalls = 1;
        ballsFired = 0;
        extraBallsEarned = 0;
        money = 0;
        ballDamage = 1;
        damageUpgradeCost = 50;
        firstBallLanded = false;

        startX = PLAYFIELD_MIN_X + (PLAYFIELD_WIDTH / 2.0);
        nextStartX = startX;

        balls.add(new Ball(startX, startY));
        spawnRow();

        waveText.setText("Wave: " + wave);
        updateShopUI();
        updateRemainingBallsUI();

        gameOverMenu.setVisible(false);
        state = GameState.AIMING;
    }

    private void spawnRow() {
        int columns = 8;
        boolean specialSpawned = false;

        // Health Scaling Logic
        int blockHealth;
        if (wave <= 10) {
            blockHealth = wave;
        } else {
            // Scales multiplicatively by 30% per wave after 10
            blockHealth = (int) Math.round(10 * Math.pow(1.3, wave - 10));
        }

        for (int i = 0; i < columns; i++) {
            if (random.nextDouble() > 0.4) {
                boolean isSpecial = false;
                if (!specialSpawned && random.nextDouble() > 0.8) {
                    isSpecial = true;
                    specialSpawned = true;
                }

                // Pass blockHealth instead of wave
                Block block = new Block(PLAYFIELD_MIN_X + (i * BLOCK_SIZE), PLAYFIELD_MIN_Y + BLOCK_SIZE, isSpecial ? 0 : blockHealth, isSpecial);
                blocks.add(block);
            }
        }
    }

    // --- Inner Classes ---

    class Ball {
        Circle circle;
        double vx = 0, vy = 0;
        boolean active = false;

        Ball(double x, double y) {
            circle = new Circle(x, y, BALL_RADIUS, Color.WHITE);
            root.getChildren().add(circle);
        }
    }

    class Block {
        Rectangle rect;
        Text text;
        int health;
        boolean isSpecial;

        Block(double x, double y, int health, boolean isSpecial) {
            this.health = health;
            this.isSpecial = isSpecial;

            rect = new Rectangle(x + 2, y + 2, BLOCK_SIZE - 4, BLOCK_SIZE - 4);

            text = new Text();
            text.setFont(Font.font("Arial", FontWeight.BOLD, 22));

            if (isSpecial) {
                rect.setFill(Color.GOLD);
                text.setText("+1");
                text.setFill(Color.BLACK);
                text.setX(x + (BLOCK_SIZE - text.getLayoutBounds().getWidth()) / 2);
            } else {
                text.setFill(Color.WHITE);
                updateVisuals();
            }

            text.setY(y + (BLOCK_SIZE + text.getLayoutBounds().getHeight() / 2) / 2 - 2);
            root.getChildren().addAll(rect, text);
        }

        void updateVisuals() {
            text.setText(String.valueOf(health));
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);

            if (!isSpecial) {
                double hue = (health * 12) % 360;
                rect.setFill(Color.hsb(hue, 0.75, 0.9));
            }
        }

        void shiftDown() {
            rect.setY(rect.getY() + BLOCK_SIZE);
            text.setY(text.getY() + BLOCK_SIZE);
        }

        void remove() {
            root.getChildren().removeAll(rect, text);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}