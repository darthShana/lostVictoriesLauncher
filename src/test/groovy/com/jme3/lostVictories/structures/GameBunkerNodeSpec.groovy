package com.jme3.lostVictories.structures

import com.jme3.bullet.BulletAppState
import com.jme3.math.FastMath
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import spock.lang.Specification
import static spock.util.matcher.HamcrestMatchers.closeTo

class GameBunkerNodeSpec extends Specification {


    def "test calculate point to enter bunker"(){
        given:
        def node = new Node(
                localTranslation: new Vector3f(100, 10, 100),
                localRotation: new Quaternion().fromAngleAxis(0, Vector3f.UNIT_Y)
        )
        def bunkerNode = new GameBunkerNode(node, Mock(BulletAppState.class), Mock(CollisionShapeFactoryProvider.class))

        expect:
        new Vector3f(100, 10, 107) == bunkerNode.calculateEntryPoint()

    }

    def "test calculate point to enter bunker angled"(){
        given:
        def node = new Node(
                localTranslation: new Vector3f(100, 10, 100),
                localRotation: new Quaternion().fromAngleAxis(FastMath.QUARTER_PI, Vector3f.UNIT_Y)
        )
        def bunkerNode = new GameBunkerNode(node, Mock(BulletAppState.class), Mock(CollisionShapeFactoryProvider.class))

        and:
        def entryPoint = bunkerNode.calculateEntryPoint()

        expect:
        entryPoint['x'] closeTo(104.95, 0.01)
        entryPoint.getY() == 10
        entryPoint['z'] closeTo(104.95, 0.01)

    }
}
