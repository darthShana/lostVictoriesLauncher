package com.jme3.lostVictories.structures;

import com.jme3.bullet.BulletAppState;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.UUID;

public class GameBunkerNode extends GameStructureNode{


    private UUID identity = UUID.randomUUID();

    public GameBunkerNode(Node house, BulletAppState bulletAppState, CollisionShapeFactoryProvider collisionShapeFactoryProvider) {
        super(house, bulletAppState, collisionShapeFactoryProvider);
        Quaternion q = new Quaternion(house.getLocalRotation());
        house.setLocalRotation(Quaternion.ZERO);
        setLocalRotation(q);
    }

    public UUID getIdentity() {
        return identity;
    }

    public Vector3f calculateEntryPoint() {
        Vector3f v = this.getLocalRotation().mult(Vector3f.UNIT_Z.mult(7));
        return this.getLocalTranslation().add(v);
    }
}
