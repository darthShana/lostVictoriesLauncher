/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.structures;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.bullet.BulletAppState;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.StructureStatus;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.network.messages.CaptureStatus;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.scene.Node;

import java.util.Map;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class GameHouseNode extends GameStructureNode{

    private final Map<Country, Node> flags;
    private final Node neuralFlag;
    private Node currentFlag;
    private Country owner;
    private FlagAction action;
    private StructureStatus structureStatus;
    private final UUID id;
    private final String houseType;
    private Country contestingOwner;
    private final Node rootNode;
    private Node flagPost;
    
    public GameHouseNode(UUID id, String houseType, Node house, Map<Country, Node> flags, Node neuralFlag, BulletAppState bulletAppState, CollisionShapeFactoryProvider collisionShapeFactoryProvider, Node rootNode) {
        super(house, bulletAppState, collisionShapeFactoryProvider);
        this.rootNode = rootNode;
        this.flags = flags;
        this.neuralFlag = neuralFlag;
        this.structureStatus = StructureStatus.NEUTRAL;
        attachFlagPost(neuralFlag);
        this.id = id;
        this.houseType = houseType;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void simpleUpate(float tpf, WorldMap map){
        if(this.action!=null){
            this.action.doFlagAction(this, flags);
            this.action = null;
        }
    }    

    public void updateOwership(HouseMessage houseMessage) {
        if(houseMessage.getCaptureStatus()==CaptureStatus.CAPTURING && structureStatus!=StructureStatus.CAPTURING){
            final Country valueOf = Country.valueOf(houseMessage.getContestingOwner().toString());
            this.action = new CaptureFlagAction(valueOf);
            structureStatus = StructureStatus.CAPTURING;
            contestingOwner = valueOf;
        }
        if(houseMessage.getCaptureStatus()==CaptureStatus.DECAPTURING && structureStatus!=StructureStatus.DECAPTURING){
            final Country valueOf = Country.valueOf(houseMessage.getOwner().toString());
            if(currentFlag==null){
                setFlag(flags.get(valueOf));
            }
            structureStatus = StructureStatus.DECAPTURING;
            this.action = new DecaptureFlagAction(this);
        }
        if(houseMessage.getCaptureStatus()==CaptureStatus.NONE && structureStatus!=StructureStatus.NEUTRAL){
            structureStatus = StructureStatus.NEUTRAL;
            this.action = new CancelFlagCaptureAction();
        }
        if(houseMessage.getCaptureStatus()==CaptureStatus.CAPTURED && structureStatus!=StructureStatus.CAPTURED){
            if(structureStatus!=StructureStatus.CAPTURING){
                this.action = new CaptureFlagAction(Country.valueOf(houseMessage.getOwner().toString()));
            }
            structureStatus = StructureStatus.CAPTURED;
            
        }
        if(houseMessage.getOwner()!=null){
            owner = Country.valueOf(houseMessage.getOwner().toString());
        }else{
            owner = null;
        }
        
        //        if(new Vector3f(houseMessage.getLocalTranslation().x, 0, houseMessage.getLocalTranslation().z).distance(new Vector3f(getLocalTranslation().x, 0, getLocalTranslation().z))>5){
//            System.out.println("house has shifted from:"+houseMessage.getLocalTranslation()+" to:"+getLocalTranslation());
//        }
        
    }
    
    public StructureStatus getStatus(){
        return structureStatus;
    }
    
    public Country getContestingOwner(){
        return contestingOwner;
    }
    
    private void attachFlagPost(Node flagPost){
        if("Models/Structures/casaMedieval.j3o".equals(houseType)){
            flagPost.setLocalTranslation(getLocalTranslation().add(0, 0, 0));
        }else if("Models/Structures/house_1.j3o".equals(houseType)){
            flagPost.setLocalTranslation(getLocalTranslation().add(3, 0, 3));
        }else if("Models/Structures/house2.j3o".equals(houseType)){
            flagPost.setLocalTranslation(getLocalTranslation().add(3, 0, 3));
        }else if("Models/Structures/house.j3o".equals(houseType)){
            flagPost.setLocalTranslation(getLocalTranslation().add(3, 0, 3));
        }else{
            flagPost.setLocalTranslation(getLocalTranslation().add(5, 0, 5));
        }
        this.flagPost = flagPost;
        rootNode.attachChild(flagPost);
    }

    private void setFlag(Node flag) {
        if(currentFlag!=null){
            currentFlag.removeFromParent();
        }
        currentFlag = flag;
        flagPost.attachChild(flag);       
    }
    
    private void removeFlag(){
        if(currentFlag!=null){
            currentFlag.removeFromParent();
        }
        currentFlag = null;
    }
    
    public Country getOwner(){
        return owner;
    }

    public boolean isOwnedBy(Country country) {
        return country.equals(owner);
    }
    
    private interface FlagAction {

        public void doFlagAction(GameHouseNode aThis, Map<Country, Node> flags);
    }

    
    private class CaptureFlagAction implements FlagAction, AnimEventListener{
        private final Country c;

        public CaptureFlagAction(Country c) {
            this.c = c;
            structureStatus = StructureStatus.CAPTURING;
        }
        
        Country getCountry(){
            return c;
        }
        
        public void doFlagAction(GameHouseNode structure, Map<Country, Node> flags) {
            structure.setFlag(flags.get(c));
            AnimControl control = currentFlag.getControl(AnimControl.class);
            AnimChannel channel;
            if(control.getNumChannels()==0){
                channel = control.createChannel();
            }else{
                channel = control.getChannel(0);
            }
            
            channel.setAnim("captureAction");
            channel.setLoopMode(LoopMode.DontLoop);
            channel.setSpeed(.3f);
            control.clearListeners();
            control.addListener(this);
            
        }
        
        public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
            if (animName.equals("captureAction")) {
                channel.setAnim("flagAction", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
                channel.setSpeed(1f);               
            }
        }

        public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
            
        }
    }

    private class DecaptureFlagAction implements FlagAction, AnimEventListener {

        private final GameHouseNode structure;
        
        public DecaptureFlagAction(GameHouseNode structure) {
            this.structure = structure;
            structureStatus = StructureStatus.DECAPTURING;
        }

        public void doFlagAction(GameHouseNode structure, Map<Country, Node> flags) {
            AnimControl control = currentFlag.getControl(AnimControl.class);
            AnimChannel channel;
            if(control.getNumChannels()==0){
                channel = control.createChannel();
            }else{
                channel = control.getChannel(0);
            }
            channel.setAnim("decaptureAction");
            channel.setLoopMode(LoopMode.DontLoop);
            channel.setSpeed(.3f);
            control.clearListeners();
            control.addListener(this);
        }

        public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
            structure.removeFlag();
            structureStatus = StructureStatus.NEUTRAL;
        }

        public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
            
        }
    }
    
    private class CancelFlagCaptureAction implements FlagAction {


        public CancelFlagCaptureAction() {
            structureStatus = StructureStatus.NEUTRAL;
        }

        public void doFlagAction(GameHouseNode structure, Map<Country, Node> flags) {
            structure.removeFlag();
        }
    }
    
}
