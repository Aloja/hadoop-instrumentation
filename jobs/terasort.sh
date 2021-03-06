HADOOP_EXECUTABLE="$HADOOP_PREFIX/bin/hadoop"
HADOOP_EXAMPLES_JAR="$HADOOP_PREFIX/hadoop-examples-1.0.4-SNAPSHOT.jar"

DATA_HDFS=/HiBench
INPUT_HDFS=${DATA_HDFS}/Terasort/Input
OUTPUT_HDFS=${DATA_HDFS}/Terasort/Output

CONFIG_MAP_NUMBER=mapred.map.tasks
CONFIG_REDUCER_NUMBER=mapred.reduce.tasks

# for prepare (total) - 200MB
# DATASIZE specifies the number of blocks of 100 bytes each
DATASIZE=2000000
NUM_MAPS=4
# for running (total)
NUM_REDS=4

$HADOOP_EXECUTABLE jar $HADOOP_EXAMPLES_JAR teragen \
    -D $CONFIG_MAP_NUMBER=$NUM_MAPS \
    $DATASIZE $INPUT_HDFS

sleep 5

$HADOOP_EXECUTABLE jar $HADOOP_EXAMPLES_JAR terasort -D $CONFIG_REDUCER_NUMBER=$NUM_REDS $INPUT_HDFS $OUTPUT_HDFS