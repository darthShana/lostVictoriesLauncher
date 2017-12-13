package com.jme3.lostVictories.network.messages.wrapper;

import com.jme3.lostVictories.network.messages.CharacterMessage;

import java.util.UUID;


public class UpdateCharactersRequest extends LostVictoryMessage {
	
    private CharacterMessage character;
    private UUID avatar;
    private long clientStartTime;

	private UpdateCharactersRequest(){}

//	public UpdateCharactersRequest(UUID clientID) {
//		super(clientID);
//	}
	
        public UpdateCharactersRequest(UUID clientID, CharacterMessage character, UUID avatar, long clientStartTime){
            super(clientID);
            this.avatar = avatar;
            this.character = character;
            this.clientStartTime = clientStartTime;
        }
        
//	public UpdateCharactersRequest(UUID clientID, Set<CharacterMessage> characters, CharacterMessage avatar) {
//		super(clientID);
//		this.characters.addAll(characters);
//		this.avatar = avatar.getId();
//	}

	public CharacterMessage getCharacter(){
            return character;
	}
	
	public UUID getAvatar(){
            return avatar;
	}

}
