package com.game.object;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.game.object.util.ObjectId;

/** Static panorama plus sky-colored cutouts where live, collidable tiles are drawn. */
public final class LevelBackdrop extends GameObject {
    private static final Color SKY = new Color(92, 148, 252);
    private final BufferedImage image;
    private final int scale;
    private final Set<Long> maskedTiles = new HashSet<>();
    private final List<Rectangle> maskedRegions = new ArrayList<>();

    public LevelBackdrop(BufferedImage image, int scale) {
        super(0, 0, ObjectId.Decoration, image.getWidth(), image.getHeight(), scale);
        this.image = image;
        this.scale = scale;
    }

    public void maskTile(int tileX, int tileY) {
        maskedTiles.add((((long) tileY) << 32) | (tileX & 0xffffffffL));
    }

    public void maskRegion(int x, int y, int width, int height) {
        maskedRegions.add(new Rectangle(x, y, width, height));
    }

    @Override public void tick() { }

    @Override public void render(Graphics g) {
        g.drawImage(image, 0, 0, image.getWidth() * scale, image.getHeight() * scale, null);
        g.setColor(SKY);
        for (long tile : maskedTiles) {
            int x = (int) tile;
            int y = (int) (tile >> 32);
            g.fillRect(x * 16 * scale, y * 16 * scale, 16 * scale, 16 * scale);
        }
        for (Rectangle region : maskedRegions) {
            g.fillRect(region.x * scale, region.y * scale, region.width * scale, region.height * scale);
        }
    }

    @Override public Rectangle getBounds() { return new Rectangle(); }
}
