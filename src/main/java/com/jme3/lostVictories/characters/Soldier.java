/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import akka.actor.ActorRef;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.lostVictories.CharcterParticleEmitter;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.blenderModels.BlenderModel;
import com.jme3.lostVictories.characters.physicsControl.BetterSoldierControl;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.network.messages.SquadType;
import com.jme3.lostVictories.objectives.*;
import com.jme3.lostVictories.structures.Pickable;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public abstract class Soldier extends AICharacterNode<BetterSoldierControl>{

    public static float SHOOTING_HEIGHT = 1.5f;
    private boolean covering;
    private MoveMode currentMoveMode;
    private Long moveModeToggleTime;
    
    public Soldier(UUID id, Node model, Country country, CommandingOfficer commandingOfficer, Vector3f worldCoodinates, Vector3f rotation, SquadType squadType, Node rootNode, BulletAppState bulletAppState, CharcterParticleEmitter emitter, ParticleManager particleManager, NavigationProvider pathFinder, AssetManager assetManager, BlenderModel m, BehaviorControler behaviorControler, ActorRef shootssFiredListener) {
        super(id, model, country, commandingOfficer, worldCoodinates, rotation, squadType, rootNode, bulletAppState, emitter, particleManager, pathFinder, assetManager, m, behaviorControler, shootssFiredListener);
        
    }
    
    @Override
    public void travel(Vector3f contactPoint, GameCharacterNode issuingCharacter) {
        if(!isUnderChainOfCommandOf(issuingCharacter, 5)){
            return;
        }
        behaviorControler.addObjective(new TravelObjective(this, contactPoint, null));
    }
    
    @Override
    public void follow(GameCharacterNode toFollow, GameCharacterNode issuingCharacter) {
        if(!isUnderChainOfCommandOf(issuingCharacter, 5)){
            return;
        }
        Vector3f f = new Vector3f(1, 0, 1).mult((float)Math.random());
        behaviorControler.addObjective(new FollowCommander(f, 5));
    }

    @Override
    public void attack(Vector3f target, GameCharacterNode issuingCharacter) {
        if(!isUnderChainOfCommandOf(issuingCharacter, 5)){
            return;
        }

        if(WorldMap.get().characterInRangeAndLOStoTarget(this, rootNode, target)){
            behaviorControler.addObjective(new AttackObjective(target));
        }else{
            behaviorControler.addObjective(new AttackAndTakeCoverObjective(this, getLocalTranslation(), target, WorldMap.get(), rootNode, new HashMap<Node, Integer>()));
        }

    }

    public void collect(Pickable pickable, GameCharacterNode issuingCharacter) {
        if(!isUnderChainOfCommandOf(issuingCharacter, 5)){
            return;
        }
        behaviorControler.addObjective(new CollectEquipment(pickable));
    }

    public void requestBoarding(GameVehicleNode key, GameCharacterNode issuingCharacter) {
        if(!isUnderChainOfCommandOf(issuingCharacter, 5)){
            return;
        }
        
        behaviorControler.addObjective(new BoardVehicle(this, key));
    }
    
    
    
    public void cover(Vector3f mousePress, Vector3f mouseRelease, GameCharacterNode issuingCharacter){
        if(!isUnderChainOfCommandOf(issuingCharacter, 5)){
            return;
        }
        behaviorControler.addObjective(new Cover(mousePress, mouseRelease, rootNode));
    }
    
    public void crouch() {
        characterAction.stopForwardMovement();
        model.doCrouchAction(channel, shell);
    }

    public void stand() {
        model.doStandAction(channel, shell);
    }

    @Override
    public boolean takeBullet(CollisionResult result, GameCharacterNode shooter) {
        doTakeBulletEffects(result.getContactPoint());
        die(shooter);
        return true;
    }

    public boolean takeMissile(CollisionResult result, GameCharacterNode shooter) {
        doTakeBulletEffects(result.getContactPoint());
        die(shooter);
        return true;
    }

    @Override
    boolean takeLightBlast(GameCharacterNode shooter) {
        die(shooter);
        return true;
    }
    
    public BetterSoldierControl getCharacterControl(){
        return playerControl;
    }
    
    protected BetterSoldierControl createCharacterControl(AssetManager manager) {
        BetterSoldierControl pc = new BetterSoldierControl(this,.4f, 2f, 150);
        pc.setGravity(new Vector3f(0f, 10f,0f));
        pc.setJumpForce(new Vector3f(0f,.1f,0f));
        return pc;
    }
    
    @Override
    public void playDestroyAnimation(Vector3f point) {
        doTakeBulletEffects(point);
    }    
    
    void doTakeBulletEffects(Vector3f point) {
        bloodDebris.setLocalTranslation(point);
        bloodDebris.emitAllParticles();
    }

    public long getMoveSpeed() {
        return 100;
    }

    public float getRadius() {
        return .5f;
    }

    @Override
    public boolean isAbbandoned() {
        return false;
    }
    
    public void doWalkAction(){
        if(!channel.getAnimationName().contains("shootAction") && !channel.getAnimationName().contains("aimAction") && !channel.getAnimationName().contains("walkAction")){
            try {
                model.doWalkAction(channel, shell);
            }catch (IllegalArgumentException e){
                System.out.println("cant find animation for :"+getClass()+" model:"+model.getModelPath()+" currentAnimation:"+channel.getAnimationName());
                throw e;
            }
        }
    }
    
    public void doRunAction(){
        if(!channel.getAnimationName().contains("shootAction") && !channel.getAnimationName().contains("aimAction") && !channel.getAnimationName().contains("runAction")){
            model.doRunAction(channel, shell);
        }
    }
    
    public boolean isStillDeployed() {
        return covering;
    }

    public void setCovering(boolean b) {
        covering = b;
    }
    
    public MoveMode getMoveMode(float distanceToWaypoint) {
        if(currentMoveMode==null){
            moveModeToggleTime = System.currentTimeMillis();
            if(distanceToWaypoint>5){
                currentMoveMode = MoveMode.RUN;
            }else{
                currentMoveMode = MoveMode.WALK;
            }
        }
        
        if(MoveMode.RUN==currentMoveMode && System.currentTimeMillis()-moveModeToggleTime>20000){
            moveModeToggleTime = System.currentTimeMillis();
            currentMoveMode = MoveMode.WALK;
        }
        if(MoveMode.WALK==currentMoveMode && System.currentTimeMillis()-moveModeToggleTime>10000 && distanceToWaypoint>5){
            moveModeToggleTime = System.currentTimeMillis();
            currentMoveMode = MoveMode.RUN;
        }
        return currentMoveMode;
    }

    public boolean isHuman() {
        return true;
    }
    
    
          
}
