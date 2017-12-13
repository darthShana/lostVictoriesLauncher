/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author dharshanar
 */
public class ShootPointAction implements AIAction<GameCharacterNode>{
    private final Vector3f target;
    private boolean turnRequired;

    public ShootPointAction(Vector3f target, boolean turnRequired) {
        this.target = target;
        this.turnRequired = turnRequired;
    }

    
    
    public boolean doAction(GameCharacterNode aThis, Node rootNode, GameAnimChannel channel, float tpf) {
        if(turnRequired) {
            aThis.getCharacterControl().setViewDirection(target.subtract(aThis.getLocalTranslation()).normalizeLocal());
        }
        if(aThis.canShootMultipleTargets()){
            List<Vector3f> collect = IntStream.of(4).mapToObj(i -> new Vector3f(target)).collect(Collectors.toList());
            aThis.shoot(collect.toArray(new Vector3f[]{}));
        }
        aThis.shoot(target);
        return true;
    }
    
}
