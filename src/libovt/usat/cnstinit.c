/********************************************************/
/* cnstinit.c						*/
/*							*/
/* initialize math constants and physical constants	*/
/********************************************************/

/***** description
 *
 *	$Id: cnstinit.c,v 2.2 2003/09/04 16:02:33 ko Exp $
 *
 */

/***** modification history
 *
 *	$Log: cnstinit.c,v $
 *	Revision 2.2  2003/09/04 16:02:33  ko
 *	dos2unix
 *	
 *	Revision 2.1  2001/06/21 14:20:24  oleh
 *	Release 2.1
 *	
 *	Revision 1.2  2001/04/04 14:42:45  misha
 *	*** empty log message ***
 *	
 * Revision 1.10  1993/10/20  16:46:34  craig
 * updated the orbits
 *
 * Revision 1.9  1993/10/14  21:37:11  craig
 * added the declarations of the variables M and T from oearth.c
 * for NeXT computers.
 *
 * Revision 1.8  1993/05/12  19:12:57  craig
 * updated the orbital elements for the planets.
 *
 * Revision 1.7  1993/04/30  18:04:57  craig
 * Changed the earth's orbital elements to the elements for
 * 13 Jan 93.
 *
 * Added the global variable outfile for use in directing the
 * output.
 *
 * Revision 1.6  1993/04/27  17:00:16  craig
 * added initialization of pcnsts.caupda --- the speed of light
 * in au per day.
 *
 * Revision 1.5  1993/04/22  19:02:29  craig
 * Moved some statics back to aa.c
 *
 * Revision 1.4  1993/04/22  18:31:41  craig
 * Moved the global variables from aa.c to this file.
 *
 * Revision 1.3  1993/04/21  21:00:24  craig
 * added initialization of pcnsts.c and pcnsts.k
 *
 * Revision 1.2  1993/04/21  20:52:11  craig
 * changed path for satellite.h include.  changed ecnsts to
 * pcnsts. added initialization for pcnsts.secpda.
 *
 * Revision 1.1  1993/04/21  15:02:19  craig
 * Initial revision
 *
 *
 */

/***** include files *****/

#include <math.h>
#include "satellite.h"
#include "aaproto.h"

/***** global variables *****/

struct PCONSTANTS pcnsts;
struct MCONSTANTS mcnsts;

double M;		/* Mean anomaly of the earth (and sun) */
double T;		/* centuries from 1900.0 */

double dradt = 0.0;
double ddecdt = 0.0;


int    objnum = 0;			/* I.D. number of object */
double obpolar[3];
double rearth[3];
double eapolar[3];
double JD;
double TDT;
double UT;
int    jdflag = 0;
double dp[3];
int    prtflg = 1;
FILE  *outfile;
/****************/
/* cnstinit	*/
/****************/

void cnstinit (void)
{
    double qo, so, xj2, xj3, xj4;

    /***** initialize constants *****/

    mcnsts.pi = 3.14159265358979323846;
    mcnsts.pio2 = mcnsts.pi / 2.;
    mcnsts.twopi = mcnsts.pi * 2.;
    mcnsts.x3pio2 = mcnsts.pi * 3. / 2.;
    mcnsts.tothrd = 2. / 3.;
    mcnsts.e6a = 1.e-6;
    mcnsts.de2ra = 2. * mcnsts.pi / 360.;
    mcnsts.ra2de = 1. / mcnsts.de2ra;
    mcnsts.ra2sec = 3600. * mcnsts.ra2de;
    mcnsts.sec2ra = 1. / mcnsts.ra2sec;

    qo = 120.0;
    so = 78.0;
    xj2 = .001082616;			/* second grav. zonal harmonic */
    xj3 = -.253881e-5;			/* third grav. zonal harmonic */
    xj4 = -1.65597e-6;			/* fourth grav. zonal harmonic */

    pcnsts.c = 299792458.;		/* speed of light in m/s */
    pcnsts.k = 0.01720209895;		/* gaussian grav. constant */
    pcnsts.ae = 1.;			/* distance units = 1 earth radii */
    pcnsts.kmpau = 1.4959787066e8;	/* km per au */
    pcnsts.xkmper = 6378.135;		/* earth radius in km */
    pcnsts.xmnpda = 1440.;		/* minutes per day */
    pcnsts.secpda = 86400.;		/* seconds per day */
    pcnsts.dapcen = 36525.0;		/* days per julian century */
    pcnsts.J2000 = 2451545.0;		/* julian day for 2000 Jan 1.5 */
    pcnsts.B1950 = 2433282.423;		/* julian day for 1950 Jan 0.923 */
    pcnsts.J1900 = 2415020.0;		/* julian day for 1900 Jan 0.5 */

    pcnsts.caupda = pcnsts.c * pcnsts.secpda / 1000. / pcnsts.kmpau;
    pcnsts.xauper = pcnsts.xkmper / pcnsts.kmpau; /* earth radius in au */
    pcnsts.rapau = 1. / pcnsts.xauper;	/* # earth radii per au */

    pcnsts.ck2 = xj2 * pcnsts.ae * pcnsts.ae / 2.;
    pcnsts.ck4 = (-3. / 8.) * xj4 * pow (pcnsts.ae, 4.);
    pcnsts.xj3 = xj3;
    pcnsts.xke = .0743669161;
    pcnsts.qoms2t = pow (qo - so, 4.) * pow (pcnsts.ae / pcnsts.xkmper, 4.);
    pcnsts.s = pcnsts.ae * ((so / pcnsts.xkmper) + 1.);

    return;
}
