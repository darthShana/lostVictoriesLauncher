/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages.actions;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author dharshanar
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="class")
public class Shoot extends Action {
    private long shootTime;
    private Vector[] targets;

    private Shoot() {}
    
    public Shoot(long shootTime, Vector[] targets) {
        super("shoot");
        this.shootTime = shootTime;
        this.targets = targets;
    }

    public Shoot(long shootStartTime, Vector3f[] currentTargets) {
        super("shoot");
        this.shootTime = shootStartTime;
        this.targets = new Vector[currentTargets.length];
        for(int i = 0;i<currentTargets.length;i++){
            this.targets[i] = new Vector(currentTargets[i].x, currentTargets[i].y, currentTargets[i].z);
        }
    }

    public long getTime() {
        return shootTime;
    }

    public Vector3f[] getTargets() {
        Vector3f[] ret = new Vector3f[targets.length];
        for(int i = 0;i<targets.length;i++){
            ret[i] = new Vector3f(targets[i].x, targets[i].y, targets[i].z);
        }
        return ret;
    }

    @Override
    public com.lostVictories.api.Action toMessage() {
        com.lostVictories.api.Action.Builder builder = com.lostVictories.api.Action.newBuilder()
                .setActionType(com.lostVictories.api.Action.ActionType.SHOOT).setShootTime(shootTime);
        for(Vector v:targets){
            builder.addTargets(v.toMessage());
        }
        return builder.build();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) { return false; }
      if (obj == this) { return true; }
      if (obj.getClass() != getClass()) {
        return false;
      }
      Shoot rhs = (Shoot) obj;
      return new EqualsBuilder()
        .appendSuper(super.equals(obj))
        .append(shootTime, rhs.shootTime)
        .append(targets, rhs.targets)
        .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(15, 35)
            .append(shootTime)
            .append(targets)
          .toHashCode();
    }
    
}
