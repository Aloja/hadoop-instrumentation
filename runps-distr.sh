while read node
do
rm /tmp/ps_log.txt
done < $HADOOP_PREFIX/conf/slaves

while read node
do
	ssh $node 'bash /aplic/smendoza/MS/hadoopextrae/runps.sh'
done < $HADOOP_PREFIX/conf/slaves

while read node
do
	scp $node:/tmp/ps_log.txt /tmp/distributed-merge/ps_log_$node.txt
done < $HADOOP_PREFIX/conf/slaves






