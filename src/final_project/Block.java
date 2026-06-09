package final_project;

import javafx.scene.layout.Pane;
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
    BlockType type;
    boolean burning = false;

    private Pane root;

    public Block(double x, double y, int health, BlockType type, Pane root) {
        this.root = root;
        this.health = health;
        this.type = type;

        rect = new Rectangle(x + 2, y + 2, GameConfig.BLOCK_SIZE - 4, GameConfig.BLOCK_SIZE - 4);

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

        } else {
            text.setFill(Color.WHITE);
            updateVisuals();
            root.getChildren().addAll(rect, text);
        }

        text.setY(y + (GameConfig.BLOCK_SIZE + text.getLayoutBounds().getHeight() / 2) / 2 - 2);
    }

    public void setBurning(boolean burning) {
        this.burning = burning;
        updateVisuals();
    }

    public void updateVisuals() {
        if (type == BlockType.FIRE_POWER) {
            text.setText("F");
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);
            rect.setFill(Color.web("#ffcc66"));
            return;
        }

        if (type == BlockType.ICE_POWER) {
            text.setText("I");
            text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);
            rect.setFill(Color.web("#99ddff"));
            return;
        }

        text.setText(String.valueOf(health));
        text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);

        if (burning) {
            rect.setFill(Color.ORANGERED);
        } else {
            double hue = (health * 12) % 360;
            rect.setFill(Color.hsb(hue, 0.75, 0.9));
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