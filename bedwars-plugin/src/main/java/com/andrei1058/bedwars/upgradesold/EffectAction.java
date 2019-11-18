package com.andrei1058.bedwars.upgradesold;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("WeakerAccess")
public class EffectAction extends UpgradeAction {

    private String name, apply;
    private PotionEffectType potionEffectType;
    private int amplifier, duration;

    public EffectAction(String name, PotionEffectType potionEffectType, int amplifier, String apply, int duration){
        this.name = name;
        this.potionEffectType = potionEffectType;
        this.amplifier = amplifier;
        this.apply = apply;
        if (apply.equals("enemybaseenter") && duration <= 0){
            this.duration = 20;
        } else {
            this.duration = duration;
        }
        BedWars.debug("Loading new EffectAction: "+getName());
    }

    @Override
    public void execute(ITeam bwt, int i) {
        if (apply.equalsIgnoreCase("members")){
            bwt.addTeamEffect(getPotionEffectType(), getAmplifier(), getDuration());
        } else if(apply.equalsIgnoreCase("base")){
            bwt.addBaseEffect(getPotionEffectType(), getAmplifier(), Integer.MAX_VALUE);
        } else if (apply.equalsIgnoreCase("enemybaseenter")){
            bwt.addEnemyBaseEnterEffect(getPotionEffectType(), getAmplifier(), i, getDuration());
        }
    }

    public String getName() {
        return name;
    }

    public PotionEffectType getPotionEffectType() {
        return potionEffectType;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public int getDuration() {
        return duration;
    }
}