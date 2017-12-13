/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.math.Vector3f;

import java.awt.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GameSector {
    
    private final Set<Rectangle> rects = new HashSet<Rectangle>();
    final Set<GameStructureNode> structures = new HashSet<GameStructureNode>();

    public GameSector(Rectangle rect) {
        this.rects.add(rect);
    }

    public boolean isJoinedTo(GameSector s) {
        for(Rectangle r1: rects){
            for(Rectangle r2:s.rects){
                if(new Rectangle(r1.x-1, r1.y-1, r1.width+2, r1.height+2).intersects(r2)){
                    return true;
                }
            }

        }
        return false;
    }

    public void merge(GameSector neighbour) {
        structures.addAll(neighbour.structures);
        rects.addAll(neighbour.rects);
    }

    boolean containsHouse(GameStructureNode house) {
        for(Rectangle r:rects){
            if(r.contains(house.getLocalTranslation().x, house.getLocalTranslation().z)){
                return true;
            }
        }
        return false;
    }
    
    public boolean containsPoint(float x, float z) {
        if(rects.isEmpty()){
            return false;
        }
        Rectangle union = null;
        for(Iterator<Rectangle> it = rects.iterator();it.hasNext();){
            if(union == null){
                union = it.next();
            }else{
                union.add(it.next());
            }
        }
        return (union!=null)?union.contains(x, z):false;
    }

    public void add(GameStructureNode structure) {
        structures.add(structure);
    }

    public boolean isSecured(Country country) {
        return getHouses().stream().allMatch(h->h.isOwnedBy(country));
    }

    public Vector3f location() {
        final Rectangle next = rects.iterator().next();
        return new Vector3f((float)next.getCenterX(), 0, (float)next.getCenterY());
    }

    public Set<GameHouseNode> getHouses(){
        Set<GameHouseNode> hh = new HashSet<GameHouseNode>();
        for(GameStructureNode h: structures){
            if(h instanceof GameHouseNode){
                hh.add((GameHouseNode) h);
            }
        }
        return hh;
    }

    public Iterable<GameStructureNode> structures() {
        return structures;
    }
}
