/*********************************************************/
/* matan2.c						 */
/*							 */
/* return quadrant correct arc-tangent in range 0 to 2pi */
/*********************************************************/

/***** description
 *
 *	$Id: matan2.c,v 2.2 2003/09/04 16:02:33 ko Exp $
 *
 */

/***** modification history
 *
 *	$Log: matan2.c,v $
 *	Revision 2.2  2003/09/04 16:02:33  ko
 *	dos2unix
 *	
 *	Revision 2.1  2001/06/21 14:20:25  oleh
 *	Release 2.1
 *	
 *	Revision 1.2  2001/04/04 14:42:45  misha
 *	*** empty log message ***
 *	
 * Revision 1.2  1993/04/21  21:25:05  craig
 * Changed the path of the satellite.h include.
 *
 * Revision 1.1  1993/04/21  15:21:13  craig
 * Initial revision
 *
 *
 */

/***** include files *****/

#include <math.h>
#include "aaproto.h"
#include "satellite.h"

/***** global variables *****/

extern struct MCONSTANTS mcnsts;

/**********/
/* matan2 */
/**********/

double matan2 (double y, double x)
{
    double result;


    /* ansi C atan2 function returns angle between -pi and pi */

    result = atan2 (y, x);

    if (result < 0.0)
    {
	/* add 2 pi to result if in -pi to 0 range */

	result += mcnsts.twopi;
    }

    return (result);
}
