/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.asset.AssetManager;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.minimap.MinimapNode;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;

import java.io.IOException;
import java.util.UUID;


/**
 *
 * @author dharshanar
 */
public abstract class Objective<T extends GameCharacterNode> {
    private UUID identity = UUID.randomUUID();
    protected boolean isComplete;
    
    public UUID getIdentity(){
        return identity;
    }
    
    public void setIdentity(UUID identity){
        this.identity = identity;
    }
    
    protected Node getCircle(AssetManager assetManager, ColorRGBA color, float radius) {
        Cylinder b = new Cylinder(12, 12, radius, .5f, true, false);
        
        Geometry clone = new Geometry("selected", b);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", color);
        clone.setMaterial(mark_mat);
        final Node node = new Node();
        node.attachChild(clone);
        node.setLocalRotation(MinimapNode.x_rot);
        return node;
    }
    
    public boolean isComplete() {
        return isComplete;
    }

    public void completeObjective() {
        isComplete = true;
    }

    abstract public AIAction planObjective(T character, WorldMap worldMap);

    abstract public boolean clashesWith(Objective objective);

    abstract public ObjectNode toJson();
    
    abstract public Objective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException;
    
}
