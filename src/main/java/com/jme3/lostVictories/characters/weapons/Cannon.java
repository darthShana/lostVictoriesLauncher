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
import com.jme3.math.*;
import com.jme3.scene.Node;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author dharshanar
 */
public class Cannon extends Weapon{

    public Quaternion setupRotation = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X);
    
    public Cannon(String... firingSequence) {
        super("cannon_loadAction", "cannon_shootAction", "cannon_standByAction");
    }
    
    @Override
    public float getMaxRange() {
        return 200;
    }

    @Override
    public float getPartiplesPerSecond() {
        return 0;
    }

    @Override
    public Vector3f getMuzzelLocation() {
        return new Vector3f(0f, 1.05f, 2.35f);
    }

    @Override
    public boolean canMoveDuringSetup() {
        return false;
    }

    @Override
    public boolean isInFiringSequence(String animationName) {
        return animationName!=null && (animationName.contains("loadAction") || animationName.contains("shootAction"));
    }

    @Override
    public boolean isReadyToShoot(String animationName, Vector3f playerDirection, Vector3f aimingDirection) {
        return isWithinFieldOfVision(playerDirection, aimingDirection);
    }
    
    @Override
    public boolean hasFiredProjectile(String animName) {
        return animName!=null && animName.contains("shootAction");
    }

    @Override
    public String getDieAction(GameAnimChannel channel) {
        return "dieStandingAction";
    }

    @Override
    public boolean isStanding(GameAnimChannel channel) {
        return true;
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
    public float getDefaultFiringSpeend() {
        return 2;
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
    public void transitionFiringSequence(GameAnimChannel channel, String completedAnimation, ParticleEmitter muzzelFlash, ParticleManager particleManager, GameCharacterNode player, List<Ray> rays) {
        if("cannon_loadAction".equals(completedAnimation)) {
            particleManager.playTracerCannonEffect(player.getShootingLocation(), rays.get(0), 1);
        }
        super.transitionFiringSequence(channel, completedAnimation, muzzelFlash, particleManager, player, rays);
    }

    @Override
    public boolean isWithinFieldOfVision(Vector3f playerDirection, Vector3f aimingDirection) {
        Vector2f v1 = new Vector2f(playerDirection.x, playerDirection.z).normalizeLocal();
        Vector2f v2 = new Vector2f(aimingDirection.x, aimingDirection.z).normalizeLocal();
        return v1.smallestAngleBetween(v2) < (FastMath.QUARTER_PI / 4);
    }

    @Override
    public void doDamage(GameCharacterNode shooter, CollisionResult result, Optional<CanInteractWith> victim, ParticleManager particleManager, ParticleEmitter bulletFragments) {
        particleManager.playExplosionEffect(result.getContactPoint());
        if(victim.isPresent()) {
            victim.get().takeMissile(result, shooter);
        }
    }

    @Override
    public String getName() {
        return "cannon";
    }

    @Override
    public void removeUnusedWeapons(Node node) {
    }

}
