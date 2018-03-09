package com.jme3.lostVictories.objectives.reactiveObjectives;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.jme3.lostVictories.actions.ShootTargetAction;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.objectives.reactiveObjectives.messages.CharacterMovedAction;

public class CharacterAttackActor extends AbstractActor{

    static public Props props() { return Props.create(CharacterAttackActor.class, () -> new CharacterAttackActor());}

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(CharacterMovedAction.class, cma -> {
            AICharacterNode character = cma.getCharacter();
            GameCharacterNode intruder = cma.getIntruder();
            if(character.getLocalTranslation().distance(intruder.getLocalTranslation())< character.getMaxRange() &&
                    character.hasClearLOSTo(intruder)){
                character.doAction(new ShootTargetAction(intruder));
            }
        }).build();
    }


}
