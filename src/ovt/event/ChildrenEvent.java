/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/event/ChildrenEvent.java,v $
Date:      $Date: 2003/09/28 17:52:39 $
Version:   $Revision: 2.2 $


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
 * ChildrenEvent.java
 *
 * Created on June 23, 2002, 7:13 PM
 */

package ovt.event;

import ovt.object.OVTObject;
import ovt.datatype.Children;

/**
 * This event usually propagates from OVTObject.children to OVTNode which
 * will update the TreePanel (JTree).
 * @author  ko
 * @version 
 */
public class ChildrenEvent extends java.util.EventObject {

    public static final int CHILD_ADDED      = 1;
    public static final int CHILD_REMOVED    = 2;
    public static final int CHILDREN_CHANGED = 3;
    
    private int type;
    private OVTObject child;
    
    /** Creates new ChildrenEvent with type <CODE>CHILDREN_CHANGED</CODE> */
    public ChildrenEvent(Children source) {
        super(source);
        type = CHILDREN_CHANGED;
    }
    
    /** Creates new ChildrenEvent with type <CODE>CHILDREN_CHANGED</CODE> 
     *@param tyep can be <CODE>CHILD_ADDED</CODE> or <CODE>CHILD_REMOVED</CODE>
     *@param child the child being added/removed
     */
    public ChildrenEvent(Children source, int type, OVTObject child) {
        super(source);
        this.type = type;
        this.child = child;
    }
    
    public int getType() {
        return type;
    }
    
    public OVTObject getChild() {
        return child;
    }

}
