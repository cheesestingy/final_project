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
import javafx.scene.control.Slider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class GameController {

    private Pane root;
    private Polyline aimPath;
    private Text waveText;
    private Text highScoreText;
    private Text moneyText;
    private Text remainingBallsText;
    private Button upgradeDamageBtn;
    private SoundManager soundManager;

    private VBox gameOverMenu;
    private Text gameOverWaveText;

    private Text editModeText;
    private EditModeManager editModeManager;

    private VBox mainMenu;
    private VBox optionsMenu;

    private int fireRoundsAvailable = 0;
    private boolean fireRoundActive = false;
    private boolean forceFireRound = false;
    private boolean forceIceRound = false;
    private boolean iceRoundActive = false;
    private int iceRoundsAvailable = 0;
    private int pierceRoundsAvailable = 0;
    private boolean pierceRoundActive = false;
    private boolean forcePierceRound = false;
    private int shrinkRoundsAvailable = 0;
    private boolean shrinkRoundActive = false;
    private boolean forceShrinkRound = false;


    private List<Ball> balls = new ArrayList<>();
    private List<Block> blocks = new ArrayList<>();
    private List<Block> pendingBlocksToAdd = new ArrayList<>();

    private int wave = 1;
    private int highestWave = 1;
    private int totalBalls = 1;
    private int money = 0;
    private int ballDamage = GameConfig.BALL_DAMAGE_START;
    private int damageUpgradeCost = GameConfig.BALL_DAMAGE_UPGRADE_COST_START;
    private double powerUpSpawnChance = GameConfig.POWER_UP_SPAWN_CHANCE_START;
    private int powerUpUpgradeCost = GameConfig.POWER_UP_UPGRADE_COST_START;
    private Button upgradePowerUpBtn;
    private double coinRewardMultiplier = GameConfig.COIN_REWARD_MULTIPLIER_START;
    private int coinRewardUpgradeCost = GameConfig.COIN_REWARD_UPGRADE_COST_START;
    private Button upgradeCoinRewardBtn;





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
        soundManager = new SoundManager();
        soundManager.playBackgroundMusic();
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
        setupMainMenu();
        setupOptionsMenu();

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
        scene.getStylesheets().add(
                getClass().getResource("style.css").toExternalForm()
        );
        setupEditMode(scene);

        primaryStage.setTitle("Block Breaker Final Project");
        primaryStage.setScene(scene);
        primaryStage.show();
        root.requestFocus();

        state = GameState.MAIN_MENU;
        updateShopUI();

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
        editModeText.getStyleClass().add("edit-text");
        editModeText.setX(20);
        editModeText.setY(180);
        editModeText.setFill(Color.LIGHTGREEN);
        editModeText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        editModeText.setVisible(false);

        root.getChildren().add(editModeText);

        root.setFocusTraversable(true);
        root.setOnMouseClicked(e -> root.requestFocus());

        editModeManager = new EditModeManager(this, editModeText, root);
        editModeManager.attach(scene);
    }

    private void enableHoldUpgrade(Button button, Runnable action) {
        Timeline holdTimer = new Timeline(
                new KeyFrame(Duration.millis(120), e -> action.run())
        );

        holdTimer.setCycleCount(Timeline.INDEFINITE);

        button.setOnMousePressed(e -> {
            action.run();
            holdTimer.playFromStart();
        });

        button.setOnMouseReleased(e -> holdTimer.stop());
        button.setOnMouseExited(e -> holdTimer.stop());
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

        waveText.getStyleClass().add("game-text");
        highScoreText.getStyleClass().add("game-text");
        moneyText.getStyleClass().add("money-text");

        Text shopTitle = new Text("SHOP");
        shopTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        shopTitle.setFill(Color.WHITE);
        shopTitle.setX(GameConfig.PLAYFIELD_MAX_X + 20);
        shopTitle.setY(130);

        upgradeDamageBtn = new Button();
        upgradeDamageBtn.setLayoutX(GameConfig.PLAYFIELD_MAX_X + 20);
        upgradeDamageBtn.setLayoutY(150);
        upgradeDamageBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: black; -fx-font-weight: bold;");
        enableHoldUpgrade(upgradeDamageBtn, this::handleUpgradePurchase);
        upgradePowerUpBtn = new Button();
        upgradePowerUpBtn.setLayoutX(GameConfig.PLAYFIELD_MAX_X + 20);
        upgradePowerUpBtn.setLayoutY(250);
        upgradePowerUpBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold;");
        enableHoldUpgrade(upgradePowerUpBtn, this::handlePowerUpUpgradePurchase);
        upgradeCoinRewardBtn = new Button();
        upgradeCoinRewardBtn.setLayoutX(GameConfig.PLAYFIELD_MAX_X + 20);
        upgradeCoinRewardBtn.setLayoutY(350);
        upgradeCoinRewardBtn.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: black; -fx-font-weight: bold;");
        enableHoldUpgrade(upgradeCoinRewardBtn, this::handleCoinRewardUpgradePurchase);

        remainingBallsText = new Text("x" + totalBalls);
        remainingBallsText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        remainingBallsText.setFill(Color.WHITE);
        updateShopUI();

        Button openOptionsBtn = new Button("⚙ Options");
        openOptionsBtn.setLayoutX(20);
        openOptionsBtn.setLayoutY(GameConfig.WINDOW_HEIGHT - 60);
        openOptionsBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold;");
        openOptionsBtn.setOnAction(e -> {
            if (state == GameState.AIMING || state == GameState.WAITING) {
                state = GameState.PAUSED;
                optionsMenu.toFront();
                optionsMenu.setVisible(true);
            }
        });

        root.getChildren().addAll(
                waveText,
                highScoreText,
                moneyText,
                shopTitle,
                upgradeDamageBtn,
                upgradePowerUpBtn,
                upgradeCoinRewardBtn,
                remainingBallsText,
                openOptionsBtn
        );
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

        if (money >= damageUpgradeCost &&
                ballDamage < GameConfig.BALL_DAMAGE_MAX) {
            soundManager.playBuy();
            money -= damageUpgradeCost;
            ballDamage = (int) Math.ceil(
                    ballDamage * GameConfig.BALL_DAMAGE_UPGRADE_MULTIPLIER
            );

            if (ballDamage > GameConfig.BALL_DAMAGE_MAX) {
                ballDamage = GameConfig.BALL_DAMAGE_MAX;
            }

            damageUpgradeCost = (int) Math.ceil(
                    damageUpgradeCost * GameConfig.BALL_DAMAGE_COST_MULTIPLIER
            );
            updateShopUI();
        }
    }

    private void handlePowerUpUpgradePurchase() {
        if (state == GameState.GAME_OVER) return;

        if (money >= powerUpUpgradeCost &&
                powerUpSpawnChance < GameConfig.POWER_UP_SPAWN_CHANCE_MAX) {
            soundManager.playBuy();
            money -= powerUpUpgradeCost;
            powerUpSpawnChance += GameConfig.POWER_UP_SPAWN_CHANCE_UPGRADE_AMOUNT;

            if (powerUpSpawnChance > GameConfig.POWER_UP_SPAWN_CHANCE_MAX) {
                powerUpSpawnChance = GameConfig.POWER_UP_SPAWN_CHANCE_MAX;
            }

            powerUpUpgradeCost = (int) Math.ceil(
                    powerUpUpgradeCost * GameConfig.POWER_UP_COST_MULTIPLIER
            );
            updateShopUI();
        }
    }
    private void handleCoinRewardUpgradePurchase() {
        if (state == GameState.GAME_OVER) return;

        if (money >= coinRewardUpgradeCost &&
                coinRewardMultiplier < GameConfig.COIN_REWARD_MULTIPLIER_MAX) {
            soundManager.playBuy();
            money -= coinRewardUpgradeCost;
            coinRewardMultiplier += GameConfig.COIN_REWARD_MULTIPLIER_UPGRADE_AMOUNT;

            if (coinRewardMultiplier > GameConfig.COIN_REWARD_MULTIPLIER_MAX) {
                coinRewardMultiplier = GameConfig.COIN_REWARD_MULTIPLIER_MAX;
            }

            coinRewardUpgradeCost = (int) Math.ceil(
                    coinRewardUpgradeCost * GameConfig.COIN_REWARD_COST_MULTIPLIER
            );
            updateShopUI();
        }
    }

    private void updateShopUI() {
        moneyText.setText("Money: $" + formatBigNumber(money));

        // 攻擊升級
        upgradeDamageBtn.setText(
                "Ball Damage +\n" +
                        "Cost: $" + formatBigNumber(damageUpgradeCost) + "\n" +
                        "Current: " + formatBigNumber(ballDamage)
        );

        boolean damageMaxed = ballDamage >= GameConfig.BALL_DAMAGE_MAX;

        upgradeDamageBtn.setDisable(money < damageUpgradeCost || damageMaxed);

        if (damageMaxed) {
            upgradeDamageBtn.setText(
                    "Ball Damage\n" +
                            "MAX\n" +
                            "Current: " + formatBigNumber(ballDamage)
            );
        }

        // Power up 出現率升級
        int percent = (int) Math.round(powerUpSpawnChance * 100);

        upgradePowerUpBtn.setText(
                "Power Up Rate +\n" +
                        "Cost: $" + formatBigNumber(powerUpUpgradeCost) + "\n" +
                        "Current: " + percent + "%"
        );

        boolean powerUpMaxed = powerUpSpawnChance >= GameConfig.POWER_UP_SPAWN_CHANCE_MAX;

        upgradePowerUpBtn.setDisable(money < powerUpUpgradeCost || powerUpMaxed);

        if (powerUpMaxed) {
            upgradePowerUpBtn.setText(
                    "Power Up Rate\n" +
                            "MAX\n" +
                            "Current: " + percent + "%"
            );
        }

        // 金幣倍率升級
        upgradeCoinRewardBtn.setText(
                "Coin Bonus +\n" +
                        "Cost: $" + formatBigNumber(coinRewardUpgradeCost) + "\n" +
                        "Current: x" + String.format("%.1f", coinRewardMultiplier)
        );

        boolean coinMaxed = coinRewardMultiplier >= GameConfig.COIN_REWARD_MULTIPLIER_MAX;

        upgradeCoinRewardBtn.setDisable(money < coinRewardUpgradeCost || coinMaxed);

        if (coinMaxed) {
            upgradeCoinRewardBtn.setText(
                    "Coin Bonus\n" +
                            "MAX\n" +
                            "Current: x" + String.format("%.1f", coinRewardMultiplier)
            );
        }
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

    private void updateWaitingBallColor() {
        boolean willBeFire = forceFireRound || fireRoundsAvailable > 0;
        boolean willBeIce = forceIceRound || iceRoundsAvailable > 0;
        boolean willBePierce = forcePierceRound || pierceRoundsAvailable > 0;
        boolean willBeShrink = forceShrinkRound || shrinkRoundsAvailable > 0;

        for (Ball b : balls) {
            b.resetType();

            if (willBeFire) {
                b.setFireBall(true);
            } else if (willBeIce) {
                b.setIceBall(true);
            } else if (willBePierce) {
                b.setPierceBall(true);
            } else if (willBeShrink) {
                b.setShrinkBall(true);
            }
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

            if (aimVy < 0) {
                soundManager.playShoot();
                state = GameState.SHOOTING;
                ballsFired = 0;
                fireDelayCounter = 0;
                firstBallLanded = false;
                extraBallsEarned = 0;


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

                if (pierceRoundsAvailable > 0) {
                    pierceRoundActive = true;
                    pierceRoundsAvailable--;
                } else {
                    pierceRoundActive = forcePierceRound;
                }

                if (shrinkRoundsAvailable > 0) {
                    shrinkRoundActive = true;
                    shrinkRoundsAvailable--;
                } else {
                    shrinkRoundActive = forceShrinkRound;
                }
                updateRemainingBallsUI();
            }
        }
    }

    private void updateAim(double mx, double my) {
        double dx = mx - startX;
        double dy = my - startY;

        if (dy >= 0) {
            aimPath.setVisible(false);
            return;
        }

        double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0) return;

        double dirX = dx / length;
        double dirY = dy / length;

        if (dirY > -0.15) {
            dirY = -0.15;
            double newLen = Math.sqrt(dirX * dirX + dirY * dirY);
            dirX /= newLen;
            dirY /= newLen;
        }

        aimVx = dirX * GameConfig.BALL_SPEED;
        aimVy = dirY * GameConfig.BALL_SPEED;

        aimPath.setVisible(true);
        aimPath.getPoints().clear();
        aimPath.getPoints().addAll(startX, startY);

        double simX = startX;
        double simY = startY;

        double stepLength = 5.0;
        int maxSteps = 400;

        Circle dummy = new Circle(0, 0, GameConfig.BALL_RADIUS);
        boolean isPiercing = pierceRoundActive || forcePierceRound;

        for (int i = 0; i < maxSteps; i++) {
            simX += dirX * stepLength;
            simY += dirY * stepLength;
            dummy.setCenterX(simX);
            dummy.setCenterY(simY);

            boolean bounced = false;

            if (simX - GameConfig.BALL_RADIUS <= GameConfig.PLAYFIELD_MIN_X) { // walls
                simX = GameConfig.PLAYFIELD_MIN_X + GameConfig.BALL_RADIUS;
                dirX = -dirX;
                bounced = true;
            } else if (simX + GameConfig.BALL_RADIUS >= GameConfig.PLAYFIELD_MAX_X) {
                simX = GameConfig.PLAYFIELD_MAX_X - GameConfig.BALL_RADIUS;
                dirX = -dirX;
                bounced = true;
            }

            if (simY - GameConfig.BALL_RADIUS <= GameConfig.PLAYFIELD_MIN_Y) { // hit ceiling
                simY = GameConfig.PLAYFIELD_MIN_Y + GameConfig.BALL_RADIUS;
                dirY = -dirY;
                bounced = true;
            }

            if (bounced) {
                aimPath.getPoints().addAll(simX, simY);
            }

            if (simY + GameConfig.BALL_RADIUS >= GameConfig.PLAYFIELD_MAX_Y) { // hit ground
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
                if (!isPiercing) { // if piercing, aim line goes thru block
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
                b.setPierceBall(pierceRoundActive || forcePierceRound);
                b.setShrinkBall(shrinkRoundActive || forceShrinkRound);

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

        double oldX = b.circle.getCenterX();
        double oldY = b.circle.getCenterY();

        b.circle.setCenterX(b.circle.getCenterX() + b.vx);
        b.circle.setCenterY(b.circle.getCenterY() + b.vy);

        b.addTrail(oldX, oldY);

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

                if (!b.pierceBall) {
                    soundManager.playHit();
                }


                if (!b.pierceBall) {
                    bounceFromBlock(b, block);
                }

                if (!block.invincible) {
                    if (!b.pierceBall || b.canPierceDamage(block)) {
                        block.health -= ballDamage;
                    }
                }

                boolean destroyed = block.health <= 0;

                if (destroyed) {
                    if (block.type == BlockType.BOSS && !block.alreadySplit) {
                        block.alreadySplit = true;
                        splitBoss(block);
                    }

                    soundManager.playDestroy();
                    handleBlockDestroyed(block);
                    block.remove();
                    it.remove();
                } else {
                    block.updateVisuals();
                }

                if (!block.invincible) {
                    if (b.fireBall) {
                        fireExplosion(block);
                    }

                    if (b.iceBall) {
                        iceExplosion(block);
                    }
                }

                if (!b.pierceBall) {
                    break;
                }
            }
        }

        if (!pendingBlocksToAdd.isEmpty()) {
            blocks.addAll(pendingBlocksToAdd);
            pendingBlocksToAdd.clear();
        }
    }

    private void bounceFromBlock(Ball b, Block block) {
        double ballX = b.circle.getCenterX();
        double ballY = b.circle.getCenterY();

        double blockCenterX = block.rect.getX() + block.rect.getWidth() / 2.0;
        double blockCenterY = block.rect.getY() + block.rect.getHeight() / 2.0;

        double dx = ballX - blockCenterX;
        double dy = ballY - blockCenterY;

        // tunneling: ball overlaps with block is why it glitches
        // fix: teleport ball outside block and then change direction

        double overlapX = (block.rect.getWidth() / 2.0 + b.circle.getRadius()) - Math.abs(dx);
        double overlapY = (block.rect.getHeight() / 2.0 + b.circle.getRadius()) - Math.abs(dy);

        if (overlapX < overlapY) {
            if (dx > 0) {
                // right
                b.circle.setCenterX(ballX + overlapX);
                b.vx = Math.abs(b.vx);
            } else {
                // left
                b.circle.setCenterX(ballX - overlapX);
                b.vx = -Math.abs(b.vx);
            }
        } else {
            if (dy > 0) {
                // top
                b.circle.setCenterY(ballY + overlapY);
                b.vy = Math.abs(b.vy);
            } else {
                // bottom
                b.circle.setCenterY(ballY - overlapY);
                b.vy = -Math.abs(b.vy);
            }
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

            if (distance <= GameConfig.FIRE_EXPLOSION_RADIUS && !block.burning && !block.invincible) {
                block.setBurning(true);
                soundManager.playBurn();
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

            if (distance <= GameConfig.ICE_EXPLOSION_RADIUS && !block.frozen && !block.invincible) {
                block.setFrozen(true);
                soundManager.playFreeze();
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
        money += (int) Math.round(block.maxHealth * coinRewardMultiplier);

        if (block.type == BlockType.EXTRA_BALL) {
            extraBallsEarned++;
            soundManager.playPowerup();

        } else if (block.type == BlockType.FIRE_POWER) {
            fireRoundsAvailable++;
            soundManager.playPowerup();

        } else if (block.type == BlockType.ICE_POWER) {
            iceRoundsAvailable++;
            soundManager.playPowerup();

        } else if (block.type == BlockType.PIERCE_POWER) {
            pierceRoundsAvailable++;
            soundManager.playPowerup();

        } else if (block.type == BlockType.SHRINK_POWER) {
            shrinkRoundsAvailable++;
            soundManager.playPowerup();
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
        // 下至上
        sortedBlocks.sort((a, b) -> Double.compare(b.rect.getY(), a.rect.getY()));

        for (Block block : sortedBlocks) {
            if (block.frozen) {
                continue;
            }

            double nextY = block.rect.getY() + GameConfig.BLOCK_SIZE;
            boolean blocked = false;

            for (Block other : blocks) {
                if (other == block) continue;

                // Add a tiny 0.1 buffer so flush edges aren't counted as an overlap
                boolean overlapX = block.rect.getX() < other.rect.getX() + other.rect.getWidth() - 0.1 &&
                        block.rect.getX() + block.rect.getWidth() > other.rect.getX() + 0.1;

                boolean overlapY = nextY < other.rect.getY() + other.rect.getHeight() - 0.1 &&
                        nextY + block.rect.getHeight() > other.rect.getY() + 0.1;

                if (overlapX && overlapY) {
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

    private boolean isBlockAreaOccupied(double x, double y, double w, double h) {
        for (Block block : blocks) {
            boolean overlap =
                    block.rect.getX() < x + w &&
                            block.rect.getX() + block.rect.getWidth() > x &&
                            block.rect.getY() < y + h &&
                            block.rect.getY() + block.rect.getHeight() > y;

            if (overlap) {
                return true;
            }
        }

        for (Block block : pendingBlocksToAdd) {
            boolean overlap =
                    block.rect.getX() < x + w &&
                            block.rect.getX() + block.rect.getWidth() > x &&
                            block.rect.getY() < y + h &&
                            block.rect.getY() + block.rect.getHeight() > y;

            if (overlap) {
                return true;
            }
        }

        return false;
    }

    private void splitBoss(Block boss) {
        if (boss.bossType != 0) return;

        int splitHp = Math.max(
                1,
                (int) (boss.maxHealth * GameConfig.BOSS1_SPLIT_HP_RATIO)
        );

        int created = 0;
        int attempts = 0;

        while (created < GameConfig.BOSS1_SPLIT_COUNT && attempts < 100) {
            attempts++;

            int maxCol = 8;
            int randomCol = random.nextInt(maxCol);

            double x = GameConfig.PLAYFIELD_MIN_X + randomCol * GameConfig.BLOCK_SIZE;
            double y = boss.rect.getY() + random.nextInt(4) * GameConfig.BLOCK_SIZE;

            double w = GameConfig.BLOCK_SIZE - 4;
            double h = GameConfig.BLOCK_SIZE - 4;

            if (isBlockAreaOccupied(x + 2, y + 2, w, h)) {
                continue;
            }

            Block miniBoss = new Block(
                    x,
                    y,
                    splitHp,
                    BlockType.NORMAL,
                    -1,
                    root
            );

            miniBoss.rect.setFill(Color.web("#7CFC00"));
            miniBoss.rect.setStroke(Color.web("#C8E6C9"));
            miniBoss.rect.setStrokeWidth(3);

            pendingBlocksToAdd.add(miniBoss);
            created++;
        }
    }

    private void boss2StoneSkill() {

        boolean boss2Exists = false;

        for (Block block : blocks) {

            if (block.type == BlockType.BOSS
                    && block.bossType == 1) {

                boss2Exists = true;
                break;
            }

        }

        if (!boss2Exists) {
            return;
        }

        // 清除上一回合的石化

        for (Block block : blocks) {
            if (block.invincible) {
                block.invincible = false;

                block.burning = false;
                block.frozen = false;

                block.updateVisuals();
            }
        }

        List<Block> candidates = new ArrayList<>();

        for (Block block : blocks) {

            if (block.type == BlockType.BOSS)
                continue;

            if (block.type != BlockType.NORMAL)
                continue;

            if (block.invincible)
                continue;

            candidates.add(block);
        }

        for (int i = 0;
             i < GameConfig.BOSS2_STONE_COUNT
                     && !candidates.isEmpty();
             i++) {

            int index =
                    random.nextInt(candidates.size());

            Block chosen =
                    candidates.remove(index);

            chosen.burning = false;
            chosen.frozen = false;

            chosen.invincible = true;
            chosen.updateVisuals();
        }
    }

    private void updateTemporaryInvincibleBlocks() {
        for (Block block : blocks) {

            if (!block.invincible) continue;

            block.invincibleTurns--;

            if (block.invincibleTurns <= 0) {
                block.invincible = false;
                block.updateVisuals();
            }
        }
    }

    private void endWave() {
        applyBurnDamage();

        updateTemporaryInvincibleBlocks();

        boss2StoneSkill();
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
            if (block.rect.getY() + block.rect.getHeight() >= GameConfig.WARNING_LINE_Y) {
                isGameOver = true;
            }
        }

        if (isGameOver) {
            triggerGameOver();
        } else {
            if (wave % GameConfig.BOSS_INTERVAL == 0) {
                spawnBoss();
            } else {
            spawnRow();
        }

            state = GameState.AIMING;
            updateRemainingBallsUI();
            updateShopUI();
            updateWaitingBallColor();
        }
    }

    private void triggerGameOver() {
        soundManager.stopBackgroundMusic();
        soundManager.playGameOver();
        state = GameState.GAME_OVER;
        gameOverWaveText.setText("You reached Wave: " + wave);
        gameOverMenu.toFront();
        gameOverMenu.setVisible(true);
        updateRemainingBallsUI();
    }

    private void clearBoard() {
        for (Block b : blocks) b.remove();
        blocks.clear();

        for (Ball b : balls) {
            root.getChildren().remove(b.circle);
        }
        balls.clear();
    }

    private void resetGame() {
        clearBoard();

        wave = 1;
        totalBalls = 1;
        ballsFired = 0;
        extraBallsEarned = 0;
        money = 0;
        coinRewardMultiplier = GameConfig.COIN_REWARD_MULTIPLIER_START;
        coinRewardUpgradeCost = GameConfig.COIN_REWARD_UPGRADE_COST_START;
        ballDamage = GameConfig.BALL_DAMAGE_START;
        damageUpgradeCost = GameConfig.BALL_DAMAGE_UPGRADE_COST_START;
        firstBallLanded = false;
        powerUpSpawnChance = GameConfig.POWER_UP_SPAWN_CHANCE_START;
        powerUpUpgradeCost = GameConfig.POWER_UP_UPGRADE_COST_START;

        fireRoundsAvailable = 0;
        fireRoundActive = false;
        iceRoundsAvailable = 0;

        forceFireRound = false;

        forceIceRound = false;
        iceRoundActive = false;

        pierceRoundsAvailable = 0;
        pierceRoundActive = false;
        forcePierceRound = false;

        shrinkRoundsAvailable = 0;
        shrinkRoundActive = false;
        forceShrinkRound = false;

        startX = GameConfig.PLAYFIELD_MIN_X + (GameConfig.PLAYFIELD_WIDTH / 2.0);
        nextStartX = startX;

        balls.add(new Ball(startX, startY, root));
        spawnRow();

        waveText.setText("Wave: " + wave);
        updateShopUI();
        updateRemainingBallsUI();

        gameOverMenu.setVisible(false);
        mainMenu.setVisible(false);
        optionsMenu.setVisible(false);

        state = GameState.AIMING;
        soundManager.playBackgroundMusic();
    }

    private boolean canSpawnNewRow() {
        double spawnY = GameConfig.PLAYFIELD_MIN_Y + GameConfig.BLOCK_SIZE;

        for (Block block : blocks) {
            // Checks if any part of the block's body is touching the top spawn row
            boolean touchesSpawnRow = block.rect.getY() < spawnY + GameConfig.BLOCK_SIZE &&
                    block.rect.getY() + block.rect.getHeight() > spawnY;
            if (touchesSpawnRow) {
                return false;
            }
        }
        return true;
    }

    private int getCurrentWaveBlockHealth() {

        if (wave <= GameConfig.BLOCK_HEALTH_LINEAR_END_WAVE) {
            return wave;
        }

        return (int) Math.round(
                GameConfig.BLOCK_HEALTH_LINEAR_END_WAVE *
                        Math.pow(
                                GameConfig.BLOCK_HEALTH_GROWTH_RATE,
                                wave - GameConfig.BLOCK_HEALTH_LINEAR_END_WAVE
                        )
        );
    }
    private void removeBlocksInBossArea(double bossX, double bossY, double bossW, double bossH) {
        Iterator<Block> it = blocks.iterator();

        while (it.hasNext()) {
            Block block = it.next();

            boolean overlap =
                    block.rect.getX() < bossX + bossW &&
                            block.rect.getX() + block.rect.getWidth() > bossX &&
                            block.rect.getY() < bossY + bossH &&
                            block.rect.getY() + block.rect.getHeight() > bossY;

            if (overlap) {
                block.remove();
                it.remove();
            }
        }
    }

    private void spawnBoss() {
        int maxBossStartCol = 8 - GameConfig.BOSS_WIDTH_BLOCKS;
        int bossStartCol = random.nextInt(maxBossStartCol + 1);

        double bossX = GameConfig.PLAYFIELD_MIN_X + bossStartCol * GameConfig.BLOCK_SIZE;
        double bossY = GameConfig.PLAYFIELD_MIN_Y + GameConfig.BLOCK_SIZE;
        double bossW = GameConfig.BLOCK_SIZE * 4;
        double bossH = GameConfig.BLOCK_SIZE * 4;

        removeBlocksInBossArea(bossX, bossY, bossW, bossH);
        int bossType = random.nextInt(2);

        int baseHealth = getCurrentWaveBlockHealth();

        int bossHealth;

        if (bossType == 0) {
            bossHealth = (int) (
                    baseHealth * GameConfig.BOSS_HP_MULTIPLIER_MIN
            );
        } else if (bossType == 1) {
            bossHealth = (int) (
                    baseHealth * GameConfig.BOSS_HP_MULTIPLIER_MID
            );
        } else {
            bossHealth = (int) (
                    baseHealth * GameConfig.BOSS_HP_MULTIPLIER_MAX
            );
        }

        bossX = GameConfig.PLAYFIELD_MIN_X
                + GameConfig.BLOCK_SIZE * 2;

        bossY = GameConfig.PLAYFIELD_MIN_Y
                + GameConfig.BLOCK_SIZE;

        Block boss = new Block(
                bossX,
                bossY,
                bossHealth,
                BlockType.BOSS,
                bossType,
                root
        );

        blocks.add(boss);
    }

    private boolean canSpawnBlockAtColumn(int column) {
        double x = GameConfig.PLAYFIELD_MIN_X + column * GameConfig.BLOCK_SIZE + 2;
        double y = GameConfig.PLAYFIELD_MIN_Y + GameConfig.BLOCK_SIZE + 2;
        double w = GameConfig.BLOCK_SIZE - 4;
        double h = GameConfig.BLOCK_SIZE - 4;

        return !isBlockAreaOccupied(x, y, w, h);
    }


    private void spawnRow() {
        int columns = 8;
        boolean specialSpawned = false;

        int blockHealth;

        blockHealth = getCurrentWaveBlockHealth();

        for (int i = 0; i < columns; i++) {

            if (!canSpawnBlockAtColumn(i)) {
                continue;
            }

            if (random.nextDouble() > 0.4) {
                BlockType type = BlockType.NORMAL;

                if (!specialSpawned && random.nextDouble() < powerUpSpawnChance) {
                    double powerRoll = random.nextDouble();

                    if (powerRoll < 0.45) {
                        type = BlockType.EXTRA_BALL;
                    } else if (powerRoll < 0.65) {
                        type = BlockType.FIRE_POWER;
                    } else if (powerRoll < 0.80) {
                        type = BlockType.ICE_POWER;
                    } else if (powerRoll < 0.92) {
                        type = BlockType.PIERCE_POWER;
                    } else {
                        type = BlockType.SHRINK_POWER;
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
        updateWaitingBallColor();
    }

    public void setForceIceRound(boolean forceIceRound) {
        this.forceIceRound = forceIceRound;
        updateWaitingBallColor();
    }

    public void setForcePierceRound(boolean forcePierceRound) {
        this.forcePierceRound = forcePierceRound;
        updateWaitingBallColor();
    }

    public void setForceShrinkRound(boolean forceShrinkRound) {
        this.forceShrinkRound = forceShrinkRound;
        updateWaitingBallColor();
    }

    public void moveBlocksUpOneRow() {
        if (state == GameState.SHOOTING || state == GameState.WAITING) return;

        Iterator<Block> it = blocks.iterator();

        while (it.hasNext()) {
            Block block = it.next();

            block.rect.setY(block.rect.getY() - GameConfig.BLOCK_SIZE);
            if (block.type == BlockType.BOSS && block.bossPane != null) {
                block.bossPane.setLayoutY(block.rect.getY());
            } else {
                if (block.text != null) {
                    block.text.setY(block.text.getY() - GameConfig.BLOCK_SIZE);
                }
                if (block.ring != null) {
                    block.ring.setCenterY(block.ring.getCenterY() - GameConfig.BLOCK_SIZE);
                }
            }
            if (block.rect.getY() + block.rect.getHeight() <= GameConfig.PLAYFIELD_MIN_Y + GameConfig.BLOCK_SIZE + 1) {
                block.remove();
                it.remove();
            }
        }

        if (wave > 1) {
            wave--;
            waveText.setText("Wave: " + wave);
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


        endWave();
    }

    private static final String[] SUFFIXES = {"", "K", "M", "B", "T", "Qa"}; // 不夠再加

    public static String formatBigNumber(int number) {
        if (number < 1000) return "" + number;

        int exponent = (int) (Math.log10(number) / 3);

        double displayNum = number / Math.pow(1000, exponent);
        return String.format("%.2f%s", displayNum, SUFFIXES[exponent]);
    }


    private void setupMainMenu() {
        mainMenu = new VBox(30);
        mainMenu.setAlignment(Pos.CENTER);
        mainMenu.setLayoutX(GameConfig.PLAYFIELD_MIN_X);
        mainMenu.setLayoutY(GameConfig.PLAYFIELD_MIN_Y);
        mainMenu.setPrefSize(GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);
        mainMenu.setStyle("-fx-background-color: #0f0f1a;");

        Text title = new Text("BLOCK BREAKER");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 45));
        title.setFill(Color.web("#3498db"));

        Button playBtn = new Button("Play Game");
        playBtn.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        playBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-min-width: 200px;");
        playBtn.setOnAction(e -> resetGame()); // resetGame handles starting the first wave

        mainMenu.getChildren().addAll(title, playBtn);
        root.getChildren().add(mainMenu);
    }

    private void setupOptionsMenu() {
        optionsMenu = new VBox(25);
        optionsMenu.setAlignment(Pos.CENTER);
        optionsMenu.setLayoutX(GameConfig.PLAYFIELD_MIN_X);
        optionsMenu.setLayoutY(GameConfig.PLAYFIELD_MIN_Y);
        optionsMenu.setPrefSize(GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);
        optionsMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.90);");
        optionsMenu.setVisible(false);

        Text title = new Text("PAUSED");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 35));
        title.setFill(Color.WHITE);

        Text volumeText = new Text("Music Volume");
        volumeText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        volumeText.setFill(Color.LIGHTGRAY);

        Slider volumeSlider = new Slider(0, 1, soundManager.getVolume());
        volumeSlider.setMaxWidth(200);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            soundManager.setVolume(newVal.doubleValue());
        });

        Text sfxText = new Text("SFX Volume");
        sfxText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        sfxText.setFill(Color.LIGHTGRAY);

        Slider sfxSlider = new Slider(0, 1, soundManager.getSfxVolume());
        sfxSlider.setMaxWidth(200);
        sfxSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            soundManager.setSfxVolume(newVal.doubleValue());
        });

        Button resumeBtn = new Button("Resume");
        resumeBtn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        resumeBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-min-width: 180px;");
        resumeBtn.setOnAction(e -> {
            optionsMenu.setVisible(false);
            state = GameState.AIMING;
        });

        Button restartBtn = new Button("Restart Game");
        restartBtn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        restartBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-min-width: 180px;");
        restartBtn.setOnAction(e -> resetGame());

        Button quitBtn = new Button("Quit to Menu");
        quitBtn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        quitBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-min-width: 180px;");
        quitBtn.setOnAction(e -> {
            optionsMenu.setVisible(false);
            clearBoard();
            mainMenu.setVisible(true);
            state = GameState.MAIN_MENU;
        });

        optionsMenu.getChildren().addAll(title, volumeText, volumeSlider, sfxText, sfxSlider, resumeBtn, restartBtn, quitBtn);
        root.getChildren().add(optionsMenu);
    }
}