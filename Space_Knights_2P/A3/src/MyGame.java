/*
* Author: Beau Derrick and Christian Salano
* Date: 5/10/17
*/
package a3;

import sage.display.*;
import sage.app.BaseGame;
import sage.scene.shape.*;
import sage.scene.HUDString;
import sage.scene.HUDObject;
import sage.camera.ICamera;
import sage.input.IInputManager;
import sage.input.action.IAction;
import sage.scene.SceneNode.CULL_MODE;
import graphicslib3D.Point3D;
import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;
import java.text.DecimalFormat;
import java.awt.Color;
import sage.renderer.*;
import sage.networking.IGameConnection.ProtocolType;
import java.io.IOException;
import java.net.InetAddress;
import sage.input.action.*;
import net.java.games.input.Event;
import javax.script.Invocable;
import sage.scene.SceneNode;
import java.io.*;
import java.util.*;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.net.UnknownHostException;
import myGameEngine.MyDisplaySystem;
import sage.scene.state.RenderState;
import sage.scene.state.TextureState;
import sage.terrain.AbstractHeightMap;
import sage.terrain.HillHeightMap;
import sage.terrain.TerrainBlock;
import sage.scene.SkyBox;
import sage.scene.Group;
import sage.texture.Texture;
import sage.texture.TextureManager;
import sage.physics.IPhysicsEngine;
import sage.physics.IPhysicsObject;
import sage.physics.PhysicsEngineFactory;
import sage.scene.TriMesh;
import sage.model.loader.OBJLoader;
import sage.model.loader.ogreXML.OgreXMLParser;
import sage.audio.Sound;
import sage.audio.SoundType;
import sage.audio.*;
import com.jogamp.openal.ALFactory;



public class MyGame extends BaseGame 
{
    IDisplaySystem display;
    ICamera camera;
    private float time;
    Client thisClient;

    String serverAddress;
    int serverPort;
    ProtocolType serverProtocol;
    
    private boolean isConnected;
    public Knight Player;
    public TriMesh model;
    MoveAvatar moveAvatar;
    
    public IInputManager inputManager;
    String kbName;
    String gpName;
    
    OrbitController orbitControl;
    
    SkyBox skybox;
    
    private Group rootNode;
    private ScriptEngine engine;
    private String scriptName = "Script.js";
    private File scriptFile;
    
    
   
    private Rectangle groundPlane;
    private IPhysicsObject groundPlaneP;
 
    
    private IPhysicsEngine physicsEngine;
    
    private TerrainBlock hillTerrain;
    
    OBJLoader loader;
    
    private Group avatarNode;
   

    private HUDString healthPoints;
    private HUDString deathsString;
    private HUDObject healthBar;
    private HUDString timeString ;
    
    private boolean isFull;
    
    IAudioManager audioMgr;
    Sound waterSound, npcSound;
    private Group soundNode;
    private float ayy;
    
    
      
    //  assumes main() gets address/port from command line
    public MyGame(String serverAddr, int sPort, boolean isF)
    { 
        super();
        this.serverAddress = serverAddr; 
        this.serverPort = sPort;
        this.serverProtocol = ProtocolType.TCP;
        isConnected = false;
        isFull = isF;
    }  
    
     private void initInput()
    {
        kbName = inputManager.getKeyboardName();    
        gpName = inputManager.getFirstGamepadName();
        
        IAction quit = new Quit(this);
        inputManager.associateAction(kbName,
                net.java.games.input.Component.Identifier.Key.ESCAPE, 
                quit,
                IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    }
     
     private IDisplaySystem createDisplaySystem()
    {
        IDisplaySystem display = new MyDisplaySystem(800, 600, 24, 20, isFull,"sage.renderer.jogl.JOGLRenderer");
        System.out.print("\nWaiting for display creation...");
        int count = 0;
        // wait until display creation completes or a timeout occurs
        while (!display.isCreated())
        {
            try
            {
            Thread.sleep(10); }
            catch (InterruptedException e)
            {
                throw new RuntimeException("Display creation interrupted"); }
                count++;
                System.out.print("+");
                if (count % 80 == 0) { System.out.println(); }
                if (count > 2000)    
                // 20 seconds (approx.)
                {
                    throw new RuntimeException("Unable to create display");
                }
            }
        
        System.out.println();
        return display;
    }
    
    public void initGame()
    {
        // Scripting
        ScriptEngineManager factory = new ScriptEngineManager();
        List<ScriptEngineFactory> list = factory.getEngineFactories();
        engine = factory.getEngineByName("js");
        scriptFile = new File(scriptName);
        this.runScript();
            
        // Set display
        //display = getDisplaySystem();
        display = createDisplaySystem();
        display.setTitle("My Game");
        setDisplaySystem(display);
        
        
        //Set camera
        camera = display.getRenderer().getCamera();
        camera.setPerspectiveFrustum(45, 1, 0.01, 1000);
        camera.setLocation(new Point3D(1,1,20));
       
        // Network
        try 
        { 
            thisClient = new Client(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);  
        } 
        
        catch (UnknownHostException e)   { e.printStackTrace();  } 
        catch (IOException e)   {  e.printStackTrace();  }
        if(thisClient != null)   
        { 
            thisClient.sendJoinMessage();
        }
 
        // Input
        inputManager = getInputManager();
        initInput();
        IAction updateCharacter = new UpdateCharacterAction();
        inputManager.associateAction(kbName,
               net.java.games.input.Component.Identifier.Key.M,
               updateCharacter,
               IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
        
        createGraphicsScene();
        initPhysicsSystem();
        createSagePhysicsWorld();
    }
    
    public void update(float elapsedTimeMS)
    {
        //Update time
        time += elapsedTimeMS;
        healthPoints.setText("Health Points = "+ Player.Health);
        deathsString.setText("Deaths = " + Player.Deaths);
        DecimalFormat df = new DecimalFormat("0.0");
        timeString.setText("Time = " + df.format(time/1000));
        
        if(thisClient != null) 
            thisClient.processPackets();
           
        Player.update(elapsedTimeMS);
        moveAvatar.update(elapsedTimeMS);    
        orbitControl.update(elapsedTimeMS);
        //Tell BaseGame to update game world State
        updatePhysics(elapsedTimeMS);
        super.update(elapsedTimeMS);
    }
    
    private void updatePhysics(float elapsedTimeMS)
    {
      Matrix3D mat;
      physicsEngine.update(elapsedTimeMS);
      
      for (SceneNode s : getGameWorld())
      { if (s.getPhysicsObject() != null)
        { 
            mat = new Matrix3D(s.getPhysicsObject().getTransform());
            s.getLocalTranslation().setCol(3,mat.getCol(3));
        }
      }
    }
    
    public TerrainBlock getTerrain()
    {
        return hillTerrain;
    }
    
    
    protected void shutdown()
    {   
        super.shutdown();
        if(thisClient != null)
        { 
            thisClient.sendByeMessage();
        try 
        { 
            thisClient.shutdown(); 
        }        
        // shutdown() is inherited
        catch (IOException e)  
        {
            e.printStackTrace();  
        }
        } 
    }
    
    public void setIsConnected(boolean flag)
    {
       isConnected = flag; 
    }
    
    public Vector3D getPlayerPosition()
    {
        Vector3D pos = new Vector3D();
        pos.setX(Player.KnightGroup.getLocalTranslation().getCol(3).getX());
        pos.setY(Player.KnightGroup.getLocalTranslation().getCol(3).getY());
        pos.setZ(Player.KnightGroup.getLocalTranslation().getCol(3).getZ());
        return pos;
    }
    
    public void AddGhostToGame(Pyramid ghost)
    {
        addGameWorldObject(ghost);
    }
    
    public void RemoveGhost(Pyramid ghost)
    {
        removeGameWorldObject(ghost);
    }
    
    private void runScript()
  { try
    { FileReader fileReader = new FileReader(scriptFile);
      engine.eval(fileReader);
      fileReader.close();
    }
    catch (FileNotFoundException e1)
    { System.out.println(scriptFile + " not found " + e1); }
    catch (IOException e2) 
    { System.out.println("IO problem with " + scriptFile + e2); }
    catch (ScriptException e3)  
    { System.out.println("ScriptException in " + scriptFile + e3); }
    catch (NullPointerException e4)
    { System.out.println ("Null ptr exception reading " + scriptFile + e4); }
  } 
    
    private void initTerrain()
{ // create height map and terrain block
    HillHeightMap myHillHeightMap = new HillHeightMap(129, 2000, 5.0f, 20.0f,(byte)2, 12345);
    myHillHeightMap.setHeightScale(0.1f);
    hillTerrain = createTerBlock(myHillHeightMap);
    // create texture and texture state to color the terrain
    TextureState grassState;
    Texture grassTexture = TextureManager.loadTexture2D("Pictures/squareMoonMap.jpg");
    grassTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
    grassState = (TextureState)
    display.getRenderer().createRenderState(RenderState.RenderStateType.Texture);
    grassState.setTexture(grassTexture,0);
    grassState.setEnabled(true);
    // apply the texture to the terrain
    hillTerrain.setRenderState(grassState);
    addGameWorldObject(hillTerrain);
}

private TerrainBlock createTerBlock(AbstractHeightMap heightMap)
	 { float heightScale = heightMap.getHeightScale();
	 Vector3D terrainScale = new Vector3D(1, heightScale, 1);
	 // use the size of the height map as the size of the terrain
	 int terrainSize = heightMap.getSize();  
	 // specify terrain origin so heightmap (0,0) is at world origin
	 float cornerHeight = heightMap.getTrueHeightAtPoint(0, 0) * heightScale;
	 Point3D terrainOrigin = new Point3D(0, -cornerHeight, 0);
	 // create a terrain block using the height map
	 String name = "Terrain:" + heightMap.getClass().getSimpleName();
	 TerrainBlock tb = new TerrainBlock(name, terrainSize, terrainScale,
	 heightMap.getHeightData(), terrainOrigin);
	 return tb;
	 }

private void createGraphicsScene()
    {
        rootNode = new Group("Root Node");
		skybox = new SkyBox("SkyBox", 100.0f, 100.0f, 100.0f);
		Texture topFile = TextureManager.loadTexture2D("Pictures/top.jpg");
		Texture leftFile = TextureManager.loadTexture2D("Pictures/left.jpg");
		Texture fntFile = TextureManager.loadTexture2D("Pictures/center.jpg");
		Texture rightFile = TextureManager.loadTexture2D("Pictures/right.jpg");
		Texture bkFile = TextureManager.loadTexture2D("Pictures/back.jpg");
		Texture botFile = TextureManager.loadTexture2D("Pictures/bottom.jpg");
		
		skybox.setTexture(SkyBox.Face.Up,topFile);
		skybox.setTexture(SkyBox.Face.Down,botFile);
		skybox.setTexture(SkyBox.Face.North,fntFile);
		skybox.setTexture(SkyBox.Face.South,bkFile);
		skybox.setTexture(SkyBox.Face.West,leftFile);
		skybox.setTexture(SkyBox.Face.East,rightFile);
		skybox.scale(3.0f, 3.0f, 3.0f);
		rootNode.addChild(skybox);
	        addGameWorldObject(rootNode);
          
                avatarNode = new Group("Avatar Node");
        
                
        initTerrain();
        initAudio();
                
        //Add World Axis
        Point3D origin = new Point3D(0,0,0);
        Point3D xEnd = new Point3D(100,0,0);
        Point3D yEnd = new Point3D(0,100,0);
        Point3D zEnd = new Point3D(0,0,100);
        Line xAxis = new Line (origin, xEnd, Color.red, 2);
        Line yAxis = new Line (origin, yEnd, Color.green, 2);
        Line zAxis = new Line (origin, zEnd, Color.blue, 2);
        addGameWorldObject(xAxis);
        addGameWorldObject(yAxis);
        addGameWorldObject(zAxis);
                
        loader =new OBJLoader();
        TriMesh myPlatform = loader.loadModel("blender/spawner.obj");
        myPlatform.scale(2f, 2f, 2f);
        Texture myplatformTexture = TextureManager.loadTexture2D("blender/spawner.png");
        myplatformTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
        display.getRenderer().createRenderState(RenderState.RenderStateType.Texture);
        myPlatform.updateLocalBound();
        addGameWorldObject(myPlatform);
        Player = new Knight(this, getPlayerAvatar(), inputManager, kbName, thisClient);
        thisClient.SetPlayer(Player);
        moveAvatar = new MoveAvatar(Player.KnightGroup, inputManager, kbName, false, thisClient);
        Player.AddController(moveAvatar);
        orbitControl = new OrbitController(camera, Player.KnightGroup, inputManager , kbName, false);
        
        
        deathsString = new HUDString("Deaths = " + Player.Deaths);
        deathsString.setLocation(0.0, 0.8);
        addGameWorldObject(deathsString);
        timeString = new HUDString("Time = " + time);
        timeString.setLocation(0,0.06); // (0,0) [lower-left] to (1,1)
        addGameWorldObject(timeString);
        healthPoints = new HUDString ("Health Points " +  Player.Health); //default is (0,0)
        addGameWorldObject(healthPoints);  
        healthPoints.setLocation(0,0.03);
        Rectangle healthStuff = new Rectangle(0.17f,0.05f);
        healthBar = new HUDObject(0.0,0.15,Color.GREEN) {
            @Override
            public void draw(IRenderer ir) {
                healthStuff.setColor(Color.GREEN);
                 float xP = (float) -.68;
                float zP = (float) 0.0;
                float yP = (float) -0.92;
                System.out.println(healthStuff.getWorldTranslation());
                Matrix3D newRect = new Matrix3D();
                newRect.translate(xP, yP, zP);
                healthStuff.setWorldTranslation(newRect);
                ir.draw(healthStuff);
            }
        };
        addGameWorldObject(healthBar);

        
  
        Matrix3D xform = new Matrix3D();
        groundPlane = new Rectangle(1f, 1f); 
        xform = new Matrix3D();
        xform.scale(1000, 1, 1000);
        groundPlane.setLocalScale(xform);
        groundPlane.setWorldScale(groundPlane.getLocalScale());
        xform = new Matrix3D();
        xform.translate(0, -100, 0);
        groundPlane.setLocalTranslation(xform);
        groundPlane.setWorldTranslation(groundPlane.getLocalTranslation());
        groundPlane.setColor(Color.RED);
        groundPlane.setCullMode(CULL_MODE.NEVER);
        groundPlane.updateLocalBound();
        groundPlane.setShowBound(true);
        addGameWorldObject(groundPlane);
        groundPlane.updateGeometricState(1.0f, true);

    
    }
    
  protected void initPhysicsSystem()
  {
      String pEngine = "sage.physics.JBullet.JBulletPhysicsEngine";
      physicsEngine = PhysicsEngineFactory.createPhysicsEngine(pEngine);
      physicsEngine.initSystem();
      float[] gravity = {0, -10f, 0};
      physicsEngine.setGravity(gravity);
  }
  
    private void createSagePhysicsWorld()
  { 

    float mass = 1f;
    Matrix3D mat;
    
//  add the ground plane physics
    float up[] = {0f, 1f, 0};  // {0,1,0} is flat
    groundPlaneP = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(),
    groundPlane.getWorldTransform().getValues(), up, 0.0f);
    groundPlaneP.setBounciness(.0001f);
    groundPlane.setPhysicsObject(groundPlaneP);
  }   
    
    public void AddObjectToGame(SceneNode n)
    {
        addGameWorldObject(n);
    }
    
    public void RemoveObjectToGame(SceneNode n)
    {
        boolean test = removeGameWorldObject(n);
        System.out.println(test);
    }
    
    
    public Knight getPlayer()
    {
        return Player;
    }
    
     public void initAudio()
        {
        AudioResource resource1, resource2;
        audioMgr = AudioManagerFactory.createAudioManager(
        "sage.audio.joal.JOALAudioManager");
        if(!audioMgr.initialize())
        { System.out.println("Audio Manager failed to initialize!");
        return;
        }
        resource1 = audioMgr.createAudioResource("Audio/bensound-instinct.wav",
        AudioResourceType.AUDIO_SAMPLE);
        resource2 = audioMgr.createAudioResource("Audio/outputfile.wav",
        AudioResourceType.AUDIO_SAMPLE);
        npcSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
        waterSound =new Sound(resource2, SoundType.SOUND_EFFECT, 100, true);
        npcSound.initialize(audioMgr);
        waterSound.initialize(audioMgr);
        npcSound.setMaxDistance(50.0f);
        npcSound.setMinDistance(3.0f);
        npcSound.setRollOff(5.0f);
        waterSound.setMaxDistance(50.0f);
        waterSound.setMinDistance(3.0f);
        waterSound.setRollOff(5.0f);
        npcSound.setLocation(new Point3D(avatarNode.getWorldTranslation().getCol(3)));
        waterSound.setLocation(new Point3D(1000,1000,1000));
        setEarParameters();
        npcSound.play();
        waterSound.play();
        }
        
         public void setEarParameters()
        {
        
                
        Matrix3D avDir = new Matrix3D();
        avDir.translate(-100.0f,-100.0f,-100.0f);
        float camAz = ayy;
        avDir.rotateY(180.0f-camAz);
        Vector3D camDir = new Vector3D(0,0,1);
        camDir = camDir.mult(avDir);
        audioMgr.getEar().setLocation(new Point3D(0,0,0));
        audioMgr.getEar().setOrientation(camDir, new Vector3D(0,1,0));
        }
    
    public Group getPlayerAvatar()
    {
        Group model = null ;
        OgreXMLParser loader = new OgreXMLParser();
        
        try
        {
            String slash = File.separator;
            model = loader.loadModel("ModelRed" + slash + "Plane.mesh.xml",
            "ModelRed" + slash + "Material.material",
            "ModelRed" + slash + "Plane.skeleton.xml");
            model.updateGeometricState(0, true);
        }
        catch (Exception e)
        { 
            e.printStackTrace();
            System.exit(1);
        }
        
        return model ;
    }
    
    
    private class UpdateCharacterAction extends AbstractInputAction 
  {   
    public void performAction(float time, Event e)
    {
        
        //cast the engine so it supports invoking functions
      Invocable invocableEngine = (Invocable) engine ;
        //get the node which is to be updated
      //SceneNode player = avatar;
  // invoke the script function
      try
      { invocableEngine.invokeFunction("updateCharacter", Player.KnightGroup); }
      catch (ScriptException e1)  
      { System.out.println("ScriptException in " + scriptFile + e1); }
      catch (NoSuchMethodException e2)
      { System.out.println("No such method exception in " + scriptFile + e2); }
      catch (NullPointerException e3)
      { System.out.println ("Null ptr exception reading " + scriptFile + e3); }
    }
  }
    
    
}
