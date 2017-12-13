/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;

import javax.imageio.ImageIO;
import java.io.IOException;

/**
 *
 * @author dharshanar
 */
public class ImageLoader implements AssetLoader{

    public Object load(AssetInfo ai) throws IOException {
        return ImageIO.read(ai.openStream());
    }
    
}
