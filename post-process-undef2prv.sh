source env-postprocessing.sh

#cat /tmp/dumping-host-port-pid.tmp | eval sed 's/[^:]*/$(hostname --ip-address)/'2 > /tmp/dumping-host-port-pid

#clean log files
rm $TMP_PPING/undef2prv.log*

#don't know what those two lines are for
cp testsergio.* input/latest/
cp $TMP_PPING/dumping-host-port-pid* input/latest/

#update & compilation of the post-processing project
git pull origin smendoza
cd $LNESS_HOME/hadoopextrae/extrae/java_wrapper
make
cd $LNESS_HOME/hadoopextrae

#execute the undef2prv post-processing
java -cp $LOG4J_JAR:$LNESS_HOME/lib/undef2prv.jar es.bsc.tools.undef2prv.Undef2prv $TMP_PPING/distributed-merge/dumping-host-port-pid $TMP_PPING/mergeoutput.prv $TMP_PPING/mergeoutput.row

