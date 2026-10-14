import com.game.gfx.BufferedImageLoader;
import com.game.gfx.Texture;
import com.game.object.Enemy;
import com.game.object.Player;
import com.game.object.util.Handler;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class KoopaTest {
    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args[0].equals("sprites")) testSprites();
        if (args.length == 0 || args[0].equals("shells")) testShells();
        System.out.println("OK: texturas de tortugas y caparazones sin lanzamiento");
    }

    private static void testSprites() throws Exception {
        BufferedImage sheet = new BufferedImageLoader().loadImage("/images/sprites/creature_atlas.png");
        Texture texture = new Texture();
        for (int i = 0; i < 2; i++) {
            assertPixels(texture.getKoopaFrames()[i], expected(sheet, 210 + i * 30, 0, 24, 32), "Koopa " + i);
            assertPixels(texture.getParatroopaFrames()[i], expected(sheet, 270 + i * 30, 0, 24, 32), "Paratroopa " + i);
        }
        assertPixels(texture.getKoopaShell(), expected(sheet, 360, 4, 16, 16), "Caparazón verde");
        for (Enemy.Kind kind : new Enemy.Kind[]{Enemy.Kind.GREEN_KOOPA, Enemy.Kind.RED_KOOPA, Enemy.Kind.PARATROOPA}) {
            Handler handler = new Handler();
            Enemy enemy = new Enemy(0, 0, 1, handler, kind);
            Player player = new Player(40, 0, 1, handler);
            int row = kind == Enemy.Kind.RED_KOOPA ? 30 : 0;
            assertRendered(enemy, expected(sheet, kind == Enemy.Kind.PARATROOPA ? 270 : 210, row, 24, 32));
            if (kind == Enemy.Kind.PARATROOPA) enemy.playerContact(player, true);
            enemy.playerContact(player, true);
            assertRendered(enemy, expected(sheet, 360, row + 4, 16, 16));
            for (int tick = 0; tick < 420; tick++) enemy.tick();
            assertRendered(enemy, expected(sheet, 210, row, 24, 32));
        }
    }

    private static void testShells() {
        for (Enemy.Kind kind : new Enemy.Kind[]{Enemy.Kind.GREEN_KOOPA, Enemy.Kind.RED_KOOPA, Enemy.Kind.PARATROOPA}) {
            for (int scale : new int[]{1, 3}) {
                Handler handler = new Handler();
                Player player = new Player(0, 0, scale, handler);
                Enemy enemy = new Enemy(40, 40, scale, handler, kind);
                float feet = enemy.getY() + enemy.getHeight();
                if (kind == Enemy.Kind.PARATROOPA) enemy.playerContact(player, true);
                if (enemy.playerContact(player, true) != Enemy.Contact.STOMPED)
                    throw new AssertionError("La tortuga debe esconderse al pisarla");
                float x = enemy.getX(), y = enemy.getY();
                if (enemy.getHeight() != 16 * scale || y + enemy.getHeight() != feet)
                    throw new AssertionError("El caparazón debe conservar el apoyo en el suelo");
                for (boolean stomp : new boolean[]{false, true}) for (int side : new int[]{-1, 1}) {
                    player.setX(x + side * 30 * scale);
                    Enemy.Contact result = enemy.playerContact(player, stomp);
                    if (result != (stomp ? Enemy.Contact.STOMPED : Enemy.Contact.NONE))
                        throw new AssertionError(kind + ": el caparazón no debe lanzarse ni hacer daño");
                    enemy.tick();
                    if (enemy.getX() != x || enemy.getY() != y || enemy.getVelX() != 0 || enemy.getVelY() != 0)
                        throw new AssertionError("El caparazón debe permanecer quieto");
                }
                for (int tick = 4; tick < 420; tick++) enemy.tick();
                if (enemy.getHeight() != 32 * scale || enemy.getY() + enemy.getHeight() != feet || enemy.getVelX() == 0)
                    throw new AssertionError("La tortuga debe volver a caminar después de esperar");
            }
        }
    }

    private static BufferedImage expected(BufferedImage sheet, int left, int top, int height, int canvasHeight) {
        BufferedImage image = new BufferedImage(16, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.drawImage(sheet.getSubimage(left, top, 16, height), 0, canvasHeight - height, null);
        g.dispose();
        return image;
    }

    private static void assertRendered(Enemy enemy, BufferedImage expected) {
        BufferedImage image = new BufferedImage(16, (int)enemy.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.translate(-enemy.getX(), -enemy.getY());
        enemy.render(g);
        g.dispose();
        BufferedImage flipped = new BufferedImage(16, expected.getHeight(), BufferedImage.TYPE_INT_ARGB);
        g = flipped.createGraphics();
        g.drawImage(expected, 16, 0, -16, expected.getHeight(), null);
        g.dispose();
        assertPixels(image, flipped, "Tortuga renderizada");
    }

    private static void assertPixels(BufferedImage actual, BufferedImage expected, String name) {
        if (actual.getWidth() != expected.getWidth() || actual.getHeight() != expected.getHeight())
            throw new AssertionError(name + ": dimensiones incorrectas");
        for (int y = 0; y < actual.getHeight(); y++) for (int x = 0; x < actual.getWidth(); x++) {
            int a = actual.getRGB(x, y), e = expected.getRGB(x, y);
            if (a != e && ((a >>> 24) != 0 || (e >>> 24) != 0))
                throw new AssertionError(name + ": pixel incorrecto en " + x + "," + y);
        }
    }
}