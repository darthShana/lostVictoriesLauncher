/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.objectives.EnemyActivityReport;

import java.util.List;
import java.util.Set;

/**
 *
 * @author dharshanar
 */
public interface CommandingOfficer extends Commandable{

    List<Commandable> getCharactersUnderCommand();

    void addCharactersUnderCommand(Set<Commandable> cc);
    void addCharactersUnderCommand(Commandable c);
    

    void removeCharacterUnderCommand(Commandable aThis);

    void removeAllUnits();

    boolean isTeam(Weapon... weapons);

    
}
