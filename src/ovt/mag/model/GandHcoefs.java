/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/model/GandHcoefs.java,v $
  Date:      $Date: 2003/09/28 17:52:44 $
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

package ovt.mag.model;

//import java.io.*;
import java.lang.Exception.*;

class GandHcoefs extends Object{
   private int Nmaxi;
   private float Gcoefs[][];
   private float Hcoefs[][];
   
   public GandHcoefs(int nmaxi){
      Nmaxi=nmaxi+1;
      Gcoefs=new float[Nmaxi][Nmaxi];
      Hcoefs=new float[Nmaxi][Nmaxi];
      for(int i=0;i<Nmaxi;++i)
         for(int j=0;j<Nmaxi;++j)
            Gcoefs[i][j]=Hcoefs[i][j]=0.0F;
   }
   
   public GandHcoefs(GandHcoefs srcGH){
      Nmaxi=srcGH.getNmaxi();
      Gcoefs=new float[Nmaxi][Nmaxi];
      Hcoefs=new float[Nmaxi][Nmaxi];
      this.init(srcGH);
   }
   
   public void init(GandHcoefs srcGH){
      int maxN=this.Nmaxi,srcNmaxi=srcGH.getNmaxi();
      if(this.Nmaxi!=srcNmaxi){  // cheking for Nmaxi
         maxN=(this.Nmaxi>srcNmaxi)?srcNmaxi:this.Nmaxi;
      }
      for(int i=0;i<maxN;++i)
         for(int j=0;j<maxN;++j){
            Gcoefs[i][j]=srcGH.getGcoefs(i,j);
            Hcoefs[i][j]=srcGH.getHcoefs(i,j);
         }
   }
   
   public int getNmaxi(){
      return Nmaxi;
   }
   
   public static void chkIdx(int ii,int jj) /*throws IOException*/{
/*      if(jj<0 || jj>=Nmaxi)
         throw new IOException("GandHcoefs: incorrect index.");*/
   }
   
   public void setGcoefs(int i,int j,float x){
      chkIdx(i,j);
      Gcoefs[i][j]=x;
   }
   
   public void setHcoefs(int i,int j,float x){
      chkIdx(i,j);
      Hcoefs[i][j]=x;
   }
   
   public void setGHcoefs(int i,int j,float gx,float hx){
      chkIdx(i,j);
      Gcoefs[i][j]=gx;
      Hcoefs[i][j]=hx;
   }
   
   public float getGcoefs(int i,int j){
      chkIdx(i,j);
      return Gcoefs[i][j];
   }

   public float getHcoefs(int i,int j){
      chkIdx(i,j);
      return Hcoefs[i][j];
   }
   
/*   public void print(int y){     // Just for cheking !
     int i,j;
     System.out.println("Year: "+y);
     for(i=0;i<Nmaxi;++i)
        for(j=0;j<=i;++j)
           System.out.println(i+","+j+": "+this.getGcoefs(i,j)+", "+this.getHcoefs(i,j));
   }*/
}
