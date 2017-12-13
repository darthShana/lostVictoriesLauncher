package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.MediumTankNode;
import com.jme3.lostVictories.characters.physicsControl.BetterTankControl;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

public class RotateTurretAndShoot extends Objective<MediumTankNode> implements AIAction<MediumTankNode>{

    private Vector3f target;

    private RotateTurretAndShoot(){}

    public RotateTurretAndShoot(Vector3f target) {
        this.target = target;
    }

    @Override
    public AIAction planObjective(MediumTankNode character, WorldMap worldMap) {
        return this;
    }

    @Override
    public boolean clashesWith(Objective objective) {
        return false;
    }


    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("target", MAPPER.valueToTree(new Vector(target)));
        return node;
    }

    public RotateTurretAndShoot fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Vector t = MAPPER.treeToValue(json.get("target"), Vector.class);
        return new RotateTurretAndShoot(new Vector3f(t.x, t.y, t.z));
    }

    @Override
    public boolean doAction(MediumTankNode aThis, Node rootNode, GameAnimChannel channel, float tpf) {
        if(aThis.isReadyToShoot(target.subtract(aThis.getShootingLocation()))){
            aThis.shoot(target);
            isComplete = true;
            return true;
        }else{
            ((BetterTankControl)aThis.getCharacterControl()).turnTurretTo(target.subtract(aThis.getLocalTranslation()));
        }

        return false;
    }
}
