/* $Source: /stor/devel/ovt2g/src/globvar.h,v $  $Date: 2000/03/20 11:42:37 */
#include "constants.h"

/* time control */
double MjdModel=9131.0;
double Model=-10.0;
double	ModelFactor=1.0;
double KpIndex=0.0;
double PSW=4.0;       /* solar wind pressue nT 0.5 to 10 */
double DSTindex=-40.0;  /* from -100 to 20  nT */
double AEindex=400.0;  /* from 40 to 800 nT */
double Msub=11.0;
double Bsub=13.5;
double MjdStart[NSAT];
int Nsat;
double MjdSat[NSAT][3];

char SatName[NSAT][15]={"S0", "S1","S2","S3","S4"};
char Filenames[NSAT][80];
double Hourfwd[NSAT]={4.40, 57.0, 66.0, 82.0, 24.0};
/*  orbital period hours = 2.7645e-6*A*sqrt(A) A(km)=semimajor axis   */
int Satnum=NSAT-1;
int Traindex[NSAT]={0,0,0,0,0,0,0,0,0,0};
int NonInit =0;  /* not initialized */
int WhichCamera=0;  /* GSM default */
int CamIndex	=0; /* zoom */
int WhichHem =1;  /* Norh hemisphere */
int RunOrbit=0;
int Zoom=0;
int Geomap=0;
int markTheCusp=0;
unsigned long Timeout=500;
int OrbitLines=IO_LINE;
int FieldStruct=EDYC_STRUCT;
char Odirectory[80]="odata";
char Mdirectory[80]="mdata";
char Freja_attitude[80]=" ";
char Viking_attitude[80]=" ";


float Xmin=(-30.0);
float	Xmax=20.0;
float Ymin=(-30.0);
float	Ymax=30.0;
float Zmin=-30.0;
float	Zmax=30.0;
double Latmin=50.0;
double MapRadius=8.0;   /* radius for mapping cusp etc. */


int 	Fgsm=(-1);
int	Fpol=(-1);
int     Fzom=(-1);
char Bitgsm[60];
char Bitpol[60];
int Sgsm = -1;
int Spol = -1;
int SaveOn=0;
int Sides=N_A_S;
int Ygrids=1;
int Egrids=1;
int Pgridlevel=0;
int Pcoord=SMC;
int Mcoord=GSM;
char *MCname[8]= {"GEI","GSM","GSE","GSEQ","GEO","SMC","COR","ECC"};
int Color_gsm=C_RGB;
int Color_pol=C_RGB;
float ColorVal=0.0;
int Gseg1=1;
int Gsegment=11;
int Osegment=12;
int Ysegment=13;
int Gtext=14;
int Zsegment=15;
int Pseg1=2;
int Psegment=21;
int Fsegment=22;
int Ptext=24;
char Destination[10]="sawapj";
/* 0=white,1=black,2=red,3=yellow,4=green,5=cyan,6=blue,7=magneta  */
#ifdef STARBASE
int LineC[8]={1,0,2,3,4,5,6,7};
int LineT[4]={0,1,2,3};
int Mark[5]={0,1,2,3,4};
#else
int LineC[8]={0,1,2,3,4,5,6,7};
int LineT[4]={1,2,3,4}; /*   0=solid,1=dash,2=dot,3=dot-dash    */
int Mark[5]={1,2,3,4,5};  /* 0=dot,1=plus,2=star,3=circle,4=x */
#endif

double Calt=100.0;

double Gcampos[3], Pcampos[3];
int CamSet[6][2]= {
/* R   */	170, 28,
/* mlt */	15, 15,
/* lat */	30, 90,
/* ref.x */	00, 00,
/* ref.y */	00, 00,
/* ref.z */	00, 10 };

int CamLim[6][2][2]={
/* R   */	10, 200, 1, 35,
/* mlt */	0, 24, 00, 24,
/* lat */	0, 90, 0, 90,
/* ref.x */	-30, 10, -8, 8,
/* ref.y */	-30, 30, -8, 8,
/* ref.z */	 -30, 30, -0,10 };

/* plot options  */
char ComposePlot[MAXPLOT]={'\0'};  
int
plotMenu[MAXPLOT]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
int  Nplots=0;

char Pol_bottom_text[50]="\0";
char Pol_top_text[50]="\0";
char Gsm_bottom_text[50]="\0";
char Gsm_top_text[50]="\0";

float Minscale=1.0;
float Maxscale=50.0;
char Scaletext[20]="B [nT]";
int Linscale=1;
int PaintB =0; /* paint B 1=b.grad.bxb  */
float Prmin=5.0;


double Imf[3] ={0.0, 0.0, 0.0};
int ImfMethod =0;
int Polbgr=0;


