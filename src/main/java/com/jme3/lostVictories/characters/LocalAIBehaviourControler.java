/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.fasterxml.jackson.databind.JsonNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.actions.ShootTargetAction;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.lostVictories.objectives.Objectives;
import com.jme3.lostVictories.objectives.SecureSector;
import com.jme3.scene.Node;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class LocalAIBehaviourControler implements BehaviorControler{

    PriorityBlockingQueue<AIAction> aiActions = new PriorityBlockingQueue<>(1, new ActionComparator());
    //have only one action of a type
    Objectives objectives = new Objectives<>();
    private final Set<UUID> receivedObjectives = new HashSet<>();
    private boolean shootTargetAdded;

    @Override
    public void doActions(AICharacterNode character, Node rootNode, GameAnimChannel channel, float tpf) {
        if(character.getBoardedVehicle()!=null){
            return;
        }
        List<AIAction> toReadd = new ArrayList();
        AIAction action = aiActions.poll();
        if(action!=null){
            if(!action.doAction(character, rootNode, channel, tpf)){
                toReadd.add(action);
            }
        }
        
        action = aiActions.poll();
        if(action!=null){
            if(!action.doAction(character, rootNode, channel, tpf)){
                toReadd.add(action);
            }
        }
        aiActions.addAll(toReadd);
        
    }

    @Override
    public void planObjectives(GameCharacterNode character, WorldMap worldMap) {
        aiActions.clear();
        aiActions.addAll(objectives.planObjectives(character, worldMap));
        shootTargetAdded = false;
    }

    @Override
    public void addAction(AIAction action) {
        if(action instanceof ShootTargetAction){
            if(shootTargetAdded){
                return;
            }
            shootTargetAdded = true;
        }
        aiActions.add(action);
    }

    public void addObjective(Objective o) {
        objectives.addObjective(o);   
    }
    
    public Set<String> getCompletedObjectives(){
        return objectives.getCompletedObjectives();
    }
    
    public void addObjectivesFromRemoteCharacters(Map<String, String> objectives, GameCharacterNode character, NavigationProvider pathfinder, Node rootNode, WorldMap map) throws IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, InstantiationException, IllegalArgumentException, ClassNotFoundException, IOException {
        //ThreadSafeRootNodeWrapper
        for(Map.Entry<String, String> e: objectives.entrySet()){
            UUID o = UUID.fromString(e.getKey());
            if(!receivedObjectives.contains(o) && !character.getAllObjectives().containsKey(o) && !getCompletedObjectives().contains(o.toString())){
                final JsonNode readTree = MAPPER.readTree(e.getValue());
                Class oClass = Class.forName(readTree.get("class").asText());
                if(oClass == SecureSector.class){
                    System.out.println("adding secure sector to:"+character.getIdentity()+" rank:"+character.getClass()+":"+character.getRank());
                }
                Constructor<Objective> constructor = oClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Objective temp = constructor.newInstance();
                Objective real = null;
                try{
                    real = temp.fromJson(readTree, character, pathfinder, rootNode, map);
                }catch(Throwable ex){
                    ex.printStackTrace();
                }
                if(real!=null){
                    real.setIdentity(o);
                    character.addObjective(real);
                    receivedObjectives.add(o);
                }
                
            }
        }
    }

    public Set<Objective> getAllObjectives() {
        return objectives.getAllObjectives();
    }

    public boolean isBusy() {
        return objectives.isBusy();
    }

    public boolean isAttacking() {
        return objectives.isAttacking();
    }
    
    
    
}
