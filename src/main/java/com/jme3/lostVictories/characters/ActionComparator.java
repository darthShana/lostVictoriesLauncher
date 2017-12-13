/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.lostVictories.actions.ShootPointAction;
import com.jme3.lostVictories.actions.ShootTargetAction;
import com.jme3.lostVictories.actions.ShootTargetsAction;

import java.util.Comparator;

/**
 *
 * @author dharshanar
 */
class ActionComparator<AiAction> implements Comparator<AiAction>{

    public int compare(AiAction o1, AiAction o2) {
        if((o2 instanceof ShootPointAction || o2 instanceof ShootTargetAction || o2 instanceof ShootTargetsAction) &&
                !(o2 instanceof ShootPointAction || o1 instanceof ShootTargetAction || o1 instanceof ShootTargetsAction) ){
            return 1;
        }else{
            return 0;
        }
    }


    
}
