/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author dharshanar
 */
public class ShotsFiredListener {
    private static ShotsFiredListener instance;
    
    private Quadtree<Node> shotsFired = new Quadtree<Node>(-512, -512, 1024, 4);

    private ShotsFiredListener() {
    }

    
    
    public static ShotsFiredListener instance() {
        if(instance==null){
            instance = new ShotsFiredListener();
        }
        return instance;
    }

    public void register(Vector3f localTranslation) {
        Node n = new Node();
        n.setLocalTranslation(new Vector3f(localTranslation));
        shotsFired.insert(n);
    }

    public Set<Vector3f> getShootsFiredInRange(Vector3f localTranslation, int max, int min) {
        ArrayList<Node> fullBounds = new ArrayList<Node>();
        final Rectangle2D.Float aFloat = new Rectangle.Float(localTranslation.x-max, localTranslation.z-max, max*2, max*2);
        shotsFired.query(aFloat, fullBounds);
        Set<Vector3f> ret = new HashSet<Vector3f>();
        for(Node r:fullBounds){
            if(localTranslation.distance(r.getLocalTranslation())>min && localTranslation.distance(r.getLocalTranslation())<max){
                ret.add(new Vector3f(r.getLocalTranslation().x, localTranslation.y, r.getLocalTranslation().z));
            }
        }
        return ret;
    }

    void clear() {
        shotsFired = new Quadtree(-512, -512, 1024, 4);
    }
    
}
