/* tsyg2001.f -- translated by f2c (version 19970219).
   and processed by Mykola Khotyaintsev's hands.
*/

#include <jni.h>
#include "ovt_mag_model_Tsyganenko2001.h"
#include <math.h>

#define TRUE_ (1)
#define FALSE_ (0)

/* Common Block Declarations */

union {
    struct {
	double dxshift1, dxshift2, d__, deltady;
    } _1;
    struct {
	double dxshift1, dxshift2, d0, deltady;
    } _2;
} tail_;

#define tail_1 (tail_._1)
#define tail_2 (tail_._2)

struct {
    double xkappa1, xkappa2;
} birkpar_;

#define birkpar_1 birkpar_

union {
    struct {
	double sc_sy__, sc_as__, phi;
    } _1;
    struct {
	double sc_sy__, sc_pr__, phi;
    } _2;
} rcpar_;

#define rcpar_1 (rcpar_._1)
#define rcpar_2 (rcpar_._2)

struct {
    double g;
} g_;

#define g_1 g_

struct rh0_1_ {
    double rh0;
};

#define rh0_1 (*(struct rh0_1_ *) &rh0_)

struct {
    double dphi, b, rho_0__, xkappa;
} dphi_b_rho0__;

#define dphi_b_rho0__1 dphi_b_rho0__

struct {
    int m;
} modenum_;

#define modenum_1 modenum_

struct {
    double dtheta;
} dtheta_;

#define dtheta_1 dtheta_

/* Initialized data */

struct {
    double e_1;
    } rh0_ = { 8. };


/* Table of constant values */

static int c__9 = 9;
static int c__1 = 1;
static int c__4 = 4;
static int c__0 = 0;
static int c__43 = 43;
static double c_b18 = .33333333;
static int c__2 = 2;
static double c_b55 = .33333333333333331;
static double c_b66 = 0.;


/*   This is a sample main program, calculating the field from external */
/*   (magnetospheric) sources of the geomagnetic field at a specified */
/*   point of space {XGSM,YGSM,ZGSM}.  The earth's dipole tilt angle */
/*   PS should be specified in radians, the solar wind ram pressure PDYN */
/*   in nanoPascals, Dst index, IMF By and Bz components in nT. The two */
/*  IMF-related indices G1 and G2 take into account the IMF and solar wind*/
/*  conditions during the preceding 1-hour interval; their exact definition*/
/*  is given in the paper "A new data-based model of the near magnetosphere*/
/*   magnetic field.  2. Parameterization and fitting to observations". */
/*  The papers are available online from anonymous ftp-area www-istp.gsfc.nasa
.gov,*/
/*   /pub/kolya/T01. */

/*      DIMENSION PARMOD(10) */
/*     COMMON /GEOPACK/ AAA(15),PS,BBB(21) */

/*      PRINT *, '  ENTER PS,PDYN,DST,BYIMF,BZIMF,G1,G2' */
/*      READ *, PS,PDYN,DST,BYIMF,BZIMF,G1,G2 */
/* 1     PRINT *, '  ENTER X,Y,Z' */
/*      READ *, XGSM,YGSM,ZGSM */

/*      PARMOD(1)=PDYN */
/*      PARMOD(2)=DST */
/*      PARMOD(3)=BYIMF */
/*      PARMOD(4)=BZIMF */
/*      PARMOD(5)=G1 */
/*      PARMOD(6)=G2 */
/*      CALL T01_01 (0,PARMOD,PS,XGSM,YGSM,ZGSM,FXGSM,FYGSM,FZGSM) */
/*      PRINT *, '  FXGSM,FYGSM,FZGSM=',FXGSM,FYGSM,FZGSM */
/*      GOTO 1 */
/*      END */

/*$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
$$$$$$$$$$$$*/








/* Subroutine */ int t01_01__(int *iopt, float *parmod, float *ps, float *x, 
	float *y, float *z__, float *bx, float *by, float *bz)
{
    /* Initialized data */

    static double a[43] = { 1.,2.48341,.58315,.31917,-.08796,-1.17266,
	    3.57478,-.06143,-.01113,.70924,-.01675,-.46056,-.87754,-.03025,
	    .18933,.28089,.16636,-.02932,.02592,-.23537,-.07659,.09117,
	    -.02492,.06816,.55417,.68918,-.04604,2.33521,3.90147,1.28978,
	    .03139,.98751,.21824,41.60182,1.12761,.01376,1.02751,.02969,.1579,
	    8.94335,28.3128,1.24364,.38013 };

    
    double sqrt(double);

    /* Local variables */
    static double bxcf, bycf, bzcf, bxr11, byr11, bzr11, bxr12, byr12, 
	    bzr12, bxr21, byr21, bzr21, bxr22, byr22, bzr22, pdyn, byimf, 
	    bzimf, hximf, hyimf, hzimf, bxprc, byprc, bzprc, bxsrc, bysrc, 
	    bzsrc, g1, g2, xx, yy, zz;
    extern /* Subroutine */ int extall_(int *, int *, int *, 
	    int *, double *, int *, double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *, double *, 
	    double *);
    static double bbx, bby, bbz, pss, dst_ast__, bxt1, byt1, bzt1, bxt2, 
	    byt2, bzt2;


/*     RELEASE DATE OF THIS VERSION:   AUGUST 8, 2001. */

/* -------------------------------------------------------------------- */
/*  A DATA-BASED MODEL OF THE EXTERNAL (I.E., WITHOUT EARTH'S CONTRIBUTION
) PART OF THE*/
/*   MAGNETOSPHERIC MAGNETIC FIELD, CALIBRATED BY */
/*    (1) SOLAR WIND PRESSURE PDYN (NANOPASCALS), */
/*    (2) DST (NANOTESLA), */
/*    (3) BYIMF, */
/*    (4) BZIMF (NANOTESLA) */
/*    (5) G1-INDEX */
/*   (6) G2-INDEX  (SEE TSYGANENKO [2001] FOR AN EXACT DEFINITION OF THESE
 TWO INDICES)*/
/*   THESE INPUT PARAMETERS SHOULD BE PLACED IN THE FIRST 6 ELEMENTS */
/*   OF THE ARRAY PARMOD(10). */

/*  THE REST OF THE INPUT VARIABLES ARE: THE GEODIPOLE TILT ANGLE PS (RADI
ANS),*/
/*     AND   X,Y,Z -  GSM POSITION (RE) */

/*  IOPT  IS JUST A DUMMY INPUT PARAMETER, NECESSARY TO MAKE THIS SUBROUTI
NE*/
/*   COMPATIBLE WITH THE TRACING SOFTWARE PACKAGE (GEOPACK). IN THIS MODEL
 */
/*   IT DOES NOT AFFECT THE OUTPUT FIELD. */

/************************************************************************
*********************/
/*** ATTENTION:  THE MODEL IS BASED ON DATA TAKEN SUNWARD FROM X=-15Re, AN
D HENCE BECOMES   **/
/***              INVALID AT LARGER TAILWARD DISTANCES !!!                
                  **/
/************************************************************************
*********************/

/*  OUTPUT:  GSM COMPONENTS OF THE EXTERNAL MAGNETIC FIELD (BX,BY,BZ, nano
tesla)*/
/*           COMPUTED AS A SUM OF CONTRIBUTIONS FROM PRINCIPAL FIELD SOURC
ES*/

/*  (C) Copr. 2001, Nikolai A. Tsyganenko, USRA, Code 690.2, NASA GSFC */
/*      Greenbelt, MD 20771, USA */

/*                            REFERENCE: */

/*   N. A. Tsyganenko, A new data-based model of the near magnetosphere ma
gnetic field:*/
/*       1. Mathematical structure. */
/*       2. Parameterization and fitting to observations. */

/*             (submitted to JGR, July 2001) */


/* ---------------------------------------------------------------------- 
*/


    /* Parameter adjustments */
    --parmod;

    /* Function Body */

    /* This part was modified by ko to skip do_lio, *_wsle functions */
    if (*x < -20.f) {
	printf("ATTENTION:  THE MODEL IS VALID SUNWARD FROM X=-15 Re ONLY,\n");
	printf("WHILE YOU ARE TRYING TO USE IT AT X=%f\n", *x);
    }

    pdyn = parmod[1];
    dst_ast__ = parmod[2] * .8f - sqrt(pdyn) * 13.f;
    byimf = parmod[3];
    bzimf = parmod[4];

    g1 = parmod[5];
    g2 = parmod[6];
    pss = *ps;
    xx = *x;
    yy = *y;
    zz = *z__;

    extall_(&c__0, &c__0, &c__0, &c__0, a, &c__43, &pdyn, &dst_ast__, &byimf, 
	    &bzimf, &g1, &g2, &pss, &xx, &yy, &zz, &bxcf, &bycf, &bzcf, &bxt1,
	     &byt1, &bzt1, &bxt2, &byt2, &bzt2, &bxsrc, &bysrc, &bzsrc, &
	    bxprc, &byprc, &bzprc, &bxr11, &byr11, &bzr11, &bxr12, &byr12, &
	    bzr12, &bxr21, &byr21, &bzr21, &bxr22, &byr22, &bzr22, &hximf, &
	    hyimf, &hzimf, &bbx, &bby, &bbz);

    *bx = bbx;
    *by = bby;
    *bz = bbz;

    return 0;
} /* t01_01__ */


/* ================================================================ */
/* Subroutine */ int extall_(int *iopgen, int *iopt, int *iopb, 
	int *iopr, double *a, int *ntot, double *pdyn, 
	double *dst, double *byimf, double *bzimf, double *
	vbimf1, double *vbimf2, double *ps, double *x, double 
	*y, double *z__, double *bxcf, double *bycf, double *
	bzcf, double *bxt1, double *byt1, double *bzt1, 
	double *bxt2, double *byt2, double *bzt2, double *
	bxsrc, double *bysrc, double *bzsrc, double *bxprc, 
	double *byprc, double *bzprc, double *bxr11, double *
	byr11, double *bzr11, double *bxr12, double *byr12, 
	double *bzr12, double *bxr21, double *byr21, double *
	bzr21, double *bxr22, double *byr22, double *bzr22, 
	double *hximf, double *hyimf, double *hzimf, double *
	bx, double *by, double *bz)
{
    /* Initialized data */

    static double a0_a__ = 34.586;
    static double a0_s0__ = 1.196;
    static double a0_x0__ = 3.4397;
    static double dsig = .003;
    static double rh2 = -5.2;

    /* System generated locals */
    double d__1, d__2, d__3;

    /* Builtin functions */
    double pow(double, double), sin(double), sqrt(
	    double), atan2(double, double), cos(double), tanh(
	    double);

    /* Local variables */
    static double a_r11__, a_r12__, a_r21__, a_r22__;
    extern /* Subroutine */ int deformed_(int *, double *, double 
	    *, double *, double *, double *, double *, 
	    double *, double *, double *, double *);
    static double znam, fint, fext, xmxm;
    extern /* Subroutine */ int shlcar3x3_dup(double *, double *, 
	    double *, double *, double *, double *, 
	    double *), birk_tot__(int *, double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *);
    static double tamp1, tamp2, r__, a_prc__, a_src__, sigma, theta, 
	    bperp, xappa, oimfx, oimfy, oimfz, xsold, zsold, s0, x0, xappa3, 
	    dd, am, ct, rh, st, qx, qy, qz, ys, zs;
    extern /* Subroutine */ int dipole_dup(double *, double *, 
	    double *, double *, double *, double *, 
	    double *);
    static double xx, yy, zz, bbx, bby, bbz, aro, cfx, cfy, asq, cfz, 
	    factimf, sps;
    extern /* Subroutine */ int full_rc__(int *, double *, double 
	    *, double *, double *, double *, double *, 
	    double *, double *, double *, double *);
    static double xss, sthetah, zss, cospsas, sinpsas, dlp1, dlp2, rho2, 
	    axx0;


/*   IOPGEN - GENERAL OPTION FLAG:  IOPGEN=0 - CALCULATE TOTAL FIELD */
/*                                  IOPGEN=1 - DIPOLE SHIELDING ONLY */
/*                                  IOPGEN=2 - TAIL FIELD ONLY */
/*                                  IOPGEN=3 - BIRKELAND FIELD ONLY */
/*                                  IOPGEN=4 - RING CURRENT FIELD ONLY */
/*                                  IOPGEN=5 - INTERCONNECTION FIELD ONLY 
*/

/*   IOPT -  TAIL FIELD FLAG:       IOPT=0  -  BOTH MODES */
/*                                  IOPT=1  -  MODE 1 ONLY */
/*                                  IOPT=2  -  MODE 2 ONLY */

/*   IOPB -  BIRKELAND FIELD FLAG:  IOPB=0  -  ALL 4 TERMS */
/*                                  IOPB=1  -  REGION 1, MODES 1 AND 2 */
/*                                  IOPB=2  -  REGION 2, MODES 1 AND 2 */

/*   IOPR -  RING CURRENT FLAG:     IOPR=0  -  BOTH SRC AND PRC */
/*                                  IOPR=1  -  SRC ONLY */
/*                                  IOPR=2  -  PRC ONLY */



/* THE COMMON BLOCKS FOR */

    /* Parameter adjustments */
    --a;

    /* Function Body */
/*   SHUE ET A */

    d__1 = *pdyn / 2.f;
    xappa = pow(d__1, a[39]);
/*  NOW THIS IS A VARIABLE PARAMETER */
    rh0_1.rh0 = a[40];
    g_1.g = a[41];
/* Computing 3rd power */
    d__1 = xappa, d__2 = d__1;
    xappa3 = d__2 * (d__1 * d__1);
    xx = *x * xappa;
    yy = *y * xappa;
    zz = *z__ * xappa;

    sps = sin(*ps);

    x0 = a0_x0__ / xappa;
    am = a0_a__ / xappa;
    s0 = a0_s0__;

/* Computing 2nd power */
    d__1 = *byimf;
/* Computing 2nd power */
    d__2 = *bzimf;
    bperp = sqrt(d__1 * d__1 + d__2 * d__2);

/*   CALCULATE THE IMF CLOCK ANGLE: */

    if (*byimf == 0. && *bzimf == 0.) {
	theta = 0.;
    } else {
	theta = atan2(*byimf, *bzimf);
	if (theta <= 0.) {
	    theta += 6.283185307;
	}
    }

    ct = cos(theta);
    st = sin(theta);
    ys = *y * ct - *z__ * st;
    zs = *z__ * ct + *y * st;
/* Computing 2nd power */
    d__1 = sin(theta / 2.f);
    sthetah = d__1 * d__1;

/* CALCULATE "IMF" COMPONENTS OUTSIDE THE MAGNETOPAUSE LAYER (HENCE BEGIN 
WITH "O")*/
/* THEY ARE NEEDED ONLY IF THE POINT (X,Y,Z) IS WITHIN THE TRANSITION MAGN
ETOPAUSE LAYER*/
/*  OR OUTSIDE THE MAGNETOSPHERE: */

    factimf = a[24] + a[25] * sthetah;

    oimfx = 0.;
    oimfy = *byimf * factimf;
    oimfz = *bzimf * factimf;

/* Computing 2nd power */
    d__1 = *x;
/* Computing 2nd power */
    d__2 = *y;
/* Computing 2nd power */
    d__3 = *z__;
    r__ = sqrt(d__1 * d__1 + d__2 * d__2 + d__3 * d__3);
    xss = *x;
    zss = *z__;
L1:
    xsold = xss;
/*   BEGIN ITERATIVE SEARCH OF UNWARPED COORDS (TO F */
    zsold = zss;
/* Computing 2nd power */
    d__1 = zss / r__;
    rh = rh0_1.rh0 + rh2 * (d__1 * d__1);
/* Computing 3rd power */
    d__2 = r__ / rh, d__3 = d__2;
    d__1 = d__3 * (d__2 * d__2) + 1.;
    sinpsas = sps / pow(d__1, c_b18);
/* Computing 2nd power */
    d__1 = sinpsas;
    cospsas = sqrt(1. - d__1 * d__1);
    zss = *x * sinpsas + *z__ * cospsas;
    xss = *x * cospsas - *z__ * sinpsas;
    dd = (d__1 = xss - xsold, abs(d__1)) + (d__2 = zss - zsold, abs(d__2));
    if (dd > 1e-6) {
	goto L1;
    }
/*                                END OF ITERATIVE SEARCH */
/* Computing 2nd power */
    d__1 = *y;
/* Computing 2nd power */
    d__2 = zss;
    rho2 = d__1 * d__1 + d__2 * d__2;
/* Computing 2nd power */
    d__1 = am;
    asq = d__1 * d__1;
    xmxm = am + xss - x0;
    if (xmxm < 0.f) {
	xmxm = 0.f;
    }
/* THE BOUNDARY IS A CYLINDER TAILWARD OF X */
/* Computing 2nd power */
    d__1 = xmxm;
    axx0 = d__1 * d__1;
    aro = asq + rho2;
/* Computing 2nd power */
    d__1 = aro + axx0;
    sigma = sqrt((aro + axx0 + sqrt(d__1 * d__1 - asq * 4.f * axx0)) / (asq * 
	    2.f));

/*   NOW, THERE ARE THREE POSSIBLE CASES: */
/*    (1) INSIDE THE MAGNETOSPHERE   (SIGMA */
/*    (2) IN THE BOUNDARY LAYER */
/*    (3) OUTSIDE THE MAGNETOSPHERE AND B.LAYER */
/*       FIRST OF ALL, CONSIDER THE CASES (1) AND (2): */

/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
++++++++++++++++++++*/
    if (sigma < s0 + dsig) {
/*                             (WITH THE POTENTIAL "PENETRATED" INTERC
ONNECTION FIELD):*/
/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
++++++++++++++++++++++++*/

/*  CASES (1) OR (2); CALCULATE THE MOD */
	if (*iopgen <= 1) {
	    shlcar3x3_dup(&xx, &yy, &zz, ps, &cfx, &cfy, &cfz);
/*  DIPOLE SHIEL */
	    *bxcf = cfx * xappa3;
	    *bycf = cfy * xappa3;
	    *bzcf = cfz * xappa3;
	} else {
	    *bxcf = 0.;
	    *bycf = 0.;
	    *bzcf = 0.;
	}
	if (*iopgen == 0 || *iopgen == 2) {
	    tail_1.dxshift1 = a[26] + a[27] * *vbimf2;
	    tail_1.dxshift2 = 0.;
	    tail_1.d__ = a[28];
	    tail_1.deltady = a[29];
	    deformed_(iopt, ps, &xx, &yy, &zz, bxt1, byt1, bzt1, bxt2, byt2, 
		    bzt2);
/*  TAIL FIELD ( */
	} else {
	    *bxt1 = 0.;
	    *byt1 = 0.;
	    *bzt1 = 0.;
	    *bxt2 = 0.;
	    *byt2 = 0.;
	    *bzt2 = 0.;
	}
	if (*iopgen == 0 || *iopgen == 3) {
	    birkpar_1.xkappa1 = a[35] + a[36] * *vbimf2;
	    birkpar_1.xkappa2 = a[37] + a[38] * *vbimf2;
	    birk_tot__(iopb, ps, &xx, &yy, &zz, bxr11, byr11, bzr11, bxr12, 
		    byr12, bzr12, bxr21, byr21, bzr21, bxr22, byr22, bzr22);
/*   BIRKELAND FIE */
	} else {
	    *bxr11 = 0.;
	    *byr11 = 0.;
	    *bzr11 = 0.;
	    *bxr12 = 0.;
	    *byr12 = 0.;
	    *bzr12 = 0.;
	    *bxr21 = 0.;
	    *byr21 = 0.;
	    *bzr21 = 0.;
	    *bxr22 = 0.;
	    *byr22 = 0.;
	    *bzr22 = 0.;
	}
	if (*iopgen == 0 || *iopgen == 4) {
	    rcpar_1.phi = tanh(abs(*dst) / a[34]) * 1.5707963;
	    znam = abs(*dst);
	    if (znam < 20.) {
		znam = 20.;
	    }
	    d__1 = 20. / znam;
	    rcpar_1.sc_sy__ = a[30] * pow(d__1, a[31]) * xappa;

	    d__1 = 20. / znam;
	    rcpar_1.sc_as__ = a[32] * pow(d__1, a[33]) * xappa;
	    full_rc__(iopr, ps, &xx, &yy, &zz, bxsrc, bysrc, bzsrc, bxprc, 
		    byprc, bzprc);
/*  SHIELDED RING C */
	} else {
	    *bxsrc = 0.;
	    *bysrc = 0.;
	    *bzsrc = 0.;
	    *bxprc = 0.;
	    *byprc = 0.;
	    *bzprc = 0.;
	}

	if (*iopgen == 0 || *iopgen == 5) {
	    *hximf = 0.;
	    *hyimf = *byimf;
	    *hzimf = *bzimf;
/*                       IN OTHER WORDS, THESE ARE DERIVATIVES OF 
THE PENETRATION FIELD COMPONENTS WITH RESPECT*/
/*                       TO THE PENETRATION COEFFICIENT.   WE ASSU
ME THAT ONLY THE TRANSVERSE COMPONENT OF THE*/
/*                        FIELD PENETRATES INSIDE. */
/* THESE ARE COMPONENTS OF THE PENETRATED FIELD PE */
	} else {
	    *hximf = 0.;
	    *hyimf = 0.;
	    *hzimf = 0.;
	}

/* ----------------------------------------------------------- */

/*    NOW, ADD UP ALL THE COMPONENTS: */
	d__1 = *pdyn / 2.;
	dlp1 = pow(d__1, a[42]);
	d__1 = *pdyn / 2.;
	dlp2 = pow(d__1, a[43]);
	tamp1 = a[2] + a[3] * dlp1 + a[4] * *vbimf1 + a[5] * *dst;
	tamp2 = a[6] + a[7] * dlp2 + a[8] * *vbimf1 + a[9] * *dst;
	a_src__ = a[10] + a[11] * *dst + a[12] * sqrt(*pdyn);
	a_prc__ = a[13] + a[14] * *dst + a[15] * sqrt(*pdyn);
	a_r11__ = a[16] + a[17] * *vbimf2;
	a_r12__ = a[18] + a[19] * *vbimf2;
	a_r21__ = a[20] + a[21] * *vbimf2;
	a_r22__ = a[22] + a[23] * *vbimf2;
	bbx = a[1] * *bxcf + tamp1 * *bxt1 + tamp2 * *bxt2 + a_src__ * *bxsrc 
		+ a_prc__ * *bxprc + a_r11__ * *bxr11 + a_r12__ * *bxr12 + 
		a_r21__ * *bxr21 + a_r22__ * *bxr22 + a[24] * *hximf + a[25] *
		 *hximf * sthetah;
	bby = a[1] * *bycf + tamp1 * *byt1 + tamp2 * *byt2 + a_src__ * *bysrc 
		+ a_prc__ * *byprc + a_r11__ * *byr11 + a_r12__ * *byr12 + 
		a_r21__ * *byr21 + a_r22__ * *byr22 + a[24] * *hyimf + a[25] *
		 *hyimf * sthetah;
	bbz = a[1] * *bzcf + tamp1 * *bzt1 + tamp2 * *bzt2 + a_src__ * *bzsrc 
		+ a_prc__ * *bzprc + a_r11__ * *bzr11 + a_r12__ * *bzr12 + 
		a_r21__ * *bzr21 + a_r22__ * *bzr22 + a[24] * *hzimf + a[25] *
		 *hzimf * sthetah;

/*   AND WE HAVE THE TOTAL EXTERNAL FIELD. */

/*  NOW, LET US CHECK WHETHER WE HAVE THE CASE (1). IF YES - WE ARE DO
NE: */

	if (sigma < s0 - dsig) {
/*---------------------------------------------------------------
----------*/
/*  (X,Y,Z) IS INSIDE THE MAGNETOSPHE */
	    *bx = bbx;
	    *by = bby;
	    *bz = bbz;
/*---------------------------------------------------------------
----------*/
	} else {
/*                                             THE INTERPOLATION R
EGION */
/*  THIS IS THE MOST COMPLEX CASE: WE */
	    fint = (1.f - (sigma - s0) / dsig) * .5f;
	    fext = ((sigma - s0) / dsig + 1.f) * .5f;

	    dipole_dup(ps, x, y, z__, &qx, &qy, &qz);
	    *bx = (bbx + qx) * fint + oimfx * fext - qx;
	    *by = (bby + qy) * fint + oimfy * fext - qy;
	    *bz = (bbz + qz) * fint + oimfz * fext - qz;

	}
/*                      POSSIBILITY IS NOW THE CASE (3): */
/*-------------------------------------------------------------------
-------*/
/*   THE CASES (1) AND (2) ARE EXHAUSTED; THE ONLY REMAINI */
    } else {
	dipole_dup(ps, x, y, z__, &qx, &qy, &qz);
	*bx = oimfx - qx;
	*by = oimfy - qy;
	*bz = oimfz - qz;
    }

    return 0;
} /* extall_ */


/* $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ */

/* Subroutine */ int shlcar3x3_dup(double *x, double *y, double *z__,
	 double *ps, double *bx, double *by, double *bz)
{
    /* Initialized data */

    static double a[50] = { -901.2327248,895.8011176,817.6208321,
	    -845.5880889,-83.73539535,86.58542841,336.8781402,-329.3619944,
	    -311.294712,308.6011161,31.94469304,-31.30824526,125.8739681,
	    -372.3384278,-235.4720434,286.7594095,21.86305585,-27.42344605,
	    -150.4874688,2.669338538,1.395023949,-.5540427503,-56.85224007,
	    3.681827033,-43.48705106,5.103131905,1.073551279,-.6673083508,
	    12.21404266,4.177465543,5.799964188,-.3977802319,-1.044652977,
	    .570356001,3.536082962,-3.222069852,9.620648151,6.082014949,
	    27.75216226,12.44199571,5.122226936,6.982039615,20.12149582,
	    6.150973118,4.663639687,15.73319647,2.303504968,5.840511214,
	    .08385953499,.3477844929 };

    /* System generated locals */
    double d__1, d__2;

    /* Builtin functions */
    double cos(double), sin(double), sqrt(double), exp(double)
	    ;

    /* Local variables */
    static double expr, exqs, sqpr, sqqs, a1, a2, a3, a4, a5, a6, a7, a8, 
	    a9, p1, p2, p3, r1, r2, r3, q1, q2, q3, s1, s2, s3, t1, t2, x1, 
	    z1, x2, z2, ct1, ct2, fx1, fx2, fz1, hy1, hx1, hz1, hy2, fz2, hx2,
	     st1, st2, hz2, fx3, hy3, fz3, hx3, hz3, fx4, hy4, fz4, hx4, hz4, 
	    fx5, hy5, fz5, hx5, hz5, fx6, hy6, fz6, hx6, hz6, fx7, hy7, fz7, 
	    hx7, hz7, fx8, hy8, fz8, hx8, hz8, fx9, hy9, fz9, hx9, hz9, cps, 
	    cyp, cyq, czr, czs, sps, syp, syq, szr, szs, s2ps;


/*   THIS S/R RETURNS THE SHIELDING FIELD FOR THE EARTH'S DIPOLE, */
/*   REPRESENTED BY  2x3x3=18 "CARTESIAN" HARMONICS, tilted with respect 
*/
/*   to the z=0 plane  (NB#4, p.74) */

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
/* The 36 coefficients enter in pairs in the amplitudes of the "cartesian"
*/
/*    harmonics (A(1)-A(36). */
/* The 14 nonlinear parameters (A(37)-A(50) are the scales Pi,Ri,Qi,and Si
*/
/*  entering the arguments of exponents, sines, and cosines in each of the
*/
/*  18 "Cartesian" harmonics  PLUS TWO TILT ANGLES FOR THE CARTESIAN HARMO
NICS*/
/*       (ONE FOR THE PSI=0 MODE AND ANOTHER FOR THE PSI=90 MODE) */
/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */



    p1 = a[36];
    p2 = a[37];
    p3 = a[38];
    r1 = a[39];
    r2 = a[40];
    r3 = a[41];
    q1 = a[42];
    q2 = a[43];
    q3 = a[44];
    s1 = a[45];
    s2 = a[46];
    s3 = a[47];
    t1 = a[48];
    t2 = a[49];

    cps = cos(*ps);
    sps = sin(*ps);
    s2ps = cps * 2.;

/*   MODIFIED HERE (SIN(2*PS) INSTEAD OF SIN(3* */
    st1 = sin(*ps * t1);
    ct1 = cos(*ps * t1);
    st2 = sin(*ps * t2);
    ct2 = cos(*ps * t2);
    x1 = *x * ct1 - *z__ * st1;
    z1 = *x * st1 + *z__ * ct1;
    x2 = *x * ct2 - *z__ * st2;
    z2 = *x * st2 + *z__ * ct2;


/*  MAKE THE TERMS IN THE 1ST SUM ("PERPENDICULAR" SYMMETRY): */

/*       I=1: */

/* Computing 2nd power */
    d__1 = p1;
/* Computing 2nd power */
    d__2 = r1;
    sqpr = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyp = cos(*y / p1);
    syp = sin(*y / p1);
    czr = cos(z1 / r1);
    szr = sin(z1 / r1);
    expr = exp(sqpr * x1);
    fx1 = -sqpr * expr * cyp * szr;
    hy1 = expr / p1 * syp * szr;
    fz1 = -expr * cyp / r1 * czr;
    hx1 = fx1 * ct1 + fz1 * st1;
    hz1 = -fx1 * st1 + fz1 * ct1;
/* Computing 2nd power */
    d__1 = p1;
/* Computing 2nd power */
    d__2 = r2;
    sqpr = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyp = cos(*y / p1);
    syp = sin(*y / p1);
    czr = cos(z1 / r2);
    szr = sin(z1 / r2);
    expr = exp(sqpr * x1);
    fx2 = -sqpr * expr * cyp * szr;
    hy2 = expr / p1 * syp * szr;
    fz2 = -expr * cyp / r2 * czr;
    hx2 = fx2 * ct1 + fz2 * st1;
    hz2 = -fx2 * st1 + fz2 * ct1;
/* Computing 2nd power */
    d__1 = p1;
/* Computing 2nd power */
    d__2 = r3;
    sqpr = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyp = cos(*y / p1);
    syp = sin(*y / p1);
    czr = cos(z1 / r3);
    szr = sin(z1 / r3);
    expr = exp(sqpr * x1);
    fx3 = -expr * cyp * (sqpr * z1 * czr + szr / r3 * (x1 + 1. / sqpr));
    hy3 = expr / p1 * syp * (z1 * czr + x1 / r3 * szr / sqpr);
/* Computing 2nd power */
    d__1 = r3;
    fz3 = -expr * cyp * (czr * (x1 / (d__1 * d__1) / sqpr + 1.) - z1 / r3 * 
	    szr);
    hx3 = fx3 * ct1 + fz3 * st1;
    hz3 = -fx3 * st1 + fz3 * ct1;

/*       I=2: */

/* Computing 2nd power */
    d__1 = p2;
/* Computing 2nd power */
    d__2 = r1;
    sqpr = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyp = cos(*y / p2);
    syp = sin(*y / p2);
    czr = cos(z1 / r1);
    szr = sin(z1 / r1);
    expr = exp(sqpr * x1);
    fx4 = -sqpr * expr * cyp * szr;
    hy4 = expr / p2 * syp * szr;
    fz4 = -expr * cyp / r1 * czr;
    hx4 = fx4 * ct1 + fz4 * st1;
    hz4 = -fx4 * st1 + fz4 * ct1;
/* Computing 2nd power */
    d__1 = p2;
/* Computing 2nd power */
    d__2 = r2;
    sqpr = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyp = cos(*y / p2);
    syp = sin(*y / p2);
    czr = cos(z1 / r2);
    szr = sin(z1 / r2);
    expr = exp(sqpr * x1);
    fx5 = -sqpr * expr * cyp * szr;
    hy5 = expr / p2 * syp * szr;
    fz5 = -expr * cyp / r2 * czr;
    hx5 = fx5 * ct1 + fz5 * st1;
    hz5 = -fx5 * st1 + fz5 * ct1;
/* Computing 2nd power */
    d__1 = p2;
/* Computing 2nd power */
    d__2 = r3;
    sqpr = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyp = cos(*y / p2);
    syp = sin(*y / p2);
    czr = cos(z1 / r3);
    szr = sin(z1 / r3);
    expr = exp(sqpr * x1);
    fx6 = -expr * cyp * (sqpr * z1 * czr + szr / r3 * (x1 + 1. / sqpr));
    hy6 = expr / p2 * syp * (z1 * czr + x1 / r3 * szr / sqpr);
/* Computing 2nd power */
    d__1 = r3;
    fz6 = -expr * cyp * (czr * (x1 / (d__1 * d__1) / sqpr + 1.) - z1 / r3 * 
	    szr);
    hx6 = fx6 * ct1 + fz6 * st1;
    hz6 = -fx6 * st1 + fz6 * ct1;

/*      I=3: */

/* Computing 2nd power */
    d__1 = p3;
/* Computing 2nd power */
    d__2 = r1;
    sqpr = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyp = cos(*y / p3);
    syp = sin(*y / p3);
    czr = cos(z1 / r1);
    szr = sin(z1 / r1);
    expr = exp(sqpr * x1);
    fx7 = -sqpr * expr * cyp * szr;
    hy7 = expr / p3 * syp * szr;
    fz7 = -expr * cyp / r1 * czr;
    hx7 = fx7 * ct1 + fz7 * st1;
    hz7 = -fx7 * st1 + fz7 * ct1;
/* Computing 2nd power */
    d__1 = p3;
/* Computing 2nd power */
    d__2 = r2;
    sqpr = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyp = cos(*y / p3);
    syp = sin(*y / p3);
    czr = cos(z1 / r2);
    szr = sin(z1 / r2);
    expr = exp(sqpr * x1);
    fx8 = -sqpr * expr * cyp * szr;
    hy8 = expr / p3 * syp * szr;
    fz8 = -expr * cyp / r2 * czr;
    hx8 = fx8 * ct1 + fz8 * st1;
    hz8 = -fx8 * st1 + fz8 * ct1;
/* Computing 2nd power */
    d__1 = p3;
/* Computing 2nd power */
    d__2 = r3;
    sqpr = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyp = cos(*y / p3);
    syp = sin(*y / p3);
    czr = cos(z1 / r3);
    szr = sin(z1 / r3);
    expr = exp(sqpr * x1);
    fx9 = -expr * cyp * (sqpr * z1 * czr + szr / r3 * (x1 + 1. / sqpr));
    hy9 = expr / p3 * syp * (z1 * czr + x1 / r3 * szr / sqpr);
/* Computing 2nd power */
    d__1 = r3;
    fz9 = -expr * cyp * (czr * (x1 / (d__1 * d__1) / sqpr + 1.) - z1 / r3 * 
	    szr);
    hx9 = fx9 * ct1 + fz9 * st1;
    hz9 = -fx9 * st1 + fz9 * ct1;

    a1 = a[0] + a[1] * cps;
    a2 = a[2] + a[3] * cps;
    a3 = a[4] + a[5] * cps;
    a4 = a[6] + a[7] * cps;
    a5 = a[8] + a[9] * cps;
    a6 = a[10] + a[11] * cps;
    a7 = a[12] + a[13] * cps;
    a8 = a[14] + a[15] * cps;
    a9 = a[16] + a[17] * cps;
    *bx = a1 * hx1 + a2 * hx2 + a3 * hx3 + a4 * hx4 + a5 * hx5 + a6 * hx6 + 
	    a7 * hx7 + a8 * hx8 + a9 * hx9;
    *by = a1 * hy1 + a2 * hy2 + a3 * hy3 + a4 * hy4 + a5 * hy5 + a6 * hy6 + 
	    a7 * hy7 + a8 * hy8 + a9 * hy9;
    *bz = a1 * hz1 + a2 * hz2 + a3 * hz3 + a4 * hz4 + a5 * hz5 + a6 * hz6 + 
	    a7 * hz7 + a8 * hz8 + a9 * hz9;
/*  MAKE THE TERMS IN THE 2ND SUM ("PARALLEL" SYMMETRY): */

/*       I=1 */

/* Computing 2nd power */
    d__1 = q1;
/* Computing 2nd power */
    d__2 = s1;
    sqqs = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyq = cos(*y / q1);
    syq = sin(*y / q1);
    czs = cos(z2 / s1);
    szs = sin(z2 / s1);
    exqs = exp(sqqs * x2);
    fx1 = -sqqs * exqs * cyq * czs * sps;
    hy1 = exqs / q1 * syq * czs * sps;
    fz1 = exqs * cyq / s1 * szs * sps;
    hx1 = fx1 * ct2 + fz1 * st2;
    hz1 = -fx1 * st2 + fz1 * ct2;
/* Computing 2nd power */
    d__1 = q1;
/* Computing 2nd power */
    d__2 = s2;
    sqqs = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyq = cos(*y / q1);
    syq = sin(*y / q1);
    czs = cos(z2 / s2);
    szs = sin(z2 / s2);
    exqs = exp(sqqs * x2);
    fx2 = -sqqs * exqs * cyq * czs * sps;
    hy2 = exqs / q1 * syq * czs * sps;
    fz2 = exqs * cyq / s2 * szs * sps;
    hx2 = fx2 * ct2 + fz2 * st2;
    hz2 = -fx2 * st2 + fz2 * ct2;
/* Computing 2nd power */
    d__1 = q1;
/* Computing 2nd power */
    d__2 = s3;
    sqqs = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyq = cos(*y / q1);
    syq = sin(*y / q1);
    czs = cos(z2 / s3);
    szs = sin(z2 / s3);
    exqs = exp(sqqs * x2);
    fx3 = -sqqs * exqs * cyq * czs * sps;
    hy3 = exqs / q1 * syq * czs * sps;
    fz3 = exqs * cyq / s3 * szs * sps;
    hx3 = fx3 * ct2 + fz3 * st2;
    hz3 = -fx3 * st2 + fz3 * ct2;

/*       I=2: */

/* Computing 2nd power */
    d__1 = q2;
/* Computing 2nd power */
    d__2 = s1;
    sqqs = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyq = cos(*y / q2);
    syq = sin(*y / q2);
    czs = cos(z2 / s1);
    szs = sin(z2 / s1);
    exqs = exp(sqqs * x2);
    fx4 = -sqqs * exqs * cyq * czs * sps;
    hy4 = exqs / q2 * syq * czs * sps;
    fz4 = exqs * cyq / s1 * szs * sps;
    hx4 = fx4 * ct2 + fz4 * st2;
    hz4 = -fx4 * st2 + fz4 * ct2;
/* Computing 2nd power */
    d__1 = q2;
/* Computing 2nd power */
    d__2 = s2;
    sqqs = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyq = cos(*y / q2);
    syq = sin(*y / q2);
    czs = cos(z2 / s2);
    szs = sin(z2 / s2);
    exqs = exp(sqqs * x2);
    fx5 = -sqqs * exqs * cyq * czs * sps;
    hy5 = exqs / q2 * syq * czs * sps;
    fz5 = exqs * cyq / s2 * szs * sps;
    hx5 = fx5 * ct2 + fz5 * st2;
    hz5 = -fx5 * st2 + fz5 * ct2;
/* Computing 2nd power */
    d__1 = q2;
/* Computing 2nd power */
    d__2 = s3;
    sqqs = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyq = cos(*y / q2);
    syq = sin(*y / q2);
    czs = cos(z2 / s3);
    szs = sin(z2 / s3);
    exqs = exp(sqqs * x2);
    fx6 = -sqqs * exqs * cyq * czs * sps;
    hy6 = exqs / q2 * syq * czs * sps;
    fz6 = exqs * cyq / s3 * szs * sps;
    hx6 = fx6 * ct2 + fz6 * st2;
    hz6 = -fx6 * st2 + fz6 * ct2;

/*       I=3: */

/* Computing 2nd power */
    d__1 = q3;
/* Computing 2nd power */
    d__2 = s1;
    sqqs = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyq = cos(*y / q3);
    syq = sin(*y / q3);
    czs = cos(z2 / s1);
    szs = sin(z2 / s1);
    exqs = exp(sqqs * x2);
    fx7 = -sqqs * exqs * cyq * czs * sps;
    hy7 = exqs / q3 * syq * czs * sps;
    fz7 = exqs * cyq / s1 * szs * sps;
    hx7 = fx7 * ct2 + fz7 * st2;
    hz7 = -fx7 * st2 + fz7 * ct2;
/* Computing 2nd power */
    d__1 = q3;
/* Computing 2nd power */
    d__2 = s2;
    sqqs = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyq = cos(*y / q3);
    syq = sin(*y / q3);
    czs = cos(z2 / s2);
    szs = sin(z2 / s2);
    exqs = exp(sqqs * x2);
    fx8 = -sqqs * exqs * cyq * czs * sps;
    hy8 = exqs / q3 * syq * czs * sps;
    fz8 = exqs * cyq / s2 * szs * sps;
    hx8 = fx8 * ct2 + fz8 * st2;
    hz8 = -fx8 * st2 + fz8 * ct2;
/* Computing 2nd power */
    d__1 = q3;
/* Computing 2nd power */
    d__2 = s3;
    sqqs = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
    cyq = cos(*y / q3);
    syq = sin(*y / q3);
    czs = cos(z2 / s3);
    szs = sin(z2 / s3);
    exqs = exp(sqqs * x2);
    fx9 = -sqqs * exqs * cyq * czs * sps;
    hy9 = exqs / q3 * syq * czs * sps;
    fz9 = exqs * cyq / s3 * szs * sps;
    hx9 = fx9 * ct2 + fz9 * st2;
    hz9 = -fx9 * st2 + fz9 * ct2;
    a1 = a[18] + a[19] * s2ps;
    a2 = a[20] + a[21] * s2ps;
    a3 = a[22] + a[23] * s2ps;
    a4 = a[24] + a[25] * s2ps;
    a5 = a[26] + a[27] * s2ps;
    a6 = a[28] + a[29] * s2ps;
    a7 = a[30] + a[31] * s2ps;
    a8 = a[32] + a[33] * s2ps;
    a9 = a[34] + a[35] * s2ps;
    *bx = *bx + a1 * hx1 + a2 * hx2 + a3 * hx3 + a4 * hx4 + a5 * hx5 + a6 * 
	    hx6 + a7 * hx7 + a8 * hx8 + a9 * hx9;
    *by = *by + a1 * hy1 + a2 * hy2 + a3 * hy3 + a4 * hy4 + a5 * hy5 + a6 * 
	    hy6 + a7 * hy7 + a8 * hy8 + a9 * hy9;
    *bz = *bz + a1 * hz1 + a2 * hz2 + a3 * hz3 + a4 * hz4 + a5 * hz5 + a6 * 
	    hz6 + a7 * hz7 + a8 * hz8 + a9 * hz9;

    return 0;
} /* shlcar3x3_dup */


/*############################################################################
*/


/* Subroutine */ int deformed_(int *iopt, double *ps, double *x, 
	double *y, double *z__, double *bx1, double *by1, 
	double *bz1, double *bx2, double *by2, double *bz2)
{
    /* Initialized data */

    static double rh2 = -5.2;
    static int ieps = 3;

    /* System generated locals */
    int i__1, i__2;
    double d__1, d__2, d__3;

    /* Builtin functions */
    double sin(double), sqrt(double), 
	     pow(double , double );

    /* Local variables */
    static double dfdr, bxas1, byas1, bzas1, bxas2, byas2, bzas2, f, r__, 
	    dfdrh, facps, drhdr, cpsas, drhdz, spsas, psasx, psasy, psasz, r2,
	     rh, zr;
    extern /* Subroutine */ int warped_(int *, double *, double *,
	     double *, double *, double *, double *, 
	    double *, double *, double *, double *);
    static double dxasdx, dxasdy, dxasdz, dzasdx, dzasdy, dzasdz, cps, 
	    rrh, xas, zas, sps, fac1, fac2, fac3;


/*  IOPT - TAIL FIELD MODE FLAG:   IOPT=0 - THE TWO TAIL MODES ARE ADDED U
P*/
/*                                  IOPT=1 - MODE 1 ONLY */
/*                                  IOPT=2 - MODE 2 ONLY */

/*   CALCULATES GSM COMPONENTS OF TWO UNIT-AMPLITUDE TAIL FIELD MODES, */
/*    TAKING INTO ACCOUNT BOTH EFFECTS OF DIPOLE TILT: */
/*   WARPING IN Y-Z (DONE BY THE S/R WARPED) AND BENDING IN X-Z (DONE BY T
HIS SUBROUTINE)*/


/* RH0,RH1,RH2, AND IEPS CONTROL THE TILT-RELATED DEFORMATION OF THE TAIL 
FIELD*/

    sps = sin(*ps);
/* Computing 2nd power */
    d__1 = sps;
    cps = sqrt(1. - d__1 * d__1);
/* Computing 2nd power */
    d__1 = *x;
/* Computing 2nd power */
    d__2 = *y;
/* Computing 2nd power */
    d__3 = *z__;
    r2 = d__1 * d__1 + d__2 * d__2 + d__3 * d__3;
    r__ = sqrt(r2);
    zr = *z__ / r__;
/* Computing 2nd power */
    d__1 = zr;
    rh = rh0_1.rh0 + rh2 * (d__1 * d__1);
    drhdr = -zr / r__ * 2. * rh2 * zr;
    drhdz = rh2 * 2. * zr / r__;

    rrh = r__ / rh;
    d__1 = pow(rrh, ieps) + 1.;
    d__2 = 1. / ieps;
    f = 1. / pow(d__1, d__2);
    i__1 = ieps - 1;
    i__2 = ieps + 1;
    dfdr = -pow(rrh, i__1) * pow(f, i__2) / rh;
    dfdrh = -rrh * dfdr;

    spsas = sps * f;
/* Computing 2nd power */
    d__1 = spsas;
    cpsas = sqrt(1. - d__1 * d__1);

    xas = *x * cpsas - *z__ * spsas;
    zas = *x * spsas + *z__ * cpsas;

    facps = sps / cpsas * (dfdr + dfdrh * drhdr) / r__;
    psasx = facps * *x;
    psasy = facps * *y;
    psasz = facps * *z__ + sps / cpsas * dfdrh * drhdz;

    dxasdx = cpsas - zas * psasx;
    dxasdy = -zas * psasy;
    dxasdz = -spsas - zas * psasz;
    dzasdx = spsas + xas * psasx;
    dzasdy = xas * psasy;
    dzasdz = cpsas + xas * psasz;
    fac1 = dxasdz * dzasdy - dxasdy * dzasdz;
    fac2 = dxasdx * dzasdz - dxasdz * dzasdx;
    fac3 = dzasdx * dxasdy - dxasdx * dzasdy;

/*     DEFORM: */

    warped_(iopt, ps, &xas, y, &zas, &bxas1, &byas1, &bzas1, &bxas2, &byas2, &
	    bzas2);

    *bx1 = bxas1 * dzasdz - bzas1 * dxasdz + byas1 * fac1;
    *by1 = byas1 * fac2;
    *bz1 = bzas1 * dxasdx - bxas1 * dzasdx + byas1 * fac3;
    *bx2 = bxas2 * dzasdz - bzas2 * dxasdz + byas2 * fac1;
    *by2 = byas2 * fac2;
    *bz2 = bzas2 * dxasdx - bxas2 * dzasdx + byas2 * fac3;
    return 0;
} /* deformed_ */


/* ------------------------------------------------------------------ */

/* Subroutine */ int warped_(int *iopt, double *ps, double *x, 
	double *y, double *z__, double *bx1, double *by1, 
	double *bz1, double *bx2, double *by2, double *bz2)
{
    /* System generated locals */
    double d__1, d__2, d__3;

    /* Builtin functions */
    double sin(double), sqrt(double), atan2(double, double), 
	    cos(double);

    /* Local variables */
    static double cphi, dfdx, dgdx, sphi, rr4l4;
    extern /* Subroutine */ int unwarped_(int *, double *, double 
	    *, double *, double *, double *, double *, 
	    double *, double *, double *);
    static double f, dxldx, bx_as1__, by_as1__, bz_as1__, bx_as2__, 
	    by_as2__, bz_as2__, cf, sf, dfdphi, xl, bphi_s__, dfdrho, 
	    brho_s__, phi, bphi_as__, rho, yas, zas, brho_as__, sps, rho2;


/*  CALCULATES GSM COMPONENTS OF THE WARPED FIELD FOR TWO TAIL UNIT MODES.
*/
/*   THE WARPING DEFORMATION IS IMPOSED ON THE UNWARPED FIELD, COMPUTED */
/*   BY THE S/R "UNWARPED".  THE WARPING PARAMETER G WAS OBTAINED BY LEAST
 */
/*   SQUARES FITTING TO THE ENTIRE DATASET. */

/*  IOPT - TAIL FIELD MODE FLAG:   IOPT=0 - THE TWO TAIL MODES ARE ADDED U
P*/
/*                                  IOPT=1 - MODE 1 ONLY */
/*                                  IOPT=2 - MODE 2 ONLY */


    dgdx = 0.;
    xl = 20.;
    dxldx = 0.;
    sps = sin(*ps);
/* Computing 2nd power */
    d__1 = *y;
/* Computing 2nd power */
    d__2 = *z__;
    rho2 = d__1 * d__1 + d__2 * d__2;
    rho = sqrt(rho2);
    if (*y == 0. && *z__ == 0.) {
	phi = 0.;
	cphi = 1.;
	sphi = 0.;
    } else {
	phi = atan2(*z__, *y);
	cphi = *y / rho;
	sphi = *z__ / rho;
    }
/* Computing 2nd power */
    d__1 = rho2;
/* Computing 4th power */
    d__2 = xl, d__2 *= d__2;
    rr4l4 = rho / (d__1 * d__1 + d__2 * d__2);
    f = phi + g_1.g * rho2 * rr4l4 * cphi * sps;
    dfdphi = 1. - g_1.g * rho2 * rr4l4 * sphi * sps;
/* Computing 2nd power */
    d__1 = rr4l4;
/* Computing 4th power */
    d__2 = xl, d__2 *= d__2;
/* Computing 2nd power */
    d__3 = rho2;
    dfdrho = g_1.g * (d__1 * d__1) * (d__2 * d__2 * 3. - d__3 * d__3) * cphi *
	     sps;
/* Computing 3rd power */
    d__1 = xl, d__2 = d__1;
    dfdx = rr4l4 * cphi * sps * (dgdx * rho2 - g_1.g * rho * rr4l4 * 4. * (
	    d__2 * (d__1 * d__1)) * dxldx);
    cf = cos(f);
    sf = sin(f);
    yas = rho * cf;
    zas = rho * sf;
    unwarped_(iopt, x, &yas, &zas, &bx_as1__, &by_as1__, &bz_as1__, &bx_as2__,
	     &by_as2__, &bz_as2__);
    brho_as__ = by_as1__ * cf + bz_as1__ * sf;
/*   DEFORM THE 1ST MODE */
    bphi_as__ = -by_as1__ * sf + bz_as1__ * cf;
    brho_s__ = brho_as__ * dfdphi;
    bphi_s__ = bphi_as__ - rho * (bx_as1__ * dfdx + brho_as__ * dfdrho);
    *bx1 = bx_as1__ * dfdphi;
    *by1 = brho_s__ * cphi - bphi_s__ * sphi;
    *bz1 = brho_s__ * sphi + bphi_s__ * cphi;
/*   DONE */
    brho_as__ = by_as2__ * cf + bz_as2__ * sf;
/*   DEFORM THE 2ND MODE */
    bphi_as__ = -by_as2__ * sf + bz_as2__ * cf;
    brho_s__ = brho_as__ * dfdphi;
    bphi_s__ = bphi_as__ - rho * (bx_as2__ * dfdx + brho_as__ * dfdrho);
    *bx2 = bx_as2__ * dfdphi;
    *by2 = brho_s__ * cphi - bphi_s__ * sphi;
    *bz2 = brho_s__ * sphi + bphi_s__ * cphi;
/*   DONE */
    return 0;
} /* warped_ */


/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%*/

/* Subroutine */ int unwarped_(int *iopt, double *x, double *y, 
	double *z__, double *bx1, double *by1, double *bz1, 
	double *bx2, double *by2, double *bz2)
{
    /* Initialized data */

    static double deltadx1 = 1.;
    static double alpha1 = 1.1;
    static double xshift1 = 6.;
    static double deltadx2 = 0.;
    static double alpha2 = .25;
    static double xshift2 = 4.;
    static double a1[60] = { -25.45869857,57.3589908,317.5501869,
	    -2.626756717,-93.38053698,-199.6467926,-858.8129729,34.09192395,
	    845.4214929,-29.07463068,47.10678547,-128.9797943,-781.7512093,
	    6.165038619,167.8905046,492.068041,1654.724031,-46.7733792,
	    -1635.922669,40.86186772,-.1349775602,-.09661991179,-.1662302354,
	    .002810467517,.2487355077,.1025565237,-14.41750229,-.8185333989,
	    11.07693629,.7569503173,-9.655264745,112.2446542,777.5948964,
	    -5.745008536,-83.03921993,-490.2278695,-1155.004209,39.0802332,
	    1172.780574,-39.44349797,-14.07211198,-40.41201127,-313.2277343,
	    2.203920979,8.232835341,197.7065115,391.2733948,-18.57424451,
	    -437.2779053,23.04976898,11.75673963,13.60497313,4.69192706,
	    18.20923547,27.59044809,6.677425469,1.398283308,2.839005878,
	    31.24817706,24.53577264 };
    static double a2[60] = { -287187.1962,4970.499233,410490.1952,
	    -1347.839052,-386370.324,3317.98375,-143462.3895,5706.513767,
	    171176.2904,250.888275,-506570.8891,5733.592632,397975.5842,
	    9771.762168,-941834.2436,7990.97526,54313.10318,447.538806,
	    528046.3449,12751.04453,-21920.98301,-21.05075617,31971.07875,
	    3012.641612,-301822.9103,-3601.107387,1797.577552,-6.315855803,
	    142578.8406,13161.9364,804184.841,-14168.99698,-851926.636,
	    -1890.885671,972475.6869,-8571.862853,26432.49197,-2554.752298,
	    -482308.3431,-4391.473324,105155.916,-1134.62205,-74353.53091,
	    -5382.670711,695055.0788,-916.3365144,-12111.06667,67.20923358,
	    -367200.9285,-21414.14421,14.75567902,20.7563819,59.78601609,
	    16.86431444,32.58482365,23.69472951,17.24977936,13.64902647,
	    68.40989058,11.67828167 };
    static double xm1 = -12.;
    static double xm2 = -12.;

    static double d0sc1, d0sc2;
    extern /* Subroutine */ int taildisk_dup(double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *), shlcar5x5_(double *
	    , double *, double *, double *, double *, 
	    double *, double *, double *);
    static double fx1, fy1, fz1, hx1, hy1, hz1, fx2, fy2, fz2, hx2, hy2, 
	    hz2, xsc1, ysc1, zsc1, xsc2, ysc2, zsc2;

/*  IOPT - TAIL FIELD MODE FLAG:   IOPT=0 - THE TWO TAIL MODES ARE ADDED U
P*/
/*                                  IOPT=1 - MODE 1 ONLY */
/*                                  IOPT=2 - MODE 2 ONLY */

/*   CALCULATES GSM COMPONENTS OF THE SHIELDED FIELD OF TWO TAIL MODES WIT
H UNIT*/
/*   AMPLITUDES,  WITHOUT ANY WARPING OR BENDING.  NONLINEAR PARAMETERS OF
 THE MODES*/
/*    ARE FORWARDED HERE VIA A COMMON BLOCK /TAIL/. */


/*   TAIL SHIELDING FIELD PARAMETERS FOR T */

    if (*iopt == 2) {
	goto L1;
    }
    xsc1 = (*x - xshift1 - tail_2.dxshift1) * alpha1 - xm1 * (alpha1 - 1.);
    ysc1 = *y * alpha1;
    zsc1 = *z__ * alpha1;
    d0sc1 = tail_2.d0 * alpha1;
/* HERE WE USE A SINGLE VALUE D0 OF THE THICKNESS */
    taildisk_dup(&d0sc1, &deltadx1, &tail_2.deltady, &xsc1, &ysc1, &zsc1, &fx1, &
	    fy1, &fz1);
    shlcar5x5_(a1, x, y, z__, &tail_2.dxshift1, &hx1, &hy1, &hz1);
    *bx1 = fx1 + hx1;
    *by1 = fy1 + hy1;
    *bz1 = fz1 + hz1;
    if (*iopt == 1) {
	*bx2 = 0.;
	*by2 = 0.;
	*bz2 = 0.;
	return 0;
    }
L1:
    xsc2 = (*x - xshift2 - tail_2.dxshift2) * alpha2 - xm2 * (alpha2 - 1.);
    ysc2 = *y * alpha2;
    zsc2 = *z__ * alpha2;
    d0sc2 = tail_2.d0 * alpha2;
/* HERE WE USE A SINGLE VALUE D0 OF THE THICKNESS */
    taildisk_dup(&d0sc2, &deltadx2, &tail_2.deltady, &xsc2, &ysc2, &zsc2, &fx2, &
	    fy2, &fz2);
    shlcar5x5_(a2, x, y, z__, &tail_2.dxshift2, &hx2, &hy2, &hz2);
    *bx2 = fx2 + hx2;
    *by2 = fy2 + hy2;
    *bz2 = fz2 + hz2;
    if (*iopt == 2) {
	*bx1 = 0.;
	*by1 = 0.;
	*bz1 = 0.;
	return 0;
    }
    return 0;
} /* unwarped_ */


/* $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ */

/* Subroutine */ int taildisk_dup(double *d0, double *deltadx, 
	double *deltady, double *x, double *y, double *z__, 
	double *bx, double *by, double *bz)
{
    /* Initialized data */

    static double f[5] = { -71.09346626,-1014.308601,-1272.939359,
	    -3224.935936,-44546.86232 };
    static double b[5] = { 10.90101242,12.68393898,13.51791954,
	    14.86775017,15.12306404 };
    static double c__[5] = { .7954069972,.6716601849,1.174866319,
	    2.56524992,10.0198679 };

    /* System generated locals */
    double d__1, d__2;

    /* Builtin functions */
    double sqrt(double), exp(double);

    /* Local variables */
    static double dddx, dddy, s1ps2, s1ts2, ddzetadx, ddzetady, ddzetadz, 
	    ds1dx, ds1dy, ds1dz, ds2dx, ds2dy, ds2dz, d__;
    static int i__;
    static double dasdx, dasdy, dasdz, dzeta, s1, s2, dasds1, dasds2, 
	    ds1ddz, ds2ddz, bi, ci, as, drhodx, drhody, s1ps2sq, ds1drho, 
	    ds2drho, dbx, dby, dbz, dex, rho, fac1;


/*      THIS SUBROUTINE COMPUTES THE COMPONENTS OF THE TAIL CURRENT FIELD,
*/
/*       SIMILAR TO THAT DESCRIBED BY TSYGANENKO AND PEREDO (1994).  THE 
*/
/*       DIFFERENCE IS THAT NOW WE USE SPACEWARPING, AS DESCRIBED IN OUR 
*/
/*       PAPER ON MODELING BIRKELAND CURRENTS (TSYGANENKO AND STERN, 1996)
 */
/*       INSTEAD OF SHEARING IT IN THE SPIRIT OF THE T89 TAIL MODEL. */




/* Computing 2nd power */
    d__1 = *x;
/* Computing 2nd power */
    d__2 = *y;
    rho = sqrt(d__1 * d__1 + d__2 * d__2);
    drhodx = *x / rho;
    drhody = *y / rho;
    dex = exp(*x / 7.);
/* Computing 2nd power */
    d__1 = *y / 20.;
    d__ = *d0 + *deltady * (d__1 * d__1) + *deltadx * dex;
/*   THE LAST TERM (INTRODU */
    dddy = *deltady * *y * .005;
/*   THICKEN SUNWARD, TO AV */
    dddx = *deltadx / 7. * dex;

/* Computing 2nd power */
    d__1 = *z__;
/* Computing 2nd power */
    d__2 = d__;
    dzeta = sqrt(d__1 * d__1 + d__2 * d__2);
/*                                       OUT THE SHEET, AS THAT USED IN T8
9*/
/*  THIS IS THE SAME SIMPLE WAY TO SPREAD */
    ddzetadx = d__ * dddx / dzeta;
    ddzetady = d__ * dddy / dzeta;
    ddzetadz = *z__ / dzeta;

    dbx = 0.;
    dby = 0.;
    dbz = 0.;

    for (i__ = 1; i__ <= 5; ++i__) {

	bi = b[i__ - 1];
	ci = c__[i__ - 1];

/* Computing 2nd power */
	d__1 = rho + bi;
/* Computing 2nd power */
	d__2 = dzeta + ci;
	s1 = sqrt(d__1 * d__1 + d__2 * d__2);
/* Computing 2nd power */
	d__1 = rho - bi;
/* Computing 2nd power */
	d__2 = dzeta + ci;
	s2 = sqrt(d__1 * d__1 + d__2 * d__2);
	ds1drho = (rho + bi) / s1;
	ds2drho = (rho - bi) / s2;
	ds1ddz = (dzeta + ci) / s1;
	ds2ddz = (dzeta + ci) / s2;

	ds1dx = ds1drho * drhodx + ds1ddz * ddzetadx;
	ds1dy = ds1drho * drhody + ds1ddz * ddzetady;
	ds1dz = ds1ddz * ddzetadz;

	ds2dx = ds2drho * drhodx + ds2ddz * ddzetadx;
	ds2dy = ds2drho * drhody + ds2ddz * ddzetady;
	ds2dz = ds2ddz * ddzetadz;

	s1ts2 = s1 * s2;
	s1ps2 = s1 + s2;
/* Computing 2nd power */
	d__1 = s1ps2;
	s1ps2sq = d__1 * d__1;
/* Computing 2nd power */
	d__1 = bi * 2.;
	fac1 = sqrt(s1ps2sq - d__1 * d__1);
	as = fac1 / (s1ts2 * s1ps2sq);
	dasds1 = (1. / (fac1 * s2) - as / s1ps2 * (s2 * s2 + s1 * (s1 * 3. + 
		s2 * 4.))) / (s1 * s1ps2);
	dasds2 = (1. / (fac1 * s1) - as / s1ps2 * (s1 * s1 + s2 * (s2 * 3. + 
		s1 * 4.))) / (s2 * s1ps2);

	dasdx = dasds1 * ds1dx + dasds2 * ds2dx;
	dasdy = dasds1 * ds1dy + dasds2 * ds2dy;
	dasdz = dasds1 * ds1dz + dasds2 * ds2dz;

	dbx -= f[i__ - 1] * *x * dasdz;
	dby -= f[i__ - 1] * *y * dasdz;
/* L1: */
	dbz += f[i__ - 1] * (as * 2. + *x * dasdx + *y * dasdy);
    }
    *bx = dbx;
    *by = dby;
    *bz = dbz;
    return 0;
} /* taildisk_dup */


/* $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ */

/*THIS CODE RETURNS THE SHIELDING FIELD REPRESENTED BY  5x5=25 "CARTESIAN"*/
/*    HARMONICS */

/* Subroutine */ int shlcar5x5_(double *a, double *x, double *y, 
	double *z__, double *dshift, double *hx, double *hy, 
	double *hz)
{
    /* System generated locals */
    double d__1, d__2;

    /* Builtin functions */
    double cos(double), sin(double), sqrt(double), exp(double)
	    ;

    /* Local variables */
    static double coef, cypi, czrk, sypi, sqpr, szrk;
    static int i__, k, l;
    static double rp, rr, dbx, dby, dbz, dhx, dhy, dhz, epr;


/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
/*  The NLIN coefficients are the amplitudes of the "cartesian" */
/*    harmonics (A(1)-A(NLIN). */
/* The NNP nonlinear parameters (A(NLIN+1)-A(NTOT) are the scales Pi and R
i*/
/*  entering the arguments of exponents, sines, and cosines in each of the
*/
/*   NLIN "Cartesian" harmonics */
/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */



    /* Parameter adjustments */
    --a;

    /* Function Body */
    dhx = 0.;
    dhy = 0.;
    dhz = 0.;
    l = 0;

    for (i__ = 1; i__ <= 5; ++i__) {
	rp = 1. / a[i__ + 50];
	cypi = cos(*y * rp);
	sypi = sin(*y * rp);

	for (k = 1; k <= 5; ++k) {
	    rr = 1. / a[k + 55];
	    szrk = sin(*z__ * rr);
	    czrk = cos(*z__ * rr);
/* Computing 2nd power */
	    d__1 = rp;
/* Computing 2nd power */
	    d__2 = rr;
	    sqpr = sqrt(d__1 * d__1 + d__2 * d__2);
	    epr = exp(*x * sqpr);

	    dbx = -sqpr * epr * cypi * szrk;
	    dby = rp * epr * sypi * szrk;
	    dbz = -rr * epr * cypi * czrk;
	    l += 2;
	    coef = a[l - 1] + a[l] * *dshift;
	    dhx += coef * dbx;
	    dhy += coef * dby;
	    dhz += coef * dbz;

/* L2: */
	}
    }
    *hx = dhx;
    *hy = dhy;
    *hz = dhz;

    return 0;
} /* shlcar5x5_ */


/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

/* Subroutine */ int birk_tot__(int *iopb, double *ps, double *x, 
	double *y, double *z__, double *bx11, double *by11, 
	double *bz11, double *bx12, double *by12, double *
	bz12, double *bx21, double *by21, double *bz21, 
	double *bx22, double *by22, double *bz22)
{
    /* Initialized data */

    static double sh11[86] = { 46488.84663,-15541.95244,-23210.09824,
	    -32625.03856,-109894.4551,-71415.32808,58168.94612,55564.87578,
	    -22890.60626,-6056.763968,5091.3681,239.7001538,-13899.49253,
	    4648.016991,6971.310672,9699.351891,32633.34599,21028.48811,
	    -17395.9619,-16461.11037,7447.621471,2528.844345,-1934.094784,
	    -588.3108359,-32588.88216,10894.11453,16238.25044,22925.60557,
	    77251.11274,50375.97787,-40763.78048,-39088.6066,15546.53559,
	    3559.617561,-3187.730438,309.1487975,88.22153914,-243.0721938,
	    -63.63543051,191.1109142,69.94451996,-187.9539415,-49.89923833,
	    104.0902848,-120.2459738,253.5572433,89.25456949,-205.6516252,
	    -44.93654156,124.7026309,32.53005523,-98.85321751,-36.51904756,
	    98.8824169,24.88493459,-55.04058524,61.14493565,-128.4224895,
	    -45.3502346,105.0548704,-43.66748755,119.3284161,31.38442798,
	    -92.87946767,-33.52716686,89.98992001,25.87341323,-48.86305045,
	    59.69362881,-126.5353789,-44.39474251,101.5196856,59.41537992,
	    41.18892281,80.861012,3.066809418,7.893523804,30.56212082,
	    10.36861082,8.222335945,19.97575641,2.050148531,4.992657093,
	    2.300564232,.2256245602,-.05841594319 };
    static double sh12[86] = { 210260.4816,-1443587.401,-1468919.281,
	    281939.2993,-1131124.839,729331.7943,2573541.307,304616.7457,
	    468887.5847,181554.7517,-1300722.65,-257012.8601,645888.8041,
	    -2048126.412,-2529093.041,571093.7972,-2115508.353,1122035.951,
	    4489168.802,75234.22743,823905.6909,147926.6121,-2276322.876,
	    -155528.5992,-858076.2979,3474422.388,3986279.931,-834613.9747,
	    3250625.781,-1818680.377,-7040468.986,-414359.6073,-1295117.666,
	    -346320.6487,3565527.409,430091.9496,-.1565573462,7.377619826,
	    .4115646037,-6.14607888,3.808028815,-.5232034932,1.454841807,
	    -12.32274869,-4.466974237,-2.941184626,-.6172620658,12.6461349,
	    1.494922012,-21.35489898,-1.65225696,16.81799898,-1.404079922,
	    -24.09369677,-10.99900839,45.9423782,2.248579894,31.91234041,
	    7.575026816,-45.80833339,-1.507664976,14.60016998,1.348516288,
	    -11.05980247,-5.402866968,31.69094514,12.28261196,-37.55354174,
	    4.155626879,-33.70159657,-8.437907434,36.22672602,145.0262164,
	    70.73187036,85.51110098,21.47490989,24.34554406,31.34405345,
	    4.655207476,5.747889264,7.802304187,1.844169801,4.86725455,
	    2.941393119,.1379899178,.06607020029 };
    static double sh21[86] = { 162294.6224,503885.1125,-27057.67122,
	    -531450.1339,84747.05678,-237142.1712,84133.6149,259530.0402,
	    69196.0516,-189093.5264,-19278.55134,195724.5034,-263082.6367,
	    -818899.6923,43061.10073,863506.6932,-139707.9428,389984.885,
	    -135167.5555,-426286.9206,-109504.0387,295258.3531,30415.07087,
	    -305502.9405,100785.34,315010.9567,-15999.50673,-332052.2548,
	    54964.34639,-152808.375,51024.67566,166720.0603,40389.67945,
	    -106257.7272,-11126.14442,109876.2047,2.978695024,558.6019011,
	    2.685592939,-338.000473,-81.9972409,-444.1102659,89.44617716,
	    212.0849592,-32.58562625,-982.7336105,-35.10860935,567.8931751,
	    -1.917212423,-260.2023543,-1.023821735,157.5533477,23.00200055,
	    232.0603673,-36.79100036,-111.9110936,18.05429984,447.0481,
	    15.10187415,-258.7297813,-1.032340149,-298.6402478,-1.676201415,
	    180.5856487,64.52313024,209.0160857,-53.8557401,-98.5216429,
	    14.35891214,536.7666279,20.09318806,-309.734953,58.54144539,
	    67.4522685,97.92374406,4.75244976,10.46824379,32.9185611,
	    12.05124381,9.962933904,15.91258637,1.804233877,6.578149088,
	    2.515223491,.1930034238,-.02261109942 };
    static double sh22[86] = { -131287.8986,-631927.6885,-318797.4173,
	    616785.8782,-50027.36189,863099.9833,47680.2024,-1053367.944,
	    -501120.3811,-174400.9476,222328.6873,333551.7374,-389338.7841,
	    -1995527.467,-982971.3024,1960434.268,297239.7137,2676525.168,
	    -147113.4775,-3358059.979,-2106979.191,-462827.1322,1017607.96,
	    1039018.475,520266.9296,2627427.473,1301981.763,-2577171.706,
	    -238071.9956,-3539781.111,94628.1642,4411304.724,2598205.733,
	    637504.9351,-1234794.298,-1372562.403,-2.646186796,-31.10055575,
	    2.295799273,19.20203279,30.01931202,-302.102855,-14.78310655,
	    162.1561899,.4943938056,176.8089129,-.244492168,-100.6148929,
	    9.172262228,137.430344,-8.451613443,-84.20684224,-167.3354083,
	    1321.830393,76.89928813,-705.7586223,18.28186732,-770.1665162,
	    -9.084224422,436.3368157,-6.374255638,-107.2730177,6.080451222,
	    65.53843753,143.2872994,-1028.009017,-64.2273933,547.8536586,
	    -20.58928632,597.3893669,10.17964133,-337.7800252,159.3532209,
	    76.34445954,84.74398828,12.76722651,27.63870691,32.69873634,
	    5.145153451,6.310949163,6.996159733,1.971629939,4.436299219,
	    2.904964304,.1486276863,.06859991529 };

    static double x_sc__;
    extern /* Subroutine */ int birk_shl__(double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *), birk_1n2__(int *, 
	    int *, double *, double *, double *, double *,
	     double *, double *, double *);
    static double fx11, fy11, fz11, hx11, hy11, hz11, fx12, fy12, fz12, 
	    hx12, hy12, hz12, fx21, fy21, fz21, hx21, hy21, hz21, fx22, fy22, 
	    fz22, hx22, hy22, hz22;


/*      IOPB -  BIRKELAND FIELD MODE FLAG: */
/*         IOPB=0 - ALL COMPONENTS */
/*         IOPB=1 - REGION 1, MODES 1 & 2 */
/*         IOPB=2 - REGION 2, MODES 1 & 2 */

/*  INPUT PARAMETERS, SPECIFIED */
/* PARAMETERS, CONTROLLING */

    dphi_b_rho0__1.xkappa = birkpar_1.xkappa1;
/*  FORWARDED IN BIRK_1N2 */
    x_sc__ = birkpar_1.xkappa1 - 1.1;
/*  FORWARDED IN BIRK_SHL */
    if (*iopb == 0 || *iopb == 1) {
	birk_1n2__(&c__1, &c__1, ps, x, y, z__, &fx11, &fy11, &fz11);
/*  REGION 1, */
	birk_shl__(sh11, ps, &x_sc__, x, y, z__, &hx11, &hy11, &hz11);
	*bx11 = fx11 + hx11;
	*by11 = fy11 + hy11;
	*bz11 = fz11 + hz11;
	birk_1n2__(&c__1, &c__2, ps, x, y, z__, &fx12, &fy12, &fz12);
/*  REGION 1, */
	birk_shl__(sh12, ps, &x_sc__, x, y, z__, &hx12, &hy12, &hz12);
	*bx12 = fx12 + hx12;
	*by12 = fy12 + hy12;
	*bz12 = fz12 + hz12;
    }
    dphi_b_rho0__1.xkappa = birkpar_1.xkappa2;
/*  FORWARDED IN BIRK_1N2 */
    x_sc__ = birkpar_1.xkappa2 - 1.;
/*  FORWARDED IN BIRK_SHL */
    if (*iopb == 0 || *iopb == 2) {
	birk_1n2__(&c__2, &c__1, ps, x, y, z__, &fx21, &fy21, &fz21);
/*  REGION 2, */
	birk_shl__(sh21, ps, &x_sc__, x, y, z__, &hx21, &hy21, &hz21);
	*bx21 = fx21 + hx21;
	*by21 = fy21 + hy21;
	*bz21 = fz21 + hz21;
	birk_1n2__(&c__2, &c__2, ps, x, y, z__, &fx22, &fy22, &fz22);
/*  REGION 2, */
	birk_shl__(sh22, ps, &x_sc__, x, y, z__, &hx22, &hy22, &hz22);
	*bx22 = fx22 + hx22;
	*by22 = fy22 + hy22;
	*bz22 = fz22 + hz22;
    }
    return 0;
} /* birk_tot__ */


/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/


/* Subroutine */ int birk_1n2__(int *numb, int *mode, double *ps, 
	double *x, double *y, double *z__, double *bx, 
	double *by, double *bz)
{
    /* Initialized data */

    static double beta = .9;
    static double rh = 10.;
    static double eps = 3.;
    static double a11[31] = { .161806835,-.1797957553,2.999642482,
	    -.9322708978,-.681105976,.2099057262,-8.358815746,-14.8603355,
	    .3838362986,-16.30945494,4.537022847,2.685836007,27.97833029,
	    6.330871059,1.876532361,18.95619213,.96515281,.4217195118,
	    -.0895777002,-1.823555887,.7457045438,-.5785916524,-1.010200918,
	    .01112389357,.09572927448,-.3599292276,8.713700514,.9763932955,
	    3.834602998,2.492118385,.7113544659 };
    static double a12[31] = { .705802694,-.2845938535,5.715471266,
	    -2.47282088,-.7738802408,.347829393,-11.37653694,-38.64768867,
	    .6932927651,-212.4017288,4.944204937,3.071270411,33.05882281,
	    7.387533799,2.366769108,79.22572682,.6154290178,.5592050551,
	    -.1796585105,-1.65493221,.7309108776,-.4926292779,-1.130266095,
	    -.009613974555,.1484586169,-.2215347198,7.883592948,.02768251655,
	    2.950280953,1.212634762,.5567714182 };
    static double a21[31] = { .1278764024,-.2320034273,1.805623266,
	    -32.3724144,-.9931490648,.317508563,-2.492465814,-16.21600096,
	    .2695393416,-6.752691265,3.971794901,14.54477563,41.10158386,
	    7.91288973,1.258297372,9.583547721,1.014141963,.5104134759,
	    -.1790430468,-1.756358428,.7561986717,-.6775248254,-.0401401642,
	    .01446794851,.1200521731,-.2203584559,4.50896385,.8221623576,
	    1.77993373,1.102649543,.886788002 };
    static double a22[31] = { .4036015198,-.3302974212,2.82773093,
	    -45.4440583,-1.611103927,.4927112073,-.003258457559,-49.59014949,
	    .3796217108,-233.7884098,4.31266698,18.05051709,28.95320323,
	    11.09948019,.7471649558,67.10246193,.5667096597,.6468519751,
	    -.1560665317,-1.460805289,.7719653528,-.6658988668,2.515179349e-6,
	    .02426021891,.1195003324,-.2625739255,4.377172556,.2421190547,
	    2.503482679,1.071587299,.724799743 };

    /* System generated locals */
    double d__1, d__2, d__3, d__4, d__5;

    /* Builtin functions */
    double sqrt(double), atan2(double, double), sin(double), 
	    cos(double), pow(double, double);

    /* Local variables */
    static double by_s__, byas, phis, dphisphi, dphisrho;
    extern /* Subroutine */ int twocones_(double *, double *, 
	    double *, double *, double *, double *, 
	    double *);
    static double brack, cphic, sphic, psias, bphi_s__, bphias, cphics, 
	    xs, brho_s__, zs, brhoas, sphics, phi, rho, rsc, bxs, xsc, ysc, 
	    zsc, bzs, dphisdy, rho2, r1rh;


/* CALCULATES COMPONENTS  OF REGION 1/2 FIELD IN SPHERICAL COORDS.  DERIVE
D FROM THE S/R DIPDEF2C (WHICH*/
/*   DOES THE SAME JOB, BUT INPUT/OUTPUT THERE WAS IN SPHERICAL COORDS, WH
ILE HERE WE USE CARTESIAN ONES)*/

/*   INPUT:  NUMB=1 (2) FOR REGION 1 (2) CURRENTS */
/*          MODE=1 YIELDS SIMPLE SINUSOIDAL MLT VARIATION, WITH MAXIMUM CU
RRENT AT DAWN/DUSK MERIDIAN*/
/*     WHILE MODE=2 YIELDS THE SECOND HARMONIC. */


/*   NB# 6 */
/* (1) DPHI:   HALF-DIFFERENCE (IN RADIANS) BETWEEN DAY AND NIGHT LATITUDE
 OF FAC OVAL AT IONOSPHERIC ALTITUDE;*/
/*              TYPICAL VALUE: 0.06 */
/* (2) B:      AN ASYMMETRY FACTOR AT HIGH-ALTITUDES;  FOR B=0, THE ONLY A
SYMMETRY IS THAT FROM DPHI*/
/*              TYPICAL VALUES: 0.35-0.70 */
/* (3) RHO_0:  A FIXED PARAMETER, DEFINING THE DISTANCE RHO, AT WHICH THE 
LATITUDE SHIFT GRADUALLY SATURATES AND*/
/*              STOPS INCREASING */
/*              ITS VALUE WAS ASSUMED FIXED, EQUAL TO 7.0. */
/* (4) XKAPPA: AN OVERALL SCALING FACTOR, WHICH CAN BE USED FOR CHANGING T
HE SIZE OF THE F.A.C. OVAL*/

/* THESE PARAMETERS CONTRO */
/* parameters of the tilt-depend */
    dphi_b_rho0__1.b = .5f;
    dphi_b_rho0__1.rho_0__ = 7.f;
    modenum_1.m = *mode;
    if (*numb == 1) {
	dphi_b_rho0__1.dphi = .055;
	dtheta_1.dtheta = .06;
    }
    if (*numb == 2) {
	dphi_b_rho0__1.dphi = .03;
	dtheta_1.dtheta = .09;
    }
    xsc = *x * dphi_b_rho0__1.xkappa;
    ysc = *y * dphi_b_rho0__1.xkappa;
    zsc = *z__ * dphi_b_rho0__1.xkappa;
/* Computing 2nd power */
    d__1 = xsc;
/* Computing 2nd power */
    d__2 = zsc;
    rho = sqrt(d__1 * d__1 + d__2 * d__2);
/* Computing 2nd power */
    d__1 = xsc;
/* Computing 2nd power */
    d__2 = ysc;
/* Computing 2nd power */
    d__3 = zsc;
    rsc = sqrt(d__1 * d__1 + d__2 * d__2 + d__3 * d__3);

/* Computing 2nd power */
    d__1 = dphi_b_rho0__1.rho_0__;
    rho2 = d__1 * d__1;
    if (xsc == 0. && zsc == 0.) {
	phi = 0.;
    } else {
	phi = atan2(-zsc, xsc);
/*  FROM CARTESIAN TO CYLINDRICAL (RHO,PHI */
    }
    sphic = sin(phi);
    cphic = cos(phi);
/*  "C" means "CYLINDRICAL", TO DISTINGUISH FROM S */
/* Computing 2nd power */
    d__1 = rho;
/* Computing 2nd power */
    d__2 = rho;
    brack = dphi_b_rho0__1.dphi + dphi_b_rho0__1.b * rho2 / (rho2 + 1.) * (
	    d__1 * d__1 - 1.) / (rho2 + d__2 * d__2);
    r1rh = (rsc - 1.) / rh;
    d__1 = pow(r1rh, eps) + 1.;
    d__2 = 1. / eps;
    psias = beta * *ps / pow(d__1, d__2);
    phis = phi - brack * sin(phi) - psias;
    dphisphi = 1. - brack * cos(phi);
/* Computing 2nd power */
    d__2 = rho;
/* Computing 2nd power */
    d__1 = rho2 + d__2 * d__2;
    d__3 = eps - 1.;
    d__4 = pow(r1rh,eps) + 1.;
    d__5 = 1. / eps + 1.;
    dphisrho = dphi_b_rho0__1.b * -2. * rho2 * rho / (d__1 * d__1) * sin(phi) 
	    + beta * *ps * pow(r1rh, d__3) * rho / (rh * rsc * pow(d__4, d__5));
    d__1 = eps - 1.;
    d__2 = pow(r1rh, eps) + 1.;
    d__3 = 1. / eps + 1.;
    dphisdy = beta * *ps * pow(r1rh, d__1) * ysc / (rh * rsc * pow(d__2, d__3));
    sphics = sin(phis);
    cphics = cos(phis);
    xs = rho * cphics;
    zs = -rho * sphics;
    if (*numb == 1) {
	if (*mode == 1) {
	    twocones_(a11, &xs, &ysc, &zs, &bxs, &byas, &bzs);
	}
	if (*mode == 2) {
	    twocones_(a12, &xs, &ysc, &zs, &bxs, &byas, &bzs);
	}
    } else {
	if (*mode == 1) {
	    twocones_(a21, &xs, &ysc, &zs, &bxs, &byas, &bzs);
	}
	if (*mode == 2) {
	    twocones_(a22, &xs, &ysc, &zs, &bxs, &byas, &bzs);
	}
    }
    brhoas = bxs * cphics - bzs * sphics;
    bphias = -bxs * sphics - bzs * cphics;
    brho_s__ = brhoas * dphisphi * dphi_b_rho0__1.xkappa;
    bphi_s__ = (bphias - rho * (byas * dphisdy + brhoas * dphisrho)) * 
	    dphi_b_rho0__1.xkappa;
    by_s__ = byas * dphisphi * dphi_b_rho0__1.xkappa;
    *bx = brho_s__ * cphic - bphi_s__ * sphic;
    *by = by_s__;
    *bz = -brho_s__ * sphic - bphi_s__ * cphic;
    return 0;
} /* birk_1n2__ */


/*=========================================================================*/

/* Subroutine */ int twocones_(double *a, double *x, double *y, 
	double *z__, double *bx, double *by, double *bz)
{
    /* System generated locals */
    double d__1, d__2;

    /* Local variables */
    extern /* Subroutine */ int one_cone__(double *, double *, 
	    double *, double *, double *, double *, 
	    double *);
    static double bxn, byn, bzn, bxs, bys, bzs;


/* ADDS FIELDS FROM TWO CONES (NORTHERN AND SOUTHERN), WITH A PROPER SYMME
TRY*/
/* OF THE CURRENT AND FIELD, CORRESPONDING TO THE REGION 1 BIRKELAND CURRE
NTS. (NB #6, P.58).*/

    /* Parameter adjustments */
    --a;

    /* Function Body */
    one_cone__(&a[1], x, y, z__, &bxn, &byn, &bzn);
    d__1 = -(*y);
    d__2 = -(*z__);
    one_cone__(&a[1], x, &d__1, &d__2, &bxs, &bys, &bzs);
    *bx = bxn - bxs;
    *by = byn + bys;
    *bz = bzn + bzs;
    return 0;
} /* twocones_ */


/*-------------------------------------------------------------------------*/

/* Subroutine */ int one_cone__(double *a, double *x, double *y, 
	double *z__, double *bx, double *by, double *bz)
{
    /* Initialized data */

    static double dr = 1e-6;
    static double dt = 1e-6;

    /* System generated locals */
    double d__1, d__2;

    /* Builtin functions */
    double sqrt(double), atan2(double, double), sin(double);

    /* Local variables */
    static double bphi, phis, c__, r__, s, bfast, theta, btast, drsdr, 
	    drsdt, dtsdr, dtsdt, stsst, theta0, be, cf, br, sf, rs, btheta, 
	    thetas, phi;
    extern double r_s__(double *, double *, double *);
    static double rho;
    extern /* Subroutine */ int fialcos_(double *, double *, 
	    double *, double *, double *, int *, double *,
	     double *);
    extern double theta_s__(double *, double *, double *);
    static double rsr, rho2;


/* RETURNS FIELD COMPONENTS FOR A DEFORMED CONICAL CURRENT SYSTEM, FITTED 
TO A BIOSAVART FIELD*/
/*  HERE ONLY THE NORTHERN CONE IS TAKEN INTO ACCOUNT. */

    /* Parameter adjustments */
    --a;

    /* Function Body */
/*   JUST FOR NUMERICAL DIFFERENTIATION */
    theta0 = a[31];
/* Computing 2nd power */
    d__1 = *x;
/* Computing 2nd power */
    d__2 = *y;
    rho2 = d__1 * d__1 + d__2 * d__2;
    rho = sqrt(rho2);
/* Computing 2nd power */
    d__1 = *z__;
    r__ = sqrt(rho2 + d__1 * d__1);
    theta = atan2(rho, *z__);
    phi = atan2(*y, *x);

/*   MAKE THE DEFORMATION OF COORDINATES: */

    rs = r_s__(&a[1], &r__, &theta);
    thetas = theta_s__(&a[1], &r__, &theta);
    phis = phi;

/*   CALCULATE FIELD COMPONENTS AT THE NEW POSITION (ASTERISKED): */

    fialcos_(&rs, &thetas, &phis, &btast, &bfast, &modenum_1.m, &theta0, &
	    dtheta_1.dtheta);

/*   NOW TRANSFORM B{R,T,F}_AST BY THE DEFORMATION TENSOR: */

/*      FIRST OF ALL, FIND THE DERIVATIVES: */


    d__1 = r__ + dr;
    d__2 = r__ - dr;
    drsdr = (r_s__(&a[1], &d__1, &theta) - r_s__(&a[1], &d__2, &theta)) / (dr 
	    * 2.);
    d__1 = theta + dt;
    d__2 = theta - dt;
    drsdt = (r_s__(&a[1], &r__, &d__1) - r_s__(&a[1], &r__, &d__2)) / (dt * 
	    2.);
    d__1 = r__ + dr;
    d__2 = r__ - dr;
    dtsdr = (theta_s__(&a[1], &d__1, &theta) - theta_s__(&a[1], &d__2, &theta)
	    ) / (dr * 2.);
    d__1 = theta + dt;
    d__2 = theta - dt;
    dtsdt = (theta_s__(&a[1], &r__, &d__1) - theta_s__(&a[1], &r__, &d__2)) / 
	    (dt * 2.);
    stsst = sin(thetas) / sin(theta);
    rsr = rs / r__;
    br = -rsr / r__ * stsst * btast * drsdt;
/*   NB#6, P.43 */
    btheta = rsr * stsst * btast * drsdr;
/*          (IT IS */
    bphi = rsr * bfast * (drsdr * dtsdt - drsdt * dtsdr);
    s = rho / r__;
    c__ = *z__ / r__;
    sf = *y / rho;
    cf = *x / rho;
    be = br * s + btheta * c__;
    *bx = a[1] * (be * cf - bphi * sf);
    *by = a[1] * (be * sf + bphi * cf);
    *bz = a[1] * (br * c__ - btheta * s);
    return 0;
} /* one_cone__ */


/*===========================================================================
==========*/
double r_s__(double *a, double *r__, double *theta)
{
    /* System generated locals */
    double ret_val, d__1, d__2, d__3, d__4, d__5, d__6, d__7, d__8, d__9, 
	    d__10, d__11, d__12, d__13;

    /* Builtin functions */
    double sqrt(double), cos(double);


    /* Parameter adjustments */
    --a;

    /* Function Body */
/* Computing 2nd power */
    d__1 = *r__;
/* Computing 2nd power */
    d__2 = a[11];
/* Computing 2nd power */
    d__3 = *r__;
/* Computing 2nd power */
    d__4 = a[12];
/* Computing 2nd power */
    d__5 = *r__;
/* Computing 2nd power */
    d__6 = a[13];
/* Computing 2nd power */
    d__7 = *r__;
/* Computing 2nd power */
    d__8 = a[14];
/* Computing 2nd power */
    d__9 = *r__;
/* Computing 2nd power */
    d__10 = a[15];
/* Computing 2nd power */
    d__12 = *r__;
/* Computing 2nd power */
    d__13 = a[16];
/* Computing 2nd power */
    d__11 = d__12 * d__12 + d__13 * d__13;
    ret_val = *r__ + a[2] / *r__ + a[3] * *r__ / sqrt(d__1 * d__1 + d__2 * 
	    d__2) + a[4] * *r__ / (d__3 * d__3 + d__4 * d__4) + (a[5] + a[6] /
	     *r__ + a[7] * *r__ / sqrt(d__5 * d__5 + d__6 * d__6) + a[8] * *
	    r__ / (d__7 * d__7 + d__8 * d__8)) * cos(*theta) + (a[9] * *r__ / 
	    sqrt(d__9 * d__9 + d__10 * d__10) + a[10] * *r__ / (d__11 * d__11)
	    ) * cos(*theta * 2.);

    return ret_val;
} /* r_s__ */


/*---------------------------------------------------------------------------
--*/

double theta_s__(double *a, double *r__, double *theta)
{
    /* System generated locals */
    double ret_val, d__1, d__2, d__3, d__4, d__5, d__6, d__7, d__8, d__9;

    /* Builtin functions */
    double sqrt(double), sin(double);


    /* Parameter adjustments */
    --a;

    /* Function Body */
/* Computing 2nd power */
    d__1 = *r__;
/* Computing 2nd power */
    d__2 = *r__;
/* Computing 2nd power */
    d__3 = a[27];
/* Computing 2nd power */
    d__4 = *r__;
/* Computing 2nd power */
    d__5 = a[28];
/* Computing 2nd power */
    d__6 = *r__;
/* Computing 2nd power */
    d__7 = a[29];
/* Computing 2nd power */
    d__8 = *r__;
/* Computing 2nd power */
    d__9 = a[30];
    ret_val = *theta + (a[17] + a[18] / *r__ + a[19] / (d__1 * d__1) + a[20] *
	     *r__ / sqrt(d__2 * d__2 + d__3 * d__3)) * sin(*theta) + (a[21] + 
	    a[22] * *r__ / sqrt(d__4 * d__4 + d__5 * d__5) + a[23] * *r__ / (
	    d__6 * d__6 + d__7 * d__7)) * sin(*theta * 2.) + (a[24] + a[25] / 
	    *r__ + a[26] * *r__ / (d__8 * d__8 + d__9 * d__9)) * sin(*theta * 
	    3.);

    return ret_val;
} /* theta_s__ */


/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */

/* Subroutine */ int fialcos_(double *r__, double *theta, double *
	phi, double *btheta, double *bphi, int *n, double *
	theta0, double *dt)
{
    /* System generated locals */
    int i__1;

    /* Builtin functions */
    double sin(double), cos(double), tan(double);

    /* Local variables */
    static double ccos[10], ssin[10], cosm1, tgm2m, sinm1, tgp2m;
    static int m;
    static double t, cosfi, tgm2m1, sinfi, coste, sinte, fc, tg, ro, tm, 
	    tetanm, fc1, tetanp, ctg, tg21, bpn[10], btn[10], tgm, tgp, dtt, 
	    tgm2, dtt0, tgp2;


/* CONICAL MODEL OF BIRKELAND CURRENT FIELD; BASED ON THE OLD S/R FIALCO (
OF 1990-91)*/
/* NB OF 1985-86-88, NOTE OF MARCH 5, BUT HERE BOTH INPUT AND OUTPUT ARE I
N SPHERICAL CDS.*/
/* BTN, AND BPN ARE THE ARRAYS OF BTHETA AND BPHI (BTN(i), BPN(i) CORRESPO
ND TO i-th MODE).*/
/*   ONLY FIRST  N  MODE AMPLITUDES ARE COMPUTED (N<=10). */
/*   THETA0 IS THE ANGULAR HALF-WIDTH OF THE CONE, DT IS THE ANGULAR H.-W.
 OF THE CURRENT LAYER*/
/*   NOTE:  BR=0  (BECAUSE ONLY RADIAL CURRENTS ARE PRESENT IN THIS MODEL)
 */

    sinte = sin(*theta);
    ro = *r__ * sinte;
    coste = cos(*theta);
    sinfi = sin(*phi);
    cosfi = cos(*phi);
    tg = sinte / (coste + 1.);
/*        TAN(THETA/2) */
    ctg = sinte / (1. - coste);


/*        COT(THETA/2) */
    tetanp = *theta0 + *dt;
    tetanm = *theta0 - *dt;
    if (*theta < tetanm) {
	goto L1;
    }
    tgp = tan(tetanp * .5);
    tgm = tan(tetanm * .5);
    tgm2 = tgm * tgm;
    tgp2 = tgp * tgp;
L1:
    cosm1 = 1.;
    sinm1 = 0.;
    tm = 1.;
    tgm2m = 1.;
    tgp2m = 1.;
    i__1 = *n;
    for (m = 1; m <= i__1; ++m) {
	tm *= tg;
	ccos[m - 1] = cosm1 * cosfi - sinm1 * sinfi;
	ssin[m - 1] = sinm1 * cosfi + cosm1 * sinfi;
	cosm1 = ccos[m - 1];
	sinm1 = ssin[m - 1];
	if (*theta < tetanm) {
	    t = tm;
	    dtt = m * .5 * tm * (tg + ctg);
	    dtt0 = 0.;
	} else if (*theta < tetanp) {
	    tgm2m *= tgm2;
	    fc = 1. / (tgp - tgm);
	    fc1 = 1. / ((m << 1) + 1);
	    tgm2m1 = tgm2m * tgm;
	    tg21 = tg * tg + 1.;
	    t = fc * (tm * (tgp - tg) + fc1 * (tm * tg - tgm2m1 / tm));
	    dtt = m * .5 * fc * tg21 * (tm / tg * (tgp - tg) - fc1 * (tm - 
		    tgm2m1 / (tm * tg)));
	    dtt0 = fc * .5 * ((tgp + tgm) * (tm * tg - fc1 * (tm * tg - 
		    tgm2m1 / tm)) + tm * (1. - tgp * tgm) - (tgm2 + 1.) * 
		    tgm2m / tm);
	} else {
	    tgp2m *= tgp2;
	    tgm2m *= tgm2;
	    fc = 1. / (tgp - tgm);
	    fc1 = 1. / ((m << 1) + 1);
	    t = fc * fc1 * (tgp2m * tgp - tgm2m * tgm) / tm;
	    dtt = -t * m * .5 * (tg + ctg);
	}
	btn[m - 1] = m * t * ccos[m - 1] / ro;
/* L2: */
	bpn[m - 1] = -dtt * ssin[m - 1] / *r__;
    }
    *btheta = btn[*n - 1] * 800.f;
    *bphi = bpn[*n - 1] * 800.f;
    return 0;
} /* fialcos_ */


/*-------------------------------------------------------------------------*/


/* Subroutine */ int birk_shl__(double *a, double *ps, double *
	x_sc__, double *x, double *y, double *z__, double *bx,
	 double *by, double *bz)
{
    /* System generated locals */
    double d__1, d__2;

    /* Builtin functions */
    double cos(double), sin(double), sqrt(double), exp(double)
	    ;

    /* Local variables */
    static double cypi, cyqi, czrk, czsk, sypi, syqi, sqpr, sqqs, szrk, 
	    szsk;
    static int i__, k, l, m, n;
    static double p, q, r__, s, x1, x2, z1, z2;
    static int nn;
    static double fx, gx, gy, gz, fy, fz, hx, hy, hz, ct1, ct2, st1, st2, 
	    cps, epr, eqs, hxr, hzr, sps, pst1, s3ps, pst2;



    /* Parameter adjustments */
    --a;

    /* Function Body */
    cps = cos(*ps);
    sps = sin(*ps);
    s3ps = cps * 2.;

    pst1 = *ps * a[85];
    pst2 = *ps * a[86];
    st1 = sin(pst1);
    ct1 = cos(pst1);
    st2 = sin(pst2);
    ct2 = cos(pst2);
    x1 = *x * ct1 - *z__ * st1;
    z1 = *x * st1 + *z__ * ct1;
    x2 = *x * ct2 - *z__ * st2;
    z2 = *x * st2 + *z__ * ct2;

    l = 0;
    gx = 0.;
    gy = 0.;
    gz = 0.;

    for (m = 1; m <= 2; ++m) {
/*                         AND M=2 IS FOR THE SECOND SUM ("PARALL." SY
MMETRY)*/
/*    M=1 IS FOR THE 1ST SUM ("PERP." SYMMETRY) */
	for (i__ = 1; i__ <= 3; ++i__) {
	    p = a[i__ + 72];
	    q = a[i__ + 78];
	    cypi = cos(*y / p);
	    cyqi = cos(*y / q);
	    sypi = sin(*y / p);
	    syqi = sin(*y / q);

	    for (k = 1; k <= 3; ++k) {
		r__ = a[k + 75];
		s = a[k + 81];
		szrk = sin(z1 / r__);
		czsk = cos(z2 / s);
		czrk = cos(z1 / r__);
		szsk = sin(z2 / s);
/* Computing 2nd power */
		d__1 = p;
/* Computing 2nd power */
		d__2 = r__;
		sqpr = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
/* Computing 2nd power */
		d__1 = q;
/* Computing 2nd power */
		d__2 = s;
		sqqs = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
		epr = exp(x1 * sqpr);
		eqs = exp(x2 * sqqs);

		for (n = 1; n <= 2; ++n) {
/*                                AND N=2 IS FOR THE SECON
D ONE */
/* N=1 IS FOR THE FIRST PART OF EACH COEFFI */
		    for (nn = 1; nn <= 2; ++nn) {
/*                                        TO TAKE INTO
 ACCOUNT THE SCALE FACTOR DEPENDENCE*/
/*   NN = 1,2 FURTHER SPLITS THE COEFFICI */
			if (m == 1) {
			    fx = -sqpr * epr * cypi * szrk;
			    fy = epr * sypi * szrk / p;
			    fz = -epr * cypi * czrk / r__;
			    if (n == 1) {
				if (nn == 1) {
				    hx = fx;
				    hy = fy;
				    hz = fz;
				} else {
				    hx = fx * *x_sc__;
				    hy = fy * *x_sc__;
				    hz = fz * *x_sc__;
				}
			    } else {
				if (nn == 1) {
				    hx = fx * cps;
				    hy = fy * cps;
				    hz = fz * cps;
				} else {
				    hx = fx * cps * *x_sc__;
				    hy = fy * cps * *x_sc__;
				    hz = fz * cps * *x_sc__;
				}
			    }
			} else {
/*   M.EQ.2 */
			    fx = -sps * sqqs * eqs * cyqi * czsk;
			    fy = sps / q * eqs * syqi * czsk;
			    fz = sps / s * eqs * cyqi * szsk;
			    if (n == 1) {
				if (nn == 1) {
				    hx = fx;
				    hy = fy;
				    hz = fz;
				} else {
				    hx = fx * *x_sc__;
				    hy = fy * *x_sc__;
				    hz = fz * *x_sc__;
				}
			    } else {
				if (nn == 1) {
				    hx = fx * s3ps;
				    hy = fy * s3ps;
				    hz = fz * s3ps;
				} else {
				    hx = fx * s3ps * *x_sc__;
				    hy = fy * s3ps * *x_sc__;
				    hz = fz * s3ps * *x_sc__;
				}
			    }
			}
			++l;
			if (m == 1) {
			    hxr = hx * ct1 + hz * st1;
			    hzr = -hx * st1 + hz * ct1;
			} else {
			    hxr = hx * ct2 + hz * st2;
			    hzr = -hx * st2 + hz * ct2;
			}
			gx += hxr * a[l];
			gy += hy * a[l];
/* L5: */
			gz += hzr * a[l];
		    }
/* L4: */
		}
/* L3: */
	    }
/* L2: */
	}
/* L1: */
    }
    *bx = gx;
    *by = gy;
    *bz = gz;
    return 0;
} /* birk_shl__ */


/****************************************************************************
**********/

/* Subroutine */ int full_rc__(int *iopr, double *ps, double *x, 
	double *y, double *z__, double *bxsrc, double *bysrc, 
	double *bzsrc, double *bxprc, double *byprc, double *
	bzprc)
{
    /* Initialized data */

    static double c_sy__[86] = { 1675.694858,1780.006388,-961.6082149,
	    -1668.914259,-27.40437029,-107.416967,27.76189943,92.89740503,
	    -43.92949274,-403.6444072,6.167161865,298.2779761,-1680.779044,
	    -1780.933039,964.1861088,1670.988659,27.4886465,107.7809519,
	    -27.84600972,-93.20691865,44.28496784,404.4537249,-6.28195873,
	    -298.6050952,-7.971914848,2.017383761,-1.492230168,-1.957411655,
	    -.08525523181,-.3811813235,.08446716725,.3215044399,-.7141912767,
	    -.9086294596,.2966677742,-.04736679933,-11.38731325,.1719795189,
	    1.356233066,.8613438429,-.09143823092,-.2593979098,.04244838338,
	    .06318383319,-.5861372726,-.03368780733,-.07104470269,
	    -.06909052953,-60.18659631,-32.87563877,11.76450433,5.891673644,
	    2.562360333,6.215377232,-1.273945165,-1.864704763,-5.394837143,
	    -8.799382627,3.743066561,-.7649164511,57.09210569,32.61236511,
	    -11.28688017,-5.849523392,-2.470635922,-5.961417272,1.230031099,
	    1.793192595,5.383736074,8.369895153,-3.611544412,.7898988697,
	    7.970609948,7.981216562,35.16822497,12.45651654,1.689755359,
	    3.678712366,23.66117284,6.987136092,6.886678677,20.91245928,
	    1.650064156,3.474068566,.3474715765,.6564043111 };
    static double c_pr__[86] = { -64820.58481,-63965.62048,66267.93413,
	    135049.7504,-36.56316878,124.6614669,56.75637955,-87.56841077,
	    5848.631425,4981.097722,-6233.712207,-10986.40188,68716.52057,
	    65682.69473,-69673.32198,-138829.3568,43.45817708,-117.9565488,
	    -62.14836263,79.83651604,-6211.451069,-5151.633113,6544.481271,
	    11353.03491,23.72352603,-256.4846331,25.77629189,145.2377187,
	    -4.472639098,-3.554312754,2.936973114,2.682302576,2.728979958,
	    26.43396781,-9.312348296,-29.65427726,-247.5855336,-206.9111326,
	    74.25277664,106.4069993,15.45391072,16.35943569,-5.96517775,
	    -6.0794517,115.6748385,-35.27377307,-32.28763497,-32.53122151,
	    93.7440931,84.25677504,-29.23010465,-43.79485175,-6.434679514,
	    -6.620247951,2.443524317,2.266538956,-43.82903825,6.904117876,
	    12.24289401,17.62014361,152.3078796,124.5505289,-44.5869029,
	    -63.0238241,-8.999368955,-9.693774119,3.510930306,3.770949738,
	    -77.96705716,22.07730961,20.46491655,18.67728847,9.451290614,
	    9.313661792,644.762097,418.2515954,7.183754387,35.62128817,
	    19.43180682,39.57218411,15.69384715,7.123215241,2.300635346,
	    21.90881131,-.0177583937,.399634671 };

    static double x_sc__;
    extern /* Subroutine */ int rc_shield__(double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *);
    static double hxprc, hyprc, hxsrc, hysrc, hzsrc, hzprc, fpx, fpy, fpz,
	     fsx, fsy, fsz;
    extern /* Subroutine */ int src_prc__(int *, double *, double 
	    *, double *, double *, double *, double *, 
	    double *, double *, double *, double *, 
	    double *, double *, double *);


/*  CALCULATES GSM FIELD COMPONENTS OF THE SYMMETRIC (SRC) AND PARTIAL (PR
C) COMPONENTS OF THE RING CURRENT*/
/*   SRC  PROVIDES A DEPRESSION OF -28 nT AT EARTH */
/*  PRC  CORRESPONDS TO THE PRESSURE DIFFERENCE OF 2 nPa BETWEEN MIDNIGHT 
AND NOON RING CURRENT*/
/*            PARTICLE PRESSURE AND YIELDS A DEPRESSION OF -17 nT AT X=-6R
e*/

/*  SC_SY AND SC_PR ARE SCALING FACTORS FOR THE SYMMETRIC AND PARTIAL COMP
ONENTS:*/
/*          VALUES LARGER THAN 1 RESULT IN SPATIALLY LARGER CURRENTS */

/*  PHI IS THE ROTATION ANGLE IN RADIANS OF THE PARTIAL RING CURRENT (MEAS
URED FROM MIDNIGHT TOWARD DUSK)*/

/*    IOPR -  A RING CURRENT CALCULATION FLAG (FOR LEAST-SQUARES FITTING O
NLY):*/
/*             IOPR=0 - BOTH SRC AND PRC FIELDS ARE CALCULATED */
/*             IOPR=1 - SRC ONLY */
/*             IOPR=2 - PRC ONLY */


    src_prc__(iopr, &rcpar_2.sc_sy__, &rcpar_2.sc_pr__, &rcpar_2.phi, ps, x, 
	    y, z__, &hxsrc, &hysrc, &hzsrc, &hxprc, &hyprc, &hzprc);
    x_sc__ = rcpar_2.sc_sy__ - 1.;
    if (*iopr == 0 || *iopr == 1) {
	rc_shield__(c_sy__, ps, &x_sc__, x, y, z__, &fsx, &fsy, &fsz);
    } else {
	fsx = 0.;
	fsy = 0.;
	fsz = 0.;
    }
    x_sc__ = rcpar_2.sc_pr__ - 1.;
    if (*iopr == 0 || *iopr == 2) {
	rc_shield__(c_pr__, ps, &x_sc__, x, y, z__, &fpx, &fpy, &fpz);
    } else {
	fpx = 0.;
	fpy = 0.;
	fpz = 0.;
    }
    *bxsrc = hxsrc + fsx;
    *bysrc = hysrc + fsy;
    *bzsrc = hzsrc + fsz;
    *bxprc = hxprc + fpx;
    *byprc = hyprc + fpy;
    *bzprc = hzprc + fpz;
    return 0;
} /* full_rc__ */

/*---------------------------------------------------------------------------
------------*/

/* Subroutine */ int src_prc__(int *iopr, double *sc_sy__, double 
	*sc_pr__, double *phi, double *ps, double *x, double *
	y, double *z__, double *bxsrc, double *bysrc, double *
	bzsrc, double *bxprc, double *byprc, double *bzprc)
{
    /* Builtin functions */
    double cos(double), sin(double);

    /* Local variables */
    extern /* Subroutine */ int prc_quad__(double *, double *, 
	    double *, double *, double *, double *), 
	    prc_symm__(double *, double *, double *, double *,
	     double *, double *);
    static double bxa_q__, bxa_s__, bya_s__, bza_s__, bza_q__, bya_q__, 
	    cp, sp, xr, yr, xt, bxa_qr__, zt, bya_qr__, cps, bxp, byp, bzp, 
	    xta, yta, zta, bxs, bys, bzs, sps, xts, yts, zts;
    extern /* Subroutine */ int rc_symm__(double *, double *, 
	    double *, double *, double *, double *);


/*  RETURNS FIELD COMPONENTS FROM A MODEL RING CURRENT, INCLUDING ITS SYMM
ETRIC PART*/
/*    AND A PARTIAL RING CURRENT, CLOSED VIA BIRKELAND CURRENTS. BASED ON 
RESULTS, DESCRIBED*/
/*    IN A PAPER "MODELING THE INNER MAGNETOSPHERE: ASYMMETRIC RING CURREN
T AND REGION 2*/
/*     BIRKELAND CURRENTS REVISITED" (JGR, DEC.2000). */

/*    IOPR -  A RING CURRENT CALCULATION FLAG (FOR LEAST-SQUARES FITTING O
NLY):*/
/*             IOPR=0 - BOTH SRC AND PRC FIELDS ARE CALCULATED */
/*             IOPR=1 - SRC ONLY */
/*             IOPR=2 - PRC ONLY */

/*    SC_SY &  SC_PR ARE SCALE FACTORS FOR THE ABOVE COMPONENTS;  TAKING S
C<1 OR SC>1 MAKES THE CURRENTS*/
/*                      SHRINK OR EXPAND, RESPECTIVELY. */

/*  PHI IS THE ROTATION ANGLE (RADIANS) OF THE PARTIAL RING CURRENT (MEASU
RED FROM MIDNIGHT TOWARD DUSK)*/


/*   1.  TRANSFORM TO TILTED COORDINATES (i.e., SM coordinates): */

    cps = cos(*ps);
    sps = sin(*ps);
    xt = *x * cps - *z__ * sps;
    zt = *z__ * cps + *x * sps;

/*  2.  SCALE THE COORDINATES FOR THE SYMMETRIC AND PARTIAL RC COMPONENTS:
*/

    xts = xt / *sc_sy__;
/*  SYMMETRIC */
    yts = *y / *sc_sy__;
    zts = zt / *sc_sy__;
    xta = xt / *sc_pr__;
/*  PARTIAL */
    yta = *y / *sc_pr__;
    zta = zt / *sc_pr__;

/*  3.  CALCULATE COMPONENTS OF THE TOTAL FIELD IN THE TILTED (SOLAR-MAGNE
TIC) COORDINATE SYSTEM:*/

/* ==========   ONLY FOR LEAST SQUARES FITTING: */
    bxs = 0.;
    bys = 0.;
    bzs = 0.;
    bxa_s__ = 0.;
    bya_s__ = 0.;
    bza_s__ = 0.;
    bxa_qr__ = 0.;
    bya_qr__ = 0.;
    bza_q__ = 0.;
/* ============================================ */

/*    3a. SYMMETRIC FIELD: */

    if (*iopr <= 1) {
	rc_symm__(&xts, &yts, &zts, &bxs, &bys, &bzs);
    }
    if (*iopr == 0 || *iopr == 2) {
	prc_symm__(&xta, &yta, &zta, &bxa_s__, &bya_s__, &bza_s__);
    }
/*   3b. ROTATE THE SCALED SM COORDINATES BY PHI AROUND ZSM AXIS AND CALCU
LATE QUADRUPOLE PRC FIELD*/
/*         IN THOSE COORDS: */
    cp = cos(*phi);
    sp = sin(*phi);
    xr = xta * cp - yta * sp;
    yr = xta * sp + yta * cp;
    if (*iopr == 0 || *iopr == 2) {
	prc_quad__(&xr, &yr, &zta, &bxa_qr__, &bya_qr__, &bza_q__);
    }
/*    3c. TRANSFORM THE QUADRUPOLE FIELD COMPONENTS BACK TO THE SM COORDS:
 */

    bxa_q__ = bxa_qr__ * cp + bya_qr__ * sp;
    bya_q__ = -bxa_qr__ * sp + bya_qr__ * cp;
/*    3d. FIND THE TOTAL FIELD OF PRC (SYMM.+QUADR.) IN THE SM COORDS: */

    bxp = bxa_s__ + bxa_q__;
    byp = bya_s__ + bya_q__;
    bzp = bza_s__ + bza_q__;

/*  4.  TRANSFORM THE FIELDS OF BOTH PARTS OF THE RING CURRENT BACK TO THE
 GSM SYSTEM:*/

    *bxsrc = bxs * cps + bzs * sps;
/*    SYMMETRIC RC */
    *bysrc = bys;
    *bzsrc = bzs * cps - bxs * sps;

    *bxprc = bxp * cps + bzp * sps;
/*    PARTIAL RC */
    *byprc = byp;
    *bzprc = bzp * cps - bxp * sps;

    return 0;
} /* src_prc__ */


/*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
&&&*/

/* Subroutine */ int rc_symm__(double *x, double *y, double *z__, 
	double *bx, double *by, double *bz)
{
    /* Initialized data */

    static double ds = .01;
    static double dc = .99994999875;
    static double d__ = 1e-4;
    static double drd = 5e3;

    /* System generated locals */
    double d__1, d__2;

    /* Builtin functions */
    double sqrt(double), atan2(double, double), sin(double), 
	    cos(double);

    /* Local variables */
    static double cost, sint, a, r__, dardr, theta, costm, costp, sintm, 
	    sintp, r2;
    extern double ap_(double *, double *, double *);
    static double br, bt, rm, tm, rp, tp, fxy, rho2;

/* DS=SIN(TH */
/* Computing 2nd power */
    d__1 = *x;
/* Computing 2nd power */
    d__2 = *y;
    rho2 = d__1 * d__1 + d__2 * d__2;
/* Computing 2nd power */
    d__1 = *z__;
    r2 = rho2 + d__1 * d__1;
    r__ = sqrt(r2);
    rp = r__ + d__;
    rm = r__ - d__;
    sint = sqrt(rho2) / r__;
    cost = *z__ / r__;
    if (sint < ds) {
/*                                    TO AVOID THE SINGULARITY PROBLEM
 */
/*  TOO CLOSE TO THE Z-AXIS; USING A LINEAR A */
	a = ap_(&r__, &ds, &dc) / ds;
	dardr = (rp * ap_(&rp, &ds, &dc) - rm * ap_(&rm, &ds, &dc)) * drd;
	fxy = *z__ * (a * 2. - dardr) / (r__ * r2);
	*bx = fxy * *x;
	*by = fxy * *y;
/* Computing 2nd power */
	d__1 = cost;
/* Computing 2nd power */
	d__2 = sint;
	*bz = (a * 2. * (d__1 * d__1) + dardr * (d__2 * d__2)) / r__;
    } else {
	theta = atan2(sint, cost);
	tp = theta + d__;
	tm = theta - d__;
	sintp = sin(tp);
	sintm = sin(tm);
	costp = cos(tp);
	costm = cos(tm);
	br = (sintp * ap_(&r__, &sintp, &costp) - sintm * ap_(&r__, &sintm, &
		costm)) / (r__ * sint) * drd;
	bt = (rm * ap_(&rm, &sint, &cost) - rp * ap_(&rp, &sint, &cost)) / 
		r__ * drd;
	fxy = (br + bt * cost / sint) / r__;
	*bx = fxy * *x;
	*by = fxy * *y;
	*bz = br * cost - bt * sint;
    }
    return 0;
} /* rc_symm__ */


/*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
&&&&&&&&&*/

double ap_(double *r__, double *sint, double *cost)
{
    /* Initialized data */

    static double a1 = -563.3722359;
    static double a2 = 425.0891691;
    static double rrc1 = 4.150588549;
    static double dd1 = 2.266150226;
    static double rrc2 = 3.334503403;
    static double dd2 = 3.079071195;
    static double p1 = .02602428295;
    static double r1 = 8.937790598;
    static double dr1 = 3.327934895;
    static double dla1 = .4487061833;
    static double p2 = .09125832351;
    static double r2 = 6.243029867;
    static double dr2 = 1.75014591;
    static double dla2 = .4181957162;
    static double p3 = .06106691992;
    static double r3 = 2.079908581;
    static double dr3 = .6828548533;

    /* System generated locals */
    double ret_val, d__1, d__2, d__3, d__4, d__5;

    /* Builtin functions */
    double exp(double), sqrt(double), pow(double, double), log(double);

    /* Local variables */
    static double rhos;
    static int prox;
    static double aphi1, aphi2, cost1, rhos2, sint1, c__, f, g, p, q, 
	    gamma, alpha, alsqh, costs, sints, dl, xk, rs, zs, xkrho12, xk2, 
	    gammas2, ele, elk, gamma_s__, alpha_s__, xk2s;


/*     Calculates azimuthal component of the vector potential of the symme
tric*/
/*  part of the model ring current. */

/*                                                            OF DIPOLAR C
OORDINATES BECOMES INACCURATE*/
/*  INDICATES WHETHER WE ARE TOO CLOSE TO THE AXIS O */
    prox = FALSE_;
    sint1 = *sint;
    cost1 = *cost;
    if (sint1 < .01) {
/*  TOO CLOSE TO Z-AXIS;  USE LINEAR INTE */
	sint1 = .01;
	cost1 = .99994999875f;
	prox = TRUE_;
    }
/* Computing 2nd power */
    d__1 = sint1;
    alpha = d__1 * d__1 / *r__;
/*  R,THETA -> ALPHA,GAMMA */
/* Computing 2nd power */
    d__1 = *r__;
    gamma = cost1 / (d__1 * d__1);
/* Computing 2nd power */
    d__1 = (*r__ - r1) / dr1;
/* Computing 2nd power */
    d__2 = cost1 / dla1;
/* Computing 2nd power */
    d__3 = (*r__ - r2) / dr2;
/* Computing 2nd power */
    d__4 = cost1 / dla2;
/* Computing 2nd power */
    d__5 = (*r__ - r3) / dr3;
    alpha_s__ = alpha * (p1 * exp(-(d__1 * d__1) - d__2 * d__2) + 1. + p2 * 
	    exp(-(d__3 * d__3) - d__4 * d__4) + p3 * exp(-(d__5 * d__5)));
/*  ALPHA -> ALPHA_S  (DEFORMED) */
    gamma_s__ = gamma;
/* Computing 2nd power */
    d__1 = gamma_s__;
    gammas2 = d__1 * d__1;
/* Computing 2nd power */
    d__1 = alpha_s__;
    alsqh = d__1 * d__1 / 2.;
/*  ALPHA_S,GAMMA_S -> RS,SINTS */
/* Computing 2nd power */
    d__1 = alsqh;
    f = gammas2 * 2.3703703703703702 + d__1 * d__1;
    d__1 = sqrt(f) + alsqh;
    q = pow(d__1, c_b55);
    c__ = q - pow(gammas2, c_b55) * 4. / (q * 3.);
    if (c__ < 0.) {
	c__ = 0.;
    }
/* Computing 2nd power */
    d__1 = c__;
    g = sqrt(d__1 * d__1 + pow(gammas2, c_b55) * 4.);
    rs = 4. / ((sqrt(g * 2. - c__) + sqrt(c__)) * (g + c__));
/* Computing 2nd power */
    d__1 = rs;
    costs = gamma_s__ * (d__1 * d__1);
/* Computing 2nd power */
    d__1 = costs;
    sints = sqrt(1. - d__1 * d__1);
    rhos = rs * sints;
/* Computing 2nd power */
    d__1 = rhos;
    rhos2 = d__1 * d__1;
    zs = rs * costs;

/*  1st loop: */
/* Computing 2nd power */
    d__1 = rrc1 + rhos;
/* Computing 2nd power */
    d__2 = zs;
/* Computing 2nd power */
    d__3 = dd1;
    p = d__1 * d__1 + d__2 * d__2 + d__3 * d__3;
    xk2 = rrc1 * 4. * rhos / p;
    xk = sqrt(xk2);
    xkrho12 = xk * sqrt(rhos);

/*    NB#4, P.3 */
    xk2s = 1. - xk2;
    dl = log(1. / xk2s);
    elk = xk2s * (xk2s * (xk2s * (xk2s * .01451196212f + .03742563713f) + 
	    .03590092383f) + .09666344259) + 1.38629436112 + dl * (xk2s * (
	    xk2s * (xk2s * (xk2s * .00441787012 + .03328355346) + 
	    .06880248576) + .12498593597) + .5);
    ele = xk2s * (xk2s * (xk2s * (xk2s * .01736506451 + .04757383546) + 
	    .0626060122) + .44325141463) + 1. + dl * xk2s * (xk2s * (xk2s * (
	    xk2s * .00526449639 + .04069697526) + .09200180037) + .2499836831)
	    ;

    aphi1 = ((1. - xk2 * .5) * elk - ele) / xkrho12;

/*  2nd loop: */
/* Computing 2nd power */
    d__1 = rrc2 + rhos;
/* Computing 2nd power */
    d__2 = zs;
/* Computing 2nd power */
    d__3 = dd2;
    p = d__1 * d__1 + d__2 * d__2 + d__3 * d__3;
    xk2 = rrc2 * 4. * rhos / p;
    xk = sqrt(xk2);
    xkrho12 = xk * sqrt(rhos);

/*    NB#4, P.3 */
    xk2s = 1. - xk2;
    dl = log(1. / xk2s);
    elk = xk2s * (xk2s * (xk2s * (xk2s * .01451196212f + .03742563713f) + 
	    .03590092383f) + .09666344259) + 1.38629436112 + dl * (xk2s * (
	    xk2s * (xk2s * (xk2s * .00441787012 + .03328355346) + 
	    .06880248576) + .12498593597) + .5);
    ele = xk2s * (xk2s * (xk2s * (xk2s * .01736506451 + .04757383546) + 
	    .0626060122) + .44325141463) + 1. + dl * xk2s * (xk2s * (xk2s * (
	    xk2s * .00526449639 + .04069697526) + .09200180037) + .2499836831)
	    ;

    aphi2 = ((1. - xk2 * .5) * elk - ele) / xkrho12;
    ret_val = a1 * aphi1 + a2 * aphi2;
    if (prox) {
	ret_val = ret_val * *sint / sint1;
    }

/*   LINEAR INTERPOLATION, IF TOO CLO */
    return ret_val;
} /* ap_ */


/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/

/* Subroutine */ int prc_symm__(double *x, double *y, double *z__,
	 double *bx, double *by, double *bz)
{
    /* Initialized data */

    static double ds = .01;
    static double dc = .99994999875;
    static double d__ = 1e-4;
    static double drd = 5e3;

    /* System generated locals */
    double d__1, d__2;

    /* Builtin functions */
    double sqrt(double), atan2(double, double), sin(double), 
	    cos(double);

    /* Local variables */
    static double cost, sint, a, r__, dardr;
    extern double apprc_(double *, double *, double *);
    static double theta, costm, costp, sintm, sintp, r2, br, bt, rm, tm, 
	    rp, tp, fxy, rho2;

/* DS=SIN(TH */
/* Computing 2nd power */
    d__1 = *x;
/* Computing 2nd power */
    d__2 = *y;
    rho2 = d__1 * d__1 + d__2 * d__2;
/* Computing 2nd power */
    d__1 = *z__;
    r2 = rho2 + d__1 * d__1;
    r__ = sqrt(r2);
    rp = r__ + d__;
    rm = r__ - d__;
    sint = sqrt(rho2) / r__;
    cost = *z__ / r__;
    if (sint < ds) {
/*                                    TO AVOID THE SINGULARITY PROBLEM
 */
/*  TOO CLOSE TO THE Z-AXIS; USING A LINEAR A */
	a = apprc_(&r__, &ds, &dc) / ds;
	dardr = (rp * apprc_(&rp, &ds, &dc) - rm * apprc_(&rm, &ds, &dc)) * 
		drd;
	fxy = *z__ * (a * 2. - dardr) / (r__ * r2);
	*bx = fxy * *x;
	*by = fxy * *y;
/* Computing 2nd power */
	d__1 = cost;
/* Computing 2nd power */
	d__2 = sint;
	*bz = (a * 2. * (d__1 * d__1) + dardr * (d__2 * d__2)) / r__;
    } else {
	theta = atan2(sint, cost);
	tp = theta + d__;
	tm = theta - d__;
	sintp = sin(tp);
	sintm = sin(tm);
	costp = cos(tp);
	costm = cos(tm);
	br = (sintp * apprc_(&r__, &sintp, &costp) - sintm * apprc_(&r__, &
		sintm, &costm)) / (r__ * sint) * drd;
	bt = (rm * apprc_(&rm, &sint, &cost) - rp * apprc_(&rp, &sint, &cost))
		 / r__ * drd;
	fxy = (br + bt * cost / sint) / r__;
	*bx = fxy * *x;
	*by = fxy * *y;
	*bz = br * cost - bt * sint;
    }
    return 0;
} /* prc_symm__ */


/*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
&&&&&&&&&*/


double apprc_(double *r__, double *sint, double *cost)
{
    /* Initialized data */

    static double a1 = -80.11202281;
    static double a2 = 12.58246758;
    static double rrc1 = 6.560486035;
    static double dd1 = 1.930711037;
    static double rrc2 = 3.827208119;
    static double dd2 = .7789990504;
    static double p1 = .3058309043;
    static double alpha1 = .1817139853;
    static double dal1 = .1257532909;
    static double beta1 = 3.422509402;
    static double dg1 = .04742939676;
    static double p2 = -4.800458958;
    static double alpha2 = -.02845643596;
    static double dal2 = .2188114228;
    static double beta2 = 2.545944574;
    static double dg2 = .00813272793;
    static double beta3 = .35868244;
    static double p3 = 103.1601001;
    static double alpha3 = -.00764731187;
    static double dal3 = .1046487459;
    static double beta4 = 2.958863546;
    static double dg3 = .01172314188;
    static double beta5 = .4382872938;
    static double q0 = .0113490815;
    static double q1 = 14.51339943;
    static double alpha4 = .2647095287;
    static double dal4 = .07091230197;
    static double dg4 = .01512963586;
    static double q2 = 6.861329631;
    static double alpha5 = .1677400816;
    static double dal5 = .04433648846;
    static double dg5 = .05553741389;
    static double beta6 = .7665599464;
    static double beta7 = .7277854652;

    /* System generated locals */
    double ret_val, d__1, d__2, d__3, d__4, d__5, d__6, d__7, d__8, d__9, 
	    d__10, d__11, d__12;

    /* Builtin functions */
    double pow(double, double), exp(double), sqrt(
	    double), log(double);

    /* Local variables */
    static double rhos;
    static int prox;
    static double aphi1, aphi2, cost1, rhos2, sint1, c__, f, g, p, q, 
	    gamma, alpha, alsqh, costs, sints, dl, xk, rs, zs, xkrho12, xk2, 
	    gammas2, ele, elk, gamma_s__, alpha_s__, xk2s;


/*     Calculates azimuthal component of the vector potential of the symme
tric*/
/*  part of the model PARTIAL ring current. */

    prox = FALSE_;
    sint1 = *sint;
    cost1 = *cost;
    if (sint1 < .01) {
/*  TOO CLOSE TO Z-AXIS;  USE LINEAR INTE */
	sint1 = .01;
	cost1 = .99994999875f;
	prox = TRUE_;
    }
/* Computing 2nd power */
    d__1 = sint1;
    alpha = d__1 * d__1 / *r__;
/*  R,THETA -> ALPHA,GAMMA */
/* Computing 2nd power */
    d__1 = *r__;
    gamma = cost1 / (d__1 * d__1);
/* Computing 2nd power */
    d__2 = (alpha - alpha1) / dal1;
    d__1 = d__2 * d__2 + 1.;
/* Computing 2nd power */
    d__3 = gamma / dg1;
/* Computing 2nd power */
    d__5 = (alpha - alpha2) / dal2;
    d__4 = d__5 * d__5 + 1.;
/* Computing 2nd power */
    d__7 = gamma / dg2;
    d__6 = d__7 * d__7 + 1.;
/* Computing 2nd power */
    d__8 = alpha - alpha3;
/* Computing 2nd power */
    d__10 = (alpha - alpha3) / dal3;
    d__9 = d__10 * d__10 + 1.;
/* Computing 2nd power */
    d__12 = gamma / dg3;
    d__11 = d__12 * d__12 + 1.;
    alpha_s__ = alpha * (p1 / pow(d__1, beta1) * exp(-(d__3 * d__3)) + 
	    1. + p2 * (alpha - alpha2) / pow(d__4, beta2) / pow(d__6,
	     beta3) + p3 * (d__8 * d__8) / pow(d__9, beta4) / pow(d__11, beta5));
/*  ALPHA -> ALPHA_S  (DEFORMED) */
/* Computing 2nd power */
    d__1 = (alpha - alpha4) / dal4;
/* Computing 2nd power */
    d__2 = gamma / dg4;
/* Computing 2nd power */
    d__4 = (alpha - alpha5) / dal5;
    d__3 = d__4 * d__4 + 1.;
/* Computing 2nd power */
    d__6 = gamma / dg5;
    d__5 = d__6 * d__6 + 1.;
    gamma_s__ = gamma * (q0 + 1. + q1 * (alpha - alpha4) * exp(-(d__1 * d__1) 
	    - d__2 * d__2) + q2 * (alpha - alpha5) / pow(d__3, beta6) / 
	    pow(d__5, beta7));
/*  GAMMA -> GAM */
/* Computing 2nd power */
    d__1 = gamma_s__;
    gammas2 = d__1 * d__1;
/* Computing 2nd power */
    d__1 = alpha_s__;
    alsqh = d__1 * d__1 / 2.;
/*  ALPHA_S,GAM */
/* Computing 2nd power */
    d__1 = alsqh;
    f = gammas2 * 2.3703703703703702 + d__1 * d__1;
    d__1 = sqrt(f) + alsqh;
    q = pow(d__1, c_b55);
    c__ = q - pow(gammas2, c_b55) * 4. / (q * 3.);
    if (c__ < 0.) {
	c__ = 0.;
    }
/* Computing 2nd power */
    d__1 = c__;
    g = sqrt(d__1 * d__1 + pow(gammas2, c_b55) * 4.);
    rs = 4. / ((sqrt(g * 2. - c__) + sqrt(c__)) * (g + c__));
/* Computing 2nd power */
    d__1 = rs;
    costs = gamma_s__ * (d__1 * d__1);
/* Computing 2nd power */
    d__1 = costs;
    sints = sqrt(1. - d__1 * d__1);
    rhos = rs * sints;
/* Computing 2nd power */
    d__1 = rhos;
    rhos2 = d__1 * d__1;
    zs = rs * costs;

/*  1st loop: */
/* Computing 2nd power */
    d__1 = rrc1 + rhos;
/* Computing 2nd power */
    d__2 = zs;
/* Computing 2nd power */
    d__3 = dd1;
    p = d__1 * d__1 + d__2 * d__2 + d__3 * d__3;
    xk2 = rrc1 * 4. * rhos / p;
    xk = sqrt(xk2);
    xkrho12 = xk * sqrt(rhos);

/*    NB#4, P.3 */
    xk2s = 1. - xk2;
    dl = log(1. / xk2s);
    elk = xk2s * (xk2s * (xk2s * (xk2s * .01451196212f + .03742563713f) + 
	    .03590092383f) + .09666344259) + 1.38629436112 + dl * (xk2s * (
	    xk2s * (xk2s * (xk2s * .00441787012 + .03328355346) + 
	    .06880248576) + .12498593597) + .5);
    ele = xk2s * (xk2s * (xk2s * (xk2s * .01736506451 + .04757383546) + 
	    .0626060122) + .44325141463) + 1. + dl * xk2s * (xk2s * (xk2s * (
	    xk2s * .00526449639 + .04069697526) + .09200180037) + .2499836831)
	    ;

    aphi1 = ((1. - xk2 * .5) * elk - ele) / xkrho12;

/*  2nd loop: */
/* Computing 2nd power */
    d__1 = rrc2 + rhos;
/* Computing 2nd power */
    d__2 = zs;
/* Computing 2nd power */
    d__3 = dd2;
    p = d__1 * d__1 + d__2 * d__2 + d__3 * d__3;
    xk2 = rrc2 * 4. * rhos / p;
    xk = sqrt(xk2);
    xkrho12 = xk * sqrt(rhos);

/*    NB#4, P.3 */
    xk2s = 1. - xk2;
    dl = log(1. / xk2s);
    elk = xk2s * (xk2s * (xk2s * (xk2s * .01451196212f + .03742563713f) + 
	    .03590092383f) + .09666344259) + 1.38629436112 + dl * (xk2s * (
	    xk2s * (xk2s * (xk2s * .00441787012 + .03328355346) + 
	    .06880248576) + .12498593597) + .5);
    ele = xk2s * (xk2s * (xk2s * (xk2s * .01736506451 + .04757383546) + 
	    .0626060122) + .44325141463) + 1. + dl * xk2s * (xk2s * (xk2s * (
	    xk2s * .00526449639 + .04069697526) + .09200180037) + .2499836831)
	    ;

    aphi2 = ((1. - xk2 * .5) * elk - ele) / xkrho12;
    ret_val = a1 * aphi1 + a2 * aphi2;
    if (prox) {
	ret_val = ret_val * *sint / sint1;
    }

/*   LINEAR INTERPOLATION, IF TO */
    return ret_val;
} /* apprc_ */


/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@@@@@@@@@@@*/


/* Subroutine */ int prc_quad__(double *x, double *y, double *z__,
	 double *bx, double *by, double *bz)
{
    /* Initialized data */

    static double d__ = 1e-4;
    static double dd = 2e-4;
    static double ds = .01;
    static double dc = .99994999875;

    /* System generated locals */
    double d__1, d__2, d__3, d__4;

    /* Builtin functions */
    double sqrt(double), atan2(double, double), sin(double), 
	    cos(double);

    /* Local variables */
    static double cphi, dbrr, dbtt, sphi;
    extern double br_prc_q__(double *, double *, double *);
    static double cost;
    extern double bt_prc_q__(double *, double *, double *);
    static double fcxy, sint, r__, theta, costm, costp, sintm, sintp, br, 
	    bt, ct, rm, tm, rp, tp, st, rho, rho2;


/* CALCULATES COMPONENTS OF THE FIELD FROM THE "QUADRUPOLE" COMPONENT OF T
HE PRC*/

/* Computing 2nd power */
    d__1 = *x;
/* Computing 2nd power */
    d__2 = *y;
    rho2 = d__1 * d__1 + d__2 * d__2;
/* Computing 2nd power */
    d__1 = *z__;
    r__ = sqrt(rho2 + d__1 * d__1);
    rho = sqrt(rho2);
    sint = rho / r__;
    cost = *z__ / r__;
    rp = r__ + d__;
    rm = r__ - d__;
    if (sint > ds) {
	cphi = *x / rho;
	sphi = *y / rho;
	br = br_prc_q__(&r__, &sint, &cost);
	bt = bt_prc_q__(&r__, &sint, &cost);
	dbrr = (br_prc_q__(&rp, &sint, &cost) - br_prc_q__(&rm, &sint, &cost))
		 / dd;
	theta = atan2(sint, cost);
	tp = theta + d__;
	tm = theta - d__;
	sintp = sin(tp);
	costp = cos(tp);
	sintm = sin(tm);
	costm = cos(tm);
	dbtt = (bt_prc_q__(&r__, &sintp, &costp) - bt_prc_q__(&r__, &sintm, &
		costm)) / dd;
/* Computing 2nd power */
	d__1 = sphi;
	*bx = sint * (br + (br + r__ * dbrr + dbtt) * (d__1 * d__1)) + cost * 
		bt;
	*by = -sint * sphi * cphi * (br + r__ * dbrr + dbtt);
	*bz = (br * cost - bt * sint) * cphi;
    } else {
	st = ds;
	ct = dc;
	if (*z__ < 0.) {
	    ct = -dc;
	}
	theta = atan2(st, ct);
	tp = theta + d__;
	tm = theta - d__;
	sintp = sin(tp);
	costp = cos(tp);
	sintm = sin(tm);
	costm = cos(tm);
	br = br_prc_q__(&r__, &st, &ct);
	bt = bt_prc_q__(&r__, &st, &ct);
	dbrr = (br_prc_q__(&rp, &st, &ct) - br_prc_q__(&rm, &st, &ct)) / dd;
	dbtt = (bt_prc_q__(&r__, &sintp, &costp) - bt_prc_q__(&r__, &sintm, &
		costm)) / dd;
	fcxy = r__ * dbrr + dbtt;
/* Computing 2nd power */
	d__1 = *x;
/* Computing 2nd power */
	d__2 = *y;
/* Computing 2nd power */
	d__3 = *y;
/* Computing 2nd power */
	d__4 = r__ * st;
	*bx = (br * (d__1 * d__1 + d__2 * d__2 * 2.) + fcxy * (d__3 * d__3)) /
		 (d__4 * d__4) + bt * cost;
/* Computing 2nd power */
	d__1 = r__ * st;
	*by = -(br + fcxy) * *x * *y / (d__1 * d__1);
	*bz = (br * cost / st - bt) * *x / r__;
    }
    return 0;
} /* prc_quad__ */


/*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
&&&&&&&&&&&&&&*/

double br_prc_q__(double *r__, double *sint, double *cost)
{
    /* Initialized data */

    static double a1 = -21.2666329;
    static double a2 = 32.24527521;
    static double a3 = -6.062894078;
    static double a4 = 7.515660734;
    static double a5 = 233.7341288;
    static double a6 = -227.1195714;
    static double a7 = 8.483233889;
    static double a8 = 16.80642754;
    static double a9 = -24.63534184;
    static double a10 = 9.067120578;
    static double a11 = -1.052686913;
    static double a12 = -12.08384538;
    static double a13 = 18.61969572;
    static double a14 = -12.71686069;
    static double a15 = 47017.35679;
    static double a16 = -50646.71204;
    static double a17 = 7746.058231;
    static double a18 = 1.531069371;
    static double xk1 = 2.318824273;
    static double al1 = .1417519429;
    static double dal1 = .00638801311;
    static double b1 = 5.303934488;
    static double be1 = 4.213397467;
    static double xk2 = .7955534018;
    static double al2 = .1401142771;
    static double dal2 = .02306094179;
    static double b2 = 3.462235072;
    static double be2 = 2.56874301;
    static double xk3 = 3.477425908;
    static double xk4 = 1.92215511;
    static double al3 = .1485233485;
    static double dal3 = .02319676273;
    static double b3 = 7.830223587;
    static double be3 = 8.492933868;
    static double al4 = .1295221828;
    static double dal4 = .01753008801;
    static double dg1 = .01125504083;
    static double al5 = .1811846095;
    static double dal5 = .04841237481;
    static double dg2 = .01981805097;
    static double c1 = 6.557801891;
    static double c2 = 6.348576071;
    static double c3 = 5.744436687;
    static double al6 = .2265212965;
    static double dal6 = .1301957209;
    static double drm = .5654023158;

    /* System generated locals */
    double ret_val, d__1, d__2, d__3;

    /* Builtin functions */
    double pow(double, double);

    /* Local variables */
    static double arga, argg, cost2, sint2, f, gamma, alpha, d1, d2, d3, 
	    d4, d5, d6, d7, d8, d9, fa, d10, d11, d12, d13, d14, d15, d16, 
	    d17, d18, sc, fs;
    extern /* Subroutine */ int ffs_(double *, double *, double *,
	     double *, double *, double *);


/*alculates the radial component of the "quadrupole" part of the model par
tial ring current.*/

/* Computing 2nd power */
    d__1 = *sint;
    sint2 = d__1 * d__1;
/* Computing 2nd power */
    d__1 = *cost;
    cost2 = d__1 * d__1;
    sc = *sint * *cost;
    alpha = sint2 / *r__;
/* Computing 2nd power */
    d__1 = *r__;
    gamma = *cost / (d__1 * d__1);
    ffs_(&alpha, &al1, &dal1, &f, &fa, &fs);
    d__1 = *r__ / b1;
    d1 = sc * pow(f, xk1) / (pow(d__1, be1) + 1.);
    d2 = d1 * cost2;
    ffs_(&alpha, &al2, &dal2, &f, &fa, &fs);
    d__1 = *r__ / b2;
    d3 = sc * pow(fs, xk2) / (pow(d__1, be2) + 1.);
    d4 = d3 * cost2;
    ffs_(&alpha, &al3, &dal3, &f, &fa, &fs);
    d__1 = *r__ / b3;
    d5 = sc * pow(alpha, xk3) * pow(fs, xk4) / (pow(d__1, be3) 
	    + 1.);
    d6 = d5 * cost2;
/* Computing 2nd power */
    d__1 = (alpha - al4) / dal4;
    arga = d__1 * d__1 + 1.;
/* Computing 2nd power */
    d__1 = gamma / dg1;
    argg = d__1 * d__1 + 1.;
    d7 = sc / arga / argg;
    d8 = d7 / arga;
    d9 = d8 / arga;
    d10 = d9 / arga;
/* Computing 2nd power */
    d__1 = (alpha - al5) / dal5;
    arga = d__1 * d__1 + 1.;
/* Computing 2nd power */
    d__1 = gamma / dg2;
    argg = d__1 * d__1 + 1.;
    d11 = sc / arga / argg;
    d12 = d11 / arga;
    d13 = d12 / arga;
    d14 = d13 / arga;
/* Computing 4th power */
    d__1 = *r__, d__1 *= d__1;
/* Computing 4th power */
    d__2 = c1, d__2 *= d__2;
    d15 = sc / (d__1 * d__1 + d__2 * d__2);
/* Computing 4th power */
    d__1 = *r__, d__1 *= d__1;
/* Computing 4th power */
    d__2 = c2, d__2 *= d__2;
    d16 = sc / (d__1 * d__1 + d__2 * d__2) * cost2;
/* Computing 4th power */
    d__1 = *r__, d__1 *= d__1;
/* Computing 4th power */
    d__2 = c3, d__2 *= d__2;
/* Computing 2nd power */
    d__3 = cost2;
    d17 = sc / (d__1 * d__1 + d__2 * d__2) * (d__3 * d__3);
    ffs_(&alpha, &al6, &dal6, &f, &fa, &fs);
/* Computing 2nd power */
    d__1 = (*r__ - 1.2) / drm;
    d18 = sc * fs / (d__1 * d__1 + 1.);
    ret_val = a1 * d1 + a2 * d2 + a3 * d3 + a4 * d4 + a5 * d5 + a6 * d6 + a7 *
	     d7 + a8 * d8 + a9 * d9 + a10 * d10 + a11 * d11 + a12 * d12 + a13 
	    * d13 + a14 * d14 + a15 * d15 + a16 * d16 + a17 * d17 + a18 * d18;

    return ret_val;
} /* br_prc_q__ */


/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%*/

double bt_prc_q__(double *r__, double *sint, double *cost)
{
    /* Initialized data */

    static double a1 = 12.74640393;
    static double a2 = -7.516393516;
    static double a3 = -5.476233865;
    static double a4 = 3.212704645;
    static double a5 = -59.10926169;
    static double a6 = 46.62198189;
    static double a7 = -.01644280062;
    static double a8 = .1234229112;
    static double a9 = -.08579198697;
    static double a10 = .01321366966;
    static double a11 = .8970494003;
    static double a12 = 9.136186247;
    static double a13 = -38.19301215;
    static double a14 = 21.73775846;
    static double a15 = -410.0783424;
    static double a16 = -69.9083269;
    static double a17 = -848.854344;
    static double xk1 = 1.243288286;
    static double al1 = .207172136;
    static double dal1 = .05030555417;
    static double b1 = 7.471332374;
    static double be1 = 3.180533613;
    static double xk2 = 1.376743507;
    static double al2 = .1568504222;
    static double dal2 = .02092910682;
    static double be2 = 1.985148197;
    static double xk3 = .315713994;
    static double xk4 = 1.056309517;
    static double al3 = .1701395257;
    static double dal3 = .101987007;
    static double b3 = 6.293740981;
    static double be3 = 5.671824276;
    static double al4 = .1280772299;
    static double dal4 = .02189060799;
    static double dg1 = .0104069608;
    static double al5 = .1648265607;
    static double dal5 = .04701592613;
    static double dg2 = .01526400086;
    static double c1 = 12.88384229;
    static double c2 = 3.361775101;
    static double c3 = 23.44173897;

    /* System generated locals */
    double ret_val, d__1, d__2, d__3;

    /* Builtin functions */
    double pow(double, double);

    /* Local variables */
    static double cost2, sint2, f, gamma, alpha, d1, d2, d3, d4, d5, d6, 
	    d7, d8, d9, fa, d10, d11, d12, d13, d14, d15, d16, d17, sc, fs, 
	    fcc, arg;
    extern /* Subroutine */ int ffs_(double *, double *, double *,
	     double *, double *, double *);


/*alculates the Theta component of the "quadrupole" part of the model part
ial ring current.*/

/* Computing 2nd power */
    d__1 = *sint;
    sint2 = d__1 * d__1;
/* Computing 2nd power */
    d__1 = *cost;
    cost2 = d__1 * d__1;
    sc = *sint * *cost;
    alpha = sint2 / *r__;
/* Computing 2nd power */
    d__1 = *r__;
    gamma = *cost / (d__1 * d__1);
    ffs_(&alpha, &al1, &dal1, &f, &fa, &fs);
    d__1 = *r__ / b1;
    d1 = pow(f, xk1) / (pow(d__1, be1) + 1.);
    d2 = d1 * cost2;
    ffs_(&alpha, &al2, &dal2, &f, &fa, &fs);
    d3 = pow(fa, xk2) / pow(*r__, be2);
    d4 = d3 * cost2;
    ffs_(&alpha, &al3, &dal3, &f, &fa, &fs);
    d__1 = *r__ / b3;
    d5 = pow(fs, xk3) * pow(alpha, xk4) / (pow(d__1, be3) + 1.)
	    ;
    d6 = d5 * cost2;
    ffs_(&gamma, &c_b66, &dg1, &f, &fa, &fs);
/* Computing 2nd power */
    d__1 = (alpha - al4) / dal4;
    fcc = d__1 * d__1 + 1.;
    d7 = 1. / fcc * fs;
    d8 = d7 / fcc;
    d9 = d8 / fcc;
    d10 = d9 / fcc;
/* Computing 2nd power */
    d__1 = (alpha - al5) / dal5;
    arg = d__1 * d__1 + 1.;
/* Computing 2nd power */
    d__1 = gamma / dg2;
    d11 = 1. / arg / (d__1 * d__1 + 1.);
    d12 = d11 / arg;
    d13 = d12 / arg;
    d14 = d13 / arg;
/* Computing 4th power */
    d__1 = *r__, d__1 *= d__1;
/* Computing 2nd power */
    d__2 = c1;
    d15 = 1. / (d__1 * d__1 + d__2 * d__2);
/* Computing 4th power */
    d__1 = *r__, d__1 *= d__1;
/* Computing 2nd power */
    d__2 = c2;
    d16 = cost2 / (d__1 * d__1 + d__2 * d__2);
/* Computing 2nd power */
    d__1 = cost2;
/* Computing 4th power */
    d__2 = *r__, d__2 *= d__2;
/* Computing 2nd power */
    d__3 = c3;
    d17 = d__1 * d__1 / (d__2 * d__2 + d__3 * d__3);

    ret_val = a1 * d1 + a2 * d2 + a3 * d3 + a4 * d4 + a5 * d5 + a6 * d6 + a7 *
	     d7 + a8 * d8 + a9 * d9 + a10 * d10 + a11 * d11 + a12 * d12 + a13 
	    * d13 + a14 * d14 + a15 * d15 + a16 * d16 + a17 * d17;

    return ret_val;
} /* bt_prc_q__ */


/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/
/* Subroutine */ int ffs_(double *a, double *a0, double *da, 
	double *f, double *fa, double *fs)
{
    /* System generated locals */
    double d__1, d__2;

    /* Builtin functions */
    double sqrt(double);

    /* Local variables */
    static double sq1, sq2;

/* Computing 2nd power */
    d__1 = *a + *a0;
/* Computing 2nd power */
    d__2 = *da;
    sq1 = sqrt(d__1 * d__1 + d__2 * d__2);
/* Computing 2nd power */
    d__1 = *a - *a0;
/* Computing 2nd power */
    d__2 = *da;
    sq2 = sqrt(d__1 * d__1 + d__2 * d__2);
    *fa = 2. / (sq1 + sq2);
    *f = *fa * *a;
    *fs = (sq1 + sq2) * .5 / (sq1 * sq2) * (1. - *f * *f);
    return 0;
} /* ffs_ */


/*|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
|||*/


/* Subroutine */ int rc_shield__(double *a, double *ps, double *
	x_sc__, double *x, double *y, double *z__, double *bx,
	 double *by, double *bz)
{
    /* System generated locals */
    double d__1, d__2;

    /* Builtin functions */
    double cos(double), sin(double), sqrt(double), exp(double)
	    ;

    /* Local variables */
    static double cypi, cyqi, czrk, czsk, sypi, syqi, sqpr, sqqs, szrk, 
	    szsk;
    static int i__, k, l, m, n;
    static double p, q, r__, s, x1, x2, z1, z2, fac_sc__;
    static int nn;
    static double fx, gx, gy, gz, fy, fz, hx, hy, hz, ct1, ct2, st1, st2, 
	    cps, epr, eqs, hxr, hzr, sps, pst1, s3ps, pst2;


/*   COMPUTES THE COMPONENTS OF THE SHIELDING FIELD FOR THE RING CURRENT 
*/
/*       (EITHER PARTIAL OR AXISYMMETRICAL) */
/*  INPUT:   A - AN ARRAY CONTAINING THE HARMONIC COEFFICIENTS AND NONLINE
AR PARAMETERS*/
/*            PS - GEODIPOLE TILT ANGLE IN RADIANS */
/*           X_SC - SCALING FACTOR ( X_SC>1 AND X_SC<1 CORRESPOND TO LARGE
R/SMALLER*/
/*                  RING CURRENT, RESP.) */
/*            X,Y,Z - POSITION IN RE (GSM COORDS) */
/*   OUTPUT:  BX,BY,BZ - SHIELDING FIELD COMPONENTS (GSM) */


    /* Parameter adjustments */
    --a;

    /* Function Body */
/* Computing 3rd power */
    d__1 = *x_sc__ + 1., d__2 = d__1;
    fac_sc__ = d__2 * (d__1 * d__1);

    cps = cos(*ps);
    sps = sin(*ps);
    s3ps = cps * 2.;

    pst1 = *ps * a[85];
    pst2 = *ps * a[86];
    st1 = sin(pst1);
    ct1 = cos(pst1);
    st2 = sin(pst2);
    ct2 = cos(pst2);
    x1 = *x * ct1 - *z__ * st1;
    z1 = *x * st1 + *z__ * ct1;
    x2 = *x * ct2 - *z__ * st2;
    z2 = *x * st2 + *z__ * ct2;

    l = 0;
    gx = 0.;
    gy = 0.;
    gz = 0.;

    for (m = 1; m <= 2; ++m) {
/*                          AND M=2 IS FOR THE SECOND SUM ("PARALL." S
YMMETRY)*/
/*    M=1 IS FOR THE 1ST SUM ("PERP." SYMMETRY) */
	for (i__ = 1; i__ <= 3; ++i__) {
	    p = a[i__ + 72];
	    q = a[i__ + 78];
	    cypi = cos(*y / p);
	    cyqi = cos(*y / q);
	    sypi = sin(*y / p);
	    syqi = sin(*y / q);

	    for (k = 1; k <= 3; ++k) {
		r__ = a[k + 75];
		s = a[k + 81];
		szrk = sin(z1 / r__);
		czsk = cos(z2 / s);
		czrk = cos(z1 / r__);
		szsk = sin(z2 / s);
/* Computing 2nd power */
		d__1 = p;
/* Computing 2nd power */
		d__2 = r__;
		sqpr = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
/* Computing 2nd power */
		d__1 = q;
/* Computing 2nd power */
		d__2 = s;
		sqqs = sqrt(1. / (d__1 * d__1) + 1. / (d__2 * d__2));
		epr = exp(x1 * sqpr);
		eqs = exp(x2 * sqqs);

		for (n = 1; n <= 2; ++n) {
/*                                AND N=2 IS FOR THE SECON
D ONE */
/* N=1 IS FOR THE FIRST PART OF EACH COEFFI */
		    for (nn = 1; nn <= 2; ++nn) {
/*                                        TO TAKE INTO
 ACCOUNT THE SCALE FACTOR DEPENDENCE*/
/*   NN = 1,2 FURTHER SPLITS THE COEFFICI */
			if (m == 1) {
			    fx = -sqpr * epr * cypi * szrk * fac_sc__;
			    fy = epr * sypi * szrk / p * fac_sc__;
			    fz = -epr * cypi * czrk / r__ * fac_sc__;
			    if (n == 1) {
				if (nn == 1) {
				    hx = fx;
				    hy = fy;
				    hz = fz;
				} else {
				    hx = fx * *x_sc__;
				    hy = fy * *x_sc__;
				    hz = fz * *x_sc__;
				}
			    } else {
				if (nn == 1) {
				    hx = fx * cps;
				    hy = fy * cps;
				    hz = fz * cps;
				} else {
				    hx = fx * cps * *x_sc__;
				    hy = fy * cps * *x_sc__;
				    hz = fz * cps * *x_sc__;
				}
			    }
			} else {
/*   M.EQ.2 */
			    fx = -sps * sqqs * eqs * cyqi * czsk * fac_sc__;
			    fy = sps / q * eqs * syqi * czsk * fac_sc__;
			    fz = sps / s * eqs * cyqi * szsk * fac_sc__;
			    if (n == 1) {
				if (nn == 1) {
				    hx = fx;
				    hy = fy;
				    hz = fz;
				} else {
				    hx = fx * *x_sc__;
				    hy = fy * *x_sc__;
				    hz = fz * *x_sc__;
				}
			    } else {
				if (nn == 1) {
				    hx = fx * s3ps;
				    hy = fy * s3ps;
				    hz = fz * s3ps;
				} else {
				    hx = fx * s3ps * *x_sc__;
				    hy = fy * s3ps * *x_sc__;
				    hz = fz * s3ps * *x_sc__;
				}
			    }
			}
			++l;
			if (m == 1) {
			    hxr = hx * ct1 + hz * st1;
			    hzr = -hx * st1 + hz * ct1;
			} else {
			    hxr = hx * ct2 + hz * st2;
			    hzr = -hx * st2 + hz * ct2;
			}
			gx += hxr * a[l];
			gy += hy * a[l];
/* L5: */
			gz += hzr * a[l];
		    }
/* L4: */
		}
/* L3: */
	    }
/* L2: */
	}
/* L1: */
    }
    *bx = gx;
    *by = gy;
    *bz = gz;
    return 0;
} /* rc_shield__ */


/*===========================================================================
*/

/* Subroutine */ int dipole_dup(double *ps, double *x, double *y, 
	double *z__, double *bx, double *by, double *bz)
{
    /* Initialized data */

    static int m = 0;
    static double psi = 5.;

    /* System generated locals */
    double d__1, d__2;

    /* Builtin functions */
    double sin(double), cos(double), sqrt(double);

    /* Local variables */
    static double p, q, t, u, v, cps, sps;


/*    THIS IS A DOUBLE PRECISION ROUTINE, OTHERWISE IDENTICAL TO THE S/R D
IP OF GEOPACK*/

/*    CALCULATES GSM COMPONENTS OF A GEODIPOLE FIELD WITH THE DIPOLE MOMEN
T*/
/*     CORRESPONDING TO THE EPOCH OF 2000. */

/* ------INPUT PARAMETERS: */
/*       PS - GEODIPOLE TILT ANGLE IN RADIANS, */
/*       X,Y,Z - GSM COORDINATES IN RE (1 RE = 6371.2 km) */

/* ----OUTPUT PARAMETERS: */
/*     BX,BY,BZ - FIELD COMPONENTS IN GSM SYSTEM, IN NANOTESLA. */

/*   LAST MODIFICATION: JAN. 5, 2001. THE VALUE OF THE DIPOLE MOMENT WAS U
PDATED TO 2000.*/
/*     AND A "SAVE" STATEMENT HAS BEEN ADDED, TO AVOID POTENTIAL PROBLEMS 
WITH SOME*/
/*      FORTRAN COMPILERS */

/*    WRITTEN BY: N. A. TSYGANENKO */

    if (m == 1 && (d__1 = *ps - psi, abs(d__1)) < 1e-5) {
	goto L1;
    }
/*   THIS IS TO AVOID */
    sps = sin(*ps);
/*   OF SIN(PS) AND C */
    cps = cos(*ps);
/*   REMAINS UNCHANGE */
    psi = *ps;
    m = 1;
L1:
/* Computing 2nd power */
    d__1 = *x;
    p = d__1 * d__1;
/* Computing 2nd power */
    d__1 = *z__;
    u = d__1 * d__1;
    v = *z__ * 3. * *x;
/* Computing 2nd power */
    d__1 = *y;
    t = d__1 * d__1;
/* Computing 5th power */
    d__1 = sqrt(p + t + u), d__2 = d__1, d__1 *= d__1;
    q = 30115. / (d__2 * (d__1 * d__1));
    *bx = q * ((t + u - p * 2.) * sps - v * cps);
    *by = *y * -3. * q * (*x * sps + *z__ * cps);
    *bz = q * ((p + t - u * 2.) * cps - v * sps);
    return 0;
} /* dipole_dup */

/*  -------------------------- OVT C Interface ---------------------------*/

/* Interface to main tsyganenko 2001 subroutine t01_01__
* Input:
*  gsm[3] - point coordinate in GSM Coordinate System
*  ps - dipole tilt angle in RAD
*  pdyn - solar wind dynamic pressure [nPa]
*  dst - DstIndex [nTl]
*  byimf, bzimf - y and z components of IMF [nTl]
*  g1, g2 - indexes, (SEE TSYGANENKO [2001] FOR AN EXACT DEFINITION OF THESE
*           TWO INDICES)
* Output:
*  bv[3] - Magnetic field in GSM coordinate system.
*/

int tsyganenko2001_(float *gsm, float *ps, float *pdyn, float *dst,
	 float *byimf, float *bzimf, float *g1, float *g2, float *bv)
{
    /*extern  Subroutine int t01_01__(int *, float *, float *, float *, 
	float *, float *, float *, float *, float *) */
	
    static float parmod[10];
    int iopt = 0; /* dummy param */
    
    /* Parameter adjustments */
    --bv;
    --gsm;

    /* Function Body */
    parmod[0] = *pdyn;
    parmod[1] = *dst;
    parmod[2] = *byimf;
    parmod[3] = *bzimf;
    parmod[4] = *g1;
    parmod[5] = *g2;
    t01_01__(&iopt, parmod, ps, &gsm[1], &gsm[2], &gsm[3], &bv[1], &bv[2], &bv[3]);
    return 0;
} /* tsyganenko2001_ */


/*  ------------------------ OVT JNI Interface ---------------------------*/


JNIEXPORT void JNICALL Java_ovt_mag_model_Tsyganenko2001_tsyganenko2001JNI
  (env, obj, jgsm, jtilt, jswp, jdst, jByImf, jBzImf, jG1, jG2, jBv)
JNIEnv *env;
jobject obj;
jdoubleArray jgsm, jBv;
jdouble jtilt, jswp, jdst, jByImf, jBzImf, jG1, jG2;
{
   jfloat GSMf[3],PSf=jtilt,PDYNf=jswp,DSTf=jdst,BYIMFf=jByImf,
   	BZIMFf=jBzImf, G1=jG1, G2=jG2, BVf[3];
   jint i;
   jdouble *gsm=(*env)->GetDoubleArrayElements(env, jgsm, 0);
   jdouble *Bv=(*env)->GetDoubleArrayElements(env, jBv, 0);
   
   for(i=0;i<3;++i)    /* double2float*/
      GSMf[i]=gsm[i];

   tsyganenko2001_(GSMf,&PSf,&PDYNf,&DSTf,&BYIMFf,&BZIMFf,&G1,&G2,BVf);

   for(i=0;i<3;++i)    /* double2float*/
      Bv[i]=BVf[i];

   (*env)->ReleaseDoubleArrayElements(env, jgsm, gsm, 0);
   (*env)->ReleaseDoubleArrayElements(env, jBv, Bv, 0);
}


/*#include<stdio.h>
void main()
{
   double gsm[3]={15.2,30.4,20.3},bv[3],
   ps=0.089,pdyn=5.0,dst=-20.3,byimf=-3,bzimf=1.5;
   
   tsyganenko96JNI(gsm,ps,pdyn,dst,byimf,bzimf,bv);
   printf("%f %f %f\n",(float)bv[0],(float)bv[1],(float)bv[2]);
}*/
