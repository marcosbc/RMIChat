#!/bin/bash

# Parar ejecucion si hay errores
set -e

# Configuracion
CWD=`pwd`
SVRDIR=$CWD/RMIChatServer
CLIDIR=$CWD/RMIChatClient

mkdir -p $SVRDIR/bin $CLIDIR/bin

# Compilar dependencias para servidor y cliente
cd $CLIDIR/bin
javac -encoding iso-8859-1 -d . ../src/Sesion.java
cp Sesion.class $SVRDIR/bin
cd $SVRDIR/bin
javac -encoding iso-8859-1 -d . ../src/Usuario.java ../src/ServicioChat.java
cp ServicioChat.class $CLIDIR/bin

# Compilar servidor
cd $SVRDIR/bin
javac -encoding iso-8859-1 -d . ../src/*.java

# Compilar cliente
cd $CLIDIR/bin
javac -encoding iso-8859-1 -d . ../src/*.java

echo "Compilacion terminada correctamente"

