package com.jme3.lostVictories.objectives.reactiveObjectives

import com.jme3.lostVictories.characters.BehaviorControler
import com.jme3.lostVictories.characters.GameAnimChannel
import com.jme3.lostVictories.characters.Private
import com.jme3.lostVictories.characters.blenderModels.SoldierBlenderModel
import com.jme3.lostVictories.characters.physicsControl.GameCharacterControl
import com.jme3.lostVictories.characters.weapons.Weapon
import com.jme3.math.Vector3f
import spock.lang.Specification

class CharacterAttackActorTest extends Specification {


    def "attack targets in direction for mortars" () {
        def animationChannel = Mock(GameAnimChannel.class)
        def characterControl = Mock(GameCharacterControl.class)
        given:
        def character = [
                localTranslation: [100,80,100] as Vector3f,
                model: new SoldierBlenderModel("", 1, Weapon.mortar()),
                playerControl: characterControl,
                channel: animationChannel,
                behaviorControler: Mock(BehaviorControler.class)
        ] as Private
        def intruder = [
                localTranslation: [100,80,150] as Vector3f,
                model: new SoldierBlenderModel("", 1, Weapon.rifle()),
                playerControl:Mock(GameCharacterControl.class),
                channel: Mock(GameAnimChannel.class)
        ] as Private

        when:
        animationChannel.getAnimationName() >> "mortar_standByAction"
        characterControl.getViewDirection() >> new Vector3f(0, 0, 1).normalize()
        def willAttack = CharacterAttackActor.doAttack(character, intruder)

        then:
        willAttack
    }
}
