/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/ImageOperations.java,v $
  Date:      $Date: 2003/09/28 17:52:55 $
  Version:   $Revision: 2.4 $
 
 
Copyright (c) 2000-2003 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev,
Yuri Khotyaintsev)
All rights reserved.
 
Redistribution and use in source and binary forms, with or without
modification is permitted provided that the following conditions are met:
 
 * No part of the software can be included in any commercial package without
written consent from the OVT team.
 
 * Redistributions of the source or binary code must retain the above
copyright notice, this list of conditions and the following disclaimer.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS
IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT OR
INDIRECT DAMAGES  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 
OVT Team (http://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev
 
=========================================================================*/

/*
 * ImageOperations.java
 *
 * Created on March 21, 2000, 7:04 PM
 */

package ovt.util;

import ovt.*;
import ovt.object.*;

import vtk.*;

import java.io.*;
import java.awt.*;
import java.awt.print.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File; 
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;


/**
 *
 * @author  root
 * @version
 */
public class ImageOperations {
    public static final String COPYRIGHT = "Produced by OVT ("+ovt.OVTCore.ovtHomePage+")";
    public static final String PRINT_JOB_NAME = "OVT printing";
    
    private static final String DEFAULT_IMAGE_FILE = "Image.File";
    public static void exportImage2old(OVTCore core, String filename) throws Exception {
        
        
        
        String ext = FilenameUtils.getExtension(filename);
        
       
        XYZWindow frameOwner = core.getXYZWin();
        
        /* method 1
        //whole screen
         Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        //VTK window
         screenRect = frameOwner.getRenpanel().getComponent().getBounds();
         
        // Program window
        screenRect = frameOwner.getCore().getXYZWin().getBounds();
         
         BufferedImage capture = new Robot().createScreenCapture(screenRect);
        //ImageIO.write(capture, ext, new File(filename));
        */
        

       //WIDTH, HEIGHT = 1920, 1080

         /* method 2, sphere
        vtkSphereSource source = new vtkSphereSource();

        //source = vtk.vtkSphereSource()
        source.SetCenter(0, 0, 0);
        source.SetRadius(5);

        vtkPolyDataMapper mapper = new vtkPolyDataMapper();

        
        //core.getXYZWin().getVisualizationPanel().getRenderer().GetVTKWindow()
        //mapper = vtk.vtkPolyDataMapper()
        mapper.SetInputConnection(source.GetOutputPort());
        mapper.ImmediateModeRenderingOff();

        vtkActor actor = new vtkActor();

        //actor = vtk.vtkActor()
        actor.SetMapper(mapper);

        vtkRenderer renderer = new vtkRenderer();
        //renderer = vtk.vtkRenderer()
        renderer.SetBackground(1, 1, 1);
        renderer.AddActor(actor);
        renderer.ResetCamera();

        vtkRenderWindow renwin = new vtkRenderWindow();
        //renwin = vtk.vtkRenderWindow()
        renwin.OffScreenRenderingOn();
        renwin.SetSize(1920, 1080);
        renwin.AddRenderer(renderer);

        //#interactor = vtk.vtkRenderWindowInteractor()
        renwin.Render();
        vtkRenderWindowInteractor interactor = new vtkRenderWindowInteractor();



        interactor.SetRenderWindow(renwin);

        //renwin.Render();

        vtkWindowToImageFilter w2if = new vtkWindowToImageFilter();

        w2if.SetInputBufferTypeToRGBA();
        w2if.SetMagnification(1);
        w2if.SetInput(renwin);
        w2if.ShouldRerenderOff();
        w2if.ReadFrontBufferOff();


        w2if.Update();
        vtkImageData image = w2if.GetOutput();

        vtkPNGWriter writer2 = new vtkPNGWriter();
        writer2.SetInputConnection(w2if.GetOutputPort());
        interactor.Render();
        w2if.Modified();
        w2if.Update();

        writer2.SetFileName("frame.png");
        writer2.Write();
         */

         
    vtkRenderer renderer = frameOwner.getRenpanel().getRenderer();
    renderer.ResetCamera();

    vtkRenderWindow renwin = new vtkRenderWindow();
    //vtkRenderWindow renwin = frameOwner.getRenderWindow();
    //renwin = vtk.vtkRenderWindow()
    renwin.OffScreenRenderingOn();
    renwin.SetSize(1920, 1080); 
    renwin.AddRenderer(renderer);

    vtkRenderWindowInteractor interactor = new vtkGenericRenderWindowInteractor();

//    vtkRenderWindowInteractor interactor = new vtkRenderWindowInteractor();
    //vtkRenderWindowInteractor interactor=frameOwner.getRenpanel().getRenderWindowInteractor();
    
    
    interactor.SetRenderWindow(renwin);
    renwin.Render();
    vtkWindowToImageFilter w2if = new vtkWindowToImageFilter();

    w2if.SetInputBufferTypeToRGBA();
    w2if.SetMagnification(1);
    w2if.SetInput(renwin);
    w2if.ShouldRerenderOff();
    w2if.ReadFrontBufferOff();


    w2if.Update();
    vtkImageData image = w2if.GetOutput();

    vtkPNGWriter writer2 = new vtkPNGWriter();
    writer2.SetInputConnection(w2if.GetOutputPort());
    interactor.Render();
    w2if.Modified();
    w2if.Update();

    writer2.SetFileName(filename);
    writer2.Write();
    
    //frameOwner.getRenpanel().getComponent().requestFocus();
    //renderer.ResetCamera();

        
        
        
    //capture frame
//    BufferedImage image = new Robot().createScreenCapture( 
//        new Rectangle( myframe.getX(), myframe.getY(), 
 //           myframe.getWidth(), myframe.getHeight() ) );
    
    
        
        
         
         
         //java.awt.Robot.
         
//        Lock();
/* should hav also worked
    vtkWindowToImageFilter w2if = new vtkWindowToImageFilter();
    
    w2if.ReadFrontBufferOff();
    w2if.SetInput(renderWindow);

    w2if.SetMagnification(3);
    w2if.Update();

    vtkPNGWriter writer = new vtkPNGWriter();
    writer.SetInputConnection(w2if.GetOutputPort());
    writer.SetFileName(filename);
    writer.Write();
*/
    }
    public static void exportImage2(OVTCore core, String filename) throws Exception {
        
        

         vtkImageWriter writer;
        //renderWindow.
        if (filename.endsWith(".bmp"))
            writer = new vtkBMPWriter();
        
        else if (filename.endsWith(".tif")  || filename.endsWith(".tiff"))
            writer = new vtkTIFFWriter();
        
        else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
            writer = new vtkJPEGWriter();
        
        else if (filename.endsWith(".png"))
       
            writer = new vtkPNGWriter();
      
        else throw new IllegalArgumentException("The graphics format is not supported"); // No known formats were selected.
       
        
        
    String ext = FilenameUtils.getExtension(filename);
    


    XYZWindow frameOwner = core.getXYZWin();
    vtkRenderWindow renderWindow = frameOwner.getRenderWindow();

        // Setup offscreen rendering
        vtkGraphicsFactory graphics_factory = new vtkGraphicsFactory();
        graphics_factory.SetOffScreenOnlyMode(1);
        graphics_factory.SetUseMesaClasses(1);

     //   vtkImagingFactory imf = new vtkImagingFactory();
     //   imf.SetUseMesaClasses(1);
    renderWindow.GetSize();
    //vtkRenderer renderer = frameOwner.getRenderer();
    vtkRenderer rendbackup = core.getRenderer();
    
    vtkRenderer renderer = rendbackup;

//renderer.ResetCamera();
    double[] oldbackground = renderer.GetBackground();
    renderer.SetBackground(1,1,1); // Background color white
    //renderer.SetBackground(core.getRenderer().GetBackground());
    vtkRenderWindow renwin = new vtkRenderWindow();
    //vtkRenderWindow renwin = frameOwner.getRenderWindow();
    //renwin = vtk.vtkRenderWindow()
    renwin.OffScreenRenderingOn();
    //renwin.SetSize(1920, 1080); 
    renwin.SetSize(renderWindow.GetSize());
    
    
    /*
    vtkRenderer rend2 = new vtkRenderer();

    vtkActorCollection collActs = renderer.GetActors();
    vtkMapper map = new vtkMapper();
    
      for (int i=0; i<collActs.GetNumberOfItems(); i++)
      {
          rend2.AddActor(collActs.GetNextActor());
          
      }
      
      */
    //  rend2.SetActiveCamera(renderer.GetActiveCamera());
    
    vtkRendererCollection collection = renderWindow.GetRenderers();
//      assemblyGetActors(collection);

    //collection.InitTraversal();
   // collection->InitTraversal();
    //vtkIdType i = new vtkIdType();
    
    //for(vtkIdType i = 0;i < collection.GetNumberOfItems(); i++)
    //for (int i=0; i<collection.GetNumberOfItems(); i++)renwin.AddRenderer(collection.GetNextItem());
    
  //for(i = 0; i < collection->GetNumberOfItems(); i++){    vtkActor::SafeDownCast(collection->GetNextProp())->GetProperty()->SetOpacity(0.5);}
    
    
    
    renderer.SetRenderWindow(renwin);
    renderer.Render();

    renwin.AddRenderer(renderer);

    //renderer.SetRenderWindow(renwin);
    //renderer.Render();
    //renwin.AddRenderer(renderer);

    vtkRenderWindowInteractor interactor = new vtkGenericRenderWindowInteractor();
 
    interactor.SetRenderWindow(renwin);
    renwin.Render();
    vtkWindowToImageFilter w2if = new vtkWindowToImageFilter();

    w2if.SetInputBufferTypeToRGBA();
    w2if.SetMagnification(2);
    w2if.SetInput(renwin);
    w2if.ShouldRerenderOff();
    w2if.ReadFrontBufferOff();


    w2if.Update();
    writer.SetInputConnection(w2if.GetOutputPort());

    //renderer->SetBackground(1,1,1); // Background color white

    interactor.Render();
    w2if.Modified();
    w2if.Update();

    writer.SetFileName(filename);
    writer.Write();
    
       
    //try to change everything back
    renwin.OffScreenRenderingOff();
    //renwin.RemoveRenderer(renderer);
    //renwin.Delete();
    //renderWindow.MakeCurrent();
    
    /*****
    collection.InitTraversal();
        for (int i=0; i<collection.GetNumberOfItems(); i++) {
            collection.GetNextItem().SetRenderWindow(renderWindow);
            collection.Render();
        };
*////
    
    rendbackup.SetRenderWindow(renderWindow);
    rendbackup.Render();
    renderWindow.Render();
    renderWindow.Modified();
    //renderWindow.AddRenderer(renderer);
    //interactor.Delete();
    //interactor.SetRenderWindow(renderWindow);
    //renderWindow.Render();
    //interactor.Render();
    //frameOwner.getRenpanel().getComponent().requestFocus();
    //interactor.Delete();
//    frameOwner.Render();
//    frameOwner.getRenpanel().resetCamera();
//    frameOwner.toFront(); 
    /*******/
          }
    
    public static void exportImage222(OVTCore core, String filename) throws Exception {
        
        

         vtkImageWriter writer;
        //renderWindow.
        if (filename.endsWith(".bmp"))
            writer = new vtkBMPWriter();
        
        else if (filename.endsWith(".tif")  || filename.endsWith(".tiff"))
            writer = new vtkTIFFWriter();
        
        else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
            writer = new vtkJPEGWriter();
        
        else if (filename.endsWith(".png"))
       
            writer = new vtkPNGWriter();
      
        else throw new IllegalArgumentException("The graphics format is not supported"); // No known formats were selected.
       
        
        
    String ext = FilenameUtils.getExtension(filename);
    


    XYZWindow frameOwner = core.getXYZWin();
    vtkRenderWindow renderWindow = frameOwner.getRenderWindow();

        // Setup offscreen rendering
        vtkGraphicsFactory graphics_factory = new vtkGraphicsFactory();
        graphics_factory.SetOffScreenOnlyMode(1);
        graphics_factory.SetUseMesaClasses(1);
        
        
        renderWindow.OffScreenRenderingOn();

    renderWindow.GetSize();
    //vtkRenderer renderer = frameOwner.getRenderer();
    vtkRenderer renderer = core.getRenderer();
    
//renderer.ResetCamera();
    renderer.SetBackground(1,1,1); // Background color white
    //renderer.SetBackground(core.getRenderer().GetBackground());
    //vtkRenderWindow renwin = new vtkRenderWindow();
    //vtkRenderWindow renwin = frameOwner.getRenderWindow();
    //renwin = vtk.vtkRenderWindow()
    //renwin.OffScreenRenderingOn();
    //renwin.SetSize(1920, 1080); 
    //renwin.SetSize(renderWindow.GetSize());
    
    
    /*
    vtkRenderer rend2 = new vtkRenderer();

    vtkActorCollection collActs = renderer.GetActors();
    vtkMapper map = new vtkMapper();
    
      for (int i=0; i<collActs.GetNumberOfItems(); i++)
      {
          rend2.AddActor(collActs.GetNextActor());
          
      }
      
      */
    //  rend2.SetActiveCamera(renderer.GetActiveCamera());
    
    vtkRendererCollection collection = renderWindow.GetRenderers();
//      assemblyGetActors(collection);

    //collection.InitTraversal();
   // collection->InitTraversal();
    //vtkIdType i = new vtkIdType();
    
    //for(vtkIdType i = 0;i < collection.GetNumberOfItems(); i++)
    //for (int i=0; i<collection.GetNumberOfItems(); i++)renwin.AddRenderer(collection.GetNextItem());
    
  //for(i = 0; i < collection->GetNumberOfItems(); i++){    vtkActor::SafeDownCast(collection->GetNextProp())->GetProperty()->SetOpacity(0.5);}
    
    
   
    renderer.SetRenderWindow(renderWindow);
    //renderer.Render();

    //renwin.AddRenderer(renderer);

    //renderer.SetRenderWindow(renwin);
    //renderer.Render();
    //renwin.AddRenderer(renderer);

    vtkRenderWindowInteractor interactor = new vtkGenericRenderWindowInteractor();
 
    interactor.SetRenderWindow(renderWindow);
    renderWindow.Render();
    vtkWindowToImageFilter w2if = new vtkWindowToImageFilter();

    w2if.SetInputBufferTypeToRGBA();
    w2if.SetMagnification(2);
    w2if.SetInput(renderWindow);
    w2if.ShouldRerenderOff();
    w2if.ReadFrontBufferOff();


    w2if.Update();
    writer.SetInputConnection(w2if.GetOutputPort());

    //renderer->SetBackground(1,1,1); // Background color white

    interactor.Render();
    w2if.Modified();
    w2if.Update();

    writer.SetFileName(filename);
    writer.Write();
    
       
    //try to change everything back
    renderWindow.OffScreenRenderingOff();
    //renwin.RemoveRenderer(renderer);
    //renwin.Delete();
    //renderWindow.MakeCurrent();
    
    /*****
    collection.InitTraversal();
        for (int i=0; i<collection.GetNumberOfItems(); i++) {
            collection.GetNextItem().SetRenderWindow(renderWindow);
            collection.Render();
        };
*////
    
    //renderer.SetRenderWindow(renderWindow);
    //renderer.Render();
    renderWindow.Render();
    renderWindow.Modified();
    //renderWindow.AddRenderer(renderer);
    //interactor.Delete();
    //interactor.SetRenderWindow(renderWindow);
    //renderWindow.Render();
    //interactor.Render();
    //frameOwner.getRenpanel().getComponent().requestFocus();
    //interactor.Delete();
//    frameOwner.Render();
//    frameOwner.getRenpanel().resetCamera();
//    frameOwner.toFront(); 
    /*******/
          }
    public static void exportImage(vtkRenderWindow renderWindow, String filename) {
        vtkImageWriter writer;
        //renderWindow.
        if (filename.endsWith(".bmp"))
            writer = new vtkBMPWriter();
        
        else if (filename.endsWith(".tif")  || filename.endsWith(".tiff"))
            writer = new vtkTIFFWriter();
        
        else if (filename.endsWith(".pnm"))
            writer = new vtkPNMWriter();
        
        else if (filename.endsWith(".png"))
        
           writer = new vtkPNGWriter();
      

        else throw new IllegalArgumentException("The graphics format is not supported"); // No known formats were selected.
        
        //toFront(); // Places this window at the top of the stacking order and
        //shows it in front of any other windows.
        
        vtkWindowToImageFilter windowToImageFilter = new vtkWindowToImageFilter();
        // ---- Experimental code below /EJ ---------------------------------------------------------------        
        // The original code never produces a file, never produces any error message.
        // This is experimental code to try and fix that.  /Erik P G Johansson
        //renderWindow.Render();
        windowToImageFilter.SetInput(renderWindow);   // Original code
        //windowToImageFilter.Update();
        //windowToImageFilter.SetInputConnection(renderWindow.);
            vtkWindowToImageFilter w2if = new vtkWindowToImageFilter();
    
            //w2if.ReadFrontBufferOff();
            w2if.SetInput(renderWindow);

            w2if.SetMagnification(3);
            w2if.Update();

            vtkPNGWriter writer2 = new vtkPNGWriter();
            writer2.SetInputConnection(w2if.GetOutputPort());
            //writer2.SetFileName(filename);
            //writer2.Write();
            

   
        //windowToImageFilter.ReadFrontBufferOn();
        //windowToImageFilter.ReadFrontBufferOff();
        //windowToImageFilter.SetInputBufferTypeToRGB();
        //windowToImageFilter.SetMagnification(1);
        //renderWindow.Render();
        //windowToImageFilter.Modified();
        //windowToImageFilter.Modified();
        //writer.SetInputData(windowToImageFilter.GetOutput());   // Original code
        writer.SetInputConnection(windowToImageFilter.GetOutputPort());
        writer.SetFileName(filename);   // Original code
        //renderWindow.Render();
        //windowToImageFilter.Modified();
        // ---- Experimental code above ---------------------------------------------------------------
        
        writer.Write();
        
    }
        public static final void makeScreenshot(JOGLVisPanel renPanel) {
        
                        
    Rectangle rec = renPanel.getBounds();
    BufferedImage bufferedImage = new BufferedImage(rec.width, rec.height,
            BufferedImage.TYPE_INT_ARGB);
    renPanel.getComponent().paint(bufferedImage.getGraphics());

    try {
        // Create temp file.
        File temp = File.createTempFile("screenshot", ".png");

        // Use the ImageIO API to write the bufferedImage to a temporary file
        ImageIO.write(bufferedImage, "png", temp);

        //BufferedImage image = new Robot().createScreenCapture( 
        //new Rectangle( renPanel.getBounds() ) );
        
        
        // Delete temp file when program exits.
        temp.deleteOnExit();
    } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
    } // catch
} // makeScreenshot method
     public static void screenCapture(String filename) throws
           AWTException, IOException {
     // capture the whole screen
        
    String ext = FilenameUtils.getExtension(filename);
    //capture whole screen  
    BufferedImage screencapture = new Robot().createScreenCapture(
    new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()) );

    //capture frame
//    BufferedImage image = new Robot().createScreenCapture( 
//        new Rectangle( myframe.getX(), myframe.getY(), 
 //           myframe.getWidth(), myframe.getHeight() ) );
    
    
     File file = new File("filename");
     ImageIO.write(screencapture, ext, file);

  }
    public static void exportImageDialog(OVTCore core) {
        XYZWindow frameOwner = core.getXYZWin();
        vtkRenderWindow renderWindow = frameOwner.getRenderWindow();
        
        String defaultFile = OVTCore.getGlobalSetting(DEFAULT_IMAGE_FILE, core.getUserDir());
                
        JFileChooser chooser = new JFileChooser(new File(defaultFile));
        chooser.setDialogTitle("Export image");
        OvtExtensionFileFilter filter = new OvtExtensionFileFilter();
        filter.addExtension(".png");
        filter.setDescription("PNG file (*.png)");
        chooser.setFileFilter(filter);
        
        filter = new OvtExtensionFileFilter();
        filter.addExtension(".bmp");
        filter.setDescription("Windows Bitmap (*.bmp)");
        //chooser.setLocation(frameOwner.getLocation().x+frameOwner.getSize().width,frameOwner.getLocation().y);
        chooser.addChoosableFileFilter(filter);
      
        filter = new OvtExtensionFileFilter();
        filter.addExtension(".tiff");
        filter.addExtension(".tif");
        filter.setDescription("Tiff (*.tif)");
        chooser.addChoosableFileFilter(filter);
        
        filter = new OvtExtensionFileFilter();
        filter.addExtension(".jpg");
        filter.setDescription("JPG (*.jpg)");
        //chooser.setLocation(frameOwner.getLocation().x+frameOwner.getSize().width,frameOwner.getLocation().y);
        chooser.addChoosableFileFilter(filter);
        
        frameOwner.toFront(); // Places this window at the top of the stacking order and
        //shows it in front of any other windows.
        
        
        int returnVal = chooser.showSaveDialog(frameOwner);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String fname = chooser.getSelectedFile().getAbsolutePath();
            javax.swing.filechooser.FileFilter fileFilter = chooser.getFileFilter();
            // fileFilter can be (*.*). It is not OVTExtensionFilter
            // to avoid ClassCastException - if case ;-)
            if (fileFilter instanceof OvtExtensionFileFilter) {
                String ext = ((OvtExtensionFileFilter)fileFilter).getExtension();
                if (!fname.endsWith(ext)) fname += ext;
            }
            //System.out.println("You chose to open this file:" + fname);

            OutputLabel outputLabel = core.getOutputLabel();
            boolean oldVisible = outputLabel.isVisible();
            String oldText = outputLabel.getLabelText();
            if (oldVisible) outputLabel.setLabelText(oldText + "\n" + COPYRIGHT);
            else outputLabel.setLabelText(COPYRIGHT);
            outputLabel.setVisible(true);
            /* ********/
            //exportImage(renderWindow, fname);
            
            //makeScreenshot(frameOwner.getRenpanel());
            
            try{
                //screenCapture(fname);//this thing actually works, but is not preferred
                exportImage2(core, fname); //this kinda works, but makes window unusable.
                } catch (Exception ex) {System.out.println(ex.getMessage());
             }
            /* ********/
            OVTCore.setGlobalSetting(DEFAULT_IMAGE_FILE, fname);
            outputLabel.setVisible(oldVisible);
            outputLabel.setLabelText(oldText);
        }
        
    }
    
    public static void print(OVTCore core) {
        OutputLabel outputLabel = core.getOutputLabel();
        boolean oldVisible = outputLabel.isVisible();
        String oldText = outputLabel.getLabelText();
        if (oldVisible) outputLabel.setLabelText(oldText + "\n" + COPYRIGHT);
        else outputLabel.setLabelText(COPYRIGHT);
        outputLabel.setVisible(true);
        /*********/
        
        ovt.interfaces.RenPanel visualPanel = core.getXYZWin().getVisualizationPanel();

        try {
            //if (true) throw new NoClassDefFoundError();
            PrinterJob printJob = PrinterJob.getPrinterJob();
            Printable printable = new PrintableImage(visualPanel.getImage());
            printJob.setJobName(PRINT_JOB_NAME);
            printJob.setPrintable(printable);
            boolean pDialogState = printJob.printDialog();
            if (pDialogState) printJob.print();
        } catch (java.security.AccessControlException ace) {
            System.err.println("Can't access printer! " + ace);
        } catch (java.awt.print.PrinterException pe) {
            System.err.println("Printing error! " + pe);
        }
        catch (NoClassDefFoundError e) {
            System.out.println("Package awt.print.* not found. Using java 1.1 printing");
            /* old, printing invocation
            try {
                PrintVTKWindow.PrintVTKWindow(core.getXYZWin(), core.getXYZWin().getRenderWindow());
            } catch (IOException e2) {}
            */
            Toolkit tk = Toolkit.getDefaultToolkit();
            Image image = visualPanel.getImage();

            if (tk != null && image != null) {
                PrintJob printJob =  tk.getPrintJob(core.getXYZWin(), PRINT_JOB_NAME, null);
                if (printJob != null) {
                    Graphics gr = printJob.getGraphics();
                    if (gr != null) {
                        Dimension imageSize = getImageSize(image);
                        if (imageSize != null) {
                            scaleGraphics(gr, imageSize, printJob.getPageDimension());
                            /*TmpWindow tpw = new TmpWindow(image, imageSize); tpw.print(gr);*/
                            gr.drawImage(image, 0, 0, null);
                            gr.dispose();
                        }
                        else System.err.println("Can't obtain Image size");
                    }
                    else System.err.println("Graphics is null");
                    printJob.end();
                }
            }
            else System.err.println("ToolKit or Image is null");
        }
        
        /*********/
        outputLabel.setVisible(oldVisible);
        outputLabel.setLabelText(oldText);
    }
    
    public static void scaleGraphics(Graphics gr, Dimension image, Dimension page) {
        System.out.println("Scaling graphics:\nimage_size="+image+"\n page_size="+page);
        
        // Move left corner to center, than have to move back
        gr.translate(page.width / 2, page.height / 2);
        
        // scale to fit page
        double s = Math.min(page.width/image.width, page.height/image.height);
        if (s < 1.0) {
            System.out.println("Scaling to fit page");
            ((Graphics2D)gr).scale(s,s);
        }

        // move back, centering
        gr.translate(-image.width/2, -image.height/2);
    }

    /** Tries to obtain image size for 5 seconds */
    static public Dimension getImageSize(Image image) {
        int width, height;
        int tr = 0;
        for (int i=0; i<50; i++){
            width  = image.getWidth (null);
            height = image.getHeight(null);
            if (width != -1 && height != -1) return new Dimension(width, height);
            try { Thread.sleep(100); } catch(InterruptedException e) {}
        }
        return null;
    }
}

/* Temporary print window. Do not think we need it
class TmpWindow extends Frame {
    protected Image image;
    
    TmpWindow(Image image, Dimension imageSize) {
        this.image = image;
        setSize(new Dimension(image.getWidth(this), image.getHeight(this))); 
        //setSize(imageSize);
        setTitle("Print Preview");
        setLocation(0,0);
        setVisible(true);
    }
    
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }
}
*/
