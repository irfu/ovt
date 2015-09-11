/************************/
/* sgp4.c		*/
/*			*/
/*  sgp4 orbital model	*/
/************************/

/***** description
 *
 *     $Id: sgp4.c,v 2.3 2003/09/08 10:28:13 ko Exp $
 *
 */


/***** include files *****/

#include <math.h>
#include "satellite.h"
#include "aaproto.h"

/***** global variables *****/

extern struct PCONSTANTS pcnsts;
extern struct MCONSTANTS mcnsts;


/******************/
/* SGP4  3 NOV 80 */
/******************/

/************************************************************************************************* 
 *
 * The original subroutine - int    sgp4 (int *iflag, double tsince)
 * 
 *
 * Input:
 *	iflag - could not get what it stands for. Definitely it helps to speed up the process
 *		and not to compute some variables once again. 
 *		It is always safe to use iflag = 1. 
 *	elem - the orbit element taken from TLE
 *	tsince  - the time from (*element).epoch for wich the velocity and position will be computed 
 * Output:
 *	(*element).x,y,z - position
 *	(*element).xdot,ydot,zdot - velocity
 *
 *************************************************************************************************/ 

int    sgp4 (int *iflag, ELEMENT *element, double tsince)
{
    /* Local variables */

    double a3ovk2, axn, ayn, aynl, beta, betal, betao, betao2;
    double capu, coef, coef1, ddummy, del1, delo;
    double ecose, eeta, elsq, eosq, epw, esine, etasq;
    double omgadf, perige, pinvsq, psisq, qoms24, rdot;
    double rdotk, rfdot, rfdotk, theta2, theta4, tsi, tsq;
    double x1m5th, xhdot1, xinck, xll, xlt, xmdf, xmx, xmy;
    double xnoddf, xnode, xnodek;

    double temp, temp1, temp2, temp3, temp4, temp5, temp6;
    double tempa, tempe, templ;

    double cosu, sinu, cos2u, sin2u, cosik, sinik, cosuk, sinuk;
    double cosnok, sinnok, cosepw, sinepw;

    double a, e, r, u;

    double a1, ao, c2, pl, rk, s4, uk, ux, uy, uz;
    double vx, vy, vz, xl, xn;


    double c1sq, c3, delm, delomg, omega, tcube, tfour, xmp;


    static int isimp;

    register int i;

    static double aycof, omgdot, x1mth2, x3thm1, x7thm1, xlcof, xmdot;
    static double xnodcf, xnodot, xnodp;

    static double c1, c4, cosio, sinio, t2cof;


    static double aodp, eta;
    static double c5, d2, d3, d4, delmo, omgcof, sinmo, xmcof;
    static double t3cof, t4cof, t5cof;


    if (*iflag != 0)
    {

	/* RECOVER ORIGINAL MEAN MOTION (XNODP) AND SEMIMAJOR AXIS (AODP) */
	/* FROM INPUT ELEMENTS */

	ddummy = pcnsts.xke / (*element).xno;
	a1 = pow (ddummy, mcnsts.tothrd);
	cosio = cos ((*element).xincl);
	theta2 = cosio * cosio;
	x3thm1 = theta2 * 3. - 1.;
	eosq = (*element).eo * (*element).eo;
	betao2 = 1. - eosq;
	betao = sqrt (betao2);
	del1 = pcnsts.ck2 * 1.5 * x3thm1 / (a1 * a1 * betao * betao2);
	ao = a1 * (1. - del1 * (mcnsts.tothrd * .5 + del1 *
				(del1 * 134. / 81. + 1.)));
	delo = pcnsts.ck2 * 1.5 * x3thm1 / (ao * ao * betao * betao2);
	xnodp = (*element).xno / (delo + 1.);
	aodp = ao / (1. - delo);

	/* INITIALIZATION */
	/* FOR PERIGEE LESS THAN 220 KILOMETERS, THE ISIMP FLAG IS SET AND */
	/* THE EQUATIONS ARE TRUNCATED TO LINEAR VARIATION IN SQRT A AND */
	/* QUADRATIC VARIATION IN MEAN ANOMALY.  ALSO, THE C3 TERM, THE */
	/* DELTA OMEGA TERM, AND THE DELTA M TERM ARE DROPPED. */

	if (aodp * (1. - (*element).eo) / pcnsts.ae <
		220. / pcnsts.xkmper + pcnsts.ae)
	{
	    isimp = 1;
	}
	else
	{
	    isimp = 0;
	}

	s4 = pcnsts.s;
	qoms24 = pcnsts.qoms2t;
	perige = (aodp * (1. - (*element).eo) - pcnsts.ae) * pcnsts.xkmper;

	/* FOR PERIGEE BELOW 156 KM, THE VALUES OF */
	/* S AND QOMS2T ARE ALTERED */

	if (perige < 156.)
	{
	    if (perige > 98.)
	    {
		s4 = perige - 78.;
	    }
	    else
	    {
		s4 = 20.;
	    }

	    qoms24 = pow ((120. - s4) * pcnsts.ae / pcnsts.xkmper, 4.);
	    s4 = s4 / pcnsts.xkmper + pcnsts.ae;
	}

	pinvsq = 1. / (aodp * aodp * betao2 * betao2);
	tsi = 1. / (aodp - s4);
	eta = aodp * (*element).eo * tsi;
	etasq = eta * eta;
	eeta = (*element).eo * eta;
	psisq = fabs (1. - etasq);

	coef = qoms24 * pow (tsi, 4.);
	coef1 = coef / pow (psisq, 3.5);
	c2 = coef1 * xnodp * (aodp * (etasq * 1.5 + 1.
		  + eeta * (etasq + 4.)) + pcnsts.ck2 * .75 * tsi / psisq
			    * x3thm1 * (etasq * 3. * (etasq + 8.) + 8.));
	c1 = (*element).bstar * c2;
	sinio = sin ((*element).xincl);

	a3ovk2 = -pcnsts.xj3 / pcnsts.ck2 * pow (pcnsts.ae, 3.);
	c3 = coef * tsi * a3ovk2 * xnodp * pcnsts.ae * sinio / (*element).eo;
	x1mth2 = 1. - theta2;
	c4 = xnodp * 2. * coef1 * aodp * betao2 *
	    (eta * (etasq * .5 + 2.) + (*element).eo * (etasq * 2. + .5)
	     - pcnsts.ck2 * 2. * tsi / (aodp * psisq) * (x3thm1 * -3.
			   * (1. - eeta * 2. + etasq * (1.5 - eeta * .5))
		      + x1mth2 * .75 * (etasq * 2. - eeta * (etasq + 1.))
					   * cos ((*element).omegao * 2.)));
	c5 = coef1 * 2. * aodp * betao2 * ((etasq + eeta)
					   * 2.75 + 1. + eeta * etasq);
	theta4 = theta2 * theta2;
	temp1 = pcnsts.ck2 * 3. * pinvsq * xnodp;
	temp2 = temp1 * pcnsts.ck2 * pinvsq;
	temp3 = pcnsts.ck4 * 1.25 * pinvsq * pinvsq * xnodp;
	xmdot = xnodp + temp1 * .5 * betao * x3thm1 + temp2
	    * .0625 * betao * (13. - theta2 * 78. + theta4 * 137.);
	x1m5th = 1. - theta2 * 5.;
	omgdot = temp1 * -.5 * x1m5th + temp2 * .0625
	    * (7. - theta2 * 114. + theta4 * 395.) + temp3
	    * (3. - theta2 * 36. + theta4 * 49.);
	xhdot1 = -temp1 * cosio;

	xnodot = xhdot1 + (temp2 * .5 * (4. - theta2 * 19.)
			   + temp3 * 2. * (3. - theta2 * 7.)) * cosio;
	omgcof = (*element).bstar * c3 * cos ((*element).omegao);
	xmcof = -mcnsts.tothrd * coef * (*element).bstar * pcnsts.ae / eeta;

	xnodcf = betao2 * 3.5 * xhdot1 * c1;
	t2cof = c1 * 1.5;
	xlcof = a3ovk2 * .125 * sinio * (cosio * 5. + 3.) / (cosio + 1.);
	aycof = a3ovk2 * .25 * sinio;


	delmo = pow (1. + eta * cos ((*element).xmo), 3.);
	sinmo = sin ((*element).xmo);
	x7thm1 = theta2 * 7. - 1.;

	if (isimp != 1)
	{
	    c1sq = c1 * c1;
	    d2 = aodp * 4. * tsi * c1sq;
	    temp = d2 * tsi * c1 / 3.;
	    d3 = (aodp * 17. + s4) * temp;
	    d4 = temp * .5 * aodp * tsi * (aodp * 221. + s4 * 31.) * c1;
	    t3cof = d2 + c1sq * 2.;
	    t4cof = (d3 * 3. + c1 * (d2 * 12. + c1sq * 10.)) * .25;
	    t5cof = (d4 * 3. + c1 * 12. * d3 + d2 * 6. * d2
		     + c1sq * 15. * (d2 * 2. + c1sq)) * .2;
	}
	*iflag = 0;
    }

    /* UPDATE FOR SECULAR GRAVITY AND ATMOSPHERIC DRAG */

    xmdf = (*element).xmo + xmdot * tsince;
    omgadf = (*element).omegao + omgdot * tsince;
    xnoddf = (*element).xnodeo + xnodot * tsince;
    omega = omgadf;
    xmp = xmdf;
    tsq = tsince * tsince;
    xnode = xnoddf + xnodcf * tsq;
    tempa = 1. - c1 * tsince;
    tempe = (*element).bstar * c4 * tsince;
    templ = t2cof * tsq;

    if (isimp != 1)
    {
	delomg = omgcof * tsince;

	delm = xmcof * (pow (1. + eta * cos (xmdf), 3.) - delmo);
	temp = delomg + delm;
	xmp = xmdf + temp;
	omega = omgadf - temp;
	tcube = tsq * tsince;
	tfour = tsince * tcube;
	tempa = tempa - d2 * tsq - d3 * tcube - d4 * tfour;
	tempe += (*element).bstar * c5 * (sin (xmp) - sinmo);
	templ = templ + t3cof * tcube + tfour * (t4cof + tsince * t5cof);
    }

    a = aodp * tempa * tempa;
    e = (*element).eo - tempe;
    xl = xmp + omega + xnode + xnodp * templ;
    beta = sqrt (1. - e * e);
    xn = pcnsts.xke / pow (a, 1.5);

    /* LONG PERIOD PERIODICS */

    axn = e * cos (omega);
    temp = 1. / (a * beta * beta);
    xll = temp * xlcof * axn;
    aynl = temp * aycof;
    xlt = xl + xll;
    ayn = e * sin (omega) + aynl;

    /* SOLVE KEPLERS EQUATION */

    capu = fmod2p (xlt - xnode);
    temp2 = capu;

    for (i = 1; i <= 10; ++i)
    {
	sinepw = sin (temp2);
	cosepw = cos (temp2);
	temp3 = axn * sinepw;
	temp4 = ayn * cosepw;
	temp5 = axn * cosepw;
	temp6 = ayn * sinepw;
	epw = (capu - temp4 + temp3 - temp2) / (1. - temp5 - temp6) + temp2;

	if (fabs (epw - temp2) <= mcnsts.e6a)
	{
	    break;
	}

	temp2 = epw;
    }

    /* SHORT PERIOD PRELIMINARY QUANTITIES */

    ecose = temp5 + temp6;
    esine = temp3 - temp4;
    
    
    
    elsq = axn * axn + ayn * ayn;
    temp = 1. - elsq;
    pl = a * temp;
    r = a * (1. - ecose);
    temp1 = 1. / r;
    rdot = pcnsts.xke * sqrt (a) * esine * temp1;
    rfdot = pcnsts.xke * sqrt (pl) * temp1;
    temp2 = a * temp1;
    betal = sqrt (temp);
    temp3 = 1. / (betal + 1.);
 
    cosu = temp2 * (cosepw - axn + ayn * esine * temp3);
    sinu = temp2 * (sinepw - ayn - axn * esine * temp3);
    u = matan2 (sinu, cosu);
    sin2u = sinu * 2. * cosu;
    cos2u = cosu * 2. * cosu - 1.;
    temp = 1. / pl;
    temp1 = pcnsts.ck2 * temp;
    temp2 = temp1 * temp;

    /* UPDATE FOR SHORT PERIODICS */

    rk = r * (1. - temp2 * 1.5 * betal * x3thm1) +
	temp1 * .5 * x1mth2 * cos2u;

    uk = u - temp2 * .25 * x7thm1 * sin2u;
    xnodek = xnode + temp2 * 1.5 * cosio * sin2u;
    xinck = (*element).xincl + temp2 * 1.5 * cosio * sinio * cos2u;
    rdotk = rdot - xn * temp1 * x1mth2 * sin2u;
    rfdotk = rfdot + xn * temp1 * (x1mth2 * cos2u + x3thm1 * 1.5);

    /* ORIENTATION VECTORS */

 
    sinuk = sin (uk);
    cosuk = cos (uk);
    sinik = sin (xinck);
    cosik = cos (xinck);
    sinnok = sin (xnodek);
    cosnok = cos (xnodek);
    xmx = -sinnok * cosik;
 
 
    xmy = cosnok * cosik;
    ux = xmx * sinuk + cosnok * cosuk;
    uy = xmy * sinuk + sinnok * cosuk;
    uz = sinik * sinuk;
    vx = xmx * cosuk - cosnok * sinuk;
    vy = xmy * cosuk - sinnok * sinuk;
    vz = sinik * cosuk;

    /* POSITION AND VELOCITY */

    (*element).x = rk * ux;
    (*element).y = rk * uy;
    (*element).z = rk * uz;
    (*element).xdot = rdotk * ux + rfdotk * vx;
    (*element).ydot = rdotk * uy + rfdotk * vy;
    (*element).zdot = rdotk * uz + rfdotk * vz;

    return (0);
}
