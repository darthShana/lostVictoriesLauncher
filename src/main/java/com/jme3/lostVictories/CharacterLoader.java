/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.jme3.ai.navmesh.NavMesh;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.lostVictories.characters.*;
import com.jme3.lostVictories.characters.blenderModels.*;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.effects.ParticleEmitterFactory;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.network.CharacterUpdate;
import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.objectives.reactiveObjectives.ShootsFiredActor;
import com.jme3.lostVictories.structures.UnclaimedEquipmentNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.BatchNode;
import com.jme3.scene.Node;
import com.lostVictories.api.LostVictoryCheckout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static com.jme3.lostVictories.network.CharacterUpdateMessageAssembler.fromMessage;

/**
 *
 * @author dharshanar
 */
public class CharacterLoader {
    private static CharacterLoader instance;
    private final WorldMap worldMap;
    private final ActorRef shootsFiredListener;

    static CharacterLoader instance(Node rootNode, AssetManager assetManager, BulletAppState bulletAppState, ParticleEmitterFactory pf, HeadsUpDisplayAppState hud, ParticleManager particleManager, LostVictory app, WorldMap worldMap, ActorSystem actorSystem) {
        if(instance == null){
            instance = new CharacterLoader(rootNode, assetManager, bulletAppState, pf, hud, particleManager, app, worldMap, actorSystem);
        }
        return instance;
    }
    
    private final Node rootNode;
    private final AssetManager assetManager;
    private final BulletAppState bulletAppState;
    private final NavigationProvider pathFinder;
    private final ParticleEmitterFactory pf;
    private final HeadsUpDisplayAppState hud;
    private final ParticleManager particleManager;
    private final LostVictory app;
    

    private CharacterLoader(Node rootNode, AssetManager assetManager, BulletAppState bulletAppState, ParticleEmitterFactory pf, HeadsUpDisplayAppState hud, ParticleManager particleManager, LostVictory app, WorldMap worldMap, ActorSystem actorSystem) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.bulletAppState = bulletAppState;
        this.pathFinder = new NavigationProvider(assetManager);
        this.pf = pf;
        this.hud = hud;
        this.particleManager = particleManager;
        this.app = app;
        this.worldMap = worldMap;
        shootsFiredListener = actorSystem.actorOf(ShootsFiredActor.props(worldMap), "ShootsFiredListener");
    }
    
    public AvatarCharacterNode loadCharacters(LostVictoryCheckout checkout, UUID avatarID) throws InterruptedException {
       
        //System.out.println("recived async response:"+checkout);
        Map<UUID, GameCharacterNode> characterIdMap = new HashMap<UUID, GameCharacterNode>();
        Map<UUID, CharacterMessage> characterMessageMap = new HashMap<UUID, CharacterMessage>();
        
        HashSet<GameCharacterNode> characters = new HashSet<>();

        checkout.getCharactersList().stream().map(c->fromMessage(c)).forEach(c->{
            characterIdMap.put(c.getId(), loadCharacter(c, avatarID));
            characterMessageMap.put(c.getId(), c);
        });


        AvatarCharacterNode a1 = null;

        for(final GameCharacterNode n:characterIdMap.values()){
            if(n instanceof AvatarCharacterNode){
                a1 = (AvatarCharacterNode) n;
            }
            
            final CharacterMessage message = characterMessageMap.get(n.getIdentity());
            if(message.getCommandingOfficer()!=null){
                final CommandingOfficer co = (CommandingOfficer) characterIdMap.get(message.getCommandingOfficer());
                if(co!=null){
                    n.setCommandingOfficer(co);
                    co.addCharactersUnderCommand(new HashSet<Commandable>(){{add(n);}});
                }
            }
            characters.add(n);
        }

        for(GameCharacterNode n:characters){
            try{
                n.checkForNewObjectives(characterMessageMap.get(n.getIdentity()).getObjectives());
            }catch(Exception e){
                e.printStackTrace();
            }
        }
                        
        return a1;
    }
    
    private AvatarCharacterNode loadAvatar(UUID id, Vector3f position, Vector3f rotation, SquadType squadType, BlenderModel model, Country country, CommandingOfficer commandingOfficer, HeadsUpDisplayAppState hud, Rank rank) {
        Node player =  load(model);
        AvatarCharacterNode a = new AvatarCharacterNode(id, player, country, commandingOfficer, position, rotation, squadType, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, model, rank, hud, shootsFiredListener);
        return a;
    }
    
    private GameCharacterNode loadCharacter(UUID id, Vector3f position, Vector3f rotation, SquadType squadType, BlenderModel model, Country country, CommandingOfficer commandingOfficer, BehaviorControler behaviorControler) {
        Node player =  load(model);
        GameCharacterNode a = new Private(id, player, country, commandingOfficer, position, rotation, squadType, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, model, behaviorControler, shootsFiredListener);
        return a;
    }

    private GameVehicleNode loadHalfTrack(UUID id, Vector3f position, Vector3f rotation, SquadType squadType, Country country, CommandingOfficer commandingOfficer, BehaviorControler behaviorControler) {
        final HalfTrackBlenderModel halfTrackBlenderModel = new HalfTrackBlenderModel("Models/Vehicles/Armored_Car.j3o", 1, Weapon.mg42());
        Node vehicle =  load(halfTrackBlenderModel);
        
        
        final GameVehicleNode v = new HalfTrackNode(id, vehicle, getOperators(), country, commandingOfficer, position, rotation, squadType, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, halfTrackBlenderModel, behaviorControler, shootsFiredListener);
        if(commandingOfficer!=null){
            commandingOfficer.addCharactersUnderCommand(new HashSet<Commandable>(){{add(v);}});
        }
        return v;
    }

    private GameVehicleNode loadPanzer4(UUID id, Vector3f position, Vector3f rotation, SquadType squadType, Country country, CommandingOfficer commandingOfficer, BehaviorControler behaviorControler) {
        final Panzer4BlenderModel panzer4BlenderModel = new Panzer4BlenderModel("Models/Vehicles/PanzerIV_New.j3o", 1, Weapon.cannon());
        Node chassis =  load(panzer4BlenderModel);
        chassis.detachChildNamed("Turrent");

        Node turret = (Node) assetManager.loadModel("Models/Vehicles/PanzerIV_New.j3o");
        turret.detachChildNamed("PanzerIV");
        turret.setLocalScale(panzer4BlenderModel.getModelScale());
        final GameVehicleNode v = new MediumTankNode(id, chassis, turret, getOperators(), country, commandingOfficer, position, rotation, squadType, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, panzer4BlenderModel, behaviorControler, shootsFiredListener);
        if(commandingOfficer!=null){
            commandingOfficer.addCharactersUnderCommand(new HashSet<Commandable>(){{add(v);}});
        }
        return v;
    }

    private GameVehicleNode loadM4A2Sherman(UUID id, Vector3f position, Vector3f rotation, SquadType squadType, Country country, CommandingOfficer commandingOfficer, BehaviorControler behaviorControler) {
        final M4A2ShermanBlenderModel m4A2ShermanBlenderModel = new M4A2ShermanBlenderModel("Models/Vehicles/M4A2_76.j3o", 1, Weapon.cannon());
        Node chassis =  load(m4A2ShermanBlenderModel);
        chassis.detachChildNamed("Turrent");

        Node turret = (Node) assetManager.loadModel("Models/Vehicles/M4A2_76.j3o");
        turret.detachChildNamed("body");
        turret.detachChildNamed("chain");
        turret.setLocalScale(m4A2ShermanBlenderModel.getModelScale());
        final GameVehicleNode v = new MediumTankNode(id, chassis, turret, getOperators(), country, commandingOfficer, position, rotation, squadType, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, m4A2ShermanBlenderModel, behaviorControler, shootsFiredListener);
        if(commandingOfficer!=null){
            commandingOfficer.addCharactersUnderCommand(new HashSet<Commandable>(){{add(v);}});
        }
        return v;
    }

    private GameVehicleNode loadAmoredCar(UUID id, Vector3f position, Vector3f rotation, SquadType squadType, Country country, CommandingOfficer commandingOfficer, BehaviorControler behaviorControler) {
        final AmoredCarBlenderModel amoredCarBlenderModel = new AmoredCarBlenderModel("Models/Vehicles/M3_Scout.j3o", 1, Weapon.mg42());
        Node vehicle =  load(amoredCarBlenderModel);

        final GameVehicleNode v = new HalfTrackNode(id, vehicle, getOperators(), country, commandingOfficer, position, rotation, squadType, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, amoredCarBlenderModel, behaviorControler, shootsFiredListener);
        if(commandingOfficer!=null){
            commandingOfficer.addCharactersUnderCommand(new HashSet<Commandable>(){{add(v);}});
        }
        return v;
    }

    private GameVehicleNode loadAntiTankGun(UUID id, Vector3f position, Vector3f rotation, SquadType squadType, Country country, CommandingOfficer commandingOfficer, BehaviorControler behaviorControler) {
        final AntiTankGunModel antiTankGunModel = new AntiTankGunModel("Models/Vehicles/Anti_Tank_Gun.j3o", 1, Weapon.cannon());
        Node vehicle =  load(antiTankGunModel);
        
        final GameVehicleNode v = new AntiTankGunNode(id, vehicle, getOperators(), country, commandingOfficer, position, rotation, squadType, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, antiTankGunModel, behaviorControler, shootsFiredListener);
        if(commandingOfficer!=null){
            commandingOfficer.addCharactersUnderCommand(new HashSet<Commandable>(){{add(v);}});
        }
        return v;
    }

    private Lieutenant loadLieutenant(UUID id, Vector3f position, Vector3f rotation, SquadType squadType, BlenderModel model, Country country, CommandingOfficer commandingOfficer, BehaviorControler behaviorControler) {
        Node player =  load(model);
        Lieutenant a = new Lieutenant(id, player, country, commandingOfficer, position, rotation, squadType, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, model, behaviorControler, shootsFiredListener);
        return a;
    }
    
    private CadetCorporal loadCorporal(UUID id, Vector3f position, Vector3f rotation, SquadType squadType, BlenderModel model, Country country, CommandingOfficer commandingOfficer, BehaviorControler behaviorControler) {
        Node player =  load(model);
        CadetCorporal a = new CadetCorporal(id, player, country, commandingOfficer, position, rotation, squadType, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, model, behaviorControler, shootsFiredListener);
        return a;
    }
    
    private HeerCaptain loadHeerCaptain(UUID id, Vector3f position, Vector3f rotation, SquadType squadType, BlenderModel model, Country country, BehaviorControler behaviorControler) {
        Node player =  load(model);
        HeerCaptain a = new HeerCaptain(id, player, country, null, position, rotation, squadType, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, model, behaviorControler, shootsFiredListener);
        return a;
    }

    public void destroyCharacter(GameCharacterNode toDemote) {
        if(toDemote.getCommandingOfficer()!=null){
            toDemote.getCommandingOfficer().removeCharacterUnderCommand(toDemote);
        }

        toDemote.decomposed();
        worldMap.removeCharacter(toDemote);
    }
    
    public GameCharacterNode loadCharacter(CharacterMessage c, UUID avatarId) {
        final Country country = Country.valueOf(c.getCountry().name());
        final Weapon weapon = Weapon.get(c.getWeapon());
        
        BehaviorControler b = (c.shouldBeControledRemotely(avatarId))?new RemoteBehaviourControler(c.getId(), c):new LocalAIBehaviourControler();
        GameCharacterNode loadedCharacter;
        
        Vector3f location = c.getLocation().toVector();
        Vector3f rotation = c.getOrientation().toVector();
        SquadType squadType = c.getSquadType();
        adjustLocationToNavMap(location);

        if(CharacterType.ANTI_TANK_GUN == c.getType()){
            loadedCharacter = loadAntiTankGun(c.getId(), location, rotation, squadType, country, null, b);
        }else if(CharacterType.ARMORED_CAR == c.getType()){
            loadedCharacter = loadAmoredCar(c.getId(), location, rotation, squadType, country, null, b);
        }else if(CharacterType.HALF_TRACK == c.getType()){
            loadedCharacter = loadHalfTrack(c.getId(), location, rotation, squadType, country, null, b);
        }else if(CharacterType.PANZER4 == c.getType()){
            loadedCharacter = loadPanzer4(c.getId(), location, rotation, squadType, country, null, b);
        }else if(CharacterType.M4SHERMAN == c.getType()){
            loadedCharacter = loadM4A2Sherman(c.getId(), location, rotation, squadType, country, null, b);
        }else if(CharacterType.AVATAR == c.getType() && avatarId.equals(c.getId())){
            final Rank r = Rank.valueOf(c.getRank().name());
            loadedCharacter = loadAvatar(c.getId(), location, rotation, squadType, country.getModel(weapon, r), country, null, hud, r);
        }else if(RankMessage.COLONEL == c.getRank()){
            loadedCharacter = loadHeerCaptain(c.getId(), location, rotation, squadType, country.getModel(weapon, Rank.COLONEL), country, b);
        }else if(RankMessage.LIEUTENANT == c.getRank()){
            loadedCharacter = loadLieutenant(c.getId(), location, rotation, squadType, country.getModel(weapon, Rank.LIEUTENANT), country, null, b);
        }else if(RankMessage.CADET_CORPORAL == c.getRank()){
            loadedCharacter = loadCorporal(c.getId(), location, rotation, squadType, country.getModel(weapon, Rank.CADET_CORPORAL), country, null, b);
        }else if(RankMessage.PRIVATE == c.getRank()){
            loadedCharacter = loadCharacter(c.getId(), location, rotation, squadType, country.getModel(weapon, Rank.PRIVATE), country, null, b);
        }else{
            throw new UnsupportedOperationException("error loading character type:"+c.getType()+" rank:"+c.getRank());
        }

        loadedCharacter.initialiseKills(c.getKillCount());
        loadedCharacter.setVersion(c.getVersion());
        worldMap.addCharacter(loadedCharacter);

//        if(c.getType()==CharacterType.SOLDIER){
//            loadedCharacter.addControl(new CustomLODControl(app.getCamera(), assetManager.loadModel("Models/Soldier/stickfigure3.j3o")));
//        }
        loadedCharacter.addControl(new UnderTakerControl(app.getCamera()));
        if(c.getBoardedVehicle()==null) {
            loadedCharacter.attachToRootNode();
        }
        
        return loadedCharacter;
    }    

    public void laodUnclaimedEquipment(UnClaimedEquipmentMessage eq) {
        final Node model = load(Country.AMERICAN.getModel(Weapon.rifle(), Rank.PRIVATE));
        final Vector3f location = eq.getLocation().toVector();
        pathFinder.warpInside(null, location);
        UnclaimedEquipmentNode n = new UnclaimedEquipmentNode(eq.getId(), location, eq.getRotation().toVector(), Weapon.get(eq.getWeapon()), model, rootNode, assetManager);
        worldMap.addUnclaimedEquipment(n);
    }

    protected void adjustLocationToNavMap(Vector3f location) {
        Float f = worldMap.getTerrainHeight(new Vector3f(location.x, 200, location.z));
        if(f!=null){
            location.y = f;
        }else{
            pathFinder.warpInside(null, location);
            location.y = location.y +1;
        }
    }

    private Map<Country, Node> getOperators() {
        HashMap<Country, Node> operatorMap = new HashMap<Country, Node>();
        final BlenderModel blenderModel1 = new SoldierBlenderModel("Models/Vehicles/german_operator.j3o", 1, Weapon.mg42());
        Node gunner = load(blenderModel1);
        gunner.setLocalScale(.75f);
        gunner.setName("operator");
        operatorMap.put(Country.GERMAN, gunner);
        Node gunner2 =  load(new SoldierBlenderModel("Models/Vehicles/american_operator.j3o", 1, Weapon.mg42()));
        gunner2.setLocalScale(.25f);
        gunner2.setName("operator");
        operatorMap.put(Country.AMERICAN, gunner2);        
        return operatorMap;
    }

    protected Node load(BlenderModel model) {
            return (Node) assetManager.loadModel(model.getModelPath());
    }

        
}
