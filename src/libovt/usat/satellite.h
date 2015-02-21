/************************************************/
/* satellite.h					*/
/*						*/
/* definitions for satellite tracking routines	*/
/************************************************/

/***** description
 *
 *	$Id: satellite.h,v 2.4 2003/09/11 10:28:31 ko Exp $
 *
 */

#include <stdio.h>                      /* for FILE */

struct PCONSTANTS
{
    double c;			/* speed of light (m/s)			*/
    double caupda;		/* speed of light (au/day)		*/
    double k;			/* gaussian gravitation constant	*/
    double ck2;			/* xj2 * ae**2 / 2			*/
    double ck4;			/* -3/8 xj4 * ae**4			*/
    double qoms2t;		/* (qo - so)**4 * (ae / xkmper)**4	*/
    double s;			/* ae * ((so / xkmper) + 1)		*/
    double xj3;			/* third gravitational zonal harmonic	*/
    double xke;
    double xkmper;		/* earth radius in km			*/
    double xauper;		/* earth radius in au			*/
    double rapau;		/* earth radii per au			*/
    double xmnpda;		/* minutes per day			*/
    double secpda;		/* seconds per day			*/
    double dapcen;		/* days per julian century		*/
    double J2000;		/* 2000 January 1.5			*/
    double B1950;		/* 1950 January 0.923 Besselian epoch	*/
    double J1900;		/* 1900 January 0, 12h UT		*/
    double ae;			/* distance units in earth radii	*/
    double kmpau;		/* km per au 				*/
};

struct MCONSTANTS
{
    double de2ra;		/* degrees to radians	*/
    double ra2de;		/* radians to degrees	*/
    double ra2sec;		/* radians to arc-sec	*/
    double sec2ra;		/* arc-sec to radians	*/
    double pi;			/* pi			*/
    double pio2;		/* pi / 2		*/
    double twopi;		/* 2 pi			*/
    double x3pio2;		/* 3 pi / 2		*/
    double tothrd;		/* 2 / 3		*/
    double e6a;			/* 1 e -6		*/
};


typedef struct {
    char name[32];
    double xmo;			/* Mean Anomaly [rad] (ko)	*/
    double xnodeo;
    double omegao;
    double eo;			/* Eccentricity with assumed leading decimal (ko) */
 
    double xincl;		/* Inclination [rad] (ko) */
    double xno;			/* Revolutions per Day (Mean Motion) (ko)	*/
    double xndt2o;
    double xndd6o;
    double bstar;
    double x; 			/* coordinates          */ 
    double y;
    double z;
    double xdot;		/* velocity             */
    double ydot;
    double zdot;
    double epoch;
    double ds50;
} ELEMENT;


/***** functions found in deep.c *****/

int    dpinit (double eqsq1, double siniq1, double cosiq1, 
	    double rteqsq1, double ao, double cosq2, double sinomo1,
	    double cosmom1, double bsq1, double xlldot, double omgdt1,
	    double xnodot, double xnodp, ELEMENT element);

int    dpsec (double *xll, double *omgasm, double *xnodes,
	    double *em, double *xinc, double *xn, double tsince, ELEMENT element);

int    dpper (double *em, double *xinc, double *omgasm,
	    double *xnodes, double *xll);

/***** functions found in fmod2p.c *****/

double fmod2p (double angle);

/***** functions found in sdp4.c *****/

int    sdp4 (int *iflag, ELEMENT *element, double tsince);

/***** functions found in sdp8.c *****/

int    sdp8 (int *iflag, double tsince);

/***** functions found in cnstinit.c *****/

void  cnstinit (void);

/***** functions found in sgp4.c *****/

int    sgp4 (int *iflag, ELEMENT *element, double tsince);

/***** functions found in matan2.c *****/

double  matan2 (double y, double x);

/***** functions found in thetag.c *****/

double thetag (double ep, ELEMENT element);

/***** functions found in mjd.c *****/

double mjd (long year, long month, double day);

/***** functions found in rsat.c *****/

int    read_element (FILE *tlefile, ELEMENT *element);

