grep true /tmp/undef2prv.log | cut -d':' -f7 | cut -d'(' -f1 > /tmp/ports_to_search.txt
bash port_counter.sh /tmp/ports_to_search.txt

