/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.network.messages.SquadType;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.lostVictories.structures.Pickable;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

import java.util.Map;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public interface Commandable {
    
    public UUID getIdentity();
    
    Map<UUID, Objective> getAllObjectives();
    
    public void addObjective(Objective objective);
    
    public Vector3f getLocalTranslation();

    public void cover(Vector3f mousePress, Vector3f mouseRelease, GameCharacterNode issuingCharacter);
    
    public void travel(Vector3f contactPoint, GameCharacterNode issuingCharacter);
    
    public void follow(GameCharacterNode toFollow, GameCharacterNode issuingCharacter);
    
    public void attack(Vector3f target, GameCharacterNode issuingCharacter);
    
    public void collect(Pickable pickable, GameCharacterNode issuingCharacter);
    
    public void requestBoarding(GameVehicleNode key, GameCharacterNode issuingCharacter);
    
    public int getKillCount();
        
    public String getUnitName();
    
    public void setUnitName(String unitName);
    
    public Weapon getWeapon();
    
    public boolean isSelected();

    public boolean isBusy();

    public Geometry unSelect();

    public Commandable select(Commandable selectedCharacter);

    public boolean isUnderChainOfCommandOf(GameCharacterNode avatar, int i);

    public Rank getRank();

    public SquadType getSquadType(SquadType squadType, boolean expanded);

    public Country getCountry();

    public boolean isAttacking();

    public boolean isDead();

    public int getCurrentStrength();

    public boolean hasBoardedVehicle();

    public boolean isHuman();
}
