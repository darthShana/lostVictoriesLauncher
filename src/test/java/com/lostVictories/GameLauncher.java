package com.lostVictories;

import com.jme3.lostVictories.LostVictory;
import java.util.UUID;

public class GameLauncher {

    public static void main(String[] args){
        LostVictory app = new LostVictory(UUID.fromString("2fbe421f-f701-49c9-a0d4-abb0fa904204"), "localhost", 5055, "pre_alpha");
        app.start();
    }
}
