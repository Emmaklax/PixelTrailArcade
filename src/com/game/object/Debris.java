package com.game.object;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import com.game.main.Game;

/**
 * Efecto visual de fragmentos de ladrillo cuando Mario rompe un bloque.
 */
public class Debris {

    private final float[] posX = new float[4];
    private final float[] posY = new float[4];
    private final float[] velX = {-2.5f, 2.5f, -2.5f, 2.5f};
    private final float[] velY = {-7.5f, -7.5f, -4.5f, -4.5f};
    private final BufferedImage[] textures;
    private final int fragmentSize;

    public Debris(float x, float y, float width, float height, int scale) {
        posX[0] = x;
        posX[1] = x + width / 2f;
        posX[2] = x;
        posX[3] = x + width / 2f;

        posY[0] = y;
        posY[1] = y;
        posY[2] = y + height / 2f;
        posY[3] = y + height / 2f;

        this.fragmentSize = Math.max(1, (int)(width / 4));
        this.textures = Game.getTexture().getDebris1();
    }

    public void tick() {
        for (int i = 0; i < 4; i++) {
            posX[i] += velX[i];
            posY[i] += velY[i];
            velY[i] += 0.5f;
        }
    }

    public boolean shouldRemove() {
        for (float py : posY) {
            if (py < Game.getWindowHeight()) return false;
        }
        return true;
    }

    public void draw(Graphics g) {
        for (int i = 0; i < 4; i++) {
            if (textures != null && i < textures.length && textures[i] != null) {
                g.drawImage(textures[i], (int)posX[i], (int)posY[i], fragmentSize, fragmentSize, null);
            }
        }
    }
}
