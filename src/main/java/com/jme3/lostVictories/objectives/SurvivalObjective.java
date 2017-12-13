/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.ShotsFiredListener;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.actions.BombardTargetsAction;
import com.jme3.lostVictories.actions.ShootTargetAction;
import com.jme3.lostVictories.actions.ShootTargetsAction;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
class SurvivalObjective extends Objective<AICharacterNode> implements PassiveObjective{
    
    
    SurvivalObjective() {}

    @Override
    public AIAction planObjective(AICharacterNode character, WorldMap worldMap) {

        List<GameCharacterNode> inRange = worldMap.getCharactersInAutoAttackRange(character);
        for(GameCharacterNode other: inRange){
            if(!other.getCountry().isAlliedWith(character)){
                if(character.hasClearLOSTo(other)){
                    return new ShootTargetAction(other);
                }
            }
        }
        final Vector3f localTranslation = character.getLocalTranslation();
                
        Set<Vector3f> boogies = new HashSet<>();
        Set<Vector3f> vehicleBoogies = new HashSet<>();
        List<Vector3f> directionsToLook = new ArrayList<>();
        directionsToLook.add(character.getAimingDirection());
        ShotsFiredListener.instance().getShootsFiredInRange(localTranslation, (int)character.getMaxRange(), 5).forEach(v->directionsToLook.add(v));
        
        AIAction shootAction = null;

        for(Vector3f shot:directionsToLook){
            for(GameCharacterNode other:worldMap.getEnemyCharactersInDirection(character, shot, character.getWeapon().getMaxRange())){
                if(character.hasClearLOSTo(other)){
                    shootAction = new ShootTargetAction(other);
                }
                if(other instanceof GameVehicleNode){
                    vehicleBoogies.add(other.getLocalTranslation());
                }else{
                    boogies.add(other.getLocalTranslation());
                }                
            }
        }
        if(!boogies.isEmpty() || !vehicleBoogies.isEmpty()){
            character.reportEnemyActivity(boogies, vehicleBoogies);                   
        }else{
            character.clearEnemyActivity();
        }           
        if(shootAction!=null && !character.canShootMultipleTargets() && !character.hasProjectilePathWeapon()){
            if(character.isFirering()){
                return null;
            }else{
                return shootAction;
            }
        }
        
        boolean debug = false;
        final List<GameCharacterNode> charactersInDirection = worldMap.getEnemyCharactersInDirection(character, character.getAimingDirection(), character.getWeapon().getMaxRange(), debug);
        Set<GameCharacterNode> confirmedTargets = new HashSet<GameCharacterNode>();

//        if("16a67c6b-263b-4d74-a7a8-b6d28f014d28".equals(character.getIdentity().toString())){
//            if(!charactersInDirection.isEmpty()){
//                System.out.println("found potencial targets:"+charactersInDirection.size());
//            }
//        }
        
        for (GameCharacterNode target: charactersInDirection){
//            if("16a67c6b-263b-4d74-a7a8-b6d28f014d28".equals(character.getIdentity().toString())){
//                System.out.println("1:"+character.isReadyToShoot(target.getLocalTranslation().subtract(character.getLocalTranslation())));
//                System.out.println("2:"+character.hasClearLOSTo(target));
//                System.out.println("3:"+(character.getLocalTranslation().distance(target.getLocalTranslation())<character.getMaxRange()));
//            }
            if(character.isReadyToShoot(target.getLocalTranslation().subtract(character.getLocalTranslation())) 
                    && character.hasClearLOSTo(target)
                    && character.getLocalTranslation().distance(target.getLocalTranslation())<character.getMaxRange()){
                confirmedTargets.add(target);
            }
        }
        
//        if("16a67c6b-263b-4d74-a7a8-b6d28f014d28".equals(character.getIdentity().toString())){
//            if(!confirmedTargets.isEmpty()){
//                System.out.println("found confirmed targets:"+confirmedTargets.size());
//            }
//        }
        if(!confirmedTargets.isEmpty()){
            if (character.hasProjectilePathWeapon()) {
                return new BombardTargetsAction(confirmedTargets);
            } else {
                return new ShootTargetsAction(confirmedTargets);
            }
        }
                
        return null;
    }

    public boolean clashesWith(Objective objective) {
        return false;
    }
    
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        return node;
    }

    @Override
    public SurvivalObjective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        return new SurvivalObjective();
    }

    
    
}
