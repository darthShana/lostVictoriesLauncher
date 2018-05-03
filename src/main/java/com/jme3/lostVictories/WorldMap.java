/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.jme3.ai.steering.Obstacle;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.objectives.GameSector;
import com.jme3.lostVictories.objectives.reactiveObjectives.CharacterMovedActor;
import com.jme3.lostVictories.structures.*;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author dharshanar
 */
public class WorldMap {

    public static Rectangle mapBounds = new Rectangle(-512, -512, 1024, 1024);
    
    public static final int AUTO_ATTACK_RANGE = 15;
    public static final int LINE_OF_SIGHT = 125;
    public static final float CHARACTER_SIZE = .5f;
    public static final int BLAST_RANGE = 2;

    private static WorldMap instance;
    private Node traversableSurfaces;
    private final ActorRef characterLocator;

    static WorldMap instance(Node traversableSurfaces, ActorSystem actorSystem) {
        if(instance==null){
            instance = new WorldMap(traversableSurfaces, actorSystem);
        }
        return instance;
    }
    

    public static WorldMap get(){
        return instance;
    }
    
    private volatile BiDirectionalMap<GameCharacterNode> characters = new BiDirectionalMap<GameCharacterNode>(mapBounds);    
    private volatile BiDirectionalMap<GameObjectNode> objects = new BiDirectionalMap<GameObjectNode>(mapBounds);

    private final Set<GameStructureNode> structures = new HashSet<>();
    private final Map<UUID, GameHouseNode> houses = new HashMap<>();
    private final Map<UUID, UnclaimedEquipmentNode> unclaimedEquipment = new HashMap<>();
    Set<GameSector> gameSectors;

    private WorldMap(Node traversableSurfaces, ActorSystem actorSystem) {
        this.traversableSurfaces = traversableSurfaces;
        characterLocator = actorSystem.actorOf(CharacterMovedActor.props(this), "characterLocator");
    }

    public void addHouse(GameHouseNode addHouse) {
        houses.put(addHouse.getId(), addHouse);
        structures.add(addHouse);
    }

    public void addStructure(GameStructureNode structureNode){
        structures.add(structureNode);
    }

    public void addObject(GameObjectNode objectNode){
        final Vector3f localTranslation = objectNode.getLocalTranslation();
        final Rectangle.Float rectangle = new Rectangle.Float(localTranslation.x-CHARACTER_SIZE, localTranslation.z-CHARACTER_SIZE, CHARACTER_SIZE*2, CHARACTER_SIZE*2);
        objects.putCharacter(rectangle, objectNode);
    }

    public List<GameCharacterNode> getCharactersInAutoAttackRange(GameCharacterNode character) {
        return getCharactersInRange(character.getLocalTranslation(), AUTO_ATTACK_RANGE);
    }

    public List<GameCharacterNode> getCharactersInLineOfSightRange(GameCharacterNode character){
        return getCharactersInRange(character.getLocalTranslation(), LINE_OF_SIGHT);
    }
    
    public List<GameCharacterNode> getCharactersInBlastRange(Vector3f epicentre){
        return getCharactersInRange(epicentre, BLAST_RANGE);
    }
    

    public Set<GameCharacterNode> getAllCharacters() {
        return new HashSet<>(characters.allCharacters());
    }
    
    public GameCharacterNode getCharacter(UUID id){
        Optional<? extends GameCharacterNode> any = characters.allCharacters().stream().filter(c -> c.getIdentity().equals(id)).findAny();
        return any.isPresent()?any.get():null;
    }

    public void removeCharacter(GameCharacterNode c) {
        synchronized(this){
            characters.remove(c);
        }
    }

    private List<GameCharacterNode> getCharactersInRange(final Vector3f t, int range) {
        final Rectangle.Float rectangle = new Rectangle.Float( t.x - range, t.z - range, range * 2, range * 2);
        return getCharactersInBoundingRect(rectangle);
    }

    public Set<GameBunkerNode> getAllBunkers() {
        return structures.stream().filter(s->s instanceof GameBunkerNode).map(b->(GameBunkerNode)b).collect(Collectors.toSet());
    }
    
    public Set<GameHouseNode> getAllHouses(){
        return new HashSet<>(houses.values());
    }
    
    public List<GameCharacterNode> getEnemyCharactersInDirection(GameCharacterNode c, Vector3f _v1, float range) {
        return getEnemyCharactersInDirection(c, _v1, range, false);
    }

    public List<GameCharacterNode> getEnemyCharactersInDirection(GameCharacterNode c, Vector3f _v1, float range, boolean debug) {
        Vector3f p = c.getLocalTranslation();
        Vector3f v1 = new Vector3f(_v1);
        v1.normalizeLocal();
//new Rectangle(100, -300, 5, 5)
        List<Rectangle.Float> coveringBounds = new ArrayList<Rectangle.Float>();
        Vector3f p2 = p.add(v1.mult(5));
        coveringBounds.add(new Rectangle.Float(p2.x-2.5f, p2.z-2.5f, 5, 5));
        Vector3f p3 = p.add(v1.mult(15));
        coveringBounds.add(new Rectangle.Float(p3.x-3.5f, p3.z-3.5f, 7, 7));
        Vector3f p4 = p.add(v1.mult(30));
        coveringBounds.add(new Rectangle.Float(p4.x-5.5f, p4.z-5.5f, 11, 11));
        Vector3f p5 = p.add(v1.mult(50));
        coveringBounds.add(new Rectangle.Float(p5.x-8.5f, p5.z-8.5f, 17, 17));
        Vector3f p6 = p.add(v1.mult(75));
        coveringBounds.add(new Rectangle.Float(p6.x-12.5f, p6.z-12.5f, 25, 25));
        Vector3f p7 = p.add(v1.mult(105));
        coveringBounds.add(new Rectangle.Float(p7.x-17.5f, p7.z-17.5f, 35, 35));
        Vector3f p8 = p.add(v1.mult(140));
        coveringBounds.add(new Rectangle.Float(p8.x-20.5f, p8.z-20.5f, 41, 41));

        List<GameCharacterNode> ret = new ArrayList<>();
        if(debug){
            System.out.println("calculating charaters from "+p+" in direction:"+v1);
        }
        for(Rectangle.Float r:coveringBounds){
            final List<GameCharacterNode> charactersInBoundingRect = getCharactersInBoundingRect(r);
            if(debug){
                System.out.println("r:"+r+"found:"+charactersInBoundingRect.size());
            }
            for(GameCharacterNode characterInBounds:charactersInBoundingRect){
                if(!c.isAlliedWith(characterInBounds) && characterInBounds.getLocalTranslation().distance(p)<range){
                    ret.add(characterInBounds);
                }
                
            }
            if(!ret.isEmpty()){
                return ret;
            }
        }
        
        return ret;
    }
    
    public List<Obstacle> getCharactersInDirectionClose(Vector3f p, Vector3f _v1) {
        Vector3f v1 = new Vector3f(_v1);
        v1.normalizeLocal();

        List<Rectangle.Float> coveringBounds = new ArrayList<Rectangle.Float>();
        Vector3f p2 = p.add(v1.mult(3f));
        coveringBounds.add(new Rectangle.Float(p2.x-1.5f, p2.z-1.5f, 3, 3));
        
        List<Obstacle> ret = new ArrayList<>();
        for(Rectangle.Float r:coveringBounds){
            ret.addAll(getCharactersInBoundingRect(r));
        }
        
        return ret;
    }

    List<GameCharacterNode> getCharactersInBoundingRect(Rectangle.Float rectangle) {
        List<GameCharacterNode> ret = characters.getInBounds(rectangle);
        
        for(Iterator<GameCharacterNode> it = ret.iterator();it.hasNext();){
            final Vector3f localTranslation = it.next().getLocalTranslation();
            
            if(!rectangle.contains(new Point.Float(localTranslation.x, localTranslation.z))){
                it.remove();
            }
        }
        return ret;
    }
    
    public Set<GameStructureNode> getStructuresInRange(Vector3f localTranslation, int range) {
        Set<GameStructureNode> ret = new HashSet<GameStructureNode>();
        for(GameStructureNode s:structures){
            if(s.getLocalTranslation().distance(localTranslation)<=range){
                ret.add(s);
            }
        }
        return ret;
    }
    
    public List<GameObjectNode> getCoverInRange(Vector3f localTranslation, float range){
        final Rectangle2D.Float aFloat = new Rectangle.Float(localTranslation.x-range, localTranslation.z-range, range*2, range*2);
        return objects.getInBounds(aFloat);
    }

    public void addCharacter(GameCharacterNode c) {
        synchronized(this){
            Vector3f localTranslation = c.getLocalTranslation();
            Rectangle.Float rectangle = new Rectangle.Float(localTranslation.x-CHARACTER_SIZE, localTranslation.z-CHARACTER_SIZE, CHARACTER_SIZE*2, CHARACTER_SIZE*2);
            int tries = 0;
            while(this.characters.getCharacterByBounds(rectangle)!=null && tries<20){
                localTranslation.x += 0.1f;
                localTranslation.z += 0.1f;
                rectangle = new Rectangle.Float(localTranslation.x-CHARACTER_SIZE, localTranslation.z-CHARACTER_SIZE, CHARACTER_SIZE*2, CHARACTER_SIZE*2);
                tries++;
            }
            c.setLocalTranslation(localTranslation.x, localTranslation.y, localTranslation.z);

            this.characters.putCharacter(rectangle, c);
        }
    }

    public static boolean isClose(Vector3f v1, Vector3f v2, double d) {
        if(Math.abs(v1.x - v2.x)>d){
            return false;
        }
        
        if(Math.abs(v1.z - v2.z)>d){
            return false;
        }
        return true;
    }
    
    public static boolean isClose(Vector3f v1, Vector3f v2) {
        return isClose(v1, v2, .5f);
    }

    public Iterable<GameCharacterNode> getAllOrphanedCharacters(Country country) {
        Set<GameCharacterNode> ret = new HashSet<GameCharacterNode>();
        for(GameCharacterNode n: getAllCharacters()){
            if(n.getCountry() == country && n.getCommandingOfficer() == null){
                ret.add(n);
            }
        }
        return ret;
    }

    public boolean isOutSideWorldBounds(Vector3f add) {
        return !mapBounds.contains(new Point.Float(add.x, add.z));
    }

    public GameHouseNode getHouse(UUID id) {
        return houses.get(id);
    }

    public Optional<GameBunkerNode> getDefensiveStructure(UUID defenciveStructure) {
        return structures.stream().filter(s->s instanceof GameBunkerNode).map(d->(GameBunkerNode)d).filter(b->b.getIdentity().equals(defenciveStructure)).findAny();
    }

    public boolean hasUnclaimedEquipment(UnClaimedEquipmentMessage eq) {
        return  unclaimedEquipment.containsKey(eq.getId());
    }

    void addUnclaimedEquipment(UnclaimedEquipmentNode n) {
        unclaimedEquipment.put(n.getId(), n);
    }

    public Pickable getEquipment(UUID id) {
        return unclaimedEquipment.get(id);
    }

    public Collection<UnclaimedEquipmentNode> getAllEquipment() {
        return new HashSet<UnclaimedEquipmentNode>(unclaimedEquipment.values());
    }

    public void removeEquipment(UnclaimedEquipmentNode n) {
        unclaimedEquipment.remove(n.getId());
    }
    
    public Float getTerrainHeight(Vector3f point) {
        Ray r = new Ray(point, Vector3f.UNIT_Y.negate());
        r.setLimit(100);
        CollisionResults results = new CollisionResults();
        try{
            traversableSurfaces.collideWith(r, results);
            CollisionResult result = results.getClosestCollision();
            if(result!=null){
                return result.getContactPoint().y;
            }
        }catch(Exception e){}
        return null;
    }
    
    public boolean characterInRangeAndLOStoTarget(GameCharacterNode c, Node root, Vector3f...ts) {
        for(Vector3f t: ts){
            Vector3f direction = t.subtract(c.getShootingLocation()).normalizeLocal();
            final Vector3f rayStart = c.getShootingLocation(direction);
            if(hasLOS(c, rayStart, direction, t, root)){
                return true;
            }

        }
        return false;
    }

    public boolean hasLOS(GameCharacterNode character, Vector3f origin, Vector3f direction, Vector3f target, Node root) {
        Ray ray = new Ray(origin, direction);
        ray.setLimit(character.getMaxRange());
        try{
            CollisionResults results = new CollisionResults();
            root.collideWith(ray, results);
            for(CollisionResult r:results){
                if(r.getGeometry()!=null && character.hasChild(r.getGeometry())){
                    continue;
                }
                if(Math.abs(r.getDistance() - origin.distance(target))<1){
                    return true;
                }
            }

        }catch(Throwable e){
            System.out.println("error calculating ray cast");
        }
        return false;
    }

    public Set<GameSector> getGameSectors() {
        if(gameSectors==null){
            Set<GameStructureNode> set = new HashSet<>();
            set.addAll(getAllHouses());
            set.addAll(getAllBunkers());
            gameSectors = calculateGameSector(set);
            gameSectors.forEach(d-> System.out.println("defences:"+d.getDefences().size()));
        }
        return gameSectors;
    }
    
    public static <T extends GameStructureNode> Set<GameSector> calculateGameSector(Set<T> allStructures) {
        Set<GameSector> ret = new HashSet<>();

        for(int y = WorldMap.mapBounds.y;y<=WorldMap.mapBounds.getMaxY();y=y+50){
            for(int x = WorldMap.mapBounds.x;x<=WorldMap.mapBounds.getMaxX();x=x+50){
                ret.add(new GameSector(new Rectangle(x, y, 50, 50)));
            }
        }
        
        for(GameStructureNode structure:allStructures){
            for(GameSector sector:ret){
                if(sector.containsStructure(structure)){
                    sector.add(structure);
                }
            }
        }
        
        for(Iterator<GameSector> it = ret.iterator();it.hasNext();){
            if(it.next().getStructures().isEmpty()){
                it.remove();
            }
        }
        
        Set<GameSector> merged = new HashSet<>();
        GameSector next = ret.iterator().next();
        merged.add(next);
        ret.remove(next);
		
        while(!ret.isEmpty()){
            boolean foundMerge = false;
            for(GameSector sector:merged){
                Optional<GameSector> neighbour = findNeighbouringSector(sector, ret);
                if(neighbour.isPresent()){
                    sector.merge(neighbour.get());
                    ret.remove(neighbour.get());
                    foundMerge = true;
                }
            }
            if(!foundMerge){
                next = ret.iterator().next();
                merged.add(next);
                ret.remove(next);
            }
        		
        }
        System.out.println("calculated game sectors:"+merged.size());

        return merged;
    }
    
    public static Optional<GameSector> findNeighbouringSector(GameSector sector, Set<GameSector> ret) {
        for(GameSector s:ret){
            if(sector.isJoinedTo(s)){
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    public Optional<GameSector> findSector(Vector centre) {
        return getGameSectors().stream().filter(sector->sector.containsPoint(centre.x, centre.z)).findAny();
    }


    public void characterMoved(GameCharacterNode node) {
        characterLocator.tell(new LocateCharacterRequest(node), ActorRef.noSender());
    }

    public void updateCharacterLocation(GameCharacterNode character) {
        Vector3f localTranslation = character.getLocalTranslation();
        Rectangle.Float rectangle = new Rectangle.Float(localTranslation.x-CHARACTER_SIZE, localTranslation.z-CHARACTER_SIZE, CHARACTER_SIZE*2, CHARACTER_SIZE*2);

        characters.updateCharacter(rectangle, character);
    }
}
