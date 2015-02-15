#!/usr/bin/env bash
set -o errexit  # Exit immediately on non-zero status
set -o nounset  # Treat unset variables as an error
#set -o xtrace   # Debug mode: display the command and its expanded arguments

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/vars.sh

# Check job parameter
RUN_JOB="${HADOOP_JOBS}/default.sh"
if [ $# -ge 1 ]; then
    if [ -e "${HADOOP_JOBS}/${1}.sh" ]; then
        RUN_JOB="${HADOOP_JOBS}/${1}.sh"
    else
        echo "ERROR: ${HADOOP_JOBS}/${1}.sh not found!"
        exit 1
    fi
fi


echo "##################################################"
echo "### CHECKING SNIFFER #############################"
echo "##################################################"
# Try to change permission
while read node
do
ssh $node <<ENDSSH
sudo --non-interactive setcap cap_net_raw=eip ${SNIFFER_BIN} || true
ENDSSH
done < $HADOOP_PREFIX/conf/slaves

# Check sniffer binary has the correct capabilities set
while read node
do
ssh -n $node "setcap -q -v cap_net_raw=eip \"${SNIFFER_BIN}\"" || { \
echo "Insuficient capabilities set in node \"$node\" binary ${SNIFFER_BIN}
Please execute as root:
    sudo setcap cap_net_raw=eip ${SNIFFER_BIN}"; \
exit 1; }
done < $HADOOP_PREFIX/conf/slaves

echo "##################################################"
echo "### COPYING HADOOP CONFIG ########################"
echo "##################################################"
# Copy hadoop config to all nodes
while read node
do
scp ${CONFIG_HADOOP}/* ${node}:${HADOOP_PREFIX}/conf/
done < ${CONFIG_HADOOP}/slaves


echo "##################################################"
echo "### CLEANING HADOOP CLUSTER ######################"
echo "##################################################"
#Stopping Cluster DFS & MapRed
$HADOOP_PREFIX/bin/stop-all.sh

#Limpiando hadoop cluster y temporales en distribuido...
while read node
do
ssh $node <<ENDSSH
killall -9 sar
killall -2 sniffer
rm -rf $EXTRAE_DIR/*
rm -f $HADOOP_PREFIX/hs_err_pid*.log
rm -rf $HADOOP_PREFIX/logs/*
rm -rf /tmp/hadoop-\${USER}/*
rm -f /tmp/smfile
mkdir -p $EXTRAE_DIR
ENDSSH
done < $HADOOP_PREFIX/conf/slaves

#Formateando DFS...
$HADOOP_PREFIX/bin/hadoop namenode -format
sleep 5

echo "##################################################"
echo "### STARTING SYSSTAT #############################"
echo "##################################################"
while read node
do
ssh $node <<ENDSSH
sar -o $EXTRAE_DIR/sysstat.sar 1 >/dev/null 2>&1 &
ENDSSH
done < $HADOOP_PREFIX/conf/slaves

echo "##################################################"
echo "### STARTING HADOOP CLUSTER ######################"
echo "##################################################"
#Iniciando Hadoop
$HADOOP_PREFIX/bin/start-all.sh
sleep 5

echo "##################################################"
echo "### EXECUTING OVER HADOOP CLUSTER ################"
echo "##################################################"
# Run the hadoop job
echo "Executing ${RUN_JOB}"
. "${RUN_JOB}"
sleep 5

echo "##################################################"
echo "### STOPPING HADOOP CLUSTER ######################"
echo "##################################################"
$HADOOP_PREFIX/bin/stop-all.sh

echo "##################################################"
echo "### STOPPING SYSSTAT #############################"
echo "##################################################"
while read node
do
ssh $node <<ENDSSH
killall -9 sar
ENDSSH
done < $HADOOP_PREFIX/conf/slaves

echo "##################################################"
echo "### STOPPING SNIFFER #############################"
echo "##################################################"
while read node
do
ssh $node <<ENDSSH
killall -2 sniffer
ENDSSH
done < $HADOOP_PREFIX/conf/slaves


