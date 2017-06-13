package a2;
import sage.app.BaseGame;
import sage.camera.ICamera;
import sage.scene.SceneNode;
import sage.scene.TriMesh;
import sage.display.*;
import sage.input.*;
import sage.input.action.*;
import sage.display.DisplaySystem;
import sage.event.IEventListener;
import sage.event.IGameEvent;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import graphicslib3D.Point3D;
import net.java.games.input.*;

public class MyPyramid extends TriMesh implements IEventListener
{
	private static float[] vrts = new float[] {0,1,0,-1,-1,1,1,-1,1,1,-1,-1,-1,-1,-1};
	private static float[] cl = new float[] {1,0,0,1,0,1,0,1,0,0,1,1,1,1,0,1,1,0,1,1};
	private static float[] vl = new float[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	private static int[] triangles = new int[] {0,1,2,0,2,3,0,3,4,0,4,1,1,4,2,4,3,2};
	         FloatBuffer colorBuffer1 =
			 com.jogamp.common.nio.Buffers.newDirectFloatBuffer(cl);
			 FloatBuffer colorBuffer2 =
			 com.jogamp.common.nio.Buffers.newDirectFloatBuffer(vl);
	 public MyPyramid()
	 { int i;
	 FloatBuffer vertBuf =
	 com.jogamp.common.nio.Buffers.newDirectFloatBuffer(vrts);
	 FloatBuffer colorBuf =
	 com.jogamp.common.nio.Buffers.newDirectFloatBuffer(cl);
	 FloatBuffer colorBuffer1 =
	 com.jogamp.common.nio.Buffers.newDirectFloatBuffer(cl);
	 FloatBuffer colorBuffer2 =
	 com.jogamp.common.nio.Buffers.newDirectFloatBuffer(vl);
	 IntBuffer triangleBuf =
	 com.jogamp.common.nio.Buffers.newDirectIntBuffer(triangles);
	 this.setVertexBuffer(vertBuf);
	 this.setColorBuffer(colorBuf);
	 this.setIndexBuffer(triangleBuf); }	
	
// (Pyramid info goes here as before, but with two different color buffers)
 public boolean handleEvent(IGameEvent event)
 { // if the event has programmer-defined information in it,
 // it must be cast to the programmer-defined event type.
 CrashEvent cevent = (CrashEvent) event;
 int crashCount = cevent.getWhichCrash();
 if (crashCount % 2 == 0) this.setColorBuffer(colorBuffer1);
 else this.setColorBuffer(colorBuffer2);
 return true;
 }
}