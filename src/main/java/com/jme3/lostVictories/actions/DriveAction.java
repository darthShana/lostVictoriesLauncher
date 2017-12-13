/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.characters.physicsControl.BetterVehicleControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

/**
 *
 * @author dharshanar
 */
public abstract class DriveAction implements AIAction<GameVehicleNode>{
    static final float f = FastMath.QUARTER_PI*0.008f;
    public static Quaternion qLeft = new Quaternion().fromAngleAxis(f, Vector3f.UNIT_Y);
    public static Quaternion qRight= new Quaternion().fromAngleAxis(-f, Vector3f.UNIT_Y);
    
    
    protected boolean doDriveAction(BetterVehicleControl controle, Vector3f pathStep, float tpf, GameVehicleNode c, float distancetoWayPoint) {
        float move_speed;
        Vector3f vehicleDirection = controle.getForwardVector(null).normalize();
        Vector2f currentDirection = new Vector2f(vehicleDirection.x, vehicleDirection.z).normalizeLocal();
        Vector2f requiredDirection = new Vector2f(pathStep.x, pathStep.z).normalizeLocal();
        final float angleBetween = currentDirection.smallestAngleBetween(requiredDirection);        
        
        if(angleBetween>FastMath.QUARTER_PI/8f){            
            Vector3f v1 = qLeft.mult(vehicleDirection);
            Vector3f v2 = qRight.mult(vehicleDirection);
            if(requiredDirection.smallestAngleBetween(new Vector2f(v1.x, v1.z))>requiredDirection.smallestAngleBetween(new Vector2f(v2.x, v2.z))){
                c.turnRight();
                controle.stearRight();
            }else{
                c.turnLeft();
                controle.stearLeft();
            }
                     
            move_speed = c.getTurnSpeed();
            
        }else{
            move_speed = c.getDriveSpeed();
            c.straighten();
            controle.staighten();
        }
        if(distancetoWayPoint<4){
            move_speed = c.getTurnSpeed();
        }
        float incline = FastMath.asin(pathStep.y);
        if(controle.getCurrentVehicleSpeedKmHour()<move_speed){ 
            if(incline>0.07){
                controle.turboBoost();
            }else{               
                controle.forward();
            }
        }else{
            controle.neutral();
        }
        
        
        return false;
    }
    
    protected boolean doBackoutAction(GameVehicleNode node, BetterVehicleControl controle, Vector3f collisionPoint) {
        if(node.getLocalTranslation().distance(collisionPoint)>3){
            return true;
        }
        controle.reverseStearing();
        
        if(controle.getCurrentVehicleSpeedKmHour()>-15){
            controle.backward();
        }else{              
            controle.accelerate(0);
            controle.brake(50);
        }
        
        return false;
    }
    
    protected boolean doReverseAction(BetterVehicleControl controle, Vector3f localTranslation, Vector3f pathStep, GameVehicleNode c) {
//        Vector2f aa = new Vector2f(controle.getViewDirection().x, controle.getViewDirection().z);
//        float cc = aa.smallestAngleBetween(new Vector2f(pathStep.x, pathStep.z));
//        float stearing = (.5f/FastMath.QUARTER_PI)*cc;
//        Vector3f v1 = qLeft.mult(controle.getViewDirection());
//        Vector3f v2 = qRight.mult(controle.getViewDirection());
//        if(new Vector2f(pathStep.x, pathStep.z).smallestAngleBetween(new Vector2f(v1.x, v1.z))>new Vector2f(pathStep.x, pathStep.z).smallestAngleBetween(new Vector2f(v2.x, v2.z))){
//            controle.steer(stearing);
//            c.turnLeft();
//        }else{
//            controle.steer(-stearing);
//            c.turnRight();            
//        }
        controle.steer(0);
        if(controle.getCurrentVehicleSpeedKmHour()>-15){
            controle.backward();
        }else{              
            controle.accelerate(0);
            controle.brake(50);
        }
        return false;
    }
}
