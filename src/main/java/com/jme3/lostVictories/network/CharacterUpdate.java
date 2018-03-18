/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;

import com.jme3.lostVictories.network.messages.*;

import java.util.*;

/**
 *
 * @author dharshanar
 */
public class CharacterUpdate {

    private final UUID id;
    private final HashSet<CharacterMessage> charcters;
    private final HashSet<CharacterMessage> relatedCharcters;


    CharacterUpdate(UUID id, HashSet<CharacterMessage> charcters, HashSet<CharacterMessage> relatedCharacters) {
        this.id = id;
        this.charcters = charcters;
        this.relatedCharcters = relatedCharacters;

        
    }

    public Collection<CharacterMessage> getAllUnits() {
        return charcters;
    }


    public Collection<CharacterMessage> getAllRelatedUnits() {
        return relatedCharcters;
    }
    
}
