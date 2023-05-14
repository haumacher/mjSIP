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
#	ua		builds ua.jar 
#	server		builds server.jar 
#	sbc		builds sbc.jar 
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
#	@echo MjSIP: select the package you want to build
	$(MAKE) all


# ******************************** Cleans ******************************
clean: 
	@echo make clean: to be implemented..


cleanlogs:
	cd $(LOGDIR);$(RM) *.log; cd..


# ****************************** Builds all ****************************
all: 
	$(MAKE) sip
	$(MAKE) ua
	$(MAKE) server
	$(MAKE) sbc



# *************************** Creates sip.jar **************************
sip:
	@echo ------------------ MAKING SIP ------------------
	cd $(SRCDIR);	\
	$(JAVAC) -d ../$(CLASSDIR) $(NAMESPACE_PATH)/tools/*.java $(NAMESPACE_PATH)/sound/*.java $(NAMESPACE_PATH)/net/*.java $(NAMESPACE_PATH)/sdp/*.java;	\
	$(JAVAC) -classpath ../$(CLASSDIR) -d ../$(CLASSDIR) $(addsuffix /*.java,$(addprefix $(NAMESPACE_PATH)/sip/,$(SIP_PACKAGES)));	\
	cd ..

	cd $(CLASSDIR);	\
	$(JAR) -cf ../$(LIBDIR)/sip.jar $(addprefix $(NAMESPACE_PATH)/,tools sound net sdp sip) -C ../$(LIBDIR) COPYRIGHT.txt -C ../$(LIBDIR) license.txt;	\
	cd ..



# *************************** Creates ua.jar ***************************
ua:
	@echo ------------------- MAKING UA -------------------
	$(JAVAC) -classpath "$(LIBDIR)/sip.jar" -d $(CLASSDIR) $(addsuffix /*.java,$(addprefix $(SRCDIR)/local/,net media ua))

	cd $(CLASSDIR);	\
	$(JAR) -cf ../$(LIBDIR)/ua.jar $(addprefix local/,net media ua) $(addprefix -C ../resources media/local/,media ua);	\
	cd ..



# *************************** Creates ua.jar ***************************
jmf:
	@echo ------------------- MAKING UA WITH JMF -------------------
	$(JAVAC) -classpath "$(LIBDIR)/sip.jar" -d $(CLASSDIR) $(addsuffix /*.java,$(addprefix $(SRCDIR)/local/,net media ua) $(SRCDIR)/local.media.jmf)

	cd $(CLASSDIR);	\
	$(JAR) -cf ../$(LIBDIR)/ua.jar $(addprefix local/,net media ua) $(addprefix -C ../resources media/local/,media ua);	\
	cd ..




# ************************** Creates server.jar ************************
server:
	@echo ----------------- MAKING SERVER ----------------
	$(JAVAC) -classpath "$(LIBDIR)/sip.jar" -d $(CLASSDIR) $(addsuffix /*.java,$(addprefix $(SRCDIR)/local/,server))

	cd $(CLASSDIR);	\
	$(JAR) -cf ../$(LIBDIR)/server.jar $(addprefix local/,server);	\
	cd ..



# **************************** Creates sbc.jar ****************************
SBC_LIBS= $(LIBDIR)/sip.jar$(COLON)$(LIBDIR)/server.jar
sbc:
	@echo ------------------ MAKING SBC -------------------
	$(JAVAC) -classpath "$(SBC_LIBS)" -d $(CLASSDIR) $(addsuffix /*.java,$(addprefix $(SRCDIR)/local/,sbc net))

	cd $(CLASSDIR);	\
	$(JAR) -cf ../$(LIBDIR)/sbc.jar $(addprefix local/,sbc net);	\
	cd ..



# ************************** Creates conference.jar ************************
CONFERENCE_LIBS= $(LIBDIR)/sip.jar$(COLON)$(LIBDIR)/codec.jar$(COLON)$(LIBDIR)/server.jar
conference:
	@echo --------------- MAKING CONFERENCE --------------
	$(JAVAC) -classpath "$(CONFERENCE_LIBS)" -d $(CLASSDIR) $(addsuffix /*.java,$(addprefix $(SRCDIR)/local/,net media conference))

	cd $(CLASSDIR);	\
	$(JAR) -cf ../$(LIBDIR)/conference.jar $(addprefix local/,net media conference);	\
	cd ..
