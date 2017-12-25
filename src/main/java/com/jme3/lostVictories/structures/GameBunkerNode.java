package com.jme3.lostVictories.structures;

import com.jme3.bullet.BulletAppState;
import com.jme3.scene.Node;

import java.util.UUID;

public class GameBunkerNode extends GameStructureNode{


    private UUID identity = UUID.randomUUID();

    public GameBunkerNode(Node house, BulletAppState bulletAppState, CollisionShapeFactoryProvider collisionShapeFactoryProvider) {
        super(house, bulletAppState, collisionShapeFactoryProvider);
    }

    public UUID getIdentity() {
        return identity;
    }
}
