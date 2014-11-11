source env-postprocessing.sh

#dumping_wala="/tmp/dumping.wala"
#grep WALA $HADOOP_PREFIX/logs/* -R > $dumping_wala

#cat $dumping_wala | cut -d':' -f2,3,4 > $dumping_wala'.tmp'
#mv $dumping_wala'.tmp' $dumping_wala

#replace del /
#sed -r 's/[\/\ ]+//g' $dumping_wala > $dumping_wala'.tmp'
#mv $dumping_wala'.tmp' $dumping_wala

#127.0.0.1 must be replaced by the nodeip 

#cat $dumping_wala >> /tmp/dumping-host-port-pid

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
cat $dumping_wala >> $TMP_PPING/dumping-host-port-pid
ENDSSH
done < $HADOOP_PREFIX/conf/slaves

#/aplic/smendoza/MS/dist/hadoop-dist/logs/hadoop-root-datanode-pccalvo.out:PORT_LOG_WALA:12742:/172.20.0.16:48273 --> pccalvo/172.20.0.16:50000
