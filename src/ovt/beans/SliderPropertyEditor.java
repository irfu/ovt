/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/SliderPropertyEditor.java,v $
  Date:      $Date: 2009/10/27 12:14:36 $
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

package ovt.beans;

import ovt.util.*;
import ovt.interfaces.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.beans.*;

import java.util.*;
import java.text.*;

/**
 * This is a ComponentPropertyEditor with a JSlider component, can edit int and double properties.
 * The class uses value transformation chain: value <-> x <-> sliderPos
 * That's why there are different limits for value, x, pos.
 * @author  ko
 * @version
 */
public class SliderPropertyEditor extends ComponentPropertyEditor implements PropertyChangeListener {
    
    /** slider == component */
    private JSlider slider = null; 
    
    private ChangeListener changeListener;
    
    /** Holds value of the minimal value the property. */
    private double minimumValue = 0.;
    
    /** Holds value of the maximum value of the property. */
    private double maximumValue =  100.;
   
    /** This is a interval in points between small ticks on JSlider.
     * Is measured in slider's coordinates.
     */
    private double minorTickSpacing = 1;
    
    /** This is a interval in points between big ticks on JSlider.
     * Is measured in slider's coordinates.
     */
    private double majorTickSpacing = 1;
   
    /** Indicates if the labels have to be shown. */
    private boolean labeled = false;
    
    /** if true - the edited property is double, if not - int */
    protected boolean isDoubleEditor = false;
    
    /** Indicates how many figures are behind the dot. By default 1. */
    private DecimalFormat format = new DecimalFormat("#.#");    
    
    private double[] labelValues;
    private String[] labelTitles;
   
    /** Creates new linear SliderPropertyEditor without labels. */
    public SliderPropertyEditor(BasicPropertyDescriptor pd, double minValue, double maxValue) {
        super(pd);
        initialize(minValue, maxValue);
    }

    /** Creates new linear SliderPropertyEditor without labels. 
    public SliderPropertyEditor(BasicPropertyDescriptor pd, double minValue, double maxValue, int numberOfMajorLabels) {
        super(pd);
        initialize(minValue, maxValue);
        setNumberOfMajorLabels(numberOfMajorLabels);
        setLabeled(true);
    }*/

    /** Creates new linear SliderPropertyEditor */
    public SliderPropertyEditor(BasicPropertyDescriptor pd, double minValue, double maxValue, double minorTickSpacing, double majorTickSpacing) {
        super(pd);
        initialize( minValue, maxValue);
        setMinorTickSpacing(minorTickSpacing);
        setMajorTickSpacing(majorTickSpacing);
        
        // determine precision from the acuracy of given majorTickSpacing
        String majorTS = ""+majorTickSpacing;
        int dotIndex = majorTS.indexOf(".");
        //Log.log("majorTickSpacing="+majorTickSpacing+" dotindex="+dotIndex);
        if (dotIndex != -1 && !majorTS.endsWith("0")) // ends with 0 when "92.0"
            setPrecision(majorTS.length() - dotIndex - 1);
        else 
            setPrecision(0);
        //Log.log("Precesion="+getPrecision());
        
        setLabeled(true);
    }
    
    /** Creates new linear SliderPropertyEditor */
    public SliderPropertyEditor(BasicPropertyDescriptor pd, double minValue, double maxValue, double minorTickSpacing, double[] labelValues) {
        super(pd);
        initialize( minValue, maxValue);
        setMinorTickSpacing(minorTickSpacing);
        this.labelValues = labelValues;
        
        // determine precision from the acuracy of given labels
        // examine all the labels and find the highest precesion
        int pr = 0; int maxPrecision = 0;
        for (int i=0; i<labelValues.length; i++) {
            String majorTS = ""+labelValues[i];
            int dotIndex = majorTS.indexOf(".");
            if (dotIndex != -1 && !majorTS.endsWith("0")) // ends with 0 when "92.0"
                pr = majorTS.length() - dotIndex - 1;
            else 
                pr = 0;
            if (pr > maxPrecision) maxPrecision = pr;
        }
        setPrecision(maxPrecision);
        
        setLabeled(true);
    }
    
    /** Creates new linear SliderPropertyEditor */
    public SliderPropertyEditor(BasicPropertyDescriptor pd, double minValue, double maxValue, 
            double minorTickSpacing, double[] labelValues, String[] labelTitles) throws IllegalArgumentException {
        super(pd);
        initialize( minValue, maxValue);
        setMinorTickSpacing(minorTickSpacing);
        this.labelValues = labelValues;
        this.labelTitles = labelTitles;
        
        if (labelValues.length != labelTitles.length) 
            throw new IllegalArgumentException("labelValues.length != labelTitles.length");
        
        // determine precision from the acuracy of given labels
        // examine all the labels and find the highest precesion
        int pr = 0; int maxPrecision = 0;
        for (int i=0; i<labelValues.length; i++) {
            String majorTS = ""+labelValues[i];
            int dotIndex = majorTS.indexOf(".");
            if (dotIndex != -1 && !majorTS.endsWith("0")) // ends with 0 when "92.0"
                pr = majorTS.length() - dotIndex - 1;
            else 
                pr = 0;
            if (pr > maxPrecision) maxPrecision = pr;
        }
        setPrecision(maxPrecision);
        
        setLabeled(true);
    }
    
    
    /** Creates new linear SliderPropertyEditor */
    public SliderPropertyEditor(BasicPropertyDescriptor pd, int minValue, int maxValue, int majorTickSpacing) {
        super(pd);
        initialize( minValue, maxValue);
        setMinorTickSpacing(majorTickSpacing);
        setMajorTickSpacing(majorTickSpacing);
        
    }
    
    /** Creates new linear SliderPropertyEditor */
    public SliderPropertyEditor(BasicPropertyDescriptor pd, int minValue, int maxValue, int majorTickSpacing, int minorTickSpacing) {
        super(pd);
        initialize( minValue, maxValue);
        setMinorTickSpacing(minorTickSpacing);
        setMajorTickSpacing(majorTickSpacing);
        
        setLabeled(true);
    }

    
    private void initialize(double minValue, double maxValue) {
        // check for the property type
        String srcType = getPropertyDescriptor().getWriteMethod().getParameterTypes()[0].getName();
        setMinimumValue(minValue);
        setMaximumValue(maxValue);
        if (srcType.equals("double"))
            isDoubleEditor = true;
            
    }

    protected double pos2x(int pos) {
        return getMinX() +  ((double)(pos - getMinimumSliderValue())) * (getMaxX() - getMinX()) / ((double)(getMaximumSliderValue() - getMinimumSliderValue()));
    }
    
    protected int x2pos(double x) {
        return (int)Math.round((double)(getMaximumSliderValue() - getMinimumSliderValue())/(getMaxX() - getMinX())*(x - getMinX())) + getMinimumSliderValue();
    }
    
    /** linear dependence */
    protected double value2x( double value) {
        return value;
    }
    
    /** linear dependence */
    protected double x2value( double x) {
        return x;
    }

    protected double calcValueFromSliderPosition(int pos) {
        return x2value(pos2x(pos));
    }
    
    protected int calcSliderPositionFromValue(double value) {
        return x2pos(value2x(value));
    }
    
    private double getValueFromSlider() {
        //Log.log("SliderPosition="+getSliderPosition()+" value="+calcValueFromSliderPosition(getSliderPosition()));
        return calcValueFromSliderPosition(getSliderPosition());
    }
    
    private int getSliderPosition() {
        return ((JSlider)component).getValue();
    }
    
    private int calcSliderPositionFromValue() {
        return calcSliderPositionFromValue(getValueAsDouble());
    }
    
    
    /** To get the value of the property one should use this method.
     * If the prperty is int - it is casted to double.
     */
    double getValueAsDouble() {
        Object obj = getValue();
        if (obj instanceof Integer) {
            return (double)((Integer) obj).intValue();
        } else if (obj instanceof Double) {
            return ((Double) obj).doubleValue();
        }
        return -1;
    }

    /** Getter for property min.
     * @return Value of property min.
     */
    public double getMinimumValue() {
        return minimumValue;
    }
    
    /** Setter for property min. Also set's minX
     * @param min New value of property min.
     */
    public void setMinimumValue(double min) {
        this.minimumValue = min;
    }
    
    /** Getter for property max.
     * @return Value of property max.
     */
    public double getMaximumValue() {
        return maximumValue;
    }
    
    /** Setter for property max. Also set's MaximumSliderValue
     * @param max New value of property max.
     */
    public void setMaximumValue(double max) {
        this.maximumValue = max;
    }
    
    public double getMinX() {
        return value2x(getMinimumValue());
    }
    
    public double getMaxX() {
        return value2x(getMaximumValue());
    }
    
    


        /** Getter for property minorTickSpacing.
     * @return Value of property minorTickSpacing.
     */
    public double getMinorTickSpacing() {
        return minorTickSpacing;
    }
    
    /** Setter for property minorTickSpacing.
     * @param minorTickSpacing New value of property minorTickSpacing.
 */
    public void setMinorTickSpacing(double minorTickSpacing) {
        this.minorTickSpacing = minorTickSpacing;
    }
    
    /** Getter for property majorTickSpacing for Slider
     * @return Value of property majorTickSpacing.
     
    protected int getMajorSliderTickSpacing() {
        return majorTickSpacing;
    }*/

    
    /** Getter for property majorTickSpacing.
     * @return Value of property majorTickSpacing.
     */
    public double getMajorTickSpacing() {
        return majorTickSpacing;
    }
    
    /** Setter for property majorTickSpacing.
     * @param majorTickSpacing New value of property majorTickSpacing.
     */
    public void setMajorTickSpacing(double majorTickSpacing) {
        this.majorTickSpacing = majorTickSpacing;
    }
    
/** Getter for property minimumSliderValue.
 * @return Value of property minimumSliderValue.
 */
    protected int getMinimumSliderValue() {
        return 0;
        /*if (isDoubleEditor) 
            return (int)Math.round(minimumValue*(precision + 1));
        else  // int editor
            return (int)minimumValue;*/
    }
    
/** Getter for property maximumSliderValue.
 * @return Value of property maximumSliderValue.
 */
    protected int getMaximumSliderValue() {
        /*if (isDoubleEditor) 
            return (int)Math.round(maximumValue*(precision + 1));
        else  // int editor
            return (int)maximumValue;
        */
        if (isDoubleEditor) 
            return (int)Math.round((maximumValue - minimumValue)/minorTickSpacing);
        else  // int editor
            return (int)Math.round(maximumValue - minimumValue);
        
    }
    
    
    protected String getLabel(double x) {
        if (isDoubleEditor)
            return format.format(x);
        else
            return "" + (int)x;
    }
    
    
private void setLabels() {
    if (slider == null) return;
    
    if (isDoubleEditor) {
        // minor tick is always equal to 1
        //do not paint minor ticks if double editor
        //slider.setMinorTickSpacing(1);
        if (labelValues == null) slider.setMajorTickSpacing((int)Math.round(majorTickSpacing/minorTickSpacing));
    } else {
        slider.setMinorTickSpacing((int)minorTickSpacing);
        if (labelValues == null) slider.setMajorTickSpacing((int)majorTickSpacing);
    }
    
    if (isLabeled()) {
        
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setSnapToTicks(snapToTicks());
        
        Dictionary lbls = new Hashtable();
        
        if (this.labelValues != null) {
            // labels were specified
            for (int i=0; i<labelValues.length; i++) {
                double x = labelValues[i];
                String title;
                
                if (this.labelTitles != null) title = labelTitles[i]; // lable titles were specified
                else title = getLabel(x);
                
                JLabel label = new JLabel(title);
                label.setFont(ovt.gui.Style.getSliderLabelsFont());
                lbls.put(new Integer(calcSliderPositionFromValue(x)), label);
                //Log.log("label="+getLabel(x)+ " sliderpos="+calcSliderPositionFromValue(x));
            }
        } else {
            //double minX = getMinX();
            //double maxX = getMaxX();
            
            for (double x = minimumValue; x <= maximumValue + 1.e-6; x+=majorTickSpacing) {
                //double x = minX + (n/((double)getNumberOfMajorLabels()-1.))*(maxX - minX);
                JLabel label = new JLabel(getLabel(x));
                label.setFont(ovt.gui.Style.getSliderLabelsFont());
                lbls.put(new Integer(calcSliderPositionFromValue(x)), label);
                //Log.log(""+x+"\t"+getLabel(i));
            }
        }
        slider.setLabelTable(lbls);
    }
    else { // no labels are given
        slider.setPaintTicks(false);
        slider.setPaintLabels(false);
        slider.setSnapToTicks(false);
    }
}



public Component getComponent() {
    if (component == null) {
        slider = new JSlider();
        // set the component
        component = slider;
        // create slider change listener
        changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                //Log.log("Slider value :"+getValueFromSlider()+" pos="+getSliderPosition());
                try {
                    if (isDoubleEditor)
                        setValue(new Double(getValueFromSlider()));
                    else // integer
                        setValue(new Integer((int)Math.round(getValueFromSlider())));
                    
                    fireEditingFinished();
                } catch (PropertyVetoException ignore) {}
            }
        };
        slider.addChangeListener(changeListener);
        updateSlider();
    }
    return component;
}

private void updateSlider() {
    if (slider != null) {
        slider.removeChangeListener(changeListener);
        slider.setMinimum(getMinimumSliderValue());
        //slider.setMaximum(isLinear() ? maximumSliderValue : maximumSliderValue+1);
        //Log.log("Setting max value for slider="+getMaximumSliderValue());
        slider.setMaximum(getMaximumSliderValue());
        slider.setValue(calcSliderPositionFromValue());
        setLabels();
        slider.addChangeListener(changeListener);
    }
}

/** Getter for property labeled.
 * @return Value of property labeled.
 */
public boolean isLabeled() {
    return labeled;
}

/** Setter for property labeled.
 * @param labeled New value of property labeled.
 */
public void setLabeled(boolean labeled) {
    this.labeled = labeled;
}

public void propertyChange(PropertyChangeEvent evt) {
    //Log.log("event with value = "+getValueAsDouble());
    
    if (slider != null) {
        slider.removeChangeListener(changeListener);
        slider.setValue(calcSliderPositionFromValue());
        slider.addChangeListener(changeListener);
    }
    //super.propertyChange(evt);// ???
}


protected boolean snapToTicks() {
    if (isDoubleEditor) return false;
    else return true;
}



/** Getter for property precision.
 * @return Value of property precision.
 */
public int getPrecision() {
    return format.getMaximumFractionDigits();
}

/** Setter for property precision.
 * @param precision New value of property precision.
 */
public void setPrecision(int precision) {
    int oldPrecision = getPrecision();
    format.setMaximumFractionDigits(precision);
    //firePropertyChange("precision", new Integer(oldPrecision), new Integer(getPrecision()));
}
    
}
