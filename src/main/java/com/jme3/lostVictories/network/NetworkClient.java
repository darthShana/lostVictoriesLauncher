/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;


import com.google.protobuf.ByteString;
import com.lostVictories.api.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 *
 * @author dharshanar
 */
public class NetworkClient {
    
    final ManagedChannel grpcChannel;

    private final UUID clientID;
    private final String ipAddress;
    private final int port;
    private final LostVictoriesServerGrpc.LostVictoriesServerStub lostVictoriesServerStub;
    private final StreamObserver<LostVictoryMessage> ignoreObserver;
    private final StreamObserver<LostVictoryMessage> updateCharacterResponseStreamObserver;
    private final StreamObserver<UpdateCharactersRequest> updateCharactersRequestStreamObserver;
    public int backoff = 0;


    public NetworkClient(String ipAddress, int port, UUID clientID, final CharacterUpdateMessageHandler characterUpdateMessageHandler, final GameStatusMessageHandler gameStatusMessageHandler) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.clientID = clientID;

        grpcChannel = ManagedChannelBuilder.forAddress(ipAddress, port)
                .usePlaintext(true)
                .build();

        lostVictoriesServerStub = LostVictoriesServerGrpc.newStub(grpcChannel);

        updateCharacterResponseStreamObserver = new StreamObserver<LostVictoryMessage>() {
            @Override
            public void onNext(LostVictoryMessage value) {
                if(value.hasCharacterStatusResponse() && clientID.equals(uuid(value.getCharacterStatusResponse().getUnit().getId()))){
                    if(backoff==0 && value.getCharacterStatusResponse().getBackoff()!=0){
                        System.out.println("server request backoff");
                    }else if(backoff!=0 && value.getCharacterStatusResponse().getBackoff()==0){
                        System.out.println("server clears backoff");
                    }
                    backoff = value.getCharacterStatusResponse().getBackoff();
                }
                characterUpdateMessageHandler.messageReceived(value);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {}
        };
        updateCharactersRequestStreamObserver = lostVictoriesServerStub.updateLocalCharacters(updateCharacterResponseStreamObserver);

        StreamObserver<LostVictoryStatusMessage> gameStatusResponseObserver = new StreamObserver<LostVictoryStatusMessage>() {
            @Override
            public void onNext(LostVictoryStatusMessage lostVictoryStatusMessage) {
                gameStatusMessageHandler.messageReceived(lostVictoryStatusMessage);
            }

            @Override
            public void onError(Throwable throwable) { }

            @Override
            public void onCompleted() { }
        };
        StreamObserver<RegisterClientRequest> registerClientRequestStreamObserver = lostVictoriesServerStub.registerClient(gameStatusResponseObserver);
        registerClientRequestStreamObserver.onNext(RegisterClientRequest.newBuilder().setClientID(bytes(clientID)).build());

        ignoreObserver = new StreamObserver<LostVictoryMessage>() {
            @Override
            public void onNext(LostVictoryMessage value) {
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() { }
        };


    }
    
    public LostVictoryCheckout checkoutScene(UUID avatar) {
        LostVictoriesServerGrpc.LostVictoriesServerBlockingStub checkoutRPC = LostVictoriesServerGrpc.newBlockingStub(grpcChannel);
        return checkoutRPC.checkoutSceen(CheckoutScreenRequest.newBuilder()
                .setClientID(bytes(clientID))
                .setAvatar(bytes(avatar))
                .build());
    }

    public void updateLocalCharacter(com.jme3.lostVictories.network.messages.CharacterMessage toUpdate, UUID avatar, long clientStartTime) {
        UpdateCharactersRequest cm = convertToCharacterMessage(toUpdate, avatar, System.currentTimeMillis() - clientStartTime);
        updateCharactersRequestStreamObserver.onNext(cm);
    }


    public void deathNotification(UUID killer, UUID victim) {
        lostVictoriesServerStub.deathNotification(DeathNotificationRequest.newBuilder()
                .setClientID(bytes(clientID))
                .setKiller(bytes(killer))
                .setVictim(bytes(victim))
                .build(), ignoreObserver);
    }
    
    public void gunnerDeathNotification(UUID killer, UUID victim) {
        lostVictoriesServerStub.gunnerDeathNotification(PassengerDeathNotificationRequest.newBuilder()
            .setClientID(bytes(clientID))
                .setKiller(bytes(killer))
                .setVictim(bytes(victim))
                .build(), ignoreObserver);
    }

    public void addObjective(UUID characterId, UUID identity, String toMessage) {
        lostVictoriesServerStub.addObjective(AddObjectiveRequest.newBuilder()
                .setClientID(bytes(clientID))
                .setCharacterId(bytes(characterId))
                .setIdentity(bytes(identity))
                .setObjective(toMessage)
                .build(), ignoreObserver);
    }

    public void requestEquipmentCollection(UUID equipmentID, UUID characterID) {
        lostVictoriesServerStub.requestEquipmentCollection(EquipmentCollectionRequest.newBuilder()
                .setClientID(bytes(clientID))
                .setEquipmentID(bytes(equipmentID))
                .setCharacterID(bytes(characterID))
                .build(), ignoreObserver);
    }
    
    public void boardVehicle(UUID vehicleUUID, UUID characterID) {
        lostVictoriesServerStub.boardVehicle(BoardVehicleRequest.newBuilder()
                .setClientID(bytes(clientID))
                .setVehicleID(bytes(vehicleUUID))
                .setCharacterID(bytes(characterID))
                .build(), ignoreObserver);
    }

    public void disembarkPassengers(UUID vehicleUUID) {
        lostVictoriesServerStub.disembarkPassengers(DisembarkPassengersRequest.newBuilder()
                .setClientID(bytes(clientID))
                .setVehicleID(bytes(vehicleUUID))
                .build(), ignoreObserver);
    }
    
    

    public static ByteString bytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return ByteString.copyFrom(bb.array());
    }

    public static UUID uuid(ByteString byteString){
        if(byteString==null || byteString.isEmpty()){
            return null;
        }
        ByteBuffer bb = byteString.asReadOnlyByteBuffer();
        long high = bb.getLong();
        long low = bb.getLong();
        return new UUID(high, low);
    }

    private UpdateCharactersRequest convertToCharacterMessage(com.jme3.lostVictories.network.messages.CharacterMessage cm, UUID avatar, long l) {

        CharacterMessage.Builder characterBuilder = CharacterMessage.newBuilder()
                .setId(bytes(cm.getId()))
                .setLocation(cm.getLocation().toMessage())
                .setOrientation(cm.getOrientation().toMessage())
                .putAllObjectives(cm.getObjectives()).addAllCompletedObjectives(cm.getCompletedObjectives().stream().map(oid-> bytes(UUID.fromString(oid))).collect(Collectors.toSet()))
                .setDead(cm.isDead())
                .setCreationTime(cm.getCreationTime())
                .setEngineDamaged(cm.hasEngineDamage())
                .setVersion(cm.getVersion());

        cm.getActions().forEach(a->{
            characterBuilder.addActions(a.toMessage());
        });

        UpdateCharactersRequest.Builder builder = UpdateCharactersRequest.newBuilder()
                .setClientID(bytes(clientID))
                .setAvatar(bytes(avatar))
                .setClientStartTime(l)
                .setCharacter(characterBuilder.build());
        return builder.build();
    }


}
