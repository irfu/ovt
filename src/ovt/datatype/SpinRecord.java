/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/SpinRecord.java,v $
  Date:      $Date: 2006/02/20 16:06:39 $
  Version:   $Revision: 2.4 $


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

import ovt.util.*;
import ovt.datatype.*;

import java.io.*;
import java.util.*;
import java.lang.Math;

//@author KONO

/**
 *
 */
public class SpinRecord{
      public static final double toRad=Math.PI/180.0;
      public static final int err=-1000000;     // Error code
      private boolean isInvalid=true;
      
      public byte scid;            // S/C ID
      public char prerec;          // R (reconstituted) or P (predict)
      public double vsttimMjd=100; // start time (from J2000), UTC
      public double ventimMjd=-100;// end time (from J2000), UTC
      public double sprasc=0.0;    // right ascension (J2000), 0..2pi
      public double spdecl=0.0;    // declinition (J2000), -0.5pi..0.5pi
      public double sprate=0.0;    // spin rate
      public double scphas=0.0;    // spin phase at SPR, 0..2pi
      public double comshXb=0.0;   // center of mass pos. Xb (mm)
      public double comshYb=0.0;   // center of mass pos. Yb (mm)
      public double comshZb=0.0;   // center of mass pos. Zb (mm)
      public double tpsi_2=0.0;    // spin axis tilt-first Euler angle around Zb, rad
      public double tpsi_1=0.0;    // spin axis tilt-second Euler angle around Yb, rad
      public double gentimMjd=-10; // generated time (from J2000), UTC
      
      /*private byte idx[14][2]={    //indexes of fields pos. in string
        {0,2},{3,4},{5,25},{26,46},{47,53},{54,60},{61,70},
        {71,78},{79,84},{85,90},{92,96},{97,102},{103,108},{109,129}};*/
      
      public SpinRecord(){
      }
      
      public SpinRecord(String ss){
         //if(setRecord(ss)==false)
         //   System.err.println("SpinRecord: invalid string:\n"+ss);
      }
      
      public boolean isInvalid(){
         return isInvalid;
      }
      
      protected void isInvalid(boolean bb){
         this.isInvalid=bb;
      }
      
      public boolean setRecord(String data){
        int tmp;
        String tmps;
        try {
           StringTokenizer tok=new StringTokenizer(data);
           if(tok.countTokens()<14){
              isInvalid(true);
              return false;
           }
           tmps=tok.nextToken();
           scid=new Byte(tmps).byteValue();
           tmps=tok.nextToken();
           prerec=tmps.charAt(0);
           if(prerec!='R' && prerec!='P'){
              isInvalid(true);
              return false;
           }
           tmps=tok.nextToken();
           vsttimMjd=ccsds2mjd(tmps);
           tmps=tok.nextToken();
           ventimMjd=ccsds2mjd(tmps);
           if(vsttimMjd==err || ventimMjd==err){
              isInvalid(true);
              return false;
           }
           tmps=tok.nextToken();
           sprasc=new Double(tmps).doubleValue()*toRad;
           tmps=tok.nextToken();
           spdecl=new Double(tmps).doubleValue()*toRad;
           tmps=tok.nextToken();
           sprate=new Double(tmps).doubleValue();
           tmps=tok.nextToken();
           scphas=new Double(tmps).doubleValue()*toRad;
           tmps=tok.nextToken();
           comshXb=new Double(tmps).doubleValue();
           tmps=tok.nextToken();
           comshYb=new Double(tmps).doubleValue();
           tmps=tok.nextToken();
           comshZb=new Double(tmps).doubleValue();
           tmps=tok.nextToken();
           tpsi_2=new Double(tmps).doubleValue()*toRad;
           tmps=tok.nextToken();
           tpsi_1=new Double(tmps).doubleValue()*toRad;
           tmps=tok.nextToken();
           gentimMjd=ccsds2mjd(tmps);
           if(gentimMjd==err){
              isInvalid(true);
              return false;
           }
           isInvalid(false);
           return true;
        } catch (NumberFormatException e1){
           //System.err.println("SpinRecord.setRecord: NumberFormatException");
           isInvalid(true);
           return false;
        } catch (NoSuchElementException e2){
           //System.err.println("SpinRecord.setRecord: NoSuchElementException");
           isInvalid(true);
           return false;
        }
      }

      
   //Convert time in CCSDS format (YYYY-MM-DDThh:mm:ssZ) to MJD (from 2000)
   // ...or (YYYY-MM-DDZ) ...
   public static double ccsds2mjd(String str){
      double mjd=err;
      int y=0,m=0,d=0,hh=0,mm=0,ss=0,strlen=str.length();
            // 01234567890123456789
            // YYYY-MM-DDThh:mm:ssZ
      try{
         if(strlen>=4){
            y=new Integer(str.substring(0,4)).intValue();
            if(strlen>=7){
               m=new Integer(str.substring(5,7)).intValue();
               if(strlen>=10){
                  d=new Integer(str.substring(8,10)).intValue();
                  if(strlen>=13){
                     hh=new Integer(str.substring(11,13)).intValue();
                     if(strlen>=16){
                        mm=new Integer(str.substring(14,16)).intValue();
                        if(strlen>=19)
                           ss=new Integer(str.substring(17,19)).intValue();
                     }
                  }
               }
            }
         } else {
            //System.err.println("SpinRecord.ccsds2mjd: Invalid CCSDS format");
            return err;
         }
         mjd=Time.getMjd(y,m,d,hh,mm,ss)-18262.0;  //from 2000 epoch!!!
         return mjd;
      } catch (NumberFormatException e1){
         //System.err.println("SpinRecord.ccsds2mjd: NumberFormatException");
         return err;
      } catch (IllegalArgumentException e2){
         //System.err.println("SpinRecord.ccsds2mjd: IllegalArgumentException");
         return err;
      }
   }
      public void print(){
         System.out.println(scid+", "+prerec+", "+vsttimMjd+", "+ventimMjd+", "+
           sprasc+", "+spdecl+", "+sprate+", "+scphas+", "+comshXb+", "+comshYb+
           ", "+comshZb+", "+tpsi_2+", "+tpsi_1+", "+gentimMjd);
      }
}
