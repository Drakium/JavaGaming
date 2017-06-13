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
import java.util.Timer;

import graphicslib3D.Point3D;
import net.java.games.input.*;

public class MyTruck extends TriMesh implements IEventListener
{
	private static float[] vrts = new float[] {-2,2,2,-2,-2,2,2,-2,2,2,2,2,-2,2,-2,-2,-2,-2
			,2,2,-2,2,-2,-2};
	private static float[] vrtsScaled = new float[] {-4,4,4,-4,-4,4,4,-4,4,4,4,4,-4,4,-4,-4,-4,-4
			,4,4,-4,4,-4,-4};
	private static float[] cl = new float[] {1,0,0,1,
			0,1,0,1,(float) .25,1,1,1,0,(float) .5,1,1,(float) .5,1,1,0,1,1,1,(float) .25,1,(float) .25,1,1,1,0,(float) .5,1};
	private static float[] vl = new float[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
	private static int[] triangles = new int[] {0,1,2,0,3,2,0,1,5,0,4,5,4,5,7,4,6,7,7,6,3,7,2,3,1,2,5,5,7,2};
			FloatBuffer colorBuffer1 =
			 com.jogamp.common.nio.Buffers.newDirectFloatBuffer(cl);
			 FloatBuffer colorBuffer2 =
					 com.jogamp.common.nio.Buffers.newDirectFloatBuffer(vl);
	public MyTruck(){
		 int i;
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
				 this.setIndexBuffer(triangleBuf);
		
	}
	@Override
	 public boolean handleEvent(IGameEvent event)
	 { // if the event has programmer-defined information in it,
	 // it must be cast to the programmer-defined event type.
	 CrashEvent cevent = (CrashEvent) event;
	 int crashCount = cevent.getWhichCrash();
	 
	 System.out.println(crashCount);
	 //Checks if there's a crash, if there is then enlarges the box and changes its color
	 if (crashCount % 2 == 0){
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
						 this.setIndexBuffer(triangleBuf);
		 this.setColorBuffer(colorBuffer1);
	 }
	 else {
	 FloatBuffer vertBuf =
			 com.jogamp.common.nio.Buffers.newDirectFloatBuffer(vrtsScaled);
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
					 this.setIndexBuffer(triangleBuf);
					 this.setColorBuffer(colorBuffer2);
	 }
	 return true;
	 }
	}