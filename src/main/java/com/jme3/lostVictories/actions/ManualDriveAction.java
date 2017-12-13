/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.network.messages.actions.Action;
import com.jme3.scene.Node;

/**
 *
 * @author dharshanar
 */
public class ManualDriveAction implements AIAction<GameVehicleNode>{

    private Gear gear = Gear.NEUTRAL;
    private Steering steering = Steering.STRAIGHT;
    private float currentSteering = 0;

    public void forward() {
        this.gear = Gear.FORWARD;
    }
    
    public void reverse() {
        this.gear = Gear.REVERSE;
    }

    public void neutral() {
        this.gear = Gear.NEUTRAL;
    }

    public void turnLeft(){
        this.steering = Steering.LEFT;
    }
    
    public void turnRight(){
        this.steering = Steering.RIGHT;
    }
    
    public void straighten(){
        this.steering = Steering.STRAIGHT;
    }
    
    
    

    @Override
    public boolean doAction(GameVehicleNode vehicle, Node rootNode, GameAnimChannel channel, float tpf) {
//        if("58858bc9-e92e-4c97-99e3-b094d8f5917f".equals(vehicle.getIdentity().toString())){
//            System.out.println("steering:"+steering);
//            System.out.println("gear:"+gear);
//        }
        if(steering == Steering.RIGHT&& currentSteering>-vehicle.getMaxStearingAngle()){
            vehicle.turnRight();
            currentSteering -= 0.1f;
            vehicle.getCharacterControl().steer(currentSteering);
            
        }
        if(steering == Steering.LEFT&& currentSteering<vehicle.getMaxStearingAngle()){
            vehicle.turnLeft();
            currentSteering += 0.1f;
            vehicle.getCharacterControl().steer(currentSteering);
            
        }
        if(steering == Steering.STRAIGHT){
            vehicle.straighten();
            vehicle.getCharacterControl().steer(0);
            currentSteering = 0;
        }
        if(gear == Gear.FORWARD){
            if(steering == Steering.STRAIGHT){
                vehicle.straighten();
            }
            if(vehicle.getCharacterControl().getCurrentVehicleSpeedKmHour()<vehicle.getDriveSpeed()){
                vehicle.getCharacterControl().forward();
            }
        }
        if(gear == Gear.NEUTRAL){
            vehicle.getCharacterControl().neutral();
            vehicle.stop();
        }
        if(gear == Gear.REVERSE){
            vehicle.straighten();
            currentSteering = 0;
            vehicle.getCharacterControl().steer(currentSteering);
            if(vehicle.getCharacterControl().getCurrentVehicleSpeedKmHour()>-15){
                vehicle.getCharacterControl().backward();
            }
        }
        
        return false;
    }

    public Action toMessage() {
        return new com.jme3.lostVictories.network.messages.actions.ManualControl(gear.name(), steering.name());
    }

    public static enum Gear {
        NEUTRAL, FORWARD, REVERSE


    }

    public static enum Steering {
        STRAIGHT, LEFT, RIGHT

    }
    
}
