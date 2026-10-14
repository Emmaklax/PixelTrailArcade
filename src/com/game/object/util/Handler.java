package com.game.object.util;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.game.object.Block;
import com.game.object.Coin;
import com.game.object.Enemy;
import com.game.object.EnemyProjectile;
import com.game.object.Fireball;
import com.game.object.Flagpole;
import com.game.object.GameObject;
import com.game.object.Player;
import com.game.object.PowerUp;

/**
 * Gestor central de entidades activas en el escenario: actualiza fisicas, logica y renderizado.
 */
public class Handler {

    private final List<GameObject> gameObjs;
    private Player player;
    private volatile Flagpole endingFlag;

    public Handler() {
        this.gameObjs = new ArrayList<>();
    }

    public void tick() {
        if (endingFlag != null) {
            endingFlag.tick();
            return;
        }
        for (int i = 0; i < gameObjs.size(); i++) {
            GameObject object = gameObjs.get(i);
            object.tick();
            if (endingFlag != null) return;

            boolean shouldRemove = false;
            if (object instanceof PowerUp powerUp) {
                shouldRemove = powerUp.shouldRemove();
            } else if (object instanceof Fireball fireball) {
                shouldRemove = fireball.shouldRemove();
            } else if (object instanceof Coin coin) {
                shouldRemove = coin.shouldRemove();
            } else if (object instanceof Enemy enemy) {
                shouldRemove = enemy.shouldRemove();
            } else if (object instanceof EnemyProjectile projectile) {
                shouldRemove = projectile.shouldRemove();
            }

            if (shouldRemove) {
                gameObjs.remove(i--);
            }
        }

        if (player != null) {
            for (Block block : player.getAndResetRemoveBlocks()) {
                removeObj(block);
            }
        }
    }

    public void render(Graphics g) {
        for (int i = 0; i < gameObjs.size(); i++) {
            gameObjs.get(i).render(g);
        }
    }

    public void addObj(GameObject obj) {
        if (obj != null && !gameObjs.contains(obj)) {
            gameObjs.add(obj);
        }
    }

    public void removeObj(GameObject obj) {
        gameObjs.remove(obj);
    }

    public void clearLevelObjects() {
        gameObjs.clear();
        player = null;
        endingFlag = null;
    }

    public void beginLevelEnd(Flagpole flag) {
        if (endingFlag != null || player == null || player.isDead() || player.isGameOver()) return;
        endingFlag = flag;
        player.setCrouching(false);
        player.setPipeDownRequested(false);
        player.setJumped(false);
        player.setVelX(0);
        player.setVelY(0);
    }

    public boolean isLevelEnding() { return endingFlag != null; }
    public boolean isDemoComplete() { return endingFlag != null && endingFlag.isComplete(); }

    public List<GameObject> getGameObjs() {
        return gameObjs;
    }

    public int setPlayer(Player player) {
        if (this.player != null) {
            return -1;
        }
        addObj(player);
        this.player = player;
        return 0;
    }

    public int removePlayer() {
        if (player == null) {
            return -1;
        }
        removeObj(player);
        this.player = null;
        return 0;
    }

    public Player getPlayer() {
        return player;
    }
}
