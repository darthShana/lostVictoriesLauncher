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
import com.jme3.lostVictories.characters.*;
import com.jme3.lostVictories.minimap.MinimapPresentable;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

/**
 *
 * @author dharshanar
 */
public class CompleteBootCamp extends Objective<Soldier> implements MinimapPresentable{
    private Vector3f location;
    private Stage currentStage = Stage.INITIAL;
    private boolean targetDistryed;

    private CompleteBootCamp() {
    }
    
    private CompleteBootCamp(Vector3f toVector) {
        this.location = toVector;
    }

    public AIAction<Soldier> planObjective(Soldier character, WorldMap worldMap) {
        return null;
    }

    public boolean clashesWith(Objective objective) {
        return false;
    }

    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("location", MAPPER.valueToTree(new Vector(location)));
        return node;
    }

    public CompleteBootCamp fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Vector d = MAPPER.treeToValue(json.get("location"), Vector.class);
        return new CompleteBootCamp(d.toVector());
    }

    public Vector3f getObjectiveLocation() {
        return location;
    }

    public List<String> getInstructions() {
        return currentStage.instructions();
    }

    public boolean updatedStatus(AvatarCharacterNode avatar) {
        Stage newStage = currentStage.transition(avatar, location, targetDistryed);
        if(isComplete){
            newStage = Stage.TRAINING_COMPLETE;
        }
        if(newStage == Stage.TRAINING_COMPLETE){
            isComplete = true;
        }
        if( newStage != currentStage){
            currentStage = newStage;
            targetDistryed = false;
            return true;
        }
        return false;
    }

    public Node getShape(AssetManager assetManager, GameCharacterNode c) {
        return getCircle(assetManager, ColorRGBA.Green, 20);
    }
    
    

    public void targetDistroyedByAvatar() {
       this.targetDistryed = true;
    }

    public void targetDistroyedBySquad() {
        this.targetDistryed = true;
    }
    
    enum Stage{
        INITIAL{            
            Stage transition(GameCharacterNode character, Vector3f location, boolean targetDistroyed){
                if(character.getLocalTranslation().distance(location)<15){
                    return SHOOT;
                }
                return INITIAL;
            }
            List<String> instructions(){
                List<String> instructions = new ArrayList<String>();
                instructions.add("* Welcome to Bootcamp... press x the skip");
                instructions.add("* Lets start by getting used to moving. Travel to the green marker on your minimap");
                instructions.add("* To get your character to move right click on the terrain in the direction you want to move");
                return instructions;
            }
            Vector3f location(){
                return new Vector3f();
            }
            ColorRGBA colour(){
                return ColorRGBA.Red;
            }
        }, 
        SHOOT{
            Stage transition(GameCharacterNode character, Vector3f location, boolean targetDistroyed){
                if(targetDistroyed){
                    ((AvatarCharacterNode)character).resetCrouchCheck();
                    return CROUCH;
                }else{
                    return SHOOT;
                }
                
            }
            List<String> instructions(){
                List<String> instructions = new ArrayList<String>();
                instructions.add("* Great!! so you can get around. now let try some shooting");
                instructions.add("* Look around your location to try and find a target to practice shooting");
                instructions.add("* Holding down the space bar while moving the mouse helps you look around");
                instructions.add("* Right click on the target to shoot it");
                return instructions;
            }
            
            ColorRGBA colour(){
                return ColorRGBA.Red;
            }
        },
        CROUCH {
            Stage transition(GameCharacterNode character, Vector3f location, boolean targetDistroyed){
                if(((AvatarCharacterNode)character).hasCrouchedAsSomeStage()){
                    return SWITCH_TO_FIRST_PERSON;
                }else{
                    return CROUCH;
                }
                
            }
            List<String> instructions(){
                List<String> instructions = new ArrayList<String>();
                instructions.add("* Good shooting!! so you can defend your self.");
                instructions.add("* You can also crouch, In battle crouching behind cover can give you huge advantage");
                instructions.add("* Press the s key and crouch!");
                return instructions;
            }
            
            ColorRGBA colour(){
                return ColorRGBA.Red;
            }
        },
        SWITCH_TO_FIRST_PERSON{
            @Override
            Stage transition(GameCharacterNode character, Vector3f location, boolean targetDistroyed) {
                if(targetDistroyed){
                    return SWITCH_TO_THIRD_PERSON;
                }else{
                    return SWITCH_TO_FIRST_PERSON;
                }
            }

            @Override
            List<String> instructions() {
                List<String> instructions = new ArrayList<String>();
                instructions.add("* Great, now shooting targets this way can be difficult specially when they are moving");
                instructions.add("* You can switch to first person shooter view using the tab key.. go ahead try it!!");
                instructions.add("* shoot the target agian by right clicking");
                return instructions;
            }

            @Override
            ColorRGBA colour() {
                return ColorRGBA.Red;
            }
            
        },
        SWITCH_TO_THIRD_PERSON{
            @Override
            Stage transition(GameCharacterNode character, Vector3f location, boolean targetDistroyed) {
                if(targetDistroyed){
                    return COMMAND_UNIT;
                }else{
                    return SWITCH_TO_THIRD_PERSON;
                }
            }

            @Override
            List<String> instructions() {
                List<String> instructions = new ArrayList<String>();
                instructions.add("* Great... this first person view is great for manoeuvring and shooting targets");
                instructions.add("* and the third person veiw is great for navigating around the map and giving commands to your unit");
                instructions.add("* feel free to swtich back and forth using the tab key");
                instructions.add("* for now lets switch back to the third person view");
                instructions.add("* shoot the target again to continue");
                return instructions;
            }

            @Override
            ColorRGBA colour() {
                return ColorRGBA.Red;
            }
        },
        COMMAND_UNIT{
            Stage transition(GameCharacterNode character, Vector3f location, boolean targetDistroyed){
                if(targetDistroyed){
                    return DEPLOY_UNIT;
                }else{
                    return COMMAND_UNIT;
                }
            }
            List<String> instructions(){
                List<String> instructions = new ArrayList<String>();
                instructions.add("* Great!! so you can defend your self.");
                instructions.add("* You are the leader of a squad, your unit should be following you. Left click a member to select them.");
                instructions.add("* You can also click on the icons on the top right or use keys 2, 3 and 4. Use key 1 to select your self");
                instructions.add("* right click on terrain to order him to move OR on target to order him to shoot");
                instructions.add("* go ahead get someone from your unit to shoot the target as well");
                instructions.add("* REMEMBER once you have asked the member to do anything it will stop following you");
                instructions.add("* to get them to follow you again select the member and right click yourself");
                return instructions;
            }
            
            ColorRGBA colour(){
                return ColorRGBA.Red;
            }
        },
        DEPLOY_UNIT{
            Stage transition(GameCharacterNode character, Vector3f location, boolean targetDistroyed){
                if(character instanceof CommandingOfficer){
                    for(Commandable n: ((CommandingOfficer)character).getCharactersUnderCommand()){
                        if(n instanceof Soldier && ((Soldier)n).isStillDeployed()){
                            return TRAINING_COMPLETE;
                        }
                    }
                    return DEPLOY_UNIT;
                }
                return TRAINING_COMPLETE;
                
            }
            List<String> instructions(){
                List<String> instructions = new ArrayList<String>();
                instructions.add("* Great!!! so you can use your squad.");
                instructions.add("* Another usefull order is to command a unit to cover a direction");
                instructions.add("* units covering a direction will shoot any enemy in that direction");
                instructions.add("* click and drag to ask a unit to travel to the click and cover in the direction of the drag");
                instructions.add("* once you command squads you can command the entire squad to cover a direction");
                return instructions;
            }
            Vector3f location(){
                return new Vector3f();
            }
            ColorRGBA colour(){
                return ColorRGBA.Red;
            }
        }, 
        TRAINING_COMPLETE{

            @Override
            Stage transition(GameCharacterNode character, Vector3f location, boolean targetDistroyed) {
                return TRAINING_COMPLETE;
            }

            @Override
            List<String> instructions() {
                List<String> instructions = new ArrayList<String>();
                instructions.add("* congratulations!! you have completed Bootcamp...");
                instructions.add("* await orders from you Commanding Officer...");
                return instructions;
            }

            @Override
            ColorRGBA colour() {
                return ColorRGBA.Red;
            }            
            
        };
        abstract Stage transition(GameCharacterNode character, Vector3f location, boolean targetDistroyed);
        abstract List<String> instructions();
        abstract ColorRGBA colour();
        
    }
    
}
