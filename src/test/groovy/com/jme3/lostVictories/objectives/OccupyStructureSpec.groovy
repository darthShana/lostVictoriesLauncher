package com.jme3.lostVictories.objectives

import com.jme3.lostVictories.WorldMap
import com.jme3.lostVictories.actions.AIAction
import com.jme3.lostVictories.actions.MoveAction
import com.jme3.lostVictories.characters.CadetCorporal
import com.jme3.lostVictories.characters.Commandable
import com.jme3.lostVictories.structures.GameBunkerNode
import com.jme3.math.FastMath
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import spock.lang.Specification


class OccupyStructureSpec extends Specification {

    def "plan objective sends units close to the bunker"(){
        given:
        def occupyStructure = new OccupyStructure(defenciveStructure: Mock(GameBunkerNode.class))

        and:
        def character = Stub(CadetCorporal.class)
        character.getIdentity() >> UUID.randomUUID()
        character.getCharactersUnderCommand() >> new ArrayList<Commandable>()

        when:
        occupyStructure.planObjective(character, Mock(WorldMap.class))

        then:
        def transport1 = occupyStructure.currentObjective[character.identity]
        transport1 instanceof TransportSquad
        occupyStructure.state == OccupyStructure.State.TRAVEL_TO_ENTRY


    }

    def "once units getting to the entry they should go inside"(){
        def bunkerNode = Mock(GameBunkerNode.class)
        given:
        def occupyStructure = new OccupyStructure(defenciveStructure: bunkerNode)

        and:
        def character = Stub(CadetCorporal.class)
        character.getIdentity() >> UUID.randomUUID()
        character.getLocalTranslation() >> new Vector3f(10, 10, 10)
        character.getCharactersUnderCommand() >> new ArrayList<Commandable>()
        bunkerNode.getLocalTranslation() >> new Vector3f(100, 10, 100)
        occupyStructure.currentObjective[character.identity] = new TransportSquad(isComplete: true)
        occupyStructure.state = OccupyStructure.State.TRAVEL_TO_ENTRY

        when:
        occupyStructure.planObjective(character, Mock(WorldMap.class))

        then:
        occupyStructure.state == OccupyStructure.State.ENTER_BUNKER
        occupyStructure.currentObjective == [:]

        when:
        occupyStructure.planObjective(character, Mock(WorldMap.class))

        then:
        occupyStructure.currentObjective[character.identity] instanceof TravelObjective
        occupyStructure.currentObjective[character.identity].computePath.waypoints == [new Vector3f(10, 10, 10), new Vector3f(100, 10, 100)]

    }

}
