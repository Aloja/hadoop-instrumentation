for port in 22 894 
do
	mv nova.prv nova_dirty.prv
	grep -v ":77773:$port:" nova_dirty.prv | grep -v ":77771:$port" >nova.prv
	echo "cleaning port $port"
done
cp nova.prv latest.prv
