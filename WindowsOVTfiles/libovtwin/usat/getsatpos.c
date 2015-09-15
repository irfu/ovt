//////////////////////////
// Added by M.Khodosko //
////////////////////////

#include <jni.h>
#include "ovt_object_TLESat.h"
#include "aaproto.h"
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include <stdio.h>
#include "satellite.h"


extern struct PCONSTANTS pcnsts;
extern struct MCONSTANTS mcnsts;


/**
 * Returns satellite's postion and velocity for the specified times.
 * Input: 
 *        OrbitFileName - filename
 *        mjd - double array of mjds. All mjds should be in the order: mjd[0] < mjd[1] < ... < mjd[numberOfMjds-1]
 *        numberOfMjds - number of mjds
 * Output:
 *        gei - array of positions of the satellite in GEI coordinate system
 *        vei - array of velocities of the satellite in GEI coordinate system 
 */
int get_sat_pos_and_vel (char *filename, double *mjd, double *gei[], double* vei[], int numberOfMjds) 
{
    /***** Local variables *****/

    double delo, delt, del1, temp;
    double a1, xnodp, ao, ae;
    double N, D, x;
    int   k, iflag=0, i, use_deep_space;
    int iept=0,line_number=2;
    double oalt=0, oaz=0, ora=0, odec=0, firstJD;
    FILE *tlefile;
    double JD=0, tsince;
    ELEMENT element, last_element;

    /***** initialize constants *****/
    element.epoch = 0;
    cnstinit ();

    /*** open the TLE file ***/

    tlefile = fopen (filename, "r");
    if (tlefile == NULL) {
    	fprintf (stderr, "Error in opening TLE file: %s\n", filename);
    	return (-1);
    }
    
    /*fseek(tlefile,0,SEEK_SET);  moving pointer to the begining of file.tle */
   
    /* DEBUG 
    printf("File %s opened successfuly. Curent position is %d\n",filename,ftell(tlefile)); */
     
    

    if (read_element(tlefile, &last_element) != 0) {
    	fprintf(stderr,"%s:%d,%d: error: read_element problem!\n", filename, line_number,line_number+1);
		return -1;
    }
    line_number+=2;
    firstJD = last_element.epoch; /* the time of the first data in the TLE file */ 
    copy_element(&last_element, &element);
    

    

    /* go through all mjd's and fill in gei[][] and vei[][] */
 
    for (i=0; i<numberOfMjds; i++) { 
	iflag = 1;
	
	JD = mjd[i] + 2433282.50; /*is used to convert MJD to JD */
	
	/* choose the model - NEAR or DEEP SPACE. Let us do it every time, because orbit 
	   can change in principle  */
	/***** INPUT CHECK FOR PERIOD VS EPHEMERIS SELECTED *****/
   	/***** PERIOD GE 225 MINUTES  IS DEEP SPACE *****/   
    	a1 = pow (pcnsts.xke / element.xno, mcnsts.tothrd);
    	temp = pcnsts.ck2 * 1.5 *
      		(cos (element.xincl) * cos (element.xincl) * 3. - 1.) /
       		pow (1. - element.eo * element.eo, 1.5);

    	del1 = temp / (a1 * a1);
    	ao = a1 * (1. - del1 * (mcnsts.tothrd * .5 + del1 *
        	(del1 * 134. / 81. + 1.)));
    	delo = temp / (ao * ao);
    	xnodp = element.xno / (delo + 1.);
   
    	if (mcnsts.twopi / xnodp / pcnsts.xmnpda >= .15625)
    	{	
    		use_deep_space = 1; /* DEEP SPACE (sdp4) will be used */
    	}
    		else use_deep_space = 0;    /* NEAR SPACE (sgp4) will be used */

	
	/* DE BUG 
        printf("for loop. mjd[%d]. Curent fpos=%d. JD=%f last_element.epoch=%f\n",i,ftell(tlefile),JD, last_element.epoch); 
    */	
	
        if (JD < firstJD) {
          printf("The requested mjd[%d] = %f is earlier then the first mjd (%f), file='%s'\n",i,mjd[i],firstJD - 2433282.5,filename);
          fclose(tlefile); return(-1);
        }   
        if (JD < element.epoch) {
	  printf("Error: The elements in mjd[] array are not in order!/n"); 
          printf("The requested mjd[%d] = %f is earlier then elements mjd (%f), file='%s'\n",i,mjd[i],element.epoch - 2433282.5,filename);
          fclose(tlefile); return(-1);
        }   
        
	/* search for the element */ 
        while (last_element.epoch < JD) {            
            copy_element(&last_element, &element); 

            if (read_element(tlefile, &last_element) != 0) { 
				printf("Error in get_sat_pos_and_vel : read_element problem. File: '%s' Line #%d/%d\n",filename, line_number,line_number+1);
				printf("\tRequested mjd[%d] = %f\n",i,mjd[i]); 
		        fclose(tlefile); return(-1);
            };
	    	line_number+=2;
			
	    	/* printf("read_element. mjd[%d]. Curent line is %d",i,line_number);
	    	printf(" JD=%f last_element.epoch=%f ", JD, last_element.epoch);
	    	printf(" feof()=%d\n",feof(tlefile)); */
        }       
               
		tsince = (JD - element.epoch) * pcnsts.xmnpda;

    	switch (use_deep_space)
        {
      	  case 0:
        	sgp4 (&iflag, &element, tsince);
          break;

      	  case 1:
        	sdp4 (&iflag, &element, tsince);
          break;
    	}
    	gei[i][0] = element.x * pcnsts.xkmper / pcnsts.ae;
        gei[i][1] = element.y * pcnsts.xkmper / pcnsts.ae;
        gei[i][2] = element.z * pcnsts.xkmper / pcnsts.ae;

        vei[i][0] = element.xdot * pcnsts.xkmper / pcnsts.ae * pcnsts.xmnpda / pcnsts.secpda;
        vei[i][1] = element.ydot * pcnsts.xkmper / pcnsts.ae * pcnsts.xmnpda / pcnsts.secpda;
        vei[i][2] = element.zdot * pcnsts.xkmper / pcnsts.ae * pcnsts.xmnpda / pcnsts.secpda;

        /* fseek(elefile,0,SEEK_SET); moving pointer to the begining of file.tle */
  }

  /*  if (use_deep_space == 1)
    {
      printf("Sdp4 was used\n");
    }
    if (use_deep_space == 0)
    {
      printf("Sgp4 was used\n");
    } */

    fclose(tlefile);
    return 0;
}

/**
 * Copies source_element to target_element
 */
copy_element(ELEMENT *source_elem, ELEMENT *target_elem)
{

    (*target_elem).xmo    = (*source_elem).xmo;
    (*target_elem).xnodeo = (*source_elem).xnodeo;
    (*target_elem).omegao = (*source_elem).omegao;
    (*target_elem).eo     = (*source_elem).eo;
    (*target_elem).xincl  = (*source_elem).xincl;   
    (*target_elem).xno    = (*source_elem).xno;
    (*target_elem).xndt2o = (*source_elem).xndt2o;
    (*target_elem).xndd6o = (*source_elem).xndd6o;
    (*target_elem).bstar  = (*source_elem).bstar;
    (*target_elem).epoch  = (*source_elem).epoch;
    (*target_elem).ds50   = (*source_elem).ds50;   
}


/** JNI - called from Sat.java
  Input:
	OrbitFileName(jfilename) 
  	array of mjd(jmjd)
	size of mjd array(jn) 
  Output:
	array of gei[jn][3](jgei) 
	array of vei[jn][3](jvei)
	ErrorCode : 0 if everything was ok, -1 in the case of a problem
*/ 
JNIEXPORT jint JNICALL
Java_ovt_object_TLESat_getSatPosJNI(env,obj,jfilename,jmjd,jgei,jvei,jn)
JNIEnv* env;
jobject obj; 
jdoubleArray jmjd;
jstring jfilename;
jobjectArray jgei;
jobjectArray jvei;
const jint jn; 
{
jdouble *mjd;
jint result;
int i,j,errCode=0;
jdoubleArray temp_gei;
jdoubleArray temp_vei;
jdouble** gei = (jdouble**)calloc(jn,sizeof(jdouble*)); 
jdouble** vei = (jdouble**)calloc(jn,sizeof(jdouble*));

char fname[70];

    const char *filename = (*env)->GetStringUTFChars(env,jfilename,0);
    mjd =(*env)->GetDoubleArrayElements(env,jmjd,0);
    strcpy(fname,filename);   
    for (i=0; i<jn; i++) {
      jdoubleArray temp_gei = (jdoubleArray)(*env)->GetObjectArrayElement(env,jgei,i);/* point temp_gei to the one row of array jgei*/  
      jdoubleArray temp_vei = (jdoubleArray)(*env)->GetObjectArrayElement(env,jvei,i);/* point temp_vei to the one row of array jvei*/         
      gei[i] =(*env)->GetDoubleArrayElements(env,temp_gei, 0);/*point gei to the double array[][] - jgei*/
      vei[i] =(*env)->GetDoubleArrayElements(env,temp_vei, 0);/*point vei to the double array[][] - jvei*/ 
    }
   
  if (get_sat_pos_and_vel(fname,mjd,gei,vei,jn) != 0) {
    printf("Error in get_sat_pos_and_vel\n");
    errCode = 1;
  }

/*       Misha's proposition - dead duck
  (*env)->ReleaseStringUTFChars(env, jfilename, filename); 
  for (i=0; i<jn; i++) {
    (*env)->ReleaseDoubleArrayElements(env, gei_rel[i], gei[i], 0);
    (*env)->ReleaseDoubleArrayElements(env, vei_rel[i], vei[i], 0);  
   }
*/
// ---- Yuri's idea ----
    for  (i=0;  i<jn;  i++)  {
	jdoubleArray  temp_gei  =  (jdoubleArray)(*env)->GetObjectArrayElement(env,jgei,i);
	jdoubleArray  temp_vei  =  (jdoubleArray)(*env)->GetObjectArrayElement(env,jvei,i);    
	(*env)->ReleaseDoubleArrayElements(env,  temp_gei,  gei[i],  0);
	(*env)->ReleaseDoubleArrayElements(env,  temp_vei,  vei[i],  0);
    }
// ----

  free(gei);
  free(vei);
  return errCode;
}

/*******************************/
/* KOI is used by read_element */
/*******************************/

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

/**
 * Reads the element from TLE file 
 * Element contains topocentric geocentric right  
 * ascension,  declination, altitude, and azimuth of the        
 * satellite.
 **/
int read_element (FILE *tlefile, ELEMENT *element)
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

    if (fgets ((*element).name, 71, tlefile) == NULL)
    {
        fprintf(stderr, "Error: fgets ((*element).name, 71, tlefile) == NULL\n");
	return (-1);
    }

    /* fprintf (stdout, "%s\n", (*element).name); */
    /*** remove the carriage return from the name ***/
    for (i = 0; i < strlen ((*element).name); i++)
    {
	if (iscntrl ((*element).name[i]))
	{
	    (*element).name[i] = 0x20;		/* space */
	}
    }
    /*** read the second "card" into the line buffer ***/

    /* Graeme Waddington (wgw@vax.ox.ac.uk) reports that fread
     * on VMS and MSDOS systems have some trouble with the <cr><lf>
     * at the end of the element line.  He suggested changing the 
     * 70 below to a 71 to correct the problem.  Perhaps using fgets
     * instead of fread may work also.
     */

    if (fgets (eline, 71, tlefile) == NULL)
    {
      fprintf(stderr, "Error: fgets (eline, 71, tlefile) == NULL\n");
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
		    fprintf(stderr, "Error: sscanf (&eline[i], \"\%%1d\", &idummy) != 1\n");
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
        fprintf(stderr, "Error: Checksum element line[68] is not present.\n");
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
        fprintf(stderr, "Error: sscanf (tstrng, \"\%%d\", &idummy) != 1\n");
	return (-1);
    }

    tstrng[0] = '\0';
    strncat (tstrng, &eline[20], 12);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
        fprintf(stderr, "Error: 1 sscanf (tstrng, \"\%%lf\", &ddummy) != 1\n");
	return (-1);
    }

    /* get the mjd day number for the epoch year */
    if(idummy<50)
     idummy += 2000;
    else
     idummy += 1900;
    (*element).epoch = mjd ((long) idummy, 1L, 0.0);

    /* add the epoch julian day */

    (*element).epoch += ddummy;		/* modified julian day number */

    /*** xndt2o ***/

    tstrng[0] = '\0';
    strncat (tstrng, &eline[33], 10);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
        fprintf(stderr, "Error: 2 sscanf (tstrng, \"\%%lf\", &ddummy) != 1\n");
	return (-1);
    }

    (*element).xndt2o = ddummy * mcnsts.twopi / pcnsts.xmnpda /
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
        fprintf(stderr, "Error: 3 sscanf (tstrng, \"\%%lf\", &ddummy) != 1\n");
	return (-1);
    }

    if (ddummy == 0.)
    {
	(*element).xndd6o = 0.;
    }
    else
    {
	tstrng[0] = '\0';
	strncat (tstrng, &eline[50], 2);/* exponent field */

	if (sscanf (tstrng, "%d", &idummy) != 1)
	{
	    fprintf(stderr, "Error: 4 sscanf (tstrng, \"\%%d\", &idummy) != 1\n");
	    return (-1);
	}

	(*element).xndd6o = ddummy * pow (10., (double) idummy);
	(*element).xndd6o = (*element).xndd6o * mcnsts.twopi /
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
        fprintf(stderr, "Error: 5 sscanf (tstrng, \"\%%lf\", &ddummy) != 1\n");
	return (-1);
    }

    if (ddummy == 0.)
    {
	(*element).bstar = 0.;
    }
    else
    {
	tstrng[0] = '\0';
	strncat (tstrng, &eline[59], 2);/* exponent field */

	if (sscanf (tstrng, "%d", &idummy) != 1)
	{
	    fprintf(stderr, "Error: 6 sscanf (tstrng, \"\%%d\", &idummy) != 1\n");
	    return (-1);
	}

	(*element).bstar = ddummy * pow (10., (double) idummy) / pcnsts.ae;
    }

    /*** read the third "card" into the line buffer ***/


    /* Graeme Waddington (wgw@vax.ox.ac.uk) reports that fread
     * on VMS and MSDOS systems have some trouble with the <cr><lf>
     * at the end of the element line.  He suggested changing the 
     * 70 below to a 71 to correct the problem.  Perhaps using fgets
     * instead of fread may work also.
     */

    if (fgets (eline, 70, tlefile) == NULL)
    {
        fprintf(stderr, "Error: 7 fgets (eline, 70, tlefile) == NULL\n");
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
		    fprintf(stderr, "Error: 8 sscanf (&eline[i], \"\%%1d\", &idummy) != 1\n");
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
        fprintf(stderr, "Error: 9 sscanf (tstrng, \"\%%d\", &idummy) != 1\n");
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
        fprintf(stderr, "Error: 10 sscanf (tstrng, \"\%%lf\", &ddummy) != 1\n");
	return (-1);
    }

    (*element).xincl = ddummy * mcnsts.de2ra;

    /*** xnodeo - Right Ascension of Ascending Node  ***/

    tstrng[0] = '\0';
    strncat (tstrng, &eline[17], 8);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
        fprintf(stderr, "Error: 11 sscanf (tstrng, \"\%%lf\", &ddummy) != 1\n");
	return (-1);
    }

    (*element).xnodeo = ddummy * mcnsts.de2ra;

    /*** eo - Eccentricity with assumed leading decimal ***/

    tstrng[0] = pchar;
    tstrng[1] = '\0';
    strncat (tstrng, &eline[26], 7);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
        fprintf(stderr, "Error: 12 sscanf (tstrng, \"\%%lf\", &ddummy) != 1\n");
	return (-1);
    }

    (*element).eo = ddummy;

    /*** omegao ***/

    tstrng[0] = '\0';
    strncat (tstrng, &eline[34], 8);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
        fprintf(stderr, "Error: 13 sscanf (tstrng, \"\%%lf\", &ddummy) != 1\n");
	return (-1);
    }

    (*element).omegao = ddummy * mcnsts.de2ra;

    /*** xmo - Mean Anomaly  ***/

    tstrng[0] = '\0';
    strncat (tstrng, &eline[43], 8);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
        fprintf(stderr, "Error: 14 sscanf (tstrng, \"\%%lf\", &ddummy) != 1\n");
	return (-1);
    }

    (*element).xmo = ddummy * mcnsts.de2ra;

    /*** xno - Revolutions per Day (Mean Motion)  ***/

    tstrng[0] = '\0';
    strncat (tstrng, &eline[52], 11);

    if (sscanf (tstrng, "%lf", &ddummy) != 1)
    {
        fprintf(stderr, "Error: 15 sscanf (tstrng, \"\%%lf\", &ddummy) != 1\n");
	return (-1);
    }

    (*element).xno = ddummy * mcnsts.twopi / pcnsts.xmnpda;

    /***** check for valid elements *****/

    if ((*element).xno <= 0.)
    {
        fprintf(stderr, "Error: 16 (*element).xno <= 0.\n");
	return (-1);
    }

    return (0);
}
