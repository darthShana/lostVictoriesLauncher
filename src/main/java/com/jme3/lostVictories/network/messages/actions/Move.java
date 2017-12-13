/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages.actions;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 * @author dharshanar
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="class")
public class Move extends Action {

    public Move() {
        super("move"); 
    }

    @Override
    public com.lostVictories.api.Action toMessage() {
        return com.lostVictories.api.Action.newBuilder().setActionType(com.lostVictories.api.Action.ActionType.MOVE).build();
    }
}
