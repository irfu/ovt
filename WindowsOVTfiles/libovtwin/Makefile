#
# Makefile to build OVT shared libs & bins.
#
# $Id: Makefile.in,v 2.17 2009/10/27 15:01:15 yuri Exp $
#

SHELL = /bin/sh
RM = rm

.SUFFIXES: .c .h

INSTALL = /usr/bin/install -c
INSTALL_PROGRAM = ${INSTALL}

CC = gcc

SHLIB_CFLAGS = -fPIC
SHLIB_BUILD_FLAGS = -dynamiclib

CFLAGS = -O3 -mmacosx-version-min=10.8
CC_FLAGS = ${SHLIB_CFLAGS} ${CFLAGS} -DUNIX

srcdir        = .

JAVA_INCLUDES = -I/Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home/include
JAVA_INCLUDES_OS = -I/Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home/include/darwin
INSTALLDIR = ../lib

OVTVERSION = 3.0

OBJS = utils.o tsyg96.o tsyg2001.o magpack.o 

USATOBJS = usat/cnstinit.o usat/deep.o usat/fmod2p_2.o usat/getsatpos.o usat/matan2.o usat/mjd.o usat/sdp4.o usat/sgp4.o usat/thetag.o

WEIMEROBJ = weimer/cgmc2.o weimer/cgmc4.o

all: libovt-${OVTVERSION}.jnilib

.c.o:
	${CC} ${CC_FLAGS_XTRA} ${CC_FLAGS} -c ${JAVA_INCLUDES} ${JAVA_INCLUDES_OS} $< -o $@

libovt-${OVTVERSION}.jnilib: $(OBJS) $(USATOBJS) $(WEIMEROBJ)
	${CC}  ${CC_FLAGS_XTRA} ${SHLIB_BUILD_FLAGS} ${OBJS} $(USATOBJS) $(WEIMEROBJ) -o $@


install:
	@echo "Installing OVT shared library ..."
	${INSTALL} -m 555 libovt-${OVTVERSION}.jnilib ${exec_prefix}/lib

clean: clean-objs
	${RM} -rf arch
	
clean-objs:
	${RM} -f *.o *.jnilib  usat/*.o  weimer/*.o
