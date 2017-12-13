/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.effects;

import com.jme3.effect.ParticleEmitter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author dharshanar
 */
class ExplosionParticleControl extends AbstractControl {
    private float time = 0;
    private int state = 0;
    protected float speed = .5f;
    private final Node explosionEffect;
    

    ExplosionParticleControl(Node explosionEffect) {
        this.explosionEffect = explosionEffect;
        ((ParticleEmitter)explosionEffect.getChild("Flash")).emitAllParticles();
            ((ParticleEmitter)explosionEffect.getChild("Spark")).emitAllParticles();
            ((ParticleEmitter)explosionEffect.getChild("SmokeTrail")).emitAllParticles();            
            ((ParticleEmitter)explosionEffect.getChild("Debris")).emitAllParticles();
            ((ParticleEmitter)explosionEffect.getChild("Shockwave")).emitAllParticles();
            ((ParticleEmitter)explosionEffect.getChild("Flame")).emitAllParticles();
            ((ParticleEmitter)explosionEffect.getChild("RoundSpark")).emitAllParticles();
            
    }

    @Override
    protected void controlUpdate(float tpf) {
                time += tpf / speed;
        if (time > 1f && state == 0){
            state++;
        }
        if (time > 1f + .05f / speed && state == 1){            
            state++;
        }
        
        // rewind the effect
        if (time > 10 / speed && state == 2){
            state = 0;
            time = 0;

            ((ParticleEmitter)explosionEffect.getChild("Flash")).killAllParticles();
            ((ParticleEmitter)explosionEffect.getChild("Spark")).killAllParticles();
            ((ParticleEmitter)explosionEffect.getChild("SmokeTrail")).killAllParticles();
            ((ParticleEmitter)explosionEffect.getChild("Debris")).killAllParticles();
            ((ParticleEmitter)explosionEffect.getChild("Flame")).killAllParticles();
            ((ParticleEmitter)explosionEffect.getChild("RoundSpark")).killAllParticles();
            ((ParticleEmitter)explosionEffect.getChild("Shockwave")).killAllParticles();
            explosionEffect.removeFromParent();
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
    
}
