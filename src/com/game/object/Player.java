package com.game.object;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import com.game.gfx.Animation;
import com.game.gfx.PlayerState;
import com.game.main.Game;
import com.game.object.util.Handler;
import com.game.object.util.ObjectId;

/**
 * Entidad controlable principal (Mario) con soporte para transformaciones,
 * estados de movimiento, fisicas de salto, disparo de proyectiles y estado de muerte.
 */
public class Player extends GameObject {

    private final Handler handler;
    private final BufferedImage[] small;
    private final BufferedImage[] large;
    private final BufferedImage[] fireMario;
    private final Animation walkSmall;
    private final Animation walkLarge;
    private final Animation walkFire;
    private final LinkedList<Block> removeBlocks = new LinkedList<>();

    private PlayerState state = PlayerState.Small;
    private boolean jumped;
    private boolean forward = true;
    private boolean crouching;
    private boolean crouchRequested;
    private int growthTicks;
    private boolean firePowered;
    private volatile boolean fireRequested;
    private int fireCooldown;
    private int fireTurnTicks;
    private int hurtInvincibilityTicks;
    private int deathTicks;
    private int lives = 3;
    private int coinCount;
    private boolean pipeDownRequested;
    private boolean gameOver;

    private final float spawnX;
    private final float spawnY;
    private float previousY;

    public Player(float x, float y, int scale, Handler handler) {
        super(x, y, ObjectId.Player, 16, 16, scale);
        this.handler = handler;
        this.spawnX = getX();
        this.spawnY = getY();
        this.previousY = getY();

        this.small = Game.getTexture().getMarioS();
        this.large = Game.getTexture().getMarioL();
        this.fireMario = Game.getTexture().getMarioFire();

        this.walkSmall = new Animation(6, small[1], small[2], small[3]);
        this.walkLarge = new Animation(6, large[1], large[2], large[3]);
        this.walkFire = new Animation(6, fireMario[9], fireMario[10], fireMario[11], fireMario[12]);
    }

    @Override
    public void tick() {
        if (gameOver) return;

        // Proceso del estado de muerte
        if (state == PlayerState.Dead) {
            tickDeathSequence();
            return;
        }

        // Verificacion de caida en vacio (pozo)
        if (getY() > Game.getWindowHeight() + 32) {
            triggerDeath();
            return;
        }

        previousY = getY();
        setX(getX() + getVelX());
        setY(getY() + getVelY());
        applyGravity();

        processCollisions();

        if (state == PlayerState.Small) {
            walkSmall.runAnimation();
        } else if (firePowered) {
            walkFire.runAnimation();
        } else {
            walkLarge.runAnimation();
        }

        if (hurtInvincibilityTicks > 0) hurtInvincibilityTicks--;
        if (fireTurnTicks > 0) fireTurnTicks--;

        if (getVelX() != 0) {
            boolean movingRight = getVelX() > 0;
            if (movingRight != forward) {
                forward = movingRight;
                fireTurnTicks = firePowered ? 6 : 0;
            }
        }

        if (fireCooldown > 0) fireCooldown--;
        if (fireRequested) {
            fireRequested = false;
            launchFireball();
        }

        if (growthTicks > 0) growthTicks--;
        if (crouchRequested && !crouching) tryCrouch();
        if (crouching && !crouchRequested) tryStand();
    }

    private void tickDeathSequence() {
        deathTicks++;
        if (deathTicks < 20) {
            setVelX(0);
            setVelY(0);
        } else if (deathTicks == 20) {
            setVelX(0);
            setVelY(-10.5f);
        } else {
            setY(getY() + getVelY());
            setVelY(Math.min(12f, getVelY() + 0.45f));
        }

        if (deathTicks >= 140 || getY() > Game.getWindowHeight() + 120) {
            lives--;
            if (lives <= 0) {
                gameOver = true;
                setVelX(0);
                setVelY(0);
            } else {
                respawn();
            }
        }
    }

    private void respawn() {
        state = PlayerState.Small;
        firePowered = false;
        crouching = false;
        crouchRequested = false;
        setHeight(16);
        setWidth(16);
        setX(spawnX);
        setY(spawnY);
        setVelX(0);
        setVelY(0);
        jumped = false;
        forward = true;
        hurtInvincibilityTicks = 90;
        deathTicks = 0;
    }

    public void triggerDeath() {
        if (state == PlayerState.Dead || gameOver) return;
        state = PlayerState.Dead;
        deathTicks = 0;
        firePowered = false;
        crouching = false;
        crouchRequested = false;
        setVelX(0);
        setVelY(0);
    }

    private void processCollisions() {
        List<PowerUp> collectedPowerUps = new LinkedList<>();
        List<Coin> spawnedCoins = new LinkedList<>();
        List<PowerUp> spawnedPowerUps = new LinkedList<>();

        for (GameObject obj : handler.getGameObjs()) {
            if (obj instanceof Enemy enemy) {
                Rectangle body = new Rectangle(
                        (int)getX() + 2, (int)getY(),
                        Math.max(1, (int)getWidth() - 4), (int)getHeight()
                );
                if (!body.intersects(enemy.getBounds())) continue;

                boolean stomp = getVelY() > 0 && previousY + getHeight() <= enemy.getY() + 5
                        && getY() + getHeight() >= enemy.getY();
                Enemy.Contact contact = enemy.playerContact(this, stomp);
                if (contact == Enemy.Contact.STOMPED) {
                    setY(enemy.getY() - getHeight());
                    setVelY(-10f);
                    jumped = true;
                }
                continue;
            }

            if (obj instanceof PowerUp powerUp) {
                if (powerUp.getBounds().intersects(getBounds()) && powerUp.collect(this)) {
                    collectedPowerUps.add(powerUp);
                    Game.playSound("powerup_collect.wav");
                }
                continue;
            }

            if (obj == this || (obj.getId() != ObjectId.Block && obj.getId() != ObjectId.Pipe)) {
                continue;
            }

            Rectangle r = obj.getBounds();
            if (getBounds().intersects(r) && getVelY() >= 0) {
                setY(r.y - getHeight());
                setVelY(0);
                jumped = false;
            } else if (getBoundsTop().intersects(r) && getVelY() < 0) {
                setY(r.y + r.height);
                setVelY(0);
                if (obj instanceof Block b) {
                    boolean wasBroken = b.isBroken();
                    b.hit(state != PlayerState.Small);
                    if (!wasBroken && b.isBroken()) Game.playSound("block_crumble.wav");
                    if (b.isBroken() && !removeBlocks.contains(b)) removeBlocks.add(b);

                    PowerUp.Type type = b.releasePowerUp();
                    if (type != null) {
                        float itemX = b.getX() / b.getScale();
                        float itemY = (b.getY() - b.getHeight()) / b.getScale();
                        spawnedPowerUps.add(new PowerUp(itemX, itemY, b.getScale(), handler, type));
                        Game.playSound("powerup_spawn.wav");
                    } else if (b.releaseCoin()) {
                        float itemX = b.getX() / b.getScale();
                        float itemY = (b.getY() - b.getHeight()) / b.getScale();
                        spawnedCoins.add(new Coin(itemX, itemY, b.getScale()));
                        coinCount++;
                        if (coinCount % 100 == 0) lives++;
                        Game.playSound("coin_collect.wav");
                    }
                }
            }

            if (getBoundsRight().intersects(r) && getVelX() > 0) setX(r.x - getWidth());
            if (getBoundsLeft().intersects(r) && getVelX() < 0) setX(r.x + r.width);
        }

        for (PowerUp powerUp : collectedPowerUps) handler.removeObj(powerUp);
        for (PowerUp powerUp : spawnedPowerUps) handler.addObj(powerUp);
        for (Coin coin : spawnedCoins) handler.addObj(coin);
    }

    public LinkedList<Block> getAndResetRemoveBlocks() {
        LinkedList<Block> removed = new LinkedList<>();
        for (Block b : removeBlocks) {
            if (b.shouldRemove()) removed.add(b);
        }
        removeBlocks.removeAll(removed);
        return removed;
    }

    @Override
    public void render(Graphics g) {
        if (state == PlayerState.Dead) {
            BufferedImage deathSprite = small.length > 6 && small[6] != null ? small[6] : small[0];
            g.drawImage(deathSprite, (int)getX(), (int)getY(), (int)getWidth(), (int)getHeight(), null);
            return;
        }

        if (hurtInvincibilityTicks > 0 && (hurtInvincibilityTicks / 5) % 2 == 0) return;

        boolean showSmallGrowthFrame = state == PlayerState.Large && !crouching
                && growthTicks > 0 && (growthTicks / 3) % 2 == 0;
        BufferedImage sprite;
        int drawY = (int)getY();
        int drawHeight = (int)getHeight();

        if (showSmallGrowthFrame) {
            sprite = small[0];
            drawHeight = 16 * getScale();
            drawY += (int)getHeight() - drawHeight;
        } else if (crouching) {
            BufferedImage[] frames = currentLargeSprites();
            sprite = firePowered ? frames[15] : (frames.length > 6 && frames[6] != null ? frames[6] : frames[0]);
            drawY -= 16 * getScale();
            if (firePowered) drawY += 5 * getScale();
            drawHeight = 32 * getScale();
        } else if (jumped) {
            BufferedImage[] frames = state == PlayerState.Small ? small : currentLargeSprites();
            sprite = firePowered ? frames[14] : (frames.length > 5 && frames[5] != null ? frames[5] : frames[0]);
        } else if (firePowered && fireTurnTicks > 0) {
            sprite = fireMario[13];
        } else {
            sprite = state == PlayerState.Small ? small[0] : currentLargeSprites()[firePowered ? 8 : 0];
        }

        Animation animation = state == PlayerState.Small ? walkSmall : (firePowered ? walkFire : walkLarge);
        int drawX = forward ? (int)getX() : (int)(getX() + getWidth());
        int drawW = forward ? (int)getWidth() : -(int)getWidth();

        if (getVelX() != 0 && !jumped && !showSmallGrowthFrame && !crouching && !(firePowered && fireTurnTicks > 0)) {
            animation.drawAnimation(g, drawX, (int)getY(), drawW, (int)getHeight());
        } else {
            g.drawImage(sprite, drawX, drawY, drawW, drawHeight, null);
        }

        if (getVelX() != 0) forward = getVelX() > 0;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)(getX() + getWidth() / 4), (int)(getY() + getHeight() / 2), (int)(getWidth() / 2), (int)(getHeight() / 2));
    }

    public Rectangle getBoundsTop() {
        return new Rectangle((int)(getX() + getWidth() / 4), (int)getY(), (int)(getWidth() / 2), (int)(getHeight() / 2));
    }

    public Rectangle getBoundsRight() {
        return new Rectangle((int)(getX() + getWidth() - 5), (int)(getY() + 5), 5, Math.max(1, (int)getHeight() - 10));
    }

    public Rectangle getBoundsLeft() {
        return new Rectangle((int)getX(), (int)(getY() + 5), 5, Math.max(1, (int)getHeight() - 10));
    }

    public boolean hasJumped() { return jumped; }
    public void setJumped(boolean jumped) { this.jumped = jumped; }
    public void setFacing(boolean forward) { this.forward = forward; }
    public boolean isFacingRight() { return forward; }
    public boolean isCrouching() { return crouching; }

    public void setCrouching(boolean requested) {
        crouchRequested = requested;
        if (state != PlayerState.Large) return;
        if (requested) {
            tryCrouch();
        } else {
            tryStand();
        }
    }

    private void tryCrouch() {
        if (state != PlayerState.Large || crouching || jumped || Math.abs(getVelY()) >= 0.01f) return;
        setY(getY() + 16 * getScale());
        setHeight(16);
        crouching = true;
        setVelX(0);
    }

    private void tryStand() {
        if (!crouching) return;
        float standHeight = 32 * getScale();
        float standY = getY() - 16 * getScale();
        Rectangle standingBounds = new Rectangle(
                (int)(getX() + getWidth() / 4), (int)standY,
                (int)(getWidth() / 2), (int)standHeight);
        for (GameObject object : handler.getGameObjs()) {
            if (object == this || (object.getId() != ObjectId.Block && object.getId() != ObjectId.Pipe)) continue;
            if (standingBounds.intersects(object.getBounds())) return;
        }
        setY(standY);
        setHeight(32);
        crouching = false;
    }

    public boolean grow() {
        if (state != PlayerState.Small) return false;
        setY(getY() - 16 * getScale());
        setHeight(32);
        state = PlayerState.Large;
        growthTicks = 24;
        if (crouchRequested) setCrouching(true);
        return true;
    }

    public boolean isSmall() { return state == PlayerState.Small; }
    public boolean isDead() { return state == PlayerState.Dead; }

    private BufferedImage[] currentLargeSprites() {
        return firePowered ? fireMario : large;
    }

    public boolean applyPowerUp(PowerUp.Type type) {
        if (state == PlayerState.Dead || gameOver) return false;
        if (type == PowerUp.Type.ONE_UP) {
            lives++;
            return true;
        }
        if (state == PlayerState.Small) return grow();
        if (type == PowerUp.Type.FIRE_FLOWER) {
            firePowered = true;
            return true;
        }
        return false;
    }

    public boolean hasFirePower() { return firePowered; }
    public boolean isGameOver() { return gameOver; }

    public void takeEnemyHit() {
        takeEnemyHit(false);
    }

    public void takeEnemyHit(boolean enemyIsLeft) {
        if (gameOver || state == PlayerState.Dead || hurtInvincibilityTicks > 0) return;

        if (firePowered) {
            firePowered = false;
            hurtInvincibilityTicks = 90;
            setVelX(enemyIsLeft ? 5f : -5f);
            setVelY(-7f);
            jumped = true;
        } else if (state == PlayerState.Large) {
            boolean wasCrouching = crouching;
            if (!wasCrouching) setY(getY() + 16 * getScale());
            setHeight(16);
            state = PlayerState.Small;
            crouching = false;
            crouchRequested = false;
            hurtInvincibilityTicks = 90;
            setVelX(enemyIsLeft ? 5f : -5f);
            setVelY(-7f);
            jumped = true;
        } else {
            triggerDeath();
        }
    }

    public int getLives() { return lives; }
    public int getCoinCount() { return coinCount; }
    public void setPipeDownRequested(boolean requested) { pipeDownRequested = requested; }
    public boolean isPipeDownRequested() { return pipeDownRequested; }

    public void requestFireball() {
        if (firePowered && state != PlayerState.Dead && !gameOver) fireRequested = true;
    }

    private void launchFireball() {
        if (!firePowered || fireCooldown > 0 || state == PlayerState.Dead || gameOver) return;
        int active = 0;
        for (GameObject object : handler.getGameObjs()) {
            if (object instanceof Fireball) active++;
        }
        if (active >= 2) return;
        float startX = getX() + (forward ? getWidth() : -8 * getScale());
        float startY = getY() + getHeight() * 0.55f;
        handler.addObj(new Fireball(startX / getScale(), startY / getScale(), getScale(), forward, handler));
        fireCooldown = 18;
        Game.playSound("fireball_launch.wav");
    }
}
