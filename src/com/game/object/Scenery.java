package com.game.object;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.game.object.util.ObjectId;

/** A non-colliding, pixel-art scenery sprite rendered behind the level geometry. */
public final class Scenery extends GameObject {
    private final BufferedImage image;

    public Scenery(int x, int y, BufferedImage image, int scale) {
        super(x, y, ObjectId.Decoration, image.getWidth(), image.getHeight(), scale);
        this.image = image;
    }

    @Override public void tick() { }

    @Override public void render(Graphics g) {
        g.drawImage(image, (int)getX(), (int)getY(), (int)getWidth(), (int)getHeight(), null);
    }

    @Override public Rectangle getBounds() { return new Rectangle(); }
}
