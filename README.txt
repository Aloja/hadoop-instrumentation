QUICK START
-----------
Install vagrant (https://www.vagrantup.com/) and virtualbox (https://www.virtualbox.org/), clone this repo, go to the "vagrant" folder and run the command "vagrant up". This will create two virtual machines (one Hadoop master and one Hadoop slave). To access the machines, execute "vagrant ssh" (for the master) and "vagrant ssh slave-1" for the slave. Inside the machines, the repo is located in $HOME/workspace and is shared between both machines (all the changes are visible at the same time).

The script make.sh calls a Makefile (accepts the same parameters: all, clean, ...) to compile Hadoop and all its dependencies. It will create three folders:

- /hadoop-src: the original Hadoop source with the applied patches (located in /patch).
- /hadoop-build: Hadoop built from /hadoop-src (with the Extrae injection included).
- $HOME/instrumentation: binaries and libraries needed during execution and post-process steps. It's outside the repo folder (/vagrant/workspace) because the mount point that vagrant creates is incompatible with some permissions.

The next step is to copy some folders to all the nodes of the Hadoop cluster. When using vagrant, the repo folder is shared between all the nodes so no need to copy it, but the folder $HOME/instrumentation has to be copied manually (it only changes when recompiling).

Once everything is in place, the script run.sh runs all the steps needed for the instrumented execution of Hadoop. By default will launch a grep job (located in /jobs/default.sh), but a different job can be specified as a parameter. For example "./run.sh sort" will launch a sort job located in /jobs/sort.sh

After the execution is done, every node has the traces generated stored locally. The final step is to run the script post-process.sh that will copy the traces from all the nodes, merge them in a single file, and fix some things (like synchronization and complete communications). The output directory is /traces, with the definitive file named /traces/mergeoutput-out.prv that can be opened with Paraver. Some Paraver custom views are already configured inside the /CFGs folder.


CONFIGURATION
-------------
Some scripts need environment variables with different paths set (like make.sh and post-process.sh). All these scripts load the file /vars.sh, that will load /vars-default.sh (with all the default options) and, if found, the file /vars-local.sh (that is ignored from version control). If some option needs to be overriden, just create that file and put it there.


PATCHES
-------
- hadoop-compile-java7.patch: Hadoop 1.0.3 has an error and doesn't compile with java7, this patch fixes it.
- hadoop-extraewrapper-inject.patch: injects Extrae events inside the Hadoop code.
- hadoop-extraewrapper-inject.patch.withwalas: old version from Sergio, not used anymore.
- hadoop-build-classpath.patch: adds extraewrapper.jar (with the Extrae code to generate the events) to the build process. Also removes some unnecessary build targets to speed up compilation time.
- hadoop-launch-classpath.patch: adds extrae libraries to java library path for execution.


TIPS
----

If the generated trace is very big, the post-process.sh step might run out of memory. Open the script and change in the last line the value of "-Xmx1536m".

When running in the minerva cluster, the sniffer binary needs special permissions. There is a command (sudo setcap_sniffer) that will set these special permissions to the file /scratch/hdd/jcugat/instrumentation/bin/sniffer (it has to be run in every node).


ALOJA
-----
For some weird reason, the sniffer binary remembers the location of the libraries at compilation time, and ignores the LD_LIBRARY_PATH environment variable.

Also tried statically compilling the sniffer so there is no need for external libraries (there is a branch with this work), but unfortunately the extrae events of the sniffer were not saved to the trace.

The solution is to create the desired path (/scratch/local/aplic/instrumentation/) and modify the variable LOCAL_DIR inside vars-default.sh to point to this path, so that when executing in Aloja machines the path of the sniffer binary and libraries is the same.

Also, the ubuntu versions are different (the instrumentation repo uses 14.04 and Aloja repo 12.04), so the binaries and libraries compiled in one might not work in the other. The best way is to compile all the binaries and libs (./make.sh extraewrapper) inside the Aloja vagrant (ubuntu 12.04) with the modified path.
