 for i in `cat tmp2`; do grep ":77771:$i" /tmp/mergeoutput.prv |cut -d: -f14|sort -u ; done >salida
sort -u salida

