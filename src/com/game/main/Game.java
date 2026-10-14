package com.game.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;

import com.game.gfx.Windows;
import com.game.audio.MusicPlayer;
import com.game.object.util.Handler;
import com.game.main.util.LevelHandler;

public class Game extends Canvas implements Runnable {

    // GAME CONSTANTS

    private static final int MILLIS_PER_SEC = 1000;
    private static final int NANOS_PER_SEC = 1000000000;
    private static final double NUM_TICKS = 60.0;
    private static final int WINDOW_WIDTH = 960;
    private static final int WINDOW_HEIGHT = 720;

    // GAME VARIABLES

    private volatile boolean running;

    // GAME COMPONENTS

    private Thread thread;
    private Handler handler;
    private static com.game.gfx.Texture texture;
    private LevelHandler levelHandler;
    private static MusicPlayer musicPlayer;

    public Game() {

        initialize();

    }

    public static void main(String[] args) {

        new Game();

    }

    private void initialize() {

        handler = new Handler();
        texture = new com.game.gfx.Texture();
        addKeyListener(new com.game.object.util.KeyInput(handler));
        new Windows(this);
        levelHandler = new LevelHandler(handler);
        levelHandler.start();
        musicPlayer = new MusicPlayer();
        musicPlayer.playLoop("/audio/music/overworld_loop.wav");

        start();

    }

    private synchronized void start() {

        if (running) {

            return;

        }

        running = true;

        thread = new Thread(this, "Game-Thread");

        thread.start();

    }

    private synchronized void stop() {

        if (!running) {
            if (musicPlayer != null) musicPlayer.close();
            return;

        }

        running = false;
        if (musicPlayer != null) musicPlayer.close();

        if (thread != null && Thread.currentThread() != thread) {

            try {

                thread.join();

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                e.printStackTrace();

            }

        }

    }

    @Override
    public void run() {

        long lastTime = System.nanoTime();

        double amountOfTicks = NUM_TICKS;

        double ns = NANOS_PER_SEC / amountOfTicks;

        double delta = 0;

        long timer = System.currentTimeMillis();

        int frames = 0;

        int updates = 0;

        while (running) {

            long now = System.nanoTime();

            delta += (now - lastTime) / ns;

            lastTime = now;

            while (delta >= 1) {

                tick();

                updates++;

                delta--;

            }

            if (running) {

                render();

                frames++;

            }

            if (System.currentTimeMillis() - timer > MILLIS_PER_SEC) {

                timer += MILLIS_PER_SEC;

                System.out.println(
                    "FPS: " + frames + "  TPS: " + updates
                );

                updates = 0;

                frames = 0;

            }

        }

        stop();

    }

    private void tick() {

        handler.tick();
        if (levelHandler != null) levelHandler.tick();

    }

    private void render() {

        BufferStrategy buf = getBufferStrategy();

        if (buf == null) {

            createBufferStrategy(3);

            return;

        }

        Graphics g = buf.getDrawGraphics();
        if (g instanceof Graphics2D g2) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        }

        try {

            // DRAW GRAPHICS

            g.setColor(new Color(92, 148, 252));
            g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            int cameraX = 0;
            if (handler.getPlayer() != null) {
                int worldRight = WINDOW_WIDTH;
                for (var object : handler.getGameObjs())
                    worldRight = Math.max(worldRight, (int)(object.getX() + object.getWidth()));
                cameraX = (int)(handler.getPlayer().getX() - WINDOW_WIDTH / 3.0);
                cameraX = Math.max(0, Math.min(cameraX, worldRight - WINDOW_WIDTH));
                g.translate(-cameraX, 0);
            }
            handler.render(g);
            if (handler.getPlayer() != null) {
                g.translate(cameraX, 0);
                g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
                String status = "LIVES " + handler.getPlayer().getLives()
                        + "     COINS " + handler.getPlayer().getCoinCount();
                g.setColor(Color.BLACK);
                g.drawString(status, 13, 25);
                g.setColor(Color.WHITE);
                g.drawString(status, 12, 24);
                if (handler.isDemoComplete()) {
                    g.setColor(new Color(0, 0, 0, 190));
                    g.fillRect(0, WINDOW_HEIGHT / 2 - 65, WINDOW_WIDTH, 130);
                    g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 42));
                    String message = "TERMINASTE LA DEMO";
                    int messageX = (WINDOW_WIDTH - g.getFontMetrics().stringWidth(message)) / 2;
                    g.setColor(Color.WHITE);
                    g.drawString(message, messageX, WINDOW_HEIGHT / 2 + 14);
                } else if (handler.getPlayer().isGameOver()) {
                    g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 42));
                    g.setColor(Color.BLACK);
                    g.drawString("GAME OVER", WINDOW_WIDTH / 2 - 145, WINDOW_HEIGHT / 2 + 14);
                    g.setColor(Color.WHITE);
                    g.drawString("GAME OVER", WINDOW_WIDTH / 2 - 148, WINDOW_HEIGHT / 2 + 11);
                }
            }

        } finally {

            // CLEAN FOR NEXT FRAME

            g.dispose();

        }

        buf.show();

    }

    public static int getWindowHeight() {

        return WINDOW_HEIGHT;

    }

    public void shutdown() {
        running = false;
        if (musicPlayer != null) musicPlayer.close();
    }

    public static com.game.gfx.Texture getTexture() {
        if (texture == null) {
            texture = new com.game.gfx.Texture();
        }
        return texture;
    }

    public static void playSound(String fileName) {
        // Effects share the game's audio lifecycle and never replace the music clip.
        // The player is static so gameplay objects can trigger sounds without owning audio resources.
        if (musicPlayer != null) musicPlayer.playEffect("/audio/effects/" + fileName);
    }

    public static int getWindowWidth() {

        return WINDOW_WIDTH;

    }

}
