/*
 * VisualizationPanel.java
 *
 * Created on September 27, 2000, 12:06 PM
 */

package ovt;

import ovt.graphics.*;
import ovt.event.*;
import ovt.interfaces.*;
import ovt.util.Utils;

import vtk.*;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;


/**
 *
 * @author  ko
 * @version
 */
public class VisualizationPanel extends vtkPanel implements RenPanel {
    
    private final XYZWindow xyzWindow;
    private Dimension oldSize = new Dimension();

    /** Returns Image from vtkRenderWindow
   * @return Image from vtkRenderWindow*/
    public Image getImage() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        if (tmpDir == null) tmpDir = ovt.OVTCore.getUserdataDir();
        String tempFile = Utils.getRandomFilename(tmpDir, ".bmp");
        //tempFile = "xyz.bmp";

        // write to temporary bmp file
        vtkBMPWriter writer = new vtkBMPWriter();
        vtkWindowToImageFilter windowToImageFilter = new vtkWindowToImageFilter();
        windowToImageFilter.SetInput(getRenderWindow());
        writer.SetInputData(windowToImageFilter.GetOutput());
        writer.SetFileName(tempFile);
        writer.Write();
        
        Image image = null;
        try {
            image = BmpDecoder.getImage(tempFile);    // throws IOException
        } catch (java.io.IOException e) {
            System.err.println("Error loading image in BmpDecoder - " + e);
        }
        new File(tempFile).delete();    // delete temprorary file
        return image;
    }
    
    private void checkSizeChanged() {
        Dimension size = getSize();
        if (!size.equals(oldSize)) {
            //System.out.println("Size changed");
            oldSize = size;
            try {
                getXYZWindow().getCore().getOutputLabel().updatePosition();
            }   catch(NullPointerException e) {
                System.err.println("Output label is null!");
            }
        }
    }

    private final CameraChangeSupport cameraChangeSupport;
    
    /** Creates new VisualizationPanel */
    public VisualizationPanel(XYZWindow xyzWindow) {
        super();
    this.cameraChangeSupport = new CameraChangeSupport (this);
        this.xyzWindow = xyzWindow;
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        fireCameraChange(new CameraEvent());
        //System.out.println("Mouse released!!!!");
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        // DO NOTHING here! 
        // no requestFocus() anyMore!
    }

    public void fireCameraChange(CameraEvent evt) {
        cameraChangeSupport.fireCameraChange(evt);
    }
    
    public void addCameraChangeListener(CameraChangeListener l) {
        cameraChangeSupport.addCameraChangeListener (l);
    }
    
    public void removeCameraChangeListener(CameraChangeListener l) {
        cameraChangeSupport.removeCameraChangeListener (l);
    }
    
    @Override
    public synchronized void Render() {
        checkSizeChanged(); // if size changed -> reposition label
        cam = ren.GetActiveCamera();
        
        //getRenderer().ResetCameraClippingRange(-1000,1000,-1000,1000,-1000,1000);
        //getRenderer().ResetCameraClippingRange();
        //cam.ComputeViewPlaneNormal();
        //cam.SetClippingRange(0.01, 1000.01);
        //System.out.println("Reset camera clipping");
        
        lgt.SetPosition(cam.GetPosition());
        lgt.SetFocalPoint(cam.GetFocalPoint());
        super.Render();
    }
    
    public XYZWindow getXYZWindow() {
        return xyzWindow;
    }
    
    /** Returns the light, which is copuled to camera
   * @return vtkLight */
    @Override
    public vtkLight getCameraLight() {
        return lgt;
    }
    
    /** Overriding method, because otherwise it always returns getSize()
   * @return  Dimension*/
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0,0);
    }
    
    public vtkRenderer getRenderer() {
    	return GetRenderer();
    }
    
    public vtkRenderWindow getRenderWindow() {
    	return GetRenderWindow();
    }
    
    
}

class CameraChangeSupport {
    
    private final Vector listeners;
    private Object source;
    
    CameraChangeSupport(Object source) {
    this.source = null;
    this.listeners = new Vector();
        this.source = source;
    }
    
    public void addCameraChangeListener (CameraChangeListener listener) {
        listeners.addElement(listener);
    }
    
    public void removeCameraChangeListener (CameraChangeListener listener) {
        listeners.removeElement(listener);
    }
    
    public void fireCameraChange(CameraEvent evt) {
        Enumeration e = listeners.elements();
        fireCameraChange(evt, e);
    }
    
  /** Deliver event evt to all elements of enumeration e */
    public static void fireCameraChange(CameraEvent evt, Enumeration e) {
        CameraChangeListener cameraListener;
        while (e.hasMoreElements()) {
            try {
                cameraListener = ((CameraChangeListener)(e.nextElement()));
                if (OVTCore.DEBUG > 0) {
                    try {
                        System.out.println("TimeChangeEvent ->" + ((NamedObject)cameraListener).getName());
                    } catch (ClassCastException e2) {}
                }
                cameraListener.cameraChanged(evt);
            } catch (ClassCastException e2) {}
        }
    }
  /**
   * public void fireTimeChange(String property, Object oldValue, Object newValue, Vector timeMap) {
   * TimeEvent evt = new TimeEvent(source, property, oldValue, newValue, timeMap);
   * fireTimeChange(evt);
   * }*/
    
    public boolean hasListener(CameraChangeListener listener) {
        return listeners.contains(listener);
    }
    
}
