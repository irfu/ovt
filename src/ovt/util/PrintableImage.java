/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/PrintableImage.java,v $
  Date:      $Date: 2009/10/27 12:14:36 $
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


package ovt.util;

import java.awt.*;
import java.awt.print.*;

/**
 *
 * @author  Oleg
 * @version
 */
public class PrintableImage implements Printable {
    
    /** Holds value of property image. */
    private Image image;
    
    /** Creates new PrintableImage */
    public PrintableImage(Image image) {
        setImage(image);
    }
    
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0)
            return Printable.NO_SUCH_PAGE;
        if (getImage() == null) {
            System.err.println("PrintImage::print(): image is null");
            return Printable.NO_SUCH_PAGE;
        }
        
        // Position image on
        Graphics2D gr = (Graphics2D) graphics;
        
        /* rotate image if not PORTRAIT orientation
        int orient = pageFormat.getOrientation();
        switch(orient) {
            case PageFormat.LANDSCAPE:
                System.out.println("rotating to LANDSCAPE");
                gr.rotate(Math.PI/2);
                break;
            case PageFormat.REVERSE_LANDSCAPE:
                System.out.println("rotating to REVERSE_LANDSCAPE");
                gr.rotate(-Math.PI/2);
                break;
            case PageFormat.PORTRAIT:
                System.out.println("rotating to PORTRAIT");
                break;
        }
         */
        
        gr.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        // Move left corner to center, than have to move back
        gr.translate(pageFormat.getImageableWidth() / 2, 
                     pageFormat.getImageableHeight() / 2);
        
        Dimension d = ImageOperations.getImageSize(getImage());
        if (d==null) {
            System.err.println("Can't obtain image size");
            return Printable.NO_SUCH_PAGE;
        }

        // scale
        double scale = Math.min(pageFormat.getImageableWidth()  / d.width,
                                pageFormat.getImageableHeight() / d.height);

        if (scale < 1.0) gr.scale(scale, scale);

        // move back, centering
        gr.translate(-d.width/2.0, -d.height/2.0);
        
        // draw image to printer context
        gr.drawImage(getImage(), 0, 0, null);
        
        //return Printable.NO_SUCH_PAGE;
        return Printable.PAGE_EXISTS;
    }
    
    /** Getter for property image.
     * @return Value of property image.
     */
    public Image getImage() {
        return image;
    }
    
    /** Setter for property image.
     * @param image New value of property image.
     */
    public void setImage(Image image) {
        this.image = image;
    }
}
