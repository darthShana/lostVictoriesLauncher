/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters.weapons;

import com.jme3.animation.LoopMode;
import com.jme3.collision.CollisionResult;
import com.jme3.effect.ParticleEmitter;
import com.jme3.lostVictories.CanInteractWith;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author dharshanar
 */
public abstract class Weapon {

    public static Weapon get(com.jme3.lostVictories.network.messages.Weapon weapon) {
        if(com.jme3.lostVictories.network.messages.Weapon.RIFLE == weapon){
            return rifle();
        }
        if(com.jme3.lostVictories.network.messages.Weapon.MG42 == weapon){
            return mg42();
        }
        if(com.jme3.lostVictories.network.messages.Weapon.MORTAR == weapon){
            return mortar();
        }
        if(com.jme3.lostVictories.network.messages.Weapon.CANNON == weapon){
            return cannon();
        }
        if(com.jme3.lostVictories.network.messages.Weapon.BAZOOKA == weapon){
            return bazooka();
        }
        return null;
    }
    
    private final LinkedList<String> firingSequence = new LinkedList<String>();
    
    private static Rifle rifle = new Rifle();
    private static Mg42 mg42 = new Mg42();
    private static Mortar mortar = new Mortar();
    private static Cannon cannon = new Cannon();
    private static Bazooka bazooka = new Bazooka();

    public static Rifle rifle(){
        return rifle;
    }
    
    public static Mg42 mg42(){
        return mg42;
    }
    
    public static Mortar mortar(){
        return mortar;
    }
    
    public static Cannon cannon(){
        return cannon;
    }

    public static Bazooka bazooka(){ return bazooka; }

    protected Weapon(String... firingSequence) {
        this.firingSequence.addAll(Arrays.asList(firingSequence));
    }
    
    public abstract float getMaxRange();

    public abstract float getPartiplesPerSecond();

    public abstract Vector3f getMuzzelLocation();

    public abstract boolean canMoveDuringSetup();

    public abstract boolean isInFiringSequence(String animationName);

    public abstract boolean isReadyToShoot(String animationName, Vector3f playerDirection, Vector3f aimingDirection);

    public abstract boolean hasFiredProjectile(String animName);

    public abstract void doDamage(GameCharacterNode shooter, CollisionResult result, Optional<CanInteractWith> victim, ParticleManager particleManager, ParticleEmitter bulletFragments);

    public LinkedList<String> getFiringSequence() {
        return firingSequence;
    }

    public void transitionFiringSequence(GameAnimChannel channel, String completedAnimation, ParticleEmitter muzzelFlash, ParticleManager particleManager, GameCharacterNode gameCharacterNode, List<Ray> rays) {
        int i = getFiringSequence().indexOf(completedAnimation);
        if(i!=-1 && i<getFiringSequence().size()-1){
            channel.setAnim(getFiringSequence().get(i+1), LoopMode.DontLoop);
        }else if(channel.getAnimationName()!=null && channel.getAnimationName().contains("aimAction")){
            channel.setAnim(getName()+"_standByAction", LoopMode.DontLoop);
        }
    }

    public abstract String getDieAction(GameAnimChannel channel);

    public abstract boolean isStanding(GameAnimChannel channel);

    public abstract Quaternion getSetupRotation();

    public abstract Vector3f getSetupTranslation();

    public abstract boolean takesProjectilePath();

    public abstract boolean canShootMultipleTargets();

    public abstract boolean isWithinFieldOfVision(Vector3f playerDirection, Vector3f aimingDirection);

    public abstract String getName();
    
    public void removeUnusedWeapons(Node node) {}

    public float getDefaultFiringSpeend() {
        return 1;
    }


    
}
