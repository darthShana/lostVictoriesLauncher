/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import java.io.Serializable;

/**
 *
 * @author dharshanar
 */
public class GameStatistics implements Serializable{

    private Integer blueHouses;
    private Integer redHouses;
    private Integer blueVictoryPoints;
    private Integer redVictoryPoints;
    private Integer avatarRespawnEstimate;

    private GameStatistics(){}

    public GameStatistics(com.lostVictories.api.GameStatistics gameStatistics) {
        this.blueHouses = gameStatistics.getBlueHouses();
        this.redHouses = gameStatistics.getRedHouses();
        this.blueVictoryPoints = gameStatistics.getBlueVictoryPoints();
        this.redVictoryPoints = gameStatistics.getRedVictoryPoints();
        this.avatarRespawnEstimate = gameStatistics.getAvatarRespawnEstimate();
    }

    public Integer getBlueHouses(){
        return blueHouses;
    }
    
    public Integer getRedHouses(){
        return redHouses;
    }
    
    public Integer getBlueVictoryPoints(){
        return blueVictoryPoints;
    }
    
    public Integer getRedVictoryPoints(){
        return redVictoryPoints;
    }
    
    public Integer getAvatarReswapnInterval(){
        return avatarRespawnEstimate;
    }

}
