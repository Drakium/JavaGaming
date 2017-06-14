/*
* Author: Beau Derrick and Christian Solano
* Date: 5/10/17
*/
package a3;

 import sage.scene.SceneNode;
 import sage.camera.ICamera;
 import sage.input.IInputManager;
 import sage.input.action.AbstractInputAction;
 import sage.input.action.IAction;        
 import graphicslib3D.Point3D;
 import graphicslib3D.Vector3D;
 import sage.util.MathUtils;  
 import net.java.games.input.Event;

/**
 * A orbit camera controller
 */
public class OrbitController
{ 
  private ICamera cam;                    
//the camera being controlled
  private SceneNode target;           
//the target the camera looks at
  private float cameraAzimuth = 0.0f;      
//rotation of camera around target Y axis
  private float cameraElevation;    
//elevation of camera above target
  private float cameraDistanceFromTarget;
  private Point3D targetPos;           
// avatar’s position in the world
  private Vector3D worldUpVec;
  private float maxZoomIn = 5.0f;
  private float maxZoomOut = 40.0f;
  
  
  public OrbitController(ICamera cam, SceneNode target,
                            IInputManager inputMgr, String controllerName, boolean gamePad)
  {  
      this.cam = cam;
      this.target = target;
      worldUpVec = new Vector3D(0,1,0);
      cameraDistanceFromTarget = 5.0f;
      cameraAzimuth = 180;         
      // start from BEHIND and ABOVE the target
      cameraElevation = 20.0f;     
      // elevation is in degrees
      update(0.0f);
      // initialize camera state
      setupInput(inputMgr, controllerName, gamePad); 
  }   
  public void update(float time)
  {   
    updateTarget();
    updateCameraPosition();     
    cam.lookAt(targetPos, worldUpVec);
  }  
  
  private void updateTarget()
  {    targetPos = new Point3D(target.getWorldTranslation().getCol(3)); 
  } 
  
  private void updateCameraPosition()
  {   
    double theta = cameraAzimuth;
    double phi = cameraElevation ;
    double r = cameraDistanceFromTarget;
    
    // calculate new camera position in Cartesian coords
    Point3D relativePosition = MathUtils.sphericalToCartesian(theta, phi, r);
    Point3D desiredCameraLoc = relativePosition.add(targetPos);
    cam.setLocation(desiredCameraLoc);
  }   
  
  private void setupInput(IInputManager im, String cn, boolean gamePad)
  {   
      if (gamePad)
      {
          IAction gpOrbitLeftAction = new OrbitAroundAction(true);
          im.associateAction(cn, net.java.games.input.Component.Identifier.Button._6, gpOrbitLeftAction, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      
          IAction gpOrbitRightAction = new OrbitAroundAction(false);
          im.associateAction(cn, net.java.games.input.Component.Identifier.Button._7, gpOrbitRightAction, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      
          IAction zoomActionIn = new ZoomAction(true);
          im.associateAction(cn, net.java.games.input.Component.Identifier.Button._1, zoomActionIn, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      
          IAction zoomActionOut = new ZoomAction(false);
          im.associateAction(cn, net.java.games.input.Component.Identifier.Button._3, zoomActionOut, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      }
      else
      {
          IAction gpOrbitLeftAction = new OrbitAroundAction(true);
          im.associateAction(cn, net.java.games.input.Component.Identifier.Key.H, gpOrbitLeftAction, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      
          IAction gpOrbitRightAction = new OrbitAroundAction(false);
          im.associateAction(cn, net.java.games.input.Component.Identifier.Key.K, gpOrbitRightAction, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      
          IAction zoomActionIn = new ZoomAction(true);
          im.associateAction(cn, net.java.games.input.Component.Identifier.Key.U, zoomActionIn, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      
          IAction zoomActionOut = new ZoomAction(false);
          im.associateAction(cn, net.java.games.input.Component.Identifier.Key.I, zoomActionOut, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      }
  } 
  
  private class OrbitAroundAction extends AbstractInputAction
  {  
    boolean right;
    float rotAmount;
    
    public OrbitAroundAction(boolean l)
    {
        right = l;
        
        if (right == true)
        {
            rotAmount = -0.1f;
        }
      
        else
        {
            rotAmount = 0.1f;
        }
    }
    public void performAction(float time, Event evt)
    {
      cameraAzimuth += rotAmount;
      cameraAzimuth = cameraAzimuth % 360;
    }
  } 
  
  private class ZoomAction extends AbstractInputAction
  {
      boolean in;
      float zoomAmount;
      
      public ZoomAction(boolean i)
      {
        in = i;
        if (in == true)
        {
            zoomAmount = -0.1f;
        }
        else
        {
            zoomAmount = 0.1f;
        }
    }
        
      public void performAction(float time, Event evt)
      {
        cameraDistanceFromTarget += zoomAmount;
        
        if (cameraDistanceFromTarget > maxZoomOut)
        {
            cameraDistanceFromTarget = maxZoomOut;
        
        }
        else if (cameraDistanceFromTarget < maxZoomIn)
        {
            cameraDistanceFromTarget = maxZoomIn;
        }
      }
  }
}
