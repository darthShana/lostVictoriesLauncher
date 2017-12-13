/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.actions.StopAction;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.Lieutenant;
import com.jme3.lostVictories.characters.Rank;
import com.jme3.lostVictories.network.messages.SquadType;
import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 *
 * @author dharshanar
 */
enum SecureSectorState {
    
    WAIT_FOR_REENFORCEMENTS{
    
        AIAction<AICharacterNode> planObjective(Lieutenant c, WorldMap worldMap, Node rootNode, SecureSector objective){
            return null;
        }

        SecureSectorState transition(Lieutenant c, WorldMap worldMap, SecureSector objective){
            if(!c.getEnemyActivity().activity().isEmpty()){
                return ATTACK_TARGET;
            }
            if(c.getCurrentStrength()>=objective.deploymentStrength){
                return DEPLOY_TO_SECTOR;
            }
            return WAIT_FOR_REENFORCEMENTS;
        }
    },
    
    RETREAT {
        AIAction<AICharacterNode> planObjective(Lieutenant c, WorldMap worldMap, Node rootNode, SecureSector objective){
            for(Commandable unit:c.getCharactersUnderCommand()){
                if(!objective.issuedOrders.containsKey(unit.getIdentity()) && unit.getRank()== Rank.CADET_CORPORAL){
                    TransportSquad t = new TransportSquad(objective.homeBase);
                    objective.issuedOrders.put(unit.getIdentity(), t);
                    unit.addObjective(t);
                }
            }
            if(!objective.issuedOrders.containsKey(c.getIdentity())){
                TravelObjective t = new TravelObjective(c, objective.homeBase, null);
                objective.issuedOrders.put(c.getIdentity(), t);
            }
            return objective.issuedOrders.get(c.getIdentity()).planObjective(c, worldMap);
        }

        SecureSectorState transition(Lieutenant c, WorldMap worldMap, SecureSector objective){
            final Objective get = objective.issuedOrders.get(c.getIdentity());
            if(get==null || get.isComplete){
                return WAIT_FOR_REENFORCEMENTS;
            }
            return RETREAT;
        }
    },
    
    DEPLOY_TO_SECTOR{
        @Override
        AIAction<AICharacterNode> planObjective(Lieutenant c, WorldMap worldMap, Node rootNode, SecureSector objective) {
            for(Commandable unit:c.getCharactersUnderCommand()){
                if(!objective.issuedOrders.containsKey(unit.getIdentity()) && unit.getRank()== Rank.CADET_CORPORAL){
                    TransportSquad deployToSector = new TransportSquad(objective.centre);
                    unit.addObjective(deployToSector);
                    objective.issuedOrders.put(unit.getIdentity(), deployToSector);
                }
            }
            if(!objective.issuedOrders.containsKey(c.getIdentity())){
                TravelObjective t = new TravelObjective(c, objective.centre, null);
                objective.issuedOrders.put(c.getIdentity(), t);
            }
            return objective.issuedOrders.get(c.getIdentity()).planObjective(c, worldMap);
        }

        @Override
        SecureSectorState transition(Lieutenant c, WorldMap worldMap, SecureSector objective) {
            if(c.getCurrentStrength()<=objective.minimumFightingStrenght){
                return RETREAT;
            }
            if(!c.getEnemyActivity().activity().isEmpty()){
                return ATTACK_TARGET;
            }
            if(!objective.issuedOrders.containsKey(c.getIdentity()) || objective.issuedOrders.get(c.getIdentity()).isComplete){
                return CAPTURE_HOUSES;
            }
            if(objective.boundary.contains(new Point2D.Float(c.getLocation().x, c.getLocation().z))){
                return CAPTURE_HOUSES;
            }

            return DEPLOY_TO_SECTOR;
        }

    },
    CAPTURE_HOUSES{
        @Override
        AIAction<AICharacterNode> planObjective(Lieutenant c, WorldMap worldMap, Node rootNode, final SecureSector objective) {
            List<GameHouseNode> sortedHouses = new ArrayList<GameHouseNode>(objective.houses);
            Collections.sort(sortedHouses, new Comparator<GameHouseNode>(){

                public int compare(GameHouseNode o1, GameHouseNode o2) {
                    if(o1.getLocalTranslation().distance(objective.centre)>o2.getLocalTranslation().distance(objective.centre)){
                        return 1;
                    }else{
                        return -1;
                    }
                }
            });
            for(Iterator<Map.Entry<UUID, Objective>> it = objective.issuedOrders.entrySet().iterator();it.hasNext();){
                if(it.next().getValue().isComplete){
                    it.remove();
                }
            }

            for(GameHouseNode house: sortedHouses){
                if(!house.isOwnedBy(c.getCountry()) && !objective.attemptedHouses.contains(house.getId())){
                    for(Commandable unit:c.getCharactersUnderCommand()){
                        if(!objective.issuedOrders.containsKey(unit.getIdentity())){
                            final CaptureStructure captureStructure = new CaptureStructure(unit, house);
                            objective.issuedOrders.put(unit.getIdentity(), captureStructure);
                            objective.attemptedHouses.add(house.getId());
                            unit.addObjective(captureStructure);
                        }
                    }
                }
            }
            return null;
        }

        @Override
        SecureSectorState transition(Lieutenant c, WorldMap worldMap, SecureSector objective) {
            if(c.getCurrentStrength()<=objective.minimumFightingStrenght){
                return RETREAT;
            }
            if(!c.getEnemyActivity().activity().isEmpty()){
                return ATTACK_TARGET;
            }
            for(Objective o:objective.issuedOrders.values()){
                if(!o.isComplete){
                    return CAPTURE_HOUSES;
                }
            }
            return DEFEND_SECTOR;
        }


    },
    DEFEND_SECTOR {
        @Override
        AIAction<AICharacterNode> planObjective(Lieutenant c, WorldMap worldMap, Node rootNode, SecureSector objective) {
            Rectangle2D.Float sb = calculateSectorBoundry(objective.houses);
            final Iterator<Commandable> iterator = c.getCharactersUnderCommand().iterator();
            if(iterator.hasNext()){
                Commandable c1 = iterator.next();
                if(!objective.issuedOrders.containsKey(c1.getIdentity())){
                    final Vector3f mc = new Vector3f(sb.x, 0, sb.y);
                    CoverFront cf = new CoverFront(mc, mc.add(mc.subtract(objective.centre)), rootNode);
                    objective.issuedOrders.put(c1.getIdentity(), cf);
                    c1.addObjective(cf);
                }
            }
            if(iterator.hasNext()){
                Commandable c1 = iterator.next();
                if(!objective.issuedOrders.containsKey(c1.getIdentity())){
                    final Vector3f mc = new Vector3f(sb.x+sb.width, 0, sb.y);
                    CoverFront cf = new CoverFront(mc, mc.add(mc.subtract(objective.centre)), rootNode);
                    objective.issuedOrders.put(c1.getIdentity(), cf);
                    c1.addObjective(cf);
                }
            }
            if(iterator.hasNext()){
                Commandable c1 = iterator.next();
                if(!objective.issuedOrders.containsKey(c1.getIdentity())){
                    final Vector3f mc = new Vector3f(sb.x, 0, sb.y+sb.height);
                    CoverFront cf = new CoverFront(mc, mc.add(mc.subtract(objective.centre)), rootNode);
                    objective.issuedOrders.put(c1.getIdentity(), cf);
                    c1.addObjective(cf);
                }
            }
            if(iterator.hasNext()){
                Commandable c1 = iterator.next();
                if(!objective.issuedOrders.containsKey(c1.getIdentity())){
                    final Vector3f mc = new Vector3f(sb.x+sb.width, 0, sb.y+sb.height);
                    CoverFront cf = new CoverFront(mc, mc.add(mc.subtract(objective.centre)), rootNode);
                    objective.issuedOrders.put(c1.getIdentity(), cf);
                    c1.addObjective(cf);
                }
            }
            return null;
        }

        @Override
        SecureSectorState transition(Lieutenant c, WorldMap worldMap, SecureSector objective) {
            if(c.getCurrentStrength()<=objective.minimumFightingStrenght){
                return RETREAT;
            }
            if(!c.getEnemyActivity().activity().isEmpty()){
                return ATTACK_TARGET;
            }
            return DEFEND_SECTOR;
        }   

        private Rectangle2D.Float calculateSectorBoundry(Set<GameHouseNode> houses) {
            Float minX = null;
            Float maxX = null;
            Float minZ = null;
            Float maxZ = null;
            for(GameHouseNode house:houses){
                if(minX==null || house.getLocalTranslation().x<minX){
                    minX = house.getLocalTranslation().x;
                }
                if(minZ==null || house.getLocalTranslation().z<minZ){
                    minZ = house.getLocalTranslation().z;
                }
                if(maxX==null || house.getLocalTranslation().x>maxX){
                    maxX = house.getLocalTranslation().x;
                }
                if(maxZ==null || house.getLocalTranslation().z>maxZ){
                    maxZ = house.getLocalTranslation().z;
                }
            }
            return new Rectangle2D.Float(minX, minZ, maxX-minX, maxZ-minZ);
        }            

    },
        
    ATTACK_TARGET{
        @Override
        AIAction<AICharacterNode> planObjective(Lieutenant c, WorldMap worldMap, Node rootNode, SecureSector objective) {
            final EnemyActivityReport enemyActivity = c.getEnemyActivity();
            for(Iterator<Map.Entry<UUID, Objective>> it = objective.issuedOrders.entrySet().iterator();it.hasNext();){
                if(it.next().getValue().isComplete){
                    it.remove();
                }
            }
            if(enemyActivity.activity().isEmpty()){
                return null;
            }
            for(Commandable unit:c.getCharactersUnderCommand()){
                if(unit.getRank()==Rank.CADET_CORPORAL && !objective.issuedOrders.containsKey(unit.getIdentity())){
                    Objective ab;
                    if(unit.getSquadType(SquadType.RIFLE_TEAM, true)==SquadType.MORTAR_TEAM){
                        ab = new BombardTargets(enemyActivity.activity(), rootNode);
                    }else{
                        System.out.println("attacking enemies at:"+enemyActivity.activity());
                        ab = new AttackBoggies(enemyActivity.activity(), rootNode);
                    }
                    objective.issuedOrders.put(unit.getIdentity(), ab);
                    unit.addObjective(ab);
                }
            }

            return new StopAction();
        }

        @Override
        SecureSectorState transition(Lieutenant c, WorldMap worldMap, SecureSector objective) {
            if(c.getCurrentStrength()<=objective.minimumFightingStrenght){
                return RETREAT;
            }
            if(c.getEnemyActivity().activity().isEmpty()) {
                System.out.println("clear of enemy acitivty so going back to:"+objective.lastState);
                return objective.lastState;
            }
            return  ATTACK_TARGET;
        }



    };

    abstract AIAction<AICharacterNode> planObjective(Lieutenant c, WorldMap worldMap, Node rootNode, SecureSector objective);

    abstract SecureSectorState transition(Lieutenant c, WorldMap worldMap, SecureSector objective);
}
