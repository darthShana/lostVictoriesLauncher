/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.app.state.AbstractAppState;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.network.GameStatusMessageHandler;
import com.jme3.lostVictories.network.NetworkClient;
import com.jme3.lostVictories.network.CharacterUpdateMessageHandler;
import com.jme3.lostVictories.network.CharacterUpdate;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.lostVictories.api.LostVictoryCheckout;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 *
 * @author dharshanar
 */
public class NetworkClientAppState extends AbstractAppState {
    
    private static NetworkClientAppState instance;
    private static float CLIENT_RANGE = 251;
    
    private final LostVictory app;
    private final NetworkClient networkClient;
    private final CharacterUpdateMessageHandler characterHandler;
    private final GameStatusMessageHandler gameStatusHandler;
    private long lastRunTime;



    public static NetworkClientAppState init(LostVictory app, NetworkClient networkClient, CharacterUpdateMessageHandler charcterSync, GameStatusMessageHandler gameStatusSync){
        instance = new NetworkClientAppState(app, networkClient, charcterSync, gameStatusSync);
        return instance;
    }
    
    public static NetworkClientAppState get(){
        return instance;
    }
    final Map<UUID, CharacterMessage> lastSent = new HashMap<>();
    private final long clientStartTime;

    protected NetworkClientAppState(LostVictory app, NetworkClient networkClient, CharacterUpdateMessageHandler characterSync, GameStatusMessageHandler gameStatusSync) {
        this.app = app;
        this.networkClient = networkClient;
        this.characterHandler = characterSync;
        this.gameStatusHandler = gameStatusSync;
        this.clientStartTime = System.currentTimeMillis();
    }

    @Override
    public void update(float tpf) {
        characterHandler.synchroniseWithServerView();
        gameStatusHandler.synchroniseWithServerView();

        if((System.currentTimeMillis()- lastRunTime)>networkClient.backoff) {
            Set<GameCharacterNode> charactersInRange = WorldMap.get().getAllCharacters();
            Point.Float p = new Point.Float(app.avatar.getLocalTranslation().x, app.avatar.getLocalTranslation().z);
            Rectangle.Float r = new Rectangle.Float(p.x - CLIENT_RANGE, p.y - CLIENT_RANGE, CLIENT_RANGE * 2, CLIENT_RANGE * 2);

            charactersInRange = charactersInRange.parallelStream()
                    .filter(c -> !c.isDead() && c.isControledLocaly() && r.contains(new Point.Float(c.getLocalTranslation().x, c.getLocalTranslation().z)))
                    .collect(Collectors.toSet());
            charactersInRange.add(app.avatar);

            filterCharactersAndSend(charactersInRange);
            lastRunTime = System.currentTimeMillis();
        }
            
    }

    void filterCharactersAndSend(Set<GameCharacterNode> charactersInRange) {
        charactersInRange.stream()
                .filter(hc-> needsToBeSent(hc))
                .map(c->c.toMessage())
                .forEach(cm -> {
                    networkClient.updateLocalCharacter(cm, (app.avatar!=null)?app.avatar.getIdentity():null, clientStartTime);
                    lastSent.put(cm.getId(), cm);
                });


    }

    boolean needsToBeSent(GameCharacterNode hc) {
        if(!lastSent.containsKey(hc.getIdentity())){
            lastSent.put(hc.getIdentity(), hc.toMessage());
        }
        return  !lastSent.get(hc.getIdentity()).hasBeenSentRecently() ||
                (!lastSent.get(hc.getIdentity()).isSameAs(hc) && lastSent.get(hc.getIdentity()).isOlderVersion(hc.getVersion()));
    }


    @Override
    public void cleanup() {
        System.out.println("shutting down network client");
        super.cleanup();
    }

        
    public LostVictoryCheckout checkoutSceenSynchronous(UUID avatar) throws InterruptedException {
        System.out.println("sending checkout request");
        LostVictoryCheckout lostVictoryCheckout = networkClient.checkoutScene(avatar);
        System.out.println("received checkout messages:"+ lostVictoryCheckout.getCharactersCount());
        return lostVictoryCheckout;
    }

    public void notifyDeath(UUID killer, UUID victim) {
        networkClient.deathNotification(killer, victim);
    }
    
    public void notifyGunnerDeath(UUID killer, UUID victim) {
        networkClient.gunnerDeathNotification(killer, victim);
    }

    public void addObjective(UUID characterID, UUID identity, String toMessage) {
        networkClient.addObjective(characterID, identity, toMessage);
    }

    public void requestEquipmentCollection(UUID equipmentID, UUID characterID) {
        networkClient.requestEquipmentCollection(equipmentID, characterID);
    }

    public void requestBoardVehicle(UUID vehicleID, UUID characterID){
        networkClient.boardVehicle(vehicleID, characterID);
    }

    public void disembarkPassengers(UUID identity) {
        networkClient.disembarkPassengers(identity);
    }


    
    
}
