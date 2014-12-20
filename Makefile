BASE_DIR := $(abspath $(dir $(CURDIR)/$(word $(words $(MAKEFILE_LIST)),$(MAKEFILE_LIST))))
DEPS_DIR := $(BASE_DIR)/deps
LOCAL_DIR := $(HOME)/instrumentation

.PHONY: all clean binutils-bfd binutils-libiberty extrae hadoop-build

all: extraewrapper hadoop-build

extraewrapper: hadoop-src binutils-bfd binutils-libiberty extrae deps/libpcap
	# Use java7 to compile (extraewrapper needs it)
	sudo update-alternatives --set java /usr/lib/jvm/java-7-oracle/jre/bin/java
	make -C $(BASE_DIR)/extrae/java_wrapper/

hadoop-src:
	mkdir -p $(BASE_DIR)/hadoop-src
	tar xf $(DEPS_DIR)/hadoop-1.0.3.tar.gz --strip-components=1 -C $(BASE_DIR)/hadoop-src
	patch --directory=$(BASE_DIR)/hadoop-src --forward --reject-file=- -p1 < $(BASE_DIR)/patch/hadoop-extraewrapper-inject.patch
	patch --directory=$(BASE_DIR)/hadoop-src --forward --reject-file=- -p1 < $(BASE_DIR)/patch/hadoop-launch-classpath.patch
	patch --directory=$(BASE_DIR)/hadoop-src --forward --reject-file=- -p1 < $(BASE_DIR)/patch/hadoop-build-classpath.patch

deps/binutils:
	mkdir -p $(DEPS_DIR)/binutils
	tar xf $(DEPS_DIR)/binutils-2.23.tar.gz --strip-components=1 -C $(DEPS_DIR)/binutils

deps/binutils/bfd/Makefile: deps/binutils
	( cd $(DEPS_DIR)/binutils/bfd/ ; ./configure --prefix=$(LOCAL_DIR)/ --enable-shared=yes )

binutils-bfd: deps/binutils/bfd/Makefile
	make -C $(DEPS_DIR)/binutils/bfd/
	make -C $(DEPS_DIR)/binutils/bfd/ install

deps/binutils/libiberty/Makefile: deps/binutils
	( cd $(DEPS_DIR)/binutils/libiberty/ ; CFLAGS=-fPIC ./configure --prefix=$(LOCAL_DIR)/ )

binutils-libiberty: deps/binutils/libiberty/Makefile
	make -C $(DEPS_DIR)/binutils/libiberty/
	make -C $(DEPS_DIR)/binutils/libiberty/ install

deps/extrae:
	mkdir -p $(DEPS_DIR)/extrae
	tar xf $(DEPS_DIR)/extrae-2.5.1.tar --strip-components=1 -C $(DEPS_DIR)/extrae

deps/extrae/Makefile: deps/extrae
	( cd $(DEPS_DIR)/extrae/ ; ./configure --without-mpi --without-unwind --without-dyninst --without-papi --with-binutils=$(LOCAL_DIR) --prefix=$(LOCAL_DIR)/ )

extrae: deps/extrae/Makefile
	make -C $(DEPS_DIR)/extrae/
	make -C $(DEPS_DIR)/extrae/ install

deps/libpcap:
	mkdir -p $(DEPS_DIR)/libpcap
	tar xf $(DEPS_DIR)/libpcap-1.4.0.tar.gz --strip-components=1 -C $(DEPS_DIR)/libpcap

hadoop-build: hadoop-src
	# Use java6 to compile hadoop (starting from hadoop v1.1.0 it can be compiled with java7, but not before)
	# https://issues.apache.org/jira/browse/HADOOP-8329
	sudo update-alternatives --set java /usr/lib/jvm/java-6-oracle/jre/bin/java
	ant -buildfile $(BASE_DIR)/hadoop-src/build.xml -Ddist.dir='$(BASE_DIR)/hadoop-build' -Dskip.compile-mapred-classes=true -Dextraewrapper.lib.dir='$(LOCAL_DIR)/lib/' package

clean:
	rm -rf $(DEPS_DIR)/binutils
	rm -rf $(DEPS_DIR)/extrae
	rm -rf $(DEPS_DIR)/libpcap
	rm -rf $(BASE_DIR)/hadoop-build
	rm -rf $(BASE_DIR)/hadoop-src
	@echo
	@echo Remember to delete the following folders to complelety remove the installation:
	@echo $(LOCAL_DIR)/bin
	@echo $(LOCAL_DIR)/etc
	@echo $(LOCAL_DIR)/include
	@echo $(LOCAL_DIR)/lib
	@echo $(LOCAL_DIR)/share
