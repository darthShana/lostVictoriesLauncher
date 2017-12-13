package com.jme3.lostVictories.network.messages.wrapper;

import com.jme3.lostVictories.network.messages.CharacterMessage;


/**
 * Created by dharshanar on 1/04/17.
 */

public class CharacterStatusResponse extends LostVictoryMessage {

    private CharacterMessage unit;

    private CharacterStatusResponse(){}
    
    public CharacterStatusResponse(CharacterMessage unit) {
        this.unit = unit;
    }
    public CharacterMessage getCharacter() {
        return unit;
    }
}
