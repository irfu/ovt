/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/TLERecord.java,v $
  Date:      $Date: 2003/09/28 17:52:38 $
  Version:   $Revision: 1.3 $


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

import ovt.util.Sgp4Sdp4;

/**
 *  Two Line Element File Record.
 *   For more information concerning TLE format refer to docs/tle.html
 */

public class TLERecord {
    
   //Math.toRadians appeared only since JDK 1.2
   public static double toRad=0.017453293F;
   // 0th line (name), length<=24, all other lines <=69
   public String satName = "";
   
   // Line 1
   
   /**  Line No. Identification <CODE>Line 1 : char 01 </CODE>*/
   public final byte lineNum1=1;         
   /** Catalog No. <CODE>Line 1 : char 03-07 </CODE>*/
   public int satNumber1;               
   /** Security Classification <CODE>Line 1 : char 08</CODE>*/
   public char type;                     
   /**  <CODE>Line 1 : char 10-11 </CODE>*/
   public int launchYear;               
   /** <CODE> Line 1 :char 12-14 </CODE>*/
   public int launchNumOfYear;          
   /** <CODE> Line 1 :char 15-17 </CODE>*/
   public char[] pieceLaunch=new char[3];
   /** <CODE> Line 1 :char 19-20 </CODE>*/
   public int epochYear;                
   /** <CODE> Line 1 :char 21-32 </CODE>*/
   public double epochDay;                
   /** 1st Time Derative <CODE>Line 1 : char 34-43 </CODE>*/
   public double fstTimeDerivOfMeanMotion;
   /** 2nd Time Derivative <CODE>Line 1 : char 45-52 </CODE>*/
   public double sndTimeDerivOfMeanMotion;
   /** Bstar/Drag Term <CODE>Line 1 : char 54-61 </CODE>*/
   public double bstar;                   
   /** Ephemeris Type <CODE>1-SGP,2-SGP4,3-SDP4,4-SGP8,5-SDP8. Line 1 :char 63 </CODE>*/
   public byte ephemerisType;          
   /** Element Number <CODE>Line 1 : char 65-68 </CODE>*/
   public int elemNumber;              
   /** Check Sum, Modulo 10 <CODE>Line 1 : char 69 </CODE>*/
   public byte checkSum1;              
   
   // Line 2
   
   /**  Line No. Identification <CODE>Line 2 : char 01 </CODE>*/
   public final byte lineNum2=2;       
   /** Catalog No. <CODE>Line 1 : char 03-07 </CODE>*/
   public int satNumber2;                
   /** Inclination <CODE>[radians] Line 2 : char 09-16 [deg]</CODE> */
   public double incl; 
   /** Right Ascension of Ascending Node <CODE>[radians] Line 2 : char 18-25 [deg]</CODE> */
   public double ascenOfNode;            
   /**  Eccentricity with assumed leading decimal [after 0..] <CODE> Line 2 : char 27-33</CODE> */
   public double eccentricity;            
   /**  Argument of the Perigee [radians] <CODE> Line 2 : char 35-42 [deg]</CODE> */
   public double argOfPerigee;            
   /**  Mean Anomaly [radians] <CODE> Line 2 : char 44-51 [deg]</CODE> */
   public double meanAnomaly;             
   /** Revolutions per Day (Mean Motion)  [rev/day] <CODE> Line 2 : char 53-63</CODE> */
   public double meanMotion;              
   /**  Revolution Number at Epoch <CODE> Line 2 : char 64-68 </CODE> */
   public int revNum;                    
   /** Check Sum Modulo 10 <CODE> Line 2 : char 69</CODE> */
   public byte checkSum2;                

   public TLERecord(){
   }

   public TLERecord(String ss){
      setLine(ss);
   }
   
   public TLERecord(String l1, String l2){ //set data records (both lines)
      setLine(l1);
      setLine(l2);
   }

   public TLERecord(String l0, String l1, String l2){ //name + data lines
      setLine(l0);
      setLine(l1);
      setLine(l2);
   }

   /** Processing of line from TLE data files
    * @param strX Input string
    * @return 0 if success or -1 if error
    */
   public byte setLine(String strX){
      byte id=TLERecord.identLine(strX);
      char ch;
      int i,chksum=0;

      if(id==-1){
         return id;
      }

      for(i=0;i<(strX.length()-1);++i){ //Calculating checksum
         ch=strX.charAt(i);
         if(ch>='0' && ch<='9')
            chksum+=ch-'0';
         else if(ch=='-')
            ++chksum;
      }
      chksum%=10;
      try {
         switch(id){
            case -1:
               return -1;
            case 0:
               satName=new String(strX);
               return 0;
            case 1:
               satNumber1=new Integer(numSubstring(strX,2,7)).intValue();
               type=strX.charAt(7);
               launchYear=new Integer(numSubstring(strX,9,11)).intValue();
               launchYear=Y2K(launchYear);
               launchNumOfYear=new Integer(numSubstring(strX,11,14)).intValue(); //!!!
               for(i=14;i<17;++i)
                  pieceLaunch[i-14]=strX.charAt(i);
               epochYear=new Integer(numSubstring(strX,18,20)).intValue();
               epochYear=Y2K(epochYear);
               epochDay=new Double(numSubstring(strX,20,32)).doubleValue();
               fstTimeDerivOfMeanMotion=new Double(numSubstring(strX,33,43)).doubleValue();
               sndTimeDerivOfMeanMotion=modifyExpFmt(strX.substring(44,52));
               bstar=modifyExpFmt(strX.substring(53,61));
               ephemerisType=new Byte(numSubstring(strX,62,63)).byteValue();
               elemNumber=new Integer(numSubstring(strX,64,68)).intValue();
               checkSum1=new Byte(numSubstring(strX,68,69)).byteValue();
               break;
            case 2:
               satNumber2=new Integer(numSubstring(strX,2,7)).intValue();
               incl=new Double(numSubstring(strX,8,16)).doubleValue()*toRad;
               ascenOfNode=new Double(numSubstring(strX,17,25)).doubleValue()*toRad;
               eccentricity=new Double("0."+strX.substring(26,33)).doubleValue();
               argOfPerigee=new Double(numSubstring(strX,34,42)).doubleValue()*toRad;
               meanAnomaly=new Double(numSubstring(strX,43,51)).doubleValue()*toRad;
               meanMotion=new Double(numSubstring(strX,52,63)).doubleValue();
               revNum=new Integer(numSubstring(strX,63,68)).intValue();
               checkSum2=new Byte(numSubstring(strX,68,69)).byteValue();
               break;
            default:
               System.err.println("TLERecord.setLine: Unknown identLine code!");
               return -1;                    //strange error
         }
      } catch (NumberFormatException e) {
         System.err.println("Invalid number format in TLE record: "+strX);
         return -1;
      }
/*      if(chksum!=(id==1?checkSum1:checkSum2)){
         System.err.println("Bad checksum in line: "+strX);
         return -1;
      }*/

      return 0;
   }
   /** Identification of lines from TLE data files
    * @param strX Input string
    * @return TLE record line number (0..2) or -1 if error
    */
   public static byte identLine(String strX){
   //Returns 0, 1 or 2 (line number) or -1 if any error
/*      if(strX.length()>69){
         System.err.println("TLERecord.setLine: Invalid identLine code!");
         return -1;                         // ERROR: too long string
      } else*/ if(strX.length()<69)
         return 0;                          //probably line 0
      char lineID=strX.charAt(0);
      switch(lineID){
         case '1': return 1;
         case '2': return 2;
         default: return -1;
      }
   }
   
   /** Converts moified exponential format: 102034-01 -> 0.102034e-01
    * @param String ss - Input string
    * @return double value
    */
   public static double modifyExpFmt(String ss){
      double mantissa=0.0, exp,sign=1.0,pow=1.0;
      char cc;

      for(int i=0;i<6;++i){
         cc=ss.charAt(i);
         if(cc=='0' || cc=='1' || cc=='2' || cc=='3' || cc=='4' || 
         cc=='5' || cc=='6' || cc=='7' || cc=='8' || cc=='9'){
            pow*=0.1;
            mantissa+=pow*(cc-'0');
         } else if(cc=='-'){
            sign=-1;
         } else if(cc=='+'){
            sign=1;
         }
      }
      exp=new Double(ss.substring(ss.length()-2,ss.length())).doubleValue();
      exp=Math.pow(10.0,exp);
      return sign*mantissa*exp;
   }
   
   public static String numSubstring(String str,int beg, int end){
      String res=new String(str.substring(beg,end));
      return res.replace(' ','0');
   }
   
   public static int Y2K(int yy){
      int xx=yy>50?1900:2000; //Y2K :-)
      return (yy<1950)?yy+xx:yy;
   }
   
   /** Computes and returns the revolution period of the satellite in days on the basis of meanMotion, incl, and eccentricity.. 
     * Can be used to choose the deep-space or near-space model for orbit calculation. 
    *  <code>periodDays = 1./ meanMotion </code>
    */
   public double getPeriodDays()  {
    // final double MINUTES_IN_DAY = 14400;
    // printAll();
     if (meanMotion == 0) throw new IllegalArgumentException("Second line is not set in the record.");
     /* This part is taken from usat, does not work. Do not know  why.
      System.out.println("meanMotion="+meanMotion);
     double a1 = Math.pow (Sgp4Sdp4.xke / meanMotion, 1.5 );
     System.out.println("a1="+a1);
     double temp = Sgp4Sdp4.ck2 * 1.5 *  (Math.cos (incl) * Math.cos (incl) * 3. - 1.) /
         Math.pow (1. - eccentricity*eccentricity, 1.5);
     System.out.println("temp="+temp);
     double del1 = temp / (a1 * a1);
     System.out.println("del1="+del1);
     double ao = a1 * (1. - del1 * (1.5 * .5 + del1 *  (del1 * 134. / 81. + 1.)));
     System.out.println("ao="+ao);
     double delo = temp / (eccentricity * eccentricity);
     System.out.println("delo="+delo);
     double xnodp = meanMotion / (delo + 1.);
     
     System.out.println("xnodp="+xnodp);

     return 2.*Math.PI / (xnodp * MINUTES_IN_DAY); */

     /*  orbital period hours = 2.7645e-6*A*sqrt(A) A(km)=semimajor axis   */

      return 1./meanMotion;
   }

   public TLERecord cloneMe() {
      TLERecord tmp=new TLERecord();
      
      if(satName.length()>0)
         tmp.satName=new String(satName);
      tmp.satNumber1=satNumber1;
      tmp.type=type;
      tmp.launchYear=launchYear;
      tmp.launchNumOfYear=launchNumOfYear;
      for(byte i=0;i<3;++i)
         tmp.pieceLaunch[i]=pieceLaunch[i];
      tmp.epochYear=epochYear;
      tmp.epochDay=epochDay;
      tmp.fstTimeDerivOfMeanMotion=fstTimeDerivOfMeanMotion;
      tmp.sndTimeDerivOfMeanMotion=sndTimeDerivOfMeanMotion;
      tmp.bstar=bstar;
      tmp.ephemerisType=ephemerisType;
      tmp.elemNumber=elemNumber;
      tmp.checkSum1=checkSum1;
      tmp.satNumber2=satNumber2;
      tmp.incl=incl;
      tmp.ascenOfNode=ascenOfNode;
      tmp.eccentricity=eccentricity;
      tmp.argOfPerigee=argOfPerigee;
      tmp.meanAnomaly=meanAnomaly;
      tmp.meanMotion=meanMotion;
      tmp.revNum=revNum;
      tmp.checkSum2=checkSum2;
      return tmp;
   }
   
   public void printAll(){
      print1stLine();
      print2ndLine();
   }
   
   public void print1stLine(){
      System.out.print(new Byte(lineNum1).toString()+',');
      System.out.print(satNumber1+",");
      System.out.print(String.valueOf(type)+',');
      System.out.print(launchYear+",");
      System.out.print(launchNumOfYear+",");
      System.out.print(new String(pieceLaunch)+",");
      System.out.print(epochYear+",");
      System.out.print(epochDay+",");
      System.out.print(fstTimeDerivOfMeanMotion+",");
      System.out.print(sndTimeDerivOfMeanMotion+",");
      System.out.print(bstar+",");
      System.out.print(new Byte(ephemerisType).toString()+",");
      System.out.print(elemNumber+",");
      System.out.println(new Byte(checkSum1).toString());
   }

   public void print2ndLine(){
      System.out.print(lineNum2+",");
      System.out.print(satNumber2+",");
      System.out.print(incl/toRad+",");
      System.out.print(ascenOfNode/toRad+",");
      System.out.print(eccentricity+",");
      System.out.print(argOfPerigee/toRad+",");
      System.out.print(meanAnomaly/toRad+",");
      System.out.print(meanMotion+",");
      System.out.print(revNum+",");
      System.out.println(checkSum2);

   }
}

