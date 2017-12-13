/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.effect.ParticleEmitter;

/**
 *
 * @author dharshanar
 */
public class CharcterParticleEmitter {
    
    private final ParticleEmitter flash;
    private final ParticleEmitter smokeTrail;
    private final ParticleEmitter blood;
    private final ParticleEmitter bulletFragments;
    private final ParticleEmitter blastFragments;
    private final ParticleEmitter smoke;

        public CharcterParticleEmitter(ParticleEmitter createFlash, ParticleEmitter smokeTrail, ParticleEmitter smoke, ParticleEmitter debris, ParticleEmitter bulletFragments, ParticleEmitter blastFragments) {
            this.flash = createFlash;
            this.smokeTrail = smokeTrail;
            this.smoke = smoke;
            this.blood = debris;
            this.bulletFragments = bulletFragments;
            this.blastFragments = blastFragments;
        }
        
        public ParticleEmitter getFlashEmitter(){
            return flash;
        }
        
        public ParticleEmitter getSmokeTrailEmitter(){
            return smokeTrail;
        }
        
        public ParticleEmitter getSmokeEmiter(){
            return smoke;
        }
        
        public ParticleEmitter getBloodEmitter(){
            return blood;
        }

        public ParticleEmitter getBulletFragments() {
            return bulletFragments;
        }

        public ParticleEmitter getBlastFragments() {
            return blastFragments;
        }
        
    }
