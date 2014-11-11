#!/bin/sh
PATH_EXTRAE=/home/smendoza/lightness/hadoopextrae/extrae/
#echo "Sourcing Extrae Environment" &&\
#sh ${PATH_MS}/env_extrae.sh &&\
#\
#echo "Cleaning previous builds" &&\
#cd ${PATH_EXTRAE}m4-1.4.6 &&\
#make clean &&\
#cd ${PATH_EXTRAE}automake-1.11.6 &&\
#make clean &&\
#cd ${PATH_EXTRAE}autoconf-2.68 &&\
#make clean &&\
#cd ${PATH_EXTRAE}libpcap-1.4.0 &&\
#make clean &&\
#cd ${PATH_EXTRAE}extrae-2.3.4 &&\
#make clean &&\
#\
#echo "Starting configure/build process" &&\
#cd ${PATH_EXTRAE}autoconf-2.68 &&\
#./configure --prefix=${PATH_EXTRAE}autoconf && make -j 4 && make install  &&\
#cd ${PATH_EXTRAE}automake-1.11.6 &&\
#./configure --prefix=${PATH_EXTRAE}automake && make -j 4 && make install  &&\
#cd ${PATH_EXTRAE}m4-1.4.6 &&\
#./configure --prefix=${PATH_EXTRAE}m4 && make -j 4 && make install  &&\
#cd ${PATH_EXTRAE}libpcap-1.4.0 &&\
#./configure --prefix=${PATH_EXTRAE}pcap && make -j 4 && make install  &&\
cd ${PATH_EXTRAE}extrae-2.3.4 &&\
./configure --prefix=${PATH_EXTRAE}dist --without-mpi --without-unwind --without-dyninst --without-papi &&\
make -j 8 && make install &&\
\
#echo "Sourcing Hadoop Environment" &&\
#. ${PATH_MS}/env_hadoop.sh &&\
# \
#cd ${PATH_EXTRAE}java_wrapper &&\
#cat env.sh &&\
#. ${PATH_EXTRAE}java_wrapper/env.sh &&\
#make &&\

echo "Build successful!"

