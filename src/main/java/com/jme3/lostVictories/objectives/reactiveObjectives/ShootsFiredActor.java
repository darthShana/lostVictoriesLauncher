package com.jme3.lostVictories.objectives.reactiveObjectives;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.objectives.reactiveObjectives.messages.CharacterMovedAction;
import com.jme3.lostVictories.objectives.reactiveObjectives.messages.ShootsFired;

import java.util.List;

public class ShootsFiredActor extends AbstractActor{

    private final ActorRef proximityAttackRouter;

    public static Props props(WorldMap map){return Props.create(ShootsFiredActor.class, ()->new ShootsFiredActor(map));}

    private WorldMap worldMap;

    public ShootsFiredActor(WorldMap worldMap) {
        this.worldMap = worldMap;
        proximityAttackRouter = getContext().getSystem().actorOf(CharacterTurnToFaceAttackActor.props().withRouter(new RoundRobinPool(2)), "responseToShootsFiredRouter");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ShootsFired.class, sf->{
            List<GameCharacterNode> enemyCharactersInDirection = worldMap.getEnemyCharactersInDirection(sf.getShooter(), sf.getShootingDirection(), sf.getShooter().getMaxRange());
            enemyCharactersInDirection.stream()
                    .filter(c->!c.isFirering())
                    .filter(c->c instanceof AICharacterNode)
                    .map(c->(AICharacterNode)c)
                    .forEach(c->proximityAttackRouter.tell(new CharacterMovedAction(c, sf.getShooter()), ActorRef.noSender()));
        }).build();
    }
}
