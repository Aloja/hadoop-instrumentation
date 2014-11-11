source env-postprocessing.sh

#############################################
### GET THE HADOOP NODES DUMPING's ##########
#############################################
#dumping: filtrado del volcado del lsof
while read node
do
ssh $node <<ENDSSH
chown -R $PCUSER: $TMP_PPING/set-*
mv $TMP_PPING/dumping-host-port-pid $TMP_PPING/dumping-host-port-pid.unfiltered
cat $TMP_PPING/dumping-host-port-pid.unfiltered | grep -v COMMAND | grep -v LISTEN | grep java | tr -s ' ' | sed -e 's/:/ /g' | sed -e 's/->/ /g' | cut -d ' ' -f 2,9,10 | sed -e 's/ /:/g' >> $TMP_PPING/dumping-host-port-pid
cat $TMP_PPING/dumping-host-port-pid.unfiltered | grep -v COMMAND | grep LISTEN | grep java | tr -s ' ' | sed -e 's/:/ /g' | sed -e 's/->/ /g' | cut -d ' ' -f 2,9,10 | sed -e 's/ /:/g'  >> $TMP_PPING/dumping-host-port-pid
mv $TMP_PPING/dumping-host-port-pid $TMP_PPING/dumping-host-port-pid_unsorted
sort -k1,3 -u  $TMP_PPING/dumping-host-port-pid_unsorted   > $TMP_PPING/dumping-host-port-pid
mv $TMP_PPING/dumping-host-port-pid $TMP_PPING/dumping-host-port-pid.tmp
cat $TMP_PPING/dumping-host-port-pid.tmp | eval sed 's/[^:]*/\$(hostname --ip-address)/'2 > $TMP_PPING/dumping-host-port-pid.lsof
ENDSSH
done < $HADOOP_PREFIX/conf/slaves

#############################################
### GET WALA's FROM HADOOP LOGS #############
#############################################
dumping_wala=$TMP_PPING/dumping.wala
#Getting all the WALA from all the hadoop logs from all nodes
while read node
do
ssh $PCUSER@$node <<ENDSSH
grep WALA $HADOOP_PREFIX/logs/* -R > $dumping_wala
cat $dumping_wala | cut -d':' -f2,3,4 > $dumping_wala'.tmp'
mv $dumping_wala'.tmp' $dumping_wala
#replace del /
sed -r 's/[\/\ ]+//g' $dumping_wala > $dumping_wala'.tmp'
mv $dumping_wala'.tmp' $dumping_wala
#127.0.0.1 must be replaced by the nodeip
cat $dumping_wala >> $TMP_PPING/dumping-host-port-pid.hlog
ENDSSH
done < $HADOOP_PREFIX/conf/slaves

#get dumping-host-port-pid from hadoop nodes
mkdir $TMP_PPING/distributed-merge
while read node
do
scp $node:$TMP_PPING/dumping-host-port-pid.lsof $TMP_PPING/distributed-merge/dumping-host-port-pid_$node.lsof
scp $node:$TMP_PPING/dumping-host-port-pid.hlog $TMP_PPING/distributed-merge/dumping-host-port-pid_$node.hlog
done < $HADOOP_PREFIX/conf/slaves

#merge all nodes dumping-host-port-pid into a unique
rm -f $TMP_PPING/distributed-merge/dumping-host-port-pid
for f in `ls -d $TMP_PPING/distributed-merge/* | grep dumping-host-port-pid_`
do
cat $f >> $TMP_PPING/distributed-merge/dumping-host-port-pid
done


