#source /home/$USER/MS/env_all.sh &&\
source $MS_HOME/hadoopextrae/env_all.sh &&\
export ANT_HOME=$MS_HOME/aplic/apache-ant-1.9.1
export PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$MS_HOME/extrae/m4/bin:$MS_HOME/extrae/autoconf/bin:$MS_HOME/extrae/automake/bin:$PATH

export EXTRAE_ON=1
export EXTRAE_DIR=/tmp
#export EXTRAE_HOME=$MS_HOME/extrae/dist
export EXTRAE_HOME=$MS_HOME/dist/extrae-dist
export EXTRAE_LABELS=$MS_HOME/hadoopextrae/labels.txt
export LOG4J_HOME=/aplic/smendoza/jincludes/apache-log4j-1.2.17
export LOG4J_JAR=$LOG4J_HOME/log4j-1.2.17.jar
export LOG4J_CONFFILE=$MS_HOME/hadoopextrae/log4j.configuration
export SIESTA=50000
export LD_LIBRARY_PATH=$EXTRAE_HOME/lib

#sudo setcap cap_net_raw,cap_net_admin=eip /home/dcarrera/MS/aplic/jdk1.7.0_25/bin/java
