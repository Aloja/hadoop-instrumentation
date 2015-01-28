
all:	extrae_jar undef2prv_jar

extrae_wrapper:
	$(JAVAC) -cp $(HADOOP_LIBS) es/bsc/tools/extrae/*.java

undef2prv:
	$(JAVAC) -cp libs/log4j-1.2.17.jar es/bsc/tools/undef2prv/*.java

javah:  extrae_wrapper
	$(JAVAH) -d $(JNI_H_DEST) -jni es.bsc.tools.extrae.Wrapper

extrae_jar: javah
	$(JAR) cvf $(EXTRAE_WRAPPER_JAR) es/bsc/tools/extrae/*.class

undef2prv_jar: undef2prv
	$(JAR) cvf $(UNDEF2PRV_JAR) es/bsc/tools/undef2prv/*.class
	cp libs/log4j-1.2.17.jar $(LIBDIR)

clean:
	rm -f es/bsc/tools/extrae/*.class
	rm -f es/bsc/tools/undef2prv/*.class
	rm -f $(EXTRAE_WRAPPER_JAR)
	rm -f $(UNDEF2PRV_JAR)

