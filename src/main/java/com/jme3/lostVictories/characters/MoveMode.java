/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

/**
 *
 * @author dharshanar
 */
public enum MoveMode {

    WALK{
        public float speed(){
            return 100;
        }

        @Override
        public void doAnimation(AvatarCharacterNode aThis) {
            aThis.doWalkAction();
        }

        @Override
        public void doAnimation(Soldier aThis) {
            aThis.doWalkAction();
        }
        
        
    }, 
    RUN{
        public float speed(){
            return 200;
        }

        @Override
        public void doAnimation(AvatarCharacterNode aThis) {
            aThis.doRunAction();
        }

        @Override
        public void doAnimation(Soldier aThis) {
            aThis.doRunAction();
        }
        
        
    };

    public abstract float speed();

    public abstract void doAnimation(AvatarCharacterNode aThis);
    public abstract void doAnimation(Soldier aThis);
    
}
