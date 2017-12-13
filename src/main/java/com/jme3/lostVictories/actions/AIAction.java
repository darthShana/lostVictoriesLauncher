/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.scene.Node;

/**
 *
 * @author dharshanar
 */
public interface AIAction<T extends GameCharacterNode> {

    public boolean doAction(T aThis, Node rootNode, GameAnimChannel channel, float tpf);
    
}
