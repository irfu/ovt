/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/TemproraryPrintWindow.java,v $
  Date:      $Date: 2003/09/28 17:52:42 $
  Version:   $Revision: 2.3 $


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

package ovt.gui;

import java.awt.*;


public class TemproraryPrintWindow extends Frame {

protected Image im;

public TemproraryPrintWindow(String imageFileName) {

	super("Print Preview");
	
	im = Toolkit.getDefaultToolkit().getImage(imageFileName);
	//im = Toolkit.getDefaultToolkit().getImage("images/beach.gif");
	
	//setBounds(100,100,220,330);
	
	
	setSize(500,500);
	show();

	//setSize(im.getWidth(this), im.getHeight(this));
	System.out.println(imageFileName + ": Width = " + im.getWidth(this) + 
					   "Height = " + im.getHeight(this));
					   
}

public void paint(Graphics g) {

	g.drawImage(im, 20, 50, this);

}


}
