/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/BasicBeanDescriptor.java,v $
  Date:      $Date: 2003/09/28 17:52:32 $
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
 * BasicBeanDescriptor.java
 *
 * Created on March 29, 2001, 4:40 PM
 */

package ovt.beans;

import ovt.object.*;
import ovt.interfaces.*;

import java.beans.*;
import java.lang.reflect.*;

/**
 * This object is used to know the bean. ;-)
 * @author  ko
 * @version 
 */
public class BasicBeanDescriptor extends Object {

    private Field field;
    private String name;
    
    /** Creates new BasicBeanDescriptor */
    public BasicBeanDescriptor(String beanName, String fieldName, Class beanClass) throws NoSuchFieldException {
        name = beanName;
        field = beanClass.getField(fieldName);
    }
    
    public String getName() {
        return name;
    }
    
    public Object getValue(Object bean) throws IllegalArgumentException, IllegalAccessException {
        return field.get(bean);
    }

}
