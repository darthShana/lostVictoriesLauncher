package com.jme3.lostVictories

import com.jme3.lostVictories.structures.GameBunkerNode
import com.jme3.lostVictories.structures.GameHouseNode
import spock.lang.Specification

import java.awt.Rectangle

class GameSectorSpec extends Specification {


    def "find the bunkers in a sector" (){
        given:
        def sector = new GameSector(new Rectangle(), )
        sector.structures.addAll([Stub(GameHouseNode.class), Stub(GameHouseNode.class), Stub(GameBunkerNode.class)])

        and:
        def defences = sector.getDefences();

        expect:
        defences.size() == 1

    }
}
