/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.minimap;

import com.google.common.base.Function;
import com.jme3.asset.AssetManager;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.LostVictory;
import com.jme3.lostVictories.StructureStatus;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.VirtualGameCharacterNode;
import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.texture.Texture;

import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author dharshanar
 */
public class MinimapNode extends Node{
    public static final ColorRGBA LIGHT_GREEN = new ColorRGBA(51f/255, 204f/255, 51f/255, 1);
    public static final ColorRGBA LIGHT_YELLOW = new ColorRGBA(102f/255, 51f/255, 0/255, 1);;
    public static final ColorRGBA NEUTRAL = new ColorRGBA(0, 0, 0, 1);
    public static final ColorRGBA GREEN = new ColorRGBA(51f/255, 204f/255, 51f/255, 1);
    public static final ColorRGBA ENEMY = new ColorRGBA(102f/255, 51f/255, 0/255, 1);

    Node avatar = new Node("avatar");
    Node objective = new Node("objective");
    Node houses = new Node("houses");
    Node enemies = new Node("enemies");
    Node allies = new Node("allies");
    private final LostVictory app;
    protected float viewAngle;
    protected Quaternion q;
    protected Quaternion q_neg;
    public static Quaternion x_rot = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X);
    public static Quaternion y_rot = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y);
//    final Quaternion camRotation = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X).mult(new Quaternion().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Z));
    final Quaternion camRotation = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X);
    
    Map<UUID, Geometry> houseIdentity = new HashMap<UUID, Geometry>();
    Map<UUID, ColorRGBA> houseStatus = new HashMap<UUID, ColorRGBA>();
    Map<Vector3f, Geometry> enemyMap = new HashMap<Vector3f, Geometry>();
    Map<Vector3f, Geometry> allyMap = new HashMap<Vector3f, Geometry>();
    boolean avatarSelected;
    
    private Camera camera;
    private long lastUpdated = 0;
    
    Function<Integer, Mesh> circle = new Function<Integer, Mesh>() {
        public Mesh apply(Integer radius) {
            return new Cylinder(12, 12, radius, .5f, true, false);
        }
    };
    
    Function<Integer, Mesh> box = new Function<Integer, Mesh>() {
        public Mesh apply(Integer radius) {
            return new Box(radius, radius, radius);
        }
    };
    
    public MinimapNode(String minimap, LostVictory app) {
        super(minimap);
        this.app = app;
        
        Geometry clone4 = getGeometry(circle.apply(10), app.getAssetManager(), ColorRGBA.Cyan);
        
        
        avatar.attachChild(clone4);
        Geometry background = loadBackground(app.getAssetManager());
        
        attachChild(background);
        attachChild(objective);
        attachChild(houses);
        attachChild(avatar);
        attachChild(enemies);
        attachChild(allies);
    
    }
    
    
    private Geometry getGeometry(Mesh b, AssetManager assetManager, ColorRGBA color) {
        Geometry clone = new Geometry("selected", b);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", color);
        clone.setMaterial(mark_mat);
        clone.setLocalRotation(x_rot);
        return clone;
    }
    
    private Node getArrow(AssetManager assetManager, ColorRGBA color, float length) {
        Cylinder b = new Cylinder(6, 6, 5, length, true, false);
        Geometry clone1 = new Geometry("selected", b);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", color);
        clone1.setMaterial(mark_mat);
        
        Cylinder b1 = new Cylinder(6, 6, 2.5f, length/2, true, false);
        Geometry clone2 = new Geometry("s1", b1);
        clone2.setMaterial(mark_mat);
        clone2.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.QUARTER_PI, Vector3f.UNIT_Y));
        clone2.setLocalTranslation(new Vector3f(-10, 0, 25));
        
        Cylinder b2 = new Cylinder(6, 6, 2.5f, length/2, true, false);
        Geometry clone3 = new Geometry("s2", b2);
        clone3.setMaterial(mark_mat);
        clone3.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.QUARTER_PI, Vector3f.UNIT_Y));
        clone3.setLocalTranslation(new Vector3f(10, 0, 25));
        
        final Node node = new Node();
        node.attachChild(clone1);
        node.attachChild(clone2);
        node.attachChild(clone3);
        
        return node;
    }

    Node shape;
    
    public void updateMinimap(float tpf, float cameraRoation) {
        if(app.avatar==null){
            return;
        }
        
        if(System.currentTimeMillis()-lastUpdated>1000){
            lastUpdated = System.currentTimeMillis();
            for(GameHouseNode house: WorldMap.get().getAllHouses()){
                if(!houseIdentity.containsKey(house.getId())){
                    final ColorRGBA statusColour = getStatusColour(house, app.avatar.getCountry());
                    final Geometry h = getGeometry(box.apply(5), app.getAssetManager(), statusColour);
                    h.setUserData("color", statusColour);
                    h.setLocalTranslation(house.getLocalTranslation().x, 0, house.getLocalTranslation().z);
                    houseIdentity.put(house.getId(), h);
                    houseStatus.put(house.getId(), statusColour);
                    houses.attachChild(h);
                }else{
                    final ColorRGBA statusColour = getStatusColour(house, app.avatar.getCountry(), houseIdentity.get(house.getId()));
                    if(!statusColour.equals(houseStatus.get(house.getId()))){
                        final Geometry get = houseIdentity.get(house.getId());
                        Vector3f v = new Vector3f(get.getLocalTranslation());
                        get.removeFromParent();
                        final Geometry h = getGeometry(box.apply(5), app.getAssetManager(), statusColour);
                        h.setUserData("color", statusColour);
                        h.setLocalTranslation(v);
                        houseIdentity.put(house.getId(), h);
                        houseStatus.put(house.getId(), statusColour);
                        houses.attachChild(h);
                    }
                }
            }
            
            Set<Vector3f> tmpMap = new HashSet<Vector3f>();
            for(Vector3f enemy: app.avatar.getSupreamLeader(10).getEnemyActivity().activity()){
                Vector3f ee = new Vector3f(enemy.x, 0, enemy.z);
                if(!enemyMap.containsKey(ee)){
                    Geometry e = getGeometry(circle.apply(5), app.getAssetManager(), ColorRGBA.Red);
                    e.setLocalTranslation(ee);
                    enemyMap.put(ee, e);
                    enemies.attachChild(e);
                }
                tmpMap.add(ee);
            }
            enemyMap = updateGeometry(enemyMap, tmpMap);
            
            tmpMap = new HashSet<Vector3f>();
            for(Commandable c:WorldMap.get().getAllCharacters()){
                if(c.getCountry().isAlliedWith(app.avatar)){
                    updateCharacterOnMap(c, tmpMap);
                }
            }
            for(Commandable c: app.avatar.getCharactersUnderCommand()){
                if(c instanceof VirtualGameCharacterNode){
                    updateCharacterOnMap(c, tmpMap);
                }
            }
            allyMap = updateGeometry(allyMap, tmpMap);
            
        }
        
        
        Vector3f loca = app.avatar.getLocalTranslation();
        avatar.setLocalTranslation(loca.x, 0, loca.z);
        
        
        
        camera.setLocation(new Vector3f(loca.x, -100, loca.z));
        camera.setRotation(camRotation.mult(new Quaternion().fromAngleAxis(cameraRoation+FastMath.HALF_PI, Vector3f.UNIT_Z)));
    }

    

    private Geometry loadBackground(AssetManager assetManager) {
        Box bgCube = new Box(512, 1, 512);
        Geometry bgGeo = new Geometry("bg", bgCube);
        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture cube1Tex = assetManager.loadTexture("Scenes/stalingrad.jpg");
        m.setTexture("ColorMap", cube1Tex);
        bgGeo.setMaterial(m);
        //bgGeo.setLocalTranslation(225, -1, -100);
        bgGeo.setLocalTranslation(0, -1, 0);
        bgGeo.setLocalRotation(y_rot);
        return bgGeo;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public static ColorRGBA getStatusColour(GameHouseNode house, Country country, Geometry... g) {
        BiFunction<Geometry[], ColorRGBA, ColorRGBA> p = new BiFunction<Geometry[], ColorRGBA, ColorRGBA>() {

            public ColorRGBA apply(Geometry[] g, ColorRGBA c) {
                if(g.length>0){
                    if(g[0].getUserData("color")==c){
                        return ColorRGBA.Black;
                    }
                }
                return c;
            }
        };
        
        if(house.getStatus()== StructureStatus.CAPTURING){
            if(house.getContestingOwner()==country){
                return p.apply(g, LIGHT_GREEN);
            }else{
                return p.apply(g, LIGHT_YELLOW);
            }
        }
        
        if(house.getStatus()== StructureStatus.DECAPTURING){
            if(house.getOwner()==country){
                return p.apply(g, LIGHT_YELLOW);
            }else{
                return p.apply(g, LIGHT_GREEN);
            }
        }
        
        if(house.getStatus()==StructureStatus.NEUTRAL){
            return NEUTRAL;
        }
        
        if(house.getOwner()==country){
            return GREEN;
        }else{
            return ENEMY;
        }
        
        
    }

    private Map<Vector3f, Geometry> updateGeometry(Map<Vector3f, Geometry> map, Set<Vector3f> tmpMap) {
        for(Iterator<Entry<Vector3f, Geometry>> it = map.entrySet().iterator();it.hasNext();){
            Entry<Vector3f, Geometry> e = it.next();
            if(!tmpMap.contains(e.getKey())){
                e.getValue().removeFromParent();
                it.remove();
            }
        }
        return map;
    }

    public void updateCharacterOnMap(Commandable c, Set<Vector3f> tmpMap) {
        ColorRGBA col = c.isSelected()?ColorRGBA.White:ColorRGBA.Blue;
        Vector3f ee = new Vector3f(c.getLocalTranslation().x, 0, c.getLocalTranslation().z);
        final Geometry get = allyMap.get(ee);
        if(get!=null && !col.equals(get.getUserData("colour"))){
            get.removeFromParent();
            allyMap.remove(ee);
        }
        if(!allyMap.containsKey(ee)){                        
            Geometry g = getGeometry(circle.apply(5), app.getAssetManager(), col);
            g.setUserData("colour", col);
            g.setLocalTranslation(ee);
            allyMap.put(ee, g);
            allies.attachChild(g);
        }
        tmpMap.add(ee);
    }
    
}
