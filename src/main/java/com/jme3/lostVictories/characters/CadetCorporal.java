/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import akka.actor.ActorRef;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.lostVictories.CharcterParticleEmitter;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.characters.blenderModels.BlenderModel;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.network.messages.SquadType;
import com.jme3.lostVictories.objectives.AttackTargetsInDirection;
import com.jme3.lostVictories.objectives.CoverFront;
import com.jme3.lostVictories.objectives.FollowCommander;
import com.jme3.lostVictories.structures.Pickable;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;

import java.util.*;

/**
 *
 * @author dharshanar
 */
public class CadetCorporal extends Soldier implements CommandingOfficer{

    List<Commandable> charactersToCommand = new ArrayList<Commandable>();

    CadetCorporal(){}
    
    public CadetCorporal(UUID id, Node player, Country country, CommandingOfficer commandingOfficer, Vector3f position, Vector3f rotation, SquadType squadType, Node rootNode, BulletAppState bulletAppState, CharcterParticleEmitter characterParticleEmitters, ParticleManager particleManager, NavigationProvider navMeshPathfinder, AssetManager assetManager, BlenderModel model, BehaviorControler behaviorControler, ActorRef shootssFiredListener) {
        super(id, player, country, commandingOfficer, position, rotation, squadType, rootNode, bulletAppState, characterParticleEmitters, particleManager, navMeshPathfinder, assetManager, model, behaviorControler, shootssFiredListener);
        if(getWeapon()==Weapon.cannon()){
            throw new RuntimeException("invalid weapon");
        }
    }

    @Override
    public void travel(Vector3f contactPoint, GameCharacterNode issuingCharacter) {
        if(!isUnderChainOfCommandOf(issuingCharacter, 5)){
            return;
        }
        
        for(Commandable unit:getCharactersUnderCommand()){
            Vector3f f = new Vector3f(1, 0, 1).mult((float)Math.random());
            unit.addObjective(new FollowCommander(f, 3));
        }
        
        super.travel(contactPoint, issuingCharacter);
    }

    @Override
    public void cover(Vector3f mousePress, Vector3f mouseRelease, GameCharacterNode issuingCharacter) {
        if(!isUnderChainOfCommandOf(issuingCharacter, 5)){
            return;
        }
        addObjective(new CoverFront(mousePress, mouseRelease, rootNode));
        
    }

    @Override
    public void attack(Vector3f target, GameCharacterNode issuingCharacter) {
        if(!isUnderChainOfCommandOf(issuingCharacter, 5)){
            return;
        }
        behaviorControler.addObjective(new AttackTargetsInDirection(target, rootNode));
    }

    @Override
    public void collect(Pickable pickable, GameCharacterNode issuingCharacter) {
        final List<Commandable> charactersUnderCommand = getCharactersUnderCommand();
        if(!charactersUnderCommand.isEmpty()){
            charactersUnderCommand.iterator().next().collect(pickable, this);
        }else{
            super.collect(pickable, issuingCharacter);
        }
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
        this.charactersToCommand.clear();
    }

    @Override
    public Rank getRank() {
        return Rank.CADET_CORPORAL;
    }

    public boolean isTeam(Weapon... weapons) {
        if(hasWeapon(weapons)){
            return true;
        }
        
        for(Commandable n:charactersToCommand){
            if(n instanceof GameCharacterNode){
                if(((GameCharacterNode)n).hasWeapon(weapons)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasProjectilePathWeapon() {
        if(super.hasProjectilePathWeapon()){
            return true;
        }
        for(Commandable n:charactersToCommand){
            if(n instanceof GameCharacterNode){
                if(((GameCharacterNode)n).hasProjectilePathWeapon()){
                    return true;
                }
            }
        }
        return false;
    }

    public Commandable findSoldierWithWeapon(Weapon weapon) {
        for(Commandable c: getCharactersUnderCommand()){
            if(c.getWeapon() == weapon){
                return c;
            }
        }
        return null;
    }

    @Override
    public Commandable select(Commandable selectedCharacter) {
        Geometry g = createSubselectionMarker();        
        for(Commandable n: getCharactersUnderCommand()){
            if(n instanceof GameCharacterNode){
                if(((GameCharacterNode)n).getChild("subselection")==null){
                    ((GameCharacterNode)n).attachChild(g.clone());
                }
            }
        }
        
        return super.select(selectedCharacter);
    }

    @Override
    public Geometry unSelect() {
        for(Commandable n: getCharactersUnderCommand()){
            if(n instanceof GameCharacterNode && ((GameCharacterNode)n).getChild("subselection")!=null){
                ((GameCharacterNode)n).getChild("subselection").removeFromParent();
            }
        }
        return super.unSelect();
    }
        
    protected Geometry createSubselectionMarker() {
        Cylinder sphere = new Cylinder(12, 12, 1f, .2f, false, false);
        Geometry mark = new Geometry("subselection", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", new ColorRGBA(1, 1, 1, .3f));
        mark_mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mark.setQueueBucket(RenderQueue.Bucket.Transparent);
        mark.setMaterial(mark_mat);
        mark.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
        return mark;
    }
    
    protected Geometry createHighlightMarker() {
        Cylinder sphere = new Cylinder(12, 12, 1f, .2f, false, false);
        Geometry mark = new Geometry("highlight", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", new ColorRGBA(1, 1, 1, .2f));
        mark_mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mark.setQueueBucket(RenderQueue.Bucket.Transparent);
        mark.setMaterial(mark_mat);
        mark.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
        return mark;
    }

    public void highlightSquad() {
        Geometry g = createHighlightMarker();
        if(getChild("highlight")==null){
            attachChild(g);
        }
        for(Commandable n: getCharactersUnderCommand()){
            if(n instanceof GameCharacterNode && ((GameCharacterNode)n).getChild("highlight")==null){
                ((GameCharacterNode)n).attachChild(g.clone());
            }
        }
    }

    public void unhighlightSquad() {
        final Spatial highlight = getChild("highlight");
        if(highlight!=null){
            highlight.removeFromParent();
        }
        for(Commandable n: getCharactersUnderCommand()){
            if(n instanceof GameCharacterNode && ((GameCharacterNode)n).getChild("highlight")!=null){
                ((GameCharacterNode)n).getChild("highlight").removeFromParent();
            }
        }
    }
  
}
