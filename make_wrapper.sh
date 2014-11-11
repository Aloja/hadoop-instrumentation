#!/bin/sh
PATH_MS=/home/$USER/MS/
PATH_EXTRAE=${PATH_MS}extrae/
echo "Sourcing Hadoop Environment" &&\
. ${PATH_MS}/env_hadoop.sh &&\
\
cd ${PATH_EXTRAE}java_wrapper &&\
cat env.sh &&\
. ${PATH_EXTRAE}java_wrapper/env.sh &&\
make &&\
echo "Build successful!"

