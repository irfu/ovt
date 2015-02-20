/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/beans/editor/RepresentationEditor.java,v $
Date:      $Date: 2003/09/28 17:52:36 $
Version:   $Revision: 1.2 $


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
 * RepresentationEditor.java
 *
 * Created on June 25, 2002, 9:17 PM
 */

package ovt.beans.editor;

import java.beans.*;

/**
 * The PropertyEditor class for the vtkActor representation - POINT, WIREFRAME or SURFACE.
 * @author  ko
 * @version 
 */
public class RepresentationEditor extends PropertyEditorSupport {
    
    /** Wireframe representation (the same value as VTK_POINT) */
    public static final int POINT = 0;
    /** Wireframe representation (the same value as VTK_WIREFRAME) */
    public static final int WIREFRAME = 1;
    /** Surface representation (the same value as VTK_SURFACE) */
    public static final int SURFACE   = 2;
    
    private static final String[] tags = { "Point", "Wireframe", "Surface" };
    
    /** Creates new RepresentationEditor */
    public RepresentationEditor() {
    }

    public String[] getTags() {
        return tags;
    }
    
    public String getAsText() {
        int intValue = ((Integer)getValue()).intValue();
        return tags[intValue];
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        Integer value = null;
        for (int i=0; i<tags.length; i++) {
            if (text.equals(tags[i])) {
                setValue(new Integer(i));
                return;
            }
        }
        throw new IllegalArgumentException(text);
    }
    
    
}
