/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/PolarCoord.java,v $
  Date:      $Date: 2003/09/28 17:52:38 $
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

package ovt.datatype;

/**
 * This object contains Polar Coordinate - longitude and lattitude
 * @author  ko
 * @version 
 */
public class PolarCoord extends Object {

    public double lat = 0;
    public double lon = 0;
    
    /** Creates new PolarCoord */
    public PolarCoord() {
    }

    /** Creates new PolarCoord */
    public PolarCoord(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }
    
    public double[] getAsArray() {
        return new double[]{ lat, lon};
    }
    
    /** Overrides Object.hashCode() */
    public int hashCode() {
        return new String(""+lon+" "+lat).hashCode();
    }
    
    /** Overrides Object.equals() method */
    public boolean equals(Object obj) {
        try {
            PolarCoord pc = (PolarCoord)obj;
            return (pc.lat == lat  &&  pc.lon == lon);
        } catch (ClassCastException e2) {
            return false;
        }
    }
    
    public double getLon() {
        return lon;
    }
    
    public void setLon(double lon) {
        this.lon=lon;
    }
    
    public double getLat() {
        return lat;
    }
    
    public void setLat(double lat) {
        this.lat=lat;
    }
    
    public String toString() {
        return "("+lat+","+lon;
    }
}
