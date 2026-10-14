package com.game.object;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import com.game.main.Game;
import com.game.object.util.Handler;
import com.game.object.util.ObjectId;

/**
 * Proyectiles hostiles generados por enemigos (martillos de Hammer Bro y fuego de Bowser).
 */
public final class EnemyProjectile extends GameObject {

    public enum Kind {
        HAMMER,
        BOWSER_FIRE
    }

    private final Kind kind;
    private final Handler handler;
    private int lifetime;
    private boolean removed;

    public EnemyProjectile(float x, float y, int scale, boolean facesLeft, Kind kind, Handler handler) {
        super(x, y, ObjectId.Item, kind == Kind.HAMMER ? 8 : 32, kind == Kind.HAMMER ? 8 : 5, scale);
        this.kind = kind;
        this.handler = handler;
        setVelX((facesLeft ? -1 : 1) * (kind == Kind.HAMMER ? 4.5f : 3.5f) * scale);
        setVelY(kind == Kind.HAMMER ? -5.5f * scale : 0);
    }

    @Override
    public void tick() {
        if (removed) return;
        lifetime++;
        setX(getX() + getVelX());
        setY(getY() + getVelY());

        if (kind == Kind.HAMMER) {
            setVelY(Math.min(8f * getScale(), getVelY() + 0.28f * getScale()));
        }

        Player player = handler.getPlayer();
        if (player != null && !player.isDead() && getBounds().intersects(player.getBounds())) {
            player.takeEnemyHit(getVelX() > 0);
            removed = true;
        }

        if (lifetime > 150 || getY() > Game.getWindowHeight() + getHeight()
                || getX() < -getWidth() || getX() > Game.getWindowWidth() * 4f) {
            removed = true;
        }
    }

    public boolean shouldRemove() {
        return removed;
    }

    @Override
    public void render(Graphics g) {
        if (kind == Kind.BOWSER_FIRE) {
            var fireFrames = Game.getTexture().getBowserFireFrames();
            if (fireFrames != null && fireFrames.length > 0) {
                g.drawImage(fireFrames[(lifetime / 5) % fireFrames.length],
                        (int)getX(), (int)getY(), (int)getWidth(), (int)getHeight(), null);
            }
            return;
        }

        int x = (int)getX();
        int y = (int)getY();
        int s = getScale();

        g.setColor(new Color(100, 55, 22));
        g.fillRect(x + 3 * s, y, 2 * s, 6 * s);
        g.setColor(new Color(185, 130, 65));
        g.fillRect(x, y + 2 * s, 8 * s, 3 * s);
        g.setColor(new Color(55, 42, 32));
        g.fillRect(x + 1 * s, y + 1 * s, 6 * s, 1 * s);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
    }
}
