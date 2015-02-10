HADOOP_EXECUTABLE="$HADOOP_PREFIX/bin/hadoop"
HADOOP_EXAMPLES_JAR="$HADOOP_PREFIX/hadoop-examples-1.0.4-SNAPSHOT.jar"

DATA_HDFS=/HiBench
INPUT_HDFS=${DATA_HDFS}/Sort/Input
OUTPUT_HDFS=${DATA_HDFS}/Sort/Output

# 250MB
DATASIZE=250000000
NUM_MAPS=4
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