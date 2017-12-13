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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author dharshanar
 */
public class MessageBoard {
    private final Element niftyPanel;
    private final Nifty nifty;
    private final LinkedList<Element> allElements = new LinkedList<Element>();
    private final Map<Element, Long> elementTime = new HashMap<Element, Long>();
    private Element currentElement;
    private UnmodifiableListIterator<Character> currentLine;
    StringBuilder currentRenderedLine;
    LinkedList<String> messageQueue = new LinkedList<>();
    TextBuilder textBuilder;
    private int updateTime;


    public MessageBoard(Nifty nifty, Element niftyPanel) {
        this.niftyPanel = niftyPanel;
        this.nifty = nifty;
    }

    public void appendMessages(String meassage) {
        if(meassage==null){
            return;
        }

        if(allElements.size()>14){
            final Element remove = allElements.remove(0);
            nifty.removeElement(nifty.getCurrentScreen(), remove);
            elementTime.remove(remove);
        }

        messageQueue.addFirst(meassage);

    }

    public void update() {
        for(Iterator<Entry<Element, Long>> it =elementTime.entrySet().iterator();it.hasNext();){
            Entry<Element, Long> e = it.next();
            if(System.currentTimeMillis()-e.getValue()>20000){
                final Element remove = allElements.remove(0);
                nifty.removeElement(nifty.getCurrentScreen(), remove);
                it.remove();
            }
        }

        if(!messageQueue.isEmpty() && currentElement==null){
            currentRenderedLine = new StringBuilder();
            final TextBuilder textBuilder = getTextBuilder();
            textBuilder.text("");
            textBuilder.font("Interface/Fonts/SansSerif.fnt");
            textBuilder.alignLeft();
            currentElement = textBuilder.build(nifty, nifty.getCurrentScreen(), niftyPanel);
            allElements.add(currentElement);
            elementTime.put(currentElement, System.currentTimeMillis());
            ImmutableList<Character> chars = Lists.charactersOf(messageQueue.removeLast());
            currentLine = chars.listIterator();
            currentRenderedLine = new StringBuilder();
        }
        if(currentLine!=null && currentLine.hasNext()){
            if(updateTime == 0) {
                currentRenderedLine.append(currentLine.next());
                final TextRenderer renderer = currentElement.getRenderer(TextRenderer.class);
                renderer.setText(currentRenderedLine.toString());
                renderer.setTextHAlign(HorizontalAlign.left);
                updateTime = 4;
            }
        }else{
            currentElement = null;
        }
        if(updateTime>0){
            updateTime--;
        }
    }

    private TextBuilder getTextBuilder() {
        if(textBuilder==null) {
            textBuilder = new TextBuilder();
        }
        return textBuilder;
    }


}
