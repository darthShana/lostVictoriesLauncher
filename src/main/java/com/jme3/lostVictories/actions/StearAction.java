/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.characters.physicsControl.BetterVehicleControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author dharshanar
 */
public class StearAction implements AIAction<GameVehicleNode> {
    private final Vector3f target;
    private boolean isComplete;
    StearingDirection stearingDirection;
    
    public StearAction(Vector3f target) {
        this.target = target;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public boolean doAction(GameVehicleNode character, Node rootNode, GameAnimChannel channel, float tpf) {
        BetterVehicleControl controle = character.getCharacterControl();
        Vector3f vehicleDirection = controle.getForwardVector(null).normalize();
        Vector3f pathStep = target.subtract(character.getLocalTranslation()).normalize();
        Vector2f currentDirection = new Vector2f(vehicleDirection.x, vehicleDirection.z);
        Vector2f requiredDirection = new Vector2f(pathStep.x, pathStep.z);
        final float angleBetween = currentDirection.smallestAngleBetween(requiredDirection);
        
        if(!character.canShootWhileMoving() && character.isFirering()){
            return true;
        }
        
        if(angleBetween> FastMath.QUARTER_PI/8f){
            Vector3f v1 = DriveAction.qLeft.mult(vehicleDirection);
            Vector3f v2 = DriveAction.qRight.mult(vehicleDirection);
            if(requiredDirection.smallestAngleBetween(new Vector2f(v1.x, v1.z))>requiredDirection.smallestAngleBetween(new Vector2f(v2.x, v2.z))){
                
                if(stearingDirection==null){
                    character.turnRight();
                    stearingDirection = StearingDirection.RIGHT;
                    controle.steer(-character.getMaxStearingAngle()); 
                }else if(stearingDirection!= StearingDirection.RIGHT){
                    return completeStearing(controle);
                }
            }else{
                if(stearingDirection==null){
                    character.turnLeft(); 
                    stearingDirection = StearingDirection.LEFT;
                    controle.steer(character.getMaxStearingAngle());
                }else if(stearingDirection!= StearingDirection.LEFT){
                    return completeStearing(controle);
                }
                
            }
            
            
            if(controle.getCurrentVehicleSpeedKmHour()<character.getTurnSpeed()){
                controle.accelerate(character.getEnginePower());
            }else{              
                controle.accelerate(0);
                controle.brake(50);
            }
            
            return false;
        }else{
            
            return completeStearing(controle);
        }

    }

    public boolean completeStearing(BetterVehicleControl controle) {
        controle.steer(0);
        controle.accelerate(0);
        controle.brake(2000);
        isComplete = true;
        return true;
    }

    private static enum StearingDirection {
        RIGHT, LEFT


    }
    
}
