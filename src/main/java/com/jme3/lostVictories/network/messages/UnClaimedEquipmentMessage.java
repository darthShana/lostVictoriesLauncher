/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import java.io.Serializable;
import java.util.UUID;

import static com.jme3.lostVictories.network.NetworkClient.uuid;

/**
 *
 * @author dharshanar
 */
public class UnClaimedEquipmentMessage implements Serializable{
    
    private UUID id;
    private Weapon weapon;
    private Vector location;
    private Vector rotation;

    private UnClaimedEquipmentMessage(){}

    public UnClaimedEquipmentMessage(com.lostVictories.api.UnClaimedEquipmentMessage unClaimedEquipment) {
        id = uuid(unClaimedEquipment.getId());
        weapon = Weapon.valueOf(unClaimedEquipment.getWeapon().name());
        location = new Vector(unClaimedEquipment.getLocation());
        rotation = new Vector(unClaimedEquipment.getRotation());
    }

    public UUID getId() {
        return id;
    }
    
    public Vector getLocation(){
        return location;
    }
    
    public Vector getRotation(){
        return rotation;
    }
    
    public Weapon getWeapon(){
        return weapon;
    }
    
}
