#!/bin/sh
# EXPORTS USER FOR CONFIG FILES AND WHEN RAN AS ROOT

export BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

export PCUSER=smendoza
export LNESS_HOME=${BASE_DIR}
export JAVA_HOME=$(readlink -f /usr/bin/java | sed "s:/bin/java::" | sed "s:/jre::")
export LNESS_LIBS=$LNESS_HOME/lib
export LD_LIBRARY_PATH=$LNESS_HOME/lib
export LOG4J_HOME=$LNESS_HOME/hadoop-apps/apache-log4j-1.2.17
export LOG4J_JAR=$LOG4J_HOME/log4j-1.2.17.jar
export LOG4J_CONFFILE=$LNESS_HOME/hadoopextrae/log4j.undef2prv.properties
export TMP_PPING=/tmp/smendoza
export HADOOP_PREFIX="${BASE_DIR}/hadoop-build"
export HADOOP_JAR_EXAMPLES="${HADOOP_PREFIX}/hadoop-examples-*.jar"

export EXTRAE_HOME=$LNESS_HOME/hadoop-apps/extrae-2.5.1-dist

export CATALINA_HOME=/home/smendoza/lightness/hadoop-apps/apache-tomcat-6.0.4

export PATH=$HADOOP_PREFIX:$JAVA_HOME/bin:$EXTRAE_HOME/bin:$PATH
