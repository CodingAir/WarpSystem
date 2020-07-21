package de.codingair.warpsystem.spigot.features.animations.utils;

import de.codingair.codingapi.particles.animations.customanimations.CustomAnimation;
import de.codingair.codingapi.particles.animations.movables.LocationMid;
import de.codingair.codingapi.particles.animations.movables.MovableMid;
import de.codingair.codingapi.particles.animations.movables.PlayerMid;
import de.codingair.codingapi.player.MessageAPI;
import de.codingair.codingapi.tools.HitBox;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class AnimationPlayer {
    private final Player player;
    private final Animation animation;
    private final MovableMid animMid;

    private boolean loop = false;
    private final int seconds;
    private boolean running = false;
    private BukkitRunnable runnable;
    private final List<CustomAnimation> animations = new ArrayList<>();
    private final List<PotionEffect> buffBackup = new ArrayList<>();
    private double maxDistance = 70;
    private final boolean sounds;
    private boolean teleportSound;
    private final Player[] viewers;

    private HitBox hitBox = null;

    public AnimationPlayer(Player player, Animation animation, int seconds) {
        this(player, new PlayerMid(player), animation, seconds);
    }

    public AnimationPlayer(Location location, Animation animation) {
        this(location, animation, -1);
    }

    public AnimationPlayer(Location location, Animation animation, int seconds) {
        this(null, new LocationMid(location), animation, seconds);
    }

    public AnimationPlayer(Player player, MovableMid location, Animation animation, int seconds) {
        this(player, location, animation, seconds, true, false);
    }

    public AnimationPlayer(Player player, MovableMid location, Animation animation, int seconds, boolean sounds, boolean viewers) {
        this.player = player;
        this.animMid = location;
        this.animation = animation;
        this.seconds = seconds;
        this.sounds = sounds;
        this.teleportSound = sounds;

        if(viewers && player != null && !player.hasPotionEffect(PotionEffectType.INVISIBILITY) && player.getGameMode() != GameMode.SPECTATOR) {
            List<Player> players = new ArrayList<>();
            players.add(player);

            Bukkit.getOnlinePlayers().forEach(p -> {
                if(!p.equals(player) && p.getWorld().equals(player.getWorld()) && p.canSee(player)) players.add(p);
            });

            this.viewers = players.toArray(new Player[0]);
        } else this.viewers = player == null ? new Player[0] : new Player[] {player};
    }

    private void buildBuffBackup() {
        if(player == null || animation.getBuffList().isEmpty()) return;
        for(PotionEffect p : player.getActivePotionEffects()) {
            buffBackup.add(new PotionEffect(p.getType(), p.getDuration(), p.getAmplifier(), p.isAmbient(), p.hasParticles()));
        }
    }

    private void removeActivePotionEffects() {
        if(player == null || animation.getBuffList().isEmpty()) return;
        for(PotionEffect p : player.getActivePotionEffects()) {
            player.removePotionEffect(p.getType());
        }
    }

    private void restoreBuffs() {
        if(player == null || animation.getBuffList().isEmpty()) return;
        for(PotionEffect potionEffect : this.buffBackup) {
            player.addPotionEffect(potionEffect);
        }
    }

    private void buildAnimations() {
        for(ParticlePart particlePart : this.animation.getParticleParts()) {
            try {
                CustomAnimation anim = particlePart.build(viewers, animMid);
                if(anim != null) {
                    this.animations.add(anim);
                    anim.setMaxDistance(maxDistance);
                }
            } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    private void buildRunnable() {
        this.runnable = new BukkitRunnable() {
            private int left = seconds;
            private final String msg = Lang.get("Teleporting_Info");

            @Override
            public void run() {
                if(seconds == -1 || left > 0) {
                    if(player != null) {
                        for(Buff buff : animation.getBuffList()) {
                            if(buff.getTimeBeforeTeleport() == left || (left == seconds && buff.getTimeBeforeTeleport() > left)) {
                                player.addPotionEffect(new PotionEffect(buff.getType(), 20 * left + 20 * buff.getTimeAfterTeleport() * (buff.getTimeAfterTeleport() == 0 ? 10 : 1), buff.getLevel(), false, false));
                            }
                        }
                    }

                    if(sounds && animation.getTickSound() != null && player != null) animation.getTickSound().play(player);
                    if(seconds == -1) return;
                } else if(left == 0) {
                    if(!loop) {
                        for(CustomAnimation anim : animations) {
                            anim.setRunning(false);
                        }
                    }

                    if(player != null) {
                        for(Buff buff : animation.getBuffList()) {
                            if(buff.getTimeAfterTeleport() == 0) {
                                player.removePotionEffect(buff.getType());
                            } else if(buff.getTimeBeforeTeleport() == 0) {
                                player.addPotionEffect(new PotionEffect(buff.getType(), 20 * buff.getTimeAfterTeleport(), buff.getLevel(), false, false));
                            }
                        }
                    }

                    if(teleportSound && animation.getTeleportSound() != null && player != null) animation.getTeleportSound().play(player);

                    if(player == null || player.getActivePotionEffects().isEmpty()) setRunning(false);
                } else {
                    if(player != null) {
                        for(Buff buff : animation.getBuffList()) {
                            if(buff.getTimeAfterTeleport() == -left) {
                                player.removePotionEffect(buff.getType());
                            }
                        }
                    }

                    if(player == null || player.getActivePotionEffects().isEmpty()) setRunning(false);
                }

                left--;
            }
        };
    }

    public HitBox getHitBox() {
        if(this.animations == null) return null;
        if(hitBox != null) return hitBox;
        else {
            for(CustomAnimation a : animations) {
                HitBox box = a.getHitBox();
                if(hitBox == null) hitBox = box;
                else hitBox.addProperty(box);
            }

            return hitBox;
        }
    }

    public void update() {
        if(running) {
            if(loop) {
                setLoop(false);
                setRunning(false);
                setRunning(true);
                setLoop(true);
            } else {
                setRunning(false);
                setRunning(true);
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        if(this.animation != null) {
            if(this.running != running) {
                if(running) {
                    buildBuffBackup();
                    removeActivePotionEffects();
                    buildRunnable();

                    if(animations.isEmpty()) {
                        buildAnimations();

                        for(CustomAnimation anim : this.animations) {
                            anim.setRunning(true);
                        }
                    }

                    this.runnable.runTaskTimer(WarpSystem.getInstance(), 0, 20);
                } else {
                    if(!loop) {
                        for(CustomAnimation anim : this.animations) {
                            anim.setRunning(false);
                        }
                    }

                    this.runnable.cancel();
                    removeActivePotionEffects();
                    if(!loop) {
                        restoreBuffs();
                        buffBackup.clear();
                        animations.clear();
                    }

                    if(loop) {
                        this.running = running;
                        setRunning(true);
                        return;
                    }
                }
            }
        }

        this.running = running;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public boolean isTeleportSound() {
        return teleportSound;
    }

    public void setTeleportSound(boolean teleportSound) {
        this.teleportSound = teleportSound;
    }

    public Player[] getViewers() {
        return viewers;
    }
}
