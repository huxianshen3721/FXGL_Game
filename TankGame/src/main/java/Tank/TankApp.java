package Tank;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.time.LocalTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Point2D;
import javafx.scene.ParallelCamera;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author HuXianshen
 * @creat 2022-03-21:09
 */

public class TankApp extends GameApplication {

    private Entity tankEntity;

    private boolean isMoving;

    private LocalTimer shootTimer;

    private Duration shootDelay = Duration.seconds(0.25);

    private Dir dir;
    public enum Dir {
        UP, DOWN, LEFT, RIGHT
    }

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(700);
        gameSettings.setHeight(500);
        gameSettings.setTitle("Tank Battle");
        gameSettings.setVersion("0.1");
        gameSettings.setAppIcon("tank.png");
    }

    @Override
    protected void onPreInit() {
        FXGL.getSettings().setGlobalMusicVolume(0.5);
        FXGL.getSettings().setGlobalSoundVolume(0.8);
        FXGL.loopBGM("bg.mp3");
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("x", 0.0);
        vars.put("list", new ArrayList<>());
        vars.put("name", "");
    }

    @Override
    protected void initGame() {
        FXGL.getip("score").addListener((ob, ov, nv) ->{
            if(nv.intValue() >= 20) {
                FXGL.getNotificationService().pushNotification("very good");
            }
        });
        shootTimer = FXGL.newLocalTimer();
        Canvas canvas = new Canvas(100,100);
        GraphicsContext g2d = canvas.getGraphicsContext2D();
        g2d.setFill(Color.web("#FFEC03"));
        g2d.fillRect(0, 0, 80, 30);
        g2d.setFill(Color.web("#CEBC17"));
        g2d.fillRect(15, 30, 50, 40);
        g2d.setFill(Color.web("#FFEC03"));
        g2d.fillRect(0, 70, 80, 30);
        g2d.setFill(Color.web("#F9EE8A"));
        g2d.fillRect(40, 40, 60, 20);

        tankEntity = FXGL.entityBuilder()
                //.view(canvas)
                //.bbox(BoundingShape.box(100,100))
                .viewWithBBox(canvas)
                //.view(new Text("Mercury"))
                .build();
        tankEntity.setRotationOrigin(new Point2D(50, 50));
        FXGL.getGameWorld().addEntity(tankEntity);

        //Point2D center = tankEntity.getCenter();
        //System.out.println(center);
        //System.out.println(tankEntity.getHeight());
        //System.out.println(tankEntity.getWidth());

        createEnmy();
    }

    private void createEnmy() {
        FXGL.entityBuilder()
                .type(GameType.ENEMY)
                .at(FXGLMath.random(60, 700 - 60), FXGLMath.random(60, 500 - 60))
                .viewWithBBox(new Rectangle(60, 60, Color.BLUE))
                .collidable()
                .buildAndAttach();
    }

    @Override
    protected void initInput() {
        FXGL.getInput().addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                if(isMoving) {
                    return;
                }
                dir = Dir.UP;
                isMoving = true;
                tankEntity.translateY(-5);
                tankEntity.setRotation(270);
            }
        }, KeyCode.UP);
        FXGL.getInput().addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                if(isMoving) {
                    return;
                }
                dir = Dir.DOWN;
                isMoving = true;
                tankEntity.translateY(5);
                tankEntity.setRotation(90);
            }
        }, KeyCode.DOWN);
        FXGL.getInput().addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                if(isMoving) {
                    return;
                }
                dir = Dir.LEFT;
                isMoving = true;
                tankEntity.translateX(-5);
                tankEntity.setRotation(180);
            }
        }, KeyCode.LEFT);
        FXGL.getInput().addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                if(isMoving) {
                    return;
                }
                dir = Dir.RIGHT;
                isMoving = true;
                tankEntity.translateX(5);
                tankEntity.setRotation(0);
            }
        }, KeyCode.RIGHT);

        FXGL.getInput().addAction(new UserAction("shoot") {
            @Override
            protected void onAction() {
                //Entity build = FXGL.entityBuilder().build();
                //FXGL.getGameWorld().addEntity(build);
                if(!shootTimer.elapsed(shootDelay)) {
                    return;
                }
                FXGL.play("shoot.wav");
                shootTimer.capture();

                Point2D p = null;
                if(dir==Dir.UP) {
                    p = new Point2D(0, -1);
                } else if(dir==Dir.DOWN) {
                    p = new Point2D(0, 1);
                } else if(dir==Dir.LEFT) {
                    p = new Point2D(-1, 0);
                } else if(dir==Dir.RIGHT) {
                    p = new Point2D(1, 0);
                }
                Entity bullet = FXGL.entityBuilder()
                        .type(GameType.BULLET)
                        .at(tankEntity.getCenter().getX()-10, tankEntity.getCenter().getY()-10)
                        .viewWithBBox(new Rectangle(20, 20))
                        .with(new ProjectileComponent(p, 600))
                        .with(new OffscreenCleanComponent())
                        .collidable()
                        .buildAndAttach();
            }
        }, KeyCode.SPACE);
    }

    @Override
    protected void initPhysics() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.ENEMY) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity enemy) {
                //int score = FXGL.geti("score") + 10;
                //FXGL.set("score", score);
                FXGL.play("bomb.wav");
                FXGL.inc("score", 10);
                bullet.removeFromWorld();
                Point2D center = enemy.getCenter();
                enemy.removeFromWorld();

                Circle circle = new Circle(10, Color.RED);
                Duration duration = Duration.seconds(.35);
                Entity boom = FXGL.entityBuilder()
                        .at(center)
                        .with(new ExpireCleanComponent(duration))
                        .view(circle)
                        .buildAndAttach();
                ScaleTransition st = new ScaleTransition(duration, circle);
                st.setToX(10);
                st.setToY(10);

                FadeTransition ft = new FadeTransition(duration, circle);
                ft.setToValue(0);

                ParallelTransition pt = new ParallelTransition(st, ft);
                //pt.setOnFinished(event -> boom.removeFromWorld());
                pt.play();

                createEnmy();
            }
        });
    }

    @Override
    protected void initUI() {
        //Text text = FXGL.addVarText("score", 20, 30);
        //text.setFill(Color.BLUE);
        //text.fontProperty().unbind();
        //text.setFont(Font.font(35));
        Text text = FXGL.getUIFactoryService()
                .newText(FXGL.getip("score").asString("score:%d"));
        text.setLayoutX(30);
        text.setLayoutY(30);
        text.setFill(Color.BLUE);
        FXGL.addUINode(text);
    }

    @Override
    protected void onUpdate(double tpf) {
        isMoving = false;
        //System.out.println(FXGL.getGameWorld().getEntities().size());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
