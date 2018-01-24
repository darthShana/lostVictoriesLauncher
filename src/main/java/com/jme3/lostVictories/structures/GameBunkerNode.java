package com.jme3.lostVictories.structures;

import com.jme3.bullet.BulletAppState;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public Vector3f getEntryPoint() {
        Vector3f v = this.getLocalRotation().mult(Vector3f.UNIT_Z.mult(7));
        return this.getLocalTranslation().add(v);
    }

    public List<Vector3f[]> getOccupationPoints(){
        Vector3f v11 = Vector3f.UNIT_Z.add(Vector3f.UNIT_X.mult(.5f));
        Vector3f v21 = Vector3f.UNIT_Z.add(Vector3f.UNIT_X.mult(-.5f));

        Vector3f v = new Vector3f(getLocalTranslation());
        List<Vector3f[]> ret = new ArrayList<>();

        ret.add(new Vector3f[]{
                    new Vector3f(v),
                    new Vector3f(v).add(Vector3f.UNIT_Z.negate())
        });
        ret.add(new Vector3f[]{
                    new Vector3f(v).add(this.getLocalRotation().mult(v11)),
                    new Vector3f(v).add(Vector3f.UNIT_Z).add(Vector3f.UNIT_X.mult(1.5f))
        });
        ret.add(new Vector3f[]{
                    new Vector3f(v).add(this.getLocalRotation().mult(v21)),
                    new Vector3f(v).add(Vector3f.UNIT_Z).add(Vector3f.UNIT_X.mult(-1.5f))});
        ret.add(new Vector3f[]{
                    getEntryPoint(),
                    new Vector3f(v).add(Vector3f.UNIT_Z.mult(8))
        });

        return ret;

    }
}
