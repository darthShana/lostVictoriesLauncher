/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.structures;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.scene.Node;

/**
 *
 * @author dharshanar
 */
public class CollisionShapeFactoryProvider {

    public RigidBodyControl createRigidBodyControl(Node house) {
        final CollisionShape createMeshShape = CollisionShapeFactory.createMeshShape(house);
        return  new RigidBodyControl(createMeshShape, 0);
    }
    
}
