# -----------------------------------------------------------------
# $Revision: 1.5.2.2 $
# $Date: 2005/04/14 22:01:21 $
# -----------------------------------------------------------------
# Programmer(s): Radu Serban and Aaron Collier @ LLNL
# -----------------------------------------------------------------
# Copyright (c) 2002, The Regents of the University of California.
# Produced at the Lawrence Livermore National Laboratory.
# All rights reserved.
# For details, see sundials/cvode/LICENSE.
# -----------------------------------------------------------------
# Makefile for CVODE serial examples
#
# cvode/examples_ser/Makefile.  Generated from Makefile.in by configure.
# -----------------------------------------------------------------

SHELL = /bin/sh

srcdir       = .
builddir     = .
top_builddir = ../../
top_builddir = ../../
prefix       = /Users/mr_smith22586/Documents/workspace/rulebender/distributionResources/BioNetGen/mac64/BioNetGen-2.1.8r597/Network2
exec_prefix  = ${prefix}
includedir   = ${prefix}/include
libdir       = ${exec_prefix}/lib



CPP      = /usr/bin/cc -E
CPPFLAGS = 
CC       = /usr/bin/cc
CFLAGS   = -g -O2
LDFLAGS  = 
LIBS     = -lm 

COMPILER_PREFIX = 
LINKER_PREFIX   = 

SUNDIALS_INC_DIR = $(includedir)
SUNDIALS_LIB_DIR = $(libdir)
SUNDIALS_LIBS    = -lsundials_cvode -lsundials_nvecserial -lsundials_shared

EXEC_FILES = cvdx cvdxe cvbx cvkx cvkxb cvdemd cvdemk

all: $(EXEC_FILES)

cvdx: $(srcdir)/cvdx.c
	$(COMPILER_PREFIX) $(CC) $(CPPFLAGS) -I$(SUNDIALS_INC_DIR) $(CFLAGS) -c $(srcdir)/cvdx.c
	$(LINKER_PREFIX) $(CC) -o $(builddir)/cvdx $(builddir)/cvdx.o $(LDFLAGS) -L$(SUNDIALS_LIB_DIR) $(SUNDIALS_LIBS) $(LIBS) 
cvdxe: $(srcdir)/cvdxe.c
	$(COMPILER_PREFIX) $(CC) $(CPPFLAGS) -I$(SUNDIALS_INC_DIR) $(CFLAGS) -c $(srcdir)/cvdxe.c
	$(LINKER_PREFIX) $(CC) -o $(builddir)/cvdxe $(builddir)/cvdxe.o $(LDFLAGS) -L$(SUNDIALS_LIB_DIR) $(SUNDIALS_LIBS) $(LIBS) 
cvbx: $(srcdir)/cvbx.c
	$(COMPILER_PREFIX) $(CC) $(CPPFLAGS) -I$(SUNDIALS_INC_DIR) $(CFLAGS) -c $(srcdir)/cvbx.c
	$(LINKER_PREFIX) $(CC) -o $(builddir)/cvbx $(builddir)/cvbx.o $(LDFLAGS) -L$(SUNDIALS_LIB_DIR) $(SUNDIALS_LIBS) $(LIBS) 
cvkx: $(srcdir)/cvkx.c
	$(COMPILER_PREFIX) $(CC) $(CPPFLAGS) -I$(SUNDIALS_INC_DIR) $(CFLAGS) -c $(srcdir)/cvkx.c
	$(LINKER_PREFIX) $(CC) -o $(builddir)/cvkx $(builddir)/cvkx.o $(LDFLAGS) -L$(SUNDIALS_LIB_DIR) $(SUNDIALS_LIBS) $(LIBS) 
cvkxb: $(srcdir)/cvkxb.c
	$(COMPILER_PREFIX) $(CC) $(CPPFLAGS) -I$(SUNDIALS_INC_DIR) $(CFLAGS) -c $(srcdir)/cvkxb.c
	$(LINKER_PREFIX) $(CC) -o $(builddir)/cvkxb $(builddir)/cvkxb.o $(LDFLAGS) -L$(SUNDIALS_LIB_DIR) $(SUNDIALS_LIBS) $(LIBS) 
cvdemd: $(srcdir)/cvdemd.c
	$(COMPILER_PREFIX) $(CC) $(CPPFLAGS) -I$(SUNDIALS_INC_DIR) $(CFLAGS) -c $(srcdir)/cvdemd.c
	$(LINKER_PREFIX) $(CC) -o $(builddir)/cvdemd $(builddir)/cvdemd.o $(LDFLAGS) -L$(SUNDIALS_LIB_DIR) $(SUNDIALS_LIBS) $(LIBS) 
cvdemk: $(srcdir)/cvdemk.c
	$(COMPILER_PREFIX) $(CC) $(CPPFLAGS) -I$(SUNDIALS_INC_DIR) $(CFLAGS) -c $(srcdir)/cvdemk.c
	$(LINKER_PREFIX) $(CC) -o $(builddir)/cvdemk $(builddir)/cvdemk.o $(LDFLAGS) -L$(SUNDIALS_LIB_DIR) $(SUNDIALS_LIBS) $(LIBS) 

clean:
	rm -rf .libs
	rm -f *.lo
	rm -f *.o

uninstall: clean
	rm -f $(EXEC_FILES)
