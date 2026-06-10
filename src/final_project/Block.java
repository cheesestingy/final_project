package final_project;

import java.math.BigInteger;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;


public class Block {
    Rectangle rect;
    Text text;
    Circle ring;
    BigInteger health;
    BigInteger max_health;
    BlockType type;
    boolean burning = false;
    boolean frozen = false;
    int maxHealth;

    private Pane root;

    public Block(double x, double y, BigInteger health, BlockType type, Pane root) {
        this.root = root;
        this.health = health;
        this.max_health = health;
        this.type = type;

        rect = new Rectangle(x + 2, y + 2, GameConfig.BLOCK_SIZE - 4, GameConfig.BLOCK_SIZE - 4);
        rect.setArcWidth(0);
        rect.setArcHeight(0);
        rect.setEffect(new DropShadow(8, Color.BLACK));

        text = new Text();
        text.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        if (type == BlockType.EXTRA_BALL) {
            ring = new Circle(x + GameConfig.BLOCK_SIZE / 2, y + GameConfig.BLOCK_SIZE / 2, GameConfig.BLOCK_SIZE / 2 - 8);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.BLACK);
            ring.setStrokeWidth(3);

            text.setFill(Color.BLACK);
            updateVisuals();
            root.getChildren().addAll(rect, ring, text);

        } else if (type == BlockType.FIRE_POWER) {
            ring = new Circle(x + GameConfig.BLOCK_SIZE / 2, y + GameConfig.BLOCK_SIZE / 2, GameConfig.BLOCK_SIZE / 2 - 8);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.ORANGERED);
            ring.setStrokeWidth(4);

            rect.setFill(Color.web("#ffcc66"));
            text.setText("F");
            text.setFill(Color.ORANGERED);
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);

            root.getChildren().addAll(rect, ring, text);

        } else if (type == BlockType.ICE_POWER) {
            ring = new Circle(x + GameConfig.BLOCK_SIZE / 2, y + GameConfig.BLOCK_SIZE / 2, GameConfig.BLOCK_SIZE / 2 - 8);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.LIGHTBLUE);
            ring.setStrokeWidth(4);

            rect.setFill(Color.web("#99ddff"));
            text.setText("I");
            text.setFill(Color.BLUE);
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);

            root.getChildren().addAll(rect, ring, text);

        }else if (type == BlockType.PIERCE_POWER) {
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
        } else {
            text.setFill(Color.WHITE);
            updateVisuals();
            root.getChildren().addAll(rect, text);
        }

        text.setY(y + (GameConfig.BLOCK_SIZE + text.getLayoutBounds().getHeight() / 2) / 2 - 2);
    }

    private void setGlow(Color color) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(15);
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
        if (type == BlockType.FIRE_POWER) {
            text.setText("F");
            text.setFill(Color.ORANGERED);
            rect.setFill(Color.web("#ffcc66"));
            rect.setStroke(Color.ORANGERED);
            rect.setStrokeWidth(3);
            return;
        }
        if (type == BlockType.ICE_POWER) {
            text.setText("I");
            text.setFill(Color.DEEPSKYBLUE);
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
            return;
        }
        if (type == BlockType.SHRINK_POWER) {
            text.setText("S");
            text.setFill(Color.HOTPINK);
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);
            rect.setFill(Color.PINK);
            return;
        }

        text.setText(String.valueOf(health));
        text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);

        if (frozen) {
            rect.setFill(Color.LIGHTBLUE);
            setGlow(Color.LIGHTBLUE);
        } else if (burning) {
            rect.setFill(Color.ORANGE);
            setGlow(Color.ORANGE);
        } else {
            double hue = health.multiply(BigInteger.valueOf(12))
                    .remainder(BigInteger.valueOf(360))
                    .doubleValue();
            rect.setFill(Color.hsb(hue, 0.75, 0.9));
            rect.setStroke(Color.LIGHTGRAY);
            rect.setStrokeWidth(1.5);
            rect.setFill(Color.hsb(hue, 0.75, 0.9));
            setGlow(Color.color(0, 0, 0, 0.4));
        }


    }



    public void shiftDown() {
        rect.setY(rect.getY() + GameConfig.BLOCK_SIZE);
        text.setY(text.getY() + GameConfig.BLOCK_SIZE);

        if (ring != null) {
            ring.setCenterY(ring.getCenterY() + GameConfig.BLOCK_SIZE);
        }
    }

    public void remove() {
        if (ring != null) {
            root.getChildren().removeAll(rect, ring, text);
        } else {
            root.getChildren().removeAll(rect, text);
        }
    }
}