package com.jme3.lostVictories.network.messages.wrapper;

import java.util.UUID;

public class PassengerDeathNotificationRequest extends LostVictoryMessage {

	private UUID killer;
    private UUID victim;

    private PassengerDeathNotificationRequest(){}

    public PassengerDeathNotificationRequest(UUID clientID, UUID killer, UUID victim) {
        super(clientID);
        this.killer = killer;
        this.victim = victim;        
    }
    
    public UUID getKiller() {
		return killer;
	}
    
    public UUID getVictim() {
		return victim;
	}
}
