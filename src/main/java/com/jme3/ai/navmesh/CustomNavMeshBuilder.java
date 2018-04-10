/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.ai.navmesh;


import com.jme3.export.binary.BinaryExporter;
import com.jme3.scene.Geometry;

import java.io.File;

/**
 *
 * @author dharshanar
 */
public class CustomNavMeshBuilder {

    public static NavMesh buildMesh(Geometry geometry) {
        try {
//            BinaryExporter exporter = BinaryExporter.getInstance();
//                File file = new File("NavMesh.j3o");
//                Geometry toSave = new Geometry("NavMesh", geometry.getMesh());
//                exporter.save(toSave, file);
//            final CustomNavMesh navMesh = new CustomNavMesh(geometry.getMesh());
            NavMesh navMesh = new NavMesh(geometry.getMesh());
            return navMesh;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
