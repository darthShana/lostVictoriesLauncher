/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import com.jme3.math.Vector3f;

import java.io.Serializable;
import java.util.UUID;

import static com.jme3.lostVictories.network.NetworkClient.uuid;

/**
 *
 * @author dharshanar
 */
public class HouseMessage implements Serializable{
    
    private UUID id;
    private String type;
    private Vector location;
    private Quaternion rotation;
    Country owner;
    Country contestingOwner;
    CaptureStatus captureStatus;
    Long statusChangeTime;
    
    private HouseMessage(){}
    
    public HouseMessage(String type, Vector location, Quaternion rotation) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.location = location;
        this.rotation = rotation;
        this.captureStatus = CaptureStatus.NONE;
    }

    public HouseMessage(com.lostVictories.api.HouseMessage house) {
        this.id = uuid(house.getId());
        this.type = house.getType();
        this.location = new Vector(house.getLocation());
        this.rotation = new Quaternion(house.getRotation());
        if(house.getOwner()!= com.lostVictories.api.Country.NEUTRAL) {
            this.owner = Country.valueOf(house.getOwner().name());
        }
        if(house.getContestingOwner()!= com.lostVictories.api.Country.NEUTRAL){
            this.contestingOwner = Country.valueOf(house.getContestingOwner().name());
        }
        if(house.getCaptureStatus()!=null) {
            this.captureStatus = CaptureStatus.valueOf(house.getCaptureStatus().name());
        }
        this.statusChangeTime = house.getStatusChangeTime();
    }

    public UUID getId(){
        return id;
    }

    public String getType() {
        return type;
    }

    public Vector3f getLocalTranslation() {
        return new Vector3f(location.x, location.y, location.z);
    }

    public com.jme3.math.Quaternion getLocalRotation() {
        return new com.jme3.math.Quaternion(rotation.x, rotation.y, rotation.z, rotation.w);
    }

    public CaptureStatus getCaptureStatus() {
        return captureStatus;
    }
    
    public void setCaptureStatus(CaptureStatus captureStatus){
        this.captureStatus = captureStatus;
    }

    public Country getContestingOwner() {
        return contestingOwner;
    }

    public Country getOwner() {
        return owner;
    }
    
    public void setOwner(Country owner){
        this.owner = owner;
    }
    
    
    
}
