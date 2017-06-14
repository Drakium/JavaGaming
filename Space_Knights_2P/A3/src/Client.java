/*
* Author: Beau Derrick and Christian Solano
* Date: 5/10/17
*/
package a3;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import java.util.Vector;
import graphicslib3D.Vector3D;
import graphicslib3D.Matrix3D;
import sage.scene.SceneNode;
import sage.networking.client.GameConnectionClient;

public class Client extends GameConnectionClient
{
    private MyGame game;
    private UUID id;
    private Vector<GhostAvatar> ghostAvatars;
    private Vector<GhostNPC> ghostNPCs;
    private Knight player;
    
    public Client(InetAddress remAddr, int remPort, ProtocolType pType, MyGame game) throws IOException 
    {   
        super(remAddr, remPort, pType);
        this.game = game;
        this.id = UUID.randomUUID();
        ghostAvatars = new Vector<GhostAvatar>();
        ghostNPCs = new Vector<GhostNPC>();
    } 
    
    public void SetPlayer(Knight p)
    {
        player = p;
    }
    
      protected void processPacket(Object o)  // override
      {   
        String msg = (String)o;
        String[] messageTokens = msg.split(",");
        
        if(messageTokens[0].compareTo("join") == 0)  // receive “join”
        {
            //  format:  join, success   or   join, failure
            if(messageTokens[1].compareTo("success") == 0)
            { 
                game.setIsConnected(true);
                sendCreateMessage(game.getPlayerPosition());
                SetUpNPCs();
            } 
            
            if(messageTokens[1].compareTo("failure") == 0)      
                game.setIsConnected(false);
        }
    
        if(messageTokens[0].compareTo("bye") == 0)  // receive “bye”
        {
            //  format:  bye, remoteId
            UUID ghostID = UUID.fromString(messageTokens[1]);
            removeGhostAvatar(ghostID);
        }
        
        if(messageTokens[0].compareTo("create") == 0)  // receive “create”
        {
            //  format:  create, remoteId, x,y,z   or   dsfr, remoteId, x,y,z
            UUID ghostID = UUID.fromString(messageTokens[1]);
            Vector3D ghostPosition = new Vector3D();
            ghostPosition.setX(Double.parseDouble(messageTokens[2]));
            ghostPosition.setY(Double.parseDouble(messageTokens[3]));
            ghostPosition.setZ(Double.parseDouble(messageTokens[4]));
            createGhostAvatar(ghostID, ghostPosition);
        }
        
         if(messageTokens[0].compareTo("move") == 0) // receive “move”
        {  
          for (GhostAvatar g : ghostAvatars)
          {
              if (g.GetID().equals(UUID.fromString(messageTokens[1])));
              {
                  Vector3D ghostPosition = new Vector3D();
                  ghostPosition.setX(Double.parseDouble(messageTokens[2]));
                  ghostPosition.setY(Double.parseDouble(messageTokens[3]));
                  ghostPosition.setZ(Double.parseDouble(messageTokens[4]));
                  g.SetPosistion(ghostPosition);
              }
          }
         }
        
          
        if(messageTokens[0].compareTo("rotate") == 0) // receive “move”
        {  
          for (GhostAvatar g : ghostAvatars)
          {
              if (g.GetID().equals(UUID.fromString(messageTokens[1])));
              {
                  g.ghostAvatar.rotate((float) Double.parseDouble(messageTokens[2]), new Vector3D(0, 1, 0));
              }
          }
        }
        
          
        if(messageTokens[0].compareTo("npcrotate") == 0) // receive “move”
        {  
          for (GhostNPC g : ghostNPCs)
          {
              g.NPCghostAvatar.rotate((float) Double.parseDouble(messageTokens[2]), new Vector3D(0, 1, 0));
          }
        }
        
        if(messageTokens[0].compareTo("wants") == 0)  // receive “wants...”
        {
            
            UUID newClientID = UUID.fromString(messageTokens[1]);
            sendDetailsForMessage(newClientID, game.getPlayerPosition());
        } 
        
        //NPCs
        if ((messageTokens[0].compareTo("npcinfo") == 0))
        {
            //  format:  create, remoteId, x,y,z   or   dsfr, remoteId, x,y,z
            int ghostID = Integer.parseInt(messageTokens[1]);
            Vector3D ghostNPCPosition = new Vector3D();
            ghostNPCPosition.setX(Double.parseDouble(messageTokens[2]));
            ghostNPCPosition.setY(Double.parseDouble(messageTokens[3]));
            ghostNPCPosition.setZ(Double.parseDouble(messageTokens[4]));
            createGhostNPC(ghostID, ghostNPCPosition);
        }
        
        if ((messageTokens[0].compareTo("npcinfoupdate") == 0))
        {
            //  format:  create, remoteId, x,y,z   or   dsfr, remoteId, x,y,z
            int ghostID = Integer.parseInt(messageTokens[1]);
            Vector3D ghostNPCPosition = new Vector3D();
            ghostNPCPosition.setX(Double.parseDouble(messageTokens[2]));
            ghostNPCPosition.setY(Double.parseDouble(messageTokens[3]));
            ghostNPCPosition.setZ(Double.parseDouble(messageTokens[4]));
            double d = Double.parseDouble(messageTokens[5]);
            
            double distance = Math.sqrt(Math.pow(player.KnightGroup.getLocalTranslation().getCol(3).getX() - ghostNPCPosition.getX(), 2)
                                            + Math.pow(player.KnightGroup.getLocalTranslation().getCol(3).getY() - ghostNPCPosition.getY(), 2) 
                                            + Math.pow(player.KnightGroup.getLocalTranslation().getCol(3).getZ() - ghostNPCPosition.getZ(), 2));
          
          if (distance < d)
          {
              d = distance;
              sendSetTarget(ghostID, d);
          }
        }
        
        if ((messageTokens[0].compareTo("npcmove") == 0))
        {
            //  format:  create, remoteId, x,y,z   or   dsfr, remoteId, x,y,z
            int ghostID = Integer.parseInt(messageTokens[1]);
            Vector3D ghostNPCPosition = new Vector3D();
            ghostNPCPosition.setX(Double.parseDouble(messageTokens[2]));
            ghostNPCPosition.setY(Double.parseDouble(messageTokens[3]));
            ghostNPCPosition.setZ(Double.parseDouble(messageTokens[4]));
            MoveNPC(ghostID, ghostNPCPosition);
        }
            
        if ((messageTokens[0].compareTo("big") == 0))
        {
            //  format:  create, remoteId, x,y,z   or   dsfr, remoteId, x,y,z
            int ghostID = Integer.parseInt(messageTokens[1]);
            Big(ghostID);
        }
        
        if ((messageTokens[0].compareTo("takedamage") == 0))
        {
            //  format:  create, remoteId, x,y,z   or   dsfr, remoteId, x,y,z
            float d = Float.parseFloat(messageTokens[2]);
            player.TakeDamage(d);
        }
      }
      
      public void Big(int i)
      {
          for (GhostNPC npc : ghostNPCs)
          {
              if (npc.id == i)
              {
                  npc.GetBig();
              }
          }
              
      }
      
      public void MoveNPC(int NPCid, Vector3D pos)
      {
          for (GhostNPC npc : ghostNPCs)
          {
              if (npc.id == NPCid)
              {
                  npc.SetPosistion(pos);
              }
          }
      }
      public void sendCreateMessage(Vector3D pos)
      {  
        //  format:  (create, localId, x,y,z 
        try 
        { 
            String message = new String("create," + id.toString());
            message += "," + pos.getX()+"," + pos.getY() + "," + pos.getZ();
            sendPacket(message);
        } 
        catch (IOException e)  
        {  
            e.printStackTrace(); 
        }
      } 
      
      
      public void sendJoinMessage()
      {  
        //  format:  join, localId
       try 
       {   
           sendPacket(new String("join," + id.toString())); 
       } 
       catch (IOException e)  
       { 
           e.printStackTrace();
       } 
      }

      public void sendByeMessage()
      {  
        try 
        { 
            String message = new String("bye," + id.toString());
            sendPacket(message);
        } 
        catch (IOException e)  
        {  
            e.printStackTrace(); 
        }
      } 
      
      
      // Ghost
      public void sendDetailsForMessage(UUID remId, Vector3D pos) 
      {  
         try 
        { 
            
            String message = new String("dsfr," + remId.toString());
            message += "," + id.toString();
            message += "," + pos.getX()+"," + pos.getY() + "," + pos.getZ();
            sendPacket(message);
        } 
        catch (IOException e)  
        {  
            e.printStackTrace(); 
        }
      }
      
       
       public void sendSetTarget(int i, double d)
       {
            try 
        { 
            String message = new String("targetinfo," + i);
            message += "," + player.KnightGroup.getLocalTranslation().getCol(3).getX()+"," + player.KnightGroup.getLocalTranslation().getCol(3).getY() + "," + player.KnightGroup.getLocalTranslation().getCol(3).getZ();
            message += "," + d;
            sendPacket(message);
        } 
        catch (IOException e)  
        {  
            e.printStackTrace(); 
        }
       }
       
       public void SetUpNPCs()
       {
            try 
            { 
                
                String message = new String("npcinfo," + id.toString());
                sendPacket(message);
            } 
            catch (IOException e)  
            {  
                e.printStackTrace(); 
            }
       }
      public void rotate(double r)
      {
          sendRotateMessage(r);
      }
      
      public void move()
      {
          Vector3D p = new Vector3D();
                  p.setX(game.Player.KnightGroup.getLocalTranslation().getRow(0).getW());
                  p.setY(game.Player.KnightGroup.getLocalTranslation().getRow(1).getW());
                  p.setZ(game.Player.KnightGroup.getLocalTranslation().getRow(2).getW());
          sendMoveMessage(p);
          
          
      }
      
      public void sendRotateMessage(double r)
      {
        try 
        { 
            String message = new String("rotate," + id.toString());
            message += "," + r;
            sendPacket(message);
        } 
        catch (IOException e)  
        {  
            e.printStackTrace(); 
        }
      }
      public void sendMoveMessage(Vector3D pos)
      {  
        try 
        { 
            String message = new String("move," + id.toString());
            message += "," + pos.getX()+"," + pos.getY() + "," + pos.getZ();
            sendPacket(message);
        } 
        catch (IOException e)  
        {  
            e.printStackTrace(); 
        }
      } 
      
      private void createGhostAvatar(UUID id, Vector3D pos)
      {
          GhostAvatar ghost = new GhostAvatar(pos);
          ghost.SetID(id);
          ghost.SetPosistion(pos);
          ghostAvatars.add(ghost);
          game.AddObjectToGame(ghost.ghostAvatar);
      }
      
      private void createGhostNPC(int id, Vector3D pos)
      {
          GhostNPC NPCghost = new GhostNPC(pos);
          NPCghost.SetID(id);
          NPCghost.SetPosistion(pos);
          ghostNPCs.add(NPCghost);
          game.AddObjectToGame(NPCghost.NPCghostAvatar);
      } 
      
      private void removeGhostAvatar(UUID id)
      {
          GhostAvatar ghost = null;
          
          for (GhostAvatar g : ghostAvatars)
          {
              if (g.GetID().equals(id))
              {
                  ghost = g;
                  break;
              }
          }
          
          if (ghost != null)
          {
              game.RemoveObjectToGame(ghost.ghostAvatar);
              ghostAvatars.remove(ghost);   
          }
      }
      
      private void removeGhostNPC(int id)
      {
          GhostNPC ghostNPC = null;
          
          for (GhostNPC g : ghostNPCs)
          {
              if (g.GetID() == id)
              {
                  ghostNPC = g;
                  break;
              }
          }
          
          if (ghostNPC != null)
          {
              game.RemoveObjectToGame(ghostNPC.NPCghostAvatar);
              ghostAvatars.remove(ghostNPC);   
          }
      }
      
      public void SendAttackMsg(Matrix3D pos, float Damage)
      {
          for (GhostAvatar g : ghostAvatars)
          {
               double distance = Math.sqrt(Math.pow(g.position.getX() - pos.getCol(3).getX(), 2d) + Math.pow(g.position.getY() - pos.getCol(3).getY(), 2d) + Math.pow(g.position.getZ() - pos.getCol(3).getZ(), 2d));
                if (distance < 1f)
                {
                    try 
                    { 
                        String message = new String("dodamage," + g.id.toString());
                        message += "," + Damage;
                        sendPacket(message);
                        g.ghost.TakeDamage(Damage);
                    } 
                    catch (IOException e)  
                    {  
                        e.printStackTrace(); 
                    }
                }
          }
      }
      
      private class GhostAvatar
      {
          private UUID id;
          private Vector3D position;
          private Vector3D oldPosition;
          public SceneNode ghostAvatar;
          public Knight ghost;
          
          
          public GhostAvatar(Vector3D p)
          {
              ghost =  new Knight(game, game.getPlayerAvatar(), p);
              ghostAvatar = ghost.KnightGroup;
              oldPosition = p;
              position = p;
          }
          
          public void SetID(UUID i)
          {
              id = i;
          }
          
          public UUID GetID()
          {
              return id;
          }
          
          public void SetPosistion(Vector3D p)
          {
              oldPosition = position;
              position = p;
              MoveGhost();
          }
          
          public void MoveGhost()
          {
              ghostAvatar.translate((float)(position.getX() - oldPosition.getX()), (float)(position.getY() - oldPosition.getY()),(float) (position.getZ() - oldPosition.getZ()));
          }
      }
      
      private class GhostNPC
      {
          private int id;
          private Vector3D position;
          private Vector3D oldPosition;
          public SceneNode NPCghostAvatar;
          public Knight NPCghost;
          public boolean flip;
          
          public GhostNPC(Vector3D p)
          {
              NPCghost =  new Knight(game, game.getPlayerAvatar(), p);
              NPCghostAvatar = NPCghost.KnightGroup;
              oldPosition = p;
              position = p;
              flip = false;
          }
          
          public void GetBig()
          {
              float scaler = 1;
              if (flip)
                  scaler = 0.5f;
              else
                  scaler = 2f;
                          
              NPCghost.KnightGroup.scale(scaler, scaler, scaler);
              flip = !flip;
          }
     
          public void SetID(int i)
          {
              id = i;
          }
          
          public int GetID()
          {
              return id;
          }
          
          public void SetPosistion(Vector3D p)
          {
              oldPosition = position;
              position = p;
              MoveGhost();
          }
          
          public void MoveGhost()
          {
              NPCghostAvatar.translate((float)(position.getX() - oldPosition.getX()), (float)(position.getY() - oldPosition.getY()),(float) (position.getZ() - oldPosition.getZ()));
          }
      }
      
}


