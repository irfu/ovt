package ovt;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import ovt.event.CameraEvent;
import ovt.interfaces.CameraChangeListener;
import vtk.rendering.jogl.vtkJoglCanvasComponent;
import vtk.vtkLight;
import vtk.vtkRenderWindow;
import vtk.vtkRenderer;

/**
 *
 * @author yuri
 */
public class JOGLVisPanel extends vtkJoglCanvasComponent implements ovt.interfaces.RenPanel {

  private final ovt.CameraChangeSupport cameraChangeSupport;
  public JOGLVisPanel() {
    super();
    this.cameraChangeSupport = new CameraChangeSupport (this);
  }

  @Override
  public vtkLight getCameraLight() {
    return null;
  }

  @Override
  public int getWidth() {
    return this.getComponent().getWidth();
  }

  @Override
  public int getHeight() {
    return this.getComponent().getHeight();
  }

  public Dimension getSize() {
    return this.getComponent().getSize();
  }


  public void fireCameraChange(CameraEvent evt) {
    cameraChangeSupport.fireCameraChange(evt);
  }

  @Override
  public void addCameraChangeListener(CameraChangeListener l) {
    cameraChangeSupport.addCameraChangeListener(l);
  }

  public void removeCameraChangeListener(CameraChangeListener l) {
    cameraChangeSupport.removeCameraChangeListener(l);
  }

  public Rectangle getBounds() {
    return this.getComponent().getBounds();
}

  @Override
  public Image getImage() {
    throw new UnsupportedOperationException("Not supported yet. "+getClass().getSimpleName()+"#getImage() not implemented.");
  }

}