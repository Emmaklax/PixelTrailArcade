package com.game.object.util;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import com.game.main.Game;
import com.game.object.Player;

/**
 * Gestor de eventos de teclado para el control de Mario (movimiento, salto, agacharse y disparo).
 */
public class KeyInput extends KeyAdapter {

    private final boolean[] keysPressed = new boolean[2]; // [0] = Izquierda, [1] = Derecha
    private final Handler handler;

    public KeyInput(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE) {
            System.exit(0);
            return;
        }

        Player player = handler.getPlayer();
        if (player == null || player.isGameOver() || player.isDead() || handler.isLevelEnding()) {
            return;
        }

        switch (code) {
            case KeyEvent.VK_W, KeyEvent.VK_UP, KeyEvent.VK_SPACE -> {
                player.setCrouching(false);
                if (!player.isCrouching() && !player.hasJumped()) {
                    player.setVelY(-15f);
                    player.setJumped(true);
                    Game.playSound(player.isSmall() ? "jump_small.wav" : "jump_large.wav");
                }
            }
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> {
                keysPressed[0] = true;
                updateHorizontalMovement(player);
            }
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> {
                keysPressed[1] = true;
                updateHorizontalMovement(player);
            }
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> {
                player.setPipeDownRequested(true);
                player.setCrouching(true);
                updateHorizontalMovement(player);
            }
            case KeyEvent.VK_F, KeyEvent.VK_X, KeyEvent.VK_J -> {
                player.requestFireball();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Player player = handler.getPlayer();
        if (player == null || handler.isLevelEnding()) return;

        int code = e.getKeyCode();
        switch (code) {
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> keysPressed[0] = false;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> keysPressed[1] = false;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> {
                player.setPipeDownRequested(false);
                player.setCrouching(false);
            }
        }
        updateHorizontalMovement(player);
    }

    private void updateHorizontalMovement(Player player) {
        if (player == null || player.isDead()) return;

        if (player.isCrouching()) {
            player.setVelX(0);
            if (keysPressed[0] && !keysPressed[1]) player.setFacing(false);
            else if (keysPressed[1] && !keysPressed[0]) player.setFacing(true);
            return;
        }

        float speed = 8f;
        if (keysPressed[0] && !keysPressed[1]) {
            player.setVelX(-speed);
        } else if (keysPressed[1] && !keysPressed[0]) {
            player.setVelX(speed);
        } else {
            player.setVelX(0);
        }
    }
}
