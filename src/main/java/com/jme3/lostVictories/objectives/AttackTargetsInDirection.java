/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.asset.AssetManager;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.*;
import com.jme3.lostVictories.minimap.MinimapPresentable;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class AttackTargetsInDirection extends Objective<CadetCorporal> implements MinimapPresentable{
    Vector3f lastKnownLocation;
    private Node rootNode;


    private AttackTargetsInDirection(){}
    
    public AttackTargetsInDirection(Vector3f l, Node rootNode) {
        this.lastKnownLocation = l;
        this.rootNode = rootNode;
    }

    public AIAction planObjective(CadetCorporal character, WorldMap worldMap) {
        System.out.println(character.getIdentity()+":attacking in direction:"+character.getCountry());

        return null;
    }

    public boolean clashesWith(Objective objective) {
        return objective instanceof CaptureStructure || 
                objective instanceof Cover || 
                objective instanceof TransportSquad || 
                objective instanceof BombardTargets;
    }


    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.set("lastKnownLocation", MAPPER.valueToTree(new Vector(lastKnownLocation)));
        return node;
    }

    public AttackTargetsInDirection fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Vector d = MAPPER.treeToValue(json.get("lastKnownLocation"), Vector.class);
        return new AttackTargetsInDirection(d.toVector(), rootNode);
    }

    public Vector3f getObjectiveLocation() {
        return lastKnownLocation;
    }

    public List<String> getInstructions() {
        List<String> instructions = new ArrayList<>();
        instructions.add("The enemy has been sighted, Launch an asault with your units!");
        instructions.add("The red marker shows the location of the sighting");
        instructions.add("Proceed towards the marker and attack the enemy in the area.");
        return instructions;
    }

    public boolean updatedStatus(AvatarCharacterNode avatar) {
        return false;
    }

    @Override
    public Node getShape(AssetManager assetManager, GameCharacterNode c) {
        return new Node();
    }


}
