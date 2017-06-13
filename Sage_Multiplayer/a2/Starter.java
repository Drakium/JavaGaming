//Christian Solano
//A2 CSC 165
//3D SpaceFarming Multiplayer

package a2;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.text.DecimalFormat;
import java.util.ArrayList;


import graphicslib3D.*;
import net.java.games.input.Controller;
import sage.app.BaseGame;
import sage.camera.*;
import sage.camera.controllers.ThirdPersonOrbitCameraController;
import sage.display.IDisplaySystem;
import sage.event.EventManager;
import sage.event.IEventManager;
import sage.input.IInputManager;
import sage.input.action.IAction;
import sage.input.action.QuitGameAction;
import sage.input.*;
import sage.renderer.*;
import sage.scene.*;
import sage.scene.shape.Cube;
import sage.scene.shape.Line;
import sage.scene.shape.Pyramid;
import sage.scene.shape.Rectangle;
import sage.scene.shape.Sphere;
import sage.scene.shape.Teapot;
import myGameEngine.*;
public class Starter extends BaseGame
{
 private IRenderer renderer;
 private IInputManager inputMgr;
 private SceneNode player1, player2;
 private ICamera camera1, camera2;
 private ICamera p1Position;
 private IEventManager eventMgr;
 private int randomNumber;
 int max = 20;
 int min = -5;
 private GraphicsDevice device;
 private int scoreP1 = -5;
 private int scoreP2 = -5;
 private float timeP1 = 0;
 private float timeP2 = 0;// game elapsed time
 private float fTime = 0;
 private Group myPlants,myTruckGroup;
 private HUDString scoreString ;
 private HUDString timeString ;
 private HUDString finalTime;
 private HUDString scoreStringP1;
 private HUDString timeStringP1;
 private HUDString scoreStringP2;
 private HUDString timeStringP2;
 private IDisplaySystem display;
 Rectangle rect;
 MyPyramid aPyr,aPyr2,aPyr3,aPyr4,aPyr5,aPyr6;
 MyTruck myTruck;
 Sphere spher,spher2;
 private ThirdPersonCameraController cam1Controller;
 private Camera3Pcontroller cam2Controller;
 private MyPlantsController plantController;
 private MyTruckController truckController;
 int numCrashes = -8;
 int boxCountP1 = -4,boxCountP2 = -4;
 int boxSwitch = 0;
 
 private IDisplaySystem createDisplaySystem()
 {
	 display = new MyDisplaySystem(
				800,
				600,
				64,
				60,
				true,
				"sage.renderer.jogl.JOGLRenderer"
				);
 System.out.print("\nWaiting for display creation...");
 int count = 0;
 // wait until display creation completes or a timeout occurs
 while (!display.isCreated())
 {
 try
 { Thread.sleep(10); }
 catch (InterruptedException e)
 { throw new RuntimeException("Display creation interrupted"); }
 count++;
 System.out.print("+");
 if (count % 80 == 0) { System.out.println(); }
 if (count > 2000) // 20 seconds (approx.)
 { throw new RuntimeException("Unable to create display");
 }
 }
 System.out.println();
 return display ;
 }
 protected void shutdown()
 { display.close() ;
 //...other shutdown methods here as necessary...
 }
 protected void initSystem() {
		// call a local method to create a DisplaySystem object
		IDisplaySystem display = createDisplaySystem();
		setDisplaySystem(display);
	
		// create an Input Manager
		IInputManager im = new InputManager();
		setInputManager(im);
		
		// create an (empty) gameworld
		ArrayList<SceneNode> gameWorld = new ArrayList<SceneNode>();
		setGameWorld(gameWorld);
	}
 
 protected void initGame()
 {
	 display = getDisplaySystem();
	 getDisplaySystem().setTitle("SpaceFarming3D - Multiplayer");
 
 
 eventMgr = EventManager.getInstance();
 renderer = display.getRenderer();
 inputMgr = getInputManager();
 createPlayers();
 createScene();

 System.out.println("");
 initInput();
 
 }
 
 private void createPlayers()
 { 
	 
 player1 = new Pyramid("PLAYER1");
 player1.updateWorldBound(); 
 player1.translate(0, 1, -10);
 player1.rotate(0, new Vector3D(0, 1, 0));
 
 
 addGameWorldObject(player1);
 
 //System.out.println(player1.getWorldBound());
 camera1 = new JOGLCamera(renderer);
 camera1.setPerspectiveFrustum(60, 2, 1, 1000);
 camera1.setViewport(0.0, 1.0, 0.0, 0.45);
 player2 = new Cube("PLAYER2");
 player2.updateWorldBound();
 player2.translate(30, 1, 0);
 player2.rotate(0, new Vector3D(0, 1, 0));
 addGameWorldObject(player2);
 camera2 = new JOGLCamera(renderer);
 camera2.setPerspectiveFrustum(60, 2, 1, 1000);
 camera2.setViewport(0.0, 1.0, 0.55, 1.0);
//  System.out.println(camera2.getViewDirection());
 
 createPlayerHUDs();
 }
 
 private void createPlayerHUDs()
 {
 
 timeStringP1 = new HUDString("Time = " + timeP1);
 HUDString player1ID = new HUDString("Player1");
 scoreStringP1 = new HUDString("Plants Collected: "+ scoreP1);
 player1ID.setName("Player1ID");
 player1ID.setLocation(.01, .20);
 player1ID.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
 player1ID.setColor(Color.blue);
 player1ID.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
 timeStringP1.setLocation(.01,.10);
 scoreStringP1.setLocation(.011,.01);
 camera1.addToHUD(player1ID);
 camera1.addToHUD(timeStringP1);
 camera1.addToHUD(scoreStringP1);
 timeStringP2 = new HUDString("Time = " + timeP2);
 HUDString player2ID = new HUDString("Player2");
 scoreStringP2 = new HUDString("Plants Collected: "+ scoreP1);
 player2ID.setName("Player2ID");
 player2ID.setLocation(0.01, .20);
 player2ID.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
 player2ID.setColor(Color.yellow);
 player2ID.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
 timeStringP2.setLocation(.01,.10);
 scoreStringP2.setLocation(.011,.01);
 camera2.addToHUD(player2ID);
 camera2.addToHUD(timeStringP2);
 camera2.addToHUD(scoreStringP2);
 }
 

private void initInput()
 {
 
 String mouseName = inputMgr.getMouseName();
 String gpName = inputMgr.getFirstGamepadName();
 Controller keyBind = inputMgr.getKeyboardController(1);
 Controller mouseBind = inputMgr.getMouseController(1);
 IAction quitGame = new QuitGameAction(this); 
// System.out.println(inputMgr.getControllers());
 String test = mouseBind.toString();
 //System.out.println(test);
 // this example uses the built-in SAGE 3P camera controller
 cam1Controller =
 new ThirdPersonCameraController(camera1,player1,inputMgr,
 test);
 cam2Controller =
 new Camera3Pcontroller(camera2, player2, inputMgr,
 gpName);
 // player 1 additional actions
 IAction moveForward1 = new MoveForwardAction(player1);
 
 inputMgr.associateAction(keyBind,
 net.java.games.input.Component.Identifier.Key.W,
 moveForward1, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 IAction moveBackward1 = new MoveBackwardAction(player1);
 player1.updateWorldBound();
 inputMgr.associateAction(keyBind,
 net.java.games.input.Component.Identifier.Key.S,
 moveBackward1, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 IAction moveLeft1 = new MoveLeftAction(player1);
 player1.updateWorldBound();
 inputMgr.associateAction(keyBind,
 net.java.games.input.Component.Identifier.Key.D,
 moveLeft1, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 IAction moveRight1 = new MoveRightAction(player1);
 player1.updateWorldBound();
 inputMgr.associateAction(keyBind,
 net.java.games.input.Component.Identifier.Key.A,
 moveRight1, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 // player 2 additional actions
 IAction moveForward2 = new MoveForwardAction(player2);
 inputMgr.associateAction(gpName,
 net.java.games.input.Component.Identifier.Button._3,
 moveForward2, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 IAction moveBackward2 = new MoveBackwardAction(player2);
 inputMgr.associateAction(gpName,
 net.java.games.input.Component.Identifier.Button._0,
 moveBackward2, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 IAction moveLeft2 = new MoveLeftAction(player2);
 player1.updateWorldBound();
 inputMgr.associateAction(gpName,
		 net.java.games.input.Component.Identifier.Button._1,
 moveLeft2, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 IAction moveRight2 = new MoveRightAction(player2);
 player2.updateWorldBound();
 inputMgr.associateAction(gpName,
		 net.java.games.input.Component.Identifier.Button._2,
 moveRight2, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 // additional actions go here
 
 
 inputMgr.associateAction (keyBind, net.java.games.input.Component.Identifier.Key.ESCAPE,
		 quitGame, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
 }





protected void update(float time)
{
	
	//System.out.println(player1.getWorldTranslation());
	//System.out.println(camera1.getLocation());
	
	
	if (spher.getWorldBound().intersects(player1.getWorldBound())) { 
		 numCrashes++;
		 CrashEvent newCrash = new CrashEvent(numCrashes);
		 eventMgr.triggerEvent(newCrash);
		 myPlants.removeChild(spher);
		 scoreP1= scoreP1 +2;
		 spher = new Sphere();
		 Matrix3D sphM = spher.getLocalTranslation();
		 sphM.translate(randomNum(),1,randomNum());
		 spher.setLocalTranslation(sphM);
		 myPlants.addChild(spher);
		 spher.updateWorldBound();
		 spher2 = new Sphere();
		 Matrix3D sphM2 = spher2.getLocalTranslation();
		 sphM2.translate(1,boxCountP1,-5);
		 spher2.setLocalTranslation(sphM2);
		 myPlants.addChild(spher2);
		 spher2.updateWorldBound();
		 boxCountP1++;

		 }
	
	if (aPyr.getWorldBound().intersects(player1.getWorldBound()))
	 { numCrashes++;
	 CrashEvent newCrash = new CrashEvent(numCrashes);
	 eventMgr.triggerEvent(newCrash);
	 myPlants.removeChild(aPyr);
	 scoreP1++;
	 aPyr = new MyPyramid();
	 Matrix3D pyrM = aPyr.getLocalTranslation();
	 pyrM.translate(randomNum(),1,randomNum());
	 aPyr.setLocalTranslation(pyrM);
	 myPlants.addChild(aPyr);
	 //addGameWorldObject(aPyr);
	 aPyr.updateWorldBound();
	 aPyr4 = new MyPyramid();
	 Matrix3D pyrM4 = aPyr4.getLocalTranslation();
	 pyrM4.translate(1,boxCountP1,-5);
	 aPyr4.setLocalTranslation(pyrM4);
	 myPlants.addChild(aPyr4);
	 //addGameWorldObject(aPyr4);
	 aPyr4.updateWorldBound();
	 boxCountP1++;
	 }
	
	if (aPyr2.getWorldBound().intersects(player1.getWorldBound()))
	 { numCrashes++;
	 CrashEvent newCrash = new CrashEvent(numCrashes);
	 eventMgr.triggerEvent(newCrash);
	 myPlants.removeChild(aPyr2);
	 scoreP1++;
	 aPyr2 = new MyPyramid();
	 Matrix3D pyrM2 = aPyr2.getLocalTranslation();
	 pyrM2.translate(randomNum(),1,randomNum());
	 aPyr2.setLocalTranslation(pyrM2);
	 myPlants.addChild(aPyr2);
	 // addGameWorldObject(aPyr2);
	 aPyr2.updateWorldBound();
	 aPyr5 = new MyPyramid();
	 Matrix3D pyrM5 = aPyr5.getLocalTranslation();
	 pyrM5.translate(1,boxCountP1,-5);
	 aPyr5.setLocalTranslation(pyrM5);
	 myPlants.addChild(aPyr5);
	 // addGameWorldObject(aPyr5);
	 aPyr5.updateWorldBound();
	 boxCountP1++;
	 
	 
	 }
	
	if (aPyr3.getWorldBound().intersects(player1.getWorldBound()))
	 { 
		
	 numCrashes++;
	 scoreP1++;
	 CrashEvent newCrash = new CrashEvent(numCrashes);
	 eventMgr.triggerEvent(newCrash);
	 myPlants.removeChild(aPyr3);
	 aPyr3 = new MyPyramid();
	 Matrix3D pyrM3 = aPyr3.getLocalTranslation();
	 pyrM3.translate(randomNum(),1,randomNum());
	 aPyr3.setLocalTranslation(pyrM3);
	 myPlants.addChild(aPyr3);
	 aPyr3.updateWorldBound();
	 aPyr6 = new MyPyramid();
	 Matrix3D pyrM6 = aPyr6.getLocalTranslation();
	 pyrM6.translate(1,boxCountP1,-5);
	 aPyr6.setLocalTranslation(pyrM6);
	 myPlants.addChild(aPyr6);
	 //addGameWorldObject(aPyr6);
	 aPyr6.updateWorldBound();
	 boxCountP1++;
	 }
	
	if (spher.getWorldBound().intersects(player2.getWorldBound())) { 
		 numCrashes++;
		 CrashEvent newCrash = new CrashEvent(numCrashes);
		 eventMgr.triggerEvent(newCrash);
		 myPlants.removeChild(spher);
		 scoreP2= scoreP2 +2;
		 spher = new Sphere();
		 Matrix3D sphM = spher.getLocalTranslation();
		 sphM.translate(randomNum(),1,randomNum());
		 spher.setLocalTranslation(sphM);
		 myPlants.addChild(spher);
		 spher.updateWorldBound();
		 spher2 = new Sphere();
		 Matrix3D sphM2 = spher2.getLocalTranslation();
		 sphM2.translate(1,boxCountP1,-5);
		 spher2.setLocalTranslation(sphM2);
		 myPlants.addChild(spher2);
		 spher2.updateWorldBound();
		 boxCountP2++;

		 }
	if (aPyr.getWorldBound().intersects(player2.getWorldBound()))
	 { numCrashes++;
	 CrashEvent newCrash = new CrashEvent(numCrashes);
	 eventMgr.triggerEvent(newCrash);
	 myPlants.removeChild(aPyr);
	 scoreP2++;
	 aPyr = new MyPyramid();
	 Matrix3D pyrM = aPyr.getLocalTranslation();
	 pyrM.translate(randomNum(),1,randomNum());
	 aPyr.setLocalTranslation(pyrM);
	 myPlants.addChild(aPyr);
	 aPyr.updateWorldBound();
	 aPyr4 = new MyPyramid();
	 Matrix3D pyrM4 = aPyr4.getLocalTranslation();
	 pyrM4.translate(1,boxCountP1,-5);
	 aPyr4.setLocalTranslation(pyrM4);
	 myPlants.addChild(aPyr4);
	 aPyr4.updateWorldBound();
	 boxCountP2++;
	 }
	if (aPyr2.getWorldBound().intersects(player2.getWorldBound()))
	 { numCrashes++;
	 CrashEvent newCrash = new CrashEvent(numCrashes);
	 eventMgr.triggerEvent(newCrash);
	 myPlants.removeChild(aPyr2);
	 scoreP2++;
	 aPyr2 = new MyPyramid();
	 Matrix3D pyrM2 = aPyr2.getLocalTranslation();
	 pyrM2.translate(randomNum(),1,randomNum());
	 aPyr2.setLocalTranslation(pyrM2);
	 myPlants.addChild(aPyr2);
	 aPyr2.updateWorldBound();
	 aPyr5 = new MyPyramid();
	 Matrix3D pyrM5 = aPyr5.getLocalTranslation();
	 pyrM5.translate(1,boxCountP1,-5);
	 aPyr5.setLocalTranslation(pyrM5);
	 myPlants.addChild(aPyr5);
	 aPyr5.updateWorldBound();
	 boxCountP2++;
	 
	 
	 }
	if (aPyr3.getWorldBound().intersects(player2.getWorldBound()))
	 { 
		
	 numCrashes++;
	 CrashEvent newCrash = new CrashEvent(numCrashes);
	 eventMgr.triggerEvent(newCrash);
	 myPlants.removeChild(aPyr3);
	 aPyr3 = new MyPyramid();
	 scoreP2++;
	 Matrix3D pyrM3 = aPyr3.getLocalTranslation();
	 pyrM3.translate(randomNum(),1,randomNum());
	 aPyr3.setLocalTranslation(pyrM3);
	 myPlants.addChild(aPyr3);
	 aPyr3.updateWorldBound();
	 aPyr6 = new MyPyramid();
	 Matrix3D pyrM6 = aPyr6.getLocalTranslation();
	 pyrM6.translate(1,boxCountP1,-5);
	 aPyr6.setLocalTranslation(pyrM6);
	 myPlants.addChild(aPyr6);
	 aPyr6.updateWorldBound();
	 boxCountP2++;
	 }
	
	//System.out.println(boxCountP1);
// scoreString.setText("Plants Collected = " + score);
	scoreStringP1.setText("Plants Collected = " + scoreP1);
	scoreStringP2.setText("Plants Collected = " + scoreP2);
 DecimalFormat df = new DecimalFormat("0.0");
 if (scoreP1 == 10 || scoreP1 == 11){
		 timeStringP1.setText("Final Time = " +  df.format(timeP1/1000));
 }else{
	 timeP1 += time;
	 
	 
	 
	timeStringP1.setText("Time = " + df.format(timeP1/1000));
 }	
 if (scoreP2 == 10 || scoreP2 == 11){
	 timeStringP2.setText("Final Time = " +  df.format(timeP2/1000));
}else{
 timeP2 += time;
 
 
 
timeStringP2.setText("Time = " + df.format(timeP2/1000));
}	
	
plantController = new MyPlantsController();
plantController.addControlledNode(myPlants);

truckController = new MyTruckController();
truckController.addControlledNode(myTruckGroup);


myPlants.addController(plantController);
myTruckGroup.addController(truckController);
cam1Controller.update(time);
cam2Controller.update(time);
super.update(time);


}

private void createScene()
{
// insertion of other game objects and axes go here
	 
	myPlants = new Group("root");
	myTruckGroup = new Group("root2");
	//Pyramid 1
	 aPyr = new MyPyramid();
	 Matrix3D pyrM = aPyr.getLocalTranslation();
	 pyrM.translate(randomNum(),1,randomNum());
	 aPyr.setLocalTranslation(pyrM);
	// addGameWorldObject(aPyr);
	// aPyr.updateWorldBound();
	 
	 
	 //Pyramid 2
	 aPyr2 = new MyPyramid();
	 Matrix3D pyrM2 = aPyr2.getLocalTranslation();
	 pyrM2.translate(randomNum(),1,randomNum());
	 aPyr2.setLocalTranslation(pyrM2);
	// addGameWorldObject(aPyr2);
	// aPyr2.updateWorldBound();
	 
	 //Pyramid 3
	 aPyr3 = new MyPyramid();
	 Matrix3D pyrM3 = aPyr3.getLocalTranslation();
	 pyrM3.translate(randomNum(),1,randomNum());
	 aPyr3.setLocalTranslation(pyrM3);
	 //addGameWorldObject(aPyr3);
//	 aPyr3.updateWorldBound();
	 
	 //My truck, implemented through MyTruck.java
	 myTruck = new MyTruck();
	 Matrix3D truckM = myTruck.getLocalTranslation();
	 truckM.translate(2,2,-5);
	 
	 myTruck.setLocalTranslation(truckM);
	 myTruckGroup.addChild(myTruck);
	// addGameWorldObject(myTruck);
	 
	 //Sage Sphere
	 spher = new Sphere();
	 Matrix3D sphM = spher.getLocalTranslation();
	 sphM.translate(randomNum(),1,randomNum());
	 spher.setLocalTranslation(sphM);
	// addGameWorldObject(spher);
	 spher.updateWorldBound(); 
	 
	 myPlants.addChild(aPyr);
	 myPlants.addChild(aPyr2);
	 myPlants.addChild(aPyr3);
	 myPlants.addChild(spher);
	
	 addGameWorldObject(myTruckGroup);
	 addGameWorldObject(myPlants);
	 SceneNode test = myPlants.getChild("spher");
	 spher.updateWorldBound();
	 aPyr.updateWorldBound();
	 aPyr2.updateWorldBound();
	 aPyr3.updateWorldBound();
	 
	 rect = new Rectangle("GROUND FLOOR");
	 rect.translate(0, 0, 0);
	 Color myColor = new Color(168,101,1);
	 rect.setColor(myColor);
	 rect.scale(100,100,100);
	 rect.rotate(90, new Vector3D(1, 0, 0));
	 addGameWorldObject(rect);
	 Point3D origin = new Point3D(0,0,0);
	 Point3D xEnd = new Point3D(1000,0,0);
	 Point3D yEnd = new Point3D(0,1000,0);
	 Point3D zEnd = new Point3D(0,0,1000);
	 Line xAxis = new Line (origin, xEnd, Color.red, 2);
	 Line yAxis = new Line (origin, yEnd, Color.green, 2);
	 Line zAxis = new Line (origin, zEnd, Color.blue, 2);
	 addGameWorldObject(xAxis); addGameWorldObject(yAxis);
	 addGameWorldObject(zAxis); 
	 eventMgr.addListener(myTruck, CrashEvent.class);
	 
}
public int randomNum(){
	 randomNumber = min + (int)(Math.random() * max); 
	return randomNumber;
	 
	 
}
// split screen requires overriding the default basegame renderer
protected void render()
{
renderer.setCamera(camera1);
super.render();
renderer.setCamera(camera2);
super.render();
}
public static void main (String [] args)
{ new Starter().start(); }
}