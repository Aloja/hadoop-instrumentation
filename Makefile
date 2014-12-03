BASE_DIR := $(abspath $(dir $(CURDIR)/$(word $(words $(MAKEFILE_LIST)),$(MAKEFILE_LIST))))
DEPS_DIR := $(BASE_DIR)/deps
LOCAL_DIR := $(BASE_DIR)/local

.PHONY: all clean binutils-bfd binutils-libiberty extrae hadoop-build

all: extraewrapper

extraewrapper: hadoop-src binutils-bfd binutils-libiberty extrae deps/libpcap
	# Use java7 to compile (extraewrapper needs it)
	sudo update-alternatives --set java /usr/lib/jvm/java-7-oracle/jre/bin/java
	make -C $(BASE_DIR)/extrae/java_wrapper/

hadoop-src:
	mkdir -p $(BASE_DIR)/hadoop-src
	tar xf $(DEPS_DIR)/hadoop-1.0.3.tar.gz --strip-components=1 -C $(BASE_DIR)/hadoop-src
	patch --directory=$(BASE_DIR)/hadoop-src --forward --reject-file=- -p1 < $(BASE_DIR)/hadoop-src-changes.patch
	patch --directory=$(BASE_DIR)/hadoop-src --forward --reject-file=- -p1 < $(BASE_DIR)/hadoop_vanilla_to_compile.patch
	sed -i'' 's|<target name="package" depends="compile, jar, javadoc, docs, cn-docs, api-report, examples, tools-jar, jar-test, ant-tasks, package-librecordio"|<target name="package" depends="compile, jar, javadoc, api-report, examples, tools-jar, jar-test, ant-tasks, package-librecordio"|' $(BASE_DIR)/hadoop-src/build.xml

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
	ant -buildfile hadoop-src/build.xml -Ddist.dir='$(BASE_DIR)/hadoop-build' -Dskip.compile-mapred-classes=true package

clean:
	rm -rf $(DEPS_DIR)/binutils
	rm -rf $(DEPS_DIR)/extrae
	rm -rf $(DEPS_DIR)/libpcap
	rm -rf $(BASE_DIR)/hadoop-build
	rm -rf $(BASE_DIR)/hadoop-src
	rm -rf $(LOCAL_DIR)/bin
	rm -rf $(LOCAL_DIR)/etc
	rm -rf $(LOCAL_DIR)/include
	rm -rf $(LOCAL_DIR)/lib
	rm -rf $(LOCAL_DIR)/share
