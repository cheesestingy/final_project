package final_project;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.effect.Glow;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Line;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class Ball {
    Circle circle;
    double vx = 0;
    double vy = 0;
    boolean active = false;
    boolean returning = false;

    boolean fireBall = false;
    boolean iceBall = false;


    private Pane root;
    private static final double TRAIL_LIFETIME = 0.35;



    public Ball(double x, double y, Pane root) {
        this.root = root;

        circle = new Circle(x, y, GameConfig.BALL_RADIUS, Color.WHITE);
        setGlow(Color.WHITE);
        root.getChildren().add(circle);
    }

    public void setFireBall(boolean fireBall) {
        this.fireBall = fireBall;

        if (fireBall) {
            circle.setFill(Color.ORANGERED);
            setGlow(Color.ORANGERED);
        } else if (!iceBall) {
            circle.setFill(Color.WHITE);
            setGlow(Color.WHITE);
        }
    }

    public void setIceBall(boolean iceBall) {
        this.iceBall = iceBall;

        if (iceBall) {
            circle.setFill(Color.LIGHTBLUE);
            setGlow(Color.LIGHTBLUE);
        } else if (!fireBall) {
            circle.setFill(Color.WHITE);
            setGlow(Color.WHITE);
        }
    }

    public void resetType() {
        fireBall = false;
        iceBall = false;
        circle.setFill(Color.WHITE);
    }

    private void setGlow(Color color) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(10);
        glow.setSpread(0.3);
        circle.setEffect(glow);
    }

    public void addTrail(double oldX, double oldY) {
        Line line = new Line(oldX, oldY, circle.getCenterX(), circle.getCenterY());

        if (fireBall) {
            line.setStroke(Color.ORANGE);
        } else if (iceBall) {
            line.setStroke(Color.LIGHTBLUE);
        } else {
            line.setStroke(Color.WHITE);
        }

        line.setStrokeWidth(5);
        line.setOpacity(0.45);
        line.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        DropShadow glow = new DropShadow();
        glow.setRadius(15);
        glow.setSpread(0.5);
        glow.setColor((Color) line.getStroke());
        line.setEffect(glow);

        root.getChildren().add(line);

        int circleIndex = root.getChildren().indexOf(circle);
        root.getChildren().remove(line);
        root.getChildren().add(Math.max(0, circleIndex), line);

        circle.toFront();

        FadeTransition fade = new FadeTransition(Duration.seconds(TRAIL_LIFETIME), line);
        fade.setFromValue(0.45);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> root.getChildren().remove(line));
        fade.play();
    }


}