/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.math.Vector3f;

import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author dharshanar
 */
public class EnemyActivityReport {
    private final Map<Vector3f, Long> soldiers = new HashMap<Vector3f, Long>();
    private final Map<Vector3f, Long> vehicles = new HashMap<Vector3f, Long>();

    public EnemyActivityReport(Set<Vector3f> targets, Set<Vector3f> vs) {
        accumulate(targets, vs);
    }

    public EnemyActivityReport(EnemyActivityReport e) {
        accumulate(e);
    }

    public Set<Vector3f> getCurrentHumanTargets() {
        
        
        
        Set<Vector3f> ret = new HashSet<Vector3f>(soldiers.keySet());
        if(ret.isEmpty()){
            return ret;
        }
        
        Set<String> newRet = convertToString(ret);
        
        
        return ret;
    }
    
    public Set<Vector3f> getCurrentVehicleTargets() {
        
        for(Iterator<Entry<Vector3f, Long>> it =vehicles.entrySet().iterator();it.hasNext();){
            if(it.next().getValue()<System.currentTimeMillis()-(2*60*1000)){
                it.remove();
            }
        }
        
        Set<Vector3f> ret = new HashSet<Vector3f>(vehicles.keySet());
        if(ret.isEmpty()){
            return ret;
        }
        
        Set<String> newRet = convertToString(ret);
        
        
        return ret;
    }

    private boolean hassAllValues(Set<String> ret, Set<String> previousRet) {
        for(String r:ret){
            if(!previousRet.contains(r)){
                return false;
            }
        }
        return true;
    }

    private Set<String> convertToString(Set<Vector3f> ret) {
        Set<String> ss = new HashSet<String>();
        for(Vector3f r:ret){
            ss.add(r.x+","+r.y+","+r.z);
        }
        return ss;
    }

    public void accumulate(Set<Vector3f> targets, Set<Vector3f> vs) {
        for(Vector3f target:targets){
            soldiers.put(new Vector3f(target), System.currentTimeMillis());
        }
        for(Vector3f v:vs){
            vehicles.put(new Vector3f(v), System.currentTimeMillis());
        }
    }

    public void accumulate(EnemyActivityReport e) {
        if(e!=null){
            accumulate(e.getCurrentHumanTargets(), e.getCurrentVehicleTargets());
        }
    }

    public Set<Vector3f> activity() {
        Set<Vector3f> activity = new HashSet<Vector3f>(soldiers.keySet());
        activity.addAll(vehicles.keySet());
        return activity;
    }

}
