/************************/
/* sdp4.c		*/
/*			*/
/* sdp4 orbital model	*/
/************************/

/***** description
 *
 *	$Id: sdp4.c,v 2.3 2003/09/08 10:28:13 ko Exp $
 *
 */

/***** include files *****/

#include <math.h>
#include "satellite.h"
#include "aaproto.h"

/***** global variables *****/

extern struct PCONSTANTS pcnsts;
extern struct MCONSTANTS mcnsts;

/*****************/
/* SDP4 3 NOV 80 */
/*****************/

int    sdp4 (int *iflag, ELEMENT *element, double tsince)
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


    double aodp, eta; 
    double cosg, em, sing, xinc, xmam; 

    
    register int i;

    static double aycof, omgdot, x1mth2, x3thm1, x7thm1, xlcof, xmdot; 
    static double xnodcf, xnodot, xnodp;

    static double c1, c4, cosio, sinio, t2cof; 

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
	/* FOR PERIGEE BELOW 156 KM, THE VALUES OF */
	/* S AND QOMS2T ARE ALTERED */

	s4 = pcnsts.s;
	qoms24 = pcnsts.qoms2t;
	perige = (aodp * (1. - (*element).eo) - pcnsts.ae) * pcnsts.xkmper;

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
	sing = sin ((*element).omegao);
	cosg = cos ((*element).omegao);
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
	x1mth2 = 1. - theta2;
	c4 = xnodp * 2. * coef1 * aodp * betao2 *
	    (eta * (etasq * .5 + 2.) + (*element).eo * (etasq * 2. + .5)
	     - pcnsts.ck2 * 2. * tsi / (aodp * psisq) * (x3thm1 * -3.
			   * (1. - eeta * 2. + etasq * (1.5 - eeta * .5))
		      + x1mth2 * .75 * (etasq * 2. - eeta * (etasq + 1.))
					   * cos ((*element).omegao * 2.)));
	theta4 = theta2 * theta2;
	temp1 = pcnsts.ck2 * 3. * pinvsq * xnodp;
	temp2 = temp1 * pcnsts.ck2 * pinvsq;
	temp3 = pcnsts.ck4 * 1.25 * pinvsq * pinvsq * xnodp;
	xmdot = xnodp + temp1 * .5 * betao * x3thm1 + temp2 * .0625
	    * betao * (13. - theta2 * 78. + theta4 * 137.);
	x1m5th = 1. - theta2 * 5.;
	omgdot = temp1 * -.5 * x1m5th + temp2 * .0625 *
	    (7. - theta2 * 114. + theta4 * 395.) + temp3 *
	    (3. - theta2 * 36. + theta4 * 49.);
	xhdot1 = -temp1 * cosio;
	xnodot = xhdot1 + (temp2 * .5 * (4. - theta2 * 19.)
			   + temp3 * 2. * (3. - theta2 * 7.)) * cosio;
	xnodcf = betao2 * 3.5 * xhdot1 * c1;
	t2cof = c1 * 1.5;
	xlcof = a3ovk2 * .125 * sinio * (cosio * 5. + 3.) / (cosio + 1.);
	aycof = a3ovk2 * .25 * sinio;
	x7thm1 = theta2 * 7. - 1.;

	*iflag = 0;

	dpinit (eosq, sinio, cosio, betao, aodp, theta2, sing, cosg,
		betao2, xmdot, omgdot, xnodot, xnodp, (*element));

    }

    /* UPDATE FOR SECULAR GRAVITY AND ATMOSPHERIC DRAG */

    xmdf = (*element).xmo + xmdot * tsince;
    omgadf = (*element).omegao + omgdot * tsince;
    xnoddf = (*element).xnodeo + xnodot * tsince;
    tsq = tsince * tsince;
    xnode = xnoddf + xnodcf * tsq;
    tempa = 1. - c1 * tsince;
    tempe = (*element).bstar * c4 * tsince;
    templ = t2cof * tsq;
    xn = xnodp;

    dpsec (&xmdf, &omgadf, &xnode, &em, &xinc, &xn, tsince, (*element));

    a = pow (pcnsts.xke / xn, mcnsts.tothrd) * tempa * tempa;
    e = em - tempe;
    xmam = xmdf + xnodp * templ;

    dpper (&e, &xinc, &omgadf, &xnode, &xmam);

    xl = xmam + omgadf + xnode;
    beta = sqrt (1. - e * e);
    xn = pcnsts.xke / pow (a, 1.5);

    /* LONG PERIOD PERIODICS */

    axn = e * cos (omgadf);
    temp = 1. / (a * beta * beta);
    xll = temp * xlcof * axn;
    aynl = temp * aycof;
    xlt = xl + xll;
    ayn = e * sin (omgadf) + aynl;

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

    rk = r * (1. - temp2 * 1.5 * betal * x3thm1)
	+ temp1 * .5 * x1mth2 * cos2u;
    uk = u - temp2 * .25 * x7thm1 * sin2u;
    xnodek = xnode + temp2 * 1.5 * cosio * sin2u;
    xinck = xinc + temp2 * 1.5 * cosio * sinio * cos2u;
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
