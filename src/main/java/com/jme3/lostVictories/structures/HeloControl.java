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
    private final AssetManager assetManager;
    Geometry helo;
    float scale = 1;
    private long lastUpdate;
    private final LostVictory app;

    public HeloControl(AssetManager assetManager, LostVictory app) {
        this.assetManager = assetManager;
        this.app = app;
        
    }

    @Override
    protected void controlUpdate(float tpf) {
        if(System.currentTimeMillis()-lastUpdate>100 && spatial.getLocalTranslation().distance(app.avatar.getLocalTranslation())<50){
            lastUpdate = System.currentTimeMillis();
            if(helo==null){
                Cylinder b = new Cylinder(12, 12, 2.5f, .125f, false, false);
                helo = new Geometry("selected", b);
                Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mark_mat.setColor("Color", ColorRGBA.White);
                helo.setMaterial(mark_mat);
                helo.setLocalRotation(x_rot);
                helo.setLocalTranslation(0, 0, 1.4f);
                ((Node)spatial).attachChild(helo);
            }else{
                scale = scale - .025f;
                if(scale<=0){
                    scale = 1;
                }
                helo.setLocalScale(scale, scale, 1);
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
