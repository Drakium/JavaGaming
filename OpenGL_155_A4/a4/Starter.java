package a4;

import graphicslib3D.*;
import graphicslib3D.light.*;
import graphicslib3D.GLSLUtils.*;
import graphicslib3D.shape.*;

import java.nio.*;
import javax.swing.*;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import java.awt.event.*;
import java.io.*;


public class Starter extends JFrame implements GLEventListener,KeyListener,MouseMotionListener
        ,MouseWheelListener
{	private GLCanvas myCanvas;
	private Material thisMaterial;
	private String[] vBlinn1ShaderSource, vBlinn2ShaderSource, fBlinn2ShaderSource,axesFrag,axesVert,instanceFrag,
                instanceVert,skyVert,cloudVert,skyFrag,cloudFrag,bumpVert,bumpFrag;
        private String[] tessVert,tessFrag,tessE,tessC;
	private int rendering_program1, rendering_program2,rendering_program3,rendering_program4,
                rendering_program5,rendering_cube_map,render_tess_Program,render_bump;
	private int vao[] = new int[1];
	private int vbo[] = new int[8];
	private int mv_location, proj_location, vertexLoc, n_location,lightSwitch=0;
        private float camLocX = 1.0f, camLocY=-1.2f, camLocZ=6.0f;
        private float JLocX= -10.0f, JLocY , JLocZ,JEyeV;
        private float lightLocX,lightLocY,lightLocZ=4.0f;
        private float cubeLocX= 2.1f,cubeLocY=-2.0f,cubeLocZ=5.3f;
        private float instanceCX, instanceCY, instanceCZ;
        private int textureID2;
        
        private Torus myTorusE = new Torus (1.0f,0.5f,48);
        private float amt = 0.0f;
        
	private float aspect;
	private GLSLUtils util = new GLSLUtils();
	
	// location of torus and camera
	private Point3D torusLoc = new Point3D(2.6, 0.0, -0.3);
	private Point3D pyrLoc = new Point3D(0.0, 0.1, 0.3);
	private Point3D cameraLoc = new Point3D(0.0, 0.2, 6.0);
	private Point3D lightLoc = new Point3D(-3.8f, 2.2f, 0.1f);
	
	private Matrix3D m_matrix = new Matrix3D();
	private Matrix3D v_matrix = new Matrix3D();
	private Matrix3D mv_matrix = new Matrix3D();
	private Matrix3D proj_matrix = new Matrix3D();
	
	// light stuff
	private float [] globalAmbient = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
	private PositionalLight currentLight = new PositionalLight();
	
	// shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadow_tex = new int[2];
	private int [] shadow_buffer = new int[1];
	private Matrix3D lightV_matrix = new Matrix3D();
	private Matrix3D lightP_matrix = new Matrix3D();
	private Matrix3D shadowMVP1 = new Matrix3D();
	private Matrix3D shadowMVP2 = new Matrix3D();
	private Matrix3D b = new Matrix3D();
        
        //shuttle texture
        private int shuttleTexture;
	private Texture joglShuttleTexture;
        
        //Ice texture
        private int iceTexture;
        private Texture joglIceTexture;
        
	// model stuff
	private ImportedModel pyramid = new ImportedModel("shuttle.obj");
	private Torus myTorus = new Torus(0.6f, 0.4f, 48);
	private int numPyramidVertices, numTorusVertices;
	
        //axes Switch
        private int axesSwitch = 0;
        private int instanceSwitch = 0;
        
        //Tess
        private Texture moonTex;
        private int tessTex;
        private Matrix3D mvp_matrix = new Matrix3D();
        
        //3d obj
        private ImportedModel dolphin = new ImportedModel("dolphinLowPoly.obj");
        private int numObjVertices;
        private int texHeight= 200;
	private int texWidth = 200;
	private int texDepth = 200;
	private double[][][] tex3Dpattern = new double[texHeight][texWidth][texDepth];
	private int textureID3;
        
	private double rotAmt = 0.0;
        
	public Starter()
	{	setTitle("Christian Solano A4");
		setSize(900, 900);
                
                //Adds all listeners to canvas
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
                myCanvas.addKeyListener(this);
                myCanvas.addMouseMotionListener(this);
                myCanvas.addMouseWheelListener(this);
		getContentPane().add(myCanvas);
		setVisible(true);
                
		FPSAnimator animator = new FPSAnimator(myCanvas, 30);
		animator.start();
	}

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
        
           
               
             Point3D lightLoc = new Point3D(lightLocX,-lightLocY,lightLocZ);
                //Used to Toggle the Light Effects
                if (lightSwitch == 0 ){
		currentLight.setPosition(lightLoc);
                }
                if (lightSwitch==1){    
                }
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		proj_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);
		
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadow_buffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadow_tex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);

		gl.glEnable(GL_POLYGON_OFFSET_FILL);	// for reducing
		gl.glPolygonOffset(2.0f, 4.0f);			//  shadow artifacts
                
		passOne();
		
		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
	
		gl.glDrawBuffer(GL_FRONT);
		
		passTwo();
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passOne()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(rendering_program1);
		
		Point3D origin = new Point3D(0.0, 0.0, 0.0);
		Vector3D up = new Vector3D(0.0, 1.0, 0.0);
		lightV_matrix.setToIdentity();
		lightP_matrix.setToIdentity();
	
		lightV_matrix = lookAt(currentLight.getPosition(), origin, up);	// vector from light to origin
		lightP_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);

		// draw the torus
		
		m_matrix.setToIdentity();
		m_matrix.translate(torusLoc.getX(),torusLoc.getY(),torusLoc.getZ());
		m_matrix.rotateX(25.0);
		
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);
		int shadow_location = gl.glGetUniformLocation(rendering_program1, "shadowMVP");
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up torus vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);	
                
		gl.glClear(GL_DEPTH_BUFFER_BIT);
                
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
                
		gl.glDrawArrays(GL_TRIANGLES, 0, numTorusVertices);

		// ---- draw the shuttle
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(pyrLoc.getX(),pyrLoc.getY(),pyrLoc.getZ());
                m_matrix.scale(2.5, 2.5, 2.5);
		m_matrix.rotateX(20.0);
		m_matrix.rotateY(30.0);

		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
                
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
                
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
                
		gl.glDrawArrays(GL_TRIANGLES, 0, pyramid.getNumVertices());
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passTwo()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
        
                // draw cube map------------------------------------------
		Matrix3D cubeV_matrix = new Matrix3D();
		gl.glUseProgram(rendering_cube_map);

		//  put the V matrix into the corresponding uniforms
		cubeV_matrix = (Matrix3D) v_matrix.clone();
		cubeV_matrix.scale(1.0, -1.0, -1.4);
		int v_location = gl.glGetUniformLocation(rendering_cube_map, "v_matrix");
		gl.glUniformMatrix4fv(v_location, 1, false, cubeV_matrix.getFloatValues(), 0);
		
		// put the P matrix into the corresponding uniform
		int ploc = gl.glGetUniformLocation(rendering_cube_map, "p_matrix");
		gl.glUniformMatrix4fv(ploc, 1, false, proj_matrix.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, textureID2);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);
                
                
                
                //Torus, shuttle, and light--------------------------------------------------------
                
		gl.glUseProgram(rendering_program2);

                
		// draw the  first torus
		
		thisMaterial = graphicslib3D.Material.BRONZE;		
		
		mv_location = gl.glGetUniformLocation(rendering_program2, "mv_matrix");
		proj_location = gl.glGetUniformLocation(rendering_program2, "proj_matrix");
		n_location = gl.glGetUniformLocation(rendering_program2, "normalMat");
		int shadow_location = gl.glGetUniformLocation(rendering_program2,  "shadowMVP");
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
                
		m_matrix.translate(torusLoc.getX(),torusLoc.getY(),torusLoc.getZ());
		m_matrix.rotateX(25.0);

		//  build the VIEW matrix
		v_matrix.setToIdentity();
                v_matrix.rotateX(JLocX);
                v_matrix.rotateY(JLocY);
                v_matrix.rotateZ(JLocZ);
		v_matrix.translate(-camLocX,-camLocY,-camLocZ);
		
		installLights(rendering_program2, v_matrix);
		
		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		
		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
		// set up torus vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up torus normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);	
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
                
		gl.glDrawArrays(GL_TRIANGLES, 0, numTorusVertices);
                
                //Draw the 2nd Taurus
                thisMaterial = graphicslib3D.Material.GOLD;	
                
                installLights(rendering_program2, v_matrix);
                m_matrix.setToIdentity();
               
		m_matrix.translate(cubeLocX,cubeLocY,cubeLocZ);
               // System.out.println("xyz"+ lightLocX+ " " +lightLocY+" "+ lightLocZ);
                m_matrix.scale(0.54, 0.54, 0.54);
		//m_matrix.rotateX(25.0);
                mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		
		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
		// set up torus vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up torus normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);	
                
                //Passes Vertices for texture
                gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
                
                //implements Ice Texture
                gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, iceTexture);
                
		//gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
                
		gl.glDrawArrays(GL_TRIANGLES, 0, numTorusVertices);
                
               
                
                //Draw Light Box, Point where the light is coming from
                
                
                thisMaterial = graphicslib3D.Material.GOLD;	
                installLights(rendering_program2, v_matrix);
                m_matrix.setToIdentity();
               
		m_matrix.translate(lightLocX,-lightLocY,lightLocZ);
               // System.out.println("xyz"+ lightLocX+ " " +lightLocY+" "+ lightLocZ);
                m_matrix.scale(0.02, 0.02, 0.02);
		//m_matrix.rotateX(25.0);
                mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		
		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
		// set up torus vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up torus normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);	
	
                
                
                
		//gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
                
               // gl.glDisable (GL_TEXTURE_1D);
              //  gl.glDisable (GL_TEXTURE_2D);
              //  gl.glDisable (GL_TEXTURE_3D);
		gl.glDrawArrays(GL_TRIANGLES, 0, numTorusVertices);
                
                
                
		// draws the nasa shuttle
		
		thisMaterial = graphicslib3D.Material.SILVER;		
		installLights(rendering_program2, v_matrix);
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(pyrLoc.getX(),pyrLoc.getY(),pyrLoc.getZ());
                m_matrix.scale(2.5, 2.5, 2.5);
		m_matrix.rotateX(20.0);
		m_matrix.rotateY(30.0);

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// Texture Buffer
		
                gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
                
                gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, shuttleTexture);
                
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
                 
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
                
		gl.glDrawArrays(GL_TRIANGLES, 0, pyramid.getNumVertices());
            
                gl.glDisable(GL_TEXTURE_2D);
                
                
                //Creates boxes instancing
                gl.glUseProgram(rendering_program4);
                int m_loc = gl.glGetUniformLocation(rendering_program4, "m_matrix");
		int v_loc = gl.glGetUniformLocation(rendering_program4, "v_matrix");
		int proj_loc = gl.glGetUniformLocation(rendering_program4, "proj_matrix");
                Matrix3D vMat = new Matrix3D();
                vMat.translate(-0.0f, -0.0f, -0.0f);
                
                Matrix3D mMat = new Matrix3D();
		double timeFactor = (double) (System.currentTimeMillis()%3600000)/10000.0;
                Matrix3D pMat = perspective(50.0f, aspect, 0.1f, 1000.0f);
                
                gl.glUniformMatrix4fv(m_loc, 1, false, mMat.getFloatValues(), 0);
		gl.glUniformMatrix4fv(v_loc, 1, false, vMat.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
                
                int tf_loc = gl.glGetUniformLocation(rendering_program4, "tf");
		gl.glUniform1f(tf_loc, (float)timeFactor);
                
                gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		//gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

                if (instanceSwitch ==0){
		gl.glDrawArraysInstanced(GL_TRIANGLES, 0, 36, 10000);	// 0, 36, 24  when 24 instances
                } else if(instanceSwitch ==1){
                    
                    
                }
                
                //Renders 3rd program for axes
		gl.glUseProgram(rendering_program3);
                
                mv_location = gl.glGetUniformLocation(rendering_program3, "mv_matrix");
                proj_location = gl.glGetUniformLocation(rendering_program3, "proj_matrix");
                
                gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
                gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
                
                //Axes Switch off and On
                if (axesSwitch == 0 ){
                gl.glDrawArrays(GL_LINES, 0, 6);
                }else if (axesSwitch ==1){
                    
                    
                }
                
                
                
                
                //envioronment mapping torus
                gl.glUseProgram(rendering_program5);
                mv_location = gl.glGetUniformLocation(rendering_program5, "mv_matrix");
		proj_location = gl.glGetUniformLocation(rendering_program5, "proj_matrix");
		n_location = gl.glGetUniformLocation(rendering_program5, "normalMat");
                
                m_matrix.setToIdentity();
		m_matrix.translate(1.0,-1.0,-5.0);
		amt += 0.5f; m_matrix.rotateX(40.0); m_matrix.rotateZ(amt);
                
                //  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
                
                //  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, ((mv_matrix.inverse()).transpose()).getFloatValues(),0);
		
		// set up torus vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// set up torus normal buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, textureID2);
	
		//gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numTorusVertices);
                
                 //Moon tess height mapping
                gl.glUseProgram(render_tess_Program);
                
                int mvp_location = gl.glGetUniformLocation(render_tess_Program, "mvp");
                
                proj_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);
	
		m_matrix.setToIdentity();
		m_matrix.translate(1.0,0.0,-10.0);
                m_matrix.scale(100.0, 100.0, 100.0);
		//m_matrix.rotateZ(180.0f);
               // m_matrix.rotateZ(10.f);
                m_matrix.rotateX(-10.0f);
               // m_matrix.rotateY(10.0f);
		
		//v_matrix.setToIdentity();
		v_matrix.translate(-0,10.1,-0);
		
		mvp_matrix.setToIdentity();
		mvp_matrix.concatenate(proj_matrix);
		mvp_matrix.concatenate(v_matrix);
		mvp_matrix.concatenate(m_matrix);
		
		gl.glUniformMatrix4fv(mvp_location, 1, false, mvp_matrix.getFloatValues(), 0);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, tessTex);
	
		//gl.glFrontFace(GL_CCW);
                
                //gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		//gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
                

		gl.glPatchParameteri(GL_PATCH_VERTICES, 1);
		gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		gl.glDrawArrays(GL_PATCHES, 0, 1);
                
                gl.glUseProgram(render_bump);

		mv_location = gl.glGetUniformLocation(render_bump, "mv_matrix");
		proj_location = gl.glGetUniformLocation(render_bump, "proj_matrix");

		m_matrix.setToIdentity();
		m_matrix.translate(-1,0,0);
		m_matrix.rotateX(15.0f); m_matrix.rotateY(45.0f);

		//v_matrix.setToIdentity();
		v_matrix.translate(0,0,0);
		
		//mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
	
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_3D, textureID3);
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numObjVertices);
               
                
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		createShaderPrograms();
		setupVertices();
		setupShadowBuffers();
		//Shuttle Texture and Ice Texture
                joglShuttleTexture = loadTexture("spstob_1.jpg");
		shuttleTexture = joglShuttleTexture.getTextureObject();
                
                
           
                
                instanceCX = 1.0f; instanceCY = 1.0f; instanceCZ = 1.0f;
                textureID2 = loadCubeMap();
                
                //dolphin 3d
                generate3Dpattern();
                textureID3 = load3DTexture();
                
                gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
                
                
		gl.glBindTexture(GL_TEXTURE_2D, shuttleTexture);
                gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
                gl.glGenerateMipmap(GL_TEXTURE_2D);
                
                
                joglIceTexture = loadTexture("ice.jpg");
                iceTexture = joglIceTexture.getTextureObject();
                
                gl.glBindTexture(GL_TEXTURE_2D, iceTexture);
                gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
                gl.glGenerateMipmap(GL_TEXTURE_2D);
                
                moonTex = loadTexture("squareMoonMap.jpg");
                tessTex = moonTex.getTextureObject();
                
                gl.glBindTexture(GL_TEXTURE_2D, tessTex);
                gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
                gl.glGenerateMipmap(GL_TEXTURE_2D);
                
                
                b.setElementAt(0,0,0.5);b.setElementAt(0,1,0.0);b.setElementAt(0,2,0.0);b.setElementAt(0,3,0.5f);
		b.setElementAt(1,0,0.0);b.setElementAt(1,1,0.5);b.setElementAt(1,2,0.0);b.setElementAt(1,3,0.5f);
		b.setElementAt(2,0,0.0);b.setElementAt(2,1,0.0);b.setElementAt(2,2,0.5);b.setElementAt(2,3,0.5f);
		b.setElementAt(3,0,0.0);b.setElementAt(3,1,0.0);b.setElementAt(3,2,0.0);b.setElementAt(3,3,1.0f);
		
                     
                
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}
	
	public void setupShadowBuffers()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadow_buffer, 0);
	
		gl.glGenTextures(1, shadow_tex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
	}

// -----------------------------
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		setupShadowBuffers();
	}

	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
                cubeLocX = 1.0f;
                cubeLocY = 1.0f;
                cubeLocZ = 1.0f;
                
                
		// pyramid definition
		Vertex3D[] pyramid_vertices = pyramid.getVertices();
		numPyramidVertices = pyramid.getNumVertices();
                
                //dolphin stuff
                Vertex3D[] dolphinVertices = dolphin.getVertices();
                numObjVertices = dolphin.getNumVertices();
                float[] dolph = new float[numObjVertices*3];
                
                for (int i=0; i<numObjVertices; i++)
		{	dolph[i*3]   = (float) (dolphinVertices[i]).getX();
			dolph[i*3+1] = (float) (dolphinVertices[i]).getY();
			dolph[i*3+2] = (float) (dolphinVertices[i]).getZ();
		}
                
		float[] pyramid_vertex_positions = new float[numPyramidVertices*3];
		float[] pyramid_normals = new float[numPyramidVertices*3];
                float[] pyramid_texture_positions = new float[numPyramidVertices*2];
                
                float[] vertex_positions_point =
		{	-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
			-1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
			1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
		};
                
                
                
		for (int i=0; i<numPyramidVertices; i++)
		{	pyramid_vertex_positions[i*3]   = (float) (pyramid_vertices[i]).getX();			
			pyramid_vertex_positions[i*3+1] = (float) (pyramid_vertices[i]).getY();
			pyramid_vertex_positions[i*3+2] = (float) (pyramid_vertices[i]).getZ();
			
                        pyramid_texture_positions[i*2] =  (float) (pyramid_vertices[i]).getS();
                        pyramid_texture_positions[i*2+1] =(float) (pyramid_vertices[i]).getT();
			pyramid_normals[i*3]   = (float) (pyramid_vertices[i]).getNormalX();
			pyramid_normals[i*3+1] = (float) (pyramid_vertices[i]).getNormalY();
			pyramid_normals[i*3+2] = (float) (pyramid_vertices[i]).getNormalZ();
		
                        
                }

		Vertex3D[] torus_vertices = myTorus.getVertices();
		
		int[] torus_indices = myTorus.getIndices();	
		float[] torus_fvalues = new float[torus_indices.length*3];
		float[] torus_nvalues = new float[torus_indices.length*3];
                float[] torus_tvalues = new float[torus_indices.length*2];
		
		for (int i=0; i<torus_indices.length; i++)
		{	torus_fvalues[i*3]   = (float) (torus_vertices[torus_indices[i]]).getX();			
			torus_fvalues[i*3+1] = (float) (torus_vertices[torus_indices[i]]).getY();
			torus_fvalues[i*3+2] = (float) (torus_vertices[torus_indices[i]]).getZ();
			torus_tvalues[i*2]   = (float) (torus_vertices[torus_indices[i]]).getS();
                        torus_tvalues[i*2+1] = (float) (torus_vertices[torus_indices[i]]).getT();
			torus_nvalues[i*3]   = (float) (torus_vertices[torus_indices[i]]).getNormalX();
			torus_nvalues[i*3+1] = (float) (torus_vertices[torus_indices[i]]).getNormalY();
			torus_nvalues[i*3+2] = (float) (torus_vertices[torus_indices[i]]).getNormalZ();
		}
		
		numTorusVertices = torus_indices.length;

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		gl.glGenBuffers(8, vbo, 0);

		//  put the Torus vertices into the first buffer,
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(torus_fvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
		
		//  load the pyramid vertices into the second buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer pyrVertBuf = Buffers.newDirectFloatBuffer(pyramid_vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrVertBuf.limit()*4, pyrVertBuf, GL_STATIC_DRAW);
		
		// load the torus normal coordinates into the third buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer torusNorBuf = Buffers.newDirectFloatBuffer(torus_nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torusNorBuf.limit()*4, torusNorBuf, GL_STATIC_DRAW);
		
		// load the pyramid normal coordinates into the fourth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer pyrNorBuf = Buffers.newDirectFloatBuffer(pyramid_normals);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrNorBuf.limit()*4, pyrNorBuf, GL_STATIC_DRAW);
                
                gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer pointBuf = Buffers.newDirectFloatBuffer(vertex_positions_point);
		gl.glBufferData(GL_ARRAY_BUFFER,pointBuf.limit()*4, pointBuf, GL_STATIC_DRAW);
                
                //Texture Buffer for Shuttle
                gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
               FloatBuffer pyrTexBuf = Buffers.newDirectFloatBuffer(pyramid_texture_positions);
               gl.glBufferData(GL_ARRAY_BUFFER, pyrTexBuf.limit()*4, pyrTexBuf, GL_STATIC_DRAW);
               
               //Texture buffer for torus
               gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
            FloatBuffer torusTexBuf = Buffers.newDirectFloatBuffer(torus_tvalues);
            gl.glBufferData(GL_ARRAY_BUFFER, torusTexBuf.limit()*4, torusTexBuf, GL_STATIC_DRAW);

            gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer vertBufz = Buffers.newDirectFloatBuffer(dolph);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBufz.limit()*4, vertBuf, GL_STATIC_DRAW);
        }              
        
        
	
	private void installLights(int rendering_program, Matrix3D v_matrix)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		Material currentMaterial = new Material();
		currentMaterial = thisMaterial;
		
		Point3D lightP = currentLight.getPosition();
		Point3D lightPv = lightP.mult(v_matrix);
		
		float [] currLightPos = new float[] { (float) lightPv.getX(),
			(float) lightPv.getY(),
			(float) lightPv.getZ() };

		// get the location of the global ambient light field in the shader
		int globalAmbLoc = gl.glGetUniformLocation(rendering_program, "globalAmbient");
	
		// set the current globalAmbient settings
		gl.glProgramUniform4fv(rendering_program, globalAmbLoc, 1, globalAmbient, 0);

		// get the locations of the light and material fields in the shader
		int ambLoc = gl.glGetUniformLocation(rendering_program, "light.ambient");
		int diffLoc = gl.glGetUniformLocation(rendering_program, "light.diffuse");
		int specLoc = gl.glGetUniformLocation(rendering_program, "light.specular");
		int posLoc = gl.glGetUniformLocation(rendering_program, "light.position");

		int MambLoc = gl.glGetUniformLocation(rendering_program, "material.ambient");
		int MdiffLoc = gl.glGetUniformLocation(rendering_program, "material.diffuse");
		int MspecLoc = gl.glGetUniformLocation(rendering_program, "material.specular");
		int MshiLoc = gl.glGetUniformLocation(rendering_program, "material.shininess");

		// set the uniform light and material values in the shader
		gl.glProgramUniform4fv(rendering_program, ambLoc, 1, currentLight.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, diffLoc, 1, currentLight.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, specLoc, 1, currentLight.getSpecular(), 0);
		gl.glProgramUniform3fv(rendering_program, posLoc, 1, currLightPos, 0);
	
		gl.glProgramUniform4fv(rendering_program, MambLoc, 1, currentMaterial.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, MdiffLoc, 1, currentMaterial.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, MspecLoc, 1, currentMaterial.getSpecular(), 0);
		gl.glProgramUniform1f(rendering_program, MshiLoc, currentMaterial.getShininess());
	}

	public static void main(String[] args) { new Starter(); }

	@Override
	public void dispose(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) drawable.getGL();
		gl.glDeleteVertexArrays(1, vao, 0);
	}

//-----------------
        private void fillDataArray(byte data[])
	{ for (int i=0; i<texHeight; i++)
	  { for (int j=0; j<texWidth; j++)
	    { for (int k=0; k<texDepth; k++)
	      {
			if (tex3Dpattern[i][j][k] == 1.0)
			{	// yellow color
				data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+0] = (byte) 255; //red
				data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+1] = (byte) 255; //green
				data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+2] = (byte) 0; //blue
				data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+3] = (byte) 0; //alpha
			}
			else
			{	// blue color
				data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+0] = (byte) 0; //red
				data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+1] = (byte) 0; //green
				data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+2] = (byte) 255; //blue
				data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+3] = (byte) 0; //alpha
			}
	} } } }

	private int load3DTexture()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		byte[] data = new byte[texHeight*texWidth*texDepth*4];
		
		fillDataArray(data);

		ByteBuffer bb = Buffers.newDirectByteBuffer(data);

		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];

		gl.glBindTexture(GL_TEXTURE_3D, textureID);

		gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, texWidth, texHeight, texDepth);
		gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
				texWidth, texHeight, texDepth, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, bb);
		
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		return textureID;
	}
	/*
	void generate3Dpattern()
	{	for (int x=0; x<texHeight; x++)
		{	for (int y=0; y<texWidth; y++)
			{	for (int z=0; z<texDepth; z++)
				{	if ((y/10)%2 == 0)
						tex3Dpattern[x][y][z] = 0.0;
					else
						tex3Dpattern[x][y][z] = 1.0;
	}	}	}	}
	*/
	//  replace above function with the one below
	//	to change the stripes to a checkerboard.
	
	void generate3Dpattern()
{	int xStep, yStep, zStep, sumSteps;
	for (int x=0; x<texWidth; x++)
	{	for (int y=0; y<texHeight; y++)
		{	for (int z=0; z<texDepth; z++)
			{	xStep = (x / 10) % 2;
				yStep = (y / 10) % 2;
				zStep = (z / 10) % 2;
				sumSteps = xStep + yStep + zStep;
				if ((sumSteps % 2) == 0)
					tex3Dpattern[x][y][z] = 0.0;
				else
					tex3Dpattern[x][y][z] = 1.0;
	}	}	}	}
        
	private void createShaderPrograms()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		
                //Tesselation Shaders
                tessVert  = util.readShaderSource("a4/tess/tessVert.shader");
                tessFrag = util.readShaderSource("a4/tess/tessFrag.shader");
		tessC = util.readShaderSource("a4/tess/tessC.shader");
		tessE = util.readShaderSource("a4/tess/tessE.shader");
		

		int tessvShader  = gl.glCreateShader(GL_VERTEX_SHADER);
                int tessfShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		int tcShader = gl.glCreateShader(GL_TESS_CONTROL_SHADER);
		int teShader = gl.glCreateShader(GL_TESS_EVALUATION_SHADER);
	

		gl.glShaderSource(tessvShader, tessVert.length, tessVert, null, 0);
		gl.glShaderSource(tcShader, tessC.length, tessC, null, 0);
		gl.glShaderSource(teShader, tessE.length, tessE, null, 0);
		gl.glShaderSource(tessfShader, tessFrag.length, tessFrag, null, 0);

		gl.glCompileShader(tessvShader);
		gl.glCompileShader(tcShader);
		gl.glCompileShader(teShader);
		gl.glCompileShader(tessfShader);
                
                
                //3D object shaders
                bumpVert = util.readShaderSource("a4/3Dtex/shaderVert");
                bumpFrag = util.readShaderSource("a4/3Dtex/shaderFrag");
                
                int bumpvShader = gl.glCreateShader(GL_VERTEX_SHADER);
                int bumpfShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
                
                gl.glShaderSource(bumpvShader,bumpVert.length,bumpVert,null,0);
                gl.glShaderSource(bumpfShader,bumpFrag.length,bumpFrag,null,0);
                
                gl.glCompileShader(bumpvShader);
                gl.glCompileShader(bumpfShader);
                
                
		vBlinn1ShaderSource = util.readShaderSource("a4/blinnVert1.shader");
		vBlinn2ShaderSource = util.readShaderSource("a4/blinnVert2.shader");
		fBlinn2ShaderSource = util.readShaderSource("a4/blinnFrag2.shader");
                instanceFrag = util.readShaderSource("a4/instancefrag.shader");
                instanceVert = util.readShaderSource("a4/instancevert.shader");
                
                
                
                skyVert = util.readShaderSource("a4/skyboxVert.shader");
                cloudVert = util.readShaderSource("a4/vertC.shader");
                skyFrag = util.readShaderSource("a4/skyboxFrag.shader");
                cloudFrag = util.readShaderSource("a4/fragC.shader");
                
                axesFrag = util.readShaderSource("a4/axesF1.frag");
		axesVert = util.readShaderSource("a4/axesV1.vert");
                

		int vertexShader1 = gl.glCreateShader(GL_VERTEX_SHADER);
		int vertexShader2 = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader2 = gl.glCreateShader(GL_FRAGMENT_SHADER);
                int instanceShader1F = gl.glCreateShader(GL_FRAGMENT_SHADER);
                int instanceShader1V = gl.glCreateShader(GL_VERTEX_SHADER);
                
                
                
                int skyShaderV = gl.glCreateShader(GL_VERTEX_SHADER);
                int skyShaderF = gl.glCreateShader(GL_FRAGMENT_SHADER);
                int cubeCloudF = gl.glCreateShader(GL_FRAGMENT_SHADER);
                int cubeCloudV = gl.glCreateShader(GL_VERTEX_SHADER);
                
                int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
                int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);

                gl.glShaderSource(vShader, axesVert.length, axesVert, null, 0);
                gl.glShaderSource(fShader, axesFrag.length, axesFrag, null, 0);
                
                
                
		gl.glShaderSource(vertexShader1, vBlinn1ShaderSource.length, vBlinn1ShaderSource, null, 0);
		gl.glShaderSource(vertexShader2, vBlinn2ShaderSource.length, vBlinn2ShaderSource, null, 0);
		gl.glShaderSource(fragmentShader2, fBlinn2ShaderSource.length, fBlinn2ShaderSource, null, 0);
                gl.glShaderSource(instanceShader1V, instanceVert.length, instanceVert, null, 0);
		gl.glShaderSource(instanceShader1F, instanceFrag.length, instanceFrag, null, 0);
                
                gl.glShaderSource(skyShaderV, skyVert.length, skyVert, null,0);
                gl.glShaderSource(skyShaderF, skyFrag.length, skyFrag, null,0);
                
                gl.glShaderSource(cubeCloudV, cloudVert.length, cloudVert, null, 0);
                gl.glShaderSource(cubeCloudF , cloudFrag.length, cloudFrag, null,0);
                
		gl.glCompileShader(vertexShader1);
		gl.glCompileShader(vertexShader2);
		gl.glCompileShader(fragmentShader2);
                gl.glCompileShader(instanceShader1V);
                gl.glCompileShader(instanceShader1F);
               
                gl.glCompileShader(skyShaderV);
                gl.glCompileShader(skyShaderF);
                
                gl.glCompileShader(cubeCloudV);
                gl.glCompileShader(cubeCloudF);
                
                gl.glCompileShader(vShader);
                gl.glCompileShader(fShader);

		rendering_program1 = gl.glCreateProgram();
		rendering_program2 = gl.glCreateProgram();
                rendering_program3 = gl.glCreateProgram();
                rendering_program4 = gl.glCreateProgram();
                rendering_program5 = gl.glCreateProgram();
                rendering_cube_map = gl.glCreateProgram();
                render_tess_Program = gl.glCreateProgram();
                render_bump = gl.glCreateProgram();

		gl.glAttachShader(rendering_program1, vertexShader1);
		gl.glAttachShader(rendering_program2, vertexShader2);
		gl.glAttachShader(rendering_program2, fragmentShader2);
                
                //attach Axes
                gl.glAttachShader(rendering_program3, vShader);
                gl.glAttachShader(rendering_program3, fShader);
                
                //Attach Instancing boxes
                gl.glAttachShader(rendering_program4, instanceShader1V);
                gl.glAttachShader(rendering_program4, instanceShader1F);
                //Skybox and environment map attach
                gl.glAttachShader(rendering_program5, skyShaderV);
                gl.glAttachShader(rendering_program5, skyShaderF);
                gl.glAttachShader(rendering_cube_map, cubeCloudV);
                gl.glAttachShader(rendering_cube_map, cubeCloudF);
                //tess attach
                gl.glAttachShader(render_tess_Program,tessvShader);
                gl.glAttachShader(render_tess_Program,tessfShader);
                gl.glAttachShader(render_tess_Program,tcShader);
                gl.glAttachShader(render_tess_Program,teShader);
                gl.glAttachShader(render_bump,bumpvShader);
                gl.glAttachShader(render_bump,bumpfShader);
                
		gl.glLinkProgram(rendering_program1);
		gl.glLinkProgram(rendering_program2);
                gl.glLinkProgram(rendering_program3);
                gl.glLinkProgram(rendering_program4);
                gl.glLinkProgram(rendering_program5);
                gl.glLinkProgram(rendering_cube_map);
                gl.glLinkProgram(render_tess_Program);
                gl.glLinkProgram(render_bump);
	}

//------------------
        
        
        
        private int loadCubeMap()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		GLProfile glp = gl.getGLProfile();
		Texture tex = new Texture(GL_TEXTURE_CUBE_MAP);
		
		try {
			TextureData topFile = TextureIO.newTextureData(glp, new File("Stars/top.jpg"), false, "jpg");
			TextureData leftFile = TextureIO.newTextureData(glp, new File("Stars/left.jpg"), false, "jpg");
			TextureData fntFile = TextureIO.newTextureData(glp, new File("Stars/center.jpg"), false, "jpg");
			TextureData rightFile = TextureIO.newTextureData(glp, new File("Stars/right.jpg"), false, "jpg");
			TextureData bkFile = TextureIO.newTextureData(glp, new File("Stars/back.jpg"), false, "jpg");
			TextureData botFile = TextureIO.newTextureData(glp, new File("Stars/bottom.jpg"), false, "jpg");
			
			tex.updateImage(gl, rightFile, GL_TEXTURE_CUBE_MAP_POSITIVE_X);
			tex.updateImage(gl, leftFile, GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
			tex.updateImage(gl, botFile, GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
			tex.updateImage(gl, topFile, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
			tex.updateImage(gl, fntFile, GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
			tex.updateImage(gl, bkFile, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
		} catch (IOException|GLException e) {}
		
		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = tex.getTextureObject();
		
		// reduce seams
		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
                
		return textureID;
	}
	private Matrix3D perspective(float fovy, float aspect, float n, float f)
	{	float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B = (n + f) / (n - f);
		float C = (2.0f * n * f) / (n - f);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0,0,A);
		r.setElementAt(1,1,q);
		r.setElementAt(2,2,B);
		r.setElementAt(3,2,-1.0f);
		r.setElementAt(2,3,C);
		r.setElementAt(3,3,0.0f);
		return r;
	}
       
	private Matrix3D lookAt(Point3D eye, Point3D target, Vector3D y)
	{	Vector3D eyeV = new Vector3D(eye);
		Vector3D targetV = new Vector3D(target);
		Vector3D fwd = (targetV.minus(eyeV)).normalize();
		Vector3D side = (fwd.cross(y)).normalize();
		Vector3D up = (side.cross(fwd)).normalize();
		Matrix3D look = new Matrix3D();
		look.setElementAt(0,0, side.getX());
		look.setElementAt(1,0, up.getX());
		look.setElementAt(2,0, -fwd.getX());
		look.setElementAt(3,0, 0.0f);
		look.setElementAt(0,1, side.getY());
		look.setElementAt(1,1, up.getY());
		look.setElementAt(2,1, -fwd.getY());
		look.setElementAt(3,1, 0.0f);
		look.setElementAt(0,2, side.getZ());
		look.setElementAt(1,2, up.getZ());
		look.setElementAt(2,2, -fwd.getZ());
		look.setElementAt(3,2, 0.0f);
		look.setElementAt(0,3, side.dot(eyeV.mult(-1)));
		look.setElementAt(1,3, up.dot(eyeV.mult(-1)));
		look.setElementAt(2,3, (fwd.mult(-1)).dot(eyeV.mult(-1)));
		look.setElementAt(3,3, 1.0f);
		return(look);
	}

         public Texture loadTexture(String textureFileName)
	{	Texture tex = null;
		try { tex = TextureIO.newTexture(new File(textureFileName), false); }
		catch (Exception e) { e.printStackTrace(); }
		return tex;
	}
    @Override
    public void keyTyped(KeyEvent e) {
     //   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    //All inputs
    @Override
    public void keyPressed(KeyEvent e) {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        if(e.getKeyCode() == KeyEvent.VK_W){
           System.out.println("Camera Forward");
           camLocZ= camLocZ-1.0f;
       }
       if(e.getKeyCode() == KeyEvent.VK_S){
           System.out.println("Camera Back");
           camLocZ= camLocZ+1.0f;
       }
       if(e.getKeyCode() == KeyEvent.VK_A){
           System.out.println("Camera Strafe Left");
           camLocX = camLocX -1.0f;
          
           
       }
       if(e.getKeyCode() == KeyEvent.VK_D){
           System.out.println("Camera Strafe Right");
           
           camLocX = camLocX +1.0f;
       }
       if(e.getKeyCode() == KeyEvent.VK_E){
           System.out.println("Camera Down");
             camLocY= camLocY-1.0f;
          
       }
       if(e.getKeyCode() == KeyEvent.VK_Q){
           System.out.println("Camera Down");
            camLocY= camLocY+1.0f;
           
       }
       if(e.getKeyCode() == KeyEvent.VK_LEFT){
           System.out.println("Camera Rotate Left");
           
           
           JLocY = JLocY - 1.0f;
           //JEyeV= JEyeV-10.0f;
    
    
       }
       if(e.getKeyCode() == KeyEvent.VK_RIGHT){
           System.out.println("Camera Rotate Right");
           
           JLocY = JLocY + 1.0f;
           //JLocY = 1.0f;
          // JLocX = 0.0f;
        //   JEyeV= JEyeV+10.0f;
        
           
       }
        if(e.getKeyCode() == KeyEvent.VK_UP){
           System.out.println("Camera Rotate Up");
           
           JLocX = JLocX - 1.0f;
            //JLocY = 0.0f;
           //JLocX = 1.0f;
          // JEyeV= JEyeV-10.0f;
          
       }
       if(e.getKeyCode() == KeyEvent.VK_DOWN){
           System.out.println("Camera Rotate Down");
           JLocX = JLocX + 1.0f;
            // JLocY = 0.0f;
          // JLocX = 1.0f;
           // JEyeV=JEyeV+10.0f;
       }
       if(e.getKeyCode() == KeyEvent.VK_L){
           if (lightSwitch ==0){
               System.out.println("Lights Off");
             
               lightSwitch = 1;
           }else if( lightSwitch == 1){
           System.out.println("Lights On");
            
           lightSwitch = 0;
           
           }
       }
       if(e.getKeyCode() == KeyEvent.VK_O){
           if (axesSwitch ==0){
               System.out.println("Axes OFF");
             
               axesSwitch = 1;
           }else if( axesSwitch == 1){
           System.out.println("Axes On");
            
           axesSwitch = 0;
           
           }
       }
       if(e.getKeyCode() == KeyEvent.VK_I){
           if (instanceSwitch ==0){
               System.out.println("Instance Boxes OFF");
             
               instanceSwitch = 1;
           }else if( instanceSwitch == 1){
           System.out.println("Instance Boxes On");
            
           instanceSwitch = 0;
           
           }
       }
    }
    

    @Override
    public void keyReleased(KeyEvent e) {
    //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseDragged(MouseEvent e) {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseMoved(MouseEvent e) {
     //   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
           // System.out.println("position");
          
           
           
           lightLocX = 2*((float)e.getX()/(float) myCanvas.getWidth());
           
           lightLocY = 2*((float)e.getY()/(float) myCanvas.getHeight());
           
         //  System.out.println("X: " + lightLocX + "Y: " + lightLocY);
     
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
       
       if (e.getWheelRotation()== -1){
                lightLocZ= lightLocZ + 1.0f;
                            
                }else{
                  lightLocZ= lightLocZ - 1.0f;
                           
               }
       
        //System.out.println("Z: " + lightLocZ);
    }
}