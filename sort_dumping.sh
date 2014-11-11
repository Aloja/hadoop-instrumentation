echo PID:IP:PORT
sort -t":" -k 3,3 -u  /tmp/dumping-host-port-pid  | sort -t":" -k 2,3
