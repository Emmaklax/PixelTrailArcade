package com.game.object;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.game.gfx.Animation;
import com.game.main.Game;
import com.game.object.util.Handler;
import com.game.object.util.ObjectId;

/**
 * Objeto coleccionable que otorga mejoras al jugador con colisiones solidas.
 */
public class PowerUp extends GameObject {
    public enum Type {
        MUSHROOM,
        FIRE_FLOWER,
        ONE_UP
    }

    private final BufferedImage sprite;
    private final Animation flowerAnimation;
    private final Type type;
    private final Handler handler;
    private boolean collected;

    public PowerUp(float x, float y, int scale) {
        this(x, y, scale, null);
    }

    public PowerUp(float x, float y, int scale, Handler handler) {
        this(x, y, scale, handler, Type.MUSHROOM);
    }

    public PowerUp(float x, float y, int scale, Handler handler, Type type) {
        super(x, y, ObjectId.Item, 16, 16, scale);
        this.handler = handler;
        this.type = type;
        sprite = switch (type) {
            case MUSHROOM -> Game.getTexture().getMushroom();
            case FIRE_FLOWER -> Game.getTexture().getFireFlower();
            case ONE_UP -> Game.getTexture().getOneUpMushroom();
        };
        flowerAnimation = type == Type.FIRE_FLOWER ? Game.getTexture().createFireFlowerAnimation() : null;
        if (type == Type.MUSHROOM || type == Type.ONE_UP) {
            setVelX(2.5f);
            setVelY(-5f);
        }
    }

    @Override
    public void tick() {
        if (flowerAnimation != null) {
            flowerAnimation.runAnimation();
        }
        if (type == Type.FIRE_FLOWER) return;

        float oldY = getY();
        setX(getX() + getVelX());
        Rectangle sideBounds = new Rectangle(
                (int)getX(), (int)getY() + 2,
                (int)getWidth(), Math.max(1, (int)getHeight() - 4));
        for (GameObject object : getLevelObjects()) {
            if (!isSolid(object) || !sideBounds.intersects(object.getBounds())) continue;
            if (getVelX() > 0) setX(object.getBounds().x - getWidth());
            else if (getVelX() < 0) setX(object.getBounds().x + object.getBounds().width);
            setVelX(-getVelX());
        }

        setY(getY() + getVelY());
        for (GameObject object : getLevelObjects()) {
            if (!isSolid(object) || !getBounds().intersects(object.getBounds())) continue;
            Rectangle obstacle = object.getBounds();
            if (getVelY() >= 0 && oldY + getHeight() <= obstacle.y + Math.max(0, getVelY())) {
                setY(obstacle.y - getHeight());
                setVelY(0f);
            } else if (getVelY() < 0) {
                setY(obstacle.y + obstacle.height);
                setVelY(0);
            }
        }
        setVelY(Math.min(12f, getVelY() + 0.5f));
    }

    private boolean isSolid(GameObject object) {
        return object != this && (object.getId() == ObjectId.Block || object.getId() == ObjectId.Pipe);
    }

    private java.util.List<GameObject> getLevelObjects() {
        return handler == null ? java.util.List.of() : handler.getGameObjs();
    }

    @Override
    public void render(Graphics g) {
        if (!collected) {
            if (flowerAnimation != null) {
                flowerAnimation.drawAnimation(g, (int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
            } else if (sprite != null) {
                g.drawImage(sprite, (int)getX(), (int)getY(), (int)getWidth(), (int)getHeight(), null);
            }
        }
    }

    public boolean collect(Player player) {
        if (collected || !player.applyPowerUp(type)) return false;
        collected = true;
        return true;
    }

    public boolean shouldRemove() {
        return collected || getY() > Game.getWindowHeight() + getHeight();
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
    }

    public Type getType() {
        return type;
    }
}
