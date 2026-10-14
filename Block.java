package com.game.object;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.game.main.Game;
import com.game.object.util.ObjectId;

public class Block extends GameObject {
    private static final int[] BUMP_OFFSETS = {0, -5, -12, -18, -16, -12, -8, -4, -1, 2, 1, 0};

    private int index;
    private boolean broken;
    private boolean activated;
    private boolean showQuestionDuringBump;
    private PowerUp.Type containedPowerUp;
    private boolean containsCoin;
    private boolean hidden;
    private int glowTicks;
    private int glowFrame;
    private int bumpTicks;
    private Debris debris;
    private BufferedImage[] sprite;

    public Block(int x, int y, int width, int height, int index, int scale) {
        this(x, y, width, height, index, scale, false);
    }

    public Block(int x, int y, int width, int height, int index, int scale, boolean containsMushroom) {
        this(x, y, width, height, index, scale,
                containsMushroom ? PowerUp.Type.MUSHROOM : null);
    }

    public Block(int x, int y, int width, int height, int index, int scale, PowerUp.Type containedPowerUp) {
        this(x, y, width, height, index, scale, containedPowerUp, false);
    }

    public Block(int x, int y, int width, int height, int index, int scale,
                 PowerUp.Type containedPowerUp, boolean hidden) {
        super(x, y, ObjectId.Block, width, height, scale);

        this.index = index;
        this.containedPowerUp = containedPowerUp;
        this.containsCoin = index == 2 && containedPowerUp == null;
        this.hidden = hidden;
        sprite = Game.getTexture().getTile1();
    }

    @Override
    public void tick() {
        if (broken && debris != null) debris.tick();
        else {
            if (bumpTicks > 0) {
                bumpTicks--;
                if (bumpTicks == 0) showQuestionDuringBump = false;
            }
            if (index == 2 && !activated && ++glowTicks >= 10) {
                glowTicks = 0;
                glowFrame = (glowFrame + 1) % 3;
            }
        }
    }

    @Override
    public void render(Graphics g) {
        if (broken) { debris.draw(g); return; }
        if (hidden && !activated) return;
        int bumpOffset = 0;
        if (bumpTicks > 0) {
            int elapsed = 12 - bumpTicks;
            bumpOffset = BUMP_OFFSETS[Math.min(elapsed, BUMP_OFFSETS.length - 1)];
        }
        int drawY = (int)getY() + bumpOffset;
        if (index == 2 && activated && !showQuestionDuringBump) {
            g.drawImage(Game.getTexture().getEmptyQuestionBlock(), (int)getX(), drawY, (int)getWidth(), (int)getHeight(), null);
            return;
        }
        if (index == 2) {
            g.drawImage(Game.getTexture().getQuestionFrame(glowFrame), (int)getX(), drawY, (int)getWidth(), (int)getHeight(), null);
            return;
        }
        g.drawImage(
                sprite[index],
                (int) getX(),
                drawY,
                (int) getWidth(),
                (int) getHeight(),
                null
        );
    }

    public void hit(boolean canBreakBricks) {
        if (index == 1 && canBreakBricks && !broken) {
            broken = true;
            debris = new Debris(getX(), getY(), getWidth(), getHeight(), 1);
        } else if (index == 2) {
            if (!activated) {
                startBump();
                activated = true;
                showQuestionDuringBump = true;
            }
        } else if (index == 0 || index == 1) {
            startBump();
        }
    }

    private void startBump() { bumpTicks = BUMP_OFFSETS.length; }

    public PowerUp.Type releasePowerUp() {
        boolean ready = index == 2 ? activated : index == 1 && broken;
        if (!ready || containedPowerUp == null) return null;
        PowerUp.Type released = containedPowerUp;
        containedPowerUp = null;
        return released;
    }

    public boolean releaseCoin() {
        if (index != 2 || !activated || !containsCoin) return false;
        containsCoin = false;
        return true;
    }

    public boolean isBroken() { return broken; }
    public boolean shouldRemove() { return broken && debris != null && debris.shouldRemove(); }

    @Override
    public Rectangle getBounds() {
        if (broken) return new Rectangle();
        return new Rectangle((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
    }
}
