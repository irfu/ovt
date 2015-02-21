#include "common.h"
#include <jni.h>
#include "ovt_util_Utils.h"

/**-------------------------------------------------------------*/
double  actan (sinx, cosx)
double  sinx, cosx;
{
  double  x;
  x = atan2 (sinx, cosx);
  if (x < 0.)
    x += PI2;                   /* keep in range 0 - 2pi */
  return (x);
}
/**-------------------------------------------------------------*/
/*Ncross (v1, v2, v3)
double  v1[3], v2[3], v3[3]; {
//  V3x V3y V3z
//  V1x V1y V1z
//  V2x V2y V2z
  double  len;
  int   i;
  v3[X] = v1[Y] * v2[Z] - v2[Y] * v1[Z];
  v3[Y] = v1[Z] * v2[X] - v2[Z] * v1[X];
  v3[Z] = v1[X] * v2[Y] - v2[X] * v1[Y];
}
*/
/**-------------------------------------------------------------*/
/*deg2dms (degrees, idegrees, minutes, seconds)
double  degrees;
int  *idegrees, *minutes, *seconds;
{
  double  x, xminutes;

  if (degrees > 0.)
    x = degrees;
  else
    x = -degrees;

  *idegrees = x;
  xminutes = (x - (double) * idegrees) * 60.;
  *minutes = xminutes;
  *seconds = (xminutes - (double) * minutes) * 60.;

  if (degrees < 0.)
    *idegrees = -*idegrees;
}
*/

/**-------------------------------------------------------------*/
void deg2hms (double degrees,int *hours,int *minutes,int *seconds)
{
  double  xhours, xminutes;
  degrees = fmod360 (degrees);
  xhours = degrees / 15.;
  *hours = xhours;
  xminutes = (xhours - (double) * hours) * 60.;
  *minutes = xminutes;
  *seconds = (xminutes - (double) * minutes) * 60.;
}

/**-------------------------------------------------------------*/
/*double  dot_prod (vector1, vector2)
double  vector1[3], vector2[3]; {
  double  len;
  int   i;
  for (i = 0, len = 0.; i < 3; i++) {
    len += vector1[i] * vector2[i];
  }
  return (len);
}
*/
/**-------------------------------------------------------------*/
/*dump_elements (string, elem)
char *string;
struct elem_ *elem;
{
  printf ("%s", string);
  print_jtime (stdout, &epoch_day);
  printf (" eccentricity= %lf\n", elem -> e);
  printf (" inclination= %lf\n", elem -> i);
  printf (" true anomaly= %lf\n", elem -> u);
  printf (" arg_periapsis= %lf\n", elem -> w);
  printf (" asc_node= %lf\n", elem -> W);
  printf (" rev_per_day= %lf\n", elem -> n0);
  printf (" mean_dist= %lf\n", elem -> a);
}
*/
/**-------------------------------------------------------------*/
/*dump_time (fout_, tm_)
FILE * fout_;
struct tm  *tm_;
{
  static char days[15] = {
    "SuMoTuWeThFrSa"
  };
  char *cp;

  cp = &days[(tm_ -> tm_wday) * 2];
  fprintf (fout_, " %02d-%02d-%02d %c%c %02d:%02d:%02d ",
      tm_ -> tm_year, (tm_ -> tm_mon) + 1, tm_ -> tm_mday, *cp, *(cp + 1),
      tm_ -> tm_hour, tm_ -> tm_min, tm_ -> tm_sec);

}
*/
/**-------------------------------------------------------------*/

int print_d (
FILE *fout_,                    /* where to print to */
char *string,                   /* prompt string */
double *d_array,                /* values to print */
int   npts                      /* # of pts in array, pass &x if npts=1 */
)
{
  int   i;
  fprintf (fout_, "%s ", string);
  for (i = 0; i < npts; i++) {
    fprintf (fout_, "  %15.5lf  ", d_array[i]);
    if (((i & 3) == 3) || (i == (npts - 1)))
      fprintf (fout_, "\n");
  }
}

/**-----------------------------------------------------------*/
int printmat (char *string,double mat[3][3])
{
  int   i,
        j;
  double *p;
  printf ("%s\n", string);
  p = &mat[0][0];
  for (i = 0; i < 3; i++) {
    for (j = 0; j < 3; j++) {
      printf (" %10.5lf ", *p++);
    }
    printf ("\n");
  }
}
/**-----------------------------------------------------------*/

int matmult (double  a[3][3],double b[3][3],double c[3][3])
/* multiply a*b => c */
{
  int   i,
        j,
        k;

  double  sum;

  if (debug_flag > 2) {
    printmat ("a = ", a);
  }

  for (i = 0; i < 3; i++) {
    for (k = 0; k < 3; k++) {
      sum = 0;
      for (j = 0; j < 3; j++) {
        sum += a[i][j] * b[j][k];
      }
      c[i][k] = sum;
    }
  }
  if (debug_flag > 2) {
    printmat ("c = ", c);
  }
}
/**-----------------------------------------------------------*/
int identity (double  mat[3][3])                  /* make identity matrix */
{
  int   i,
        j;
  for (i = 0; i < 3; i++) {
    for (j = 0; j < 3; j++) {
      mat[i][j] = 0.;
    }
    mat[i][i] = 1.;
  }
}
/**-------------------------------------------------------------*/
int rmatrix (double angle,int index,double mat[3][3])
/* input; rotation angle in degrees */
/* output: rotation  matrix */
/* 1,2,3 = x,y,z */
{
  double  c,                    /* cosine */
          s;                    /* sine */

  int   i,
        j;


  identity (mat);               /* start with identity */

  c = cos (rad * angle);
  s = sin (rad * angle);

  switch (index) {
    case X:                     /* rotate about x axis */
      mat[1][1] = c;
      mat[1][2] = s;
      mat[2][1] = -s;
      mat[2][2] = c;
      break;
    case Y:                     /* rotate about y axis */
      mat[0][0] = c;
      mat[0][2] = -s;
      mat[2][0] = s;
      mat[2][2] = c;
      break;
    case Z:                     /* rotate about z axis */
      mat[0][0] = c;
      mat[0][1] = s;
      mat[1][0] = -s;
      mat[1][1] = c;
      break;
    default: 
      printf (" index = %d in rmatrix\n", index);
      return(-1);
  }
  if (debug_flag > 2) {
    printmat ("rmatrix = ", mat);
  }
}

/**-----------------------------------------------------------*/

int rotate3 (
  double angle1,int index1,               /* angle in degrees, and axis, where */
  double angle2,int index2,               /* 0,1,2 indicate x,y,z */
  double angle3,int index3,
  double rot_mat[3][3])                      /* output compound rotation matrix */
{
  double  mat1[3][3], mat2[3][3], mat3[3][3], temp[3][3];

  rmatrix (angle1, index1, mat1);
  rmatrix (angle2, index2, mat2);
  rmatrix (angle3, index3, mat3);

  matmult (mat1, mat2, temp);
  matmult (temp, mat3, rot_mat);
}

/**-----------------------------------------------------------*/
int vect_xform (
double vector_in[3],          /* vector to be transformed (input) */
double xform_matrix[3][3],    /* matrix to be used in transform (input) */
double vector_out[3]          /* result of transform (output) */
)
{
  double  sum;
  int   i, j;
  for (i = 0; i < 3; i++) {
    sum = 0.;
    for (j = 0; j < 3; j++)
      sum += vector_in[j] * xform_matrix[i][j];
    vector_out[i] = sum;
  }
}
/**-----------------------------------------------------------*/
int rec2sph (double xyz[3],double *radius,double *alpha,double *delta)
{
/*----------------------------------------------------------------
        r*cos(delta)*cos(alpha) = x
        r*cos(delta)*sin(alpha) = y
        r*sin(delta)            = z
        c       alpha = atan(y/x)
        delta = asin(z/r)
        r = sqrt(x*x + y*y + z*z)
----------------------------------------------------------------*/
  double  arg;

  *radius = sqrt (xyz[0] * xyz[0] + xyz[1] * xyz[1] + xyz[2] * xyz[2]);

  if ((xyz[Y] != 0.) && (xyz[X] != 0.))
    *alpha = irad * atan2 (xyz[Y], xyz[X]);
  else
    *alpha = 0;

  if (*alpha < 0.)
    *alpha = *alpha + 360.;
  arg = xyz[Z] / *radius;

  if (arg < 1.)
    *delta = irad * asin (arg);
  else
    *delta = 90.;
}

/**-----------------------------------------------------------*/
int station (
double  latitude,                     /* input: (geodetic) in deg. 90 = n pole */
double  longitude,                    /* input: east is positive */
double  height,                       /* input: height in km above sea level */
struct julian *jday,                  /* input: julian day */
double  geocent_v[3],                 /* input: geocentric vector to object km. */
double  topocent_v[3],                /* output: topo vector to object (km)  */
double  horizon_v[3],                 /* output: in elev-azim coord system */
double  station_v[3]                  /* output: geocentric vector to station */
)
{
  double  rot_mat[3][3], vector1[3], vector2[3],
          gha_, a, e, d, x, z, geolat, theta, ad, esin;
  int   i;

  a = R_EARTH;                  /* equatorial radius Bate & White 94 */
  e = 0.08182;                  /* eccentricity of geoid */
  esin = e * sin (rad * latitude);
  d = sqrt (1.- esin * esin);
  x = (a / d + height) * cos (rad * latitude);/* pg 98 */
  z = ((a * sqrt (1.- e * e)) / d + height) * sin (rad * latitude);
  geolat = irad * atan2 (z, x); /* geocentric latitude */
  gha_ = gha (jday);
  if (debug_flag > 1)
    printf (" gha= %g\n", gha_);
  theta = longitude + gha_;

  station_v[X] = x * cos (rad * theta);
  station_v[Y] = x * sin (rad * theta);
  station_v[Z] = z;             /* equatorial inertial vector of observer */

  for (i = 0; i < 3; i++)       /* topo. inertial vector */
    topocent_v[i] = geocent_v[i] - station_v[i];

  rotate3 (                     /* make z point up, x point south */
      (90.- geolat), Y,         /* y rotation */
      theta, Z,                 /* z rotation */
      0., X,                    /* null */
      rot_mat);

  vect_xform (topocent_v, rot_mat, horizon_v);

  if (debug_flag > 1) {
    print_d (stdout, " geocent v = ", geocent_v, 3);
    printmat (" rot_mat", rot_mat);
    print_d (stdout, " station_v = ", station_v, 3);
    print_d (stdout, " topocent_v = ", topocent_v, 3);
    print_d (stdout, " horizon_v = ", horizon_v, 3);
  }

}

int cdate(int *ymd,int *hms,int *ms,double *mjd,int dir)
/* converts yymmdd hhmmss ms  to mjd if dir>0
   converts mjd to yymmdd if dir<0
   mjd is the day number counted from 1 January 1950 00hh 00mi 00ss.
   This routine is valid for dates between 1 Jan 1950 and 31 Dec 2099.
   */
{
    int yy, mo, dd, hh, mi, ss;
    int l, n, m, jj, jday;
    double  temp;

    if (dir > 0) {
        yy = (*ymd);
        hh = (*hms);
        dd = yy % 100;
        ss = hh % 100;
        mo = (yy / 100) % 100;
        mi = (hh / 100) % 100;
        yy /= 10000;
        hh /= 10000;
        jj  = (14 - mo) / 12;
        l   = yy - 1900 * (yy / 1900) - jj;
        *mjd = dd - 18234 + (1461 * l) / 4 + (367 * (mo - 2 + jj * 12)) / 12;
        *mjd += ((hh * 3600 + mi * 60 + ss) * 1000  + (*ms) ) / 86400000.0;
    } else {
        temp = (*mjd) + 0.0005 / 86400.0;
        jday = temp;
        l = (4000 * (jday + 18204)) / 1461001;
        n = jday - (1461 * l) / 4 + 18234;
        m = (80 * n) / 2447;
        dd   = n - (2447 * m) / 80;
        jj    = m / 11;
        mo = m + 2 - 12 * jj;
        yy  = 1900 + l + jj;
        temp = (temp - jday) * 24.00;
        hh  = temp;
        temp = (temp - hh) * 60.00;
        mi   = temp;
        temp = (temp - mi) * 60.00;
        ss   = temp;
        temp = (temp - ss) * 1000.00;
        *ms = (int)temp;
        *ymd = yy % 100 * 10000 + mo * 100 + dd;
        *hms = hh * 10000 + mi * 100 + ss;
    }
}
/**-----------------------------------------------------------*/

void earth (
struct julian *jday,                         /* julian day */
double  vector2[3]                       /* heliocentric vector of earth */
)
{
  double  rot_mat[3][3], vector1[3], t, t2, t3, elements[3],
          longitude, mean_anomaly, eccentricity, Radius,
          mean_dist, inclination, asc_node, eccentric_anomaly,
          vector_2[3], alpha, delta, A, B, C, D, E, H;

  int   i, j;

  static double lme[3][4] = {
    279.69668, 36000.76892,.0003025, 0.,/* longitude */
    358.47583, 35999.04975, -.000150, -.0000033,/* mean anomaly */
    .01675104, -.0000418, -.000000126, 0./* eccentricity */
  };

  t = (tdt (jday) + (jday -> integer - 2415020.0) + jday -> fraction)
    / 36525.;                   /* rel to 1900 */

  t2 = t * t;
  t3 = t2 * t;

  for (i = 0; i < 3; i++) {
    elements[i] =
      lme[i][0] +
      lme[i][1] * t +
      lme[i][2] * t2 +
      lme[i][3] * t3;
  }
/*----------------------------------------------------------------
  Additional perturbations, pg 82 Meeus
----------------------------------------------------------------*/
  A = fmod2p (rad * 153.23 + 22518.7541 * t);/* venus */
  B = fmod2p (rad * 216.57 + 45037.5082 * t);/* venus */
  C = fmod2p (rad * 312.69 + 32964.3557 * t);/* jupiter */
  D = fmod2p (rad * 350.74 + 445267.1142 * t -.00144 * t2);/* moon */
  E = fmod2p (rad * 231.19 + 20.2 * t);
  H = fmod2p (rad * 353.40 + 65928.7155 * t);


  longitude = fmod360 (elements[0] +
      .00134 * cos (A) +
      .00154 * cos (B) +
      .00200 * cos (C) +
      .00179 * sin (D) +
      .00178 * sin (E));

  Radius = 1.0000002 +
    .00000543 * sin (A) +
    .00002575 * sin (B) +
    .00001627 * sin (C) +
    .00003076 * cos (D) +
    .00000927 * sin (H);

  mean_anomaly = fmod360 (elements[1]);
  eccentricity = elements[2];
  mean_dist = Radius * au;
  inclination = 0.;
  asc_node = 0.;

  if (debug_flag > 1)
    print_d (stdout, " lme ", elements, 3);

  eccentric_anomaly =
    kepler (eccentricity, mean_anomaly);

  vector1[X] = -mean_dist *
    (cos (rad * eccentric_anomaly) - eccentricity);

  vector1[Y] = -mean_dist *
    (sqrt (1.- eccentricity * eccentricity) * sin (rad * eccentric_anomaly));

  vector1[Z] = 0.;

  if (debug_flag > 1)
    print_d (stdout, " vector1 ", vector1, 3);

  rotate3 (
      (mean_anomaly - longitude), Z,
      0., X,
      0., X,
      rot_mat);

  vect_xform (vector1, rot_mat, vector2);

  if (debug_flag > 1)
    print_d (stdout, " vector2 ", vector2, 3);

}

/**-------------------------------------------------------------*/
double  ecliptic (struct julian *jday)
{
  double  t, t2, t3, e;
  t = (jday -> integer - 2451545.) / 36525.;/* rel to 2000.0 */
  t2 = t * t;
  t3 = t2 * t;
  e = 23.439291 - 1.300417e-2 * t - 1.63889e-7 * t2 + 5.03611e-7 * t3;
  return (e);
}
/**-------------------------------------------------------------*/
double  fmod2p (x)
double  x;
{
  double  y;
  y = fmod (x, PI2);
  if (y < 0.)
    y += PI2;
  return (y);
}
#ifdef UNIX                     /* this seems to be missing */
double  fmod (x, y)
double  x, y;
{
  double  z;
  z = x - y * (double) ((long) (x / y));
  return (z);
}
#endif

/**-------------------------------------------------------------*/
double  fmod360 (x)
double  x;
{
  x = fmod (x, 360.);
  if (x < 0.)
    x += 360.;
  return (x);
}

/*
get_time_zone () {
//----------------------------------------------------------------
//sets up the global variables:
//local_2_gmt = (GMT-local) in days
//local_2_gmt_sec = (GMT-local) in seconds
//----------------------------------------------------------------
int tz_hours;

#ifdef UNIX			// the UNIX routines are a little different
  struct timeval  tp;
  struct timezone tzp;
  gettimeofday (&tp, &tzp);
  timez = ((double) tzp.tz_minuteswest) * 60.;
  daylight = tzp.tz_dsttime;
  tz_hours = (timez+1) / 3600.;  // seconds -> hours (watch for roundoff)
  tz_hours += daylight;		//compensate for daylight savings

#else				// MS-DOS
  tzset ();                     // MS-C for setup of daylight & timezone 
  tz_hours = (timez+1) / 3600.;  //seconds -> hours (watch for roundoff)
#endif

  prompt_i (" GMT-local in hrs. (including daylight savings) ", &tz_hours, 1);
  timez = tz_hours * 3600.;
  local_2_gmt_sec = timez;
  local_2_gmt = (double) (tz_hours)/ 24.0;
}


struct tm  *get_tm (jday)
struct julian *jday;
{
  struct tm  *tm_;
  long  gm_time, local_ltime;

  gm_time =
    ((jday -> integer - J1970) +
      jday -> fraction) * SEC_PER_DAY;

  if ((jday -> integer) < 0.) {
    printf (" get_tm will not work for t < J1970\n");
    exit (1);
  }
  local_ltime = gm_time - local_2_gmt_sec;
  tm_ = gmtime (&local_ltime);
  return (tm_);
}

get_day (jday)
struct julian *jday;
{
  double  hour, minute, second, lha;
  int   year, month, day, ah, am, as;
  struct tm  *tm_;
  long  gm_time, local_ltime;

  time (&gm_time);
  local_ltime = gm_time - local_2_gmt_sec;

  tm_ = gmtime (&local_ltime);

  year = tm_ -> tm_year + 1900;
  month = 1 + tm_ -> tm_mon;
  day = tm_ -> tm_mday;
  hour = tm_ -> tm_hour;
  minute = tm_ -> tm_min;
  second = tm_ -> tm_sec;

  prompt_i (" year ", &year, 1);
  prompt_i (" month ", &month, 1);
  prompt_i (" day ", &day, 1);
  prompt_d (" hour ", &hour, 1);
  prompt_d (" minute ", &minute, 1);
  prompt_d (" second ", &second, 1);
  second += local_2_gmt_sec;
  julian_day (year, month, day, hour, minute, second, jday);
  printf (" Jday = %14.5lf ", jday -> integer + jday -> fraction);
  lha = gha (jday) + station_long;
  deg2hms (lha, &ah, &am, &as);
  printf (" LST = %02d:%02d:%02d\n", ah, am, as);
}
*/

/*----------------------------------------------------------------
returns the angle in degrees between the vernal equinox (X axis) and the
Greewich meridian. All numbers from pgs B6-B7 of 1984 Alamanc
----------------------------------------------------------------*/

double  gha (jday)              /* returns degrees */
struct julian *jday;            /* julian day */
{
  double  interval, t, jday0, gmst, gha_;
  int   ah, am, as;
  
 /* THIS STUPID PART OF CODE SOMEHOW WAS INSERTED HERE!
   WHO DID AND WHEN? MAY BE IT WA ME? (ko) :)
  jday -> integer = 10;
  jday -> fraction = 20;
 */
  t = (jday -> integer - J2000) / 36525;
 /* Julian centuries since 2000.0 */



  gmst = (
      24110.54841 +             /* Greenwich mean sidereal time */
      8640184.812866 * t +      /* at midnight of this day = 0h UT */
      0.093104 * t * t -        /* coeff. are for seconds of time */
      6.2e-6 * t * t * t)
    / 3600.;                    /* 3600 sec. -> hour */


  interval =                    /* siderial hours since midnight */
    1.0027379093 * 24.* (jday -> fraction);
  debug_flag = 0;
  if (debug_flag > 0) {
    printf (" gha day = %10.2lf  %15.5lf\n",
        jday -> integer, jday -> fraction);

    deg2hms (gmst * 15., &ah, &am, &as);
    printf (" gmst = %02d:%02d:%02d\n", ah, am, as);
    deg2hms (interval * 15., &ah, &am, &as);
    printf (" interval = %02d:%02d:%02d\n", ah, am, as);
  }

  gmst += interval;      /*       add in interval since midnight */

  gha_ = fmod360                /* hrs => degrees, make modulo 360 */
    (gmst * 15.);               /* (15 deg/hr) */
  return (gha_);
}

/**-------------------------------------------------------------*/
void julian_day (int year, int month, int day_of_month, 
double hour,double minute,double second,struct julian *jday)
{
  jday -> integer =
    367 * (long) year -
    (7 * ((long) year + ((long) month + 9) / 12)) / 4 +
    (275 * (long) month) / 9 +
    (long) day_of_month +
    1721013.5;

  jday -> fraction =
    (hour + (minute + second / 60.) / 60.) / 24.;
}
/**-------------------------------------------------------------*/
double  kepler (double e,double m)           /* solves kepler - e*sin(kepler) = m */
                  /* e eccentricity */
                  /* m mean anomaly in degrees */
{
  double  rm,                   /* m in radians */
          k;                    /* result */
  int   i;
  rm = rad * m;
  for (k = rm, i = 0; i < 10; i++)
    k = rm + e * sin (k);
  return (k / rad);             /* back to degrees */
}
/**-------------------------------------------------------------*/
double  length (vector)
double  vector[3]; {
  double  len;
  int   i;
  for (i = 0, len = 0.; i < 3; i++) {
    len += vector[i] * vector[i];
  }
  len = sqrt (len);
  return (len);
}
/**-----------------------------------------------------------*/
void local_coord (struct julian *jday,
double geocentric[3],double station_v[3],double *ra,double *dec,
double *elev,double *azim)
{
  double  radius, topocent_v[3], horizon_v[3],
          ra_, dec_, elev_, azim_;

  int   ah, am, as, dd, dh, dm, ds;

  if (debug_flag > 1) {
    rec2sph (geocentric, &radius, &ra_, &dec_);
    deg2hms (ra_, &ah, &am, &as);
    printf (" geocen rad= %13.6e\n", radius);
    printf (" ra = %8.3f %02d:%02d:%02d\n", ra_, ah, am, as);
    printf (" dec= %8.3f\n", dec_);
  }

  station (
      station_lat,              /* input: (geodetic) in deg., 90 = n. pole */
      station_long,             /* input: east is positive */
      station_height,           /* input: height in km above sealevel */
      jday,                     /* input: julian day */
      geocentric,               /* input: geocentric vector to object (km) */
      topocent_v,               /* output: topo vector to object (km)  */
      horizon_v,                /* output: in elev-azim coord system */
      station_v                 /* output: geocentric vector to station */
    );

  rec2sph (horizon_v, &radius, &azim_, &elev_);
  if (debug_flag > 1)
    print_d (stdout, " horizon_v = ", horizon_v, 3);
  azim_ = -azim_ + 180;         /* x points south, make north */
  azim_ = fmod360 (azim_);      /* 0 = north, 90 = east */

  rec2sph (topocent_v, &radius, &ra_, &dec_);
  if (ra_ < 0.)
    ra_ = ra_ + 360.;

  *ra = ra_;
  *dec = dec_;
  *elev = elev_;
  *azim = azim_;
  if (debug_flag)
    printf (" range = %g\n", length (horizon_v));
}

/**-----------------------------------------------------------*/

double  sinde (x)
double  x;
{
  return (sin (rad * x));
}

double  cosde (x)
double  x;
{
  return (cos (rad * x));
}

/*
moon_vect (jday, m_vector)
struct julian *jday;
double  m_vector[3];
{
  double  ra, dec, elev, azim, sun_v[3], station_v[3];
  double  lp, m, mp, d, f, o, e, lambda, b, w1, w2, beta, p,
          lmdf, sin1782, sino, t, t2, t3, eclip_vect[3], radius,
          rot_mat[3][3];

  int   ah, ad, am, as;
  t = (tdt (jday) + (jday -> integer - 2415020.) + jday -> fraction) / 36525.;
  t2 = t * t;
  t3 = t2 * t;

  lp = fmod ((270.434164 + 481267.8831 * t
        -.001133 * t2 +.0000019 * t3), 360.);// moon's longitude

  m = fmod ((358.475833 + 35999.0498 * t
        -.000150 * t2 -.0000033 * t3), 360.);// sun's anomaly

  mp = fmod ((296.104608 + 477198.8491 * t
        +.009192 * t2 +.0000144 * t3), 360.);// moon's anomaly

  d = fmod ((350.737486 + 445267.1142 * t
        -.001436 * t2 +.0000019 * t3), 360.);// moon's elongation

  f = fmod ((11.250889 + 483202.0251 * t
        -.003211 * t2 -.0000003 * t3), 360.);// dist from asc node

  o = fmod ((259.183275 - 1934.1420 * t
        +.002078 * t2 + 0000022 * t3), 360.);// moon's asc node

// preturbations

  lmdf = 0.003964 * sinde (346.560 + 132.870 * t -.0091731 * t2);
  sin1782 = sinde (51.2 + 20.2 * t);
  sino = sinde (o);

  if (debug_flag > 1) {
    printf (" t = %20.13lf\n", t);
    printf (" lmpdf= %lf %lf %lf %lf %lf\n", lp, m, mp, d, f);
  }

  lp += (0.000233 * sin1782 + 0.001964 * sino + lmdf);
  m -= (0.001778 * sin1782);
  mp += (0.000817 * sin1782 + 0.002541 * sino + lmdf);
  d += (0.002011 * sin1782 + 0.001964 * sino + lmdf);
  f = f - (0.024691 * sino) -
    0.004328 * sinde (o + 275.05 - 2.3 * t) +
    lmdf;


  if (debug_flag > 1)
    printf (" lmpdf= %lf %lf %lf %lf %lf\n", lp, m, mp, d, f);

  e = 1 - 0.002495 * t - 0.00000752 * t2;
//  change to radians
  mp *= rad;
  m *= rad;
  d *= rad;
  f *= rad;

  lambda = lp
    + 6.288750 * sin (mp)
    + 1.274018 * sin (2.* d - mp)
    + 0.658309 * sin (2.* d)
    + 0.213616 * sin (2.* mp)
    - 0.185596 * sin (m) * e;

  lambda = lambda
    - 0.114336 * sin (2.* f)
    + 0.058793 * sin (2.* d - 2.* mp)
    + 0.057212 * sin (2.* d - m - mp)
    + 0.053320 * sin (2.* d + mp)
    + 0.045874 * sin (2.* d - m) * e
    + 0.041024 * sin (mp - m) * e
    - 0.034718 * sin (d);

  lambda = lambda
    - 0.030465 * sin (m + mp) * e
    + 0.015326 * sin (2.* d - 2.* f)
    - 0.012528 * sin (2.* f + mp)
    - 0.010980 * sin (2.* f - mp)
    + 0.010674 * sin (4.* d - mp)
    + 0.010034 * sin (3.* mp)
    + 0.008548 * sin (4.* d - 2.* mp);

  lambda = lambda
    - 0.007910 * sin (m - mp + 2.* d) * e
    - 0.006783 * sin (2.* d + m) * e
    + 0.005162 * sin (mp - d)
    + 0.005000 * sin (m + d) * e
    + 0.004049 * sin (mp - m + 2.* d) * e
    + 0.003996 * sin (2.* mp + 2.* d)
    + 0.003862 * sin (4.* d);

  lambda = lambda
    + 0.003665 * sin (2.* d - 3.* mp)
    + 0.002695 * sin (2.* mp - m) * e
    + 0.002602 * sin (mp - 2.* f - 2.* d)
    + 0.002369 * sin (2.* d - m - 2.* mp) * e
    - 0.002349 * sin (mp + d)
    + 0.002249 * sin (2.* d - 2.* m) * e * e
    - 0.002125 * sin (2.* mp + m) * e;

  lambda = lambda
    - 0.002079 * sin (2.* m) * e * e
    + 0.002059 * sin (2.* d - mp - 2.* m) * e * e
    - 0.001773 * sin (mp + 2.* d - 2.* f)
    - 0.001595 * sin (2.* f + 2.* d)
    + 0.001220 * sin (4.* d - m - mp) * e
    - 0.001110 * sin (2.* mp + 2.* f);

  lambda = lambda
    + 0.000892 * sin (mp - 3.* d)
    - 0.000811 * sin (m + mp + 2.* d) * e
    + 0.000761 * sin (4.* d - m - 2.* mp)
    + 0.000717 * sin (mp - 2.* m) * e * e
    + 0.000704 * sin (mp - 2.* m - 2.* d) * e * e
    + 0.000693 * sin (m - 2.* mp + 2.* d) * e;

  lambda = lambda
    + 0.000598 * sin (2.* d - m - 2.* f) * e
    + 0.000550 * sin (mp + 4.* d)
    + 0.000538 * sin (4.* mp)
    + 0.000521 * sin (4.* d - m) * e
    + 0.000486 * sin (2.* mp - d);


  b = 5.128189 * sin (f)
    + 0.280606 * sin (mp + f)
    + 0.277693 * sin (mp - f)
    + 0.173238 * sin (2.* d - f)
    + 0.055413 * sin (2.* d + f - mp)
    + 0.046272 * sin (2.* d - f - mp);

  b = b
    + 0.032573 * sin (2.* d + f)
    + 0.017198 * sin (2.* mp + f)
    + 0.009267 * sin (2.* d + mp - f)
    + 0.008823 * sin (2.* mp - f)
    + 0.008247 * sin (2.* d - m - f) * e;

  b +=
    0.004323 * sin (2.* d - f - 2.* mp)
    + 0.004200 * sin (2.* d + f + mp)
    + 0.003372 * sin (f - m - 2.* d) * e
    + 0.002472 * sin (2.* d + f - m - mp) * e
    + 0.002222 * sin (2.* d + f - m) * e
    + 0.002072 * sin (2.* d - f - m - mp) * e;

  b = b
    + 0.001877 * sin (f - m + mp) * e
    + 0.001828 * sin (4.* d - f - mp)
    - 0.001803 * sin (f + m) * e
    - 0.001750 * sin (3.* f)
    + 0.001570 * sin (mp - m - f) * e
    - 0.001487 * sin (f + d);

  b +=
    -0.001481 * sin (f + m + mp) * e
    + 0.001417 * sin (f - m - mp) * e
    + 0.001350 * sin (f - m) * e
    + 0.001330 * sin (f - d)
    + 0.001106 * sin (f + 3.* mp)
    + 0.001020 * sin (4.* d - f);

  b = b
    + 0.000833 * sin (f + 4.* d - mp)
    + 0.000781 * sin (mp - 3.* f)
    + 0.000670 * sin (f + 4.* d - 2.* mp)
    + 0.000606 * sin (2.* d - 3.* f)
    + 0.000597 * sin (2.* d + 2.* mp - f);

  b +=
    0.000492 * sin (2.* d + mp - m - f) * e
    + 0.000450 * sin (2.* mp - f - 2.* d)
    + 0.000439 * sin (3.* mp - f)
    + 0.000423 * sin (f + 2.* d + 2.* mp)
    + 0.000422 * sin (2.* d - f - 3.* mp)
    - 0.000367 * sin (m + f + 2.* d - mp) * e;

  b = b
    - 0.000353 * sin (m + f + 2.* d)
    + 0.000331 * sin (f + 4.* d)
    + 0.000317 * sin (2.* d + f - m + mp) * e
    + 0.000306 * sin (2.* d - 2.* m - f) * e * e
    - 0.000283 * sin (mp + 3.* f);


  w1 = 0.0004664 * cosde (o);

  w2 = 0.0000754 * cosde (o + 275.083 - 2.5 * t);

  beta = b * (1.- w1 - w2);

  p = 0.950724
    + 0.051818 * cos (mp)
    + 0.009531 * cos (2.* d - mp)
    + 0.007843 * cos (2.* d)
    + 0.002824 * cos (2.* mp)
    + 0.000857 * cos (2.* d + mp);

  p = p
    + 0.000533 * cos (2.* d - m) * e
    + 0.000401 * cos (2.* d - m - mp) * e
    + 0.000320 * cos (mp - m) * e
    - 0.000271 * cos (d);

  p +=
    -0.000264 * cos (m + mp) * e
    - 0.000198 * cos (2.* f - mp)
    + 0.000173 * cos (3.* mp)
    + 0.000167 * cos (4.* d - mp)
    - 0.000111 * cos (m) * e
    + 0.000103 * cos (4.* d - 2.* mp);

  p = p
    - 0.000084 * cos (2.* m - 2.* d)
    - 0.000083 * cos (2.* d + m) * e
    + 0.000079 * cos (2.* d + 2.* mp)
    + 0.000072 * cos (4.* d);

  p +=
    0.000064 * cos (2.* d - m + mp) * e
    - 0.000063 * cos (2.* d + m - mp) * e
    + 0.000041 * cos (m + d) * e
    + 0.000035 * cos (2.* mp - m)
    - 0.000030 * cos (mp + d)
    - 0.000029 * cos (2.* f - 2.* d);

  p = p
    - 0.000029 * cos (2.* mp + m) * e
    + 0.000026 * cos (2.* d - 2.* m) * e * e
    - 0.000023 * cos (2.* f - 2.* d + mp)
    + 0.000019 * cos (4.* d - m - mp);

  if (debug_flag > 1) {
    printf (" lambda= %lf\n", lambda);
    printf (" beta= %lf\n", beta);
    printf (" pi= %lf\n", p);
  }
// vector from earth to moon in ecliptic coordinates
  radius = R_EARTH / sinde (p);
  eclip_vect[X] = radius * cosde (beta) * cosde (lambda);
  eclip_vect[Y] = radius * cosde (beta) * sinde (lambda);
  eclip_vect[Z] = radius * sinde (beta);

  if (debug_flag > 1)
    print_d (stdout, " moon eclip_vect ", eclip_vect, 3);

  rotate3 (                     // ecliptic to equatorial
      -ecliptic (jday), X,
      0., X,
      0., X,
      rot_mat);

  vect_xform (eclip_vect, rot_mat, m_vector);

  if (debug_flag > 1)
    print_d (stdout, " moon vector ", m_vector, 3);

}
*/
/**-----------------------------------------------------------*/
/*
precess (
  jday1,                        // input: day of catalog
  jday2,                        // input: day of observation
  rot_mat                       //  output: rotation matrix
)
struct julian *jday1, *jday2;
double  rot_mat[3][3];
{
  double  eta, zeta, theta, ti, tf, tf2, tf3;

  ti = ((jday1 -> integer - 2415020.5) + jday1 -> fraction)
    / 36525.0;                  // rel to 1900

  tf = ((jday2 -> integer - jday1 -> integer) +
      (jday2 -> fraction - jday1 -> fraction))
    / 36525.0;                  // per century

  tf2 = tf * tf;
  tf3 = tf2 * tf;

  eta = (2304.253 + 1.3975 * ti +.00006 * ti * ti) * tf
    + (.3023 - 0.00027 * ti) * tf2 +.018 * tf3;

  zeta = eta + (.7927 +.00066 * ti) * tf2 +.00032 * tf;

  theta = (2004.682 -.8533 * ti -.00037 * ti * ti) * tf -
    (0.426 + 0.00037 * ti) * tf2 -.04180 * tf3;

  if (debug_flag)
    printf (" ti=%g tf=%g eta=%g zeta=%g theta=%g\n",
        ti, tf, eta, zeta, theta);

  eta = eta / 3600.;            // convert to degrees
  zeta = zeta / 3600.;
  theta = theta / 3600.;

  rotate3 (
      -eta, Z,
      theta, Y,
      -zeta, Z,
      rot_mat);

  if (debug_flag)
    printmat (" rot_mat = ", rot_mat);
}
*/

/**--- does work, but there was a warning, and noone uses this subroutine ---

int print_jtime (FILE *fout,struct julian *jday)
{
  long  gm_time, local_time;
  struct tm  *tm_;
  static char date_buf[27];

  gm_time =                       // seconds since 1970.0 rel to jday 
    ((jday -> integer - J1970) +
      jday -> fraction) * SEC_PER_DAY;

  if ((jday -> integer) < 0.) {
    fprintf (fout, " fprint_jtime wont work for t < J1970\n");
    return (0);
  }

  local_time = gm_time - local_2_gmt_sec;
  strcpy (date_buf, asctime (tm_ = gmtime (&local_time)));

  date_buf[24] = 0;             // kill \n 
  fprintf (fout, " %s (local)", date_buf);
  strcpy (date_buf, asctime (tm_ = gmtime (&gm_time)));

  date_buf[19] = 0;             // kill year, \n 
  fprintf (fout, " %s (GMT)\n", date_buf + 10);
}*/

/**-----------------------------------------------------------*/
int prompt_d (
char *string,                   /* prompt string */
double *d_array,                /* default value */
int   npts                     /* # of pts in array, pass &x if npts=1 */
)
{
  char  buffer[80],
       *resp;
  int   i;
  for (resp = buffer;;) {       /* loop till <CR> */
    printf ("%s ", string);
    for (i = 0; i < npts; printf (" %lg ", d_array[i++]));
    gets (resp);                /* get user's response */
    if (strlen (resp) == 0)     /* <CR> => exit from loop */
      return(0);
    switch (npts) {             /* this is brute force, but it works */
      case 1: 
        sscanf (resp, "%lf", d_array);
        break;
      case 2: 
        sscanf (resp, "%lf %lf",
            &d_array[0], &d_array[1]);
        break;
      case 3: 
        sscanf (resp, "%lf %lf %lf",
            &d_array[0], &d_array[1], &d_array[2]);
        break;
      case 4: 
        sscanf (resp, "%lf %lf %lf %lf",
            &d_array[0], &d_array[1], &d_array[2], &d_array[3]);
        break;
      default: 
        printf (" npts = %d; must be 1->4\n", npts);
        return(0);
    }
  }
}
/**-----------------------------------------------------------*/
/*
void prompt_i (
char *string,                  // prompt string
int  *i_array[],               // default value
int   npts                     // # of pts in array, pass &x if npts=1
)
{
  char  buffer[80],
       *resp;
  int   i;
  for (resp = buffer;;) {       // loop till <CR>
    printf ("%s ", string);
    for (i = 0; i < npts; printf (" %d ", i_array[i++]));
    gets (resp);                // get user's response
    if (strlen (resp) == 0)     // <CR> => exit from loop
      return;
    switch (npts) {             // this is brute force, but it works
      case 1: 
        sscanf (resp, "%d", i_array);
        break;
      case 2: 
        sscanf (resp, "%d %d",
            &i_array[0], &i_array[1]);
        break;
      case 3: 
        sscanf (resp, "%d %d %d",
            &i_array[0], &i_array[1], &i_array[2]);
        break;
      case 4: 
        sscanf (resp, "%d %d %d %d",
            &i_array[0], &i_array[1], &i_array[2], &i_array[3]);
        break;
      default: 
        printf (" npts = %d; must be 1->4\n", npts);
        return;
    }
  }
}
*/

/**-----------------------------------------------------------*/
/*radec (jday, vector)           // prints ra & dec from rectangular
double  vector[3];              // geocentric vector
struct julian *jday;
{
  double  radius, alpha, delta, topocent_v[3], horizon_v[3], station_v[3];
  int   ah, am, as, dd, dh, dm, ds;

  rec2sph (vector, &radius, &alpha, &delta);
  deg2hms (alpha, &ah, &am, &as);

  if (debug_flag) {
    printf (" geocen rad= %13.6e", radius);
    printf (" ra = %8.3f %02d:%02d:%02d", alpha, ah, am, as);
    printf (" dec= %8.3f\n", delta);
  }

  station (
      station_lat,              // input: (geodetic) in deg., 90 = n. pole
      station_long,             // input: east is positive
      station_height,           // input: height in km above sealevel
      jday,                     // input: julian day 
      vector,                   // input: geocentric vector to object (km)
      topocent_v,               // output: topo vector to object (km) 
      horizon_v,                // output: in elev-azim coord system 
      station_v                 // output: geocentric vector to station
    );

  rec2sph (topocent_v, &radius, &alpha, &delta);
  if (alpha > 180.)
    alpha = alpha - 360.;

  if (debug_flag >= 1) {
    print_d (stdout, " topocent_v = ", topocent_v, 3);
    print_d (stdout, " horizon_v = ", horizon_v, 3);
  }
  deg2hms (alpha, &ah, &am, &as);
  printf (" ra = %8.3f %02d:%02d:%02d", alpha, ah, am, as);
  printf (" dec= %8.3f\n", delta);


  rec2sph (horizon_v, &radius, &alpha, &delta);
  alpha += 180;                 // x points south, make north
  if (alpha > 180.)
    alpha = alpha - 360.;
  printf (" azim= %8.3f", alpha);
  printf (" elev= %8.3f", delta);
}
*/

/**-----------------------------------------------------------*/
int station_vect (              /* subset of above for station_v only */
struct julian *jday,            /* input: julian day */
double station_v[3]             /* output: geocentric vector to station */
)
{
  double  gha_, a, e, d, x, z, theta_rad, ad, esin, lat_rad;
  int   i;

  a = R_EARTH;                  /* equatorial radius Bate & White 94 */
  e = 0.08182;                  /* eccentricity of geoid */
  lat_rad = station_lat * rad;
  esin = e * sin (lat_rad);
  d = sqrt (1.- esin * esin);
  x = (a / d + station_height) * cos (lat_rad);/* pg 98 */
  z = ((a * sqrt (1.- e * e)) / d + station_height) * sin (lat_rad);
  gha_ = gha (jday);
  if (debug_flag > 1)
    printf (" gha= %g\n", gha_);
  theta_rad = rad * (station_long + gha_);

  station_v[X] = x * cos (theta_rad);
  station_v[Y] = x * sin (theta_rad);
  station_v[Z] = z;             /* equatorial inertial vector of observer */

}

/**-----------------------------------------------------------*/

int sun_vect (struct julian *jday,double geocentric[3]) 
/* vect from earth to sun */
{
  double  rot_mat[3][3], eclip_v[3];
  int   i;

  earth (jday, eclip_v);

  for (i = 0; i < 3; i++)       /* earth -> sun */
    eclip_v[i] = -eclip_v[i];

  rotate3 (                     /* ecliptic to equatorial */
      -ecliptic (jday), X,
      0., X,
      0., X,
      rot_mat);

  vect_xform (eclip_v, rot_mat, geocentric);
}
/*----------------------------------------------------------------
returns correction factor to convert universal time (UT) to terrestrial
dynamic time (TDT)
----------------------------------------------------------------*/
/**-----------------------------------------------------------*/
double  tdt (jday)
struct julian *jday; {
  return ((54.6 + 0.9 * (jday -> integer - J1985) / 365.) / SEC_PER_DAY);
}

/**-----------------------------------------------------------*/
/*unit_length (v)                 // make vector unit length
double  v[3];
{
  double  length,
          scale;
  int   i;

  if ((length = v[0] * v[0] + v[1] * v[1] + v[2] * v[2]) <.0001) {
    printf (" bad unit_length vector= %g %g %g\n",
        v[0], v[1], v[2]);
    length = 1.;                // kludge to keep going
  }

  scale = 1./ sqrt (length);

  for (i = 0; i < 3; i++)
    v[i] *= scale;
}
*/
/**-----------------------------------------------------------*/
void mjd2jul(double *mjd,struct julian *jday,int dir)
/* converts mjd to julian if dir=1 else julian to mjd */
{
  int ymd,hms,ms;
double mj0;

	if(dir==1) {
    cdate(&ymd,&hms,&ms,mjd,-1);
    hms=0;
  ms=0;
    cdate(&ymd,&hms,&ms,&mj0,1);
    jday -> integer = mj0+ J1950;
    jday -> fraction = *mjd-mj0; 
    } else {
    *mjd= (jday->integer -J1950) + jday->fraction;
   }
}

void mjd2day(double *mjd,double *day,int dir)
{
  int ymd,hms,ms;
  double mj0;
  if(dir>0) {
  cdate(&ymd,&hms,&ms,mjd,-1);
   ymd= (ymd/10000)*10000 +101;
    hms=0;
  ms=0;
    cdate(&ymd,&hms,&ms,&mj0,1);
  *day= *mjd -mj0+1;
  }
  else {
  ymd=(int)(*day-1);
  ymd=ymd*10000+101;
  hms=0;
  ms=0;
  cdate(&ymd,&hms,&ms,mjd,1);
  *mjd += fmod(*day,1.0e0);
 }
}

/*----------- JNI to earth -------------------------
	input:
		double integer  -	julian->integer,    
		double fraction -	julian->fraction
   output:
 
 	 vector2                      - heliocentric vector of earth 

-----------------------------------------------------*/

JNIEXPORT jdoubleArray JNICALL 
Java_ovt_util_Utils_earthJNI(env, obj, integer, fraction)
JNIEnv *env;
jobject obj;
jdouble integer, fraction;
{

	jdoubleArray jres = (*env)->NewDoubleArray(env, 3);
	jdouble *res = (*env)->GetDoubleArrayElements(env, jres, 0);
	struct julian jday;
	jday.integer = integer;
	jday.fraction = fraction;
	
	earth(&jday, res);
   
	(*env)->ReleaseDoubleArrayElements(env, jres, res, 0);
   
	return jres;
}


/*----------- JNI to sun_vect -> ------------------------
	input:
		double integer  -	julian->integer,    
		double fraction -	julian->fraction
   output:
 
 	 vector from earth to sun

-----------------------------------------------------*/

JNIEXPORT jdoubleArray JNICALL 
Java_ovt_util_Utils_sun_1vectJNI(env, obj, integer, fraction)
JNIEnv *env;
jobject obj;
jdouble integer, fraction;
{

	jdoubleArray jres = (*env)->NewDoubleArray(env, 3);
	jdouble *res = (*env)->GetDoubleArrayElements(env, jres, 0);
	struct julian jday;
	jday.integer = integer;
	jday.fraction = fraction;
	
	sun_vect(&jday, res);
   
	(*env)->ReleaseDoubleArrayElements(env, jres, res, 0);
   
	return jres;
}

/*----------- JNI to sun_vect -> ------------------------
	input:
		double integer  -	julian->integer,    
		double fraction -	julian->fraction
   output:
 
 	The angle in degrees between the vernal equinox (X axis) and the
	Greewich meridian. All numbers from pgs B6-B7 of 1984 Alamanc
-----------------------------------------------------*/

JNIEXPORT jdouble JNICALL 
Java_ovt_mag_MagPack_ghaJNI(env, obj, integer, fraction)
JNIEnv *env;
jobject obj;
jdouble integer, fraction;
{

	struct julian jday;
	jday.integer = integer;
	jday.fraction = fraction;
	
	return gha(&jday);
}
