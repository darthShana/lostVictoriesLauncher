/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.scene.Node;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author dharshanar
 */
public interface BehaviorControler {

    public void doActions(AICharacterNode aThis, Node rootNode, GameAnimChannel channel, float tpf);

    public void addObjective(Objective o);
    
    public void planObjectives(GameCharacterNode character, WorldMap worldMap);

    public void addObjectivesFromRemoteCharacters(Map<String, String> objectives, GameCharacterNode character, NavigationProvider pathfinder, Node rootNode, WorldMap map) throws IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, InstantiationException, IllegalArgumentException, ClassNotFoundException, IOException;

    public Set<Objective> getAllObjectives();

    public boolean isBusy();

    public boolean isAttacking();
    
}
