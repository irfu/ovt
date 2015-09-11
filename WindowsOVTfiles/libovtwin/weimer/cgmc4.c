/*-----------------------------------------------------
 * The following code is an JNI interface function and
 * Win32-DLL initialisation function for CGMC.
 *-----------------------------------------------------*/

//#include <windows.h>
#include <stdio.h>
#include "ovt_util_cgmc.h"
#include "cgmc3.h"

#ifdef __cplusplus
extern "C" {
#endif

/*#ifdef __WIN32__
#pragma argsused
BOOL WINAPI DllEntryPoint(
    HINSTANCE hinstDLL,	// handle to DLL module
    DWORD fdwReason,	// reason for calling function
    LPVOID lpvReserved 	// reserved
   ){
  printf("CGMC-DllEntryPoint: Reason=%lu\n",fdwReason);
  return TRUE;
}
#else
#error Another DLL-initialisation function needed for non Win32 system
#endif
*/

JNIEXPORT void JNICALL Java_ovt_util_CGMC_newFiles(
  JNIEnv *env, jobject _this, jstring _path, jint _year){
/**/
  int nlat,ilat,idlat, nlon,ilon,idlon;
  real clat, glat, clon, glon, rlat1, rlon1, hi, re, rh;
  real dla1, dla2, dlo1, dlo2, pmi1, pmi2;
  const char *path;
  char fn_g2c[130], fn_c2g[130];
  FILE *f;
  int year = _year;
/**/
  path = (*env)->GetStringUTFChars(env, _path, 0);
  printf("CGMC-newFile: path=%s\n", path);
  sprintf(fn_g2c,"%sg2c_%04d.dat", path,year);
  sprintf(fn_c2g,"%sc2g_%04d.dat", path,year);
  (*env)->ReleaseStringUTFChars(env, _path, path);
/**/
  fill_commons(10,year);
  idlat = 5;
  idlon = 10;
  hi = 300.;
  re = 6371.2;
  rh = (re + hi) / re;
  nlat = 180 / idlat + 1;
  nlon = 360 / idlon + 1;
  rlat1 = -90.;
  rlon1 = 0.;
  if( (f = fopen(fn_g2c,"w+t")) == NULL ){
    printf("Can't create file \"%s\"", fn_g2c);
  }else{
    fprintf(f,"%5.0f %d %d %5.0f %d %d\n", rlat1,nlat,idlat, rlon1,nlon,idlon);
    for (ilat = 1; ilat <= nlat; ++ilat) {
      glat = (ilat - 1) * idlat + rlat1;
      for (ilon = 1; ilon <= nlon; ++ilon) {
	glon = (ilon - 1) * idlon + rlon1;
	geocor_(&glat, &glon, &rh, &dla1, &dlo1, &clat, &clon, &pmi1);
        fprintf(f,"%5.0f %5.0f %8.2f %8.2f\n", glat,glon,clat,clon);
      }
    }
    fclose(f);
  }
  if( (f = fopen(fn_c2g,"w+t")) == NULL ){
    printf("Can't create file \"%s\"", fn_c2g);
  }else{
    fprintf(f,"%5.0f %d %d %5.0f %d %d\n", rlat1,nlat,idlat, rlon1,nlon,idlon);
    for (ilat = 1; ilat <= nlat; ++ilat) {
      clat = (ilat - 1) * idlat + rlat1;
      for (ilon = 1; ilon <= nlon; ++ilon) {
	clon = (ilon - 1) * idlon + rlon1;
	corgeo_(&glat, &glon, &rh, &dla2, &dlo2, &clat, &clon, &pmi2);
        fprintf(f,"%5.0f %5.0f %8.2f %8.2f\n", clat,clon,glat,glon);
      }
    }
    fclose(f);
  }
}

#ifdef __cplusplus
}
#endif
