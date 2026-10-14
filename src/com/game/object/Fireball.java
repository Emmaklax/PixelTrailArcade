package com.game.object;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.game.main.Game;
import com.game.object.util.Handler;
import com.game.object.util.ObjectId;

/**
 * Proyectil de fuego lanzado por Mario: rebota en el suelo y destruye enemigos al impactar.
 */
public final class Fireball extends GameObject {

    private static final float SPEED = 10f;
    private static final float BOUNCE_SPEED = -6.5f;

    private final Handler handler;
    private int age;
    private int bounces;
    private boolean removeRequested;

    public Fireball(float x, float y, int scale, boolean facesRight, Handler handler) {
        super(x, y, ObjectId.Item, 8, 8, scale);
        this.handler = handler;
        setVelX(facesRight ? SPEED : -SPEED);
        setVelY(1f);
    }

    @Override
    public void tick() {
        if (++age > 180) return;

        float oldY = getY();
        setX(getX() + getVelX());
        if (checkEnemyHit()) return;

        Rectangle bounds = getBounds();
        for (GameObject object : handler.getGameObjs()) {
            if (!isSolid(object) || !bounds.intersects(object.getBounds())) continue;
            if (object instanceof Pipe || object instanceof Block) {
                Rectangle obstacle = object.getBounds();
                if (getVelX() > 0) setX(obstacle.x - getWidth());
                else setX(obstacle.x + obstacle.width);
                setVelX(0);
                setVelY(0);
                return;
            }
        }

        setY(getY() + getVelY());
        if (checkEnemyHit()) return;

        for (GameObject object : handler.getGameObjs()) {
            if (!isSolid(object) || !getBounds().intersects(object.getBounds())) continue;
            Rectangle obstacle = object.getBounds();
            if (getVelY() >= 0 && oldY + getHeight() <= obstacle.y + getVelY() + 1) {
                setY(obstacle.y - getHeight());
                setVelY(BOUNCE_SPEED);
                if (++bounces > 3) {
                    setVelY(0);
                    setVelX(0);
                }
            } else if (getVelY() < 0) {
                setY(obstacle.y + obstacle.height);
                setVelY(0);
            }
        }
        setVelY(Math.min(10f, getVelY() + 0.45f));
    }

    private boolean isSolid(GameObject object) {
        return object != this && (object.getId() == ObjectId.Block || object.getId() == ObjectId.Pipe);
    }

    private boolean checkEnemyHit() {
        for (GameObject object : handler.getGameObjs()) {
            if (!(object instanceof Enemy enemy) || !enemy.isActive()
                    || !getBounds().intersects(enemy.getBounds())) continue;

            if (enemy.hitByFireball()) {
                removeRequested = true;
                return true;
            }
        }
        return false;
    }

    public boolean shouldRemove() {
        return removeRequested || age > 180 || getY() > Game.getWindowHeight() + getHeight() || getVelX() == 0;
    }

    @Override
    public void render(Graphics g) {
        int frame = age / 4;
        int drawX = getVelX() < 0 ? (int)(getX() + getWidth()) : (int)getX();
        int drawWidth = getVelX() < 0 ? (int)-getWidth() : (int)getWidth();
        g.drawImage(Game.getTexture().getFireballFrame(frame), drawX, (int)getY(), drawWidth, (int)getHeight(), null);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
    }
}
