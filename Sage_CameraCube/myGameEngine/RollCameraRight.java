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

public class RollCameraRight extends AbstractInputAction
{

	private ICamera camera;
	private SetSpeedAction runAction;
	public RollCameraRight(ICamera c, SetSpeedAction r)
	{ camera = c;
	runAction = r;
	}
	
	@Override
	public void performAction(float time, Event e) {
		float moveAmount;
		Matrix3D rotationAmt = new Matrix3D();
		
		if (runAction.isRunning())
		 { moveAmount = (float) 0.2  ; }
		else
		{ moveAmount = (float) 0.05  ; }
		
		// TODO Auto-generated method stub
		
		
		rotationAmt.rotate(moveAmount, camera.getViewDirection());
		 
				camera.setUpAxis(camera.getUpAxis().mult(rotationAmt).normalize());
				camera.setRightAxis(camera.getRightAxis().mult(rotationAmt).normalize());

        
	 }

		
		
		

		
		
	}
  