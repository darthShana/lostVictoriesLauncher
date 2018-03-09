package com.jme3.lostVictories;

import com.jme3.scene.Node;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * 
 * @author matheusdev
 *
 */
public class QuadTree<E extends Node> {
   
   private final Rectangle.Float bounds;
   private E[] elements;
   private Collection<QuadTree<E>> children = new ConcurrentLinkedQueue<>();


    public QuadTree(float x, float y, float size, int elemPerQuad) {
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
         return children.stream().filter(c->c.insert(e)).findFirst().isPresent();

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

      children.stream().forEach(c->c.query(range, results));
   }

    public void remove(E c) {
        Rectangle2D.Float range = new Rectangle.Float(c.getLocalTranslation().x, c.getLocalTranslation().z, 1, 1);
        remove(c, range);
    }

    private void remove(E c, Rectangle.Float range) {
        if (!bounds.intersects(range)) {
            return;
        }

        Optional<E> any = Arrays.stream(elements)
                .filter(e -> e!=null && range.contains(e.getLocalTranslation().x, e.getLocalTranslation().z))
                .filter(e -> e.equals(c)).findAny();

        if(any.isPresent()){
            for(int i=0;i<elements.length;i++){
                if(elements[i]!=null && elements[i].equals(any.get())){
                    elements[i] = null;
                }
            }
        }

        children.stream().forEach(child->child.remove(c, range));
    }

   
   /**
    * <p>
    * Subdivides this QuadTree into 4 other quadtrees.
    * </p>
    * <p>
    * This is usually used, when this QuadTree already has an
    * Element.
    * </p>
    * @return whether this QuadTree was subdivided, or didn't subdivide,
    * because it was already subdivided.
    */
   protected boolean subdivide() {
      if (!children.isEmpty()) {
         return false;
      }
      float hs = bounds.width/2;
      children.add(new QuadTree<E>(bounds.x, bounds.y, hs, maxElem()));
      children.add(new QuadTree<E>(bounds.x+hs, bounds.y, hs, maxElem()));
      children.add(new QuadTree<E>(bounds.x, bounds.y+hs, hs, maxElem()));
      children.add(new QuadTree<E>(bounds.x+hs, bounds.y+hs, hs, maxElem()));
      return true;
   }

    public void update(E c) {
        Rectangle2D.Float range = new Rectangle.Float(c.getLocalTranslation().x, c.getLocalTranslation().z, 1, 1);
        ArrayList<E> results = new ArrayList<>();
        query(range, results);
        if(!results.stream().filter(e->e.equals(c)).findAny().isPresent()){
            remove(c);
            insert(c);

        }
    }




}