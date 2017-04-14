#!/bin/bash

# Parar ejecucion si hay errores
set -e

# Configuracion
CWD=`pwd`
SVRDIR=$CWD/RMIChatServer
CLIDIR=$CWD/RMIChatClient

# Compilar dependencias para servidor y cliente
cd $CLIDIR/bin
javac -encoding iso-8859-1 -d . ../src/Cliente.java
cp Cliente.class $SVRDIR/bin
cd $SVRDIR/bin
javac -encoding iso-8859-1 -d . ../src/ServicioChat.java
cp ServicioChat.class $CLIDIR/bin

# Compilar servidor
cd $SVRDIR/bin
javac -encoding iso-8859-1 -d . ../src/*.java

# Compilar cliente
cd $CLIDIR/bin
javac -encoding iso-8859-1 -d . ../src/*.java

echo "Compilacion terminada correctamente"

