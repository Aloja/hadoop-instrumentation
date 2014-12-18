#!/usr/bin/env bash
set -o errexit  # Exit immediately on non-zero status
set -o nounset  # Treat unset variables as an error
set -o xtrace   # Debug mode: display the command and its expanded arguments

source "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/env-execution.sh

#while read node
#do
#ssh $node <<ENDSSH

#rm -rf $TMP_PPING/foo1
#mkdir $TMP_PPING/foo1

#for f in \`ls $TMP_PPING |grep set\`
#do
#        cp $TMP_PPING/\$f/* $TMP_PPING/foo1/
#done

#for f in \`ls $TMP_PPING/foo1\`
#do
#        if [ ! -z "${command}" ]; 
#        then 
#                command="$command  -- ";
#        fi;
#        
#        command="$command  $TMP_PPING/foo1/$f";
#
#done

#ENDSSH
#done < $HADOOP_PREFIX/conf/slaves

#get extrae traces from hadoop nodes
rm -f $TMP_PPING/distributed-merge/TRACE.mpits
while read node
do
rm -rf $TMP_PPING/distributed-merge/$node
mkdir -p $TMP_PPING/distributed-merge/$node
scp -r $node:$TMP_PPING/set-* $TMP_PPING/distributed-merge/$node
scp -r $node:$TMP_PPING/TRACE.mpits $TMP_PPING/distributed-merge/$node
for f in $TMP_PPING/distributed-merge/$node/set-*/*mpit
do
echo $f on minerva-$node named>> $TMP_PPING/distributed-merge/TRACE.mpits
echo "--" >> $TMP_PPING/distributed-merge/TRACE.mpits
done
done < $HADOOP_PREFIX/conf/slaves

#quito el ultimo -- para que mpi2prv no me de error
cp $TMP_PPING/distributed-merge/TRACE.mpits $TMP_PPING/distributed-merge/TRACE.mpits.tmp
head -n -1 $TMP_PPING/distributed-merge/TRACE.mpits.tmp > $TMP_PPING/distributed-merge/TRACE.mpits
rm $TMP_PPING/distributed-merge/TRACE.mpits.tmp

#Generacion de todos los mpits con el TRACE.mpits separados por apps
${BIN_DIR}/mpi2prv -syn -f $TMP_PPING/distributed-merge/TRACE.mpits -o $TMP_PPING/mergeoutput.prv

: '
rm $TMP_PPING/distributed-merge/TRACE*.mpit
rm $TMP_PPING/distributed-merge/TRACE*.sym
while read node
do
scp $node:$TMP_PPING/foo1/* $TMP_PPING/distributed-merge/
done < $HADOOP_PREFIX/conf/slaves
'
: '
#Generacion de todos los .mpit directamente pasados como args al mpi2prv
#echo "$EXTRAE_HOME/bin/mpi2prv `ls  $TMP_PPING/distributed-merge/*.mpit` -o $TMP_PPING/mergeoutput2.prv >& $TMP_PPING/info.txt"
$EXTRAE_HOME/bin/mpi2prv -syn `ls  $TMP_PPING/distributed-merge/*/set-*/*.mpit` -o $TMP_PPING/mergeoutput2.prv >& $TMP_PPING/info.txt

#Generacion de los .prv por separado de cada nodo
rm /tmp/smendoza/distributed-merge/losnevents.tmp
for f in `ls -d $TMP_PPING/distributed-merge/minerva* | grep minerva`
do
$EXTRAE_HOME/bin/mpi2prv -f $f/TRACE.mpits -o $f/mergeoutput.prv >& $f/info.txt
grep 77770 $f/mergeoutput.prv >> /tmp/smendoza/distributed-merge/losnevents.tmp
done
' 





