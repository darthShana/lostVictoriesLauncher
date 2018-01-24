package com.jme3.lostVictories.structures

import com.jme3.bullet.BulletAppState
import com.jme3.math.FastMath
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import spock.lang.Specification
import static spock.util.matcher.HamcrestSupport.that
import static spock.util.matcher.HamcrestMatchers.closeTo

class GameBunkerNodeSpec extends Specification {

    void isVectorClose(Vector3f v1, Vector3f v2){
        assert that(v1['x'], closeTo(v2['x'], 0.01))
        assert v1['y'] == v2['y']
        assert that(v1['z'], closeTo(v2['z'], 0.01))
    }

    def "test calculate point to enter bunker"(){
        given:
        def node = new Node(
                localTranslation: new Vector3f(100, 10, 100),
                localRotation: new Quaternion().fromAngleAxis(0, Vector3f.UNIT_Y)
        )
        def bunkerNode = new GameBunkerNode(node, Mock(BulletAppState.class), Mock(CollisionShapeFactoryProvider.class))

        expect:
        new Vector3f(100, 10, 107) == bunkerNode.getEntryPoint()

    }

    def "test calculate point to enter bunker angled"(){
        given:
        def node = new Node(
                localTranslation: new Vector3f(100, 10, 100),
                localRotation: new Quaternion().fromAngleAxis(FastMath.QUARTER_PI, Vector3f.UNIT_Y)
        )
        def bunkerNode = new GameBunkerNode(node, Mock(BulletAppState.class), Mock(CollisionShapeFactoryProvider.class))

        and:
        def entryPoint = bunkerNode.getEntryPoint()

        expect:
        isVectorClose(new Vector3f(104.95f, 10, 104.95f), entryPoint)

    }

    def "test calculate occupation points"(){
        given:
        def node = new Node(
                localTranslation: new Vector3f(100, 10, 100),
                localRotation: new Quaternion().fromAngleAxis(0, Vector3f.UNIT_Y)
        )
        def bunkerNode = new GameBunkerNode(node, Mock(BulletAppState.class), Mock(CollisionShapeFactoryProvider.class))
        def occupationPoints = bunkerNode.getOccupationPoints()

        expect:
        new Vector3f(100, 10, 100) == occupationPoints[0][0]
        new Vector3f(100.5f, 10, 101) == occupationPoints[1][0]
        new Vector3f(99.5f, 10, 101) == occupationPoints[2][0]
        new Vector3f(100, 10, 107) == occupationPoints[3][0]

        and:
        new Vector3f(100, 10, 99) == occupationPoints[0][1]
        new Vector3f(101.5f, 10, 101) == occupationPoints[1][1]
        new Vector3f(98.5f, 10, 101) == occupationPoints[2][1]
        new Vector3f(100, 10, 108) == occupationPoints[3][1]

    }

    def "test calculate occupation points on angled structure"(){
        given:
        def node = new Node(
                localTranslation: new Vector3f(100, 10, 100),
                localRotation: new Quaternion().fromAngleAxis(FastMath.QUARTER_PI, Vector3f.UNIT_Y)
        )
        def bunkerNode = new GameBunkerNode(node, Mock(BulletAppState.class), Mock(CollisionShapeFactoryProvider.class))
        def occupationPoints = bunkerNode.getOccupationPoints()

        expect:
        isVectorClose(new Vector3f(100, 10, 100),  occupationPoints[0][0])
        isVectorClose(new Vector3f(101.07f, 10, 100.35f),  occupationPoints[1][0])
        isVectorClose(new Vector3f(100.35f, 10, 101.06f),  occupationPoints[2][0])
        isVectorClose(new Vector3f(104.95f, 10, 104.95f),  occupationPoints[3][0])

        and:
        new Vector3f(100, 10, 99) == occupationPoints[0][1]
        new Vector3f(101.5f, 10, 101) == occupationPoints[1][1]
        new Vector3f(98.5f, 10, 101) == occupationPoints[2][1]

    }

}
