#!/bin/bash

LOGS_FOLDER=$1
LOGS_FOLDER=/home/$USER/MS/hadoop-dist/logs

EXTRAE_LABELS=
mpi2prv /tmp/set-0/* -o $1.prv
mv $1.prv $1_raw.prv
cat $1_raw.prv | grep -v ^1 > $1.prv
rm $1_raw.prv

exit

#Ther following version is for multiple applications

unset command
SRC_FOLDER=${EXTRAE_DIR}/set-0

for f in `ls /tmp/set-0`
do
	if [ ! -z "${command}" ]; 
	then 
		command="$command  -- ";
	fi;
	
	command="$command  ${SRC_FOLDER}/$f";

done


#echo mpi2prv $command -o foo.prv
mpi2prv $command -o $1.prv

#grep DCARRERA $LOGS_FOLDER/* | cut -d "(" -f 2 | cut -d ")" -f 1 | cut -d "," -f 1
grep DCARRERA $LOGS_FOLDER/* | cut -d "(" -f 2 | cut -d ")" -f 1 | sort -u
