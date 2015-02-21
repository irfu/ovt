@echo off

SET OVT_VERSION=2.2
SET JAVA_HOME=c:\ko\jdk1.3.1

set MSVCDir=C:\MSVS\VC98

rem
echo Setting environment for using Microsoft Visual C++ tools.
rem

rem set PATH=C:\Pro\VISUAL_C\OS\SYSTEM;%MSVCDir%\bin;%PATH%
set PATH=%MSVCDir%\bin;%PATH%
set INCLUDE=%MSVCDir%\include
set LIB=%MSVCDir%\lib

del ovt2g-%OVT_VERSION%.dll

cl utils.c magpack.c tsyg96.c tsyg2001.c usat\cnstinit.c usat\deep.c usat\fmod2p_2.c usat\getsatpos.c usat\matan2.c usat\mjd.c usat\rsat.c usat\sdp4.c usat\sgp4.c usat\thetag.c -I%JAVA_HOME%\include -I%JAVA_HOME%\include\win32 -I%MSVCDir%\include -Feovt2g-%OVT_VERSION%.dll -MD -LD -nologo %JAVA_HOME%\lib\jvm.lib >err.txt

echo PATH=%PATH%
echo MSVCDir=%MSVCDir%
echo incl=%INCLUDE%
echo lib=%MSVCDir%\lib

copy ovt2g-%OVT_VERSION%.dll ..\..\release\bin\
