package final_project;

import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class Block {
    Rectangle rect;
    Text text;
    Circle ring;

    int health;
    int maxHealth;
    BlockType type;

    boolean burning = false;
    boolean frozen = false;
    boolean alreadySplit = false;
    boolean invincible = false;
    int invincibleTurns = 0;

    public int bossType = -1;

    public StackPane bossPane;
    public Label bossLabel;

    private Pane root;

    public Block(double x, double y, int health, BlockType type, Pane root) {
        this(x, y, health, type, -1, root);
    }

    public Block(double x, double y, int health, BlockType type, int bossType, Pane root) {
        this.root = root;
        this.health = health;
        this.maxHealth = health;
        this.type = type;
        this.bossType = bossType;

        rect = new Rectangle(
                x + 2,
                y + 2,
                GameConfig.BLOCK_SIZE - 4,
                GameConfig.BLOCK_SIZE - 4
        );

        text = new Text();
        text.setFont(Font.font("Impact", FontWeight.BOLD, 24));

        rect.setEffect(new DropShadow(8, Color.BLACK));

        if (type == BlockType.EXTRA_BALL) {
            ring = new Circle(
                    x + GameConfig.BLOCK_SIZE / 2,
                    y + GameConfig.BLOCK_SIZE / 2,
                    GameConfig.BLOCK_SIZE / 2 - 8
            );
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.BLACK);
            ring.setStrokeWidth(3);

            text.setFill(Color.BLACK);
            updateVisuals();
            root.getChildren().addAll(rect, ring, text);

        } else if (type == BlockType.FIRE_POWER) {
            ring = new Circle(
                    x + GameConfig.BLOCK_SIZE / 2,
                    y + GameConfig.BLOCK_SIZE / 2,
                    GameConfig.BLOCK_SIZE / 2 - 8
            );
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.ORANGERED);
            ring.setStrokeWidth(4);

            rect.setFill(Color.web("#ffcc66"));
            text.setText("F");
            text.setFill(Color.ORANGERED);
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);

            root.getChildren().addAll(rect, ring, text);

        } else if (type == BlockType.ICE_POWER) {
            ring = new Circle(
                    x + GameConfig.BLOCK_SIZE / 2,
                    y + GameConfig.BLOCK_SIZE / 2,
                    GameConfig.BLOCK_SIZE / 2 - 8
            );
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.LIGHTBLUE);
            ring.setStrokeWidth(4);

            rect.setFill(Color.web("#99ddff"));
            text.setText("I");
            text.setFill(Color.BLUE);
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);

            root.getChildren().addAll(rect, ring, text);

        } else if (type == BlockType.PIERCE_POWER) {
            ring = new Circle(
                    x + GameConfig.BLOCK_SIZE / 2,
                    y + GameConfig.BLOCK_SIZE / 2,
                    GameConfig.BLOCK_SIZE / 2 - 8
            );
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.DARKGRAY);
            ring.setStrokeWidth(4);

            rect.setFill(Color.GRAY);
            text.setText("P");
            text.setFill(Color.BLACK);
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);

            root.getChildren().addAll(rect, ring, text);

        } else if (type == BlockType.SHRINK_POWER) {
            ring = new Circle(
                    x + GameConfig.BLOCK_SIZE / 2,
                    y + GameConfig.BLOCK_SIZE / 2,
                    GameConfig.BLOCK_SIZE / 2 - 8
            );
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.HOTPINK);
            ring.setStrokeWidth(4);

            rect.setFill(Color.PINK);
            text.setText("S");
            text.setFill(Color.HOTPINK);
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);

            root.getChildren().addAll(rect, ring, text);

        } else if (type == BlockType.BOSS) {
            rect.setWidth(GameConfig.BLOCK_SIZE * 4 - 4);
            rect.setHeight(GameConfig.BLOCK_SIZE * 4 - 4);

            rect.setFill(Color.web("#66BB6A"));
            rect.setStroke(Color.web("#C8E6C9"));
            rect.setStrokeWidth(6);

            bossLabel = new Label(health + "\n=w=");
            bossLabel.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 42;" +
                            "-fx-font-family: 'Impact';" +
                            "-fx-font-weight: bold;" +
                            "-fx-effect: dropshadow(gaussian, black, 4, 0.8, 2, 2);"
            );

            bossPane = new StackPane();
            bossPane.setLayoutX(rect.getX());
            bossPane.setLayoutY(rect.getY());
            bossPane.setPrefSize(rect.getWidth(), rect.getHeight());
            bossPane.getChildren().add(bossLabel);

            root.getChildren().addAll(rect, bossPane);

        } else {
            text.setFill(Color.WHITE);
            updateVisuals();
            root.getChildren().addAll(rect, text);
        }

        text.setY(y + (GameConfig.BLOCK_SIZE + text.getLayoutBounds().getHeight() / 2) / 2 - 2);
        updateVisuals();
    }

    private void setGlow(Color color) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(22);
        glow.setSpread(0.5);
        rect.setEffect(glow);
    }

    public void setBurning(boolean burning) {
        this.burning = burning;
        updateVisuals();
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
        updateVisuals();
    }

    public void updateVisuals() {
        if (type == BlockType.BOSS) {
            if (bossLabel != null) {
                bossLabel.setText(health + "\n0w0");
            }

            if (bossPane != null) {
                bossPane.setLayoutX(rect.getX());
                bossPane.setLayoutY(rect.getY());
                bossPane.setPrefSize(rect.getWidth(), rect.getHeight());
            }

            if (bossType == 1) {
                rect.setFill(Color.GRAY);
                rect.setStroke(Color.BLACK);
            } else {
                rect.setFill(Color.web("#66BB6A"));
                rect.setStroke(Color.web("#C8E6C9"));
            }

            rect.setStrokeWidth(6);
            return;
        }

        if (type == BlockType.FIRE_POWER) {
            text.setText("F");
            text.setFill(Color.ORANGERED);
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);
            rect.setFill(Color.web("#ffcc66"));
            rect.setStroke(Color.ORANGERED);
            rect.setStrokeWidth(3);
            return;
        }

        if (type == BlockType.ICE_POWER) {
            text.setText("I");
            text.setFill(Color.DEEPSKYBLUE);
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);
            rect.setFill(Color.web("#99ddff"));
            rect.setStroke(Color.DEEPSKYBLUE);
            rect.setStrokeWidth(3);
            return;
        }

        if (type == BlockType.PIERCE_POWER) {
            text.setText("P");
            text.setFill(Color.BLACK);
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);
            rect.setFill(Color.GRAY);
            rect.setStroke(Color.DARKGRAY);
            rect.setStrokeWidth(3);
            return;
        }

        if (type == BlockType.SHRINK_POWER) {
            text.setText("S");
            text.setFill(Color.HOTPINK);
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);
            rect.setFill(Color.PINK);
            rect.setStroke(Color.HOTPINK);
            rect.setStrokeWidth(3);
            return;
        }
        if (invincible) {
            text.setText("∞");
            text.setFill(Color.BLACK);
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);

            rect.setFill(Color.GRAY);
            rect.setStroke(Color.BLACK);
            rect.setStrokeWidth(4);

            setGlow(Color.BLACK);

            return;
        }
        text.setText(String.valueOf(health));
        text.setFill(Color.WHITE);
        text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);

        if (frozen) {
            rect.setFill(Color.LIGHTBLUE);
            setGlow(Color.LIGHTBLUE);
        } else if (burning) {
            rect.setFill(Color.ORANGE);
            setGlow(Color.ORANGE);
        } else {
            double hue = (health * 12) % 360;
            rect.setFill(Color.hsb(hue, 0.75, 0.9));
            rect.setStroke(Color.WHITE);
            rect.setStrokeWidth(1.5);
            setGlow(Color.color(0, 0, 0, 0.4));
        }
    }

    public void shiftDown() {
        rect.setY(rect.getY() + GameConfig.BLOCK_SIZE);

        if (type == BlockType.BOSS) {
            if (bossPane != null) {
                bossPane.setLayoutY(rect.getY());
            }
        } else {
            text.setY(text.getY() + GameConfig.BLOCK_SIZE);

            if (ring != null) {
                ring.setCenterY(ring.getCenterY() + GameConfig.BLOCK_SIZE);
            }
        }

        updateVisuals();
    }

    public void remove() {
        if (type == BlockType.BOSS) {
            root.getChildren().removeAll(rect, bossPane);
            return;
        }

        if (ring != null) {
            root.getChildren().removeAll(rect, ring, text);
        } else {
            root.getChildren().removeAll(rect, text);
        }
    }
}