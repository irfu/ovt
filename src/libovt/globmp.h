/* $Source: /stor/devel/ovt2g/src/globmp.h,v $
   $Date: 2001/06/21 14:20:22 $  */
#include "constants.h"
#define NAS      6     /* 10 (6) number of angular sectors in 90 deg range */
#define LIM      6     /* 15 (6) number of edge points on 0 -Xlim interval */
#define MAXE (2*NAS+LIM+1) /* number of points on the mpause edge */
#define NPF      120     /* max number of points along a field line */
#define NPO      120     /* max number of points on satellite orbit */
#define NPY	 7      /* number of y-plane field lines */

#define D_TO_R  0.017453293  /* degrees to radians */
#define M_TO_R  0.261799388  /* hours to radians   */
#define RE   6371.2     /* radius of the earth used in the program */
#define FALT  100.0     /* altitude (km) for footprint tracing */
#define ERR_MP  0.05     /* error (RE) in last closed line search */
#define ERR_TILT 4.0     /* update structure with tilt step ERR_TILT */
#define C_SEPARATION  1.5 /* exterior cusp separation (RE) at cusp_index */
#define EQ_MIN    0     /* if 1 then the eqatorial cross is minimum of
			   the northern and southern hemisphere.
			   if 0 north and south hemispheres are independent */

struct magnetosphere
{
   double mjd;          /* modified Julian day */
   double model;        /* magnetic field model */
   double tilt;         /* dipole tilt angle (deg) */
   double xlim;		/* minimum distance in the tail */
   double imf[3];       /* interplanetary magnetic field */
   int method;         /* method=0 no imf, 1=linear, 2=liner vanishing
			   3= normal vanishing */
   int n_cusp_index;    /* index such that f_line with NPF=ns-index */
   int s_cusp_index;    /* gives position of the exterior cusp    */
   double n_cusp[3];    /* exterior cusp north */
   double s_cusp[3];    /* exterior cusp south */
   int npy[2][NPY];       /* number of points in yplane lines */
   float yplane[2][NPY][NPF*3];  /*field lines at 70,75,80,85,90,85,80,75,70 */
   int ns[MAXE][4];     /* number of points  in f_line */
   double fcusp[MAXE][4][3]; /* corrected coordinates of cusp */
   double f_line[MAXE][4][NPF*3];  /* coordinates of f-lines */
} mps;                  /* all coordinates are in GSM in units of RE 
                           index 0 = north dusk
                                 1 = north dawn
                                 2 = south dawn
                                 3 = south dusk
                         */

struct ycutfield
{
   double mjd;          /* modified Julian day */
   double model;        /* magnetic field model */
   double tilt;         /* dipole tilt angle (deg) */
   double xlim;		/* minimum distance in the tail */
   double imf[3];       /* interplanetary magnetic field */
   int method;         /* method=0 no imf, 1=linear, 2=liner vanishing
			   3= normal vanishing */
   int cusp_index[4];    /* index such that edges with index 
                          gives position of the exterior cusp    */
   int nedge[4];	/* number of points in edges */
   float edges[4][NPF*3]; /* field lines at edges */
   int npy[2][NPY];       /* number of points in yplane lines */
   float yplane[2][NPY][NPF*3];  /*field lines at 70,75,80,85,90,85,80,75,70 
			 first 2 lines in NPY mark the cusp  */
   double cccusp[4][3]; /* corrected coordinates of cusp field lines */
   double smcusp[4][3]; /* dipole (SMC) coordinates of cusp field lines */
} ycut;


struct mag_activity
{
   double mjd;          /* modified Julian day */
   double model;        /* magnetic field model */
   double kp_index;     /* for future use */
   double imf[3];       /* interplanetary magnetic field */
   int method;          /* method=0 no imf, 1=lin 2=lin van 3=normal van */
} act[NPO][NSAT-1];             /* activity along the trajectory  
			   current position at tt[].lastin */


struct trajectory
{
   double mjd;          /* modified Julian day */
   double model;	/* magentic field model used to distances */
   double rr;		/* radius vector (RE)  */
   double gei[3];       /* GEI coordinates (RE) of the satellite */
   double vei[3];	/* GEI velocity  km/s  */
   double geo[3];       /* GEO coordinates (RE) of the satellite */
   double gsm[3];       /* GSM coordinates (RE) of the satellite */
   double gse[3];	/* GSE coordinates (RE) of the satellite */
   double dgsm[3];      /* distant footprint (GSM) of the sat.   */
   double fgeo[3];      /* footprint coord (GEO) at FALT */
   double fsmc[3];       /* footprint coord (SM)  corrected mag   */
   double fsm[3];       /* footprint coord (SM)     */
   double efoot_gsm[3]; /* equatorial foot in GSM */
   double foot_cor[2][3]; /* north=0/south=1 foot COR  */
   double foot_geo[2][3]; /* north=0/south=1 foot GEO */
   double dist_bs;      /* distance from bow shock (RE) */
   double dist_mp;      /* distance from magnetopause   */
   double dist_eq;      /* distance from equatorial structure */
   double dist_cu;      /* distance from the exterior cusp */
} tra[NPO][NSAT-1];       /* current position at tt.lastin */


