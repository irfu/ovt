/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/Sgp4Sdp4.java,v $
  Date:      $Date: 2003/09/16 16:36:53 $
  Version:   $Revision: 1.2 $

=========================================================================*/

package ovt.util;

import ovt.datatype.*;

import java.io.*;
import java.util.*;
import java.lang.Math.*;

public class Sgp4Sdp4 {

  public final static double
  twopi = 2.0*Math.PI,
  ae    = 1.0,
  tothrd= 2.0/3.0,
  xkmper= 6378.135,            //Earth equatorial radius - kilometers (WGS '72)
  f     = 1.0/298.26,          //Earth flattening (WGS '72)
  ge    = 398600.8,            //Earth gravitational constant (WGS '72)
  J2    = 1.0826158E-3,        //J2 harmonic (WGS '72)
  J3    = -2.53881E-6,         //J3 harmonic (WGS '72)
  J4    = -1.65597E-6,         //J4 harmonic (WGS '72)
  ck2   = J2/2.0,
  ck4   = -3.0*J4/8.0,
  xj3   = J3,
  qo    = ae + 120.0/xkmper,
  s     = ae + 78.0/xkmper,
  e6a   = 1.0E-6;
  /** Minutes per day */
  public final static double xmnpda  =  1440.0;
  /** Seconds per day */
  public final static double  secday  = 86400.0;
  public final static double omega_E = 1.00273790934,     //Earth rotations per sidereal day (non-constant)
  omega_ER= omega_E*twopi,     //Earth rotation, radians per sidereal day
  xke     = Math.sqrt(3600.0*ge/Math.pow(xkmper,3.0)), //Sqrt(ge) ER^3/min^2
  qoms2t  = Math.pow(qo-s,4.0),//(qo-s)^4 ER^4
  JD1950  =Sgp4Sdp4.julianDateOfYear(1950);

  final static int
  dpinit= 1,                   //Deep-space initialization code
  dpsec = 2,                   //Deep-space secular code
  dpper = 3;                   //Deep-space periodic code

  public static int iflag,ideep;

  public static double xmo,xnodeo,omegao,eo,xincl,xno,xndt2o,xndd6o,bstar,
  mjulian_epoch/*,xke*/;

  public static double epoch=0,UTC_offset=0,ds50;


  // dpinit
  public static double eqsq,siniq,cosiq,rteqsq,ao,cosq2,sinomo,cosomo,
  bsq,xlldot,omgdt,xnodot,xnodp;

  // dpsec/dpper
  public static double xll,omgasm,xnodes,_em,xinc,xn,t/*,qoms2t*/;

  public static double delt=0,ft=0;

  public static void SGP4(double tsince,int iflag, double[] pos, double[] vel){
    int isimp=0;
    double a1=0,a3ovk2=0,ao=0,aodp=0,aycof=0,betao=0,betao2=0,c1=0,c1sq=0,
    c2=0,c3=0,c4=0,c5=0,coef=0,coef1=0,cosio=0,d2=0,d3=0,d4=0,del1=0,delmo=0,
    delo=0,eeta=0,eosq=0,eta=0,etasq=0,omgcof=0,omgdot=0,perige=0,pinvsq=0,
    psisq=0,qoms24=0,s4=0,sinio=0,sinmo=0,t2cof=0,t3cof=0,t4cof=0,t5cof=0,
    temp=0,temp1=0,temp2=0,temp3=0,theta2=0,theta4=0,tsi=0,x1m5th=0,x1mth2=0,
    x3thm1=0,x7thm1=0,xhdot1=0,xlcof=0,xmcof=0,xmdot=0,xnodcf=0,xnodot=0,
    xnodp=0;

    int i;
    double cosuk=0,sinuk=0,rfdotk=0,vx=0,vy=0,vz=0,ux=0,uy=0,uz=0,xmy=0,xmx=0,
    cosnok=0,sinnok=0,cosik=0,sinik=0,rdotk=0,xinck=0,xnodek=0,uk=0,rk=0,
    cos2u=0,sin2u=0,u=0,sinu=0,cosu=0,betal=0,rfdot=0,rdot=0,r=0,pl=0,elsq=0,
    esine=0,ecose=0,epw=0,temp6=0,temp5=0,temp4=0,cosepw=0,sinepw=0,capu=0,
    ayn=0,xlt=0,aynl=0,xll=0,axn=0,xn=0,beta=0,xl=0,e=0,a=0,tfour=0,tcube=0,
    delm=0,delomg=0,templ=0,tempe=0,tempa=0,xnode=0,tsq=0,xmp=0,omega=0,
    xnoddf=0,omgadf=0,xmdf=0,x=0,y=0,z=0,xdot=0,ydot=0,zdot=0;
    
    //{ Recover original mean motion (xnodp) and semimajor axis (aodp) }
    //{ from input elements. }
    if (iflag!=0){
      //    goto m100;
      a1 = Math.pow(xke/xno,tothrd);
      cosio = Math.cos(xincl);
      theta2 = cosio*cosio;
      x3thm1 = 3.0*theta2 - 1.0;
      eosq = eo*eo;
      betao2 = 1.0 - eosq;
      betao = Math.sqrt(betao2);
      del1 = 1.5*ck2*x3thm1/(a1*a1*betao*betao2);
      ao = a1*(1.0 - del1*(0.5*tothrd + del1*(1.0 + 134.0/81.0*del1)));
      delo = 1.5*ck2*x3thm1/(ao*ao*betao*betao2);
      xnodp = xno/(1.0 + delo);
      aodp = ao/(1.0 - delo);

      //{ Initialization }
      /*{ For perigee less than 220 kilometers, the isimp flag is set and
      the equations are truncated to linear variation in sqrt a and
      quadratic variation in mean anomaly.  Also, the c3 term, the
      delta omega term, and the delta m term are dropped. */
      isimp = 0;
      if ((aodp*(1.0 - eo)/ae) < (220.0/xkmper + ae) )
      isimp = 1;
      //{ For perigee below 156 km, the values of s and qoms2t are altered. }
      s4 = s;
      qoms24 = qoms2t;
      perige = (aodp*(1.0 - eo) - ae)*xkmper;
      if (perige < 156){
        //    goto m10;
        s4 = perige - 78;
        if (perige <= 98)
        //     goto m9;
        s4 = 20.0;
        //   m9:
        qoms24 = Math.pow((120.0 - s4)*ae/xkmper,4.0);
        s4 = s4/xkmper + ae;
      }
      // m10:
      pinvsq = 1.0/(aodp*aodp*betao2*betao2);
      tsi = 1.0/(aodp - s4);
      eta = aodp*eo*tsi;
      etasq = eta*eta;
      eeta = eo*eta;
      psisq = Math.abs(1.0 - etasq);
      coef = qoms24*Math.pow(tsi,4.0);
      coef1 = coef/Math.pow(psisq,3.5);
      c2 = coef1*xnodp*(aodp*(1.0 + 1.5*etasq + eeta*(4.0 + etasq))
      + 0.75*ck2*tsi/psisq*x3thm1*(8.0 + 3.0*etasq*(8.0 + etasq)));
      c1 = bstar*c2;
      sinio = Math.sin(xincl);
      a3ovk2 = -xj3/ck2*Math.pow(ae,3);
      c3 = coef*tsi*a3ovk2*xnodp*ae*sinio/eo;
      x1mth2 = 1.0 - theta2;
      c4 = 2*xnodp*coef1*aodp*betao2*(eta*(2.0 + 0.5*etasq)
      + eo*(0.5 + 2*etasq) - 2.0*ck2*tsi/(aodp*psisq)
      *(-3*x3thm1*(1 - 2*eeta + etasq*(1.5 - 0.5*eeta))
      + 0.75*x1mth2*(2*etasq - eeta*(1 + etasq))*Math.cos(2*omegao)));
      c5 = 2*coef1*aodp*betao2*(1.0 + 2.75*(etasq + eeta) + eeta*etasq);
      theta4 = theta2*theta2;
      temp1 = 3.0*ck2*pinvsq*xnodp;
      temp2 = temp1*ck2*pinvsq;
      temp3 = 1.25*ck4*pinvsq*pinvsq*xnodp;
      xmdot = xnodp + 0.5*temp1*betao*x3thm1
      + 0.0625*temp2*betao*(13.0 - 78.0*theta2 + 137.0*theta4);
      x1m5th = 1.0 - 5.0*theta2;
      omgdot = -0.5*temp1*x1m5th + 0.0625*temp2*(7 - 114.0*theta2 +395.0*theta4)
      + temp3*(3.0 - 36.0*theta2 + 49.0*theta4);
      xhdot1 = -temp1*cosio;
      xnodot = xhdot1 + (0.5*temp2*(4.0 - 19.0*theta2)
      + 2*temp3*(3.0 - 7.0*theta2))*cosio;
      omgcof = bstar*c3*Math.cos(omegao);
      xmcof = -tothrd*coef*bstar*ae/eeta;
      xnodcf = 3.5*betao2*xhdot1*c1;
      t2cof = 1.5*c1;
      xlcof = 0.125*a3ovk2*sinio*(3 + 5.0*cosio)/(1.0 + cosio);
      aycof = 0.25*a3ovk2*sinio;
      delmo = Math.pow(1.0 + eta*Math.cos(xmo),3.0);
      sinmo = Math.sin(xmo);
      x7thm1 = 7*theta2 - 1;
      if (isimp!=1){
        //   goto m90;
        c1sq = c1*c1;
        d2 = 4.0*aodp*tsi*c1sq;
        temp = d2*tsi*c1/3.0;
        d3 = (17.0*aodp + s4)*temp;
        d4 = 0.5*temp*aodp*tsi*(221.0*aodp + 31.0*s4)*c1;
        t3cof = d2 + 2*c1sq;
        t4cof = 0.25*(3*d3 + c1*(12.0*d2 + 10.0*c1sq));
        t5cof = 0.2*(3.0*d4 + 12.0*c1*d3 + 6.0*d2*d2 + 15.0*c1sq*(2.0*d2 + c1sq));
      }
      //m90:
      iflag = 0;
      //{ Update for secular gravity and atmospheric drag. }
    }
    //m100:
    xmdf = xmo + xmdot*tsince;
    omgadf = omegao + omgdot*tsince;
    xnoddf = xnodeo + xnodot*tsince;
    omega = omgadf;
    xmp = xmdf;
    tsq = tsince*tsince;
    xnode = xnoddf + xnodcf*tsq;
    tempa = 1.0 - c1*tsince;
    tempe = bstar*c4*tsince;
    templ = t2cof*tsq;
    if (isimp!=1){
      //    goto m110;
      delomg = omgcof*tsince;
      delm = xmcof*(Math.pow(1.0 + eta*Math.cos(xmdf),3) - delmo);
      temp = delomg + delm;
      xmp = xmdf + temp;
      omega = omgadf - temp;
      tcube = tsq*tsince;
      tfour = tsince*tcube;
      tempa = tempa - d2*tsq - d3*tcube - d4*tfour;
      tempe = tempe + bstar*c5*(Math.sin(xmp) - sinmo);
      templ = templ + t3cof*tcube + tfour*(t4cof + tsince*t5cof);
    }
    //m110:
    a = aodp*tempa*tempa;   //a = aodp*Sqr(tempa);
    e = eo - tempe;
    xl = xmp + omega + xnode + xnodp*templ;
    beta = Math.sqrt(1.0 - e*e);
    xn = xke/Math.pow(a,1.5);
    //{ Long period periodics }
    axn = e*Math.cos(omega);
    temp = 1.0/(a*beta*beta);
    xll = temp*xlcof*axn;
    aynl = temp*aycof;
    xlt = xl + xll;
    ayn = e*Math.sin(omega) + aynl;
    //{ Solve Kepler's Equation }
    capu = fmod2p(xlt - xnode);
    temp2 = capu;
    i=1;
    do{
      sinepw = Math.sin(temp2);
      cosepw = Math.cos(temp2);
      temp3 = axn*sinepw;
      temp4 = ayn*cosepw;
      temp5 = axn*cosepw;
      temp6 = ayn*sinepw;
      epw = (capu - temp4 + temp3 - temp2)/(1.0 - temp5 - temp6) + temp2;
      if (Math.abs(epw - temp2) <= e6a)
      break;
      //      goto m140;
      m130:
      temp2 = epw;
      ++i;
    }while(i<=10);//{for i}
    //{ Short period preliminary quantities }
    m140:
    ecose = temp5 + temp6;
    esine = temp3 - temp4;
    elsq = axn*axn + ayn*ayn;
    temp = 1.0 - elsq;
    pl = a*temp;
    r = a*(1.0 - ecose);
    temp1 = 1.0/r;
    rdot = xke*Math.sqrt(a)*esine*temp1;
    rfdot = xke*Math.sqrt(pl)*temp1;
    temp2 = a*temp1;
    betal = Math.sqrt(temp);
    temp3 = 1.0/(1.0 + betal);
    cosu = temp2*(cosepw - axn + ayn*esine*temp3);
    sinu = temp2*(sinepw - ayn - axn*esine*temp3);
    u = Math.atan2(sinu,cosu);
    sin2u = 2.0*sinu*cosu;
    cos2u = 2.0*cosu*cosu - 1.0;
    temp = 1.0/pl;
    temp1 = ck2*temp;
    temp2 = temp1*temp;
    //{ Update for short periodics }
    rk = r*(1.0 - 1.5*temp2*betal*x3thm1) + 0.5*temp1*x1mth2*cos2u;
    uk = u - 0.25*temp2*x7thm1*sin2u;
    xnodek = xnode + 1.5*temp2*cosio*sin2u;
    xinck = xincl + 1.5*temp2*cosio*sinio*cos2u;
    rdotk = rdot - xn*temp1*x1mth2*sin2u;
    rfdotk = rfdot + xn*temp1*(x1mth2*cos2u + 1.5*x3thm1);
    //{ Orientation vectors }
    sinuk = Math.sin(uk);
    cosuk = Math.cos(uk);
    sinik = Math.sin(xinck);
    cosik = Math.cos(xinck);
    sinnok = Math.sin(xnodek);
    cosnok = Math.cos(xnodek);
    xmx = -sinnok*cosik;
    xmy = cosnok*cosik;
    ux = xmx*sinuk + cosnok*cosuk;
    uy = xmy*sinuk + sinnok*cosuk;
    uz = sinik*sinuk;
    vx = xmx*cosuk - cosnok*sinuk;
    vy = xmy*cosuk - sinnok*sinuk;
    vz = sinik*cosuk;
    //{ Position and velocity }
    x = rk*ux;  pos[0] = x;
    y = rk*uy;  pos[1] = y;
    z = rk*uz;  pos[2] = z;
    xdot = rdotk*ux + rfdotk*vx;  vel[0] = xdot;
    ydot = rdotk*uy + rfdotk*vy;  vel[1] = ydot;
    zdot = rdotk*uz + rfdotk*vz;  vel[2] = zdot;
    i=0;
  }


  public static void Deep(int ideep_){
    double zns=1.19459E-5,c1ss=2.9864797E-6,zes=0.01675,znl=1.5835218E-4,
    c1l=4.7968065E-7,zel=0.05490,zcosis=0.91744867,zsinis=0.39785416,
    zsings= -0.98088458,zcosgs=0.1945905,zcoshs=1,zsinhs=0,q22=1.7891679E-6,
    q31=2.1460748E-6,q33=2.2123015E-7,g22=5.7686396,g32=0.95240898,
    g44=1.8014998,g52=1.0508330,g54=4.4108898,root22=1.7891679E-6,
    root32=3.7393792E-7,root44=7.3636953E-9,root52=1.1428639E-7,
    root54=2.1765803E-9,thdt=4.3752691E-3;

    int iresfl=0,isynfl=0,iret=0,iretn=0,ls=0;

    double a1=0,a2=0,a3=0,a4=0,a5=0,a6=0,a7=0,a8=0,a9=0,a10=0,
    ainv2=0,alfdp=0,aqnv=0,atime=0,betdp=0,bfact=0,c=0,cc=0,cosis=0,cosok=0,
    cosq=0,ctem=0,d2201=0,d2211=0,d3210=0,d3222=0,d4410=0,d4422=0,d5220=0,
    d5232=0,d5421=0,d5433=0,dalf=0,day=0,dbet=0,del1=0,del2=0,del3=0,
    dls=0,e3=0,ee2=0,eoc=0,eq=0,f2=0,f220=0,f221=0,f3=0,f311=0,f321=0,f322=0,
    f330=0,f441=0,f442=0,f522=0,f523=0,f542=0,f543=0,fasx2=0,fasx4=0,fasx6=0,
    g200=0,g201=0,g211=0,g300=0,g310=0,g322=0,g410=0,g422=0,g520=0,g521=0,
    g532=0,g533=0,gam=0,omegaq=0,pe=0,pgh=0,ph=0,pinc=0,pl=0,preep=0,s1=0,s2=0,
    s3=0,s4=0,s5=0,s6=0,s7=0,savtsn=0,se=0,se2=0,se3=0,sel=0,ses=0,sgh=0,
    sgh2=0,sgh3=0,sgh4=0,sghl=0,sghs=0,sh=0,sh2=0,sh3=0,sh1=0,shs=0,si=0,si2=0,
    si3=0,sil=0,sini2=0,sinis=0,sinok=0,sinq=0,sinzf=0,sis=0,sl=0,sl2=0,sl3=0,
    sl4=0,sll=0,sls=0,sse=0,ssg=0,ssh=0,ssi=0,ssl=0,stem=0,step2=0,stepn=0,
    stepp=0,temp=0,temp1=0,thgr=0,x1=0,x2=0,x2li=0,x2omi=0,x3=0,x4=0,x5=0,x6=0,
    x7=0,x8=0,xfact=0,xgh2=0,xgh3=0,xgh4=0,xh2=0,xh3=0,xi2=0,xi3=0,xl=0,xl2=0,
    xl3=0,xl4=0,xlamo=0,xldot=0,xli=0,xls=0,xmao=0,xnddt=0,xndot=0,xni=0,xno2=0,
    xnodce=0,xnoi=0,xnq=0,xomi=0,xpidot=0,xqncl=0,z1=0,z11=0,z12=0,z13=0,z2=0,
    z21=0,z22=0,z23=0,z3=0,z31=0,z32=0,z33=0,zcosg=0,zcosgl=0,zcosh=0,zcoshl=0,
    zcosi=0,zcosil=0,ze=0,zf=0,zm=0,zmo=0,zmol=0,zmos=0,zn=0,zsing=0,zsingl=0,
    zsinh=0,zsinhl=0,zsini=0,zsinil=0,zx=0,zy=0;

    switch(ideep_){
      //dpinit : begin { Entrance for deep space initialization }
      case 1:
      thgr = Thetag(epoch);
      eq = eo;
      xnq = xnodp;
      aqnv = 1.0/ao;
      xqncl = xincl;
      xmao = xmo;
      xpidot = omgdt + xnodot;
      sinq = Math.sin(xnodeo);
      cosq = Math.cos(xnodeo);
      omegaq = omegao;
      //{ Initialize lunar solar terms }
      //5:
      day = ds50 + 18261.5;  //{Days since 1900 Jan 0.5}
      if (day!=preep){
        //Goto 10;
        preep = day;
        xnodce = 4.5236020 - 9.2422029E-4*day;
        stem = Math.sin(xnodce);
        ctem = Math.cos(xnodce);
        zcosil = 0.91375164 - 0.03568096*ctem;
        zsinil = Math.sqrt(1 - zcosil*zcosil);
        zsinhl = 0.089683511*stem/zsinil;
        zcoshl = Math.sqrt(1 - zsinhl*zsinhl);
        c = 4.7199672 + 0.22997150*day;
        gam = 5.8351514 + 0.0019443680*day;
        zmol = fmod2p(c - gam);
        zx = 0.39785416*stem/zsinil;
        zy = zcoshl*ctem + 0.91744867*zsinhl*stem;
        zx = Math.atan2(zx,zy);
        zx = gam + zx - xnodce;
        zcosgl = Math.cos(zx);
        zsingl = Math.sin(zx);
        zmos = 6.2565837 + 0.017201977*day;
        zmos = fmod2p(zmos);
      }
      //{ Do solar terms }
      //10:
      savtsn = 1E20;
      zcosg = zcosgs;
      zsing = zsings;
      zcosi = zcosis;
      zsini = zsinis;
      zcosh = cosq;
      zsinh = sinq;
      cc = c1ss;
      zn = zns;
      ze = zes;
      zmo = zmos;
      xnoi = 1.0/xnq;
      ls = 30; //{assign 30 to ls}
      //m20:
      do{
        a1 = zcosg*zcosh + zsing*zcosi*zsinh;
        a3 = -zsing*zcosh + zcosg*zcosi*zsinh;
        a7 = -zcosg*zsinh + zsing*zcosi*zcosh;
        a8 = zsing*zsini;
        a9 = zsing*zsinh + zcosg*zcosi*zcosh;
        a10 = zcosg*zsini;
        a2 = cosiq*a7 +  siniq*a8;
        a4 = cosiq*a9 +  siniq*a10;
        a5 = -siniq*a7 +  cosiq*a8;
        a6 = -siniq*a9 +  cosiq*a10;
        x1 = a1*cosomo + a2*sinomo;
        x2 = a3*cosomo + a4*sinomo;
        x3 = -a1*sinomo + a2*cosomo;
        x4 = -a3*sinomo + a4*cosomo;
        x5 = a5*sinomo;
        x6 = a6*sinomo;
        x7 = a5*cosomo;
        x8 = a6*cosomo;
        z31 = 12.0*x1*x1 - 3.0*x3*x3;
        z32 = 24.0*x1*x2 - 6.0*x3*x4;
        z33 = 12.0*x2*x2 - 3.0*x4*x4;
        z1 = 3.0*(a1*a1 + a2*a2) + z31*eqsq;
        z2 = 6.0*(a1*a3 + a2*a4) + z32*eqsq;
        z3 = 3.0*(a3*a3 + a4*a4) + z33*eqsq;
        z11 = -6*a1*a5 + eqsq*(-24.0*x1*x7 - 6.0*x3*x5);
        z12 = -6.0*(a1*a6 + a3*a5)
        + eqsq*(-24.0*(x2*x7 + x1*x8) - 6.0*(x3*x6 + x4*x5));
        z13 = -6*a3*a6 + eqsq*(-24*x2*x8 - 6.0*x4*x6);
        z21 = 6.0*a2*a5 + eqsq*(24.0*x1*x5 - 6.0*x3*x7);
        z22 = 6.0*(a4*a5 + a2*a6)
        + eqsq*(24.0*(x2*x5 + x1*x6) - 6.0*(x4*x7 + x3*x8));
        z23 = 6.0*a4*a6 + eqsq*(24.0*x2*x6 - 6.0*x4*x8);
        z1 = z1 + z1 + bsq*z31;
        z2 = z2 + z2 + bsq*z32;
        z3 = z3 + z3 + bsq*z33;
        s3 = cc*xnoi;
        s2 = -0.5*s3/rteqsq;
        s4 = s3*rteqsq;
        s1 = -15.0*eq*s4;
        s5 = x1*x3 + x2*x4;
        s6 = x2*x3 + x1*x4;
        s7 = x2*x4 - x1*x3;
        se = s1*zn*s5;
        si = s2*zn*(z11 + z13);
        sl = -zn*s3*(z1 + z3 - 14.0 - 6.0*eqsq);
        sgh = s4*zn*(z31 + z33 - 6.0);
        sh = -zn*s2*(z21 + z23);
        if (xqncl < 5.2359877E-2)
        sh = 0;
        ee2 = 2.0*s1*s6;
        e3 = 2*s1*s7;
        xi2 = 2*s2*z12;
        xi3 = 2*s2*(z13 - z11);
        xl2 = -2*s3*z2;
        xl3 = -2*s3*(z3 - z1);
        xl4 = -2*s3*(-21 - 9*eqsq)*ze;
        xgh2 = 2*s4*z32;
        xgh3 = 2*s4*(z33 - z31);
        xgh4 = -18*s4*ze;
        xh2 = -2*s2*z22;
        xh3 = -2*s2*(z23 - z21);
        if(ls==40)
        break;  //Goto 40;
        else
        if(ls==30){
          //{ Do lunar terms }
          m30:
          sse = se;
          ssi = si;
          ssl = sl;
          ssh = sh/siniq;
          ssg = sgh - cosiq*ssh;
          se2 = ee2;
          si2 = xi2;
          sl2 = xl2;
          sgh2 = xgh2;
          sh2 = xh2;
          se3 = e3;
          si3 = xi3;
          sl3 = xl3;
          sgh3 = xgh3;
          sh3 = xh3;
          sl4 = xl4;
          sgh4 = xgh4;
          zcosg = zcosgl;
          zsing = zsingl;
          zcosi = zcosil;
          zsini = zsinil;
          zcosh = zcoshl*cosq + zsinhl*sinq;
          zsinh = sinq*zcoshl - cosq*zsinhl;
          zn = znl;
          cc = c1l;
          ze = zel;
          zmo = zmol;
          ls = 40;        //{assign 40 to ls}
        }else return;
      }while(true);              //Goto 20;
      m40:
      sse = sse + se;
      ssi = ssi + si;
      ssl = ssl + sl;
      ssg = ssg + sgh - cosiq/siniq*sh;
      ssh = ssh + sh/siniq;
      //{Geopotential resonance initialization for 12 hour orbits}
      iresfl = 0;
      isynfl = 0;
      if (!((xnq < 0.0052359877) && (xnq > 0.0034906585))){
        //               Goto 70;
        if ((xnq < 8.26E-3) || (xnq > 9.24E-3))
           return;   //exit;
        if (eq < 0.5)
           return;    //exit;
        iresfl = 1;
        eoc = eq*eqsq;
        g201 = -0.306 - (eq - 0.64)*0.440;
        if (!(eq > 0.65)){
          g211 = 3.616 - 13.247*eq + 16.290*eqsq;
          g310 = -19.302 + 117.390*eq - 228.419*eqsq + 156.591*eoc;
          g322 = -18.9068 + 109.7927*eq - 214.6334*eqsq + 146.5816*eoc;
          g410 = -41.122 + 242.694*eq - 471.094*eqsq + 313.953*eoc;
          g422 = -146.407 + 841.880*eq - 1629.014*eqsq + 1083.435*eoc;
          g520 = -532.114 + 3017.977*eq - 5740*eqsq + 3708.276*eoc;
        } else {
          g211 = -72.099 + 331.819*eq - 508.738*eqsq + 266.724*eoc;
          g310 = -346.844 + 1582.851*eq - 2415.925*eqsq + 1246.113*eoc;
          g322 = -342.585 + 1554.908*eq - 2366.899*eqsq + 1215.972*eoc;
          g410 = -1052.797 + 4758.686*eq - 7193.992*eqsq + 3651.957*eoc;
          g422 = -3581.69 + 16178.11*eq - 24462.77*eqsq + 12422.52*eoc;
          if (!(eq > 0.715))
          g520 = 1464.74 - 4664.75*eq + 3763.64*eqsq;
          else
          g520 = -5149.66 + 29936.92*eq - 54087.36*eqsq + 31324.56*eoc;
        }
        if (!(eq >= (0.7))){
          g533 = -919.2277 + 4988.61*eq - 9064.77*eqsq + 5542.21*eoc;
          g521 = -822.71072 + 4568.6173*eq - 8491.4146*eqsq + 5337.524*eoc;
          g532 = -853.666 + 4690.25*eq - 8624.77*eqsq + 5341.4*eoc;
        } else {
          g533 = -37995.78 + 161616.52*eq - 229838.2*eqsq + 109377.94*eoc;
          g521 = -51752.104 + 218913.95*eq - 309468.16*eqsq + 146349.42*eoc;
          g532 = -40023.88 + 170470.89*eq - 242699.48*eqsq + 115605.82*eoc;
        }
        sini2 = siniq*siniq;
        f220 = 0.75*(1 + 2*cosiq + cosq2);
        f221 = 1.5*sini2;
        f321 = 1.875*siniq*(1 - 2*cosiq - 3*cosq2);
        f322 = -1.875*siniq*(1 + 2*cosiq - 3*cosq2);
        f441 = 35*sini2*f220;
        f442 = 39.3750*sini2*sini2;
        f522 = 9.84375*siniq*(sini2*(1 - 2*cosiq - 5*cosq2)
        + 0.33333333*(-2 + 4*cosiq + 6*cosq2));
        f523 = siniq*(4.92187512*sini2*(-2 - 4*cosiq + 10*cosq2)
        + 6.56250012*(1 + 2*cosiq - 3*cosq2));
        f542 = 29.53125*siniq*(2 - 8*cosiq + cosq2*(-12 + 8*cosiq + 10*cosq2));
        f543 = 29.53125*siniq*(-2 - 8*cosiq + cosq2*(12 + 8*cosiq - 10*cosq2));
        xno2 = xnq*xnq;
        ainv2 = aqnv*aqnv;
        temp1 = 3*xno2*ainv2;
        temp = temp1*root22;
        d2201 = temp*f220*g201;
        d2211 = temp*f221*g211;
        temp1 = temp1*aqnv;
        temp = temp1*root32;
        d3210 = temp*f321*g310;
        d3222 = temp*f322*g322;
        temp1 = temp1*aqnv;
        temp = 2*temp1*root44;
        d4410 = temp*f441*g410;
        d4422 = temp*f442*g422;
        temp1 = temp1*aqnv;
        temp = temp1*root52;
        d5220 = temp*f522*g520;
        d5232 = temp*f523*g532;
        temp = 2*temp1*root54;
        d5421 = temp*f542*g521;
        d5433 = temp*f543*g533;
        xlamo = xmao + xnodeo + xnodeo - thgr - thgr;
        bfact = xlldot + xnodot + xnodot - thdt - thdt;
        bfact = bfact + ssl + ssh + ssh;
      } else {
        //{ Synchronous resonance terms initialization }
        iresfl = 1;
        isynfl = 1;
        g200 = 1 + eqsq*(-2.5 + 0.8125*eqsq);
        g310 = 1 + 2*eqsq;
        g300 = 1 + eqsq*(-6 + 6.60937*eqsq);
        f220 = 0.75*(1 + cosiq)*(1 + cosiq);
        f311 = 0.9375*siniq*siniq*(1 + 3*cosiq) - 0.75*(1 + cosiq);
        f330 = 1 + cosiq;
        f330 = 1.875*f330*f330*f330;
        del1 = 3*xnq*xnq*aqnv*aqnv;
        del2 = 2*del1*f220*g200*q22;
        del3 = 3*del1*f330*g300*q33*aqnv;
        del1 = del1*f311*g310*q31*aqnv;
        fasx2 = 0.13130908;
        fasx4 = 2.8843198;
        fasx6 = 0.37448087;
        xlamo = xmao + xnodeo + omegao - thgr;
        bfact = xlldot + xpidot - thdt;
        bfact = bfact + ssl + ssg + ssh;
      }
      xfact = bfact - xnq;
      //{ Initialize integrator }
      xli = xlamo;
      xni = xnq;
      atime = 0;
      stepp = 720;
      stepn = -720;
      step2 = 259200;
      break;

      case 2:
      //dpsec  : begin { Entrance for deep space secular effects }
    xll += ssl * t;
    omgasm += ssg * t;
    xnodes += ssh * t;
    _em = eo + sse * t;
    xinc = xincl + ssi * t;

    if (xinc < 0.){
	xinc = -(xinc);
	xnodes += Math.PI;
	omgasm -= Math.PI;
    }
    if (iresfl == 0)
	return;

    for (;;){
	if (iret == 0){
	    if (atime == 0.0 || (t >= 0. && atime < 0.0) ||
		    (t < 0.0 && atime >= 0.)){
		// EPOCH RESTART

		if (t < 0.){
		    delt = stepn;
		} else {
		    delt = stepp;
		}
		atime = 0.0;
		xni = xnq;
		xli = xlamo;
	    } else {
		if (Math.abs (t) >= Math.abs (atime)){
		    if (t > 0.0){
			delt = stepp;
		    } else{
			delt = stepn;
		    }
		} else {
		    if (t < 0.){
			delt = stepp;
		    } else {
			delt = stepn;
		    }

		    // goto L150;
		}
	    }
	}

	if (Math.abs (t - atime) < stepp){
	    ft = t - atime;
	    iretn = 0;
	} else {
	    iret = 1;
	}

	//DOT TERMS CALCULATED

	// L150:

	if (isynfl != 0){
	    xndot = del1 * Math.sin(xli - fasx2) + del2 *
		Math.sin((xli - fasx4) * 2.0) + del3 *
		Math.sin((xli - fasx6) * 3.0);
	    xnddt = del1 * Math.cos(xli - fasx2)
		+ del2 * 2.0 * Math.cos((xli - fasx4) * 2.0) +
		del3 * 3.0 * Math.cos((xli - fasx6) * 3.0);
	} else {
	    xomi = omegaq + omgdt * atime;
	    x2omi = xomi + xomi;
	    x2li = xli + xli;
	    xndot = d2201 * Math.sin(x2omi + xli - g22)
		+ d2211 * Math.sin(xli - g22) + d3210 *
		Math.sin(xomi + xli - g32) + d3222 * Math.sin(-xomi + xli - g32)
		+ d4410 * Math.sin(x2omi + x2li - g44) + d4422 * 
		Math.sin(x2li - g44) + d5220 * Math.sin(xomi + xli - g52) 
		+ d5232 * Math.sin(-xomi + xli - g52) + d5421 * 
		Math.sin(xomi + x2li - g54) + d5433 * Math.sin(-xomi 
		+ x2li - g54);
	    xnddt = d2201 * Math.cos(x2omi + xli - g22)
		+ d2211 * Math.cos(xli - g22) + d3210 *
		Math.cos(xomi + xli - g32) + d3222 * Math.cos(-xomi + xli - g32)
		+ d5220 * Math.cos(xomi + xli - g52) + d5232 * 
		Math.cos(-xomi + xli - g52) + (d4410 * 
		Math.cos(x2omi + x2li - g44) + d4422 * Math.cos( x2li - g44) + 
		d5421 * Math.cos(xomi + x2li - g54) + d5433 * 
		Math.cos(-xomi + x2li - g54)) * 2.0;
	}
	xldot = xni + xfact;
	xnddt *= xldot;
	if (iretn == 0){
	    break;
	}

       // INTEGRATOR
	xli = xli + xldot * delt + xndot * step2;
	xni = xni + xndot * delt + xnddt * step2;
	atime += delt;
    }

    xn = xni + xndot * ft + xnddt * ft * ft * 0.5;
    xl = xli + xldot * ft + xndot * ft * ft * 0.5;
    temp = -(xnodes) + thgr + t * thdt;
    xll = xl - omgasm + temp;

    if (isynfl == 0){
	xll = xl + temp + temp;
    }

//***********************************************************
/*         xll = xll + ssl*t;
         omgasm = omgasm + ssg*t;
         xnodes = xnodes + ssh*t;
         _em = eo + sse*t;
         xinc = xincl + ssi*t;
         if (xinc < 0){
            xinc = -xinc;
            xnodes = xnodes  +  Math.PI;
            omgasm = omgasm - Math.PI;
         }
         if (iresfl==0)
            return;   //exit
   int label=100;
   while(true){
      if(label==100){
         m100:
         if ( !(atime==0 || ((t >= 0) && (atime < 0)) ||
         ((t < 0) && (atime >=0))) ){
            if (Math.abs(t) >= Math.abs(atime))
               label=120;
            else {
               delt = stepp;
               if (t >= 0)
                  delt = stepn;
               iret = 100; //{assign 100 to iret}
               label=160;
            }
         m120:
            if(label==120){
               delt = stepn;
               if (t > 0)
               delt = stepp;
            }
         m125:
            if(label==125){
               if (Math.abs(t - atime) < stepp)
                  label=130;
               iret = 125; //{assign 125 to iret}
               label=160;
            }
         m130:
            if(label==130){
               ft = t - atime;
               iretn = 140; //{assign 140 to iretn}
               label=150;
            }
         m140:
            if(label==140){
               xn = xni + xndot*ft + xnddt*ft*ft*0.5;
               xl = xli + xldot*ft + xndot*ft*ft*0.5;
               temp = -xnodes + thgr + t*thdt;
               xll = xl - omgasm + temp;
               if (isynfl==0)
                  xll = xl + temp + temp;
               return;    //exit;
            }
         m150:
            if(label==150){
               //{ Dot terms calculated }
               if (isynfl!=0){
                  xndot = del1*Math.sin(xli - fasx2) + del2*Math.sin(2*(xli - fasx4))
                     + del3*Math.sin(3*(xli - fasx6));
                  xnddt = del1*Math.cos(xli - fasx2)
                     + 2*del2*Math.cos(2*(xli - fasx4))
                     + 3*del3*Math.cos(3*(xli - fasx6));
               } else {
                  xomi = omegaq + omgdt*atime;
                  x2omi = xomi + xomi;
                  x2li = xli + xli;
                  xndot = d2201*Math.sin(x2omi + xli - g22)
                     + d2211*Math.sin(xli - g22)
                     + d3210*Math.sin(xomi + xli - g32)
                     + d3222*Math.sin(-xomi + xli - g32)
                     + d4410*Math.sin(x2omi + x2li - g44)
                     + d4422*Math.sin(x2li - g44)
                     + d5220*Math.sin(xomi + xli - g52)
                     + d5232*Math.sin(-xomi + xli - g52)
                     + d5421*Math.sin(xomi + x2li - g54)
                     + d5433*Math.sin(-xomi + x2li - g54);
                  xnddt = d2201*Math.cos(x2omi + xli - g22)
                     + d2211*Math.cos(xli - g22)
                     + d3210*Math.cos(xomi + xli - g32)
                     + d3222*Math.cos(-xomi + xli - g32)
                     + d5220*Math.cos(xomi + xli - g52)
                     + d5232*Math.cos(-xomi + xli - g52)
                     + 2*(d4410*Math.cos(x2omi + x2li - g44)
                     + d4422*Math.cos(x2li - g44)
                     + d5421*Math.cos(xomi + x2li - g54)
                     + d5433*Math.cos(-xomi + x2li - g54));
               }
            }
            xldot = xni + xfact;
            xnddt = xnddt*xldot;
            switch(iretn){
               case 140: label=140; break;
               case 165: label=165; break;
               default: return;
            }
         m160:
            // Integrator
            if(label==160){
               iretn = 165; //{assign 165 to iretn}
               label=150;
            }
         m165:
            if(label==165){
               xli = xli + xldot*delt + xndot*step2;
               xni = xni + xndot*delt + xnddt*step2;
               atime = atime + delt;
               switch(iret){
                  case 100: label=100; break;
                  case 125: label=125; break;
                  default: return;   //Halt
               }
            }
         }
      }
         //{ Epoch restart }
         if (t < 0){
            delt = stepn;
         } else
         delt = stepp;
         atime = 0;
         xni = xnq;
         xli = xlamo;
         label=125;
   }
*/
/*         100:
         if ( !(atime==0 || ((t >= 0) && (atime < 0)) ||
         ((t < 0) && (atime >=0))) ){
            if (Math.abs(t) >= Math.abs(atime))
               Goto 120;
            delt = stepp;
            if (t >= 0)
               delt = stepn;
            iret = 100; //{assign 100 to iret}
            Goto 160;
         120:
            delt = stepn;
            if (t > 0)
            delt = stepp;
         125:
            if (Math.abs(t - atime) < stepp)
               Goto 130;
            iret = 125; //{assign 125 to iret}
            Goto 160;
         130:
            ft = t - atime;
            iretn = 140; //{assign 140 to iretn}
            Goto 150;
         140:
            xn = xni + xndot*ft + xnddt*ft*ft*0.5;
            xl = xli + xldot*ft + xndot*ft*ft*0.5;
            temp = -xnodes + thgr + t*thdt;
            xll = xl - omgasm + temp;
            if (isynfl==0)
               xll = xl + temp + temp;
            return;    //exit;
         150:
            //{ Dot terms calculated }
            if (isynfl!=0){
               xndot = del1*Math.sin(xli - fasx2) + del2*Math.sin(2*(xli - fasx4))
                  + del3*Math.sin(3*(xli - fasx6));
               xnddt = del1*Math.cos(xli - fasx2)
                  + 2*del2*Math.cos(2*(xli - fasx4))
                  + 3*del3*Math.cos(3*(xli - fasx6));
            } else {
               xomi = omegaq + omgdt*atime;
               x2omi = xomi + xomi;
               x2li = xli + xli;
               xndot = d2201*Math.sin(x2omi + xli - g22)
                  + d2211*Math.sin(xli - g22)
                  + d3210*Math.sin(xomi + xli - g32)
                  + d3222*Math.sin(-xomi + xli - g32)
                  + d4410*Math.sin(x2omi + x2li - g44)
                  + d4422*Math.sin(x2li - g44)
                  + d5220*Math.sin(xomi + xli - g52)
                  + d5232*Math.sin(-xomi + xli - g52)
                  + d5421*Math.sin(xomi + x2li - g54)
                  + d5433*Math.sin(-xomi + x2li - g54);
               xnddt = d2201*Math.cos(x2omi + xli - g22)
                  + d2211*Math.cos(xli - g22)
                  + d3210*Math.cos(xomi + xli - g32)
                  + d3222*Math.cos(-xomi + xli - g32)
                  + d5220*Math.cos(xomi + xli - g52)
                  + d5232*Math.cos(-xomi + xli - g52)
                  + 2*(d4410*Math.cos(x2omi + x2li - g44)
                  + d4422*Math.cos(x2li - g44)
                  + d5421*Math.cos(xomi + x2li - g54)
                  + d5433*Math.cos(-xomi + x2li - g54));
            }
            xldot = xni + xfact;
            xnddt = xnddt*xldot;
            case iretn of
               140 : Goto 140;
               165 : Goto 165;
            else
            return;
            //end; //{case}
         //{ Integrator }
         160:
            iretn = 165; //{assign 165 to iretn}
            Goto 150;
         165:
            xli = xli + xldot*delt + xndot*step2;
            xni = xni + xndot*delt + xnddt*step2;
            atime = atime + delt;
            case iret of
               100 : Goto 100;
               125 : Goto 125;
            else
            return;   //Halt
         }
         //{ Epoch restart }
         if (t < 0){
            delt = stepn;
         } else
         delt = stepp;
         atime = 0;
         xni = xnq;
         xli = xlamo;
         Goto 125;
*/
      case 3:
      //dpper  : begin //{ Entrance for lunar-solar periodics }
         sinis = Math.sin(xinc);
         cosis = Math.cos(xinc);
         if (Math.abs(savtsn - t) >= 30){
           //Goto 210;
           savtsn = t;
           zm = zmos + zns*t;
           //205:
           zf = zm + 2*zes*Math.sin(zm);
           sinzf = Math.sin(zf);
           f2 = 0.5*sinzf*sinzf - 0.25;
           f3 = -0.5*sinzf*Math.cos(zf);
           ses = se2*f2 + se3*f3;
           sis = si2*f2 + si3*f3;
           sls = sl2*f2 + sl3*f3 + sl4*sinzf;
           sghs = sgh2*f2 + sgh3*f3 + sgh4*sinzf;
           shs = sh2*f2 + sh3*f3;
           zm = zmol + znl*t;
           zf = zm + 2*zel*Math.sin(zm);
           sinzf = Math.sin(zf);
           f2 = 0.5*sinzf*sinzf - 0.25;
           f3 = -0.5*sinzf*Math.cos(zf);
           sel = ee2*f2 + e3*f3;
           sil = xi2*f2 + xi3*f3;
           sll = xl2*f2 + xl3*f3 + xl4*sinzf;
           sghl = xgh2*f2 + xgh3*f3 + xgh4*sinzf;
           sh1 = xh2*f2 + xh3*f3;
           pe = ses + sel;
           pinc = sis + sil;
           pl = sls + sll;
         }
         //210:
         pgh = sghs + sghl;
         ph = shs + sh1;
         xinc = xinc + pinc;
         _em = _em + pe;
         if (xqncl >= 0.2){
           //{ Apply periodics directly }
           ph = ph/siniq;
           pgh = pgh - cosiq*ph;
           omgasm = omgasm + pgh;
           xnodes = xnodes + ph;
           xll = xll + pl;
           break; //Goto 230;
         }
         //{Apply periodics with Lyddane modification }
         sinok = Math.sin(xnodes);
         cosok = Math.cos(xnodes);
         alfdp = sinis*sinok;
         betdp = sinis*cosok;
         dalf = ph*cosok + pinc*cosis*sinok;
         dbet = -ph*sinok + pinc*cosis*cosok;
         alfdp = alfdp + dalf;
         betdp = betdp + dbet;
         xls = xll + omgasm + cosis*xnodes;
         dls = pl + pgh - pinc*xnodes*sinis;
         xls = xls + dls;
         xnodes = Math.atan2(alfdp,betdp);
         xll = xll + pl;
         omgasm = xls - xll - Math.cos(xinc)*xnodes;
         //230: end; //{dpper}
    }
  } //{Procedure Deep}


/*  public static void Call_dpinit(double eosq, double sinio, double cosio,
  double betao, double aodp, double theta2, double sing, double cosg,
  double betao2, double xmdot, double omgdot, double xnodott, double xnodpp){
    eqsq=eosq;
    siniq=sinio;
    cosiq=cosio;
    rteqsq=betao;
    ao=aodp;
    cosq2=theta2;
    sinomo=sing;
    cosomo=cosg;
    bsq=betao2;
    xlldot=xmdot;
    omgdt=omgdot;
    xnodot=xnodott;
    xnodp=xnodpp;
    Deep(1);
    eosq=eqsq;
    sinio=siniq;
    cosio=cosiq;
    betao=rteqsq;
    aodp=ao;
    theta2=cosq2;
    sing=sinomo;
    cosg=cosomo;
    betao2=bsq;
    xmdot=xlldot;
    omgdot=omgdt;
    xnodott=xnodot;
    xnodpp=xnodp;
  }
*/

/*  public static void Call_dpsec(double xmdf, double omgadf, double xnode,
  double emm, double xincc, double xnn, double tsince){
    xll=xmdf;
    omgasm=omgadf;
    xnodes=xnode;
    //{_em=emm;xinc=xincc;}
    xn=xnn;
    t=tsince;
    Deep(2);
    xmdf=xll;
    omgadf=omgasm;
    xnode=xnodes;
    emm=_em;
    xincc=xinc;
    xnn=xn;
    tsince=t;
  }
*/
/*  public static void Call_dpper(double e, double xincc, double omgadf,
  double xnode, double xmam){
    _em=e;
    xinc=xincc;
    omgasm=omgadf;
    xnodes=xnode;
    xll=xmam;
    Deep(3);
    e=_em;
    xincc=xinc;
    omgadf=omgasm;
    xnode=xnodes;
    xmam=xll;
  }
*/

  public static void SDP4(double tsince,int iflag,double[] pos, double[] vel){
    double a1=0,a3ovk2=0,ao=0,aodp=0,aycof=0,betao=0,betao2=0,c1=0,c2=0,c4=0,
    coef=0,coef1=0,cosg=0,cosio=0,del1=0,delo=0,eeta=0,eosq=0,eta=0,etasq=0,
    omgdot=0,perige=0,pinvsq=0,psisq=0,qoms24=0,s4=0,sing=0,sinio=0,t2cof=0,
    temp1=0,temp2=0,temp3=0,theta2=0,theta4=0,tsi=0,x1m5th=0,x1mth2=0,
    x3thm1=0,x7thm1=0,xhdot1=0,xlcof=0,xmdot=0,xnodcf=0,xnodot=0,xnodp=0;
    int i;

    double a=0,axn=0,ayn=0,aynl=0,beta=0,betal=0,capu=0,cos2u=0,cosepw=0,
    cosik=0,cosnok=0,cosu=0,cosuk=0,e=0,ecose=0,elsq=0,em=0,epw=0,esine=0,
    omgadf=0,pl=0,r=0,rdot=0,rdotk=0,rfdot=0,rfdotk=0,rk=0,sin2u=0,sinepw=0,
    sinik=0,sinnok=0,sinu=0,sinuk=0,temp=0,temp4=0,temp5=0,temp6=0,tempa=0,
    tempe=0,templ=0,tsq=0,u=0,uk=0,ux=0,uy=0,uz=0,vx=0,vy=0,vz=0,xinc=0,
    xinck=0,xl=0,xll=0,xlt=0,xmam=0,xmdf=0,xmx=0,xmy=0,xn=0,xnoddf=0,xnode=0,
    xnodek=0,x=0,y=0,z=0,xdot=0,ydot=0,zdot=0;

    if (iflag!=0){
      // Recover original mean motion (xnodp) and semimajor axis (aodp)
      // from input elements.
      a1=Math.pow(xke/xno,tothrd);
      cosio=Math.cos(xincl);
      theta2=cosio*cosio;
      x3thm1=3.0*theta2 - 1.0;
      eosq=eo*eo;
      betao2=1.0 - eosq;
      betao=Math.sqrt(betao2);
      del1=1.5*ck2*x3thm1/(a1*a1*betao*betao2);
      ao=a1*(1.0 - del1*(0.5*tothrd + del1*(1.0 + 134.0/81.0*del1)));
      delo=1.5*ck2*x3thm1/(ao*ao*betao*betao2);
      xnodp=xno/(1.0 + delo);
      aodp=ao/(1.0 - delo);
      //{ Initialization }
      //{ For perigee below 156 km, the values of s and qoms2t are altered. }
      s4=s;
      qoms24=qoms2t;
      perige=(aodp*(1.0 - eo) - ae)*xkmper;
      if (!(perige >= 156.0)){
        s4=perige - 78.0;
        if (!(perige > 98.0))
           s4=20;
        else {
          qoms24=Math.pow((120.0 - s4)*ae/xkmper,4.0);
          s4=s4/xkmper + ae;
        }
      }
      //10:
      pinvsq=1.0/(aodp*aodp*betao2*betao2);
      sing=Math.sin(omegao);
      cosg=Math.cos(omegao);
      tsi=1.0/(aodp - s4);
      eta=aodp*eo*tsi;
      etasq=eta*eta;
      eeta=eo*eta;
      psisq=Math.abs(1.0 - etasq);
      coef=qoms24*Math.pow(tsi,4);
      coef1=coef/Math.pow(psisq,3.5);
      c2=coef1*xnodp*(aodp*(1 + 1.5*etasq + eeta*(4 + etasq))
         + 0.75*ck2*tsi/psisq*x3thm1*(8 + 3*etasq*(8 + etasq)));
      c1=bstar*c2;
      sinio=Math.sin(xincl);
      a3ovk2=-xj3/ck2*Math.pow(ae,3);
      x1mth2=1 - theta2;
      c4=2*xnodp*coef1*aodp*betao2*(eta*(2 + 0.5*etasq)
         + eo*(0.5 + 2*etasq) - 2*ck2*tsi/(aodp*psisq)
         *(-3*x3thm1*(1 - 2*eeta + etasq*(1.5 - 0.5*eeta))
         + 0.75*x1mth2*(2*etasq - eeta*(1 + etasq))*Math.cos(2*omegao)));
      theta4=theta2*theta2;
      temp1=3.0*ck2*pinvsq*xnodp;
      temp2=temp1*ck2*pinvsq;
      temp3=1.25*ck4*pinvsq*pinvsq*xnodp;
      xmdot=xnodp + 0.5*temp1*betao*x3thm1
         + 0.0625*temp2*betao*(13.0 - 78.0*theta2 + 137.0*theta4);
      x1m5th=1.0 - 5.0*theta2;
      omgdot=-0.5*temp1*x1m5th + 0.0625*temp2*(7.0-114.0*theta2 + 395.0*theta4)
         + temp3*(3.0 - 36.0*theta2 + 49.0*theta4);
      xhdot1=-temp1*cosio;
      xnodot=xhdot1 + (0.5*temp2*(4.0 - 19.0*theta2)
         + 2*temp3*(3.0 - 7.0*theta2))*cosio;
      xnodcf=3.5*betao2*xhdot1*c1;
      t2cof=1.5*c1;
      xlcof=0.125*a3ovk2*sinio*(3.0 + 5.0*cosio)/(1.0 + cosio);
      aycof=0.25*a3ovk2*sinio;
      x7thm1=7.0*theta2 - 1.0;
      // 90:
      iflag=0;

    eqsq=eosq;
    siniq=sinio;
    cosiq=cosio;
    rteqsq=betao;
    ao=aodp;
    cosq2=theta2;
    sinomo=sing;
    cosomo=cosg;
    bsq=betao2;
    xlldot=xmdot;
    omgdt=omgdot;
//    xnodot=xnodott;
//    xnodp=xnodpp;
    Deep(1);
    eosq=eqsq;
    sinio=siniq;
    cosio=cosiq;
    betao=rteqsq;
    aodp=ao;
    theta2=cosq2;
    sing=sinomo;
    cosg=cosomo;
    betao2=bsq;
    xmdot=xlldot;
    omgdot=omgdt;
//    xnodott=xnodot;
//    xnodpp=xnodp;

/*      Call_dpinit(eosq,sinio,cosio,betao,aodp,theta2,sing,cosg,
      betao2,xmdot,omgdot,xnodot,xnodp);
*/
    }
    //100:
    //{ Update for secular gravity and atmospheric drag }
    xmdf=xmo + xmdot*tsince;
    omgadf=omegao + omgdot*tsince;
    xnoddf=xnodeo + xnodot*tsince;
    tsq=tsince*tsince;
    xnode=xnoddf + xnodcf*tsq;
    tempa=1.0 - c1*tsince;
    tempe=bstar*c4*tsince;
    templ=t2cof*tsq;
    xn=xnodp;

    xll=xmdf;
    omgasm=omgadf;
    xnodes=xnode;
    //{_em=emm;xinc=xincc;}
//    xn=xnn;
    t=tsince;
    Deep(2);
    xmdf=xll;
    omgadf=omgasm;
    xnode=xnodes;
    em=_em;
//    xincc=xinc;
//    xnn=xn;
    tsince=t;
//    Call_dpsec(xmdf,omgadf,xnode,em,xinc,xn,tsince);
    
    a=Math.pow(xke/xn,tothrd)*tempa*tempa;
    e=em - tempe;
    xmam=xmdf + xnodp*templ;
//System.out.println(" #0 "+a+" "+omgadf+" "+xnode);
    _em=e;
//    xinc=xincc;
    omgasm=omgadf;
    xnodes=xnode;
    xll=xmam;
    Deep(3);
    e=_em;
//    xincc=xinc;
    omgadf=omgasm;
    xnode=xnodes;
    xmam=xll;
//    Call_dpper(e,xinc,omgadf,xnode,xmam);
//System.out.println(" # "+xmam+" "+omgadf+" "+xnode);
    xl=xmam + omgadf + xnode;
    beta=Math.sqrt(1.0 - e*e);
    xn=xke/Math.pow(a,1.5);
    //{ Long period periodics }
    axn=e*Math.cos(omgadf);
    temp=1.0/(a*beta*beta);
    xll=temp*xlcof*axn;
    aynl=temp*aycof;
    xlt=xl + xll;
    ayn=e*Math.sin(omgadf) + aynl;
    //{ Solve Kepler's Equation }
    capu=fmod2p(xlt - xnode);
    temp2=capu;
    for (i=1;i<=10;++i){
      sinepw=Math.sin(temp2);
      cosepw=Math.cos(temp2);
      temp3=axn*sinepw;
      temp4=ayn*cosepw;
      temp5=axn*cosepw;
      temp6=ayn*sinepw;
      epw=(capu - temp4 + temp3 - temp2)/(1.0 - temp5 - temp6) + temp2;
      if (Math.abs(epw - temp2) <= e6a) break;
      //130:
      temp2=epw;
    }
    //{ Short period preliminary quantities }
    //140:
    ecose=temp5 + temp6;
    esine=temp3 - temp4;
    elsq=axn*axn + ayn*ayn;
    temp=1.0 - elsq;
    pl=a*temp;
    r=a*(1.0 - ecose);
    temp1=1.0/r;
    rdot=xke*Math.sqrt(a)*esine*temp1;
    rfdot=xke*Math.sqrt(pl)*temp1;
    temp2=a*temp1;
    betal=Math.sqrt(temp);
    temp3=1.0/(1.0 + betal);
    cosu=temp2*(cosepw - axn + ayn*esine*temp3);
    sinu=temp2*(sinepw - ayn - axn*esine*temp3);
    u=Math.atan2(sinu,cosu);
    sin2u=2.0*sinu*cosu;
    cos2u=2.0*cosu*cosu - 1.0;
    temp=1.0/pl;
    temp1=ck2*temp;
    temp2=temp1*temp;
    //{ Update for short periodics }
    rk=r*(1.0 - 1.5*temp2*betal*x3thm1) + 0.5*temp1*x1mth2*cos2u;
    uk=u - 0.25*temp2*x7thm1*sin2u;
    xnodek=xnode + 1.5*temp2*cosio*sin2u;
    xinck=xinc + 1.5*temp2*cosio*sinio*cos2u;
    rdotk=rdot - xn*temp1*x1mth2*sin2u;
    rfdotk=rfdot + xn*temp1*(x1mth2*cos2u + 1.5*x3thm1);
    //{ Orientation vectors }
    sinuk=Math.sin(uk);
    cosuk=Math.cos(uk);
    sinik=Math.sin(xinck);
    cosik=Math.cos(xinck);
    sinnok=Math.sin(xnodek);
    cosnok=Math.cos(xnodek);
    xmx=-sinnok*cosik;
    xmy=cosnok*cosik;
    ux=xmx*sinuk + cosnok*cosuk;
    uy=xmy*sinuk + sinnok*cosuk;
    uz=sinik*sinuk;
    vx=xmx*cosuk - cosnok*sinuk;
    vy=xmy*cosuk - sinnok*sinuk;
    vz=sinik*cosuk;
    //{ Position and velocity }
    x=rk*ux;  pos[0]=x;
    y=rk*uy;  pos[1]=y;
    z=rk*uz;  pos[2]=z;
    xdot=rdotk*ux + rfdotk*vx;  vel[0]=xdot;
    ydot=rdotk*uy + rfdotk*vy;  vel[1]=ydot;
    zdot=rdotk*uz + rfdotk*vz;  vel[2]=zdot;
  }


  //mjd - from 1950
  public static void SGP(double mjd, double[] pos, double[] vel){
    double tsince;
    tsince=(mjd-mjulian_epoch)*xmnpda;
//tsince=mjd;    //for testing only!
    if (ideep==0)
       SGP4(tsince,iflag,pos,vel);
    else
       SDP4(tsince,iflag,pos,vel);
  }

  public static double fmod2p(double arg){
    double modu, ret;
    modu = arg - (int)(arg/twopi) * twopi;
    if (modu >= 0.0)ret = modu;
    else ret = modu + twopi;
    return ret;
  }

  public static double Thetag(double epoch_a){
    // Reference:  The 1992 Astronomical Almanac, page B6.
    double year_a,day_a,UT,jd_a,TU,GMST,ThetaG;
    // Modification to support Y2K
    // Valid 1957 through 2056
    year_a=Math.ceil(epoch_a*1.0E-3);
    if(year_a < 57)
    year_a=year_a + 2000;
    else
    year_a=year_a + 1900;

    day_a=getFrac(epoch_a*1.0E-3)*1.0E3;
    UT=getFrac(day_a);
    day_a=Math.floor(day_a);
    jd_a=julianDateOfYear(year_a) + day_a;
    TU=(jd_a - 2451545.0)/36525;
    GMST=24110.54841 + TU * (8640184.812866 + TU * (0.093104 - TU * 6.2E-6));
    GMST=getModulus(GMST + secday*omega_E*UT,secday);
    ThetaG=twopi * GMST/secday;
    ds50=jd_a - 2433281.5 + UT;
    // ThetaG := Modulus(6.3003880987*ds50 + 1.72944494,twopi);
    return ThetaG;
  }

  public static double getModulus(double arg1,double arg2){
    double modu,Modulus;

    modu=arg1-Math.floor(arg1/arg2)*arg2;
    if(modu>=0.0)
    Modulus=modu;
    else
    Modulus=modu+arg2;
    return Modulus;
  }

  public static double getFrac(double arg1){
    return arg1-Math.floor(arg1);
  }

  public static double julianDateOfYear(double year){
    //{ Astronomical Formulae for Calculators, Jean Meeus, pages 23-25 }
    //{ Calculate Julian Date of 0.0 Jan year }
    double A,B;

    --year;
    A= Math.floor(year/100);
    B= 2 - A + Math.floor(A/4);
    return Math.floor(365.25 * year)+Math.floor(30.6001 * 14)+1720994.5 + B;
  }

  /*public static double julianDateOfEpoch(double epoch_a){
  double year_a,day_a;

  //{ Modification to support Y2K }
  //{ Valid 1957 through 2056 }
  year_a=Math.floor(epoch_a*1E-3);
  if(year_a<57)
  year_a= year_a + 2000;
  else
  year_a= year_a + 1900;
  day_a = getFrac(epoch_a*1E-3)*1E3;
  return julianDateOfEpoch(year_a) + day_a;
  }*/

  public static void convertSatelliteData(TLERecord tlerec){
    //int iexp,ibexp;
    double a1,ao,del1,delo,xnodp,temp;

    //    abuf                       : two_line;
    //  abuf := sat_data[arg];
    // Decode Card 1
    //catnr   = tlerec.satNumber1;
    //epoch   = Real_Value(abuf[1],19,14);
    //mjulian_epoch= Julian_Date_of_Epoch(epoch);
    mjulian_epoch= julianDateOfYear(tlerec.epochYear)+tlerec.epochDay-JD1950;
    xndt2o  = tlerec.fstTimeDerivOfMeanMotion;
    xndd6o  = tlerec.sndTimeDerivOfMeanMotion;
    //iexp    = Integer_Value(abuf[1],51,2);
    bstar   = tlerec.bstar;
    //ibexp   = Integer_Value(abuf[1],60,2);
    //elset   = ThreeDigit(Integer_Value(abuf[1],66,3));
    // Decode Card 2
    xincl   = tlerec.incl;
    xnodeo  = tlerec.ascenOfNode;
    eo      = tlerec.eccentricity;
    omegao  = tlerec.argOfPerigee;
    xmo     = tlerec.meanAnomaly;
    xno     = tlerec.meanMotion;
    // period  = 1/xno; }
    // Convert to proper units
    //xndd6o  = xndd6o*Power(10.0,iexp);
    //bstar   = bstar*Power(10.0,ibexp)/ae;
    //xnodeo  = Radians(xnodeo);
    //omegao  = Radians(omegao);
    //xmo     = Radians(xmo);
    //xincl   = Radians(xincl);
    xno     = xno*twopi/xmnpda;
    xndt2o  = xndt2o*twopi/(xmnpda*xmnpda);
    xndd6o  = xndd6o*twopi/(xmnpda*xmnpda*xmnpda);

    // Determine whether Deep-Space Model is needed
    a1= Math.pow(xke/xno,tothrd);
    temp= 1.5*ck2*(3.0*Math.pow(Math.cos(xincl),2.0)-1)/Math.pow(1.0-eo*eo,1.5);
    del1= temp/(a1*a1);
    ao= a1*(1.0 - del1*(0.5*tothrd + del1*(1.0 + 134.0/81.0*del1)));
    delo= temp/(ao*ao);
    xnodp= xno/(1.0 + delo);
/*    if (twopi/xnodp >= 225.0)
       ideep=1;
    else*/
       ideep=0; //Currently SDP4 we do not use
    iflag=1;
  }

  public static void convertSatState(double[] pos, double[] vel){
    for(int i=0;i<3;++i){
      pos[i]=pos[i]*xkmper;    //kilometers
      vel[i]=vel[i]*xkmper/60; //kilometers/second
    }
    //Magnitude(pos);
    //Magnitude(vel);
  }

  public static GeiAndVei getSatPos(double mjd,TLERecord tr){
    GeiAndVei x=new GeiAndVei();
    nullAll();
    convertSatelliteData(tr);
    System.gc();
    SGP(mjd,x.gei,x.vei);
    convertSatState(x.gei,x.vei);   //to km & km/sec
    return x;
  }

  public static void nullAll(){
     iflag=0;xmo=0;xnodeo=0;omegao=0;eo=0;xincl=0;xno=0;xndt2o=0;
     xndd6o=0;bstar=0;mjulian_epoch=0;epoch=0;UTC_offset=0;ds50=0;
     eqsq=0;siniq=0;cosiq=0;rteqsq=0;ao=0;cosq2=0;sinomo=0;cosomo=0;
     bsq=0;xlldot=0;omgdt=0;xnodot=0;xnodp=0;xll=0;omgasm=0;xnodes=0;
     _em=0;xinc=0;xn=0;t=0;delt=0;ft=0;
  }

  /*   public static void Define_Derived_Constants(){
  xke:=Sqrt(3600*ge/Cube(xkmper));  //Sqrt(ge) ER^3/min^2
  qoms2t := Sqr(Sqr(qo-s));              //(qo-s)^4 ER^4
  }
   */

  /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  begin
  Define_Derived_Constants;
  end.
   */

  /*   public static void main(String[] sss){

  }
   */

}
