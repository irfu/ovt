/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /ovt/datatype/LTOFRecord.java,v $
  Date:      $Date: 2015/09/15 13:36:00 $
  Version:   $Revision: 1.4 $


 Copyright (c) 2000-2015 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev,
 Yuri Khotyaintsev, Erik P. G. Johansson, Fredrik Johansson)
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
 Khotyaintsev, E. P. G. Johansson, F. Johansson
 
=========================================================================*/


package ovt.datatype;

import java.util.*;

/**
 * The record class of the Long Term Orbit Files, used by {@link ovt.object.LTOFSat}.
 * It does MOST of the interpretation of LTOF files, but NOT all interpretation.
 * 
 * For more information concerning LTOF format refer to docs/ltof.pdf.
 * 
 * Some information on the LTOF format can be found in the "CLUSTER
 * Data Disposition System - Data Delivery Interface Document (DDID)"
 * http://www.jsoc.rl.ac.uk/pub/fd_files/index.php
 * 
 * @author  ko
 */
public  class LTOFRecord  {
    /** The maximum number of polynomial coefficient lines. */
    public final int MaxCoefs=10;

    /** S/c number */
    public int sc_id = -1;

    /** 2nd record id */
    public int recID = -1; 
    // Shall be 200+sc_id
    /**  Start of the period for when the data is valid [in mjd] */
    public double dayBeg = -1;
    /**  End of the period for when the data is valid [in mjd] */
    public double dayEnd = -1;
    /** Time of the epoque of the reference kepler orbit [in mjd] */
    public double epoch = -1;
    public double revEpo = -1;
    /** Semimajor axis 'a' in km, of the reference Kepler orbit */
    public double smAxis = -1;
    
    /**
     * Inverse mean motion = a*sqrt(a/miu) of the reference Kepler orbit in sec/rad, miu = central Earth potential.
     * NOTE: The "Cluster Data Delivery Interface Document (DDID)"
     * actually calls the variable "OMOTIN", i.e. it is not a misspelling.
     */
    public double oMotin = -1;

    /** 3rd record: x-y-z components of the position and velocity of the reference Kepler orbit [km], [km/sec] */
    public double[] Y = new double[6];
    /** 3rd record: absolut value 'r' of the position vector of the reference Kepler orbit [km] */
    public double rDist;
    /** 3rd record: Number of lines containing polynomial coefficents. */
    public int coeffLinesNumber;
    
    //Data record
    public double[][] coeffs;   //Data matrix with up to 10x6 coefs.
    
    public LTOFRecord() {
    }
    
    /** The line which starts with 201 or 2XX, it contains mjd start, mjd end, semimajor axis, mean motion. 
     * The format of the line : <code>I3, F12.6, F12.6, F15.9, F11.3, F13.5, F13.5, A1</code> */
    public void set2ndRecord(String line) throws IllegalArgumentException {
        try {   
            recID = Integer.parseInt(line.substring(0,3)); 
        } catch (NumberFormatException e2) {
            throw new IllegalArgumentException("Wrong recID");
        }
        try {
            dayBeg = Double.parseDouble(line.substring(3,15)) + Time.Y2000;
        } catch (NumberFormatException e2) {
            throw new IllegalArgumentException("Wrong dayBeg");
        }
        try {
            dayEnd = Double.parseDouble(line.substring(15,27)) + Time.Y2000;
         } catch (NumberFormatException e2) {
            throw new IllegalArgumentException("Wrong dayEnd");
        }
        try {
            epoch = Double.parseDouble(line.substring(27,42)) + Time.Y2000;
         } catch (NumberFormatException e2) {
            throw new IllegalArgumentException("Wrong epoch");
        }
        try {
            revEpo = Double.parseDouble(line.substring(42, 53));
         } catch (NumberFormatException e2) {
            throw new IllegalArgumentException("Wrong revEpo");
        }
        try {
            smAxis = Double.parseDouble(line.substring(53,66));
         } catch (NumberFormatException e2) {
            throw new IllegalArgumentException("Wrong smAxis");
        }
        try {
            oMotin = Double.parseDouble(line.substring(66, 79));
        } catch (NumberFormatException e2) {
            throw new IllegalArgumentException("Wrong oMotin");
        }
                
       /*int i=0;  //Elements counter
       StringTokenizer stok = new StringTokenizer(line);
       while(stok.hasMoreTokens() && i<7){
          String s1=new String(stok.nextToken());
          switch (i) {
             case 0: //recID;
                recID=new Integer(s1).intValue();
                //if (recID! = (sc_id+200))   //cheking for error
                //   return 1;
                //else 
                    break;
             case 1: //dayBeg in mjd
                dayBeg=new Double(s1).doubleValue() + Time.Y2000;
                break;
             case 2: //dayEnd in mjd
                dayEnd=new Double(s1).doubleValue() + Time.Y2000;
                break;
             case 3: //epoch in mjd
                epoch=new Double(s1).doubleValue() + Time.Y2000;
                break;
             case 4: //revEpo
                revEpo=new Double(s1).doubleValue();
                break;
             case 5: //smAxis
                smAxis=new Double(s1).doubleValue();
                break;
             case 6: //oMotin
                oMotin=new Double(s1).doubleValue();
          }
          ++i;
       }
       if (i<7) 
           throw new IllegalArgumentException("Too few parameters in the line : "+i+". Should be 7."); */
       
    }
    
    
    /** 
     * Reads one line of LTOF file data. See specification.
     * Footnote: The number of decimals in the specification does not seem to always be correct. In reality they can vary.
     * 
     * New implementation of set3rdRecord to fix bugs.
     * 
     * @author Erik P G Johansson, 2015-08-xx
     */
    public void set3rdRecord(String line) throws IllegalArgumentException {
        
        try {
            final int UNKNOWN_CONSTANT = 300;
            final double[] position_velocity = new double[6];
            int i_fieldBegin = 0;
        
            final int tmp_coeffLinesNumber = Integer.parseInt(line.substring(i_fieldBegin, i_fieldBegin+3));
            i_fieldBegin += 3;            
            
            // Subtraction with odd constant is needed to get the number
            // of lines with coefficients (read in LTOFRecord#setDataRecord).
            coeffLinesNumber = tmp_coeffLinesNumber - UNKNOWN_CONSTANT;
            if (coeffLinesNumber<0 || coeffLinesNumber>MaxCoefs) {
                throw new IllegalArgumentException("Wrong number of polynomial coefficient lines : "+coeffLinesNumber+" . ");
            }
            
            final int FIELD_LENGTH = 11;
            for (int i_posVel=0; i_posVel<6; i_posVel++) {
                position_velocity[i_posVel] = Double.parseDouble(line.substring(i_fieldBegin, i_fieldBegin+FIELD_LENGTH));
                i_fieldBegin += FIELD_LENGTH;
            }
            Y = position_velocity;
            this.coeffs = new double[coeffLinesNumber][6];    // Initialize array to be fillew values later in LTOFRecord#setDataRecord.
            rDist = Double.parseDouble(line.substring(i_fieldBegin, i_fieldBegin+11));
            
        } catch (NumberFormatException e2) {
            throw new IllegalArgumentException("Wrong recID");
        }
    }

    
    
    /** 
     * Read polynomial coefficients.
     * @param id should be 1..10 */
    public int setDataRecord(int id, String ss) throws IllegalArgumentException {
        if (id <1 || id>10) {
            throw new IllegalArgumentException("Wrong record number : "+id+". Can be 1..10.");
        }
        final StringTokenizer stok=new StringTokenizer(ss);
        int nRec4Check;            // Just for checking
        int i=0;  //Elements counter
       
        while(stok.hasMoreTokens() && i<7){
            final String s1 = stok.nextToken();
            switch(i){
                case 0: //nrec4Check;
                    nRec4Check = Integer.parseInt(s1);
                    if(nRec4Check!=(11*id+coeffLinesNumber)) {  // checking for error
                        return 1;
                    } else {
                        break;
                    }
                default: //setting data record
                    if(i>=1 && i<=6) {
                        coeffs[id-1][i-1] = Double.parseDouble(s1);
                        break;
                    } else return 0;
          }
          ++i;
       }
       if(i<7) return 1;  // should be more parameters in line!
       return 0; // No any error!!!    
    }
    
    public String toString() {
        return "2-nd line: recID="+recID+", dayBeg="+dayBeg+", dayEnd = "+dayEnd+", epoch ="+epoch+ ", revEpo ="+revEpo+", smAxis ="+smAxis+", oMotin ="+oMotin; 
    }
}
