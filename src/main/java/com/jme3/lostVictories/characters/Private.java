/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.lostVictories.CharcterParticleEmitter;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.characters.blenderModels.BlenderModel;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class Private extends Soldier{

    public Private(UUID id, Node model, Country country, CommandingOfficer commandingOfficer, Vector3f worldCoodinates, Vector3f rotation, Node rootNode, BulletAppState bulletAppState, CharcterParticleEmitter emitter, ParticleManager particleManager, NavigationProvider pathFinder, AssetManager assetManager, BlenderModel m, BehaviorControler behaviorControler, Camera camera) {
        super(id, model, country, commandingOfficer, worldCoodinates, rotation, rootNode, bulletAppState, emitter, particleManager, pathFinder, assetManager, m, behaviorControler, camera);
    }


        
}
