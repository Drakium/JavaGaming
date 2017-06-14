package myGameEngine;
import sage.app.BaseGame;
import sage.camera.ICamera;
import sage.scene.SceneNode;
import sage.display.*;
import sage.input.*;
import sage.input.action.*;
import sage.display.DisplaySystem;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import net.java.games.input.*;

public class BackwardAction extends AbstractInputAction
{ private ICamera camera;
 private SetSpeedAction runAction;
 public BackwardAction(ICamera c, SetSpeedAction r)
 { camera = c;
 runAction = r;
 }
 public void performAction(float time, Event e)
 { 
 float moveAmount ;
 Vector3D viewDir = camera.getViewDirection().normalize();
 Vector3D curLocVector = new Vector3D(camera.getLocation());
 Vector3D newLocVec = new Vector3D();
 
 if (runAction.isRunning())
 { moveAmount = (float) 0.5  ; }
else
{ moveAmount = (float) 0.1  ; }
 
 if (e.getValue() < -0.2)
 { newLocVec = curLocVector.minus(viewDir.mult(moveAmount * time)); }
 else { if (e.getValue() > 0.2)
 { newLocVec = curLocVector.add(viewDir.mult(moveAmount * time)); }
 else { newLocVec = curLocVector; }
 }
 
// Vector3D viewDir = camera.getViewDirection().normalize();
// Vector3D curLocVector = new Vector3D(camera.getLocation());
// Vector3D newLocVec = curLocVector.add(viewDir.mult(moveAmount));
 
 
 double newX = newLocVec.getX();
 double newY = newLocVec.getY();
 double newZ = newLocVec.getZ();
 Point3D newLoc = new Point3D(newX, newY, newZ);
 camera.setLocation(newLoc);
 }
}