#!/usr/bin/env bash
set -o errexit  # Exit immediately on non-zero status
set -o nounset  # Treat unset variables as an error
set -o xtrace   # Debug mode: display the command and its expanded arguments

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/vars.sh



#############################
### POST-PROCESS-DUMPINGD ###
#############################

#dumping: filtrado del volcado del lsof
while read node
do
ssh $node <<ENDSSH
mv $TMP_PPING/dumping-host-port-pid $TMP_PPING/dumping-host-port-pid.unfiltered
cat $TMP_PPING/dumping-host-port-pid.unfiltered | grep -v COMMAND | grep -v LISTEN | grep java | tr -s ' ' | sed -e 's/:/ /g' | sed -e 's/->/ /g' | cut -d ' ' -f 2,9,10 | sed -e 's/ /:/g' >> $TMP_PPING/dumping-host-port-pid
cat $TMP_PPING/dumping-host-port-pid.unfiltered | grep -v COMMAND | grep LISTEN | grep java | tr -s ' ' | sed -e 's/:/ /g' | sed -e 's/->/ /g' | cut -d ' ' -f 2,9,10 | sed -e 's/ /:/g'  >> $TMP_PPING/dumping-host-port-pid
mv $TMP_PPING/dumping-host-port-pid $TMP_PPING/dumping-host-port-pid_unsorted
sort -k1,3 -u  $TMP_PPING/dumping-host-port-pid_unsorted   > $TMP_PPING/dumping-host-port-pid
mv $TMP_PPING/dumping-host-port-pid $TMP_PPING/dumping-host-port-pid.tmp
cat $TMP_PPING/dumping-host-port-pid.tmp | eval sed 's/[^:]*/\$(hostname --ip-address)/'2 > $TMP_PPING/dumping-host-port-pid.lsof
ENDSSH
done < $HADOOP_PREFIX/conf/slaves

#get dumping-host-port-pid from hadoop nodes
mkdir $TMP_PPING/distributed-merge
while read node
do
scp $node:$TMP_PPING/dumping-host-port-pid.lsof $TMP_PPING/distributed-merge/dumping-host-port-pid_$node.lsof
done < $HADOOP_PREFIX/conf/slaves

#merge all nodes dumping-host-port-pid into a unique
rm -f $TMP_PPING/distributed-merge/dumping-host-port-pid
for f in `ls -d $TMP_PPING/distributed-merge/* | grep dumping-host-port-pid_`
do
cat $f >> $TMP_PPING/distributed-merge/dumping-host-port-pid
done



##################################
### POST-PROCESS-EXTRAE-TRACES ###
##################################

#get extrae traces from hadoop nodes
rm -f $TMP_PPING/distributed-merge/TRACE.mpits
while read node
do
rm -rf $TMP_PPING/distributed-merge/$node
mkdir -p $TMP_PPING/distributed-merge/$node
scp -r $node:$TMP_PPING/set-* $TMP_PPING/distributed-merge/$node
scp -r $node:$TMP_PPING/TRACE.mpits $TMP_PPING/distributed-merge/$node
for f in $TMP_PPING/distributed-merge/$node/set-*/*mpit
do
echo $f on minerva-$node named>> $TMP_PPING/distributed-merge/TRACE.mpits
echo "--" >> $TMP_PPING/distributed-merge/TRACE.mpits
done
done < $HADOOP_PREFIX/conf/slaves

#quito el ultimo -- para que mpi2prv no me de error
cp $TMP_PPING/distributed-merge/TRACE.mpits $TMP_PPING/distributed-merge/TRACE.mpits.tmp
head -n -1 $TMP_PPING/distributed-merge/TRACE.mpits.tmp > $TMP_PPING/distributed-merge/TRACE.mpits
rm $TMP_PPING/distributed-merge/TRACE.mpits.tmp

#Generacion de todos los mpits con el TRACE.mpits separados por apps
${BIN_DIR}/mpi2prv -no-syn -f $TMP_PPING/distributed-merge/TRACE.mpits -o $TMP_PPING/mergeoutput.prv



##############################
### POST-PROCESS-UNDEF2PRV ###
##############################

#clean log files
rm -f $TMP_PPING/undef2prv.log*

#execute the undef2prv post-processing
#procesa els ports i els genera al output
${JAVA} -cp "${LIB_DIR}/*" es.bsc.tools.undef2prv.Undef2prv $TMP_PPING/distributed-merge/dumping-host-port-pid $TMP_PPING/mergeoutput.prv $TMP_PPING/mergeoutput.row $TMP_PPING/mergeoutput.pcf
