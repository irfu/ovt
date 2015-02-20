/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/ModelTypes.java,v $
  Date:      $Date: 2003/09/28 17:52:44 $
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

/*
 * ModelType.java
 *
 * Created on March 24, 2000, 3:28 PM
 */
 
package ovt.mag;

/** 
 *
 * @author  root
 * @version 
 */

public class ModelTypes {
  protected int internal;
  protected int external;
   
  /** Creates new ModelType */
  public ModelTypes(int internal,int external) {
    this.internal = internal;
    this.external = external;
  }
  
  public ModelTypes(ModelTypes modelT) { //added by kono
    this.internal = modelT.getInternal();
    this.external = modelT.getExternal();
  }
  
  public int getInternal() {
    return internal;
  }
  
  public int getExternal() {
    return external;
  }
}
