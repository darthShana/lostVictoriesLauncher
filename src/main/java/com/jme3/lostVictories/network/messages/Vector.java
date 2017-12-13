/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jme3.math.Vector3f;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 *
 * @author dharshanar
 */
public class Vector implements Serializable{
   	
    public float x;
    public float y;
    public float z;

    public Vector(double x, double y, double z) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
    }

    @JsonCreator
    public Vector(@JsonProperty("x")float x, @JsonProperty("y")float y, @JsonProperty("z")float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vector(Vector3f v){
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public Vector(com.lostVictories.api.Vector vector) {
        this.x = vector.getX();
        this.y = vector.getY();
        this.z = vector.getZ();
    }

    @Override
    public String toString() {
            return x+","+y+","+z;
    }
     
    public Vector3f toVector(){
        return new Vector3f(x, y, z);
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
                 .append(x, rhs.x)
                 .append(y, rhs.y)
                 .append(z, rhs.z)
                 .isEquals();
    }

    @Override
   public int hashCode() {
     return new HashCodeBuilder(17, 37).
       append(x).
       append(y).
       append(z).
       toHashCode();
   }


    public com.lostVictories.api.Vector toMessage() {
        return com.lostVictories.api.Vector.newBuilder()
                .setX(x).setY(y).setZ(z).build();
    }
}
