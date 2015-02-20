/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/model/IgrfModel.java,v $
  Date:      $Date: 2001/06/21 14:17:41 $
  Version:   $Revision: 2.1 $


=========================================================================*/

package ovt.mag.model;

import java.io.*;
import java.util.*;
import java.lang.Math.*;
import java.lang.Exception.*;

import ovt.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.datatype.*;
import ovt.mag.model.GandHcoefs;

/*
 *
 * @author  root
 * @version 
 */
 
public class IgrfModel extends AbstractMagModel {

  protected String igrfDatFile=OVTCore.getOVTHomeDir()+File.separator+"mdata"+File.separator+"igrf.d";
  public final static int ERROR_YEAR = -10000;
  protected int year = ERROR_YEAR;
  
  // Gh[144] IGRF coefficients array 
  public static double Gh[] = new double[144];
  // maximum no of harmonics in igrf
  protected static int Nmax = 10;          //not more !

  // Excentric dipole coordinates derived from Gh
  protected static double Eccrr[] = {-0.0625,  0.0405,  0.0281 };
  protected static double Eccdx[] = { 0.3211, -0.9276, -0.1911 };
  protected static double Eccdy[] = { 0.9450,  0.3271,  0.0000 };
  /** Coordinates of z-axis of Excentric dipole derived from Gh.
   * It is dipole vector.
   */
  protected static double Eccdz[] = { 0.0625, -0.1806,  0.9816 };

  private static Hashtable ghTable=new Hashtable();
  private static int minY,maxY;    // Years limits
  private static GandHcoefs addCol=new GandHcoefs(Nmax);
  private static boolean isaddCol=false;
  private static double mjdPrev=-100000.0;

  /** Creates new Igrf */
  public IgrfModel(MagProps magProps) {
    super(magProps);
  }

  protected void setIGRF(double mjd){
     if(Math.abs(this.mjdPrev-mjd)<=31.0)return;  //Speed up it!
     this.mjdPrev=mjd;
     
     Time timeTmp=new Time(mjd);
     float yearf;
     yearf=(float)timeTmp.getYear(mjd)+(float)timeTmp.getMonth()*0.083333F;
     setIgrf(yearf);
  }
  
  public double[] bv(double[] gsm, double mjd){
    setIGRF(mjd);
    // get transformation class
    Trans trans = getTrans(mjd);
    double[] geo = trans.gsm2geo(gsm);
    double[] bb=trans.geo2gsm(igrf(geo));
    return bb;
  }

  //Returns mag. field in GEO CS
  public double[] bvGEO(double[] geo,double mjd){
    setIGRF(mjd);
    return igrf(geo);
  }
  
  // Returns the year, for which IGRF coofissients are valid  
/* ------------------------------------------------------------ 
   FUNCTION: 
      compute igrf field for cartesian geo
   input: 
      geo(3) position vector (geo) in earth radii (re = 6371.2 km)
   output: 
      bv(3)  magnetic field vector in geo (units as set by setigrf)
   files/COMMONs:
      COMMON /cigrf/ with coefficients set by setigrf(mjd)
   remarks: 
        CALL setigrf(mjd) before first use 
--------------------------------------------------------------- */
  protected double[] igrf(double[] geo){

    // Local variables
    int imax, nmax;
    double  f, h[] = new double[144];
    int i, k, m;
    double  s, x, y, z;
    int ihmax, ih, il;
    double  xi[] = new double[3], rq;
    int ihm, ilm;
    double  srq;
    int j;
    double bv[] = new double[3];     // - output
    
    rq = Vect.absv2 (geo);

    if (rq < .8) {
        System.out.println ("igrf call below surface");
    }

    rq = 1. / rq;
    srq = Math.sqrt(rq);
    if (rq < 0.25)
        nmax = (int)((Nmax - 3) * 4.0 * rq + 3);
    else
        nmax = Nmax;

    // number of harmonics depends on the distance from the earth

    //for (d1 = xi, d2 = geo; d1 < xi + 3; )
    //    *d1++ = *d2++ * rq;
    for (j=0; j<3; j++)
        xi[j] = geo[j] * rq;

    ihmax = nmax * nmax;
    imax = nmax + nmax - 2;
    il = ihmax + nmax + nmax;

//    d1 = h + ihmax;
//    d2 = Gh + ihmax;
//    for ( ; d1 <= h + il; )
//        *d1++ = *d2++;
    
    for (j=ihmax; j<il; j++)
        h[j] = Gh[j];
    
    for (k = 0; k < 3; k += 2) {
        i = imax;
        ih = ihmax;
        while (i >= k) {
            il = ih - i - 1;
            f = 2. / (double) (i - k + 2);
            x = xi[0] * f;
            y = xi[1] * f;
            z = xi[2] * (f + f);
            i += -2;
            if (i >= 2) {
                for (m = 3; m <= i + 1; m += 2) {
                    ihm = ih + m;
                    ilm = il + m;
                    h[ilm+1] = Gh[ilm+1] + z * h[ihm+1] + x * (h[ihm+3] - h[ihm-1])
                    -y * (h[ihm+2] + h[ihm-2]);
                    h[ilm] = Gh[ilm] + z * h[ihm] + x * (h[ihm+2] - h[ihm-2])
                     + y * (h[ihm + 3] + h[ihm - 1]);
                }
                h[il + 2] = Gh[il + 2] + z * h[ih + 2] + x * h[ih + 4] 
                    -y * (h[ih + 3] + h[ih]);
                h[il+1] = Gh[il+1] + z * h[ih+1] + y * h[ih + 4] 
                     + x * (h[ih + 3] - h[ih]);
            } else if (i == 0) {
                h[il + 2] = Gh[il + 2] + z * h[ih + 2] + x * h[ih + 4] 
                    -y * (h[ih + 3] + h[ih]);
                h[il+1] = Gh[il+1] + z * h[ih+1] + y * h[ih + 4] 
                     + x * (h[ih + 3] - h[ih]);
            }

            h[il] = Gh[il] + z * h[ih] + (x * h[ih+1] + y * h[ih + 2]) * 2.;
            ih = il;
        }
    }

    s = h[0] * .5 + (h[1] * xi[2] + h[2] * xi[0] + h[3] * xi[1]) * 2.;
    f = (rq + rq) * srq;
    x = f * (h[2] - s * geo[0]);
    y = f * (h[3] - s * geo[1]);
    z = f * (h[1] - s * geo[2]);
    bv[0] = x;
    bv[1] = y;
    bv[2] = z;
    return bv;
  }

  public double[] getEccrr(double mjd) {
    setIGRF(mjd);
    return Eccrr;
  }
  public double[] getEccdx(double mjd) {
    setIGRF(mjd);
    return Eccdx;
  }
  public double[] getEccdy(double mjd) { 
    setIGRF(mjd);
    return Eccdy;
  }
  public double[] getEccdz(double mjd) { 
    setIGRF(mjd);
    return Eccdz;
  }
  
  // Initializing GH coefs for year #yy
  public static void initHashTable(String DatFile,int yy)
  throws /*FileNotFoundException,*/ IOException{
     if(!ghTable.containsKey(new Integer(yy)))
        initHashTable(DatFile,yy,false);
  }
  
  public static void initHashTable(String DatFile,int yy,boolean initH)
  throws IOException{
     int i,j,neededCol,m_idx=-1,n_idx=-1;
     char ghMarker='\0';
     float flt=0.0F;
     String invalidFileFormat=new String("Invalid format of IGRF data file.");
     BufferedReader inData;
     String str=new String(),tmps=new String();
     GandHcoefs ghCoefs=new GandHcoefs(Nmax);  // for Hashtable

     try {
        inData=new BufferedReader(new FileReader(DatFile));
     } catch (FileNotFoundException e){
        throw new IOException("File "+DatFile+" not found.");
     } catch (IOException e){
        throw new IOException("IO error with "+DatFile+" datafile.");
     }

     str=inData.readLine();        //Getting first Line (header)
     if(initH==true){              // First time starting (treats header)
        // Reading header
        StringTokenizer hdTok = new StringTokenizer(str);
        i=0;
        while (hdTok.hasMoreTokens()) {
           ++i;          // skiping "g/h n m" fields
           tmps=new String(hdTok.nextToken());
           switch(i){
              case 4: minY=new Float(tmps).intValue();break;
           }
        }
        maxY=new Float(tmps).intValue();
        if(minY>=maxY)
           throw new IOException(invalidFileFormat);
        return;    // Return from init. mode
     }
     
     //Checking for corrected year number
     if((yy%5)!=0 || yy<minY || yy>maxY)
        throw new IOException("Invalid number of year in IGRF init.");
     
     // Is this year in Hashtable?
     if(ghTable.containsKey(new Integer(yy)))return;

     // Reading gh coefs. for year ##yy
     neededCol=4+(yy-minY)/5;          //Definition of needed column
     while(inData.ready()){
        str=inData.readLine();
        if (str == null) break;
        StringTokenizer tokGH=new StringTokenizer(str);
        i=0;                           // Number parsed columns
        while(tokGH.hasMoreTokens()){  // Parsing one line
           ++i;
           tmps=tokGH.nextToken();
           switch(i){
              case 1:                  // g/h marker
                 char tmpc[]=new char[tmps.length()];
                 tmpc=tmps.toCharArray();
                 ghMarker=tmpc[0];
                 break;
              case 2:                  // getting n index
                 n_idx=new Integer(tmps).intValue();
                 break;
              case 3:                  // getting m index
                 m_idx=new Integer(tmps).intValue(); 
                 break;
           }
           if(i==neededCol){           // Founded needed column!
              flt=new Float(tmps).floatValue();
              
              if(n_idx>Nmax || m_idx>Nmax || n_idx<0 || m_idx<0)
                 throw new IOException(invalidFileFormat);

              switch(ghMarker){
                 case 'g': ghCoefs.setGcoefs(n_idx,m_idx,flt);break;
                 case 'h': ghCoefs.setHcoefs(n_idx,m_idx,flt);break;
                 default: 
                    throw new IOException(invalidFileFormat);
              }
           } else if(tokGH.hasMoreTokens()==false){  //Is last column?
              if(isaddCol==true)       // addCol already loaded
                 break;                // goto the next line
              else {                   // loading addCol
                 flt=new Float(tmps).floatValue();
                 switch(ghMarker){
                    case 'g': addCol.setGcoefs(n_idx,m_idx,flt);
                       break;
                    case 'h': addCol.setHcoefs(n_idx,m_idx,flt);break;
                    default: 
                       throw new IOException(invalidFileFormat);
                 }
              }
           }

        }
        if(i<neededCol)
           throw new IOException(invalidFileFormat);
     }
     inData.close();
     
     // Putting year #yy (key) & GH coefs. into hash table
     ghTable.put(new Integer(yy),ghCoefs);

     if(isaddCol==false)
        isaddCol=true;
  }

/*
 * Sets up coefficients <code>Gh</code> for magnetic field computation 
 * and  position of the eccentric dipole (re)
 *   <code>Eccrr</code>, <code>Eccdx</code>, <code>Eccdy</code>, <code>Eccdz</code>
 * @see #Gh #Eccrr #Eccdx #Eccdy #Eccdz
 */

  protected void setIgrf(float yearf){
     GandHcoefs gANDh=new GandHcoefs(Nmax);
     GandHcoefs ghFloor=new GandHcoefs(Nmax);
     GandHcoefs ghCeil=new GandHcoefs(Nmax);
     int i,j,year=(int)yearf,floorY, ceilY;
     float w1a=0.0F,w2a=0.0F,gg,hh;

     try {
        if(isaddCol==false)      // Starting for the first time
           initHashTable(igrfDatFile,0,true);
      
        floorY=(int)(year/10)*10;
        if((year-floorY)>5)
           floorY+=5;
        ceilY=floorY+5;

        initHashTable(igrfDatFile,floorY);     // Requesting FLOOR year
        ghFloor=(GandHcoefs)ghTable.get(new Integer(floorY));

        if(ceilY<=maxY){   // We have not to use additional column
           initHashTable(igrfDatFile,ceilY);   // Requesting CEIL year
           ghCeil=(GandHcoefs)ghTable.get(new Integer(ceilY));
           w1a=((float)ceilY-yearf)/(float)(ceilY-floorY);
           w2a=1.0F-w1a;
        } else {       // Last additional column have be used (after 2000)
           w1a=1.0F;
           w2a=yearf-(float)floorY;
           ghCeil=addCol;    // Using addCol
        }

     } catch (IOException e){
        System.out.println(e);
     }

     for(i=0;i<=Nmax;++i)       //Computing Coefs. Gij & Hij
        for(j=0;j<=i;++j){
           gg=w1a*ghFloor.getGcoefs(i,j)+w2a*ghCeil.getGcoefs(i,j);
           hh=w1a*ghFloor.getHcoefs(i,j)+w2a*ghCeil.getHcoefs(i,j);
           gANDh.setGHcoefs(i,j,gg,hh);
        }
     if(!ghTable.containsKey(new Integer(year)))
        ghTable.put(new Integer(year),gANDh);  // Store yaer in Hashtable
     
     //Calculating (recalculating) Gh
     float tmp1,tmp2,f,f0;
     int d1,d2,k;
     f0=-1.0F;         // -1.0e-5  for output in gauss
     Gh[0]=0.0F;
     k=2;
     for(i=1;i<=Nmax;++i){
        f0*=0.5*(float)i;
        f=f0/1.4142136F;    //sqrt(2.0)
        d1=i+1;
        d2=1;
        Gh[k-1]=f0*gANDh.getGcoefs(d1-1,d2-1);
        ++k;
        for(j=1;j<=i;++j){
           tmp1=(float)(i+j);
           tmp2=(float)(i-j+1);
           f*=Math.sqrt(tmp1/tmp2);
           d1=i+1;
           d2=j+1;
           Gh[k-1]=f*gANDh.getGcoefs(d1-1,d2-1);
           Gh[k]=f*gANDh.getHcoefs(d1-1,d2-1);
           k+=2;
        }
     }
     this.year=year;

     //Calculating (recalculating) d?,Eccrr, ...
     double h0,dipmom,w1,w2,lx,ly,lz,tmp1d,tmp2d;
     h0=gANDh.getGcoefs(1,0)*gANDh.getGcoefs(1,0)+
        gANDh.getGcoefs(1,1)*gANDh.getGcoefs(1,1)+
        gANDh.getHcoefs(1,1)*gANDh.getHcoefs(1,1);
     dipmom=-Math.sqrt(h0);
     w1=Math.abs(gANDh.getGcoefs(1,0)/dipmom);
     w2=Math.sqrt(1.0-w1*w1);
     tmp1d=Math.atan(gANDh.getHcoefs(1,1)/gANDh.getGcoefs(1,1));
     Eccdz[0]=w2*Math.cos(tmp1d);
     Eccdz[1]=w2*Math.sin(tmp1d);
     Eccdz[2]=w1;
     Eccdx[0]=Eccdx[1]=0.0;
     Eccdx[2]=1.0;

     Vect.crossn(Eccdx,Eccdz,Eccdy);
     Vect.crossn(Eccdy,Eccdz,Eccdx);

     //Excentric dipole (Chapman & Bartels, 1940)
     final float sqrt3=1.7320508F;

     lx=-gANDh.getGcoefs(1,1)*gANDh.getGcoefs(2,0)+
      sqrt3*(gANDh.getGcoefs(1,0)*gANDh.getGcoefs(2,1)+
             gANDh.getGcoefs(1,1)*gANDh.getGcoefs(2,2)+
             gANDh.getHcoefs(1,1)*gANDh.getHcoefs(2,2));
     ly=-gANDh.getHcoefs(1,1)*gANDh.getGcoefs(2,0)+
      sqrt3*(gANDh.getGcoefs(1,0)*gANDh.getHcoefs(2,1)+
             gANDh.getHcoefs(1,1)*gANDh.getGcoefs(2,2)-
             gANDh.getGcoefs(1,1)*gANDh.getHcoefs(2,2));
     lz=2.0*gANDh.getGcoefs(1,0)*gANDh.getGcoefs(2,0)+
      sqrt3*(gANDh.getGcoefs(1,1)*gANDh.getGcoefs(2,1)+
             gANDh.getHcoefs(1,1)*gANDh.getHcoefs(2,1));
     tmp2d=0.25*(lz*gANDh.getGcoefs(1,0)+lx*gANDh.getGcoefs(1,1)+
                ly*gANDh.getHcoefs(1,1))/h0;
     Eccrr[0]=(lx-gANDh.getGcoefs(1,1)*tmp2d)/(3.0*h0);
     Eccrr[1]=(ly-gANDh.getHcoefs(1,1)*tmp2d)/(3.0*h0);
     Eccrr[2]=(lz-gANDh.getGcoefs(1,0)*tmp2d)/(3.0*h0);

/*     //Just for checking
    System.out.println("****Year: "+yearf);
     for(i=0;i<3;++i)
        System.out.println(" "+i+"Eccr: "+Eccrr[i]+" Eccdx: "+Eccdx[i]+" Eccdy: "+Eccdy[i]+" Eccdz: "+Eccdz[i]);
*/
  }

/*  //******************************************************
  // Cheking main block!!!
  public static void main(String a[])
  {
     try{
        IgrfModel igrf=new IgrfModel();
        igrf.setIgrf("igrf_www.d",1993.34F);
        
//        IgrfModel.initHashTable("igrf_www.d",2000);
//        for(int i=1999;i<=2010;i+=2){
//           IgrfModel igrf=new IgrfModel();
//           igrf.setIgrf("igrf_www.d",(float)i);
//        }
     } catch (FileNotFoundException e){
        System.err.println(e);
     } catch (IOException e){
        System.err.println(e);
     }
    
  }
  //******************************************************
*/
}
