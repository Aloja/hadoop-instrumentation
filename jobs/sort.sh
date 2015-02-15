HADOOP_EXECUTABLE="$HADOOP_PREFIX/bin/hadoop"
HADOOP_EXAMPLES_JAR="$HADOOP_PREFIX/hadoop-examples-1.0.4-SNAPSHOT.jar"

DATA_HDFS=/HiBench
INPUT_HDFS=${DATA_HDFS}/Sort/Input
OUTPUT_HDFS=${DATA_HDFS}/Sort/Output

# sort 400MB total
# for prepare (per node) - 200MB/node
DATASIZE=200000000
NUM_MAPS=2
# for running (in total)
NUM_REDS=4

$HADOOP_EXECUTABLE jar $HADOOP_EXAMPLES_JAR randomtextwriter \
-D test.randomtextwrite.bytes_per_map=$((${DATASIZE} / ${NUM_MAPS})) \
-D test.randomtextwrite.maps_per_host=${NUM_MAPS} \
$INPUT_HDFS

sleep 5

$HADOOP_EXECUTABLE jar $HADOOP_EXAMPLES_JAR sort \
-outKey org.apache.hadoop.io.Text \
-outValue org.apache.hadoop.io.Text \
-r ${NUM_REDS} \
$INPUT_HDFS $OUTPUT_HDFS