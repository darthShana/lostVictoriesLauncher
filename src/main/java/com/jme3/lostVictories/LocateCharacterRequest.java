package com.jme3.lostVictories;

import com.jme3.lostVictories.characters.GameCharacterNode;

public class LocateCharacterRequest {

    private GameCharacterNode character;

    LocateCharacterRequest(GameCharacterNode character){
        this.character = character;
    }

    public GameCharacterNode getCharacter() {
        return character;
    }
}
