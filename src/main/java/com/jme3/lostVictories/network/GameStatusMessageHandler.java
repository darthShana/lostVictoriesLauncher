package com.jme3.lostVictories.network;

import com.jme3.lostVictories.CharacterLoader;
import com.jme3.lostVictories.HeadsUpDisplayAppState;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.WorldRunner;
import com.jme3.lostVictories.structures.UnclaimedEquipmentNode;
import com.lostVictories.api.LostVictoryStatusMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class GameStatusMessageHandler {

    private final GameStatusMessageAssembler messageAssembler;
    private final Map<UUID, Long> receivedEquipmentMessages = new HashMap<>();
    private final WorldMap worldMap;
    private final HeadsUpDisplayAppState hud;
    private final CharacterLoader characterLoader;

    public GameStatusMessageHandler(WorldMap worldMap, CharacterLoader characterLoader, HeadsUpDisplayAppState hud){
        this.worldMap = worldMap;
        this.hud = hud;
        this.characterLoader = characterLoader;
        this.messageAssembler = new GameStatusMessageAssembler();
    }

    public void synchroniseWithServerView(){
        GameStatusUpdate popResponse = messageAssembler.popResponse();

        popResponse.getAllHouses().forEach(houseMessage->{
            if(worldMap.getHouse(houseMessage.getId())!=null){
                worldMap.getHouse(houseMessage.getId()).updateOwership(houseMessage);
            }
        });

        if(popResponse.getMessages()!=null){
            popResponse.getMessages().forEach(m->hud.addMessage(m));
        }
        if(popResponse.getGameStatistics()!=null){
            WorldRunner.get().setGameStatistics(popResponse.getGameStatistics());
        }
        if(popResponse.getAchivementStatus()!=null){
            WorldRunner.get().setAchiveemntStatus(popResponse.getAchivementStatus());
        }

        popResponse.getAllEquipment().forEach(eq->{
            receivedEquipmentMessages.put(eq.getId(), System.currentTimeMillis());
            if(!worldMap.hasUnclaimedEquipment(eq)){
                characterLoader.laodUnclaimedEquipment(eq);
            }
        });

        for(Iterator<Map.Entry<UUID, Long>> it = receivedEquipmentMessages.entrySet().iterator(); it.hasNext();){
            final Map.Entry<UUID, Long> next = it.next();
            final UnclaimedEquipmentNode equipment = (UnclaimedEquipmentNode) WorldMap.get().getEquipment(next.getKey());
            if(equipment!=null && System.currentTimeMillis()-next.getValue()>3000){
                equipment.destroy();
                worldMap.removeEquipment(equipment);
            }
        }
    }

    public void messageReceived(LostVictoryStatusMessage lostVictoryStatusMessage) {
        messageAssembler.messageReceived(lostVictoryStatusMessage);
    }
}
