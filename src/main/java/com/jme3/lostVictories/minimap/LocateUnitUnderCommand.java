/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.minimap;

import com.jme3.asset.AssetManager;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dharshanar
 */
class LocateUnitUnderCommand implements MinimapPresentable {
    private final Commandable commandable;

    public LocateUnitUnderCommand(Commandable commandable) {
        this.commandable = commandable;
    }

    public Vector3f getObjectiveLocation() {
        return commandable.getLocalTranslation();
    }

    public List<String> getInstructions() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("Your unit is shown on the minimap");
        return arrayList;
    }

    public boolean updatedStatus(AvatarCharacterNode avatar) {
        if(avatar.getCurrentObjectives()!=null && avatar.getCurrentObjectives() instanceof MinimapPresentable){
            return ((MinimapPresentable) avatar.getCurrentObjectives()).updatedStatus(avatar);
        }
        return true;
    }

    public Node getShape(AssetManager assetManager, GameCharacterNode c) {
        return getCircle(assetManager, ColorRGBA.Blue, 15);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }
        if(!(obj instanceof LocateUnitUnderCommand)){
            return false;
        }
        return ((LocateUnitUnderCommand)obj).commandable.equals(commandable);
    }

    @Override
    public int hashCode() {
        return commandable.getIdentity().hashCode();
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
    
}
