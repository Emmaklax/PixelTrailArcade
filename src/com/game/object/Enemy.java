package com.game.object;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.game.gfx.Animation;
import com.game.main.Game;
import com.game.object.util.Handler;
import com.game.object.util.ObjectId;

/**
 * Representa a los diferentes tipos de enemigos del juego con inteligencia artificial,
 * fisicas, estados de daño, aplastamiento, proyeccion en caparazon y animaciones por atlas.
 */
public final class Enemy extends GameObject {

    public enum Kind {
        GOOMBA,
        GREEN_KOOPA,
        RED_KOOPA,
        PARATROOPA,
        PIRANHA_PLANT,
        BLOOPER,
        HAMMER_BRO,
        BUZZY_BEETLE,
        SPINY,
        CHEEP_CHEEP,
        BOWSER
    }

    public enum Contact {
        NONE,
        STOMPED,
        KICKED,
        HURT
    }

    private enum State {
        WALKING,
        SQUASHED,
        SHELL_IDLE,
        SHELL_MOVING,
        HIDDEN,
        RISING,
        EXPOSED,
        FALLING,
        DYING,
        DEAD
    }

    private final Handler handler;
    private Kind kind;
    private State state = State.WALKING;
    private Animation walkAnimation;
    private int animationTicks;
    private int stateTicks;
    private int health = 5;
    private int attackTicks;
    private final float pipeTopY;
    private final float fullPlantY;
    private final float aquaticBaseY;
    private boolean facingRight;
    private boolean removed;

    public Enemy(float x, float y, int scale, Handler handler, Kind kind) {
        super(x, y, ObjectId.Enemy, 16, nativeHeight(kind), scale);
        this.handler = handler;
        this.kind = kind;
        this.pipeTopY = y * scale;
        this.fullPlantY = pipeTopY - getHeight();
        this.aquaticBaseY = getY();
        setVelX(-calculateEnemySpeed(kind, scale));

        if (kind == Kind.PIRANHA_PLANT) {
            state = State.HIDDEN;
            stateTicks = 70;
            setVelX(0);
        }
        if (kind == Kind.BLOOPER || kind == Kind.CHEEP_CHEEP) {
            setVelX(-calculateEnemySpeed(kind, scale));
        }
        setupAnimation();
    }

    private static int nativeHeight(Kind kind) {
        return switch (kind) {
            case GREEN_KOOPA, RED_KOOPA, PARATROOPA, BLOOPER, HAMMER_BRO, BOWSER -> 32;
            case PIRANHA_PLANT -> 24;
            default -> 16;
        };
    }

    private static float calculateEnemySpeed(Kind kind, int scale) {
        float baseSpeed = switch (kind) {
            case BOWSER -> 0.45f;
            case BLOOPER, CHEEP_CHEEP, PARATROOPA -> 0.8f;
            case HAMMER_BRO -> 0f;
            default -> 0.65f;
        };
        return baseSpeed * scale;
    }

    private void setupAnimation() {
        BufferedImage[] frames = resolveSpriteFrames();
        if (frames != null && frames.length > 1) {
            walkAnimation = new Animation(kind == Kind.BOWSER ? 10 : 7, frames);
        }
    }

    private BufferedImage[] resolveSpriteFrames() {
        var texture = Game.getTexture();
        return switch (kind) {
            case GOOMBA -> texture.getGoombaFrames();
            case GREEN_KOOPA -> texture.getKoopaFrames();
            case RED_KOOPA -> texture.getRedKoopaFrames();
            case PARATROOPA -> texture.getParatroopaFrames();
            case PIRANHA_PLANT -> texture.getPiranhaFrames();
            case BLOOPER -> texture.getBlooperFrames();
            case HAMMER_BRO -> texture.getHammerBroFrames();
            case BUZZY_BEETLE -> texture.getBuzzyBeetleFrames();
            case SPINY -> texture.getSpinyFrames();
            case CHEEP_CHEEP -> texture.getCheepCheepFrames();
            case BOWSER -> texture.getBowserFrames();
        };
    }

    @Override
    public void tick() {
        if (state == State.DEAD || removed) return;

        if (state == State.DYING) {
            setY(getY() + getVelY());
            setVelY(getVelY() + 0.5f);
            if (--stateTicks <= 0 || getY() > Game.getWindowHeight() + getHeight()) {
                state = State.DEAD;
                removed = true;
            }
            return;
        }

        if (state == State.SQUASHED) {
            if (--stateTicks <= 0) {
                state = State.DEAD;
                removed = true;
            }
            return;
        }

        if (state == State.SHELL_IDLE) {
            if (stateTicks > 0 && --stateTicks == 0) {
                wakeFromShell();
            }
            return;
        }

        if (kind == Kind.PIRANHA_PLANT) {
            tickPiranha();
            return;
        }

        if (kind == Kind.BLOOPER || kind == Kind.CHEEP_CHEEP) {
            tickAquatic();
            return;
        }

        if (kind == Kind.HAMMER_BRO) tickHammerBro();
        if (kind == Kind.BOWSER) tickBowser();
        if (kind == Kind.PARATROOPA) tickParatroopa();

        animationTicks++;
        if (walkAnimation != null && animationTicks % 7 == 0) {
            walkAnimation.runAnimation();
        }

        if (state == State.SHELL_MOVING) {
            tickShell();
        } else {
            tickWalker();
        }
    }

    private void tickWalker() {
        float oldX = getX();
        setX(getX() + getVelX());
        Rectangle horizontal = new Rectangle((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
        for (GameObject object : handler.getGameObjs()) {
            if (!isSolid(object) || !horizontal.intersects(object.getBounds())) continue;
            if (getVelX() > 0) setX(object.getBounds().x - getWidth());
            else setX(object.getBounds().x + object.getBounds().width);
            setVelX(-getVelX());
            facingRight = getVelX() > 0;
            break;
        }
        if (oldX == getX()) {
            setVelX(-getVelX());
            facingRight = getVelX() > 0;
        }

        setVelY(Math.min(12f, getVelY() + 0.5f));
        float oldY = getY();
        setY(getY() + getVelY());
        for (GameObject object : handler.getGameObjs()) {
            Rectangle body = new Rectangle((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
            if (!isSolid(object) || !body.intersects(object.getBounds())) continue;
            Rectangle obstacle = object.getBounds();
            if (getVelY() >= 0 && oldY + getHeight() <= obstacle.y + getVelY() + 2) {
                setY(obstacle.y - getHeight());
                setVelY(0);
            } else if (getVelY() < 0) {
                setY(obstacle.y + obstacle.height);
                setVelY(0);
            }
        }

        // Koopas rojos no se caen por bordes
        if (kind == Kind.RED_KOOPA && getVelY() == 0 && !hasFloorAhead()) {
            setVelX(-getVelX());
            facingRight = getVelX() > 0;
        }

        if (getY() > Game.getWindowHeight() + getHeight()) removed = true;
        if (getX() < 0) {
            setX(0);
            setVelX(Math.abs(getVelX()));
            facingRight = true;
        }
    }

    private boolean hasFloorAhead() {
        int probeX = (int)(getVelX() > 0 ? getX() + getWidth() + 2 : getX() - 2);
        Rectangle probe = new Rectangle(probeX, (int)(getY() + getHeight() + 1), 2, 3);
        for (GameObject object : handler.getGameObjs()) {
            if (isSolid(object) && probe.intersects(object.getBounds())) return true;
        }
        return false;
    }

    private void tickShell() {
        setX(getX() + getVelX());
        for (GameObject object : handler.getGameObjs()) {
            if (!isSolid(object) || !getBounds().intersects(object.getBounds())) continue;
            Rectangle wall = object.getBounds();
            if (getVelX() > 0) setX(wall.x - getWidth());
            else setX(wall.x + wall.width);
            setVelX(-getVelX());
            facingRight = getVelX() > 0;
            break;
        }

        for (GameObject object : handler.getGameObjs()) {
            if (object instanceof Enemy other && other != this && other.isActive()
                    && getBounds().intersects(other.getBounds())) {
                other.squashByShell();
            }
        }

        setVelY(Math.min(12f, getVelY() + 0.5f));
        float oldY = getY();
        setY(getY() + getVelY());
        for (GameObject object : handler.getGameObjs()) {
            if (!isSolid(object) || !getBounds().intersects(object.getBounds())) continue;
            if (getVelY() >= 0 && oldY + getHeight() <= object.getBounds().y + getVelY() + 2) {
                setY(object.getBounds().y - getHeight());
                setVelY(0);
            }
        }
    }

    private void tickParatroopa() {
        if (getVelY() == 0 && (++animationTicks % 42) == 0) {
            setVelY(-5.5f * getScale());
        }
    }

    private void tickPiranha() {
        Player player = handler.getPlayer();
        boolean playerNear = player != null && Math.abs(player.getX() + player.getWidth() / 2f
                - (getX() + getWidth() / 2f)) < 64 * getScale();
        float riseSpeed = 1.5f * getScale();

        switch (state) {
            case HIDDEN -> {
                setY(pipeTopY);
                if (!playerNear && --stateTicks <= 0) {
                    state = State.RISING;
                    stateTicks = 0;
                }
            }
            case RISING -> {
                setY(Math.max(fullPlantY, getY() - riseSpeed));
                if (getY() <= fullPlantY) {
                    state = State.EXPOSED;
                    stateTicks = 55;
                }
                if (playerNear) state = State.FALLING;
            }
            case EXPOSED -> {
                if (--stateTicks <= 0 || playerNear) state = State.FALLING;
            }
            case FALLING -> {
                setY(Math.min(pipeTopY, getY() + riseSpeed));
                if (getY() >= pipeTopY) {
                    state = State.HIDDEN;
                    stateTicks = 70;
                }
            }
            default -> { }
        }

        animationTicks++;
        if (walkAnimation != null && animationTicks % 8 == 0) {
            walkAnimation.runAnimation();
        }
    }

    private void tickAquatic() {
        animationTicks++;
        if (walkAnimation != null && animationTicks % 8 == 0) {
            walkAnimation.runAnimation();
        }
        setX(getX() + getVelX());
        Player player = handler.getPlayer();
        if (getX() < 0 || (player != null && getX() > player.getX() + Game.getWindowWidth() * 2f)) {
            setVelX(-getVelX());
        }
        float targetY = aquaticBaseY + (float)Math.sin(animationTicks / 24.0) * 25 * getScale();
        if (kind == Kind.BLOOPER && player != null) {
            targetY += Math.signum(player.getY() - getY()) * 12 * getScale();
        }
        setY(getY() + Math.max(-2.2f * getScale(), Math.min(2.2f * getScale(), targetY - getY())));
    }

    private void tickHammerBro() {
        if (++attackTicks >= 95) {
            attackTicks = 0;
            Player player = handler.getPlayer();
            if (player != null) {
                handler.addObj(new EnemyProjectile(
                        getX() / getScale(),
                        (getY() + 8 * getScale()) / getScale(),
                        getScale(),
                        player.getX() < getX(),
                        EnemyProjectile.Kind.HAMMER,
                        handler
                ));
            }
        }
    }

    private void tickBowser() {
        if (++attackTicks >= 110) {
            attackTicks = 0;
            Player player = handler.getPlayer();
            if (player != null) {
                handler.addObj(new EnemyProjectile(
                        getX() / getScale(),
                        (getY() + 17 * getScale()) / getScale(),
                        getScale(),
                        player.getX() < getX(),
                        EnemyProjectile.Kind.BOWSER_FIRE,
                        handler
                ));
            }
        }
    }

    private boolean isSolid(GameObject object) {
        return object != this && (object.getId() == ObjectId.Block || object.getId() == ObjectId.Pipe);
    }

    public Contact playerContact(Player player, boolean stomp) {
        if (!isActive() || player.isDead()) return Contact.NONE;

        if (state == State.SHELL_MOVING && stomp) {
            state = State.SHELL_IDLE;
            stateTicks = 420;
            setVelX(0);
            setVelY(0);
            return Contact.STOMPED;
        }

        if (state == State.SHELL_IDLE) {
            if (kind == Kind.GREEN_KOOPA || kind == Kind.RED_KOOPA) {
                return stomp ? Contact.STOMPED : Contact.NONE;
            }
            kick(player.getX() < getX());
            return Contact.KICKED;
        }

        if (stomp && tryStomp()) {
            return Contact.STOMPED;
        }

        player.takeEnemyHit(getX() + getWidth() / 2f < player.getX() + player.getWidth() / 2f);
        return Contact.HURT;
    }

    private boolean tryStomp() {
        if (kind == Kind.GOOMBA) {
            state = State.SQUASHED;
            stateTicks = 20;
            setVelX(0);
            setVelY(0);
            return true;
        }
        if (kind == Kind.PARATROOPA) {
            kind = Kind.GREEN_KOOPA;
            state = State.WALKING;
            setVelY(0);
            setHeight(32);
            setupAnimation();
            return true;
        }
        if (kind == Kind.GREEN_KOOPA || kind == Kind.RED_KOOPA || kind == Kind.BUZZY_BEETLE) {
            state = State.SHELL_IDLE;
            stateTicks = 420;
            setY(getY() + 16 * getScale());
            setHeight(16);
            setVelX(0);
            setVelY(0);
            return true;
        }
        if (kind == Kind.CHEEP_CHEEP || kind == Kind.BLOOPER) {
            die(18);
            return true;
        }
        return false;
    }

    private void kick(boolean toLeft) {
        state = State.SHELL_MOVING;
        stateTicks = 0;
        setVelX((toLeft ? -1 : 1) * 8.5f * getScale());
        setVelY(-2 * getScale());
    }

    public boolean hitByFireball() {
        if (!isActive() || kind == Kind.BUZZY_BEETLE) return false;
        if (kind == Kind.BOWSER && --health > 0) {
            attackTicks = -14;
            return true;
        }
        die(20);
        return true;
    }

    public void squashByShell() {
        if (kind == Kind.BOWSER) return;
        die(20);
    }

    public void die(int ticks) {
        state = State.DYING;
        stateTicks = ticks;
        setVelX(0);
        setVelY(-4.5f);
    }

    private void wakeFromShell() {
        state = State.WALKING;
        if (kind != Kind.RED_KOOPA) kind = Kind.GREEN_KOOPA;
        setHeight(32);
        setY(getY() - 16 * getScale());
        setVelX((facingRight ? 1 : -1) * calculateEnemySpeed(kind, getScale()));
        setupAnimation();
    }

    public boolean isActive() {
        return state != State.DEAD && state != State.SQUASHED && state != State.DYING
                && state != State.HIDDEN;
    }

    public boolean shouldRemove() {
        return removed || state == State.DEAD;
    }

    @Override
    public void render(Graphics g) {
        if (state == State.HIDDEN || state == State.DEAD) return;

        if (kind == Kind.PIRANHA_PLANT) {
            int visibleHeight = Math.min((int)getHeight(), Math.max(0, (int)(pipeTopY - getY())));
            if (visibleHeight > 0) {
                BufferedImage[] frames = resolveSpriteFrames();
                BufferedImage plantSprite = frames[(animationTicks / 8) % frames.length];
                int srcH = (int)(visibleHeight / (float)getScale());
                srcH = Math.min(plantSprite.getHeight(), Math.max(1, srcH));
                BufferedImage sub = plantSprite.getSubimage(0, 0, plantSprite.getWidth(), srcH);
                g.drawImage(sub, (int)getX(), (int)getY(), (int)getWidth(), visibleHeight, null);
            }
            return;
        }

        BufferedImage sprite;
        if (state == State.SQUASHED) {
            sprite = Game.getTexture().getGoombaSquashed();
        } else if (state == State.SHELL_IDLE || state == State.SHELL_MOVING) {
            sprite = (kind == Kind.BUZZY_BEETLE)
                    ? Game.getTexture().getBuzzyBeetleFrames()[0]
                    : kind == Kind.RED_KOOPA ? Game.getTexture().getRedKoopaShell()
                    : Game.getTexture().getKoopaShell();
        } else if (walkAnimation != null && (kind == Kind.GOOMBA || kind == Kind.GREEN_KOOPA
                || kind == Kind.RED_KOOPA || kind == Kind.PARATROOPA
                || kind == Kind.BLOOPER || kind == Kind.HAMMER_BRO || kind == Kind.BOWSER)) {
            int x = facingRight ? (int)getX() : (int)(getX() + getWidth());
            int width = facingRight ? (int)getWidth() : -(int)getWidth();
            walkAnimation.drawAnimation(g, x, (int)getY(), width, (int)getHeight());
            return;
        } else {
            BufferedImage[] frames = resolveSpriteFrames();
            sprite = frames[(animationTicks / 8) % frames.length];
        }

        int x = facingRight ? (int)getX() : (int)(getX() + getWidth());
        int width = facingRight ? (int)getWidth() : (int)-getWidth();
        g.drawImage(sprite, x, (int)getY(), width, (int)getHeight(), null);
    }

    @Override
    public Rectangle getBounds() {
        if (state == State.HIDDEN || state == State.DEAD || state == State.SQUASHED || state == State.DYING) {
            return new Rectangle();
        }
        return new Rectangle(
                (int)getX() + (int)(getWidth() * 0.15f),
                (int)getY() + (int)(getHeight() * 0.1f),
                Math.max(1, (int)(getWidth() * 0.7f)),
                Math.max(1, (int)(getHeight() * 0.8f))
        );
    }
}
