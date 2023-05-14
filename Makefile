# **********************************************************************
# *                           MJSIP MAKEFILE                           *
# **********************************************************************
#
# This is a make file that builds everything.
# this works with the gnu make tool.
# If you are working with MS Windows, you can install
# cygwin (http://www.cygwin.org) or
# djgpp (http://www.delorie.com/djgpp)
#
# Major make targets:
#
#	all 	compiles and builds jar files
#	jar		builds jar files
#
# **********************************************************************

ROOT= .
include $(ROOT)/makefile-config


DOCDIR= doc
SRCDIR= src
CLASSDIR= classes
LIBDIR= lib
#LOGDIR= log


ifeq (${OS},Windows)
	COLON= ;
else
	COLON= :
endif


MJSIP_LIBS= $(LIBDIR)/sip.jar


#%.class: %.java
#	$(JAVAC) $<


# **************************** Default action **************************
default: 
#	@echo MjSIP: select the package you want to build
	$(MAKE) all


# ******************************** Cleans ******************************
clean: 
	@echo make clean: to be implemented..


cleanlogs:
	cd $(LOGDIR);$(RM) *.log; cd..


# ****************************** Builds all ****************************
all: 
	$(MAKE) compile
	$(MAKE) jar



# *************************** Compiles source files **************************
compile:
	@echo ------------------ COMPILING SRC ------------------
	cd $(SRCDIR);	\
	$(JAVAC) -d ../$(CLASSDIR) $(addsuffix /*.java, $(addprefix org/zoolu/, net sound $(addprefix sound/, codec $(addprefix codec/, amr g711 g726 gsm)) util));	\
	$(JAVAC) -classpath ../$(CLASSDIR) -d ../$(CLASSDIR) $(addsuffix /*.java, $(addprefix org/mjsip/, media net rtp sdp sdp/field server server/sbc $(addprefix sip/, address authentication call dialog header message provider transaction) ua $(addprefix ua/, cli gui)));	\
	cd ..



# ************************** Creates jar files  ************************
jar:
	@echo ------------------ CREATING JARs ------------------
	cd $(CLASSDIR);	\
	$(JAR) -cf ../$(LIBDIR)/sip.jar org/zoolu $(addprefix org/mjsip/, rtp sdp sip) -C ../$(LIBDIR) COPYRIGHT.txt -C ../$(LIBDIR) license.txt;	\
	$(JAR) -cf ../$(LIBDIR)/ua.jar $(addprefix org/mjsip/, media net ua) -C ../resources media/org/mjsip/ua;	\
	$(JAR) -cf ../$(LIBDIR)/server.jar $(addprefix org/mjsip/, net server) -C ../$(LIBDIR) COPYRIGHT.txt -C ../$(LIBDIR) license.txt;	\
	cd ..

