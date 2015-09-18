/* $Source: /stor/devel/ovt2g/src/exglobvar.h,v $   $Date: 2001/06/21 14:20:21 $   */
#include "constants.h"

/* time control */
extern	double MjdModel;
extern double ModelFactor;
extern double KpIndex;
extern double PSW, DSTindex, AEindex;
extern double Msub;
extern double Bsub;
extern int Nsat;
extern double MjdStart[NSAT];
extern	double MjdSat[NSAT][3];
extern char SatName[NSAT][15];
extern char Filenames[NSAT][80];
extern double Hourfwd[NSAT];
extern int Traindex[NSAT];

/* model */
extern	double Model;
extern	int Satnum;
extern	int WhichCamera;
extern int CamIndex;
extern int WhichHem;
extern int RunOrbit;
extern int Zoom;
extern int Geomap;
extern int markTheCusp;
extern unsigned long Timeout;
extern char Odirectory[80];
extern char Mdirectory[80];
extern int NonInit;
extern int OrbitLines;
extern int FieldStruct;
extern char Freja_attitude[80];
extern char Viking_attitude[80];



extern	float Xmin;
extern	float	Xmax;
extern	float Ymin;
extern	float	Ymax;
extern	float Zmin;
extern	float	Zmax;
extern	double Latmin;
extern double MapRadius;

extern	int Fgsm;
extern	int Fpol;
extern int Fzom;
extern char Bitgsm[60];
extern char Bitpol[60];
extern  int Sgsm;
extern  int Spol;
extern  int SaveOn;
extern	int Sides;
extern  int Ygrids;
extern int Egrids;
extern 	int Pgridlevel;
extern	int Pcoord;
extern int Mcoord;
extern char *MCname[8];
extern	int Color_gsm;
extern	int Color_pol;
extern float ColorVal;

extern int Gseg1;
extern int Gsegment;
extern int Osegment;
extern int Ysegment;
extern int Gtext;
extern int Zsegment;
extern int Pseg1;
extern int Psegment;
extern int Fsegment;
extern int Ptext;
extern char Destination[10];
extern int LineC[8];
extern int LineT[4];
extern int Mark[5];

extern double Calt;

extern	double Gcampos[3], Pcampos[3];
extern	int CamSet[6][2];
extern	int CamLim[6][2][2];

extern char ComposePlot[MAXPLOT];
extern int plotMenu[MAXPLOT];
extern int Nplots;

extern char Pol_bottom_text[50];
extern char Pol_top_text[50];
extern char Gsm_bottom_text[50];
extern char Gsm_top_text[50];

extern float Minscale;
extern float Maxscale;
extern char Scaletext[20];
extern int Linscale;
extern int PaintB;
extern float Prmin;

extern double Imf[3];
extern int ImfMethod;
extern int Polbgr;

