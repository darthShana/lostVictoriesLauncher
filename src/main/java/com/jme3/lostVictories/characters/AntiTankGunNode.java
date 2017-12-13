/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.lostVictories.CharcterParticleEmitter;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.blenderModels.BlenderModel;
import com.jme3.lostVictories.characters.blenderModels.SoldierBlenderModel;
import com.jme3.lostVictories.characters.blenderModels.VehicleBlenderModel;
import com.jme3.lostVictories.characters.physicsControl.BetterVehicleControl;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

import java.util.Map;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class AntiTankGunNode extends GameVehicleNode{
    
    private final BlenderModel gunnerModel;
    
    public AntiTankGunNode(UUID id, Node model, Map<Country, Node> operator, Country country, CommandingOfficer commandingOfficer, Vector3f worldCoodinates, Vector3f rotation, Node rootNode, BulletAppState bulletAppState, CharcterParticleEmitter emitter, ParticleManager particleManager, NavigationProvider pathFinder, AssetManager assetManager, VehicleBlenderModel m, BehaviorControler behaviorControler, Camera camera) {
        super(id, model, operator, country, commandingOfficer, worldCoodinates, rotation, rootNode, bulletAppState, emitter, particleManager, pathFinder, assetManager, m, behaviorControler, camera);
        
        gunnerModel = new SoldierBlenderModel("", 2, Weapon.cannon());
        if(operatorChannel.get(country)!=null){
            operatorChannel.get(country).setAnim(m.getOperatorIdleAnimation(), LoopMode.Loop, null, (float) Math.random());
        }

    }

    @Override
    public void simpleUpate(float tpf, WorldMap map, Node rootNode) {
        if(isAbbandoned()){
            return;
        }
        super.simpleUpate(tpf, map, rootNode); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void turnLeft() {
        if(isAbbandoned() || "gunnerDeathAction".equals(operatorChannel.get(country).getAnimationName())){
            return;
        }
        if(!"leftTurnAction".equals(operatorChannel.get(country).getAnimationName())){
            operatorChannel.get(country).setAnim("leftTurnAction", 500f, LoopMode.Loop, 1f, null);
         }   
        super.turnLeft();        
    }

    @Override
    public void turnRight() {
        if(isAbbandoned() || "gunnerDeathAction".equals(operatorChannel.get(country).getAnimationName())){
            return;
        }
        if(!"rightTurnAction".equals(operatorChannel.get(country).getAnimationName())){
            operatorChannel.get(country).setAnim("rightTurnAction", 500f, LoopMode.Loop, 1f, null);
         }    
        super.turnRight();
    }

    @Override
    public void straighten() {
        if(isAbbandoned() || "gunnerDeathAction".equals(operatorChannel.get(country).getAnimationName())){
            return;
        }
        if(!"forwardAction".equals(operatorChannel.get(country).getAnimationName())){
            operatorChannel.get(country).setAnim("forwardAction", 500f, LoopMode.Loop, null, null);
        }
        
        super.straighten();
    }

    @Override
    public void stop() {
        if(isAbbandoned() || "gunnerDeathAction".equals(operatorChannel.get(country).getAnimationName())){
            return;
        }
        
        if(!"idleAction".equals(channel.getAnimationName())){
            channel.setAnim("idleAction", LoopMode.Loop, null);
        }
        
        if(!"idleAction".equals(operatorChannel.get(country).getAnimationName())){
            operatorChannel.get(country).setAnim("idleAction", .1f, LoopMode.Loop, null, null);
        }
        
    }

    @Override
    public void setupWeapon(Vector3f direction) {
        if(isAbbandoned() || "gunnerDeathAction".equals(operatorChannel.get(country).getAnimationName())){
            return;
        }
        super.setupWeapon(direction);
        if(!"aimAction".equals(operatorChannel.get(country).getAnimationName())){
            operatorChannel.get(country).setAnim("aimAction", .1f, LoopMode.DontLoop, null, null);
        }
    }
    
    @Override
    public float getTurnSpeed() {
        return 5;
    }
    
    @Override
    protected BetterVehicleControl createCharacterControl(AssetManager manager) {
        return new BetterVehicleControl(800, this, (VehicleBlenderModel) model, manager);
    }

    @Override
    public boolean shoot(Vector3f... targets) {
        if(isAbbandoned() || "gunnerDeathAction".equals(operatorChannel.get(country).getAnimationName())){
            return false;
        }
        
        if(super.shoot(targets)){
            gunnerModel.startFiringSequence(operatorChannel.get(country));
            return true;
        }
        return false;
        
    }
    
    
    @Override
    public Vector3f getPositionToTarget(GameCharacterNode targetedBy) {
        if(targetedBy.getWeapon() == Weapon.cannon()){
            return getLocalTranslation().add(new Vector3f(0f, 1.0f, 0f));
        }
        return getLocalTranslation().add(new Vector3f(0f, 2.0f, 0f));
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        gunnerModel.transitionFireingSequence(operatorChannel.get(country), animName, muzzelFlash);
        super.onAnimCycleDone(control, channel, animName);
    }

    @Override
    public float getDriveSpeed() {
        return 10;
    }

    @Override
    public boolean canBoard(Commandable selectedCharacter) {
        if(super.canBoard(selectedCharacter)){
            return isAbbandoned();
        }
        return false;
    }

    @Override
    public float getEnginePower() {
        return 800;
    }

    @Override
    protected void removeOperator() {
        super.removeOperator();
        channel.setAnim("idleAction");
        playerControl.applyBreak();

    }

    @Override
    public float getMaxStearingAngle() {
        return .9f;
    }

    @Override
    protected void addOperator(Country newCountry) {
        super.addOperator(newCountry);
        if(operatorChannel.get(newCountry)!=null && finishedGunnerDeath){
            operatorChannel.get(newCountry).setAnimForce(((VehicleBlenderModel)model).getOperatorIdleAnimation(), LoopMode.Loop); 
            finishedGunnerDeath = false;
        }
    }
    
  
}
