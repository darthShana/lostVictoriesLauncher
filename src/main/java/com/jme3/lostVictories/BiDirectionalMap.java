/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.scene.Node;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

import static com.jme3.lostVictories.WorldMap.CHARACTER_SIZE;

/**
 *
 * @author dharshanar
 */
class BiDirectionalMap<T extends Node> {
    
    private Map<Rectangle.Float, T> boundsToCharacters = new HashMap<Rectangle.Float, T>();
    private Map<T, Rectangle.Float> charactersToBounds = new HashMap<T, Rectangle.Float>();
    private volatile Quadtree<T> characterRoot;

    public BiDirectionalMap(Rectangle mapBounds) {
        characterRoot = new Quadtree(mapBounds.x-CHARACTER_SIZE, mapBounds.y-CHARACTER_SIZE, mapBounds.width+(CHARACTER_SIZE*2), 4);
    }
    
    
    
    Collection<? extends T> allCharacters() {
        return charactersToBounds.keySet();
    }

    T getCharacterByBounds(Rectangle.Float r) {
        return boundsToCharacters.get(r);
    }

    void putCharacter(Rectangle.Float rectangle, T c) {
        if(charactersToBounds.containsKey(c)){
            final Rectangle.Float remove = charactersToBounds.remove(c);
            if(remove!=null){
                boundsToCharacters.remove(remove);
            }
        }
        boundsToCharacters.put(rectangle, c);
        charactersToBounds.put(c, rectangle);
        characterRoot.insert(c);
    }

    void remove(T c) {
        final Rectangle.Float remove = charactersToBounds.remove(c);
        if(remove!=null){
            boundsToCharacters.remove(remove);
        }
    }

    protected List<T> getInBounds(Rectangle2D.Float rectangle) {
//        trim rect to map bounds?
        final ArrayList<T> arrayList = new ArrayList<T>();
        retrieve(arrayList, rectangle);
        
        return arrayList;
    }

    void retrieve(ArrayList<T> arrayList, Rectangle2D.Float rectangle) {
        characterRoot.query(rectangle, arrayList);
    }
    
}
