package com.lostVictories;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jme3.lostVictories.LostVictory;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;


public class LostVictoriesClientLauncher {

    public static ObjectMapper MAPPER;
    static{
        MAPPER = new ObjectMapper();
        MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    }

    public static void main(String[] args) throws IOException, DecoderException {
        String playerID, serverIP, gameVersion;
        int port = 5055;

        if(args.length>0) {
            String s = args.length > 1 ? args[1].substring("lostVictoriesLauncher/game=".length()) : args[0].substring("lostvic://lostVictoriesLauncher/game=".length());

            System.out.println("s:" + s);

            JsonNode gameJson = getJsonNode(s);
            playerID = gameJson.get("avatarID").asText();
            serverIP = gameJson.get("host").asText();
            port = Integer.parseInt(gameJson.get("port").asText());
            gameVersion = gameJson.get("gameVersion").asText();
        }else{
            Map<String, String> env = System.getenv();
            playerID = env.get("player_id");
            serverIP = env.get("server_ip");
            if(playerID==null){
                playerID = "2fbe421f-f701-49c9-a0d4-abb0fa904204"; //german
    //               playerID = "d993932f-a185-4a6f-8d86-4ef6e2c5ff95"; //american 1
                //playerID = "844fd93d-e65a-438a-82c5-dab9ad58e854"; //american 2
            }
            if(serverIP == null){
    //                serverIP = "localhost";
                serverIP = "connect.lostvictories.com";
            }
            gameVersion = "pre_alpha";

        }

        LostVictory app = new LostVictory(UUID.fromString(playerID), serverIP, port, gameVersion);
        app.start();
    }

    private static JsonNode getJsonNode(String s) throws IOException {
        Base64 decoder = new Base64();
        byte[] decodedBytes = decoder.decode(s);
        String ss = new String(decodedBytes);
        System.out.println("ss:"+ss);

        return MAPPER.readTree(ss);
    }
}
