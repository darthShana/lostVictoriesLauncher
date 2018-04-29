package com.jme3.lostVictories.characters;

import com.jme3.math.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EnemyActivityReport {

    private Map<Vector3f, Long> activity = new HashMap<>();

    public Set<Vector3f> activity() {
        activity.entrySet().removeIf(e->System.currentTimeMillis()-e.getValue()>5000);
        return activity.keySet();

    }

    public void put(Vector3f point, long time) {
        activity.put(point, time);
        activity.entrySet().removeIf(e->System.currentTimeMillis()-e.getValue()>5000);
    }

    public void merge(EnemyActivityReport enemyActivity) {
        enemyActivity.activity.entrySet().forEach(e->put(e.getKey(), e.getValue()));
    }
}
