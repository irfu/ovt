/* CGMC2.F -- translated by f2c (version 19950110).
   You must link the resulting object file with the libraries:
	-lf2c -lm   (in that order)
*/

#include "f2c.h"

#ifdef __cplusplus
extern "C" {
#endif

/* Common Block Declarations */

struct {
    integer nm;
} nm_;

#define nm_1 nm_

struct {
    integer iyr;
} iyr_;

#define iyr_1 iyr_

struct {
    real ds3;
} a5_;

#define a5_1 a5_

struct {
    real g0, g1, h1;
} dmom_;

#define dmom_1 dmom_

union {
    struct {
	real st0, ct0, sl0, cl0, ctcl, stcl, ctsl, stsl, ab[19];
	integer k, iy;
	real bb[8];
    } _1;
    struct {
	real st0, ct0, sl0, cl0, ctcl, stcl, ctsl, stsl, sfi, cfi, sps, cps, 
		shi, chi, hi, psi, xmut, a11, a21, a31, a12, a22, a32, a13,
		a23, a33, ds3;
	integer k, iy;
	real ba[8];
    } _2;
} c1_;

#define c1_1 (c1_._1)
#define c1_2 (c1_._2)

/* Table of constant values */

static integer c__1 = 1;
static integer c_n1 = -1;
static integer c__0 = 0;
static integer c__25 = 25;
static doublereal c_b79 = 360.;



/*this function added after f2c*/
void fill_commons(int nharm, int year){
  nm_1.nm = nharm;
  iyr_1.iyr = year;
}



/* This version is prepared to be translated from Fortran to C with use */
/* of f2c on Linux. */

/* Main program */ MAIN__()
{
    /* System generated locals */
    integer i__1, i__2;

    /* Local variables */
    static real clat, glat;
    static integer ilat;
    static real clon;
    static integer nlat;
    static real glon;
    static integer ilon, nlon;
    static real rlat1, rlon1, rh_bp__;
    static integer idlat, idlon;
    static real hi, re;
    extern /* Subroutine */ int geocor_(), corgeo_();
    static real dla1, dla2, dlo1, dlo2, pmi1, pmi2;

/*      write(*,'(A\)') ' Enter number of harmonics in IGRF (eg. 10) ? '
*/
/*      read(*,*) NM */
    nm_1.nm = 10;
/*      write(*,'(A\)') ' Enter year (eg. 1999) ? ' */
/*      read(*,*) IYR */
    iyr_1.iyr = 2000;
/*      write(*,'(A\)') */
/*     + ' Enter altitude in km above the Earth''s surface (eg. 300.0) ? '
 */
/*      read(*,*) HI */
    hi = (float)300.;
/*      write(*,'(A\)') ' Enter latitude step (eg. 5) ? ' */
/*      read(*,*) idlat */
    idlat = 5;
/*      write(*,'(A\)') ' Enter longitude step (eg. 10) ? ' */
/*      read(*,*) idlon */
    idlon = 10;
    re = (float)6371.2;
    rh_bp__ = (re + hi) / re;
    nlat = 180 / idlat + 1;
    nlon = 360 / idlon + 1;
    rlat1 = (float)-90.;
    rlon1 = (float)0.;
/*      open(5,file='geo2cor.dat') !,status='new') */
/*      write(5,51) rlat1,nlat,idlat, rlon1,nlon,idlon */
    i__1 = nlat;
    for (ilat = 1; ilat <= i__1; ++ilat) {
	glat = (ilat - 1) * idlat + rlat1;
	i__2 = nlon;
	for (ilon = 1; ilon <= i__2; ++ilon) {
	    glon = (ilon - 1) * idlon + rlon1;
	    geocor_(&glat, &glon, &rh_bp__, &dla1, &dlo1, &clat, &clon, &pmi1)
		    ;
/* 	  write(5,52) glat,glon,clat,clon */
	}
    }
/*      close(5) */
/*      open(5,file='cor2geo.dat') !,status='new') */
/*      write(5,51) rlat1,nlat,idlat, rlon1,nlon,idlon */
    i__1 = nlat;
    for (ilat = 1; ilat <= i__1; ++ilat) {
	clat = (ilat - 1) * idlat + rlat1;
	i__2 = nlon;
	for (ilon = 1; ilon <= i__2; ++ilon) {
	    clon = (ilon - 1) * idlon + rlon1;
	    corgeo_(&glat, &glon, &rh_bp__, &dla2, &dlo2, &clat, &clon, &pmi2)
		    ;
/* 	  write(5,52) clat,clon,glat,glon */
	}
    }
/*      close(5) */
/* 51    format(2(F5.0,I3,I3)) */
/* 52    format(2F5.0,2F8.2) */
} /* MAIN__ */

/*  ********************************************************************* */
/* Subroutine */ int corgeo_(sla, slo, rh, dla, dlo, cla, clo, pmi)
real *sla, *slo, *rh, *dla, *dlo, *cla, *clo, *pmi;
{
    /* System generated locals */
    real r__1, r__2, r__3;

    /* Builtin functions */
    double sin(), sqrt(), atan();

    /* Local variables */
    static real frac, scla;
    extern /* Subroutine */ int shag_();
    static real clas, gxla, clos, gtet, r, x, y, z, r0, r1, x1, y1, z1;
    static integer jc, ng;
    static real pf, ds, th, rl;
    extern /* Subroutine */ int geomag_();
    static real rm, sn, xm, ym, zm;
    extern /* Subroutine */ int geocor_(), sphcar_();
    static real dr0, dr1, sn2, aa10, saa, dr10, col, rfi, gth, dls, saq, rlo, 
	    pms;

/*  Calculates geocentric coordinates from corrected geomagnetic ones. */
/*  The code is written by Vladimir Popov and Vladimir Papitashvili */
/*  in mid-1980s; revised by V. Papitashvili in February 1999 */
/*  This takes care if CLA is a dummy value (e.g., 999.99) */
    jc = 0;
    if (dabs(*cla) < (float)1.) {
/*          write(*,*) */
/*     +'WARNING - No calculations within +/-1 degree near CGM equator
' */
	jc = 1;
    }
    if (*cla > (float)999. || jc == 1) {
	*sla = (float)999.99;
	*slo = (float)999.99;
	*dla = (float)999.99;
	*dlo = (float)999.99;
	*pmi = (float)999.99;
	return 0;
    }
    ng = nm_1.nm;
    col = (float)90. - *cla;
    r = (float)10.;
    r1 = r;
    r0 = r;
    col *= (float).017453293;
    rlo = *clo * (float).017453293;
    sn = sin(col);
    sn2 = sn * sn;
/*  The CGM latitude should be at least 0.01 deg. away of the CGM pole */
    if (sn2 < (float)3e-9) {
	sn2 = (float)3e-9;
    }
/*      RFI = 1./SN2 */
    rfi = *rh / sn2;
    *pmi = rfi;
    if (*pmi > (float)99.999) {
	*pmi = (float)999.99;
    }
    aa10 = r / rfi;
/*  RFI = R if COL = 90 deg. */
    if (rfi <= r) {
	goto L1;
    }
    saa = aa10 / ((float)1. - aa10);
    saq = sqrt(saa);
    scla = atan(saq);
    if (*cla < (float)0.) {
	scla = (float)3.14159265359 - scla;
    }
    goto L3;
L1:
    scla = (float)1.57079632679;
    r0 = rfi;
L3:
    sphcar_(&r0, &scla, &rlo, &xm, &ym, &zm, &c__1);
    geomag_(&x, &y, &z, &xm, &ym, &zm, &c_n1, &iyr_1.iyr);
    rl = r0;
    frac = (float)-.03 / ((float)3. / (rl - (float).6) + (float)1.);
    if (*cla < (float)0.) {
	frac = -(doublereal)frac;
    }
    r = r0;
L5:
    ds = r * frac;
    nm_1.nm = (float)9. / r + (float)1. + (float).5;
    shag_(&x, &y, &z, &ds);
/* Computing 2nd power */
    r__1 = x;
/* Computing 2nd power */
    r__2 = y;
/* Computing 2nd power */
    r__3 = z;
    r = sqrt(r__1 * r__1 + r__2 * r__2 + r__3 * r__3);
    if (r <= *rh) {
	goto L7;
    }
    r1 = r;
    x1 = x;
    y1 = y;
    z1 = z;
    goto L5;
/*  Define intersection with the start surface */
L7:
    dr1 = (r__1 = *rh - r1, dabs(r__1));
    dr0 = (r__1 = *rh - r, dabs(r__1));
    dr10 = dr1 + dr0;
    if (dr10 != (float)0.) {
	ds *= dr1 / dr10;
	shag_(&x1, &y1, &z1, &ds);
    }
    sphcar_(&r, &gtet, &gxla, &x1, &y1, &z1, &c_n1);
    gth = gtet * (float)57.2957751;
    *slo = gxla * (float)57.2957751;
    *sla = (float)90. - gth;
    geomag_(&x1, &y1, &z1, &xm, &ym, &zm, &c__1, &iyr_1.iyr);
    sphcar_(&rm, &th, &pf, &xm, &ym, &zm, &c_n1);
    *dlo = pf * (float)57.2957751;
    *dla = (float)90. - th * (float)57.2957751;
    nm_1.nm = ng;
/*  Because CORGEO cannot check if the CGM --> GEO transformation is */
/*  performed correctly in the equatorial area (that is, where the IGRF */
/*  field line may never cross the dipole equatorial plane). Therefore, */
/*  the backward check is required for geocentric latitudes lower than */
/*  30 degrees (see the paper referenced in GEOLOW) */
    if (dabs(*sla) < (float)30. || dabs(*cla) < (float)30.) {
	geocor_(sla, slo, rh, &dls, &dls, &clas, &clos, &pms);
/* BP modification */
/*      IF(CLAS.GT.999.) CALL GEOLOW(SLA,SLO,RH,CLAS,CLOS,RBM,SLAC,SLO
C) */
/*       IF(ABS(ABS(CLA)-ABS(CLAS)).GE.1.) THEN */
/*          write(*,*) */
/*     +'WARNING - Selected CGM_Lat.=',CLA,' is located in the ', */
/*     +'near CGM equator area where the latter cannot be defined' */
	*sla = (float)999.99;
	*slo = (float)999.99;
	*pmi = (float)999.99;
/*        ENDIF */
    }
    return 0;
} /* corgeo_ */

/*  ********************************************************************* */
/*  ********************************************************************* */
/* Subroutine */ int geocor_(sla, slo, rh, dla, dlo, cla, clo, pmi)
real *sla, *slo, *rh, *dla, *dlo, *cla, *clo, *pmi;
{
    /* System generated locals */
    real r__1, r__2;

    /* Builtin functions */
    double sin(), sqrt(), atan();

    /* Local variables */
    static real frac;
    extern /* Subroutine */ int shag_();
    static real gxla, ssla, gtet, c, r, s, x, y, z, r1, x1, y1, z1;
    static integer ng;
    static real pf, ds, th, rl;
    extern /* Subroutine */ int geomag_();
    static real rm, sn, xm, ym, zm, st;
    extern /* Subroutine */ int sphcar_();
    static real dcl, dco, hhh, col, rrh, rlo, rzm, szm;

/*  Calculates corrected geomagnetic coordinates from geocentric ones */
/*  The code is written by Vladimir Popov and Vladimir Papitashvili */
/*  in mid-1980s; revised by V. Papitashvili in February 1999 */
/*  This takes care if SLA is a dummy value (e.g., 999.99) */
    if (*sla > (float)999.) {
	*cla = (float)999.99;
	*clo = (float)999.99;
	*dla = (float)999.99;
	*dlo = (float)999.99;
	*pmi = (float)999.99;
	return 0;
    }
    ng = nm_1.nm;
    col = (float)90. - *sla;
    r = *rh;
    r1 = r;
    col *= (float).017453293;
    rlo = *slo * (float).017453293;
    sphcar_(&r, &col, &rlo, &x, &y, &z, &c__1);
    geomag_(&x, &y, &z, &xm, &ym, &zm, &c__1, &iyr_1.iyr);
    sphcar_(&rm, &th, &pf, &xm, &ym, &zm, &c_n1);
    szm = zm;
    *dlo = pf * (float)57.2957751;
    dco = th * (float)57.2957751;
    *dla = (float)90. - dco;
/* Computing 2nd power */
    r__1 = sin(th);
    rl = r / (r__1 * r__1);
    frac = (float).03 / ((float)3. / (rl - (float).6) + (float)1.);
    if (szm < (float)0.) {
	frac = -(doublereal)frac;
    }
/*  Error to determine the dipole equtorial plane: aprox. 0.5 arc min */
    hhh = (float)1.571e-4;
/*  Trace the IGRF magnetic field line to the dipole equatorial plane */
L1:
    ds = r * frac;
L3:
    nm_1.nm = (float)9. / r + (float)1. + (float).5;
    r1 = r;
    x1 = x;
    y1 = y;
    z1 = z;
    shag_(&x, &y, &z, &ds);
    geomag_(&x, &y, &z, &xm, &ym, &zm, &c__1, &iyr_1.iyr);
    sphcar_(&r, &c, &s, &xm, &ym, &zm, &c_n1);
/*  As tracing goes above (RH+10_Re), use the dipole field line */
    if (r > *rh + (float)10.) {
	goto L9;
    }
/*  If the field line returns to the start surface without crossing the */
/*  dipole equatorial plane, no CGM coordinates can be calculated */
    if (r <= *rh) {
	goto L11;
    }
    dcl = c - (float)1.5707963268;
    if (dabs(dcl) <= hhh) {
	goto L9;
    }
    rzm = zm;
    if (szm > (float)0. && rzm > (float)0.) {
	goto L1;
    }
    if (szm < (float)0. && rzm < (float)0.) {
	goto L1;
    }
    r = r1;
    x = x1;
    y = y1;
    z = z1;
    ds /= (float)2.;
    goto L3;
L9:
    geomag_(&x, &y, &z, &xm, &ym, &zm, &c__1, &iyr_1.iyr);
    sphcar_(&r, &gtet, &gxla, &xm, &ym, &zm, &c_n1);
    st = (r__1 = sin(gtet), dabs(r__1));
/* Computing 2nd power */
    r__2 = st;
    rrh = (r__1 = *rh / (r - *rh * (r__2 * r__2)), dabs(r__1));
    *cla = (float)1.5707963 - atan(st * sqrt(rrh));
    *cla *= (float)57.2957751;
    *clo = gxla * (float)57.2957751;
    if (szm < (float)0.) {
	*cla = -(doublereal)(*cla);
    }
    ssla = (float)90. - *cla;
    ssla *= (float).017453293;
    sn = sin(ssla);
/*       PMI = 1/(SN*SN) */
    *pmi = *rh / (sn * sn);
    goto L13;
L11:
    *cla = (float)999.99;
    *clo = (float)999.99;
    *pmi = (float)999.99;
L13:
    nm_1.nm = ng;
    return 0;
} /* geocor_ */

/*  ********************************************************************* */
/*  ********************************************************************* */
/* Subroutine */ int shag_(x, y, z, ds)
real *x, *y, *z, *ds;
{
    /* System generated locals */
    real r__1, r__2, r__3;

    /* Local variables */
    extern /* Subroutine */ int right_();
    static real r11, r12, r13, r21, r22, r23, r31, r32, r33, r41, r42, r43, 
	    r51, r52, r53;

/*  Similar to SUBR STEP from GEOPACK-1996 but SHAG takes into account */
/*  only internal sources */
/*  The code is re-written from Tsyganenko's subroutine STEP by */
/*  Natalia and Vladimir Papitashvili in mid-1980s */
    a5_1.ds3 = -(doublereal)(*ds) / (float)3.;
    right_(x, y, z, &r11, &r12, &r13);
    r__1 = *x + r11;
    r__2 = *y + r12;
    r__3 = *z + r13;
    right_(&r__1, &r__2, &r__3, &r21, &r22, &r23);
    r__1 = *x + (r11 + r21) * (float).5;
    r__2 = *y + (r12 + r22) * (float).5;
    r__3 = *z + (r13 + r23) * (float).5;
    right_(&r__1, &r__2, &r__3, &r31, &r32, &r33);
    r__1 = *x + (r11 + r31 * (float)3.) * (float).375;
    r__2 = *y + (r12 + r32 * (float)3.) * (float).375;
    r__3 = *z + (r13 + r33 * (float)3.) * (float).375;
    right_(&r__1, &r__2, &r__3, &r41, &r42, &r43);
    r__1 = *x + (r11 - r31 * (float)3. + r41 * (float)4.) * (float)1.5;
    r__2 = *y + (r12 - r32 * (float)3. + r42 * (float)4.) * (float)1.5;
    r__3 = *z + (r13 - r33 * (float)3. + r43 * (float)4.) * (float)1.5;
    right_(&r__1, &r__2, &r__3, &r51, &r52, &r53);
    *x += (r11 + r41 * (float)4. + r51) * (float).5;
    *y += (r12 + r42 * (float)4. + r52) * (float).5;
    *z += (r13 + r43 * (float)4. + r53) * (float).5;
    return 0;
} /* shag_ */

/*  ********************************************************************* */
/*  ********************************************************************* */
/* Subroutine */ int right_(x, y, z, r1, r2, r3)
real *x, *y, *z, *r1, *r2, *r3;
{
    /* System generated locals */
    real r__1, r__2, r__3;

    /* Builtin functions */
    double sqrt();

    /* Local variables */
    extern /* Subroutine */ int igrf_();
    static real b, f, r, t, bf, br, bt, bx, by, bz;
    extern /* Subroutine */ int bspcar_(), sphcar_();

/*  Similar to SUBR RHAND from GEOPACK-1996 but RIGHT takes into account 
*/
/*  only internal sources */
/*  The code is re-written from Tsyganenko's subroutine RHAND */
/*  by Natalia and Vladimir Papitashvili in mid-1980s */
    sphcar_(&r, &t, &f, x, y, z, &c_n1);
    igrf_(&iyr_1.iyr, &nm_1.nm, &r, &t, &f, &br, &bt, &bf);
    bspcar_(&t, &f, &br, &bt, &bf, &bx, &by, &bz);
/* Computing 2nd power */
    r__1 = bx;
/* Computing 2nd power */
    r__2 = by;
/* Computing 2nd power */
    r__3 = bz;
    b = a5_1.ds3 / sqrt(r__1 * r__1 + r__2 * r__2 + r__3 * r__3);
    *r1 = bx * b;
    *r2 = by * b;
    *r3 = bz * b;
    return 0;
} /* right_ */

/*  ********************************************************************* */
/*  ********************************************************************* */
/* Subroutine */ int igrf_(iy, nm, r, t, f, br, bt, bf)
integer *iy, *nm;
real *r, *t, *f, *br, *bt, *bf;
{
    /* Initialized data */

    static real g1945[66] = { (float)0.,(float)-30594.,(float)-2285.,(float)
	    -1244.,(float)2990.,(float)1578.,(float)1282.,(float)-1834.,(
	    float)1255.,(float)913.,(float)944.,(float)776.,(float)544.,(
	    float)-421.,(float)304.,(float)-253.,(float)346.,(float)194.,(
	    float)-20.,(float)-142.,(float)-82.,(float)59.,(float)57.,(float)
	    6.,(float)-246.,(float)-25.,(float)21.,(float)-104.,(float)70.,(
	    float)-40.,(float)0.,(float)0.,(float)-29.,(float)-10.,(float)15.,
	    (float)29.,(float)13.,(float)7.,(float)-8.,(float)-5.,(float)9.,(
	    float)7.,(float)-10.,(float)7.,(float)2.,(float)5.,(float)-21.,(
	    float)1.,(float)-11.,(float)3.,(float)16.,(float)-3.,(float)-4.,(
	    float)-3.,(float)-4.,(float)-3.,(float)11.,(float)1.,(float)2.,(
	    float)-5.,(float)-1.,(float)8.,(float)-1.,(float)-3.,(float)5.,(
	    float)-2. };
    static real h1945[66] = { (float)0.,(float)0.,(float)5810.,(float)0.,(
	    float)-1702.,(float)477.,(float)0.,(float)-499.,(float)186.,(
	    float)-11.,(float)0.,(float)144.,(float)-276.,(float)-55.,(float)
	    -178.,(float)0.,(float)-12.,(float)95.,(float)-67.,(float)-119.,(
	    float)82.,(float)0.,(float)6.,(float)100.,(float)16.,(float)-9.,(
	    float)-16.,(float)-39.,(float)0.,(float)-45.,(float)-18.,(float)
	    2.,(float)6.,(float)28.,(float)-17.,(float)-22.,(float)0.,(float)
	    12.,(float)-21.,(float)-12.,(float)-7.,(float)2.,(float)18.,(
	    float)3.,(float)-11.,(float)0.,(float)-27.,(float)17.,(float)29.,(
	    float)-9.,(float)4.,(float)9.,(float)6.,(float)1.,(float)8.,(
	    float)0.,(float)5.,(float)1.,(float)-20.,(float)-1.,(float)-6.,(
	    float)6.,(float)-4.,(float)-2.,(float)0.,(float)-2. };
    static real g1950[66] = { (float)0.,(float)-30554.,(float)-2250.,(float)
	    -1341.,(float)2998.,(float)1576.,(float)1297.,(float)-1889.,(
	    float)1274.,(float)896.,(float)954.,(float)792.,(float)528.,(
	    float)-408.,(float)303.,(float)-240.,(float)349.,(float)211.,(
	    float)-20.,(float)-147.,(float)-76.,(float)54.,(float)57.,(float)
	    4.,(float)-247.,(float)-16.,(float)12.,(float)-105.,(float)65.,(
	    float)-55.,(float)2.,(float)1.,(float)-40.,(float)-7.,(float)5.,(
	    float)19.,(float)22.,(float)15.,(float)-4.,(float)-1.,(float)11.,(
	    float)15.,(float)-13.,(float)5.,(float)-1.,(float)3.,(float)-7.,(
	    float)-1.,(float)-25.,(float)10.,(float)5.,(float)-5.,(float)-2.,(
	    float)3.,(float)8.,(float)-8.,(float)4.,(float)-1.,(float)13.,(
	    float)-4.,(float)4.,(float)12.,(float)3.,(float)2.,(float)10.,(
	    float)3. };
    static real h1950[66] = { (float)0.,(float)0.,(float)5815.,(float)0.,(
	    float)-1810.,(float)381.,(float)0.,(float)-476.,(float)206.,(
	    float)-46.,(float)0.,(float)136.,(float)-278.,(float)-37.,(float)
	    -210.,(float)0.,(float)3.,(float)103.,(float)-87.,(float)-122.,(
	    float)80.,(float)0.,(float)-1.,(float)99.,(float)33.,(float)-12.,(
	    float)-12.,(float)-30.,(float)0.,(float)-35.,(float)-17.,(float)
	    0.,(float)10.,(float)36.,(float)-18.,(float)-16.,(float)0.,(float)
	    5.,(float)-22.,(float)0.,(float)-21.,(float)-8.,(float)17.,(float)
	    -4.,(float)-17.,(float)0.,(float)-24.,(float)19.,(float)12.,(
	    float)2.,(float)2.,(float)8.,(float)8.,(float)-11.,(float)-7.,(
	    float)0.,(float)13.,(float)-2.,(float)-10.,(float)2.,(float)-3.,(
	    float)6.,(float)-3.,(float)6.,(float)11.,(float)8. };
    static real g1955[66] = { (float)0.,(float)-30500.,(float)-2215.,(float)
	    -1440.,(float)3003.,(float)1581.,(float)1302.,(float)-1944.,(
	    float)1288.,(float)882.,(float)958.,(float)796.,(float)510.,(
	    float)-397.,(float)290.,(float)-229.,(float)360.,(float)230.,(
	    float)-23.,(float)-152.,(float)-69.,(float)47.,(float)57.,(float)
	    3.,(float)-247.,(float)-8.,(float)7.,(float)-107.,(float)65.,(
	    float)-56.,(float)2.,(float)10.,(float)-32.,(float)-11.,(float)9.,
	    (float)18.,(float)11.,(float)9.,(float)-6.,(float)-14.,(float)6.,(
	    float)10.,(float)-7.,(float)6.,(float)9.,(float)4.,(float)9.,(
	    float)-4.,(float)-5.,(float)2.,(float)4.,(float)1.,(float)2.,(
	    float)2.,(float)5.,(float)-3.,(float)-5.,(float)-1.,(float)2.,(
	    float)-3.,(float)7.,(float)4.,(float)-2.,(float)6.,(float)-2.,(
	    float)0. };
    static real h1955[66] = { (float)0.,(float)0.,(float)5820.,(float)0.,(
	    float)-1898.,(float)291.,(float)0.,(float)-462.,(float)216.,(
	    float)-83.,(float)0.,(float)133.,(float)-274.,(float)-23.,(float)
	    -230.,(float)0.,(float)15.,(float)110.,(float)-98.,(float)-121.,(
	    float)78.,(float)0.,(float)-9.,(float)96.,(float)48.,(float)-16.,(
	    float)-12.,(float)-24.,(float)0.,(float)-50.,(float)-24.,(float)
	    -4.,(float)8.,(float)28.,(float)-20.,(float)-18.,(float)0.,(float)
	    10.,(float)-15.,(float)5.,(float)-23.,(float)3.,(float)23.,(float)
	    -4.,(float)-13.,(float)0.,(float)-11.,(float)12.,(float)7.,(float)
	    6.,(float)-2.,(float)10.,(float)7.,(float)-6.,(float)5.,(float)0.,
	    (float)-4.,(float)0.,(float)-8.,(float)-2.,(float)-4.,(float)1.,(
	    float)-3.,(float)7.,(float)-1.,(float)-3. };
    static real g1960[66] = { (float)0.,(float)-30421.,(float)-2169.,(float)
	    -1555.,(float)3002.,(float)1590.,(float)1302.,(float)-1992.,(
	    float)1289.,(float)878.,(float)957.,(float)800.,(float)504.,(
	    float)-394.,(float)269.,(float)-222.,(float)362.,(float)242.,(
	    float)-26.,(float)-156.,(float)-63.,(float)46.,(float)58.,(float)
	    1.,(float)-237.,(float)-1.,(float)-2.,(float)-113.,(float)67.,(
	    float)-56.,(float)5.,(float)15.,(float)-32.,(float)-7.,(float)17.,
	    (float)8.,(float)15.,(float)6.,(float)-4.,(float)-11.,(float)2.,(
	    float)10.,(float)-5.,(float)10.,(float)8.,(float)4.,(float)6.,(
	    float)0.,(float)-9.,(float)1.,(float)4.,(float)-1.,(float)-2.,(
	    float)3.,(float)-1.,(float)1.,(float)-3.,(float)4.,(float)0.,(
	    float)-1.,(float)4.,(float)6.,(float)1.,(float)-1.,(float)2.,(
	    float)0. };
    static real h1960[66] = { (float)0.,(float)0.,(float)5791.,(float)0.,(
	    float)-1967.,(float)206.,(float)0.,(float)-414.,(float)224.,(
	    float)-130.,(float)0.,(float)135.,(float)-278.,(float)3.,(float)
	    -255.,(float)0.,(float)16.,(float)125.,(float)-117.,(float)-114.,(
	    float)81.,(float)0.,(float)-10.,(float)99.,(float)60.,(float)-20.,
	    (float)-11.,(float)-17.,(float)0.,(float)-55.,(float)-28.,(float)
	    -6.,(float)7.,(float)23.,(float)-18.,(float)-17.,(float)0.,(float)
	    11.,(float)-14.,(float)7.,(float)-18.,(float)4.,(float)23.,(float)
	    1.,(float)-20.,(float)0.,(float)-18.,(float)12.,(float)2.,(float)
	    0.,(float)-3.,(float)9.,(float)8.,(float)0.,(float)5.,(float)0.,(
	    float)4.,(float)1.,(float)0.,(float)2.,(float)-5.,(float)1.,(
	    float)-1.,(float)6.,(float)0.,(float)-7. };
    static real g1965[66] = { (float)0.,(float)-30334.,(float)-2119.,(float)
	    -1662.,(float)2997.,(float)1594.,(float)1297.,(float)-2038.,(
	    float)1292.,(float)856.,(float)957.,(float)804.,(float)479.,(
	    float)-390.,(float)252.,(float)-219.,(float)358.,(float)254.,(
	    float)-31.,(float)-157.,(float)-62.,(float)45.,(float)61.,(float)
	    8.,(float)-228.,(float)4.,(float)1.,(float)-111.,(float)75.,(
	    float)-57.,(float)4.,(float)13.,(float)-26.,(float)-6.,(float)13.,
	    (float)1.,(float)13.,(float)5.,(float)-4.,(float)-14.,(float)0.,(
	    float)8.,(float)-1.,(float)11.,(float)4.,(float)8.,(float)10.,(
	    float)2.,(float)-13.,(float)10.,(float)-1.,(float)-1.,(float)5.,(
	    float)1.,(float)-2.,(float)-2.,(float)-3.,(float)2.,(float)-5.,(
	    float)-2.,(float)4.,(float)4.,(float)0.,(float)2.,(float)2.,(
	    float)0. };
    static real h1965[66] = { (float)0.,(float)0.,(float)5776.,(float)0.,(
	    float)-2016.,(float)114.,(float)0.,(float)-404.,(float)240.,(
	    float)-165.,(float)0.,(float)148.,(float)-269.,(float)13.,(float)
	    -269.,(float)0.,(float)19.,(float)128.,(float)-126.,(float)-97.,(
	    float)81.,(float)0.,(float)-11.,(float)100.,(float)68.,(float)
	    -32.,(float)-8.,(float)-7.,(float)0.,(float)-61.,(float)-27.,(
	    float)-2.,(float)6.,(float)26.,(float)-23.,(float)-12.,(float)0.,(
	    float)7.,(float)-12.,(float)9.,(float)-16.,(float)4.,(float)24.,(
	    float)-3.,(float)-17.,(float)0.,(float)-22.,(float)15.,(float)7.,(
	    float)-4.,(float)-5.,(float)10.,(float)10.,(float)-4.,(float)1.,(
	    float)0.,(float)2.,(float)1.,(float)2.,(float)6.,(float)-4.,(
	    float)0.,(float)-2.,(float)3.,(float)0.,(float)-6. };
    static real g1970[66] = { (float)0.,(float)-30220.,(float)-2068.,(float)
	    -1781.,(float)3e3,(float)1611.,(float)1287.,(float)-2091.,(float)
	    1278.,(float)838.,(float)952.,(float)800.,(float)461.,(float)
	    -395.,(float)234.,(float)-216.,(float)359.,(float)262.,(float)
	    -42.,(float)-160.,(float)-56.,(float)43.,(float)64.,(float)15.,(
	    float)-212.,(float)2.,(float)3.,(float)-112.,(float)72.,(float)
	    -57.,(float)1.,(float)14.,(float)-22.,(float)-2.,(float)13.,(
	    float)-2.,(float)14.,(float)6.,(float)-2.,(float)-13.,(float)-3.,(
	    float)5.,(float)0.,(float)11.,(float)3.,(float)8.,(float)10.,(
	    float)2.,(float)-12.,(float)10.,(float)-1.,(float)0.,(float)3.,(
	    float)1.,(float)-1.,(float)-3.,(float)-3.,(float)2.,(float)-5.,(
	    float)-1.,(float)6.,(float)4.,(float)1.,(float)0.,(float)3.,(
	    float)-1. };
    static real h1970[66] = { (float)0.,(float)0.,(float)5737.,(float)0.,(
	    float)-2047.,(float)25.,(float)0.,(float)-366.,(float)251.,(float)
	    -196.,(float)0.,(float)167.,(float)-266.,(float)26.,(float)-279.,(
	    float)0.,(float)26.,(float)139.,(float)-139.,(float)-91.,(float)
	    83.,(float)0.,(float)-12.,(float)100.,(float)72.,(float)-37.,(
	    float)-6.,(float)1.,(float)0.,(float)-70.,(float)-27.,(float)-4.,(
	    float)8.,(float)23.,(float)-23.,(float)-11.,(float)0.,(float)7.,(
	    float)-15.,(float)6.,(float)-17.,(float)6.,(float)21.,(float)-6.,(
	    float)-16.,(float)0.,(float)-21.,(float)16.,(float)6.,(float)-4.,(
	    float)-5.,(float)10.,(float)11.,(float)-2.,(float)1.,(float)0.,(
	    float)1.,(float)1.,(float)3.,(float)4.,(float)-4.,(float)0.,(
	    float)-1.,(float)3.,(float)1.,(float)-4. };
    static real g1975[66] = { (float)0.,(float)-30100.,(float)-2013.,(float)
	    -1902.,(float)3010.,(float)1632.,(float)1276.,(float)-2144.,(
	    float)1260.,(float)830.,(float)946.,(float)791.,(float)438.,(
	    float)-405.,(float)216.,(float)-218.,(float)356.,(float)264.,(
	    float)-59.,(float)-159.,(float)-49.,(float)45.,(float)66.,(float)
	    28.,(float)-198.,(float)1.,(float)6.,(float)-111.,(float)71.,(
	    float)-56.,(float)1.,(float)16.,(float)-14.,(float)0.,(float)12.,(
	    float)-5.,(float)14.,(float)6.,(float)-1.,(float)-12.,(float)-8.,(
	    float)4.,(float)0.,(float)10.,(float)1.,(float)7.,(float)10.,(
	    float)2.,(float)-12.,(float)10.,(float)-1.,(float)-1.,(float)4.,(
	    float)1.,(float)-2.,(float)-3.,(float)-3.,(float)2.,(float)-5.,(
	    float)-2.,(float)5.,(float)4.,(float)1.,(float)0.,(float)3.,(
	    float)-1. };
    static real h1975[66] = { (float)0.,(float)0.,(float)5675.,(float)0.,(
	    float)-2067.,(float)-68.,(float)0.,(float)-333.,(float)262.,(
	    float)-223.,(float)0.,(float)191.,(float)-265.,(float)39.,(float)
	    -288.,(float)0.,(float)31.,(float)148.,(float)-152.,(float)-83.,(
	    float)88.,(float)0.,(float)-13.,(float)99.,(float)75.,(float)-41.,
	    (float)-4.,(float)11.,(float)0.,(float)-77.,(float)-26.,(float)
	    -5.,(float)10.,(float)22.,(float)-23.,(float)-12.,(float)0.,(
	    float)6.,(float)-16.,(float)4.,(float)-19.,(float)6.,(float)18.,(
	    float)-10.,(float)-17.,(float)0.,(float)-21.,(float)16.,(float)7.,
	    (float)-4.,(float)-5.,(float)10.,(float)11.,(float)-3.,(float)1.,(
	    float)0.,(float)1.,(float)1.,(float)3.,(float)4.,(float)-4.,(
	    float)-1.,(float)-1.,(float)3.,(float)1.,(float)-5. };
    static real g1980[66] = { (float)0.,(float)-29992.,(float)-1956.,(float)
	    -1997.,(float)3027.,(float)1663.,(float)1281.,(float)-2180.,(
	    float)1251.,(float)833.,(float)938.,(float)782.,(float)398.,(
	    float)-419.,(float)199.,(float)-218.,(float)357.,(float)261.,(
	    float)-74.,(float)-162.,(float)-48.,(float)48.,(float)66.,(float)
	    42.,(float)-192.,(float)4.,(float)14.,(float)-108.,(float)72.,(
	    float)-59.,(float)2.,(float)21.,(float)-12.,(float)1.,(float)11.,(
	    float)-2.,(float)18.,(float)6.,(float)0.,(float)-11.,(float)-7.,(
	    float)4.,(float)3.,(float)6.,(float)-1.,(float)5.,(float)10.,(
	    float)1.,(float)-12.,(float)9.,(float)-3.,(float)-1.,(float)7.,(
	    float)2.,(float)-5.,(float)-4.,(float)-4.,(float)2.,(float)-5.,(
	    float)-2.,(float)5.,(float)3.,(float)1.,(float)2.,(float)3.,(
	    float)0. };
    static real h1980[66] = { (float)0.,(float)0.,(float)5604.,(float)0.,(
	    float)-2129.,(float)-200.,(float)0.,(float)-336.,(float)271.,(
	    float)-252.,(float)0.,(float)212.,(float)-257.,(float)53.,(float)
	    -297.,(float)0.,(float)46.,(float)150.,(float)-151.,(float)-78.,(
	    float)92.,(float)0.,(float)-15.,(float)93.,(float)71.,(float)-43.,
	    (float)-2.,(float)17.,(float)0.,(float)-82.,(float)-27.,(float)
	    -5.,(float)16.,(float)18.,(float)-23.,(float)-10.,(float)0.,(
	    float)7.,(float)-18.,(float)4.,(float)-22.,(float)9.,(float)16.,(
	    float)-13.,(float)-15.,(float)0.,(float)-21.,(float)16.,(float)9.,
	    (float)-5.,(float)-6.,(float)9.,(float)10.,(float)-6.,(float)2.,(
	    float)0.,(float)1.,(float)0.,(float)3.,(float)6.,(float)-4.,(
	    float)0.,(float)-1.,(float)4.,(float)0.,(float)-6. };
    static real g1985[66] = { (float)0.,(float)-29873.,(float)-1905.,(float)
	    -2072.,(float)3044.,(float)1687.,(float)1296.,(float)-2208.,(
	    float)1247.,(float)829.,(float)936.,(float)780.,(float)361.,(
	    float)-424.,(float)170.,(float)-214.,(float)355.,(float)253.,(
	    float)-93.,(float)-164.,(float)-46.,(float)53.,(float)65.,(float)
	    51.,(float)-185.,(float)4.,(float)16.,(float)-102.,(float)74.,(
	    float)-62.,(float)3.,(float)24.,(float)-6.,(float)4.,(float)10.,(
	    float)0.,(float)21.,(float)6.,(float)0.,(float)-11.,(float)-9.,(
	    float)4.,(float)4.,(float)4.,(float)-4.,(float)5.,(float)10.,(
	    float)1.,(float)-12.,(float)9.,(float)-3.,(float)-1.,(float)7.,(
	    float)1.,(float)-5.,(float)-4.,(float)-4.,(float)3.,(float)-5.,(
	    float)-2.,(float)5.,(float)3.,(float)1.,(float)2.,(float)3.,(
	    float)0. };
    static real h1985[66] = { (float)0.,(float)0.,(float)5500.,(float)0.,(
	    float)-2197.,(float)-306.,(float)0.,(float)-310.,(float)284.,(
	    float)-297.,(float)0.,(float)232.,(float)-249.,(float)69.,(float)
	    -297.,(float)0.,(float)47.,(float)150.,(float)-154.,(float)-75.,(
	    float)95.,(float)0.,(float)-16.,(float)88.,(float)69.,(float)-48.,
	    (float)-1.,(float)21.,(float)0.,(float)-83.,(float)-27.,(float)
	    -2.,(float)20.,(float)17.,(float)-23.,(float)-7.,(float)0.,(float)
	    8.,(float)-19.,(float)5.,(float)-23.,(float)11.,(float)14.,(float)
	    -15.,(float)-11.,(float)0.,(float)-21.,(float)15.,(float)9.,(
	    float)-6.,(float)-6.,(float)9.,(float)9.,(float)-7.,(float)2.,(
	    float)0.,(float)1.,(float)0.,(float)3.,(float)6.,(float)-4.,(
	    float)0.,(float)-1.,(float)4.,(float)0.,(float)-6. };
    static real g1990[66] = { (float)0.,(float)-29775.,(float)-1848.,(float)
	    -2131.,(float)3059.,(float)1686.,(float)1314.,(float)-2239.,(
	    float)1248.,(float)802.,(float)939.,(float)780.,(float)325.,(
	    float)-423.,(float)141.,(float)-214.,(float)353.,(float)245.,(
	    float)-109.,(float)-165.,(float)-36.,(float)61.,(float)65.,(float)
	    59.,(float)-178.,(float)3.,(float)18.,(float)-96.,(float)77.,(
	    float)-64.,(float)2.,(float)26.,(float)-1.,(float)5.,(float)9.,(
	    float)0.,(float)23.,(float)5.,(float)-1.,(float)-10.,(float)-12.,(
	    float)3.,(float)4.,(float)2.,(float)-6.,(float)4.,(float)9.,(
	    float)1.,(float)-12.,(float)9.,(float)-4.,(float)-2.,(float)7.,(
	    float)1.,(float)-6.,(float)-3.,(float)-4.,(float)2.,(float)-5.,(
	    float)-2.,(float)4.,(float)3.,(float)1.,(float)3.,(float)3.,(
	    float)0. };
    static real h1990[66] = { (float)0.,(float)0.,(float)5406.,(float)0.,(
	    float)-2279.,(float)-373.,(float)0.,(float)-284.,(float)293.,(
	    float)-352.,(float)0.,(float)247.,(float)-240.,(float)84.,(float)
	    -299.,(float)0.,(float)46.,(float)154.,(float)-153.,(float)-69.,(
	    float)97.,(float)0.,(float)-16.,(float)82.,(float)69.,(float)-52.,
	    (float)1.,(float)24.,(float)0.,(float)-80.,(float)-26.,(float)0.,(
	    float)21.,(float)17.,(float)-23.,(float)-4.,(float)0.,(float)10.,(
	    float)-19.,(float)6.,(float)-22.,(float)12.,(float)12.,(float)
	    -16.,(float)-10.,(float)0.,(float)-20.,(float)15.,(float)11.,(
	    float)-7.,(float)-7.,(float)9.,(float)8.,(float)-7.,(float)2.,(
	    float)0.,(float)2.,(float)1.,(float)3.,(float)6.,(float)-4.,(
	    float)0.,(float)-2.,(float)3.,(float)-1.,(float)-6. };
    static real g1995[66] = { (float)0.,(float)-29682.,(float)-1789.,(float)
	    -2197.,(float)3074.,(float)1685.,(float)1329.,(float)-2268.,(
	    float)1249.,(float)769.,(float)941.,(float)782.,(float)291.,(
	    float)-421.,(float)116.,(float)-210.,(float)352.,(float)237.,(
	    float)-122.,(float)-167.,(float)-26.,(float)66.,(float)64.,(float)
	    65.,(float)-172.,(float)2.,(float)17.,(float)-94.,(float)78.,(
	    float)-67.,(float)1.,(float)29.,(float)4.,(float)8.,(float)10.,(
	    float)-2.,(float)24.,(float)4.,(float)-1.,(float)-9.,(float)-14.,(
	    float)4.,(float)5.,(float)0.,(float)-7.,(float)4.,(float)9.,(
	    float)1.,(float)-12.,(float)9.,(float)-4.,(float)-2.,(float)7.,(
	    float)0.,(float)-6.,(float)-3.,(float)-4.,(float)2.,(float)-5.,(
	    float)-2.,(float)4.,(float)3.,(float)1.,(float)3.,(float)3.,(
	    float)0. };
    static real h1995[66] = { (float)0.,(float)0.,(float)5318.,(float)0.,(
	    float)-2356.,(float)-425.,(float)0.,(float)-263.,(float)302.,(
	    float)-406.,(float)0.,(float)262.,(float)-232.,(float)98.,(float)
	    -301.,(float)0.,(float)44.,(float)157.,(float)-152.,(float)-64.,(
	    float)99.,(float)0.,(float)-16.,(float)77.,(float)67.,(float)-57.,
	    (float)4.,(float)28.,(float)0.,(float)-77.,(float)-25.,(float)3.,(
	    float)22.,(float)16.,(float)-23.,(float)-3.,(float)0.,(float)12.,(
	    float)-20.,(float)7.,(float)-21.,(float)12.,(float)10.,(float)
	    -17.,(float)-10.,(float)0.,(float)-19.,(float)15.,(float)11.,(
	    float)-7.,(float)-7.,(float)9.,(float)7.,(float)-8.,(float)1.,(
	    float)0.,(float)2.,(float)1.,(float)3.,(float)6.,(float)-4.,(
	    float)0.,(float)-2.,(float)3.,(float)-1.,(float)-6. };
    static real dg2000[45] = { (float)0.,(float)17.6,(float)13.,(float)-13.2,(
	    float)3.7,(float)-.8,(float)1.5,(float)-6.4,(float)-.2,(float)
	    -8.1,(float).8,(float).9,(float)-6.9,(float).5,(float)-4.6,(float)
	    .8,(float).1,(float)-1.5,(float)-2.,(float)-.1,(float)2.3,(float)
	    .5,(float)-.4,(float).6,(float)1.9,(float)-.2,(float)-.2,(float)
	    0.,(float)-.2,(float)-.8,(float)-.6,(float).6,(float)1.2,(float)
	    .1,(float).2,(float)-.6,(float).3,(float)-.2,(float).1,(float).4,(
	    float)-1.1,(float).3,(float).2,(float)-.9,(float)-.3 };
    static real dh2000[45] = { (float)0.,(float)0.,(float)-18.3,(float)0.,(
	    float)-15.,(float)-8.8,(float)0.,(float)4.1,(float)2.2,(float)
	    -12.1,(float)0.,(float)1.8,(float)1.2,(float)2.7,(float)-1.,(
	    float)0.,(float).2,(float)1.2,(float).3,(float)1.8,(float).9,(
	    float)0.,(float).3,(float)-1.6,(float)-.2,(float)-.9,(float)1.,(
	    float)2.2,(float)0.,(float).8,(float).2,(float).6,(float)-.4,(
	    float)0.,(float)-.3,(float)0.,(float)0.,(float).4,(float)-.2,(
	    float).2,(float).7,(float)0.,(float)-1.2,(float)-.7,(float)-.6 };
    static integer ma = 0;
    static integer iyr = 0;
    static integer ipr = 0;

    /* System generated locals */
    integer i__1, i__2;

    /* Builtin functions */
    double sqrt(), cos(), sin();

    /* Local variables */
    static real a[11], b[11], c, d, e, g[66], h[66];
    static integer k, m, n;
    static real p, q, s, u, w, x, y, z, d2, f2, f1;
    static integer n2;
    static real p2, aa, cf, bi;
    static logical bk;
    static real an, hh;
    static logical bm;
    static real dp, dt, sf;
    static integer mm, mn;
    static real pm, pp, qq, xk, bbf, rec[66], bbr, bbt;
    static integer knm, mnn;

/*  Aug 26, 1997: Subroutine IGRF is modified by V. Papitashvili - SHA */
/*    coefficients for DGRF-1990, IGRF-1995, and SV 1995-2000 are added */
/*    (EOS, v.77, No.16, p.153, April 16, 1996) */
/*  Feb 03, 1995: Modified by Vladimir Papitashvili (SPRL, University of 
*/
/*    Michigan) to accept dates between 1945 and 2000 */
/*  MODIFIED TO ACCEPT DATES BETWEEN 1965 AND 2000; COEFFICIENTS FOR IGRF 
*/
/*  1985 HAVE BEEN REPLACED WITH DGRF1985 COEFFICIENTS [EOS TRANS. AGU */
/*  APRIL 21, 1992, C  P. 182]. ALSO, THE CODE IS MODIFIED TO ACCEPT */
/*  DATES BEYOND 1990, AND TO USE LINEAR EXTRAPOLATION BETWEEN 1990 AND */
/*  2000 BASED ON THE IGRF COEFFICIENTS FROM THE SAME EOS ARTICLE */
/*  Modified by Mauricio Peredo, Hughes STX at NASA/GSFC, September 1992 
*/
/*  CALCULATES COMPONENTS OF MAIN GEOMAGNETIC FIELD IN SPHERICAL */
/*  GEOCENTRIC COORDINATE SYSTEM BY USING THIRD GENERATION IGRF MODEL */
/*  (J. GEOMAG. GEOELECTR. V.34, P.313-315, 1982; GEOMAGNETISM AND */
/*  AERONOMY V.26, P.523-525, 1986). */
/*  UPDATING OF COEFFICIENTS TO A GIVEN EPOCH IS MADE DURING THE FIRST */
/*  CALL AND AFTER EVERY CHANGE OF PARAMETER IY */
/* ---INPUT PARAMETERS: */
/*  IY - YEAR NUMBER (FROM 1945 UP TO 1990) */
/*  NM - MAXIMAL ORDER OF HARMONICS TAKEN INTO ACCOUNT (NOT MORE THAN 10) 
*/
/*  R,T,F - SPHERICAL COORDINATES OF THE POINT (R IN UNITS RE=6371.2 KM, 
*/
/*    COLATITUDE T AND LONGITUDE F IN RADIANS) */
/* ---OUTPUT PARAMETERS: */
/*  BR,BT,BF - SPHERICAL COMPONENTS OF MAIN GEOMAGNETIC FIELD (in nT) */
/*  AUTHOR: NIKOLAI A. TSYGANENKO, INSTITUTE OF PHYSICS, ST.-PETERSBURG */
/*      STATE UNIVERSITY, STARY PETERGOF 198904, ST.-PETERSBURG, RUSSIA */
/*      (now the NASA Goddard Space Fligth Center, Greenbelt, Maryland) */
/*  G0, G1, and H1 are used in SUBROUTINE DIP to calculate geodipole's */
/*  moment for a given year */
    if (ma != 1) {
	goto L10;
    }
    if (*iy != iyr) {
	goto L30;
    }
    goto L130;
L10:
    ma = 1;
    knm = 15;
    for (n = 1; n <= 11; ++n) {
	n2 = (n << 1) - 1;
	n2 *= n2 - 2;
	i__1 = n;
	for (m = 1; m <= i__1; ++m) {
	    mn = n * (n - 1) / 2 + m;
/* L20: */
	    rec[mn - 1] = (real) ((n - m) * (n + m - 2)) / (real) n2;
	}
    }
L30:
    iyr = *iy;
    if (iyr < 1945) {
	iyr = 1945;
    }
    if (iyr > 2000) {
	iyr = 2000;
    }
/*      IF (IY.NE.IYR.AND.IPR.EQ.0) write(*,999)IY,IYR */
/* 999   FORMAT(//1X, */
/*     * '*** IGRF WARNS: YEAR IS OUT OF INTERVAL 1945-2000: IY =',I5/, */
/*     *',         CALCULATIONS WILL BE DONE FOR IYR =',I5,' ****'//) */
    if (iyr != *iy) {
	ipr = 1;
    }
    if (iyr < 1950) {
	goto L1950;
    }
/* INTERPOLATE BETWEEN 1945 - 1950 */
    if (iyr < 1955) {
	goto L1955;
    }
/* INTERPOLATE BETWEEN 1950 - 1955 */
    if (iyr < 1960) {
	goto L1960;
    }
/* INTERPOLATE BETWEEN 1955 - 1960 */
    if (iyr < 1965) {
	goto L1965;
    }
/* INTERPOLATE BETWEEN 1960 - 1965 */
    if (iyr < 1970) {
	goto L1970;
    }
/* INTERPOLATE BETWEEN 1965 - 1970 */
    if (iyr < 1975) {
	goto L1975;
    }
/* INTERPOLATE BETWEEN 1970 - 1975 */
    if (iyr < 1980) {
	goto L1980;
    }
/* INTERPOLATE BETWEEN 1975 - 1980 */
    if (iyr < 1985) {
	goto L1985;
    }
/* INTERPOLATE BETWEEN 1980 - 1985 */
    if (iyr < 1990) {
	goto L1990;
    }
/* INTERPOLATE BETWEEN 1985 - 1990 */
    if (iyr < 1995) {
	goto L1995;
    }
/*  EXTRAPOLATE BETWEEN 1995 - 2000 */
/* INTERPOLATE BETWEEN 1990 - 1995 */
    dt = (real) iyr - (float)1995.;
    for (n = 1; n <= 66; ++n) {
	g[n - 1] = g1995[n - 1];
	h[n - 1] = h1995[n - 1];
	if (n > 45) {
	    goto L1000;
	}
	g[n - 1] += dg2000[n - 1] * dt;
	h[n - 1] += dh2000[n - 1] * dt;
L1000:
	;
    }
    goto L300;
/*  INTERPOLATE BETWEEEN 1945 - 1950 */
L1950:
    f2 = (iyr - 1945) / (float)5.;
    f1 = (float)1. - f2;
    for (n = 1; n <= 66; ++n) {
	g[n - 1] = g1945[n - 1] * f1 + g1950[n - 1] * f2;
/* L1953: */
	h[n - 1] = h1945[n - 1] * f1 + h1950[n - 1] * f2;
    }
    goto L300;
/*  INTERPOLATE BETWEEEN 1950 - 1955 */
L1955:
    f2 = (iyr - 1950) / (float)5.;
    f1 = (float)1. - f2;
    for (n = 1; n <= 66; ++n) {
	g[n - 1] = g1950[n - 1] * f1 + g1955[n - 1] * f2;
/* L1958: */
	h[n - 1] = h1950[n - 1] * f1 + h1955[n - 1] * f2;
    }
    goto L300;
/*  INTERPOLATE BETWEEEN 1955 - 1960 */
L1960:
    f2 = (iyr - 1955) / (float)5.;
    f1 = (float)1. - f2;
    for (n = 1; n <= 66; ++n) {
	g[n - 1] = g1955[n - 1] * f1 + g1960[n - 1] * f2;
/* L1963: */
	h[n - 1] = h1955[n - 1] * f1 + h1960[n - 1] * f2;
    }
    goto L300;
/*  INTERPOLATE BETWEEEN 1960 - 1965 */
L1965:
    f2 = (iyr - 1960) / (float)5.;
    f1 = (float)1. - f2;
    for (n = 1; n <= 66; ++n) {
	g[n - 1] = g1960[n - 1] * f1 + g1965[n - 1] * f2;
/* L1968: */
	h[n - 1] = h1960[n - 1] * f1 + h1965[n - 1] * f2;
    }
    goto L300;
/*  INTERPOLATE BETWEEEN 1965 - 1970 */
L1970:
    f2 = (iyr - 1965) / (float)5.;
    f1 = (float)1. - f2;
    for (n = 1; n <= 66; ++n) {
	g[n - 1] = g1965[n - 1] * f1 + g1970[n - 1] * f2;
/* L1973: */
	h[n - 1] = h1965[n - 1] * f1 + h1970[n - 1] * f2;
    }
    goto L300;
/*  INTERPOLATE BETWEEN 1970 - 1975 */
L1975:
    f2 = (iyr - 1970) / (float)5.;
    f1 = (float)1. - f2;
    for (n = 1; n <= 66; ++n) {
	g[n - 1] = g1970[n - 1] * f1 + g1975[n - 1] * f2;
/* L1978: */
	h[n - 1] = h1970[n - 1] * f1 + h1975[n - 1] * f2;
    }
    goto L300;
/*  INTERPOLATE BETWEEN 1975 - 1980 */
L1980:
    f2 = (iyr - 1975) / (float)5.;
    f1 = (float)1. - f2;
    for (n = 1; n <= 66; ++n) {
	g[n - 1] = g1975[n - 1] * f1 + g1980[n - 1] * f2;
/* L1983: */
	h[n - 1] = h1975[n - 1] * f1 + h1980[n - 1] * f2;
    }
    goto L300;
/*  INTERPOLATE BETWEEN 1980 - 1985 */
L1985:
    f2 = (iyr - 1980) / (float)5.;
    f1 = (float)1. - f2;
    for (n = 1; n <= 66; ++n) {
	g[n - 1] = g1980[n - 1] * f1 + g1985[n - 1] * f2;
/* L1988: */
	h[n - 1] = h1980[n - 1] * f1 + h1985[n - 1] * f2;
    }
    goto L300;
/*  INTERPOLATE BETWEEN 1985 - 1990 */
L1990:
    f2 = (iyr - 1985) / (float)5.;
    f1 = (float)1. - f2;
    for (n = 1; n <= 66; ++n) {
	g[n - 1] = g1985[n - 1] * f1 + g1990[n - 1] * f2;
/* L1993: */
	h[n - 1] = h1985[n - 1] * f1 + h1990[n - 1] * f2;
    }
    goto L300;
/*  INTERPOLATE BETWEEN 1990 - 1995 */
L1995:
    f2 = (iyr - 1990) / (float)5.;
    f1 = (float)1. - f2;
    for (n = 1; n <= 66; ++n) {
	g[n - 1] = g1990[n - 1] * f1 + g1995[n - 1] * f2;
/* L1998: */
	h[n - 1] = h1990[n - 1] * f1 + h1995[n - 1] * f2;
    }
    goto L300;
/*  GET HERE WHEN COEFFICIENTS FOR APPROPRIATE IGRF MODEL HAVE BEEN */
/*  ASSIGNED */
L300:
    s = (float)1.;
    dmom_1.g0 = g[1];
    dmom_1.g1 = g[2];
    dmom_1.h1 = h[2];
    for (n = 2; n <= 11; ++n) {
	mn = n * (n - 1) / 2 + 1;
	s = s * (real) ((n << 1) - 3) / (real) (n - 1);
	g[mn - 1] *= s;
	h[mn - 1] *= s;
	p = s;
	i__1 = n;
	for (m = 2; m <= i__1; ++m) {
	    aa = (float)1.;
	    if (m == 2) {
		aa = (float)2.;
	    }
	    p *= sqrt(aa * (real) (n - m + 1) / (real) (n + m - 2));
	    mnn = mn + m - 1;
	    g[mnn - 1] *= p;
/* L120: */
	    h[mnn - 1] *= p;
	}
    }
L130:
    if (knm == *nm) {
	goto L140;
    }
    knm = *nm;
    k = knm + 1;
L140:
    pp = (float)1. / *r;
    p = pp;
    i__1 = k;
    for (n = 1; n <= i__1; ++n) {
	p *= pp;
	a[n - 1] = p;
/* L150: */
	b[n - 1] = p * n;
    }
    p = (float)1.;
    d = (float)0.;
    bbr = (float)0.;
    bbt = (float)0.;
    bbf = (float)0.;
    u = *t;
    cf = cos(*f);
    sf = sin(*f);
    c = cos(u);
    s = sin(u);
    bk = s < (float)1e-5;
    i__1 = k;
    for (m = 1; m <= i__1; ++m) {
	bm = m == 1;
	if (bm) {
	    goto L160;
	}
	mm = m - 1;
	w = x;
	x = w * cf + y * sf;
	y = y * cf - w * sf;
	goto L170;
L160:
	x = (float)0.;
	y = (float)1.;
L170:
	q = p;
	z = d;
	bi = (float)0.;
	p2 = (float)0.;
	d2 = (float)0.;
	i__2 = k;
	for (n = m; n <= i__2; ++n) {
	    an = a[n - 1];
	    mn = n * (n - 1) / 2 + m;
	    e = g[mn - 1];
	    hh = h[mn - 1];
	    w = e * y + hh * x;
	    bbr += b[n - 1] * w * q;
	    bbt -= an * w * z;
	    if (bm) {
		goto L180;
	    }
	    qq = q;
	    if (bk) {
		qq = z;
	    }
	    bi += an * (e * x - hh * y) * qq;
L180:
	    xk = rec[mn - 1];
	    dp = c * z - s * q - xk * d2;
	    pm = c * q - xk * p2;
	    d2 = z;
	    p2 = q;
	    z = dp;
/* L190: */
	    q = pm;
	}
	d = s * d + c * p;
	p = s * p;
	if (bm) {
	    goto L200;
	}
	bi *= mm;
	bbf += bi;
L200:
	;
    }
    *br = bbr;
    *bt = bbt;
    if (bk) {
	goto L210;
    }
    *bf = bbf / s;
    goto L220;
L210:
    if (c < (float)0.) {
	bbf = -(doublereal)bbf;
    }
    *bf = bbf;
L220:
    return 0;
} /* igrf_ */

/*  ********************************************************************* */
/*  ********************************************************************* */
/* Subroutine */ int sphcar_(r, teta, phi, x, y, z, j)
real *r, *teta, *phi, *x, *y, *z;
integer *j;
{
    /* System generated locals */
    real r__1, r__2;

    /* Builtin functions */
    double sqrt(), atan2(), sin(), cos();

    /* Local variables */
    static real sq;

/*   CONVERTS SPHERICAL COORDS INTO CARTESIAN ONES AND VICA VERSA */
/*    (TETA AND PHI IN RADIANS). */
/*                  J>0            J<0 */
/* -----INPUT:   J,R,TETA,PHI     J,X,Y,Z */
/* ----OUTPUT:      X,Y,Z        R,TETA,PHI */
/*  AUTHOR: NIKOLAI A. TSYGANENKO, INSTITUTE OF PHYSICS, ST.-PETERSBURG */
/*      STATE UNIVERSITY, STARY PETERGOF 198904, ST.-PETERSBURG, RUSSIA */
/*      (now the NASA Goddard Space Fligth Center, Greenbelt, Maryland) */
    if (*j > 0) {
	goto L3;
    }
/* Computing 2nd power */
    r__1 = *x;
/* Computing 2nd power */
    r__2 = *y;
    sq = r__1 * r__1 + r__2 * r__2;
/* Computing 2nd power */
    r__1 = *z;
    *r = sqrt(sq + r__1 * r__1);
    if (sq != (float)0.) {
	goto L2;
    }
    *phi = (float)0.;
    if (*z < (float)0.) {
	goto L1;
    }
    *teta = (float)0.;
    return 0;
L1:
    *teta = (float)3.141592654;
    return 0;
L2:
    sq = sqrt(sq);
    *phi = atan2(*y, *x);
    *teta = atan2(sq, *z);
    if (*phi < (float)0.) {
	*phi += (float)6.28318531;
    }
    return 0;
L3:
    sq = *r * sin(*teta);
    *x = sq * cos(*phi);
    *y = sq * sin(*phi);
    *z = *r * cos(*teta);
    return 0;
} /* sphcar_ */

/*  ********************************************************************* */
/*  ********************************************************************* */
/* Subroutine */ int bspcar_(teta, phi, br, btet, bphi, bx, by, bz)
real *teta, *phi, *br, *btet, *bphi, *bx, *by, *bz;
{
    /* Builtin functions */
    double sin(), cos();

    /* Local variables */
    static real c, s, be, cf, sf;

/*   CALCULATES CARTESIAN FIELD COMPONENTS FROM SPHERICAL ONES */
/* -----INPUT:   TETA,PHI - SPHERICAL ANGLES OF THE POINT IN RADIANS */
/*              BR,BTET,BPHI -  SPHERICAL COMPONENTS OF THE FIELD */
/* -----OUTPUT:  BX,BY,BZ - CARTESIAN COMPONENTS OF THE FIELD */
/*  AUTHOR: NIKOLAI A. TSYGANENKO, INSTITUTE OF PHYSICS, ST.-PETERSBURG */
/*      STATE UNIVERSITY, STARY PETERGOF 198904, ST.-PETERSBURG, RUSSIA */
/*      (now the NASA Goddard Space Fligth Center, Greenbelt, Maryland) */
    s = sin(*teta);
    c = cos(*teta);
    sf = sin(*phi);
    cf = cos(*phi);
    be = *br * s + *btet * c;
    *bx = be * cf - *bphi * sf;
    *by = be * sf + *bphi * cf;
    *bz = *br * c - *btet * s;
    return 0;
} /* bspcar_ */

/*  ********************************************************************* */
/*  ********************************************************************* */
/* Subroutine */ int geomag_(xgeo, ygeo, zgeo, xmag, ymag, zmag, j, iyr)
real *xgeo, *ygeo, *zgeo, *xmag, *ymag, *zmag;
integer *j, *iyr;
{
    /* Initialized data */

    static integer ii = 1;

    extern /* Subroutine */ int recalc_();

/* CONVERTS GEOCENTRIC (GEO) TO DIPOLE (MAG) COORDINATES OR VICA VERSA. */
/* IYR IS YEAR NUMBER (FOUR DIGITS). */
/*                           J>0                J<0 */
/* -----INPUT:  J,XGEO,YGEO,ZGEO,IYR   J,XMAG,YMAG,ZMAG,IYR */
/* -----OUTPUT:    XMAG,YMAG,ZMAG        XGEO,YGEO,ZGEO */
/*  AUTHOR: NIKOLAI A. TSYGANENKO, INSTITUTE OF PHYSICS, ST.-PETERSBURG */
/*      STATE UNIVERSITY, STARY PETERGOF 198904, ST.-PETERSBURG, RUSSIA */
/*      (now the NASA Goddard Space Fligth Center, Greenbelt, Maryland) */
    if (*iyr == ii) {
	goto L1;
    }
    ii = *iyr;
    recalc_(&ii, &c__0, &c__25, &c__0, &c__0);
L1:
    if (*j < 0) {
	goto L2;
    }
    *xmag = *xgeo * c1_1.ctcl + *ygeo * c1_1.ctsl - *zgeo * c1_1.st0;
    *ymag = *ygeo * c1_1.cl0 - *xgeo * c1_1.sl0;
    *zmag = *xgeo * c1_1.stcl + *ygeo * c1_1.stsl + *zgeo * c1_1.ct0;
    return 0;
L2:
    *xgeo = *xmag * c1_1.ctcl - *ymag * c1_1.sl0 + *zmag * c1_1.stcl;
    *ygeo = *xmag * c1_1.ctsl + *ymag * c1_1.cl0 + *zmag * c1_1.stsl;
    *zgeo = *zmag * c1_1.ct0 - *xmag * c1_1.st0;
    return 0;
} /* geomag_ */

/*  ********************************************************************* */
/*  ********************************************************************* */
/* Subroutine */ int recalc_(iyr, iday, ihour, min__, isec)
integer *iyr, *iday, *ihour, *min__, *isec;
{
    /* Initialized data */

    static integer iye = 0;
    static integer ide = 0;
    static integer ipr = 0;

    /* System generated locals */
    real r__1, r__2;

    /* Builtin functions */
    double sqrt(), cos(), sin(), asin(), atan2();

    /* Local variables */
    static real sdec, cgst, sgst, t, y, obliq, slong, f1, f2, srasn, s1, s2,
	    s3, y1, y2, y3, z1, z2, z3, g10, g11, h11, dj, dt, sq, exmagx, 
	    exmagy, exmagz, eymagx, eymagy, dy1, dz1, dz2, dz3, dy2, dy3, gst,
	     sqq, sqr;
    extern /* Subroutine */ int sun_();
    static real dip1, dip2, dip3;

/*  THIS IS A MODIFIED VERSION OF THE SUBROUTINE RECOMP WRITTEN BY */
/*  N. A. TSYGANENKO. SINCE I WANT TO USE IT IN PLACE OF SUBROUTINE */
/*  RECALC, I HAVE RENAMED THIS ROUTINE RECALC AND ELIMINATED THE */
/*  ORIGINAL RECALC FROM THIS VERSION OF THE <GEOPACK.FOR> PACKAGE. */
/*  THIS WAY ALL ORIGINAL CALLS TO RECALC WILL CONTINUE TO WORK WITHOUT */
/*  HAVING TO CHANGE THEM TO CALLS TO RECOMP. */
/*  AN ALTERNATIVE VERSION OF THE SUBROUTINE RECALC FROM THE GEOPACK */
/*  PACKAGE BASED ON A DIFFERENT APPROACH TO DERIVATION OF ROTATION */
/*  MATRIX ELEMENTS */
/*  THIS SUBROUTINE WORKS BY 20% FASTER THAN RECALC AND IS EASIER TO */
/*  UNDERSTAND */
/*  ##################################################### */
/*  #  WRITTEN BY  N.A. TSYGANENKO ON DECEMBER 1, 1991  # */
/*  ##################################################### */
/*  Modified by Mauricio Peredo, Hughes STX at NASA/GSFC Code 695, */
/*  September 1992 */
/*  Modified to accept dates up to year 2000 and updated IGRF coeficients 
*/
/*  from 1945 (updated by V. Papitashvili, February 1995) */
/*   OTHER SUBROUTINES CALLED BY THIS ONE: SUN */
/*     IYR = YEAR NUMBER (FOUR DIGITS) */
/*     IDAY = DAY OF YEAR (DAY 1 = JAN 1) */
/*     IHOUR = HOUR OF DAY (00 TO 23) */
/*     MIN = MINUTE OF HOUR (00 TO 59) */
/*     ISEC = SECONDS OF DAY(00 TO 59) */
    if (*iyr == iye && *iday == ide) {
	goto L5;
    }
/*  IYE AND IDE ARE THE CURRENT VALUES OF YEAR AND DAY NUMBER */
    c1_2.iy = *iyr;
    ide = *iday;
    if (c1_2.iy < 1945) {
	c1_2.iy = 1945;
    }
    if (c1_2.iy > 2000) {
	c1_2.iy = 2000;
    }
/*  WE ARE RESTRICTED BY THE INTERVAL 1945-2000, FOR WHICH THE IGRF */
/*  COEFFICIENTS ARE KNOWN; IF IYR IS OUTSIDE THIS INTERVAL, THE */
/*  SUBROUTINE GIVES A WARNING (BUT DOES NOT REPEAT IT AT THE NEXT CALLS) 
*/
/*      IF(IY.NE.IYR.AND.IPR.EQ.0) PRINT 10,IYR,IY */
    if (c1_2.iy != *iyr) {
	ipr = 1;
    }
    iye = c1_2.iy;
/*  LINEAR INTERPOLATION OF THE GEODIPOLE MOMENT COMPONENTS BETWEEN THE */
/*  VALUES FOR THE NEAREST EPOCHS: */
    if (c1_2.iy < 1950) {
/* 1945-1950 */
	f2 = ((real) c1_2.iy + (real) (*iday) / (float)365. - (float)1945.) / 
		(float)5.;
	f1 = 1. - f2;
	g10 = f1 * (float)30594. + f2 * (float)30554.;
	g11 = f1 * (float)-2285. - f2 * (float)2250.;
	h11 = f1 * (float)5810. + f2 * (float)5815.;
    } else if (c1_2.iy < 1955) {
/* 1950-1955 */
	f2 = ((real) c1_2.iy + (real) (*iday) / (float)365. - (float)1950.) / 
		(float)5.;
	f1 = 1. - f2;
	g10 = f1 * (float)30554. + f2 * (float)30500.;
	g11 = f1 * (float)-2250. - f2 * (float)2215.;
	h11 = f1 * (float)5815. + f2 * (float)5820.;
    } else if (c1_2.iy < 1960) {
/* 1955-1960 */
	f2 = ((real) c1_2.iy + (real) (*iday) / (float)365. - (float)1955.) / 
		(float)5.;
	f1 = 1. - f2;
	g10 = f1 * (float)30500. + f2 * (float)30421.;
	g11 = f1 * (float)-2215. - f2 * (float)2169.;
	h11 = f1 * (float)5820. + f2 * (float)5791.;
    } else if (c1_2.iy < 1965) {
/* 1960-1965 */
	f2 = ((real) c1_2.iy + (real) (*iday) / (float)365. - (float)1960.) / 
		(float)5.;
	f1 = 1. - f2;
	g10 = f1 * (float)30421. + f2 * (float)30334.;
	g11 = f1 * (float)-2169. - f2 * (float)2119.;
	h11 = f1 * (float)5791. + f2 * (float)5776.;
    } else if (c1_2.iy < 1970) {
/* 1965-1970 */
	f2 = ((real) c1_2.iy + (real) (*iday) / (float)365. - (float)1965.) / 
		(float)5.;
	f1 = 1. - f2;
	g10 = f1 * (float)30334. + f2 * (float)30220.;
	g11 = f1 * (float)-2119. - f2 * (float)2068.;
	h11 = f1 * (float)5776. + f2 * (float)5737.;
    } else if (c1_2.iy < 1975) {
/* 1970-1975 */
	f2 = ((real) c1_2.iy + (real) (*iday) / (float)365. - (float)1970.) / 
		(float)5.;
	f1 = 1. - f2;
	g10 = f1 * (float)30220. + f2 * (float)30100.;
	g11 = f1 * (float)-2068. - f2 * (float)2013.;
	h11 = f1 * (float)5737. + f2 * (float)5675.;
    } else if (c1_2.iy < 1980) {
/* 1975-1980 */
	f2 = ((doublereal) c1_2.iy + (doublereal) (*iday) / (float)365. - (
		float)1975.) / (float)5.;
	f1 = 1. - f2;
	g10 = f1 * (float)30100. + f2 * (float)29992.;
	g11 = f1 * (float)-2013. - f2 * (float)1956.;
	h11 = f1 * (float)5675. + f2 * (float)5604.;
    } else if (c1_2.iy < 1985) {
/* 1980-1985 */
	f2 = ((real) c1_2.iy + (real) (*iday) / (float)365. - (float)1980.) / 
		(float)5.;
	f1 = 1. - f2;
	g10 = f1 * (float)29992. + f2 * (float)29873.;
	g11 = f1 * (float)-1956. - f2 * (float)1905.;
	h11 = f1 * (float)5604. + f2 * (float)5500.;
    } else if (c1_2.iy < 1990) {
/* 1985-1990 */
	f2 = ((real) c1_2.iy + (real) (*iday) / (float)365. - (float)1985.) / 
		(float)5.;
	f1 = 1. - f2;
	g10 = f1 * (float)29873. + f2 * (float)29775.;
	g11 = f1 * (float)-1905. - f2 * (float)1848.;
	h11 = f1 * (float)5500. + f2 * (float)5406.;
    } else if (c1_2.iy < 1995) {
/* 1990-1995 */
	f2 = ((real) c1_2.iy + (real) (*iday) / (float)365. - (float)1990.) / 
		(float)5.;
	f1 = 1. - f2;
	g10 = f1 * (float)29775. + f2 * (float)29682.;
	g11 = f1 * (float)-1848. - f2 * (float)1789.;
	h11 = f1 * (float)5406. + f2 * (float)5318.;
    } else {
/* 1995-2000 */
	dt = (real) c1_2.iy + (real) (*iday) / (float)365. - (float)1995.;
	g10 = (float)29682. - dt * (float)17.6;
	g11 = dt * (float)13. - (float)1789.;
	h11 = (float)5318. - dt * (float)18.3;
    }
/*  NOW CALCULATE THE COMPONENTS OF THE UNIT VECTOR EzMAG IN GEO COORD */
/*  SYSTEM: */
/*  SIN(TETA0)*COS(LAMBDA0), SIN(TETA0)*SIN(LAMBDA0), AND COS(TETA0) */
/*         ST0 * CL0                ST0 * SL0                CT0 */
/* Computing 2nd power */
    r__1 = g11;
/* Computing 2nd power */
    r__2 = h11;
    sq = r__1 * r__1 + r__2 * r__2;
    sqq = sqrt(sq);
/* Computing 2nd power */
    r__1 = g10;
    sqr = sqrt(r__1 * r__1 + sq);
    c1_2.sl0 = -(doublereal)h11 / sqq;
    c1_2.cl0 = -(doublereal)g11 / sqq;
    c1_2.st0 = sqq / sqr;
    c1_2.ct0 = g10 / sqr;
    c1_2.stcl = c1_2.st0 * c1_2.cl0;
    c1_2.stsl = c1_2.st0 * c1_2.sl0;
    c1_2.ctsl = c1_2.ct0 * c1_2.sl0;
    c1_2.ctcl = c1_2.ct0 * c1_2.cl0;
/*  THE CALCULATIONS ARE TERMINATED IF ONLY GEO-MAG TRANSFORMATION */
/*  IS TO BE DONE  (IHOUR>24 IS THE AGREED CONDITION FOR THIS CASE): */
L5:
    if (*ihour > 24) {
	return 0;
    }
    sun_(&c1_2.iy, iday, ihour, min__, isec, &gst, &slong, &srasn, &sdec);
/*  S1,S2, AND S3 ARE THE COMPONENTS OF THE UNIT VECTOR EXGSM=EXGSE */
/*  IN THE SYSTEM GEI POINTING FROM THE EARTH'S CENTER TO THE SUN: */
    s1 = cos(srasn) * cos(sdec);
    s2 = sin(srasn) * cos(sdec);
    s3 = sin(sdec);
    cgst = cos(gst);
    sgst = sin(gst);
/*  DIP1, DIP2, AND DIP3 ARE THE COMPONENTS OF THE UNIT VECTOR */
/*  EZSM=EZMAG IN THE SYSTEM GEI: */
    dip1 = c1_2.stcl * cgst - c1_2.stsl * sgst;
    dip2 = c1_2.stcl * sgst + c1_2.stsl * cgst;
    dip3 = c1_2.ct0;
/*  NOW CALCULATE THE COMPONENTS OF THE UNIT VECTOR EYGSM IN THE SYSTEM */
/*  GEI BY TAKING THE VECTOR PRODUCT D x S AND NORMALIZING IT TO UNIT */
/*  LENGTH: */
    y1 = dip2 * s3 - dip3 * s2;
    y2 = dip3 * s1 - dip1 * s3;
    y3 = dip1 * s2 - dip2 * s1;
    y = sqrt(y1 * y1 + y2 * y2 + y3 * y3);
    y1 /= y;
    y2 /= y;
    y3 /= y;
/*  THEN IN THE GEI SYSTEM THE UNIT VECTOR Z=EZGSM=EXGSM x EYGSM=S x Y */
/*  HAS THE COMPONENTS: */
    z1 = s2 * y3 - s3 * y2;
    z2 = s3 * y1 - s1 * y3;
    z3 = s1 * y2 - s2 * y1;
/*  THE VECTOR EZGSE (HERE DZ) IN GEI HAS THE COMPONENTS (0,-SIN(DELTA), 
*/
/*  COS(DELTA)) = (0.,-0.397823,0.917462); HERE DELTA = 23.44214 DEG FOR 
*/
/*  THE EPOCH 1978 (SEE THE BOOK BY GUREVICH OR OTHER ASTRONOMICAL */
/*  HANDBOOKS). HERE THE MOST ACCURATE TIME-DEPENDENT FORMULA IS USED: */
    dj = (real) ((c1_2.iy - 1900) * 365 + (c1_2.iy - 1901) / 4 + *iday) - (
	    float).5 + (real) (*isec) / (float)86400.;
    t = dj / (float)36525.;
    obliq = ((float)23.45229 - t * (float).0130125) / (float)57.2957795;
    dz1 = (float)0.;
    dz2 = -(doublereal)sin(obliq);
    dz3 = cos(obliq);
/*  THEN THE UNIT VECTOR EYGSE IN GEI SYSTEM IS THE VECTOR PRODUCT DZ x S 
*/
    dy1 = dz2 * s3 - dz3 * s2;
    dy2 = dz3 * s1 - dz1 * s3;
    dy3 = dz1 * s2 - dz2 * s1;
/*  THE ELEMENTS OF THE MATRIX GSE TO GSM ARE THE SCALAR PRODUCTS: */
/*  CHI=EM22=(EYGSM,EYGSE), SHI=EM23=(EYGSM,EZGSE), */
/*  EM32=(EZGSM,EYGSE)=-EM23, AND EM33=(EZGSM,EZGSE)=EM22 */
    c1_2.chi = y1 * dy1 + y2 * dy2 + y3 * dy3;
    c1_2.shi = y1 * dz1 + y2 * dz2 + y3 * dz3;
    c1_2.hi = asin(c1_2.shi);
/*  TILT ANGLE: PSI=ARCSIN(DIP,EXGSM) */
    c1_2.sps = dip1 * s1 + dip2 * s2 + dip3 * s3;
/* Computing 2nd power */
    r__1 = c1_2.sps;
    c1_2.cps = sqrt((float)1. - r__1 * r__1);
    c1_2.psi = asin(c1_2.sps);
/*  THE ELEMENTS OF THE MATRIX MAG TO SM ARE THE SCALAR PRODUCTS: */
/*  CFI=GM22=(EYSM,EYMAG), SFI=GM23=(EYSM,EXMAG); THEY CAN BE DERIVED */
/*  AS FOLLOWS: */
/*  IN GEO THE VECTORS EXMAG AND EYMAG HAVE THE COMPONENTS */
/*  (CT0*CL0,CT0*SL0,-ST0) AND (-SL0,CL0,0), RESPECTIVELY. HENCE, IN */
/*  GEI SYSTEM THE COMPONENTS ARE: */
/*  EXMAG:    CT0*CL0*COS(GST)-CT0*SL0*SIN(GST) */
/*            CT0*CL0*SIN(GST)+CT0*SL0*COS(GST) */
/*            -ST0 */
/*  EYMAG:    -SL0*COS(GST)-CL0*SIN(GST) */
/*            -SL0*SIN(GST)+CL0*COS(GST) */
/*             0 */
/*  THE COMPONENTS OF EYSM IN GEI WERE FOUND ABOVE AS Y1, Y2, AND Y3; */
/*  NOW WE ONLY HAVE TO COMBINE THE QUANTITIES INTO SCALAR PRODUCTS: */
    exmagx = c1_2.ct0 * (c1_2.cl0 * cgst - c1_2.sl0 * sgst);
    exmagy = c1_2.ct0 * (c1_2.cl0 * sgst + c1_2.sl0 * cgst);
    exmagz = -(doublereal)c1_2.st0;
    eymagx = -(doublereal)(c1_2.sl0 * cgst + c1_2.cl0 * sgst);
    eymagy = -(doublereal)(c1_2.sl0 * sgst - c1_2.cl0 * cgst);
    c1_2.cfi = y1 * eymagx + y2 * eymagy;
    c1_2.sfi = y1 * exmagx + y2 * exmagy + y3 * exmagz;
    c1_2.xmut = (atan2(c1_2.sfi, c1_2.cfi) + (float)3.1415926536) * (float)
	    3.8197186342;
/*  THE ELEMENTS OF THE MATRIX GEO TO GSM ARE THE SCALAR PRODUCTS: */
/*  A11=(EXGEO,EXGSM), A12=(EYGEO,EXGSM), A13=(EZGEO,EXGSM), */
/*  A21=(EXGEO,EYGSM), A22=(EYGEO,EYGSM), A23=(EZGEO,EYGSM), */
/*  A31=(EXGEO,EZGSM), A32=(EYGEO,EZGSM), A33=(EZGEO,EZGSM), */
/*  ALL THE UNIT VECTORS IN BRACKETS ARE ALREADY DEFINED IN GEI: */
/*  EXGEO=(CGST,SGST,0), EYGEO=(-SGST,CGST,0), EZGEO=(0,0,1) */
/*  EXGSM=(S1,S2,S3),  EYGSM=(Y1,Y2,Y3),   EZGSM=(Z1,Z2,Z3) */
/*  AND  THEREFORE: */
    c1_2.a11 = s1 * cgst + s2 * sgst;
    c1_2.a12 = -(doublereal)s1 * sgst + s2 * cgst;
    c1_2.a13 = s3;
    c1_2.a21 = y1 * cgst + y2 * sgst;
    c1_2.a22 = -(doublereal)y1 * sgst + y2 * cgst;
    c1_2.a23 = y3;
    c1_2.a31 = z1 * cgst + z2 * sgst;
    c1_2.a32 = -(doublereal)z1 * sgst + z2 * cgst;
    c1_2.a33 = z3;
/* 10   FORMAT(//1X, */
/*     * '****RECALC WARNS: YEAR IS OUT OF INTERVAL 1945-2000: IYR=',I4, 
*/
/*     * /,6X,'CALCULATIONS WILL BE DONE FOR IYR=',I4,/) */
    return 0;
} /* recalc_ */

/*  ********************************************************************* */
/*  ********************************************************************* */
/* Subroutine */ int sun_(iyr, iday, ihour, min__, isec, gst, slong, srasn, 
	sdec)
integer *iyr, *iday, *ihour, *min__, *isec;
real *gst, *slong, *srasn, *sdec;
{
    /* Initialized data */

    static real rad = (float)57.295779513;

    /* System generated locals */
    real r__1;
    doublereal d__1;

    /* Builtin functions */
    double sin(), sqrt(), atan(), cos(), atan2();

    /* Local variables */
    static doublereal fday;
    static real cosd, sind, g, t, obliq;
    static doublereal dj;
    static real sc, vl;
    extern doublereal gjdmod_();
    static real sob, slp;

/*  CALCULATES FOUR QUANTITIES NECESSARY FOR COORDINATE TRANSFORMATIONS */
/*  WHICH DEPEND ON SUN POSITION (AND, HENCE, ON UNIVERSAL TIME AND */
/*  SEASON) */
/* ---INPUT PARAMETERS: */
/*  IYR,IDAY,IHOUR,MIN,ISEC - YEAR, DAY, AND UNIVERSAL TIME IN HOURS, */
/*    MINUTES, AND SECONDS  (IDAY=1 CORRESPONDS TO JANUARY 1). */
/* ---OUTPUT PARAMETERS: */
/*  GST - GREENWICH MEAN SIDEREAL TIME, SLONG - LONGITUDE ALONG ECLIPTIC 
*/
/*  SRASN - RIGHT ASCENSION,  SDEC - DECLINATION  OF THE SUN (RADIANS) */
/*  THIS SUBROUTINE HAS BEEN COMPILED FROM: */
/*  RUSSELL C.T., COSM.ELECTRODYN., 1971, V.2,PP.184-196. */
/*  AUTHOR: Gilbert D. Mead */
    if (*iyr < 1901 || *iyr > 2099) {
	return 0;
    }
    fday = (doublereal) (*ihour * 3600 + *min__ * 60 + *isec) / 86400.;
    dj = (*iyr - 1900) * 365 + (*iyr - 1901) / 4 + *iday - .5 + fday;
    t = dj / (float)36525.;
    d__1 = dj * (float).9856473354 + (float)279.696678;
    vl = gjdmod_(&d__1, &c_b79);
    d__1 = dj * (float).9856473354 + (float)279.690983 + fday * (float)360. + 
	    (float)180.;
    *gst = gjdmod_(&d__1, &c_b79) / rad;
    d__1 = dj * (float).985600267 + (float)358.475845;
    g = gjdmod_(&d__1, &c_b79) / rad;
    *slong = (vl + ((float)1.91946 - t * (float).004789) * sin(g) + sin(g * (
	    float)2.) * (float).020094) / rad;
    if (*slong > (float)6.2831853) {
	*slong += (float)-6.2831853;
    }
    if (*slong < (float)0.) {
	*slong += (float)6.2831853;
    }
    obliq = ((float)23.45229 - t * (float).0130125) / rad;
    sob = sin(obliq);
    slp = *slong - (float)9.924e-5;
/*   THE LAST CONSTANT IS A CORRECTION FOR THE ANGULAR ABERRATION */
/*   DUE TO THE ORBITAL MOTION OF THE EARTH */
    sind = sob * sin(slp);
/* Computing 2nd power */
    r__1 = sind;
    cosd = sqrt((float)1. - r__1 * r__1);
    sc = sind / cosd;
    *sdec = atan(sc);
    *srasn = (float)3.141592654 - atan2(cos(obliq) / sob * sc, -(doublereal)
	    cos(slp) / cosd);
    return 0;
} /* sun_ */

/*  ********************************************************************* */
/*added by GJ to the version translated from Fortran to C with f2c on Linux*/
/* because the C library doesn't have DMOD() */
doublereal gjdmod_(a, b)
doublereal *a, *b;
{
    /* System generated locals */
    doublereal ret_val;

    /* Local variables */
    static doublereal x;

    x = *a;
L1:
    if (x < *b) {
	goto L2;
    }
    x = *a - *b;
    goto L1;
L2:
    if (x >= 0.) {
	goto L3;
    }
    x = *a - *b;
    goto L2;
L3:
    ret_val = x;
    return ret_val;
} /* gjdmod_ */

#ifdef __cplusplus
}
#endif

