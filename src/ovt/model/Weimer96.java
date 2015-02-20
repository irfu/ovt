/* This is Java version translated from Fortan 
   (translation by Grzegorz Juchnikowski/SRC)

************************ Copyright 1996, Dan Weimer/MRC ***********************
*
* Subroutines to calculate the electric potentials from the Weimer '96 model of
* the polar cap ionospheric electric potentials.
*
* To use, first call subroutine ReadCoef once.
* Next, call SetModel with the specified input parameters.
* The function EpotVal(gLAT,gMLT) can then be used repeatively to get the
* electric potential at the desired location in geomagnetic coordinates.
*
* This code is protected by copyright and is
* distributed for research or educational use only.
* Commerical use without written permission from Dan Weimer/MRC is prohibited.
*/

package ovt.model;

import java.io.*;
import java.lang.Math.*;
import ovt.util.ElectPotFileReader;
import ovt.OVTCore;

public class Weimer96 {

  //common SetCoef
  int ML,MM;
  double[][][] Coef;
  double pi;

  //common AllCoefs
  int MaxL,MaxM,MaxN;
  double[][][][][][] Cn;

  private boolean initialized = false;

  private void cWeimer96(String _path){
    Coef = new double[2][9][4];
    Cn = new double[4][2][5][2][9][4];
    pi = Math.PI;
    //load coefficients for Weimer96, this can be done once.
    try {
      ReadCoef(_path+"w96.dat");
      initialized = true;
    } catch(IOException e){
      System.out.println(getClass().getName() + " -> " + e.toString());
    }
  }

  //this constructor is for separate testing of the model.
  public Weimer96(String _path){ cWeimer96(_path); }

  //this constructor is used in OVT
  public Weimer96(OVTCore core){ cWeimer96(core.getUserdataDir()); }


  public double EpotVal(double gLAT, double gMLT){
/*
Return the value of the electric potential in kV at
corrected geomagnetic coordinates gLAT (degrees) and gMLT (hours).
Must first call ReadCoef and SetModel to set up the model coeficients for
the desired values of Bt, IMF clock angle, Dipole tilt angle, and SW Vel.
*/
    if(! initialized ) return 0;
    double Theta,Phi,Z,ct,Phim,r;
    double[][] Plm = new double[21][21];
    int l,m,limit;
    r = 90 - gLAT;
    if( r < 45.0 ){
      Theta = r*pi/45;
      Phi = gMLT*pi/12;
      Z = Coef[0][0][0];
      ct = Math.cos(Theta);
      Legendre(ct,ML,MM,Plm);
      for( l=1; l<=ML; l++ ){
        Z += Coef[0][l][0] * Plm[l][0];
        limit = (l < MM) ? l : MM;
        for( m=1; m<=limit; m++ ){
          Phim = Phi*m;
          Z += Coef[0][l][m]*Plm[l][m]*Math.cos(Phim) 
             + Coef[1][l][m]*Plm[l][m]*Math.sin(Phim);
        }
      }
    }else{
      Z = 0;
    }
    return Z;
  }

/*
	FUNCTION EpotVal(gLAT,gMLT)
	REAL gLAT,gMLT
	Real Theta,Phi,Z,ct,Phim
	REAL Plm(0:20,0:20)

	REAL Coef(0:1,0:8,0:3),pi
	INTEGER ML,MM
	COMMON/SetCoef/ML,MM,Coef,pi

	r=90.-gLAT
	IF(r .LT. 45.)THEN
	  Theta=r*pi/45.
          Phi=gMLT*pi/12.
	  Z=Coef(0,0,0)
	  ct=COS(Theta)
	  CALL Legendre(ct,ML,MM,Plm)
	  DO l=1,ML
	    Z=Z + Coef(0,l,0)*Plm(l,0)
	    IF(l.LT.MM)THEN
	      limit=l
	    ELSE
	      limit=MM
	    ENDIF
	    DO m=1,limit
	      phim=phi*m
	      Z=Z + Coef(0,l,m)*Plm(l,m)*COS(phim) +
     $		   Coef(1,l,m)*Plm(l,m)*SIN(phim) 
	    ENDDO
	  ENDDO
	ELSE
	  Z=0.
	ENDIF
	EpotVal=Z
	RETURN
	END
*/



  private void ReadCoef(String fn) throws IOException {
/*
Read in the data file with the model coeficients
*/
System.out.println("Weimer96: loading file \""+fn+"\"");
    ElectPotFileReader f = new ElectPotFileReader(fn);
    int i,l,k,n,m,ii,ll,kk,nn,mm,mlimit,klimit,ilimit,ic;

    f.SkipEOL();    //skip the first line
    MaxL = (int)f.GetNumber();
    MaxM = (int)f.GetNumber();
    MaxN = (int)f.GetNumber();
    f.SkipEOL();
//System.out.println("MaxL="+MaxL+" MaxM="+MaxM+" MaxN="+MaxN);
    for( l=0; l<=MaxL; l++ ){
      mlimit = (l < MaxM) ? l : MaxM;
      for( m=0; m<=mlimit; m++ ){
        klimit = (m < 1) ? 0 : 1;
        for( k=0; k<=klimit; k++ ){
          ll = (int)f.GetNumber();
          mm = (int)f.GetNumber();
          kk = (int)f.GetNumber();
          f.SkipEOL();
          if( (ll != l) || (mm != m) || (kk != k) ) throw new IOException("Data File Format Error");
          for( n=0; n<=MaxN; n++ ){
            ilimit = (n < 1) ? 0 : 1;
            for( i=0; i<=ilimit; i++ ){
              nn = (int)f.GetNumber();
              ii = (int)f.GetNumber();
              if( (nn != n) || (ii != i) ) throw new IOException("Data File Format Error");
              for( ic=0; ic<4; ic++ ) Cn[ic][i][n][k][l][m] = f.GetNumber();
              f.SkipEOL();
            }
          }
        }
      }
    }
    f.Close();
  }

/*
	SUBROUTINE ReadCoef
	INTEGER udat
	CHARACTER*15 cfile,skip
	REAL C(0:3)
	REAL Cn( 0:3 , 0:1 , 0:4 , 0:1 , 0:8 , 0:3 )
	INTEGER MaxL,MaxM,MaxN
	COMMON /AllCoefs/MaxL,MaxM,MaxN,Cn

	cfile='w96.dat'
	udat=99
	OPEN(udat,FILE=cfile,STATUS='OLD')
  900   FORMAT(A15)
 1000	FORMAT(3I8)
 2000	FORMAT(3I2)
 3000	FORMAT(2I2,4E15.6)

	READ(udat,900) skip
	READ(udat,1000) MaxL,MaxM,MaxN
	DO l=0,MaxL
	  IF(l.LT.MaxM)THEN
	    mlimit=l
	  ELSE
	    mlimit=MaxM
	  ENDIF
	  DO m=0,mlimit
	    IF(m.LT.1)THEN
	      klimit=0
	    ELSE
	      klimit=1
	    ENDIF
	    DO k=0,klimit
	      READ(udat,2000) ll,mm,kk
	      IF(ll.NE.l .OR. mm.NE.m .OR. kk.NE.k)THEN
		PRINT *,'Data File Format Error'
		STOP
	      ENDIF
	      DO n=0,MaxN
	        IF(n.LT.1)THEN
	          ilimit=0
	        ELSE
	          ilimit=1
	        ENDIF
		DO i=0,ilimit
		  READ(udat,3000) nn,ii,C
	          IF(nn.NE.n .OR. ii.NE.i)THEN
		    PRINT *,'Data File Format Error'
		    STOP
		  ENDIF
		  Cn(0,i,n,k,l,m)=C(0)
		  Cn(1,i,n,k,l,m)=C(1)
		  Cn(2,i,n,k,l,m)=C(2)
		  Cn(3,i,n,k,l,m)=C(3)
		ENDDO
	      ENDDO
	    ENDDO
	  ENDDO
	ENDDO

	CLOSE(udat)
	RETURN
	END
*/



  private double FSVal(double omega, int MaxN, double[][] FSC){
/*
Evaluate a  Sine/Cosine Fourier series for N terms up to MaxN
at angle omega, given the coeficients in FSC
*/
    double Y,theta;
    int n;
    Y = 0;
    for( n=0; n<=MaxN; n++ ){
      theta = omega*n;
      Y += FSC[0][n] * Math.cos(theta) + FSC[1][n] * Math.sin(theta);
    }
    return Y;
  }

/*
	FUNCTION FSVal(omega,MaxN,FSC)
	REAL omega,FSC(0:1,0:*)
	INTEGER MaxN,n
	REAL Y,theta
	Y=0.
	DO n=0,MaxN
	  theta=omega*n
	  Y=Y + FSC(0,n)*COS(theta) + FSC(1,n)*SIN(theta)
	ENDDO
	FSVal=Y
	RETURN
	END
*/



  public void SetModel(double angle, double Bt, double Tilt, double SWVel){
/*
Calculate the complete set of spherical harmonic coeficients,
given an aribitrary IMF angle (degrees from northward toward +Y),
magnitude Bt (nT), dipole tilt angle (degrees), 
and solar wind velocity (km/sec).
Returns the Coef in the common block SetCoef.
*/
    if(! initialized ) return;
    double[][] FSC = new double[2][5];
    double SinTilt,omega;
    int l,m,k,n,i,mlimit,klimit,ilimit;
    ML = MaxL;
    MM = MaxM;
    SinTilt = Math.sin(Tilt*pi/180);
    omega = angle*pi/180;
    for( l=0; l<=MaxL; l++ ){
      mlimit = (l < MaxM) ? l : MaxM;
      for( m=0; m<=mlimit; m++ ){
        klimit = (m < 1) ? 0 : 1;
        for( k=0; k<=klimit; k++ ){
          // Retrieve the regression coeficients and evaluate the function
          // as a function of Bt,Tilt,and SWVel to get each Fourier coeficient.
          for( n=0; n<=MaxN; n++ ){
            ilimit = (n < 1) ? 0 : 1;
            for( i=0; i<=ilimit; i++ ){
              FSC[i][n] = Cn[0][i][n][k][l][m] + Bt*Cn[1][i][n][k][l][m] +
                SinTilt*Cn[2][i][n][k][l][m] + SWVel*Cn[3][i][n][k][l][m];
            }
          }
          // Next evaluate the Fourier series as a function of angle.
          Coef[k][l][m] = FSVal(omega,MaxN,FSC);
        }
      }
    }
  }

/*
	SUBROUTINE SetModel(angle,Bt,Tilt,SWVel)
	REAL angle,Bt,Tilt,SWVel
	REAL FSC(0:1,0:4)
	REAL Cn( 0:3 , 0:1 , 0:4 , 0:1 , 0:8 , 0:3 )
	INTEGER MaxL,MaxM,MaxN
	COMMON /AllCoefs/MaxL,MaxM,MaxN,Cn

	REAL Coef(0:1,0:8,0:3),pi
	INTEGER ML,MM
	COMMON/SetCoef/ML,MM,Coef,pi

	pi=2.*ASIN(1.)
	ML=MaxL
	MM=MaxM
	SinTilt=SIND(Tilt)

	omega=angle*pi/180.
	DO l=0,MaxL
	  IF(l.LT.MaxM)THEN
	    mlimit=l
	  ELSE
	    mlimit=MaxM
	  ENDIF
	  DO m=0,mlimit
	    IF(m.LT.1)THEN
	      klimit=0
	    ELSE
	      klimit=1
	    ENDIF
	    DO k=0,klimit
* Retrieve the regression coeficients and evaluate the function
* as a function of Bt,Tilt,and SWVel to get each Fourier coeficient.
	      DO n=0,MaxN
	        IF(n.LT.1)THEN
	          ilimit=0
	        ELSE
	          ilimit=1
	        ENDIF
		DO i=0,ilimit
		  FSC(i,n)=Cn(0,i,n,k,l,m) + Bt*Cn(1,i,n,k,l,m) +
     $		   SinTilt*Cn(2,i,n,k,l,m) + SWVel*Cn(3,i,n,k,l,m)
		ENDDO
	      ENDDO
* Next evaluate the Fourier series as a function of angle.
      	      Coef(k,l,m)=FSVal(omega,MaxN,FSC)
	    ENDDO
	  ENDDO
	ENDDO
	RETURN
	END
*/



  private static void Legendre(double x, int lmax, int mmax, double[][] Plm){
/*
compute Associate Legendre Function P_l^m(x)
for all l up to lmax and all m up to mmax.
returns results in array Plm
if X is out of range ( abs(x)>1 ) then value is returns as if x=1.
*/
    double xx,fact;
    int l,m,lm2;
    for( l=0; l<=20; l++ ) for( m=0; m<=20; m++ ) Plm[l][m] = 0;
    xx = Math.max(Math.min(x,1),-1);
    if( (lmax < 0) || (mmax < 0) || (mmax > lmax) ){
      System.out.println("Weimer96: Bad arguments to Legendre");
      return;
    }
    //First calculate all Pl0 for l=0 to l
    Plm[0][0] = 1;
    if( lmax > 0 ) Plm[1][0] = xx;
    if( lmax > 1 ) for( l=2; l<=lmax; l++ ){
      Plm[l][0] = ((2*l-1)*xx*Plm[l-1][0] - (l-1)*Plm[l-2][0]) / l;
    }
    if( mmax == 0 ) return;
    fact = Math.sqrt((1-xx)*(1+xx));
    for( m=1; m<=mmax; m++ ) for( l=m; l<=lmax; l++ ){
      lm2 = Math.max(l-2,0);
      Plm[l][m] = Plm[lm2][m] - (2*l-1)*fact*Plm[l-1][m-1];
    }
  }

/*
	SUBROUTINE LEGENDRE(x,lmax,mmax,Plm)
	DIMENSION Plm(0:20,0:20)
	  DO l=0,20
	    DO m=0,20
		Plm(l,m)=0.
	    ENDDO
	  ENDDO
	xx=MIN(x,1.)
	xx=MAX(xx,-1.)
	IF(lmax .LT. 0 .OR. mmax .LT. 0 .OR. mmax .GT. lmax )THEN
	  Print *,'Bad arguments to Legendre'
	  RETURN
	ENDIF
* First calculate all Pl0 for l=0 to l
	Plm(0,0)=1.
	IF(lmax.GT.0)Plm(1,0)=xx
	IF (lmax .GT. 1 )THEN
	  DO L=2,lmax
	    Plm(L,0)=( (2.*L-1)*xx*Plm(L-1,0) - (L-1)*Plm(L-2,0) )/L
	  ENDDO
	ENDIF
	IF (mmax .EQ. 0 )RETURN
	fact=SQRT( (1.-xx)*(1.+xx) )
	DO M=1,mmax
	  DO L=m,lmax
	    lm2=MAX(L-2,0)
	    Plm(L,M)=Plm(lm2,M) - ( 2*L-1)*fact*Plm(L-1,M-1)
	  ENDDO
	ENDDO
	RETURN
	END
*/


  public static void main(String[] args){
    double angle,Bt,Tilt,SWVel, gLAT,gMLT;
    Weimer96 w96;
    try {
      w96 = new Weimer96("");
    } catch (Exception e){
      System.out.println("Exception when reading file: " + e);
      return;
    }

    angle = 45;
    Bt = 10;
    Tilt = 0;
    SWVel = 400;
    w96.SetModel(angle,Bt,Tilt,SWVel);

    for( gMLT=0; gMLT<24; gMLT+=2 )
    for( gLAT=60; gLAT<90; gLAT+=2 ){
      System.out.println("EpotVal("+gLAT+","+gMLT+") = "+w96.EpotVal(gLAT,gMLT));
    }
  }

    
}
