/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.Set;

/**
 *
 * @author dharshanar
 */
public class ShootTargetsAction implements AIAction<AICharacterNode> {
    private final Set<GameCharacterNode> targets;
    Vector3f[] shoots = null;

    public ShootTargetsAction(Set<GameCharacterNode> targets) {
        this.targets = targets;
    }



    public boolean doAction(AICharacterNode character, Node rootNode, GameAnimChannel channel, float tpf) {
        if(shoots!=null){
            character.shoot(shoots);
            return false;
        }
        
        if(targets.isEmpty()){
            return true;
        }
        final Vector3f initial = targets.iterator().next().getPositionToTarget(character);
                
        Vector3f clock = initial;
        Vector3f counterClock = initial;
        
        Vector2f shooter = new Vector2f(character.getLocalTranslation().x, -character.getLocalTranslation().z);
        
        for (GameCharacterNode tt: targets){
            if(isMoreClockwise(shooter, new Vector2f(clock.x, -clock.z), new Vector2f(tt.getLocalTranslation().x, -tt.getLocalTranslation().z))){
                clock = tt.getPositionToTarget(character);
            }
            if(isMoreAntiClockwise(shooter, new Vector2f(counterClock.x, -counterClock.z), new Vector2f(tt.getLocalTranslation().x, -tt.getLocalTranslation().z))){
                counterClock = tt.getPositionToTarget(character);
            }

            
        }
        shoots = calculateSpread(character.getLocalTranslation(), counterClock, clock);
        character.shoot(shoots);
        return false;
    }

    boolean isMoreClockwise(Vector2f shooter, Vector2f b, Vector2f c) {
        return FastMath.counterClockwise(shooter, b, c)!=1;
    }

    boolean isMoreAntiClockwise(Vector2f shooter, Vector2f b, Vector2f c) {
        return FastMath.counterClockwise(shooter, b, c)==1;
    }

    public static Vector3f[] calculateSpread(Vector3f source, Vector3f counterClock, Vector3f clock) {
        Vector3f[] shoots = new Vector3f[5];
        final Vector3f cc = counterClock.subtract(source);
        final Vector3f c = clock.subtract(source);
        shoots[0] = counterClock;
        shoots[1] = source.add(FastMath.interpolateLinear(0.25f, cc, c));
        shoots[2] = source.add(FastMath.interpolateLinear(0.5f, cc, c));
        shoots[3] = source.add(FastMath.interpolateLinear(0.75f, cc, c));
        shoots[4] = clock;
        return shoots;
    }
    
}
