/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/editor/IntegerStyledDocument.java,v $
  Date:      $Date: 2002/04/12 12:31:54 $
  Version:   $Revision: 2.3 $


Copyright (c) 2000 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev, 
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

package ovt.object.editor;

import javax.swing.*; 
import javax.swing.text.*; 
import java.awt.Toolkit;

public class IntegerStyledDocument extends DefaultStyledDocument {

    public void insertString(int offs, String str, AttributeSet a) 
        throws BadLocationException {
        if ( isDigit(str) )
            super.insertString(offs, str, a);
        else
            Toolkit.getDefaultToolkit().beep();
    }
    
    private static boolean isDigit(String str){
      if (str != null )
      for (int i=0;i<str.length();i++)
        if(!java.lang.Character.isDigit(str.charAt(i))) return false;
      return true;
    }
}
