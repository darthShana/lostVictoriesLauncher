/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.animation.AnimChannel;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.lostVictories.characters.*;
import com.jme3.lostVictories.minimap.MinimapNode;
import com.jme3.lostVictories.structures.Pickable;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Line;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author dharshanar
 */
public class RealTimeStrategyAppState extends AbstractAppState implements ActionListener, AnalogListener{

    private ChaseCamera chaseCamera;
    private LostVictory app;
    private AvatarCharacterNode avatar;
    private Commandable selectedCharacter;
    private Vector3f mousePressPosition;
    public Geometry line1;
    public Geometry line2;
    public Material lineMaterial;
    public JmeCursor enemyCursor;
    public JmeCursor alliedCursor;
    public JmeCursor objectCursor;
    public Entry<CanInteractWith, Vector3f> targetGuidance;
    public Entry<GameCharacterNode, Vector3f> selectionGuidance;
    private Long mouseDragTime;
    MinimapNode minimapNode;
    protected AnimChannel bustChannel;
    private Long enemySelectTime;
    private GameVehicleNode boardedVehicle;
    private HeadsUpDisplayController characterSelectionListener;

    public RealTimeStrategyAppState(MinimapNode minimapNode) {
        this.minimapNode = minimapNode;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (LostVictory) app;

        lineMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        if (app.getInputManager() != null) {
            if (chaseCamera == null) {
                avatar = this.app.getAvatar();
                enemyCursor = (JmeCursor) app.getAssetManager().loadAsset("Textures/Red-cursor.cur");
                alliedCursor = (JmeCursor) app.getAssetManager().loadAsset("Textures/Blue-cursor.cur");
                objectCursor = (JmeCursor) app.getAssetManager().loadAsset("Textures/Green-cursor.cur");
                chaseCamera = new ChaseCamera(app.getCamera(), avatar, app.getInputManager());
                chaseCamera.setDefaultHorizontalRotation(FastMath.HALF_PI);
                chaseCamera.setToggleRotationTrigger(new KeyTrigger(KeyInput.KEY_SPACE));
//                chaseCamera.setSmoothMotion(true);
                chaseCamera.setMaxDistance(50);
                chaseCamera.setDefaultDistance(25);


                setupMinimapPanel(minimapNode, app);
            } else {
                chaseCamera.setEnabled(true);
            }
        }
        initKeys();
        if(selectedCharacter==null){
            selectedCharacter = avatar.select(selectionMark());
        }
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        if(avatar!=app.getAvatar()){
            avatar = app.getAvatar();
            if (avatar.getBoardedVehicle()==null) {
                chaseCamera.setSpatial(avatar);
                avatar.addControl(chaseCamera);
            }
            selectedCharacter = avatar.select(selectedCharacter);
            boardedVehicle = null;
        }
        if(avatar.getBoardedVehicle()!=null && avatar.getBoardedVehicle()!=boardedVehicle){
            boardedVehicle = avatar.getBoardedVehicle();
            chaseCamera.setSpatial(boardedVehicle);
            selectedCharacter = boardedVehicle.select(selectedCharacter);
            boardedVehicle.addControl(chaseCamera);
        }else if(boardedVehicle!=null && avatar.getBoardedVehicle()==null){
            boardedVehicle = null;
            chaseCamera.setSpatial(avatar);
            avatar.addControl(chaseCamera);
            selectedCharacter = avatar.select(selectedCharacter);
        }

        if(mouseDragTime!=null && System.currentTimeMillis()-mouseDragTime>2000){
            if(line1!=null && line2!=null){
                app.getRootNode().detachChild(line1);
                app.getRootNode().detachChild(line2);
            }
            targetGuidance = null;
            app.getInputManager().setMouseCursor(null);
            mousePressPosition = null;
            mouseDragTime = null;
        }

//        if(selectedCharacter instanceof GameCharacterNode){
//            updateBustAnimation((GameCharacterNode)selectedCharacter);
//        }
        minimapNode.updateMinimap(tpf, chaseCamera.getHorizontalRotation());
    }

    /** Custom Keybinding: Map named messages to inputs. */
    private void initKeys() {
        app.getInputManager().addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        app.getInputManager().addListener(this, "Forward");
        app.getInputManager().addMapping("turnLeft", new KeyTrigger(KeyInput.KEY_A));
        app.getInputManager().addListener(this, "turnLeft");
        app.getInputManager().addMapping("turnRight", new KeyTrigger(KeyInput.KEY_D));
        app.getInputManager().addListener(this, "turnRight");
        app.getInputManager().addMapping("Reverse", new KeyTrigger(KeyInput.KEY_S));
        app.getInputManager().addListener(this, "Reverse");
        app.getInputManager().addMapping("selectAvatar", new KeyTrigger(KeyInput.KEY_1));
        app.getInputManager().addListener(this, "selectAvatar");
        app.getInputManager().addMapping("selectUnit1", new KeyTrigger(KeyInput.KEY_2));
        app.getInputManager().addListener(this, "selectUnit1");
        app.getInputManager().addMapping("selectUnit2", new KeyTrigger(KeyInput.KEY_3));
        app.getInputManager().addListener(this, "selectUnit2");
        app.getInputManager().addMapping("selectUnit3", new KeyTrigger(KeyInput.KEY_4));
        app.getInputManager().addListener(this, "selectUnit3");
        app.getInputManager().addMapping("completeBootCamp", new KeyTrigger(KeyInput.KEY_X));
        app.getInputManager().addListener(this, "completeBootCamp");
        app.getInputManager().addMapping("fireWeapon", new KeyTrigger(KeyInput.KEY_R));
        app.getInputManager().addListener(this, "fireWeapon");
        app.getInputManager().addMapping("disembark", new KeyTrigger(KeyInput.KEY_E));
        app.getInputManager().addListener(this, "disembark");
        app.getInputManager().addMapping("crouch", new KeyTrigger(KeyInput.KEY_M));
        app.getInputManager().addListener(this, "crouch");
        app.getInputManager().addMapping("turretLeft", new KeyTrigger(KeyInput.KEY_K));
        app.getInputManager().addListener(this, "turretLeft");
        app.getInputManager().addMapping("turretRight", new KeyTrigger(KeyInput.KEY_L));
        app.getInputManager().addListener(this, "turretRight");
        app.getInputManager().addMapping("action", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        app.getInputManager().addListener(this, "action");
        app.getInputManager().addMapping("pick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addListener(this, "pick");
        app.getInputManager().addMapping("mouse_move", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        app.getInputManager().addMapping("mouse_move", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        app.getInputManager().addMapping("mouse_move", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        app.getInputManager().addMapping("mouse_move", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        app.getInputManager().addListener(this, "mouse_move");
    }

    protected Geometry selectionMark() {
        Cylinder sphere = new Cylinder(12, 12, .5f, .1f, false, false);
        Geometry mark = new Geometry("selected", sphere);
        Material mark_mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.White);
        mark.setMaterial(mark_mat);
        mark.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
        return mark;
    }

    @Override
    public void cleanup() {
        chaseCamera.setEnabled(false);
        app.getInputManager().deleteMapping("Forward");
        app.getInputManager().deleteMapping("Reverse");
        app.getInputManager().deleteMapping("turnLeft");
        app.getInputManager().deleteMapping("turnRight");
        app.getInputManager().deleteMapping("selectAvatar");
        app.getInputManager().deleteMapping("selectUnit1");
        app.getInputManager().deleteMapping("selectUnit2");
        app.getInputManager().deleteMapping("selectUnit3");
        app.getInputManager().deleteMapping("completeBootCamp");
        app.getInputManager().deleteMapping("fireWeapon");
        app.getInputManager().deleteMapping("disembark");
        app.getInputManager().deleteMapping("crouch");
        app.getInputManager().deleteMapping("action");
        app.getInputManager().deleteMapping("pick");
        app.getInputManager().deleteMapping("turretLeft");
        app.getInputManager().deleteMapping("turretRight");

        app.getInputManager().removeListener(this);
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if(avatar.isDead()){
            return;
        }
        if (name.equals("Forward")) {
            if(isPressed){
                avatar.goForward();
            }else{
                avatar.stopForwardMovement();
            }
        }
        if (name.equals("Reverse")) {
            if(isPressed){
                avatar.goBackward();
            }else{
                avatar.stopForwardMovement();
            }
        }

        if (name.equals("turnLeft")) {
            if(isPressed){
                if(avatar.isWalkingForward()){
                    avatar.turnLeft();
                }else{
                    avatar.shiftLeft();
                }
            }else{
                if(avatar.isWalkingForward()){
                    avatar.stopTurningLeft();
                }else{
                    avatar.stopForwardMovement();
                }
            }
        }
        if (name.equals("turnRight")) {
           if(isPressed){
               if(avatar.isWalkingForward()){
                   avatar.turnRight();
               }else{
                   avatar.shiftRight();
               }
            }else{
               if(avatar.isWalkingForward()){
                   avatar.stopTurningRight();
               }else{
                   avatar.stopForwardMovement();
               }
            }
        }
        if (name.equals("crouch")) {
           if(isPressed){
                avatar.crouch(avatar);
            }else{
                avatar.stand(avatar);
            }
        }
        if (name.equals("turretLeft")) {
            if(boardedVehicle instanceof MediumTankNode)
            if(isPressed){
                ((MediumTankNode)boardedVehicle).turretLeft();
            }else{
                ((MediumTankNode)boardedVehicle).stopTurret();
            }
        }
        if (name.equals("turretRight")) {
           if(boardedVehicle instanceof MediumTankNode)
            if(isPressed){
                ((MediumTankNode)boardedVehicle).turretRight();
            }else{
                ((MediumTankNode)boardedVehicle).stopTurret();
            }
        }

        if (name.equals("completeBootCamp")) {
           if(isPressed){
               avatar.completeBootCamp();
           }
        }

        if(name.equals("fireWeapon")){
            if(isPressed){
                GameCharacterNode c = (GameCharacterNode)selectedCharacter;
                c.shoot(c.getLocalTranslation().add(c.getAimingDirection().mult(c.getMaxRange())));

            }
        }

        if(name.equals("disembark")){
            if(isPressed){
                if(selectedCharacter instanceof GameVehicleNode){
                    ((GameVehicleNode)selectedCharacter).requestDisembarkPassengers(avatar);
                }
            }
        }

        if(name.equals("pick") && isPressed){
            if(selectionGuidance!=null && !selectionGuidance.getKey().isSelected()){
                toggleSelectionIcon(selectionGuidance.getKey());
                selectCharacter(selectionGuidance.getKey());
            }else{
                CollisionResult closestCollision = getCursorPossitionInWorld();
                if(closestCollision!=null){
                    for(Node n = closestCollision.getGeometry().getParent();n!=null;n = n.getParent()){
                        if(n.getUserData("GameCharacterControl")!=null && !"blank".equals(n.getUserData("GameCharacterControl")) && ((GameCharacterNode)n).isUnderChainOfCommandOf(avatar, 5) || n==avatar){
                            toggleSelectionIcon((GameCharacterNode)n);
                            selectCharacter((GameCharacterNode) n);
                        }
                    }
                }
            }
            if(line1!=null && line2!=null){
                app.getRootNode().detachChild(line1);
                app.getRootNode().detachChild(line2);
            }
            mouseDragTime = null;
        }
        if (name.equals("action")) {
            if(isPressed && targetGuidance!=null){
                if(targetGuidance.getKey() instanceof Pickable){
                    selectedCharacter.collect((Pickable)targetGuidance.getKey(), avatar);
                }else{
                    selectedCharacter.attack(targetGuidance.getKey().getLocalTranslation().add(targetGuidance.getValue()), avatar);
                }
                return;
            }else if(isPressed && selectionGuidance!=null){
                if(selectionGuidance.getKey() instanceof GameVehicleNode && ((GameVehicleNode)selectionGuidance.getKey()).canBoard(selectedCharacter)){
                    selectedCharacter.requestBoarding((GameVehicleNode)selectionGuidance.getKey(), avatar);
                }
            }else{

                CollisionResult closestCollision = getCursorPossitionInWorld();
                if(closestCollision!=null){
                    Vector3f contactPoint = closestCollision.getContactPoint();

                    if(!app.getTerrain().hasChild(closestCollision.getGeometry())){
                        if(selectedCharacter!=null && selectedCharacter instanceof GameCharacterNode){
                            ((GameCharacterNode)selectedCharacter).getPathFinder().warpInside(contactPoint);
                        }
                    }

                    if(isPressed){
                        mousePressPosition = contactPoint;
                        mouseDragTime = System.currentTimeMillis();
                    } else{
                        if(mousePressPosition!=null && !isClose(mousePressPosition, contactPoint)){
                            selectedCharacter.cover(mousePressPosition, contactPoint, avatar);
                        } else if(avatar.hasChild(closestCollision.getGeometry()) && selectedCharacter!=avatar){
                            selectedCharacter.follow(avatar, avatar);
                        } else if(mousePressPosition!=null){
                            selectedCharacter.travel(contactPoint, avatar);
                        }

                    }

                }
            }
            if(!isPressed){
                mousePressPosition = null;
            }
        }

        if (name.equals("selectAvatar") && isPressed && selectedCharacter!=avatar) {
            characterSelectionListener.clearSelected();
            if(boardedVehicle!=null){
                boardedVehicle.select(selectedCharacter);
            }else{
                selectedCharacter = avatar.select(selectedCharacter);
            }
        }
        List<Commandable> charactersUnderCommand = avatar.getCharactersUnderCommand().stream().filter(c->!c.hasBoardedVehicle()).collect(Collectors.toList());
        if (name.equals("selectUnit1") && isPressed ) {
            if(charactersUnderCommand.size()>0){
                toggleSelectionIcon(charactersUnderCommand.get(0));
                selectedCharacter = charactersUnderCommand.get(0).select(selectedCharacter);
            }
        }
        if (name.equals("selectUnit2") && isPressed) {
            if(charactersUnderCommand.size()>1){
                toggleSelectionIcon(charactersUnderCommand.get(1));
                selectedCharacter = charactersUnderCommand.get(1).select(selectedCharacter);
            }
        }
        if (name.equals("selectUnit3") && isPressed) {
            if(charactersUnderCommand.size()>2){
                toggleSelectionIcon(charactersUnderCommand.get(2));
                selectedCharacter = charactersUnderCommand.get(2).select(selectedCharacter);
            }
        }
    }

    private void toggleSelectionIcon(Commandable toSelect) {
        if(selectedCharacter!=null) {
            characterSelectionListener.toggleIcon(selectedCharacter, false);
        }
        characterSelectionListener.toggleIcon(toSelect, true);
    }

    Commandable getSelectedCharacter() {
        return selectedCharacter;
    }

    public void selectCharacter(Commandable n) {
        selectedCharacter = n.select(selectedCharacter);
    }

    public CollisionResult getCursorPossitionInWorld() {
        Vector2f click2d = new Vector2f(app.getInputManager().getCursorPosition());
        final Vector3f pos = app.getCamera().getWorldCoordinates(click2d, 0);
        final Vector3f dir = app.getCamera().getWorldCoordinates(click2d, 0.1f);
        dir.subtractLocal(pos).normalizeLocal();
        Ray ray = new Ray(pos, dir);
        ray.setLimit(500);
        CollisionResults results = new CollisionResults();
        try{
            app.getRootNode().collideWith(ray, results);
        }catch(Throwable e){}
        final CollisionResult closestCollision = results.getClosestCollision();
        return closestCollision;
    }

    private boolean isClose(Vector3f v1, Vector3f v2) {
        if(Math.abs(v1.x - v2.x)>1){
            return false;
        }
        if(Math.abs(v1.y - v2.y)>1){
            return false;
        }
        if(Math.abs(v1.z - v2.z)>1){
            return false;
        }
        return true;
    }

    public void onAnalog(String name, float value, float tpf) {
        if (name.equals("mouse_move")) {
            if(mousePressPosition!=null && (System.currentTimeMillis()-mouseDragTime)>100){
                CollisionResult closestCollision = getCursorPossitionInWorld();
                if(closestCollision!=null){
                    final Vector3f contactPoint = closestCollision.getContactPoint();

                    if(!isClose(mousePressPosition, contactPoint)){
                        if(line1!=null && line2!=null){
                            app.getRootNode().detachChild(line1);
                            app.getRootNode().detachChild(line2);
                        }
                        Vector3f d1=contactPoint.subtract(mousePressPosition).normalizeLocal().mult(20);
                        Vector3f d2=new Quaternion().fromAngleAxis(FastMath.QUARTER_PI/2, Vector3f.UNIT_Y).mult(d1);
                        Vector3f d3=new Quaternion().fromAngleAxis(-FastMath.QUARTER_PI/2, Vector3f.UNIT_Y).mult(d1);
                        line1 = new Geometry("line1", new Line(mousePressPosition, mousePressPosition.add(d2)));
                        line2 = new Geometry("line2", new Line(mousePressPosition, mousePressPosition.add(d3)));
                        line1.setMaterial(lineMaterial);
                        line2.setMaterial(lineMaterial);
                        app.getRootNode().attachChild(line1);
                        app.getRootNode().attachChild(line2);
                    }
                }
            }else{
                if(line1!=null && line2!=null){
                    app.getRootNode().detachChild(line1);
                    app.getRootNode().detachChild(line2);
                }
                Entry<CanInteractWith, Vector3f> guidance = null;
                try{
                    guidance = targetingGuidance();
                }catch(NoSuchElementException e){
                }
                if(guidance!=null){
                    handleSelectionGuidance(guidance, app.getInputManager(), app.avatar);
                }else if(enemySelectTime==null || (System.currentTimeMillis()-enemySelectTime)>100){
                    app.getInputManager().setMouseCursor(null);
                    targetGuidance = null;
                    if(selectionGuidance!=null && selectionGuidance.getKey() instanceof CadetCorporal){
                        ((CadetCorporal)selectionGuidance.getKey()).unhighlightSquad();
                    }
                    selectionGuidance = null;
                }
            }
        }
    }

    private Entry<CanInteractWith, Vector3f> targetingGuidance() {
        final CollisionResult closesetCollision = getCursorPossitionInWorld();
        Entry hum = getObjectTarget(closesetCollision);
        if(hum!=null){
            return hum;
        }
        if(closesetCollision!=null){
            final List<GameCharacterNode> charactersInBlastRange = WorldMap.get().getCharactersInBlastRange(closesetCollision.getContactPoint());
            for(GameCharacterNode c:charactersInBlastRange){
                if(!c.isAlliedWith(app.avatar)){
                    return new AbstractMap.SimpleEntry<CanInteractWith, Vector3f>(c, c.getPositionToTarget(app.avatar));                     
                }
            }
            
        }
        
        return null;
    }

//    public void setSelectedCharacterBust(GameCharacterNode character) {
//        Node clone = character.getGeometry().clone(true);
//        final Spatial child = closeUpNode.getChild("avatar_bust");
//        if(child!=null){
//            child.removeFromParent();
//        }
//        clone.setName("avatar_bust");
//        clone.setLocalTranslation(character.getBustTranslation());
//        if(character.getControl(AnimControl.class)!=null){
//            final AnimControl control = clone.getControl(AnimControl.class);
//            bustChannel = control.createChannel();
//            bustChannel.setAnim(character.getControl(AnimControl.class).getChannel(0).getAnimationName());
//            bustChannel.setSpeed(.2f);
//            bustChannel.setLoopMode(LoopMode.Loop);
//        }
//        closeUpNode.attachChild(clone);
//    }

//    private void updateBustAnimation(GameCharacterNode selectedCharacter) {
//        try{
//            AnimChannel a = selectedCharacter.getControl(AnimControl.class).getChannel(0);
//            if(!bustChannel.getAnimationName().equals(a.getAnimationName())){
//                bustChannel.setAnim(a.getAnimationName());
//                bustChannel.setSpeed(a.getSpeed());
//                bustChannel.setLoopMode(a.getLoopMode());
//            }
//        }catch(NullPointerException e){}
//
//    }

    public static Entry<CanInteractWith, Vector3f> getObjectTarget(CollisionResult closestCollision) {
        if(closestCollision!=null){
            final Geometry geometry = closestCollision.getGeometry();
            for(Node n = geometry.getParent();n!=null;n = n.getParent()){
                if(n.getUserData("GameCharacterControl")!=null){
                    final CanInteractWith target = (CanInteractWith)n;
                    return new AbstractMap.SimpleEntry<CanInteractWith, Vector3f>(target, closestCollision.getContactPoint().subtract(target.getLocalTranslation()));
                }
            }
        }
        return null;
    }


    
    private void setupMinimapPanel(MinimapNode panel, Application app){
        this.app.getRootNode().attachChild(panel);
        panel.setLocalTranslation(0, -900, 0);     
        
        Camera cam2 = app.getCamera().clone();
        cam2.setViewPort(0.75f, 1f, 0f, 0.22f);
        cam2.setLocation(new Vector3f(0f, -700, 0f));
        minimapNode.setCamera(cam2);

        ViewPort view2 = app.getRenderManager().createMainView("Bottom Right", cam2);
        view2.setBackgroundColor(new ColorRGBA(.50f, .50f, .50f, 1));
        view2.setClearFlags(true, true, true);
        view2.attachScene(panel);
    }

    public boolean isSelected(UUID identity) {
        return selectedCharacter!=null && selectedCharacter.getIdentity().equals(identity);
    }

    public void handleSelectionGuidance(Entry<CanInteractWith, Vector3f> guidance, InputManager inputManager, AvatarCharacterNode avatar) {
        if(!guidance.getKey().isAlliedWith(avatar) && !guidance.getKey().isAbbandoned()){
            inputManager.setMouseCursor(enemyCursor);
            enemySelectTime = System.currentTimeMillis();
            targetGuidance = guidance;
        }else if(guidance.getKey() instanceof GameCharacterNode) {
            GameCharacterNode selected = (GameCharacterNode) guidance.getKey();   
            if(selected.isUnderChainOfCommandOf(avatar, 5) || guidance.getKey() instanceof GameVehicleNode){
                inputManager.setMouseCursor(alliedCursor);
                selectionGuidance = new AbstractMap.SimpleEntry<>(selected, guidance.getValue());
                if(selected instanceof Private && selected.getCommandingOfficer()!=avatar && selected.getCommandingOfficer() instanceof CadetCorporal){
                    selectionGuidance = new AbstractMap.SimpleEntry<>((GameCharacterNode) selected.getCommandingOfficer(), guidance.getValue());
                    ((CadetCorporal)selectionGuidance.getKey()).highlightSquad();
                }else if(selected instanceof CadetCorporal){
                    ((CadetCorporal)selectionGuidance.getKey()).highlightSquad();
                }
            }
        }else if(guidance.getKey() instanceof Pickable){
            inputManager.setMouseCursor(objectCursor);
            targetGuidance = guidance;
        }
    }


    public void setCharacterSelectionListener(HeadsUpDisplayController characterSelectionListener) {
        this.characterSelectionListener = characterSelectionListener;
    }
}
