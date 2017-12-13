/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.LoopMode;
import com.jme3.lostVictories.characters.blenderModels.BlenderModel;
import com.jme3.lostVictories.characters.weapons.Weapon;

/**
 *
 * @author dharshanar
 */
public class GameAnimChannel {
    private final AnimChannel channel;
    private final BlenderModel model;

    GameAnimChannel(AnimChannel channel, BlenderModel m) {
        this.channel = channel;
        this.model = m;
    }

    public String getAnimationName() {
        return channel.getAnimationName();
    }

    public void setAnim(String first) {
        setAnim(first, null, null, null, null);
    }
    
    public void setAnim(String string, Float blend) {
        setAnim(string, blend, null, null, null);
    }
    
    public void setAnim(String string, LoopMode loopMode) {
        setAnim(string, null, loopMode, null, null);
    }
    
    public void setAnim(String string, LoopMode loopMode, Float speed) {
        setAnim(string, null, loopMode, speed, null);
    }
    
    public void setAnim(String string, LoopMode loopMode, Float speed, Float time) {
        setAnim(string, null, loopMode, speed, time);
    }
    
    void setAnimForce(String animName) {
        channel.setAnim(animName);
    }
    
    void setAnimForce(String animName, LoopMode loopMode) {
        channel.setAnim(animName);
        if(loopMode!=null){
            channel.setLoopMode(loopMode);
        }
    }

    public void setAnim(String string, Float blend, LoopMode loopMode, Float speed, Float time) {
        if(!animationsStoped()){
            if(blend==null){
                try {
                    channel.setAnim(string);
                }catch (Exception e){
                    System.out.println("error setting animation:"+string+" on model:"+model);
                    throw e;
                }
            }else{
                channel.setAnim(string, blend);
            }
            if(loopMode!=null){
                channel.setLoopMode(loopMode);
            }
            if(speed!=null){
                channel.setSpeed(speed);
            }
            if(time!=null){
                channel.setTime(time);
            }
        }
    }

    public double getAnimMaxTime() {
        return channel.getAnimMaxTime();
    }

    private boolean animationsStoped() {
        if(channel.getAnimationName() == null){
            return false;
        }
        if("embark_vehicle".equals(channel.getAnimationName())){
            return true;
        }
        if("gunnerDeathAction".equals(channel.getAnimationName())){
            return true;
        }
        return false;
    }

    public float getSpeed() {
        return channel.getSpeed();
    }

    public void setAnimNice(String animationName, Weapon weapon, LoopMode loopMode) {
        if(!weapon.isInFiringSequence(channel.getAnimationName())){
            setAnim(animationName, loopMode);
        }
    }
}
