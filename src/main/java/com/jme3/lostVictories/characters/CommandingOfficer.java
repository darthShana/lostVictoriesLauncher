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

    public List<Commandable> getCharactersUnderCommand();

    public void addCharactersUnderCommand(Set<Commandable> cc);
    public void addCharactersUnderCommand(Commandable c);
    
    public EnemyActivityReport getEnemyActivity();

    public void removeCharacterUnderCommand(Commandable aThis);

    public void removeAllUnits();

    public boolean isTeam(Weapon... weapons);

    
}
