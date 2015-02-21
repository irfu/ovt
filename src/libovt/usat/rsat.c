/****************************************************************/
/* rsat.c							*/
/*								*/
/* The following program produces topocentric geocentric right	*/
/* ascension,  declination, altitude, and azimuth of the	*/
/* satellite .							*/
/****************************************************************/

/***** description
 *
 *	$Id: rsat.c,v 2.2 2003/09/04 16:02:33 ko Exp $
 *
 */

/***** modification history
 *
 *	$Log: rsat.c,v $
 *	Revision 2.2  2003/09/04 16:02:33  ko
 *	dos2unix
 *	
 *	Revision 2.1  2001/06/21 14:20:25  oleh
 *	Release 2.1
 *	
 *	Revision 1.4  2001/04/07 17:10:53  misha
 *	*** empty log message ***
 *	
 *	Revision 1.3  2001/04/05 18:12:20  ko
 *	*** empty log message ***
 *	
 *	Revision 1.2  2001/04/04 14:42:46  misha
 *	*** empty log message ***
 *	
 * Revision 1.8  1993/07/12  19:34:07  craig
 * Changed call to mjd to be longs instead of ints.  Added warning
 * for MSDOS and VMS users reguarding fread.
 *
 * Revision 1.7  1993/05/12  18:55:31  craig
 * satreduce just returns the alt, az, ra, and dec now.  Moved all
 * of the decision making and printing to search.c..
 *
 * Added a new subroutine --- do_orbit which gets the geocentric
 * retangular coordinate using the NORAD software.
 *
 * Revision 1.6  1993/05/05  19:14:20  craig
 * Added an additional output option that only gives the satellite name
 * and time.
 *
 * Revision 1.4  1993/04/30  18:29:08  craig
 * Removed most of the error messages from rdelement ().
 *
 * Revision 1.3  1993/04/30  14:39:17  craig
 * rdelement has been moved to this file.
 *
 * Revision 1.2  1993/04/27  17:11:17  craig
 * Set prtflg to 0 so that only Topocentric coordinate info is
 * displayed.
 *
 * Revision 1.1  1993/04/26  19:26:24  craig
 * Initial revision
 *
 * Remade by M.Khodosko 2001/03/24 
 */

/***** include files *****/

#include <string.h>
#include <ctype.h>
#include <math.h>
#include "satproto.h"
#include "satellite.h"
#include "aaproto.h"

/***** global variables *****/


extern FILE *elefile;
struct ELEMENT element;

/* from cnstinit.c */

extern double JD;
extern double Gei[3];
extern double Vei[3];

extern struct PCONSTANTS pcnsts;
extern struct MCONSTANTS mcnsts;


/*************/
/* rdelement */
/*************/

int KOI(void) 
{
  char test1[5];
  double test2;

  strcpy(test1,"77.77");
  sscanf(test1,"%lf",&test2);
  if (test2 == 77.77) 
    return (1);
  else
    return (2);
}
int rdelement (void)
{
    int    idummy, csum;
    char   eline[80], tstrng[20];
    double ddummy;
    int k;
    int kod; 
    char pchar;
    register i;

    for (i = 0; i < 80; i++)
    {
	eline[i] = '\0';
    }

    /***** read in mean elements from 2 card trans format *****/
    /*** read the first "card" into the line buffer ***/

    if (fgets (element.name, 71, elefile) == NULL)
    {
	return (-1);
    }

    /* fprintf (stdout, "%s\n", element.name); */
    /*** remove the carriage return from the name ***/
    for (i = 0; i < strlen (element.name); i++)
    {
	if (iscntrl (element.name[i]))
	{
	    element.name[i] = 0x20;		/* space */
	}
    }
    /*** read the second "card" into the line buffer ***/

    /* Graeme Waddington (wgw@vax.ox.ac.uk) reports that fread
     * on VMS and MSDOS systems have some trouble with the <cr><lf>
     * at the end of the element line.  He suggested changing the 
     * 70 below to a 71 to correct the problem.  Perhaps using fgets
     * instead of fread may work also.
     */

    if (fgets (eline, 71, elefile) == NULL)
    {
      return (-1);
    }
    
    if (KOI() != 1){    
     for (i = 0; i < 71; i++){

       if (eline[i] == '.') 
	 eline[i] = ',';    
     }
     pchar = ',';
    }
    else
     pchar = '.';


    /*** check checksum ***/

    csum = 0;
    for (i = 0; i < 68; i++)
    {
	if (eline[i] == '-')
	{
	    csum++;
	}
	else
	{
	    idummy = (int) eline[i]; 
	    if (isdigit (idummy) != 0)
	    {
                if (sscanf (&eline[i], "%1d", &idummy) != 1)
                {
	            return (-1);
                }
		csum += idummy;
	    }
	}
    }
    csum = csum % 10;

    tstrng[0] = '\0';
    strncat (tstrng, &eline[68], 1);

    if (sscanf (tstrng, "%d", &idummy) != 1)
    {
	return (-1);
    }

    if (idummy != csum)
    {
	fprintf (stderr, "checksum error: %d != %d\n", csum, idummy);
	return (-1);
    }

    /*** epoch ***/

    tstrng[0] = '\0';
    strncat (tstrng, &eline[18], 2);

    if (sscanf (tstrng, "%d", &idummy) != 1)
    {
	return (-1);
    }

    tstrng[0] = '\0';
    strncat (tstrng, &eline[20], 12);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
	return (-1);
    }

    /* get the mjd day number for the epoch year */
    if(idummy<50)
     idummy += 2000;
    else
     idummy += 1900;
    element.epoch = mjd ((long) idummy, 1L, 0.0);

    /* add the epoch julian day */

    element.epoch += ddummy;		/* modified julian day number */

    /*** xndt2o ***/

    tstrng[0] = '\0';
    strncat (tstrng, &eline[33], 10);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
	return (-1);
    }

    element.xndt2o = ddummy * mcnsts.twopi / pcnsts.xmnpda /
	pcnsts.xmnpda;

    /*** xndd6o ***/

    tstrng[0] = '\0';
    strncat (tstrng, &eline[44], 1);	/* sign field */
    tstrng[1] = '0';
    tstrng[2] = pchar;
    tstrng[3] = '\0';
    strncat (tstrng, &eline[45], 5);	/* number field */

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
	return (-1);
    }

    if (ddummy == 0.)
    {
	element.xndd6o = 0.;
    }
    else
    {
	tstrng[0] = '\0';
	strncat (tstrng, &eline[50], 2);/* exponent field */

	if (sscanf (tstrng, "%d", &idummy) != 1)
	{
	    return (-1);
	}

	element.xndd6o = ddummy * pow (10., (double) idummy);
	element.xndd6o = element.xndd6o * mcnsts.twopi /
	    pow (pcnsts.xmnpda, 3.);
    }

    /*** bstar ***/

    tstrng[0] = '\0';
    strncat (tstrng, &eline[53], 1);	/* sign field */
    tstrng[1] = '0';
    tstrng[2] = pchar;
    tstrng[3] = '\0';
    strncat (tstrng, &eline[54], 5);	/* number field */

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
	return (-1);
    }

    if (ddummy == 0.)
    {
	element.bstar = 0.;
    }
    else
    {
	tstrng[0] = '\0';
	strncat (tstrng, &eline[59], 2);/* exponent field */

	if (sscanf (tstrng, "%d", &idummy) != 1)
	{
	    return (-1);
	}

	element.bstar = ddummy * pow (10., (double) idummy) / pcnsts.ae;
    }

    /*** read the third "card" into the line buffer ***/


    /* Graeme Waddington (wgw@vax.ox.ac.uk) reports that fread
     * on VMS and MSDOS systems have some trouble with the <cr><lf>
     * at the end of the element line.  He suggested changing the 
     * 70 below to a 71 to correct the problem.  Perhaps using fgets
     * instead of fread may work also.
     */

    if (fgets (eline, 70, elefile) == NULL)
    {
	return (-1);
    }

    if (KOI() != 1){ 
     for (i = 0; i < 70; i++){

      if (eline[i] == '.') 
	eline[i] = ',';     
      } 
    }

    /*** check checksum ***/

    csum = 0;
    for (i = 0; i < 68; i++)
    {
	if (eline[i] == '-')
	{
	    csum++;
	}
	else
	{
	    idummy = (int) eline[i];
	    if (isdigit (idummy) != 0)
	    {
                if (sscanf (&eline[i], "%1d", &idummy) != 1)
                {
	            return (-1);
                }
		csum += idummy;
	    }
	}
    }
    csum = csum % 10;

    tstrng[0] = '\0';
    strncat (tstrng, &eline[68], 1);

    if (sscanf (tstrng, "%d", &idummy) != 1)
    {
	return (-1);
    }

    if (idummy != csum)
    {
	fprintf (stderr, "checksum error: %d != %d\n", csum, idummy);
	return (-1);
    }

    /*** xincl ***/

    tstrng[0] = '\0';
    strncat (tstrng, &eline[8], 8);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
	return (-1);
    }

    element.xincl = ddummy * mcnsts.de2ra;

    /*** xnodeo ***/

    tstrng[0] = '\0';
    strncat (tstrng, &eline[17], 8);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
	return (-1);
    }

    element.xnodeo = ddummy * mcnsts.de2ra;

    /*** eo ***/

    tstrng[0] = pchar;
    tstrng[1] = '\0';
    strncat (tstrng, &eline[26], 7);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
	return (-1);
    }

    element.eo = ddummy;

    /*** omegao ***/

    tstrng[0] = '\0';
    strncat (tstrng, &eline[34], 8);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
	return (-1);
    }

    element.omegao = ddummy * mcnsts.de2ra;

    /*** xmo ***/

    tstrng[0] = '\0';
    strncat (tstrng, &eline[43], 8);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
	return (-1);
    }

    element.xmo = ddummy * mcnsts.de2ra;

    /*** xno ***/

    tstrng[0] = '\0';
    strncat (tstrng, &eline[52], 11);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
	return (-1);
    }

    element.xno = ddummy * mcnsts.twopi / pcnsts.xmnpda;

    /***** check for valid elements *****/

    if (element.xno <= 0.)
    {
	return (-1);
    }

    return (0);
}

/***********************************************************/
/* do_orbit						   */
/*							   */
/* update the satellite geocentric rectangular coordinates */
/***********************************************************/

void do_orbit (int iflag, int orbflag)
{
    double tsince;

    tsince = (JD - element.epoch) * pcnsts.xmnpda;

    switch (orbflag)
    {
      case 1:
	sgp4 (&iflag, tsince);
	break;

      case 2:
	sdp4 (&iflag, tsince);
	break;
    }
    element.x = element.x * pcnsts.xkmper / pcnsts.ae;

    element.y = element.y * pcnsts.xkmper / pcnsts.ae;

    element.z = element.z * pcnsts.xkmper / pcnsts.ae;

    element.xdot = element.xdot * pcnsts.xkmper / pcnsts.ae * pcnsts.xmnpda / pcnsts.secpda;

    element.ydot = element.ydot * pcnsts.xkmper / pcnsts.ae * pcnsts.xmnpda / pcnsts.secpda;

    element.zdot = element.zdot * pcnsts.xkmper / pcnsts.ae * pcnsts.xmnpda / pcnsts.secpda;

    Gei[0] = element.x;
    Gei[1] = element.y;
    Gei[2] = element.z;

    Vei[0] = element.xdot;
    Vei[1] = element.ydot;
    Vei[2] = element.zdot;

    return;
}
