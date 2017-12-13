package com.jme3.lostVictories.network.messages.wrapper;

import com.jme3.lostVictories.network.messages.TreeGroupMessage;

import java.util.Set;

/**
 * Created by dharshanar on 1/04/17.
 */
public class TreeStatusResponse extends LostVictoryMessage {
    private Set<TreeGroupMessage> allTrees;

    private TreeStatusResponse(){}
    
    public TreeStatusResponse(Set<TreeGroupMessage> allTrees) {
        this.allTrees = allTrees;
    }

    public Set<TreeGroupMessage> getTrees() {
        return allTrees;
    }
}
