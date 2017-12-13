/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters.weapons;

import com.jme3.collision.CollisionResult;
import com.jme3.effect.ParticleEmitter;
import com.jme3.lostVictories.CanInteractWith;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.Optional;

/**
 *
 * @author dharshanar
 */
public class Rifle extends Weapon{
    public static Vector3f muzzelLocation = new Vector3f(0f, 1.55f, 1.4f);
    
    protected Rifle() {
        super("rifle_aimAction", "rifle_shootAction", "rifle_standDownAction", "rifle_idleAction");
    }

    
    
    @Override
    public float getMaxRange(){
        return 100;
    }

    @Override
    public float getPartiplesPerSecond() {
        return 0;
    }

    @Override
    public Vector3f getMuzzelLocation() {
        return muzzelLocation;
    }

    @Override
    public boolean canMoveDuringSetup() {
        return true;
    }

    @Override
    public boolean isReadyToShoot(String animationName, Vector3f playerDirection, Vector3f aimingDirection) {
        return !isInFiringSequence(animationName);
    }

    @Override
    public boolean hasFiredProjectile(String animName) {
        return animName.contains("shootAction");
    }

    @Override
    public boolean isInFiringSequence(String animationName) {
        return animationName.contains("aimAction") || animationName.contains("shootAction");
    }

    @Override
    public String getDieAction(GameAnimChannel channel) {
        return getName()+"_dieAction";
    }

    @Override
    public boolean isStanding(GameAnimChannel channel) {
        return true;
    }

    @Override
    public Quaternion getSetupRotation() {
        return null;
    }

    @Override
    public Vector3f getSetupTranslation() {
        return null;
    }

    @Override
    public boolean takesProjectilePath() {
        return false;
    }

    @Override
    public boolean canShootMultipleTargets() {
        return false;
    }

    @Override
    public boolean isWithinFieldOfVision(Vector3f playerDirection, Vector3f aimingDirection) {
        return true;
    }

    @Override
    public String getName() {
        return "rifle";
    }

    @Override
    public void doDamage(GameCharacterNode shooter, CollisionResult result, Optional<CanInteractWith> victim, ParticleManager particleManager, ParticleEmitter bulletFragments) {
        if(victim.isPresent()) {
            victim.get().takeBullet(result, shooter);
        }else{
            bulletFragments.setLocalTranslation(result.getContactPoint());
            bulletFragments.emitAllParticles();
        }
    }
    
   @Override
    public void removeUnusedWeapons(Node node) {
        final Spatial c2 = node.getChild("mortar");
        if(c2!=null){
            c2.removeFromParent();
        }
        final Spatial c4 = node.getChild("mg42");
        if(c4!=null){
            c4.removeFromParent();
        }
        final Spatial c7 = node.getChild("missile");
        if(c7!=null){
            c7.removeFromParent();
        }
       final Spatial c12= node.getChild("bazooka");
       if(c12!=null){
           c12.removeFromParent();
       }
    }

    
}
