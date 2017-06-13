//Christian Solano A1
//CSC 165
//SpaceFarming3D

package a1;

//Imports
import sage.app.BaseGame;
import sage.camera.ICamera;
import sage.scene.HUDString;
import sage.scene.SceneNode;
import sage.scene.shape.Line;
import sage.scene.shape.Teapot;
import sage.display.*;
import sage.input.*;
import sage.input.action.*;
import sage.display.DisplaySystem;
import sage.event.EventManager;
import sage.event.IEventManager;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import sage.scene.shape.Sphere;
import net.java.games.input.*;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

//My file imports for camera
import myGameEngine.*;


 
public class Starter extends BaseGame
{
 
 IDisplaySystem display;
 ICamera camera;
 IInputManager im;
 IEventManager eventMgr;
 
 String gpName;
 MyPyramid aPyr,aPyr2,aPyr3,aPyr4,aPyr5,aPyr6;
 Teapot teap;
 MyTruck myTruck;
 Sphere spher,spher2;
 private int randomNumber;
 private int score = 0;
 private float time = 0; // game elapsed time
 private float fTime = 0;
 private HUDString scoreString ;
 private HUDString timeString ;
 private HUDString finalTime;
 int max = 25;
 int min = -10;
 int numCrashes = 0;
 int boxCount = 1;
 int boxSwitch = 0;
 protected void initGame()
 { 
 
 //Picks up keyboard and gamepad
 eventMgr = EventManager.getInstance();
 initGameObjects();
 im = getInputManager();
 gpName = im.getFirstGamepadName();
 String myKey = "Razer BlackWidow Ultimate";
 ArrayList<Controller> test = im.getControllers();
 String kbName = im.getKeyboardName();
 Controller keyBind = im.getKeyboardController(1);
 
 SetSpeedAction setSpeed = new SetSpeedAction();
 
 //Actions for camera movement with keyboard
 ForwardAction mvForward = new ForwardAction(camera, setSpeed);
 BackwardAction mvBack = new BackwardAction(camera, setSpeed);
 MoveLeft mvLeft = new MoveLeft(camera,setSpeed);
 MoveRight mvRight = new MoveRight(camera,setSpeed);
 RotateLeft rotLeft = new RotateLeft(camera,setSpeed);
 RotateRight rotRight = new RotateRight(camera,setSpeed);
 RotateUp rotUp = new RotateUp(camera,setSpeed);
 RotateDown rotDown = new RotateDown(camera,setSpeed);
 
 //Actions for camera movement with gamepad controller
 MoveRXAxis rotateX = new MoveRXAxis(camera, setSpeed);
 MoveRYAxis rotateY = new MoveRYAxis(camera, setSpeed);
 RollCameraLeft rollCamLeft = new RollCameraLeft(camera,setSpeed);
 RollCameraRight rollCamRight = new RollCameraRight(camera,setSpeed);
 MoveXAxis moveSides = new MoveXAxis(camera,setSpeed);

 
 IAction quitGame = new QuitGameAction(this); 
 System.out.println("\n" + keyBind);
 System.out.println(gpName);
 
 //if (gpName != null) {
 //Controller Camera Movements
 im.associateAction(gpName,
		 net.java.games.input.Component.Identifier.Axis.Y,
		 mvForward,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 im.associateAction(gpName,
		 net.java.games.input.Component.Identifier.Axis.X,
		 moveSides,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 

 im.associateAction(gpName,
		 net.java.games.input.Component.Identifier.Axis.RX,
		 rotateX,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

 im.associateAction(gpName,
		 net.java.games.input.Component.Identifier.Axis.RY,
		 rotateY,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 

 im.associateAction(gpName,
		 net.java.games.input.Component.Identifier.Button._4,
		 rollCamLeft,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 im.associateAction(gpName,
		 net.java.games.input.Component.Identifier.Button._5,
		 rollCamRight,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
// }
 
 //Keyboard Camera Movements;
 im.associateAction(keyBind,
		 net.java.games.input.Component.Identifier.Key.S,
		 mvForward,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 im.associateAction(keyBind,
		 net.java.games.input.Component.Identifier.Key.W,
		 mvBack,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 im.associateAction(keyBind,
		 net.java.games.input.Component.Identifier.Key.A,
		 mvLeft,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 im.associateAction(keyBind,
		 net.java.games.input.Component.Identifier.Key.D,
		 mvRight,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 im.associateAction(keyBind,
		 net.java.games.input.Component.Identifier.Key.UP,
		 rotUp,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

 im.associateAction(keyBind,
		 net.java.games.input.Component.Identifier.Key.DOWN,
		 rotDown,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 im.associateAction(keyBind,
		 net.java.games.input.Component.Identifier.Key.LEFT,
		 rotLeft,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 im.associateAction(keyBind,
		 net.java.games.input.Component.Identifier.Key.RIGHT,
		 rotRight,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 

 im.associateAction(keyBind,
		 net.java.games.input.Component.Identifier.Key.Q,
		 rollCamLeft,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 im.associateAction(keyBind,
		 net.java.games.input.Component.Identifier.Key.E,
		 rollCamRight,
		 IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
 
 
 //Quit game
 im.associateAction (gpName, net.java.games.input.Component.Identifier.Button._7,
 quitGame, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
 
 im.associateAction (keyBind, net.java.games.input.Component.Identifier.Key.ESCAPE,
 quitGame, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
 
 
 
 timeString = new HUDString("Time = " + time);
 
 timeString.setLocation(0,0.05); // (0,0) [lower-left] to (1,1)
 addGameWorldObject(timeString);
 scoreString = new HUDString ("Plants Collected " + score); //default is (0,0)
 addGameWorldObject(scoreString);
 
 }
 public void update(float elapsedTimeMS)
 
 //Creates the sphere from sage and checks for crashes
 { if (spher.getWorldBound().contains(camera.getLocation())) { 
 numCrashes++;
 CrashEvent newCrash = new CrashEvent(numCrashes);
 eventMgr.triggerEvent(newCrash);
 removeGameWorldObject(spher);
 score= score +2;
 spher = new Sphere();
 Matrix3D sphM = spher.getLocalTranslation();
 sphM.translate(randomNum(),randomNum(),randomNum());
 spher.setLocalTranslation(sphM);
 addGameWorldObject(spher);
 spher.updateWorldBound();
 spher2 = new Sphere();
 Matrix3D sphM2 = spher2.getLocalTranslation();
 sphM2.translate(1,boxCount,-5);
 spher2.setLocalTranslation(sphM2);
 addGameWorldObject(spher2);
 spher2.updateWorldBound();
 boxCount++;;
 
 }
 
 //Creates the pyramids for score and checks for crashes
 //Removes the object that is crashed from camera and places into trucks
 if (aPyr.getWorldBound().contains(camera.getLocation()))
 { numCrashes++;
 CrashEvent newCrash = new CrashEvent(numCrashes);
 eventMgr.triggerEvent(newCrash);
 removeGameWorldObject(aPyr);
 score++;
 aPyr = new MyPyramid();
 Matrix3D pyrM = aPyr.getLocalTranslation();
 pyrM.translate(randomNum(),randomNum(),randomNum());
 aPyr.setLocalTranslation(pyrM);
 addGameWorldObject(aPyr);
 aPyr.updateWorldBound();
 aPyr4 = new MyPyramid();
 Matrix3D pyrM4 = aPyr4.getLocalTranslation();
 pyrM4.translate(1,boxCount,-5);
 aPyr4.setLocalTranslation(pyrM4);
 addGameWorldObject(aPyr4);
 boxCount++;
 }
 if (aPyr2.getWorldBound().contains(camera.getLocation()))
 { numCrashes++;
 CrashEvent newCrash = new CrashEvent(numCrashes);
 eventMgr.triggerEvent(newCrash);
 removeGameWorldObject(aPyr2);
 score++;
 aPyr2 = new MyPyramid();
 Matrix3D pyrM2 = aPyr2.getLocalTranslation();
 pyrM2.translate(randomNum(),randomNum(),randomNum());
 aPyr2.setLocalTranslation(pyrM2);
 addGameWorldObject(aPyr2);
 aPyr2.updateWorldBound();
 aPyr5 = new MyPyramid();
 Matrix3D pyrM5 = aPyr5.getLocalTranslation();
 pyrM5.translate(1,boxCount,-5);
 aPyr5.setLocalTranslation(pyrM5);
 addGameWorldObject(aPyr5);
 boxCount++;
 
 
 }
 if (aPyr3.getWorldBound().contains(camera.getLocation()))
 { numCrashes++;
 CrashEvent newCrash = new CrashEvent(numCrashes);
 eventMgr.triggerEvent(newCrash);
 removeGameWorldObject(aPyr3);
 score++;
 aPyr3 = new MyPyramid();
 Matrix3D pyrM3 = aPyr3.getLocalTranslation();
 pyrM3.translate(randomNum(),randomNum(),randomNum());
 aPyr3.setLocalTranslation(pyrM3);
 addGameWorldObject(aPyr3);
 aPyr3.updateWorldBound();
 aPyr6 = new MyPyramid();
 Matrix3D pyrM6 = aPyr6.getLocalTranslation();
 pyrM6.translate(1,boxCount,-5);
 aPyr6.setLocalTranslation(pyrM6);
 addGameWorldObject(aPyr6);
 boxCount++;
 }
 
 
 
//update the HUD
 scoreString.setText("Plants Collected = " + score);
 DecimalFormat df = new DecimalFormat("0.0");
 if (score == 10 || score == 11){
	 timeString.setText("Final Time = " +  df.format(time/1000));
 }else{
 time += elapsedTimeMS;
 
 
 
 timeString.setText("Time = " + df.format(time/1000));
 }
 // tell BaseGame to update game world state
 super.update(elapsedTimeMS);
 }
 private void initGameObjects()
 { display = getDisplaySystem();
 camera = display.getRenderer().getCamera();
 camera.setPerspectiveFrustum(45, 1, 0.01, 1000);

 camera.setLocation(new Point3D(3, 2, 40));
 
 //Pyramid 1
 aPyr = new MyPyramid();
 Matrix3D pyrM = aPyr.getLocalTranslation();
 pyrM.translate(randomNum(),randomNum(),randomNum());
 aPyr.setLocalTranslation(pyrM);
 addGameWorldObject(aPyr);
 aPyr.updateWorldBound();
 
 
 //Pyramid 2
 aPyr2 = new MyPyramid();
 Matrix3D pyrM2 = aPyr2.getLocalTranslation();
 pyrM2.translate(randomNum(),randomNum(),randomNum());
 aPyr2.setLocalTranslation(pyrM2);
 addGameWorldObject(aPyr2);
 aPyr2.updateWorldBound();
 
 //Pyramid 3
 aPyr3 = new MyPyramid();
 Matrix3D pyrM3 = aPyr3.getLocalTranslation();
 pyrM3.translate(randomNum(),randomNum(),randomNum());
 aPyr3.setLocalTranslation(pyrM3);
 addGameWorldObject(aPyr3);
 aPyr3.updateWorldBound();
 
 //My truck, implemented through MyTruck.java
 myTruck = new MyTruck();
 Matrix3D truckM = myTruck.getLocalTranslation();
 truckM.translate(2,1,-5);
 
 myTruck.setLocalTranslation(truckM);
 addGameWorldObject(myTruck);
 
 //Sage Sphere
 spher = new Sphere();
 Matrix3D sphM = spher.getLocalTranslation();
 sphM.translate(randomNum(),randomNum(),randomNum());
 spher.setLocalTranslation(sphM);
 addGameWorldObject(spher);
 spher.updateWorldBound();

 eventMgr.addListener(myTruck, CrashEvent.class);
 
 
 Point3D origin = new Point3D(0,0,0);
 Point3D xEnd = new Point3D(1000,0,0);
 Point3D yEnd = new Point3D(0,1000,0);
 Point3D zEnd = new Point3D(0,0,1000);
 Line xAxis = new Line (origin, xEnd, Color.red, 2);
 Line yAxis = new Line (origin, yEnd, Color.green, 2);
 Line zAxis = new Line (origin, zEnd, Color.blue, 2);
 addGameWorldObject(xAxis); addGameWorldObject(yAxis);
 addGameWorldObject(zAxis); 
 
 }
 public int randomNum(){
	 randomNumber = min + (int)(Math.random() * max); 
	return randomNumber;
	 
	 
 }
 public static void main (String [] args)
 { new Starter().start(); }
}

