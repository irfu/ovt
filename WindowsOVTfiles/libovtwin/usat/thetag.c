/************************************************/
/* thetag.c					*/
/*						*/
/* obtain the location of Greenwich at epoch	*/
/* and convert epoch to minutes since 1950	*/
/************************************************/

/***** description
 *
 *	$Id: thetag.c,v 2.3 2003/09/08 10:28:13 ko Exp $
 *
 */

/***** modification history
 *
 *	translated by f2c (version of 12 March 1993  7:07:21).
 *
 *	$Log: thetag.c,v $
 *	Revision 2.3  2003/09/08 10:28:13  ko
 *	Removal of global variables in usat - elements, GEI, VEI.
 *	
 *	Revision 2.2  2003/09/04 16:02:34  ko
 *	dos2unix
 *	
 *	Revision 2.1  2001/06/21 14:20:26  oleh
 *	Release 2.1
 *	
 *	Revision 1.2  2001/04/04 14:42:46  misha
 *	*** empty log message ***
 *	
 * Revision 1.3  1993/04/27  21:20:45  craig
 * fixed the double constant mjd1950 to reflect the current use of
 * using the julian day number instead of the mjd number.
 *
 * Revision 1.2  1993/04/02  18:06:20  craig
 * fixed update of ds50 using the mjd epoch
 *
 * Revision 1.1  1993/04/01  21:06:52  craig
 * Initial revision
 *
 *
 */

/***** include files *****/

#include "satellite.h"

/***** global variable *****/

extern struct MCONSTANTS mcnsts;

/**********/
/* thetag */
/**********/

double thetag (double ep, ELEMENT element)
{
    /* System generated locals */

    double ret_val;

    /* Local variables */

    int i;

    double temp, theta;

    static double mjd1950 = 2433281.5;	/* mjd day number for 1950 0 jan */

    element.ds50 = ep - mjd1950;
    theta = element.ds50 * 6.3003880987 + 1.72944494;
    temp = theta / mcnsts.twopi;
    i = (int) temp;
    temp = (double) i;
    ret_val = theta - temp * mcnsts.twopi;

    if (ret_val < 0.)
    {
	ret_val += mcnsts.twopi;
    }

    return (ret_val);
}	
