/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/MinutesAndSeconds.java,v $
  Date:      $Date: 2003/09/28 17:52:37 $
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
 * MinutesAndSeconds.java
 *
 * Created on March 4, 2000, 6:28 PM
 */
 
package ovt.datatype;

/** 
 *
 * @author  root
 * @version 
 */
public class MinutesAndSeconds {

  protected int mins = 0;
  protected int secs = 0;
  protected double in_days = 0;
  
  /** Creates new MinutesAndSeconds */
  public MinutesAndSeconds(double in_days) throws IllegalArgumentException {
    if (in_days*24 > 1) throw new IllegalArgumentException("The value is larger than 1 hour.");
    this.in_days = in_days;
    mins = (int)(in_days*Time.MINUTES_IN_DAY);
    secs = (int)(in_days*Time.SECONDS_IN_DAY - mins*60);
  }
  
  public MinutesAndSeconds(String text) throws IllegalArgumentException {
    // text format : "MM:SS"
    String errorMsg = "Time format: MM:SS";
    if ((text.length()>5) || (text.indexOf(":") != 2)) throw new IllegalArgumentException(errorMsg);
    try {
      int m = Integer.valueOf(text.substring(0,2)).intValue();
      int s = Integer.valueOf(text.substring(3,5)).intValue();
      
      if ((m > 60) || (s > 59)) throw new IllegalArgumentException(errorMsg);
      mins = m;
      secs = s;
      in_days = (double)mins/(double)Time.MINUTES_IN_DAY + (double)secs/(double)Time.SECONDS_IN_DAY;
    } catch (NumberFormatException e2) { throw new IllegalArgumentException(errorMsg);}
  }
  
  public String toString() {
    return "" + ((mins<10)? "0" : "") + mins + ":" + ((secs<10)? "0" : "")+secs;
  }
  
  public int getMins()
    { return mins; }
    
  public int getSecs()
    { return secs; }
  
  public double getInDays()
    { return in_days; }
    
  public static double getInDays(String minAndSec) {
    MinutesAndSeconds m = new MinutesAndSeconds(minAndSec);
    return m.getInDays();
  }
    
  public static String toString(double in_days) throws IllegalArgumentException 
    { return new MinutesAndSeconds(in_days).toString(); }
    
  public static void main(String argv[]) {
      MinutesAndSeconds ms = new MinutesAndSeconds("54:01");
      //System.out.println(ms.toString()+" days: "+ms.getInDays());
      
      MinutesAndSeconds ms2 = new MinutesAndSeconds(ms.getInDays());
      //System.out.println(ms2.toString());
  }
}
