
package server;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import sage.networking.IGameConnection;
import sage.networking.server.GameConnectionServer;
import sage.networking.server.IClientInfo;
import graphicslib3D.Point3D;
import java.util.Random;
import sage.ai.behaviortrees.BehaviorTree;
import sage.ai.behaviortrees.BTCompositeType;
import sage.ai.behaviortrees.BTSequence;
import sage.ai.behaviortrees.*;

public class GameServerTCP extends GameConnectionServer<UUID> 
{ 
    
   NPCcontroller NPCcontrol;
   private float lastUpdateTime;
   private float lastThinkTime;
   private BehaviorTree bt;
   private int numClients = 0;
    
  public GameServerTCP(int localPort) throws IOException  
  { 
    super(localPort, IGameConnection.ProtocolType.TCP);
    NPCcontrol = new NPCcontroller();
    lastUpdateTime = 0;
    lastThinkTime = 0;
    bt = new BehaviorTree(BTCompositeType.SELECTOR);
    setUpBT();
    npcLoop();
  } 
  
    public void npcLoop()
// NPC control loop
  {  while (true)
    {
      long frameStartTime = System.nanoTime();
      float elapMilSecs =(frameStartTime- lastUpdateTime)/(1000000.0f);
      if (elapMilSecs >= 50.0f)
      {
          lastUpdateTime = frameStartTime;
          NPCcontrol.updateNPCs();
         // testTCPServer.sendNPCinfo();
      }
      
      elapMilSecs =(frameStartTime- lastThinkTime)/(1000000.0f);
      if (elapMilSecs >= 500.0f)
      {
          lastThinkTime = frameStartTime;
          bt.update(elapMilSecs);
         // testTCPServer.sendNPCinfo();
      }
      Thread.yield();
    } 
  }
    
    public void setUpBT()
    {
        bt.insertAtRoot(new BTSequence(10));
        bt.insert(10, new AvatarNear(this, NPCcontrol, NPCcontrol.NPClist[0], false));
        bt.insert(10, new GetBig(NPCcontrol.NPClist[0]));
    }
  
  public void acceptClient(IClientInfo ci, Object o) // override
  {   
      String message = (String)o;
      String[] messageTokens = message.split(","); 
      
      if(messageTokens.length > 0)
      {
        if(messageTokens[0].compareTo("join") == 0) // received “join”
        {
            // format:  join,localid
            UUID clientID = UUID.fromString(messageTokens[1]);
            addClient(ci, clientID);
            numClients++;
            sendJoinedMessage(clientID, true);
        } 
      } 
  }
  
    public void processPacket(Object o, InetAddress senderIP, int sndPort)
    {   
        String message = (String)o; 
        String[] msgTokens = message.split(",");
        
        //System.out.println(message);
        if(msgTokens.length > 0)
        {
            // Ghosts
            if(msgTokens[0].compareTo("bye") == 0) //  receive “bye”
            {     
                //  format:  bye,localid
                UUID clientID = UUID.fromString(msgTokens[1]);
                sendByeMessages(clientID);
                removeClient(clientID);
                numClients--;
            }
            
            if(msgTokens[0].compareTo("create") == 0)  // receive “create”
            {
                // format:  create,localid,x,y,z
                UUID clientID = UUID.fromString(msgTokens[1]);
                String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
                sendCreateMessages(clientID, pos);
                sendWantsDetailsMessages(clientID);
            }
            
            if(msgTokens[0].compareTo("move") == 0)  //  receive “move”
            {  
                UUID clientID = UUID.fromString(msgTokens[1]);
                String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
                sendMoveMessages(clientID, pos);
            } 
            
             if(msgTokens[0].compareTo("rotate") == 0)  
            {  
                UUID clientID = UUID.fromString(msgTokens[1]);
                double r = Double.parseDouble(msgTokens[2]);
                sendRotateMessage(clientID, r);
            } 
            
            
            if(msgTokens[0].compareTo("dsfr") == 0)  // receive “details for”
            {
               
               UUID clientID = UUID.fromString(msgTokens[1]);
               UUID remoteID = UUID.fromString(msgTokens[2]);
               String[] p = new String[3];
               p[0] = msgTokens[3];
               p[1] = msgTokens[4];
               p[2] = msgTokens[5];
               sndDetailsMsg(clientID, remoteID, p);
            }
            
            //NPC receive target info
            
            if (msgTokens[0].compareTo("targetinfo") == 0)
            {
                int i = Integer.parseInt(msgTokens[1]);
                Point3D p = new Point3D();
                p.setX(Double.parseDouble(msgTokens[2]));
                p.setX(Double.parseDouble(msgTokens[3]));
                p.setX(Double.parseDouble(msgTokens[4]));
                double d = Double.parseDouble(msgTokens[5]);
                UpdateTarget(i, p, d);
            }
            
            
            if (msgTokens[0].compareTo("npcinfo") == 0)
            {
                UUID clientID = UUID.fromString(msgTokens[1]);
                sendNPCInfo(clientID); 
            }
            
            if (msgTokens[0].compareTo("dodamage") == 0)
            {
                UUID clientID = UUID.fromString(msgTokens[1]);
                float damage = Float.parseFloat(msgTokens[2]);
                DoDamage(clientID, damage); 
            }
        } 
    } 
    
    public void DoDamage(UUID clientID, float d)
    {
            try
            {
                String message = new String("takedamage," + clientID);
                message += "," + d;
                sendPacket(message, clientID);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
    }
    
    public void sendNPCInfo(UUID clientID)
    { 
        for (NPC npc : NPCcontrol.NPClist)
        {
           try
            {
                String message = new String("npcinfo," + npc.id);
                message += "," + npc.getX();
                message += "," + npc.getY();
                message += "," + npc.getZ();
                sendPacket(message, clientID);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void sendNPCInfoUpdate()
    { 
        for (NPC npc : NPCcontrol.NPClist)
        {
           try
            {
                String message = new String("npcinfoupdate," + npc.id);
                message += "," + npc.getX();
                message += "," + npc.getY();
                message += "," + npc.getZ();
                message += "," + npc.currentMinDistance;
                sendPacketToAll(message);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
  
   
  public void sendJoinedMessage(UUID clientID, boolean success)
  {   
    //  format:  join, success   or   join, failure
    try 
    {    
        String message = new String("join,");
        
        if(success) 
            message += "success";
        else
            message += "failure";
        
        sendPacket(message, clientID);
    } 
    catch (IOException e)  
    {  
        e.printStackTrace();
    } 
  }
  
  public void sendCreateMessages(UUID clientID, String[] position)
  {  
    //  format:  create, remoteId, x, y, z
    try 
    {   
        String message = new String("create," + clientID.toString());
        message += "," + position[0];
        message += "," + position[1];
        message += "," + position[2];
        forwardPacketToAll(message, clientID);
    } 
    
    catch (IOException e)  
    { 
        e.printStackTrace();
    } 
  }
  

  public void sndDetailsMsg(UUID clientID, UUID remoteId, String[] position)
  {   
    try 
    {   
        String message = new String("create," + remoteId.toString());
        message += "," + position[0];
        message += "," + position[1];
        message += "," + position[2];
        sendPacket(message, clientID);
    } 
    
    catch (IOException e)  
    { 
        e.printStackTrace();
    }
  } 
  

  public void sendWantsDetailsMessages(UUID clientID)
  {  
         try 
    {   
        String message = new String("wants," + clientID.toString());
        forwardPacketToAll(message, clientID);
    } 
    
    catch (IOException e)  
    { 
        e.printStackTrace();
    } 
  }
  
  public void sendMoveMessages(UUID clientID, String[] position)
  {  
    try 
    {   
        String message = new String("move," + clientID.toString());
        message += "," + position[0];
        message += "," + position[1];
        message += "," + position[2];
        forwardPacketToAll(message, clientID);
    } 
    
    catch (IOException e)  
    { 
        e.printStackTrace();
    }  
  } 
  
public void sendRotateMessage(UUID clientID, double r)
  {  
    try 
    {  
        String message = new String("rotate," + clientID.toString());
        message += "," + r;
        forwardPacketToAll(message, clientID);
    } 
    
    catch (IOException e)  
    { 
        e.printStackTrace();
    } 
  } 
  
  public void sendByeMessages(UUID clientID)
  {  
    try 
    {   
        String message = new String("bye," + clientID.toString());
        forwardPacketToAll(message, clientID);
    } 
    
    catch (IOException e)  
    { 
        e.printStackTrace();
    } 
  } 
  
  //NPCs
  
  public void sendNPCMoveMessages(int NPCID, Point3D position)
  {  
    try 
    {   
        String message = new String("npcmove," + NPCID);
        message += "," + position.getX();
        message += "," + position.getY();
        message += "," + position.getZ();
        sendPacketToAll(message);
    } 
    
    catch (IOException e)  
    { 
        e.printStackTrace();
    }  
  }
  
  public void sendBigMessage(int i)
  {
       try 
    {   
        String message = new String("big," + i);
        sendPacketToAll(message);
    } 
    
    catch (IOException e)  
    { 
        e.printStackTrace();
    }
  }
  
  public void UpdateTarget(int NPCid, Point3D p, double d)
  {
      NPCcontrol.UpdateTarget(NPCid, p, d);
  }
  
  public class NPC
  { 
      Point3D location;
      Point3D target;
      public int id;
      public double currentMinDistance;
      private int clientIndex;
      private boolean gotTarget;
     
      public NPC()
      {
          location = new Point3D(0, 1, 0);
          currentMinDistance = 9999;
          target = new Point3D(0, 1, 0);
          clientIndex = 0;
      }
      
      public void updateLocation()
      {
          SetLocation(target.getX(), target.getY(), target.getZ());
          sendNPCMoveMessages(id, location);
      } 
      
      public boolean isNear()
      {
          double distance = Math.sqrt(Math.pow(location.getX() - 0, 2) + Math.pow(location.getY() - 0, 2) + Math.pow(location.getZ() - 0, 2));
          if (distance < 2)
              return true;
          else
              return false;
      }
      
      public void getBig()
      {
          sendBigMessage(id);
      }
  
      public void UpdateTarget(Point3D p, double d)
      {
          target = p;
          currentMinDistance = d;
          updateLocation();
      }
      
      public void SetLocation(double x, double y, double z)
      {
          location.setX(x);
          location.setY(y);
          location.setZ(z);
      }
 
      public double getX() { return location.getX(); }
      public double getY() { return location.getY(); }
      public double getZ() { return location.getZ(); }
      
      public void rotate()
      {
            try 
            {   
                String message = new String("npcrotate," + 0);
                message += "," + 1d;
                sendPacketToAll(message);
            } 

            catch (IOException e)  
            { 
                e.printStackTrace();
            } 
      }
  
  } 
  
  public class NPCcontroller
{ 
  private NPC[] NPClist; 
  private int numNPCs;;
  private Point3D[] spawnPoints;
  private int numSpawnPoints;
  private boolean hasTarget = false;
 
  public NPCcontroller()
  {
      NPClist = new NPC[1];
      numNPCs = 1;
      spawnPoints = new Point3D[4];
      SetUpNPCs();
      numSpawnPoints = spawnPoints.length;
      SetUpSpawnPoints();
      SpawnNPCs();
  }
  
  public void UpdateTarget(int NPCid, Point3D p, double d)
  {
      for (int i = 0; i < numNPCs; i++)
      {
          if (NPClist[i].id == NPCid)
          {
             NPClist[i].UpdateTarget(p, d);
             break;
          }
      }
  }
  
  private void SetUpNPCs()
  {
      for (int i = 0; i < numNPCs; i++)
      {
          NPClist[i] = new NPC();
          NPClist[i].id = i;
      }
  }
  
  
  private void SetUpSpawnPoints()
  {
      Random random = new Random();
      
      for (int i = 0; i < numSpawnPoints; i++)
      {
          spawnPoints[i] = new Point3D();
          spawnPoints[i].setX(random.nextDouble() * 100d);
          spawnPoints[i].setY(1d);
          spawnPoints[i].setZ(random.nextDouble() * 100d);
      }
      
  }
  
  public void SpawnNPCs()
  {
      Random random = new Random();
      
      for (int i = 0; i < numNPCs; i++)
      {
          int p = random.nextInt(numSpawnPoints);
          NPClist[i].SetLocation(spawnPoints[p].getX(), spawnPoints[p].getY(), spawnPoints[p].getZ());
      }
  }
  
  
  public void updateNPCs()
  { 
    for (int i=0;  i < numNPCs; i++)
    { 
        // sendNPCInfoUpdate();
    } 
  }
}
  
  public class AvatarNear extends BTCondition
  {
      private GameServerTCP server;
      NPCcontroller controller;
      NPC npc;
      
      public AvatarNear(GameServerTCP s, NPCcontroller c, NPC n, boolean toNegate)
      {
          super(toNegate);
          server = s;
          controller = c;
          npc = n;
      }
      
      protected boolean check()
      {
          return npc.isNear();
      }
      
  }
  
   public class GetBig extends BTAction
  {
      NPC npc;
      
      public GetBig ( NPC n)
      {
          npc = n;
      }
      
      protected BTStatus update(float elsapsedTime)
      {
          npc.getBig();
          return BTStatus.BH_SUCCESS;
      }
      
  }
  
}
