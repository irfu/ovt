/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/Style.java,v $
  Date:      $Date: 2005/12/13 16:33:06 $
  Version:   $Revision: 2.7 $


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
 * Style.java
 *
 * Created on March 17, 2001, 5:19 PM
 */

package ovt.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

/**
 *
 * @author  ko
 * @version 
 */
public class Style extends Object {
    
    /** Holds value of property menuFont. */
    private static Font menuFont = new Font("Arial", Font.PLAIN, 12);
    
    /** Holds value of property sliderLablesFont. */
    private static Font sliderLabelsFont = new Font("Verdana", Font.PLAIN, 10);  
    
    private static Font dialogFont = new java.awt.Font ("Dialog", 0, 11);
    
    private static Font labelFont = new java.awt.Font ("Arial", Font.PLAIN, 12);
    
    private static final JTextField textField = new JTextField();
    
    /** Creates new Style*/
    public Style() {
    }

    public static Font getTextFont() {
      return textField.getFont();
    }
    
    /** Getter for property menuFont.
     * @return Value of property menuFont.
 */
    public static Font getMenuFont() {
        return menuFont;
    }
    
    /** Setter for property menuFont.
     * @param menuFont New value of property menuFont.
 */
    public static void setMenuFont(Font font) {
        menuFont = font;
    }
    
    /** Getter for property sliderLablesFont.
     * @return Value of property sliderLablesFont.
     */
    public static Font getSliderLabelsFont() {
        return sliderLabelsFont;
    }
    
    /** Setter for property sliderLablesFont.
     * @param sliderLablesFont New value of property sliderLablesFont.
     */
    public static void setSliderLabelsFont(Font font) {
        sliderLabelsFont = font;
    }
    
    /** Getter for property sliderLablesFont.
     * @return Value of property sliderLablesFont.
     */
    public static Font getLabelFont() {
        return labelFont;
    }
    
    public static Font getDialogFont() {
        return dialogFont;
    }
}
