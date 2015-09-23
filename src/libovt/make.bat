@echo off


SET OVT_VERSION=3.0

SET JAVA_HOME="c:\Users\frejon\Desktop\jdk1.8.0_60"
set MSVCDir="C:\Program Files (x86)\Microsoft Visual Studio 12.0\VC"


rem

echo Setting environment for using Microsoft Visual C++ tools.

rem


rem
rem set PATH=C:\Pro\VISUAL_C\OS\SYSTEM;%MSVCDir%\bin;%PATH%

rem set PATH=%MSVCDir%\bin;%PATH%

rem set INCLUDE=%MSVCDir%\include

rem set LIB=%MSVCDir%\lib


rem del ovt2g-%OVT_VERSION%.dll


cl utils.c magpack.c tsyg96.c tsyg2001.c usat\cnstinit.c usat\deep.c usat\fmod2p_2.c usat\getsatpos.c usat\matan2.c usat\mjd.c usat\rsat.c usat\sdp4.c usat\sgp4.c usat\thetag.c -I%JAVA_HOME%\include -I%JAVA_HOME%\include\win32 -I%MSVCDir%\include -Feovt2g-%OVT_VERSION%.dll -MD -LD -nologo %JAVA_HOME%\lib\jvm.lib >err.txt

rem
 echo PATH=%PATH%

rem echo MSVCDir=%MSVCDir%

rem echo incl=%INCLUDE%

rem echo lib=%MSVCDir%\lib


copy ovt2g-%OVT_VERSION%.dll ..\..\release\bin\
