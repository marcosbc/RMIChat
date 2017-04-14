#/bin/bash

RMIPORT=54321

ps aux | grep rmiregistry | awk '{ print $2 }' | xargs kill 2>/dev/null
rmiregistry $RMIPORT &
java -Djava.security.policy=../servidor.permisos ServidorChat $RMIPORT
