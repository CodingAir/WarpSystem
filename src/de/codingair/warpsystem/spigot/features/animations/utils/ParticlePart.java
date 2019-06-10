package de.codingair.warpsystem.spigot.features.animations.utils;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.PlayerAnimation;
import de.codingair.codingapi.particles.animations.customanimations.CustomAnimation;
import de.codingair.codingapi.particles.utils.Color;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.Serializable;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.lang.reflect.InvocationTargetException;

public class ParticlePart implements Serializable {
    private AnimationType animation;
    private Particle particle;
    private Color color = Color.RED;
    private double radius, height;
    private int xRotation, yRotation, zRotation;
    private int speed;

    public ParticlePart() {
    }

    public ParticlePart(AnimationType animation, Particle particle, double radius, double height, int speed) {
        this.animation = animation;
        this.particle = particle;
        this.radius = radius;
        this.height = height;
        this.speed = speed;
    }

    @Override
    public boolean read(JSONObject json) {
        animation = AnimationType.getById(Integer.parseInt(json.get("animation") + ""));
        particle = Particle.getById(Integer.parseInt(json.get("particle") + ""));
        radius = Double.parseDouble(json.get("radius") + "");
        height = Double.parseDouble(json.get("height") + "");
        speed = Integer.parseInt(json.get("speed") + "");
        color = json.get("color") == null ? null : Color.valueOf((String) json.get("color"));
        xRotation = json.get("xrot") == null ? 0 : Integer.parseInt(json.get("xrot") + "");
        yRotation = json.get("yrot") == null ? 0 : Integer.parseInt(json.get("yrot") + "");
        zRotation = json.get("zrot") == null ? 0 : Integer.parseInt(json.get("zrot") + "");
        return true;
    }

    @Override
    public void write(JSONObject json) {
        json.put("animation", animation.getId());
        json.put("particle", particle.getId());
        json.put("radius", radius);
        json.put("speed", speed);
        json.put("color", color == null ? null : color.name());
        json.put("xrot", xRotation);
        json.put("yrot", yRotation);
        json.put("zrot", zRotation);
    }

    @Override
    public void destroy() {
    }

    public CustomAnimation build(Player player) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return animation == null ? null : animation.build(particle, player, radius, height, speed).setXRotation(xRotation).setYRotation(yRotation).setZRotation(zRotation).setColor(color);
    }

    public AnimationType getAnimation() {
        return animation;
    }

    public void setAnimation(AnimationType animation) {
        this.animation = animation;
    }

    public Particle getParticle() {
        return particle;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = round(radius);
        if(this.radius < 0.1) this.radius = 0.1;
        if(this.radius > 3) this.radius = 3;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = round(height);
        if(this.height < 0) this.height = 0;
        if(this.height > 3) this.height = 3;
    }

    private double round(double d) {
        return ((double) Math.round(d * 10)) / 10;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
        if(this.speed < PlayerAnimation.MIN_SPEED) this.speed = PlayerAnimation.MIN_SPEED;
        if(this.speed > PlayerAnimation.MAX_SPEED) this.speed = PlayerAnimation.MAX_SPEED;
    }

    public int getxRotation() {
        return xRotation;
    }

    public void setxRotation(int xRotation) {
        this.xRotation = xRotation;
    }

    public int getyRotation() {
        return yRotation;
    }

    public void setyRotation(int yRotation) {
        this.yRotation = yRotation;
    }

    public int getzRotation() {
        return zRotation;
    }

    public void setzRotation(int zRotation) {
        this.zRotation = zRotation;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
