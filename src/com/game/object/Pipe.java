package com.game.object;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.game.main.Game;
import com.game.object.util.ObjectId;

/**
 * Tuberias del escenario (entrantes o solidas) con soporte para transporte subterraneo.
 */
public class Pipe extends GameObject {

    private final int spriteIndex;
    private final BufferedImage[] pipeSprites;
    private final boolean enterable;

    public Pipe(int x, int y, int width, int height, int scale, int spriteIndex, boolean enterable) {
        super(x, y, ObjectId.Pipe, width, height, scale);
        this.enterable = enterable;
        this.spriteIndex = Math.floorMod(spriteIndex, 4);
        this.pipeSprites = Game.getTexture().getPipe1();
    }

    @Override
    public void tick() {
        // Objeto solido estatico
    }

    @Override
    public void render(Graphics g) {
        if (pipeSprites != null && spriteIndex < pipeSprites.length && pipeSprites[spriteIndex] != null) {
            g.drawImage(
                    pipeSprites[spriteIndex],
                    (int) getX(),
                    (int) getY(),
                    (int) getWidth(),
                    (int) getHeight(),
                    null
            );
        }
    }

    public boolean isEnterable() {
        return enterable;
    }

    @Override
    public Rectangle getBounds() {
        int xOffset = (spriteIndex == 1 || spriteIndex == 3) ? 2 * getScale() : 0;
        int widthReduction = (spriteIndex == 1 || spriteIndex == 3) ? 4 * getScale() : 0;
        return new Rectangle(
                (int) getX() + xOffset,
                (int) getY(),
                (int) getWidth() - widthReduction,
                (int) getHeight()
        );
    }
}
