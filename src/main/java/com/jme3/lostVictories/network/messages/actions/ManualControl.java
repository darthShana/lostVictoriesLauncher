/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages.actions;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author dharshanar
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="class")
public class ManualControl extends Action{
    private String gear;
    private String steering;

    private ManualControl(){}
    
    public ManualControl(String gear, String steering) {
        super("manualControl");
        this.gear = gear;
        this.steering = steering;
    }

    public String getGear() {
        return gear;
    }

    public String getSteering() {
        return steering;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj == null) { return false; }
      if (obj == this) { return true; }
      if (obj.getClass() != getClass()) {
        return false;
      }
      ManualControl rhs = (ManualControl) obj;
      return new EqualsBuilder()
        .appendSuper(super.equals(obj))
        .append(gear, rhs.gear)
        .append(steering, rhs.steering)
        .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(15, 35)
            .append(gear)
            .append(steering)
          .toHashCode();
    }

    @Override
    public com.lostVictories.api.Action toMessage() {
        return com.lostVictories.api.Action.newBuilder()
                .setActionType(com.lostVictories.api.Action.ActionType.MANUAL_CONTROL)
                .setGear(gear)
                .setSteering(steering).build();
    }
}
