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
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.Optional;

/**
 *
 * @author dharshanar
 */
public class Mortar extends Weapon{
    
    public Quaternion setupRotation = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X);
    
    public Mortar() {
        super("mortar_readyToShootAction", "mortar_shootAction", "mortar_standByAction");
    }

    @Override
    public float getMaxRange() {
        return 150;
    }

    @Override
    public float getPartiplesPerSecond() {
        return 0;
    }

    @Override
    public Vector3f getMuzzelLocation() {
        return new Vector3f(0, 1.25f, .75f);
    }

    @Override
    public boolean canMoveDuringSetup() {
        return false;
    }

    @Override
    public boolean isInFiringSequence(String animationName) {
        return animationName.contains("readyToShootAction") || animationName.contains("shootAction");
    }

    @Override
    public boolean isReadyToShoot(String animationName, Vector3f playerDirection, Vector3f aimingDirection) {
        return animationName.contains("standByAction") && isWithinFieldOfVision(playerDirection, aimingDirection);
    }

    @Override
    public boolean hasFiredProjectile(String animName) {
        return animName.contains("shootAction");
    }

    @Override
    public String getDieAction(GameAnimChannel channel) {
        if(getFiringSequence().contains(channel.getAnimationName())){
            return getName()+"_dieShootingAction";
        }else{
            return getName()+"_dieStandingAction";
        }
    }

    @Override
    public void doDamage(GameCharacterNode shooter, CollisionResult result, Optional<CanInteractWith> victim, ParticleManager particleManager, ParticleEmitter bulletFragments) {
        //need to do something here
    }

    @Override
    public boolean isStanding(GameAnimChannel channel) {
        return getFiringSequence().contains(channel.getAnimationName());
    }

    @Override
    public Quaternion getSetupRotation() {
        return setupRotation;
    }

    @Override
    public Vector3f getSetupTranslation() {
        return new Vector3f(0, .5f, 0);
    }

    @Override
    public boolean takesProjectilePath() {
        return true;
    }

    @Override
    public boolean canShootMultipleTargets() {
        return false;
    }
    
    public boolean isWithinFieldOfVision(Vector3f playerDirection, Vector3f aimingDirection) {
        Vector2f v1 = new Vector2f(playerDirection.x, playerDirection.z).normalizeLocal();
        Vector2f v2 = new Vector2f(aimingDirection.x, aimingDirection.z).normalizeLocal();
        return v1.smallestAngleBetween(v2) < (FastMath.QUARTER_PI/2);
    }

    @Override
    public String getName() {
        return "mortar";
    }
     
    @Override
    public void removeUnusedWeapons(Node node) {
        final Spatial c2 = node.getChild("mg42");
        if(c2!=null){
            c2.removeFromParent();
        }
        final Spatial c11= node.getChild("rifle");
        if(c11!=null){
            c11.removeFromParent();
        }
        final Spatial c12= node.getChild("bazooka");
        if(c12!=null){
            c12.removeFromParent();
        }
        
    }


    
}
