/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;

import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.actions.Action;

import java.util.*;
import java.util.stream.Collectors;

import static com.jme3.lostVictories.network.NetworkClient.uuid;

/**
 *
 * @author dharshanar
 */
public class CharacterUpdateMessageAssembler {

    private Map<UUID, CharacterMessage> characters = new HashMap<>();
    private Map<UUID, CharacterMessage> relatedCharacters = new HashMap<>();

    public CharacterUpdateMessageAssembler() {
        
    }

    public void append(com.lostVictories.api.LostVictoryMessage message) {
        synchronized(this){
            if(message.hasCharacterStatusResponse()){
                CharacterMessage cm = fromMessage(message.getCharacterStatusResponse().getUnit());
                characters.put(cm.getId(), cm);
            }
            if(message.hasRelatedCharacterStatusResponse()){
                CharacterMessage cm = fromMessage(message.getRelatedCharacterStatusResponse().getUnit());
                relatedCharacters.put(cm.getId(), cm);
            }

        }

    }


    CharacterUpdate popResponse() {
        synchronized(this){
            CharacterUpdate ret = new CharacterUpdate(UUID.randomUUID(),
                    new HashSet<>(characters.values()), 
                    new HashSet<>(relatedCharacters.values()));
            
            characters.clear();
            relatedCharacters.clear();
            return ret;
        }
    }

    public static CharacterMessage fromMessage(com.lostVictories.api.CharacterMessage unit) {
        Set actions = unit.getActionsList().stream().map(action -> {
            switch (action.getActionType()){
                case IDLE:
                    return Action.idle();
                case MOVE:
                    return Action.move();
                case SHOOT:
                    return Action.shoot(action.getShootTime(), action.getTargetsList().stream().map(v->new Vector(v)).collect(Collectors.toSet()));
                case CROUCH:
                    return Action.crouch();
                case SETUP_WEAPON:
                    return Action.setupWeapom();
                case MANUAL_CONTROL:
                    return Action.manualControl(action.getGear(), action.getSteering());
            }
            throw new RuntimeException("unknown action:"+action.getActionType());
        }).collect(Collectors.toSet());

        Set<String> completedObjectives = unit.getCompletedObjectivesList().stream().map(b -> uuid(b).toString()).collect(Collectors.toSet());

        CharacterMessage ret = new CharacterMessage(uuid(unit.getId()), new Vector(unit.getLocation()), new Vector(unit.getOrientation()), RankMessage.valueOf(unit.getRank().name()), actions, unit.getObjectivesMap(), completedObjectives, unit.getVersion());
        ret.setCountry(Country.valueOf(unit.getCountry().name()));
        ret.setWeapon(Weapon.valueOf(unit.getWeapon().name()));
        ret.setCommandingOfficer(uuid(unit.getCommandingOfficer()));
        ret.setBoardedVehicle(uuid(unit.getBoardedVehicle()));
        ret.setUnitsUnderCommand(unit.getUnitsUnderCommandList().stream().map(u->uuid(u)).collect(Collectors.toSet()));
        ret.setPassengers(unit.getPassengersList().stream().map(u->uuid(u)).collect(Collectors.toSet()));
        ret.setCheckoutClient(uuid(unit.getCheckoutClient()));
        ret.setCheckoutTime(unit.getCheckoutTime());
        ret.setType(CharacterType.valueOf(unit.getType().name()));
        ret.setDead(unit.getDead());
        ret.setEngineDamage(unit.getEngineDamaged());
        ret.setTimeOfDeath(unit.getTimeOfDeath());
        ret.setKillCount(unit.getKillCount());
        ret.setSquadType(SquadType.fromMessage(unit.getSquadType()));
        ret.setCreationTime(unit.getCreationTime());
        ret.setBusy(unit.getBusy());
        ret.setAttacking(unit.getAttacking());

        return ret;
    }

}
