/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import com.jme3.lostVictories.characters.Rank;

/**
 *
 * @author dharshanar
 */
public enum RankMessage {
	COLONEL, LIEUTENANT, CADET_CORPORAL, PRIVATE;

    public static RankMessage fromRank(Rank rank) {
        if(rank==Rank.PRIVATE){
            return RankMessage.PRIVATE;
        }
        if(rank==Rank.CADET_CORPORAL){
            return RankMessage.CADET_CORPORAL;
        }
        if(rank==Rank.LIEUTENANT){
            return RankMessage.LIEUTENANT;
        }
        if(rank==Rank.COLONEL){
            return RankMessage.COLONEL;
        }
        throw new RuntimeException("unknow rank:"+rank);
    }
}
