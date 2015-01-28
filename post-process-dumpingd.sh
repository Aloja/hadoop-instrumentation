#!/usr/bin/env bash
set -o errexit  # Exit immediately on non-zero status
set -o nounset  # Treat unset variables as an error
set -o xtrace   # Debug mode: display the command and its expanded arguments

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/vars.sh

#############################################
### GET THE HADOOP NODES DUMPING's ##########
#############################################
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


