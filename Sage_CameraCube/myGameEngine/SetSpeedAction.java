package myGameEngine;
import sage.app.BaseGame;
import sage.camera.ICamera;
import sage.scene.SceneNode;
import sage.display.*;
import sage.input.*;
import sage.input.action.*;
import sage.display.DisplaySystem;
import graphicslib3D.Point3D;
import net.java.games.input.*;
public class SetSpeedAction extends AbstractInputAction
{ private boolean running = false;
 public boolean isRunning() { return running; }
 public void performAction(float time, Event event)
 { System.out.println("changed"); running = !running; }
}