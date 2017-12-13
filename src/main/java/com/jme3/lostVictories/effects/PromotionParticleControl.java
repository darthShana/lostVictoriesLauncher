/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.effects;

import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;


public class PromotionParticleControl extends AbstractControl {
    
    private float lifespan;
    private long spawnTime;
    private final GameCharacterNode centre;
    private final float radius;
    private boolean inOrbit;
    public Quaternion orbit;
 
    public PromotionParticleControl(float lifespan, GameCharacterNode centre, float radius) {
        
        this.lifespan = lifespan;
        spawnTime = System.currentTimeMillis();
        this.centre = centre;
        this.radius = radius;
        orbit = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
    }
 
    @Override
    protected void controlUpdate(float tpf) {
        // movement
        if(!inOrbit){
            spatial.setLocalTranslation(centre.getLocalTranslation().add(new Vector3f(Vector3f.UNIT_X).mult(radius)));
            inOrbit = true;
        }else{
            Vector3f v1 = spatial.getLocalTranslation().subtract(centre.getLocalTranslation());
            Vector3f v2 = orbit.mult(v1);
            v2.addLocal(new Vector3f(0, .025f, 0));
            spatial.move(v2.subtract(v1));
        }
        
        long difTime = System.currentTimeMillis() - spawnTime;
        if (difTime > lifespan) {
            spatial.removeFromParent();
        }
        
    }
 
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}
 

}
