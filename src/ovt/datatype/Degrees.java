/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/Degrees.java,v $
  Date:      $Date: 2003/09/28 17:52:36 $
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
 * Degrees.java
 *
 * Created on 13 Март 2001 г., 10:47
 */


package ovt.datatype;

import java.util.*;

/**
 *
 * @author  oleg
 * @version 
 */
public class Degrees {

    private double value;
    
    /** Creates new Degrees from degrees double value*/
    public Degrees(double val) {
        this.value = value;
    }

    public Degrees(String val) {
        this.setValue(val);
    }

    public double getValue() {
        return this.value;
    }

    public double getValueRadian() {
        return this.value * Math.PI / 180;
    }

    public void setValue(double val) {
        this.value = val;
    }
    
    public void setValue(String svalue) throws IllegalArgumentException {
        setValue(Degrees.todouble(svalue));
    }
        
    public String toString() {
        return Degrees.fromdouble(this.value);
    }
        
    static private int reminder(double val, int divisor)
    {
        val -= (int) val;
        val *= divisor;
        return (int) val;
    }

    /** Converts double to String representation of degrees*/
    static public String fromdouble(double val)
    {
        StringBuffer sb = new StringBuffer();
        sb.append((int)val).append("d ");
        sb.append(reminder(val, 60)).append("' ");
        val *= 60;
        sb.append(reminder(val, 60)).append("\"");
        return sb.toString();
    }
    
    /** Converts String representation of degrees (123° 45' 67") to double */
    static public double todouble(String svalue) throws IllegalArgumentException {
        double val;
        int sign = 1;
        StringTokenizer st = new StringTokenizer(svalue) /*{
            public String nextToken(String delim)
            {
                String s = super.nextToken(delim);
                System.out.println("Degrees::todouble: token = " + s);
                return s;
            }
        }*/; 
        try {
            String tok = st.nextToken("d ");
            val = Integer.parseInt(tok);
            if (val<0) {
                sign = -1;
                val  = -val;
            }
            if (!st.nextToken(" 0123456789").equals("d")) 
                throw new IllegalArgumentException("\"d\" expected");

            try {
                tok = st.nextToken("' "); 
                int parsed = Integer.parseInt(tok);
                if (parsed < 0)
                    throw new IllegalArgumentException("minutes value must be > 0");
                val +=  (double)parsed / 60; 
            } catch (NoSuchElementException e) {
                return val * sign;
            }
            
            if (!st.nextToken(" 0123456789").equals("'")) 
                throw new IllegalArgumentException("' expected");
            
            try {
                tok = st.nextToken("\" "); 
                int parsed = Integer.parseInt(tok);
                if (parsed < 0)
                    throw new IllegalArgumentException("seconds value must be > 0");
                val += (double)parsed / 360;
            } catch (NoSuchElementException e) {
                return val * sign;
            }
            
            if (!st.nextToken(" 0123456789").equals("\"")) 
                throw new IllegalArgumentException("\" expected");
            
            return val * sign;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("unexpected end of line");
        }
    }
}
