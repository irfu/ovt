#
# Makefile to build OVT shared libs & bins.
#
# $Id: Makefile.in,v 2.16 2009/10/26 23:33:03 yuri Exp $
#

SHELL = /usr/local/bin/bash
RM = rm

.SUFFIXES: .c .h

INSTALL = /usr/bin/install -c
INSTALL_PROGRAM = ${INSTALL}

CC = gcc

SHLIB_CFLAGS = -fPIC
SHLIB_BUILD_FLAGS = -shared

CFLAGS = -O3
CC_FLAGS = ${SHLIB_CFLAGS} ${CFLAGS} -DUNIX

srcdir        = .

JAVA_INCLUDES = -I/usr/local/diablo-jdk1.6.0/include
JAVA_INCLUDES_OS = -I/usr/local/diablo-jdk1.6.0/include/freebsd
INSTALLDIR = ../lib

OVTVERSION = 2.3

OBJS = utils.o tsyg96.o tsyg2001.o magpack.o 

USATOBJS = usat/cnstinit.o usat/deep.o usat/fmod2p_2.o usat/getsatpos.o usat/matan2.o usat/mjd.o usat/sdp4.o usat/sgp4.o usat/thetag.o

WEIMEROBJ = weimer/cgmc2.o weimer/cgmc4.o

all:
	if test "xfreebsd" = "xdarwin"; then archs="i386 x86_64 ppc";\
	for arch in $$archs;do\
		echo "Building $$arch";\
		$(MAKE) clean-objs;\
		CC_FLAGS_XTRA="-mmacosx-version-min=10.4 -arch $$arch" \
		$(MAKE) libovt-${OVTVERSION}.so;\
		[ ! -e libovt-${OVTVERSION}.so ] && exit 1;\
		mkdir -p arch/$$arch && mv libovt-${OVTVERSION}.so arch/$$arch/;\
	done;\
	echo "Creating universal library libovt-${OVTVERSION}.so ...";\
	lipo -create arch/*/libovt-${OVTVERSION}.so -output libovt-${OVTVERSION}.so;\
	echo "Done.";\
	else $(MAKE) libovt-${OVTVERSION}.so;\
	fi

.c.o:
	${CC} ${CC_FLAGS_XTRA} ${CC_FLAGS} -c ${JAVA_INCLUDES} ${JAVA_INCLUDES_OS} $< -o $@

libovt-${OVTVERSION}.so: $(OBJS) $(USATOBJS) $(WEIMEROBJ)
	${CC}  ${CC_FLAGS_XTRA} ${SHLIB_BUILD_FLAGS} ${OBJS} $(USATOBJS) $(WEIMEROBJ) -o $@


install:
	@echo "Installing OVT shared library ..."
	${INSTALL} -m 555 libovt-${OVTVERSION}.so ${exec_prefix}/lib

clean: clean-objs
	${RM} -rf arch
	
clean-objs:
	${RM} -f config.* *.o *.so  usat/*.o  weimer/*.o
