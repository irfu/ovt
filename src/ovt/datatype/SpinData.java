/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/SpinData.java,v $
  Date:      $Date: 2003/09/28 17:52:38 $
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

package ovt.datatype;

import ovt.util.*;

import java.io.*;
import java.util.*;
import java.lang.Math;



/**
 * @author kono
 */
public class SpinData {  
  
   protected String fname;
   private boolean isAvailable=false;
   protected SpinRecord spinRecord=new SpinRecord();
   
   //for test only!
   public SpinData(){
   }
  
   //for test only!
   public static void testRecord(String x){
      SpinRecord tmp=new SpinRecord(x);
      tmp.print();
   }
  
   public SpinData(String fName){
      this.fname=new String(fName);
      this.isAvailable=isAvailable(fname);
   }
   
   public boolean isAvailable(){
      return isAvailable;
   }
  
   public static boolean isAvailable(String filename){
      File file=new File(filename);
      if(file.exists()==false) return false;
      if(file.length()<100)return false;
      if(file.canRead()==false)return false;
      return true;
   }
   
   /**
    *@param MJD (from J1950)
    *@return x,y,z - spin vector in GEI CS (abs. value indicates spin rate!)
    */
   public double[] getSpinVect(double mjd){
      mjd-=18262.0;              //from J2000 !!!
      if(!this.isAvailable())
         return null;
      try {
         if(this.spinRecord!=null){   //trying to access to previos data
            if(mjd>=spinRecord.vsttimMjd && 
                  mjd<=spinRecord.ventimMjd && !spinRecord.isInvalid() ){
//System.out.println("  From BUFFER!!! ");
               return Utils.astro2xyz(spinRecord.sprasc,spinRecord.spdecl,spinRecord.sprate);
            }
         }
         this.spinRecord=getSpinRecord(this.fname,mjd);  //reading file
         if(this.spinRecord==null){
//System.out.println("  NULL!!!");
            return null;
          }
//System.out.println("  From FILE!!! ");
         return Utils.astro2xyz(spinRecord.sprasc,spinRecord.spdecl,spinRecord.sprate);
      } catch (IOException e){
         this.isAvailable=false;
         return null;
      }
   }
   
   /* Returns null if any error or spin parameters (record)
    * @param mjd - MJD from 1950
    * @param fileName - File name
    * @return null or spin record
    */
   public static SpinRecord getSpinRecord(String fileName,double mjd) throws IOException{
      if(isAvailable(fileName)==false)
         throw new IOException("File "+fileName+" is not available.");
      
      BufferedReader inData;
      boolean isFound=false;
      String str;
      double mjdx=mjd; //-18262.0;  //mjd from J1950, but mjdx from J2000 !!!
      double[] spinVect={0,0,0};
      SpinRecord spRec=new SpinRecord();
      SpinRecord closestRec=new SpinRecord();
      
      try {
         inData=new BufferedReader(new FileReader(fileName));
      } catch (FileNotFoundException e){
         throw new IOException("File "+fileName+" not found.");
      } catch (IOException e){
         throw new IOException("IO error with "+fileName+" datafile.");
      }

      while(inData.ready() && isFound==false){
         str=inData.readLine();
         spRec.setRecord(str);
        
         if(spRec.isInvalid())
            continue;                  // just skip bad records
         if(mjdx>=spRec.vsttimMjd && mjdx<=spRec.ventimMjd){
            isFound=true;
         }
      }
      inData.close();
      
      if(isFound)
         return spRec;
      else return null;
   }
  
   public static void main(String[] s){
/*      SpinData xx=new SpinData();
      String str=new String(" 1 R 2001-01-01T12:35:25Z 2001-01-02T16:35:25Z 273.45 -43.56 14.456789 333.723 777.9  -2.1   1.3  0.02 -0.05 2001-01-02T16:33:31Z");
      xx.testRecord(str);*/
     SpinData xx=new SpinData("/export/home/kono/ovt2g/odata/satt.dat");
     double[] spinVect=xx.getSpinVect(369.1+18262.0);
     if(spinVect!=null)
        System.out.println("res: "+spinVect[0]+", "+spinVect[1]+", "+spinVect[2]);
     else System.out.println("Error :-(");
  }
}
