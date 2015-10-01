@echo off
rem NOTE: This file is intended for Windows. Hence, if editing it outside of Windows it is good to make sure that newline=CR+LF.

SET OVT_VERSION=3.0
rem SET "JAVA_HOME=c:\Users\frejon\Desktop\jdk1.8.0_60"
set "MSVCDir=C:\Program Files (x86)\Microsoft Visual Studio 12.0\VC"
rem
echo Setting environment for using Microsoft Visual C++ tools.
rem
rem
rem set PATH=C:\Pro\VISUAL_C\OS\SYSTEM;%MSVCDir%\bin;%PATH%
rem set PATH=%MSVCDir%\bin;%PATH%
rem set INCLUDE=%MSVCDir%\include
rem set LIB=%MSVCDir%\lib
rem del ovt-%OVT_VERSION%.dll
rem =====================================
rem cl.exe
rem ---------------
rem cl.exe = Microsoft Visual Studio Compiler
rem Options:
rem    /nologo Suppresses display of sign-on banner.
rem    /Fe Renames the executable file.
rem    /LD Creates a dynamic-link library.
rem    /MD Compiles to create a multithreaded DLL, by using MSVCRT.lib.
rem    /I Searches a directory for include files.
rem    
echo =====================================
cl utils.c magpack.c tsyg96.c tsyg2001.c usat\cnstinit.c usat\deep.c usat\fmod2p_2.c usat\getsatpos.c usat\matan2.c usat\mjd.c usat\rsat.c usat\sdp4.c usat\sgp4.c usat\thetag.c "-I%JAVA_HOME%\include" "-I%JAVA_HOME%\include\win32" "-I%MSVCDir%\include" -Feovt-%OVT_VERSION%.dll -MD -LD -nologo "%JAVA_HOME%\lib\jvm.lib" >err.txt


rem echo PATH=%PATH%
rem echo MSVCDir=%MSVCDir%
rem echo incl=%INCLUDE%
rem echo lib=%MSVCDir%\lib

