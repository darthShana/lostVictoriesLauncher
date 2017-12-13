/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.effects;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dharshanar
 */
public class ParticleManager {
    

    private Node guiNode;
    private Spatial standardParticle, tracerBulletParticle;
 
    private Node particleNode;
    private Node explosionEffect = new Node("explosionFX");
    private final ParticleEmitter smokeEmitter;
 
    public ParticleManager(Node guiNode, AssetManager assetManager, RenderManager renderManager) {
        this.guiNode = guiNode;
        this.standardParticle = getPromotionSpatial("promotion", assetManager);
        this.tracerBulletParticle = getTracerBulletSpatial("tracerBullet", assetManager);
 
        particleNode = new Node("particles");
        guiNode.attachChild(particleNode);
        
        smokeEmitter = createSmoke(assetManager);
        
        explosionEffect.attachChild(createFlame(assetManager));
        explosionEffect.attachChild(createFlash(assetManager));
        explosionEffect.attachChild(createSpark(assetManager));
        explosionEffect.attachChild(createRoundSpark(assetManager));
        explosionEffect.attachChild(createSmokeTrail(assetManager));
        explosionEffect.attachChild(createDebris(assetManager));
        explosionEffect.attachChild(createShockwave(assetManager));
        explosionEffect.setLocalScale(0.5f);
        renderManager.preloadScene(explosionEffect);

        guiNode.attachChild(explosionEffect);
    }
    
    public void playExplosionEffect(Vector3f position) {
        Spatial particle = explosionEffect.clone();
        particle.setLocalTranslation(position);
        particle.addControl(new ExplosionParticleControl((Node) particle));
        particleNode.attachChild(particle);
    }
    
    public void playPromotionEffect(GameCharacterNode character) {
        // init colors
        Spatial particle = standardParticle.clone();

        particle.setLocalTranslation(character.getLocalTranslation());
        particle.addControl(new PromotionParticleControl(5000, character, .75f));
        particleNode.attachChild(particle);

    }
    
    public void playTracerBulletEffect(GameCharacterNode shooter, Vector3f relativePosition, List<Ray> _rays, List<Float> _lifeSpans){
        List<Ray> rays = new ArrayList<>(_rays);
        List<Float> lifeSpans = new ArrayList<>(_lifeSpans);
        Node switchNode = new Node();
         
        switchNode.addControl(new TracerBulletControl(shooter, relativePosition, tracerBulletParticle.clone(), rays, lifeSpans, 20, 40));
        particleNode.attachChild(switchNode);
    }
    
    public void playTracerCannonEffect(Vector3f position, Ray ray, float lifeSpan){
        Node switchNode = new Node();
        switchNode.setLocalTranslation(position);
         
        switchNode.addControl(new SmokeTrailControl(switchNode, smokeEmitter.clone(), smokeEmitter.clone(), ray, lifeSpan));
        particleNode.attachChild(switchNode);
    }



    private Spatial getPromotionSpatial(String name, AssetManager assetManager) {
        Node node = new Node(name);

        Sphere b= new Sphere(8, 8, .05f);
        Geometry mark = new Geometry(name, b);
        Material picMat = new Material(assetManager, "Common/MatDefs/Gui/Gui.j3md");
        picMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mark.setQueueBucket(RenderQueue.Bucket.Transparent);
        
        picMat.setColor("Color", new ColorRGBA(0.9f,0.7f,0.1f,.8f));
        mark.setMaterial(picMat);   
        node.attachChild(mark);
        return node;
    }

    private Spatial getTracerBulletSpatial(String tracer, AssetManager assetManager) {
        //Node node = new Node(tracer);

        Cylinder b= new Cylinder(6, 6, .05f, 1);
        Geometry mark = new Geometry(tracer, b);
        Material picMat = new Material(assetManager, "Common/MatDefs/Gui/Gui.j3md");
        //picMat.setTexture("Texture", assetManager.loadTexture("Textures/"+name+".png"));
        picMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mark.setQueueBucket(RenderQueue.Bucket.Transparent);
        
        picMat.setColor("Color", new ColorRGBA(0.9f,0.7f,0.1f,.5f));
        mark.setMaterial(picMat);   
        //node.attachChild(mark);
        return mark;
    }


    private static final int COUNT_FACTOR = 1;
    private static final float COUNT_FACTOR_F = 1f;

    private static final boolean POINT_SPRITE = true;
    private static final ParticleMesh.Type EMITTER_TYPE = POINT_SPRITE ? ParticleMesh.Type.Point : ParticleMesh.Type.Triangle;

    private ParticleEmitter createFlame(AssetManager assetManager){
        ParticleEmitter flame = new ParticleEmitter("Flame", EMITTER_TYPE, 32 * COUNT_FACTOR);
        flame.setSelectRandomImage(true);
        flame.setStartColor(new ColorRGBA(1f, 0.4f, 0.05f, (float) (1f / COUNT_FACTOR_F)));
        flame.setEndColor(new ColorRGBA(.4f, .22f, .12f, 0f));
        flame.setStartSize(1.3f);
        flame.setEndSize(4f);
        flame.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        flame.setParticlesPerSec(0);
        flame.setGravity(0, -5, 0);
        flame.setLowLife(.7f);
        flame.setHighLife(.8f);
        flame.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 3.5f, 0));
        flame.getParticleInfluencer().setVelocityVariation(1f);
        flame.setImagesX(2);
        flame.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        mat.setBoolean("PointSprite", POINT_SPRITE);
        flame.setMaterial(mat);
        return flame;
    }

    private ParticleEmitter createFlash(AssetManager assetManager){
        ParticleEmitter flash = new ParticleEmitter("Flash", EMITTER_TYPE, 24 * COUNT_FACTOR);
        flash.setSelectRandomImage(true);
        flash.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1f / COUNT_FACTOR_F)));
        flash.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        flash.setStartSize(.1f);
        flash.setEndSize(5.5f);
        flash.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
        flash.setParticlesPerSec(0);
        flash.setGravity(0, 0, 0);
        flash.setLowLife(.4f);
        flash.setHighLife(.4f);
        flash.setInitialVelocity(new Vector3f(0, 2.5f, 0));
        flash.setVelocityVariation(1);
        flash.setImagesX(2);
        flash.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flash.png"));
        mat.setBoolean("PointSprite", POINT_SPRITE);
        flash.setMaterial(mat);
        return flash;
    }

    private ParticleEmitter createRoundSpark(AssetManager assetManager){
        ParticleEmitter roundspark = new ParticleEmitter("RoundSpark", EMITTER_TYPE, 20 * COUNT_FACTOR);
        roundspark.setStartColor(new ColorRGBA(1f, 0.29f, 0.34f, (float) (1.0 / COUNT_FACTOR_F)));
        roundspark.setEndColor(new ColorRGBA(0, 0, 0, (float) (0.5f / COUNT_FACTOR_F)));
        roundspark.setStartSize(2.2f);
        roundspark.setEndSize(3.2f);
        roundspark.setShape(new EmitterSphereShape(Vector3f.ZERO, 2f));
        roundspark.setParticlesPerSec(0);
        roundspark.setGravity(0, -.5f, 0);
        roundspark.setLowLife(2.8f);
        roundspark.setHighLife(3f);
        roundspark.setInitialVelocity(new Vector3f(0, 1.5f, 0));
        roundspark.setVelocityVariation(.5f);
        roundspark.setImagesX(1);
        roundspark.setImagesY(1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/roundspark.png"));
        mat.setBoolean("PointSprite", POINT_SPRITE);
        roundspark.setMaterial(mat);
        return roundspark;
    }

    private ParticleEmitter createSpark(AssetManager assetManager){
        ParticleEmitter spark = new ParticleEmitter("Spark", ParticleMesh.Type.Triangle, 30 * COUNT_FACTOR);
        spark.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1.0f / COUNT_FACTOR_F)));
        spark.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        spark.setStartSize(2f);
        spark.setEndSize(2f);
        spark.setFacingVelocity(true);
        spark.setParticlesPerSec(0);
        spark.setGravity(0, 5, 0);
        spark.setLowLife(2.1f);
        spark.setHighLife(2.5f);
        spark.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 10, 0));
        spark.getParticleInfluencer().setVelocityVariation(1);
        spark.setImagesX(1);
        spark.setImagesY(1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/spark.png"));
        spark.setMaterial(mat);
        return spark;
    }

    private ParticleEmitter createSmokeTrail(AssetManager assetManager){
        ParticleEmitter smoketrail = new ParticleEmitter("SmokeTrail", ParticleMesh.Type.Triangle, 22 * COUNT_FACTOR);
        smoketrail.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1.0f / COUNT_FACTOR_F)));
        smoketrail.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        smoketrail.setStartSize(.2f);
        smoketrail.setEndSize(3.5f);

//        smoketrail.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        smoketrail.setFacingVelocity(true);
        smoketrail.setParticlesPerSec(0);
        smoketrail.setGravity(0, 1, 0);
        smoketrail.setLowLife(1.8f);
        smoketrail.setHighLife(2.1f);
        smoketrail.setInitialVelocity(new Vector3f(0, 6, 0));
        smoketrail.setVelocityVariation(1);
        smoketrail.setImagesX(1);
        smoketrail.setImagesY(3);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/smoketrail.png"));
        smoketrail.setMaterial(mat);
        return smoketrail;
    }

    private ParticleEmitter createDebris(AssetManager assetManager){
        ParticleEmitter debris = new ParticleEmitter("Debris", ParticleMesh.Type.Triangle, 15 * COUNT_FACTOR);
        debris.setSelectRandomImage(true);
        debris.setRandomAngle(true);
        debris.setRotateSpeed(FastMath.TWO_PI * 4);
        debris.setStartColor(new ColorRGBA(1f, 0.59f, 0.28f, (float) (1.0f / COUNT_FACTOR_F)));
        debris.setEndColor(new ColorRGBA(.5f, 0.5f, 0.5f, 0f));
        debris.setStartSize(.3f);
        debris.setEndSize(.3f);

//        debris.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
        debris.setParticlesPerSec(0);
        debris.setGravity(0, 12f, 0);
        debris.setLowLife(2.4f);
        debris.setHighLife(2.5f);
        debris.setInitialVelocity(new Vector3f(0, 7.5f, 0));
        debris.setVelocityVariation(.60f);
        debris.setImagesX(3);
        debris.setImagesY(3);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/Debris.png"));
        debris.setMaterial(mat);
        return debris;
    }

    private ParticleEmitter createShockwave(AssetManager assetManager){
        ParticleEmitter shockwave = new ParticleEmitter("Shockwave", ParticleMesh.Type.Triangle, 1 * COUNT_FACTOR);
//        shockwave.setRandomAngle(true);
        shockwave.setFaceNormal(Vector3f.UNIT_Y);
        shockwave.setStartColor(new ColorRGBA(.48f, 0.17f, 0.01f, (float) (.8f / COUNT_FACTOR_F)));
        shockwave.setEndColor(new ColorRGBA(.48f, 0.17f, 0.01f, 0f));

        shockwave.setStartSize(0f);
        shockwave.setEndSize(11);

        shockwave.setParticlesPerSec(0);
        shockwave.setGravity(0, 0, 0);
        shockwave.setLowLife(1f);
        shockwave.setHighLife(1f);
        shockwave.setInitialVelocity(new Vector3f(0, 0, 0));
        shockwave.setVelocityVariation(0f);
        shockwave.setImagesX(1);
        shockwave.setImagesY(1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/shockwave.png"));
        shockwave.setMaterial(mat);
        return shockwave;
    }
    
    private ParticleEmitter createSmoke(AssetManager assetManager){
        ParticleEmitter emit = new ParticleEmitter("Smoke", ParticleMesh.Type.Triangle, 300);
        emit.setParticlesPerSec(600);
        emit.setGravity(0, 0, 0);
        emit.setVelocityVariation(1);
        emit.setLowLife(2);
        emit.setHighLife(2);
        emit.setInitialVelocity(new Vector3f(0, 3f, 0));
        emit.setImagesX(15);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Smoke/Smoke.png"));
        emit.setMaterial(mat);
        emit.setEnabled(false);
        return emit;
    }



}