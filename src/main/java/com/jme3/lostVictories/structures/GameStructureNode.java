/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.structures;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.LodControl;
import jme3tools.optimize.LodGenerator;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author dharshanar
 */
public class GameStructureNode extends Node {
    
    final Map<Rectangle, Vector3f> coverPossitions = new HashMap<>();
    
    protected final BoundingBox bounds;

    public GameStructureNode(Node house, BulletAppState bulletAppState, CollisionShapeFactoryProvider collisionShapeFactoryProvider) {
        final Vector3f possition = new Vector3f(house.getLocalTranslation());
        bakeLOD(house);
        
        
        house.setLocalTranslation(Vector3f.ZERO);
        attachChild(house);
        setLocalTranslation(possition);
        this.bounds = (BoundingBox)house.getWorldBound();

        RigidBodyControl r = collisionShapeFactoryProvider.createRigidBodyControl(house);
        if(r!=null) {
            house.addControl(r);
            bulletAppState.getPhysicsSpace().add(r);
        }
        
        calulateCoverPosstions(house);
    }

    private void calulateCoverPosstions(Node model) {
        final BoundingBox w = (BoundingBox) model.getWorldBound();
        if(w==null){
            return;
        }
        Vector3f l = w.getCenter();
//        System.out.println("m:"+model.getLocalRotation());
//        System.out.println("w:"+w);
        
        float width = w.getXExtent();
        float height = w.getZExtent();
        float alt = getLocalTranslation().y;

        coverPossitions.put(new Rectangle((int)(l.x+width), (int)(l.z+height), 100, 200),         new Vector3f(l.x+width, alt, l.z-height));
        coverPossitions.put(new Rectangle((int)(l.x-width-100), (int)(l.z+height), 100, 200),      new Vector3f(l.x-width, alt, l.z-height));
        coverPossitions.put(new Rectangle((int)(l.x+width), (int)(l.z-height-200), 100, 200),     new Vector3f(l.x+width, alt, l.z+height));
        coverPossitions.put(new Rectangle((int)(l.x-width-100), (int)(l.z-height-200), 100, 200),  new Vector3f(l.x-width, alt, l.z+height));
        coverPossitions.put(new Rectangle((int)(l.x-width-200), (int)(l.z-height-100), 200, 100),  new Vector3f(l.x+width, alt, l.z-height));
        coverPossitions.put(new Rectangle((int)(l.x+width), (int)(l.z-height-100), 200, 100),      new Vector3f(l.x-width, alt, l.z-height));
        coverPossitions.put(new Rectangle((int)(l.x-width-200), (int)(l.z+height), 200, 100),     new Vector3f(l.x+width, alt, l.z+height));
        coverPossitions.put(new Rectangle((int)(l.x+width), (int)(l.z+height), 200, 100),         new Vector3f(l.x-width, alt, l.z+height));

        
    }

    
    public Vector3f getAttackPossition(GameCharacterNode character, Vector3f _target, Node rootNode){
        Vector3f target = new Vector3f(_target);

        for(Rectangle r:coverPossitions.keySet()){
            if(r.contains(new Point((int)target.x, (int)target.z))){
                Vector3f p = new Vector3f(coverPossitions.get(r));
                p = character.getShootingLocation(p, target.subtract(p));
                final Vector3f attackDirection = target.subtract(p).normalizeLocal();
                Ray ray = new Ray(p, attackDirection);
                ray.setLimit(character.getMaxRange());
                CollisionResults results = new CollisionResults();
                if(rootNode!=null){

                    try{
                        rootNode.collideWith(ray, results);
                    }catch(Throwable e){}

                    if(results.size()>0){
                        if(results.getClosestCollision().getDistance()>=_target.distance(p)-2.5){
                            return coverPossitions.get(r);
                        }
                    }
                }else{
                    return coverPossitions.get(r);
                }

            }
        }
        return null;
    }

    public BoundingVolume getBounds(){
        return new BoundingBox(bounds.getCenter(), bounds.getXExtent(), bounds.getYExtent(), bounds.getZExtent());
    }

    private static void bakeLOD(Node house) {

        for(Spatial s:house.getChildren()){
            if(s instanceof Geometry){
                try{
                    LodGenerator lod = new LodGenerator((Geometry) s);
                    lod.bakeLods(LodGenerator.TriangleReductionMethod.PROPORTIONAL, .25f, .5f, .75f);                        
                    s.addControl(new LodControl());
                }catch(Exception e){}
                
            }else if(s instanceof Node){
                bakeLOD((Node) s);
            }
        }
    }

    
}
