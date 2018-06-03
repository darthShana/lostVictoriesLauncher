/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;


import akka.actor.ActorRef;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.lostVictories.*;
import com.jme3.lostVictories.actions.MoveAction;
import com.jme3.lostVictories.characters.blenderModels.BlenderModel;
import com.jme3.lostVictories.characters.blenderModels.VehicleBlenderModel;
import com.jme3.lostVictories.characters.physicsControl.BetterSoldierControl;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.SquadType;
import com.jme3.lostVictories.network.messages.actions.Action;
import com.jme3.lostVictories.objectives.CompleteBootCamp;
import com.jme3.lostVictories.objectives.ManualControlByAvatar;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.lostVictories.structures.Pickable;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.*;

/**
 *
 * @author dharshanar
 */
public class AvatarCharacterNode extends GameCharacterNode<BetterSoldierControl> implements CommandingOfficer{
    public static final Quaternion REVERSE_ROTATION = new Quaternion().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
    public static final Quaternion LEFT_ROTATION = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
    public static final Quaternion RIGHT_ROTATION = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y);
    private List<Commandable> charactersUnderCommand = new ArrayList<>();
    private final Rank rank;
    private final HeadsUpDisplayAppState hud;
    private Objective objective;
    private MoveMode currentMoveMode;
    private Long moveModeToggleTime;
    private boolean hasChrouched = false;
    private ManualControlByAvatar boaredVehicleControl;
    protected Vector3f fpsPossition = new Vector3f(0, 3f, 0);
    private EnemyActivityReport activityReport = new EnemyActivityReport();;

    public AvatarCharacterNode(UUID id, Node model, Country country, CommandingOfficer commandingOfficer, Vector3f worldCoodinates, Vector3f rotation, SquadType squadType, Node rootNode, BulletAppState bulletAppState, CharcterParticleEmitter particleEmitter, ParticleManager particleManager, NavigationProvider pathFinder, AssetManager assetManager, BlenderModel m, Rank rank, HeadsUpDisplayAppState hud, ActorRef shootssFiredListener) {
        super(id, model, country, commandingOfficer, worldCoodinates, rotation, squadType, rootNode, bulletAppState, particleEmitter, particleManager, pathFinder, assetManager, m, shootssFiredListener);
        this.rank = rank;
        this.hud = hud;
    }
        
    public void simpleUpate(float tpf, WorldMap map, Node rootNode) {
        Vector3f playerDirection = null;
        characterAction.travelPath();

        if(characterAction.isTraversingPath()){
            final Vector3f waypoint = characterAction.getTraversingPathNextStep();
            MoveMode moveMode = getMoveMode(waypoint!=null?getLocalTranslation().subtract(waypoint).length():null);
            Vector3f pathStep = characterAction.traversePath(this, 0.016f*moveMode.speed(), getLocalTranslation(), rootNode);
            if(pathStep == null){
                idle();
            }else if(!Vector3f.ZERO.equals(pathStep)){
                moveMode.doAnimation(this);
                Vector3f newDirection = MoveAction.calculateTurnTowardsDirection(playerControl, pathStep.normalize());
                
                playerControl.setWalkDirection(pathStep);
                playerDirection = newDirection;
            }
        }else{
            if(characterAction.isTurningToTarget()){
                playerDirection = characterAction.turn(getPlayerDirection(), 0.016f);
            }
            if(characterAction.isMovingForward()){
                final MoveMode moveMode = getMoveMode(7f);
                moveMode.doAnimation(this);
                Vector3f walkDirection = getPlayerDirection().mult(0.016f*moveMode.speed());
                playerControl.setWalkDirection(walkDirection);
            }
            if(characterAction.isMovingBackward()){
                final Vector3f mult = REVERSE_ROTATION.mult(getPlayerDirection());
                Vector3f walkDirection = mult.mult(0.016f*MoveMode.WALK.speed());
                playerControl.setWalkDirection(walkDirection);
            }
            if(characterAction.isShiftLeft()){
                final Vector3f mult = LEFT_ROTATION.mult(getPlayerDirection());
                Vector3f walkDirection = mult.mult(0.016f*MoveMode.WALK.speed());
                playerControl.setWalkDirection(walkDirection);
            }
            if(characterAction.isShiftRight()){
                final Vector3f mult = RIGHT_ROTATION.mult(getPlayerDirection());
                Vector3f walkDirection = mult.mult(0.016f*MoveMode.WALK.speed());
                playerControl.setWalkDirection(walkDirection);
            }
            if(characterAction.isTurningLeft()){
                Quaternion leftRotation = new Quaternion().fromAngleAxis(FastMath.HALF_PI*0.016f, Vector3f.UNIT_Y);
                playerDirection = leftRotation.mult(getPlayerDirection());
            }
            if(characterAction.isTurningRight()){
                Quaternion rightRotation = new Quaternion().fromAngleAxis(-FastMath.HALF_PI*0.016f, Vector3f.UNIT_Y);
                playerDirection = rightRotation.mult(getPlayerDirection());
            }
        }
        if(characterAction.hasStopped()){
            playerControl.setWalkDirection(Vector3f.ZERO);
        }

        if(playerDirection!=null && !model.isAlreadyFiring(channel)){
            playerControl.setViewDirection(playerDirection);
        }
        if(getLocalTranslation().y<-100){
            die(this);
        }

    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        super.onAnimCycleDone(control, channel, animName);
        if(animName.contains("_aimAction") && characterAction.getTarget()!=null){
            shoot(characterAction.getTarget());
            characterAction.clearTarget();        
        }
    }

    @Override
    public void planObjectives(WorldMap worldMap) {

    }

    @Override
    public void travel(Vector3f contactPoint, GameCharacterNode issuingCharacter) {
//        System.out.println("in here avatar location:"+getLocalTranslation());
        if(isDead || model.isAlreadyFiring(channel)){
            return;
        }
        if(getLocalTranslation().distance(contactPoint)>50){
            hud.addMessage("hmm.. thats a really long walk");
            hud.addMessage("see if you can use a vehicle");
            hud.addMessage("you can use a vehicle in your unit or commandeer one by");
            hud.addMessage("moving very close to the vehicle and right clicking on it");
            hud.addMessage("you can ask your units to get in as well, just select them and right click on the vehicle you control");
        }

        characterAction.calculatePath(this, getLocalTranslation(), contactPoint, pathFinder);
        
    }

    @Override
    public void attack(Vector3f target, GameCharacterNode issuingCharacter) {
        //characterAction.lookAt(getLocalTranslation(), target, playerControl.getViewDirection());
        Vector3f[] spread;
        if(canShootMultipleTargets()){
            spread = new Vector3f[]{new Vector3f(target), new Vector3f(target), new Vector3f(target), new Vector3f(target), new Vector3f(target)};
        }else{
            spread = new Vector3f[]{target};
        }
        final Vector3f subtract = target.subtract(getLocalTranslation());
        playerControl.setViewDirection(new Vector3f(subtract.x, 0, subtract.z).normalizeLocal());
        
        if(!model.canShootWithoutSetup() && !model.isReadyToShoot(channel, getAimingDirection(), target.subtract(getLocalTranslation()))){
            characterAction.stopForwardMovement();
            characterAction.turn(target , 0.016f);
            characterAction.target(spread);
            setupWeapon(target);
        }else{    
            shoot(spread);
        }
    }
    
    public void collect(Pickable pickable, GameCharacterNode issuingCharacter) {
        NetworkClientAppState.get().requestEquipmentCollection(pickable.getId(), getIdentity());
    }

    public void requestBoarding(GameVehicleNode vehicle, GameCharacterNode issuingCharacter) {
        NetworkClientAppState.get().requestBoardVehicle(vehicle.getIdentity(), getIdentity());
    }

    @Override
    void boardVehicleAction(GameVehicleNode vehicle) {
        super.boardVehicleAction(vehicle);
        hud.addMessage("Great! you can use this vehicle to travel faster!");
        hud.addMessage("you can right click on the terrain and the driver will drive to the destination");
        hud.addMessage("or you can drive your self use w=forward, a=left, d=right, s=reverse");
        hud.addMessage("press e to get out");
    }
    
    
    

    public void cover(Vector3f mousePress, Vector3f mouseRelease, GameCharacterNode issuingCharacter) {
        characterAction.stopForwardMovement();
        characterAction.lookAt(getLocalTranslation(), mouseRelease, playerControl.getViewDirection());
        super.setupWeapon(mouseRelease.subtract(mousePress));
    }
    
    public void goForward() {
        if(!isDead && canPlayMoveAnimation(channel.getAnimationName())){
            characterAction.goForward();
            getMoveMode(7f).doAnimation(this);
            ManualControlByAvatar boardedVehicleControl = getControlOfBoardedVehicle();
            if(boardedVehicleControl!=null){                
                boardedVehicleControl.forward();
            }
        }
        
    }
    
    public void goBackward() {
        if(!isDead && canPlayMoveAnimation(channel.getAnimationName())){
            characterAction.goBackward();
            doReverseWalkActoin();
            ManualControlByAvatar boardedVehicleControl = getControlOfBoardedVehicle();
            if(boardedVehicleControl!=null){
                boardedVehicleControl.reverse();
            }
        }
    }
    
    public void shiftLeft() {
        if(!isDead && canPlayMoveAnimation(channel.getAnimationName())){
            characterAction.shiftLeft();
            doWalkAction();
        }
    }
    
    public void shiftRight() {
        if(!isDead){
            characterAction.shiftRight();
            doWalkAction();
        }
    }

    public void crouch(GameCharacterNode issuingCharacter) {
        characterAction.stopForwardMovement();
        model.doCrouchAction(channel, shell);
        hasChrouched = true;
    }

    public void stand(GameCharacterNode issuingCharacter) {
        model.doStandAction(channel, shell);
    }
    
    
    
    public void doWalkAction(){
        if(canPlayMoveAnimation(channel.getAnimationName()) && !channel.getAnimationName().contains("walkAction")){
            model.doWalkAction(channel, shell);
        }
    }

    public void doRunAction(){
        if(canPlayMoveAnimation(channel.getAnimationName()) && !channel.getAnimationName().contains("runAction")){
            model.doRunAction(channel, shell);
        }
    }
    
    public void stopForwardMovement() {
        if(!isDead){
            characterAction.stopForwardMovement();
            idle();
            ManualControlByAvatar boardedVehicleControl = getControlOfBoardedVehicle();
            if(boardedVehicleControl!=null){
                boardedVehicleControl.neutral();
            }
        }
    }
        
    public void turnLeft() {
        characterAction.turnLeft();
        ManualControlByAvatar boardedVehicleControl = getControlOfBoardedVehicle();
        if(boardedVehicleControl!=null){
            boardedVehicleControl.turnLeft();
        }
    }

    public void stopTurningLeft() {
        characterAction.stopTurnLeft();
        ManualControlByAvatar boardedVehicleControl = getControlOfBoardedVehicle();
        if(boardedVehicleControl!=null){
            boardedVehicleControl.straighten();
        }
    }

    public void turnRight() {
        characterAction.turnRight();
        ManualControlByAvatar boardedVehicleControl = getControlOfBoardedVehicle();
        if(boardedVehicleControl!=null){
            boardedVehicleControl.turnRight();
        }
    }

    public void stopTurningRight() {
        characterAction.stopTurnRight();
        ManualControlByAvatar boardedVehicleControl = getControlOfBoardedVehicle();
        if(boardedVehicleControl!=null){
            boardedVehicleControl.straighten();
        }
    }
    
    @Override
    public void addObjective(Objective objective) {
        this.objective = objective;
    }

    @Override
    public Set<String> getCompletedObjectives() {
        return new HashSet<>();
    }

    Map<UUID, Objective> emptyObjectives = new HashMap<UUID, Objective>();
    @Override
    public Map<UUID, Objective> getAllObjectives() {        
        return emptyObjectives;
    }

    @Override
    public void addEnemyActivity(Vector3f localTranslation, long l) { }

    @Override
    public EnemyActivityReport getEnemyActivity() {
        return activityReport;
    }

    @Override
    public boolean isBusy() {
        return objective!=null && !objective.isComplete();
    }

    public boolean isAttacking() {
        return false;
    }
    
    

    public List<Commandable> getCharactersUnderCommand() {
        return this.charactersUnderCommand;
    }

    public void addCharactersUnderCommand(Set<Commandable> cc) {
        this.charactersUnderCommand.addAll(cc);
        hud.updateHeadsUpDisplay();
    }
    
    public void addCharactersUnderCommand(Commandable c) {
        this.charactersUnderCommand.add(c);
        hud.updateHeadsUpDisplay();
    }

    public void removeAllUnits() {
        this.charactersUnderCommand.clear();
        hud.updateHeadsUpDisplay();
    }

    public void removeCharacterUnderCommand(Commandable aThis) {
        charactersUnderCommand.remove(aThis);
        hud.updateHeadsUpDisplay();
    }
    
    public void updateHeadsUpDisplay(){
        hud.updateHeadsUpDisplay();        
    }
    
    @Override
    public int getKillCount() {
        int k = super.getKillCount();
        for(Commandable n: getCharactersUnderCommand()){
            k+=n.getKillCount();
        }
        return k;
    }

    @Override
    public Rank getRank() {
        return rank;
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
    
    protected BetterSoldierControl createCharacterControl(AssetManager manager) {
        BetterSoldierControl pc = new BetterSoldierControl(this,.5f, 2f, 150);
        pc.setGravity(new Vector3f(0f,10,0f));
        pc.setJumpForce(new Vector3f(0f,.1f,0f));
        
        return pc;
    }

    @Override
    public BetterSoldierControl getCharacterControl() {
        return playerControl;
    }
    
    

    @Override
    public void follow(GameCharacterNode toFollow, GameCharacterNode issuingCharacter) { }

    @Override
    void doTakeBulletEffects(Vector3f point) {
        bloodDebris.setLocalTranslation(point);
        bloodDebris.emitAllParticles();
    }

    public Objective getCurrentObjectives() {
        return objective;
    }

    public boolean isTeam(Weapon... weapons) {
        if(hasWeapon(weapons)){
            return true;
        }
        
        for(Commandable n:charactersUnderCommand){
            if(n instanceof GameCharacterNode){
                if(((GameCharacterNode)n).hasWeapon(weapons)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isTravesingPath() {
        return characterAction.isTraversingPath();
    }
    
    public boolean isWalkingForward(){
        return characterAction.isMovingForward();
    }

    public Vector3f getTravesingPathDestination() {
        return characterAction.getTraversingPathDestination();
    }

    @Override
    public void playDestroyAnimation(Vector3f point) {
        doTakeBulletEffects(point);
    }

    private MoveMode getMoveMode(Float distanceToWaypoint) {
        if(currentMoveMode==null){
            moveModeToggleTime = System.currentTimeMillis();
            if(distanceToWaypoint!=null && distanceToWaypoint>5){
                currentMoveMode = MoveMode.RUN;
            }else{
                currentMoveMode = MoveMode.WALK;
            }
        }
        
        if(MoveMode.RUN==currentMoveMode && System.currentTimeMillis()-moveModeToggleTime>10000){
            moveModeToggleTime = System.currentTimeMillis();
            currentMoveMode = MoveMode.WALK;
        }
        if(MoveMode.WALK==currentMoveMode && System.currentTimeMillis()-moveModeToggleTime>30000 && distanceToWaypoint!=null && distanceToWaypoint>5){
            moveModeToggleTime = System.currentTimeMillis();
            currentMoveMode = MoveMode.RUN;
        }
        return currentMoveMode;
    }

    public boolean hasCrouchedAsSomeStage() {
        return hasChrouched;
    }

    public void resetCrouchCheck() {
        hasChrouched = false;
    }

    public void completeBootCamp() {
        if(objective!=null && objective instanceof CompleteBootCamp){
            objective.completeObjective();
        }
    }
    
    public float getRadius() {
        return .5f;
    }

    @Override
    public CharacterMessage toMessage() {
        final CharacterMessage toMessage = super.toMessage();

        toMessage.setType(CharacterType.AVATAR);
        return toMessage;
    }

    @Override
    public Set<Action> getActionsToMessage() {
        Set<Action> actionsToMessage = super.getActionsToMessage();
        if(boaredVehicleControl!=null){
            actionsToMessage.add(boaredVehicleControl.getAction().toMessage());
        }
        return actionsToMessage;
    }

    //to delete
    
    @Override
    public boolean isControledLocaly() {
        return true;
    }
    
    @Override
    public boolean isControledRemotely() {
        return false;
    }

    @Override
    public boolean isAbbandoned() {
        return false;
    }
    
    @Override
    public void checkForNewObjectives(Map<String, String> objectives) {
        
    }

    @Override
    public BehaviorControler getBehaviourControler() {
        return null;
    }

    @Override
    public void setBehaviourControler(BehaviorControler remoteBehaviourControler) {}

    ManualControlByAvatar getControlOfBoardedVehicle() {
        if(boardedVehicle !=null){
            if(boaredVehicleControl==null){
                 boaredVehicleControl = new ManualControlByAvatar();
                 boardedVehicle.addObjective(boaredVehicleControl);
                 return boaredVehicleControl;
            }
            return boaredVehicleControl;
        }else{
            return null;
        }
    }

    void clearBoardedVehicleControl() {
        if(boaredVehicleControl!=null){
            boaredVehicleControl.completeObjective();
            boaredVehicleControl = null;
        }
    }

    public boolean isHuman() {
        return true;
    }

    public Vector3f getFPSViewPoint() {
        if(getBoardedVehicle()!=null){
            fpsPossition.y = ((VehicleBlenderModel)getBoardedVehicle().model).getOperatorTranslation().y+1.75f;
            return fpsPossition;
        }
        if(isCrouched()){
            fpsPossition.y = .75f;
        }else{
            fpsPossition.y = 1.5f;
        }
        return fpsPossition;
    }
    
    
    
}
