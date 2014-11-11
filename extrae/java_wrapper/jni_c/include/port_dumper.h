/* 
 * File:   net_dumper.h
 * Author: smendoza
 *
 * Created on 20 de septiembre de 2013, 12:25
 */

#include <pcap.h>
#include <ctype.h>
#include <errno.h>
#include <math.h>
#include <pcap.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/if_ether.h>
#include <netinet/tcp.h>
#include <netinet/ip.h>
#include <pthread.h>
#include <syslog.h>
#include <assert.h>

#define MAX_HOSTNAME_SIZE 50

//dumping.c
void dumpingd(int ports[], int nports);
