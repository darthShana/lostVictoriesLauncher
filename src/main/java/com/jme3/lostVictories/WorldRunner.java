/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.network.messages.AchievementStatus;
import com.jme3.lostVictories.network.messages.GameStatistics;


/**
 *
 * @author dharshanar
 */
public class WorldRunner implements Runnable {
    private static WorldRunner instance;
    private final WorldMap worldMap;
    private Country winner;
    private volatile GameStatistics gameStatistics;
    private AchievementStatus achivementStatus;



    public static WorldRunner instance(WorldMap characterQuadtree){
        instance = new WorldRunner(characterQuadtree);
        return instance;
    }
    
    public static WorldRunner get(){
        return instance;
    }
    private long lastClearTime;
    
    
    
    
    private WorldRunner(WorldMap characterQuadtree) {
        this.worldMap = characterQuadtree;        
    }

    public void run() {
        for(GameCharacterNode c: worldMap.getAllCharacters()){
            try{
                c.planObjectives(worldMap);
            }catch(Throwable e){
                e.printStackTrace();
            }
        }
        if(System.currentTimeMillis()-lastClearTime>4000){
            lastClearTime = System.currentTimeMillis();
            ShotsFiredListener.instance().clear();
        }


    }

    
    
    boolean hasCapturedAllStructures(Country country) {
        return winner == country;
    }

    boolean hasLostAllStructures(Country country) {
        return winner != null && winner != country;
    }

    public void setGameStatistics(GameStatistics gameStatistics){
        this.gameStatistics = gameStatistics;
    }
    
    public void setAchiveemntStatus(AchievementStatus achivementStatus){
        this.achivementStatus = achivementStatus;
    }
    
    public AchievementStatus getAchivementStatus(){
        return achivementStatus;
    }

    int getBlueVictoryPoints() {
        if(gameStatistics!=null && gameStatistics.getBlueVictoryPoints()!=null){
            return gameStatistics.getBlueVictoryPoints();
        }
        return 1000;
    }
    
    int getRedVictoryPoints() {
        if(gameStatistics!=null && gameStatistics.getRedVictoryPoints()!=null){
            return gameStatistics.getRedVictoryPoints();
        }
        return 1000;
    }
    
    int getBlueHouese() {
        if(gameStatistics!=null && gameStatistics.getBlueHouses()!=null){
            return gameStatistics.getBlueHouses().intValue();
        }
        return 0;
    }

    int getRedHouses() {
        if(gameStatistics!=null && gameStatistics.getRedHouses()!=null){
            return gameStatistics.getRedHouses().intValue();
        }
        return 0;
    }

    boolean hasTheOnlyVictoryPoints() {
        if(gameStatistics!=null){
            if(gameStatistics.getBlueVictoryPoints()!=null && gameStatistics.getBlueVictoryPoints()>0 && gameStatistics.getRedVictoryPoints()<=0){
                return true;
            }
        
        }
        return false;
    }

    boolean hasNoVictoryPoints() {
        if(gameStatistics!=null){
            if(gameStatistics.getBlueVictoryPoints()!=null && gameStatistics.getBlueVictoryPoints()<=0){
                return true;
            }
        
        }
        return false;
    }

    Integer getNextReSpawnTime(Country c) {
        if(gameStatistics!=null && gameStatistics.getAvatarReswapnInterval()!=null){
            return gameStatistics.getAvatarReswapnInterval().intValue();
        }
        return null;
    }
    
}
