/************************************************/
/* aaproto.h					*/
/*						*/
/* function prototypes for astronomical alminac	*/
/************************************************/

/***** description
 *
 *	$Id: aaproto.h,v 2.2 2003/09/04 16:02:32 ko Exp $
 *
 */

/***** modification history
 *
 *	$Log: aaproto.h,v $
 *	Revision 2.2  2003/09/04 16:02:32  ko
 *	dos2unix
 *	
 *	Revision 2.1  2001/06/21 14:20:23  oleh
 *	Release 2.1
 *	
 *	Revision 1.2  2001/04/04 14:42:45  misha
 *	*** empty log message ***
 *	
 * Revision 1.3  1993/05/18  16:32:10  craig
 * reflects various changes in the almanac software to make
 * the subroutines more flexable with reguards to printing.
 *
 * Revision 1.2  1993/04/22  21:07:42  craig
 * changed the function prototype for kinit.  kinit was changed
 * to allow for the input of a filename and a flag.
 *
 * Revision 1.1  1993/04/21  14:58:29  craig
 * Initial revision
 *
 *
 */

/***** include files *****/

/* #include "../astro.h" */		/* for POLAR struct */

#include <stdio.h>			/* for FILE */
			/* for orbit and star structs */

/***** routines in cnstinit.c *****/

void   cnstinit (void);

/***** routines in matan2.c *****/

double matan2 (double y, double x);
