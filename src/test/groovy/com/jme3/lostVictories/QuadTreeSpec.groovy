package com.jme3.lostVictories

import com.jme3.math.Vector3f
import com.jme3.scene.Node
import spock.lang.Specification

import java.awt.Rectangle
import java.awt.geom.Rectangle2D

import static com.jme3.lostVictories.WorldMap.CHARACTER_SIZE

class QuadTreeSpec extends Specification {

    def mapBounds = new Rectangle(-512, -512, 1024, 1024)

    def "test quad tree insert point" (){
        given:
        def quadTree = new QuadTree(mapBounds.x-CHARACTER_SIZE as float, mapBounds.y-CHARACTER_SIZE as float, mapBounds.width+(CHARACTER_SIZE*2) as float, 4)
        quadTree.insert(new Node(localTranslation: new Vector3f(-10, 10, 10)))
        quadTree.insert(new Node(localTranslation: new Vector3f(-20, 10, 20)))
        quadTree.insert(new Node(localTranslation: new Vector3f(30, 10, -30)))
        quadTree.insert(new Node(localTranslation: new Vector3f(40, 10, -40)))
        quadTree.insert(new Node(localTranslation: new Vector3f(-50, 10, -50)))
        quadTree.insert(new Node(localTranslation: new Vector3f(-60, 10, -60)))
        quadTree.insert(new Node(localTranslation: new Vector3f(70, 10, 70)))
        quadTree.insert(new Node(localTranslation: new Vector3f(80, 10, 80)))

        when:
        def res = new ArrayList()
        quadTree.query(new Rectangle2D.Float(69, 69, 2, 2), res)

        then:
        res.size() == 1
        res.get(0).localTranslation == new Vector3f(70, 10, 70)
    }

    def "test quad tree remove point" (){
        given:
        def quadTree = new QuadTree(mapBounds.x-CHARACTER_SIZE as float, mapBounds.y-CHARACTER_SIZE as float, mapBounds.width+(CHARACTER_SIZE*2) as float, 4)
        def node = new Node(localTranslation: new Vector3f(70, 10, 70))
        quadTree.insert(new Node(localTranslation: new Vector3f(-10, 10, 10)))
        quadTree.insert(new Node(localTranslation: new Vector3f(-20, 10, 20)))
        quadTree.insert(new Node(localTranslation: new Vector3f(30, 10, -30)))
        quadTree.insert(new Node(localTranslation: new Vector3f(40, 10, -40)))
        quadTree.insert(new Node(localTranslation: new Vector3f(-50, 10, -50)))
        quadTree.insert(new Node(localTranslation: new Vector3f(-60, 10, -60)))
        quadTree.insert(node)
        quadTree.insert(new Node(localTranslation: new Vector3f(80, 10, 80)))

        when:
        def res = new ArrayList()
        quadTree.remove(node)
        quadTree.query(new Rectangle2D.Float(69, 69, 2, 2), res)

        then:
        res.size() == 0
    }

    def "test quad tree update point" (){
        given:
        def quadTree = new QuadTree(mapBounds.x-CHARACTER_SIZE as float, mapBounds.y-CHARACTER_SIZE as float, mapBounds.width+(CHARACTER_SIZE*2) as float, 4)
        def node = new Node(localTranslation: new Vector3f(70, 10, 70))
        quadTree.insert(new Node(localTranslation: new Vector3f(-10, 10, 10)))
        quadTree.insert(new Node(localTranslation: new Vector3f(-20, 10, 20)))
        quadTree.insert(new Node(localTranslation: new Vector3f(30, 10, -30)))
        quadTree.insert(new Node(localTranslation: new Vector3f(40, 10, -0)))
        quadTree.insert(new Node(localTranslation: new Vector3f(-50, 10, -50)))
        quadTree.insert(new Node(localTranslation: new Vector3f(-60, 10, -60)))
        quadTree.insert(node)
        quadTree.insert(new Node(localTranslation: new Vector3f(80, 10, 80)))

        when:
        node.localTranslation = new Vector3f(-135, 10, -135)
        def res = new ArrayList()
        quadTree.update(node)
        quadTree.query(new Rectangle2D.Float(69, 69, 2, 2), res)

        then:
        res.size() == 0

        when:
        quadTree.query(new Rectangle2D.Float(-136, -136, 2, 2), res)

        then:
        res.size() == 1
        res.get(0).localTranslation == new Vector3f(-135, 10, -135)
    }

}
