#JAVA_HOME from env_all.sh
#MS_HOME from env_all.sh
#EXTRAE_HOME from env_extrae.sh
#LOG4J from env_extrae.sh

export HADOOP_PREFIX=/home/smendoza/lightness/hadoop/hadoop-dist-dcarrera
export LOG4J_HOME=/home/smendoza/lightness/hadoop-apps/apache-log4j-1.2.17
export LOG4J_JAR=/home/smendoza/lightness/hadoop-apps/apache-log4j-1.2.17/log4j-1.2.17.jar
export LOG4J_CONFFILE=/home/smendoza/lightness/hadoopextrae/log4j.configuration
export JAVA_HOME=/home/smendoza/lightness/jdk/jdk1.7.0_55
export LD_LIBRARY_PATH=/home/smendoza/lightness/hadoop-apps/libpcap-1.4.0-dist/lib:/home/smendoza/lightness/hadoop-apps/extrae-2.5.1-dist/lib
export JAVAC=$(JAVA_HOME)/bin/javac
export JAVAH=$(JAVA_HOME)/bin/javah
export JAR=$(JAVA_HOME)/bin/jar
export JNI_H_DEST=$(PWD)/jni_c/include
export JAVA_INCLUDE=$(JAVA_HOME)/include/linux
export JAVA_INCLUDE2=$(JAVA_HOME)/include

#destino de las libs de jni_c, deberia ser local
export LIBDIR=/home/smendoza/lightness/lib
#export LIBDIR=/scratch/hdd/smendoza/lightness/lib

export LIB_PATH=$(LIBDIR)
export PCAP_HOME=/home/smendoza/lightness/hadoop-apps/libpcap-1.4.0-dist
export PCAP_INC=$(PCAP_HOME)/include
#export PCAP_LIBS=$(PCAP_HOME)/lib
export PCAP_LIBS=/usr/lib/x86_64-linux-gnu
export CC=gcc
export CPP=g++
export CFLAGS=-fPIC
export LIBS=

export EXTRAE_ON=1
export EXTRAE_DIR=/tmp/smendoza

export EXTRAE_HOME=/home/smendoza/lightness/hadoop-apps/extrae-2.5.1-dist
export EXTRAELIBS=$(EXTRAE_HOME)/lib
export EXTRAEINC=$(EXTRAE_HOME)/include
#export EXTRAELIB=pttrace
export EXTRAELIB=seqtrace
export PCAPLIB=pcap
export EXTRAE_SHARED_LIB=$(EXTRAE_HOME)/lib
export EXTRAE_WRAPPER_JAR=$(LIBDIR)/extraewrapper.jar
export EXTRAE_WRAPPER_SHARED_LIB=$(LIBDIR)

export UNDEF2PRV_JAR=$(LIBDIR)/undef2prv.jar

export HADOOP_LIBS=$(HADOOP_PREFIX)/build/hadoop-1.0.4-SNAPSHOT/hadoop-core-1.0.4-SNAPSHOT.jar

export JNI_JAVA_SRC=$(PWD)/jni_java

export DEBUG_JNI=-verbose:jni


.PHONY: jni_c jni_java

all: jni_c jni_java

# ************************
# Compilation
# ************************

jni_java: 
	make -C jni_java
jni_c: jni_java
	make -C jni_c

runtest: 
	echo $(JAVA_HOME)/bin/java $(DEBUG_JNI) -cp $(EXTRAE_WRAPPER_JAR) -Djava.library.path=$(EXTRAE_WRAPPER_SHARED_LIB):$(EXTRAE_SHARED_LIB):$(PCAP_LIBS) es.bsc.tools.extrae.Test
	$(JAVA_HOME)/bin/java $(DEBUG_JNI) -cp $(EXTRAE_WRAPPER_JAR) -Djava.library.path=$(EXTRAE_WRAPPER_SHARED_LIB):$(EXTRAE_SHARED_LIB):$(PCAP_LIBS) es.bsc.tools.extrae.Test
	#ºstrace -f $(JAVA_HOME)/bin/java $(DEBUG_JNI) -cp $(EXTRAE_WRAPPER_JAR) -Djava.library.path=$(EXTRAE_WRAPPER_SHARED_LIB):$(EXTRAE_SHARED_LIB) es.bsc.tools.extrae.Test

testextrae:
	gcc -I/home/smendoza/lightness/hadoop-apps/extrae-2.5.1-dist/include -I/home/smendoza/lightness/jdk/jdk1.7.0_55/include/linux -I/home/smendoza/lightness/jdk/jdk1.7.0_55/include -I/home/smendoza/lightness/hadoopextrae/extrae/java_wrapper/jni_c/include -fPIC test_extrae.c -o testextrae -L/home/smendoza/lightness/hadoop-apps/extrae-2.5.1-dist/lib -lseqtrace -L/lib -lpcap
runtt:
	./testextrae

convert:
	$(EXTRAE_HOME)/bin/mpi2prv -f $(EXTRAE_DIR)/TRACE.mpits

# ***************************
# Run
# ***************************

clean:
	make -C jni_java clean
	make -C jni_c clean
	rm -rf $(EXTRAE_DIR)/set-0
	#rm EXTRAE*
	rm $(LIBDIR)/*
