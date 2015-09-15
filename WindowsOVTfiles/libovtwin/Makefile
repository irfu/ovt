#
# Makefile to build OVT shared libs & bins.
#
# $Id: Makefile.in,v 2.17 2009/10/27 15:01:15 yuri Exp $
#

SHELL = /bin/sh
RM = rm

.SUFFIXES: .c .h

INSTALL = /bin/install -c
INSTALL_PROGRAM = ${INSTALL}

CC = gcc

SHLIB_CFLAGS = -fPIC
SHLIB_BUILD_FLAGS = 

CFLAGS = -g -O2
CC_FLAGS = ${SHLIB_CFLAGS} ${CFLAGS} -DUNIX

srcdir        = .

JAVA_INCLUDES = -IC:/Users/frejon/Desktop/jre1.8.0_51/include
JAVA_INCLUDES_OS = -IC:/Users/frejon/Desktop/jre1.8.0_51/include/unsupported
INSTALLDIR = ../lib

OVTVERSION = 3.0

OBJS = utils.o tsyg96.o tsyg2001.o magpack.o 

USATOBJS = usat/cnstinit.o usat/deep.o usat/fmod2p_2.o usat/getsatpos.o usat/matan2.o usat/mjd.o usat/sdp4.o usat/sgp4.o usat/thetag.o

WEIMEROBJ = weimer/cgmc2.o weimer/cgmc4.o

all: ovt-${OVTVERSION}

.c.o:
	${CC} ${CC_FLAGS_XTRA} ${CC_FLAGS} -c ${JAVA_INCLUDES} ${JAVA_INCLUDES_OS} $< -o $@

ovt-${OVTVERSION}: $(OBJS) $(USATOBJS) $(WEIMEROBJ)
	${CC}  ${CC_FLAGS_XTRA} ${SHLIB_BUILD_FLAGS} ${OBJS} $(USATOBJS) $(WEIMEROBJ) -o $@


install:
	@echo "Installing OVT shared library ..."
	${INSTALL} -m 555 ovt-${OVTVERSION} ${exec_prefix}/lib

clean: clean-objs
	${RM} -rf arch
	
clean-objs:
	${RM} -f *.o *  usat/*.o  weimer/*.o
