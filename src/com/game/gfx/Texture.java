package com.game.gfx;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;

public class Texture {
    private static final String ROOT = "/images/sprites/";
    private final BufferedImage[] marioL = new BufferedImage[15];
    private final BufferedImage[] marioFire = new BufferedImage[16];
    private final BufferedImage[] marioS = new BufferedImage[14];
    private final BufferedImage[] tile1 = new BufferedImage[61];
    private final BufferedImage[] questionFrames = new BufferedImage[3];
    private BufferedImage mushroom;
    private BufferedImage fireFlower;
    private final BufferedImage[] fireFlowerFrames = new BufferedImage[4];
    private final BufferedImage[] fireballFrames = new BufferedImage[2];
    private final BufferedImage[] goombaFrames = new BufferedImage[2];
    private BufferedImage goombaSquashed;
    private final BufferedImage[] koopaFrames = new BufferedImage[2];
    private BufferedImage koopaShell;
    private final BufferedImage[] redKoopaFrames = new BufferedImage[2];
    private BufferedImage redKoopaShell;
    private final BufferedImage[] paratroopaFrames = new BufferedImage[2];
    private final BufferedImage[] piranhaFrames = new BufferedImage[2];
    private final BufferedImage[] blooperFrames = new BufferedImage[2];
    private final BufferedImage[] hammerBroFrames = new BufferedImage[2];
    private final BufferedImage[] buzzyBeetleFrames = new BufferedImage[2];
    private final BufferedImage[] spinyFrames = new BufferedImage[2];
    private final BufferedImage[] cheepCheepFrames = new BufferedImage[2];
    private final BufferedImage[] bowserFrames = new BufferedImage[4];
    private final BufferedImage[] bowserFireFrames = new BufferedImage[4];
    private BufferedImage oneUpMushroom;
    private BufferedImage star;
    private BufferedImage coin;
    private final BufferedImage[] coinFrames = new BufferedImage[4];
    private BufferedImage emptyQuestionBlock;
    private final BufferedImage[] pipe1 = new BufferedImage[4];
    private final BufferedImage[] scenery = new BufferedImage[6];
    private BufferedImage flagpole;
    private BufferedImage castle;
    private final BufferedImage[] debris1 = new BufferedImage[4];

    public Texture() {
        createFallbackTextures();
        BufferedImageLoader loader = new BufferedImageLoader();
        try {
            BufferedImage player = loader.loadImage(ROOT + "hero_small_frames.png");
            extractMiniMarioFrames(player);
            System.out.println("Sprites provisionales de Mini Mario reemplazados por el PNG encontrado.");
        } catch (Exception e) {
            System.err.println("Sprites de Mini Mario no disponibles; se mantienen los provisionales.");
        }
        try {
            BufferedImage player = loader.loadImage(ROOT + "hero_large_frames.png");
            extractFrames(player, marioL, 32);
            System.out.println("Sprites de Mario grande cargados.");
        } catch (Exception e) {
            System.err.println("Sprites de Mario grande no disponibles; se mantienen los provisionales.");
        }
        try {
            BufferedImage firePlayer = loader.loadImage(ROOT + "hero_fire_frames.png");
            extractFireMarioFrames(firePlayer, marioFire, 0);
            System.out.println("Sprites de Fire Mario cargados.");
        } catch (Exception e) {
            System.err.println("Sprites de Fire Mario no disponibles; se usan los de Mario grande.");
        }
        try {
            BufferedImage tiles = loader.loadImage(ROOT + "terrain_atlas.png");
            extractBlocks(tiles);
            // Standalone replacements added for the overworld: preserve their exact 16x16 pixels.
            tile1[0] = loader.loadImage(ROOT + "ground_tile.png");
            tile1[3] = loader.loadImage(ROOT + "solid_tile.png");
            System.out.println("Texturas de bloques cargadas desde terrain_atlas.png.");
        } catch (Exception e) {
            System.err.println("terrain_atlas.png no encontrado; se mantienen los gráficos provisionales.");
        }
        try {
            BufferedImage items = loader.loadImage(ROOT + "collectible_atlas.png");
            // The atlas aligns these item regions to 16px cells. Keep the transparent canvas
            // so narrow coin frames retain their original aspect ratio during 3x rendering.
            mushroom = extractCanvas(items, 0, 0, 16, 16);
            oneUpMushroom = extractCanvas(items, 16, 0, 16, 16);
            // Four adjacent 16px fire-flower color frames in the verified item row.
            for (int i = 0; i < fireFlowerFrames.length; i++)
                fireFlowerFrames[i] = extractCanvas(items, i * 16, 32, 16, 16);
            fireFlower = fireFlowerFrames[0];
            star = extractCanvas(items, 0, 48, 16, 16);
            for (int i = 0; i < coinFrames.length; i++) coinFrames[i] = extractCanvas(items, i * 16, 96, 16, 16);
            coin = coinFrames[0];
            System.out.println("Sprites de mushroom, estrella y moneda cargados desde collectible_atlas.png.");
        } catch (Exception e) {
            System.err.println("collectible_atlas.png no disponible; se mantienen los placeholders.");
        }
        try {
            BufferedImage enemies = loader.loadImage(ROOT + "creature_atlas.png");
            extractEnemySprites(enemies);
            System.out.println("Sprites de enemigos cargados desde creature_atlas.png.");
        } catch (Exception e) {
            System.err.println("creature_atlas.png no disponible; se mantienen los sprites provisionales.");
        }
        try {
            BufferedImage objects = loader.loadImage(ROOT + "world_objects.gif");
            scenery[0] = objects.getSubimage(86, 5, 80, 35);   // large hill
            scenery[1] = objects.getSubimage(169, 21, 48, 19); // small hill
            scenery[2] = objects.getSubimage(220, 24, 64, 16); // bush
            scenery[3] = objects.getSubimage(288, 24, 32, 16); // small bush
            scenery[4] = objects.getSubimage(144, 69, 64, 24); // cloud
            scenery[5] = objects.getSubimage(211, 69, 32, 24); // small cloud
            flagpole = objects.getSubimage(117, 594, 23, 168);
            castle = objects.getSubimage(247, 863, 80, 80);
            pipe1[0] = objects.getSubimage(230, 385, 32, 16); // green pipe cap
            pipe1[1] = objects.getSubimage(230, 401, 32, 16); // matching body segment
            pipe1[2] = pipe1[0];
            pipe1[3] = pipe1[1];
            System.out.println("Sprites de tapa y cuerpo de tubería cargados.");
        } catch (Exception e) {
            System.err.println("Sprites de tubería no disponibles; se mantienen los placeholders.");
        }
    }

    private BufferedImage extractCanvas(BufferedImage sheet, int x, int y, int width, int height) {
        if (x < 0 || y < 0 || x + width > sheet.getWidth() || y + height > sheet.getHeight()) {
            throw new IllegalArgumentException("Región de sprite fuera de collectible_atlas.png");
        }
        return sheet.getSubimage(x, y, width, height);
    }

    /** Extrae los fotogramas de la hoja de sprites de enemigos con las coordenadas exactas de la cuadricula. */
    private void extractEnemySprites(BufferedImage sheet) {
        // Los Goombas caminando ocupan las filas 4 a 19, incluidos los pies.
        goombaFrames[0] = extractCanvas(sheet, 0, 4, 16, 16);
        goombaFrames[1] = extractCanvas(sheet, 30, 4, 16, 16);
        goombaSquashed = extractCanvas(sheet, 60, 0, 16, 16);

        // Tortugas mirando a la derecha, sin mezclar las filas de colores del atlas.
        for (int i = 0; i < koopaFrames.length; i++) {
            koopaFrames[i] = extractTurtleFrame(sheet, 210 + i * 30, 0);
            redKoopaFrames[i] = extractTurtleFrame(sheet, 210 + i * 30, 30);
            paratroopaFrames[i] = extractTurtleFrame(sheet, 270 + i * 30, 0);
        }
        koopaShell = extractCanvas(sheet, 360, 4, 16, 16);
        redKoopaShell = extractCanvas(sheet, 360, 34, 16, 16);

        piranhaFrames[0] = extractCanvas(sheet, 390, 32, 16, 24);
        piranhaFrames[1] = extractCanvas(sheet, 420, 32, 16, 24);

        blooperFrames[0] = extractCanvas(sheet, 390, 0, 16, 24);
        blooperFrames[1] = extractCanvas(sheet, 420, 0, 16, 24);

        hammerBroFrames[0] = extractCanvas(sheet, 0, 92, 16, 32);
        hammerBroFrames[1] = extractCanvas(sheet, 30, 92, 16, 32);

        buzzyBeetleFrames[0] = extractCanvas(sheet, 0, 189, 16, 16);
        buzzyBeetleFrames[1] = extractCanvas(sheet, 30, 189, 16, 16);

        spinyFrames[0] = extractCanvas(sheet, 90, 189, 16, 16);
        spinyFrames[1] = extractCanvas(sheet, 120, 189, 16, 16);

        cheepCheepFrames[0] = extractCanvas(sheet, 300, 156, 16, 16);
        cheepCheepFrames[1] = extractCanvas(sheet, 330, 156, 16, 16);

        bowserFrames[0] = extractCanvas(sheet, 2, 216, 32, 32);
        bowserFrames[1] = extractCanvas(sheet, 42, 216, 32, 32);
        bowserFrames[2] = extractCanvas(sheet, 82, 216, 32, 32);
        bowserFrames[3] = extractCanvas(sheet, 122, 216, 32, 32);

        bowserFireFrames[0] = extractCanvas(sheet, 96, 256, 32, 5);
        bowserFireFrames[1] = extractCanvas(sheet, 128, 256, 32, 5);
        bowserFireFrames[2] = extractCanvas(sheet, 160, 256, 32, 5);
        bowserFireFrames[3] = extractCanvas(sheet, 192, 256, 32, 5);
    }

    private BufferedImage extractTurtleFrame(BufferedImage sheet, int x, int y) {
        // Conserva la escala original y apoya los pies en la base del lienzo de 32 píxeles.
        BufferedImage canvas = new BufferedImage(16, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.drawImage(extractCanvas(sheet, x, y, 16, 24), 0, 8, null);
        g.dispose();
        return canvas;
    }

    private void createFallbackEnemySprites() {
        BufferedImage fallback = makeMario(16, new Color(155, 70, 35));
        goombaFrames[0] = fallback;
        goombaFrames[1] = fallback;
        goombaSquashed = fallback;
        koopaFrames[0] = makeMario(32, new Color(40, 155, 55));
        koopaFrames[1] = koopaFrames[0];
        koopaShell = fallback;
        redKoopaFrames[0] = makeMario(32, new Color(210, 55, 35));
        redKoopaFrames[1] = redKoopaFrames[0];
        redKoopaShell = fallback;
        paratroopaFrames[0] = koopaFrames[0];
        paratroopaFrames[1] = koopaFrames[0];
        piranhaFrames[0] = makeMario(32, new Color(40, 170, 45));
        piranhaFrames[1] = piranhaFrames[0];
        blooperFrames[0] = makeMario(32, Color.WHITE);
        blooperFrames[1] = blooperFrames[0];
        hammerBroFrames[0] = koopaFrames[0];
        hammerBroFrames[1] = koopaFrames[0];
        buzzyBeetleFrames[0] = fallback;
        buzzyBeetleFrames[1] = fallback;
        spinyFrames[0] = fallback;
        spinyFrames[1] = fallback;
        cheepCheepFrames[0] = fallback;
        cheepCheepFrames[1] = fallback;
        for (int i = 0; i < bowserFrames.length; i++) bowserFrames[i] = makeMario(32, new Color(45, 160, 50));
        for (int i = 0; i < bowserFireFrames.length; i++) bowserFireFrames[i] = makeFireball(i % 2 != 0);
    }

    private void extractMiniMarioFrames(BufferedImage sheet) {
        extractFrames(sheet, marioS, 16);
    }

    private void extractFireMarioFrames(BufferedImage sheet, BufferedImage[] destination, int top) {
        if (sheet.getWidth() < 405 || sheet.getHeight() < 32)
            throw new IllegalArgumentException("La hoja de Fire Mario no contiene la fila de animación esperada");
        int frame = 0, x = 0;
        while (x < sheet.getWidth() && frame < destination.length) {
            while (x < sheet.getWidth() && !hasOpaquePixelInColumn(sheet, x, top, top + 32)) x++;
            if (x >= sheet.getWidth()) break;
            int left = x;
            while (x < sheet.getWidth() && hasOpaquePixelInColumn(sheet, x, top, top + 32)) x++;
            int width = x - left;
            if (width > 16) throw new IllegalArgumentException("Fotograma de Fire Mario más ancho de lo esperado");
            BufferedImage canvas = new BufferedImage(16, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = canvas.createGraphics();
            g.drawImage(sheet.getSubimage(left, top, width, 32), (16 - width) / 2, 0, null);
            g.dispose();
            destination[frame++] = canvas;
        }
        if (frame != destination.length) throw new IllegalArgumentException("No se encontraron todos los cuadros de Fire Mario");
    }

    private void extractFrames(BufferedImage sheet, BufferedImage[] destination, int height) {
        int frame = 0, x = 0;
        while (x < sheet.getWidth() && frame < destination.length) {
            while (x < sheet.getWidth() && !hasOpaquePixelInColumn(sheet, x)) x++;
            if (x >= sheet.getWidth()) break;
            int left = x;
            while (x < sheet.getWidth() && hasOpaquePixelInColumn(sheet, x)) x++;
            int width = Math.min(16, x - left);
            BufferedImage cell = new BufferedImage(16, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = cell.createGraphics();
            g.drawImage(sheet.getSubimage(left, 0, width, Math.min(height, sheet.getHeight())), (16 - width) / 2, 0, null);
            g.dispose();
            destination[frame++] = cell;
        }
        if (frame == 0) throw new IllegalArgumentException("No se encontraron sprites en la tira de Mini Mario");
    }

    private void extractBlocks(BufferedImage sheet) {
        if (sheet.getWidth() < 28 * 16 || sheet.getHeight() < 16 * 16) {
            throw new IllegalArgumentException("terrain_atlas.png debe contener una cuadrícula de sprites de 16x16");
        }
        // Bloque sólido, ladrillo e interrogación: coordenadas de la hoja en celdas.
        tile1[0] = sheet.getSubimage(16 * 16, 7 * 16, 16, 16); // ground block del overworld
        tile1[1] = sheet.getSubimage(17 * 16, 7 * 16, 16, 16); // ladrillo rompible
        tile1[2] = sheet.getSubimage(7 * 16, 7 * 16, 16, 16);  // interrogación lleno
        emptyQuestionBlock = sheet.getSubimage(9 * 16, 7 * 16, 16, 16); // vacío café del overworld
        for (int i = 0; i < questionFrames.length; i++) {
            questionFrames[i] = sheet.getSubimage((11 + i) * 16, 7 * 16, 16, 16);
        }
        for (int i = 3; i < tile1.length; i++) {
            int cell = i % (28 * 16);
            tile1[i] = sheet.getSubimage((cell % 28) * 16, (cell / 28) * 16, 16, 16);
        }
    }

    private boolean hasOpaquePixelInColumn(BufferedImage image, int x) {
        for (int y = 0; y < image.getHeight(); y++) if ((image.getRGB(x, y) >>> 24) != 0) return true;
        return false;
    }

    private boolean hasOpaquePixelInColumn(BufferedImage image, int x, int top, int bottom) {
        for (int y = top; y < bottom; y++) if ((image.getRGB(x, y) >>> 24) != 0) return true;
        return false;
    }

    private void createFallbackTextures() {
        Color[] palette = {new Color(180, 70, 30), new Color(220, 145, 55), new Color(80, 180, 65), new Color(130, 80, 35)};
        for (int i = 0; i < tile1.length; i++) {
            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setColor(palette[i % palette.length]); g.fillRect(0, 0, 16, 16);
            g.setColor(palette[(i + 1) % palette.length]); g.drawRect(1, 1, 13, 13);
            g.dispose(); tile1[i] = image;
        }
        emptyQuestionBlock = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D emptyGraphics = emptyQuestionBlock.createGraphics();
        emptyGraphics.setColor(new Color(145, 78, 28)); emptyGraphics.fillRect(0, 0, 16, 16);
        emptyGraphics.setColor(new Color(95, 45, 18)); emptyGraphics.drawRect(1, 1, 13, 13); emptyGraphics.dispose();
        for (int i = 0; i < questionFrames.length; i++) questionFrames[i] = tile1[2];
        for (int i = 0; i < marioS.length; i++) marioS[i] = makeMario(16, new Color(210, 35, 35));
        for (int i = 0; i < marioL.length; i++) marioL[i] = makeMario(32, new Color(210, 35, 35));
        for (int i = 0; i < marioFire.length; i++) marioFire[i] = marioL[i % marioL.length];
        mushroom = makeMario(16, new Color(220, 35, 35));
        fireFlower = makeMario(16, new Color(245, 150, 20));
        for (int i = 0; i < fireFlowerFrames.length; i++) fireFlowerFrames[i] = fireFlower;
        fireballFrames[0] = makeFireball(false);
        fireballFrames[1] = makeFireball(true);
        createFallbackEnemySprites();
        oneUpMushroom = makeMario(16, new Color(45, 190, 65));
        star = tile1[2];
        coin = tile1[1];
        for (int i = 0; i < coinFrames.length; i++) coinFrames[i] = coin;
        for (int i = 0; i < pipe1.length; i++) {
            BufferedImage image = new BufferedImage(32, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics(); g.setColor(new Color(30, 175, 55));
            g.fillRect(i < 2 ? 0 : 4, 0, i < 2 ? 32 : 24, 16);
            g.setColor(new Color(15, 105, 35)); g.drawRect(1, 1, 29, 13); g.dispose(); pipe1[i] = image;
        }
        for (int i = 0; i < debris1.length; i++) {
            BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics(); g.setColor(new Color(180, 100, 45)); g.fillRect(0, 0, 8, 8); g.dispose(); debris1[i] = image;
        }
    }

    private BufferedImage makeMario(int height, Color color) {
        BufferedImage image = new BufferedImage(16, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color); g.fillRect(3, 0, 10, height / 3);
        g.setColor(new Color(245, 185, 125)); g.fillRect(4, height / 3, 9, height / 3);
        g.setColor(new Color(35, 75, 190)); g.fillRect(2, 2 * height / 3, 12, height / 3);
        g.setColor(Color.BLACK); g.fillRect(10, height / 3 + 2, 2, 2); g.dispose();
        return image;
    }

    private BufferedImage makeFireball(boolean alternate) {
        BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        String[] pixels = alternate
                ? new String[]{"...YY...", "..YOOY..", ".YORRRY.", "YORRRRY.", "YORRRRY.", ".YORRY..", "..YY....", "........"}
                : new String[]{"........", "...YY...", "..YOOY..", ".YORRRY.", ".YORRRY.", "..YORY..", "...YY...", "........"};
        Color yellow = new Color(255, 235, 55), orange = new Color(255, 135, 25), red = new Color(225, 45, 20);
        for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) {
            int pixel = pixels[y].charAt(x);
            if (pixel == 'Y') image.setRGB(x, y, yellow.getRGB());
            else if (pixel == 'O') image.setRGB(x, y, orange.getRGB());
            else if (pixel == 'R') image.setRGB(x, y, red.getRGB());
        }
        return image;
    }

    public BufferedImage[] getMarioL() { return marioL; }
    public BufferedImage[] getMarioFire() { return marioFire; }
    public BufferedImage getMushroom() { return mushroom; }
    public BufferedImage getFireFlower() { return fireFlower; }
    public BufferedImage getFireballFrame(int frame) { return fireballFrames[Math.floorMod(frame, fireballFrames.length)]; }
    public Animation createFireFlowerAnimation() {
        return new Animation(8, fireFlowerFrames);
    }
    public BufferedImage getOneUpMushroom() { return oneUpMushroom; }
    public BufferedImage getStar() { return star; }
    public BufferedImage getCoin() { return coin; }
    public Animation createCoinAnimation() {
        return new Animation(8, coinFrames[0], coinFrames[1], coinFrames[2], coinFrames[3], coinFrames[2], coinFrames[1]);
    }
    public BufferedImage[] getMarioS() { return marioS; }
    public BufferedImage[] getTile1() { return tile1; }
    public BufferedImage getEmptyQuestionBlock() { return emptyQuestionBlock; }
    public BufferedImage getQuestionFrame(int frame) { return questionFrames[Math.floorMod(frame, questionFrames.length)]; }
    public BufferedImage[] getPipe1() { return pipe1; }
    public BufferedImage[] getScenery() { return scenery; }
    public BufferedImage getFlagpole() { return flagpole; }
    public BufferedImage getCastle() { return castle; }
    public BufferedImage[] getDebris1() { return debris1; }
    public BufferedImage[] getGoombaFrames() { return goombaFrames; }
    public BufferedImage getGoombaSquashed() { return goombaSquashed; }
    public BufferedImage[] getKoopaFrames() { return koopaFrames; }
    public BufferedImage getKoopaShell() { return koopaShell; }
    public BufferedImage[] getRedKoopaFrames() { return redKoopaFrames; }
    public BufferedImage getRedKoopaShell() { return redKoopaShell; }
    public BufferedImage[] getParatroopaFrames() { return paratroopaFrames; }
    public BufferedImage[] getPiranhaFrames() { return piranhaFrames; }
    public BufferedImage[] getBlooperFrames() { return blooperFrames; }
    public BufferedImage[] getHammerBroFrames() { return hammerBroFrames; }
    public BufferedImage[] getBuzzyBeetleFrames() { return buzzyBeetleFrames; }
    public BufferedImage[] getSpinyFrames() { return spinyFrames; }
    public BufferedImage[] getCheepCheepFrames() { return cheepCheepFrames; }
    public BufferedImage[] getBowserFrames() { return bowserFrames; }
    public BufferedImage[] getBowserFireFrames() { return bowserFireFrames; }
}
