package se233.project2.model;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se233.project2.Launcher;
import se233.project2.controller.RespawnTask;
import se233.project2.view.GameMenu;
import se233.project2.view.GameStage;

import java.util.ArrayList;

public class PlayerShip extends Character {
    private static final Logger logger = LogManager.getLogger(PlayerShip.class);
    private KeyCode moveUpKey = KeyCode.W;
    private KeyCode moveLeftKey = KeyCode.A;
    private KeyCode moveDownKey = KeyCode.S;
    private KeyCode moveRightKey = KeyCode.D;
    private KeyCode turnLeftKey = KeyCode.LEFT;
    private KeyCode turnRightKey = KeyCode.RIGHT;
    private KeyCode shootKey = KeyCode.SPACE;
    private KeyCode shootLaserKey = KeyCode.UP;
    private boolean isDead, isActive, isImmune;
    private final int bulletSpeed = GameMenu.gameDataMap.getOrDefault("BulletSpeed", 20);
    private ArrayList<Bullet> bulletList;
    private long lastShotTime = 0;
    private int fireRate = GameMenu.gameDataMap.getOrDefault("FireRate", 3);
    private int score;
    private Polygon hitbox;
    private int specialAttacKCount;
    private final Double[] points = {
            31.25, 11.5,
            6.25, 44.0,
            31.25, 52.5,
            55.25, 44.0
    };

    public PlayerShip(double x, double y, AnimatedSprite animatedSprite, int hp, double width, double height, int specialAttacKCount) {
        super(x, y, 0, 0, animatedSprite, hp, width, height);
        isDead = false;
        isActive = true;
        isImmune = false;
        animatedSprite.curIndex = 1;
        animatedSprite.setTimeline(100, 3);
        bulletList = new ArrayList<>();
        score = 0;
        hitbox = new Polygon();
        hitbox.getPoints().addAll(points);
        hitbox.setFill(Color.TRANSPARENT);
        this.specialAttacKCount = specialAttacKCount;
        this.getChildren().add(hitbox);
    }

    @Override
    public void draw() {
        Platform.runLater( () -> {
            if (getAx() != 0 || getAy() != 0)
                animatedSprite.start();
            this.setTranslateX(getX() - animatedSprite.getFitWidth()/2);
            this.setTranslateY(getY() - animatedSprite.getFitHeight()/2);
        });
    }

    @Override
    public boolean isCollided(MovingObject movingObject) {
        if (isDead) return false;
        synchronized (hitbox) {
            Bounds boundsInScene = hitbox.localToScene(hitbox.getBoundsInLocal());
            Bounds boundsInOuter = ((GameStage) this.getParent()).sceneToLocal(boundsInScene);
            return boundsInOuter.intersects(movingObject.getBoundsInParent());
        }
    }

    public void moveRight() { setAx(1); }
    public void moveLeft() {
        setAx(-1);
    }
    public void moveUp() { setAy(-1); }
    public void moveDown() { setAy(1); }

    public void turnLeft() {
        Platform.runLater(() -> {
            animatedSprite.setRotate(animatedSprite.getRotate() - 10);
            Rotate rotate = new Rotate(-10, getWidth()/2, getHeight()/2);
            hitbox.getTransforms().add(rotate);
            logger.trace(String.format("Turned Left: %.2f", animatedSprite.getRotate()));
        });
    }
    public void turnRight() {
        Platform.runLater(() -> {
            animatedSprite.setRotate(animatedSprite.getRotate() + 10);
            Rotate rotate = new Rotate(10, getWidth()/2, getHeight()/2);
            hitbox.getTransforms().add(rotate);
            logger.trace(String.format("Turned Right: %.2f", animatedSprite.getRotate()));
        });
    }
    public void die() {
        isDead = true;
        this.setHp(getHp() - 1);
        if (this.getHp() == 0)
            ((GameStage) this.getParent()).setRunning(false);
    }

    public void shoot() {
        if (System.currentTimeMillis() - lastShotTime < 1000/fireRate || !isActive()) {
            return;
        }
        double bvx = Math.sin(Math.toRadians(animatedSprite.getRotate())) * bulletSpeed;
        double bvy = -Math.cos(Math.toRadians(animatedSprite.getRotate())) * bulletSpeed;
        double bx = Math.sin(Math.toRadians(animatedSprite.getRotate())) * animatedSprite.getFitHeight()/2 + getX();
        double by = -Math.cos(Math.toRadians(animatedSprite.getRotate())) * animatedSprite.getFitHeight()/2 + getY();

        Media shotSound = new Media(Launcher.class.getResource("audio/bulletShot.mp3").toString());
        MediaPlayer shotPlayer = new MediaPlayer(shotSound);
        shotPlayer.play();
        Bullet bullet = new Bullet(bx, by, 0, 0, new AnimatedSprite(new Image(Launcher.class.getResource("bullet_sprite_yellow.png").toString()), 4, 4, 1, 0, 0, 100, 69), 30, 25);
        bullet.setVx(bvx);
        bullet.setVy(bvy);
        Platform.runLater(() -> bullet.animatedSprite.setRotate(animatedSprite.getRotate() - 90));
        bulletList.add(bullet);
        Platform.runLater(() -> ((Pane) getParent()).getChildren().add(bullet));
        lastShotTime = System.currentTimeMillis();
        logger.debug(String.format("Shot Bullet: x:%.2f, y:%.2f, vx:%.2f, vy:%.2f, direction:%.2f", bx, by, bvx, bvy, bullet.animatedSprite.getRotate() + 90));
    }

    public void shootBomb() {
        if (System.currentTimeMillis() - lastShotTime < 1000 || !isActive( ) || specialAttacKCount<=0) {
            return;
        }
        double bvx = Math.sin(Math.toRadians(animatedSprite.getRotate())) * bulletSpeed;
        double bvy = -Math.cos(Math.toRadians(animatedSprite.getRotate())) * bulletSpeed;

        double bx = Math.sin(Math.toRadians(animatedSprite.getRotate())) * (animatedSprite.getFitHeight()/2) + getX();
        double by = -Math.cos(Math.toRadians(animatedSprite.getRotate())) * (animatedSprite.getFitHeight()/2) + getY();

        Media shotSound = new Media(Launcher.class.getResource("audio/Bomb.mp3").toString());
        MediaPlayer shotPlayer = new MediaPlayer(shotSound);
        shotPlayer.play();
        Bomb bomb = new Bomb(bx, by, 0, 0, new AnimatedSprite(new Image(Launcher.class.getResource("bomb_sprite_sheet.png").toString()), 3, 3, 1, 0, 0, 128, 159), 40, 40);
        bomb.setBulletLife(25);
        bomb.setVx(bvx);
        bomb.setVy(bvy);
        Platform.runLater(() -> {
            bomb.animatedSprite.setRotate(animatedSprite.getRotate());
        });
        Platform.runLater(() -> ((Pane) getParent()).getChildren().add(bomb));
        bulletList.add(bomb);
        lastShotTime = System.currentTimeMillis();
        specialAttacKCount--;
        logger.debug(String.format("Shot Bomb: x:%.2f, y:%.2f, vx:%.2f, vy:%.2f, direction:%.2f", bx, by, bvx, bvy, bomb.animatedSprite.getRotate()));
    }

    public void deathRender() {
        Platform.runLater(() -> {
            this.setVisible(false);
            Explosion explosion = new Explosion((Pane) this.getParent(), this.getX(), this.getY());
//            playerShip.setActive(false);
        });
        logger.info(String.format("Died: x:%.2f, y:%.2f", getX(), getY()));
    }

    public void respawnRender() {
        isImmune = true;
        new Thread(new RespawnTask(this)).start();
        logger.info(String.format("Respawned"));
    }


    public boolean isDead() { return isDead; }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public int getScore() {
        return score;
    }

    public void increaseScore(int score) {
        this.score += score;
        logger.info(String.format("Increased Score: %d, Score: %d", score, this.score));
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isImmune() {
        return isImmune;
    }

    public void setImmune(boolean immune) {
        this.isImmune = immune;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
    public KeyCode getMoveUpKey() {
        return moveUpKey;
    }

    public KeyCode getMoveLeftKey() {
        return moveLeftKey;
    }

    public KeyCode getMoveDownKey() {
        return moveDownKey;
    }

    public KeyCode getMoveRightKey() {
        return moveRightKey;
    }

    public ArrayList<Bullet> getBulletList() {
        return bulletList;
    }

    public KeyCode getTurnLeftKey() {
        return turnLeftKey;
    }

    public KeyCode getTurnRightKey() {
        return turnRightKey;
    }

    public KeyCode getShootKey() {
        return shootKey;
    }

    public KeyCode getShootLaserKey() {
        return shootLaserKey;
    }

    public Polygon getHitbox() {
        return hitbox;
    }

    public int getSpecialAttacKCount() {
        return specialAttacKCount;
    }

    public void setSpecialAttacKCount(int specialAttacKCount) {
        this.specialAttacKCount = specialAttacKCount;
    }
}
