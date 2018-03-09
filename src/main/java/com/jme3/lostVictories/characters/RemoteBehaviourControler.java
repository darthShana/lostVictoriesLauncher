/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.asset.AssetManager;
import com.jme3.lostVictories.NetworkClientAppState;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.actions.StopAction;
import com.jme3.lostVictories.actions.TeleportAction;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.actions.Shoot;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 *
 * @author dharshanar
 */
public class RemoteBehaviourControler implements BehaviorControler {
    
    public static ObjectMapper MAPPER;
    static{
            MAPPER = new ObjectMapper();
            MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
            MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    }
    
    private CharacterMessage remoteState;
    private long shootCommandTime;    
    private final UUID characterID;
    private Geometry reqPosBox;
    RemoteVehicleControl vehicleControle = new RemoteVehicleControl();
//    private ManualControlByAvatar manulaDriveObjective;
    public WorldMap worldMap;

    public RemoteBehaviourControler(UUID characterID, CharacterMessage remoteState) {
        this.characterID = characterID;
        this.remoteState = remoteState;
    }
    
    public void updateRemoteState(CharacterMessage remoteState){
        this.remoteState = remoteState;
    
    }

    public void doActions(AICharacterNode character, Node rootNode, GameAnimChannel channel, float tpf) {
        if(remoteState==null){
            return;
        }
        final Vector3f requiredPossition = new Vector3f(remoteState.getLocation().x, remoteState.getLocation().y, remoteState.getLocation().z);
        final Vector3f requiredOrientation = new Vector3f(remoteState.getOrientation().x, remoteState.getOrientation().y, remoteState.getOrientation().z);
        
        if(remoteState.getBoardedVehicle()!=null){
            return;
        }
       
        if(remoteState.isDead() && !character.isDead){
            character.doDeathEffects();
        }
        final boolean hasSetupWeapon = remoteState.hasSetupWeapon();
        
        Shoot shooting = remoteState.isShooting();
        if(shooting!=null){
            if(shootCommandTime<shooting.getTime()){
                if("d993932f-a185-4a6f-8d86-4ef6e2c5ff95".equals(remoteState.getId().toString())){
                    System.out.println("in here remote shoot delay:"+(System.currentTimeMillis()-shooting.getTime()));
                }
                shootCommandTime = shooting.getTime();
                character.shoot(shooting.getTargets());
            }
        }else if(hasSetupWeapon && !character.hasSetupWeapon()){
            character.setupWeapon(remoteState.getOrientation().toVector());
        }
        
        if(character instanceof GameVehicleNode){
            vehicleControle.handleLocationUpdates(requiredPossition, requiredOrientation, (GameVehicleNode) character, rootNode, channel, remoteState);
            if(character instanceof HalfTrackNode && remoteState.hasEngineDamage() && !((HalfTrackNode)character).hasEngineDamage()){
                ((HalfTrackNode)character).doEngineDamage();
            }
        }else{
            if(remoteState.isMoving()!=null || !WorldMap.isClose(character.getLocalTranslation(), requiredPossition, .5)){
                TeleportAction mtp = new TeleportAction(requiredPossition);
                mtp.doAction(character, rootNode, channel, tpf);
            } else if(new Vector2f(character.getPlayerDirection().x, character.getPlayerDirection().z).smallestAngleBetween(new Vector2f(requiredOrientation.x, requiredOrientation.z))> FastMath.QUARTER_PI){
                character.getCharacterControl().setViewDirection(requiredOrientation);
            } else if(character.isMoving()){

                StopAction stop = new StopAction(requiredOrientation);
                stop.doAction(character, rootNode, channel, tpf);
            }
        
        }
        
        if(remoteState.isCrouching() && !character.isCrouched() && character instanceof Soldier){
            if("d993932f-a185-4a6f-8d86-4ef6e2c5ff95".equals(character.getIdentity().toString())){
                System.err.println("avatar crouch detected");
            }
            ((Soldier)character).crouch();
        }else if(!remoteState.isCrouching() && character.isCrouched() && character instanceof Soldier){
            ((Soldier)character).stand();
        }
        
        
    }

    @Override
    public void addAction(AIAction action) {

    }

    public Geometry getBox(AssetManager assetManager, float x, float y, float z) {
        Box b= new Box(.5f, .5f, .5f);
        Geometry mark = new Geometry("selected", b);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.White);
        mark.setMaterial(mark_mat);
        mark.setLocalTranslation(new Vector3f(x, y, z));
        
        return mark;
    }
    
    public void addObjective(Objective o) {
        final ObjectNode valueToTree = o.toJson();
        valueToTree.put("class", o.getClass().getName());
        try {
            NetworkClientAppState.get().addObjective(characterID, o.getIdentity(), MAPPER.writeValueAsString(valueToTree));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addObjectivesFromRemoteCharacters(Map<String, String> objectives, GameCharacterNode character, NavigationProvider pathfinder, Node rootNode, WorldMap map) throws IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, InstantiationException, IllegalArgumentException, ClassNotFoundException, IOException {
        
    }


    

    public void planObjectives(GameCharacterNode character, WorldMap worldMap) {
        
    }

    public Set<Objective> getAllObjectives() {
        return new HashSet<Objective>();
    }

    public boolean isBusy() {
        return !remoteState.getObjectives().isEmpty();
    }

    public boolean isAttacking() {
        return !remoteState.getObjectives().isEmpty();
    }

    public WorldMap getWorldMap() {
        if(worldMap==null){
            worldMap = WorldMap.get();
        }
        return worldMap;
    }
    
    
    
}
