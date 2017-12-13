package com.jme3.lostVictories.network.messages.wrapper;

import com.jme3.lostVictories.network.messages.CharacterMessage;


/**
 * Created by dharshanar on 1/04/17.
 */
             
public class RelatedCharacterStatusResponse extends LostVictoryMessage {

    private CharacterMessage unit;

    private RelatedCharacterStatusResponse(){}
    
    public RelatedCharacterStatusResponse(CharacterMessage unit) {
        this.unit = unit;
    }
    public CharacterMessage getCharacter() {
        return unit;
    }
}
