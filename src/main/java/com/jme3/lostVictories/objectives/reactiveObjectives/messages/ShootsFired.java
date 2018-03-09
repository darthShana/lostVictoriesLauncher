package com.jme3.lostVictories.objectives.reactiveObjectives.messages;

import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.Vector3f;

public class ShootsFired {
    private GameCharacterNode shooter;
    private Vector3f shootingDirection;

    public ShootsFired(GameCharacterNode shooter, Vector3f shootingDirection) {
        this.shooter = shooter;
        this.shootingDirection = shootingDirection;
    }

    public GameCharacterNode getShooter() {
        return shooter;
    }

    public Vector3f getShootingDirection() {
        return shootingDirection;
    }
}
