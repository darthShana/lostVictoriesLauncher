/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.bounding.BoundingVolume;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author dharshanar
 */
class UnderTakerControl extends AbstractControl {
    private Long timeOfDeath;
    private Camera camera;

    public UnderTakerControl(Camera camera) {
        this.camera = camera;
    }
    
    

    @Override
    protected void controlUpdate(float f) {
        final GameCharacterNode charcter = (GameCharacterNode)spatial;
        if(charcter.isDead()){
            if(timeOfDeath==null){
                timeOfDeath = System.currentTimeMillis();
            }
            if(System.currentTimeMillis()-timeOfDeath>5000){
                BoundingVolume bv = charcter.getWorldBound();
                int planeState = camera.getPlaneState();
                camera.setPlaneState(0);
                Camera.FrustumIntersect result = camera.contains(bv);
                camera.setPlaneState(planeState);

                if( result == Camera.FrustumIntersect.Outside){
                    charcter.decomposed();
                }else if(System.currentTimeMillis()-timeOfDeath>10000){
                    charcter.decomposed();
                }
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}

    
}
