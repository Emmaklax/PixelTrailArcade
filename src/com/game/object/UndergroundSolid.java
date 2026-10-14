package com.game.object;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.game.object.util.ObjectId;

/** Collision tile for scenery already drawn into the supplied underground panorama. */
public final class UndergroundSolid extends GameObject {
    public UndergroundSolid(int x, int y, int width, int height, int scale) {
        super(x, y, ObjectId.Block, width, height, scale);
    }

    @Override public void tick() { }
    @Override public void render(Graphics g) { }
    @Override public Rectangle getBounds() {
        return new Rectangle((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
    }
}
