/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.asset.AssetManager;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.minimap.MinimapPresentable;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.*;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class CoverFront extends Objective<CadetCorporal> implements MinimapPresentable{
    Vector3f mousePress;
    Vector3f mouseRelease;
    private Set<UUID> issuedOrders = new HashSet<UUID>();
    private Cover travelObjective;
    private Node rootNode;

    private CoverFront(){}
    
    public CoverFront(Vector3f mousePress, Vector3f mouseRelease, Node rootNode) {
        this.mousePress = mousePress;
        this.mouseRelease = mouseRelease;
        this.rootNode = rootNode;
    }

    public AIAction planObjective(CadetCorporal character, WorldMap worldMap) {
        AIAction ret = null;
        Set<Commandable> characterNodes = new HashSet<Commandable>(character.getCharactersUnderCommand());
        Vector3f coverDirection = mouseRelease.subtract(mousePress);
        
        if(travelObjective==null){
            Quaternion q = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
            final Vector3f p = mousePress.add(q.mult(coverDirection).normalizeLocal().mult(1.5f));
            this.travelObjective = new Cover(mousePress, p.add(coverDirection), rootNode);
        }
        
        if(travelObjective == null || travelObjective.isComplete()){
            isComplete = true;
        }
        
        if(character.isTeam(Weapon.mg42(), Weapon.mortar())){
            for(Commandable n: character.getCharactersUnderCommand()){
                if(n instanceof GameCharacterNode){
                    if(((GameCharacterNode)n).canShootMultipleTargets()){
                        if(!issuedOrders.contains(n.getIdentity())){
                            n.cover(mousePress, mouseRelease, character);
                            issuedOrders.add(n.getIdentity());
                            characterNodes.remove(n);
                        }
                    }
                }
            }
        }
        
        Iterator<Commandable> it = characterNodes.iterator();
        if(it.hasNext()){
            Commandable leftGuard = it.next();
            if(!issuedOrders.contains(leftGuard.getIdentity())){
                Quaternion q = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
                final Vector3f p = mousePress.add(q.mult(coverDirection).normalizeLocal().mult(3));
                leftGuard.cover(p, p.add(coverDirection.normalize().mult(3)), character);
                issuedOrders.add(leftGuard.getIdentity());
            }
        }  
        if(it.hasNext()){
            Commandable rightGuard = it.next();
            if(!issuedOrders.contains(rightGuard.getIdentity())){
                Quaternion q = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y);
                final Vector3f p = mousePress.add(q.mult(coverDirection).normalizeLocal().mult(3));
                rightGuard.cover(p, p.add(coverDirection.normalize().mult(3)), character);
                issuedOrders.add(rightGuard.getIdentity());
            }
        }
        if(it.hasNext()){
            Commandable rearGuard = it.next();
            if(!issuedOrders.contains(rearGuard.getIdentity())){
                Quaternion q = new Quaternion().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
                final Vector3f p = mousePress.add(q.mult(coverDirection).normalize().mult(3));
                rearGuard.cover(p, p.add(coverDirection.negate().normalizeLocal().mult(3)), character);
                issuedOrders.add(rearGuard.getIdentity());
            }
        }
        if(travelObjective!=null){
            return travelObjective.planObjective(character, worldMap);
        }
        return ret;
    }

    public boolean clashesWith(Objective objective) {
        return objective instanceof TravelObjective || objective instanceof FollowCommander;
    }
    
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("mousePress", MAPPER.valueToTree(new Vector(mousePress)));
        node.put("mouseRelease", MAPPER.valueToTree(new Vector(mouseRelease)));
        return node;
    }

    public CoverFront fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        final Vector press = MAPPER.treeToValue(json.get("mousePress"), Vector.class);
        final Vector release = MAPPER.treeToValue(json.get("mouseRelease"), Vector.class);
        return new CoverFront(new Vector3f(press.x, press.y, press.z), new Vector3f(release.x, release.y, release.z), rootNode);
    }
    
    public Vector3f getObjectiveLocation() {
        return mousePress;
    }

    public List<String> getInstructions() {
        List<String> instructions = new ArrayList<String>();
        instructions.add("Provide some cover");
        instructions.add("Proceed to the green marker, and defend this possition.");
        instructions.add("Attack any enemy in the area");
        return instructions;
    }
    
    public Node getShape(AssetManager assetManager, GameCharacterNode c) {
        return getCircle(assetManager, ColorRGBA.Green, 20);
    }

    public boolean updatedStatus(AvatarCharacterNode avatar) {
        return false;
    }
    
}
