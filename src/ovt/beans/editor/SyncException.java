/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/beans/editor/SyncException.java,v $
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
 * SyncException.java
 *
 * Created on June 22, 2002, 5:19 PM
 */

package ovt.beans.editor;

import java.lang.*;

import java.awt.*;

/**
 *
 * @author  ko
 * @version 
 */
public class SyncException extends java.lang.Exception {

    private Component source;
    private Exception e;
    
    /**
 * Creates new <code>SyncException</code> without detail message.
     
    public SyncException(Component source) {
        this.source = source;
    }*/


    /**
 * Constructs an <code>SyncException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public SyncException(Component source, Exception e) {
        super(e.getMessage());
        this.source = source;
    }
    
    public Component getSource() {
        return source;
    }
    
    public Exception getException() {
        return e;
    }
    
    /*public String getMessage() {
        return e.get
    }*/
}


