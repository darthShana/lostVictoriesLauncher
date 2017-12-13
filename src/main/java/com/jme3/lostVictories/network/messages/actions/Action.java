/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages.actions;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.jme3.lostVictories.network.messages.Vector;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Set;

/**
 *
 * @author dharshanar
 */
@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="class")
public abstract class Action {
    
    protected String type;

    Action(){}
    
    public Action(String type) {
        this.type = type;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj == null) { return false; }
      if (obj == this) { return true; }
      if (obj.getClass() != getClass()) {
        return false;
      }
      Action rhs = (Action) obj;
      return new EqualsBuilder()
        .append(type, rhs.type)
        .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(15, 35)
            .append(type)
          .toHashCode();
    }
    
    
    public static Action idle(){
        return new Idle();
    }
    public static Action move(){
        return new Move();
    }
    public static Action crouch(){
        return new Crouch();
    }
    public static Action shoot(long shootTime, Vector[] targets){
        return new Shoot(shootTime, targets);
    }
    public static Action shoot(long shootTime, Set<Vector> targets){
        return new Shoot(shootTime, targets.toArray(new Vector[]{}));
    }
    public static Action setupWeapom() {return new SetupWeapon();}
    public static Action manualControl(String gear, String stearing) {return new ManualControl(gear, stearing);}
    public String getType() {
        return type;
    }

    public abstract com.lostVictories.api.Action toMessage();



}
