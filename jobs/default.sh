# Copy some files to HDFS
$HADOOP_PREFIX/bin/hadoop fs -put $HADOOP_PREFIX/conf input

sleep 5

$HADOOP_PREFIX/bin/hadoop jar $HADOOP_JAR_EXAMPLES grep input output 'dfs[a-z.]+'