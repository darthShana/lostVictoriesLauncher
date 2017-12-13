/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;

import com.jme3.lostVictories.network.messages.*;

import java.util.*;

/**
 *
 * @author dharshanar
 */
public class ServerResponse {

    private final UUID id;
    private final HashSet<CharacterMessage> charcters;
    private final HashSet<CharacterMessage> relatedCharcters;
    private final HashSet<HouseMessage> houses;
    private final HashSet<UnClaimedEquipmentMessage> equipment;
    private final HashSet<TreeGroupMessage> trees;
    private final List<String> messages;
    private final AchievementStatus achievementStatus;
    private final GameStatistics gameStatistics;

    ServerResponse(UUID id, HashSet<CharacterMessage> charcters, HashSet<CharacterMessage> relatedCharcters, HashSet<HouseMessage> houses, HashSet<UnClaimedEquipmentMessage> equipment, HashSet<TreeGroupMessage> trees, List<String> messages, AchievementStatus achievementStatus, GameStatistics gameStatistics) {
        this.id = id;
        this.charcters = charcters;
        this.relatedCharcters = relatedCharcters;
        this.houses = houses;
        this.equipment = equipment;
        this.trees = trees;
        this.messages = messages;
        this.achievementStatus = achievementStatus;
        this.gameStatistics = gameStatistics;
        
    }

    public Collection<CharacterMessage> getAllUnits() {
        return charcters;
    }

   public Collection<CharacterMessage> getAllRelatedCharacters() {
        return relatedCharcters;
    }

    public Iterable<UnClaimedEquipmentMessage> getAllEquipment() {
        return equipment;
    }

    public Set<HouseMessage> getAllHouses() {
        return houses;
    }

    public Set<TreeGroupMessage> getAllTrees() {
        return trees;
    }

    GameStatistics getGameStatistics() {
        return gameStatistics;
    }

    AchievementStatus getAchivementStatus() {
        return achievementStatus;
    }

    List<String> getMessages() {
        return messages;
    }

    public Collection<CharacterMessage> getAllRelatedUnits() {
        return relatedCharcters;
    }
    
}
