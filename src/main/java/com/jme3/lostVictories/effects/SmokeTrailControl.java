/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.effects;

import com.jme3.effect.ParticleEmitter;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author dharshanar
 */
class SmokeTrailControl extends AbstractControl {

    private final Node switchNode;
    private final Vector3f startingLocation;
    private final Vector3f trailDirection;
    private final ParticleEmitter emitter;

    public SmokeTrailControl(Node switchNode, ParticleEmitter emitter1, ParticleEmitter emitter2, Ray ray, float lifeSpan) {
        this.switchNode = switchNode;
        this.emitter = emitter1;
        
        switchNode.attachChild(emitter);
        switchNode.attachChild(emitter2);
        switchNode.setUserData("lifeSpan", lifeSpan);
        switchNode.setUserData("startTime", System.currentTimeMillis());
        
        startingLocation = new Vector3f(switchNode.getLocalTranslation());
        trailDirection = ray.getDirection();
        
        emitter.setEnabled(true);
        emitter2.setEnabled(true);
        emitter2.emitAllParticles();
    }

    @Override
    protected void controlUpdate(float tpf) {
        long time = System.currentTimeMillis() - (Long)switchNode.getUserData("startTime");
        float v = time/1000f*30;
        switchNode.setLocalTranslation(startingLocation.add(trailDirection.mult(v)));
        if(isExpired()){
            emitter.setEnabled(false);
            emitter.killAllParticles();
            switchNode.removeFromParent();
        }
    }

    private boolean isExpired() {
        return (System.currentTimeMillis()-(Long)switchNode.getUserData("startTime"))>=((Float)switchNode.getUserData("lifeSpan")*1000);
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}
    
}
