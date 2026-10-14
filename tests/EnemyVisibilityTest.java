import com.game.main.Game;
import com.game.main.util.LevelHandler;
import com.game.object.Block;
import com.game.object.Enemy;
import com.game.object.util.Handler;
import java.awt.image.BufferedImage;

public class EnemyVisibilityTest {
    public static void main(String[] args) {
        for (Enemy.Kind kind : new Enemy.Kind[]{Enemy.Kind.GOOMBA, Enemy.Kind.GREEN_KOOPA}) {
            Handler handler = new Handler();
            for (int x = 0; x < 40; x++) handler.addObj(new Block(x * 16, 208, 16, 16, 0, 3));
            Enemy enemy = new Enemy(160, kind == Enemy.Kind.GOOMBA ? 192 : 176, 3, handler, kind);
            handler.addObj(enemy);
            BufferedImage image = new BufferedImage(960, 720, BufferedImage.TYPE_INT_ARGB);
            var graphics = image.createGraphics();
            enemy.render(graphics);
            graphics.dispose();
            int pixels = 0;
            for (int y = 0; y < 720; y++) for (int x = 0; x < 960; x++)
                if ((image.getRGB(x, y) >>> 24) != 0) pixels++;
            if (pixels == 0) throw new AssertionError(kind + " invisible");
            for (int i = 0; i < 120; i++) enemy.tick();
            if (enemy.shouldRemove() || Math.abs(enemy.getY() + enemy.getHeight() - 624) > 1)
                throw new AssertionError(kind + " atraviesa el suelo: y=" + enemy.getY());
        }
        Handler world = new Handler();
        new LevelHandler(world).start();
        long visible = world.getGameObjs().stream().filter(o -> o instanceof Enemy
                && o.getX() >= 0 && o.getX() + o.getWidth() <= Game.getWindowWidth()
                && o.getHeight() == 96).count();
        if (visible == 0) throw new AssertionError("No hay Koopa visible al iniciar");
        System.out.println("OK: sprites visibles, suelo estable y Koopa en pantalla inicial");
    }
}
