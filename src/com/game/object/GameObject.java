package com.game.object;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.game.object.util.ObjectId;

/**
 * Clase base abstracta para todos los elementos interactivos y estaticos del juego.
 */
public abstract class GameObject {

    private float x;
    private float y;
    private ObjectId id;
    private float velX;
    private float velY;
    private float width;
    private float height;
    private final int scale;

    public GameObject(float x, float y, ObjectId id, float width, float height, int scale) {
        this.scale = scale;
        this.x = x * scale;
        this.y = y * scale;
        this.id = id;
        this.width = width * scale;
        this.height = height * scale;
    }

    public abstract void tick();

    public abstract void render(Graphics g);

    public abstract Rectangle getBounds();

    public void applyGravity() {
        velY += 0.5f;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public float getVelX() {
        return velX;
    }

    public void setVelX(float velX) {
        this.velX = velX;
    }

    public float getVelY() {
        return velY;
    }

    public void setVelY(float velY) {
        this.velY = velY;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width * scale;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height * scale;
    }

    public int getScale() {
        return scale;
    }
}
