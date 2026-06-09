package final_project;

import javafx.animation.AnimationTimer;
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

public class GameController {

    private Pane root;
    private Polyline aimPath;
    private Text waveText;
    private Text highScoreText;
    private Text moneyText;
    private Text remainingBallsText;
    private Button upgradeDamageBtn;

    private VBox gameOverMenu;
    private Text gameOverWaveText;

    private Text editModeText;
    private EditModeManager editModeManager;

    private boolean forceFireRound = false;
    private boolean forceIceRound = false;
    private boolean iceRoundActive = false;
    private int iceRoundsAvailable = 0;
    private boolean freezeAppliedThisRound = false;

    private List<Ball> balls = new ArrayList<>();
    private List<Block> blocks = new ArrayList<>();

    private int wave = 1;
    private int highestWave = 1;
    private int totalBalls = 1;
    private int money = 0;
    private int ballDamage = 1;
    private int damageUpgradeCost = 50;

    private int fireRoundsAvailable = 0;
    private boolean fireRoundActive = false;

    private double startX = GameConfig.PLAYFIELD_MIN_X + (GameConfig.PLAYFIELD_WIDTH / 2.0);
    private final double startY = GameConfig.PLAYFIELD_MAX_Y - GameConfig.BALL_RADIUS;
    private double nextStartX = startX;
    private boolean firstBallLanded = false;

    private GameState state = GameState.AIMING;

    private double aimVx;
    private double aimVy;
    private int ballsFired = 0;
    private int fireDelayCounter = 0;
    private int extraBallsEarned = 0;

    private Random random = new Random();

    public void start(Stage primaryStage) {
        root = new Pane();
        root.setStyle("-fx-background-color: #0f0f1a;");

        Rectangle playfield = new Rectangle(
                GameConfig.PLAYFIELD_MIN_X,
                GameConfig.PLAYFIELD_MIN_Y,
                GameConfig.PLAYFIELD_WIDTH,
                GameConfig.PLAYFIELD_HEIGHT
        );
        playfield.setFill(Color.web("#1a1a2e"));
        playfield.setStroke(Color.WHITE);
        playfield.setStrokeWidth(2);
        root.getChildren().add(playfield);

        Line warningLine = new Line(
                GameConfig.PLAYFIELD_MIN_X,
                GameConfig.WARNING_LINE_Y,
                GameConfig.PLAYFIELD_MAX_X,
                GameConfig.WARNING_LINE_Y
        );
        warningLine.setStroke(Color.web("#e74c3c"));
        warningLine.setStrokeWidth(2);
        warningLine.getStrokeDashArray().addAll(8d, 8d);
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

        Scene scene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        setupEditMode(scene);

        primaryStage.setTitle("Block Breaker Final Project");
        primaryStage.setScene(scene);
        primaryStage.show();
        root.requestFocus();

        balls.add(new Ball(startX, startY, root));
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

    private void setupEditMode(Scene scene) {
        editModeText = new Text();
        editModeText.setX(20);
        editModeText.setY(180);
        editModeText.setFill(Color.LIGHTGREEN);
        editModeText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        editModeText.setVisible(false);

        root.getChildren().add(editModeText);

        root.setFocusTraversable(true);
        root.setOnMouseClicked(e -> root.requestFocus());

        editModeManager = new EditModeManager(this, editModeText);
        editModeManager.attach(scene);
    }
    private void setupUI() {
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

        moneyText = new Text("Money: $0");
        moneyText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        moneyText.setFill(Color.GOLD);
        moneyText.setX(GameConfig.PLAYFIELD_MAX_X + 20);
        moneyText.setY(80);

        Text shopTitle = new Text("SHOP");
        shopTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        shopTitle.setFill(Color.WHITE);
        shopTitle.setX(GameConfig.PLAYFIELD_MAX_X + 20);
        shopTitle.setY(130);

        upgradeDamageBtn = new Button();
        updateShopUI();
        upgradeDamageBtn.setLayoutX(GameConfig.PLAYFIELD_MAX_X + 20);
        upgradeDamageBtn.setLayoutY(150);
        upgradeDamageBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        upgradeDamageBtn.setOnAction(e -> handleUpgradePurchase());

        remainingBallsText = new Text("x" + totalBalls);
        remainingBallsText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        remainingBallsText.setFill(Color.WHITE);

        root.getChildren().addAll(waveText, highScoreText, moneyText, shopTitle, upgradeDamageBtn, remainingBallsText);
    }

    private void setupGameOverMenu() {
        gameOverMenu = new VBox(20);
        gameOverMenu.setAlignment(Pos.CENTER);
        gameOverMenu.setLayoutX(GameConfig.PLAYFIELD_MIN_X);
        gameOverMenu.setLayoutY(GameConfig.PLAYFIELD_MIN_Y);
        gameOverMenu.setPrefSize(GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);
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
                freezeAppliedThisRound = false;

                if (fireRoundsAvailable > 0) {
                    fireRoundActive = true;
                    fireRoundsAvailable--;
                } else {
                    fireRoundActive = false;
                }

                if (forceIceRound) {
                    iceRoundActive = true;
                } else if (iceRoundsAvailable > 0) {
                    iceRoundActive = true;
                    iceRoundsAvailable--;
                } else {
                    iceRoundActive = false;
                }
                updateRemainingBallsUI();
            }
        }
    }

    private void updateAim(double mx, double my) {
        double dx = mx - startX;
        double dy = my - startY;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length > 0) {
            aimVx = -(dx / length) * GameConfig.BALL_SPEED;
            aimVy = -(dy / length) * GameConfig.BALL_SPEED;

            aimPath.getPoints().clear();
            aimPath.getPoints().addAll(startX, startY);

            double simX = startX;
            double simY = startY;
            double dirX = -(dx / length);
            double dirY = -(dy / length);

            Circle dummy = new Circle(0, 0, GameConfig.BALL_RADIUS);
            double stepLength = 2.0;
            int maxSteps = 400;

            for (int i = 0; i < maxSteps; i++) {
                simX += dirX * stepLength;
                simY += dirY * stepLength;

                dummy.setCenterX(simX);
                dummy.setCenterY(simY);

                boolean bounced = false;

                if (simX - GameConfig.BALL_RADIUS <= GameConfig.PLAYFIELD_MIN_X) {
                    simX = GameConfig.PLAYFIELD_MIN_X + GameConfig.BALL_RADIUS;
                    dirX = -dirX;
                    bounced = true;
                } else if (simX + GameConfig.BALL_RADIUS >= GameConfig.PLAYFIELD_MAX_X) {
                    simX = GameConfig.PLAYFIELD_MAX_X - GameConfig.BALL_RADIUS;
                    dirX = -dirX;
                    bounced = true;
                }

                if (simY - GameConfig.BALL_RADIUS <= GameConfig.PLAYFIELD_MIN_Y) {
                    simY = GameConfig.PLAYFIELD_MIN_Y + GameConfig.BALL_RADIUS;
                    dirY = -dirY;
                    bounced = true;
                }

                if (bounced) {
                    aimPath.getPoints().addAll(simX, simY);
                }

                if (simY + GameConfig.BALL_RADIUS >= GameConfig.PLAYFIELD_MAX_Y) {
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

                b.setFireBall(fireRoundActive || forceFireRound);
                b.setIceBall(iceRoundActive);

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
                if (b.active || b.returning) {
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
        if (b.returning) {
            b.circle.setCenterX(b.circle.getCenterX() + b.vx);

            if (Math.abs(nextStartX - b.circle.getCenterX()) <= Math.abs(b.vx)) {
                b.circle.setCenterX(nextStartX);
                b.vx = 0;
                b.active = false;
                b.returning = false;
            }
            return;
        }

        b.circle.setCenterX(b.circle.getCenterX() + b.vx);
        b.circle.setCenterY(b.circle.getCenterY() + b.vy);

        checkWallCollisions(b);
        checkBlockCollisions(b);

        if (b.circle.getCenterY() + GameConfig.BALL_RADIUS >= GameConfig.PLAYFIELD_MAX_Y) {
            b.circle.setCenterY(startY);
            b.vy = 0;

            if (!firstBallLanded) {
                firstBallLanded = true;
                nextStartX = b.circle.getCenterX();
                nextStartX = Math.max(
                        GameConfig.PLAYFIELD_MIN_X + GameConfig.BALL_RADIUS,
                        Math.min(GameConfig.PLAYFIELD_MAX_X - GameConfig.BALL_RADIUS, nextStartX)
                );

                b.circle.setCenterX(nextStartX);
                b.vx = 0;
                b.active = false;
            } else {
                b.returning = true;
                double distance = nextStartX - b.circle.getCenterX();

                if (Math.abs(distance) < 1.0) {
                    b.circle.setCenterX(nextStartX);
                    b.active = false;
                    b.returning = false;
                } else {
                    b.vx = Math.signum(distance) * (GameConfig.BALL_SPEED * 2.0);
                }
            }
        }
    }

    private void checkWallCollisions(Ball b) {
        if (b.circle.getCenterX() - GameConfig.BALL_RADIUS <= GameConfig.PLAYFIELD_MIN_X) {
            b.circle.setCenterX(GameConfig.PLAYFIELD_MIN_X + GameConfig.BALL_RADIUS);
            if (b.vx < 0) b.vx = -b.vx;
        } else if (b.circle.getCenterX() + GameConfig.BALL_RADIUS >= GameConfig.PLAYFIELD_MAX_X) {
            b.circle.setCenterX(GameConfig.PLAYFIELD_MAX_X - GameConfig.BALL_RADIUS);
            if (b.vx > 0) b.vx = -b.vx;
        }

        if (b.circle.getCenterY() - GameConfig.BALL_RADIUS <= GameConfig.PLAYFIELD_MIN_Y) {
            b.circle.setCenterY(GameConfig.PLAYFIELD_MIN_Y + GameConfig.BALL_RADIUS);
            if (b.vy < 0) b.vy = -b.vy;
        }
    }

    private void checkBlockCollisions(Ball b) {
        Iterator<Block> it = blocks.iterator();

        while (it.hasNext()) {
            Block block = it.next();

            if (isIntersecting(b.circle, block.rect)) {
                bounceFromBlock(b, block);

                block.health -= ballDamage;
                boolean destroyed = block.health <= 0;

                if (destroyed) {
                    handleBlockDestroyed(block);
                    block.remove();
                    it.remove();
                } else {
                    block.updateVisuals();
                }

                if (b.fireBall) {
                    fireExplosion(block);
                }

                if (b.iceBall) {
                    iceExplosion(block);
                }

                break;
            }
        }
    }

    private void bounceFromBlock(Ball b, Block block) {
        double ballX = b.circle.getCenterX();
        double ballY = b.circle.getCenterY();

        double blockCenterX = block.rect.getX() + block.rect.getWidth() / 2.0;
        double blockCenterY = block.rect.getY() + block.rect.getHeight() / 2.0;

        double dx = ballX - blockCenterX;
        double dy = ballY - blockCenterY;

        double overlapX = block.rect.getWidth() / 2.0 + b.circle.getRadius() - Math.abs(dx);
        double overlapY = block.rect.getHeight() / 2.0 + b.circle.getRadius() - Math.abs(dy);

        if (overlapX < overlapY) {
            b.vx = -b.vx;
        } else {
            b.vy = -b.vy;
        }
    }

    private void fireExplosion(Block centerBlock) {
        double centerX = centerBlock.rect.getX() + centerBlock.rect.getWidth() / 2.0;
        double centerY = centerBlock.rect.getY() + centerBlock.rect.getHeight() / 2.0;

        for (Block block : blocks) {
            double blockX = block.rect.getX() + block.rect.getWidth() / 2.0;
            double blockY = block.rect.getY() + block.rect.getHeight() / 2.0;

            double dx = blockX - centerX;
            double dy = blockY - centerY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance <= GameConfig.FIRE_EXPLOSION_RADIUS) {
                block.setBurning(true);
            }
        }
    }

    private void iceExplosion(Block centerBlock) {
        double centerX = centerBlock.rect.getX() + centerBlock.rect.getWidth() / 2.0;
        double centerY = centerBlock.rect.getY() + centerBlock.rect.getHeight() / 2.0;

        for (Block block : blocks) {
            double blockX = block.rect.getX() + block.rect.getWidth() / 2.0;
            double blockY = block.rect.getY() + block.rect.getHeight() / 2.0;

            double dx = blockX - centerX;
            double dy = blockY - centerY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance <= GameConfig.FIRE_EXPLOSION_RADIUS) {
                block.setFrozen(true);
            }
        }
    }
    private void applyBurnDamage() {
        Iterator<Block> it = blocks.iterator();

        while (it.hasNext()) {
            Block block = it.next();

            if (block.burning) {
                int burnDamage = Math.max(1, (int) Math.ceil(block.health * GameConfig.BURN_PERCENT));
                block.health -= burnDamage;

                if (block.health <= 0) {
                    handleBlockDestroyed(block);
                    block.remove();
                    it.remove();
                } else {
                    block.updateVisuals();
                }
            }
        }
    }

    private void handleBlockDestroyed(Block block) {
        money += 10;

        if (block.type == BlockType.EXTRA_BALL) {
            extraBallsEarned++;
        } else if (block.type == BlockType.FIRE_POWER) {
            fireRoundsAvailable++;
        } else if (block.type == BlockType.ICE_POWER) {
            iceRoundsAvailable++;
        }
        updateShopUI();
    }

    private boolean isIntersecting(Circle c, Rectangle r) {
        double circleDistanceX = Math.abs(c.getCenterX() - r.getX() - r.getWidth() / 2);
        double circleDistanceY = Math.abs(c.getCenterY() - r.getY() - r.getHeight() / 2);

        if (circleDistanceX > (r.getWidth() / 2 + c.getRadius())) return false;
        if (circleDistanceY > (r.getHeight() / 2 + c.getRadius())) return false;

        if (circleDistanceX <= (r.getWidth() / 2)) return true;
        if (circleDistanceY <= (r.getHeight() / 2)) return true;

        double cornerDistanceSq =
                Math.pow(circleDistanceX - r.getWidth() / 2, 2)
                        + Math.pow(circleDistanceY - r.getHeight() / 2, 2);

        return cornerDistanceSq <= Math.pow(c.getRadius(), 2);
    }


    private void moveBlocksDownWithFreeze() {
        List<Block> sortedBlocks = new ArrayList<>(blocks);

        sortedBlocks.sort((a, b) -> Double.compare(b.rect.getY(), a.rect.getY()));

        for (Block block : sortedBlocks) {
            if (block.frozen) {
                continue;
            }

            double nextY = block.rect.getY() + GameConfig.BLOCK_SIZE;

            boolean blocked = false;

            for (Block other : blocks) {
                if (other == block) continue;

                boolean sameColumn =
                        Math.abs(other.rect.getX() - block.rect.getX()) < 1;

                boolean targetOccupied =
                        Math.abs(other.rect.getY() - nextY) < 1;

                if (sameColumn && targetOccupied) {
                    blocked = true;
                    break;
                }
            }

            if (!blocked) {
                block.shiftDown();
            }
        }

        for (Block block : blocks) {
            if (block.frozen) {
                block.setFrozen(false);
            }
        }
    }

    private void endWave() {
        applyBurnDamage();

        startX = nextStartX;
        wave++;
        fireRoundActive = false;
        iceRoundActive = false;



        if (wave > highestWave) {
            highestWave = wave;
            highScoreText.setText("Highest: " + highestWave);
        }

        waveText.setText("Wave: " + wave);

        totalBalls += extraBallsEarned;

        while (balls.size() < totalBalls) {
            balls.add(new Ball(startX, startY, root));
        }

        boolean isGameOver = false;

        moveBlocksDownWithFreeze();

        for (Block block : blocks) {
            if (block.rect.getY() + GameConfig.BLOCK_SIZE >= GameConfig.WARNING_LINE_Y) {
                isGameOver = true;
            }
        }

        if (isGameOver) {
            triggerGameOver();
        } else {
            if (canSpawnNewRow()) {
                spawnRow();
            }

            state = GameState.AIMING;
            updateRemainingBallsUI();
            updateShopUI();
        }
    }

    private void triggerGameOver() {
        state = GameState.GAME_OVER;
        gameOverWaveText.setText("You reached Wave: " + wave);
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

        fireRoundsAvailable = 0;
        fireRoundActive = false;
        iceRoundsAvailable = 0;

        forceFireRound = false;
        forceIceRound = false;
        iceRoundActive = false;
        freezeAppliedThisRound = false;

        startX = GameConfig.PLAYFIELD_MIN_X + (GameConfig.PLAYFIELD_WIDTH / 2.0);
        nextStartX = startX;

        balls.add(new Ball(startX, startY, root));
        spawnRow();

        waveText.setText("Wave: " + wave);
        updateShopUI();
        updateRemainingBallsUI();

        gameOverMenu.setVisible(false);
        state = GameState.AIMING;
    }

    private boolean canSpawnNewRow() {
        for (Block block : blocks) {
            if (block.rect.getY() <= GameConfig.PLAYFIELD_MIN_Y + GameConfig.BLOCK_SIZE + 1) {
                return false;
            }
        }

        return true;
    }

    private void spawnRow() {
        int columns = 8;
        boolean specialSpawned = false;

        int blockHealth;

        if (wave <= 10) {
            blockHealth = wave;
        } else {
            blockHealth = (int) Math.round(10 * Math.pow(1.3, wave - 10));
        }

        for (int i = 0; i < columns; i++) {
            if (random.nextDouble() > 0.4) {
                BlockType type = BlockType.NORMAL;

                if (!specialSpawned && random.nextDouble() > 0.85) {
                    double powerRoll = random.nextDouble();

                    if (powerRoll < 0.65) {
                        type = BlockType.EXTRA_BALL;
                    } else if (powerRoll < 0.85) {
                        type = BlockType.FIRE_POWER;
                    } else {
                        type = BlockType.ICE_POWER;
                    }

                    specialSpawned = true;
                }

                Block block = new Block(
                        GameConfig.PLAYFIELD_MIN_X + (i * GameConfig.BLOCK_SIZE),
                        GameConfig.PLAYFIELD_MIN_Y + GameConfig.BLOCK_SIZE,
                        blockHealth,
                        type,
                        root
                );

                blocks.add(block);
            }
        }
    }

    public void setForceFireRound(boolean forceFireRound) {
        this.forceFireRound = forceFireRound;
    }

    public void setForceIceRound(boolean forceIceRound) {
        this.forceIceRound = forceIceRound;
    }

    public void moveBlocksUpOneRow() {
        if (state == GameState.SHOOTING || state == GameState.WAITING) return;

        Iterator<Block> it = blocks.iterator();

        while (it.hasNext()) {
            Block block = it.next();

            block.rect.setY(block.rect.getY() - GameConfig.BLOCK_SIZE);
            block.text.setY(block.text.getY() - GameConfig.BLOCK_SIZE);

            if (block.ring != null) {
                block.ring.setCenterY(block.ring.getCenterY() - GameConfig.BLOCK_SIZE);
            }

            if (block.rect.getY() <= GameConfig.PLAYFIELD_MIN_Y + GameConfig.BLOCK_SIZE + 1) {
                block.remove();
                it.remove();
            }
        }

        if (wave > 1) {
            wave--;
            waveText.setText("Wave: " + wave);
        }

        int targetBallCount = Math.max(1, totalBalls - 1);
        totalBalls = targetBallCount;

        while (balls.size() > totalBalls) {
            Ball removed = balls.remove(balls.size() - 1);
            root.getChildren().remove(removed.circle);
        }

        updateRemainingBallsUI();
        updateShopUI();
    }

    public void forceNextWave() {
        if (state != GameState.AIMING) return;

        for (Ball b : balls) {
            b.active = false;
            b.returning = false;
            b.vx = 0;
            b.vy = 0;
            b.circle.setCenterX(startX);
            b.circle.setCenterY(startY);
            b.resetType();
        }

        ballsFired = 0;
        fireDelayCounter = 0;
        firstBallLanded = false;
        extraBallsEarned = 0;
        freezeAppliedThisRound = false;

        endWave();
    }}