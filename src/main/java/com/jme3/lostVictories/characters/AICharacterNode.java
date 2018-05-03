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
import com.jme3.collision.CollisionResults;
import com.jme3.lostVictories.CharcterParticleEmitter;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.ShootTargetAction;
import com.jme3.lostVictories.characters.blenderModels.BlenderModel;
import com.jme3.lostVictories.characters.physicsControl.GameCharacterControl;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.network.messages.SquadType;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 *
 * @author dharshanar
 */
public abstract class AICharacterNode<T extends GameCharacterControl> extends GameCharacterNode<T> {

    List<Geometry> boxes = new ArrayList<Geometry>();
    protected BehaviorControler behaviorControler;
    private List<Vector3f> path;
    private boolean pathPlotted;
    private EnemyActivityReport enemyActivity = new EnemyActivityReport();

    AICharacterNode(){}

    public AICharacterNode(UUID id, Node model, Country country, CommandingOfficer commandingOfficer, Vector3f worldCoodinates, Vector3f rotation, SquadType squadType, Node rootNode, BulletAppState bulletAppState, CharcterParticleEmitter emitter, ParticleManager particleManager, NavigationProvider pathFinder, AssetManager assetManager, BlenderModel m, BehaviorControler behaviorControler, ActorRef shootssFiredListener) {
        super(id, model, country, commandingOfficer, worldCoodinates, rotation, squadType, rootNode, bulletAppState, emitter, particleManager, pathFinder, assetManager, m, shootssFiredListener);
        this.behaviorControler = behaviorControler;

    }
    
    public void simpleUpate(float tpf, WorldMap map, Node rootNode) {
        behaviorControler.doActions(this, rootNode, channel, tpf);
        if(getLocalTranslation().y<-100){
            die(this);
        }

        if(path!=null && !pathPlotted){
            pathPlotted = true;
            plotPath(path);
        }
            
//          if("ce0e6166-7299-4222-9f1a-938cdc9b24cb".equals(getIdentity().toString())){
//
//              for(Geometry g: boxes){
//                rootNode.detachChild(g);
//            }
//            boxes.clear();
//            Vector3f p = getLocalTranslation();
//            Vector3f v1 = getAimingDirection();
//            
//            Vector3f p2 = p.add(v1.mult(5));
//            rootNode.attachChild(getBox(5, p2.x-2.5f, p2.z-2.5f));
//            
//            Vector3f p3 = p.add(v1.mult(15));
//            rootNode.attachChild(getBox(7, (int)p3.x-3.5f, (int)p3.z-3.5f));
//            
//            Vector3f p4 = p.add(v1.mult(30));
//            rootNode.attachChild(getBox(11, (int)p4.x-5.5f, (int)p4.z-5.5f));
//            
//            Vector3f p5 = p.add(v1.mult(50));
//            rootNode.attachChild(getBox(17, (int)p5.x-8.5f, (int)p5.z-8.5f));
//            
//            Vector3f p6 = p.add(v1.mult(75));
//            rootNode.attachChild(getBox(25, (int)p6.x-12.5f, (int)p6.z-12.5f));
//            
//            Vector3f p7 = p.add(v1.mult(105));
//            rootNode.attachChild(getBox(35, (int)p7.x-17.5f, (int)p7.z-17.5f));
//        }
//        
    }

    public abstract  void travel(Vector3f contactPoint, GameCharacterNode issuingCharacter);

    public abstract void attack(Vector3f target, GameCharacterNode issuingCharacter);
    
    public abstract void cover(Vector3f mousePress, Vector3f mouseRelease, GameCharacterNode issuingCharacter);
    
    public boolean isAlliedWith(Country c) {
        return this.country == c;
    }
        public BehaviorControler getBehaviourControler() {
        return behaviorControler;
    }

        
    public void checkForNewObjectives(Map<String, String> objectives) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        behaviorControler.addObjectivesFromRemoteCharacters(objectives, this, pathFinder, rootNode, WorldMap.get());
    }

    public void setBehaviourControler(BehaviorControler behaviorControler) {
        this.behaviorControler = behaviorControler;
    }    

    public boolean isControledRemotely() {
        return behaviorControler instanceof RemoteBehaviourControler;
    }

    public boolean isControledLocaly() {
        return behaviorControler instanceof LocalAIBehaviourControler;
    }

    @Override
    public Set<String> getCompletedObjectives() {
        Set<String> completedObjectives = new HashSet<String>();
        if(behaviorControler instanceof LocalAIBehaviourControler){
            completedObjectives = ((LocalAIBehaviourControler)behaviorControler).getCompletedObjectives();
        }
        return completedObjectives;
    }

    @Override
    public boolean isBusy() {
        return behaviorControler.isBusy();
    }

    public boolean isAttacking() {
        return behaviorControler.isAttacking();
    }
    
    
    
    @Override
    public Map<UUID, Objective> getAllObjectives() {
        Map<UUID, Objective> ret = new HashMap<UUID, Objective>();
        
        for(Objective o: behaviorControler.getAllObjectives()){
            ret.put(o.getIdentity(), o);
        }
        
        return ret;
    }
       
    public Geometry getBox(float size, float x, float y, float z) {
        Box b= new Box(size, 1, size);
        Geometry mark = new Geometry("selected", b);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.White);
        mark.setMaterial(mark_mat);
        mark.setLocalTranslation(new Vector3f(x, y, z));
        boxes.add(mark);
        return mark;
    }

    public boolean isReadyToShoot(Vector3f targetDirection) {
        return model.isReadyToShoot(channel, getAimingDirection(), targetDirection);
    }

    public Set<GameVehicleNode> getVehicles() {
        HashSet<GameVehicleNode> ret = new HashSet<GameVehicleNode>();
        if(this instanceof CommandingOfficer){
            for(Commandable n: ((CommandingOfficer)this).getCharactersUnderCommand()){
                if(n instanceof GameVehicleNode){
                    ret.add((GameVehicleNode) n);
                }
            }
        }
        return ret;
    }

    @Override
    public void planObjectives(WorldMap worldMap) {
        behaviorControler.planObjectives(this, worldMap);
    }

    @Override
    public void addObjective(Objective objectve) {
        behaviorControler.addObjective(objectve);
    }

    @Override
    public void addEnemyActivity(Vector3f localTranslation, long l) {
        enemyActivity.put(localTranslation, l);
    }

    @Override
    public EnemyActivityReport getEnemyActivity() {
        EnemyActivityReport e = new EnemyActivityReport();
        e.merge(enemyActivity);
        if(this instanceof CommandingOfficer){
            ((CommandingOfficer)this).getCharactersUnderCommand().forEach(unit->{
                e.merge(unit.getEnemyActivity());
            });
        }
        return e;
    }

    boolean hasSetupWeapon() {
        return model.hasPlayedSetupAction(channel.getAnimationName());
    }

    public boolean hasClearLOSTo(GameCharacterNode target) {
        final Vector3f aimingPosition = getShootingLocation();
        Vector3f localTranslation = target.getPositionToTarget(this);
        final Vector3f aimingDirection = localTranslation.subtract(aimingPosition).normalizeLocal();
        Ray ray = new Ray(aimingPosition, aimingDirection);
        ray.setLimit(getMaxRange());
        CollisionResults results = new CollisionResults();
        try{
            rootNode.collideWith(ray, results);
            CollisionResult collision = null;
            for(Iterator<CollisionResult> it = results.iterator();it.hasNext();){
                CollisionResult c = it.next();
                if(!hasChild(c.getGeometry())){
                    collision = c;
                    break;
                }
            }

            if(collision!=null && target.hasChild(collision.getGeometry())){                
                return true;
            }
        }catch(Throwable e){}
        return false;
    }


    private void plotPath(List<Vector3f> waypoints){
        for(Geometry g: boxes){
            rootNode.detachChild(g);
        }
        boxes.clear();
        float i =.5f;
        for(Vector3f p:waypoints){
            rootNode.attachChild(getBox(i, p.x, p.y, p.z));
        }
    }

    public void showPath(List<Vector3f> path) {
        this.path = path;
    }

    public void doAction(ShootTargetAction shootTargetAction){
        behaviorControler.addAction(shootTargetAction);
    }

//    public void plotpoints(Collection<Vector3f> values) {
//        values.forEach(p->rootNode.attachChild(getBox(1, p.x, p.y, p.z)));
//    }
}
