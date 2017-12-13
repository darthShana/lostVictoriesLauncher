package com.jme3.lostVictories.network.messages;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TreeGroupMessage implements Serializable{

    private UUID id;
    private Vector location;
    private Set<TreeMessage> trees = new HashSet<TreeMessage>();

    private TreeGroupMessage(){}

    public TreeGroupMessage(UUID id, Vector location, Set<TreeMessage> trees){
        this.id = id;
        this.location = location;
        this.trees = trees;
    }
    public UUID getId() {
        return id;
    }

    public Vector getLocation(){
        return location;
    }

    public Set<TreeMessage> getTrees(){
        return trees;
    }
	
        

}
