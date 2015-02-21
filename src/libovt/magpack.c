
/*  $Source $ */

#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include "constants.h"
#include "globvar.h"
#include "extern.h"
#include "string.h"

#include "tsyg96.h"
#include "tsyg2001.h"

#include <jni.h>
/*#include "ovt_object_Sat.h"*/
/*#include "ovt_mag_model_IgrfModel.h"      Now in JAVA !!!! */
#include "ovt_mag_model_Tsyganenko87.h"
#include "ovt_mag_model_Tsyganenko89.h"
#include "ovt_mag_Trace.h"

/*********  defs.h  *********/
#define MIN(a,b) ( (a) > (b) ) ? (b) : (a)
#define TPI      6.28318531
#define RE       6371.2
#define RAD      57.295779513 
#define DIPMOM   -30483.03 
#define MIN_NUMBER      1e-30

//real
#define RMAX    100.0
#define STEPMIN  0.002
#define STEPMAX  1.0
#define MXX      200
#define NMAX  3
#define IMAX  11
#define NUSE   7
int Trcoord =GSM;      /* trace coordinates GEO GSM  */
double Gh[144];   /* IGRF coefficients array        */
int Nmax = 10;     /* maximum no of harmonics in igrf */

//
int IntModel = 0;  
int ExtModel = 0;  
float TILTf,SWPf,DSTf,BYIMFf,BZIMFf,G1f,G2f;  //  for Tsyg96() and Tsyg2001()
int isMPClipping = 0; 
double Swp, Imf_z ; //for Shue97getR()


double Sint  =   0.0;
double Cost  =   1.0;
double Tsgsm = -100.0;
double Tigrf = -100.0;
double Ggsm[9] = { 1.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,1.0};
/* magnetic DATA  for 1985-01-01 ut = 0000 */
double Eccrr[3]  = {-0.0625,0.0405,0.0281};
double Eccdx[3]   = { 0.3211, -0.9276, -0.1911 };
double Eccdy[3]   = { 0.9450,  0.3271,  0.0000 };
double Eccdz[3]   = { 0.0625, -0.1806,  0.9816 };
double   ss[MXX], xx[MXX], yy[MXX], zz[MXX];


//////////////////////////////////////////////////////////////////////////////////
//
//.Author  Serg Redko 
//

// storage 'g' & 'h' coef. for specific year
// Gcoefs, Hcoefs = float[Nmax+1][Nmax+1]
struct GandHcoefs {
   float **Gcoefs;
   float **Hcoefs;
};

// start field line calculations since 1980 
#define BASEYEAR   1980 

// LINELEN must be more then max line length in the file igrf.d !!!
#define LINELEN   180 

// storage 'g' & 'h' coef. for various years
struct GandHcoefs **ghTable;

struct GandHcoefs *addCol;
int isaddCol = 0; // false
int minY,maxY;    // Years limits

/////////////////////////////////////////////////////////////////////////////////////////////


/* --------------------------------------------------------------------
/*  integration of ordinary differential equations
/*  bulirsch-stoer method
/*  see: numerical recipes , w. press, p 566. 1986.
/*  input:
/*    y(ndim)  initial values at x
/*    ndim     number of dependent variables (maximum 9)
/*    x       independent variable (time)
/*    htry     step size (can be quite big)
/*    eps      maximum permissible error for y()
/*    hmin     minimum step (signal with error=2)
/*  output:
/*    y        updated at new x
/*    x       new value  (=x + htry ! not necessarily)
/*    hnext    recomended value for the next step
/*    error .ne. 0 signals user defined error in SUBROUTINE right()
/*                 or hmin in bsstep()
/* -----------------------------------------------------------------
/*  user has to supply a routine for right hand side derivatives:
/*  SUBROUTINE right (x,y,dydx,error)
/*  REAL*8 x,yy(10),dydx(10)
/* ----------------------------------------------------------------- */




bsstep(y, ndim, x, htry, eps, hmin, hnext, right, error)
double  *y;
int ndim;
double  *x, htry, eps, hmin, *hnext;
int *error;
  int (*right)();
{
    /* Local variables */

    static int  nseq[IMAX] = {
        2, 4, 6, 8, 12, 16, 24, 32, 48, 64, 96                      };
    register double *d1, *d2;
    register int    *l1;
    double  fabs();
    double  temp, xsav, yerr[NMAX], xest, ysav[NMAX], yseq[NMAX], h;
    int i;
    double  dysav[NMAX], errmax;
    int pow2, tmpseq;

    /* Function Body */

    h = htry;
    xsav = *x;
    for (d1 = ysav, d2 = y; d1 < ysav + ndim; )
        *d1++ = *d2++;

    (*right)(x, ysav, dysav, error);
    if (*error != 0)
        return (0);

    tmpseq = nseq[NUSE-2];
    while (fabs(h) > hmin) {
        for (l1 = nseq, i = 0; i < 11; ++l1, ++i) {
            mmid(ysav, dysav, ndim, xsav, h, *l1, yseq, right, error);
            if (*error != 0)
                return (0);
            temp = h / *l1;
            xest = temp * temp;
            rzextr(i, xest, yseq, y, yerr, ndim, NUSE);
            errmax = 0.;

            for (d1 = yerr; d1 < yerr + ndim; ++d1)
                if (errmax < fabs(*d1))
                    errmax = fabs(*d1);

            if (errmax < eps) {
                *x += h;
                temp = (double) tmpseq / *l1;
                if (temp > 2.)
                    temp = 2.;
                *hnext = h * temp;

                /* maximum factor 2 step increase */

                return (0);
            }
        }

        pow2 = 1;
        h = h * 0.25 / (double)(pow2 << ((IMAX - NUSE) / 2));
    }

    printf("bsstep error=2: too large HMIN\n");
    /*
    *error = 2;
    */

    return (0);
}


/* -------------------------------------------------------- 
   FUNCTION:
       transforms cartesian (idir = 1) to 
       spherical or vice versa when (idir =-1)
   input: 
         rv(3) geocentric vector (re)
         idir = +1 cartesian to spherical
                -1 spherical to cartesian
   output:
         rkm  = geocentric distance (km)
         lat  = latitude (deg)
         longi= longitude (deg)
   -------------------------------------------------------- */


car_sph(rv, rkm, lat, longi, idir)
double  *rv, *rkm, *lat, *longi;
int idir;
{

    double  asin(), atan2(), cos(), sin();

    double  rr;
    double  alat, along;
    double  absv();

    /* Function Body */

    if (idir > 0) {
        rr = absv (rv);
        *lat = asin(rv[2] / rr) * RAD;
        *longi = atan2(rv[1], rv[0]) * RAD;
        *rkm = rr * RE;
    } else {
        rr = *rkm / RE;
        alat = *lat / RAD;
        along = *longi / RAD;
        rv[0] = rr * cos(alat) * cos(along);
        rv[1] = rr * cos(alat) * sin(along);
        rv[2] = rr * sin(alat);
    }

    return (0);
}



//////////////////////////////////////////////////////////////////////////////////
//
//.Name      n_lastline
//
//.Descr     called by Java_ovt_mag_Trace_lastlineJNI
//           new lastline function 
//
//.Updated by Serg Redko 
//
//////////////////////////////////////////////////////////////////////////////////

/******************************************************************
 FUNCTION:
  first closed field line starting from rv(3) in the direcion dir(3) 
  mjd,model,rv(3)  =  standard variables
  dir(3):  search direction from rv(3) 
  alt (km) altitude of required footprint
  foot(3) :  footprint position (re)
    idir= 1 north 
        =-1 south
  epst = tolerance (re) for determination of the origin of last closed rv(3)
******************************************************************/

int n_lastline(mjd, xlim, rv, dir, alt, idir, epst)
double  mjd;
double  xlim, *rv, *dir, alt;
int idir;
double  epst;
{
    /* Local variables */

    extern double   absv();
    register double *d1, *d2, *d3;
    double  ftol, rmin,  swap, foot[3];
    int i;
    double  delta,rion, rfoot;
    int go;
    double  rv2[3],bv[3];
    int gout;
    double  gsr[3];


    /* Function Body */

    ftol = (epst < .005) ? .005 : epst;
    delta = 1.0;
    normf(dir, delta);
    rmin = alt / RE + 1.2;
    if (absv(rv) < rmin) {
        printf("lastline: incorrect alt or rv\n");
        exit (-1);
    }

    n_foot_ns(mjd,GSM, xlim, rv, alt, foot, idir);
    rfoot = absv(foot);
    gout = (rfoot < rmin) ? 1 : -1;

    go = 1;
    while (go > 0) {
        for (d1 = rv2, d2 = rv, d3 = dir; d2 < rv + 3; ) {
            *d1++ = *d2;
            *d2++ += *d3++ * gout;
        }

        n_foot_ns(mjd,GSM, xlim, rv, alt, foot, idir);
        rfoot = absv(foot);

        if(absv(rv)>RMAX || absv(rv)<3.0) go =0;
        if (gout < 0) {
            if (rfoot < rmin)
                go = -1;
        } else {
            if (rfoot > rmin) {
                go = -1;
                for (d1 = rv, d2 = rv2; d1 < rv + 3; ) {
                    swap = *d1;
                    *d1++ = *d2;
                    *d2++ = swap;
                }
            }
        }
    }
    if (go == 0) {
        printf("lastline problem !\n");
        return (0);  /* no need to divide interval */
        }

        /*
        printf("last: rv=%.1lf %.1lf %.1lf foot=%.1lf %.1lf %.1lf\n",
         rv[0],rv[1],rv[2],foot[0],foot[1],foot[2]);
         */

    while (delta > ftol) {
        delta /= 2.;
        for (d1 = gsr, d2 = rv, d3 = rv2; d1 < gsr + 3; )
            *d1++ = (*d2++ + *d3++) * 0.5;
        n_foot_ns(mjd, GSM, xlim, gsr, alt, foot, idir);
        rfoot = absv(foot);
        if (rfoot < rmin) {
            for (d1 = rv, d2 = gsr; d1 < rv + 3; )
                for (i = 0; i < 3; ++i) 
                    *d1++ = *d2++;
                        /* tolerance proportional to gyroradius of 1 keV proton */
                        magbv(rv,bv);
                        rion=4.58e3/absv(bv)/RE;
                        if(rion<0.02) rion=0.02;
                        ftol=rion;
        } else {
            for (d1 = rv2, d2 = gsr; d1 < rv2 + 3; )
                *d1++ = *d2++;
        }
    }

    return (1);
}







// Serg Redko  remark:
// this is old lastline function

/******************************************************************
 FUNCTION:
  first closed field line starting from rv(3) in the direcion dir(3) 
  mjd,model,rv(3)  =  standard variables
  dir(3):  search direction from rv(3) 
  alt (km) altitude of required footprint
  foot(3) :  footprint position (re)
    idir= 1 north 
        =-1 south
  epst = tolerance (re) for determination of the origin of last closed rv(3)
******************************************************************/

int lastline(mjd, model, xlim, rv, dir, alt, idir, epst)
double  mjd, model;
double  xlim, *rv, *dir, alt;
int idir;
double  epst;
{
    /* Local variables */

    extern double   absv();
    register double *d1, *d2, *d3;
    double  ftol, rmin,  swap, foot[3];
    int i;
    double  delta,rion, rfoot;
    int go;
    double  rv2[3],bv[3];
    int gout;
    double  gsr[3];


    /* Function Body */

    ftol = (epst < .005) ? .005 : epst;
    delta = 1.0;
    normf(dir, delta);
    rmin = alt / RE + 1.2;
    if (absv(rv) < rmin) {
        printf("lastline: incorrect alt or rv\n");
        exit (-1);
    }

    foot_ns(mjd, model, GSM, xlim, rv, alt, foot, idir);
    rfoot = absv(foot);
    gout = (rfoot < rmin) ? 1 : -1;

    go = 1;
    while (go > 0) {
        for (d1 = rv2, d2 = rv, d3 = dir; d2 < rv + 3; ) {
            *d1++ = *d2;
            *d2++ += *d3++ * gout;
        }

        foot_ns(mjd, model, GSM, xlim, rv, alt, foot, idir);
        rfoot = absv(foot);

        if(absv(rv)>RMAX || absv(rv)<3.0) go =0;
        if (gout < 0) {
            if (rfoot < rmin)
                go = -1;
        } else {
            if (rfoot > rmin) {
                go = -1;
                for (d1 = rv, d2 = rv2; d1 < rv + 3; ) {
                    swap = *d1;
                    *d1++ = *d2;
                    *d2++ = swap;
                }
            }
        }
    }
    if (go == 0) {
        printf("lastline problem\n");
        return (0);  /* no need to divide interval */
        }

        /*
        printf("last: rv=%.1lf %.1lf %.1lf foot=%.1lf %.1lf %.1lf\n",
         rv[0],rv[1],rv[2],foot[0],foot[1],foot[2]);
         */

    while (delta > ftol) {
        delta /= 2.;
        for (d1 = gsr, d2 = rv, d3 = rv2; d1 < gsr + 3; )
            *d1++ = (*d2++ + *d3++) * 0.5;
        foot_ns(mjd, model, GSM, xlim, gsr, alt, foot, idir);
        rfoot = absv(foot);
        if (rfoot < rmin) {
            for (d1 = rv, d2 = gsr; d1 < rv + 3; )
                for (i = 0; i < 3; ++i) 
                    *d1++ = *d2++;
                        /* tolerance proportional to gyroradius of 1 keV proton */
                        magbv(rv,bv);
                        rion=4.58e3/absv(bv)/RE;
                        if(rion<0.02) rion=0.02;
                        ftol=rion;
        } else {
            for (d1 = rv2, d2 = gsr; d1 < rv2 + 3; )
                *d1++ = *d2++;
        }
    }

    return (1);
}


com_read (fo, str)
FILE *fo;
char    *str;
{
    char    c, d;
    register char   *c1;
    char    ok = 0, done = 1;
    int cnt = 0;

    c1 = str;

    c = getc(fo);
    while (c != '\n' && c != EOF && done == 1) {
        if (c == '/') {
            d = getc(fo);
            if (d == '*')
                done = 0;
            else
             {
                cnt += 2;
                *c1++ = c;
                *c1++ = d;
                c = d;
            }
        } else
         {
            ++cnt;
            *c1++ = c;
            c = getc(fo);
        }
    }

    *c1 = 0;

    if (done == 0)
        while (c != '\n' && c != EOF)
            c = getc(fo);

    /*** check for all blanks -- if all blanks return -cnt  ***/

    if (cnt > 0) {
        c1 = str;
        while (c1 < str + cnt && !ok) {
            if (*c1++ != ' ')
                ok = 1;
        }
        if (!ok)
            cnt = -cnt;
    }


    return (cnt);
}


/************************************************************************
   compute corrected magnetic coordinates at the reference altitude alt 
   input:
      mjd:     modified julian day (use fdate() to find mjd! )
      geo(3):  geographic position (cartesian) in units of re=6371.2 km
      alt:     reference altitude (km)
   output:
      mlat:    corrected magnetic latitude (deg)
      mlong:   corrected magnetic longitude (deg)
      mlt:     corrected magnetic local time (hours)
      ell:     L value (equatorial distance of the field line igrf)
*************************************************************************/


corrgma(mjd, geo, alt, mlat, mlong, mlt, ell)
double  mjd, *geo, alt, *mlat, *mlong, *mlt, *ell;
{
    /* Initialized data */

    static double   mjdlast = 0.;

    /* Builtin functions */
    double  sqrt(), acos(), atan2();

    /* Local variables */

    extern double   absv(), new_sign(), dot(), rok();
    double  fabs(), fmod();
    double  foot[3], gmst, r;
    double  ft[3], sv[3];
    double  sig;
    double  spos[3];
    double  cos2;
    double  year;

    /* Function Body */

    if (fabs(mjd - mjdlast) > 30.) {
        year = rok(mjd);
        setigrf (year);
        mjdlast = mjd;
    }

    geo_gma(0, geo, ft, 1);
    sig = new_sign(1.0, ft[2]);
    igrf(geo, sv);
    r = dot(geo, sv) * ft[2];
    if (r > 0.) {
        *mlat = 0.;
        *mlong = 0.;
        *mlt = 0.;
        *ell = 1.;
        return (0);
    }

    traceigrf(geo, alt, 0, foot);
    geo_gma(0, foot, ft, 1);
    r = absv(ft);

    cos2 = (ft[0] * ft[0] + ft[1] * ft[1]) / (r * r);

    if (cos2 < 4.8e-7)
        cos2 = 4.8e-7;

    /* --------->  cos(89.96)^2=4.8d-7 */

    *ell = r / cos2;

    sunmjd(mjd, spos);
    gei_geo(spos, sv, mjd, 1);
    geo_gma(0, sv, spos, 1);

    r = sqrt((alt / RE + 1.) / *ell);

    if (r > 1.)
        r = 1.;

    *mlat = acos(r) * 180. /  PI * sig;
    *mlong = atan2(ft[1], ft[0]) * 180. / PI;
    *mlt = (atan2(ft[1], ft[0]) - atan2(spos[1], spos[0])) * 12. / PI + 36.;

    *mlt = fmod(*mlt, 24.0);

    return (0);
}


/* vector product c =  a x b */

 cross(a, b, c)
double  *a, *b, *c;
{
    /* Function Body */

    c[0] = a[1] * b[2] - a[2] * b[1];
    c[1] = a[2] * b[0] - a[0] * b[2];
    c[2] = a[0] * b[1] - a[1] * b[0];

    return (0);
}


/* normalized vector product */

 crossn (a, b, c)
double  *a, *b, *c;
{
    /* Local variables */

    register double *d1;
    register double x;
    double  absv();

    /* Function Body */

    cross (a, b, c);
    x = absv (c);

    if (x >= 1e-30)
        for (d1 = c; d1 < c + 3; )
            *d1++ /= x;

    return (0);
}



difv(a,b,c)
double a[3],b[3],c[3];
/* vector difference a-b=c */
{
        int i;
        for(i=0; i<3;i++)
                c[i]=a[i]-b[i];
}

double p_to_ab(p,a,b)
double p[3],a[3],b[3];
/* distance of point p to interval a-b */
{
        double pa[3],ba[3],c[3],aa;
        double absv();

        difv(p,a,pa);
        difv(b,a,ba);
        aa=absv(ba);
        if(aa<MIN_NUMBER) aa=MIN_NUMBER;
        cross(pa,ba,c);
        return (absv(c)/aa);
}

double p_to_abc(p,a,b,c)
double p[3],a[3],b[3],c[3];
/* distance of point p from  plane a-b-c */
{
        double pa[3],ba[3],ca[3],nn[3];
        double absv(), dot(), ret_val;

        difv(p,a,pa);
        difv(b,a,ba);
        difv(c,a,ca);
        crossn(ba,ca,nn);  /* normal to plane */
        ret_val=dot(pa,nn);
        return (fabs(ret_val));
}


/*  RETURNs dipole tilt angle for modified julian day mjd */


double  dip_tilt(mjd)
double  mjd;
{
    double  fabs();
    double  ret_val;
    double  asin();

    /* Local variables */

    extern double   rok();

    if (fabs(mjd - Tigrf) > 30.) {
        setigrf(rok(mjd));
        Tigrf = mjd;
    }
    setgsm(mjd);
    Tsgsm = mjd;
    ret_val = asin(Sint) * RAD;

    return (ret_val);
}


/* ------------------------------------------------------------ 
   FUNCTION: 
      compute dipole field (nt) in gsm coordinates 
   input: 
      sint, cost: sine and cosine of the tilt angle 
      gsm(3)      position vector (earth radii, re) 
   output:
      bv(3)     : field vector (nanotesla) 
   ------------------------------------------------------------ 
   dipmom = magnetic moment of the earth for igrf1985 model */


dipol(sint, cost, gsm, bv)
double  sint, cost, *gsm, *bv;
{
    double  sqrt();
    double  b, x, y, z, r2, bx, by, bz;

    /* Function Body */

    x = gsm[0] * cost - gsm[2] * sint;
    y = gsm[1];
    z = gsm[0] * sint + gsm[2] * cost;

    r2 = x * x + y * y + z * z;
    b = DIPMOM / r2 / r2 / sqrt(r2);

    bx = 3.0 * x  * z * b;
    by = 3.0 * y  * z * b;
    bz = (3.0 * z * z - r2) * b;

    bv[0] = bx * cost + bz * sint;
    bv[1] = by;
    bv[2] = -bx * sint + bz * cost;

    return (0);
}


double  angle_v(a, b)
/*angle between two vectors */
double  a[3], b[3];
{
    double  ma, mb, ret_val;
    int i;
    ma = mb = ret_val = 0.0;
    for (i = 0; i < 3; i++) {
        ret_val += a[i] * b[i];
        ma += a[i] * a[i];
        mb += b[i] * b[i];
    }
        if(ret_val<MIN_NUMBER) return(0.0);
        ret_val /= sqrt(ma * mb);
        ret_val = acos(ret_val) * RAD;
    return (ret_val);
}

double full_angle_v(x,y)
double x[3],y[3];
/* full angle 0 360 deg between vectors y and x 
                (as in the trigonometric plane)
*/
{

 double  mm, mx, my,cc,ss, ret_val;
 double z[3], yy[3];
 double absv(), dot();
     cross(x,y,z);
     cross(z,x,yy);
     mm=absv(x)*absv(y);
     if(mm<MIN_NUMBER) return(0.0);
     cc = dot(x,y)/mm;
     ss=  absv(z)/mm;
     if(dot(y,yy)<0.0) ss *= -1.0;   /* indicate angle greater than 180 */
         ret_val = acos(cc) * RAD;
         if(ss<0.0) ret_val = 360.0-ret_val;
     return (ret_val);
}

int seen(observer,pos)
double observer[3], pos[3];  /* units of RE */
/* returns 0 if earth screens pos(ition) from observer else ret=1 */
{
        double angle_v(), absv();
        double angle, h;

        angle= angle_v(observer,pos);
        if(angle<90.0) 
        return (1);
        else 
        h=absv(pos)*sin(angle/RAD);
        if(h>1.0)
        return(1);
        else
        return (0);
}




/* scalar product of two vectors */

double  dot(a, b)
double  *a, *b;
{
    /* Local variables */

    register double *d1, *d2;
    double  ret_val = 0.0;

    /* Function Body */

    for (d1 = a, d2 = b; d1 < a + 3; )
        ret_val += *d1++ * *d2++;

    return (ret_val);
}


double  dvec(a, b)
double  *a, *b;
{
    /* Local variables */

    register double *d1, *d2, *d3;
    double  c[3];
    double  absv();
    double  ret_val = 0;

    /* Function Body */

    for (d1 = a, d2 = b, d3 = c; d1 < a + 3; )
        *d3++ = *d1++ - *d2++;

    ret_val = absv (c);

    return (ret_val);
}


// Serg Redko remark:
// This is old externbv function; Tsyg95 isn't used

/* ---------------------------------------------------------------- 
   FUNCTION: 
     compute EXTERNAL field model according to tsyganenko models: 
     planet. space sci., vol 35, no 11, pp1347-1358, 1987 
     planet. space sci., vol 37, no 1, pp5-20  1989 
   input: 
     gsm(3):   position vector (re) in gsm coordinates 
     amodel:   10.0-15.0  model number for 1987 model 
               20.0-25.0  for model 1989. 
     amodel =95 for T95_06 model
             uses also PSW - solar wind pressure 0.5 to 10 nPa
                       DSTindex  -100 to 20 nT
                       AEindex     40 to 800 nT
     sint,cost: sine and cosine of the dipole tilt angle 
   output: 
     bex(3):   EXTERNAL field (nt) in gsm coordinates 
   ---------------------------------------------------------------- */

externbv(gsm, amodel, sint, cost, bex)
double  *gsm, amodel, sint, cost, *bex;
{

    /* Local variables */

    double  fmod();
    double  kp;
    double atan();
    register double *d1, *d2;

    /* Function Body */

    kp = fmod(amodel, 10.0);


    if (amodel >= 10. && amodel <= 15.)
        tsyg87(gsm, kp, sint, cost, bex);
    else if (amodel >= 20. && amodel <= 25.)
        tsyg89(gsm, kp, sint, cost, bex);
    else if (amodel == 95.0) {
        kp=atan(sint/cost);
        tsyg95(gsm,PSW,DSTindex,AEindex, Imf, kp, bex);
        }
    else {
        printf("externbv: wrong model number\n");
        exit (-1);
    }
    if (ImfMethod==1)
    for (d1 = bex, d2 = Imf; d1 < bex + 3; d1++, d2++)
        *d1 +=  *d2;

    return (0);
}


/*  FUNCTION: 
/*      convert (year,month,day,hour,mins,sec,msec) to mjd or vice versa 
/*      mjd is the day number counted from 
/*      1 jan 1950 00 hr 00 mins 00 sec 
/*      this routine is valid for dates between 1 jan 1950 and 31 dec 2099
/*  inputs:  for idr = 1 
/*      year,month,day,hour,mins,sec,msec
/*      (year can have two or four digits e.g. 1984 or 84)
/*  outputs: for idr = 1
/*      mjd (r*8)
/*      when idr = -1 mjd is input and year,... output */


 fdate (year, month, day, hour, mins, sec, msec, mjd, idr)

int *year, *month, *day, *hour, *mins, *sec, *msec;
double  *mjd;
int idr;
{
    /* Local variables */

    int jday;
    double  temp;
    int l, m, n, jj;

    if (idr > 0) {
        jj = (14 - *month) / 12;
        l = *year % 1900 - jj;
        *mjd = *day - 18234 + (l * 1461) / 4 + ((*month - 2 + jj * 12) * 367) / 12;
        *mjd += ((*hour * 3600 + *mins * 60 + *sec) * 1000 + *msec) / 8.64e7;
    } else {
        temp = *mjd + 5.7870370370370369e-9;
        jday = (int) temp;
        l = (jday + 18204) * 4000 / 1461001;
        n = jday - (l * 1461) / 4 + 18234;
        m = (n * 80) / 2447;
        *day = n - (m * 2447) / 80;
        jj = m / 11;
        *month = m + 2 - jj * 12;
        *year = l + 1900 + jj;
        temp = (temp - jday) * 24.;
        *hour = (int) temp;
        temp = (temp - *hour) * 60.;
        *mins = (int) temp;
        temp = (temp - *mins) * 60.;
        *sec = (int) temp;
        temp = (temp - *sec) * 1000.;
        *msec = temp + .5;
    }

    return (0);
}


/* FUNCTION:
      computes magnetic footprint (towards the northern pole)
   input: 
      mjd :  modified julian day 
      model : magnetic field model see setmodel() 
      icor   :   =  GEO coordinates 
                 =  GSM coordinates
      alt  =  altitude (km) of footprint
      rv(3) initial position (re)
   output:
      foot(3) in (re) and icor coordinates */

foot_in (mjd, model, icor, rv, alt, foot)
double  mjd, model;
int icor;
double  *rv, alt, *foot;
{
    static double   halt, step;
    static int  n;
    static double   xlim = -51.0;

    /* Function Body */

    setmodel (mjd, model, icor);
    step = 0.1;

    if (tracelin(rv, alt, step, 0, xlim, xx, yy, zz, ss, MXX, &n) > 0) {
        foot[0] = xx[n-1];
        foot[1] = yy[n-1];
        foot[2] = zz[n-1];
        return(1);
    } else {
        foot[0] = foot[1] = foot[2] = 999.0;
        /*
        printf("foot_in: wrong tracelin\n");
        */
        return(-1);
    }
}


//////////////////////////////////////////////////////////////////////////////////
//
//.Name      n_foot_ns
//
//.Descr     called by n_lastline()
//           this is new foot_ns 
//
//.Updated by Serg Redko 
//
//////////////////////////////////////////////////////////////////////////////////

/* computes magnetic footprint in the north (idir=1) or south (idir=-1) 

   input: 
    rv(3) initial position (re)
    alt = altitude (km)
   output:
      foot(3) in (re) */

n_foot_ns(mjd, icor, xlim, rv, alt, foot, idir)
double  mjd;
int icor;
double  xlim, *rv, alt, *foot;
int idir;
{

    /* Local variables */

    static double   halt;
    extern double   absv();
    static double   step;
    static int  n;

    /* Function Body */

    if (absv(rv) < 1.) {
        printf("foot_ns: Input vector below the surf\n");
        exit (-1);
    }


    step = (idir < 0) ? -0.1 : 0.1;
    if (tracelin(rv, alt, step, 0, xlim, xx, yy, zz, ss, MXX, &n) > 0) {
        foot[0] = xx[n-1];
        foot[1] = yy[n-1];
        foot[2] = zz[n-1];
        return (1);
    } else {
        foot[0] = 999.0;
        foot[1] = 999.0;
        foot[2] = 999.0;

        //printf("foot_ns: wrong tracelin\n");

        return (-1);
    }
}

// Serg Redko remark:
// this is old foot_ns function

foot_ns(mjd, model, icor, xlim, rv, alt, foot, idir)
double  mjd, model;
int icor;
double  xlim, *rv, alt, *foot;
int idir;
{

    /* Local variables */

    static double   halt;
    extern double   absv();
    static double   step;
    static int  n;

    /* Function Body */

    if (absv(rv) < 1.) {
        printf("foot_ns: Input vector below the surf\n");
        exit (-1);
    }

    setmodel(mjd, model, icor);

    step = (idir < 0) ? -0.1 : 0.1;

    if (tracelin(rv, alt, step, 0, xlim, xx, yy, zz, ss, MXX, &n) > 0) {
        foot[0] = xx[n-1];
        foot[1] = yy[n-1];
        foot[2] = zz[n-1];
        return (1);
    } else {
        foot[0] = 999.0;
        foot[1] = 999.0;
        foot[2] = 999.0;
        /*
        printf("foot_ns: wrong tracelin\n");
        */
        return (-1);
    }
}


/* FUNCTION:
     find cartesian coordinates of a magnetic footprint at z=0 of rv(3) 
   input:
     mjd,model,icor  (see foot_in) 
     xlim = negative number for the maximum distance in the tail 
     rv(3),foot(3) : see foot_in  */


foot_out(mjd, model, icor, xlim, rv, foot)
double  mjd, model;
int icor;
double  xlim, *rv, *foot;
{
    static double   step;
    static int  n;
    static double   halt = 0.0;

    /* Function Body */

    step = (rv[2] < 0.0) ? 0.1 : -0.1;
    setmodel(mjd, model, icor);

    if (tracelin(rv, halt, step, 0, xlim, xx, yy, zz, ss, MXX, &n) > 0) {
        foot[0] = xx[n-1];
        foot[1] = yy[n-1];
        foot[2] = zz[n-1];
        return (1);
    } else {
        foot[0] = 999.0;
        foot[1] = 999.0;
        foot[2] = 999.0;
        /*
        printf("foot_out: wrong tracelin\n");
        */
        return (-1);
    }
}



/* ------------------------------------------------------ 
    FUNCTION:
      transforms gei to geo when ic=+1
             or  geo to gei when ic=-1
    input:
      mjd - modified julian day
      geo or gei
    output:
      gei or geo
 -------------------------------------------------------- */


gei_geo(gei, geo, mjd, ic)
double  *gei, *geo, mjd;
int ic;
{

    double  sin(), cos();
    double  fmod();

    /* Local variables */

    extern double   gmstime();
    double  theta, ct, st;


/*  changed SEP 94 
    theta = fmod(mjd, 1.0) * TPI + gmstime(mjd);
*/  
    theta = gmstime(mjd);

    st = sin(theta);
    ct = cos(theta);

    if (ic > 0) {
        geo[0] = ct * gei[0] + st * gei[1];
        geo[1] = -st * gei[0] + ct * gei[1];
        geo[2] = gei[2];
    } else {
        gei[0] = ct * geo[0] - st * geo[1];
        gei[1] = st * geo[0] + ct * geo[1];
        gei[2] = geo[2];
    }

    return (0);
}



gei_gs(mjd, coord, dir, gei, gse)
double  mjd;
int coord;  /* GSE  or GSEQ or GSM or SMC  or GEI   */
int dir;   /* +1 from gei to gse(q) -1 reverse */
double  gei[3], gse[3];
{
    static double   mjdlast = 0;
    static int  coordlast = -10;

    static double   geigs[9], gmst;

    extern double   fmod(), gmstime(), sin(), cos();
    double  theta, ct, st;
    double  dipgei[3],sunv[3];
    static double   rotsun[3] = {
        0.122, -0.424, 0.899                    };
    static double   eqlipt[3] = {
        0.0, -0.398, 0.917                  };

   
    switch (coord) {
    case GEI:
        if (dir > 0) {
            gse[0] = gei[0];
            gse[1] = gei[1];
            gse[2] = gei[2];
        } else {
            gei[0] = gse[0];
            gei[1] = gse[1];
            gei[2] = gse[2];
        }
        return (1);
    case GSM:
    case SMC:
        sunmjd(mjd, sunv);
        /*find  dipole axis in GEI  changed SEP 94 */
        theta =  gmstime(mjd);
        st = sin(theta);
        ct = cos(theta);
        dipgei[0] = ct * Eccdz[0] - st * Eccdz[1];
        dipgei[1] = st * Eccdz[0] + ct * Eccdz[1];
        dipgei[2] = Eccdz[2];

        if(coord==GSM) {
        crossn(dipgei, sunv, geigs+3);
        crossn(sunv, geigs+3, geigs+6);
        crossn(geigs+3,geigs+6,geigs);
        } else {
        crossn(dipgei,sunv,geigs+3);
        crossn(geigs+3,dipgei,geigs);
        crossn(geigs,geigs+3,geigs+6);
        }
        break;
    case GSE:
        sunmjd(mjd, geigs);
        crossn(eqlipt, geigs, geigs+3);
        crossn(geigs, geigs+3, geigs+6);
        break;
    case GSEQ:
        sunmjd(mjd, geigs);
        crossn(rotsun, geigs, geigs+3);
        crossn(geigs, geigs+3, geigs+6);
        break;
    default:
        printf("gei_gs: incorrect coord\n");
        return (-1);
    }


    if (dir > 0) {
        gse[0] = geigs[0] * gei[0] + geigs[1] * gei[1] + geigs[2] * gei[2];
        gse[1] = geigs[3] * gei[0] + geigs[4] * gei[1] + geigs[5] * gei[2];
        gse[2] = geigs[6] * gei[0] + geigs[7] * gei[1] + geigs[8] * gei[2];
    } else {
        gei[0] = geigs[0] * gse[0] + geigs[3] * gse[1] + geigs[6] * gse[2];
        gei[1] = geigs[1] * gse[0] + geigs[4] * gse[1] + geigs[7] * gse[2];
        gei[2] = geigs[2] * gse[0] + geigs[5] * gse[1] + geigs[8] * gse[2];
    }
    return (0);
}





/*-----------------------------------------------------------------
/* transform geo(3) to gma(3) if idir =1 and vice versa if idir=-1 
/* flag=0 magnetic dipole 
/*     =1 eccentric dipole 
 -----------------------------------------------------------------*/

 geo_gma(flag, geo, gma, idir)
int flag;
double  *geo, *gma;
int idir;
{
    register double *d1, *d2, *d3;
    double  tmp[3];
    double  ff;
    extern double   dot();

    /* Function Body */

    ff = (flag == 1) ? 1.0 : 0.0;

    if (idir > 0) {
        for (d1 = tmp, d2 = geo, d3 = Eccrr; d1 < tmp + 3; )
            *d1++ = *d2++ - *d3++ * ff;

        d1 = gma;
        *d1++ = dot (Eccdx, tmp);
        *d1++ = dot (Eccdy, tmp);
        *d1   = dot (Eccdz, tmp);

    } else {
        d1 = geo;
        *d1++ = Eccdx[0] * gma[0] + Eccdy[0] * gma[1] + Eccdz[0] * gma[2] + Eccrr[0] * ff;
        *d1++ = Eccdx[1] * gma[0] + Eccdy[1] * gma[1] + Eccdz[1] * gma[2] + Eccrr[1] * ff;
        *d1   = Eccdx[2] * gma[0] + Eccdy[2] * gma[1] + Eccdz[2] * gma[2] + Eccrr[2] * ff;
    }
    return (0);
}





/*  transforms geo to gsm (ic = 1) or gsm to geo (ic=-1) */
/*  physical vector                                      */


geo_gsm(geo, gsm, ic)
double  *geo, *gsm;
int ic;
{
    double  x, y, z;

    /* Function Body */

    if (ic > 0) {
        x = geo[0];
        y = geo[1];
        z = geo[2];
        gsm[0] = Ggsm[0] * x + Ggsm[1] * y + Ggsm[2] * z;
        gsm[1] = Ggsm[3] * x + Ggsm[4] * y + Ggsm[5] * z;
        gsm[2] = Ggsm[6] * x + Ggsm[7] * y + Ggsm[8] * z;
    } else {
        x = gsm[0];
        y = gsm[1];
        z = gsm[2];
        geo[0] = Ggsm[0] * x + Ggsm[3] * y + Ggsm[6] * z;
        geo[1] = Ggsm[1] * x + Ggsm[4] * y + Ggsm[7] * z;
        geo[2] = Ggsm[2] * x + Ggsm[5] * y + Ggsm[8] * z;
    }

    return (0);
}


/*  transforms geo to gsm (ic = 1) or gsm to geo (ic=-1) */
/*  mjd modified julian day                              */

 geo_gsmd(geo, gsm, mjd, ic)
double  *geo, *gsm, mjd;
int ic;
{
    double  x, y, z;

    /* Function Body */

    setgsm(mjd);
    
    if (ic > 0) {
        x = geo[0];
        y = geo[1];
        z = geo[2];
        gsm[0] = Ggsm[0] * x + Ggsm[1] * y + Ggsm[2] * z;
        gsm[1] = Ggsm[3] * x + Ggsm[4] * y + Ggsm[5] * z;
        gsm[2] = Ggsm[6] * x + Ggsm[7] * y + Ggsm[8] * z;
    } else {
        x = gsm[0];
        y = gsm[1];
        z = gsm[2];
        geo[0] = Ggsm[0] * x + Ggsm[3] * y + Ggsm[6] * z;
        geo[1] = Ggsm[1] * x + Ggsm[4] * y + Ggsm[7] * z;
        geo[2] = Ggsm[2] * x + Ggsm[5] * y + Ggsm[8] * z;
    }

    return (0);
}


 get_dipax(oz_dip)
double  *oz_dip;
{
    /*
    extern double dz[3];
    */
    register double *d1, *d2;

    /* Function Body */

    for (d1 = oz_dip, d2 = Eccdz; d1 < oz_dip + 3; )
        *d1++ = *d2++;

    return (0);
}


/* RETURNs dipole tilt angle for modified julian day mjd */
/* and tgsm  = tilt of gsm z-axis in gseq coordinates    */
/* (for solar wind inter)                                */


 get_tilteq(mjd, tilt, tgsm)
double  mjd, *tilt, *tgsm;
{

    double  asin();
    double  fabs();

    /* Local variables */

    static double   year;
    static double   gmst, sint, sunv[3], sungeo[3];
    static double   geo[3];
    static double   gsm[3];
    extern double   rok(), dot();

    year = rok(mjd);
    if (fabs(mjd - Tigrf) > 30.) {
        setigrf(year);
        Tigrf = mjd;
    }
    gei_geo(sunv, sungeo, mjd, 1);
    sint = dot (sungeo, Eccdz);
    *tilt = asin(sint) * RAD;
    gsm[0] = 0.;
    gsm[1] = 0.;
    gsm[2] = 1.;
    setgsm(mjd);
    geo_gsm(geo, gsm, -1);
    gei_geo(gsm, geo, mjd, -1);
    gei_gs(mjd, GSEQ, 1, gsm, geo);
    *tgsm = asin(geo[1]) * RAD;

    return (0);
}


/*------------------------------------------------------ 
    FUNCTION:
       greenwich mean sideral time (radians)
       for modified julian day (mjd)
---------------------------------------------------------*/


double  gmstimeold(mjd)
double  mjd;
{

    /* Local variables */

    double  ret_val;
    double  tmp;
    double  dj;
    double  fmod();

    tmp = (mjd + 18262.5) * .065709822 + 6.646065;
    dj = fmod (tmp, 24.0);
    ret_val = dj * 15. / RAD;

    return (ret_val);
}

double gmstime(mjd)
double mjd;
{
  double ret, gha();
  struct julian jday;
 
  mjd2jul(&mjd,&jday,1);
  
  ret=gha(&jday)/RAD;

  return(ret);
}
 


/* ------------------------------------------------------------ 
   FUNCTION: 
      compute igrf field for cartesian geo
   input: 
      geo(3) position vector (geo) in earth radii (re = 6371.2 km)
   output: 
      bv(3)  magnetic field vector in geo (units as set by setigrf)
   files/COMMONs:
      COMMON /cigrf/ with coefficients set by setigrf(year)
   remarks: 
        CALL setigrf(year) before first use 
--------------------------------------------------------------- */

igrf(geo, bv)
double  *geo, *bv;
{
    double  sqrt();

    /* Local variables */

    register double *d1, *d2;
    int imax, nmax;
    double  f, h[144];
    int i, k, m;
    double  s, x, y, z;
    int ihmax, ih, il;
    double  xi[3], rq;
    int ihm, ilm;
    double  srq;
    double  absv2();

    rq = absv2 (geo);

    if (rq < .8) {
        printf ("igrf call below surface\n");
    }

    rq = 1. / rq;
    srq = sqrt(rq);
    if (rq < 0.25)
        nmax = (Nmax - 3) * 4.0 * rq + 3;
    else
        nmax = Nmax;

    /* number of harmonics depends on the distance from the earth */

    for (d1 = xi, d2 = geo; d1 < xi + 3; )
        *d1++ = *d2++ * rq;

    ihmax = nmax * nmax;
    imax = nmax + nmax - 2;
    il = ihmax + nmax + nmax;

    d1 = h + ihmax;
    d2 = Gh + ihmax;
    for ( ; d1 <= h + il; )
        *d1++ = *d2++;

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

    return (0);
}


/* -------------------------------------------------------------
C Function:
C  compute igrf magnetic field in geodetic coordinates
C input:
C     glat  (deg)  geodetic latitude
C     glon  (deg)  geodetic longitude (east)
C     alt   (km)   altitude above geodetic ellipsoide
C output:
C    bnorth,beast,bdown  field components
C                        referenced to local geodetic (z-axis down)
C    babs      field magnitude
C    units: nanotesla as set in setigrf(year)
C ------------------------------------------------------------- */

igrfgd (glat, glon, alt, bnorth, beast, bdown, babs)
double  glat, glon, alt;
double  *bnorth, *beast, *bdown, *babs;
{

    double  geo[3], bv[3];
    double  rlat, ct, st, d, rlon, cp, sp, rho, bxx, byy, bzz, brho;

    rlat = glat * 174532.93e-7;
    ct = sin(rlat);
    st = cos(rlat);
    d = sqrt(40680925.0 - 272336.0 * ct * ct);
    rlon = glon * 174532.93e-7;
    cp = cos(rlon);
    sp = sin(rlon);
    rho = (alt + 40680925.0 / d) * st / 6371.2;

    geo[0] = rho * cp;
    geo[1] = rho * sp;
    geo[2] = (alt + 40408589.0 / d) * ct / 6371.2;

    igrf(geo, bv);

    bxx = bv[0];
    byy = bv[1];
    bzz = bv[2];
    brho = byy * sp + bxx * cp;

    *babs = sqrt(bxx * bxx + byy * byy + bzz * bzz);
    *beast = byy * cp - bxx * sp;
    *bnorth = bzz * st - brho * ct;
    *bdown = -bzz * ct - brho * st;

    return (0);
}



/*  idir= 1 converts vector in km to re  */
/*       -1 converts vector in re to km  */


km_re(rv, idir)
double  *rv;
int idir;
{
    register double f;
    register double *d1;

    /* Function Body */

    f = (idir > 0) ? 1.0 / RE : RE;

    for (d1 = rv; d1 < rv + 3; )
        *d1++ *= f;

    return (0);
}

double  absv2(vec)
double  *vec;
{
    double  sqrt();
    register double sum = 0.0;
    register double *d1;

    for (d1 = vec; d1 < vec + 3; ++d1)
        sum += *d1 * *d1;
    return (sum);
}

double  absv(vec)
double  *vec;
{
    double  sqrt();
    register double sum = 0.0;
    register double *d1;

    for (d1 = vec; d1 < vec + 3; ++d1)
        sum += *d1 * *d1;
    return (sqrt(sum));
}


/* return either the lenght of length squared of a vector  */
/*      sq = 0 return length squared                       */
/*      sq = 1 return length                               */

double  len_sq(vec, sq)
double  *vec;
char    sq;
{
    double  sqrt();

    /* Local variables */

    register double sum = 0.0;
    register double *d1;
    double  ret_val;

    /* Function Body */

    for (d1 = vec; d1 < vec + 3; ++d1)
        sum += *d1 * *d1;

    ret_val = (sq == 1) ? sqrt(sum) : sum;

    return (ret_val);
}


//////////////////////////////////////////////////////////////////////////////////
//
//.Name      magbv
//
//.Descr     externbv() is inside magbv() now
//
//.Updated by Serg Redko 
//
//////////////////////////////////////////////////////////////////////////////////

/* ------------------------------------------------
    rv(3)  position vector (geo) or (gsm) in re 
    bv(3)   field nt

--------------------------------------------------- */
 magbv(rv, bv)
double  *rv, *bv;
{

    double  fabs(), fmod(), atan();
    register double *d1, *d2;
    double  b1[3],  amodel, geo[3], bex[3], gsm[3];
    double  kp;

    /* Function Body */


    for (d1 = bv, d2 = bex; d1 < bv + 3; ) {
        *d1++ = 0.0;
        *d2++ = 0.0;
    }

// Tracing only for GSM !!!

    if (Trcoord == GSM) /* gsm coordinates */ {
        if (IntModel == -10)
            dipol(Sint, Cost, rv, bv);
        else {
            geo_gsm(geo, rv, -1);
            igrf(geo, b1);
            geo_gsm(b1, bv, 1);
        }

                // it was externbv routine:

        switch (ExtModel){
          case T87:
                  tsyg87(rv, KpIndex, Sint, Cost, bex);
                  break;
          case T89:
                  tsyg89(rv, KpIndex, Sint, Cost, bex);
                  break;
          case T96:
                  tsyg96(rv, bex);
                  break;
	  case T2001:
                  tsyg2001(rv, bex);
                  break;  
          default:
          printf("externbv: wrong model number\n");
          exit (-1);
        }

// must be rem
//    if (ImfMethod==1){
//       for (d1 = bex, d2 = Imf; d1 < bex + 3; d1++, d2++)
//      *d1 +=  *d2;
//     printf("ImfMethod==1 !!! \n");
//    }



    
    } else {
        printf("incorrect Trcoord in magbv\n");
        exit (-1);
    }

    for (d1 = bv, d2 = bex; d1 < bv + 3; d1++, d2++)
        *d1 +=  *d2 * ModelFactor;

    return (0);
}



/* ----------------------------------------------------------- 
   FUNCTION: 
      compute magnetic local time and magnetic latitude 
   input: 
      mjd modified julian day 
      geo(3) geocentric position (in units of re = 6371.2km) 
      ityp = 0 dipole  ityp = 1 eccentric dipole 
   output: 
      mlat (degrees) 
      mlt  (hours) 
   ----------------------------------------------------------- 
   rh  hour to radian 
       igrf model valid for 30 days 
   ----------------------------------------------------------- */


magpos(mjd, geo, ityp, mlat, mlt)
double  mjd, *geo;
int ityp;
double  *mlat, *mlt;
{
    double  atan2(), sqrt(), asin();

    /* Local variables */

    register double *d1, *d2, *d3;
    double  gmst, sunv[3], f, s[3], a1, a2, a3, s1;
    extern double   rok();
    extern double   dot();
    double  tmp;
    double  fabs(), fmod();

    /* Function Body */

    if (fabs(mjd - Tigrf) > 30.) {
        setigrf(rok(mjd));
        Tigrf = mjd;
    }
    gei_geo(sunv, s, mjd, 1);
    a1 = dot (Eccdx, s);
    a2 = dot (Eccdy, s);
    s1 = atan2(a2, a1) / .261799;

    f = (ityp == 1) ? 1.0 : 0.0;

    for (d1 = s, d2 = geo, d3 = Eccrr; d1 < s + 3; )
        *d1++ = *d2++ - *d3++ * f;

    a1 = dot (Eccdx, s);
    a2 = dot (Eccdy, s);
    a3 = dot (Eccdz, s);

    f = atan2(a2, a1) / .261799;
    tmp = f - s1 + 36.;
    *mlt = fmod(tmp, 24.0);

    s1 = sqrt(a1 * a1 + a2 * a2 + a3 * a3);

    *mlat = asin(a3 / s1) * RAD;

    return (0);
}


/*  magnetopause currents */
/*  tsyganenko long       */


 mapll(gsm, bv, sint, cost, kp)
double  *gsm, *bv, sint, cost, kp;
{
    /* Initialized data */

    /*********  mapll.h  *********/
    static double   b4v[6] = {
        -2.084, -2.453, -2.881, -2.97, -3.221, -5.075                       };
    static double   b5v[6] = {
        .001795, .001587, -2.95e-4, .002086, -.00114, .002762                       };
    static double   b6v[6] = {
        .00638, .007402, .009055, .01275, .02166, .03277                        };
    static double   c1v[6] = {
        -23.49, -29.41, -29.48, -26.79, -30.43, -27.35                      };
    static double   c2v[6] = {
        .06082, .08101, .06394, .06328, .04049, .04986                      };
    static double   c3v[6] = {
        .01642, .02322, .03864, .03622, .05464, .06119                      };
    static double   c4v[6] = {
        -.02137, -.1091, -.2288, .08345, .00888, -.1211                         };
    static double   c5v[6] = {
        32.21, 40.75, 41.77, 39.72, 42., 47.48                      };
    static double   c6v[6] = {
        -.04373, -.07995, -.05849, -.06, -.01035, -.0502                        };
    static double   c7v[6] = {
        -.02311, -.03859, -.06443, -.07825, -.1053, -.1477                      };
    static double   c8v[6] = {
        -.2832, -.2755, -.4683, -.9698, -1.63, .838                         };
    static double   c9v[6] = {
        -.0023, -.00276, .001222, 1.78e-4, .0038, -.0101                        };
    static double   c10v[6] = {
        -6.31e-4, -4.08e-4, -5.19e-4, -5.73e-4, -.00103, -.0057                         };
    static double   dxv[6] = {
        29.21, 29.36, 28.99, 26.81, 22., 25.17                      };
    static double   a1v[6] = {
        -.09673, -.485, -1.132, -1.003, -1.539, -2.581                      };
    static double   a2v[6] = {
        -10.63, -12.84, -18.05, -16.98, -14.29, -7.726                      };
    static double   a3v[6] = {
        1.21, 1.856, 2.625, 3.14, 3.479, 5.045                      };
    static double   a4v[6] = {
        34.57, 40.06, 48.55, 52.81, 53.36, 53.31                        };
    static double   a5v[6] = {
        -.04502, -.0294, -.004868, -.08625, -.0042, .02262                      };
    static double   a6v[6] = {
        -.06553, -.09071, -.1087, -.1478, -.2043, -.1972                        };
    static double   b1v[6] = {
        -.02952, -.02993, -.03824, -.03501, -.03932, -.01981                        };
    static double   b2v[6] = {
        .3852, .5465, .8514, .55, .6409, .428                       };
    static double   b3v[6] = {
        -.03665, -.04928, -.0522, -.07778, -.1058, -.1055                       };
    static double   kp_last = -5;
    static double   a1, a2, a3, a4, a5, a6, b1, b2, b3, b4, b5, b6;
    static double   dx, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10;

    double  exp();
    double  fabs(), fmod();

    /* Local variables */

    double  y, z;
    int model, tmodel;
    double  w1, w2, y2, z2,  ex1, ex2;

    /* Function Body */

    if (fabs(kp - kp_last) > .05) {
        w2 = fmod(kp, 1.0);
        w1 = 1. - w2;
        model = (int) (kp) + 1;
        if (model > 5) {
            model = 5;
            w1 = 0.;
            w2 = 1.;
        } else if (model < 1) {
            model = 1;
            w1 = w2;
            w2 = 0.;
        }

        tmodel = model - 1;

        dx = dxv[tmodel] * w1 + dxv[model] * w2;
        a1 = a1v[tmodel] * w1 + a1v[model] * w2;
        a2 = a2v[tmodel] * w1 + a2v[model] * w2;
        a3 = a3v[tmodel] * w1 + a3v[model] * w2;
        a4 = a4v[tmodel] * w1 + a4v[model] * w2;
        a5 = a5v[tmodel] * w1 + a5v[model] * w2;
        a6 = a6v[tmodel] * w1 + a6v[model] * w2;
        b1 = b1v[tmodel] * w1 + b1v[model] * w2;
        b2 = b2v[tmodel] * w1 + b2v[model] * w2;
        b3 = b3v[tmodel] * w1 + b3v[model] * w2;
        b4 = b4v[tmodel] * w1 + b4v[model] * w2;
        b5 = b5v[tmodel] * w1 + b5v[model] * w2;
        b6 = b6v[tmodel] * w1 + b6v[model] * w2;
        c1 = c1v[tmodel] * w1 + c1v[model] * w2;
        c2 = c2v[tmodel] * w1 + c2v[model] * w2;
        c3 = c3v[tmodel] * w1 + c3v[model] * w2;
        c4 = c4v[tmodel] * w1 + c4v[model] * w2;
        c5 = c5v[tmodel] * w1 + c5v[model] * w2;
        c6 = c6v[tmodel] * w1 + c6v[model] * w2;
        c7 = c7v[tmodel] * w1 + c7v[model] * w2;
        c8 = c8v[tmodel] * w1 + c8v[model] * w2;
        c9 = c9v[tmodel] * w1 + c9v[model] * w2;
        c10 = c10v[tmodel] * w1 + c10v[model] * w2;
        kp_last = kp;
    }

    ex1 = exp(gsm[0] / dx);
    ex2 = exp(gsm[0] * 2. / dx);
    y = gsm[1];
    y2 = y * y;
    z = gsm[2];
    z2 = z * z;
    bv[0] = ex1 * (a1 * z * cost + a2 * sint) + ex2 * (a3 * z * cost + (a4 
         + a5 * y2 + a6 * z2) * sint);
    bv[1] = ex1 * (b1 * y * z * cost + b2 * y * sint) + ex2 * (b3 * y * z * 
        cost + (b4 * y + b5 * y2 * y + b6 * y * z2) * sint);
    bv[2] = ex1 * ((c1 + c2 * y2 + c3 * z2) * cost + c4 * z * sint) + ex2 * 
        ((c5 + c6 * y2 + c7 * z2) * cost + (c8 * z + c9 * z * y2 + c10 * 
        z2 * z) * sint);

    return (0);
}


mmid(y, dydx, ndim, xs, htot, nstep, yout, right, error)
double  *y, *dydx;
int ndim;
double  xs, htot;
int nstep;
double  *yout;
int *error;
    int (*right)();
{

    /* Local variables */

    register double *d1, *d2, *d3, *d4;
    double  swap, h;
    int n;
    double  x, h2, ym[10], yn[10];

    /* Function Body */

    h = htot / nstep;
    for ( d1 = ym, d2 = yn, d3 = y, d4 = dydx; d1 < ym + ndim; ) {
        *d1++ = *d3;
        *d2++ = *d3++ + h * *d4++;
    }

    x = xs + h;
    (*right)(&x, yn, yout, error);
    if (*error != 0)
        return (0);
    h2 = h * 2.;
    for (n = 2; n <= nstep; ++n) {
        for ( d1 = ym, d2 = yn, d3 = yout; d1 < ym + ndim; ) {
            swap = *d1 + h2 * *d3++;
            *d1++ = *d2;
            *d2++ = swap;
        }
        x += h;
        (*right)(&x, yn, yout, error);
        if (*error != 0)
            return (0);
    }

    for ( d1 = ym, d2 = yn, d3 = yout; d1 < ym + ndim; ++d3)
        *d3 = (*d1++ + *d2++ + h * *d3) * 0.5;

    return (0);
}



double  new_sign (a, b)
double  a, b;
{
    double  c;
    double  ret_val;
    double  fabs();

    c = fabs(a);

    ret_val = ( b < 0.0) ? -c : c;

    return (ret_val);
}


/* change length of vector vec(3) to norm */

 normf(vec, norm)
double  *vec, norm;
{

    /* Local variables */

    double  absv();
    register double *d1, s;

    /* Function Body */

    s = absv (vec);

    for (d1 = vec; d1 < vec + 3; ++d1)
        *d1 *= norm / s;

    return (0);
}


/*    decomposes input vector  into perpendicular and parallel */
/*    components to the reference vector ref                   */



perpar(vv, ref, per, par)
double  vv[3], ref[3], per[3], par[3];
{
    double  dd;
    double  dot();
    int i;
    dd = dot(vv, ref) / dot(ref, ref);
    for (i = 0; i < 3; i++) {
        par[i] = ref[i] * dd;
        per[i] = vv[i] - par[i];
    }
}



/* right hand side for field line tracing with bsstep */

rhanda(s, r, drds, error)
double  *s, *r, *drds;
int *error;
{
    /* Builtin functions */

    double  sqrt();

    /* Local variables */

    register double *d1, *d2;
    double  b, bv[3], rv[3];
    double  absv();

    /* Function Body */

    for (d1 = rv, d2 = r; d1 < rv + 3; )
        *d1++ = *d2++;

    igrf(rv, bv);

    b = absv(bv);

    for (d1 = drds, d2 = bv; d1 < drds + 3; )
        *d1++ = *d2++ / b;

    return (0);
}


/*  contribution of the ring current to EXTERNAL field (nt) */
/*  tsyganenko long                                         */


 rhandb(s, r, drds, error)
double  *s, *r, *drds;
int *error;
{
    double  sqrt();

    /* Local variables */

    double  b;
    double  r2, bv[3], rv[3];
    double  absv();
    register double *d1, *d2;

    /* Function Body */

    /* right hand side for field line tracing with bsstep */

    for (d1 = rv, d2 = r; d1 < rv + 3; )
        *d1++ = *d2++;

    magbv(rv, bv);

    b = absv(bv);

    for (d1 = drds, d2 = bv; d1 < drds + 3; )
        *d1++ = *d2++ / b;

    /* return error code if outside the magnetosphere */

    if (rv[0] > 13. || rv[0] < -52.)
        *error = 1;

    r2 = rv[1] * rv[1] + rv[2] * rv[2];
    if (r2 > RMAX*RMAX)
        *error = 1;
    return (0);
}



 ringl(gsm, bv, sint, cost, kp)
double  *gsm, *bv, sint, cost, kp;
{

    /* Initialized data */

    static double   brcv[6] = {
        -20.55, -25.51, -31.43, -39.68, -43.49, -74.43                      };
    static double   rrcv[6] = {
        5.18, 5.207, 4.878, 4.902, 4.514, 4.658                         };
    static double   kp_last = -5.;
    static double   rc, br;

    double  sqrt();
    double  fabs(), fmod();

    /* Local variables */

    double  d, r, x, z;
    int model;
    double  w1, w2,  bx, by, bz, gx, gy, gz;

    /* Function Body */

    if (fabs(kp - kp_last) > .05) {
        w2 = fmod(kp, 1.0);
        w1 = 1. - w2;
        model = (int) (kp) + 1;
        if (model > 5) {
            model = 5;
            w1 = 0.;
            w2 = 1.;
        } else if (model < 1) {
            model = 1;
            w1 = w2;
            w2 = 0.;
        }

        rc = rrcv[model-1] * w1 + rrcv[model] * w2;
        br = brcv[model-1] * w1 + brcv[model] * w2;
        kp_last = kp;
    }

    gx = gsm[0] * cost - gsm[2] * sint;
    gy = gsm[1];
    gz = gsm[0] * sint + gsm[2] * cost;
    r = sqrt(gx * gx + gy * gy + 1.0e-10) / rc;
    z = gz / rc;
    d = r * r + z * z + 4.;
    d = d * d * sqrt(d);
    bz = br * 4. * (z * 2. * z - r * r + 8.) / d;
    x = br * 12. * r * z / d;
    bx = x * gx / (r * rc);
    by = x * gy / (r * rc);
    bv[0] = bx * cost + bz * sint;
    bv[1] = by;
    bv[2] = -bx * sint + bz * cost;

    return (0);
}


/*************************************************************
   Function:
   Returns year (double) for a given modified Julian day mjd 
*************************************************************/

double  rok (mjd)
double  mjd;
{
    /* Local variables */

    double  ret_val;
    int mili, year;
    int da, hh, mi, mo, sec;

    fdate(&year, &mo, &da, &hh, &mi, &sec, &mili, &mjd, -1);
    ret_val = (double)year + (double)(mo - 1) / 12. + (double) da / 365.;

    return (ret_val);
}


 rzextr(iest, xest, yest, yz, dy, ndim, nuse)
int iest;
double  xest, *yest, *yz, *dy;
int ndim, nuse;
{

    /* Local variables */

    register double *d1, *d2, *d3, *d4, *d5, *d6;
    double  fabs();
    static double   d[70], x[11];
    double  b, c;
    double  v, b1;
    int m1;
    double  fx[7], yy, ddy;

    /* Function Body */

    x[iest] = xest;
    if (iest == 0) {
        for (d1 = yz, d2 = d, d3 = dy, d4 = yest; d1 < yz + ndim; d2 += 7) {
            *d1++ = *d4;
            *d2 = *d4;
            *d3++ = *d4++;
        }
    } else {
        --nuse;
        m1 = MIN(iest, nuse);

        for (d1 = fx + 1, d2 = x + iest - 1; d1 <= fx + m1; )
            *d1++ = *d2-- / xest;

        d2 = d;
        d5 = dy;
        d6 = yz;
        for (d1 = yest; d1 < yest + ndim; d2 += 7) {
            yy = *d1++;
            v = *d2;
            c = yy;
            *d2 = yy;
            for (d4 = d2 + 1, d3 = fx + 1; d3 <= fx + m1; ) {
                b1 = *d3++ * v;
                b = b1 - c;
                if (fabs(b) > 1e-22) {
                    b = (c - v) / b;
                    ddy = c * b;
                    c = b1 * b;
                } else
                    ddy = v;

                if (d3 != fx + m1 - 1)
                    v = *d4;

                *d4++ = ddy;
                yy += ddy;
            }
            *d5++ = ddy;
            *d6++ = yy;
        }
    }

    return (0);
}


/*  derives transformat matrix geo to gsm  */
/*     if(year<1965) sets geo = gsm tilt = 0. */

setgsm(mjd)
double  mjd;
{
    double  sqrt();

    /* Local variables */

    register double *d1;
    double  year, gmst, sunv[3];
    int igm, it;
    double  rok();
    double  dot(), fabs();
    double  tilt;

    year = rok(mjd);
    igm = (year < 1981.) ? 0 : 1;


    if (fabs(mjd - Tsgsm) < 1e-4)
        return (0);

    Tsgsm = mjd;
    if (igm == 1) {
         sunmjd(mjd, sunv);
         gei_geo(sunv, Ggsm, mjd, 1);
         Sint = dot(Ggsm, Eccdz);
         Cost = sqrt(1. - Sint * Sint);
         crossn(Eccdz, Ggsm, &Ggsm[3]);
         crossn(Ggsm, &Ggsm[3], &Ggsm[6]);
     } else {
     printf("setgsm: dipole tilt set to zero\n");
     Sint = 0.;
     Cost = 1.;

        for (d1 = Ggsm; d1 < Ggsm + 9; )
            *d1++ = 0.0;
        for (d1 = Eccrr; d1 < Eccrr + 3; )
            *d1++ = 0.0;

        Ggsm[0] = 1.;
        Ggsm[4] = 1.;
        Ggsm[8] = 1.;
    }

    return (0);
}


// Serg Redko remark :
// this is old setigrf() function that uses old format file igrf.d  
// see n_setigrf()
/* ------------------------------------------------------------ 
 FUNCTION:
   sets up coefficients Gh(144) for magnetic field computation 
   and  position of the eccentric dipole (re)
 input: 
    year  
 output:
    to var.h
 files:
    igrf.d  that contains coeficients for igrf geomagnetic field model 
   (source:eos june 17, 1986)
--------------------------------------------------------------- */

setigrf(year)
double  year;
{
    double  sqrt(), atan(), cos(), sin();

    /* Local variables */

    register double *d1, *d2, *d3;
    FILE * fopen(), *fo;
    char    str[80];
    char    gh[4];
    double  fabs();

    static int  nyrs;
    static double   f;
    static int  i, j,  m, n;
    static double   vdata[5], years[5], f0, h0, w1, w2, gg[121], hh[121];
    static int  in;
    static double   lx, ly, lz, dipmom;
    static double   tmp1, tmp2;
    char    *c1, *skip_in_str();
        
        printf("C: igrf coef for %f year\n",year);

    sprintf(str, "%sigrf_c.d", Mdirectory);

    if (( fo = fopen (str, "r")) == NULL) {
        printf("cannot read %s file\n", str);
        exit(-1);
    }

    while (com_read(fo, str) <= 0)
        ;
    sscanf (str, "%d", &nyrs);

    if (nyrs < 2 || nyrs > 5) {
        printf ("Wrong nyrs in igrf.d\n");
        exit(-1);
    }
    while (com_read(fo, str) <= 0)
        ;
    sscanf (str, "%d", &Nmax);

    if (Nmax < 3 || Nmax > 10) {
        printf ("Wrong Nmax in igrf.d\n");
        exit(-1);
    }

    while (com_read(fo, str) <= 0)
        ;

    c1 = str;
    for (i = 0; i < nyrs; ++i) {
        sscanf (c1, "%lf", &years[i]);
        c1 = skip_in_str (1, c1);
    }

    in = 0;
    for (i = 0; i < nyrs - 1; ++i)
        if (year >= years[i])
            in = i;

    w2 = year - years[in];
    if (w2 < 0.)
        w2 = 0.;
    w1 = 1.;

    if (in + 1 < nyrs - 1) {
        w2 /= years[in+1] - years[in];
        w1 = 1.0 - w2;
    }

    for (d1 = Gh; d1 < Gh + 144; )
        *d1++ = 0.;

    for (n = 0; n < Nmax + 1; ++n) {
        d1 = gg + 11 * n;
        d2 = hh + 11 * n;
        for (m = 0; m <= n; ++m) {
            /*
            while (com_read(fo,str) <= 0);
            sscanf (str,"%c %d %d",&c,&i,&j);
            c1 = skip_in_str (2,str);
        */
            fscanf(fo, "%s %d %d", gh, &i, &j);

            for (d3 = vdata; d3 < vdata + nyrs; ) {
                /*
               c1 = skip_in_str (1,c1);
               sscanf (c1,"%lf",d3++);
           */
                fscanf(fo, "%lf", d3++);
            }

            if (n != i || m != j) {
                printf ("\nWrong gg in igrf.d\n");
                exit (-1);
            }
            *d1++ =  w1 * vdata[in] + w2 * vdata[in + 1];

            /*
            while (com_read(fo,str) <= 0);
        printf("%s\n",str);
            sscanf (str,"%c %d %d",&c,&i,&j);
            c1 = skip_in_str (2,str);
        */
            fscanf (fo, "%s %d %d", gh, &i, &j);
            for (d3 = vdata; d3 < vdata + nyrs; ) {
                /*
               c1 = skip_in_str (1,c1);
               sscanf (c1,"%lf",d3++);
           */
                fscanf (fo, "%lf", d3++);
            }
            if (n != i || m != j) {
                printf ("\nWrong gg in igrf.d\n");
                exit (-1);
            }
            *d2++ = w1 * vdata[in] + w2 * vdata[in + 1];
        }
    }

    fclose (fo);

    d1 = Gh;
    *d1++ = 0.0;

    f0 = -1.;       /* f0 = -1.0d-5  for output in gauss */

    for (n = 1; n <= Nmax; ++n) {
        d2 = gg + 11 * n;
        d3 = hh + 11 * n + 1;
        f0 = f0 * .5 * n;
        f = f0 * sqrt(2.0) / 2.;
        *d1++ = *d2++ * f0;
        ++i;
        for (m = 1; m <= n; ++m) {
            tmp1 = (double) (n + m);
            tmp2 = (double) (n - m + 1);
            f = f * tmp1 / tmp2 * sqrt(tmp2 / tmp1);
            *d1++ = *d2++ * f;
            *d1++ = *d3++ * f;
        }
    }

    /* --   derivation of transformation from geograph to geomagn coord. */

    h0 = gg[11] * gg[11] + gg[12] * gg[12] + hh[12] * hh[12];
    dipmom = -sqrt(h0);
    w1 = fabs (gg[11] / dipmom);
    w2 = sqrt(1. - w1 * w1);
    tmp1 = atan(hh[12] / gg[12]);
    Eccdz[0] = w2 * cos(tmp1);
    Eccdz[1] = w2 * sin(tmp1);
    Eccdz[2] = w1;
    Eccdx[0] = 0.0;
    Eccdx[1] = 0.0;
    Eccdx[2] = 1.0;

    crossn(Eccdx, Eccdz, Eccdy);
    crossn(Eccdy, Eccdz, Eccdx);

    /* ---  eccentric dipole (chapman and bartels, 1940) */

    lx = -gg[12] * gg[22] + sqrt(3.) * (gg[11] * gg[23] + gg[12] * gg[24]
         + hh[12] * hh[24]);
    ly = -hh[12] * gg[22] + sqrt(3.) * (gg[11] * hh[23] - hh[12] * gg[24]
         + gg[12] * hh[24]);
    lz = gg[11] * 2.0 * gg[22] + sqrt(3.) * (gg[12] * gg[23] + hh[12] * hh[23]);
    tmp2 = (lz * gg[11] + lx * gg[12] + ly * hh[12]) * 0.25 / h0;

    Eccrr[0] = (lx - gg[12] * tmp2) / 3. / h0;
    Eccrr[1] = (ly - hh[12] * tmp2) / 3. / h0;
    Eccrr[2] = (lz - gg[11] * tmp2) / 3. / h0;

    return (0);
}


/* ---------------------------------------------------------- 
   FUNCTION:
         set magnetic field model and coordinates
   input:
         mjd     modified julian day

         model = -21.0 ....-25.0      (dipole + tsyganenko 89)
                 -11.0 ... -15.0      (dipole + tsyganenko 87)
                 -10.0                (dipole)
                  10.0                (igrf)
                  11.0 ...  15.0      (igrf +   tsyganenko 87)
                  21.0 ...  25.0      (igrf + tsyganenko 89)

         icor = GEO coordinates
                GSM coordinates
------------------------------------------------------------- */


setmodel(mjd, model, icor)
double  mjd, model;
int icor;
{
    /* Local variables */

    extern double   rok();
    double  fabs();

    Model = model;
    Trcoord = (icor == GEO) ? GEO : GSM;

    if (fabs (mjd - Tigrf) > 30.) {
        setigrf(rok(mjd));
        Tigrf = mjd;
    }
        
    if (fabs(mjd - Tsgsm) > 1e-5) {
        setgsm(mjd);
        Tsgsm = mjd;
    }
    return (0);
}


char    *skip_in_str (n, str)
int n;
char    *str;
{
    register char   *c1;
    register int    i;

    c1 = str;

    while (*c1 == ' ')
        ++c1;

    if (*c1 == 0)
        return (0);

    for (i = 0; i < n; ++i) {

        while (*c1 != ' ' && *c1 != 0)
            ++c1;

        if (*c1 == 0)
            return (0);

        while (*c1 == ' ')
            ++c1;

        if (*c1 == 0)
            return (0);
    }

    return (c1);
}


/*  transforms sm to gsm (ic=1) or gsm to sm (ic=-1) */

sm_gsm(sm, gsm, ic)
double  *sm, *gsm;
int ic;
{

    /* Function Body */

    if (ic > 0) {
        gsm[0] = sm[0] * Cost + sm[2] * Sint;
        gsm[1] = sm[1];
        gsm[2] = -sm[0] * Sint + sm[2] * Cost;
    } else {
        sm[0] = gsm[0] * Cost - gsm[2] * Sint;
        sm[1] = gsm[1];
        sm[2] = gsm[0] * Sint + gsm[2] * Cost;
    }

    return (0);
}


/*  transforms sm to gsm (ic=1) or gsm to sm (ic=-1) */


sm_gsmd(sm, gsm, mjd, ic)
double  *sm, *gsm, mjd;
int ic;
{
    /* Local variables */

    double  fabs();

    /* Function Body */

    if (fabs(mjd - Tsgsm) > 1e-4)
        setgsm(mjd);

    if (ic > 0) {
        gsm[0] = sm[0] * Cost + sm[2] * Sint;
        gsm[1] = sm[1];
        gsm[2] = -sm[0] * Sint + sm[2] * Cost;
    } else {
        sm[0] = gsm[0] * Cost - gsm[2] * Sint;
        sm[1] = gsm[1];
        sm[2] = gsm[0] * Sint + gsm[2] * Cost;
    }

    return (0);
}


/* ------------------------------------------------------ 
    FUNCTION: 
           calculates unit sun vector (gei) and 
           greenwich mean sideral time gmst (radians)
           for modified julian day (mjd) 
--------------------------------------------------------- */
sunmjd(mjd,sunv)
double  mjd, *sunv;
{
  struct julian jday;
  mjd2jul(&mjd,&jday,1);
  sun_vect(&jday,sunv);
  normf(sunv,1.0e0);
}


 sunmjdold(mjd, sunv, gmst)
double  mjd, *sunv, *gmst;
{
    double  sin(), cos(), fmod();
    double  tmp;
    double  fmod();

    /* Local variables */

    double  g, t, obliq, slong, dj, vl, slp;

    /* Function Body */

    dj    = mjd + 18262.5;
    tmp   = dj * .065709822 + 6.646065;
    *gmst = fmod(tmp, 24.0) * 15.0 / RAD;

    t     = dj / 36525.;
    tmp   = dj * .98564773354 + 279.696678;
    vl    = fmod(tmp, 360.0);

    tmp   = dj * .985600267 + 358.475833;
    g     = fmod(tmp, 360.0) / RAD;

    slong = vl + (1.91946 - t * .004789) * sin(g) + sin(g * 2.) * .020094;
    obliq = (23.45229 - t * .0130125) / RAD;
    slp = (slong - .005686) / RAD;
    t = sin(obliq) * sin(slp);

    sunv[0] = cos(slp);
    sunv[1] = sin(slp) * cos(obliq);
    sunv[2] = t;

    return (0);
}


/*  contribution of tail currents (tsyganenko 87) */
/*  solar-magnetospheric coordinates              */
/*  sint = sinus of dipole tilt angle             */


taill(gsm, bv, sint, cost, kp)
double  *gsm, *bv, sint, cost, kp;
{
    /* Initialized data */

    /********  taill.h  *********/
    static double   xnv[6] = {
        -2.796, -4.184, -3.151, -3.848, -2.948, -3.245                      };
    static double   dv[6] = {
        2.715, 2.641, 3.277, 2.79, 2.99, 3.39                       };
    static double   dyv[6] = {
        13.58, 16.56, 19.19, 20.91, 21.59, 21.8                         };
    static double   rhv[6] = {
        8.038, 7.795, 7.248, 6.193, 6.005, 5.62                         };
    static double   b0v[6] = {
        -6.397, -6.189, -3.696, -.9328, 4.204, 9.231                        };
    static double   b1v[6] = {
        -967., -957.8, -991.1, -872.5, -665.6, -674.3                       };
    static double   b2v[6] = {
        -8650., -7246., -6955., -5851., -1011., -900.                       };
    static double   kp_last = -5.;
    static double   xn, d, dy, rh, b0, b1, b2;
    static double   x1 = 4.;
    static double   x2 = 5.;
    static double   rt = 30.;

    double  sqrt(), atan(), log();

    /* Local variables */

    double  ksnk,  x, y, z;
    int model, tmodel;
    double  d2, g0, g1, g2, p1, p2, s0, s1, s2;
    double  w1, w2, bx, bz,  fy,  sm, sp,  zm, zp;
    double  zr, be2, bm2, bp2, gm1, gm2, gp1, gp2, ks1, ks2;
    double  sm1, sm2, sp1, sp2, ksn, xnk, ks1k, ks2k;
    double  tmp;
    double  fabs(), fmod();

    /* Function Body */

    if (fabs(kp - kp_last) > .05) {
        w2 = fmod(kp, 1.0);
        w1 = 1. - w2;
        model = (int) (kp) + 1;
        if (model > 5) {
            model = 5;
            w1 = 0.;
            w2 = 1.;
        } else if (model < 1) {
            model = 1;
            w1 = w2;
            w2 = 0.;
        }

        tmodel = model - 1;
        xn = xnv[tmodel] * w1 + xnv[model] * w2;
        d  = dv[tmodel] * w1 + dv[model] * w2;
        dy = dyv[tmodel] * w1 + dyv[model] * w2;
        rh = rhv[tmodel] * w1 + rhv[model] * w2;
        b0 = b0v[tmodel] * w1 + b0v[model] * w2;
        b1 = b1v[tmodel] * w1 + b1v[model] * w2;
        b2 = b2v[tmodel] * w1 + b2v[model] * w2;
        kp_last = kp;
    }

    x = gsm[0];
    y = gsm[1];
    z = gsm[2];
    ks1 = x1 - x;
    ks2 = x2 - x;
    ksn = xn - x;
    ks1k = ks1 * ks1;
    ks2k = ks2 * ks2;
    ksnk = ksn * ksn;
    zr = z - rh * sint;
    zp = z - rt;
    zm = z + rt;

    d2 = d * d;
    be2 = zr * zr + d2;
    bp2 = zp * zp + d2;
    bm2 = zm * zm + d2;
    s0 = (PI / 2. + atan(ksn / sqrt(be2))) / sqrt(be2);
    sp = (PI / 2. + atan(ksn / sqrt(bp2))) / sqrt(bp2);
    sm = (PI / 2. + atan(ksn / sqrt(bm2))) / sqrt(bm2);
    g0 = log((ksnk + be2) / sqrt((ksnk + bp2) * (ksnk + bm2))) * 0.5;
    bx = b0 * (zr * s0 - (zp * sp + zm * sm) * 0.5);
    bz = b0 * g0;

    xnk = (xn - x1) * (xn - x1);
    p1 = log(xnk / (ksnk + be2)) / 2. / (ks1k + be2);
    s1 = p1 - ks1 / (ks1k + be2) * s0;
    g1 = be2 * s0 / (ks1k + be2) + ks1 * p1;
    p1 = log(xnk / (ksnk + bp2)) / 2. / (ks1k + bp2);
    sp1 = p1 - ks1 / (ks1k + bp2) * sp;
    gp1 = bp2 * sp / (ks1k + bp2) + ks1 * p1;
    p1 = log(xnk / (ksnk + bm2)) / 2. / (ks1k + bm2);
    sm1 = p1 - ks1 / (ks1k + bm2) * sm;
    gm1 = bm2 * sm / (ks1k + bm2) + ks1 * p1;
    bx += b1 * (zr * s1 - (zp * sp1 + zm * sm1) * .5);
    bz += b1 * (g1 - (gp1 + gm1) * .5);

    xnk = (xn - x2) * (xn - x2);
    tmp = ks2k + be2;
    p2 = log(xnk / (ksnk + be2)) / (tmp * tmp);
    s2 = -ks2 * p2 - 1. / (xn - x2) / tmp + (ks2k - be2) * s0 / (tmp * tmp);
    g2 = (be2 - ks2k) * p2 / 2. - be2 * 2. * ks2 * s0 
         / (tmp * tmp) - ks2 / (xn - x2) / tmp;

    tmp = ks2k + bp2;
    p2 = log(xnk / (ksnk + bp2)) / (tmp * tmp);
    sp2 = -ks2 * p2 - 1. / (xn - x2) / tmp + (ks2k - bp2) * sp / (tmp * tmp);
    gp2 = (bp2 - ks2k) * p2 / 2. - bp2 * 2. * ks2 * sp / (tmp * 
        tmp) - ks2 / (xn - x2) / tmp;

    tmp = ks2k + bm2;
    p2 = log(xnk / (ksnk + bm2)) / (tmp * tmp);
    sm2 = -ks2 * p2 - 1. / (xn - x2) / tmp + (ks2k - bm2) * sm / (tmp * tmp);
    gm2 = (bm2 - ks2k) * p2 / 2. - bm2 * 2. * ks2 * sm / (tmp * 
        tmp) - ks2 / (xn - x2) / tmp;

    bx += b2 * (zr * s2 - (zp * sp2 + zm * sm2) * .5);
    bz += b2 * (g2 - (gp2 + gm2) * .5);

    tmp = y / dy;
    fy = 1. / PI / (tmp * tmp + 1.);
    bv[0] = fy * bx;
    bv[1] = 0.0;
    bv[2] = fy * bz;

    return (0);
}


/* -----------------------------------------------------------------
/*    field-line tracing 
/* input: 
/*    geo(3):   initial position (geo) units of re=6371.2 km 
/*    alt:      altitude of footprint (km) 
/*    where=1   trace to alt in the north 
/*         =0   trace to geomagnetic equator (alt not used)
/*         =-1  trace to alt in the south 
/* output:
/*    foot(3):  final vector re in geo coordinates
/* -----------------------------------------------------------------*/

#define  EPS         0.001
#define  HMIN        0.001
#define  FF          1.01
#define  MAXCOUNT  300
#define  STMIN       0.01

traceigrf(geo, alt, where, foot)
double  *geo, alt;
int where;
double  *foot;
{
    /* Local variables */

    double  fabs();
    extern double   new_sign(), absv();
    register double *d1, *d2, *d3;
    double  hmax, rmin, down, r, s = 0;
    int found = 0;
    double  hnext, rlast;
    int error = 0, stepcount = 0;
    double  rf, bv[3], rv[3];
    double  rvlast[3], gma[3];
    double  hin, dir;
    extern double   dot();

    /* Function Body */

    geo_gma(0, geo, gma, 1);
    r = absv(geo);

    for (d1 = rv, d2 = geo; d1 < rv + 3; )
        *d1++ = *d2++;

    if (where != 0) {
        rmin = alt / RE + 1.;
        dir = (double) where;
        down = 1.;
        if (rmin > r) {
            down = -1.;
            dir = -dir;
        }
        hnext = dir;
        while (found == 0 && stepcount < MAXCOUNT) {
            for (d1 = rvlast, d2 = rv; d1 < rvlast + 3; )
                *d1++ = *d2++;

            rlast = r;
            hin = hnext;
            hmax = fabs(rlast - rmin) * FF;

            if (fabs(hin) > hmax)
                hin = hmax * dir;

            if (fabs(hin) < STMIN)
                hin = dir * STMIN;

            bsstep(rv, 3, &s, hin, EPS, HMIN, &hnext, rhanda, &error);
            r = absv(rv);
            if ((r - rmin) * down < .01)
                found = 1;
            ++stepcount;
        }
        rf = (rmin - r) / (rlast - r);

        for (d1 = foot, d2 = rv, d3 = rvlast; d1 < foot + 3; ++d2)
            *d1++ = *d2 - (*d2 - *d3++) * rf;
    } else /*  trace to equator */    {
        igrf(geo, bv);
        r = dot(geo, bv);
        dir = new_sign(1.0, r);
        hnext = dir;
        while (found == 0 && stepcount < MAXCOUNT) {
            hin = hnext;
            hmax = fabs(gma[2]) * FF;
            if (fabs(hin) > hmax)
                hin = dir * hmax;

            if (fabs(hin) < STMIN)
                hin = dir * STMIN;

            bsstep(rv, 3, &s, hin, EPS, HMIN, &hnext, rhanda, &error);

            geo_gma(0, rv, gma, 1);

            if (dir * gma[2] > -.01)
                found = 1;

            if (absv(rv) > 500.)
                found = 1;

            ++stepcount;
        }

        for (d1 = foot, d2 = rv; d1 < foot + 3; )
            *d1++ = *d2++;
    }

    if (stepcount > 299) {
        printf("stepcount to large in traceigrf\n");
        exit(-1);
    }

    return (0);
}




//////////////////////////////////////////////////////////////////////////////////
//
//.Name      Shue97getR
//
//.Descr     called by tracelin functions
//           code from Java version
//
//.Updated by Serg Redko 
//
//////////////////////////////////////////////////////////////////////////////////

double Shue97getR(double cosTheta, double swp, double bz)
{
   double cc=-1.0/6.6;
   double r, r0, alfa;
      
   alfa=(0.58-0.007*bz)*(1.0+0.024*log(swp));
   r0=(10.22+1.29*tanh(0.184*(bz+8.14)))*pow(swp,cc);
   r = r0 * pow(2.0/(1.0+cosTheta),alfa);
   return r;
}


//////////////////////////////////////////////////////////////////////////////////
//
//.Name      traceline
//
//.Descr     called by Java_ovt_mag_Trace_tracelineJNI
//           derived from tracelin
//
//.Updated by Serg Redko 
//
//////////////////////////////////////////////////////////////////////////////////

/* -----------------------------------------------------------------
      field-line tracing with initial step = step*r
   input: 
      rv(3)   initial position (in icor) 
      alt     altitude of footprint (km)
              if alt=0.0 trace to icor equator
      step ( + ) parallel to b
           (-) antiparallel to b
      is=0 automatic (optimal) step size
      is=1 step size = step*sqrt(r) (enforced)
      xlim    maximum distance in the tail
      mx     maximum size of xx,yy,zz,ss
   output:
      xx(n),yy(n),zz(n) field line coordinates
      ss(n)   path length 
      n       number of points
          bx(n),by(n),bz(n) - field nt bv - use for fast creating fieldline in Java
----------------------------------------------------------------- */
 
traceline(rv, alt, step, is, xlim, xx, yy, zz, ss,  bx,by,bz,  mx, n)
double  rv[3], alt, step;
double *xx, *yy, *zz, *ss,  *bx,*by,*bz;
int is;
double  xlim;
int mx, *n;
{
    double  sqrt();
    double  fabs();


    extern double   absv(), new_sign();
    double  rlast;
    double  hmax, rmin;
    int i, tocentre;
    double  r[3],  b[3] , s, hnext;
    int error;
    double  rf, rr, dz, dsmax;
    double  dir, hin;
  
    double cosTheta, shueR;


    dir = new_sign(1.0, step);
    rlast = absv(rv);
    dz=0.0;
    dsmax=1.0;


    if (alt > 1.00) {
        tocentre = 1;
        rmin = alt / RE + 1.0;
    } else {
        tocentre = 0;
        rmin = 0.0;
    }

    ss[0] = s = 0.;
    xx[0] = r[0] = rv[0];
    yy[0] =  r[1] = rv[1];
    zz[0] =  r[2] = rv[2];

        magbv(r,b);
        bx[0] = b[0];
        by[0] = b[1];
        bz[0] = b[2];

    *n = 1;

    hnext = fabs(step) * dir * sqrt(rlast);
    error = 0;

    for (i = 1; i < mx; i++) {
        if (is == 0)
            hin = hnext;
        else {
            hin = step * sqrt(rlast);
            if (fabs(hin) > fabs(hnext))
                hin = hnext;
        }
            hmax = fabs(rlast - 1.01);
         if(tocentre==0 &&hmax>fabs(r[2])*1.3) hmax= fabs(r[2])*1.3;
            if (hmax > STEPMAX)
                hmax = STEPMAX;

        if (fabs(hin) > hmax)
            hin = dir * hmax;
        if (fabs(hin) < STEPMIN)
            hin = dir * STEPMIN;

        bsstep(r, 3, &s, hin, EPS, HMIN, &hnext, rhandb, &error);


        rf = r[1] * r[1] + r[2] * r[2];
        rr = sqrt(r[0] * r[0] + rf);
        xx[i] = r[0];
        yy[i] = r[1];
        zz[i] = r[2];
        ss[i] = s;

                magbv(r,b);
                bx[i] = b[0];
                by[i] = b[1];
                bz[i] = b[2];


 //       dz=zz[i]-zz[i-1];
        *n = i + 1;


    // check if Clipping is allowed
    if(isMPClipping == 1){  //added by kono
      // check if we are inside "Shue" magnetopause
      cosTheta = r[0]/rr;
      shueR = Shue97getR(cosTheta, Swp, Imf_z);
      if (rr - shueR > 0.5) // we are outside

          return (1);
    }


        if (tocentre==0) {
            if (dir * r[2] > -STEPMIN)
                return (1);
           if (rf > RMAX*RMAX)   //?? rem in Java !!!  <<Serg 
                return(1);
            if (r[0] > 14.)
                                return (1);
            if (error != 0)
                return (1);
            if (r[0] < xlim + STEPMIN)
                return (1);
            if(rr<rlast && r[0]<0.0) return (1);
        } else {
            if (r[0] < xlim)
                error = 1;
            if (rf > RMAX*RMAX)
                error = 1;
            if (error == 1) {
                *n -= 1;
                return (-1);
            }
            if (rr < rmin ) {
                rf = (rmin - rr) / (rlast - rr);
                xx[*n-1] -= (xx[*n-1] - xx[*n-2]) * rf;
                yy[*n-1] -= (yy[*n-1] - yy[*n-2]) * rf;
                zz[*n-1] -= (zz[*n-1] - zz[*n-2]) * rf;

                                r[0] = xx[*n-1];
                                r[1] = yy[*n-1];
                                r[2] = zz[*n-1];
                           
                                magbv(r,b);
                                bx[*n-1] = b[0];
                                by[*n-1] = b[1];
                                bz[*n-1] = b[2];
                return (1);
            }
        }
        rlast = rr;
    }
    printf ("tracelinE: too small mx=%d  init = %.1lf %.1lf %.1lf\n",i,
           rv[0],rv[1],rv[2]);
    printf ("                   end  = %.1lf %.1lf %.1lf  hin=%lf\n",
           r[0],r[1],r[2], hin);
    return (0);
}


// old  tracelin
// tracelin is called by n_foot_ns() <- n_lastline() 
tracelin(rv, alt, step, is, xlim, xx, yy, zz, ss, mx, n)
double  rv[3], alt, step;
double *xx, *yy, *zz, *ss;
int is;
double  xlim;
int mx, *n;
/* returns -1 outside mag 0= not enough steps (mx) 1=ok  */
{
    double  sqrt();
    double  fabs();

    /* Local variables */

    extern double   absv(), new_sign();
    double  rlast;
    double  hmax, rmin;
    int i, tocentre;
    double  r[3], s, hnext;
    int error;
    double  rf, rr, dz, dsmax;
    double  dir, hin;

    double cosTheta, shueR;


    dir = new_sign(1.0, step);
    rlast = absv(rv);
    dz=0.0;
    dsmax=1.0;

    if (alt > 1.00) {
        tocentre = 1;
        rmin = alt / RE + 1.0;
    } else {
        tocentre = 0;
        rmin = 0.0;
    }

    ss[0] = s = 0.;
    xx[0] = r[0] = rv[0];
    yy[0] =  r[1] = rv[1];
    zz[0] =  r[2] = rv[2];
    *n = 1;

    hnext = fabs(step) * dir * sqrt(rlast);
    error = 0;

    for (i = 1; i < mx; i++) {
        if (is == 0)
            hin = hnext;
        else {
            hin = step * sqrt(rlast);
            if (fabs(hin) > fabs(hnext))
                hin = hnext;
        }
            hmax = fabs(rlast - 1.01);
         if(tocentre==0 &&hmax>fabs(r[2])*1.3) hmax= fabs(r[2])*1.3;
            if (hmax > STEPMAX)
                hmax = STEPMAX;

        if (fabs(hin) > hmax)
            hin = dir * hmax;
        if (fabs(hin) < STEPMIN)
            hin = dir * STEPMIN;

        bsstep(r, 3, &s, hin, EPS, HMIN, &hnext, rhandb, &error);
        /*
        printf("i=%d hin=%.3lf r=%.2lf x y,z= %.3lf %.3lf %.3lf\n",i,hin,VABS(r),r[0],r[1],r[2]);
  */

        rf = r[1] * r[1] + r[2] * r[2];
        rr = sqrt(r[0] * r[0] + rf);
        xx[i] = r[0];
        yy[i] = r[1];
        zz[i] = r[2];
        ss[i] = s;
//        dz=zz[i]-zz[i-1];
        *n = i + 1;

    // check if Clipping is allowed
    if(isMPClipping == 1){  //added by kono
      // check if we are inside "Shue" magnetopause
      cosTheta = r[0]/rr;
      shueR = Shue97getR(cosTheta, Swp, Imf_z);
      if (rr - shueR > 0.5) // we are outside

          return (1);
    }

        if (tocentre==0) {
            if (dir * r[2] > -STEPMIN)
                return (1);
            if (rf > RMAX*RMAX)
                return(1);
            if (r[0] > 14.)
                return (1);
            if (error != 0)
                return (1);
            if (r[0] < xlim + STEPMIN)
                return (1);
            if(rr<rlast && r[0]<0.0) return (1);
        } else {
            if (r[0] < xlim)
                error = 1;
            if (rf > RMAX*RMAX)
                error = 1;
            if (error == 1) {
                *n -= 1;
                return (-1);
            }
            if (rr < rmin ) {
                rf = (rmin - rr) / (rlast - rr);
                xx[*n-1] -= (xx[*n-1] - xx[*n-2]) * rf;
                yy[*n-1] -= (yy[*n-1] - yy[*n-2]) * rf;
                zz[*n-1] -= (zz[*n-1] - zz[*n-2]) * rf;
                return (1);
            }
        }
        rlast = rr;
    }
    printf ("tracelin: too small mx=%d  init = %.1lf %.1lf %.1lf\n",i,
           rv[0],rv[1],rv[2]);
    printf ("                   end  = %.1lf %.1lf %.1lf  hin=%lf\n",
           r[0],r[1],r[2], hin);
    return (0);
}

/* This routine is no more used. Remove  - ? */

tsyg95(gsm,psw,dstindex,aeindex,imf,tiltr,bex)
double gsm[3];
double psw; /* dynamic solar wind pressure nPa */
double dstindex,aeindex; /* DST index and AE index */
double imf[3]; /* IMF field vector */
double tiltr;  /* dipole tilt angle radians */
double bex[3]; /* output external field nT */
{
  float pdyn,dst,ae,byimf,bzimf,ps,x,y,z,bx,by,bz;
  pdyn=(float) psw;
  dst= (float) dstindex;
  ae= (float) aeindex;
  byimf=(float) imf[1];
  bzimf=(float) imf[2];
  x= (float) gsm[0]; 
  y= (float) gsm[1]; 
  z= (float) gsm[2]; 
  ps= (float) tiltr;

   /* t95_06(&pdyn,&dst,&ae,&byimf,&bzimf,&ps,&x,&y,&z,&bx,&by,&bz);*/
   printf("ATTENTION!!!! magpac.c: t95_06(..) was removed!!!! edit magpac.c");

    bex[0]=bx;
    bex[1]=by;
    bex[2]=bz;
}


tsyg87(gsm, kp, sint, cost, bex)
double  *gsm, kp, sint, cost, *bex;
{
    double  b1[3], b2[3], b3[3];

    /* Function Body */

    ringl(gsm, b1, sint, cost, kp);
    taill(gsm, b2, sint, cost, kp);
    mapll(gsm, b3, sint, cost, kp);
    bex[0] = b1[0] + b2[0] + b3[0];
    bex[1] = b1[1] + b2[1] + b3[1];
    bex[2] = b1[2] + b2[2] + b3[2];

    return (0);
}


/*  EXTERNAL magnetic model by tsyganenko, pss 37, 5-20, 1989.
    input: 
       gsm(3)     position vector (re)
       kp         (0.1 - 6.0) model number (REAL*8)
       sint       sine of tilt angle
       cost       cosine of the tilt angle
    output:
       bv(3)    in nanotesla */


tsyg89(gsm, kp, sint, cost, bv)
double  *gsm, kp, sint, cost, *bv;
{
    /* Initialized data */


    /**********  tsyg.h  ***********/
    static double   ga1[28] = {
        -98.72, -10014., 15.03, 76.62, -10237., 1.813,
        31.1, -.07464, -.07764, .003303, -1.129, .001663, 9.88e-4, 18.21, -.03018,
        0., 0., 0., 0., 24.74, 8.16, 2.08, -.88, 9.08, 3.84, 13.55, 26.94, 5.75                         };
    static double   ga2[28] = {
        -35.65, -12800., 14.37, 124.5, -13543., 2.316,
        35.64, -.0741, -.1081, .003924, -1.451, .00202, .00111, 21.37, -.04567, 0.,
        0., 0., 0., 22.33, 8.12, 1.664, .932, 9.24, 2.43, 13.81, 28.83, 6.05                        };
    static double   ga3[28] = {
        -77.45, -14588., 64.85, 123.9, -16229., 2.641,
        42.46, -.07611, -.1579, .004078, -1.391, .00153, 7.27e-4, 21.86, -.04199,
        0., 0., 0., 0., 20.9, 6.28, 1.54, 4.18, 9.61, 6.59, 15.08, 30.57, 7.43                      };
    static double   ga4[28] = {
        -70.12, -16125., 90.71, 38.08, -19630., 3.181,
        47.5, -.1327, -.1864, .01328, -1.488, .002962, 8.97e-4, 22.74, -.04095, 0.,
        0., 0., 0., 18.64, 6.27, .935, 5.39, 8.57, 5.94, 15.63, 31.47, 8.1                      };
    static double   ga5[28] = {
        -162.5, -15806., 160.6, 5.888, -27534., 3.607,
        51.1, -.1006, -.1927, .03353, -1.392, .001594, .002439, 22.41, -.04925, 0.,
        0., 0., 0., 18.31, 6.2, .768, 5.07, 10.06, 6.67, 16.1, 30.04, 8.26                      };
    static double   ga6[28] = {
        -128.4, -16184., 149.1, 215.5, -36435., 4.09,
        49.09, -.0231, -.1359, .01989, -2.298, .004911, .003421, 21.79, -.05447,
        0., 0., 0., 0., 19.48, 5.83, .332, 6.47, 10.47, 9.08, 15.85, 25.27, 7.98                        };
    static double   del = .01;
    static double   gam = 4.;
    static double   dyc = 20.;
    static double   xd = 0.;
    static double   xld2 = 40.;
    static double   xlw2 = 170.;
    static double   a02 = 25.;
    static double   rt = 30.;
    static double   sxc = 4.;
    static double   xlwc2 = 50.;
    static double   dadd = 1.;
    static double   kp_last = -5.;

    double  fabs(), fmod();
    double  sqrt(), exp();

    /* Local variables */

    register double *d1, *d2, *d3;
    static double   pa[28], delx, dd, rdyc2, rdy2;
    double  bxcf, bycf, bzcf, ddop, adrt, rx2a2;
    double  bxdr, byrc, bxrc, bzrc, wcsm, wcsp, htps;
    double  work, wtfs, xsxc, dzsx, dzsy, adrt2, xwyw;
    double  srqc2, d, h;
    double  t, w, x, y, z, adt2r2, srx2a2, xxd2l2, sxrc16;
    double  f1, f3, f5, f7, f9, s1, w1, w2, rsprt, y2, z2;
    double  rsmrt, sxsix, xsixt, aa4sps,   at;
    int ip;
    double  fr, ex, wc, fy, sm, hy, rq, sp, zm, zp, wt, zr;
    double  zs, ddopdx,  fk1, fk2, rsqxdl, y410, ro2, xsixtd;
    double  ddr, fdr, ddx, ddy, fcy, fxm, fym, xrc, bxt, byt, bzt;
    double  hsx, xxd, hys, srq, dwx, xsm, dwy, zsm, fxp, fyp, fzp;
    double  fzm, xsx, rqc2,  x2sm;

    /* Function Body */

    x = gsm[0];
    y = gsm[1];
    z = gsm[2];
    if (fabs(kp - kp_last) > .1) {
        ip = (int) (kp) + 1;
        w2 = fmod(kp, 1.0);
        w1 = 1. - w2;
        if (ip > 5) {
            ip = 5;
            w1 = 0.;
            w2 = 1.;
        } else if (ip < 1) {
            ip = 1;
            w1 = w2;
            w2 = 0.;
        }

        switch (ip) {
        case 1:
            for (d1 = pa, d2 = ga1, d3 = ga2; d1 < pa + 28; )
                *d1++ = *d2++ * w1 + *d3++ * w2;
            break;
        case 2:
            for (d1 = pa, d2 = ga2, d3 = ga3; d1 < pa + 28; )
                *d1++ = *d2++ * w1 + *d3++ * w2;
            break;
        case 3:
            for (d1 = pa, d2 = ga3, d3 = ga4; d1 < pa + 28; )
                *d1++ = *d2++ * w1 + *d3++ * w2;
            break;
        case 4:
            for (d1 = pa, d2 = ga4, d3 = ga5; d1 < pa + 28; )
                *d1++ = *d2++ * w1 + *d3++ * w2;
            break;
        case 5:
            for (d1 = pa, d2 = ga5, d3 = ga6; d1 < pa + 28; )
                *d1++ = *d2++ * w1 + *d3++ * w2;
            break;
        }

        delx = pa[19];
        dd = pa[22];
        rdyc2 = 1.0 / (dyc * dyc);
        rdy2 =  1.0 / (pa[26] * pa[26]);

        /* pa(16) - pa(19) are found from pa(6) - pa(13) so that the magnetic */
        /* field is divergenceless */

        pa[15] = (pa[5] / delx + pa[9]) * -.5;
        pa[16] = -(pa[6] / delx + pa[10]);
        pa[17] = -(pa[7] / delx + pa[11] * 3.);
        pa[18] = -(pa[8] / delx + pa[12]) / 3.;
        kp_last = kp;
    }

    htps = sint / (cost * 2.);
    xsm = x * cost - z * sint;
    zsm = z * cost + x * sint;
    x2sm = xsm * xsm;
    y2 = y * y;
    ro2 = x2sm + y2;
    xxd = xsm - xd;
    xxd2l2 = 1.0 / (xxd * xxd + xld2);
    rsqxdl = sqrt(xxd2l2);
    h = (xxd * rsqxdl + 1.) * .5;
    hsx = xld2 * .5 * xxd2l2 * rsqxdl;
    xsixt = xsm + 16.;
    xsixtd = 1.0 / (xsixt * xsixt + 36.);
    sxsix = sqrt(xsixtd);
    ddop = dadd * .5 * (1. - xsixt * sxsix);
    ddopdx = dadd * -18. * xsixtd * sxsix;
    d = pa[21] + del * y2 + gam * h + ddop;
    ddx = gam * hsx + ddopdx;
    ddy = del * 2. * y;

    /* warped current sheet defined by zs=zs(x,y,psi): */

    xrc = xsm + pa[23];
    sxrc16 = sqrt(xrc * xrc + 16.);
    y410 = 1.0 / (y2 * y2 + 1.0e4);
    hy = y2 * y410 * y;
    hys = hy * y410 * 4.0e4;
    hy *= y;
    zs = htps * (xrc - sxrc16) - pa[24] * sint * hy;
    dzsx = htps * (1. - xrc / sxrc16);
    dzsy = -pa[24] * sint * hys;

    /* FUNCTION w(x,y): */

    xsx = xsm - pa[27];
    rq = 1. / (xsx * xsx + xlw2);
    srq = sqrt(rq);
    fy = 1. / (y2 * rdy2 + 1.);
    w = (1. - xsx * srq) * .5 * fy;

    /* d(w)/dx, d(w)/dy */

    dwx = xlw2 * (-0.5) * rq * srq * fy;
    dwy = rdy2 * -2. * w * y * fy;
    zr = zsm - zs;
    t = sqrt(zr * zr + d * d);
    at = pa[25] + t;
    s1 = sqrt(at * at + ro2);
    f5 = 1.0 / s1;
    f7 = 1.0 / (s1 + at);
    f1 = f5 * f7;
    f3 = f5 * f5 * f5;
    f9 = at * f3;
    xwyw = xsm * dwx + y * dwy;
    fr = zr * (xsm * dzsx + y * dzsy);
    wt = w / t;
    wtfs = wt * (fr - d * (xsm * ddx + y * ddy));
    bxt = (pa[0] * f1 + pa[1] * f3) * wt * zr;

    /* field components for tail current: */

    byt = bxt * y;
    bxt *= xsm;
    bzt = pa[0] * (w * f5 + xwyw * f7 + wtfs * f1) + pa[1] * (w * f9 + xwyw * 
        f1 + wtfs * f3);

    /* ring current field: */

    rx2a2 = 1. / (x2sm + a02);
    srx2a2 = sqrt(rx2a2);
    fdr = (xsm * srx2a2 + 1.) * .5;
    ddx = a02 * .5 * rx2a2 * srx2a2;
    ddr = pa[21] + dd * fdr + ddop;
    work = sqrt(zr * zr + ddr * ddr);
    adrt = pa[20] + work;
    adrt2 = adrt * adrt;
    adt2r2 = 1. / (adrt2 + ro2);
    fk1 = adt2r2 * adt2r2 * sqrt(adt2r2);
    fk2 = adrt * 3. * fk1 / work;
    bxdr = pa[4] * zr * fk2;

    /* field components for ring current: */

    byrc = bxdr * y;
    bxrc = bxdr * xsm;
    bzrc = pa[4] * ((adrt2 * 2. - ro2) * fk1 + fk2 * (fr - ddr * (dd * 
        ddx + ddopdx) * xsm));

    /* chapman-ferraro field and */
    /* average field-aligned current contribution */

    ex = exp(x / delx);
    z2 = z * z;
    bxcf = ex * (cost * pa[5] * z + sint * (pa[6] + pa[7] * y2 + pa[8] * z2));
    bycf = ex * (cost * pa[9] * y * z + sint * y * (pa[10] + pa[11] * y2 + 
        pa[12] * z2));
    bzcf = ex * (cost * (pa[13] + pa[14] * y2 + pa[15] * z2) + sint * z * (
        pa[16] + pa[17] * y2 + pa[18] * z2));

    /* magnetotail RETURN current field components */

    fcy = 1. / (y2 * rdyc2 + 1.);
    xsxc = x - sxc;
    rqc2 = 1. / (xsxc * xsxc + xlwc2);
    srqc2 = sqrt(rqc2);
    wc = (1. - xsxc * srqc2) * .5 * fcy;
    work = xlwc2 * -.5 * x * rqc2 * srqc2 * fcy - y * 2. * rdyc2 * wc * y * fcy;
    ro2 = y2 + x * x;
    zp = z + rt;
    zm = z - rt;
    sp = sqrt(zp * zp + ro2);
    sm = sqrt(zm * zm + ro2);
    wcsp = wc / sp;
    wcsm = wc / sm;
    rsprt = 1. / (sp + zp);
    rsmrt = 1. / (sm - zm);
    fxp = wcsp * rsprt;
    fxm = -wcsm * rsmrt;
    fyp = fxp * y;
    fym = fxm * y;
    fxp *= x;
    fxm *= x;
    fzp = wcsp + work * rsprt;
    fzm = wcsm + work * rsmrt;

    /* c5*SIN(psi) */

    aa4sps = pa[3] * sint;

    /* field components for RETURN current: */

    x = pa[2] * (fxp + fxm) + (fxp - fxm) * aa4sps;
    y = pa[2] * (fyp + fym) + (fyp - fym) * aa4sps;
    z = pa[2] * (fzp + fzm) + (fzp - fzm) * aa4sps;

    /* sum of fields, tail current and ring current field */
    /* components are transformed into gsm coordinates: */

    x = x + (bxt + bxrc) * cost + (bzt + bzrc) * sint;
    y = y + byt + byrc;
    z = z + (bzt + bzrc) * cost - (bxt + bxrc) * sint;
    bv[0] = x + bxcf;
    bv[1] = y + bycf;
    bv[2] = z + bzcf;

    return (0);
}



double  Bgradbc(gsm, step)
double  gsm[3], step;
/* returns b x (b.grad) b */
{
    double  ret_val, rv[3], bv[3], bv1[3], bv2[3], dxb[3], dyb[3], dzb[3];
    int n;

    magbv(gsm, bv);
    normf(bv, 1.0);

    for (n = 0; n < 3; n++) {
        rv[0] = gsm[0];
        rv[1] = gsm[1];
        rv[2] = gsm[3];

        rv[n] = gsm[n] + step / 2.0;
        magbv(rv, bv2);
        normf(bv2, 1.0);
        rv[n] = gsm[n] - step / 2.0;
        magbv(rv, bv1);
        normf(bv1, 1.0);

        dxb[n] = (bv2[0] - bv1[0]) / step;
        dyb[n] = (bv2[1] - bv1[1]) / step;
        dzb[n] = (bv2[2] - bv1[2]) / step;
    }

    bv1[0] = bv[0] * dxb[0] + bv[1] * dyb[0] + bv[2] * dzb[0];
    bv1[1] = bv[0] * dxb[1] + bv[1] * dyb[1] + bv[2] * dzb[1];
    bv1[2] = bv[0] * dxb[2] + bv[1] * dyb[2] + bv[2] * dzb[2];

    cross(bv, bv1, bv2);
    ret_val = sqrt(bv2[0] * bv2[0] + bv2[1] * bv2[1] + bv2[2] * bv2[2]);

    return(ret_val);
}


multi_abc(a, b, tb, c, tc)
double  a[9], b[9], c[9]; /* a(i,k)=b(i,j)c(j,k)  */
int tb;    /* tb=1 transpose b before use */
int tc;    /* tc=1 transpose c before use */
{
    register double sum;
    register int    i, j, k, ib, jb, jc, kc, n, nb, nc;

    if (tb != 1)
        tb = 0;
    if (tc != 1)
        tc = 0;

    ib = 3 - 2 * tb;
    jb = 4 - ib;
    jc = 3 - 2 * tc;
    kc = 4 - jc;

    for (i = 0; i < 3; ++i)
        for (k = 0; k < 3; ++k) {
            sum = 0.0;
            n = i * 3 + k;
            for (j = 0; j < 3; ++j) {
                nb = i * ib + j * jb;
                nc = j * jc + k * kc;
                sum += b[nb] * c[nc];
            }
            a[n] = sum;
        }
}


gsm_gse_ar(mjd, coord, dir, npoint, xyz, flag)
double  mjd;
int coord;  /* GSE  or GSEQ or GEI  */
int dir;   /* +1 from gsm to gse(q) -1 reverse */
int npoint;  /* number of data points */
float   xyz[]; /* x,y,z, (move_optional), x,y,z, (move_optional)  */
int flag;   /* =1 if xyz contain move indicator, otherwise 0 */
{
    static double   mjdlast = 0;
    static int  coordlast = -10;

    /* was
    register double gsmgse[9];
    register int    i, k;
    */

    /* now */
    double gsmgse[9];
    int    i, k;


    double  geigsm[9], geigse[9];
    double  dipgei[3];
    double  sin(), cos();
    double  fabs();
    double  fmod();
    double  theta, ct, st, gmst;
    double  x, y, z;

    double gmstime();   
    extern double   dot();
    static double   rotsun[3] = {
        0.122, -0.424, 0.899                    };
    static double   eqlipt[3] = {
        0.0, -0.398, 0.917                  };

    if (coord == GSM)
        return;

    if (fabs(mjd - mjdlast) > 1e-5 || abs(coordlast - coord) > 0) {
        coordlast=coord;
        /*find  dipole axis in GEI */
        sunmjd(mjd, geigsm);
        theta =  gmstime(mjd);
        st = sin(theta);
        ct = cos(theta);
        dipgei[0] = ct * Eccdz[0] - st * Eccdz[1];
        dipgei[1] = st * Eccdz[0] + ct * Eccdz[1];
        dipgei[2] = Eccdz[2];

        /* find GEI to GSM */
        crossn(dipgei, geigsm, geigsm+3);
        crossn(geigsm, geigsm+3, geigsm+6);


        /* find GEI to GSE  */
        geigse[0] = geigsm[0];
        geigse[1] = geigsm[1];
        geigse[2] = geigsm[2];

        if (coord == GSE) {
            crossn(eqlipt, geigse, geigse+3);
            crossn(geigse, geigse+3, geigse+6);
            multi_abc(gsmgse, geigsm, 0, geigse, 1);
        } else if (coord == GSEQ) {
            crossn(rotsun, geigse, geigse+3);
            crossn(geigse, geigse+3, geigse+6);
            multi_abc(gsmgse, geigsm, 0, geigse, 1);
        } else if (coord == GEI) {
            for (i = 0; i < 9; ++i)
                gsmgse[i] = geigsm[i];
        } else {
            printf("gsm_gse_ar:  incorrect coordinates\n");
            return;
        }

    }

    k = (flag == 1) ? 4 : 3;
    for (i = 0; i < npoint; ++i) {
        x = xyz[i*k];
        y = xyz[i*k+1];
        z = xyz[i*k+2];
        if (dir > 0) {
            xyz[i*k] = gsmgse[0] * x + gsmgse[3] * y + gsmgse[6] * z;
            xyz[i*k+1] = gsmgse[1] * x + gsmgse[4] * y + gsmgse[7] * z;
            xyz[i*k+2] = gsmgse[2] * x + gsmgse[5] * y + gsmgse[8] * z;
        } else {
            xyz[i*k] = gsmgse[0] * x + gsmgse[1] * y + gsmgse[2] * z;
            xyz[i*k+1] = gsmgse[3] * x + gsmgse[4] * y + gsmgse[5] * z;
            xyz[i*k+2] = gsmgse[6] * x + gsmgse[7] * y + gsmgse[8] * z;
        }
}

}

/*cdate(ymd, hms, ms, mjd, dir)
int *ymd, *hms, *ms, dir;
double  *mjd;
// converts yymmdd hhmmss ms  to mjd if dir>0
//   converts mjd to yymmdd if dir<0
//   mjd is the day number counted from 1 January 1950 00hh 00mi 00ss.
//   This routine is valid for dates between 1 Jan 1950 and 31 Dec 2099.

{
    int yy, mo, dd, hh, mi, ss;
    int l, n, m, jj, jday;
    double  temp;

    if (dir > 0) {
        yy = (*ymd);
        hh = (*hms);
        dd = yy % 100;
        ss = hh % 100;
        mo = (yy / 100) % 100;
        mi = (hh / 100) % 100;
        yy /= 10000;
        hh /= 10000;
        jj  = (14 - mo) / 12;
        l   = yy - 1900 * (yy / 1900) - jj;
        *mjd = dd - 18234 + (1461 * l) / 4 + (367 * (mo - 2 + jj * 12)) / 12;
        *mjd += ((hh * 3600 + mi * 60 + ss) * 1000  + (*ms) ) / 86400000.0;
    } else {
        temp = (*mjd) + 0.0005 / 86400.0;
        jday = temp;
        l = (4000 * (jday + 18204)) / 1461001;
        n = jday - (1461 * l) / 4 + 18234;
        m = (80 * n) / 2447;
        dd   = n - (2447 * m) / 80;
        jj    = m / 11;
        mo = m + 2 - 12 * jj;
        yy  = 1900 + l + jj;
        temp = (temp - jday) * 24.00;
        hh  = temp;
        temp = (temp - hh) * 60.00;
        mi   = temp;
        temp = (temp - mi) * 60.00;
        ss   = temp;
        temp = (temp - ss) * 1000.00;
        *ms = (int)temp;
        *ymd = yy % 100 * 10000 + mo * 100 + dd;
        *hms = hh * 10000 + mi * 100 + ss;
    }
}
*/

/*
sm_mlat(sm, alt, lat, mlt, idir)
double  sm[3], *alt, *lat, *mlt;
int idir;
// transforms sm[3] (RE) to alt(km) lat(deg) mlt(hours) if idir>0
//   or vice versa if idir<0  
{
    double  r, h;
    if (idir > 0) {
        r = sqrt(sm[0] * sm[0] + sm[1] * sm[1] + sm[2] * sm[2]);
        h = atan2(sm[1], sm[0]) / M_TO_R + 12.0;  // radian to hour  
        *mlt = fmod(h, 24.0);
        *lat = asin(sm[2] / r) / D_TO_R;
        *alt = (r - 1.0) * RE;
    } else {
        r = 1.0 + (*alt) / RE;
        h = cos((*lat) * D_TO_R);
        sm[0] = r * h * cos((*mlt - 12.0) * M_TO_R);
        sm[1] = r * h * sin((*mlt - 12.0) * M_TO_R);
        sm[2] = r * sin((*lat) * D_TO_R);
    }
}

*/


/*****************************************************************/
norm_d(v, norm)
double  v[3], norm;
{
    double  ab;
    ab = sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    v[0] = v[0] * norm / ab;
    v[1] = v[1] * norm / ab;
    v[2] = v[2] * norm / ab;
}



prerror(message)
char    *message;
{
    printf("%s\n", message);
    exit(-1);
}





/*----------- JNI sm_mlat -> ----------------------
Input: 
    sm[3] (RE) 
Output:
   [0] alt(km) 
   [1] lat(deg) 
   [2] mlt(hours) 

-----------------------------------------------------*/
/*
JNIEXPORT jdoubleArray JNICALL 
Java_ovt_mag_MagPack_smtomlat(env, obj, jsm)
JNIEnv *env;
jobject obj;
jdoubleArray jsm;
{
   jdouble *sm = (*env)->GetDoubleArrayElements(env, jsm, 0);
   jdoubleArray jres = (*env)->NewDoubleArray(env, 3);
   jdouble *res = (*env)->GetDoubleArrayElements(env, jres, 0);

   //printf("%f\t%f\t%f\n",geo[0],geo[1],geo[2])

   //sm_mlat(sm, alt, lat, mlt, idir)

   
   sm_mlat( sm, &res[0], &res[1], &res[2], 1);
   

   
   // geo[0]=1;geo[1]=2;geo[2]=3;
   (*env)->ReleaseDoubleArrayElements(env, jsm, sm, 0);
   (*env)->ReleaseDoubleArrayElements(env, jres, res, 0);
   
   return jres;
}


JNIEXPORT jdoubleArray JNICALL 
Java_ovt_mag_MagPack_mlatToSM(env, obj, alt, lat, mlt)
JNIEnv *env;
jobject obj;
jdouble alt, lat, mlt;
{

   jdoubleArray jsm = (*env)->NewDoubleArray(env, 3);
   jdouble *sm = (*env)->GetDoubleArrayElements(env, jsm, 0);

   //sm_mlat(sm, alt, lat, mlt, idir)

   sm_mlat( sm, alt, lat, mlt, -1);
   
   (*env)->ReleaseDoubleArrayElements(env, jsm, sm, 0);
   
   return jsm;
}*/




/*---------------------------------------------------
        JNI to setigtf(mjd)
  output:
                Gh[], Eccrr[], Eccdx[], Eccdy[], Eccdz[]
        
-----------------------------------------------------*/
/*
JNIEXPORT void JNICALL 
Java_ovt_mag_model_IgrfModel_setigrfJNI(env, obj, jyear,
                                        jGh, jEccrr, jEccdx, jEccdy, jEccdz)
JNIEnv *env;
jobject obj;
jint jyear;
jdoubleArray jGh, jEccrr, jEccdx, jEccdy, jEccdz;
{
        jdouble *cGh    = (*env)->GetDoubleArrayElements(env, jGh, 0);
        jdouble *cEccrr = (*env)->GetDoubleArrayElements(env, jEccrr, 0);
        jdouble *cEccdx = (*env)->GetDoubleArrayElements(env, jEccdx, 0);
        jdouble *cEccdy = (*env)->GetDoubleArrayElements(env, jEccdy, 0);
        jdouble *cEccdz = (*env)->GetDoubleArrayElements(env, jEccdz, 0);
        int i;

        setigrf((int)jyear);
        
        for (i=0; i<144; i++) cGh[i] = Gh[i];
        for (i=0; i<3; i++) {
                cEccrr[i] = Eccrr[i];
                cEccdx[i] = Eccdx[i];
                cEccdy[i] = Eccdy[i];
                cEccdz[i] = Eccdz[i];
        }
                
        (*env)->ReleaseDoubleArrayElements(env, jGh, cGh, 0);
        (*env)->ReleaseDoubleArrayElements(env, jEccrr, cEccrr, 0);
        (*env)->ReleaseDoubleArrayElements(env, jEccdx, cEccdx, 0);
        (*env)->ReleaseDoubleArrayElements(env, jEccdy, cEccdy, 0);
        (*env)->ReleaseDoubleArrayElements(env, jEccdz, cEccdz, 0);
}
*/

JNIEXPORT void JNICALL 
Java_ovt_mag_model_Tsyganenko87_tsyganenko87JNI(env, obj, jgsm, kp, sint, cost, jbex)
JNIEnv *env;
jobject obj;
jdoubleArray jgsm, jbex;
jdouble kp, sint, cost;
{
   jdouble *gsm = (*env)->GetDoubleArrayElements(env, jgsm, 0);
   jdouble *bex = (*env)->GetDoubleArrayElements(env, jbex, 0);

   tsyg87(gsm, kp, sint, cost, bex);
   
   (*env)->ReleaseDoubleArrayElements(env, jgsm, gsm, 0);
   (*env)->ReleaseDoubleArrayElements(env, jbex, bex, 0);

}


JNIEXPORT void JNICALL 
Java_ovt_mag_model_Tsyganenko89_tsyganenko89JNI(env, obj, jgsm, kp, sint, cost, jbex)
JNIEnv *env;
jobject obj;
jdoubleArray jgsm, jbex;
jdouble kp, sint, cost;
{
   jdouble *gsm = (*env)->GetDoubleArrayElements(env, jgsm, 0);
   jdouble *bex = (*env)->GetDoubleArrayElements(env, jbex, 0);

   tsyg89(gsm, kp, sint, cost, bex);
   
   (*env)->ReleaseDoubleArrayElements(env, jgsm, gsm, 0);
   (*env)->ReleaseDoubleArrayElements(env, jbex, bex, 0);

}

//////////////////////////////////////////////////////////////////////////////////
//
//.Name      tsyg96
//
//.Descr     external model Tsyganenko 96
//
//.Updated by Serg Redko 
//
//////////////////////////////////////////////////////////////////////////////////

tsyg96(gsm, bv)
double  *gsm,*bv;
{
 
   float GSMf[3],BVf[3]; 
  
   int i;

   for(i=0; i<3; ++i)    /* double2float*/
      GSMf[i]=(float)gsm[i];
        
   tsyganenko96_(GSMf,&TILTf,&SWPf,&DSTf,&BYIMFf,&BZIMFf,BVf);

   for(i=0; i<3; ++i)    /* double2float*/
      bv[i]=(double)BVf[i];
}


/////////////////////////////////////////////////////////////////////////////////
//
//.Name      tsyg2001()
//
//.Descr     external model Tsyganenko 96
//
//////////////////////////////////////////////////////////////////////////////////

tsyg2001(gsm, bv)
double  *gsm,*bv;
{
 
   float GSMf[3],BVf[3]; 
  
   int i;

   for(i=0; i<3; ++i)    /* double2float*/
      GSMf[i]=(float)gsm[i];
        
    tsyganenko2001_(GSMf,&TILTf,&SWPf,&DSTf,&BYIMFf,&BZIMFf,&G1f, &G2f, BVf);

   for(i=0; i<3; ++i)    /* double2float*/
      bv[i]=(double)BVf[i];
}




//////////////////////////////////////////////////////////////////////////////////
//
//.Name      allocGandHcoefs
//
//.Descr     called by initTable() & n_setigrf()
//           Allocate memory for GandHcoefs structure
//           use Nmax global variable for init. GandHcoefs
//
//.Author    Serg Redko 
//
//////////////////////////////////////////////////////////////////////////////////

struct GandHcoefs* allocGandHcoefs(){

     int i,j;

     struct GandHcoefs  *ghCoefs = (struct GandHcoefs *)malloc( sizeof(struct GandHcoefs) );        

     ghCoefs->Gcoefs = (float **)malloc( (Nmax+1)*sizeof(float*) ); 
     ghCoefs->Hcoefs = (float **)malloc( (Nmax+1)*sizeof(float*) ); 

     for(i=0; i<=Nmax; i++){
       ghCoefs->Gcoefs[i] = (float *)malloc( (Nmax+1)*sizeof(float) );      
       ghCoefs->Hcoefs[i] = (float *)malloc( (Nmax+1)*sizeof(float) );      

       for(j=0; j<=Nmax; j++)
         ghCoefs->Gcoefs[i][j] = ghCoefs->Hcoefs[i][j] = 0.0F ;

     }

     return ghCoefs;
}
/////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////
float newRok(double mjd) {
//this
  int year = 0, month = 0, day = 0, hour = 0, mins = 0, sec = 0, msec = 0;

// many not important calculations !!  waste a time

        int jday;
        double  temp;
        int l, m, n, jj;   


        temp = mjd + 5.7870370370370369e-9;

        jday = (int) temp;

        l = (jday + 18204) * 4000 / 1461001;

        n = jday - (l * 1461) / 4 + 18234;

        m = (n * 80) / 2447;

        day = n - (m * 2447) / 80;

        jj = m / 11;

        month = m + 2 - jj * 12;

        year = l + 1900 + jj;

        temp = (temp - jday) * 24.;

        hour = (int) temp;

        temp = (temp - hour) * 60.;

        mins = (int) temp;

        temp = (temp - mins) * 60.;

        sec = (int) temp;

        temp = (temp - sec) * 1000.;

        msec = (int)(temp + .5); 

    return ((float)year+(float)month*0.083333F);
//      return (year+month*0.083333F);

}


//////////////////////////////////////////////////////////////////////////////////
//
//.Name      initTable
//
//.Params    yy - year ; initH = 0 -> first invocation 
//
//.Descr         called by n_setigrf()
//               reads 'g' & 'h' coef. from igrf.d 
//           code from ovt\mag\model\IgrfModel.java
//
//.Author    Serg Redko 
//
//////////////////////////////////////////////////////////////////////////////////

void initTable(int yy, int initH){

   int i,j,neededCol,m_idx=-1,n_idx=-1;
   char ghMarker='\0';
   float flt=0.0F;

   FILE *stream;
   char str[LINELEN];
   char *token;
   char seps[]  = " ,\t\n";
   char tmps[10];

   struct GandHcoefs *ghCoefs = allocGandHcoefs();      



   sprintf(str, "%sigrf.d", Mdirectory);
   if( (stream = fopen( str, "r" )) != 0 ){
                                                              //Getting first Line (header)
        if(fgets( str, LINELEN, stream ) != 0) 
         if(initH == 1){              // First time starting (treats header)
         // Reading header
          i=0;
          token = strtok( str, seps );
          while( token != 0 )
          {
            ++i;          // skiping "g/h n m" fields
            sscanf( token, "%s", tmps );
            switch(i){
               case 4: sscanf( token, "%d", &minY ); break;
            }
                        // Get next token: 
            token = strtok( 0 , seps );
          }
          sscanf( tmps, "%d", &maxY );
          if(minY>=maxY) {
             printf("Invalid format of %sigrf.d (check LINELEN !) ",Mdirectory);
                     fclose( stream );
                         exit(-1);
          }
         } 

 //Checking for corrected year number
/*               if(yy != 0)  // yy = 0 -> init
         if((yy%5)!=0 || yy<minY || yy>maxY) {
             printf("Invalid number of year in IGRF init.");
                     fclose( stream );
                         exit(-1);
                 }
*/
     

 // Reading gh coefs. for year ##yy    
        neededCol=4+(yy-minY)/5;          //Definition of needed column
        while( fgets( str, LINELEN, stream ) != 0){ 
         i=0;                           // Number parsed columns
         token = strtok( str, seps );
         while( token != 0 )                    // Parsing one line
         {
           ++i;
           switch(i){
              case 1:                  // g/h marker
                 ghMarker=token[0];
                 break;
              case 2:                  // getting n index
                 sscanf( token, "%d", &n_idx );
                 break;
              case 3:                  // getting m index
                 sscanf( token, "%d", &m_idx );
                 break;
           }

           if(i==neededCol){           // Founded needed column!
              sscanf( token, "%f", &flt );
              
              if(n_idx>Nmax || m_idx>Nmax || n_idx<0 || m_idx<0){
                 printf("Invalid format of %sigrf.d",Mdirectory);
                 fclose( stream );
                 exit(-1);
              }

             switch(ghMarker){
                 case 'g': ghCoefs->Gcoefs[n_idx][m_idx] = flt; break;
                 case 'h': ghCoefs->Hcoefs[n_idx][m_idx] = flt; break;
                 default: 
                       printf("Invalid format of %sigrf.d",Mdirectory);
                       fclose( stream );
                       exit(-1);
             }
           } // if(i==neededCol)
           sscanf( token, "%s", tmps );
                   // Get next token: 
           token = strtok( 0 , seps );
         } //while( token != 0 )        

         if(isaddCol == 0) {       // addCol isn't loaded
                            // loading addCol
            sscanf( tmps, "%f", &flt );
            switch(ghMarker){
               case 'g': addCol->Gcoefs[n_idx][m_idx] = flt; break;
               case 'h': addCol->Hcoefs[n_idx][m_idx] = flt; break;
                  default: 
                     printf("Invalid File Format !!!");
                     fclose( stream );
                     exit(-1);
            }
         }

         if(i<neededCol){
             printf("Invalid format of %sigrf.d",Mdirectory);
               fclose( stream );
               exit(-1);
         }
        } //while( fgets( str, LINELEN, stream ) != 0)
        fclose( stream );
   } //if( (stream = fopen( DatFile, "r" )) != 0 )
   else{
     printf("Error in %sigrf.d !",Mdirectory);
     exit(-1);
   }
     
   if(isaddCol == 0){
      //create table of "g" and "h" coef different years 
      ghTable = (struct GandHcoefs **)malloc( (maxY-minY+1)*sizeof(struct GandHcoefs*) );  
      for(i=0; i<(maxY-minY+1); i++) ghTable[i] = 0;
      isaddCol = 1;
   }

   // Putting year #yy's GH coefs. into gh table
   if(yy != 0) ghTable[yy-BASEYEAR] = ghCoefs;
}


//////////////////////////////////////////////////////////////////////////////////
//
//.Name      n_setigrf
//
//.Params    year
//
//.Descr     new setigrf() works with new format igrf.d 
//           code from ovt\mag\model\IgrfModel.java
//
//.Author    Serg Redko 
//
//////////////////////////////////////////////////////////////////////////////////

n_setigrf(float yearf){


     struct GandHcoefs   *gANDh , *ghFloor , *ghCeil;

     int i,j,year=(int)yearf,floorY, ceilY;
     float w1a=0.0F,w2a=0.0F,gg,hh;

     double h0,dipmom,w1,w2,lx,ly,lz,tmp1d,tmp2d;
     float sqrt3=1.7320508F;

     float tmp1,tmp2,f,f0;
     int d1,d2,k;

//     printf("C: igrf coef for %f year\n",yearf);

     gANDh   = allocGandHcoefs();    

     if( isaddCol == 0 ){      // Starting for the first time
        addCol  = allocGandHcoefs();
        initTable(0,1);
     }


     if(year < minY) { //prevent irregular year
        year = minY; 
        yearf = (float)minY; 
     }
      
     if(year > maxY) { //prevent irregular year
        year = maxY; 
        yearf = (float)maxY; 
     }

/*   if( ghTable[year-BASEYEAR] != 0)
      gANDh = ghTable[year-BASEYEAR];  // get year's g h  from ghTable
   else{

     if( (year%5) == 0 ) {
         initTable(year,0);
         gANDh = ghTable[ year-BASEYEAR ];
     }
     else {
*/
        floorY=(int)(year/10)*10;
        if((year-floorY)>5)
           floorY+=5;
        ceilY=floorY+5;


        if( ghTable[floorY-BASEYEAR] == 0 ) initTable(floorY,0);     // Requesting FLOOR year
        ghFloor = ghTable[ floorY-BASEYEAR ];


        if(ceilY<=maxY){   // We have not to use additional column
           if( ghTable[ceilY-BASEYEAR] == 0 ) initTable(ceilY,0);   // Requesting CEIL year
               ghCeil = ghTable[ ceilY-BASEYEAR ];
           w1a=((float)ceilY-yearf)/(float)(ceilY-floorY);
           w2a=1.0F-w1a;
        } else {       // Last additional column have be used (after 2000)
           w1a=1.0F;
           w2a=yearf-(float)floorY;
           ghCeil=addCol;    // Using addCol
        }

        for(i=0;i<=Nmax;++i)       //Computing Coefs. Gij & Hij
           for(j=0;j<=i;++j){
              gANDh->Gcoefs[i][j] = w1a*ghFloor->Gcoefs[i][j]+w2a*ghCeil->Gcoefs[i][j];
              gANDh->Hcoefs[i][j] = w1a*ghFloor->Hcoefs[i][j]+w2a*ghCeil->Hcoefs[i][j];
           }
        if( ghTable[year-BASEYEAR] == 0)
          ghTable[year-BASEYEAR] = gANDh;  // Store year in Hashtable
/*     }
   }*/

     //Calculating (recalculating) Gh
//     float tmp1,tmp2,f,f0;
//     int d1,d2,k;
     f0=-1.0F;         // -1.0e-5  for output in gauss
     Gh[0]=0.0F;
     k=2;
     for(i=1;i<=Nmax;++i){
        f0*=0.5*(float)i;
        f=f0/1.4142136F;    //sqrt(2.0)
        d1=i+1;
        d2=1;
        Gh[k-1]=f0*gANDh->Gcoefs[d1-1][d2-1];
        ++k;
        for(j=1;j<=i;++j){
           tmp1=(float)(i+j);
           tmp2=(float)(i-j+1);
           f*=sqrt(tmp1/tmp2);
           d1=i+1;
           d2=j+1;
           Gh[k-1]=f*gANDh->Gcoefs[d1-1][d2-1];
           Gh[k]=f*gANDh->Hcoefs[d1-1][d2-1];
           k+=2;
        }
     }

     //Calculating (recalculating) d?,Eccrr, ...
//     double h0,dipmom,w1,w2,lx,ly,lz,tmp1d,tmp2d;
//     float sqrt3=1.7320508F;
     h0=gANDh->Gcoefs[1][0]*gANDh->Gcoefs[1][0]+
        gANDh->Gcoefs[1][1]*gANDh->Gcoefs[1][1]+
        gANDh->Hcoefs[1][1]*gANDh->Hcoefs[1][1];
     dipmom=-sqrt(h0);
     w1=fabs(gANDh->Gcoefs[1][0]/dipmom);
     w2=sqrt(1.0-w1*w1);
     tmp1d=atan(gANDh->Hcoefs[1][1]/gANDh->Gcoefs[1][1]);
     Eccdz[0]=w2*cos(tmp1d);
     Eccdz[1]=w2*sin(tmp1d);
     Eccdz[2]=w1;
     Eccdx[0]=Eccdx[1]=0.0;
     Eccdx[2]=1.0;


     crossn(Eccdx,Eccdz,Eccdy);
     crossn(Eccdy,Eccdz,Eccdx);

     //Excentric dipole (Chapman & Bartels, 1940)

     lx=-gANDh->Gcoefs[1][1]*gANDh->Gcoefs[2][0]+
      sqrt3*(gANDh->Gcoefs[1][0]*gANDh->Gcoefs[2][1]+
             gANDh->Gcoefs[1][1]*gANDh->Gcoefs[2][2]+
             gANDh->Hcoefs[1][1]*gANDh->Hcoefs[2][2]);
     ly=-gANDh->Hcoefs[1][1]*gANDh->Gcoefs[2][0]+
      sqrt3*(gANDh->Gcoefs[1][0]*gANDh->Hcoefs[2][1]+
             gANDh->Hcoefs[1][1]*gANDh->Gcoefs[2][2]-
             gANDh->Gcoefs[1][1]*gANDh->Hcoefs[2][2]);
     lz=2.0*gANDh->Gcoefs[1][0]*gANDh->Gcoefs[2][0]+
      sqrt3*(gANDh->Gcoefs[1][1]*gANDh->Gcoefs[2][1]+
             gANDh->Hcoefs[1][1]*gANDh->Hcoefs[2][1]);
     tmp2d=0.25*(lz*gANDh->Gcoefs[1][0]+lx*gANDh->Gcoefs[1][1]+
                ly*gANDh->Hcoefs[1][1])/h0;
     Eccrr[0]=(lx-gANDh->Gcoefs[1][1]*tmp2d)/(3.0*h0);
     Eccrr[1]=(ly-gANDh->Hcoefs[1][1]*tmp2d)/(3.0*h0);
     Eccrr[2]=(lz-gANDh->Gcoefs[1][0]*tmp2d)/(3.0*h0);


}

//////////////////////////////////////////////////////////////////////////////////
//
//.Name      Java_ovt_mag_Trace_tracelineJNI
//
//.Descr     called by tracelineJNI() - Java native method in ovt\mag\Trace.java
//
//.Author    Serg Redko 
//
//////////////////////////////////////////////////////////////////////////////////
// mjd time
// rv initial position in gsm
// alt altitude of footprint (if alt = 0 tracing is made to equatorial plane or xlim)
// step '+'- parrallel to b; '-' - antiparrallel to b
// is 0 - automatic (optimal) step size; 1 - step size = step * sqrt(r) (enforced)
// xlim maximum distance in the tail
// mx maximum number of points
// xx,yy,zz - gsm ;  ss - length;   bx,by,bz  -  bv( gsm,mjd ) where gsm_i = {xx[i],yy[i],zz[i]);
// IM - internal(igrf or dipol) EM - external(T87,89,96) model 
// Factor - Model Factor
// isMPClip = 1 if Magn.Paus Clipping is performed else isMPClip = 0;
// n[0] - out: number points in field line
// datTs[] - data array for Tsyganenko's models 87,89,96 years
//         for T87,T89 models: KPIndex, Sint,Cost (t- tilt);
//         for T96 model:      SWP, DSTIndex,Imf_z,Imf_y,TILT
//         if isMPClip = 1 :   SWP,Imf_z for all models
//////////////////////////////////////////////////////////////////////////////////

JNIEXPORT void JNICALL Java_ovt_mag_Trace_tracelineJNI
  (env, cls, mjd, jrv, alt,step,is, xlim, mx, jxx, jyy, jzz, jss, jbx, jby, jbz, IntMod, ExtMod, Factr,isMPClip,jN, jdataTs)
JNIEnv *env;
jclass cls;
jdouble mjd,alt,step,xlim,Factr;
jdoubleArray jrv,jxx, jyy, jzz,jss,jbx,jby,jbz,jdataTs;
jint is,mx,IntMod,ExtMod,isMPClip;
jintArray jN;
{
        jdouble *rv = (*env)->GetDoubleArrayElements(env, jrv, 0);
        jdouble *xx = (*env)->GetDoubleArrayElements(env, jxx, 0);
        jdouble *yy = (*env)->GetDoubleArrayElements(env, jyy, 0);
        jdouble *zz = (*env)->GetDoubleArrayElements(env, jzz, 0);
        jdouble *ss = (*env)->GetDoubleArrayElements(env, jss, 0);
        jdouble *bx = (*env)->GetDoubleArrayElements(env, jbx, 0);
        jdouble *by = (*env)->GetDoubleArrayElements(env, jby, 0);
        jdouble *bz = (*env)->GetDoubleArrayElements(env, jbz, 0);

        jdouble *dataTs = (*env)->GetDoubleArrayElements(env, jdataTs, 0);

        jint *N = (*env)->GetIntArrayElements(env, jN, 0);

        int n = 0;


        isMPClipping = isMPClip;
        IntModel = IntMod;
        ExtModel = ExtMod;
        ModelFactor = Factr;

    if (fabs (mjd - Tigrf) > 30.) {
        n_setigrf(newRok(mjd));
        Tigrf = mjd;
    }

    if (fabs(mjd - Tsgsm) > 1e-5) {
        setgsm(mjd);
        Tsgsm = mjd;
    }

///////////////////////////////////////////////////


        if(isMPClipping == 1){
          Swp   = dataTs[SWP];
          Imf_z = dataTs[IMF_Z];                                                  
        }

    if (ExtModel == T96 || ExtModel == T2001) {

      TILTf  = dataTs[TILT];
      SWPf   = dataTs[SWP];
      DSTf   = dataTs[DSTINDEX];
      BYIMFf = dataTs[IMF_Y];
      BZIMFf = dataTs[IMF_Z];
      if (ExtModel == T2001) {
         G1f = dataTs[G1];
	 G2f = dataTs[G2];
      }
      
    } else {     //ExternalModel == T87 or T89 Model
      KpIndex = dataTs[KPINDEX];
      Sint = dataTs[SINT];
      Cost = dataTs[COST];
    }


    traceline(rv, alt, step, is, xlim, xx, yy, zz, ss, bx,by,bz, mx, &n);

    N[0] = n;

    (*env)->ReleaseDoubleArrayElements(env, jrv, rv, 0);
    (*env)->ReleaseDoubleArrayElements(env, jxx, xx, 0);
    (*env)->ReleaseDoubleArrayElements(env, jyy, yy, 0);
    (*env)->ReleaseDoubleArrayElements(env, jzz, zz, 0);
    (*env)->ReleaseDoubleArrayElements(env, jss, ss, 0);
    (*env)->ReleaseDoubleArrayElements(env, jbx, bx, 0);
    (*env)->ReleaseDoubleArrayElements(env, jby, by, 0);
    (*env)->ReleaseDoubleArrayElements(env, jbz, bz, 0);

    (*env)->ReleaseDoubleArrayElements(env, jdataTs, dataTs, 0);

    (*env)->ReleaseIntArrayElements(env, jN, N, 0);
}



//////////////////////////////////////////////////////////////////////////////////
//
//.Name      Java_ovt_mag_Trace_lastlineJNI
//
//.Descr     called by lastlineJNI() - Java native method in ovt\mag\Trace.java
//
//.Author    Serg Redko 
//
//////////////////////////////////////////////////////////////////////////////////
// mjd time
// rv initial position in gsm
// alt altitude of footprint (if alt = 0 tracing is made to equatorial plane or xlim)
// step '+'- parrallel to b; '-' - antiparrallel to b
// is 0 - automatic (optimal) step size; 1 - step size = step * sqrt(r) (enforced)
// xlim maximum distance in the tail
// mx maximum number of points
// IM - internal(igrf or dipol) EM - external(87,89,96) model 
// Factor - Model Factor
// isMPClip = 1 if Magn.Paus Clipping is performed else isMPClip = 0;
// datTs[] - data array for Tsyganenko's models 87,89,96 years
//         for T87,T89 models: KPIndex, Sint,Cost (t- tilt);
//         for T96 model:      SWP, DSTIndex,Imf_z,Imf_y,TILT
//         if isMPClip = 1 :   SWP,Imf_z for all models
//////////////////////////////////////////////////////////////////////////////////


JNIEXPORT void JNICALL Java_ovt_mag_Trace_lastlineJNI
  (env, cls, mjd, jrv, jdir, xlim, alt, idir, epst, IntMod, ExtMod, Factr, isMPClip, jdataTs)
JNIEnv *env;
jclass cls;
jdouble mjd,alt,xlim,epst,Factr;
jdoubleArray jrv,jdir,jdataTs;
jint idir,isMPClip,IntMod,ExtMod;
{
        jdouble *rv  = (*env)->GetDoubleArrayElements(env, jrv, 0);
        jdouble *dir = (*env)->GetDoubleArrayElements(env, jdir, 0);
        jdouble *dataTs = (*env)->GetDoubleArrayElements(env, jdataTs, 0);

        int n = 0;

        isMPClipping = isMPClip;
        IntModel = IntMod;
        ExtModel = ExtMod;
        ModelFactor = Factr;


    if (fabs (mjd - Tigrf) > 30.) {
        n_setigrf(newRok(mjd));
        Tigrf = mjd;
    }

    if (fabs(mjd - Tsgsm) > 1e-5) {
        setgsm(mjd);
        Tsgsm = mjd;
    }

///////////////////////////////////////////////////


    if(isMPClipping == 1){
      Swp   = dataTs[SWP];
      Imf_z = dataTs[IMF_Z];                                                  
    }

    if(ExtModel == T96){

      TILTf  = dataTs[TILT];
      SWPf   = dataTs[SWP];
      DSTf   = dataTs[DSTINDEX];
      BYIMFf = dataTs[IMF_Y];
      BZIMFf = dataTs[IMF_Z];
    }
    else{     //ExternalModel == T87 or T89 Model
      KpIndex = dataTs[KPINDEX];
          Sint = dataTs[SINT];
          Cost = dataTs[COST];
    }

    n_lastline(mjd, xlim, rv, dir, alt, idir, epst);

    (*env)->ReleaseDoubleArrayElements(env, jrv,  rv,   0);
    (*env)->ReleaseDoubleArrayElements(env, jdir, dir , 0);
    (*env)->ReleaseDoubleArrayElements(env, jdataTs, dataTs, 0);
}

//////////////////////////////////////////////////////////////////////////////////
//
//.Name      Java_ovt_mag_Trace_mdirectoryJNI
//
//.Params    string Ddir - path to file igrf.d 
//
//.Descr     called by mdirectoryJNI() - Java native method in ovt\mag\Trace.java
//           writes to Mdirectory  path to file igrf.d 
//
//.Author    Serg Redko 
//
//////////////////////////////////////////////////////////////////////////////////

JNIEXPORT void JNICALL Java_ovt_mag_Trace_mdirectoryJNI (env, cls, Ddir)
JNIEnv *env;
jclass cls;
jstring Ddir;
{
   const char *ddir=(*env)->GetStringUTFChars(env,Ddir,0);
   sprintf(Mdirectory,"%s",ddir);
   (*env)->ReleaseStringUTFChars(env,Ddir,ddir);
}





