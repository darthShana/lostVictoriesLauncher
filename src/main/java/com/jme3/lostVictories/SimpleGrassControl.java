/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.lostVictories.network.messages.TreeGroupMessage;
import com.jme3.lostVictories.network.messages.TreeMessage;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Quad;
import com.jme3.shader.VarType;
import com.jme3.terrain.Terrain;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.plugins.AWTLoader;
import jme3tools.optimize.GeometryBatchFactory;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

//import jme3tools.converters.ImageToAwt;


/**
 * This is a grass control that can be added to any terrain or any spatial/node that has
 * a terrain object attached to it.
 *
 * This is a simple implementation of a grass control and does not use paging
 * or anything too fancy :p
 *
 * Grass is generated only where there is the first texture layer displayed on the terrain
 * The first layer is the grass layer (only been tested for up to 3 layers)
 *
 * Values and materials should be modified to suite your needs
 *
 */
public class SimpleGrassControl extends AbstractControl{

    AssetManager assetManager;
    Material faceMat;
    private final TerrainQuad terrain;
    Quad faceShape;
    Cylinder faceTop;
    Node grassLayer = new Node();
    Node treeLayer = new Node();
    float scale;

    /*
     * Should be greater than 1
     * GrassPatches will be scaled randomly by a factor between 1/patchScaleVariation and patchScaleVariation
     */
    float patchScaleVariation = 2f;
    /*
     * The width of the grass Quads
     */
    float patchWidth = 2.0f;
    /*
     * The height of the Grassquads
     */
    float patchHeight = .8f;
    /*
     * Increment for the uniform grass planting algorithm
     * The lower this value the more dense the grass
     * Making this a very low value may cause memory issues
     */
    float inc1 = 10;
    float inc2 = 100;
    private final BulletAppState bulletAppState;
    private final Node rootNode;
    private final Set<TreeGroupMessage> allTrees;

    public SimpleGrassControl(AssetManager assetManager, BulletAppState bulletAppState, Node rootNode, TerrainQuad terrain, Set<TreeGroupMessage> allTrees, String texturePath)
    {
        this.assetManager = assetManager;
        faceMat = new Material(assetManager,"Resources/MatDefs/Grass/grassBase.j3md");
        this.terrain = terrain;

        faceMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Off);
        faceMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        faceMat.setTransparent(true);
        faceMat.setTextureParam("ColorMap",VarType.Texture2D,assetManager.loadTexture(texturePath));
        faceMat.setBoolean("VertexLighting",false);
        faceMat.setInt("NumLights", 4);
        faceMat.setBoolean("VertexColors", false);
        faceMat.setBoolean("FadeEnabled", false);
        faceMat.setFloat("FadeEnd", 2000);
        faceMat.setFloat("FadeRange", 0);
        faceMat.setBoolean("FadeEnabled", true);
        faceMat.setBoolean("SelfShadowing", false);
        faceMat.setBoolean("Swaying",true);
        faceMat.setVector3("SwayData",new Vector3f(1.5f,1,5));
        faceMat.setVector2("Wind", new Vector2f(1,1));
        this.bulletAppState = bulletAppState;
        this.rootNode = rootNode;
        this.allTrees = allTrees;
        
   }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        Node spatNode = (Node)spatial;

        if(terrain==null||spatNode.getChildren().isEmpty()) {
            Logger.getLogger(SimpleGrassControl.class.getName()).log(Level.SEVERE, "Could not find terrain object.", new Exception());
            System.exit(0);
        }

        scale = ((Spatial)terrain).getWorldScale().x;

       //Generate grass uniformly with random offset.
       float terrainWidth = scale*terrain.getTerrainSize(); // get width length of terrain(assuming its a square)
       Vector3f centre = (((Spatial)terrain).getWorldBound().getCenter()); // get the centr location of the terrain
       Vector2f grassPatchRandomOffset = new Vector2f().zero();
       Vector3f candidateGrassPatchLocation = new Vector3f();
//       terrain.getMaterial().setTextureParam("AlphaMap", VarType.Texture2D, assetManager.loadTexture("Textures/alphamap.jpg")); //(at)pixelapp: I added this single line.
       this.assetManager.registerLoader( ImageLoader.class, "jpg");
       BufferedImage image = (BufferedImage) assetManager.loadAsset("Textures/alphamap.jpg");
       this.assetManager.registerLoader(AWTLoader.class, "jpg");
//       try{
           //final Object loadAsset = assetManager.loadAsset("Textures/alphamap.jpg");
//       }catch(IOException e){}
       
       //final Image image1 = assetManager.loadTexture().getImage();
       //image = ImageToAwt.convert(image1, true, true, 0);

       for(float x = centre.x - terrainWidth/2 + inc1; x < centre.x + terrainWidth/2 - inc1; x+=inc1){
           for(float z = centre.z - terrainWidth/2 + inc1; z < centre.z + terrainWidth/2 - inc1; z+=inc1){
               grassPatchRandomOffset.set(0, inc1);
               grassPatchRandomOffset.multLocal(new Random().nextFloat()); // make the off set length a random distance smaller than the increment size
               grassPatchRandomOffset.rotateAroundOrigin((float)(((int)(Math.random()*359))*(Math.PI/180)), true); // rotate the offset by a random angle
               candidateGrassPatchLocation.set(x+grassPatchRandomOffset.x, terrain.getHeight(new Vector2f(x+grassPatchRandomOffset.x,z+grassPatchRandomOffset.y)), z+grassPatchRandomOffset.y);

               if(isGrassLayer(candidateGrassPatchLocation, image)){
                    createGrassPatch(candidateGrassPatchLocation);
               }

           }
         }

         for(TreeGroupMessage treeGroup:allTrees){
            candidateGrassPatchLocation.set(treeGroup.getLocation().x, terrain.getHeight(new Vector2f(treeGroup.getLocation().x+grassPatchRandomOffset.x,treeGroup.getLocation().z+grassPatchRandomOffset.y)), treeGroup.getLocation().z);

            createTree(candidateGrassPatchLocation, treeGroup);
               

           
       }
       grassLayer = (Node) GeometryBatchFactory.optimize(grassLayer, true);
        rootNode.attachChild(grassLayer);
       rootNode.attachChild(treeLayer);
       
     }

    @Override
    protected void controlUpdate(float tpf) {

    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }
    
    private void createTree(Vector3f location, TreeGroupMessage treeGroup){
        Node n = new Node();
//        System.out.println("{ \"location\":{ \"x\":"+location.x+",\"y\":"+location.y+",\"z\":"+location.z+"},");
//        System.out.println("    \"trees\":[");

        for(TreeMessage tree:treeGroup.getTrees()){
            Spatial t = (Node) assetManager.loadModel("Models/Plants/Tree1.j3o");            
            t.setLocalScale(.5f);
            t.rotate(new Quaternion().fromAngleAxis( (((int)(Math.random()*359))+1) *(FastMath.PI/190), Vector3f.UNIT_Y));
            float x = tree.getLocation().x;
            float z = tree.getLocation().z;
            t.setLocalTranslation(x, 0, z);
//            System.out.println("{ \"location\":{ \"x\":"+x+",\"y\":"+0+",\"z\":"+z+"}, \"standing\":true}");
            n.attachChild(t);
        }
//        System.out.println("]},");
        n.setLocalTranslation(location);
//        n = (Node) GeometryBatchFactory.optimize(n, true);
        RigidBodyControl treeMesh = new RigidBodyControl(CollisionShapeFactory.createMeshShape((Node) n), 0);
        bulletAppState.getPhysicsSpace().add(treeMesh);

        treeLayer.attachChild(n);    
    }

    private void createGrassPatch(Vector3f location){

        Node grassPatch = new Node();
        float selectedSizeVariation = (float)(new Random().nextFloat()*(patchScaleVariation-(1/patchScaleVariation)))+(1/patchScaleVariation);
        faceShape = new Quad((patchWidth*selectedSizeVariation),patchHeight*selectedSizeVariation,false);
        Geometry face1 = new Geometry("face1",faceShape);
        face1.move(-(patchWidth*selectedSizeVariation)/2, 0, 0);
        grassPatch.attachChild(face1);

        Geometry face2 = new Geometry("face2",faceShape);
        face2.rotate(new Quaternion().fromAngleAxis(-FastMath.HALF_PI,   new Vector3f(0,1,0)));
        face2.move(0, 0, -(patchWidth*selectedSizeVariation)/2);
        grassPatch.attachChild(face2);
        
        faceTop = new Cylinder(8, 8, patchWidth*selectedSizeVariation/4, patchHeight*selectedSizeVariation/4);
        Geometry face3 = new Geometry("face2",faceTop);
        face3.rotate(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
        grassPatch.attachChild(face3);
        
        grassPatch.setCullHint(Spatial.CullHint.Dynamic);
        grassPatch.setQueueBucket(RenderQueue.Bucket.Transparent);

        face1.setMaterial(faceMat);
        face2.setMaterial(faceMat);
        face3.setMaterial(faceMat);

        grassPatch.rotate(new Quaternion().fromAngleAxis( (((int)(Math.random()*359))+1) *(FastMath.PI/190),   new Vector3f(0,1,0)));
        grassPatch.setLocalTranslation(location);

        grassLayer.attachChild(grassPatch);
    }

    private boolean isGrassLayer(Vector3f pos, BufferedImage image){
        try{
        
        
        Vector2f uv = getPointPercentagePosition(terrain, pos);

        
        int width = image.getWidth();
        int height = image.getHeight();

        int x = (int)(uv.x*width);
        int y = (int)(uv.y*height   );

        
        //ColorRGBA color = new ColorRGBA().set(ColorRGBA.Black);
            int pixel = image.getRGB(x, y);
            int alpha = (pixel >> 24) & 0xff;
            int red = (pixel >> 16) & 0xff;
            int green = (pixel >> 8) & 0xff;
            int blue = (pixel) & 0xff;
            //System.out.println("argb: " + alpha + ", " + red + ", " + green + ", " + blue);

            if(red>200 && green<100 && blue<100) {
//                System.out.println("x, y:"+x+", "+y);
                return true;
            } else {
                //System.out.println("x, y:"+x+", "+y);
                return false;
            }
        }catch(IllegalArgumentException e){
            return false;
        }

    }

    private Vector2f getPointPercentagePosition(Terrain terrain, Vector3f worldLoc) {
        Vector2f uv = new Vector2f(worldLoc.x,-worldLoc.z);
        uv.subtractLocal(((Node)terrain).getWorldTranslation().x*scale, ((Node)terrain).getWorldTranslation().z*scale); // center it on 0,0
        float scaledSize = terrain.getTerrainSize()*scale;
        uv.addLocal(scaledSize/2, scaledSize/2); // shift the bottom left corner up to 0,0
        uv.divideLocal(scaledSize); // get the location as a percentage

        return uv;
    }

}
