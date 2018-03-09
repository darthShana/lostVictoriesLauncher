/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AutoDriveAction;
import com.jme3.lostVictories.characters.physicsControl.BetterVehicleControl;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import static com.jme3.lostVictories.actions.DriveAction.qLeft;
import static com.jme3.lostVictories.actions.DriveAction.qRight;

/**
 *
 * @author dharshanar
 */
class RemoteVehicleControl {

    void handleLocationUpdates(Vector3f requiredPossition, Vector3f requiredOrientation, GameVehicleNode character, Node rootNode, GameAnimChannel channel, CharacterMessage message) {
        final Vector3f localTranslation = character.getLocalTranslation();
        BetterVehicleControl playerControl = character.getCharacterControl();
        
        if(!WorldMap.isClose(localTranslation, requiredPossition, 7)){
            playerControl.warp(requiredPossition);
            return;
        }

        Vector2f aa = new Vector2f(playerControl.getViewDirection().x, playerControl.getViewDirection().z);
        Vector2f bb = new Vector2f(requiredOrientation.x, requiredOrientation.z);
        if(aa.smallestAngleBetween(bb)>FastMath.QUARTER_PI){
            playerControl.setViewDirection(requiredOrientation);
        }
        
        if(message.isMoving()!=null || !WorldMap.isClose(localTranslation, requiredPossition, 1)){
            float ep = character.getEnginePower()*(localTranslation.distance(requiredPossition));

            final Vector3f[] reverseTrianlge = AutoDriveAction.getReverseTrianlge(localTranslation, playerControl.getViewDirection());
            if(AutoDriveAction.isInTriangle(requiredPossition, localTranslation, reverseTrianlge[0], reverseTrianlge[1])){
                character.straighten();
                playerControl.steer(0);
                playerControl.accelerate(-ep);
            }else{

//                System.out.println("in here:"+message.getId()+" remote vehicle move distance:"+localTranslation.distance(requiredPossition));

                Vector3f requiredDirection = requiredPossition.subtract(localTranslation);
                float cc = aa.smallestAngleBetween(new Vector2f(requiredDirection.x, requiredDirection.z));
                float stearing = (.5f/FastMath.QUARTER_PI)*cc;
                Vector3f v1 = qLeft.mult(playerControl.getViewDirection());
                Vector3f v2 = qRight.mult(playerControl.getViewDirection());
                if(new Vector2f(requiredDirection.x, requiredDirection.z).smallestAngleBetween(new Vector2f(v1.x, v1.z))>new Vector2f(requiredDirection.x, requiredDirection.z).smallestAngleBetween(new Vector2f(v2.x, v2.z))){
                    playerControl.steer(-stearing);
                    character.turnRight();
                }else{
                    playerControl.steer(stearing);
                    character.turnLeft();
                }
                playerControl.gas(ep);
            }
        }else{
            playerControl.neutral();
            character.stop();
            return;
        }
        

                
        
                
                
            
            
        

        
    }
    
}
