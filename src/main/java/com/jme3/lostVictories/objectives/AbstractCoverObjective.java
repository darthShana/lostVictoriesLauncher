/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
abstract class AbstractCoverObjective<T extends AICharacterNode> extends Objective<T>{
    
    Status status = Status.MOVING_IN_TO_POSITION;
    Integer countDown;
    Node rootNode;
    private Objective crouchAndShootObjective;
    Vector3f position;
    Vector3f target;
    private Integer initialCountDown;

    
    protected AbstractCoverObjective(){}

    public AbstractCoverObjective(Vector3f position, Vector3f target, Integer i, Node rootNode) {
        this.position = position;
        this.target = target;
        this.rootNode = rootNode;
        this.countDown = i;
        this.initialCountDown = i;
    }
    
    

    public AIAction doPlanObjective(T character, WorldMap worldMap, Objective<T> moveObjective) {
        if (countDown != null) {
            countDown--;
            if (countDown == 0) {
                isComplete = true;
            }
        }
        AIAction ret = null;

        if (status == Status.MOVING_IN_TO_POSITION) {
            if (moveObjective.isComplete()) {
                character.setupWeapon(target);
                status = Status.IN_POSITION;
            } else {
                ret = moveObjective.planObjective(character, worldMap);
            }
        } else if (status == Status.IN_POSITION) {
            status = Status.READY_TO_SHOOT;
            if(character instanceof Soldier && character.canShootWhileMoving()){
                if(!worldMap.getCoverInRange(character.getLocalTranslation(), 3).isEmpty()){
                    crouchAndShootObjective = new CrouchAndShoot(target);
                    ret = (AIAction) crouchAndShootObjective;
                }
            }
        } else if(status == Status.READY_TO_SHOOT){
            if(noEnemyFound(character, target, worldMap, rootNode)){
                isComplete = true;
            }else if (crouchAndShootObjective!=null){
                ret = (AIAction) crouchAndShootObjective;
            }
        }
        
        return ret;
    }
    
    private static boolean noEnemyFound(AICharacterNode character, Vector3f target, WorldMap worldMap, Node rootNode) {
        for(GameCharacterNode c:worldMap.getEnemyCharactersInDirection(character, target.subtract(character.getLocalTranslation()), character.getMaxRange())){
            if(!c.isAlliedWith(character) && worldMap.characterInRangeAndLOStoTarget(character, rootNode, c.getShootingLocation())){
                return false;
            }
        }
        return true;
    }
    
    
    @Override
    public String toString() {
        return "from:"+position+" facing:"+target;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof AbstractCoverObjective)){
            return false;
        }
        AbstractCoverObjective v = (AbstractCoverObjective) obj;
        return position.equals(v.position) && target.equals(v.target);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.position != null ? this.position.hashCode() : 0);
        hash = 47 * hash + (this.target != null ? this.target.hashCode() : 0);
        return hash;
    }
    
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("position", MAPPER.valueToTree(new Vector(position)));
        node.put("target", MAPPER.valueToTree(new Vector(target)));
        node.put("countDown", countDown);
        return node;
    }
    
    enum Status {
        MOVING_IN_TO_POSITION, IN_POSITION, READY_TO_SHOOT
    }
    
}
