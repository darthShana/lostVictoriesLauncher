/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author dharshanar
 */
public class CharacterAction {

    Gear gear = Gear.STOPPED;
    Direction direction = Direction.STRAIGHT;
    private Vector3f lookAt;
    int turnCount;
    private Future<Optional<List<Vector3f>>> computePath;
    List<Vector3f> waypoints = new ArrayList<Vector3f>();
    private Vector3f[] target;
    
    void goForward() {
        cancelPathFinding();
        gear = Gear.FORWARD;
    }

    void goBackward() {
        cancelPathFinding();
        gear = Gear.REVERSE;
    }

    void shiftLeft(){
        cancelPathFinding();
        gear = Gear.SHIFT_LEFT;
    }

    void shiftRight(){
        cancelPathFinding();
        gear = Gear.SHIFT_RIGHT;
    }

    void turnLeft() {
        cancelPathFinding();
        direction = Direction.LEFT;
    }

    void turnRight() {
        cancelPathFinding();
        direction = Direction.RIGHT;
    }

    void stopForwardMovement() {
        gear = Gear.STOPPED;
        direction = Direction.STRAIGHT;
        waypoints.clear();
    }

    void stopTurnLeft() {
        direction = Direction.STRAIGHT;
    }

    void stopTurnRight() {
        direction = Direction.STRAIGHT;
    }

    private void cancelPathFinding() {
        if(computePath!=null){
            computePath.cancel(true);
            computePath = null;
        }
    }

    
    void lookAt(Vector3f currentLocation, Vector3f target, Vector3f currentDirection) {
        Vector3f v = target.subtract(currentLocation);
        this.lookAt = new Vector3f(v.x, 0, v.z).normalizeLocal();
        Vector3f c = currentLocation.add(currentDirection.normalize());
        
        if(FastMath.counterClockwise(new Vector2f(currentLocation.x, currentLocation.z), new Vector2f(c.x, c.z), new Vector2f(target.x, target.z)) == -1){
            direction = Direction.LEFT;
        }else{
            direction = Direction.RIGHT;
        }
        turnCount = 0;
    }
    
    boolean isTurningToTarget(){
        return lookAt!=null;
    }
    
    Vector3f turn(Vector3f requiredDirection, float tpf) {
        
        Vector3f ret;
        if(lookAt!=null){
            if(requiredDirection.angleBetween(lookAt)< FastMath.QUARTER_PI/2 || turnCount > 20){
                ret = new Vector3f(lookAt);
                lookAt = null;
                direction = Direction.STRAIGHT;
            }else{
                float d = FastMath.TWO_PI*2;
                if(direction == Direction.RIGHT){
                    d = -1 * d;
                }
                Quaternion rotation = new Quaternion();
                rotation.fromAngleAxis(d*tpf, Vector3f.UNIT_Y);
                ret = rotation.mult(requiredDirection);
                turnCount++;
            }
        }else{
            ret = requiredDirection;
        }
        return ret;
    }
    
    public void travelPath(List<Vector3f> points) {
        this.gear = Gear.AUTO_PILOT;
        waypoints.clear();
        waypoints.addAll(points);
    }

    boolean isTraversingPath() {
        return this.gear == Gear.AUTO_PILOT;
    }

    public void travelPath() {
        if(computePath!=null && computePath.isDone()) {
            try {
                if (computePath.get().isPresent()) {
                    travelPath(computePath.get().get());
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            computePath = null;
        }
    }

    void calculatePath(GameCharacterNode character, Vector3f location, Vector3f contactPoint, NavigationProvider pathFinder) {
        if(computePath!=null){
            computePath.cancel(true);
            computePath = null;
        }

        try{
            computePath = pathFinder.computePathFuture(character, .8f, location, contactPoint);
        }catch(Throwable e){
            e.printStackTrace();
        }
    }


    public Vector3f traversePath(GameCharacterNode c, float f, Vector3f currentPos, Node rootNode) {

        if(!waypoints.isEmpty()){
            if(WorldMap.isClose(currentPos, waypoints.get(waypoints.size()-1))){
                waypoints.clear();
                this.gear = Gear.STOPPED;
                return null;
            }else{
                if(!waypoints.isEmpty() && WorldMap.isClose(currentPos, waypoints.get(0))){
                    waypoints.remove(0);
                }

                if(waypoints.isEmpty()){
                    return null;
                }

                final Vector3f toWaypoint = waypoints.get(0).subtract(currentPos);
                final Vector3f directionToWaypoint = toWaypoint.normalize();
                Vector3f mult = directionToWaypoint.mult(f);
                if(mult.length()>toWaypoint.length()){
                    mult = directionToWaypoint.mult(toWaypoint.length());
                }

                return mult;

            }
        }else{
            return null;
        }
    }

    boolean isMovingForward() {
        return gear == Gear.FORWARD;
    }

    boolean isMovingBackward(){
        return gear == Gear.REVERSE;
    }

    boolean isShiftLeft(){
        return gear == Gear.SHIFT_LEFT;
    }

    boolean isShiftRight(){
        return gear == Gear.SHIFT_RIGHT;
    }

    boolean isTurningLeft() {
        return direction == Direction.LEFT;
    }

    boolean isTurningRight() {
        return direction == Direction.RIGHT;
    }

    boolean hasStopped() {
        return gear == Gear.STOPPED;
    }

    Vector3f getTraversingPathDestination() {
        if(!waypoints.isEmpty()){
            return waypoints.get(waypoints.size()-1);
        }
        return null;
    }

    Vector3f getTraversingPathNextStep(){
        if(!waypoints.isEmpty()){
            return waypoints.get(0);
        }
        return null;
    }

    void target(Vector3f[] target) {
        this.target = target;;
    }

    Vector3f[] getTarget() {
        return target;
    }

    void clearTarget() {
        this.target = null;
    }

    private enum Direction {
        STRAIGHT, LEFT, RIGHT
    }

    private enum Gear {
        STOPPED, FORWARD, REVERSE, SHIFT_LEFT, SHIFT_RIGHT, AUTO_PILOT
    }
    
}
