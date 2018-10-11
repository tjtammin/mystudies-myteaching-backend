#!/bin/bash

/usr/local/bin/docker-compose -p $1 run -v $1-gradle:/home/jenkins/.gradle -v $1-m2:/home/jenkins/.m2 --rm --user jenkins --entrypoint "$2" my-studies-builder
exit_code=$?
/usr/local/bin/docker-compose -p $1 stop
/usr/local/bin/docker-compose -p $1 rm -f -v
exit $exit_code
