/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.lostVictories.network.messages.RankMessage;

/**
 *
 * @author dharshanar
 */
public enum Rank {
    CADET_CORPORAL{

        @Override
        public String getDescription() {
            return "Cadet Corporal";
        }
        
        @Override
        public boolean isSame(RankMessage msg){
            return msg == RankMessage.CADET_CORPORAL;
        }
        
        @Override
        public int getFullStrengthPopulation() {
            return 5;
        }
        
    }, 
    PRIVATE{

        @Override
        public String getDescription() {
            return "Private";
        }
        
        @Override
        public boolean isSame(RankMessage msg){
            return msg == RankMessage.PRIVATE;
        }
        
        @Override
        public int getFullStrengthPopulation() {
            return 0;
        }
        
        
    }, 
    COLONEL{

        @Override
        public String getDescription() {
            return "Colonel";
        }
        
        @Override
        public boolean isSame(RankMessage msg){
            return msg == RankMessage.COLONEL;
        }
        
        @Override
        public int getFullStrengthPopulation() {
            return 2;
        }
                            
    }, 
    LIEUTENANT{

        @Override
        public String getDescription() {
            return "Lieutenant";
        }
    
        @Override
        public boolean isSame(RankMessage msg){
            return msg == RankMessage.LIEUTENANT;
        }
        
        @Override
        public int getFullStrengthPopulation() {
            return 4;
        }
        
    };
    
    public abstract String getDescription();
    public abstract int getFullStrengthPopulation();
    public abstract boolean isSame(RankMessage rank);
}
