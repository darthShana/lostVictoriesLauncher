/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.structures;

import com.jme3.asset.AssetManager;
import com.jme3.lostVictories.LostVictory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Cylinder;

import static com.jme3.lostVictories.minimap.MinimapNode.x_rot;

/**
 *
 * @author dharshanar
 */
public class HeloControl extends AbstractControl {
    private static final float MAX_SIZE = 2;
    private final AssetManager assetManager;
    Geometry helo;
    float scale = HeloControl.MAX_SIZE;
    private long lastUpdate;
    private final LostVictory app;

    public HeloControl(AssetManager assetManager, LostVictory app) {
        this.assetManager = assetManager;
        this.app = app;
        
    }

    @Override
    protected void controlUpdate(float tpf) {
        if(spatial.getLocalTranslation().distance(app.avatar.getLocalTranslation())<100 && !((GameHouseNode)spatial).isOwnedBy(app.avatar.getCountry())){
            if(System.currentTimeMillis()-lastUpdate>100) {
                lastUpdate = System.currentTimeMillis();
                if (helo == null) {
                    Cylinder b = new Cylinder(12, 12, 2.5f, .125f, false, false);
                    helo = new Geometry("selected", b);
                    Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mark_mat.setColor("Color", ColorRGBA.White);
                    helo.setMaterial(mark_mat);
                    helo.setLocalRotation(x_rot);
                    helo.setLocalTranslation(0, 0, 0);
                    ((Node) spatial).attachChild(helo);
                } else {
                    scale = scale - (HeloControl.MAX_SIZE * .025f);
                    if (scale <= 0) {
                        scale = HeloControl.MAX_SIZE;
                    }
                    helo.setLocalScale(scale, scale, 1);
                }
            }
        }else{
            if(helo!=null){
                helo.removeFromParent();
                helo = null;
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        return new HeloControl(assetManager, app);
    }


    
}
