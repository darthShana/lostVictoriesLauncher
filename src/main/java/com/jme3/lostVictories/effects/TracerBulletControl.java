/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.effects;

import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author dharshanar
 */
class TracerBulletControl extends AbstractControl {
    public static final float GROWTH_FACTOR = 5;
    private Long spawnTime;
    private final float bulletsPerSecond = 4;
    private final float fullSize;
    private final Iterator<Ray> rays;
    private final Iterator<Float> lifeSpans;
    private final GameCharacterNode shooter;
    private final Vector3f relativePos;
    private final Spatial bullet;
    private final Map<Spatial, Ray> usedBullets = new HashMap<Spatial, Ray>();
    private final float speed;
    

    public TracerBulletControl(GameCharacterNode shooter, Vector3f relativePos, Spatial bullet, List<Ray> r, List<Float> lifeSpans, float fullSize, float speed) {
        rays = r.iterator();
        this.lifeSpans = lifeSpans.iterator();
        this.shooter = shooter;
        this.relativePos = relativePos;
        this.bullet = bullet;
        this.fullSize = fullSize;
        this.speed = speed;
    }

    @Override
    protected void controlUpdate(float tpf) {
        spatial.setLocalTranslation(shooter.getLocalTranslation().add(relativePos));
        if(rays.hasNext() && lifeSpans.hasNext() && isTimeForAnotherBullet()){
            Ray r = rays.next();
            Spatial newBullet = bullet.clone(true);
            final long currentTimeMillis = System.currentTimeMillis();
            newBullet.setUserData("lifeSpan", lifeSpans.next());
            newBullet.setUserData("startTime", currentTimeMillis);
            spawnTime = currentTimeMillis;
            
            try{
                ((Node)spatial).attachChild(newBullet);
            }catch(Exception e){
                e.printStackTrace();
            }
            
            newBullet.lookAt(spatial.getLocalTranslation().add(r.getDirection()), Vector3f.UNIT_Y);
            usedBullets.put(newBullet, r);
        }
        for(Entry<Spatial, Ray> entry:usedBullets.entrySet()){
            float newSize = entry.getKey().getLocalScale().z+GROWTH_FACTOR;
            float f;
            if(newSize<fullSize){
                entry.getKey().setLocalScale(new Vector3f(1, 1, newSize));
                f = newSize/2;
            }else{
                f = speed;
            }
            Vector3f pos = new Vector3f(entry.getKey().getLocalTranslation());
            pos.addLocal(entry.getValue().getDirection().normalize().mult(f));
            entry.getKey().setLocalTranslation(pos);
            
        }
        for(Iterator<Entry<Spatial, Ray>> it = usedBullets.entrySet().iterator();it.hasNext();){
            final Entry<Spatial, Ray> next = it.next();
            if(isExpired(next.getKey())){
                next.getKey().removeFromParent();
                it.remove();
            }
        }
        if(usedBullets.isEmpty()){
            spatial.removeFromParent();
        }
        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}

    private boolean isTimeForAnotherBullet() {
        if(spawnTime==null){
            return true;
        }
        if((System.currentTimeMillis()-spawnTime)>=(1000/bulletsPerSecond)){
            return true;
        }
        return false;
    }

    private boolean isExpired(Spatial key) {
        return (System.currentTimeMillis()-(Long)key.getUserData("startTime"))>=((Float)key.getUserData("lifeSpan")*1000);
    }
    
}
