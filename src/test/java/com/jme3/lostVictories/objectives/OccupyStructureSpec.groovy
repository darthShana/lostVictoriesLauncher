package com.jme3.lostVictories.objectives

import com.jme3.lostVictories.WorldMap
import com.jme3.lostVictories.characters.CadetCorporal
import com.jme3.lostVictories.characters.Commandable
import com.jme3.lostVictories.structures.GameBunkerNode
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
        def transport1 = occupyStructure.currentObjective
        transport1 instanceof TransportSquad

    }
}
