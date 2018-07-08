package com.jme3.lostVictories.objectives.reactiveObjectives;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.actions.ShootTargetAction;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.objectives.reactiveObjectives.messages.CharacterMovedAction;

public class CharacterAttackActor extends AbstractActor{

    static public Props props() { return Props.create(CharacterAttackActor.class, () -> new CharacterAttackActor());}

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(CharacterMovedAction.class, cma -> {
            AICharacterNode character = cma.getCharacter();
            GameCharacterNode intruder = cma.getIntruder();
            doAttack(character, intruder);
        }).build();
    }

    static boolean doAttack(AICharacterNode character, GameCharacterNode intruder) {
        if(character.getLocalTranslation().distance(intruder.getLocalTranslation())< character.getMaxRange()){

            if(character.hasProjectilePathWeapon() && character.isReadyToShoot(intruder.getPositionToTarget(character))){
                character.doAction(new ShootTargetAction(intruder));
                return true;
            }else if(character.hasClearLOSTo(intruder)) {
                character.doAction(new ShootTargetAction(intruder));
                return true;
            }
        }
        return false;
    }


}
