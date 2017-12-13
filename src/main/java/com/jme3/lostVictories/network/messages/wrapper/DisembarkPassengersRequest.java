/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages.wrapper;

import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class DisembarkPassengersRequest extends LostVictoryMessage {
    private UUID vehicleID;

    private DisembarkPassengersRequest(){}

    public DisembarkPassengersRequest(UUID clientID, UUID vehicleID) {
        super(clientID);
        this.vehicleID = vehicleID;
    }


	public UUID getVehicleID() {
		return vehicleID;
	}
    
}