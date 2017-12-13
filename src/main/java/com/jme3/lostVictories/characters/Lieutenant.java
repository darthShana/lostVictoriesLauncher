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
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.network.messages.SquadType;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class Lieutenant extends Soldier implements CommandingOfficer{

    List<Commandable> charactersToCommand = new ArrayList<>();

    public Lieutenant(UUID id, Node model, Country country, CommandingOfficer commandingOfficer, Vector3f worldCoodinates, Vector3f rotation, Node rootNode, BulletAppState bulletAppState, CharcterParticleEmitter emitter, ParticleManager particleManager, NavigationProvider pathFinder, AssetManager assetManager, BlenderModel m, BehaviorControler behaviorControler, Camera camera) {
        super(id, model, country, commandingOfficer, worldCoodinates, rotation, rootNode, bulletAppState, emitter, particleManager, pathFinder, assetManager, m, behaviorControler, camera);
    }
    
    public List<Commandable> getCharactersUnderCommand() {
        return new ArrayList<>(charactersToCommand);
    }

    
    public void addCharactersUnderCommand(Set<Commandable> cc) {
        charactersToCommand.addAll(cc);
    }
    
    public void addCharactersUnderCommand(Commandable c) {
        charactersToCommand.add(c);
    }

    public void removeCharacterUnderCommand(Commandable aThis) {
        charactersToCommand.remove(aThis);
    }

    public void removeAllUnits() {
        charactersToCommand.clear();
    }

    

    @Override
    public Rank getRank() {
        return Rank.LIEUTENANT;
    }

    public CadetCorporal findWithNoEquipment() {
        for(Commandable n: getCharactersUnderCommand()){
            final SquadType squadType = n.getSquadType(SquadType.RIFLE_TEAM, false);
            if(n instanceof CadetCorporal && squadType!=SquadType.ANTI_TANK_GUN && squadType!=SquadType.ARMORED_VEHICLE){
                return (CadetCorporal) n;
            }
        }
        return null;
    }

    public boolean isTeam(Weapon... weapons) {
        return false;
    }
    
}
