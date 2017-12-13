/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.effects;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.lostVictories.CharcterParticleEmitter;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

/**
 *
 * @author dharshanar
 */
public class ParticleEmitterFactory {
    private final AssetManager assetManager;

    private ParticleEmitterFactory(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    
    
    public static ParticleEmitterFactory instance(AssetManager assetManager) {
        return new ParticleEmitterFactory(assetManager);
    }

    public CharcterParticleEmitter getCharacterParticleEmitters() {
        return new CharcterParticleEmitter(createFlash(), createSmokeTrail(), createSmoke(), createBloodSplatter(), createBulletFragments(), createBlastFragments());
    }
        
    private ParticleEmitter createFlash(){
        ParticleEmitter flash = new ParticleEmitter("Flash", Type.Point, 1);
        flash.setSelectRandomImage(true);
        flash.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, 1f));
        flash.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        flash.setStartSize(.1f);
        flash.setEndSize(1.0f);
        flash.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
        flash.setParticlesPerSec(0);
        flash.setGravity(0, 0, 0);
        flash.setLowLife(.4f);
        flash.setHighLife(.4f);

        flash.setImagesX(2);
        flash.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flash.png"));
        mat.setBoolean("PointSprite", true);
        flash.setMaterial(mat);
        return flash;
    }
    
    private ParticleEmitter createSmokeTrail(){
        ParticleEmitter smoketrail = new ParticleEmitter("SmokeTrail", Type.Triangle, 1);
        smoketrail.setStartColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 1));
        smoketrail.setEndColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 0f));
        smoketrail.setStartSize(.5f);
        smoketrail.setEndSize(.5f);

//        smoketrail.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        smoketrail.setFacingVelocity(true);
        smoketrail.setParticlesPerSec(0);
        smoketrail.setGravity(0, 1, 0);
        smoketrail.setLowLife(.4f);
        smoketrail.setHighLife(.5f);
        //smoketrail.setInitialVelocity(new Vector3f(0, 12, 0));
        smoketrail.setVelocityVariation(0);
        smoketrail.setImagesX(1);
        smoketrail.setImagesY(3);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/smoketrail.png"));
        smoketrail.setMaterial(mat);
        return smoketrail;
    }
    
    private ParticleEmitter createSmoke(){
        ParticleEmitter emit = new ParticleEmitter("Smoke", Type.Triangle, 300);
        emit.setGravity(0, 0, 0);
        emit.setVelocityVariation(1);
        emit.setLowLife(1);
        emit.setHighLife(2);
        emit.setInitialVelocity(new Vector3f(0, 3f, 0));
        emit.setImagesX(15);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Smoke/Smoke.png"));
        emit.setMaterial(mat);
        emit.setEnabled(false);
        return emit;
    }
    
    private ParticleEmitter createBloodSplatter(){
        ParticleEmitter debris = new ParticleEmitter("Debris", Type.Triangle, 15);
        debris.setSelectRandomImage(true);
        debris.setRandomAngle(true);
        debris.setRotateSpeed(FastMath.TWO_PI * 4);
        debris.setStartColor(new ColorRGBA(1f, 0.1f, 0.1f, 0.5f));
        debris.setEndColor(new ColorRGBA(1f, 0.2f, 0.2f, 0.5f));
        debris.setStartSize(.2f);
        debris.setEndSize(.2f);

//        debris.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
        debris.setParticlesPerSec(0);
        debris.setGravity(0, 12f, 0);
        debris.setLowLife(1.4f);
        debris.setHighLife(1.5f);
        debris.setInitialVelocity(new Vector3f(0, 7.5f, 0));
        debris.setVelocityVariation(.60f);
        debris.setImagesX(3);
        debris.setImagesY(3);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/Debris.png"));
        debris.setMaterial(mat);
        return debris;
    }

    
    private ParticleEmitter createBulletFragments(){
        ParticleEmitter debris = new ParticleEmitter("Debris", Type.Triangle, 15);
        debris.setSelectRandomImage(true);
        debris.setRandomAngle(true);
        debris.setRotateSpeed(FastMath.TWO_PI * 4);
        debris.setStartColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1));
        debris.setEndColor(new ColorRGBA(0.2f, 0.2f, 0.2f, 0.8f));
        debris.setStartSize(.1f);
        debris.setEndSize(.1f);

//        debris.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
        debris.setParticlesPerSec(0);
        debris.setGravity(0, 12f, 0);
        debris.setLowLife(1.4f);
        debris.setHighLife(1.5f);
        debris.setInitialVelocity(new Vector3f(0, 7, 0));
        debris.setImagesX(3);
        debris.setImagesY(3);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/Debris.png"));
        debris.setMaterial(mat);
        return debris;
    }

    private ParticleEmitter createBlastFragments() {
        ParticleEmitter debris = new ParticleEmitter("Debris", Type.Triangle, 100);
        debris.setSelectRandomImage(true);
        debris.setRandomAngle(true);
        debris.setRotateSpeed(FastMath.TWO_PI * 4);
        debris.setStartColor(new ColorRGBA(0.9f, 0.8f, 0.8f, 0.9f));
        debris.setEndColor(new ColorRGBA(0.8f, 0.7f, 0.7f, 0.9f));
        debris.setStartSize(.2f);
        debris.setEndSize(.3f);

//        debris.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
        debris.setParticlesPerSec(0);
        debris.setGravity(0, 9.8f, 0);
        debris.setLowLife(7f);
        debris.setHighLife(7f);
        debris.setInitialVelocity(new Vector3f(0, 16, 0));
        debris.setImagesX(3);
        debris.setImagesY(3);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/Debris.png"));
        debris.setMaterial(mat);
        return debris;
    }
    
    
    
}
