/****************************************/
/* deep.c				*/
/*					*/
/* deep space perturbation subroutines	*/
/****************************************/

/***** description
 *
 *	$Id: deep.c,v 2.3 2003/09/08 10:28:12 ko Exp $
 *
 */

/***** include files *****/

#include <math.h>
#include "satellite.h"
#include "aaproto.h"

/***** global variables *****/

extern struct MCONSTANTS mcnsts;

/***** local globals *****/

static double q22 = 1.7891679e-6;
static double q31 = 2.1460748e-6;
static double q33 = 2.2123015e-7;

static double c1l = 4.7968065e-7;
static double c1ss = 2.9864797e-6;

static double root22 = 1.7891679e-6;
static double root32 = 3.7393792e-7;
static double root44 = 7.3636953e-9;
static double root52 = 1.1428639e-7;
static double root54 = 2.1765803e-9;

static double thdt = .0043752691;

static double zes = .01675;
static double znl = 1.5835218e-4;
static double zns = 1.19459e-5;
static double zel = .0549;

static double zcosis = .91744867;
static double zsinis = .39785416;
static double zsings = -.98088458;
static double zcosgs = .1945905;

static double g22 = 5.7686396;
static double g32 = .95240898;
static double g44 = 1.8014998;
static double g52 = 1.050833;
static double g54 = 4.4108898;

static int iresfl, isynfl;

static double atime, cc;
static double e3, ee2, eq, omegaq;
static double step2, stepn, stepp, savtsn;
static double thgr, temp, time;

static double d2201, d2211, d3210, d3222, d4410, d4422;
static double d5220, d5232, d5421, d5433;

static double del1, del2, del3;

static double fasx2, fasx4, fasx6;

static double se, se2, se3, si, si2, si3;
static double sl, sl2, sl3, sl4, sh, sh2, sh3;
static double sse, ssg, ssh, ssi, ssl;
static double sgh, sgh2, sgh3, sgh4;

static double xi2, xi3, xl2, xl3, xl4, xh2, xh3, xnq, xli, xni, xnoi;
static double xgh2, xgh3, xgh4;
static double xfact, xlamo, xqncl;

static double ze, zn, zmol, zmos;

static double zcosg, zsing, zcosi, zsini, zcosh, zsinh;

static double eqsq, siniq, cosiq, rteqsq, sinomo, cosomo, bsq, omgdt;

/***** local function prototypes *****/

static void doterms (void);

/*****************************/
/* deep space initialization */
/*****************************/

int    dpinit (double eqsq1, double siniq1, double cosiq1,
	         double rteqsq1, double ao, double cosq2, double sinomo1,
	        double cosomo1, double bsq1, double xlldot, double omgdt1,
	              double xnodot, double xnodp, ELEMENT element)
{
    /* Local variables */

    double aqnv, ainv2, bfact, day, eoc, temp1, zmo;

    double cosq, sinq, sini2;

    double f220, f221, f311, f321, f322, f330;
    double f441, f442, f522, f523, f542, f543;

    double g200, g201, g211, g300, g310, g322, g410, g422;
    double g520, g533, g521, g532;

    double xno2, xmao, xpidot;

    static double c, ctem, gam, preep, stem, xnodce;
    static double zx, zy;
    static double zcosil, zsinil, zsinhl, zcoshl, zcosgl, zsingl;


    eqsq = eqsq1;
    siniq = siniq1;
    cosiq = cosiq1;
    rteqsq = rteqsq1;
    sinomo = sinomo1;
    cosomo = cosomo1;
    bsq = bsq1;
    omgdt = omgdt1;

    thgr = thetag (element.epoch, element);
    eq = element.eo;
    xnq = xnodp;
    aqnv = 1. / ao;
    xqncl = element.xincl;
    xmao = element.xmo;
    xpidot = omgdt + xnodot;
    sinq = sin (element.xnodeo);
    cosq = cos (element.xnodeo);
    omegaq = element.omegao;

    /* INITIALIZE LUNAR SOLAR TERMS */

    day = element.ds50 + 18261.5;

    if (day != preep)
    {
	preep = day;
	xnodce = 4.523602 - day * 9.2422029e-4;
	stem = sin (xnodce);
	ctem = cos (xnodce);
	zcosil = .91375164 - ctem * .03568096;
	zsinil = sqrt (1. - zcosil * zcosil);
	zsinhl = stem * .089683511 / zsinil;
	zcoshl = sqrt (1. - zsinhl * zsinhl);
	c = day * .2299715 + 4.7199672;
	gam = day * .001944368 + 5.8351514;
	zmol = fmod2p (c - gam);
	zx = stem * .39785416 / zsinil;
	zy = zcoshl * ctem + zsinhl * .91744867 * stem;
	zx = matan2 (zx, zy);
	zx = gam + zx - xnodce;
	zcosgl = cos (zx);
	zsingl = sin (zx);
	zmos = day * .017201977 + 6.2565837;
	zmos = fmod2p (zmos);
    }

    /* DO SOLAR TERMS */

    savtsn = 1.e20;
    zcosg = zcosgs;
    zsing = zsings;
    zcosi = zcosis;
    zsini = zsinis;
    zcosh = cosq;
    zsinh = sinq;
    cc = c1ss;
    zn = zns;
    ze = zes;
    zmo = zmos;
    xnoi = 1. / xnq;

    doterms ();

    /* DO LUNAR TERMS */

    sse = se;
    ssi = si;
    ssl = sl;
    ssh = sh / siniq;
    ssg = sgh - cosiq * ssh;
    se2 = ee2;
    si2 = xi2;
    sl2 = xl2;
    sgh2 = xgh2;
    sh2 = xh2;
    se3 = e3;
    si3 = xi3;
    sl3 = xl3;
    sgh3 = xgh3;
    sh3 = xh3;
    sl4 = xl4;
    sgh4 = xgh4;
    zcosg = zcosgl;
    zsing = zsingl;
    zcosi = zcosil;
    zsini = zsinil;
    zcosh = zcoshl * cosq + zsinhl * sinq;
    zsinh = sinq * zcoshl - cosq * zsinhl;
    zn = znl;
    cc = c1l;
    ze = zel;
    zmo = zmol;

    doterms ();

    sse += se;
    ssi += si;
    ssl += sl;
    ssg = ssg + sgh - cosiq / siniq * sh;
    ssh += sh / siniq;


    iresfl = 0;
    isynfl = 0;

    if (xnq < .0052359877 && xnq > .0034906585)
    {
	/* SYNCHRONOUS RESONANCE TERMS INITIALIZATION */

	iresfl = 1;
	isynfl = 1;
	g200 = eqsq * (eqsq * .8125 - 2.5) + 1.;
	g310 = eqsq * 2. + 1.;
	g300 = eqsq * (eqsq * 6.60937 - 6.) + 1.;
	f220 = (cosiq + 1.) * .75 * (cosiq + 1.);
	f311 = siniq * .9375 * siniq * (cosiq * 3. + 1.) - (cosiq + 1.) *
	    .75;
	f330 = cosiq + 1.;
	f330 = f330 * 1.875 * f330 * f330;
	del1 = xnq * 3. * xnq * aqnv * aqnv;
	del2 = del1 * 2. * f220 * g200 * q22;
	del3 = del1 * 3. * f330 * g300 * q33 * aqnv;
	del1 = del1 * f311 * g310 * q31 * aqnv;
	fasx2 = .13130908;
	fasx4 = 2.8843198;
	fasx6 = .37448087;
	xlamo = xmao + element.xnodeo + element.omegao - thgr;
	bfact = xlldot + xpidot - thdt;
	bfact = bfact + ssl + ssg + ssh;
    }
    else
    {
	/* GEOPOTENTIAL RESONANCE INITIALIZATION FOR 12 HOUR ORBITS */

	if (xnq < .00826 || xnq > .00924 || eq < .5)
	{
	    return (0);
	}

	iresfl = 1;
	eoc = eq * eqsq;
	g201 = -.306 - (eq - .64) * .44;

	if (eq > .65)
	{
	    g211 = eq * 331.819 - 72.099 - eqsq * 508.738 + eoc * 266.724;
	    g310 = eq * 1582.851 - 346.844 - eqsq * 2415.925 + eoc * 1246.113;
	    g322 = eq * 1554.908 - 342.585 - eqsq * 2366.899 + eoc * 1215.972;
	    g410 = eq * 4758.686 - 1052.797 - eqsq * 7193.992 + eoc * 3651.957;
	    g422 = eq * 16178.11 - 3581.69 - eqsq * 24462.77 + eoc * 12422.52;

	    if (eq > .715)
	    {
		g520 = eq * 29936.92 - 5149.66 - eqsq * 54087.36
		    + eoc * 31324.56;
	    }
	    else
	    {
		g520 = 1464.74 - eq * 4664.75 + eqsq * 3763.64;
	    }
	}
	else
	{
	    g211 = 3.616 - eq * 13.247 + eqsq * 16.29;
	    g310 = eq * 117.39 - 19.302 - eqsq * 228.419 + eoc * 156.591;
	    g322 = eq * 109.7927 - 18.9068 - eqsq * 214.6334 + eoc * 146.5816;
	    g410 = eq * 242.694 - 41.122 - eqsq * 471.094 + eoc * 313.953;
	    g422 = eq * 841.88 - 146.407 - eqsq * 1629.014 + eoc * 1083.435;
	    g520 = eq * 3017.977 - 532.114 - eqsq * 5740. + eoc * 3708.276;
	}

	if (eq < .7)
	{
	    g533 = eq * 4988.61 - 919.2277 - eqsq * 9064.77 + eoc * 5542.21;
	    g521 = eq * 4568.6173 - 822.71072 - eqsq * 8491.4146 + eoc *
		5337.524;
	    g532 = eq * 4690.25 - 853.666 - eqsq * 8624.77 + eoc * 5341.4;
	}
	else
	{
	    g533 = eq * 161616.52 - 37995.78 - eqsq * 229838.2
		+ eoc * 109377.94;
	    g521 = eq * 218913.95 - 51752.104 - eqsq * 309468.16 + eoc *
		146349.42;
	    g532 = eq * 170470.89 - 40023.88 - eqsq * 242699.48 + eoc *
		115605.82;
	}

	sini2 = siniq * siniq;
	f220 = (cosiq * 2. + 1. + cosq2) * .75;
	f221 = sini2 * 1.5;
	f321 = siniq * 1.875 * (1. - cosiq * 2. - cosq2 * 3.);
	f322 = siniq * -1.875 * (cosiq * 2. + 1. - cosq2 * 3.);
	f441 = sini2 * 35. * f220;
	f442 = sini2 * 39.375 * sini2;
	f522 = siniq * 9.84375 * (sini2 * (1. - cosiq * 2. - cosq2 * 5.) +
			     (cosiq * 4. - 2. + cosq2 * 6.) * 1. / 3.);
	f523 = siniq * (sini2 * 4.92187512 * (-2. - cosiq * 4. + cosq2 *
		     10.) + (cosiq * 2. + 1. - cosq2 * 3.) * 6.56250012);
	f542 = siniq * 29.53125 * (2. - cosiq * 8. + cosq2 * (cosiq * 8.
						   - 12. + cosq2 * 10.));
	f543 = siniq * 29.53125 * (-2. - cosiq * 8. + cosq2 * (cosiq * 8.
						   + 12. - cosq2 * 10.));
	xno2 = xnq * xnq;
	ainv2 = aqnv * aqnv;
	temp1 = xno2 * 3. * ainv2;
	temp = temp1 * root22;
	d2201 = temp * f220 * g201;
	d2211 = temp * f221 * g211;
	temp1 *= aqnv;
	temp = temp1 * root32;
	d3210 = temp * f321 * g310;
	d3222 = temp * f322 * g322;
	temp1 *= aqnv;
	temp = temp1 * 2. * root44;
	d4410 = temp * f441 * g410;
	d4422 = temp * f442 * g422;
	temp1 *= aqnv;
	temp = temp1 * root52;
	d5220 = temp * f522 * g520;
	d5232 = temp * f523 * g532;
	temp = temp1 * 2. * root54;
	d5421 = temp * f542 * g521;
	d5433 = temp * f543 * g533;
	xlamo = xmao + element.xnodeo + element.xnodeo - thgr - thgr;
	bfact = xlldot + xnodot + xnodot - thdt - thdt;
	bfact = bfact + ssl + ssh + ssh;
    }


    /* INITIALIZE INTEGRATOR */

    xfact = bfact - xnq;
    xli = xlamo;
    xni = xnq;
    atime = 0.;
    stepp = 720.;
    stepn = -720.;
    step2 = 259200.;

    return (0);
}

/****************************************/
/* deep space secular update subroutine */
/****************************************/

int    dpsec (double *xll, double *omgasm, double *xnodes, double *em,
	             double *xinc, double *xn, double tsince, ELEMENT element)
{
    /* Local variables */

    int    iret = 0, iretn = 1;

    double xomi, x2omi, xnddt, xndot, xldot, xl, x2li;

    static double delt, ft;

    time = tsince;
    *xll += ssl * time;
    *omgasm += ssg * time;
    *xnodes += ssh * time;
    *em = element.eo + sse * time;
    *xinc = element.xincl + ssi * time;

    if (*xinc < 0.)
    {
	*xinc = -(*xinc);
	*xnodes += mcnsts.pi;
	*omgasm -= mcnsts.pi;
    }

    if (iresfl == 0)
    {
	return 0;
    }

    for (;;)
    {
	if (iret == 0)
	{
	    if (atime == 0. || (time >= 0. && atime < 0.) ||
		    (time < 0. && atime >= 0.))
	    {
		/* EPOCH RESTART */

		if (time < 0.)
		{
		    delt = stepn;
		}
		else
		{
		    delt = stepp;
		}

		atime = 0.;
		xni = xnq;
		xli = xlamo;
	    }
	    else
	    {
		if (fabs (time) >= fabs (atime))
		{
		    if (time > 0.)
		    {
			delt = stepp;
		    }
		    else
		    {
			delt = stepn;
		    }
		}

		else
		{
		    if (time < 0.)
		    {
			delt = stepp;
		    }
		    else
		    {
			delt = stepn;
		    }

		    /* goto L150; */
		}
	    }
	}

	if (fabs (time - atime) < stepp)
	{
	    ft = time - atime;
	    iretn = 0;
	}
	else
	{
	    iret = 1;
	}

	/* DOT TERMS CALCULATED */

	/* L150: */

	if (isynfl != 0)
	{

	    xndot = del1 * sin (xli - fasx2) + del2 *
		sin ((xli - fasx4) * 2.) + del3 *
		sin ((xli - fasx6) * 3.);
	    xnddt = del1 * cos (xli - fasx2)
		+ del2 * 2. * cos ((xli - fasx4) * 2.) +
		del3 * 3. * cos ((xli - fasx6) * 3.);
	}
	else
	{
	    xomi = omegaq + omgdt * atime;
	    x2omi = xomi + xomi;
	    x2li = xli + xli;
	    xndot = d2201 * sin (x2omi + xli - g22)
		+ d2211 * sin (xli - g22) + d3210 *
		sin (xomi + xli - g32) + d3222 * sin (-xomi + xli - g32)
		+ d4410 * sin (x2omi + x2li - g44) + d4422 * 
		sin (x2li - g44) + d5220 * sin (xomi + xli - g52) 
		+ d5232 * sin (-xomi + xli - g52) + d5421 * 
		sin (xomi + x2li - g54) + d5433 * sin (-( double) xomi 
		+ x2li - g54);
	    xnddt = d2201 * cos (x2omi + xli - g22)
		+ d2211 * cos (xli - g22) + d3210 *
		cos (xomi + xli - g32) + d3222 * cos (-xomi + xli - g32)
		+ d5220 * cos (xomi + xli - g52) + d5232 * 
		cos (-xomi + xli - g52) + (d4410 * 
		cos (x2omi + x2li - g44) + d4422 * cos ( x2li - g44) + 
		d5421 * cos (xomi + x2li - g54) + d5433 * 
		cos (-( double) xomi + x2li - g54)) * 2.;
	}

	xldot = xni + xfact;
	xnddt *= xldot;

	if (iretn == 0)
	{
	    break;
	}

       /* INTEGRATOR */

	xli = xli + xldot * delt + xndot * step2;
	xni = xni + xndot * delt + xnddt * step2;
	atime += delt;
    }

    *xn = xni + xndot * ft + xnddt * ft * ft * .5;
    xl = xli + xldot * ft + xndot * ft * ft * .5;
    temp = -(*xnodes) + thgr + time * thdt;
    *xll = xl - *omgasm + temp;

    if (isynfl == 0)
    {
	*xll = xl + temp + temp;
    }

    return 0;

}

/***********************************************/
/* deep space periodics application subroutine */
/***********************************************/

int    dpper (double *em, double *xinc, double *omgasm, double *xnodes,
	             double *xll)
{
    /* Local variables */

    double alfdp, betdp;
    double dalf, dbet, dls;
    double cosok, cosis, sinis, sinok;
    double ph, pgh, xls;

    static double f2, f3;
    static double pinc, pe, pl;
    static double sghl, sghs, sel, shl, sil, ses, sll, shs, sis, sls;
    static double sinzf, zf, zm;

    sinis = sin (*xinc);
    cosis = cos (*xinc);

    if (fabs (savtsn - time) >= 30.)
    {
	savtsn = time;
	zm = zmos + zns * time;
	zf = zm + zes * 2. * sin (zm);
	sinzf = sin (zf);
	f2 = sinzf * .5 * sinzf - .25;
	f3 = sinzf * -.5 * cos (zf);
	ses = se2 * f2 + se3 * f3;
	sis = si2 * f2 + si3 * f3;
	sls = sl2 * f2 + sl3 * f3 + sl4 * sinzf;
	sghs = sgh2 * f2 + sgh3 * f3 + sgh4 * sinzf;
	shs = sh2 * f2 + sh3 * f3;
	zm = zmol + znl * time;
	zf = zm + zel * 2. * sin (zm);
	sinzf = sin (zf);
	f2 = sinzf * .5 * sinzf - .25;
	f3 = sinzf * -.5 * cos (zf);
	sel = ee2 * f2 + e3 * f3;
	sil = xi2 * f2 + xi3 * f3;
	sll = xl2 * f2 + xl3 * f3 + xl4 * sinzf;
	sghl = xgh2 * f2 + xgh3 * f3 + xgh4 * sinzf;
	shl = xh2 * f2 + xh3 * f3;
	pe = ses + sel;
	pinc = sis + sil;
	pl = sls + sll;
    }

    pgh = sghs + sghl;
    ph = shs + shl;
    *xinc += pinc;
    *em += pe;

    if (xqncl < .2)
    {
	/* APPLY PERIODICS WITH LYDDANE MODIFICATION */

	sinok = sin (*xnodes);
	cosok = cos (*xnodes);
	alfdp = sinis * sinok;
	betdp = sinis * cosok;
	dalf = ph * cosok + pinc * cosis * sinok;
	dbet = -ph * sinok + pinc * cosis * cosok;

	alfdp += dalf;
	betdp += dbet;
	xls = *xll + *omgasm + cosis * *xnodes;
	dls = pl + pgh - pinc * *xnodes * sinis;
	xls += dls;
	*xnodes = matan2 (alfdp, betdp);
	*xll += pl;
	*omgasm = xls - *xll - cos (*xinc) * *xnodes;
    }
    else
    {
	/* APPLY PERIODICS DIRECTLY */

	ph /= siniq;
	pgh -= cosiq * ph;
	*omgasm += pgh;
	*xnodes += ph;
	*xll += pl;
    }
    return 0;
}


/* doterms --- calculate lunar and solar terms */

static void doterms (void)
{
    /* local variables */

    double a1, a2, a3, a4, a5, a6, a7, a8, a9, a10;
    double s1, s2, s3, s4, s5, s6, s7;
    double x1, x2, x3, x4, x5, x6, x7, x8;
    double z1, z2, z3, z11, z12, z13, z21, z22, z23, z31, z32, z33;

    a1 = zcosg * zcosh + zsing * zcosi * zsinh;
    a3 = -zsing * zcosh + zcosg * zcosi * zsinh;
    a7 = -zcosg * zsinh + zsing * zcosi * zcosh;

    a8 = zsing * zsini;
    a9 = zsing * zsinh + zcosg * zcosi * zcosh;
    a10 = zcosg * zsini;
    a2 = cosiq * a7 + siniq * a8;
    a4 = cosiq * a9 + siniq * a10;
    a5 = -(siniq) * a7 + cosiq * a8;
    a6 = -(siniq) * a9 + cosiq * a10;

    x1 = a1 * cosomo + a2 * sinomo;
    x2 = a3 * cosomo + a4 * sinomo;
    x3 = -a1 * sinomo + a2 * cosomo;
    x4 = -a3 * sinomo + a4 * cosomo;

    x5 = a5 * sinomo;
    x6 = a6 * sinomo;
    x7 = a5 * cosomo;
    x8 = a6 * cosomo;

    z31 = x1 * 12. * x1 - x3 * 3. * x3;
    z32 = x1 * 24. * x2 - x3 * 6. * x4;
    z33 = x2 * 12. * x2 - x4 * 3. * x4;
    z1 = (a1 * a1 + a2 * a2) * 3. + z31 * eqsq;
    z2 = (a1 * a3 + a2 * a4) * 6. + z32 * eqsq;
    z3 = (a3 * a3 + a4 * a4) * 3. + z33 * eqsq;
    z11 = a1 * -6. * a5 + eqsq * (x1 * -24. * x7 - x3 * 6. * x5);
    z12 = (a1 * a6 + a3 * a5) * -6. + eqsq *
	((x2 * x7 + x1 * x8) * -24. - (x3 * x6 + x4 * x5) * 6.);
    z13 = a3 * -6. * a6 + eqsq * (x2 * -24. * x8 - x4 * 6. * x6);
    z21 = a2 * 6. * a5 + eqsq * (x1 * 24. * x5 - x3 * 6. * x7);
    z22 = (a4 * a5 + a2 * a6) * 6. + eqsq *
	((x2 * x5 + x1 * x6) * 24. - (x4 * x7 + x3 * x8) * 6.);
    z23 = a4 * 6. * a6 + eqsq * (x2 * 24. * x6 - x4 * 6. * x8);
    z1 = z1 + z1 + bsq * z31;
    z2 = z2 + z2 + bsq * z32;
    z3 = z3 + z3 + bsq * z33;
    s3 = cc * xnoi;
    s2 = s3 * -.5 / rteqsq;
    s4 = s3 * rteqsq;
    s1 = eq * -15. * s4;
    s5 = x1 * x3 + x2 * x4;
    s6 = x2 * x3 + x1 * x4;
    s7 = x2 * x4 - x1 * x3;
    se = s1 * zn * s5;
    si = s2 * zn * (z11 + z13);
    sl = -zn * s3 * (z1 + z3 - 14. - eqsq * 6.);

    sgh = s4 * zn * (z31 + z33 - 6.);
    sh = -zn * s2 * (z21 + z23);

    if (xqncl < .052359877)
    {
	sh = 0.;
    }

    ee2 = s1 * 2. * s6;
    e3 = s1 * 2. * s7;
    xi2 = s2 * 2. * z12;
    xi3 = s2 * 2. * (z13 - z11);
    xl2 = s3 * -2. * z2;
    xl3 = s3 * -2. * (z3 - z1);
    xl4 = s3 * -2. * (-21. - eqsq * 9.) * ze;
    xgh2 = s4 * 2. * z32;
    xgh3 = s4 * 2. * (z33 - z31);
    xgh4 = s4 * -18. * ze;
    xh2 = s2 * -2. * z22;
    xh3 = s2 * -2. * (z23 - z21);

    return;
}
