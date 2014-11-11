#JAVA_HOME from env_all
#MS_HOME from env_all
#HADOOP_PREFIX from env_all
source $MS_HOME/hadoopextrae/env_all.sh &&\

export ANT_HOME=$MS_HOME/aplic/apache-ant-1.9.1
export PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$HADOOP_PREFIX/bin:$PATH
#export LD_LIBRARY_PATH=$MS_HOME/dist/lib:$MS_HOME/hadoopextrae/extrae/java_wrapper/lib:$MS_HOME/dist/pcap/lib
export LD_LIBRARY_PATH=$MS_HOME/lib:$HADOOP_PREFIX/lib/native:/aplic/smendoza/MS/dist/pcap/lib:/aplic/smendoza/MS/dist/extrae-dist/lib
#librerias con jar del javawrapper
export HADOOP_CLASSPATH=$MS_HOME/lib
export HADOOP_USER_CLASSPATH_FIRST=true
export HADOOP_JAR_EXAMPLES=$HADOOP_PREFIX/build/hadoop-1.0.4-SNAPSHOT/hadoop-examples-1.0.4-SNAPSHOT.jar
export HADOOP_CONF_DIR=/aplic/smendoza/MS/dist/hadoop-dist/conf

#Set those vars at  bin/hadoop-daemon.sh
#export HADOOP_ROOT_LOGGER="OFF"
#export HADOOP_SECURITY_LOGGER="OFF"
#export HDFS_AUDIT_LOGGER="OFF"

export EXTRAE_ON=1
export EXTRAE_DIR=/tmp
export EXTRAE_HOME=$MS_HOME/dist/extrae-dist
export EXTRAE_LABELS=$MS_HOME/labels.txt

#sudo setcap cap_net_raw,cap_net_admin=eip /home/dcarrera/MS/aplic/jdk1.7.0_25/bin/java
