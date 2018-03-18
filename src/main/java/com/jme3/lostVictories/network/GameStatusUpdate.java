package com.jme3.lostVictories.network;

import com.jme3.lostVictories.network.messages.*;

import java.util.List;
import java.util.Set;

public class GameStatusUpdate {

    private final Set<HouseMessage> houses;
    private final Set<UnClaimedEquipmentMessage> equipment;
    private final Set<TreeGroupMessage> trees;
    private final List<String> messages;
    private final AchievementStatus achievementStatus;
    private final GameStatistics gameStatistics;

    public GameStatusUpdate(Set<HouseMessage> houses, Set<UnClaimedEquipmentMessage> equipment, Set<TreeGroupMessage> trees, List<String> messages, AchievementStatus achievementStatus, GameStatistics gameStatistics){
        this.houses = houses;
        this.equipment = equipment;
        this.trees = trees;
        this.messages = messages;
        this.achievementStatus = achievementStatus;
        this.gameStatistics = gameStatistics;
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

}
