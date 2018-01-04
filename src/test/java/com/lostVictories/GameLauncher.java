package com.lostVictories;

import com.jme3.lostVictories.LostVictory;
import java.util.UUID;

public class GameLauncher {

    public static void main(String[] args){
        LostVictory app = new LostVictory(UUID.fromString("d993932f-a185-4a6f-8d86-4ef6e2c5ff95"), "localhost", 5055, "pre_alpha");
        app.start();
    }
}
