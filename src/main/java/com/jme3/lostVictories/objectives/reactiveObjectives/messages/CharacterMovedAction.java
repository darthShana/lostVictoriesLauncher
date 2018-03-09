package com.jme3.lostVictories.objectives.reactiveObjectives.messages;

import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;

public class CharacterMovedAction {
    private AICharacterNode character;
    private GameCharacterNode intruder;

    public CharacterMovedAction(AICharacterNode characterNode, GameCharacterNode intruder) {
        this.character = characterNode;
        this.intruder = intruder;
    }

    public AICharacterNode getCharacter() {
        return character;
    }

    public GameCharacterNode getIntruder() {
        return intruder;
    }
}
