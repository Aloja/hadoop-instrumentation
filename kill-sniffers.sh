source ~/lightness/hadoopextrae/env-execution.sh

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
