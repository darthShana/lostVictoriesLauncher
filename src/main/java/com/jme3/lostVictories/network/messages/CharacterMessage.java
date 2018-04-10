/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.network.messages.actions.*;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;
import java.util.*;

/**
 *
 * @author dharshanar
 */
public class CharacterMessage implements Serializable{

    UUID id;
    UUID userID;
    Vector location;
    Country country;
    Weapon weapon;
    RankMessage rank;
    UUID commandingOfficer;
    UUID boardedVehicle;
    Set<UUID> unitsUnderCommand = new HashSet<UUID>();
    Set<UUID> passengers = new HashSet<UUID>();
    UUID checkoutClient;
    Long checkoutTime;
    CharacterType type;
    Vector orientation;
    Set<Action> actions;
    Map<String, String> objectives ;
    Set<String> completedObjectives;
    boolean dead;
    boolean engineDamaged;
    Long timeOfDeath;
    long version;
    int killCount;
    SquadType squadType;
    long creationTime;
    boolean busy;
    boolean attacking;


    private CharacterMessage(){}
    
    public CharacterMessage(UUID id, Vector location, Vector orientation, RankMessage rank, Set<Action> actions, Map<String, String> objectives, Set<String> completedObjectives, long version) {
        this.id = id;
        this.location = location;
        this.orientation = orientation;
        this.rank = rank;
        this.actions = actions;
        this.objectives = objectives;
        this.completedObjectives = completedObjectives;
        this.version = version;
        this.creationTime = System.currentTimeMillis();
    }
    
    public UUID getId() {
        return id;
    }
    
    public Vector getLocation() {
	return location;
    }

    public Country getCountry() {
        return country;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public RankMessage getRank() {
        return rank;
    }

    public UUID getCommandingOfficer() {
        return commandingOfficer;
    }

    public void setCommandingOfficer(UUID commandingOfficer){
        this.commandingOfficer = commandingOfficer;
    }
    
    public CharacterType getType() {
        return type;
    }
    
    public void setType(CharacterType type){
        this.type = type;
    }

    public Set<UUID> getUnitsUnderCommand() {
        return unitsUnderCommand;
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isCheckedOutBy(UUID clientID) {
        return clientID.equals(this.checkoutClient);
    }

    public boolean isAvatar() {
        return type == CharacterType.AVATAR;
    }

    public boolean shouldBeControledRemotely(UUID avatarId) {
        return this.checkoutClient!=null && !this.checkoutClient.equals(avatarId) && !id.equals(avatarId);
    }

    public Vector getOrientation() {
        return orientation;
    }
    
    public int getKillCount(){
        return killCount;
    }
    
    public Map<String, String> getObjectives(){
        return new HashMap<>(objectives);
    }

    public Move isMoving() {
        for(Action a: actions){
            if(a instanceof Move){
                return (Move) a;
            }
        }
        return null;
    }

    public Shoot isShooting() {
        for(Action a: actions){
            if(a instanceof Shoot){
                return (Shoot) a;
            }
        }
        return null;
    }
    
    
    public boolean isCrouching(){
        for(Action a: actions){
            if(a instanceof Crouch){
                return true;
            }
        }
        return false;
    }

    public boolean hasSetupWeapon() {
        for(Action a: actions){
            if(a instanceof SetupWeapon){
                return true;
            }
        }
        return false;
    }
    
    public Set<UUID> getPassengers(){
        return passengers;
    }
    
    public void setPassengers(Set<UUID> passengers){
        this.passengers = passengers;
    }
    
    public SquadType getSquadType(){
        return squadType;
    }

    public UUID getBoardedVehicle() {
        return boardedVehicle;
    }
    
    public void setBoardedVehicle(UUID boardedVehicle){
        this.boardedVehicle = boardedVehicle;
    }

    public void setEngineDamage(boolean b) {
        engineDamaged = b;
    }

    public boolean hasEngineDamage() {
        return engineDamaged;
    }
    
    public void setCountry(Country country){
        this.country = country;
    }

    public void addAction(Action action) {
        actions.add(action);
    }
    
    public long getVersion(){
        return version;
    }
    
    public UUID getCheckoutClient(){
        return checkoutClient;
    }
    
    public long getCreationTime(){
        return creationTime;
    }

    public boolean isSameAs(GameCharacterNode rhs) {


        final EqualsBuilder builder = new EqualsBuilder()
                .append(id, rhs.getIdentity())
                .append(actions, rhs.getActionsToMessage())
                .append(completedObjectives, rhs.getCompletedObjectives());
        if(objectives!=null){
            builder.append(objectives.keySet(), rhs.getAllObjectives().keySet());
        }
        return builder.isEquals() && isClose(location, new Vector(rhs.getLocalTranslation()), 0.1) && isClose(orientation, new Vector(rhs.getPlayerDirection()), 0.1);
    }



    
    public static boolean isClose(Vector v1, Vector v2, double d) {
        if(Math.abs(v1.x - v2.x)>d){
            return false;
        }
        
        if(Math.abs(v1.z - v2.z)>d){
            return false;
        }
        return true;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
    
    public boolean isBusy(){
        return busy;
    }
    
    public boolean isAttacking(){
        return attacking;
    }

    @JsonIgnore
    public boolean hasBeenSentRecently() {
        return System.currentTimeMillis()-creationTime<2000;
        
    }

    public boolean isOlderVersion(long version) {
        return this.version<version;
    }

    public Set<Action> getActions() {
        return actions;
    }

    public Set<String> getCompletedObjectives() {
        return completedObjectives;
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    public void setUnitsUnderCommand(Set<UUID> unitsUnderCommand) {
        this.unitsUnderCommand = unitsUnderCommand;
    }

    public void setCheckoutClient(UUID checkoutClient) {
        this.checkoutClient = checkoutClient;
    }

    public void setCheckoutTime(long checkoutTime) {
        this.checkoutTime = checkoutTime;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public void setTimeOfDeath(long timeOfDeath) {
        this.timeOfDeath = timeOfDeath;
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;
    }

    public void setSquadType(SquadType squadType) {
        this.squadType = squadType;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

}
