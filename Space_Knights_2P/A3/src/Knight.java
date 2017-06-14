/*
* Author: Beau Derrick and Christian Salano
* Date: 5/10/17
*/
package a3;

import java.awt.Color;

import sage.scene.shape.*;
import sage.scene.Group;
import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;
import java.util.Iterator;
import net.java.games.input.Event;
import sage.input.IInputManager;
import sage.input.action.AbstractInputAction;
import sage.input.action.IAction;
import sage.scene.Model3DTriMesh;
import sage.scene.SceneNode;

public class Knight 
{
    public Group KnightGroup;
    
    public float MaxHealth;
    public float Health;
    public float Damage;
    public float DisableTimer;
    public int Deaths;
    
    
    public MoveAvatar Controller;
    
    public Group Model;
    
    public MyGame Game;
    
    public Rectangle HealthBarBack;
    public Rectangle HealthBarFront;
    private float HealthBarWidth;
    private float previousScale;
    private float previousWidth;
    
    private IInputManager im;
    String cName;
    private boolean isAttacking = false;
    private Client client;
    private float attackTime = 1f;
    private float attackTimer = 0.0f;
    private float lastTime = 0.0f;
    
    public Knight(MyGame g, Group m, IInputManager inputMgr, String controllerName, Client c)
    {
        Game = g;
        Model = m;
        Model.scale(.25f, .25f, .25f);
        im = inputMgr;
        cName = controllerName;
        client = c;
        IAction attack = new Attack();
        im.associateAction(cName, net.java.games.input.Component.Identifier.Key.B, attack, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);
        SetStats();
        SetHealthBar();
        SetGroup();
    }
    
        
    public Knight(MyGame g, Group m, Vector3D p)
    {
        Game = g;
        Model = m;
        Model.scale(.25f, .25f, .25f);
        SetStats();
        SetHealthBar();
        SetGroup();
        KnightGroup.translate((float) p.getX(), (float) p.getY(), (float) p.getZ());
    }
    
    public void update(float time)
    {
 
        Iterator<SceneNode> itr = Model.getChildren();
        while (itr.hasNext())
        { 
            Model3DTriMesh submesh = (Model3DTriMesh) itr.next();
            submesh.updateAnimation(time);
        }
        MatchTerrain();
        Game.thisClient.move();
        if(isAttacking)
        {   
            if (attackTimer > attackTime)
            {
                client.SendAttackMsg(KnightGroup.getLocalTranslation(), Damage);
                attackTimer = 0.0f;
            }
            
        }
        attackTimer += time / 1000f;
        KnightGroup.updateGeometricState(time, true);
    }
    public void TakeDamage(float damage)
    {
        Health -= damage;
        UpdateHealthBar();
        
        if (Health <= 0)
        {
            Die();
        }
    }
    
    public void Die()
    {
        Deaths++;
        Health = MaxHealth;
        UpdateHealthBar();
    }
    
    
    private void SetStats()
    {
        Deaths = 0;
        MaxHealth = 100.0f;
        Health = MaxHealth;
        Damage = 10f;
        DisableTimer = 0.0f;
        
    }
    
    private void SetHealthBar()
    {
        HealthBarWidth = 3.0f;
        previousWidth = HealthBarWidth;
        previousScale = 1.0f;
                
        
        HealthBarBack = new Rectangle("HealthBarBackground", HealthBarWidth, 1.0f);
        HealthBarBack.setColor(Color.BLACK);
        HealthBarBack.translate(0f, 6f, 0f);
        
        HealthBarFront = new Rectangle("HealthBarForeground", HealthBarWidth, 1.0f);
        HealthBarFront.setColor(Color.RED);
        HealthBarFront.translate(0f, 6f, 0f);
    }
    
    private void UpdateHealthBar()
    {
        float tempScale = Health / MaxHealth;
        float appliedScale = tempScale / previousScale;
        previousScale = tempScale;
        HealthBarFront.scale(appliedScale, 1.0f, 1.0f);
        
        float newWidth = HealthBarWidth * tempScale;
        float newLocation = (previousWidth - newWidth) / 2.0f;
        previousWidth = newWidth;
        System.out.println(newLocation);
        HealthBarFront.translate(newLocation, 0.0f, 0.0f);
        
        
    }
    
    private void ResetHealthBar()
    {
        previousScale = 1.0f;
        previousWidth = HealthBarWidth;
        float tempScale = HealthBarWidth;
        Matrix3D mat = new Matrix3D();
        mat.scale(tempScale, 1.0f, 1.0f);
        HealthBarFront.setLocalScale(mat);
        HealthBarFront.translate((float)(HealthBarBack.getLocalTranslation().getCol(3).getX() - HealthBarFront.getLocalTranslation().getCol(3).getX()) , 0.0f, 0.0f);
        
    }
    
    private void SetGroup()
    {
        KnightGroup = new Group("Knight");
        
        KnightGroup.setIsTransformSpaceParent(true);
        HealthBarFront.setIsTransformSpaceParent(true);
        HealthBarBack.setIsTransformSpaceParent(true);
        Model.setIsTransformSpaceParent(true);
        
        KnightGroup.addChild(Model);
        KnightGroup.addChild(HealthBarFront);
        KnightGroup.addChild(HealthBarBack);
        Game.AddObjectToGame(KnightGroup);
    }
    
    public void AddController(MoveAvatar c)
    {
        Controller = c;
    }
    
    private void MatchTerrain()
    {
        float xP = (float) KnightGroup.getLocalTranslation().getCol(3).getX();
        float zP = (float) KnightGroup.getLocalTranslation().getCol(3).getZ();
        float yP = Game.getTerrain().getHeight(xP, zP);
        if (yP > 0 && !Controller.isJumping() && !Controller.isFaling())
            KnightGroup.translate(0, yP - (float) KnightGroup.getLocalTranslation().getCol(3).getY() , 0);
    }
    
    
    private class Attack extends AbstractInputAction
    {
        public Attack()
        {
        }
        
        public void performAction(float time, Event e)
        {
            if (!isAttacking)
            {
                Iterator<SceneNode> itr = Model.getChildren();
                while (itr.hasNext())
                { 
                    Model3DTriMesh mesh = ((Model3DTriMesh)itr.next());
                    mesh.startAnimation("SwordAttack");
                }
                
                isAttacking = true;
            }
            else
            {
                Iterator<SceneNode> itr = Model.getChildren();
                while (itr.hasNext())
                { 
                    Model3DTriMesh mesh = ((Model3DTriMesh)itr.next());
                    mesh.stopAnimation();
                }
                
                isAttacking = false;
            }
        }
    }
}
