.PHONY: all clean hadoop-build

all: extraewrapper hadoop-build

extraewrapper: hadoop-src | deps/extrae
	make -C $(BASE_DIR)/extrae/java_wrapper/

deps/hadoop-1.0.3.tar.gz:
	curl -L --fail --progress-bar -o $(DEPS_DIR)/hadoop-1.0.3.tar.gz 'https://archive.apache.org/dist/hadoop/core/hadoop-1.0.3/hadoop-1.0.3.tar.gz'

hadoop-src: deps/hadoop-1.0.3.tar.gz
	@echo "f42d05e5c7bb43f85119546b80296435  deps/hadoop-1.0.3.tar.gz" | md5sum --status -c; \
		if [ $$? -ne 0 ] ; then \
			echo "File $(DEPS_DIR)/hadoop-1.0.3.tar.gz seems corrupt, deleting..."; \
			rm -f $(DEPS_DIR)/hadoop-1.0.3.tar.gz; \
			echo "Run make to try again"; \
		fi
	mkdir -p $(BASE_DIR)/hadoop-src
	tar xf $(DEPS_DIR)/hadoop-1.0.3.tar.gz --strip-components=1 -C $(BASE_DIR)/hadoop-src
	patch --directory=$(BASE_DIR)/hadoop-src --forward --reject-file=- -p1 < $(BASE_DIR)/patch/hadoop-extraewrapper-inject.patch
	patch --directory=$(BASE_DIR)/hadoop-src --forward --reject-file=- -p1 < $(BASE_DIR)/patch/hadoop-launch-classpath.patch
	patch --directory=$(BASE_DIR)/hadoop-src --forward --reject-file=- -p1 < $(BASE_DIR)/patch/hadoop-build-classpath.patch
    # Only needed because hadoop 1.0.3 has a syntax error and doesn't compile with java7
    # Starting from hadoop v1.1.0 it can be compiled with java7, this patch is the fix backported
    # https://issues.apache.org/jira/browse/HADOOP-8329
	patch --directory=$(BASE_DIR)/hadoop-src --forward --reject-file=- -p0 < $(BASE_DIR)/patch/hadoop-compile-java7.patch

deps/extrae:
	mkdir -p $(DEPS_DIR)/extrae
	tar xf $(DEPS_DIR)/extrae-3.0.1.tar.bz2 --strip-components=1 -C $(DEPS_DIR)/extrae
	( cd $(DEPS_DIR)/extrae/ ; ./configure --without-mpi --without-unwind --without-dyninst --without-papi --enable-dcarrera-hadoop --prefix=$(LOCAL_DIR)/ )
	make -C $(DEPS_DIR)/extrae/
	make -C $(DEPS_DIR)/extrae/ install

hadoop-build: hadoop-src extraewrapper
	ant -buildfile $(BASE_DIR)/hadoop-src/build.xml -Ddist.dir='$(BASE_DIR)/hadoop-build' -Dextraewrapper.lib.dir='$(LOCAL_DIR)/lib/' -Dskip.contrib=true -Dskip.compile-mapred-classes=true package

clean:
	rm -rf $(DEPS_DIR)/extrae
	rm -rf $(BASE_DIR)/hadoop-build
	rm -rf $(BASE_DIR)/hadoop-src

	@echo
	@if [ ! -d "$(LOCAL_DIR)/bin" -a ! -d "$(LOCAL_DIR)/etc" -a ! -d "$(LOCAL_DIR)/include" -a ! -d "$(LOCAL_DIR)/lib" -a ! -d "$(LOCAL_DIR)/share" ]; then \
		echo "All clean!"; \
	fi

	@if [ -d "$(LOCAL_DIR)/bin" -o -d "$(LOCAL_DIR)/etc" -o -d "$(LOCAL_DIR)/include" -o -d "$(LOCAL_DIR)/lib" -o -d "$(LOCAL_DIR)/share" ]; then \
		echo "Remember to delete the following folders to complelety remove the installation:"; \
		if [ -d "$(LOCAL_DIR)/bin" ]; then echo "$(LOCAL_DIR)/bin"; fi; \
		if [ -d "$(LOCAL_DIR)/etc" ]; then echo "$(LOCAL_DIR)/etc"; fi; \
		if [ -d "$(LOCAL_DIR)/include" ]; then echo "$(LOCAL_DIR)/include"; fi; \
		if [ -d "$(LOCAL_DIR)/lib" ]; then echo "$(LOCAL_DIR)/lib"; fi; \
		if [ -d "$(LOCAL_DIR)/share" ]; then echo "$(LOCAL_DIR)/share"; fi; \
	fi
