/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.structures.GameBunkerNode;
import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.math.Vector3f;

import java.awt.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameSector {

    private UUID id;
    private final Set<GameHouseNode> houses;
    private final Set<GameBunkerNode> defences;

    public GameSector(UUID id, Set<GameHouseNode> houses, Set<GameBunkerNode> defences) {
        this.id = id;
        this.houses = houses;
        this.defences = defences;
    }

    public UUID getId(){
        return id;
    }

    public void add(GameHouseNode structure) {
        houses.add(structure);
    }
    public void add(GameBunkerNode structure) {
        defences.add(structure);
    }

    public boolean isSecured(Country country) {
        return getHouses().stream().allMatch(h->h.isOwnedBy(country));
    }

    public Vector3f location() {
        Vector3f f = houses.iterator().next().getLocalTranslation();
        return new Vector3f(f.x, f.y, f.z);
    }

    public Set<GameHouseNode> getHouses(){
        return houses;
    }

    public Set<GameBunkerNode> getDefences() {
        return defences;
    }

    public Set<GameStructureNode> getStructures() {
        Set<GameStructureNode> all = new HashSet<>();
        all.addAll(houses);
        all.addAll(defences);

        return all;
    }

}
