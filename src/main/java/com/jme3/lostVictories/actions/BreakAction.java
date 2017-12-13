/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.scene.Node;

/**
 *
 * @author dharshanar
 */
public class BreakAction implements AIAction<GameVehicleNode> {

    public BreakAction() {
    }

    public boolean doAction(GameVehicleNode aThis, Node rootNode, GameAnimChannel channel, float tpf) {
        aThis.getCharacterControl().deadStop();
        aThis.stop();
        return true;
    }
    
}
