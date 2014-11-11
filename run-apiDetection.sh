source env-apiDetection.sh

MY_LOCAL_OUTPUT=~/lightness/local/tmp/output

#rm $MY_LOCAL_OUTPUT -R
#mkdir $MY_LOCAL_OUTPUT

actualdir=$PWD
cd ~/lightness/hadoop-apps/apiDetection

: '
$HADOOP_PREFIX/bin/stop-all.sh

echo "### CLEANING HADOOP CLUSTER ###########################"
#Limpiando hadoop cluster y temporales en distribuido...
while read node
do
ssh $node <<ENDSSH
rm -rf $TMP_PPING/*
rm $HADOOP_PREFIX/hs_err_pid*.log
rm -rf $HADOOP_PREFIX/logs/*
ENDSSH
done < $HADOOP_PREFIX/conf/slaves

$HADOOP_PREFIX/bin/start-all.sh

#movind data to HDFS
dst_HDFS=/user/smendoza/common-crawl/parse-output
src_HDFS=/scratch/hdd/yolandab/common-crawl/parse-output/segment
hadoop fs -mkdir $dst_HDFS
hadoop fs -copyFromLocal $src_HDFS $dst_HDFS
'
#Data Preparation
#Data Preparation - Create Behemoth Docs out of the Common Crawl Dataset
hadoop fs -rmr Behemoth-CC
hadoop jar ./behemoth-commoncrawl-1.1-SNAPSHOT-job.jar com.digitalpebble.behemoth.commoncrawl.CommonCrawlConverterJob2012 hdfs://minerva-1:50000/user/smendoza/common-crawl/parse-output/segment/1346823845675/*.arc.gz Behemoth-CC
hadoop fs -copyToLocal Behemoth-CC MY_LOCAL_OUTPUT

: '
#Data Preparation - Filtering the URLs
hadoop fs -copyFromLocal /home/smendoza/lightness/hadoop-apps/apiDetection/annotatedUrls.txt /user/smendoza/annotatedUrls.txt
hadoop fs -rmr Behemoth-CC-Training
hadoop jar ./behemoth-core-1.1-SNAPSHOT-job.jar com.digitalpebble.behemoth.util.CorpusFilter -D document.filter.urlset.keep=hdfs://minerva-1:50000/user/smendoza/annotatedUrls.txt -i Behemoth-CC -o Behemoth-CC-Training
hadoop fs -copyToLocal Behemoth-CC-Training $MY_LOCAL_OUTPUT

#Training
#Training - Tika processing (Optional) .
hadoop fs -rmr Behemoth-CC-Training-Tika
hadoop jar ./behemoth-tika-1.1-SNAPSHOT-job.jar com.digitalpebble.behemoth.tika.TikaDriver -i Behemoth-CC-Training -o Behemoth-CC-Training-Tika
hadoop fs -copyToLocal Behemoth-CC-Training-Tika $MY_LOCAL_OUTPUT

#Training - Include the training set annotations in the Behemoth dataset
#Puerto de Apache-tomcat-2.6 en $CATALINA_HOME/conf/server.xml
hadoop jar ./cc-processor-0.0.2-SNAPSHOT-job.jar uk.ac.open.kmi.iserve.crawler.TrainingSetGenerator -D sparqlEndpoint=http://minerva-1:8013/openrdf-sesame/repositories/Validator Behemoth-CC-Training-Tika Behemoth-CC-Training-Tika-Tagged
hadoop fs -copyToLocal Behemoth-CC-Training-Tika-Tagged $MY_LOCAL_OUTPUT

#Training - Update the Key of every Document to Include the Label used for Classification
hadoop fs -rmr Behemoth-CC-Training-Labelled
hadoop jar ./cc-processor-0.0.2-SNAPSHOT-job.jar uk.ac.open.kmi.iserve.crawler.BehemothKeyModifier Behemoth-CC-Training-Tika-Tagged Behemoth-CC-Training-Labelled iserve.documentType
hadoop fs -copyToLocal Behemoth-CC-Training-Labelled $MY_LOCAL_OUTPUT

#Training - Check pages have been annotated (optional — just to verify things are OK)
hadoop jar ./behemoth-core-1.1-SNAPSHOT-job.jar com.digitalpebble.behemoth.util.CorpusReader -i Behemoth-CC-Training-Labelled -m 

#Training - Now do the actual training
hadoop fs -rmr cc-mahout-vectors
hadoop jar ./behemoth-mahout-1.1-SNAPSHOT-job.jar com.digitalpebble.behemoth.mahout.SparseVectorsFromBehemoth -i Behemoth-CC-Training-Labelled -o cc-mahout-vectors -label iserve.documentType 
#Training - Split into Training and Testing
hadoop fs -rmr cc-test-vectors
mahout split -i cc-mahout-vectors/tfidf-vectors --trainingOutput cc-train-vectors --testOutput cc-test-vectors --randomSelectionPct 40 --overwrite --sequenceFiles -xm sequential 
#mahout split -i cc-mahout-vectors/tfidf-vectors --trainingOutput cc-train-vectors --testOutput cc-test-vectors --overwrite --sequenceFiles -xm mapreduce --randomSelectionPct 25 --mapRedOutputDir cc-test-vectors

#Training - Train with Naive Bayes
hadoop fs -rmr nb-test-model
mahout trainnb -i cc-train-vectors -el -li nb-label-index -o nb-test-model -ow -c

#Training - Test the Classifier
hadoop fs -rmr nb-testing
mahout testnb -i cc-test-vectors -m nb-test-model -l nb-label-index -ow -o nb-testing -c
'
cd $actualdir

export CATALINA_HOME=/home/smendoza/lightness/hadoop-apps/apache-tomcat-6.0.41
