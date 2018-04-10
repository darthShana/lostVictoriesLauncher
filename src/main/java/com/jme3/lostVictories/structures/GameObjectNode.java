/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.structures;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author dharshanar
 */
public class GameObjectNode extends Node{

    public GameObjectNode(Node n, BulletAppState bulletAppState, int mass, boolean movable, boolean fitToShape) {
        final Vector3f position = new Vector3f(n.getLocalTranslation());
        
        n.setLocalTranslation(Vector3f.ZERO);
        attachChild(n);
        setLocalTranslation(position);
        
        CollisionShape shape;
        if(fitToShape){
            shape = CollisionShapeFactory.createMeshShape(n);
        }else{
            shape = CollisionShapeFactory.createBoxShape(n);
        }
        
        RigidBodyControl r = new RigidBodyControl(shape, mass);
        n.addControl(r);  
        bulletAppState.getPhysicsSpace().add(r);
    }

    public Vector3f getCoverPossition(GameCharacterNode character, Vector3f target, Node rootNode) {
        Vector3f p = getLocalTranslation().add(new Vector3f(0, 3, 0));
        target = target.add(new Vector3f(0, 3, 0));
        Ray ray = new Ray(p, target.subtract(p).normalizeLocal());
        ray.setLimit(character.getMaxRange());
        CollisionResults results = new CollisionResults();
        try{
            rootNode.collideWith(ray, results);
        }catch(Throwable e){}
        
        if(results.size()>0){
            if(results.getClosestCollision().getDistance()>=target.distance(p)-2.5){
                Vector3f d = target.subtract(getLocalTranslation()).normalize().mult(2).negate();
                return getLocalTranslation().add(d);
            }
        }
        return null;
    }

}
