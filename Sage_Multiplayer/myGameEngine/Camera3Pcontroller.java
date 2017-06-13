package myGameEngine;

import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Event;
import sage.camera.ICamera;
import sage.input.IInputManager;
import sage.input.action.AbstractInputAction;
import sage.input.action.IAction;
import sage.scene.SceneNode;
import sage.util.MathUtils;

public class Camera3Pcontroller {
	private ICamera cam; // the camera being controlled
	private SceneNode target; // the target the camera looks at
	private float cameraAzimuth = 0; // rotation of camera around target Y axis
	private float cameraElevation; // elevation of camera above target
	private float cameraDistanceFromTarget;
	private Point3D targetPos; // avatar’s position in the world
	private Vector3D worldUpVec,worldLeftVec,worldRightVec;
	
	private float speed = 0.15f;
	private boolean orbitFlag = false;

	public Camera3Pcontroller(ICamera cam, SceneNode target, 
			IInputManager inputMgr, String controllerName//, 
			//float azimuth
			) {
		this.cam = cam;
		this.target = target;
		worldUpVec = new Vector3D(0, 1, 0);
		worldLeftVec = new Vector3D(1,0,0);
		worldRightVec = new Vector3D(-1,0,0);
		cameraDistanceFromTarget = 5.0f;
		cameraAzimuth = 180;//azimuth; // start from BEHIND and ABOVE the target
		cameraElevation = 20.0f; // elevation is in degrees
		update(0.0f); // initialize camera state
		setupInput(inputMgr, controllerName);
	}

	public void update(float time) {
		updateTarget();
		updateCameraPosition();
		cam.lookAt(targetPos, worldUpVec); // SAGE built-in function
	}

	private void updateTarget() {
		targetPos = new Point3D(target.getWorldTranslation().getCol(3));
	}

	private void updateCameraPosition() {
		double theta = cameraAzimuth;
		double phi = cameraElevation;
		double r = cameraDistanceFromTarget;
		
		// calculate new camera position in Cartesian coords
		Point3D relativePosition = MathUtils.sphericalToCartesian(theta, phi, r);
		Point3D desiredCameraLoc = relativePosition.add(targetPos);
		cam.setLocation(desiredCameraLoc);
	}

	private void setupInput(IInputManager im, String cn) {
		IAction orbitAction = new OrbitAroundAction();
		IAction zoomAction = new ZoomAround();
		IAction orbitPlayerAction = new OrbitPlayer();
		IAction orbitPlayerAroundAction = new OrbitPlayerAround();
		
		im.associateAction(cn, Identifier.Axis.Z, zoomAction, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(cn, Identifier.Axis.X, orbitAction, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(cn, Identifier.Axis.RY,orbitPlayerAction , IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(cn, Identifier.Axis.RX,orbitPlayerAroundAction , IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	}
	//Move player in x axis
	private class OrbitAroundAction extends AbstractInputAction {
		public void performAction(float time, Event evt) {
			float rotAmount = 0;
			
			 if (evt.getValue() < -0.2) { rotAmount=-0.6f; }
			 else { if (evt.getValue() > 0.2) { rotAmount=0.6f; }
			 else { rotAmount=0.0f; }
			 }
			 
			cameraAzimuth += rotAmount;
			cameraAzimuth = cameraAzimuth % 360;
			
			if (orbitFlag == false) {
				target.rotate(rotAmount, worldUpVec);
			}
		}

	}
	//Move Camera Left and right around player
	private class OrbitPlayerAround extends AbstractInputAction{
		public void performAction(float time, Event e) {
			float zoomAmount = 0.4f;
			
			//	System.out.println(zoomAmount);
					if (e.getValue() < -0.2) {
						zoomAmount = -zoomAmount;
						cameraAzimuth = cameraAzimuth +zoomAmount;
						cameraAzimuth = cameraAzimuth % 360;
						
					} else if (e.getValue() > 0.2) {
						zoomAmount = -zoomAmount;
						cameraAzimuth = cameraAzimuth -zoomAmount;
						cameraAzimuth = cameraAzimuth % 360;
					}
					
			
					
			
		}
	}
	//Move Camera up and down around player
	private class OrbitPlayer extends AbstractInputAction{
		public void performAction(float time, Event e) {
			float zoomAmount = 0.4f;
			
			//	System.out.println(zoomAmount);
					if (e.getValue() < -0.2) {
						zoomAmount = -zoomAmount;
						cameraElevation = cameraElevation +zoomAmount;
					} else if (e.getValue() > 0.2) {
						zoomAmount = -zoomAmount;
						cameraElevation = cameraElevation -zoomAmount;
					}
					
			
					
			
		}
	}
	//ForwardBack Zoom
	private class ZoomAround extends AbstractInputAction{
		public void performAction(float time, Event e) {
			float zoomAmount = 0.1f;
			
				
					if (e.getValue() < -0.2) {
						zoomAmount = -zoomAmount;
						cameraDistanceFromTarget = cameraDistanceFromTarget +zoomAmount;
					} else if (e.getValue() > 0.2) {
						zoomAmount = -zoomAmount;
						cameraDistanceFromTarget = cameraDistanceFromTarget -zoomAmount;
					}
					
			
			
			
		}
	}
}
