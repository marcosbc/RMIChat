#/bin/bash

RMIHOST=localhost
RMIPORT=54321

java -Djava.security.policy=../cliente.permisos ClienteChat $RMIHOST $RMIPORT
