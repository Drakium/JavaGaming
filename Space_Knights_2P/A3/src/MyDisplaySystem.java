/*
* Author: Beau Derrick and Christian Salano
* Date: 5/10/17
*/
package myGameEngine;

import javax.swing.JFrame;
import java.awt.GraphicsDevice;
import java.awt.Canvas;
import java.awt.*;
import sage.renderer.IRenderer;
import sage.display.IDisplaySystem;
import sage.display.DisplaySystem;
import sage.renderer.RendererFactory;

/**
 * A display system
 */
public class MyDisplaySystem implements IDisplaySystem
{
    private JFrame  myFrame;
    private GraphicsDevice device;
    private IRenderer myRenderer;
    private int width, height, bitDepth, refreshRate;
    private Canvas rendererCanvas;
    private boolean isCreated;
    private boolean isFullScreen;
    // constructor
    public MyDisplaySystem(int w, int h, int depth, int rate, boolean isFS, String rName)
    {
        //save the input parameters for accessor queries
        width = w;  height = h;  bitDepth = depth; refreshRate = rate;
        this.isFullScreen = isFS;
        //get a renderer from the RendererFactory
        myRenderer = RendererFactory.createRenderer(rName);
        if (myRenderer == null)
        {
            throw new RuntimeException("Unable to find renderer '" + rName + "'");
        }
        rendererCanvas = myRenderer.getCanvas();
        myFrame = new JFrame("Default Title");
        myFrame.add(rendererCanvas);
        //initialize the screen with the specified parameters
        DisplayMode displayMode = new DisplayMode(width, height, bitDepth, refreshRate);
        initScreen(displayMode, isFullScreen);
        //save DisplaySystem, show the frame and indicate DisplaySystem is created
        DisplaySystem.setCurrentDisplaySystem(this);
        myFrame.setVisible(true);
        isCreated = true;
    }
    
    private void initScreen(DisplayMode dispMode, boolean fullScreenRequested)
    {
        //get default screen device out of the local graphics environment
        GraphicsEnvironment environment = GraphicsEnvironment.
        getLocalGraphicsEnvironment();
        device = environment.getDefaultScreenDevice();
        if (device.isFullScreenSupported() && fullScreenRequested)
        {
            myFrame.setUndecorated(true);
            // suppress title bar, borders, etc.
            myFrame.setResizable(false);
            // full-screen so not resizeable
            myFrame.setIgnoreRepaint(true);
            // ignore AWT repaints
            // Put device in full-screen mode.  Note that this must be done BEFORE attempting
            // to change the DisplayMode; the application must first own the screen (i.e., has FSEM)
            device.setFullScreenWindow(myFrame);
            //try to set the full-screen device DisplayMode
            if (dispMode != null && device.isDisplayChangeSupported())
            {
                try
                {
                    device.setDisplayMode(dispMode);
                    myFrame.setSize(dispMode.getWidth(), dispMode.getHeight());
                } catch (Exception ex)
                {
                    System.err.println("Exception while setting device DisplayMode: " + ex );
                }
            } 
            else 
            {
                System.err.println("Cannot set display mode");
            }
             
        }
        else
        {
            //use windowed mode – set JFrame characteristics
            myFrame.setSize(dispMode.getWidth(),dispMode.getHeight());
            myFrame.setLocationRelativeTo(null);
             //centers window on screen
        }
    }
    
    public void setPredefinedCursor(int cursor){}
    public void setCustomCursor(String fileName){}
    public void convertPointToScreen(java.awt.Point p){}
    public boolean isShowing(){ return true;}
    public void addMouseMotionListener(java.awt.event.MouseMotionListener m){}
    public void addMouseListener(java.awt.event.MouseListener m){}
    public void addKeyListener(java.awt.event.KeyListener k)
    {
        
    }
    
    public boolean isFullScreen(){return isFullScreen;}
    public boolean isCreated(){return isCreated;}
    public void close()
    {
        if (device != null)
        { 
            Window window = device.getFullScreenWindow();
            if (window != null)
            { 
                window.dispose();
            }
            device.setFullScreenWindow(null);
        }     
    }
    public IRenderer getRenderer(){return myRenderer;}
    public void setTitle(java.lang.String title){ myFrame.setTitle(title);}
    public void setRefreshRate(int rate){ refreshRate = rate;}
    public void setBitDepth(int numBits){ bitDepth = numBits;}
    public void setHeight(int h){ height = h;}
    public void setWidth(int w){ width = w;}
    public int getRefreshRate(){return refreshRate;}
    public int getBitDepth(){return bitDepth;}
    public int getHeight(){return height;}
    public int getWidth(){return width;}
}
    
    