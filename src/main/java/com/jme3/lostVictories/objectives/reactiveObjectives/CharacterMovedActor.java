package com.jme3.lostVictories.objectives.reactiveObjectives;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import com.jme3.lostVictories.LocateCharacterRequest;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.objectives.reactiveObjectives.messages.CharacterMovedAction;

import java.util.List;

public class CharacterMovedActor extends AbstractActor {

    private final ActorRef proximityAttackRouter;

    static public Props props(WorldMap map) { return Props.create(CharacterMovedActor.class, ()-> new CharacterMovedActor(map));}
    private WorldMap map;

    public CharacterMovedActor(WorldMap map) {
        this.map = map;
        proximityAttackRouter = getContext().getSystem().actorOf(CharacterAttackActor.props(), "proximityAttackRouter");
    }



    @Override
    public Receive createReceive() {
        return receiveBuilder().match(LocateCharacterRequest.class, lcr -> {
            GameCharacterNode character = lcr.getCharacter();
            map.updateCharacterLocation(character);

            if(character instanceof AICharacterNode) {
                List<GameCharacterNode> enemyCharactersInDirection = map.getEnemyCharactersInDirection(character, character.getAimingDirection(), character.getMaxRange());
                if(!enemyCharactersInDirection.isEmpty()){
                    proximityAttackRouter.tell(new CharacterMovedAction((AICharacterNode) character, enemyCharactersInDirection.get(0)), ActorRef.noSender());
                }
            }

            List<GameCharacterNode> attackRange = map.getCharactersInAutoAttackRange(character);
            attackRange.stream()
                    .filter(c->!c.isAlliedWith(character))
                    .filter(c->!c.isFirering())
                    .filter(c->c instanceof AICharacterNode)
                    .map(c->(AICharacterNode)c)
                    .forEach(c->proximityAttackRouter.tell(new CharacterMovedAction(c, character), ActorRef.noSender()));

            map.getCharactersInLineOfSightRange(character).stream()
                    .filter(c->!c.isAlliedWith(character))
                    .filter(c->!c.isFirering())
                    .filter(c->!attackRange.contains(c))
                    .filter(c->c instanceof AICharacterNode)
                    .map(c->(AICharacterNode)c)
                    .forEach(c->proximityAttackRouter.tell(new CharacterMovedAction(c, character), ActorRef.noSender()));
        }).build();
    }
}
