#!/bin/bash
#arg1: file with one port each line

#cut -d':' -f2 /tmp/ports_to_search.txt | cut -d'(' -f1 > /tmp/ports_to_search.txt

grep WALA /aplic/smendoza/MS/dist/hadoop-dist/logs/* -R > /tmp/dumping.wala
grep 4dapid /aplic/smendoza/MS/dist/hadoop-dist/logs/* -R > /tmp/dumping.proc

rm /tmp/trashports.tmp
found=0
notfound=0
total=0
allports=''
arr_allports=()
while read line_port
do
	ports=`grep \$line_port /tmp/dumping.wala /tmp/dumping.proc | wc -l`
	if [ $ports -ge 1 ];
		then
			#echo "PORT FOUND $line"
			found=$(($found + 1))
		else
			echo "PORT NOT FOUND $line_port"
			lports=`grep \$line_port /tmp/dumping.undef2prv.output`
			echo $lports
			allports="$allports:$line_port"
			arr_allports+=($line_port)
			notfound=$(($notfound + 1))
			grep $line_port /tmp/mergeoutput.prv | head --lines=1 >> /tmp/trashports.tmp
			echo "grep $line_port /tmp/mergeoutput.prv | head --lines=1 >> /tmp/trashports.tmp"
	fi
	total=$(($total + 1))
done < $1

fails_50000=`grep ':50000:' /tmp/trashports.tmp | wc -l`
fails_50001=`grep ':50001:' /tmp/trashports.tmp | wc -l`
fails_50010=`grep ':50010:' /tmp/trashports.tmp | wc -l`
fails_50060=`grep ':50060:' /tmp/trashports.tmp | wc -l`

pack_all=`cat /tmp/mergeoutput.prv | wc -l`
echo "all packets: $pack_all"
for p in "${arr_allports[@]}"
do
	packs_p=`grep $p /tmp/mergeoutput.prv | wc -l`
	packs_porc=$(($packs_p * 100 / $pack_all))
	echo "port[$p]->$packs_p ($packs_porc%)"
done

echo "#FAILS 50000: $fails_50000"
echo "#FAILS 50001: $fails_50001"
echo "#FAILS 50010: $fails_50010"
echo "#FAILS 50060: $fails_50060"
echo "#PORTS FOUND: $found"
echo "#PORTS NOT FOUND: $notfound"
echo "#PORTS_total: $total"

echo $allports


