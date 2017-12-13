/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.ai.steering.Obstacle;
import com.jme3.ai.steering.behaviour.ObstacleAvoid;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.MoveMode;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dharshanar
 */
public class MoveAction implements AIAction<Soldier> {
    public static Quaternion bigQ1 = new Quaternion().fromAngleAxis(FastMath.TWO_PI*0.016f, Vector3f.UNIT_Y);
    public static Quaternion bigQ2 = new Quaternion().fromAngleAxis(-FastMath.TWO_PI*0.016f, Vector3f.UNIT_Y);
    public static Quaternion lilQ1 = new Quaternion().fromAngleAxis(FastMath.QUARTER_PI*0.016f, Vector3f.UNIT_Y);
    public static Quaternion lilQ2 = new Quaternion().fromAngleAxis(-FastMath.QUARTER_PI*0.016f, Vector3f.UNIT_Y);
    
    List<Vector3f> waypoints = new ArrayList<>();
    private final Vector3f destination;
    private boolean avoidTraffic = true;
    private final Vector3f facePoint;
    private ObstacleAvoid abstacleAvoidance = new ObstacleAvoid();

    public MoveAction(Soldier c, List<Vector3f> path, Vector3f destination, boolean avoidTraffic, Vector3f facePoint) {
        waypoints.addAll(path);
        this.destination = destination;
        this.avoidTraffic = avoidTraffic;
        this.facePoint = facePoint;
    }
    
    public MoveAction(Soldier c, List<Vector3f> path, Vector3f destination, Vector3f facePoint) {
        this.waypoints.addAll(path);
        this.destination = destination;
        this.facePoint = facePoint;
    }
    public boolean doAction(Soldier c, Node rootNode, GameAnimChannel channel, float tpf) {
        Vector3f currentPos = c.getLocalTranslation();

        if(!waypoints.isEmpty() && WorldMap.isClose(currentPos, waypoints.get(0))){
            waypoints.remove(0);
        }
        
        if(waypoints.isEmpty()){
            c.getCharacterControl().setWalkDirection(Vector3f.ZERO);
            if(facePoint!=null){
                final Vector3f subtract = facePoint.subtract(c.getLocalTranslation());
                c.getCharacterControl().setViewDirection(new Vector3f(subtract.x, 0, subtract.z).normalizeLocal());
            }
            c.idle();
            return true;
        }
        
        final Vector3f toWaypoint = waypoints.get(0).subtract(currentPos);
        
        final Vector3f normalize = toWaypoint.normalize();
        final MoveMode moveMode = c.getMoveMode(toWaypoint.length());
        Vector3f pathStep = normalize.mult(0.016f * moveMode.speed());
        if(pathStep.length()>waypoints.get(0).distance(currentPos)){
            pathStep = normalize.mult(waypoints.get(0).distance(currentPos));
        }
        
        if(WorldMap.get().isOutSideWorldBounds(c.getLocalTranslation().add(pathStep.mult(2)))){
            c.getCharacterControl().setWalkDirection(Vector3f.ZERO);
            return true;
        }

        if(!Vector3f.ZERO.equals(pathStep)){
            if(avoidTraffic){       
                final List<Obstacle> charactersInDirectionClose = WorldMap.get().getCharactersInDirectionClose(currentPos, pathStep);
                if(!charactersInDirectionClose.isEmpty()){
                    final Vector3f calculateForce = abstacleAvoidance.calculateForce(currentPos, pathStep, 1.5f, 1f, 2f, tpf, charactersInDirectionClose);
                    if(!calculateForce.equals(Vector3f.ZERO)){
                        pathStep = pathStep.add(calculateForce);
                    }
                }
            }
  
            c.getCharacterControl().setWalkDirection(pathStep);
            
            if(c.canPlayMoveAnimation(channel.getAnimationName())){
                Vector3f viewDirection = toWaypoint.normalize();
                
                if(facePoint!=null && c.getLocalTranslation().distance(destination)<5){
                    viewDirection = facePoint.subtract(destination);
                }
                viewDirection = calculateTurnTowardsDirection(c.getCharacterControl(), viewDirection);
                
                c.getCharacterControl().setViewDirection(new Vector3f(viewDirection.x, 0, viewDirection.z).normalizeLocal());

                
                moveMode.doAnimation(c);
                
            }
        }
        return false;
    }

    public static Vector3f calculateTurnTowardsDirection(BetterCharacterControl controle, Vector3f viewDirection) {
        final float angleBetween = controle.getViewDirection().angleBetween(viewDirection);
        if(angleBetween> FastMath.QUARTER_PI/16){
            
            Vector3f v1, v2;
            if(angleBetween>FastMath.QUARTER_PI){
                v1 = bigQ1.mult(controle.getViewDirection());
                v2 = bigQ2.mult(controle.getViewDirection());
            }else{
                v1 = lilQ1.mult(controle.getViewDirection());
                v2 = lilQ2.mult(controle.getViewDirection());
            }
            
            if(viewDirection.angleBetween(v1)>viewDirection.angleBetween(v2)){
                viewDirection = v2;
            }else{
                viewDirection = v1;
            }
        }else{
            viewDirection = controle.getViewDirection();
        }
        return viewDirection;
    }


   
}
