#ifdef UNIX
/*char *tzname[];*/
#endif

#ifdef UNIX
int daylight;			/* 1 -> daylight savings is active */
#endif
long timez;			/* (GMT-local) in sec (without day. sav.) */

double local_2_gmt;		/* (GMT-local) in days including day. sav. */
long  local_2_gmt_sec;		/* (GMT-local) in seconds inc. day. sav. */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#ifdef UNIX
#include <string.h>
#include <sys/time.h>
#include <sys/file.h>
#else
#define LINT_ARGS
#include <fcntl.h>
#include <sys\types.h>
#include <sys\stat.h>
#include <io.h>
#include <time.h>
#include <dos.h>
#endif

/* include "astlib.h" */
int debug_flag, iflag;

#define TRUE 1
#define FALSE 0
#define X 0
#define Y 1
#define Z 2
#define PI 3.14159265358973
#define PI2 PI*2.
#define SEC_PER_DAY 86400.
#define MIN_PER_DAY 1440.
#define J1950 2433282.50
#define J1970 2440587.50
#define J1985 2446066.50
#define J2000 2451545.00
#define R_EARTH 6378.145

#define R_MOON 1738.57
double today;
double rad
= {PI/180.}
;
double irad
= {180./PI}
;
double au
= {1.4959965E08}
;
double station_lat
= {37.34510}
;
double station_long
= { -122.20196}
;
double station_height
= {.466}
;
struct julian {
  double integer;		/* separate integer and fractional parts */
  double fraction;		/* are used to minimize roundoff problems */
}
;
#ifdef VMS
long gmdelta=7;
#endif

struct julian epoch_day;	/* epoch for current elements */
long obj;			/* NORAD # of satellite */
char sat_name[50];		/* ASCII name */

struct elem_ {			/* units kilometers, degrees, seconds */
  double R;			/* magnitude of radius vector */
  double Rdot;			/* dR/dt */
  double Rfdot;			/* R df/dt (f=true anomaly) */
  double w;			/* arg periapsis */
  double W;			/* ascending node */
  double u;			/* true anomaly + arg periapsis */
  double i;			/* inclination */
  double a;			/* semi-major axis */
  double e;			/* eccentricity */
  double n0;			/* mean motion, rev/day */
  double position[3];		/* geocentric position in Km */
  double velocity[3];		/* geocentric in Km/sec */
};

double fmod2p ();
double fmod360 ();
double gha();
double kepler ();
double tdt();

struct satco_ {
	double Lcof;
	double Mcof;
	double Mdot;
	double Mo;
	double aodp;
	double aycof;
	double bstar;
	double c1;
	double c4;
	double c5;
	double ck2;
	double cosio;
	double d2;
	double d3;
	double d4;
	double delMo;
	double eo;
	double eta;
	double omegao;
	double omgcof;
	double omgdot;
	double sinMo;
	double sinio;
	double t2cof;
	double t3cof;
	double t4cof;
	double t5cof;
	double x1mth2;
	double x3thm1;
	double x7thm1;   
	double xincl;
	double xndd6o;
	double xndt2o;
	double xnodcf;
	double xnodeo;
	double xnodot;
	double xnodp;
	int isimp;
};
