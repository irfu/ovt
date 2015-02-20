/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/OvtExtensionFileFilter.java,v $
  Date:      $Date: 2003/09/28 17:52:55 $
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

package ovt.util;

import java.util.Vector;
import java.util.Enumeration;
import java.io.File;

import javax.swing.filechooser.FileFilter;

public class OvtExtensionFileFilter extends FileFilter {

protected Vector extensions = new Vector();
protected String description;
protected boolean allExtensions = false; // this is true if '*' is added as ext.

public OvtExtensionFileFilter() {

}


public OvtExtensionFileFilter(String desc) {
	setDescription(desc);
}


public void setDescription(String desc) {
	description = desc;
}

public String getDescription() {
	return description;
}

public String getExtension() {
    return (String)extensions.firstElement();
}

public void addExtension(String ext) {
	if (ext.equals("*")) { allExtensions = true; return; }
	extensions.addElement(ext);
}


public boolean accept(File f) {
	boolean accept = false;
	String ext, name = f.getName();

	//System.out.println(name);	
	
	if (f.isDirectory()) return true;
	
	if (allExtensions) return true;
	
	
	Enumeration e = extensions.elements();
	
	while ((e.hasMoreElements()) && !accept) {
		
		ext = (String)e.nextElement();
		
		if (name.length() < ext.length()+2) return false;
		
		if (name.substring(name.length() - ext.length(), name.length()).equalsIgnoreCase(ext))
			accept = true;
	}

	return accept;
}

}
