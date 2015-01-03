#!/bin/sh
# EXPORTS USER FOR CONFIG FILES AND WHEN RAN AS ROOT

export BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

export LOCAL_DIR="${HOME}/instrumentation"
export BIN_DIR="${LOCAL_DIR}/bin"
export LIB_DIR="${LOCAL_DIR}/lib"

export CONFIG_DIR="${BASE_DIR}/config"
export CONFIG_HADOOP="${CONFIG_DIR}/hadoop-conf"

export SNIFFER_BIN="${BIN_DIR}/sniffer"

export PCUSER=smendoza
export LNESS_HOME=${BASE_DIR}
export JAVA_HOME=$(readlink -f /usr/bin/java | sed "s:/bin/java::" | sed "s:/jre::")
export JAVA="${JAVA_HOME}/bin/java"
export LNESS_LIBS=$LNESS_HOME/lib
export LD_LIBRARY_PATH=$LNESS_HOME/lib
export TMP_PPING=/tmp/smendoza
export HADOOP_PREFIX="${BASE_DIR}/hadoop-build"
export HADOOP_JAR_EXAMPLES="${HADOOP_PREFIX}/hadoop-examples-*.jar"

export LOG4J_JAR="${LOCAL_DIR}/lib/log4j-1.2.17.jar"
export LOG4J_CONFFILE="${BASE_DIR}/log4j.undef2prv.properties"

export CATALINA_HOME=/home/smendoza/lightness/hadoop-apps/apache-tomcat-6.0.4
