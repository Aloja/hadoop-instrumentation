BASE_DIR := $(abspath $(dir $(CURDIR)/$(word $(words $(MAKEFILE_LIST)),$(MAKEFILE_LIST))))
DEPS_DIR := $(BASE_DIR)/deps
LOCAL_DIR := $(HOME)/instrumentation

.PHONY: all clean hadoop-build

all: extraewrapper hadoop-build

extraewrapper: hadoop-src | deps/binutils deps/libpcap deps/extrae
	make -C $(BASE_DIR)/extrae/java_wrapper/

hadoop-src:
	mkdir -p $(BASE_DIR)/hadoop-src
	tar xf $(DEPS_DIR)/hadoop-1.0.3.tar.gz --strip-components=1 -C $(BASE_DIR)/hadoop-src
	patch --directory=$(BASE_DIR)/hadoop-src --forward --reject-file=- -p1 < $(BASE_DIR)/patch/hadoop-extraewrapper-inject.patch
	patch --directory=$(BASE_DIR)/hadoop-src --forward --reject-file=- -p1 < $(BASE_DIR)/patch/hadoop-launch-classpath.patch
	patch --directory=$(BASE_DIR)/hadoop-src --forward --reject-file=- -p1 < $(BASE_DIR)/patch/hadoop-build-classpath.patch
    # Only needed because hadoop 1.0.3 has a syntax error and doesn't compile with java7
    # Starting from hadoop v1.1.0 it can be compiled with java7, this patch is the fix backported
    # https://issues.apache.org/jira/browse/HADOOP-8329
	patch --directory=$(BASE_DIR)/hadoop-src --forward --reject-file=- -p0 < $(BASE_DIR)/patch/hadoop-compile-java7.patch

deps/binutils:
	mkdir -p $(DEPS_DIR)/binutils
	tar xf $(DEPS_DIR)/binutils-2.23.tar.gz --strip-components=1 -C $(DEPS_DIR)/binutils

    # bfd
	( cd $(DEPS_DIR)/binutils/bfd/ ; ./configure --prefix=$(LOCAL_DIR)/ --enable-shared=yes )
	make -C $(DEPS_DIR)/binutils/bfd/
	make -C $(DEPS_DIR)/binutils/bfd/ install

    # libiberty
	( cd $(DEPS_DIR)/binutils/libiberty/ ; CFLAGS=-fPIC ./configure --prefix=$(LOCAL_DIR)/ )
	make -C $(DEPS_DIR)/binutils/libiberty/
	make -C $(DEPS_DIR)/binutils/libiberty/ install

deps/extrae:
	mkdir -p $(DEPS_DIR)/extrae
	tar xf $(DEPS_DIR)/extrae-2.5.1.tar --strip-components=1 -C $(DEPS_DIR)/extrae
	( cd $(DEPS_DIR)/extrae/ ; ./configure --without-mpi --without-unwind --without-dyninst --without-papi --with-binutils=$(LOCAL_DIR) --prefix=$(LOCAL_DIR)/ )
	make -C $(DEPS_DIR)/extrae/
	make -C $(DEPS_DIR)/extrae/ install

deps/libpcap:
	mkdir -p $(DEPS_DIR)/libpcap
	tar xf $(DEPS_DIR)/libpcap-1.4.0.tar.gz --strip-components=1 -C $(DEPS_DIR)/libpcap

hadoop-build: hadoop-src
	ant -buildfile $(BASE_DIR)/hadoop-src/build.xml -Ddist.dir='$(BASE_DIR)/hadoop-build' -Dextraewrapper.lib.dir='$(LOCAL_DIR)/lib/' -Dskip.compile-mapred-classes=true package

clean:
	rm -rf $(DEPS_DIR)/binutils
	rm -rf $(DEPS_DIR)/extrae
	rm -rf $(DEPS_DIR)/libpcap
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
