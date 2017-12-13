/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;

import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.actions.Action;
import com.jme3.lostVictories.network.messages.wrapper.GameStatsResponse;

import java.util.*;
import java.util.stream.Collectors;

import static com.jme3.lostVictories.network.NetworkClient.uuid;

/**
 *
 * @author dharshanar
 */
public class ServerMessageAssembler {

    private Map<UUID, CharacterMessage> characters = new HashMap<>();
    private Map<UUID, UnClaimedEquipmentMessage> equipment = new HashMap<>();
    private Map<UUID, TreeGroupMessage> trees = new HashMap<>();
    private Map<UUID, HouseMessage> houses = new HashMap<>();
    private Map<UUID, CharacterMessage> relatedCharacters = new HashMap<>();
    private GameStatsResponse gameStatsResponse;

    public ServerMessageAssembler() {
        
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
            if(message.hasGameStatsResponse()){
                gameStatsResponse = fromMessage(message.getGameStatsResponse());
            }
            if(message.hasEquipmentStatusResponse()){
                UnClaimedEquipmentMessage em = fromMessage(message.getEquipmentStatusResponse().getUnClaimedEquipment());
                equipment.put(em.getId(), em);
            }
            if(message.hasTreeStatusResponse()){
                TreeGroupMessage tm = fromMessage(message.getTreeStatusResponse().getTreeGroup());
                trees.put(tm.getId(), tm);
            }
            if(message.hasHouseStatusResponse()){
                HouseMessage hm = fromMessage(message.getHouseStatusResponse().getHouse());
                houses.put(hm.getId(), hm);
            }
        }

    }

    private HouseMessage fromMessage(com.lostVictories.api.HouseMessage house) {
        return new HouseMessage(house);
    }

    private TreeGroupMessage fromMessage(com.lostVictories.api.TreeGroupMessage treeGroup) {
        Set<TreeMessage> set = treeGroup.getTreesList().stream().map(t -> new TreeMessage(t)).collect(Collectors.toSet());
        return new TreeGroupMessage(uuid(treeGroup.getId()), new Vector(treeGroup.getLocation()), set);
    }

    private UnClaimedEquipmentMessage fromMessage(com.lostVictories.api.UnClaimedEquipmentMessage unClaimedEquipment) {
        return new UnClaimedEquipmentMessage(unClaimedEquipment);
    }

    private GameStatsResponse fromMessage(com.lostVictories.api.GameStatsResponse gameStatsResponse) {
        return new GameStatsResponse(gameStatsResponse.getMessagesList(), new GameStatistics(gameStatsResponse.getGameStatistics()), new AchievementStatus(gameStatsResponse.getAchivementStatus()));
    }


    ServerResponse popResponces() {
        synchronized(this){
            ServerResponse ret = new ServerResponse(UUID.randomUUID(), 
                    new HashSet<>(characters.values()), 
                    new HashSet<>(relatedCharacters.values()),
                    new HashSet<>(houses.values()), 
                    new HashSet<>(equipment.values()), 
                    new HashSet<>(trees.values()),
                    (gameStatsResponse!=null)?gameStatsResponse.getMessages():null,
                    (gameStatsResponse!=null)?gameStatsResponse.getAchivementStatus():null,
                    (gameStatsResponse!=null)?gameStatsResponse.getGameStatistics():null);
            
            characters.clear();
            relatedCharacters.clear();
            houses.clear();
            equipment.clear();
            trees.clear();
            gameStatsResponse = null;
            return ret;
        }
    }

    private CharacterMessage fromMessage(com.lostVictories.api.CharacterMessage unit) {
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
