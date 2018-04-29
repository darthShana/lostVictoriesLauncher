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
    
    UUID getIdentity();

    Map<UUID, Objective> getAllObjectives();

    void addObjective(Objective objective);

    Vector3f getLocalTranslation();

    void cover(Vector3f mousePress, Vector3f mouseRelease, GameCharacterNode issuingCharacter);

    void travel(Vector3f contactPoint, GameCharacterNode issuingCharacter);

    void follow(GameCharacterNode toFollow, GameCharacterNode issuingCharacter);

    void attack(Vector3f target, GameCharacterNode issuingCharacter);

    void collect(Pickable pickable, GameCharacterNode issuingCharacter);

    void requestBoarding(GameVehicleNode key, GameCharacterNode issuingCharacter);

    int getKillCount();

    String getUnitName();

    void setUnitName(String unitName);

    Weapon getWeapon();

    boolean isSelected();

    boolean isBusy();

    Geometry unSelect();

    Commandable select(Commandable selectedCharacter);

    boolean isUnderChainOfCommandOf(GameCharacterNode avatar, int i);

    Rank getRank();

    SquadType getSquadType();

    Country getCountry();

    boolean isAttacking();

    boolean isDead();

    int getCurrentStrength();

    boolean hasBoardedVehicle();

    boolean isHuman();

    EnemyActivityReport getEnemyActivity();
}
