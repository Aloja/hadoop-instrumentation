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
cp $EXTRAE_DIR/dumping-host-port-pid $EXTRAE_DIR/dumping-host-port-pid.unfiltered
cat $EXTRAE_DIR/dumping-host-port-pid.unfiltered | grep -v COMMAND | grep -v LISTEN | grep java | tr -s ' ' | sed -e 's/:/ /g' | sed -e 's/->/ /g' | cut -d ' ' -f 2,9,10 | sed -e 's/ /:/g' >> $EXTRAE_DIR/dumping-host-port-pid.filtered
cat $EXTRAE_DIR/dumping-host-port-pid.unfiltered | grep -v COMMAND | grep LISTEN | grep java | tr -s ' ' | sed -e 's/:/ /g' | sed -e 's/->/ /g' | cut -d ' ' -f 2,9,10 | sed -e 's/ /:/g'  >> $EXTRAE_DIR/dumping-host-port-pid.filtered
cp $EXTRAE_DIR/dumping-host-port-pid.filtered $EXTRAE_DIR/dumping-host-port-pid_unsorted
sort -k1,3 -u  $EXTRAE_DIR/dumping-host-port-pid_unsorted   > $EXTRAE_DIR/dumping-host-port-pid_sorted
cat $EXTRAE_DIR/dumping-host-port-pid_sorted | eval sed 's/[^:]*/\$(hostname --ip-address)/'2 > $EXTRAE_DIR/dumping-host-port-pid.lsof
ENDSSH
done < $HADOOP_PREFIX/conf/slaves

#get dumping-host-port-pid from hadoop nodes
mkdir -p $TRACES_OUTPUT/distributed-merge
while read node
do
scp $node:$EXTRAE_DIR/dumping-host-port-pid.lsof $TRACES_OUTPUT/distributed-merge/dumping-host-port-pid_$node.lsof
done < $HADOOP_PREFIX/conf/slaves

#merge all nodes dumping-host-port-pid into a unique
rm -f $TRACES_OUTPUT/distributed-merge/dumping-host-port-pid
for f in `ls -d $TRACES_OUTPUT/distributed-merge/* | grep dumping-host-port-pid_`
do
cat $f >> $TRACES_OUTPUT/distributed-merge/dumping-host-port-pid
done



##################################
### POST-PROCESS-EXTRAE-TRACES ###
##################################

#get extrae traces from hadoop nodes
rm -f $TRACES_OUTPUT/distributed-merge/TRACE.mpits
while read node
do
rm -rf $TRACES_OUTPUT/distributed-merge/$node
mkdir -p $TRACES_OUTPUT/distributed-merge/$node
scp -r $node:$EXTRAE_DIR/set-* $TRACES_OUTPUT/distributed-merge/$node
scp -r $node:$EXTRAE_DIR/TRACE.mpits $TRACES_OUTPUT/distributed-merge/$node
for f in $TRACES_OUTPUT/distributed-merge/$node/set-*/*mpit
do
echo $f on minerva-$node named>> $TRACES_OUTPUT/distributed-merge/TRACE.mpits
echo "--" >> $TRACES_OUTPUT/distributed-merge/TRACE.mpits
done
done < $HADOOP_PREFIX/conf/slaves

#quito el ultimo -- para que mpi2prv no me de error
cp $TRACES_OUTPUT/distributed-merge/TRACE.mpits $TRACES_OUTPUT/distributed-merge/TRACE.mpits.tmp
head -n -1 $TRACES_OUTPUT/distributed-merge/TRACE.mpits.tmp > $TRACES_OUTPUT/distributed-merge/TRACE.mpits
rm $TRACES_OUTPUT/distributed-merge/TRACE.mpits.tmp

#Generacion de todos los mpits con el TRACE.mpits separados por apps
${BIN_DIR}/mpi2prv -no-syn -f $TRACES_OUTPUT/distributed-merge/TRACE.mpits -o $TRACES_OUTPUT/mergeoutput.prv



############################
### POST-PROCESS-SYSSTAT ###
############################

rm -f $TRACES_OUTPUT/sysstat*
while read node
do
scp $node:$EXTRAE_DIR/sysstat.sar $TRACES_OUTPUT/sysstat-$node.sar
sadf -d -h -U $TRACES_OUTPUT/sysstat-$node.sar -- -u -B -r -q >> $TRACES_OUTPUT/sysstat.txt
ip=$(ssh -n $node "hostname --ip-address")
sed -i "s/$node/$ip/" $TRACES_OUTPUT/sysstat.txt
done < $HADOOP_PREFIX/conf/slaves



##############################
### POST-PROCESS-UNDEF2PRV ###
##############################

#clean log files
rm -f $TRACES_OUTPUT/undef2prv.log*

#execute the undef2prv post-processing
#procesa els ports i els genera al output
${JAVA} -cp "${LIB_DIR}/*" es.bsc.tools.undef2prv.Undef2prv $TRACES_OUTPUT/distributed-merge/dumping-host-port-pid $TRACES_OUTPUT/mergeoutput.prv $TRACES_OUTPUT/mergeoutput.row $TRACES_OUTPUT/mergeoutput.pcf $TRACES_OUTPUT/sysstat.txt
