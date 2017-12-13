/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Rank;
import com.jme3.lostVictories.headsUpDisplay.MessageBoard;
import com.jme3.lostVictories.headsUpDisplay.ScorllingMessagePanel;
import com.jme3.lostVictories.network.messages.AchievementStatus;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * @author dharshanar
 */
public class HeadsUpDisplayAppState extends AbstractAppState implements ActionListener{

    Nifty nifty;
    int killCount = 0;
    String rank;
    private LostVictory app;
    Rank selectedRank;
    private boolean wasDead;
    private final RealTimeStrategyAppState chaseCameraAppState;
    private final HeadsUpDisplayController headsUpDisplayController;
    private int alliedPoints;
    private int enemyPoints;
    private int blueHouses;
    private int redHouses;
    private boolean updateHUDRequired = false;
    private AchievementStatus currentAchivementStatus;
    private MessageBoard gameMessages;
    private ScorllingMessagePanel achivementObjectivePanel;
    Node characterCacheNode;

    private int blueHouseChange;
    private int redHouseChange;
    private HashMap<String, Vector3f> closeUpPanelTranslations = new HashMap<>();
    private Camera closeupCamera;

    HeadsUpDisplayAppState(Application app, RealTimeStrategyAppState chaseCameraAppState) {
        this.chaseCameraAppState = chaseCameraAppState;
        this.headsUpDisplayController = new HeadsUpDisplayController(app, chaseCameraAppState);
        this.chaseCameraAppState.setCharacterSelectionListener(headsUpDisplayController);
    }
    
    
    @Override
    public void initialize(AppStateManager stateManager, final Application app) {
        super.initialize(stateManager, app);
        this.app = (LostVictory) app;
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
        app.getAssetManager(), app.getInputManager(), app.getAudioRenderer(), app.getGuiViewPort());
        nifty = niftyDisplay.getNifty();
        app.getGuiViewPort().addProcessor(niftyDisplay);
        headsUpDisplayController.setNifty(nifty);
    
        selectedRank = chaseCameraAppState.getSelectedCharacter().getRank();

        characterCacheNode = new Node("characterCache");
//        setupCloseupPanel(characterCacheNode, app);
 
        nifty.loadStyleFile("nifty-default-styles.xml");
        nifty.loadControlFile("nifty-default-controls.xml");
        
        nifty.addScreen("hud", new ScreenBuilder("hud"){{
                
             controller(headsUpDisplayController);
             layer(new LayerBuilder("foreground") {{
                childLayoutVertical();
                //backgroundColor("#0000");

                // panel added
                panel(new PanelBuilder("panel_top") {{
                    childLayoutHorizontal();
                    //backgroundColor("#0f08");
                    alignLeft();
                    height("10%");
                    width("100%");
                    // <!-- spacer -->
                    panel(new PanelBuilder("achivement_status") {{
                        childLayoutVertical();
                        //backgroundColor("#0f08");
                        height("50%");
                        width("35%");
                        alignLeft();
                        // <!-- spacer -->
                        panel(new PanelBuilder("achivement_status_image") {{
                            childLayoutHorizontal();
                            height("35%");
                            width("90%");
                            alignLeft();
                            // <!-- spacer -->
                        }});
                        panel(new PanelBuilder("achivement_status_text") {{
                            childLayoutVertical();
                            //backgroundColor("#0f08");
                            height("65%");
                            width("90%");
                            alignLeft();
                            // <!-- spacer -->
                        }});
                        
                    }});
                    panel(new PanelBuilder("game_points") {{
                        childLayoutHorizontal();
                        //backgroundColor("#0f08");
                        height("20%");
                        width("30%");
                        alignRight();
                        text(new TextBuilder("blue_houses") {{
                            text("");
                            font("Interface/Fonts/SansSerif.fnt");
                            height("100%");
                            width("5%");
                        }});
                        panel(new PanelBuilder("war_effort_distribution") {{
                            childLayoutHorizontal();
                            height("100%");
                            width("90%");
                        }});
                        text(new TextBuilder("red_houses") {{
                            text("");
                            font("Interface/Fonts/SansSerif.fnt");
                            height("100%");
                            width("5%");
                        }});
                    }});
                    
                    
                }});
                panel(new PanelBuilder("panel_centre") {{
                    childLayoutHorizontal();
                    //backgroundColor("#0f08");
                    height("70%");
                    width("100%");
                    panel(new PanelBuilder("panel_cneter_left") {{
                        //backgroundColor("#0f08");
                        height("100%");
                        width("40%");
                        alignRight();
                        // <!-- spacer -->
                    }});
                    panel(new PanelBuilder("game_messages") {{
                        childLayoutVertical();
                        //backgroundColor("#0f08");
                        height("100%");
                        width("20%");
                        alignRight();
                        // <!-- spacer -->
                        
                        text(new TextBuilder("game_status") {{
                                text("");
                                font("Interface/Fonts/SansSerif.fnt");
                                height("50%");
                                width("100%");
                        }});
                        text(new TextBuilder("respawn_estimate") {{
                                text("");
                                font("Interface/Fonts/SansSerif.fnt");
                                height("50%");
                                width("100%");
                        }});
                    }});
                    panel(new PanelBuilder("panel_control_units") {{
                        childLayoutVertical();
                        //backgroundColor("#0f08");
                        height("100%");
                        width("40%");
                        alignRight();
                        // <!-- spacer -->
                    }});
                    // <!-- spacer -->
                    
                }});
                panel(new PanelBuilder("panel_bottom") {{
                    childLayoutHorizontal();
                    //backgroundColor("#0f08");
                    height("20%");
                    width("100%");
                    // <!-- spacer -->
                    
                    panel(new PanelBuilder("panel_selected") {{
                        childLayoutVertical();
                        //backgroundColor("#0f08");
                        height("100%");
                        width("20%");
                        // <!-- spacer -->
                        
                        text(new TextBuilder("rank") {{
                                text("Rank: "+killCount);
                                font("Interface/Fonts/SansSerif.fnt");
                                height("10%");
                                width("100%");
                        }});
                        
                        text(new TextBuilder("score") {{
                                text("Kills: "+killCount);
                                font("Interface/Fonts/SansSerif.fnt");
                                height("10%");
                                width("100%");
                        }});
                    }});
                    panel(new PanelBuilder("objective_panel") {{
                        childLayoutVertical();
                        //backgroundColor("#0f08");
                        height("100%");
                        width("60%");
                        alignLeft();
                        // <!-- spacer -->
                        
                        
                    }});
                    panel(new PanelBuilder("minimap_panel") {{
                        childLayoutVertical();
                        //backgroundColor("#0f08");
                        height("100%");
                        width("20%");
                        // <!-- spacer -->
                    }});
                }});
                    
                
            }});
                
        }}.build(nifty));
        // </screen>
        nifty.gotoScreen("hud"); // start the screen
        gameMessages = new MessageBoard(nifty, nifty.getCurrentScreen().findElementById("objective_panel"));
        updateHeadsUpDisplayInternal(this.app.avatar);
        
    }
    
    public void onAction(String name, boolean isPressed, float tpf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void update(float tpf) {
        if(chaseCameraAppState.getSelectedCharacter().getKillCount()!=killCount){                               
            killCount = chaseCameraAppState.getSelectedCharacter().getKillCount();           
            Element niftyElement = nifty.getCurrentScreen().findElementById("score");
            niftyElement.getRenderer(TextRenderer.class).setText("Kills: "+killCount);
        }
        if(!chaseCameraAppState.getSelectedCharacter().getRank().getDescription().equals(rank)){                               
            rank = chaseCameraAppState.getSelectedCharacter().getRank().getDescription();
            Element niftyElement = nifty.getCurrentScreen().findElementById("rank");
            niftyElement.getRenderer(TextRenderer.class).setText("Rank: "+rank);
        }
        if(chaseCameraAppState.getSelectedCharacter() instanceof GameCharacterNode && chaseCameraAppState.getSelectedCharacter().getRank()!=selectedRank){
            selectedRank = chaseCameraAppState.getSelectedCharacter().getRank();
//            closeupOnModel(((GameCharacterNode)chaseCameraAppState.getSelectedCharacter()).getBlenderModel());
        }
        if(app.getAvatar().isDead()){
            Element niftyElement = nifty.getCurrentScreen().findElementById("respawn_estimate");
            final Integer nextReSpawnTime = WorldRunner.get().getNextReSpawnTime(app.getAvatar().getCountry());
            niftyElement.getRenderer(TextRenderer.class).setText("Re birth in: "+((nextReSpawnTime==null)?"??":nextReSpawnTime)+" seconds");
            wasDead = true;
        }else if(wasDead){
            Element niftyElement = nifty.getCurrentScreen().findElementById("respawn_estimate");
            niftyElement.getRenderer(TextRenderer.class).setText("");
            wasDead = false;
        }
        
        
        
        int ap = (WorldRunner.get().getBlueVictoryPoints()*100)/(WorldRunner.get().getBlueVictoryPoints()+WorldRunner.get().getRedVictoryPoints());

        int bh = WorldRunner.get().getBlueHouese();
        int rh = WorldRunner.get().getRedHouses();

        if(blueHouses!=bh || blueHouseChange>0) {

            if(blueHouses!=bh) {
                blueHouseChange = 100;
            }
            blueHouses = bh;
            Element blue = nifty.getCurrentScreen().findElementById("blue_houses");
            final TextRenderer renderer = blue.getRenderer(TextRenderer.class);
            if((blueHouseChange/20) % 2 == 0) {
                renderer.setText(blueHouses + "");
            }else{
                renderer.setText(" ");
            }
            blueHouseChange--;
        }
        if(redHouses!=rh || redHouseChange>0) {
            if(redHouses!=rh){
                redHouseChange = 100;
            }
            redHouses = rh;
            Element red = nifty.getCurrentScreen().findElementById("red_houses");
            final TextRenderer renderer = red.getRenderer(TextRenderer.class);
            if((redHouseChange/20) % 2 == 0){
                renderer.setText(redHouses+"");
            }else{
                renderer.setText(" ");
            }
            redHouseChange--;
        }


        
        if(alliedPoints!=ap){
            alliedPoints = ap;
            enemyPoints = 100-ap;

            for(Element e:nifty.getCurrentScreen().findElementById("war_effort_distribution").getChildren()){
                nifty.removeElement(nifty.getCurrentScreen(), e);
            }

            PanelBuilder panelBuilder = new PanelBuilder();
            panelBuilder.backgroundImage("Interface/friendly_progress.png");
            panelBuilder.width(alliedPoints+"%");
            panelBuilder.build(nifty, nifty.getCurrentScreen(), nifty.getCurrentScreen().findElementById("war_effort_distribution"));
            panelBuilder.backgroundImage("Interface/enemy_progress.png");
            panelBuilder.width(enemyPoints+"%");
            panelBuilder.build(nifty, nifty.getCurrentScreen(), nifty.getCurrentScreen().findElementById("war_effort_distribution"));

        }
        
        if(WorldRunner.get().getAchivementStatus()!=null && !WorldRunner.get().getAchivementStatus().equals(currentAchivementStatus)){
            currentAchivementStatus = WorldRunner.get().getAchivementStatus();
            final Element findElementById = nifty.getCurrentScreen().findElementById("achivement_status_text");
            for(Element e:findElementById.getChildren()){
                nifty.removeElement(nifty.getCurrentScreen(), e);
            }
            achivementObjectivePanel = new ScorllingMessagePanel(Collections.singletonList(currentAchivementStatus.getAchivementStatusText()), 55, nifty, findElementById, "Interface/Fonts/SansSerif.fnt", 10000l);
            
            for(Element e:nifty.getCurrentScreen().findElementById("achivement_status_image").getChildren()){
                nifty.removeElement(nifty.getCurrentScreen(), e);
            }
            PanelBuilder panelBuilder = new PanelBuilder();
            panelBuilder.backgroundColor("#0f08");
            panelBuilder.width(currentAchivementStatus.getAchivementPercentage()+"%");
            panelBuilder.build(nifty, nifty.getCurrentScreen(), nifty.getCurrentScreen().findElementById("achivement_status_image"));
            panelBuilder.backgroundColor("#0f01");
            panelBuilder.width(100-currentAchivementStatus.getAchivementPercentage()+"%");
            panelBuilder.build(nifty, nifty.getCurrentScreen(), nifty.getCurrentScreen().findElementById("achivement_status_image"));
        }
        
        if(updateHUDRequired){
            updateHeadsUpDisplayInternal(app.getAvatar());
            updateHUDRequired = false;
        }        
        if(achivementObjectivePanel!=null){
            if(achivementObjectivePanel.update()){
                achivementObjectivePanel = null;
            }
        }
        gameMessages.update();
        
    }

    public void addMessage(String message) {
        gameMessages.appendMessages(message);
    }

    public void updateHeadsUpDisplay() {
        updateHUDRequired = true;
    }
    
    private void updateHeadsUpDisplayInternal(AvatarCharacterNode avatar) {
        if(nifty==null){
            return;
        } 
        
        headsUpDisplayController.rebind(nifty, avatar);

    }

    void printVictoryText() {
        System.out.println("Victory!");
        Element niftyElement = nifty.getCurrentScreen().findElementById("game_status");
        niftyElement.getRenderer(TextRenderer.class).setText("Victory!");
    }

    void printDefeatedText() {
        System.out.println("You have Lost!");
        Element niftyElement = nifty.getCurrentScreen().findElementById("game_status");
        niftyElement.getRenderer(TextRenderer.class).setText("You have Lost!");
    }

    long x = -20;
    long y = -20;

    private void setupCloseupPanel(Node panel, Application app) {
        this.app.getRootNode().attachChild(panel);
        panel.setLocalTranslation(0, -200, 0);

        Set<String> models = new HashSet<>();
        models.add("Models/Characters/German_Corporal.j3o");
        models.add("Models/Characters/German_Lieutenant.j3o");
        models.add("Models/Characters/German_Colonel.j3o");
        models.add("Models/Characters/German_Private.j3o");
        models.add("Models/Characters/American_Corporal.j3o");
        models.add("Models/Characters/American_Lieutenant.j3o");
        models.add("Models/Characters/American_Colonel.j3o");
        models.add("Models/Characters/American_Private.j3o");
        models.add("Models/Vehicles/Armored_Car.j3o");
        models.add("Models/Vehicles/M3_Scout.j3o");
        models.add("Models/Vehicles/Anti_Tank_Gun.j3o");
        models.add("Models/Vehicles/german_operator.j3o");
        models.add("Models/Vehicles/american_operator.j3o");
        models.add("Models/Vehicles/PanzerIV.j3o");
        models.add("Models/Vehicles/M4A2_76.j3o");
        models.add("Models/Soldier/stickfigure3.j3o");


        models.forEach(modelPath->{
            Spatial model = app.getAssetManager().loadModel(modelPath);
            model.setLocalTranslation(x, y, 0);
            panel.attachChild(model);
            closeUpPanelTranslations.put(modelPath, new Vector3f(x, y, 0));
            x+=10;
            if(x>20){
                y+=10;
                x=-20;
            }
        });

        app.getCamera().setViewPort(0f, 1f, 0f, 1f);
        closeupCamera = app.getCamera().clone();
        closeupCamera.setViewPort(0f, 0.2f, 0f, 0.2f);
        closeupCamera.setLocation(new Vector3f(0, -200, 50));
        ViewPort view2 = app.getRenderManager().createMainView("Bottom Left", closeupCamera);
        view2.setBackgroundColor(ColorRGBA.Gray);
        view2.setClearFlags(true, true, true);
        view2.attachScene(panel);
    }

}
