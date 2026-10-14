package com.game.gfx;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BufferedImageLoader {

    private BufferedImage image;

    public BufferedImage loadImage(String path) throws IOException {
        var resource = getClass().getResource(path);
        if (resource == null) {
            throw new IOException("No se encontró el recurso: " + path);
        }
        image = ImageIO.read(resource);
        if (image == null) {
            throw new IOException("El recurso no es una imagen válida: " + path);
        }
        return image;
    }
}
