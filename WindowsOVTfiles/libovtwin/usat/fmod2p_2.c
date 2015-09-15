/************************************************/
/* fmod2p.c					*/
/*						*/
/* given an angle in radians, returns an angle	*/
/* in radians in the range of 0 to 2pi.		*/
/************************************************/

/***** description
 *
 *	$Id: fmod2p_2.c,v 2.3 2003/09/08 10:28:12 ko Exp $
 *
 */


/***** include files *****/

#include "satellite.h"

/***** global variables *****/

extern struct MCONSTANTS mcnsts;

/**********/
/* fmod2p */
/**********/

double fmod2p_2 (double x)
{
    /* System generated locals */

    double ret_val;

    /* Local variables */

    int i;

    ret_val = x;
    i = ret_val / mcnsts.twopi;
    ret_val -= i * mcnsts.twopi;

    if (ret_val < 0) 
    {
	ret_val += mcnsts.twopi;
    }
   
    return (ret_val);
}
