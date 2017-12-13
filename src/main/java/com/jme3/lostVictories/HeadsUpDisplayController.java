/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.app.Application;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.CommandingOfficer;
import com.jme3.lostVictories.characters.Rank;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.ImageBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author dharshanar
 */
public class HeadsUpDisplayController implements ScreenController {
    private final LostVictory app;
    private final RealTimeStrategyAppState chaseCameraAppState;
    private Map<UUID, Boolean> expandedUnits = new HashMap<>();
    private Map<UUID, Element> addedRows = new HashMap<>();
    private Map<UUID, Element> iconMap = new HashMap<>();
    private Nifty nifty;

    HeadsUpDisplayController(Application app, RealTimeStrategyAppState chaseCameraAppState) {
        this.app = (LostVictory) app;
        this.chaseCameraAppState = chaseCameraAppState;
    }

    
    public void bind(Nifty nifty, Screen screen) {
        nifty.setIgnoreKeyboardEvents(true);
    }

    public void onStartScreen() {
        
    }

    public void onEndScreen() {
        
    }

    public void clearSelected() {
        toggleIcon(chaseCameraAppState.getSelectedCharacter(), false);
    }

    public void selectCharacterInCommand(String identity){
        final Commandable characterUnderCommand = app.getAvatar().getCharacterUnderCommand(identity);
        if(characterUnderCommand!=null){
            toggleIcon(chaseCameraAppState.getSelectedCharacter(), false);
            chaseCameraAppState.selectCharacter(characterUnderCommand);
            toggleIcon(characterUnderCommand, characterUnderCommand.isSelected());
        }
    }

    public void toggleIcon(Commandable characterUnderCommand, boolean selected) {
        Element element = iconMap.get(characterUnderCommand.getIdentity());
        if(element!=null) {
            element.getChildren().forEach(e -> nifty.removeElement(nifty.getCurrentScreen(), e));
            addImageAndText(characterUnderCommand, nifty, expandedUnits.getOrDefault(characterUnderCommand.getIdentity(), false), element, selected);
        }
    }

    public void expandCharacter(String identity){
        final Commandable characterUnderCommand = app.getAvatar().getCharacterUnderCommand(identity);
        if(characterUnderCommand!=null){
            expandedUnits.put(characterUnderCommand.getIdentity(), true);
            Element element = addedRows.get(UUID.fromString(identity));
            if(element!=null){
                element.getChildren().forEach(e -> nifty.removeElement(nifty.getCurrentScreen(), e));
                populateRow(0, characterUnderCommand, nifty, element);
            }

        }
    }
            
    public void colapseCharacter(String identity){
        final Commandable characterUnderCommand = app.getAvatar().getCharacterUnderCommand(identity);
        if(characterUnderCommand!=null){
            expandedUnits.put(characterUnderCommand.getIdentity(), false);
            Element element = addedRows.get(UUID.fromString(identity));
            if(element!=null){
                element.getChildren().forEach(e -> nifty.removeElement(nifty.getCurrentScreen(), e));
                populateRow(0, characterUnderCommand, nifty, element);
            }

        }
    }

    void rebind(Nifty nifty, AvatarCharacterNode avatar) {
        if(nifty!=null && nifty.getCurrentScreen()!=null){
            List<Commandable> charactersUnderCommand = avatar.getCharactersUnderCommand().stream().filter(c->!c.hasBoardedVehicle()).collect(Collectors.toList());
            Map<UUID, Commandable> avatarUnits = charactersUnderCommand.stream().collect(Collectors.toMap(u->u.getIdentity(), Function.identity()));
            for(Iterator<Map.Entry<UUID, Element>> it = addedRows.entrySet().iterator();it.hasNext();){
                Map.Entry<UUID, Element> next = it.next();
                if(!avatarUnits.containsKey(next.getKey())){
                    nifty.removeElement(nifty.getCurrentScreen(), next.getValue());
                    it.remove();
                }
            }
            final Element parent = nifty.getCurrentScreen().findElementById("panel_control_units");

            charactersUnderCommand.forEach(u->{
                if(!addedRows.containsKey(u.getIdentity())) {
                    addedRows.put(u.getIdentity(), addRow(0, u, nifty, parent));
                }
            });

        }
    }

    
    public Element addRow(int i, Commandable n, Nifty nifty, Element parent){
        if(!expandedUnits.containsKey(n.getIdentity())){
            expandedUnits.put(n.getIdentity(), false);
        }
        
        PanelBuilder panelCreator = new PanelBuilder("row_"+elementId(n));
        panelCreator.childLayoutHorizontal();
        panelCreator.width("100%");
        panelCreator.height("180px");
        panelCreator.alignRight();
        Element row = panelCreator.build(nifty, nifty.getCurrentScreen(), parent);

        populateRow(i, n, nifty, row);
        return row;
    }

    private void populateRow(int i, Commandable n, Nifty nifty, Element row) {
        PanelBuilder panelCreator;
        panelCreator = new PanelBuilder();
        panelCreator.width("*");
        panelCreator.build(nifty, nifty.getCurrentScreen(), row);

        if (!expandedUnits.get(n.getIdentity()) || Rank.PRIVATE==n.getRank()) {
            panelCreator = new PanelBuilder(elementId(n) + "_minimised");
            panelCreator.childLayoutHorizontal();
            panelCreator.alignRight();
            panelCreator.width("190px");
            panelCreator.height("180px");
            Element minimised = panelCreator.build(nifty, nifty.getCurrentScreen(), row);

            if (Rank.PRIVATE!=n.getRank()) {
                panelCreator = new PanelBuilder(elementId(n) + "_open_icon");
                panelCreator.backgroundImage("Interface/arrow-left-01.png");
                panelCreator.width("50px");
                panelCreator.height("140px");
                panelCreator.interactOnClick("expandCharacter("+n.getIdentity()+")");
                panelCreator.build(nifty, nifty.getCurrentScreen(), minimised);

            } else {
                panelCreator = new PanelBuilder(elementId(n) + "_open");
                panelCreator.width("*");
                panelCreator.build(nifty, nifty.getCurrentScreen(), minimised);
            }

            iconMap.put(n.getIdentity(), addIcon(i, n, nifty, minimised, false));
            minimised.layoutElements();
        }

        if(Rank.PRIVATE!=n.getRank() && expandedUnits.get(n.getIdentity())){
            final List<Commandable> charactersUnderCommand = ((CommandingOfficer)n).getCharactersUnderCommand().stream().filter(c->!c.hasBoardedVehicle()).collect(Collectors.toList());

            panelCreator = new PanelBuilder(elementId(n)+"_maximised");
            panelCreator.childLayoutHorizontal();
            panelCreator.alignRight();
            panelCreator.width(((140*(charactersUnderCommand.size()+1))+50)+"px");
            panelCreator.height("180px");
            Element maximised = panelCreator.build(nifty, nifty.getCurrentScreen(), row);

            panelCreator = new PanelBuilder(elementId(n)+"_close_icon");
            panelCreator.backgroundImage("Interface/arrow-right-01.png");
            panelCreator.width("50px");
            panelCreator.height("140px");
            panelCreator.interactOnClick("colapseCharacter("+n.getIdentity()+")");
            panelCreator.build(nifty, nifty.getCurrentScreen(), maximised);
            iconMap.put(n.getIdentity(), addIcon(i, n, nifty, maximised, true));
            charactersUnderCommand.forEach(c -> iconMap.put(c.getIdentity(), addIcon(i, c, nifty, maximised, false)));

        }
        row.layoutElements();
    }

    protected Element addIcon(int i, Commandable n, Nifty nifty, Element parent, boolean expanded) {
        PanelBuilder panelCreator = new PanelBuilder();
        panelCreator.childLayoutVertical();
        panelCreator.width("140px");
        panelCreator.height("180px");
        panelCreator.alignCenter();
        Element cell = panelCreator.build(nifty, nifty.getCurrentScreen(), parent);

        addImageAndText(n, nifty, expanded, cell, n.isSelected());
        return cell;
    }

    private void addImageAndText(Commandable n, Nifty nifty, boolean expanded, Element cell, boolean selected) {
        ImageBuilder imageBuilder = new ImageBuilder();
        imageBuilder.filename(UnitIconMap.getIcon(n, expanded, selected));
        imageBuilder.visibleToMouse(true);
        imageBuilder.interactOnClick("selectCharacterInCommand("+n.getIdentity()+")");
        imageBuilder.build(nifty, nifty.getCurrentScreen(), cell);

        if(Rank.PRIVATE!=n.getRank() && !expanded){
            final TextBuilder textBuilder = new TextBuilder();
            textBuilder.text(((CommandingOfficer)n).getCharactersUnderCommand().size()+"/"+n.getRank().getFullStrengthPopulation());
            textBuilder.font("Interface/Fonts/SansSerif.fnt");
            textBuilder.build(nifty, nifty.getCurrentScreen(), cell);
        }
    }

    public String elementId(Commandable commandable) {
        return commandable.getIdentity()+"_"+commandable.getRank()+"_"+commandable.getWeapon().getName();

    }

    public void setNifty(Nifty nifty) {
        this.nifty = nifty;
    }

}
