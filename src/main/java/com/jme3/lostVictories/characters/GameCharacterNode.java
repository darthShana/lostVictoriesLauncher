/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;


import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.ai.steering.Obstacle;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.lostVictories.*;
import com.jme3.lostVictories.characters.blenderModels.BlenderModel;
import com.jme3.lostVictories.characters.physicsControl.GameCharacterControl;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.SquadType;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.actions.Action;
import com.jme3.lostVictories.network.messages.actions.Crouch;
import com.jme3.lostVictories.network.messages.actions.SetupWeapon;
import com.jme3.lostVictories.network.messages.actions.Shoot;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.lostVictories.objectives.reactiveObjectives.messages.ShootsFired;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.*;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public abstract class GameCharacterNode<T extends GameCharacterControl> extends Node implements Commandable, AnimEventListener, CanInteractWith, Obstacle{
    protected T playerControl;
    private ActorRef shootsFiredListener;
    protected CharacterAction characterAction = new CharacterAction();
    protected Node rootNode;
    protected Node characterNode = new Node();
    protected AnimControl control;
    protected BlenderModel model;
    protected Node geometry;

    protected ParticleEmitter muzzelFlash;
    protected ParticleEmitter smokeTrail;
    protected ParticleEmitter bloodDebris;
    protected ParticleEmitter bulletFragments;
    protected ParticleEmitter blastFragments;
    protected ParticleManager particleManager;
    protected NavigationProvider pathFinder;

    protected Country country;
    protected Geometry selectionMarker;
    protected CommandingOfficer commandingOfficer;
    protected GameAnimChannel channel;
    protected boolean isDead;

    protected BulletAppState bulletAppState;
    protected AssetManager assetManager;
    protected List<Ray> rays = new ArrayList<>();
    protected List<Vector3f> blasts = new ArrayList<>();
    protected String unitName;
    private SquadType squadType;

    int kills;
    protected final UUID identity;
    protected Geometry shell;
    protected long shootStartTime;
    private Vector3f[] currentTargets;
    GameVehicleNode boardedVehicle;
    private Vector3f initialRotation;
    private long version;

    GameCharacterNode(){
        this.identity = UUID.randomUUID();
    }

    GameCharacterNode(UUID id, Node model, Country country, CommandingOfficer commandingOfficer, Vector3f worldCoodinates, Vector3f rotation, SquadType squadType, Node rootNode, BulletAppState bulletAppState, CharcterParticleEmitter particleEmitter, ParticleManager particleManager, NavigationProvider pathFinder, AssetManager assetManager, BlenderModel m, ActorRef shootsFiredListener) {
        this.country = country;
        this.commandingOfficer = commandingOfficer;
        this.rootNode = rootNode;
        this.muzzelFlash = particleEmitter.getFlashEmitter();
        this.smokeTrail = particleEmitter.getSmokeTrailEmitter();
        this.bloodDebris = particleEmitter.getBloodEmitter();
        this.bulletFragments = particleEmitter.getBulletFragments();
        this.blastFragments = particleEmitter.getBlastFragments();
        this.particleManager = particleManager;
        this.pathFinder = pathFinder;
        this.bulletAppState = bulletAppState;       
        this.model = m;
        this.geometry = model;
        this.identity = id;
        playerControl=createCharacterControl(assetManager);
        this.shootsFiredListener = shootsFiredListener;
        this.squadType = squadType;

        if(Vector3f.ZERO.equals(rotation) || rotation.length()==0){
            rotation = Vector3f.UNIT_Z;
        }
        this.initialRotation = rotation;

        if(geometry.getControl(AnimControl.class)!=null){
            m.getWeapon().removeUnusedWeapons(geometry);
            control = geometry.getControl(AnimControl.class);
            control.addListener(this);
            channel = new GameAnimChannel(control.createChannel(), m);
            idle();
        }
                
        geometry.setLocalScale(m.getModelScale());
        geometry.setLocalTranslation(m.getModelTranslation());
        characterNode.attachChild(geometry);
        Cylinder b= new Cylinder(6, 6, .25f, 1.85f, true);
        shell = new Geometry("shell", b);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", new ColorRGBA(1, 1, 1, 0));
        mark_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        shell.setQueueBucket(Bucket.Transparent);
        shell.setMaterial(mark_mat);
        shell.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
        shell.setLocalTranslation(0, .95f, 0);

        characterNode.attachChild(shell);
        
        characterNode.attachChild(muzzelFlash);
        muzzelFlash.setLocalTranslation(m.getMuzzelLocation());

        setLocalTranslation(worldCoodinates);
        attachChild(characterNode);
        
        setUserData("GameCharacterControl", "GameCharacterControl");
        this.assetManager = assetManager;
                
    }
       
    public abstract Map<UUID, Objective> getAllObjectives();
    
    public abstract void addObjective(Objective objective);

    public abstract void cover(Vector3f mousePress, Vector3f mouseRelease, GameCharacterNode issuingCharacter);
    
    public abstract void travel(Vector3f contactPoint, GameCharacterNode issuingCharacter);
    
    public abstract void follow(GameCharacterNode toFollow, GameCharacterNode issuingCharacter);
    
    public abstract void attack(Vector3f target, GameCharacterNode issuingCharacter);
    
    public abstract void planObjectives(WorldMap worldMap);
    
    public abstract void simpleUpate(float tpf, WorldMap map, Node rootNode);

    public abstract boolean isBusy();

    abstract void doTakeBulletEffects(Vector3f point);
    
    abstract boolean takeLightBlast(GameCharacterNode shooter);
    public abstract void playDestroyAnimation(Vector3f point);
    
    public abstract Set<String> getCompletedObjectives();

    @Override
    public Vector3f getLocalTranslation() {
        if(boardedVehicle !=null){
            return boardedVehicle.getLocalTranslation();
        }
        return super.getLocalTranslation();
    }
    
    public void die(GameCharacterNode killer) {
        doDeathEffects();
        NetworkClientAppState.get().notifyDeath(killer.getIdentity(), this.getIdentity());
        if(!isAlliedWith(killer)){
            getCommandingOfficer().addEnemyActivity(killer.getLocalTranslation(), System.currentTimeMillis());
        }
    }
    
    public void decomposed() {
        rootNode.detachChild(bloodDebris);
        rootNode.detachChild(bulletFragments);
        rootNode.detachChild(smokeTrail);
        rootNode.detachChild(blastFragments);

        removeControl(playerControl);
        bulletAppState.getPhysicsSpace().remove(playerControl);
        this.removeFromParent();
    }
    
    
        
    public boolean shoot(Vector3f... targets) {
        this.shootStartTime = System.currentTimeMillis();
        this.currentTargets = new Vector3f[targets.length];
        
        for(int i =0;i<targets.length;i++){
            this.currentTargets[i] = new Vector3f(targets[i]);
        }
        Vector3f shotingLocation = getShootingLocation();
        List<Vector3f> targetDirections = new ArrayList<>();
        for(Vector3f target:targets){
            if(model.takesProjectilePath()){
                targetDirections.add(ProjectilePathPlanner.getAimingDirection(getLocalTranslation(), target));
            }else{
                targetDirections.add(target.subtract(shotingLocation));
            }
        }

        GameAnimChannel shootingChannel = getShootingChannel();
        if(!model.isAlreadyFiring(shootingChannel) && model.isReadyToShoot(shootingChannel, getAimingDirection(), targetDirections.get(0))){
            model.startFiringSequence(shootingChannel);
            fire(shotingLocation, targetDirections.toArray(new Vector3f[]{}));
            return true;
        }
        return false; 
                
    }
    
    public void attachToRootNode(){
        rootNode.attachChild(this);
        rootNode.attachChild(bloodDebris);
        rootNode.attachChild(bulletFragments);
        rootNode.attachChild(blastFragments);
        rootNode.attachChild(smokeTrail);
        bulletAppState.getPhysicsSpace().add(playerControl);
        addControl(playerControl);
        playerControl.setViewDirection(initialRotation);

    }

    public boolean isAlliedWith(GameCharacterNode character) {
        return country == character.country;
    }       
    
    public boolean canShootWhileMoving(){
        return model.canShootWithoutSetup();
    }
    
    public boolean canPlayMoveAnimation(String animationName) {
        return !animationName.contains("shootAction") 
                && !animationName.contains("aimAction") 
                && !animationName.contains("setupAction");
    }
    
    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {

        if(model.hasFinishedFiring(animName)){
            CollisionResults results = new CollisionResults();
            List<Float> collitionLifes = new ArrayList<>();
            
            try{
                for(Ray ray:rays){
                    CollisionResults r = new CollisionResults();
                    rootNode.collideWith(ray, r);
                    CollisionResult result = getClosestCollisionThatIsntMe(r);

                    if(result!=null && result.getDistance()<model.getMaxRange()){
                        results.addCollision(result);
                        collitionLifes.add(result.getDistance()/300);
                    }else{
                        collitionLifes.add(2f);
                    }
                }
            }catch(Throwable e){}
            
            if(!rays.isEmpty()){
                model.doFireEffect(smokeTrail, particleManager, this, getAimingDirection(), rays, collitionLifes);
                shootsFiredListener.tell(new ShootsFired(this, getAimingDirection()), ActorRef.noSender());
            }
            if(isControledLocaly()){
                for(CollisionResult result:results){
                    doRayDamage(result);
                }            
            }
            if(!blasts.isEmpty()) {
                for (Vector3f blast : blasts) {
                    blastFragments.killAllParticles();
                    blastFragments.setLocalTranslation(blast);
                    blastFragments.emitAllParticles();
                    doBlastDamage(blast);
                }
                blasts.clear();

                muzzelFlash.killAllParticles();
                muzzelFlash.setEnabled(false);
            }

        }

        model.transitionFireingSequence(getShootingChannel(), animName, muzzelFlash);




        
        if(model.hasPlayedSetupAction(animName)){
            model.doSetupShellAdjustment(shell);
        }
        
        if("embark_vehicle".equals(animName)){            
            decomposed();
        }
        if("disembark_vehicle".equals(animName)){
            idle();
        }

    }
    
    
    public Set<UUID> doRayDamage(CollisionResult result) {
        Set<UUID> newKills = new HashSet<UUID>();
        final Geometry tt = result.getGeometry();
        Optional<CanInteractWith> target = getInteractableTarget(tt);
        getWeapon().doDamage(this, result, target, particleManager, bulletFragments);

        return newKills;
    }

    private Optional<CanInteractWith> getInteractableTarget(Geometry tt){
        for(Node n = tt.getParent();n!=null;n = n.getParent()){
            if(n.getUserData("GameCharacterControl")!=null){
                return Optional.of((CanInteractWith) n);

            }
        }
        return Optional.empty();
    }

    public Set<UUID> doBlastDamage(Vector3f blast) {
        Set<UUID> kk = new HashSet<>();
        for(GameCharacterNode victim: WorldMap.get().getCharactersInBlastRange(blast)){
            if(victim.takeLightBlast(this)){
                kk.add(victim.identity);
            }
        }
        return kk;
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        
    }

    public boolean isDead(){
        return isDead;
    }

    public NavigationProvider getPathFinder() {
        return pathFinder;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
    
    public void setCountry(Country valueOf) {
        this.country = valueOf;
    }

    public String getUnitName() {
        return unitName;
    }
    
    public boolean isMoving() {
        return playerControl.isMoving();
    }
    
    public Weapon getWeapon(){
        return model.getWeapon();
    }
    
    public Vector3f getShootingLocation(){
        final Vector3f muzzelLocation = model.getMuzzelLocation();
        Quaternion q = new Quaternion();
        q.lookAt(getAimingDirection(), Vector3f.UNIT_Y);
        return getLocalTranslation().add(q.mult(muzzelLocation));
    }
    
    public Vector3f getShootingLocation(Vector3f facingDirection){
        return getShootingLocation(getLocalTranslation(), facingDirection);
    }

    public Vector3f getShootingLocation(Vector3f localTranslation, Vector3f facingDirection) {
        Quaternion q = new Quaternion();
        q.lookAt(facingDirection, Vector3f.UNIT_Y);
        q = q.mult(geometry.getLocalRotation());
        return localTranslation.add(q.mult(model.getMuzzelLocation()));
    }

    protected void fire(final Vector3f aimingPosition, Vector3f... aimingDirections) {
        rays.clear();

        if(model.takesProjectilePath()){
            Vector3f aimingDirection = aimingDirections[0];
            final Vector3f epicentre = new ProjectilePath(aimingPosition, aimingDirection.normalize(), 50).getEpicentre(rootNode);
            blasts.add(epicentre);
        }else{
            for(Vector3f aimingDirection:aimingDirections){
                float x = (float) ((Math.random()-0.5) * FastMath.PI / 180) * 2f;
                float z = (float) ((Math.random()-0.5) * FastMath.PI / 180) * 2f;
                aimingDirection = new Quaternion().fromAngleAxis(x, Vector3f.UNIT_Y).mult(aimingDirection);
                aimingDirection = new Quaternion().fromAngleAxis(z, Vector3f.UNIT_X).mult(aimingDirection);
                final Ray ray = new Ray(aimingPosition, aimingDirection.normalize());
                ray.setLimit(getMaxRange());
                rays.add(ray);
            }
        }               
        
    }

    public CollisionResult getClosestCollisionThatIsntMe(CollisionResults results) {
        if(results.size()<=0){
            return null;
        }
        
        for(CollisionResult r: results){
            if(!r.getGeometry().hasAncestor(this) && !hasBeenKilledAlready(r)){
                return r;
            }
        }
        
        return null;
    }

    private boolean hasBeenKilledAlready(CollisionResult r) {
        for(Node n = r.getGeometry().getParent();n!=null;n = n.getParent()){
            if(n.getUserData("GameCharacterControl")!=null && !"blank".equals(n.getUserData("GameCharacterControl"))){
                if(((GameCharacterNode) n).isDead){
                    return true;
                }
            }
        }
        return false;
        
    }
      
    public int getKillCount(){
        return kills;
    }

    public Rank getRank() {
        return Rank.PRIVATE;
    }

    public UUID getIdentity() {
        return identity;
    }
    
    public void doReverseWalkActoin(){
        model.doReverseWalkAction(channel);
    }

    public void setupWeapon(Vector3f direction) {
        model.doSetupAction(channel);
    }
    
    public Vector3f getPlayerDirection(){
        return playerControl.getViewDirection();
    }

    public boolean canShootMultipleTargets() {
        return model.canShootMultipleTargets();
    }
    
    protected boolean hasWeapon(Weapon... weapons) {
        for(Weapon w: weapons){
            if(w == getWeapon()){
                return true;
            }
        }
        return false;
    }
    
    public boolean hasProjectilePathWeapon() {
        return model.takesProjectilePath();
    }    
    
    public Country getCountry(){
        return country;
    }
    
    public GameCharacterNode select(Geometry selectionMarker) {
        this.selectionMarker = selectionMarker;
        attachChild(selectionMarker);
        return this;
    }

    public void setCommandingOfficer(CommandingOfficer c) {
        this.commandingOfficer = c;
    }
    public CommandingOfficer getCommandingOfficer(){
        return commandingOfficer;
    }
    
    public final boolean isUnderChainOfCommandOf(GameCharacterNode issuingCharacter, int maxdepth) {
        if(maxdepth<1){
            throw new RuntimeException();
        }
        if (issuingCharacter.equals(commandingOfficer)) {
            return true;
        }
        if(commandingOfficer!=null && commandingOfficer instanceof AICharacterNode){
            try {
                return ((AICharacterNode) commandingOfficer).isUnderChainOfCommandOf(issuingCharacter, --maxdepth);
            }catch(RuntimeException e){
                System.out.println("possible infinite loop checking command heierachy staring with co:"+commandingOfficer.getIdentity());
                throw e;
            }
        }
        return false;
    }
    
    public Commandable select(Commandable selectedCharacter) {
        if(this == selectedCharacter){
            return this;
        }
        if(selectedCharacter != null){
            Geometry g = selectedCharacter.unSelect();
            this.selectionMarker = g;
            attachChild(selectionMarker);
        }
        
        return this;
    }

    public Geometry unSelect() {
        detachChild(selectionMarker);
        return selectionMarker;
    }

    public Vector3f getPositionToTarget(GameCharacterNode targetedBy) {
        if(model.isStanding(channel)){
            return getLocalTranslation().add(new Vector3f(0, Soldier.SHOOTING_HEIGHT, 0));
        }else{
            return new Vector3f(getLocalTranslation());
        }
    }

    public abstract T getCharacterControl();
    protected abstract T createCharacterControl(AssetManager manager);

    public SquadType getSquadType() {
        return squadType;
    }
    
    public int getCurrentStrength(){
        int count = 1;
        if(this instanceof CommandingOfficer){
            for(Commandable c:((CommandingOfficer)this).getCharactersUnderCommand()){
                count+=c.getCurrentStrength();
            }
        }
        return count;
    }

    public float getMaxRange() {
        return model.getMaxRange();
    }

    public boolean isSelected() {
        return hasChild(selectionMarker) || getChild("subselection")!=null;
    }
    
    public Commandable getCharacterUnderCommand(String identity) {
        if(getIdentity().toString().equals(identity)){
            return this;
        }
        if(this instanceof CommandingOfficer){
            for(Commandable n:((CommandingOfficer)this).getCharactersUnderCommand()){
                if(n instanceof GameCharacterNode){
                    Commandable nn = ((GameCharacterNode)n).getCharacterUnderCommand(identity);
                    if(nn!=null){
                        return nn;
                    }
                }else if(n instanceof VirtualGameCharacterNode){
                    if(n.getIdentity().toString().equals(identity)){
                        return n;
                    }
                }
            }
        }
        return null;
    }

    public void idle() {
        channel.setAnim(model.getIdleAnimation(), LoopMode.Loop, .2f, (float) Math.random());
    }
    
    public boolean isCrouched() {
        return channel!=null && channel.getAnimationName().contains("crouchAction");
    }
    
    public boolean isFirering(){
        return model.isAlreadyFiring(channel);
    }

    public boolean isAbstracted() {
        return !characterNode.hasChild(geometry);
    }
    
    public Node getGeometry(){
        return geometry;
    }
    
    public CharacterAction getCharacyerAction(){
        return characterAction;
    }

    public CharacterMessage toMessage() {
        Set<Action> actions = getActionsToMessage();
        
        Map<String, String> objectives = new HashMap<>();
        for(Entry<UUID, Objective> entry: getAllObjectives().entrySet()){
            try {
                final ObjectNode valueToTree = entry.getValue().toJson();
                valueToTree.put("class", entry.getValue().getClass().getName());
                objectives.put(entry.getKey().toString(), MAPPER.writeValueAsString(valueToTree));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        Set<String> completedObjectives = getCompletedObjectives();

        CharacterMessage characterMessage = new CharacterMessage(identity, new Vector(getLocalTranslation()), new Vector(getPlayerDirection()), RankMessage.fromRank(getRank()), actions, objectives, completedObjectives, version);
        characterMessage.setDead(isDead);
        return characterMessage;
    }

    public Set<Action> getActionsToMessage() {
        Set<Action> actions = new HashSet<>();
        if(playerControl.isMoving()){
            actions.add(Action.move());
        }
        if(model.isAlreadyFiring(getShootingChannel())){
            actions.add(new Shoot(shootStartTime, currentTargets));
        }
        if(isCrouched()){
            actions.add(new Crouch());
        }
        if(channel!=null && model.hasPlayedSetupAction(channel.getAnimationName())){
            actions.add(new SetupWeapon());
        }
        return actions;
    }

    public long getVersion(){
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof GameCharacterNode)){
            return false;
        }
        return ((GameCharacterNode)obj).identity.equals(identity);
    }

    @Override
    public int hashCode() {
        return identity.hashCode();
    }

    public boolean isSameRank(CharacterMessage message) {
        return getRank().isSame(message.getRank());
    }
    
    public boolean hasSameWeapon(CharacterMessage cMessage) {
        return Weapon.get(cMessage.getWeapon()) == getWeapon();
    }

    public void doDeathEffects() {
        isDead = true;
        shell.removeFromParent();
        muzzelFlash.killAllParticles();
        muzzelFlash.setEnabled(false);
        playerControl.deadStop();
        
        model.doDieAction(channel);
        model.dropDetachableWeapons(geometry);

        //bulletAppState.getPhysicsSpace().remove(playerControl);
    }
    
    public void initialiseKills(int k) {
        this.kills = k;
    }

    public GameVehicleNode getBoardedVehicle() {
        return boardedVehicle;
    }
    
    public boolean hasBoardedVehicle(){
        return boardedVehicle !=null;
    }

    public void disembarkVehicle() {
        if(boardedVehicle !=null){
            boardedVehicle.disembarkPassenger(this);
            rootNode.attachChild(this);
            rootNode.attachChild(bloodDebris);
            rootNode.attachChild(bulletFragments);
            rootNode.attachChild(blastFragments);
            rootNode.attachChild(smokeTrail);

            addControl(playerControl);
            playerControl.warp(boardedVehicle.getEmbarkationPoint());
            bulletAppState.getPhysicsSpace().add(playerControl);
            final Vector3f n = boardedVehicle.getLocalTranslation().subtract(boardedVehicle.getEmbarkationPoint());
            playerControl.setViewDirection(new Vector3f(n.x, 0, n.z).normalizeLocal());
            
            boardedVehicle =null;
            if(channel!=null){
                channel.setAnimForce("disembark_vehicle", LoopMode.DontLoop);
            }
            if(getCommandingOfficer() instanceof AvatarCharacterNode){
                ((AvatarCharacterNode)getCommandingOfficer()).updateHeadsUpDisplay();
            }
        }
    }

    public Vector3f getLocation() {
        return getLocalTranslation();
    }
    
    public Vector3f getVelocity() {
        return playerControl.getMoveDirection();
    }

    void boardVehicleAction(GameVehicleNode vehicle) {
        playerControl.warp(vehicle.getEmbarkationPoint());

        if(channel!=null){
            channel.setAnim("embark_vehicle", LoopMode.DontLoop);
        }
        if(getCommandingOfficer() instanceof AvatarCharacterNode){
            ((AvatarCharacterNode)getCommandingOfficer()).updateHeadsUpDisplay();
        }
    }

    public GameCharacterNode getSupreamLeader(int i) {
        if(i>0 && getCommandingOfficer()!=null && getCommandingOfficer() instanceof GameCharacterNode){
            return ((GameCharacterNode)getCommandingOfficer()).getSupreamLeader(i-1);
        }
        return this;
    }

    public void setVersion(long version) {
        this.version = version;
    }
    
    public abstract boolean isControledLocaly();

    public abstract void  checkForNewObjectives(Map<String, String> objectives) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException ;

    public abstract boolean isControledRemotely() ;

    public abstract void setBehaviourControler(BehaviorControler remoteBehaviourControler);

    public abstract BehaviorControler getBehaviourControler();

    public Vector3f getAimingDirection() {
        return getPlayerDirection();
    }

    protected GameAnimChannel getShootingChannel() {
        return channel;
    }

    public abstract void addEnemyActivity(Vector3f localTranslation, long l);

    public abstract EnemyActivityReport getEnemyActivity();
}
