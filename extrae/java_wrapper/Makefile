#JAVA_HOME from env_all.sh
#MS_HOME from env_all.sh
#EXTRAE_HOME from env_extrae.sh
#LOG4J from env_extrae.sh

# Some compiled binaries will be placed here
# For example, the sniffer util from java_wrapper
export BINDIR=$(LOCAL_DIR)/bin

#destino de las libs de jni_c, deberia ser local
export LIBDIR=$(LOCAL_DIR)/lib
#export LIBDIR=/scratch/hdd/smendoza/lightness/lib

export LIB_PATH=$(LIBDIR)
export PCAP_HOME=$(BASE_DIR)/deps/libpcap
export PCAP_INC=$(PCAP_HOME)/include
#export PCAP_LIBS=$(PCAP_HOME)/lib
export PCAP_LIBS=/usr/lib/x86_64-linux-gnu
export CC=gcc
export CPP=g++
export CFLAGS=-fPIC
export LIBS=

export EXTRAE_HOME=$(BASE_DIR)/deps/extrae
export EXTRAELIBS=$(LIBDIR)
export EXTRAEINC=$(LOCAL_DIR)/include
#export EXTRAELIB=pttrace
export EXTRAELIB=seqtrace
export PCAPLIB=pcap
export EXTRAE_SHARED_LIB=$(EXTRAE_HOME)/lib
export EXTRAE_WRAPPER_JAR=$(LIBDIR)/extraewrapper.jar
export EXTRAE_WRAPPER_SHARED_LIB=$(LIBDIR)

export UNDEF2PRV_JAR=$(LIBDIR)/undef2prv.jar

export HADOOP_LIBS=$(HADOOP_SRC)/hadoop-core-1.0.3.jar

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
	#strace -f $(JAVA_HOME)/bin/java $(DEBUG_JNI) -cp $(EXTRAE_WRAPPER_JAR) -Djava.library.path=$(EXTRAE_WRAPPER_SHARED_LIB):$(EXTRAE_SHARED_LIB) es.bsc.tools.extrae.Test

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
