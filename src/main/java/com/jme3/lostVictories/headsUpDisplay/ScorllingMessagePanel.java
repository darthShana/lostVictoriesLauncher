/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.headsUpDisplay;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableListIterator;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.layout.align.HorizontalAlign;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author dharshanar
 */
public class ScorllingMessagePanel {
    private final Iterator<String> instructions;
    private final int maxWidth;
    private final Nifty nifty;
    private final Element objectivePanel;
    private final List<Element> allElements = new ArrayList<Element>();
    private Element currentElement;
    private UnmodifiableListIterator<Character> currentLine;
    private StringBuilder currentRenderedLine;
    protected Character lastCharacter;
    private final String font;
    private final Long timeout;
    private boolean finishedRendering;
    private Long finishedRenderingTime;

    public ScorllingMessagePanel(List<String> instructions, int maxWidth, Nifty nifty, Element objectivePanel, String font, Long timeout) {
        this.instructions = instructions.iterator();
        this.maxWidth = maxWidth;
        this.nifty = nifty;
        this.objectivePanel = objectivePanel;
        this.font = font;
        this.timeout = timeout;
    }
    
    
    public boolean update(){
        if(currentElement==null && instructions.hasNext()){
            final TextBuilder textBuilder = new TextBuilder();
            textBuilder.text("");
            textBuilder.font(font);
            textBuilder.alignLeft();
            currentElement = textBuilder.build(nifty, nifty.getCurrentScreen(), objectivePanel);
            allElements.add(currentElement);
            ImmutableList<Character> chars = Lists.charactersOf(instructions.next());
            currentLine = chars.listIterator();
            currentRenderedLine = new StringBuilder();
        }else if(currentElement!=null && currentRenderedLine.length()>maxWidth && lastCharacter!=null && lastCharacter==' '){
            final TextBuilder textBuilder = new TextBuilder();
            textBuilder.text("");
            textBuilder.font(font);
            textBuilder.alignLeft();
            currentElement = textBuilder.build(nifty, nifty.getCurrentScreen(), objectivePanel);
            allElements.add(currentElement);
            currentRenderedLine = new StringBuilder();
        }
        
    
        if(currentLine.hasNext()){
            lastCharacter = currentLine.next();
            currentRenderedLine.append(lastCharacter);
            final TextRenderer renderer = currentElement.getRenderer(TextRenderer.class);
            renderer.setText(currentRenderedLine.toString());
            renderer.setTextHAlign(HorizontalAlign.left);
            
            return false;
        }else if(instructions.hasNext()){
            currentElement =null;
            return false;
        }else if(!finishedRendering){
            finishedRendering = true;
            finishedRenderingTime = System.currentTimeMillis();
        }
        
        if(finishedRendering && timeout!=null && !allElements.isEmpty()){
            if(System.currentTimeMillis()-finishedRenderingTime>timeout){
                nifty.removeElement(nifty.getCurrentScreen(), allElements.remove(0));
                finishedRenderingTime = System.currentTimeMillis();
            }
            return false;
        }
        
        return true;
    }
    
}
