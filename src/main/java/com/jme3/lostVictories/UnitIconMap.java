/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.network.messages.SquadType;

/**
 *
 * @author dharshanar
 */
class UnitIconMap {

    static String getIcon(Commandable n, boolean expanded, boolean selected) {
        StringBuilder s = new StringBuilder("Interface/Icons_lighter_64/");
        SquadType squadType = n.getSquadType(SquadType.RIFLE_TEAM, expanded);
        
        
        if(squadType == SquadType.ANTI_TANK_GUN){
            s.append("at_gun_icon_");
        } else if(squadType == SquadType.ARMORED_VEHICLE){
            s.append("amored_car_icon_");
        } else if(squadType == SquadType.MORTAR_TEAM){
            s.append("mortar_icon_");
        }else if(squadType == SquadType.MG42_TEAM){
            s.append("mg42_icon_");
        }else{
            s.append("rifle_icon_");
        }
        
        
        if(selected){
            s.append("selected.png");
        }else{
            s.append("unselected.png");
        }
        return s.toString();
    }
    
}
