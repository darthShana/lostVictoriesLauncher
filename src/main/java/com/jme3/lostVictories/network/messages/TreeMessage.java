package com.jme3.lostVictories.network.messages;

import java.io.Serializable;
import java.util.UUID;

import static com.jme3.lostVictories.network.NetworkClient.uuid;

public class TreeMessage implements Serializable{
	
    private UUID id;
    private Vector location;
    private String type;
    private boolean standing;

    private TreeMessage(){}

    public TreeMessage(com.lostVictories.api.TreeMessage t) {
        id = uuid(t.getId());
        location = new Vector(t.getLocation());
        type = t.getType();
        standing = t.getStanding();
    }

    public UUID getId() {
        return id;
    }

    public Vector getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public boolean isStanding() {
        return standing;
    }
	

}
