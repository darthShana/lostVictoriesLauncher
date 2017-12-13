/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.structures;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.lostVictories.CanInteractWith;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;

import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class UnclaimedEquipmentNode extends Node implements CanInteractWith, Pickable{
    private final UUID id;
    private final Node rootNode;

    public UnclaimedEquipmentNode(UUID id, Vector3f location, Vector3f rotation, Weapon weapon, Node model, Node rootNode, AssetManager assetManager) {
        this.id = id;
        this.rootNode = rootNode;
        setUserData("GameCharacterControl", "blank");
        
        Cylinder sphere = new Cylinder(12, 12, .5f, .1f, true, false);
        Geometry mark = new Geometry(id.toString(), sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", new ColorRGBA(1, 1, 1, .5f));
        mark_mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mark.setQueueBucket(RenderQueue.Bucket.Transparent);
        mark.setMaterial(mark_mat);
        mark.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
        final Vector3f m = weapon.getMuzzelLocation();
        mark.setLocalTranslation(m.x, 0, m.z);
        
        this.attachChild(mark);
        
        weapon.removeUnusedWeapons(model);
        final Spatial c1 = model.getChild("soldier");
        if(c1!=null){
            c1.removeFromParent();
        }
        AnimControl control = model.getControl(AnimControl.class);
        AnimChannel channel = control.createChannel();
        channel.setAnim(weapon.getName()+"_standByAction");
        channel.setLoopMode(LoopMode.DontLoop);    
        
        
        this.setLocalTranslation(location);
        Quaternion q = new Quaternion();
        q.lookAt(rotation, Vector3f.UNIT_Y);
        this.setLocalRotation(q);
        model.setLocalScale(.25f);
        this.attachChild(model);
        rootNode.attachChild(this);
        
    }

    
    
    public UUID getId() {
        return id;
    }

    public boolean isAlliedWith(GameCharacterNode character) {
        return true;
    }

    public boolean takeBullet(CollisionResult result, GameCharacterNode aThis) {
        return false;
    }

    public boolean takeMissile(CollisionResult result, GameCharacterNode shooter){ return  false; }

    public void destroy() {
        this.removeFromParent();
    }

    public Vector3f getLocation() {
        return getLocalTranslation();
    }

    public boolean isAbbandoned() {
        return false;
    }
    
    
    
}
