while [ true ]
do
        ps -fA >> /tmp/ps_log.txt
	sleep 0.025
done
