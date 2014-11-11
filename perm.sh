while read node
do
ssh root@$node <<ENDSSH
chown -R $PCUSER: /tmp/set-*
chown -R $PCUSER: /tmp/dumping*
chown -R $PCUSER: /tmp/info.txt
chown -R $PCUSER: /tmp/foo1 -R
ENDSSH
done < $HADOOP_PREFIX/conf/slaves

