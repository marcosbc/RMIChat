CWD=$(shell pwd)
JAVAC=javac -encoding iso-8859-1
JAVAVERSION=$(java -version)
# Servidor
SERVDIR=$(CWD)/RMIChatServer
SERVSRC=$(SERVDIR)/src
SERVBIN=$(SERVDIR)/bin
SERVLIB=$(SERVDIR)/lib
SERVCLASSPATH=$(SERVBIN):$(SERVLIB)/jackson-annotations-2.8.5.jar:$(SERVLIB)/jackson-core-2.8.5.jar:$(SERVLIB)/jackson-databind-2.8.5.jar:$(SERVLIB)/json-20160810.jar
# Cliente
CLIDIR=$(CWD)/RMIChatClient
CLISRC=$(CLIDIR)/src
CLIBIN=$(CLIDIR)/bin
CLICLASSPATH=$(CLIBIN)

all: jdk-version-check servidor cliente

jdk-version-check:
	@echo -e "javac 1.8\n$(shell javac -version 2>&1)" | sort -ct. -k1,1n -k2,2n -k3,3n 2>/dev/null || ( echo "Tu version de Java JDK es menor a 1.8.0" && exit 1 )

depsservidor:
	$(JAVAC) -d $(SERVBIN) -cp $(SERVCLASSPATH) $(CLISRC)/Cliente.java

depscliente:
	$(JAVAC) -d $(CLIBIN) -cp $(CLICLASSPATH) $(CLISRC)/Cliente.java $(SERVSRC)/ServicioChat.java

servidor: depsservidor
	$(JAVAC) -d $(SERVBIN) -cp $(SERVCLASSPATH) $(SERVSRC)/*.java

cliente: depscliente
	$(JAVAC) -d $(CLIBIN) -cp $(CLICLASSPATH) $(CLISRC)/*.java

clean:
	rm -rf $(SERVBIN)/*.class $(CLIBIN)/*.class

