HADOOP_EXECUTABLE="$HADOOP_PREFIX/bin/hadoop"
DATATOOLS="$HADOOP_JOBS/lib/datatools.jar"
DIR="$HADOOP_JOBS/lib"

DATA_HDFS=/HiBench
PAGERANK_INPUT="Input"
PAGERANK_OUTPUT="Output"
PAGERANK_BASE_HDFS=${DATA_HDFS}/Pagerank
INPUT_HDFS=${PAGERANK_BASE_HDFS}/${PAGERANK_INPUT}
OUTPUT_HDFS=${PAGERANK_BASE_HDFS}/${PAGERANK_OUTPUT}

# for prepare
PAGES=500
NUM_MAPS=4
NUM_REDS=4

# for running
NUM_ITERATIONS=3

# generate data
#DELIMITER=\t
OPTION="-t pagerank \
	-b ${PAGERANK_BASE_HDFS} \
	-n ${PAGERANK_INPUT} \
	-m ${NUM_MAPS} \
	-r ${NUM_REDS} \
	-p ${PAGES} \
        -pbalance -pbalance \
	-o text"

#	-d ${DELIMITER} \
$HADOOP_EXECUTABLE jar ${DATATOOLS} HiBench.DataGen ${OPTION}

sleep 5

OPTION="${INPUT_HDFS}/edges ${OUTPUT_HDFS} ${PAGES} ${NUM_REDS} ${NUM_ITERATIONS} nosym new"
$HADOOP_EXECUTABLE jar ${DIR}/pegasus-2.0.jar pegasus.PagerankNaive $OPTION