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
#	all 		cleans, builds everything
#	sip		builds sip.jar 
#	server		builds server.jar 
#	ua		builds ua.jar 
#	qsip		builds qsip.jar 
#	gw		builds gw.jar 
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

NAMESPACE= org.zoolu
NAMESPACE_PATH= org/zoolu

#SIP_PACKAGES= address header message provider transaction dialog
SIP_PACKAGES= $(notdir $(wildcard $(SRCDIR)/$(NAMESPACE_PATH)/sip/*))


#%.class: %.java
#	$(JAVAC) $<


# **************************** Default action **************************
default: 
#	$(MAKE) all
	@echo MjSIP: select the package you want to build


# ******************************** Cleans ******************************
clean: 
	@echo make clean: to be implemented..


cleanlogs:
	cd $(LOGDIR);$(RM) *.log; cd..


# ****************************** Builds all ****************************
all: 
	$(MAKE) sip
	$(MAKE) server
	$(MAKE) ua



# *************************** Creates sip.jar **************************
sip:
	@echo ------------------ MAKING SIP ------------------
	cd $(SRCDIR);	\
	$(JAVAC) -d ../$(CLASSDIR) $(NAMESPACE_PATH)/tools/*.java $(NAMESPACE_PATH)/net/*.java $(NAMESPACE_PATH)/sdp/*.java;	\
	$(JAVAC) -classpath ../$(CLASSDIR) -d ../$(CLASSDIR) $(addsuffix /*.java,$(addprefix $(NAMESPACE_PATH)/sip/,$(SIP_PACKAGES)));	\
	cd ..

	cd $(CLASSDIR);	\
	$(JAR) -cf ../$(MJSIP_LIBS) $(addprefix $(NAMESPACE_PATH)/,tools net sdp sip) -C ../$(LIBDIR) COPYRIGHT.txt -C ../$(LIBDIR) license.txt;	\
	cd ..



# ************************** Creates server.jar ************************
server:
	@echo ----------------- MAKING SERVER ----------------
	$(JAVAC) -classpath "$(MJSIP_LIBS)" -d $(CLASSDIR) $(addsuffix /*.java,$(addprefix $(SRCDIR)/local/,server))

	cd $(CLASSDIR);	\
	$(JAR) -cf ../$(LIBDIR)/server.jar $(addprefix local/,server);	\
	cd ..



# **************************** Creates ua.jar **************************
ua:
	@echo ------------------- MAKING UA ------------------
	$(JAVAC) -classpath "$(MJSIP_LIBS)" -d $(CLASSDIR) $(addsuffix /*.java,$(addprefix $(SRCDIR)/local/,net media ua))

	cd $(CLASSDIR);	\
	$(JAR) -cf ../$(LIBDIR)/ua.jar $(addprefix local/,net media ua) -C .. /media/local/ua;	\
	cd ..

