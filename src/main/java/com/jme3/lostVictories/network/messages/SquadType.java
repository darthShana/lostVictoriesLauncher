/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

/**
 *
 * @author dharshanar
 */
public enum SquadType {
    ANTI_TANK_GUN, ARMORED_VEHICLE, MORTAR_TEAM, MG42_TEAM, RIFLE_TEAM, BAZOOKA_TEAM, TANK_SQUAD;

    public static SquadType fromMessage(com.lostVictories.api.SquadType squadType) {
        if(com.lostVictories.api.SquadType.ANTI_TANK_GUN_SQUAD == squadType){
            return ANTI_TANK_GUN;
        }
        if(com.lostVictories.api.SquadType.ARMORED_VEHICLE_SQUAD == squadType){
            return ARMORED_VEHICLE;
        }
        if(com.lostVictories.api.SquadType.MORTAR_TEAM_SQUAD == squadType){
            return MORTAR_TEAM;
        }
        if(com.lostVictories.api.SquadType.MG42_TEAM_SQUAD == squadType){
            return MG42_TEAM;
        }
        if(com.lostVictories.api.SquadType.RIFLE_TEAM_SQUAD == squadType){
            return RIFLE_TEAM;
        }
        if(com.lostVictories.api.SquadType.BAZOOKA_TEAM == squadType){
            return BAZOOKA_TEAM;
        }
        if(com.lostVictories.api.SquadType.TANK_SQUAD == squadType){
            return TANK_SQUAD;
        }
        throw new RuntimeException("unknown squad type:"+squadType);
    }


}
