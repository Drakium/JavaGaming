/*
* Author: Beau Derrick and Christian Solano
* Date: 5/10/17
*/
package a3;

import sage.input.action.AbstractInputAction;
import graphicslib3D.Vector3D;
import sage.input.action.IAction;
import net.java.games.input.Event;
import sage.input.IInputManager;
import sage.scene.SceneNode;

/**
 * This class implements methods for controlling the game avatar
 */
public class MoveAvatar
{
    private SceneNode avatar;
    private float jumpHeight = 10.0f;
    private boolean isJumping = false;
    private boolean isFalling = false;
    private float currentHeight = 0.0f;
    private Client client;
    private boolean isEnabled = true;
    
    public MoveAvatar (SceneNode a, IInputManager inputMgr, String controllerName, boolean gamePad, Client c)
    {
        avatar = a; 
        client = c;
        SetUpInput(inputMgr, controllerName, gamePad);
    }
    
    public void update(float time)
   {   
       if (isJumping)
       {
           if (currentHeight < jumpHeight)
           {
               avatar.translate(0, 0.1f, 0);
               currentHeight += 0.1f;
           }
           else 
           {
               isFalling = true;
               isJumping = false;
           }
       }
       else if (isFalling)
       {
           if (currentHeight > 0)
           {
               avatar.translate(0, -0.1f, 0);
               currentHeight -= 0.1f;
           }
           else
           {
               isFalling = false;
           }
               
       }
       
 
        client.move();
   } 
    
    private void SetUpInput(IInputManager im, String cn, boolean gamePad)
    {
        if (gamePad)
        {
            IAction leftRight = new LeftRight(true, false);
            im.associateAction(cn, net.java.games.input.Component.Identifier.Axis.X, leftRight, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      
            IAction backForward = new BackForward(true, false);
            im.associateAction(cn, net.java.games.input.Component.Identifier.Axis.Y, backForward, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      
            IAction jump = new Jump();
            im.associateAction(cn, net.java.games.input.Component.Identifier.Button._2, jump, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
        }
        else
        {
            IAction left = new LeftRight(false, true);
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.A, left, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            
            IAction right = new LeftRight(false, false);
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.D, right, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                    
            IAction forward= new BackForward(false, true);
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.W, forward, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            
            IAction back = new BackForward(false, false);
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.S, back, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      
            IAction jump = new Jump();
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.SPACE, jump, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            
            IAction roatateRight = new Rotate(false, true);
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.J, roatateRight, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            
            IAction roatateLeft = new Rotate(false, false);
            im.associateAction(cn, net.java.games.input.Component.Identifier.Key.L, roatateLeft, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        
        }
    }
    
    public void Disable()
    {
        isEnabled = false;
    }
    
     public void Enable()
    {
        isEnabled = true;
    }
    
    private class LeftRight extends AbstractInputAction
    {
        float amount;
        boolean gamePad;
        boolean left;
        
        private LeftRight(boolean g, boolean l)
        {
            gamePad = g;
            left = l;
        }
        
        
        public void performAction(float time, Event e)
        {
            if (gamePad)
            {
                if (e.getValue() > 0.2f)
                {
                    amount = -0.1f;
                }
                else if (e.getValue() < 0.2f)
                {
                    amount = 0.1f;
                }
            }
            else
            {
                if (left)
                {
                    amount = 0.1f;
                }
                else
                {
                    amount = -0.1f;
                }
            }
            
        avatar.translate(amount, 0, 0);
        
       }
    } 
    
     private class BackForward extends AbstractInputAction
    {
        float amount;
        boolean gamePad;
        boolean forward;
        
        private BackForward(boolean g, boolean f)
        {
            gamePad = g;
            forward = f;
        }
         
        public void performAction(float time, Event e)
        {
            if (gamePad)
            {
                if (e.getValue() > 0.2f)
                {
                    amount = -0.1f;
                }
                else if (e.getValue() < 0.2f)
                {
                    amount = 0.1f;
                }
            }
            else
            {
                if (forward)
                {
                    amount = 0.1f;
                }
                else
                {
                    amount = -0.1f;
                }
            }
        
        avatar.translate(0, 0, amount);
        
       }
    } 
     
    private class Jump extends AbstractInputAction
    {
        public void performAction(float time, Event e)
        {
            if (!isJumping && !isFalling)
            {
                isJumping = true;
            }
        }
    } 
    
    public boolean isJumping()
    {
            return isJumping;
    }
    
    public boolean isFaling()
    {
        return isFalling;
    }
    
    public class Rotate extends AbstractInputAction 
{
    private float amount;
    boolean gamePad;
    boolean right;
    
    public Rotate (boolean gp, boolean r)
    {
        gamePad = gp;
        right = r;
    }
    
    public void performAction(float time, Event e)
    {
        
         if (gamePad)
            {
                if (e.getValue() > 0.2f)
                {
                    amount = -0.1f;
                }
                else if (e.getValue() < 0.2f)
                {
                    amount = 0.1f;
                }
            }
            else
            {
                if (right)
                {
                    amount = 1f;
                }
                else
                {
                    amount = -1f;
                }
            }
         avatar.rotate(amount, new Vector3D(0, 1, 0));
         client.rotate(amount);
    }
    
    }
}



