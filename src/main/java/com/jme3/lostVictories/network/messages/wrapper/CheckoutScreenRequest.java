package com.jme3.lostVictories.network.messages.wrapper;

import java.util.UUID;

public class CheckoutScreenRequest extends LostVictoryMessage {
    
	public UUID avatar;

	private CheckoutScreenRequest(){}

    public CheckoutScreenRequest(UUID clientID, UUID avatar) {
        super(clientID);
        this.avatar = avatar;
    }
}
