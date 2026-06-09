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
    boolean isSpecial;

    private Pane root;

    public Block(double x, double y, int health, boolean isSpecial, Pane root) {
        this.root = root;
        this.health = health;
        this.isSpecial = isSpecial;

        rect = new Rectangle(
                x + 2,
                y + 2,
                GameConfig.BLOCK_SIZE - 4,
                GameConfig.BLOCK_SIZE - 4
        );

        text = new Text();
        text.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        if (isSpecial) {
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
        } else {
            text.setFill(Color.WHITE);
            updateVisuals();
            root.getChildren().addAll(rect, text);
        }

        text.setY(y + (GameConfig.BLOCK_SIZE + text.getLayoutBounds().getHeight() / 2) / 2 - 2);
    }

    public void updateVisuals() {
        text.setText(String.valueOf(health));
        text.setX(rect.getX() + (rect.getWidth() - text.getLayoutBounds().getWidth()) / 2);

        double hue = (health * 12) % 360;
        rect.setFill(Color.hsb(hue, 0.75, 0.9));
    }

    public void shiftDown() {
        rect.setY(rect.getY() + GameConfig.BLOCK_SIZE);
        text.setY(text.getY() + GameConfig.BLOCK_SIZE);

        if (isSpecial && ring != null) {
            ring.setCenterY(ring.getCenterY() + GameConfig.BLOCK_SIZE);
        }
    }

    public void remove() {
        if (isSpecial && ring != null) {
            root.getChildren().removeAll(rect, ring, text);
        } else {
            root.getChildren().removeAll(rect, text);
        }
    }
}