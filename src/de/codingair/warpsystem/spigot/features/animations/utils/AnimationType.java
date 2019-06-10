package de.codingair.warpsystem.spigot.features.animations.utils;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.customanimations.*;
import de.codingair.codingapi.particles.animations.customanimations.CircleAnimation;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;

public enum AnimationType {
    CIRCLE(0, CircleAnimation.class, "Circle"),
    PULSING_CIRCLE_ANIMATION(1, PulsingCircleAnimation.class, "Pulsing Circle"),
    ROTATING_CIRCLE_ANIMATION(2, RotatingCircleAnimation.class, "Rotating Circle"),
    SINUS_ANIMATION(3, SinusAnimation.class, "Sinus"),
    VECTOR_ANIMATION(4, VectorAnimation.class, "Vector"),
    LINE_ANIMATION(5, LineAnimation.class, "Line"),
    ;

    private int id;
    private Class<? extends CustomAnimation> clazz;
    private String displayName;

    AnimationType(int id, Class<? extends CustomAnimation> clazz, String displayName) {
        this.id = id;
        this.clazz = clazz;
        this.displayName = displayName;
    }

    public CustomAnimation build(Particle particle, Player player, double radius, double height, int speed) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return clazz.getConstructor(Particle.class, Player.class, Plugin.class, boolean.class, double.class, double.class, int.class)
                .newInstance(particle, player, WarpSystem.getInstance(), true, radius, height, speed);
    }

    public int getId() {
        return id;
    }

    public Class<? extends CustomAnimation> getClazz() {
        return clazz;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AnimationType next() {
        return next(id);
    }

    public AnimationType previous() {
        return previous(id);
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static AnimationType getById(int id) {
        for(AnimationType value : values()) {
            if(value.getId() == id) return value;
        }

        throw new IllegalArgumentException("Couldn't found AnimationType with id=" + id);
    }

    public static AnimationType next(int id) {
        for(int i = 0; i < values().length; i++) {
            if(values()[i].getId() == id) return i + 1 == values().length ? values()[0] : values()[i + 1];
        }

        throw new IllegalArgumentException("Couldn't found AnimationType with id=" + id);
    }

    public static AnimationType previous(int id) {
        for(int i = 0; i < values().length; i++) {
            if(values()[i].getId() == id) {
                return i - 1 < 0 ? values()[values().length - 1] : values()[i - 1];
            }
        }

        throw new IllegalArgumentException("Couldn't found AnimationType with id=" + id);
    }
}
