/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.minimap.MinimapNode;
import com.jme3.math.*;

import java.util.Map;


/**
 *
 * @author dharshanar
 */
class FirstPersonShooterAppState extends AbstractAppState implements ActionListener, AnalogListener{
    
    private LostVictory app;
    private AvatarCharacterNode avatar;
    private FlyByCamera flyCam;
    private BitmapText ch;
    private final MinimapNode minimapNode;
    private long lastCheckTime;
    private boolean hasMoved;
    
    public FirstPersonShooterAppState(MinimapNode minimapNode) {
        this.minimapNode = minimapNode;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (LostVictory) app;
        avatar = (AvatarCharacterNode) this.app.getAvatar();
        initKeys();
        
        if (app.getInputManager() != null) {
            final Quaternion q = new Quaternion().fromAxes(Vector3f.ZERO, Vector3f.ZERO, avatar.getPlayerDirection());
            
            this.app.getCamera().setRotation(q);
//            this.app.getCamera().setFrustumNear(.25f);
            this.app.getCamera().setLocation(avatar.getLocalTranslation().add(avatar.getFPSViewPoint()));
            
            if(flyCam==null){
                flyCam = new FlyByCamera(app.getCamera());            
                
            }
            
            flyCam.registerWithInput(app.getInputManager());
            flyCam.setEnabled(true);
            
            
            app.getInputManager().setCursorVisible(false);
            
            BitmapFont guiFont = app.getAssetManager().loadFont("Interface/Fonts/SansSerif.fnt");
            if(ch==null){
                ch = new BitmapText(guiFont, false);
                ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
                ch.setColor(ColorRGBA.Blue);
                ch.setText("+"); // crosshairs
            }
            ch.setLocalTranslation(this.app.getSettings().getWidth() / 2 - ch.getLineWidth()/2, this.app.getSettings().getHeight() / 2 + ch.getLineHeight()/2, 0);
            this.app.getGuiNode().attachChild(ch);
        }               
    }
    
    /** Custom Keybinding: Map named messages to inputs. */
    private void initKeys() {
        app.getInputManager().addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        app.getInputManager().addListener(this, "Forward");
        app.getInputManager().addMapping("Backward", new KeyTrigger(KeyInput.KEY_Z));
        app.getInputManager().addListener(this, "Backward");
        app.getInputManager().addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        app.getInputManager().addListener(this, "Left");
        app.getInputManager().addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        app.getInputManager().addListener(this, "Right");
        app.getInputManager().addMapping("Crouch", new KeyTrigger(KeyInput.KEY_S));
        app.getInputManager().addListener(this, "Crouch");
        app.getInputManager().addMapping("fireWeapon", new KeyTrigger(KeyInput.KEY_R));
        app.getInputManager().addListener(this, "fireWeapon");
        
        app.getInputManager().addMapping("shoot", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        app.getInputManager().addListener(this, "shoot");
        app.getInputManager().addListener(this, "mouse_move");

    }
    
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Forward")) {
            if(isPressed){
                avatar.goForward();
            }else{
                avatar.stopForwardMovement();
            }
        }
        if (name.equals("Backward")) {
            if(isPressed){
                avatar.goBackward();
            }else{
                avatar.stopForwardMovement();
            }
        }
        if (name.equals("Left")) {
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
        if (name.equals("Right")) {
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
        if (name.equals("Crouch")) {
            if(isPressed){
                avatar.crouch(avatar);
            }else{
                avatar.stand(avatar);
            }
        }
        if (name.equals("shoot") && isPressed) {
            Vector3f v = avatar.getShootingLocation().add(app.getCamera().getDirection().mult(5));
            avatar.attack(v, avatar);
        }
        
        if(name.equals("fireWeapon")){
            if(isPressed){
                avatar.shoot(avatar.getLocalTranslation().add(avatar.getPlayerDirection().mult(avatar.getMaxRange())));

            }
        }
    }
    
    @Override
    public void update(float tpf) {
        if(avatar!=app.getAvatar()){
            avatar = app.getAvatar();
        }
        
        super.update(tpf); //To change body of generated methods, choose Tools | Templates.
        this.app.getCamera().setLocation(avatar.getLocalTranslation().add(avatar.getFPSViewPoint()));
        final Vector3f direction = app.getCamera().getDirection();
        avatar.getCharacterControl().setViewDirection(direction.normalize());
        if(System.currentTimeMillis()-lastCheckTime>1000 || hasMoved){
            lastCheckTime = System.currentTimeMillis();
            hasMoved = false;
            Ray ray = new Ray(this.app.getCamera().getLocation(), direction);
            ray.setLimit(avatar.getMaxRange());
            CollisionResults results = new CollisionResults();
            try{
                app.getRootNode().collideWith(ray, results);
            }catch(Throwable e){
                e.printStackTrace();
            }
            final CollisionResult closestCollision = avatar.getClosestCollisionThatIsntMe(results);

            final Map.Entry<CanInteractWith, Vector3f> objectTarget = RealTimeStrategyAppState.getObjectTarget(closestCollision);
            if(objectTarget !=null && !objectTarget.getKey().isAlliedWith(avatar)){
                ch.setColor(ColorRGBA.Red);
            }else{
                ch.setColor(ColorRGBA.White);
            }
        }
        minimapNode.updateMinimap(tpf, this.app.getCamera().getRotation().getY()- FastMath.HALF_PI);
    }
    
        @Override
    public void cleanup() {
        flyCam.setEnabled(false);
        app.getInputManager().setCursorVisible(true);
        if (app.getInputManager() != null) {        
            flyCam.unregisterInput();
        }
        this.app.getGuiNode().detachChild(ch);
        app.getInputManager().deleteMapping("Forward");
        app.getInputManager().deleteMapping("Backward");
        app.getInputManager().deleteMapping("Left");
        app.getInputManager().deleteMapping("Right");
        app.getInputManager().deleteMapping("Crouch");
        app.getInputManager().deleteMapping("shoot");
        app.getInputManager().deleteMapping("mouse_move");
        app.getInputManager().removeListener(this);
    }

    public void onAnalog(String name, float value, float tpf) {
        hasMoved = true;
    }


    
}
