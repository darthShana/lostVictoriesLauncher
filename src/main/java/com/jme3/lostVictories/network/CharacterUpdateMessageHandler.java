/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;

import com.jme3.lostVictories.*;
import com.jme3.lostVictories.characters.*;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.structures.UnclaimedEquipmentNode;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author dharshanar
 */
public class CharacterUpdateMessageHandler {

    private final StructureLoader structureLoader;

    private final UUID clientID;
    private final LostVictory app;
    private final CharacterLoader characterLoader;

    private final ParticleManager particleManager;
    private final CharacterUpdateMessageAssembler characterUpdateMessageAssembler;
    private final Map<UUID, Long> receivedCharacterMessages = new HashMap<>();


    Map<UUID, CharacterMessage> relatedCharacters = new HashMap<>();

    public CharacterUpdateMessageHandler(LostVictory app, CharacterLoader characterLoader, StructureLoader structureLoader, UUID clientID, ParticleManager particleManager) {
        this.structureLoader = structureLoader;
        this.clientID = clientID;
        this.app = app;
        this.characterLoader = characterLoader;
        this.particleManager = particleManager;
        this.characterUpdateMessageAssembler = new CharacterUpdateMessageAssembler();
    }

    public void synchroniseWithServerView(){
        final WorldMap worldMap = WorldMap.get();
        final CharacterUpdate popResponces = characterUpdateMessageAssembler.popResponse();

        popResponces.getAllRelatedUnits().forEach(c->{
            c.setCreationTime(System.currentTimeMillis());
            relatedCharacters.put(c.getId(), c);
        });
        relatedCharacters = relatedCharacters.values().stream()
                .filter(value->System.currentTimeMillis()-value.getCreationTime()<5000)
                .collect(Collectors.toMap(r->r.getId(), Function.identity()));

        popResponces.getAllUnits().forEach(msg -> {
            receivedCharacterMessages.put(msg.getId(), System.currentTimeMillis());

            GameCharacterNode clientView = worldMap.getCharacter(msg.getId());
            if(clientView!=null){
                if(msg.isDead()){
                    if(!clientView.isDead()){
                        clientView.playDestroyAnimation(clientView.getPositionToTarget(clientView));
                        clientView.doDeathEffects();
                    }
                    worldMap.removeCharacter(clientView);
                }else{
                    if(!clientView.isSameRank(msg)){
                        particleManager.playPromotionEffect(clientView);
                        characterLoader.destroyCharacter(clientView);
                    }else if(!clientView.hasSameWeapon(msg)){
                        characterLoader.destroyCharacter(clientView);
                    }else{
                        updateOnSceneCharacter(clientView, msg, worldMap);
                    }
                }
            }


            if(worldMap.getCharacter(msg.getId())==null && !msg.isDead()){
                try {
                    GameCharacterNode n = characterLoader.loadCharacter(msg, clientID);
                    n.checkForNewObjectives(msg.getObjectives());
                    if(app.chaseCameraAppState != null && app.chaseCameraAppState.isSelected(n.getIdentity())){
                        app.chaseCameraAppState.selectCharacter(n);
                    }
                    if(n instanceof AvatarCharacterNode){
                        System.out.println("reincatnate avatar:"+msg.getLocation());
                        app.setAvatar((AvatarCharacterNode) n);
                        app.avatar.updateHeadsUpDisplay();

                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        for(Iterator<Entry<UUID, Long>> it = receivedCharacterMessages.entrySet().iterator();it.hasNext();){
            final Entry<UUID, Long> next = it.next();
            final GameCharacterNode character = WorldMap.get().getCharacter(next.getKey());
            if(character!=null && System.currentTimeMillis()-next.getValue()>4000){
                characterLoader.destroyCharacter(character);
                it.remove();
            }
        }

    }

    long messagesReceivedCounter;


    public void messageReceived(com.lostVictories.api.LostVictoryMessage value) {
        characterUpdateMessageAssembler.append(value);
        messagesReceivedCounter++;
    }

    public long getMessagesReceivedCouunt(){
        return messagesReceivedCounter;
    }

    private boolean hasSameUnits(CommandingOfficer c, Set<UUID> unitsUnderCommand, WorldMap worldMap, Map<UUID, CharacterMessage> relatedCharacters) {
        Set<UUID> localSquad = new HashSet<>();
        for(Commandable u:c.getCharactersUnderCommand()){
            localSquad.add(u.getIdentity());
            if(!unitsUnderCommand.contains(u.getIdentity())){
                return false;
            }else if(u instanceof VirtualGameCharacterNode && worldMap.getCharacter(u.getIdentity())!=null){
                return false;
            }else if((u instanceof VirtualGameCharacterNode) && relatedCharacters.get(u.getIdentity())!=null){
                ((VirtualGameCharacterNode)u).updateMessage(relatedCharacters.get(u.getIdentity()));
            }

        }

        for(UUID u:unitsUnderCommand){
            if(!localSquad.contains(u)){
                return false;
            }
        }
        return true;
    }

    void updateOnSceneCharacter(GameCharacterNode n, final CharacterMessage cMessage, WorldMap worldMap) {
        Commandable oldCO = n.getCommandingOfficer();
        Commandable newCo = worldMap.getCharacter(cMessage.getCommandingOfficer());

        if(oldCO!=null && cMessage.getCommandingOfficer()==null){
            n.setCommandingOfficer(null);
        }else if(oldCO==null && cMessage.getCommandingOfficer()!=null && newCo instanceof CommandingOfficer){
            n.setCommandingOfficer((CommandingOfficer) newCo);
        }else if(oldCO!=null && cMessage.getCommandingOfficer()!=null && !oldCO.getIdentity().equals(cMessage.getCommandingOfficer())){
            if(newCo!=null && newCo instanceof CommandingOfficer){
                n.setCommandingOfficer((CommandingOfficer) newCo);
            }
        }

        if(worldMap.getCharacter(cMessage.getCommandingOfficer())==null && relatedCharacters.containsKey(cMessage.getCommandingOfficer())){
            n.setCommandingOfficer(new VirtualGameCharacterNode(relatedCharacters.get(cMessage.getCommandingOfficer()), false));
        }else if(worldMap.getCharacter(cMessage.getCommandingOfficer())!=null && n.getCommandingOfficer() instanceof VirtualGameCharacterNode && worldMap.getCharacter(cMessage.getCommandingOfficer()) instanceof CommandingOfficer){
            n.setCommandingOfficer((CommandingOfficer) worldMap.getCharacter(cMessage.getCommandingOfficer()));
        }

        if(n instanceof CommandingOfficer && !hasSameUnits((CommandingOfficer) n, cMessage.getUnitsUnderCommand(), worldMap, relatedCharacters)){
            ((CommandingOfficer)n).removeAllUnits();

            for(UUID u:cMessage.getUnitsUnderCommand()){
                GameCharacterNode unit = worldMap.getCharacter(u);
                if(unit!=null){
                    ((CommandingOfficer)n).addCharactersUnderCommand(unit);
                }else if(relatedCharacters.get(u)!=null){
                    boolean selected = false;
                    final VirtualGameCharacterNode virtualGameCharacterNode = new VirtualGameCharacterNode(relatedCharacters.get(u), selected);
                    ((CommandingOfficer)n).addCharactersUnderCommand(virtualGameCharacterNode);
                    if(selected){
                        app.chaseCameraAppState.selectCharacter(virtualGameCharacterNode);
                    }
                }
            }
        }

        if(!n.getCountry().name().equals(cMessage.getCountry().name())){
            n.setCountry(Country.valueOf(cMessage.getCountry().name()));
        }

        if(cMessage.getBoardedVehicle()!=null){
            GameVehicleNode vehicle = (GameVehicleNode)worldMap.getCharacter(cMessage.getBoardedVehicle());
            final GameCharacterNode passenger = worldMap.getCharacter(cMessage.getId());
            if(vehicle!=null && passenger!=null && passenger.getBoardedVehicle()!=vehicle){
                vehicle.boardPassenger(passenger);
            }
        }else{
            GameCharacterNode nn = worldMap.getCharacter(cMessage.getId());
            if(nn!=null && nn.getBoardedVehicle()!=null){
                nn.disembarkVehicle();
            }
        }

        if(n instanceof GameVehicleNode){
            ((GameVehicleNode)n).synchronisePassengers(cMessage.getPassengers());
        }

        n.initialiseKills(cMessage.getKillCount());
        n.setVersion(cMessage.getVersion());

        if(cMessage.shouldBeControledRemotely(clientID) && !n.isControledRemotely()){
            n.setBehaviourControler(new RemoteBehaviourControler(cMessage.getId(), cMessage));
        }
        if(!cMessage.shouldBeControledRemotely(clientID) && !n.isControledLocaly()){
            n.setBehaviourControler(new LocalAIBehaviourControler());
        }
        if(n.isControledRemotely()){
            ((RemoteBehaviourControler)n.getBehaviourControler()).updateRemoteState(cMessage);
        }else{
            try {
                n.checkForNewObjectives(cMessage.getObjectives());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if(n instanceof HalfTrackNode){
            if(cMessage.hasEngineDamage() && !((HalfTrackNode)n).hasEngineDamage()){
                ((HalfTrackNode)n).doEngineDamage();
            }
        }

    }

    public CharacterUpdate getServerResponces() {
        return characterUpdateMessageAssembler.popResponse();
    }




}
