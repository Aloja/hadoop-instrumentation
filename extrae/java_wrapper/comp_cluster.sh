source ~/lightness/hadoopextrae/env-execution.sh

#set capabilities en el cluster de minerva
while read node
do
ssh $node <<ENDSSH
cd /home/smendoza/lightness/hadoopextrae/extrae/java_wrapper
make
sudo setcap_sniffer
ENDSSH
done < $HADOOP_PREFIX/conf/slaves

