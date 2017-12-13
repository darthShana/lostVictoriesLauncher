/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.minimap;

import com.jme3.asset.AssetManager;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.List;

/**
 *
 * @author dharshanar
 */
public interface MinimapPresentable {

    public Vector3f getObjectiveLocation();

    public List<String> getInstructions();

    public boolean updatedStatus(AvatarCharacterNode avatar);

    public Node getShape(AssetManager assetManager, GameCharacterNode c);
    
    
    
}
