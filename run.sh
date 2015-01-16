#!/usr/bin/env bash
set -o errexit  # Exit immediately on non-zero status
set -o nounset  # Treat unset variables as an error
set -o xtrace   # Debug mode: display the command and its expanded arguments

source "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/env-execution.sh

# DEPRECATED
#set capabilities en el cluster de minerva
#while read node
#do
#ssh $node <<ENDSSH
#sudo setcap_sniffer
#ENDSSH
#done < $HADOOP_PREFIX/conf/slaves

#Stopping Cluster DFS & MapRed
$HADOOP_PREFIX/bin/stop-mapred.sh
$HADOOP_PREFIX/bin/stop-dfs.sh

echo "### CLEANING HADOOP CLUSTER ###########################"
#Limpiando hadoop cluster y temporales en distribuido...
while read node
do
ssh $node <<ENDSSH
rm -rf $TMP_PPING/*
rm $HADOOP_PREFIX/hs_err_pid*.log
rm -rf $HADOOP_PREFIX/logs/*
ENDSSH
done < $HADOOP_PREFIX/conf/slaves

#Moving to Hadoop home
wtgb=$PWD
cd $HADOOP_PREFIX
#Formateando DFS...
bin/hadoop namenode -format
sleep 5

echo "### STARTING HADOOP CLUSTER ###########################"
#Iniciando Cluster DFS
bin/start-dfs.sh
sleep 20

#Moviendo datos al DFS...
bin/hadoop fs -put conf input
sleep 5

#Iniciando Cluster MapRed
bin/start-mapred.sh
sleep 25

echo "### EXECUTING OVER HADOOP CLUSTER #####################"
#Exec Hadoop
bin/hadoop jar $HADOOP_JAR_EXAMPLES grep input output 'dfs[a-z.]+'
sleep 5

#Exec apiDetection
#bash /home/smendoza/lightness/hadoopextrae/run-apiDetection.sh
#sleep 5

echo "### STOPPING HADOOP CLUSTER ###########################"
#Stopping Cluster DFS & MapRed
bin/stop-mapred.sh
bin/stop-dfs.sh

cd $wtgb

#Kill the sniffer on all the Hadoop nodes
filter="ps aux  | grep sniffer | grep -v grep | grep -v sh | tr -s ' '  | cut -d' ' -f2"
while read node
do
ssh $node <<ENDSSH
for sniffer_pid in \`$filter\`
do
kill -2 \$sniffer_pid
done
ENDSSH
done < $HADOOP_PREFIX/conf/slaves


