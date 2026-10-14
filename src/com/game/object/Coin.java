package com.game.object;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.game.gfx.Animation;
import com.game.main.Game;
import com.game.object.util.ObjectId;

/**
 * Moneda emergente animada obtenida al golpear bloques de interrogacion.
 */
public class Coin extends GameObject {

    private final Animation animation;
    private int lifeTicks;

    public Coin(float x, float y, int scale) {
        super(x, y, ObjectId.Item, 16, 16, scale);
        this.animation = Game.getTexture().createCoinAnimation();
    }

    @Override
    public void tick() {
        if (animation != null) {
            animation.runAnimation();
        }
        setY(getY() - 6f);
        lifeTicks++;
    }

    @Override
    public void render(Graphics g) {
        if (animation != null) {
            animation.drawAnimation(g, (int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
        }
    }

    public boolean shouldRemove() {
        return lifeTicks >= 30;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
    }
}
