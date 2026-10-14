package com.game.main.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.game.gfx.BufferedImageLoader;
import com.game.main.Game;
import com.game.object.Block;
import com.game.object.Enemy;
import com.game.object.Flagpole;
import com.game.object.LevelBackdrop;
import com.game.object.Pipe;
import com.game.object.Player;
import com.game.object.PowerUp;
import com.game.object.UndergroundSolid;
import com.game.object.util.Handler;

/** Builds the overworld from the supplied 1-1 panorama using 16px source tiles. */
public class LevelHandler {
    private static final int TILE = 16;
    private static final int SCALE = 3;
    private static final int FLOOR_ROW = 13;
    private static final int CASTLE_START = 198;
    private final Handler handler;
    private BufferedImage sourceImage;
    private boolean underground;

    public LevelHandler(Handler handler) { this.handler = handler; }

    public void start() { createWorld1_1(null); }

    public void tick() {
        Player player = handler.getPlayer();
        if (player == null || player.isDead() || player.isGameOver() || handler.isLevelEnding()) return;
        if (!underground && player.isPipeDownRequested()) {
            for (var object : handler.getGameObjs()) {
                if (!(object instanceof Pipe pipe) || !pipe.isEnterable()) continue;
                float centerX = player.getX() + player.getWidth() / 2f;
                float bottom = player.getY() + player.getHeight();
                if (centerX >= pipe.getX() && centerX <= pipe.getX() + pipe.getWidth()
                        && Math.abs(bottom - pipe.getY()) <= 6) {
                    enterUnderground(player);
                    return;
                }
            }
        } else if (underground && player.getVelX() > 0
                && player.getX() + player.getWidth() >= 208 * SCALE) {
            createWorld1_1(player);
        }
    }

    private void createWorld1_1(Player returningPlayer) {
        BufferedImage image;
        try {
            image = new BufferedImageLoader().loadImage("/stages/grassland_overview.png");
        } catch (java.io.IOException e) {
            throw new IllegalStateException("No se pudo cargar stages/grassland_overview.png", e);
        }
        if (image == null || image.getWidth() < TILE || image.getHeight() < 15 * TILE) {
            throw new IllegalStateException("No se pudo cargar stages/grassland_overview.png");
        }
        sourceImage = image;
        underground = false;
        handler.clearLevelObjects();
        if (returningPlayer != null) {
            returningPlayer.setCrouching(false);
            returningPlayer.setPipeDownRequested(false);
            returningPlayer.setVelX(0);
            returningPlayer.setVelY(0);
            returningPlayer.setJumped(false);
        }

        int columns = image.getWidth() / TILE;
        // The supplied image contains an underground illustration below the overworld.
        // Keep all 240px of the overworld so the ground's lower decorative row is preserved.
        LevelBackdrop backdrop = new LevelBackdrop(image.getSubimage(0, 0, columns * TILE, 15 * TILE), SCALE);
        handler.addObj(backdrop);
        // Separate the flag from the panorama so it can slide down the existing pole.
        backdrop.maskRegion(3160, 48, 15, 16);
        handler.addObj(new Flagpole(3175, 40, 208, SCALE, handler,
                image.getSubimage(3160, 48, 15, 16)));

        for (int x = 0; x < columns; x++) {
            for (int y = 5; y < FLOOR_ROW; y++) {
                if (x >= CASTLE_START) continue; // castle and flag are already in the panorama
                int brown = countBrickPixels(image, x, y);
                if (brown <= 35) continue;
                int gold = countQuestionPixels(image, x, y);
                int type = brown > 100 ? (gold > 80 ? 2 : 1) : 3;
                PowerUp.Type contents = null;
                if (type == 2) {
                    // The mushroom sits in the third question block in the opening group.
                    if (x == 22 && y == 5) contents = PowerUp.Type.MUSHROOM;
                    else if (x == 94 && y == 5) contents = PowerUp.Type.FIRE_FLOWER;
                }
                backdrop.maskTile(x, y);
                handler.addObj(new Block(x * TILE, y * TILE, TILE, TILE, type, SCALE, contents));
            }
        }

        // The panorama marks the secret block with a dashed outline; replace the
        // annotation with an actually hidden, hittable question block.
        backdrop.maskTile(64, 8);
        handler.addObj(new Block(64 * TILE, 8 * TILE, TILE, TILE, 2, SCALE, PowerUp.Type.ONE_UP, true));
        // This is an editor-style pipe-entry marker in the flattened map, not scenery.
        backdrop.maskTile(57, 7);
        backdrop.maskTile(57, 8);
        backdrop.maskTile(58, 7);
        backdrop.maskTile(58, 8);
        // Remove the second pipe-entry annotation too; no destination is defined here.
        for (int x = 163; x <= 164; x++)
            for (int y = 8; y <= 10; y++) backdrop.maskTile(x, y);

        // Floor tiles are read from the map's actual brown ground band; gaps stay open.
        for (int x = 0; x < columns; x++) {
            if (countBrickPixels(image, x, FLOOR_ROW) > 100) {
                backdrop.maskTile(x, FLOOR_ROW);
                handler.addObj(new Block(x * TILE, FLOOR_ROW * TILE, TILE, TILE, 0, SCALE));
            }
        }

        addPipe(backdrop, 28, 2);
        addPipe(backdrop, 38, 3);
        addPipe(backdrop, 46, 4);
        addPipe(backdrop, 57, 4, true);
        addPipe(backdrop, 163, 2);
        addPipe(backdrop, 179, 2);

        // Menos enemigos y encuentros mas espaciados para la demo.
        addGoombaGroups(12, 35, 53, 82, 99, 124, 154);
        addGroundEnemy(18, Enemy.Kind.GREEN_KOOPA);
        addGroundEnemy(91, Enemy.Kind.GREEN_KOOPA);
        addGroundEnemy(132, Enemy.Kind.RED_KOOPA);
        addGroundEnemy(169, Enemy.Kind.PARATROOPA);

        addPiranhaPlant(38, 3);
        addPiranhaPlant(179, 2);

        // Mario starts one tile above the floor, matching the world's 16px grid.
        Player player = returningPlayer == null
                ? new Player(2 * TILE, (FLOOR_ROW - 1) * TILE, SCALE, handler)
                : returningPlayer;
        if (returningPlayer != null) {
            float pipeX = 163 * TILE * SCALE;
            float pipeY = (FLOOR_ROW - 2) * TILE * SCALE;
            player.setX(pipeX + (32 * SCALE - player.getWidth()) / 2f);
            player.setY(pipeY - player.getHeight());
        }
        handler.setPlayer(player);
    }

    private void addGoombaGroups(int... tileXs) {
        for (int tileX : tileXs) addGroundEnemy(tileX, Enemy.Kind.GOOMBA);
    }

    private void addGroundEnemy(int tileX, Enemy.Kind kind) {
        int nativeHeight = switch (kind) {
            case GREEN_KOOPA, RED_KOOPA, PARATROOPA, HAMMER_BRO, BOWSER -> 32;
            default -> 16;
        };
        int y = FLOOR_ROW * TILE - nativeHeight;
        handler.addObj(new Enemy(tileX * TILE, y, SCALE, handler, kind));
    }

    private void addPiranhaPlant(int pipeTileX, int pipeHeight) {
        int topY = (FLOOR_ROW - pipeHeight) * TILE;
        // Align the 16px plant to the middle of the 32px pipe opening.
        handler.addObj(new Enemy(pipeTileX * TILE + TILE / 2f, topY,
                SCALE, handler, Enemy.Kind.PIRANHA_PLANT));
    }

    private void addPipe(LevelBackdrop backdrop, int x, int height) {
        addPipe(backdrop, x, height, false);
    }

    private void addPipe(LevelBackdrop backdrop, int x, int height, boolean enterable) {
        int top = FLOOR_ROW - height;
        for (int row = top; row < FLOOR_ROW; row++) {
            backdrop.maskTile(x, row);
            backdrop.maskTile(x + 1, row);
            int sprite = row == top ? 0 : 1;
            handler.addObj(new Pipe(x * TILE, row * TILE, 32, TILE, SCALE, sprite, enterable && sprite == 0));
        }
    }

    private void enterUnderground(Player player) {
        underground = true;
        player.setPipeDownRequested(false);
        handler.clearLevelObjects();
        player.setCrouching(false);

        final int cropX = 752, cropY = 240, roomWidth = 320, roomHeight = 240;
        BufferedImage room = new BufferedImage(roomWidth, roomHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = room.createGraphics();
        graphics.drawImage(sourceImage.getSubimage(cropX, cropY, roomWidth, roomHeight), 0, 0, null);
        // Remove the A/B route annotations from the supplied level diagram.
        graphics.setColor(Color.BLACK);
        graphics.fillRect(42, 32, 18, 22);
        graphics.fillRect(202, 185, 21, 17);
        graphics.dispose();
        handler.addObj(new LevelBackdrop(room, SCALE));

        for (int x = 0; x < roomWidth / TILE; x++) {
            for (int y = 0; y < roomHeight / TILE; y++) {
                int sourceX = cropX + x * TILE, sourceY = cropY + y * TILE;
                if (countCyanPixels(sourceImage, sourceX, sourceY) > 20
                        || countGreenPixels(sourceImage, sourceX, sourceY) > 20) {
                    handler.addObj(new UndergroundSolid(x * TILE, y * TILE, TILE, TILE, SCALE));
                }
            }
        }
        player.setX(48 * SCALE);
        player.setY(8 * SCALE);
        player.setVelX(0);
        player.setVelY(0);
        player.setJumped(false);
        handler.setPlayer(player);
    }

    private int countBrickPixels(BufferedImage image, int tileX, int tileY) {
        int count = 0;
        for (int y = tileY * TILE; y < (tileY + 1) * TILE; y++) {
            for (int x = tileX * TILE; x < (tileX + 1) * TILE; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >>> 16) & 255, g = (rgb >>> 8) & 255, b = rgb & 255;
                if (r > 120 && r > g * 1.5 && g > b * 1.15) count++;
            }
        }
        return count;
    }

    private int countQuestionPixels(BufferedImage image, int tileX, int tileY) {
        int count = 0;
        for (int y = tileY * TILE; y < (tileY + 1) * TILE; y++) {
            for (int x = tileX * TILE; x < (tileX + 1) * TILE; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >>> 16) & 255, g = (rgb >>> 8) & 255, b = rgb & 255;
                if (r > 200 && g > 130 && g > b * 1.3) count++;
            }
        }
        return count;
    }

    private int countCyanPixels(BufferedImage image, int startX, int startY) {
        int count = 0;
        for (int y = startY; y < startY + TILE; y++) for (int x = startX; x < startX + TILE; x++) {
            int rgb = image.getRGB(x, y);
            int r = (rgb >>> 16) & 255, g = (rgb >>> 8) & 255, b = rgb & 255;
            if (g > 70 && b > 70 && g > r * 1.5 && b > g * 0.65 && b < g * 1.45) count++;
        }
        return count;
    }

    private int countGreenPixels(BufferedImage image, int startX, int startY) {
        int count = 0;
        for (int y = startY; y < startY + TILE; y++) for (int x = startX; x < startX + TILE; x++) {
            int rgb = image.getRGB(x, y);
            int r = (rgb >>> 16) & 255, g = (rgb >>> 8) & 255, b = rgb & 255;
            if (g > 110 && g > r * 1.4 && g > b * 1.2) count++;
        }
        return count;
    }
}
