all: libjextrae sniffcmd
libjextrae:
	#$(CC) -I$(JAVA_INCLUDE) -I$(JNI_H_DEST) -I$(JAVA_HOME)/include -I$(PCAP_INC) -I$(EXTRAEINC) -L$(EXTRAELIBS) -L$(PCAP_LIBS) -l$(EXTRAELIB) -l$(PCAPLIB) $(CFLAGS) -shared  *.c packet-level/*.c -o $(LIBDIR)/libjextrae.so
	$(CC) -I$(EXTRAEINC) -I$(JAVA_INCLUDE) -I$(JAVA_INCLUDE2) -I$(JNI_H_DEST) $(CFLAGS) -shared  *.c packet-level/*.c -o $(LIBDIR)/libjextrae.so -L$(EXTRAELIBS) -l$(EXTRAELIB) -L$(PCAP_LIBS) -l$(PCAPLIB) -fPIC

#gcc -I/home/yolandab/jdk1.7.0_51/include/linux -I/home/yolandab/jdk1.7.0_51/include -I/home/yolandab/hadoop_tracing/hadoop_java_wrapper/jni_c/include -I -L/home/yolandab/hadoop_tracing/tracing_libs -fPIC -shared  *.c packet-level/*.c -o /home/yolandab/hadoop_tracing/tracing_libs/libjextrae.so -L/home/yolandab/hadoop_tracing/tracing_libs -lseqtrace -L/home/yolandab/hadoop_tracing/tracing_libs -lpcap
#extraetest:
#	gcc -I$(EXTRAEINC) -I$(JAVA_INCLUDE) -I$(JAVA_INCLUDE2) -I$(JNI_H_DEST) $(CFLAGS) test_extrae.c -o testextrae -L$(EXTRAELIBS) -l$(EXTRAELIB) -L$(PCAP_LIBS) -l$(PCAPLIB) -lpcap -lseqtrace
sniffcmd:
	gcc sniff_cmd.c -o /scratch/hdd/smendoza/bin/sniffer -I/home/smendoza/lightness/hadoopextrae/extrae/java_wrapper/jni_c/include -L/home/smendoza/lightness/lib -ljextrae -L/home/smendoza/lightness/hadoop-apps/extrae-2.5.1-dist/lib -lseqtrace -Wl,-rpath /home/smendoza/lightness/lib/ -Wl,-rpath /home/smendoza/lightness/hadoop-apps/extrae-2.5.1-dist/lib

clean:
	#smendoza rm -f $(JNI_H_DEST)/*
	rm -f $(LIBDIR)/libjes_bsc_tools_extrae_Wrapper.h

