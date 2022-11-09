/*
 * RenPanel.java
 *
 * Created on February 25, 2002, 2:42 PM
 */

package ovt.interfaces;

import java.awt.Image;
import vtk.vtkLight;
import vtk.vtkRenderer;

/**
 * The interface which implement OffscreenRenPanel and VisualizationPanel.
 * @author  ko
 * @version
 */
public interface RenPanel extends Renderable {

       /** Returns the light, which is copuled to camera */
    public vtkLight getCameraLight();

    public vtkRenderer getRenderer();

    public void addCameraChangeListener(CameraChangeListener l);

    public Image getImage();

    public int getWidth();

    public int getHeight();
}

