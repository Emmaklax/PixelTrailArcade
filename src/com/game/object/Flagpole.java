package com.game.object;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.game.object.util.Handler;
import com.game.object.util.ObjectId;

public final class Flagpole extends GameObject {
    private final Handler handler;
    private final BufferedImage flag;
    private final float floorY;
    private float flagY;
    private boolean activated;
    private boolean complete;
    private int finishTicks;

    public Flagpole(float x, float y, float floorY, int scale, Handler handler, BufferedImage flag) {
        super(x, y, ObjectId.Decoration, 2, floorY - y, scale);
        this.handler = handler;
        this.flag = flag;
        this.floorY = floorY * scale;
        flagY = (y + 8) * scale;
    }

    @Override
    public void tick() {
        Player player = handler.getPlayer();
        if (player == null || player.isDead() || player.isGameOver() || complete) return;
        if (!activated) {
            Rectangle body = new Rectangle((int)player.getX(), (int)player.getY(),
                    (int)player.getWidth(), (int)player.getHeight());
            if (!body.intersects(getBounds())) return;
            activated = true;
            handler.beginLevelEnd(this);
            player.setX(getX() - player.getWidth());
            player.setFacing(true);
        }

        float flagBottom = floorY - 32 * getScale();
        flagY = Math.min(flagBottom, flagY + 2 * getScale());
        player.setY(Math.min(floorY - player.getHeight(), player.getY() + 2 * getScale()));
        if (flagY == flagBottom && player.getY() + player.getHeight() == floorY) {
            complete = ++finishTicks >= 30;
        }
    }

    public boolean isComplete() { return complete; }

    @Override
    public void render(Graphics g) {
        g.drawImage(flag, (int)getX() - flag.getWidth() * getScale(), (int)flagY,
                flag.getWidth() * getScale(), flag.getHeight() * getScale(), null);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
    }
}
