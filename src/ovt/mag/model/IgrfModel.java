/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/model/IgrfModel.java,v $
  Date:      $Date: 2001/06/21 14:17:41 $
  Version:   $Revision: 2.1 $


=========================================================================*/

package ovt.mag.model;

import java.io.*;
import java.util.*;

import ovt.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.datatype.*;
import ovt.mag.model.GandHcoefs;

/*
 * NOTE: Has a highest limit to how high n it can read. Will ignore higher ones!!
 * Can not trivially raise the limit.
 *
 * @author  root
 * @version 
 */ 
public class IgrfModel extends AbstractMagModel {
    

  protected String igrfDatFileName = OVTCore.getMdataSubdir()+"igrf.d";
  public final static int ERROR_YEAR = -10000;
  protected int year = ERROR_YEAR;
  
  // Gh[144] IGRF coefficients array 
  public static double Gh[] = new double[144];
  // maximum no of harmonics in igrf
  protected final static int Nmax = 10;          // Can not read higher n than this. Will ignore higher ones!!!

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
  private static boolean isaddCol=false;   // Flag for whether addCol has already been filled with values.
  private static double mjdPrev=-100000.0;

  /** Creates new Igrf. */
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
  
  // Returns the year, for which IGRF coefficients are valid.
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
  
  /** Initializing GH coefs for year #year */
  public static void initHashTable(File dataFile, int year)
  throws /*FileNotFoundException,*/ IOException
  {
     if(!ghTable.containsKey(year)) {
        initHashTable(dataFile,year,false);
     }
  }
  
  /** Read IGRF data file (text table). Make sure that IgrfModel.ghTable contains
   * information for the specified year (HashTable key) and that IgrfModel.addCol
   * contains the identical information. Does not read the last column that is
   * not associated with one year (secular variation?).
   * 
   * NOTE: The way of handling errors is not that great. Should ideally
   * be translate into to error messages for the user (and block the change of
   * time) but not sure of good way to do this.<BR>
   * /Erik P G Johansson 2015-10-02
   * 
   * @param initHeader Iff true, only read file header (first row) and
   * initialize minY, maxY, and nothing else. Iff false, then copy g,h values from data file to
   * IgrfModel.addCol and cache.
   * @param year Iff initHeader==true, then must be divisible by five and within
   * range minY-maxY.
   */
  public static void initHashTable(File dataFile, int year, boolean initHeader)
          throws IOException
  {
     int i_column,neededCol,m_idx=-1,n_idx=-1;
     char ghMarker = '\0';
     float flt = 0.0F;
     final String invalidFileFormatMsg = "Invalid format of IGRF data file.";
     BufferedReader inData;
     String str;
     final GandHcoefs ghCoefs = new GandHcoefs(Nmax);  // for Hashtable

     try {
        inData = new BufferedReader(new FileReader(dataFile));
     } catch (NullPointerException|FileNotFoundException e){
        throw new IOException("File "+dataFile+" not found.");
     }
     
     // Read past initial rows with comments.
     // Implicitly read first line of non-comments ("header").
     do {
        str = inData.readLine();
     } while ((str != null) && str.startsWith("#"));

     if (initHeader==true) {            // First time starting (treats header)
        // Reading header
        final StringTokenizer hdTok = new StringTokenizer(str);
        i_column = 0;
        while (hdTok.hasMoreTokens()) {
           ++i_column;          // Skipping the first three "g/h n m" fields.
           String temps = hdTok.nextToken();
           if (i_column==4){
               minY = (int) Double.parseDouble(temps);
           }
           if ((i_column >= 4) && (!temps.contains("-"))) {
               /* Read year from column
                * Last column header may have a column header designating a year
                * interval, e.g. 2015-20, or may entirely lack a column header (i.e. token).
                * Must therefore be prepared for that the last token might not be
                * usable, and that the second-last one should be used.
                */
               maxY = (int) Double.parseDouble(temps);
           }
        }
        
        if(minY >= maxY) {
           throw new IOException(invalidFileFormatMsg+ " Derived start year is greater than the derived end year.");
        }
        return;    // NOTE: Return from init. mode
     }
     
     /* Read actual data in file
      * ------------------------
      * NOTE: Code checks for year divisible by five since the data file source
      * only contains data for every even five years.
     */
     if((year%5)!=0 || year<minY || year>maxY) {
         final String msg = "Invalid year in IGRF init."
                + " The specified year (year="+year+") is outside the allowed"
                + " interval "+minY+"-"+maxY+" for which IGRF can be derived, or not divisible by 5.";
        //throw new IllegalArgumentException(msg);
        throw new IOException(msg);
        //throw new NoIGRFModelForSpecifiedYear(msg);
     }
     
     // Is this year in Hashtable?
     if(ghTable.containsKey(year)) {
         return;
     }

     // Reading gh coefs. for year ##year
     neededCol = 4+(year-minY)/5;          // Definition of needed column
     while (inData.ready()) {  // Iterate over rows in file.
        str = inData.readLine();
        if (str == null) {
            break;
        }
        final StringTokenizer tokGH = new StringTokenizer(str);
        i_column = 0;                   // Number of parsed columns
        while(tokGH.hasMoreTokens()) {  // Parsing one row in file. - Iterate over tokens on row.
           ++i_column;
           final String tmps=tokGH.nextToken();
           switch(i_column){
              case 1:                  // g/h marker
                 char tmpc[] = tmps.toCharArray();
                 ghMarker=tmpc[0];
                 break;
              case 2:                  // getting n index
                 n_idx = Integer.parseInt(tmps);
                 break;
              case 3:                  // getting m index
                 m_idx = Integer.parseInt(tmps); 
                 break;
           }
           if(i_column==neededCol){           // Found the needed column!
              flt = Float.parseFloat(tmps);
              
              if(n_idx>Nmax || m_idx>Nmax || n_idx<0 || m_idx<0) {
                  // NOTE: Best to give proper error message since it is not
                  // obvious that there is an upper limit to n.
                 throw new IOException(invalidFileFormatMsg+ " Can not interpret n and/or m indices. (Can e.g. only read up to n="+Nmax+")");
              }

              switch(ghMarker){
                 case 'g': ghCoefs.setGcoefs(n_idx,m_idx,flt);break;
                 case 'h': ghCoefs.setHcoefs(n_idx,m_idx,flt);break;
                 default: 
                    throw new IOException(invalidFileFormatMsg);
              }
           } else if (tokGH.hasMoreTokens()==false) {  // Is last column?
              if (isaddCol==true)      // addCol already loaded
                 break;                // goto the next line
              else {                   // loading addCol
                 flt = Float.parseFloat(tmps);
                 switch(ghMarker){
                    case 'g': addCol.setGcoefs(n_idx,m_idx,flt);
                       break;
                    case 'h': addCol.setHcoefs(n_idx,m_idx,flt);
                        break;
                    default: 
                       throw new IOException(invalidFileFormatMsg);
                 }
              }
           }

        }
        // CASE: Iterated over all tokens/columns
        if(i_column < neededCol) {
           throw new IOException(invalidFileFormatMsg);
        }
     }
     inData.close();
     
     // Putting year #yy (key) & GH coefs. into hash table
     ghTable.put(year, ghCoefs);

     if(isaddCol==false) {
        isaddCol=true;
     }
  }

/*
 * Sets up coefficients <code>Gh</code> for magnetic field computation 
 * and  position of the eccentric dipole (re)
 *   <code>Eccrr</code>, <code>Eccdx</code>, <code>Eccdy</code>, <code>Eccdz</code>
 * @see #Gh #Eccrr #Eccdx #Eccdy #Eccdz
 */

  protected void setIgrf(float yearf) {
      
     GandHcoefs gANDh=new GandHcoefs(Nmax);
     GandHcoefs ghFloor=new GandHcoefs(Nmax);
     GandHcoefs ghCeil=new GandHcoefs(Nmax);
     int i,j, floorY, ceilY;
     int year = (int) yearf;
     float w1a=0.0F,w2a=0.0F,gg,hh;

     try {
        final File igrfDatFile = Utils.findFile(igrfDatFileName);
        if(isaddCol==false) {     // Starting for the first time
           initHashTable(igrfDatFile,0,true);
        }
      
        // Calculate two years defining a five-year interval containing "year" (and "yearf").
        floorY=(int)(year/10)*10;
        if((year-floorY)>5) {
           floorY+=5;
        }
        ceilY=floorY+5;

        initHashTable(igrfDatFile,floorY);     // Requesting FLOOR year
        ghFloor=(GandHcoefs)ghTable.get(floorY);

        if(ceilY<=maxY) {   // We have not to use additional column
           initHashTable(igrfDatFile,ceilY);   // Requesting CEIL year
           ghCeil=(GandHcoefs)ghTable.get(ceilY);
           w1a=((float)ceilY-yearf)/(float)(ceilY-floorY);
           w2a=1.0F-w1a;
        } else {       // Last additional column have be used (after 2000)
           w1a=1.0F;
           w2a=yearf-(float)floorY;
           ghCeil=addCol;    // Using addCol
        }

     } catch(IOException e) {
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
           Gh[k-1] = f*gANDh.getGcoefs(d1-1,d2-1);
           Gh[k]   = f*gANDh.getHcoefs(d1-1,d2-1);
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
  }

  //******************************************************
  
  /**
   * Informal test code.
   */
  // Checking main block!!!
  /*public static void main(String a[]) throws IOException
  {
      // Does not really work since can not easily instantiate MagProps.
      
        //IgrfModel igrf = new IgrfModel(null);
        //igrf.setIgrf(1993.34F);        
        
        IgrfModel.initHashTable(new File("/home/erjo/work_files/ovt/resources/mdata/igrf.d"), 2000);
        for (int i=1999;i<=2010;i+=2){
            //IgrfModel igrf = new IgrfModel(null);
            //igrf.setIgrf((float) i);
        }
    
  }//*/
  //******************************************************
    
}
