package com.jme3.lostVictories.characters

import com.jme3.math.Vector3f
import org.lwjgl.Sys
import spock.lang.Specification

class AICharacterNodeTest extends Specification {

    def "get consolidated activity report" (){
        given:
        def p1 = new Private()
        def p2 = new Private()
        def c1 = new CadetCorporal()
        def l1 = new Lieutenant()

        and:
        c1.addCharactersUnderCommand([p1, p2] as Set)
        l1.addCharactersUnderCommand(c1)

        when:
        p1.addEnemyActivity(new Vector3f(100, 0, 100), System.currentTimeMillis())
        p1.addEnemyActivity(new Vector3f(101, 0, 101), System.currentTimeMillis())
        p2.addEnemyActivity(new Vector3f(100, 0, 100), System.currentTimeMillis())
        p2.addEnemyActivity(new Vector3f(101, 0, 101), System.currentTimeMillis())
        c1.addEnemyActivity(new Vector3f(103, 0, 103), System.currentTimeMillis())

        then:
        !l1.getEnemyActivity().activity().isEmpty()
    }

}
