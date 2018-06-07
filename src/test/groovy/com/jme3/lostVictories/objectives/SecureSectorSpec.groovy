package com.jme3.lostVictories.objectives

import com.jme3.lostVictories.WorldMap
import com.jme3.lostVictories.characters.CadetCorporal
import com.jme3.lostVictories.characters.EnemyActivityReport
import com.jme3.lostVictories.characters.Lieutenant
import com.jme3.lostVictories.characters.Rank
import com.jme3.lostVictories.structures.GameBunkerNode
import com.jme3.lostVictories.structures.GameHouseNode
import com.jme3.math.Vector3f
import spock.lang.Specification

import java.awt.geom.Rectangle2D

class SecureSectorSpec extends Specification {
    def bunker1 = Stub(GameBunkerNode.class)
    def bunker2 = Stub(GameBunkerNode.class)
    def house1 = Stub(GameBunkerNode.class)
    def house2 = Stub(GameBunkerNode.class)
    def secureSector = new SecureSector(defences: [bunker1, bunker2], houses: [house1, house2], minimumFightingStrength: 1)

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
        character.getCurrentStrength() >> 20
        character.getEnemyActivity() >> new EnemyActivityReport()
        secureSector.state = SecureSectorState.CAPTURE_HOUSES
        secureSector.planObjective(character, Mock(WorldMap.class))
        secureSector.issuedOrders.values().each{ o -> o.isComplete = true }
        secureSector.planObjective(character, Mock(WorldMap.class))

        then:
        SecureSectorState.DEFEND_SECTOR == secureSector.state

        when:
        secureSector.planObjective(character, Mock(WorldMap.class))

        then:
        SecureSectorState.DEFEND_SECTOR == secureSector.state
        secureSector.issuedOrders.get(corp1.getIdentity()) instanceof OccupyStructure
        secureSector.issuedOrders.get(corp2.getIdentity()) instanceof OccupyStructure
    }

    def "re occupy bunker if the defenders have been killed"(){
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
        character.getCharactersUnderCommand() >> [corp2]
        secureSector.state = SecureSectorState.DEFEND_SECTOR
        secureSector.issuedOrders[corp1.identity] = new OccupyStructure()
        def activity = [:]
        activity[new Vector3f(100, 10, 100)] = System.currentTimeMillis()
        character.getEnemyActivity() >> new EnemyActivityReport(activity:activity)
        secureSector.planObjective(character, Mock(WorldMap.class))

        then:
        secureSector.state == SecureSectorState.DEFEND_SECTOR
        !secureSector.issuedOrders[corp1.identity]
        secureSector.issuedOrders[corp2.identity] instanceof OccupyStructure
    }

    def "don't issues attack orders to busy units"(){
        given:
        def corp1 = Mock(CadetCorporal.class)
        corp1.getIdentity() >> UUID.randomUUID()
        corp1.getRank() >> Rank.CADET_CORPORAL
        def corp2 = Mock(CadetCorporal.class)
        corp2.getIdentity() >> UUID.randomUUID()
        corp2.getRank() >> Rank.CADET_CORPORAL
        def character = Mock(Lieutenant.class)
        character.getIdentity() >> UUID.randomUUID()
        character.getCurrentStrength() >> 15

        when:
        character.getCharactersUnderCommand() >> [corp1, corp2]
        def activity = [:]
        activity[new Vector3f(100, 10, 100)] = System.currentTimeMillis()
        character.getEnemyActivity() >> new EnemyActivityReport(activity:activity)

        secureSector.state = SecureSectorState.ATTACK_TARGET
        secureSector.planObjective(character, Mock(WorldMap.class))
        def obj1 = secureSector.issuedOrders[corp1.identity]
        def obj2 = secureSector.issuedOrders[corp2.identity]

        then:
        obj1 instanceof AttackTargetsInDirection
        obj2 instanceof AttackTargetsInDirection

        when:
        secureSector.planObjective(character, Mock(WorldMap.class))
        def obj3 = secureSector.issuedOrders[corp1.identity]
        def obj4 = secureSector.issuedOrders[corp2.identity]

        then:
        obj1 == obj3
        obj2 == obj4

    }

}
