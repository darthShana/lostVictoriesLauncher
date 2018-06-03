/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameVehicleNode;
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
public class AutoDriveAction extends DriveAction {
    private static Quaternion qLeft90 = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
    private static Quaternion qRight90= new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y);
    private List<Vector3f>  waypoints = new ArrayList<>();
    private boolean reverseToTarget = false;
    private boolean pointsPlotted;

    public AutoDriveAction(GameVehicleNode vehicle, List<Vector3f> path) {
        waypoints.addAll(path);
        if(waypoints.get(waypoints.size()-1).distance(vehicle.getLocalTranslation())<7){
            Vector3f[] points = getReverseTrianlge(vehicle.getLocalTranslation(), vehicle.getCharacterControl().getViewDirection());            
            if(isInTriangle(waypoints.get(waypoints.size()-1), vehicle.getLocalTranslation(), points[0], points[1])){
                reverseToTarget = true;
            }
                    
        }
    }

    public boolean doAction(GameVehicleNode c, Node rootNode, GameAnimChannel channel, float tpf) {
//        if(!pointsPlotted && c instanceof GameVehicleNode){
//            pointsPlotted = true;
//            c.showPath(waypoints);
//        }
        final Vector3f localTranslation = c.getLocalTranslation();
        if(!waypoints.isEmpty()){
            if(WorldMap.isClose(localTranslation, waypoints.get(0), 1)){
                waypoints.remove(0);                
            }
        }
        if(!c.canShootWhileMoving() && c.isFirering()){
            return true;
        }
        if(waypoints.isEmpty() || c.hasEngineDamage()){
            c.stop();
            c.getCharacterControl().accelerate(0);
            return true;
        }
        
        Vector3f toWaypoint = waypoints.get(0).subtract(localTranslation);
        Vector3f pathStep = toWaypoint.normalize();
        
        if(c.hasCollidedWithUnMovableObject()){

            if(doBackoutAction(c, c.getCharacterControl(), c.getColliosionPoint())){
                c.clearCollisionWithUnmovableObject();
                c.getCharacterControl().applyBreak();    
                c.straighten();
                c.getCharacterControl().staighten();
                return true;
            }
            return false;
        }
        
        if(reverseToTarget){
            return doReverseAction(c.getCharacterControl(), localTranslation, pathStep, c);
        }
        return doDriveAction(c.getCharacterControl(), pathStep, tpf, c, toWaypoint.length());
    }

    public static boolean isInTriangle(Vector3f p, Vector3f p1, Vector3f p2, Vector3f p3) {

        double o1 = getOrientationResult(p1.x, p1.z, p2.x, p2.z, p.x, p.z);
        double o2 = getOrientationResult(p2.x, p2.z, p3.x, p3.z, p.x, p.z);
        double o3 = getOrientationResult(p3.x, p3.z, p1.x, p1.z, p.x, p.z);

        return (o1 == o2) && (o2 == o3);
    }

    private static int getOrientationResult(double x1, double y1, double x2, double y2, double px, double py) {
        double orientation = ((x2 - x1) * (py - y1)) - ((px - x1) * (y2 - y1));
        if (orientation > 0) {
            return 1;
        }
        else if (orientation < 0) {
            return -1;
        }
        else {
            return 0;
        }
    }

    public static Vector3f[] getReverseTrianlge(Vector3f localTranslation, Vector3f viewDirection) {
        Vector3f p = localTranslation.add(viewDirection.normalize().mult(-7));
        final Vector3f normalize = localTranslation.subtract(p).normalize();
        Vector3f p1 = p.add(qLeft90.mult(normalize).mult(4));
        Vector3f p2 = p.add(qRight90.mult(normalize).mult(4));
        return new Vector3f[]{p1, p2};
    }

    



    
        

    

    

}
