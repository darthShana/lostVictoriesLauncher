package com.jme3.lostVictories.objectives

import com.jme3.lostVictories.WorldMap
import com.jme3.lostVictories.characters.CadetCorporal
import com.jme3.lostVictories.characters.Private
import com.jme3.lostVictories.structures.GameBunkerNode
import com.jme3.math.Vector3f
import spock.lang.Specification

import java.lang.reflect.Array


class OccupyStructureSpec extends Specification {

    def character = Stub(CadetCorporal.class)
    def bunkerNode = Mock(GameBunkerNode.class)
    def p1 = Mock(Private.class)
    def p2 = Mock(Private.class)
    def p3 = Mock(Private.class)

    def setup() {
        character.getIdentity() >> UUID.randomUUID()
        p1.getIdentity() >> UUID.randomUUID()
        p2.getIdentity() >> UUID.randomUUID()
        p3.getIdentity() >> UUID.randomUUID()
        character.getLocalTranslation() >> new Vector3f(10, 10, 10)
        p1.getLocalTranslation() >> new Vector3f(10, 10, 10)
        p2.getLocalTranslation() >> new Vector3f(10, 10, 10)
        p3.getLocalTranslation() >> new Vector3f(10, 10, 10)
        character.getCharactersUnderCommand() >> [p1, p2, p3]
        bunkerNode.getLocalTranslation() >> new Vector3f(100, 10, 100)
        bunkerNode.getEntryPoint() >> new Vector3f(107, 10, 100)
        bunkerNode.getOccupationPoints() >> [(Vector3f[])[new Vector3f(100, 10, 100), null],
                                             (Vector3f[])[new Vector3f(100.5f, 10, 101), null],
                                             (Vector3f[])[new Vector3f(99.5f, 10, 101), null],
                                             (Vector3f[])[new Vector3f(100, 10, 107), null]]

    }

    def "plan objective sends units close to the bunker"(){
        given:
        def occupyStructure = new OccupyStructure(defenciveStructure: bunkerNode)

        when:
        occupyStructure.planObjective(character, Mock(WorldMap.class))

        then:
        def transport1 = occupyStructure.currentObjectives[character.identity]
        transport1 instanceof TransportSquad
        occupyStructure.state == OccupyStructure.State.TRAVEL_TO_ENTRY


    }

    def "once units get to the entry they should go inside"(){
        given:
        def occupyStructure = new OccupyStructure(defenciveStructure: bunkerNode)

        and:
        occupyStructure.currentObjectives[character.identity] = new TransportSquad(isComplete: true)
        occupyStructure.state = OccupyStructure.State.TRAVEL_TO_ENTRY

        when:
        occupyStructure.planObjective(character, Mock(WorldMap.class))

        then:
        occupyStructure.state == OccupyStructure.State.ENTER_BUNKER
        occupyStructure.currentObjectives == [:]

        when:
        occupyStructure.planObjective(character, Mock(WorldMap.class))

        then:
        occupyStructure.currentObjectives[p1.identity] instanceof TravelObjective
        occupyStructure.currentObjectives[p1.identity].computePath.waypoints == [new Vector3f(10, 10, 10), new Vector3f(100, 10, 100)]
        occupyStructure.currentObjectives[p2.identity] instanceof TravelObjective
        occupyStructure.currentObjectives[p2.identity].computePath.waypoints == [new Vector3f(10, 10, 10), new Vector3f(100.5f, 10, 101)]
        occupyStructure.currentObjectives[p3.identity] instanceof TravelObjective
        occupyStructure.currentObjectives[p3.identity].computePath.waypoints == [new Vector3f(10, 10, 10), new Vector3f(99.5f, 10, 101)]
        1 * p1.addObjective(_) >> {arguments -> arguments[0] instanceof TravelObjective}
        1 * p2.addObjective(_) >> {arguments -> arguments[0] instanceof TravelObjective}
        1 * p3.addObjective(_) >> {arguments -> arguments[0] instanceof TravelObjective}

    }


}
