package com.jme3.lostVictories;

import com.jme3.scene.Node;

import java.awt.*;
import java.util.List;

/**
 * 
 * @author matheusdev
 *
 */
public class Quadtree<E extends Node> {
   
   private final Rectangle.Float bounds;
   private E[] elements;
   
   private Quadtree<E> topLeft;
   private Quadtree<E> topRight;
   private Quadtree<E> botLeft;
   private Quadtree<E> botRight;
   
   public Quadtree(float size, int elemPerQuad) {
      this(0, 0, size, elemPerQuad);
   }
   
   @SuppressWarnings("unchecked")
   public Quadtree(float x, float y, float size, int elemPerQuad) {
      bounds = new Rectangle.Float(x, y, size, size);
      elements = (E[])(new Node[elemPerQuad]);
   }
   
   protected boolean set(E e) {
      for (int i = 0; i < maxElem(); i++) {
         if (elements[i] == null) {
            elements[i] = e;
            return true;
         }
      }
      return false;
   }
   
   public int maxElem() {
      return elements.length;
   }
   
   public boolean insert(E e) {
      if (!bounds.contains(e.getLocalTranslation().x, e.getLocalTranslation().z)) {
         return false;
      }
      if (set(e)) {
         return true;
      } else {
         subdivide();
         if (topRight.insert(e)) return true;
         if (topLeft.insert(e)) return true;
         if (botRight.insert(e)) return true;
         if (botLeft.insert(e)) return true;
         
         throw new RuntimeException();
      }
   }
   
   public void query(Rectangle.Float range, List<E> results) {
      if (!bounds.intersects(range)) {
         return;
      }
      for (int i = 0; i < maxElem(); i++) {
         if (elements[i] != null) {
            if (range.contains(elements[i].getLocalTranslation().x, elements[i].getLocalTranslation().z)) {
               results.add(elements[i]);
            }
         }
      }
      if (!hasChildren()) {
         return;
      }
      topLeft.query(range, results);
      topRight.query(range, results);
      botLeft.query(range, results);
      botRight.query(range, results);
   }
   
   public boolean hasChildren() {
      return topLeft != null;
   }
   
   /**
    * <p>
    * Subdivides this Quadtree into 4 other quadtrees.
    * </p>
    * <p>
    * This is usually used, when this Quadtree already has an
    * Element.
    * </p>
    * @return whether this Quadtree was subdivided, or didn't subdivide,
    * because it was already subdivided.
    */
   protected boolean subdivide() {
      if (hasChildren()) {
         return false;
      }
      float hs = bounds.width/2;
      topLeft  = new Quadtree<E>(bounds.x, bounds.y, hs, maxElem());
      topRight = new Quadtree<E>(bounds.x+hs, bounds.y, hs, maxElem());
      botLeft  = new Quadtree<E>(bounds.x, bounds.y+hs, hs, maxElem());
      botRight = new Quadtree<E>(bounds.x+hs, bounds.y+hs, hs, maxElem());
      return true;
   }
}