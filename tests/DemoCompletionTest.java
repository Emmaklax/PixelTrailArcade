import com.game.main.util.LevelHandler;
import com.game.object.Enemy;
import com.game.object.Flagpole;
import com.game.object.Player;
import com.game.object.util.Handler;
import com.game.object.util.KeyInput;
import java.awt.Canvas;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class DemoCompletionTest {
    public static void main(String[] args) {
        testCompletion(false);
        testCompletion(true);
        testDeadPlayer();
        System.out.println("OK: 13 enemigos, bandera funcional, descenso y final estable sin controles activos");
    }

    private static void testCompletion(boolean large) {
        Handler handler = new Handler();
        LevelHandler level = new LevelHandler(handler);
        level.start();
        long enemies = handler.getGameObjs().stream().filter(object -> object instanceof Enemy).count();
        if (enemies != 13) throw new AssertionError("La demo debe tener 13 enemigos, no " + enemies);
        Flagpole flag = handler.getGameObjs().stream().filter(object -> object instanceof Flagpole)
                .map(object -> (Flagpole)object).findFirst().orElseThrow();
        Player player = handler.getPlayer();
        if (large) player.grow();
        player.setX(flag.getX());
        player.setY(flag.getY() - player.getHeight());
        flag.tick();
        if (handler.isLevelEnding()) throw new AssertionError("No debe activar por encima de la bandera");
        player.setY(flag.getY() + flag.getHeight() + 1);
        flag.tick();
        if (handler.isLevelEnding()) throw new AssertionError("No debe ganar desde debajo del suelo");
        player.setX(flag.getX() - player.getWidth() - 1);
        player.setY(100 * 3);
        flag.tick();
        if (handler.isLevelEnding()) throw new AssertionError("Debe tocar realmente el poste");

        Canvas canvas = new Canvas();
        KeyInput input = new KeyInput(handler);
        input.keyPressed(key(canvas, KeyEvent.KEY_PRESSED, KeyEvent.VK_RIGHT));
        player.setPipeDownRequested(true);
        player.setX(flag.getX() - player.getWidth() + 4);
        BufferedImage before = renderFlag(flag);
        handler.tick();
        level.tick();
        if (!handler.isLevelEnding() || handler.isDemoComplete())
            throw new AssertionError("Tocar el poste debe iniciar la secuencia, no terminarla de inmediato");
        if (player.getVelX() != 0 || player.getVelY() != 0 || player.isPipeDownRequested())
            throw new AssertionError("La secuencia debe cancelar el movimiento y la entrada a tuberias");
        Enemy enemy = handler.getGameObjs().stream().filter(object -> object instanceof Enemy)
                .map(object -> (Enemy)object).findFirst().orElseThrow();
        float enemyX = enemy.getX(), enemyY = enemy.getY();
        for (int code : new int[]{KeyEvent.VK_SPACE, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN, KeyEvent.VK_F}) {
            input.keyPressed(key(canvas, KeyEvent.KEY_PRESSED, code));
            input.keyReleased(key(canvas, KeyEvent.KEY_RELEASED, code));
        }
        if (player.getVelX() != 0 || player.getVelY() != 0 || player.isCrouching())
            throw new AssertionError("Los controles no deben interrumpir el final");
        for (int tick = 0; tick < 180; tick++) {
            handler.tick();
            level.tick();
        }
        if (!handler.isDemoComplete() || player.isDead() || player.getLives() != 3)
            throw new AssertionError("La demo debe terminar sin quitar vidas");
        if (player.getY() + player.getHeight() != 208 * 3)
            throw new AssertionError("Mario debe bajar hasta el suelo");
        if (enemyX != enemy.getX() || enemyY != enemy.getY())
            throw new AssertionError("Los enemigos deben detenerse durante el final");
        BufferedImage after = renderFlag(flag);
        boolean changed = false;
        for (int y = 0; y < before.getHeight(); y++) for (int x = 0; x < before.getWidth(); x++) {
            if (before.getRGB(x, y) != after.getRGB(x, y)) changed = true;
        }
        if (!changed) throw new AssertionError("La bandera debe descender visualmente");
        float x = player.getX(), y = player.getY();
        for (int tick = 0; tick < 300; tick++) handler.tick();
        if (!handler.isDemoComplete() || x != player.getX() || y != player.getY())
            throw new AssertionError("El final debe permanecer estable");
        level.start();
        if (handler.isLevelEnding() || handler.isDemoComplete())
            throw new AssertionError("Un nivel nuevo debe reiniciar el estado de final");
    }

    private static void testDeadPlayer() {
        Handler handler = new Handler();
        new LevelHandler(handler).start();
        Flagpole flag = handler.getGameObjs().stream().filter(object -> object instanceof Flagpole)
                .map(object -> (Flagpole)object).findFirst().orElseThrow();
        Player player = handler.getPlayer();
        player.setX(flag.getX());
        player.setY(100 * 3);
        player.triggerDeath();
        flag.tick();
        if (handler.isLevelEnding()) throw new AssertionError("Mario muerto no debe activar la meta");
    }

    private static KeyEvent key(Canvas canvas, int id, int code) {
        return new KeyEvent(canvas, id, 0, 0, code, KeyEvent.CHAR_UNDEFINED);
    }

    private static BufferedImage renderFlag(Flagpole flag) {
        BufferedImage image = new BufferedImage(60, 720, BufferedImage.TYPE_INT_ARGB);
        var graphics = image.createGraphics();
        graphics.translate(50 - flag.getX(), 0);
        flag.render(graphics);
        graphics.dispose();
        return image;
    }
}
