package de.codingair.warpsystem.spigot.features.animations;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.files.loader.UTFConfig;
import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.PlayerAnimation;
import de.codingair.codingapi.server.SoundData;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.animations.utils.Animation;
import de.codingair.warpsystem.spigot.features.animations.utils.AnimationType;
import de.codingair.warpsystem.spigot.features.animations.utils.ParticlePart;
import de.codingair.warpsystem.spigot.features.effectportals.managers.PortalManager;
import de.codingair.warpsystem.utils.Manager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnimationManager implements Manager {
    private static AnimationManager instance = null;
    private List<Animation> animationList = new ArrayList<>();
    private Animation active = null;

    @Override
    public boolean load() {
        if(WarpSystem.getInstance().getFileManager().getFile("Animations") == null) WarpSystem.getInstance().getFileManager().loadFile("Animations", "/Memory/");
        WarpSystem.log("  > Loading Animations");

        UTFConfig config = WarpSystem.getInstance().getFileManager().getFile("Animations").getConfig();
        destroy();

        boolean success = true;
        for(String data : config.getStringList("Animations")) {
            try {
                Animation a = new Animation();
                JSONObject json = (JSONObject) new JSONParser().parse(data);
                a.read(json);
                animationList.add(a);
            } catch(Exception e) {
                e.printStackTrace();
                success = false;
            }
        }

        active = getAnimation(config.getString("Active", null));
        if(active == null) createStandard();

        WarpSystem.log("    ...got " + animationList.size() + " animation(s)");

        return success;
    }

    private void createStandard() {
        Animation animation = getAnimation("Standard");
        if(animation == null) {
            animation = new Animation("Standard", new ParticlePart(AnimationType.CIRCLE, Particle.FLAME, 1, 1, PlayerAnimation.MAX_SPEED));
        }
        this.active = animation;
    }

    @Override
    public void save(boolean saver) {
        if(!saver) WarpSystem.log("  > Saving Animations");
        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("Animations");

        List<String> dataList = new ArrayList<>();
        for(Animation animation : this.animationList) {
            JSONObject json = new JSONObject();
            animation.write(json);
            dataList.add(json.toJSONString());
        }

        file.getConfig().set("Animations", dataList);
        file.getConfig().set("Active", active == null ? null : active.getName());
        file.saveConfig();

        if(!saver) WarpSystem.log("    ...saved " + animationList.size() + " animation(s)");
    }

    @Override
    public void destroy() {
        this.animationList.clear();
    }

    public boolean addAnimation(Animation anim) {
        if(existsAnimation(anim.getName())) return false;
        this.animationList.add(anim);

        return true;
    }

    public Animation getAnimation(String name) {
        if(name == null) return null;
        for(Animation animation : this.animationList) {
            if(animation.getName().equalsIgnoreCase(name)) return animation;
        }

        return null;
    }

    public boolean removeAnimation(Animation animation) {
        if(!this.animationList.remove(animation)) return false;

        if(this.active == animation) {
            this.active = null;
            createStandard();
        }
        return true;
    }

    public List<Animation> getAnimationList() {
        return Collections.unmodifiableList(animationList);
    }

    public boolean existsAnimation(String name) {
        return getAnimation(name) != null;
    }

    public Animation getActive() {
        return active;
    }

    public void setActive(Animation active) {
        this.active = active;
    }

    public static AnimationManager getInstance() {
        if(instance == null) instance = WarpSystem.getInstance().getDataManager().getManager(FeatureType.ANIMATION_EDITOR);
        if(instance == null) instance = new AnimationManager();
        return instance;
    }
}
