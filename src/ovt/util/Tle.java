/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/Tle.java,v $
  Date:      $Date: 2006/04/19 10:12:44 $
  Version:   $Revision: 1.5 $


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

package ovt.util;

import ovt.util.*;
import ovt.object.*;
import ovt.datatype.*;

import ovt.datatype.TLERecord;

import java.io.*;
import java.util.*;

/** Reader and calculator of Two Line Element (TLE) file format
 * !!!! BROKEN!!!!!
 * @author kono
 */
public class Tle {
   private String filename;
//   private BufferedReader inData;
   private double firstMjd, lastMjd, prevMjd, nextMjd;
   private TLERecord prevTleRec, nextTleRec;

   public Tle() {
   }
   
   public Tle(String fn)throws IOException{
      try{
         init(fn);
      } catch (IOException e){
         throw new IOException(e.toString());
      }
   }

   public void init(String orbitTLEFile) throws IOException{
      double[] fstAndlstMjds=new double[2];
      filename= orbitTLEFile;
      
      double rr[] =getFirstLastMjdPeriodSatNumber(new File(orbitTLEFile));
      firstMjd=rr[0];
      lastMjd=rr[1];
     
   }
   
/*   public Tle(String line1,String line2){
      tleData=new TLERecord(line1,line2);
//         throw new IOException("public Tle(String[] data): Wrong number of records.");
   }*/

   public double getFirstMjd(){
      return firstMjd;
   }

   public double getLastMjd(){
      return lastMjd;
   }
   

   public static double getLastMjd(String fname) throws IOException{
      try {
         Tle tmp=new Tle(fname);
         return tmp.getLastMjd();
      } catch (IOException e){
         throw new IOException(e.toString());
      }
   }

   /**
    * @see ovt.object.TLESat#getFirstLastMjdPeriodSatNumber
    * @param orbitTLEFile
    * @throws IOException
    * @return
    */
   public static double[] getFirstLastMjdPeriodSatNumber(File orbitTLEFile) throws IOException{
      double[] res= new double[4];
      BufferedReader inData;
      String str="";
      TLERecord tlerec = null;
      double lstMjd=-2000000.0, fstMjd=2000000.0, periodDays=-1, day;
     
      try {
         inData=new BufferedReader(new FileReader(orbitTLEFile));
      } catch (FileNotFoundException e){
         throw new IOException("File "+orbitTLEFile+" not found.");
      } catch (IOException e){
         throw new IOException("IO error with "+orbitTLEFile+" datafile.");
      }

      while(inData.ready()) {
         str=inData.readLine();
         
         int code = TLERecord.identLine(str);
         
         if  ( code ==2  &&  periodDays <= 0 ) { 
             if (tlerec.setLine(str)!=-1)  //  set the 2-nd line. It contains MeanMotion. Period is derived from it.
                  periodDays = tlerec.getPeriodDays(); 
             else
                   System.err.println("WARNING: Bad data in TLE file "+orbitTLEFile+" ignored.");
            continue;
         } 
         
         if  ( code !=1) continue;               //Only 1st line!
         
         tlerec = new TLERecord();
         try {
            if (tlerec.setLine(str)==-1) {
               //throw new IOException("Bad data in TLE file "+datafile);
               System.err.println("WARNING: Bad data in TLE file "+orbitTLEFile+" ignored.");
               continue;
            } 
             
         } catch (NumberFormatException e){
            e.printStackTrace();
            throw new IOException(""+orbitTLEFile+":\nInvalid TLE format in line: "+str);
         }

         double tmpMjd = Time.getMjd(tlerec.epochYear, 1, 0, 0, 0, 0) + tlerec.epochDay;
         if(tmpMjd<fstMjd)       //Seeking for first MJD
         fstMjd=tmpMjd;
         if(tmpMjd>lstMjd)       //Seeking for last MJD
         lstMjd=tmpMjd;
      }
      inData.close();

      res[0] = fstMjd;
      res[1] = lstMjd;
      res[2] = periodDays;
      res[3] = tlerec.satNumber1;
      return res;
   }

   public static GeiAndVei getSatPos(double mjd, TLERecord rec){
      GeiAndVei gv = Sgp4Sdp4.getSatPos(mjd, rec);
      return gv;
   }
  
   //This temporary native function!!!
   public static native int getSatPosJNI(String filename, double mjd, double[] gei, double[] vei);
//   static {
//     System.loadLibrary("ovt2g");
//   }
  
   //Uses TLE data file filename
   public GeiAndVei getSatPos(double mjd) throws IOException{
/*      double tsince=0.0;
      //GeiAndVei gv;
      TLERecord tlr=new TLERecord();
      
      try{
         if(mjd<prevMjd || mjd>nextMjd)         // reading from file
            tlr=getTleDataFromFile(mjd);
         else tlr=prevTleRec;
      } catch (IOException e){
         throw new IOException(e.toString());
      }
      GeiAndVei gv=Sgp4Sdp4.getSatPos(mjd,tlr);
*/
      //This temporary call of native function!!!
      GeiAndVei gv=new GeiAndVei();
      int code = getSatPosJNI(this.filename, mjd, gv.gei, gv.vei);
      if (code != 0) throw new IOException("IOException with file " + filename);

      return gv;
   }
   
   //Modifies prev & next Data, reading from file filename
   public TLERecord getTleDataFromFile(double mjd) throws IOException{
      BufferedReader inData;
      String str1,str2;
      TLERecord tr1=new TLERecord(),tr2=new TLERecord(),closest=new TLERecord();
      int i, datacounter=0;
      byte code;
      boolean found=false;
      double closestMjd=1000000.0,mjd2;
      
      try {
         inData=new BufferedReader(new FileReader(filename));
      } catch (FileNotFoundException e){
         throw new IOException("File "+filename+" not found.");
      } catch (IOException e){
         throw new IOException("IO error with "+filename+" datafile.");
      }

      while(inData.ready()  && found==false){
         tr1=tr2.cloneMe();
         
         //Reading 1st line
         str1=inData.readLine();
         code=TLERecord.identLine(str1);
         if(code!=1)continue;
         if(tr2.setLine(str1)==-1)continue;

         //Reading 2nd line
         str2=inData.readLine();
         code=TLERecord.identLine(str2);
         if(code!=2)continue;
         if(tr2.setLine(str2)==-1)continue;

         ++datacounter;
         
         mjd2=Time.getMjd(tr2.epochYear,0,0,0,0,0)+tr2.epochDay;

         if(datacounter>1){
            prevMjd=Time.getMjd(tr1.epochYear,0,0,0,0,0)+tr1.epochDay;
            nextMjd=mjd2;
            if(mjd>=prevMjd && mjd<nextMjd)
               found=true;                    //Needed data found!
         }

         if(Math.abs(mjd-closestMjd)>Math.abs(mjd-mjd2)){
            closestMjd=mjd2;
            closest=tr2;
         }
      }
      inData.close();
      
      if(datacounter==0)
         throw new IOException("File "+filename+" is empty or invalid.");

      if (found==false){            //choosing closest to mjd
         System.err.println("\nWARNING: Requested MJD ("+mjd+") too far in "+filename);
         System.err.println("Closest orbital elements chosen: "+
            Math.abs(closestMjd-mjd)+" days difference.\n");
         tr1=tr2=closest;
      }

      prevTleRec=tr1;
      nextTleRec=tr2;
      return tr1;
   }


   public static void main(String ss[]){      
      GeiAndVei gv;

      //Test from data file
/*      try {
         Tle test=new Tle("../../odata/astrid.tle");
         System.out.println("First MJD: "+test.getFirstMjd()+", Last MJD: "+test.getLastMjd());
         gv=test.getSatPos(Time.getMjd(1999,0,0,0,0));
         for(int i=0;i<3;++i)
            System.out.println("Pos & Vel "+i+">  "+gv.gei[i]+"; "+gv.vei[i]);
      } catch (IOException e){
         System.err.println("ERROR: "+e.toString());
      }
*/

      //Test from data strings
      TLERecord test=new TLERecord(
       //Test 1
//       "1 88888U          80275.98708465  .00073094  13844-3  66816-4 0     8",
//       "2 88888  72.8435 115.9689 0086731  52.6988 110.5714 16.05824518   105");
         //Test 2
       "1 88888U          80275.98708465  .00073094  13844-3  66816-4 0      8",
       "2 88888  72.8435 115.9689 0086731  52.6988 110.5714 16.05824518    105");
         //Test from Astrid (2 last lines)
//         "1 25568U 98072B   99217.23657406  .00000268  00000-0  28301-3 0   762",
//         "2 25568  82.9496 111.7158 0024737 336.4433  23.5585 13.71132394 32578");
        //Test 3 (SDP4)
//       "1 11801U          80230.29629788  .01431103  00000-0  14311-1 0     8",
//       "2 11801  46.7916 230.4354 7318036  47.4722  10.4117  2.28537848   108");
        try {
          Tle testTle=new Tle("testFreja.tle");
          
//          double mjdx=11232.987084649969;
        double mjdx=16436.0,r=6371.3;
//      double mjdx=0.0;
          for(;mjdx<=(16436.0+5/1440.0);mjdx+=1.0/1440.0){
//      for(;mjdx<1.0;mjdx+=1.0/4.0){
         //gv=Tle.getSatPos(mjdx,test);
            gv=testTle.getSatPos(mjdx);
            System.out.println("Mjd = "+mjdx);
            for(int i=0;i<3;++i)
              System.out.println("Pos & Vel "+i+">  "+gv.gei[i]/r+"; "+gv.vei[i]);
          }
        } catch (IOException e){
          System.out.println("ERROR: "+e.toString());
          System.exit(1);
        }
   }
}
