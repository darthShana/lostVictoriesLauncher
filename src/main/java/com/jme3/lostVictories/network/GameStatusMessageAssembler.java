package com.jme3.lostVictories.network;

import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.wrapper.GameStatsResponse;
import com.lostVictories.api.LostVictoryStatusMessage;

import java.util.*;
import java.util.stream.Collectors;

import static com.jme3.lostVictories.network.NetworkClient.uuid;

public class GameStatusMessageAssembler {

    private Map<UUID, UnClaimedEquipmentMessage> equipment = new HashMap<>();
    private Map<UUID, TreeGroupMessage> trees = new HashMap<>();
    private Map<UUID, HouseMessage> houses = new HashMap<>();
    private GameStatsResponse gameStatsResponse;

    public void messageReceived(LostVictoryStatusMessage message) {

        synchronized (this){
            if(message.hasGameStatsResponse()){
                gameStatsResponse = fromMessage(message.getGameStatsResponse());
            }
            if(message.hasEquipmentStatusResponse()){
                UnClaimedEquipmentMessage em = fromMessage(message.getEquipmentStatusResponse().getUnClaimedEquipment());
                equipment.put(em.getId(), em);
            }
            if(message.hasTreeStatusResponse()){
                TreeGroupMessage tm = fromMessage(message.getTreeStatusResponse());
                trees.put(tm.getId(), tm);
            }
            if(message.hasHouseStatusResponse()){
                HouseMessage hm = fromMessage(message.getHouseStatusResponse());
                houses.put(hm.getId(), hm);
            }
        }
    }

    GameStatusUpdate popResponse() {
        synchronized(this) {
            GameStatusUpdate ret = new GameStatusUpdate(new HashSet<>(houses.values()),
                    new HashSet<>(equipment.values()),
                    new HashSet<>(trees.values()),
                    (gameStatsResponse!=null)?gameStatsResponse.getMessages():null,
                    (gameStatsResponse!=null)?gameStatsResponse.getAchivementStatus():null,
                    (gameStatsResponse!=null)?gameStatsResponse.getGameStatistics():null);

            houses.clear();
            equipment.clear();
            trees.clear();
            gameStatsResponse = null;
            return ret;
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
}
