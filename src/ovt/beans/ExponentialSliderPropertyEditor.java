/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/ExponentialSliderPropertyEditor.java,v $
  Date:      $Date: 2003/09/28 17:52:33 $
  Version:   $Revision: 1.4 $


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
 * ExpotentialSliderPropertyEditor.java
 *
 * Created on August 9, 2001, 5:09 AM
 */

package ovt.beans;

import ovt.util.*;
import ovt.interfaces.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;

/**
 * Has JSlider as a component with a exponential scale.
 * @author  ko
 * @version 
 */
public class ExponentialSliderPropertyEditor extends SliderPropertyEditor {
    
    /** The distance between two big ticks is always 10. */
    private static final int MAJOR_TICK_SPACING = 10;
    
    /** Creates new ExpotentialSliderPropertyEditor with <CODE>numberOfMinorTicks=100</CODE> 
     * numberOfMinorTicks indicates number of intervals, on which slider is discretized
     */
    public ExponentialSliderPropertyEditor(BasicPropertyDescriptor pd, double minValue, double maxValue, double[] labelValues) {
        super(pd, minValue, maxValue, (maxValue-minValue)/100., labelValues);
    }

    
    /** Creates new ExpotentialSliderPropertyEditor
     * numberOfMinorTicks indicates number of intervals, on which slider is discretized.
     */
    public ExponentialSliderPropertyEditor(BasicPropertyDescriptor pd, double minValue, double maxValue, int numberOfMinorTicks, double[] labelValues ) {
        super(pd, minValue, maxValue, (maxValue-minValue)/(double)numberOfMinorTicks, labelValues);
    }
    
    /** Creates new ExpotentialSliderPropertyEditor
     * numberOfMinorTicks indicates number of intervals, on which slider is discretized.
     */
    public ExponentialSliderPropertyEditor(BasicPropertyDescriptor pd, double minValue, double maxValue, int numberOfMinorTicks, double[] labelValues, String[]labelTitles ) throws IllegalArgumentException {
        super(pd, minValue, maxValue, (maxValue-minValue)/(double)numberOfMinorTicks, labelValues, labelTitles);
    }
    /** Redefined for the exponential law */
    protected double value2x( double value) {
        return Math.log(value);
    }
    /** Redefined for the exponential law */
    protected double x2value( double x) {
        return Math.exp(x);
    }
    
    /** returns true */
    protected boolean snapToTicks() {
        return false;
    }
    
}
