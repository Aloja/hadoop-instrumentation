# DEFAULT CONFIGURATION

export BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

export DEPS_DIR="${BASE_DIR}/deps"
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
export JAVAC="${JAVA_HOME}/bin/javac"
export JAVAH="${JAVA_HOME}/bin/javah"
export JAR="${JAVA_HOME}/bin/jar"
export JAVA_INCLUDE="${JAVA_HOME}/include/linux"
export JAVA_INCLUDE2="${JAVA_HOME}/include"
export LNESS_LIBS=$LNESS_HOME/lib
export LD_LIBRARY_PATH=$LNESS_HOME/lib
export TMP_PPING=/tmp/smendoza
export HADOOP_PREFIX="${BASE_DIR}/hadoop-build"
export HADOOP_JAR_EXAMPLES="${HADOOP_PREFIX}/hadoop-examples-*.jar"
export HADOOP_SRC="${BASE_DIR}/hadoop-src"

export JNI_H_DEST="${BASE_DIR}/extrae/java_wrapper/jni_c/include"

export LOG4J_CONFFILE="${BASE_DIR}/log4j.undef2prv.properties"

export CATALINA_HOME=/home/smendoza/lightness/hadoop-apps/apache-tomcat-6.0.4

export EXTRAE_LABELS="${BASE_DIR}/labels.txt"
