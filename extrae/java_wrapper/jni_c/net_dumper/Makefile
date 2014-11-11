all:
	$(CC) -I../include $(CFLAGS) -I/usr/include/glib-2.0 `pkg-config --cflags glib-2.0` *.c -lglib-2.0 -pthread -o ../../bin/net_dumper

clean:
	rm -f ./exec/net_dumper
