/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 *
 * @author dharshanar
 */
public class Quaternion implements Serializable{
    float x;
    float y;
    float z;
    float w;
    
    @JsonCreator
    public Quaternion(@JsonProperty("x")float x, @JsonProperty("y")float y, @JsonProperty("z")float z, @JsonProperty("w")float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Quaternion(com.lostVictories.api.Quaternion rotation) {
        this.x = rotation.getX();
        this.y = rotation.getY();
        this.z = rotation.getZ();
        this.w = rotation.getW();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Vector rhs = (Vector) obj;
        return new EqualsBuilder()
                .append(w, rhs.x)
                .append(x, rhs.x)
                .append(y, rhs.y)
                .append(z, rhs.z)
                .isEquals();
    }

    @Override
   public int hashCode() {
     return new HashCodeBuilder(17, 37).
       append(w).
       append(x).
       append(y).
       append(z).
       toHashCode();
   }
    
}
