/********************************************************/
/* mjd.c						*/
/*							*/
/* convert gregorian date to julian day number		*/
/* 							*/
/* algorithm from the Explanatory supplement to the	*/
/* Astronomical Almanac					*/
/********************************************************/

/***** description
 *
 *	$Id: mjd.c,v 2.3 2003/09/08 10:28:12 ko Exp $
 *
 */


/***** include files *****/


/*******/
/* mjd */
/*******/

double mjd (long year, long month, double day)
{
    long    terma, termb, termc;
    double  jday;

    terma = (1461 * (year + 4800 + ((month - 14) / 12))) / 4;

    termb = (367 * (month - 2 - (12 * ((month - 14) / 12)))) / 12;

    termc = (3 * ((year + 4900 + ((month - 14) / 12)) / 100)) / 4;

    jday = terma + termb - termc + day - 32075.5;

    //jday = jday - 2400000.5; 
    
    return (jday);
}	
