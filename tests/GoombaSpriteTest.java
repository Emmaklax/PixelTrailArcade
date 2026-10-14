import com.game.gfx.BufferedImageLoader;
import com.game.gfx.Texture;
import java.awt.image.BufferedImage;

public class GoombaSpriteTest {
    public static void main(String[] args) throws Exception {
        BufferedImage sheet = new BufferedImageLoader().loadImage("/images/sprites/creature_atlas.png");
        Texture texture = new Texture();
        BufferedImage[] frames = texture.getGoombaFrames();
        if (frames.length != 2) throw new AssertionError("Se esperan dos fotogramas de caminata");
        for (int i = 0; i < frames.length; i++) {
            assertSprite(frames[i], sheet, i * 30, 4, "Goomba caminando " + i);
        }
        assertSprite(texture.getGoombaSquashed(), sheet, 60, 0, "Goomba aplastado");
        System.out.println("OK: ambos Goombas conservan los pies y el sprite aplastado no cambia");
    }

    private static void assertSprite(BufferedImage sprite, BufferedImage sheet, int left, int top, String name) {
        if (sprite.getWidth() != 16 || sprite.getHeight() != 16)
            throw new AssertionError(name + ": el lienzo debe medir 16x16");
        for (int y = 15; y >= 0; y--) for (int x = 0; x < 16; x++) {
            if (sprite.getRGB(x, y) != sheet.getRGB(left + x, top + y))
                throw new AssertionError(name + ": pixel recortado incorrectamente en " + x + "," + y);
        }
    }
}