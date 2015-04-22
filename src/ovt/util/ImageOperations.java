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

/**
 *
 * @author  root
 * @version
 */
public class ImageOperations {
    public static final String COPYRIGHT = "Produced by OVT (http://ovt.irfu.se)";
    public static final String PRINT_JOB_NAME = "OVT printing";
    
    private static final String DEFAULT_IMAGE_FILE = "Image.File";
    
    public static void exportImage(vtkRenderWindow renderWindow, String filename) {
        vtkImageWriter writer;
        
        if (filename.endsWith(".bmp"))
            writer = new vtkBMPWriter();
        
        else if (filename.endsWith(".tif")  || filename.endsWith(".tiff"))
            writer = new vtkTIFFWriter();
        
        else if (filename.endsWith(".pnm"))
            writer = new vtkPNMWriter();
        
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
    
    public static void exportImageDialog(OVTCore core) {
        XYZWindow frameOwner = core.getXYZWin();
        vtkRenderWindow renderWindow = frameOwner.getRenderWindow();
        
        String defaultFile = OVTCore.getGlobalSetting(DEFAULT_IMAGE_FILE, core.getUserDir());
                
        JFileChooser chooser = new JFileChooser(new File(defaultFile));
        chooser.setDialogTitle("Export image");
        OvtExtensionFileFilter filter = new OvtExtensionFileFilter();
        filter.addExtension(".bmp");
        filter.setDescription("Windows Bitmap (*.bmp)");
        chooser.setFileFilter(filter);
        
        filter = new OvtExtensionFileFilter();
        filter.addExtension(".tiff");
        filter.addExtension(".tif");
        filter.setDescription("Tiff (*.tif)");
        chooser.addChoosableFileFilter(filter);
        
        filter = new OvtExtensionFileFilter();
        filter.addExtension(".pnm");
        filter.setDescription("PNM (*.pnm)");
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
            exportImage(renderWindow, fname);
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

    /** Tryes to obtain image size for 5 seconds */
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
