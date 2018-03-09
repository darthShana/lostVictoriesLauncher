/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import akka.actor.ActorRef;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.lostVictories.CharcterParticleEmitter;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.characters.blenderModels.TankBlenderModel;
import com.jme3.lostVictories.characters.blenderModels.VehicleBlenderModel;
import com.jme3.lostVictories.characters.physicsControl.BetterTankControl;
import com.jme3.lostVictories.characters.physicsControl.BetterVehicleControl;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.objectives.RotateTurretAndShoot;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

import java.util.Map;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class MediumTankNode extends GameVehicleNode{

    private final Node turretModel;
    private GameAnimChannel turretChannel;

    public MediumTankNode(UUID id, Node chassisModel, Node turretModel, Map<Country, Node> operator, Country country, CommandingOfficer commandingOfficer, Vector3f worldCoodinates, Vector3f rotation, Node rootNode, BulletAppState bulletAppState, CharcterParticleEmitter emitter, ParticleManager particleManager, NavigationProvider pathFinder, AssetManager assetManager, VehicleBlenderModel m, BehaviorControler behaviorControler, ActorRef shootsFiredListener) {
        super(id, chassisModel, operator, country, commandingOfficer, worldCoodinates, rotation, rootNode, bulletAppState, emitter, particleManager, pathFinder, assetManager, m, behaviorControler, shootsFiredListener);
        this.turretModel = turretModel;
        final AnimControl control1 = turretModel.getControl(AnimControl.class);
        if(control1!=null){
            turretChannel = new GameAnimChannel(control1.createChannel(), m);
            control1.addListener(this);
        }
    }

    @Override
    public void attachToRootNode() {
        super.attachToRootNode();
        ((BetterTankControl)playerControl).addTurret(this, turretModel);

    }

    @Override
    public void attack(Vector3f target, GameCharacterNode issuingCharacter) {
        if(!isUnderChainOfCommandOf(issuingCharacter, 5)){
            return;
        }
        addObjective(new RotateTurretAndShoot(target));

    }

    @Override
    public boolean shoot(Vector3f... targets) {
        boolean shoot = super.shoot(targets);
        if(!shoot && !isManuallyControlledByAvatar()){
            ((BetterTankControl)playerControl).turnTurretTo(targets[0].subtract(getLocalTranslation()));
        }
        return shoot;
    }

    @Override
    public Vector3f getPositionToTarget(GameCharacterNode targetedBy) {
        return getLocalTranslation().add(new Vector3f(0f, .5f, 0f));
    }

    @Override
    public void turnLeft() {
        if(!"forwardAction".equals(channel.getAnimationName())) {
            this.channel.setAnim("forwardAction", LoopMode.Loop);
        }
    }

    @Override
    public void turnRight() {
        if(!"forwardAction".equals(channel.getAnimationName())) {
            this.channel.setAnim("forwardAction", LoopMode.Loop);
        }
    }

    @Override
    public void setupWeapon(Vector3f direction) {}

    @Override
    public float getTurnSpeed() {
        return 10;
    }

    @Override
    public float getDriveSpeed() {
        return 25;
    }

    @Override
    public float getEnginePower() {
        return 4000;
    }

    @Override
    public float getMaxStearingAngle() {
        return .5f;
    }

    @Override
    protected BetterVehicleControl createCharacterControl(AssetManager manager) {
        return new BetterTankControl(2000, this, (TankBlenderModel) model, manager, bulletAppState);
    }

    @Override
    protected GameAnimChannel getShootingChannel() {
        return turretChannel;
    }

    @Override
    public void playDestroyAnimation(Vector3f point) {
        super.playDestroyAnimation(point);

        if(!"explodeAction".equals(turretChannel.getAnimationName())){
            turretChannel.setAnim("explodeAction", LoopMode.DontLoop, 1f);
        }

    }

    public void turretLeft() {
        ((BetterTankControl)playerControl).turretLeft();
    }

    public void stopTurret() {
        ((BetterTankControl)playerControl).turretStop();
    }

    public void turretRight() {
        ((BetterTankControl)playerControl).turretRight();
    }

    @Override
    public Vector3f getAimingDirection() {
        return ((BetterTankControl)playerControl).getTurretDirection();
    }
    
    public void setTurretChannel(GameAnimChannel channel){
        this.turretChannel = channel;
    }
    
}
