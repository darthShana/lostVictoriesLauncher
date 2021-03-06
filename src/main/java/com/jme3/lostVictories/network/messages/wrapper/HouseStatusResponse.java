package com.jme3.lostVictories.network.messages.wrapper;

import com.jme3.lostVictories.network.messages.HouseMessage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dharshanar on 1/04/17.
 */
public class HouseStatusResponse extends LostVictoryMessage {
    private Set<HouseMessage> allHouses = new HashSet<>();

    private HouseStatusResponse(){}
    
    public HouseStatusResponse(Collection<HouseMessage> allHouses) {
        this.allHouses.addAll(allHouses);
    }

    public Set<HouseMessage> getHouses() {
        return allHouses;
    }
}
