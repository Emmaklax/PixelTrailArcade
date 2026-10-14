package com.game.gfx;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Animation {
    private final int speed;
    private final BufferedImage[] images;
    private int index;
    private int count;
    private BufferedImage currentImage;

    public Animation(int speed, BufferedImage... images) {
        if (speed < 1 || images == null || images.length == 0) throw new IllegalArgumentException("La animación requiere velocidad y cuadros");
        this.speed = speed;
        this.images = images.clone();
        this.currentImage = images[0];
    }

    public void runAnimation() {
        if (++index >= speed) {
            index = 0;
            currentImage = images[count++ % images.length];
        }
    }

    public void drawAnimation(Graphics g, int x, int y, int width, int height) {
        if (currentImage != null) g.drawImage(currentImage, x, y, width, height, null);
    }
}
