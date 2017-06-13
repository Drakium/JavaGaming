package myGameEngine;
import sage.app.BaseGame;
import sage.camera.ICamera;
import sage.scene.SceneNode;
import sage.scene.shape.Line;
import sage.scene.shape.Teapot;
import sage.display.*;
import sage.input.*;
import sage.input.action.*;
import sage.display.DisplaySystem;
import sage.event.IEventManager;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import net.java.games.input.*;
import java.awt.Color;

public class MoveYAxis extends AbstractInputAction
{
 private ICamera camera;
 private SetSpeedAction runAction;
 public MoveYAxis(ICamera c, SetSpeedAction s)
 { camera = c;
 runAction = s;
 }
 public void performAction(float time, net.java.games.input.Event e)
 {
	 float moveAmount;
   if (runAction.isRunning())
	 { moveAmount = (float) 0.5  ; }
	else
	{ moveAmount = (float) 0.1  ; }
 Vector3D newLocVector = new Vector3D();
 Vector3D viewDir = camera.getViewDirection().normalize();
 Vector3D curLocVector = new Vector3D(camera.getLocation());
 if (e.getValue() < -0.2)
 { newLocVector = curLocVector.add(viewDir.mult(moveAmount * time)); }
 else { if (e.getValue() > 0.2)
 { newLocVector = curLocVector.minus(viewDir.mult(moveAmount * time)); }
 else { newLocVector = curLocVector; }
 }
 //create a point for the new location
 double newX = newLocVector.getX();
 double newY = newLocVector.getY();
 double newZ = newLocVector.getZ();
 Point3D newLoc = new Point3D(newX, newY, newZ);
 camera.setLocation(newLoc);
} }