#!/bin/sh
# EXPORTS USER FOR CONFIG FILES AND WHEN RAN AS ROOT
export PCUSER=smendoza
export LNESS_HOME=/home/smendoza/lightness
export JAVA_HOME=$LNESS_HOME/jdk/jdk1.7.0_55
export LNESS_LIBS=$LNESS_HOME/lib
export LD_LIBRARY_PATH=$LNESS_HOME/lib
export LOG4J_HOME=$LNESS_HOME/hadoop-apps/apache-log4j-1.2.17
export LOG4J_JAR=$LOG4J_HOME/log4j-1.2.17.jar
export LOG4J_CONFFILE=$LNESS_HOME/hadoopextrae/log4j.undef2prv.properties
export TMP_PPING=/tmp/smendoza
export HADOOP_PREFIX=$LNESS_HOME/hadoop/hadoop-dist-dcarrera
export HADOOP_JAR_EXAMPLES=$HADOOP_PREFIX/build/hadoop-examples-1.0.4-SNAPSHOT.jar

export EXTRAE_HOME=$LNESS_HOME/hadoop-apps/extrae-2.5.1-dist

export CATALINA_HOME=/home/smendoza/lightness/hadoop-apps/apache-tomcat-6.0.4

export PATH=$HADOOP_PREFIX:$JAVA_HOME/bin:$EXTRAE_HOME/bin:$PATH
