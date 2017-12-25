package com.jme3.lostVictories.objectives

import com.jme3.lostVictories.WorldMap
import com.jme3.lostVictories.characters.CadetCorporal
import com.jme3.lostVictories.characters.Lieutenant
import com.jme3.lostVictories.structures.GameBunkerNode
import com.jme3.lostVictories.structures.GameHouseNode
import com.jme3.math.Vector3f
import spock.lang.Specification

import java.awt.geom.Rectangle2D

class SecureSectorSpec extends Specification {
    def bunker1 = Stub(GameBunkerNode.class)
    def bunker2 = Stub(GameBunkerNode.class)
    def secureSector = new SecureSector(defences: [bunker1, bunker2])

    def "test get vacant defences"(){
        expect:
        secureSector.getVacantDefence().get() == bunker1

    }

    def "test only finds bunkers that have no orders to occupy"(){
        given:
        secureSector.issuedOrders[UUID.randomUUID()] = new OccupyStructure(bunker1)

        expect:
        secureSector.getVacantDefence().get() == bunker2
    }

    def "test no defences available"(){
        given:
        secureSector.issuedOrders[UUID.randomUUID()] = new OccupyStructure(bunker1)
        secureSector.issuedOrders[UUID.randomUUID()] = new OccupyStructure(bunker2)

        expect:
        secureSector.getVacantDefence().isPresent() == false

    }

    def "test transition to defending sector"(){
        given:
        def corp1 = Mock(CadetCorporal.class)
        corp1.getIdentity() >> UUID.randomUUID()
        def corp2 = Mock(CadetCorporal.class)
        corp2.getIdentity() >> UUID.randomUUID()
        def character = Mock(Lieutenant.class)
        character.getIdentity() >> UUID.randomUUID()

        and:
        secureSector.boundary = new Rectangle2D.Float(10, 10, 10, 10)
        secureSector.houses = [Mock(GameHouseNode.class)]
        character.getLocation() >> new Vector3f(11, 0, 11)

        when:
        character.getCharactersUnderCommand() >> [corp1, corp2]
        secureSector.planObjective(character, Mock(WorldMap.class))
        SecureSectorState.DEFEND_SECTOR == secureSector.state
        secureSector.planObjective(character, Mock(WorldMap.class))

        then:
        SecureSectorState.DEFEND_SECTOR == secureSector.state
        secureSector.issuedOrders.get(corp1.getIdentity()) instanceof OccupyStructure
        secureSector.issuedOrders.get(corp2.getIdentity()) instanceof OccupyStructure



    }
}
