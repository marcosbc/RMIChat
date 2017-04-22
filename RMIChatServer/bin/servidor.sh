#/bin/bash

RMIPORT=54321
CLASSPATH=.:../lib/jackson-annotations-2.8.5.jar:../lib/jackson-core-2.8.5.jar:../lib/jackson-databind-2.8.5.jar:../lib/json-20160810.jar
export CLASSPATH

ps aux | grep rmiregistry | awk '{ print $2 }' | xargs kill 2>/dev/null
rmiregistry $RMIPORT &
java -Djava.security.policy=../servidor.permisos ServidorChat $RMIPORT
