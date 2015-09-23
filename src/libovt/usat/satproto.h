/********************************************************/
/* satproto.h						*/
/*							*/
/* function prototypes for satellite tracking software	*/
/********************************************************/

/***** description
*
*	$Id: satproto.h,v 1.2 1993/05/18 16:33:59 craig Exp $
*
*/

/***** modification history
*
*	$Log: satproto.h,v $
* Revision 1.2  1993/05/18  16:33:59  craig
* added prototypes from mjd.c rsat.c search.c
*
* Revision 1.1  1993/04/02  18:08:33  craig
* Initial revision
*
*
*/


/***** functions found in deep.c *****/

int    dpinit(double eqsq1, double siniq1, double cosiq1,
	double rteqsq1, double ao, double cosq2, double sinomo1,
	double cosmom1, double bsq1, double xlldot, double omgdt1,
	double xnodot, double xnodp);

int    dpsec(double *xll, double *omgasm, double *xnodes,
	double *em, double *xinc, double *xn, double tsince);

int    dpper(double *em, double *xinc, double *omgasm,
	double *xnodes, double *xll);

/***** functions found in fmod2p.c *****/

double fmod2p(double angle);

/***** functions found in sdp4.c *****/

int    sdp4(int *iflag, double tsince);

/***** functions found in sdp8.c *****/

int    sdp8(int *iflag, double tsince);

/***** functions found in sgp.c *****/

int    sgp(int *iflag, double tsince);

/***** functions found in sgp4.c *****/

int    sgp4(int *iflag, double tsince);

/***** functions found in sgp8.c *****/

int    sgp8(int *iflag, double tsince);

/***** functions found in thetag.c *****/

double thetag(double ep);

/***** functions found in mjd.c *****/

double mjd(int year, int month, double day);

/***** functions found in rsat.c *****/

int    rdelement(void);
int    satreduce(double *alt, double *az, double *ra, double *dec);
void   do_orbit(int iflag, int orbflag);

/***** functions found in search.c *****/

int    search(double jdstart, double jdstop, double jddelta,
	char *satname, double horiz, double sdelta, int opflag);