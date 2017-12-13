package com.jme3.lostVictories;

import com.fasterxml.jackson.databind.JsonNode;
import com.jme3.ai.navmesh.CustomNavMeshBuilder;
import com.jme3.ai.navmesh.NavMesh;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.effects.ParticleEmitterFactory;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.minimap.MinimapNode;
import com.jme3.lostVictories.network.NetworkClient;
import com.jme3.lostVictories.network.ResponseFromServerMessageHandler;
import com.jme3.lostVictories.network.ServerResponse;
import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.util.SkyFactory;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;


public class LostVictory extends SimpleApplication implements ActionListener {
    
    public AvatarCharacterNode avatar;

    private BulletAppState bulletAppState;

    private TerrainQuad terrain;
    ScheduledExecutorService worldRunnerService;
    private final UUID avatarUUID;

    public static void main(String[] args) throws IOException, DecoderException {
        Object[] options = { "OK", "CANCEL" };
            
        String playerID, serverIP, gameVersion;
        int port = 5055;

//        args = new String[]{"lostvic://lostVictoriesLauncher/game=eyJpZCI6InNhYXJfb2ZmZW5zaXZlIiwibmFtZSI6IlNhYXIgT2ZmZW5zaXZlIiwiaG9zdCI6ImNvbm5lY3QubG9zdHZpY3Rvcmllcy5jb20iLCJwb3J0IjoiNTA1NSIsInN0YXJ0RGF0ZSI6MTUwMzQyOTc3NjczMywiam9pbmVkIjp0cnVlLCJhdmF0YXJJRCI6Ijg0NGZkOTNkLWU2NWEtNDM4YS04MmM1LWRhYjlhZDU4ZTg1NCIsImdhbWVWZXJzaW9uIjoicHJlX2FscGhhIiwiZ2FtZVN0YXR1cyI6ImluUHJvZ3Jlc3MiLCJ2aWN0b3IiOm51bGwsImVuZERhdGUiOm51bGwsImNvdW50cnkiOiJBTUVSSUNBTiJ9"};
        if(args.length>0){    
//            JOptionPane.showOptionDialog(null, args[0], "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);         
//            if(args.length>1){
//                JOptionPane.showOptionDialog(null, args[1], "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
//            }
            
            String s = args.length>1?args[1].substring("lostVictoriesLauncher/game=".length()):args[0].substring("lostvic://lostVictoriesLauncher/game=".length());
            
            System.out.println("s:"+s);
            
            Base64 decoder = new Base64();
            byte[] decodedBytes = decoder.decode(s);
            String ss = new String(decodedBytes);
            System.out.println("ss:"+ss);
            
            JsonNode gameJson = MAPPER.readTree(ss);
            playerID = gameJson.get("avatarID").asText();
            serverIP = gameJson.get("host").asText();
            port = Integer.parseInt(gameJson.get("port").asText());
            gameVersion = gameJson.get("gameVersion").asText();
        }else{
            Map<String, String> env = System.getenv();
            playerID = env.get("player_id");
            serverIP = env.get("server_ip");
            if(playerID==null){
               playerID = "2fbe421f-f701-49c9-a0d4-abb0fa904204"; //german
//               playerID = "d993932f-a185-4a6f-8d86-4ef6e2c5ff95"; //american 1
               //playerID = "844fd93d-e65a-438a-82c5-dab9ad58e854"; //american 2
            }
            if(serverIP == null){
                serverIP = "localhost";
//                serverIP = "connect.lostvictories.com";
            }
            gameVersion = "pre_alpha";
        
        }
        
        LostVictory app = new LostVictory(UUID.fromString(playerID), serverIP, port, gameVersion);
        app.start();
    }

    private NavMesh navMesh;
    private WorldMap worldMap;
    private WorldRunner worldRunner;
    private boolean gameOver;
    private CharacterLoader characterLoader;
    //private IngameText ingameText = new IngameText();
    private Node sceneGraph;
    public RealTimeStrategyAppState chaseCameraAppState;
    private FirstPersonShooterAppState firstPersonShooterAppState;
    private HeadsUpDisplayAppState headsUpDisplayAppState;
    NetworkClientAppState networkClientAppState;
    private final String ipAddress;
    private final int port;
    private final String gameVersion;
 
    public LostVictory(UUID avatorID, String ipAddress, int port, String gameVersion) {
        super( new StatsAppState(), new DebugKeysAppState() );
        this.avatarUUID = avatorID;
        System.out.println("starting client:"+avatorID);
        this.ipAddress = ipAddress;
        this.port = port;
        this.gameVersion = gameVersion;      
    }
   
    @Override
    public void simpleInitApp() {
        if(!"pre_alpha".equals(gameVersion)){
            throw new RuntimeException("Sorry your game is out of date please install version:"+gameVersion);
        }
        
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);
//        bulletAppState.setDebugEnabled(true);
        bulletAppState.getPhysicsSpace().addCollisionGroupListener(new VehicleCOllisionListener(), PhysicsCollisionObject.COLLISION_GROUP_01);

        Node traversableSurfaces = new Node();
        this.worldMap = WorldMap.instance(traversableSurfaces);
        sceneGraph = TerrainLoader.instance().loadTerrain(assetManager, bulletAppState, cam, "Scenes/testScene4.j3o", traversableSurfaces, worldMap);
        terrain = (TerrainQuad) sceneGraph.getChild("terrain-testScene4");

        DirectionalLight sun1 = new DirectionalLight();
        sun1.setColor(ColorRGBA.White);
        sun1.setDirection(new Vector3f(-.7f,-.7f,-.7f).normalizeLocal());
        rootNode.addLight(sun1);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(.6f));
        rootNode.addLight(al);

        navMesh = CustomNavMeshBuilder.buildMesh((Geometry)sceneGraph.getChild("NavMesh"));
        StructureLoader structureLoader = StructureLoader.instance(rootNode, assetManager, bulletAppState, this, terrain, sceneGraph);

        MinimapNode minimapNode = new MinimapNode("minimap", this);
        chaseCameraAppState = new RealTimeStrategyAppState(minimapNode);
        firstPersonShooterAppState = new FirstPersonShooterAppState(minimapNode);
        headsUpDisplayAppState = new HeadsUpDisplayAppState(this, chaseCameraAppState);

        ParticleEmitterFactory pf = ParticleEmitterFactory.instance(assetManager);
        ParticleManager particleManager = new ParticleManager(sceneGraph, assetManager, renderManager);
        characterLoader = CharacterLoader.instance(sceneGraph, assetManager, bulletAppState, navMesh, pf, headsUpDisplayAppState, particleManager, this, worldMap);
        ResponseFromServerMessageHandler serverSync = new ResponseFromServerMessageHandler(this, characterLoader, structureLoader, avatarUUID, particleManager, headsUpDisplayAppState);
        networkClientAppState = NetworkClientAppState.init(this, new NetworkClient(ipAddress, port, avatarUUID, serverSync), serverSync);
        
        try {
            ServerResponse checkout = networkClientAppState.checkoutSceenSynchronous(avatarUUID);
            structureLoader.loadStuctures(worldMap, checkout);
            avatar = characterLoader.loadCharacters(checkout, avatarUUID);
            sceneGraph.addControl(new SimpleGrassControl(assetManager, bulletAppState, (Node) sceneGraph, terrain, checkout.getAllTrees(), "Resources/Textures/Grass/grass.png"));
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        initKeys();

        setPauseOnLostFocus(false);
        rootNode.attachChild(sceneGraph);

        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/FullskiesBlueClear03.dds", false));

        worldRunnerService = Executors.newSingleThreadScheduledExecutor();

        worldRunnerService.scheduleAtFixedRate(this.worldMap, 0, 2, TimeUnit.SECONDS);


        worldRunner = WorldRunner.instance(this.worldMap);
        worldRunnerService.scheduleAtFixedRate(worldRunner, 1, 2, TimeUnit.SECONDS);
        
        getStateManager().attach(chaseCameraAppState);
        getStateManager().attach(headsUpDisplayAppState);
        getStateManager().attach(networkClientAppState);
//        getStateManager().attach(new DetailedProfilerState());
//        setDisplayFps(false);
//        setDisplayStatView(false);
    }
    
    

    @Override
    public void simpleUpdate(float tpf) {     
        if(gameOver){
            
            return;
        }

        for(GameCharacterNode c: worldMap.getAllCharacters()){
          if(!c.isDead()){
              c.simpleUpate(tpf, worldMap, rootNode);
          }else{
              if(c == avatar && getStateManager().hasState(firstPersonShooterAppState)){
                  getStateManager().detach(firstPersonShooterAppState);
                  getStateManager().attach(chaseCameraAppState);
              }
          }
        }
              
        for(GameHouseNode s: worldMap.getAllHouses()){
            s.simpleUpate(tpf, worldMap);
        }

        if(worldRunner.hasCapturedAllStructures(avatar.getCountry()) || worldRunner.hasTheOnlyVictoryPoints()){
            headsUpDisplayAppState.printVictoryText();
            worldRunnerService.shutdownNow();
            gameOver =true;
        }
        if(worldRunner.hasLostAllStructures(avatar.getCountry()) || worldRunner.hasNoVictoryPoints()){
            headsUpDisplayAppState.printDefeatedText();
            worldRunnerService.shutdownNow();
            gameOver =true;
            System.out.println("1:"+worldRunner.hasLostAllStructures(avatar.getCountry()));
            System.out.println("2:"+worldRunner.hasNoVictoryPoints());
        }

    }

    @Override
    public void destroy() {
        worldRunnerService.shutdownNow();
        super.destroy(); //To change body of generated methods, choose Tools | Templates.
    }

    /** Custom Keybinding: Map named actions to inputs. */
    private void initKeys() {
        inputManager.addMapping("switchMode", new KeyTrigger(KeyInput.KEY_TAB));
        inputManager.addListener(this, "switchMode");  
    }
    
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("switchMode") && isPressed) {
            if(getStateManager().hasState(chaseCameraAppState)){
                getStateManager().detach(chaseCameraAppState);
                getStateManager().attach(firstPersonShooterAppState);
            }else{
                getStateManager().detach(firstPersonShooterAppState);
                getStateManager().attach(chaseCameraAppState);
            }
        }
    }

    AvatarCharacterNode getAvatar() {
        return avatar;
    }
    
    public void setAvatar(AvatarCharacterNode newAvatar){
        this.avatar = newAvatar;
    }

    AppSettings getSettings(){
        return settings;
    }
    
    Node getTerrain(){
        return terrain;
    }
    
        
}