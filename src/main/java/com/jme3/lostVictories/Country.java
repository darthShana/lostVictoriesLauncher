
package com.jme3.lostVictories;

import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Rank;
import com.jme3.lostVictories.characters.blenderModels.BlenderModel;
import com.jme3.lostVictories.characters.blenderModels.SoldierBlenderModel;
import com.jme3.lostVictories.characters.weapons.Weapon;

/**
 *
 * @author dharshanar
 */
public enum Country {
    
    GERMAN{
        @Override
        public boolean isAlliedWith(GameCharacterNode character) {
            return character.getCountry()==GERMAN;
        }

        @Override
        BlenderModel getModel(Weapon weapon, Rank rank) {
            if(rank==Rank.CADET_CORPORAL){
                return new SoldierBlenderModel("Models/Characters/German_Corporal.j3o", "", .75f, weapon);
            }
            if(rank==Rank.LIEUTENANT){
                return new SoldierBlenderModel("Models/Characters/German_Lieutenant.j3o", "", .75f, weapon);
            }
            if(rank==Rank.COLONEL){
                return new SoldierBlenderModel("Models/Characters/German_Colonel.j3o", "", .75f, weapon);
            }
            return new SoldierBlenderModel("Models/Characters/German_Private.j3o", "", .75f, weapon);
        }

    }, AMERICAN{
        @Override
        public boolean isAlliedWith(GameCharacterNode character) {
            return character.getCountry()==AMERICAN;
        }

        @Override
        BlenderModel getModel(Weapon weapon, Rank rank) {
            if(rank==Rank.CADET_CORPORAL){
                return new SoldierBlenderModel("Models/Characters/American_Corporal.j3o", "", .75f, weapon);
            }
            if(rank==Rank.LIEUTENANT){
                return new SoldierBlenderModel("Models/Characters/American_Lieutenant.j3o", "", .75f, weapon);
            }
            if(rank==Rank.COLONEL){
                return new SoldierBlenderModel("Models/Characters/American_Colonel.j3o", "", .75f, weapon);
            }
            return new SoldierBlenderModel("Models/Characters/American_Private.j3o", "", .75f, weapon);
        }

    };

    public abstract boolean isAlliedWith(GameCharacterNode character);

    abstract BlenderModel getModel(Weapon weapon, Rank rank);

    
}
