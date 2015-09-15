/* $Source: /stor/devel/ovt2g/src/constants.h,v $  $Date: 2002/02/25 15:48:06 $  */
#include <math.h>

#define WCT     WORLD_COORDINATE_TEXT
#define D_TO_R      0.017453293   /* degrees to radians */
#define R_TO_D      57.29577951   /* radians to degrees */
#define M_TO_R      0.261799388   /* MLT to radians     */
#define GEX     45.0    /* plot extent */
#define PEX      0.75
#define RE       6371.2  /* earth radius */

#define BLACK     0.0, 0.0, 0.0
#define WHITE     1.0, 1.0, 1.0
#define RED       1.0, 0.0, 0.0
#define MAGNETA   1.0, 0.0, 1.0
#define GREEN     0.0, 1.0, 0.0
#define D_GREEN   0.0, 0.5, 0.0
#define BLUE      0.0, 0.0, 1.0
#define D_BLUE    0.0, 0.0, 0.5
#define GRAY      0.5, 0.5, 0.5
#define L_GRAY    0.8, 0.8, 0.8
#define GSM_BGR	  0.5, 0.7, 1.0
#define POL_BGR	  0.0, 0.4, 1.0
#define EARTH_BGR 0.8, 1.0, 0.5
#define SKY_BACKGROUND  1.0, 1.0, 1.0
#define DITHER  8

#define HOLLOW 0
#define SOLIDI 1
#define PATTERN 2

#define NONE_STRUCT	50
#define YCUT_STRUCT	51
#define EDGE_STRUCT	52
#define EDYC_STRUCT	53

#define IN_LINE		61
#define OUT_LINE	62
#define IO_LINE		63

#define C_BAW    0
#define C_RGB    1
#define C_SYM	 2
#define LIN	1
#define LOG	0

#define GEI	0
#define GSM	1
#define GSE	2
#define GSEQ	3
#define GEO	4
#define SMC	5
#define COR	6
#define ECC	7


#define NORTH  2
#define N_A_S  4
#define NSAT	12
#define NSAT1   11
#define MAXPLOT  30

#define VABS(v)	sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2])
#define VDOT(a,b)	(a[0]*b[0]+a[1]*b[1]+a[2]*b[2])


//added by Serg for Tsyg_xx functions' parameters
#define KPINDEX    0 
#define DSTINDEX   0 
#define SINT       1 
#define IMF_Y      1 
#define COST       2 
#define TILT       2  
#define SWP        3 
#define IMF_Z      4  
#define G1         5  
#define G2         6  



#define T2001      2001   
#define T96        96   
#define T89        89   
#define T87        87   
