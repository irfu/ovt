      COMMON /NM/NM
      COMMON /IYR/IYR
      write(*,'(A\)') ' Enter number of harmonics in IGRF (eg. 10) ? '
      read(*,*) NM
      write(*,'(A\)') ' Enter year (eg. 1999) ? '
      read(*,*) IYR
      write(*,'(A\)')
     + ' Enter altitude in km above the Earth''s surface (eg. 300.0) ? '
      read(*,*) HI
      write(*,'(A\)') ' Enter latitude step (eg. 5) ? '
      read(*,*) idlat
      write(*,'(A\)') ' Enter longitude step (eg. 10) ? '
      read(*,*) idlon
      RE=6371.2
      RH_BP=(RE+HI)/RE
      nlat=180/idlat+1
      nlon=360/idlon+1
      rlat1=-90
      rlon1=0

      open(5,file='geo2cor.dat') !,status='new')
      write(5,51) rlat1,nlat,idlat, rlon1,nlon,idlon
      do ilat=1,nlat
	glat=(ilat-1)*idlat+rlat1
	do ilon=1,nlon
	  glon=(ilon-1)*idlon+rlon1
	  call GEOCOR(glat,glon,RH_BP,DLA1,DLO1,clat,clon,PMI1)
	  write(5,52) glat,glon,clat,clon
	enddo
      enddo
      close(5)

      open(5,file='cor2geo.dat') !,status='new')
      write(5,51) rlat1,nlat,idlat, rlon1,nlon,idlon
      do ilat=1,nlat
	clat=(ilat-1)*idlat+rlat1
	do ilon=1,nlon
	  clon=(ilon-1)*idlon+rlon1
	  call CORGEO(glat,glon,RH_BP,DLA2,DLO2,clat,clon,PMI2)
	  write(5,52) clat,clon,glat,glon
	enddo
      enddo
      close(5)

51    format(2(F5.0,I3,I3))
52    format(2F5.0,2F8.2)
	end


	 	
C  *********************************************************************

      SUBROUTINE CORGEO(SLA,SLO,RH,DLA,DLO,CLA,CLO,PMI)

C  Calculates geocentric coordinates from corrected geomagnetic ones.

C  The code is written by Vladimir Popov and Vladimir Papitashvili
C  in mid-1980s; revised by V. Papitashvili in February 1999

      COMMON /NM/NM
      COMMON /IYR/IYR

C  This takes care if CLA is a dummy value (e.g., 999.99)
      
	    jc = 0
      if(abs(cla).lt.1.) then
          write(*,*)
     +'WARNING - No calculations within +/-1 degree near CGM equator'
          jc = 1
      endif
      if(cla.gt.999..or.jc.eq.1) then
        SLA = 999.99
        SLO = 999.99
        DLA = 999.99
        DLO = 999.99
        PMI = 999.99
        return
      endif

        NG = NM

       COL = 90. - CLA
         R = 10.
        R1 = R
        R0 = R
       COL = COL*0.017453293
       RLO = CLO*0.017453293
        SN = SIN(COL)
       SN2 = SN*SN

C  The CGM latitude should be at least 0.01 deg. away of the CGM pole

      IF(SN2.LT.0.000000003) SN2 = 0.000000003
C      RFI = 1./SN2
       RFI = RH/SN2
       PMI = RFI
      IF(PMI.GT.99.999) PMI = 999.99      
         AA10 = R/RFI

C  RFI = R if COL = 90 deg.

        IF(RFI.LE.R) GOTO 1
        SAA = AA10/(1.-AA10)
        SAQ = SQRT(SAA)
       SCLA = ATAN(SAQ)
      IF(CLA.LT.0) SCLA = 3.14159265359 - SCLA

      GOTO 3
    1   SCLA = 1.57079632679
          R0 = RFI

    3 CALL SPHCAR(R0,SCLA,RLO,XM,YM,ZM,1)
      CALL GEOMAG(X,Y,Z,XM,YM,ZM,-1,IYR)
         RL = R0
       FRAC = -0.03/(1. + 3./(RL - 0.6))
      IF(CLA.LT.0.) FRAC = -FRAC
          R = R0

    5    DS = R*FRAC
         NM = (1. + 9./R) + 0.5
      CALL SHAG(X,Y,Z,DS)
          R = SQRT(X**2+Y**2+Z**2)
      IF(R.LE.RH) GOTO 7
         R1 = R
         X1 = X
         Y1 = Y
         Z1 = Z
         GOTO 5

C  Define intersection with the start surface

    7   DR1 = ABS(RH - R1)
        DR0 = ABS(RH - R)
       DR10 = DR1 + DR0
       IF(DR10.NE.0.) THEN
         DS = DS*(DR1/DR10)
         CALL SHAG(X1,Y1,Z1,DS)
       ENDIF

      CALL SPHCAR(R,GTET,GXLA,X1,Y1,Z1,-1)
        GTH = GTET*57.2957751
        SLO = GXLA*57.2957751
        SLA = 90. - GTH
      CALL GEOMAG(X1,Y1,Z1,XM,YM,ZM,1,IYR)
      CALL SPHCAR(RM,TH,PF,XM,YM,ZM,-1)
        DLO = PF*57.2957751
        DLA = 90. - TH*57.2957751

        NM = NG

C  Because CORGEO cannot check if the CGM --> GEO transformation is
C  performed correctly in the equatorial area (that is, where the IGRF
C  field line may never cross the dipole equatorial plane). Therefore,
C  the backward check is required for geocentric latitudes lower than 
C  30 degrees (see the paper referenced in GEOLOW)

      IF(ABS(SLA).LT.30..OR.ABS(CLA).LT.30.) THEN
          CALL GEOCOR(SLA,SLO,RH,DLS,DLS,CLAS,CLOS,PMS)

C BP modification 

C      IF(CLAS.GT.999.) CALL GEOLOW(SLA,SLO,RH,CLAS,CLOS,RBM,SLAC,SLOC)
C       IF(ABS(ABS(CLA)-ABS(CLAS)).GE.1.) THEN
          write(*,*)
     +'WARNING - Selected CGM_Lat.=',CLA,' is located in the ',
     +'near CGM equator area where the latter cannot be defined'
           SLA = 999.99
           SLO = 999.99
           PMI = 999.99
C        ENDIF
      ENDIF

      RETURN
      END

C  *********************************************************************
C  *********************************************************************
      SUBROUTINE GEOCOR(SLA,SLO,RH,DLA,DLO,CLA,CLO,PMI)

C  Calculates corrected geomagnetic coordinates from geocentric ones

C  The code is written by Vladimir Popov and Vladimir Papitashvili
C  in mid-1980s; revised by V. Papitashvili in February 1999

      COMMON /NM/NM
      COMMON /IYR/IYR

C  This takes care if SLA is a dummy value (e.g., 999.99)

      if(sla.gt.999.) then
        CLA = 999.99
        CLO = 999.99
        DLA = 999.99
        DLO = 999.99
        PMI = 999.99
        return
      endif

         NG = NM

        COL = 90. - SLA
          R = RH
         R1 = R
        COL = COL*0.017453293
        RLO = SLO*0.017453293
      CALL SPHCAR(R,COL,RLO,X,Y,Z,1)
      CALL GEOMAG(X,Y,Z,XM,YM,ZM,1,IYR)
      CALL SPHCAR(RM,TH,PF,XM,YM,ZM,-1)
        SZM = ZM
        DLO = PF*57.2957751
        DCO = TH*57.2957751
        DLA = 90. - DCO
         RL = R/(SIN(TH))**2
       FRAC = 0.03/(1. + 3./(RL - 0.6))

      IF(SZM.LT.0.) FRAC = -FRAC

C  Error to determine the dipole equtorial plane: aprox. 0.5 arc min

        HHH = 0.0001571

C  Trace the IGRF magnetic field line to the dipole equatorial plane
   
   1     DS = R*FRAC
   3     NM = (1. + 9./R) + 0.5
         R1 = R
         X1 = X
         Y1 = Y
         Z1 = Z
      CALL SHAG(X,Y,Z,DS)
      CALL GEOMAG(X,Y,Z,XM,YM,ZM,1,IYR)
      CALL SPHCAR(R,C,S,XM,YM,ZM,-1)

C  As tracing goes above (RH+10_Re), use the dipole field line  

        IF(R.GT.10.+RH) GOTO 9

C  If the field line returns to the start surface without crossing the
C  dipole equatorial plane, no CGM coordinates can be calculated

        IF(R.LE.RH) GOTO 11

        DCL = C - 1.5707963268
        IF(ABS(DCL).LE.HHH) GOTO 9
        RZM = ZM
        IF(SZM.GT.0..AND.RZM.GT.0.) GOTO 1
        IF(SZM.LT.0..AND.RZM.LT.0.) GOTO 1
          R = R1
          X = X1   
          Y = Y1                 
          Z = Z1   
         DS = DS/2.         
          GOTO 3

   9  CALL GEOMAG(X,Y,Z,XM,YM,ZM,1,IYR)
      CALL SPHCAR(R,GTET,GXLA,XM,YM,ZM,-1)
         ST = ABS(SIN(GTET))
        RRH = ABS(RH/(R - RH*ST**2))
        CLA = 1.5707963 - ATAN(ST*SQRT(RRH))
        CLA = CLA*57.2957751
        CLO = GXLA*57.2957751
      IF(SZM.LT.0.) CLA = -CLA
       SSLA = 90. - CLA
       SSLA = SSLA*0.017453293
         SN = SIN(SSLA)
C       PMI = 1/(SN*SN)
        PMI = RH/(SN*SN)
        GOTO 13

   11   CLA = 999.99
        CLO = 999.99
        PMI = 999.99

   13    NM = NG

      RETURN
      END

C  *********************************************************************

C  *********************************************************************
      SUBROUTINE SHAG(X,Y,Z,DS)

C  Similar to SUBR STEP from GEOPACK-1996 but SHAG takes into account
C  only internal sources

C  The code is re-written from Tsyganenko's subroutine STEP by 
C  Natalia and Vladimir Papitashvili in mid-1980s

      COMMON/A5/DS3
      
          DS3 = -DS/3.
      CALL RIGHT(X,Y,Z,R11,R12,R13)
      CALL RIGHT(X+R11,Y+R12,Z+R13,R21,R22,R23)
      CALL RIGHT(X+.5*(R11+R21),Y+.5*(R12+R22),Z+.5*(R13+R23),
     *R31,R32,R33)
      CALL RIGHT(X+.375*(R11+3.*R31),Y+.375*(R12+3.*R32),
     *Z+.375*(R13+3.*R33),R41,R42,R43)
      CALL RIGHT(X+1.5*(R11-3.*R31+4.*R41),
     *Y+1.5*(R12-3.*R32+4.*R42),Z+1.5*(R13-3.*R33+4.*R43),
     *R51,R52,R53)
        X = X+.5*(R11+4.*R41+R51)
        Y = Y+.5*(R12+4.*R42+R52)
        Z = Z+.5*(R13+4.*R43+R53)

      RETURN
      END

C  *********************************************************************
C  *********************************************************************

      SUBROUTINE RIGHT(X,Y,Z,R1,R2,R3)

C  Similar to SUBR RHAND from GEOPACK-1996 but RIGHT takes into account
C  only internal sources

C  The code is re-written from Tsyganenko's subroutine RHAND
C  by Natalia and Vladimir Papitashvili in mid-1980s

      COMMON /A5/DS3
      COMMON /NM/NM
      COMMON /IYR/IYR

      CALL SPHCAR(R,T,F,X,Y,Z,-1)
      CALL IGRF(IYR,NM,R,T,F,BR,BT,BF)
      CALL BSPCAR(T,F,BR,BT,BF,BX,BY,BZ)
        B = DS3/SQRT(BX**2+BY**2+BZ**2)
       R1 = BX*B
       R2 = BY*B
       R3 = BZ*B

      RETURN
      END

C  *********************************************************************
C  *********************************************************************


        SUBROUTINE IGRF(IY,NM,R,T,F,BR,BT,BF)

c  Aug 26, 1997: Subroutine IGRF is modified by V. Papitashvili - SHA
c    coefficients for DGRF-1990, IGRF-1995, and SV 1995-2000 are added
c    (EOS, v.77, No.16, p.153, April 16, 1996)

c  Feb 03, 1995: Modified by Vladimir Papitashvili (SPRL, University of 
c    Michigan) to accept dates between 1945 and 2000 
       
C  MODIFIED TO ACCEPT DATES BETWEEN 1965 AND 2000; COEFFICIENTS FOR IGRF
C  1985 HAVE BEEN REPLACED WITH DGRF1985 COEFFICIENTS [EOS TRANS. AGU 
C  APRIL 21, 1992, C  P. 182]. ALSO, THE CODE IS MODIFIED TO ACCEPT 
C  DATES BEYOND 1990, AND TO USE LINEAR EXTRAPOLATION BETWEEN 1990 AND 
C  2000 BASED ON THE IGRF COEFFICIENTS FROM THE SAME EOS ARTICLE

C  Modified by Mauricio Peredo, Hughes STX at NASA/GSFC, September 1992

C  CALCULATES COMPONENTS OF MAIN GEOMAGNETIC FIELD IN SPHERICAL 
C  GEOCENTRIC COORDINATE SYSTEM BY USING THIRD GENERATION IGRF MODEL 
C  (J. GEOMAG. GEOELECTR. V.34, P.313-315, 1982; GEOMAGNETISM AND 
C  AERONOMY V.26, P.523-525, 1986).

C  UPDATING OF COEFFICIENTS TO A GIVEN EPOCH IS MADE DURING THE FIRST 
C  CALL AND AFTER EVERY CHANGE OF PARAMETER IY

C---INPUT PARAMETERS:
C  IY - YEAR NUMBER (FROM 1945 UP TO 1990)
C  NM - MAXIMAL ORDER OF HARMONICS TAKEN INTO ACCOUNT (NOT MORE THAN 10)
C  R,T,F - SPHERICAL COORDINATES OF THE POINT (R IN UNITS RE=6371.2 KM,
C    COLATITUDE T AND LONGITUDE F IN RADIANS)
C---OUTPUT PARAMETERS:
C  BR,BT,BF - SPHERICAL COMPONENTS OF MAIN GEOMAGNETIC FIELD (in nT)

C  AUTHOR: NIKOLAI A. TSYGANENKO, INSTITUTE OF PHYSICS, ST.-PETERSBURG
C      STATE UNIVERSITY, STARY PETERGOF 198904, ST.-PETERSBURG, RUSSIA
C      (now the NASA Goddard Space Fligth Center, Greenbelt, Maryland)

      IMPLICIT NONE

C  G0, G1, and H1 are used in SUBROUTINE DIP to calculate geodipole's 
C  moment for a given year
  
      COMMON /DMOM/ G0,G1,H1
      REAL A(11),B(11),G(66),H(66),REC(66),
     *G1945(66),H1945(66),G1950(66),H1950(66),G1955(66),H1955(66),
     *G1960(66),H1960(66),G1965(66),H1965(66),G1970(66),H1970(66),
     *G1975(66),H1975(66),G1980(66),H1980(66),G1985(66),H1985(66),
     *G1990(66),H1990(66),G1995(66),H1995(66),DG2000(45),DH2000(45)

      REAL R,T,F,BR,BT,BF,DT,F2,F1,S,P,AA,PP,D,BBR,BBF,U,CF,SF,
     *     C,W,X,Y,Z,Q,BI,P2,D2,AN,E,HH,BBT,QQ,XK,DP,PM,G0,G1,H1

      INTEGER IY,NM,MA,IPR,IYR,KNM,N,N2,M,MNN,MN,K,MM

      LOGICAL BK,BM

      DATA G1945/0.,-30594.,-2285.,-1244., 2990., 1578., 1282.,-1834.,
     * 1255.,  913.,   944.,  776.,  544., -421.,  304., -253.,  346.,
     *  194.,  -20.,  -142.,  -82.,   59.,   57.,    6., -246.,  -25.,
     *   21., -104.,    70.,  -40.,    0.,    0.,  -29.,  -10.,   15.,
     *   29.,   13.,     7.,   -8.,   -5.,    9.,    7.,  -10.,    7.,
     *    2.,    5.,   -21.,    1.,  -11.,    3.,   16.,   -3.,   -4.,
     *   -3.,   -4.,    -3.,   11.,    1.,    2.,   -5.,   -1.,    8.,
     *   -1.,   -3.,     5.,   -2./

      DATA H1945/0.,     0., 5810.,    0.,-1702.,  477.,    0., -499.,
     *  186.,  -11.,     0.,  144., -276.,  -55., -178.,    0.,  -12.,
     *   95.,  -67.,  -119.,   82.,    0.,    6.,  100.,   16.,   -9.,
     *  -16.,  -39.,     0.,  -45.,  -18.,    2.,    6.,   28.,  -17.,
     *  -22.,    0.,    12.,  -21.,  -12.,   -7.,    2.,   18.,    3.,
     *  -11.,    0.,   -27.,   17.,   29.,   -9.,    4.,    9.,    6.,
     *    1.,    8.,     0.,    5.,    1.,  -20.,   -1.,   -6.,    6.,
     *   -4.,   -2.,     0.,   -2./

      DATA G1950/0.,-30554.,-2250.,-1341., 2998., 1576., 1297.,-1889.,
     * 1274.,  896.,   954.,  792.,  528., -408.,  303., -240.,  349.,
     *  211.,  -20.,  -147.,  -76.,   54.,   57.,    4., -247.,  -16.,
     *   12., -105.,    65.,  -55.,    2.,    1.,  -40.,   -7.,    5.,
     *   19.,   22.,    15.,   -4.,   -1.,   11.,   15.,  -13.,    5.,
     *   -1.,    3.,    -7.,   -1.,  -25.,   10.,    5.,   -5.,   -2.,
     *    3.,    8.,    -8.,    4.,   -1.,   13.,   -4.,    4.,   12.,
     *    3.,    2.,    10.,    3./

      DATA H1950/0.,     0., 5815.,    0.,-1810.,  381.,    0., -476.,
     *  206.,  -46.,     0.,  136., -278.,  -37., -210.,    0.,    3.,
     *  103.,  -87.,  -122.,   80.,    0.,   -1.,   99.,   33.,  -12.,
     *  -12.,  -30.,     0.,  -35.,  -17.,    0.,   10.,   36.,  -18.,
     *  -16.,    0.,     5.,  -22.,    0.,  -21.,   -8.,   17.,   -4.,
     *  -17.,    0.,   -24.,   19.,   12.,    2.,    2.,    8.,    8.,
     *  -11.,   -7.,     0.,   13.,   -2.,  -10.,    2.,   -3.,    6.,
     *   -3.,    6.,    11.,    8./

      DATA G1955/0.,-30500.,-2215.,-1440., 3003., 1581., 1302.,-1944.,
     * 1288.,  882.,   958.,  796.,  510., -397.,  290., -229.,  360.,
     *  230.,  -23.,  -152.,  -69.,   47.,   57.,    3., -247.,   -8.,
     *    7., -107.,    65.,  -56.,    2.,   10.,  -32.,  -11.,    9.,
     *   18.,   11.,     9.,   -6.,  -14.,    6.,   10.,   -7.,    6.,
     *    9.,    4.,     9.,   -4.,   -5.,    2.,    4.,    1.,    2.,
     *    2.,    5.,    -3.,   -5.,   -1.,    2.,   -3.,    7.,    4.,
     *   -2.,    6.,    -2.,    0./

      DATA H1955/0.,     0., 5820.,    0.,-1898.,  291.,    0., -462.,
     *  216.,  -83.,     0.,  133., -274.,  -23., -230.,    0.,   15.,
     *  110.,  -98.,  -121.,   78.,    0.,   -9.,   96.,   48.,  -16.,
     *  -12.,  -24.,     0.,  -50.,  -24.,   -4.,    8.,   28.,  -20.,
     *  -18.,    0.,    10.,  -15.,    5.,  -23.,    3.,   23.,   -4.,
     *  -13.,    0.,   -11.,   12.,    7.,    6.,   -2.,   10.,    7.,
     *   -6.,    5.,     0.,   -4.,    0.,   -8.,   -2.,   -4.,    1.,
     *   -3.,    7.,    -1.,   -3./

      DATA G1960/0.,-30421.,-2169.,-1555., 3002., 1590., 1302.,-1992.,
     * 1289.,  878.,   957.,  800.,  504., -394.,  269., -222.,  362.,
     *  242.,  -26.,  -156.,  -63.,   46.,   58.,    1., -237.,   -1.,
     *   -2., -113.,    67.,  -56.,    5.,   15.,  -32.,   -7.,   17.,
     *    8.,   15.,     6.,   -4.,  -11.,    2.,   10.,   -5.,   10.,
     *    8.,    4.,     6.,    0.,   -9.,    1.,    4.,   -1.,   -2.,
     *    3.,   -1.,     1.,   -3.,    4.,    0.,   -1.,    4.,    6.,
     *    1.,   -1.,     2.,    0./

      DATA H1960/0.,     0., 5791.,    0.,-1967.,  206.,    0., -414.,
     *  224., -130.,     0.,  135., -278.,    3., -255.,    0.,   16.,
     *  125., -117.,  -114.,   81.,    0.,  -10.,   99.,   60.,  -20.,
     *  -11.,  -17.,     0.,  -55.,  -28.,   -6.,    7.,   23.,  -18.,
     *  -17.,    0.,    11.,  -14.,    7.,  -18.,    4.,   23.,    1.,
     *  -20.,    0.,   -18.,   12.,    2.,    0.,   -3.,    9.,    8.,
     *    0.,    5.,     0.,    4.,    1.,    0.,    2.,   -5.,    1.,
     *   -1.,    6.,     0.,   -7./

      DATA G1965/0.,-30334.,-2119.,-1662., 2997., 1594., 1297.,-2038.,
     * 1292.,  856.,   957.,  804.,  479., -390.,  252., -219.,  358.,
     *  254.,  -31.,  -157.,  -62.,   45.,   61.,    8., -228.,    4.,
     *    1., -111.,    75.,  -57.,    4.,   13.,  -26.,   -6.,   13.,
     *    1.,   13.,     5.,   -4.,  -14.,    0.,    8.,   -1.,   11.,
     *    4.,    8.,    10.,    2.,  -13.,   10.,   -1.,   -1.,    5.,
     *    1.,   -2.,    -2.,   -3.,    2.,   -5.,   -2.,    4.,    4.,
     *    0.,    2.,     2.,    0./

      DATA H1965/0.,     0., 5776.,    0.,-2016.,  114.,    0., -404.,
     *  240., -165.,     0.,  148., -269.,   13., -269.,    0.,   19.,
     *  128., -126.,   -97.,   81.,    0.,  -11.,  100.,   68.,  -32.,
     *   -8.,   -7.,     0.,  -61.,  -27.,   -2.,    6.,   26.,  -23.,
     *  -12.,    0.,     7.,  -12.,    9.,  -16.,    4.,   24.,   -3.,
     *  -17.,    0.,   -22.,   15.,    7.,   -4.,   -5.,   10.,   10.,
     *   -4.,    1.,     0.,    2.,    1.,    2.,    6.,   -4.,    0.,
     *   -2.,    3.,     0.,   -6./

      DATA G1970/0.,-30220.,-2068.,-1781., 3000., 1611., 1287.,-2091.,
     * 1278.,  838.,   952.,  800.,  461., -395.,  234., -216.,  359.,
     *  262.,  -42.,  -160.,  -56.,   43.,   64.,   15., -212.,    2.,
     *    3., -112.,    72.,  -57.,    1.,   14.,  -22.,   -2.,   13.,
     *   -2.,   14.,     6.,   -2.,  -13.,   -3.,    5.,    0.,   11.,
     *    3.,    8.,    10.,    2.,  -12.,   10.,   -1.,    0.,    3.,
     *    1.,   -1.,    -3.,   -3.,    2.,   -5.,   -1.,    6.,    4.,
     *    1.,    0.,     3.,   -1./

      DATA H1970/0.,     0., 5737.,    0.,-2047.,   25.,    0., -366.,
     *  251., -196.,     0.,  167., -266.,   26., -279.,    0.,   26.,
     *  139., -139.,   -91.,   83.,    0.,  -12.,  100.,   72.,  -37.,
     *   -6.,    1.,     0.,  -70.,  -27.,   -4.,    8.,   23.,  -23.,
     *  -11.,    0.,     7.,  -15.,    6.,  -17.,    6.,   21.,   -6.,
     *  -16.,    0.,   -21.,   16.,    6.,   -4.,   -5.,   10.,   11.,
     *   -2.,    1.,     0.,    1.,    1.,    3.,    4.,   -4.,    0.,
     *   -1.,    3.,     1.,   -4./

      DATA G1975/0.,-30100.,-2013.,-1902., 3010., 1632., 1276.,-2144.,
     * 1260.,  830.,   946.,  791.,  438., -405.,  216., -218.,  356.,
     *  264.,  -59.,  -159.,  -49.,   45.,   66.,   28., -198.,    1.,
     *    6., -111.,    71.,  -56.,    1.,   16.,  -14.,    0.,   12.,
     *   -5.,   14.,     6.,   -1.,  -12.,   -8.,    4.,    0.,   10.,
     *    1.,    7.,    10.,    2.,  -12.,   10.,   -1.,   -1.,    4.,
     *    1.,   -2.,    -3.,   -3.,    2.,   -5.,   -2.,    5.,    4.,
     *    1.,    0.,     3.,   -1./

      DATA H1975/0.,     0., 5675.,    0.,-2067.,  -68.,    0., -333.,
     *  262., -223.,     0.,  191., -265.,   39., -288.,    0.,   31.,
     *  148., -152.,   -83.,   88.,    0.,  -13.,   99.,   75.,  -41.,
     *   -4.,   11.,     0.,  -77.,  -26.,   -5.,   10.,   22.,  -23.,
     *  -12.,    0.,     6.,  -16.,    4.,  -19.,    6.,   18.,  -10.,
     *  -17.,    0.,   -21.,   16.,    7.,   -4.,   -5.,   10.,   11.,
     *   -3.,    1.,     0.,    1.,    1.,    3.,    4.,   -4.,   -1.,
     *   -1.,    3.,     1.,   -5./

      DATA G1980/0.,-29992.,-1956.,-1997., 3027., 1663., 1281.,-2180.,
     * 1251.,  833.,   938.,  782.,  398., -419.,  199., -218.,  357.,
     *  261.,  -74.,  -162.,  -48.,   48.,   66.,   42., -192.,    4.,
     *   14., -108.,    72.,  -59.,    2.,   21.,  -12.,    1.,   11.,
     *   -2.,   18.,     6.,    0.,  -11.,   -7.,    4.,    3.,    6.,
     *   -1.,    5.,    10.,    1.,  -12.,    9.,   -3.,   -1.,    7.,
     *    2.,   -5.,    -4.,   -4.,    2.,   -5.,   -2.,    5.,    3.,
     *    1.,    2.,     3.,    0./

      DATA H1980/0.,     0., 5604.,    0.,-2129., -200.,    0., -336.,
     *  271., -252.,     0.,  212., -257.,   53., -297.,    0.,   46.,
     *  150., -151.,   -78.,   92.,    0.,  -15.,   93.,   71.,  -43.,
     *   -2.,   17.,     0.,  -82.,  -27.,   -5.,   16.,   18.,  -23.,
     *  -10.,    0.,     7.,  -18.,    4.,  -22.,    9.,   16.,  -13.,
     *  -15.,    0.,   -21.,   16.,    9.,   -5.,   -6.,    9.,   10.,
     *   -6.,    2.,     0.,    1.,    0.,    3.,    6.,   -4.,    0.,
     *   -1.,    4.,     0.,   -6./

      DATA G1985/0.,-29873.,-1905.,-2072., 3044., 1687., 1296.,-2208.,
     * 1247.,  829.,   936.,  780.,  361., -424.,  170., -214.,  355.,
     *  253.,  -93.,  -164.,  -46.,   53.,   65.,   51., -185.,    4.,
     *   16., -102.,    74.,  -62.,    3.,   24.,   -6.,    4.,   10.,
     *    0.,   21.,     6.,    0.,  -11.,   -9.,    4.,    4.,    4.,
     *   -4.,    5.,    10.,    1.,  -12.,    9.,   -3.,   -1.,    7.,
     *    1.,   -5.,    -4.,   -4.,    3.,   -5.,   -2.,    5.,    3.,
     *    1.,    2.,     3.,    0./

      DATA H1985/0.,     0., 5500.,    0.,-2197., -306.,    0., -310.,
     *  284., -297.,     0.,  232., -249.,   69., -297.,    0.,   47.,
     *  150., -154.,   -75.,   95.,    0.,  -16.,   88.,   69.,  -48.,
     *   -1.,   21.,     0.,  -83.,  -27.,   -2.,   20.,   17.,  -23.,
     *   -7.,    0.,     8.,  -19.,    5.,  -23.,   11.,   14.,  -15.,
     *  -11.,    0.,   -21.,   15.,    9.,   -6.,   -6.,    9.,    9.,
     *   -7.,    2.,     0.,    1.,    0.,    3.,    6.,   -4.,    0.,
     *   -1.,    4.,     0.,   -6./

      DATA G1990/0.,-29775.,-1848.,-2131., 3059., 1686., 1314.,-2239.,
     * 1248.,  802.,   939.,  780.,  325., -423.,  141., -214.,  353.,
     *  245., -109.,  -165.,  -36.,   61.,   65.,   59., -178.,    3.,
     *   18.,  -96.,    77.,  -64.,    2.,   26.,   -1.,    5.,    9.,
     *    0.,   23.,     5.,   -1.,  -10.,  -12.,    3.,    4.,    2.,
     *   -6.,    4.,     9.,    1.,  -12.,    9.,   -4.,   -2.,    7.,
     *    1.,   -6.,    -3.,   -4.,    2.,   -5.,   -2.,    4.,    3.,
     *    1.,    3.,     3.,    0./

      DATA H1990/0.,     0., 5406.,    0.,-2279., -373.,    0., -284.,
     *  293., -352.,     0.,  247., -240.,   84., -299.,    0.,   46.,
     *  154., -153.,   -69.,   97.,    0.,  -16.,   82.,   69.,  -52.,
     *    1.,   24.,     0.,  -80.,  -26.,    0.,   21.,   17.,  -23.,
     *   -4.,    0.,    10.,  -19.,    6.,  -22.,   12.,   12.,  -16.,
     *  -10.,    0.,   -20.,   15.,   11.,   -7.,   -7.,    9.,    8.,
     *   -7.,    2.,     0.,    2.,    1.,    3.,    6.,   -4.,    0.,
     *   -2.,    3.,    -1.,   -6./

      DATA G1995/0.,-29682.,-1789.,-2197., 3074., 1685., 1329.,-2268.,
     * 1249.,  769.,   941.,  782.,  291., -421.,  116., -210.,  352.,
     *  237., -122.,  -167.,  -26.,   66.,   64.,   65., -172.,    2.,
     *   17.,  -94.,    78.,  -67.,    1.,   29.,    4.,    8.,   10.,
     *   -2.,   24.,     4.,   -1.,   -9.,  -14.,    4.,    5.,    0.,
     *   -7.,    4.,     9.,    1.,  -12.,    9.,   -4.,   -2.,    7.,
     *    0.,   -6.,    -3.,   -4.,    2.,   -5.,   -2.,    4.,    3.,
     *    1.,    3.,     3.,    0./

      DATA H1995/0.,     0., 5318.,    0.,-2356., -425.,    0., -263.,
     *  302., -406.,     0.,  262., -232.,   98., -301.,    0.,   44.,
     *  157., -152.,   -64.,   99.,    0.,  -16.,   77.,   67.,  -57.,
     *    4.,   28.,     0.,  -77.,  -25.,    3.,   22.,   16.,  -23.,
     *   -3.,    0.,    12.,  -20.,    7.,  -21.,   12.,   10.,  -17.,
     *  -10.,    0.,   -19.,   15.,   11.,   -7.,   -7.,    9.,    7.,
     *   -8.,    1.,     0.,    2.,    1.,    3.,    6.,   -4.,    0.,
     *   -2.,    3.,    -1.,   -6./

      DATA DG2000/0.,  17.6,  13.0, -13.2,   3.7,  -0.8,   1.5,  -6.4,
     *  -0.2,   -8.1,   0.8,   0.9,  -6.9,   0.5,  -4.6,   0.8,   0.1,
     *  -1.5,   -2.0,  -0.1,   2.3,   0.5,  -0.4,   0.6,   1.9,  -0.2,
     *  -0.2,    0.0,  -0.2,  -0.8,  -0.6,   0.6,   1.2,   0.1,   0.2,
     *  -0.6,    0.3,  -0.2,   0.1,   0.4,  -1.1,   0.3,   0.2,  -0.9,
     *  -0.3/

      DATA DH2000/0.,   0.,  -18.3,    0., -15.0,  -8.8,    0.,   4.1,
     *  2.2,   -12.1,   0.,    1.8,   1.2,   2.7,  -1.0,    0.,   0.2,
     *  1.2,     0.3,  1.8,    0.9,    0.,   0.3,  -1.6,  -0.2,  -0.9,
     *  1.0,     2.2,   0.,    0.8,    0.2,  0.6,  -0.4,   0.0,  -0.3,
     *  0.0,      0.,  0.4,   -0.2,    0.2,  0.7,   0.0,  -1.2,  -0.7,
     * -0.6/

      DATA MA,IYR,IPR/0,0,0/

      IF(MA.NE.1) GOTO 10
      IF(IY.NE.IYR) GOTO 30
      GOTO 130

10     MA = 1
      KNM = 15

      DO 20 N=1,11
         N2=2*N-1
         N2=N2*(N2-2)
         DO 20 M=1,N
            MN=N*(N-1)/2+M
20    REC(MN)=FLOAT((N-M)*(N+M-2))/FLOAT(N2)

30    IYR=IY
      IF (IYR.LT.1945) IYR=1945
      IF (IYR.GT.2000) IYR=2000
      IF (IY.NE.IYR.AND.IPR.EQ.0) write(*,999)IY,IYR
999   FORMAT(//1X,
     * '*** IGRF WARNS: YEAR IS OUT OF INTERVAL 1945-2000: IY =',I5/,
     *',         CALCULATIONS WILL BE DONE FOR IYR =',I5,' ****'//)

      IF (IYR.NE.IY) IPR=1
      IF (IYR.LT.1950) GOTO 1950      !INTERPOLATE BETWEEN 1945 - 1950
      IF (IYR.LT.1955) GOTO 1955      !INTERPOLATE BETWEEN 1950 - 1955
      IF (IYR.LT.1960) GOTO 1960      !INTERPOLATE BETWEEN 1955 - 1960
      IF (IYR.LT.1965) GOTO 1965      !INTERPOLATE BETWEEN 1960 - 1965
      IF (IYR.LT.1970) GOTO 1970      !INTERPOLATE BETWEEN 1965 - 1970
      IF (IYR.LT.1975) GOTO 1975      !INTERPOLATE BETWEEN 1970 - 1975
      IF (IYR.LT.1980) GOTO 1980      !INTERPOLATE BETWEEN 1975 - 1980
      IF (IYR.LT.1985) GOTO 1985      !INTERPOLATE BETWEEN 1980 - 1985
      IF (IYR.LT.1990) GOTO 1990      !INTERPOLATE BETWEEN 1985 - 1990
      IF (IYR.LT.1995) GOTO 1995      !INTERPOLATE BETWEEN 1990 - 1995

C  EXTRAPOLATE BETWEEN 1995 - 2000

      DT=FLOAT(IYR)-1995.
      DO 1000 N=1,66
         G(N)=G1995(N)
         H(N)=H1995(N)
         IF (N.GT.45) GOTO 1000
         G(N)=G(N)+DG2000(N)*DT
         H(N)=H(N)+DH2000(N)*DT
1000  CONTINUE
      GOTO 300

C  INTERPOLATE BETWEEEN 1945 - 1950

1950  F2=(IYR-1945)/5.
      F1=1.-F2
      DO 1953 N=1,66
         G(N)=G1945(N)*F1+G1950(N)*F2
1953     H(N)=H1945(N)*F1+H1950(N)*F2
      GOTO 300

C  INTERPOLATE BETWEEEN 1950 - 1955

1955  F2=(IYR-1950)/5.
      F1=1.-F2
      DO 1958 N=1,66
         G(N)=G1950(N)*F1+G1955(N)*F2
1958     H(N)=H1950(N)*F1+H1955(N)*F2
      GOTO 300

C  INTERPOLATE BETWEEEN 1955 - 1960

1960  F2=(IYR-1955)/5.
      F1=1.-F2
      DO 1963 N=1,66
         G(N)=G1955(N)*F1+G1960(N)*F2
1963     H(N)=H1955(N)*F1+H1960(N)*F2
      GOTO 300

C  INTERPOLATE BETWEEEN 1960 - 1965

1965  F2=(IYR-1960)/5.
      F1=1.-F2
      DO 1968 N=1,66
         G(N)=G1960(N)*F1+G1965(N)*F2
1968     H(N)=H1960(N)*F1+H1965(N)*F2
      GOTO 300

C  INTERPOLATE BETWEEEN 1965 - 1970

1970  F2=(IYR-1965)/5.
      F1=1.-F2
      DO 1973 N=1,66
         G(N)=G1965(N)*F1+G1970(N)*F2
1973     H(N)=H1965(N)*F1+H1970(N)*F2
      GOTO 300

C  INTERPOLATE BETWEEN 1970 - 1975

1975  F2=(IYR-1970)/5.
      F1=1.-F2
      DO 1978 N=1,66
         G(N)=G1970(N)*F1+G1975(N)*F2
1978     H(N)=H1970(N)*F1+H1975(N)*F2
      GOTO 300

C  INTERPOLATE BETWEEN 1975 - 1980

1980  F2=(IYR-1975)/5.
      F1=1.-F2
      DO 1983 N=1,66
         G(N)=G1975(N)*F1+G1980(N)*F2
1983     H(N)=H1975(N)*F1+H1980(N)*F2
      GOTO 300
C  INTERPOLATE BETWEEN 1980 - 1985

1985  F2=(IYR-1980)/5.
      F1=1.-F2
      DO 1988 N=1,66
         G(N)=G1980(N)*F1+G1985(N)*F2
1988     H(N)=H1980(N)*F1+H1985(N)*F2
      GOTO 300

C  INTERPOLATE BETWEEN 1985 - 1990

1990  F2=(IYR-1985)/5.
      F1=1.-F2
      DO 1993 N=1,66
         G(N)=G1985(N)*F1+G1990(N)*F2
1993     H(N)=H1985(N)*F1+H1990(N)*F2
      GOTO 300

C  INTERPOLATE BETWEEN 1990 - 1995

1995  F2=(IYR-1990)/5.
      F1=1.-F2
      DO 1998 N=1,66
         G(N)=G1990(N)*F1+G1995(N)*F2
1998     H(N)=H1990(N)*F1+H1995(N)*F2
      GOTO 300

C  GET HERE WHEN COEFFICIENTS FOR APPROPRIATE IGRF MODEL HAVE BEEN 
C  ASSIGNED

300    S = 1.

      G0 = G(2)
      G1 = G(3)
      H1 = H(3)

      DO 120 N=2,11
         MN=N*(N-1)/2+1
         S=S*FLOAT(2*N-3)/FLOAT(N-1)
         G(MN)=G(MN)*S
         H(MN)=H(MN)*S
         P=S
         DO 120 M=2,N
            AA=1.
            IF (M.EQ.2) AA=2.
            P=P*SQRT(AA*FLOAT(N-M+1)/FLOAT(N+M-2))
            MNN=MN+M-1
            G(MNN)=G(MNN)*P
120         H(MNN)=H(MNN)*P

130   IF(KNM.EQ.NM) GO TO 140
      KNM=NM
      K=KNM+1
140   PP=1./R
      P=PP
      DO 150 N=1,K
         P=P*PP
         A(N)=P
150      B(N)=P*N
      P=1.
      D=0.
      BBR=0.
      BBT=0.
      BBF=0.
      U=T
      CF=COS(F)
      SF=SIN(F)
      C=COS(U)
      S=SIN(U)
      BK=(S.LT.1.E-5)
      DO 200 M=1,K
         BM=(M.EQ.1)
         IF(BM) GOTO 160
         MM=M-1
         W=X
         X=W*CF+Y*SF
         Y=Y*CF-W*SF
         GOTO 170
160      X=0.
         Y=1.
170      Q=P
         Z=D
         BI=0.
         P2=0.
         D2=0.
         DO 190 N=M,K
            AN=A(N)
            MN=N*(N-1)/2+M
            E=G(MN)
            HH=H(MN)
            W=E*Y+HH*X
            BBR=BBR+B(N)*W*Q
            BBT=BBT-AN*W*Z
            IF(BM) GOTO 180
            QQ=Q
            IF(BK) QQ=Z
            BI=BI+AN*(E*X-HH*Y)*QQ
180         XK=REC(MN)
            DP=C*Z-S*Q-XK*D2
            PM=C*Q-XK*P2
            D2=Z
            P2=Q
            Z=DP
190        Q=PM
         D=S*D+C*P
         P=S*P
         IF(BM) GOTO 200
         BI=BI*MM
         BBF=BBF+BI
200   CONTINUE

      BR=BBR
      BT=BBT
      IF(BK) GOTO 210
      BF=BBF/S
      GOTO 220

210   IF(C.LT.0.) BBF=-BBF
      BF=BBF

220   CONTINUE

      RETURN
      END

C  *********************************************************************
     
C  *********************************************************************

      SUBROUTINE SPHCAR(R,TETA,PHI,X,Y,Z,J)

C   CONVERTS SPHERICAL COORDS INTO CARTESIAN ONES AND VICA VERSA
C    (TETA AND PHI IN RADIANS).

C                  J>0            J<0
C-----INPUT:   J,R,TETA,PHI     J,X,Y,Z
C----OUTPUT:      X,Y,Z        R,TETA,PHI

C  AUTHOR: NIKOLAI A. TSYGANENKO, INSTITUTE OF PHYSICS, ST.-PETERSBURG
C      STATE UNIVERSITY, STARY PETERGOF 198904, ST.-PETERSBURG, RUSSIA
C      (now the NASA Goddard Space Fligth Center, Greenbelt, Maryland)

        IMPLICIT NONE

        REAL R,TETA,PHI,X,Y,Z,SQ

        INTEGER J

      IF(J.GT.0) GOTO 3
      SQ=X**2+Y**2
      R=SQRT(SQ+Z**2)
      IF (SQ.NE.0.) GOTO 2
      PHI=0.
      IF (Z.LT.0.) GOTO 1
      TETA=0.
      RETURN
  1   TETA=3.141592654
      RETURN
  2   SQ=SQRT(SQ)
      PHI=ATAN2(Y,X)
      TETA=ATAN2(SQ,Z)
      IF (PHI.LT.0.) PHI=PHI+6.28318531
      RETURN
  3   SQ=R*SIN(TETA)
      X=SQ*COS(PHI)
      Y=SQ*SIN(PHI)
      Z=R*COS(TETA)

      RETURN
      END

C  *********************************************************************


C  *********************************************************************

      SUBROUTINE BSPCAR(TETA,PHI,BR,BTET,BPHI,BX,BY,BZ)

C   CALCULATES CARTESIAN FIELD COMPONENTS FROM SPHERICAL ONES
C-----INPUT:   TETA,PHI - SPHERICAL ANGLES OF THE POINT IN RADIANS
C              BR,BTET,BPHI -  SPHERICAL COMPONENTS OF THE FIELD
C-----OUTPUT:  BX,BY,BZ - CARTESIAN COMPONENTS OF THE FIELD

C  AUTHOR: NIKOLAI A. TSYGANENKO, INSTITUTE OF PHYSICS, ST.-PETERSBURG
C      STATE UNIVERSITY, STARY PETERGOF 198904, ST.-PETERSBURG, RUSSIA
C      (now the NASA Goddard Space Fligth Center, Greenbelt, Maryland)

        IMPLICIT NONE

        REAL TETA,PHI,BR,BTET,BPHI,BX,BY,BZ,S,C,SF,CF,BE

      S=SIN(TETA)
      C=COS(TETA)
      SF=SIN(PHI)
      CF=COS(PHI)
      BE=BR*S+BTET*C
      BX=BE*CF-BPHI*SF
      BY=BE*SF+BPHI*CF
      BZ=BR*C-BTET*S
      RETURN
      END

C  *********************************************************************

C  *********************************************************************

      SUBROUTINE GEOMAG(XGEO,YGEO,ZGEO,XMAG,YMAG,ZMAG,J,IYR)

C CONVERTS GEOCENTRIC (GEO) TO DIPOLE (MAG) COORDINATES OR VICA VERSA.
C IYR IS YEAR NUMBER (FOUR DIGITS).

C                           J>0                J<0
C-----INPUT:  J,XGEO,YGEO,ZGEO,IYR   J,XMAG,YMAG,ZMAG,IYR
C-----OUTPUT:    XMAG,YMAG,ZMAG        XGEO,YGEO,ZGEO

C  AUTHOR: NIKOLAI A. TSYGANENKO, INSTITUTE OF PHYSICS, ST.-PETERSBURG
C      STATE UNIVERSITY, STARY PETERGOF 198904, ST.-PETERSBURG, RUSSIA
C      (now the NASA Goddard Space Fligth Center, Greenbelt, Maryland)

        IMPLICIT NONE

        REAL XGEO,YGEO,ZGEO,XMAG,YMAG,ZMAG,ST0,CT0,SL0,CL0,CTCL,
     *       STCL,CTSL,STSL,AB(19),BB(8)

        INTEGER J,IYR,K,IY,II

      COMMON/C1/ ST0,CT0,SL0,CL0,CTCL,STCL,CTSL,STSL,AB,K,IY,BB
      DATA II/1/
      IF(IYR.EQ.II) GOTO 1
      II=IYR
      CALL RECALC(II,0,25,0,0)
  1   CONTINUE
      IF(J.LT.0) GOTO 2
      XMAG=XGEO*CTCL+YGEO*CTSL-ZGEO*ST0
      YMAG=YGEO*CL0-XGEO*SL0
      ZMAG=XGEO*STCL+YGEO*STSL+ZGEO*CT0
      RETURN
  2   XGEO=XMAG*CTCL-YMAG*SL0+ZMAG*STCL
      YGEO=XMAG*CTSL+YMAG*CL0+ZMAG*STSL
      ZGEO=ZMAG*CT0-XMAG*ST0

      RETURN
      END

C  *********************************************************************
C  *********************************************************************

      SUBROUTINE RECALC(IYR,IDAY,IHOUR,MIN,ISEC)

C  THIS IS A MODIFIED VERSION OF THE SUBROUTINE RECOMP WRITTEN BY 
C  N. A. TSYGANENKO. SINCE I WANT TO USE IT IN PLACE OF SUBROUTINE 
C  RECALC, I HAVE RENAMED THIS ROUTINE RECALC AND ELIMINATED THE 
C  ORIGINAL RECALC FROM THIS VERSION OF THE <GEOPACK.FOR> PACKAGE. 
C  THIS WAY ALL ORIGINAL CALLS TO RECALC WILL CONTINUE TO WORK WITHOUT
C  HAVING TO CHANGE THEM TO CALLS TO RECOMP.

C  AN ALTERNATIVE VERSION OF THE SUBROUTINE RECALC FROM THE GEOPACK 
C  PACKAGE BASED ON A DIFFERENT APPROACH TO DERIVATION OF ROTATION 
C  MATRIX ELEMENTS

C  THIS SUBROUTINE WORKS BY 20% FASTER THAN RECALC AND IS EASIER TO 
C  UNDERSTAND
C  #####################################################
C  #  WRITTEN BY  N.A. TSYGANENKO ON DECEMBER 1, 1991  #
C  #####################################################
C  Modified by Mauricio Peredo, Hughes STX at NASA/GSFC Code 695, 
C  September 1992

c  Modified to accept dates up to year 2000 and updated IGRF coeficients
c  from 1945 (updated by V. Papitashvili, February 1995)

C   OTHER SUBROUTINES CALLED BY THIS ONE: SUN

C     IYR = YEAR NUMBER (FOUR DIGITS)
C     IDAY = DAY OF YEAR (DAY 1 = JAN 1)
C     IHOUR = HOUR OF DAY (00 TO 23)
C     MIN = MINUTE OF HOUR (00 TO 59)
C     ISEC = SECONDS OF DAY(00 TO 59)

        IMPLICIT NONE

        REAL ST0,CT0,SL0,CL0,CTCL,STCL,CTSL,STSL,SFI,CFI,SPS,CPS,
     1       SHI,CHI,HI,PSI,XMUT,A11,A21,A31,A12,A22,A32,A13,A23,
     2       A33,DS3,F2,F1,G10,G11,H11,DT,SQ,SQQ,SQR,S1,S2,
     3       S3,CGST,SGST,DIP1,DIP2,DIP3,Y1,Y2,Y3,Y,Z1,Z2,Z3,DJ,
     4       T,OBLIQ,DZ1,DZ2,DZ3,DY1,DY2,DY3,EXMAGX,EXMAGY,EXMAGZ,
     5       EYMAGX,EYMAGY,GST,SLONG,SRASN,SDEC,BA(8)

        INTEGER IYR,IDAY,IHOUR,MIN,ISEC,K,IY,IDE,IYE,IPR

       COMMON/C1/ ST0,CT0,SL0,CL0,CTCL,STCL,CTSL,STSL,SFI,CFI,SPS,CPS,
     * SHI,CHI,HI,PSI,XMUT,A11,A21,A31,A12,A22,A32,A13,A23,A33,DS3,
     * K,IY,BA

      DATA IYE,IDE,IPR/3*0/
      IF (IYR.EQ.IYE.AND.IDAY.EQ.IDE) GOTO 5

C  IYE AND IDE ARE THE CURRENT VALUES OF YEAR AND DAY NUMBER
      IY=IYR
      IDE=IDAY
      IF(IY.LT.1945) IY=1945
      IF(IY.GT.2000) IY=2000

C  WE ARE RESTRICTED BY THE INTERVAL 1945-2000, FOR WHICH THE IGRF 
C  COEFFICIENTS ARE KNOWN; IF IYR IS OUTSIDE THIS INTERVAL, THE 
C  SUBROUTINE GIVES A WARNING (BUT DOES NOT REPEAT IT AT THE NEXT CALLS)

      IF(IY.NE.IYR.AND.IPR.EQ.0) PRINT 10,IYR,IY
      IF(IY.NE.IYR) IPR=1
      IYE=IY

C  LINEAR INTERPOLATION OF THE GEODIPOLE MOMENT COMPONENTS BETWEEN THE
C  VALUES FOR THE NEAREST EPOCHS:

        IF (IY.LT.1950) THEN                            !1945-1950
           F2=(FLOAT(IY)+FLOAT(IDAY)/365.-1945.)/5.
           F1=1.D0-F2
           G10=30594.*F1+30554.*F2
           G11=-2285.*F1-2250.*F2
           H11=5810.*F1+5815.*F2
        ELSEIF (IY.LT.1955) THEN                        !1950-1955
           F2=(FLOAT(IY)+FLOAT(IDAY)/365.-1950.)/5.
           F1=1.D0-F2
           G10=30554.*F1+30500.*F2
           G11=-2250.*F1-2215.*F2
           H11=5815.*F1+5820.*F2
        ELSEIF (IY.LT.1960) THEN                        !1955-1960
           F2=(FLOAT(IY)+FLOAT(IDAY)/365.-1955.)/5.
           F1=1.D0-F2
           G10=30500.*F1+30421.*F2
           G11=-2215.*F1-2169.*F2
           H11=5820.*F1+5791.*F2
        ELSEIF (IY.LT.1965) THEN                        !1960-1965
           F2=(FLOAT(IY)+FLOAT(IDAY)/365.-1960.)/5.
           F1=1.D0-F2
           G10=30421.*F1+30334.*F2
           G11=-2169.*F1-2119.*F2
           H11=5791.*F1+5776.*F2
        ELSEIF (IY.LT.1970) THEN                        !1965-1970
           F2=(FLOAT(IY)+FLOAT(IDAY)/365.-1965.)/5.
           F1=1.D0-F2
           G10=30334.*F1+30220.*F2
           G11=-2119.*F1-2068.*F2
           H11=5776.*F1+5737.*F2
        ELSEIF (IY.LT.1975) THEN                        !1970-1975
           F2=(FLOAT(IY)+FLOAT(IDAY)/365.-1970.)/5.
           F1=1.D0-F2
           G10=30220.*F1+30100.*F2
           G11=-2068.*F1-2013.*F2
           H11=5737.*F1+5675.*F2
        ELSEIF (IY.LT.1980) THEN                        !1975-1980
           F2=(DFLOAT(IY)+DFLOAT(IDAY)/365.-1975.)/5.
           F1=1.D0-F2
           G10=30100.*F1+29992.*F2
           G11=-2013.*F1-1956.*F2
           H11=5675.*F1+5604.*F2
        ELSEIF (IY.LT.1985) THEN                        !1980-1985
           F2=(FLOAT(IY)+FLOAT(IDAY)/365.-1980.)/5.
           F1=1.D0-F2
           G10=29992.*F1+29873.*F2
           G11=-1956.*F1-1905.*F2
           H11=5604.*F1+5500.*F2
        ELSEIF (IY.LT.1990) THEN                        !1985-1990
           F2=(FLOAT(IY)+FLOAT(IDAY)/365.-1985.)/5.
           F1=1.D0-F2
           G10=29873.*F1+29775.*F2
           G11=-1905.*F1-1848.*F2
           H11=5500.*F1+5406.*F2
        ELSEIF (IY.LT.1995) THEN                        !1990-1995
           F2=(FLOAT(IY)+FLOAT(IDAY)/365.-1990.)/5.
           F1=1.D0-F2
           G10=29775.*F1+29682.*F2
           G11=-1848.*F1-1789.*F2
           H11=5406.*F1+5318.*F2
        ELSE                                            !1995-2000
           DT=FLOAT(IY)+FLOAT(IDAY)/365.-1995.
           G10=29682.-17.6*DT
           G11=-1789.+13.0*DT
           H11=5318.-18.3*DT
        ENDIF

C  NOW CALCULATE THE COMPONENTS OF THE UNIT VECTOR EzMAG IN GEO COORD
C  SYSTEM: 
C  SIN(TETA0)*COS(LAMBDA0), SIN(TETA0)*SIN(LAMBDA0), AND COS(TETA0)
C         ST0 * CL0                ST0 * SL0                CT0

      SQ=G11**2+H11**2
      SQQ=SQRT(SQ)
      SQR=SQRT(G10**2+SQ)
      SL0=-H11/SQQ
      CL0=-G11/SQQ
      ST0=SQQ/SQR
      CT0=G10/SQR
      STCL=ST0*CL0
      STSL=ST0*SL0
      CTSL=CT0*SL0
      CTCL=CT0*CL0

C  THE CALCULATIONS ARE TERMINATED IF ONLY GEO-MAG TRANSFORMATION
C  IS TO BE DONE  (IHOUR>24 IS THE AGREED CONDITION FOR THIS CASE):

   5   IF (IHOUR.GT.24) RETURN

      CALL SUN(IY,IDAY,IHOUR,MIN,ISEC,GST,SLONG,SRASN,SDEC)

C  S1,S2, AND S3 ARE THE COMPONENTS OF THE UNIT VECTOR EXGSM=EXGSE 
C  IN THE SYSTEM GEI POINTING FROM THE EARTH'S CENTER TO THE SUN:

      S1=COS(SRASN)*COS(SDEC)
      S2=SIN(SRASN)*COS(SDEC)
      S3=SIN(SDEC)
      CGST=COS(GST)
      SGST=SIN(GST)

C  DIP1, DIP2, AND DIP3 ARE THE COMPONENTS OF THE UNIT VECTOR 
C  EZSM=EZMAG IN THE SYSTEM GEI:

      DIP1=STCL*CGST-STSL*SGST
      DIP2=STCL*SGST+STSL*CGST
      DIP3=CT0

C  NOW CALCULATE THE COMPONENTS OF THE UNIT VECTOR EYGSM IN THE SYSTEM
C  GEI BY TAKING THE VECTOR PRODUCT D x S AND NORMALIZING IT TO UNIT 
C  LENGTH:


      Y1=DIP2*S3-DIP3*S2
      Y2=DIP3*S1-DIP1*S3
      Y3=DIP1*S2-DIP2*S1
      Y=SQRT(Y1*Y1+Y2*Y2+Y3*Y3)
      Y1=Y1/Y
      Y2=Y2/Y
      Y3=Y3/Y

C  THEN IN THE GEI SYSTEM THE UNIT VECTOR Z=EZGSM=EXGSM x EYGSM=S x Y
C  HAS THE COMPONENTS:

      Z1=S2*Y3-S3*Y2
      Z2=S3*Y1-S1*Y3
      Z3=S1*Y2-S2*Y1

C  THE VECTOR EZGSE (HERE DZ) IN GEI HAS THE COMPONENTS (0,-SIN(DELTA),
C  COS(DELTA)) = (0.,-0.397823,0.917462); HERE DELTA = 23.44214 DEG FOR
C  THE EPOCH 1978 (SEE THE BOOK BY GUREVICH OR OTHER ASTRONOMICAL 
C  HANDBOOKS). HERE THE MOST ACCURATE TIME-DEPENDENT FORMULA IS USED:

      DJ=FLOAT(365*(IY-1900)+(IY-1901)/4 +IDAY)-0.5+FLOAT(ISEC)/86400.
      T=DJ/36525.
      OBLIQ=(23.45229-0.0130125*T)/57.2957795
      DZ1=0.
      DZ2=-SIN(OBLIQ)
      DZ3=COS(OBLIQ)

C  THEN THE UNIT VECTOR EYGSE IN GEI SYSTEM IS THE VECTOR PRODUCT DZ x S

      DY1=DZ2*S3-DZ3*S2
      DY2=DZ3*S1-DZ1*S3
      DY3=DZ1*S2-DZ2*S1

C  THE ELEMENTS OF THE MATRIX GSE TO GSM ARE THE SCALAR PRODUCTS:
C  CHI=EM22=(EYGSM,EYGSE), SHI=EM23=(EYGSM,EZGSE), 
C  EM32=(EZGSM,EYGSE)=-EM23, AND EM33=(EZGSM,EZGSE)=EM22

      CHI=Y1*DY1+Y2*DY2+Y3*DY3
      SHI=Y1*DZ1+Y2*DZ2+Y3*DZ3
      HI=ASIN(SHI)

C  TILT ANGLE: PSI=ARCSIN(DIP,EXGSM)

      SPS=DIP1*S1+DIP2*S2+DIP3*S3
      CPS=SQRT(1.-SPS**2)
      PSI=ASIN(SPS)

C  THE ELEMENTS OF THE MATRIX MAG TO SM ARE THE SCALAR PRODUCTS:
C  CFI=GM22=(EYSM,EYMAG), SFI=GM23=(EYSM,EXMAG); THEY CAN BE DERIVED 
C  AS FOLLOWS:

C  IN GEO THE VECTORS EXMAG AND EYMAG HAVE THE COMPONENTS 
C  (CT0*CL0,CT0*SL0,-ST0) AND (-SL0,CL0,0), RESPECTIVELY. HENCE, IN 
C  GEI SYSTEM THE COMPONENTS ARE:
C  EXMAG:    CT0*CL0*COS(GST)-CT0*SL0*SIN(GST)
C            CT0*CL0*SIN(GST)+CT0*SL0*COS(GST)
C            -ST0
C  EYMAG:    -SL0*COS(GST)-CL0*SIN(GST)
C            -SL0*SIN(GST)+CL0*COS(GST)
C             0
C  THE COMPONENTS OF EYSM IN GEI WERE FOUND ABOVE AS Y1, Y2, AND Y3;
C  NOW WE ONLY HAVE TO COMBINE THE QUANTITIES INTO SCALAR PRODUCTS:
      EXMAGX=CT0*(CL0*CGST-SL0*SGST)
      EXMAGY=CT0*(CL0*SGST+SL0*CGST)
      EXMAGZ=-ST0
      EYMAGX=-(SL0*CGST+CL0*SGST)
      EYMAGY=-(SL0*SGST-CL0*CGST)
      CFI=Y1*EYMAGX+Y2*EYMAGY
      SFI=Y1*EXMAGX+Y2*EXMAGY+Y3*EXMAGZ

      XMUT=(ATAN2(SFI,CFI)+3.1415926536)*3.8197186342

C  THE ELEMENTS OF THE MATRIX GEO TO GSM ARE THE SCALAR PRODUCTS:

C  A11=(EXGEO,EXGSM), A12=(EYGEO,EXGSM), A13=(EZGEO,EXGSM),
C  A21=(EXGEO,EYGSM), A22=(EYGEO,EYGSM), A23=(EZGEO,EYGSM),
C  A31=(EXGEO,EZGSM), A32=(EYGEO,EZGSM), A33=(EZGEO,EZGSM),

C  ALL THE UNIT VECTORS IN BRACKETS ARE ALREADY DEFINED IN GEI:

C  EXGEO=(CGST,SGST,0), EYGEO=(-SGST,CGST,0), EZGEO=(0,0,1)
C  EXGSM=(S1,S2,S3),  EYGSM=(Y1,Y2,Y3),   EZGSM=(Z1,Z2,Z3)
C  AND  THEREFORE:

      A11=S1*CGST+S2*SGST
      A12=-S1*SGST+S2*CGST
      A13=S3
      A21=Y1*CGST+Y2*SGST
      A22=-Y1*SGST+Y2*CGST
      A23=Y3
      A31=Z1*CGST+Z2*SGST
      A32=-Z1*SGST+Z2*CGST
      A33=Z3

 10   FORMAT(//1X,
     * '****RECALC WARNS: YEAR IS OUT OF INTERVAL 1945-2000: IYR=',I4,
     * /,6X,'CALCULATIONS WILL BE DONE FOR IYR=',I4,/)

      RETURN
      END

C  *********************************************************************
C  *********************************************************************

      SUBROUTINE SUN(IYR,IDAY,IHOUR,MIN,ISEC,GST,SLONG,SRASN,SDEC)

C  CALCULATES FOUR QUANTITIES NECESSARY FOR COORDINATE TRANSFORMATIONS
C  WHICH DEPEND ON SUN POSITION (AND, HENCE, ON UNIVERSAL TIME AND 
C  SEASON)

C---INPUT PARAMETERS:
C  IYR,IDAY,IHOUR,MIN,ISEC - YEAR, DAY, AND UNIVERSAL TIME IN HOURS, 
C    MINUTES, AND SECONDS  (IDAY=1 CORRESPONDS TO JANUARY 1).

C---OUTPUT PARAMETERS:
C  GST - GREENWICH MEAN SIDEREAL TIME, SLONG - LONGITUDE ALONG ECLIPTIC
C  SRASN - RIGHT ASCENSION,  SDEC - DECLINATION  OF THE SUN (RADIANS)
C  THIS SUBROUTINE HAS BEEN COMPILED FROM: 
C  RUSSELL C.T., COSM.ELECTRODYN., 1971, V.2,PP.184-196.

C  AUTHOR: Gilbert D. Mead

      IMPLICIT NONE

      REAL GST,SLONG,SRASN,SDEC,RAD,T,VL,G,OBLIQ,SOB,SLP,SIND,COSD,SC
      INTEGER IYR,IDAY,IHOUR,MIN,ISEC
      DOUBLE PRECISION DJ,FDAY

      DATA RAD/57.295779513/

      IF(IYR.LT.1901.OR.IYR.GT.2099) RETURN
      FDAY=DFLOAT(IHOUR*3600+MIN*60+ISEC)/86400.D0
      DJ=365*(IYR-1900)+(IYR-1901)/4+IDAY-0.5D0+FDAY
      T=DJ/36525.
      VL=DMOD(279.696678+0.9856473354*DJ,360.D0)
      GST=DMOD(279.690983+.9856473354*DJ+360.*FDAY+180.,360.D0)/RAD
      G=DMOD(358.475845+0.985600267*DJ,360.D0)/RAD
      SLONG=(VL+(1.91946-0.004789*T)*SIN(G)+0.020094*SIN(2.*G))/RAD
      IF(SLONG.GT.6.2831853) SLONG=SLONG-6.2831853
      IF (SLONG.LT.0.) SLONG=SLONG+6.2831853
      OBLIQ=(23.45229-0.0130125*T)/RAD
      SOB=SIN(OBLIQ)
      SLP=SLONG-9.924E-5

C   THE LAST CONSTANT IS A CORRECTION FOR THE ANGULAR ABERRATION  
C   DUE TO THE ORBITAL MOTION OF THE EARTH

      SIND=SOB*SIN(SLP)
      COSD=SQRT(1.-SIND**2)
      SC=SIND/COSD
      SDEC=ATAN(SC)
      SRASN=3.141592654-ATAN2(COS(OBLIQ)/SOB*SC,-COS(SLP)/COSD)
      RETURN
      END
     
C  *********************************************************************
