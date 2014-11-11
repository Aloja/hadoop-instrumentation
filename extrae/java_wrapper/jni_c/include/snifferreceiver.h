/* 
 * File:   snifferreceiver.h
 * Author: smendoza
 *
 * Created on 5 de agosto de 2013, 12:12
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


#define SIZE_ETHERNET 14
#define MY_ETHER_ADDR_LEN 6       /* Ethernet addresses are 6 bytes */
#define INITPARAMS_RESULTS_DIRECTORY "results.directory"

#define DST_DIR "/tmp/packet_sniffing/"
#define DST_DIR_MODE 777

#define PACKET_TCP 1


/* Ethernet header */
struct sniff_ethernet {
    u_char ether_dhost[MY_ETHER_ADDR_LEN]; /* Destination host address */
    u_char ether_shost[MY_ETHER_ADDR_LEN]; /* Source host address */
    u_short ether_type; /* IP? ARP? RARP? etc */
};

/* IP header */
struct sniff_ip {
    u_char ip_vhl; /* version << 4 | header length >> 2 */
    u_char ip_tos; /* type of service */
    u_short ip_len; // total length (more info: http://en.wikipedia.org/wiki/IPv4_header#Total_Length)
    u_short ip_id; /* identification */
    u_short ip_off; /* fragment offset field */
#define IP_RF 0x8000		/* reserved fragment flag */
#define IP_DF 0x4000		/* dont fragment flag */
#define IP_MF 0x2000		/* more fragments flag */
#define IP_OFFMASK 0x1fff	/* mask for fragmenting bits */
    u_char ip_ttl; /* time to live */
    u_char ip_p; /* protocol */
    u_short ip_sum; /* checksum */
    struct in_addr ip_src, ip_dst; /* source and dest address */
};

#define IP_V(ip)		(((ip)->ip_vhl) >> 4)

/* TCP header */
typedef u_int tcp_seq;

/* TCP header */
struct sniff_tcp {
    u_short th_sport; /* source port */
    u_short th_dport; /* destination port */
    tcp_seq th_seq; /* sequence number */
    tcp_seq th_ack; /* acknowledgement number */

    u_char th_offx2; /* data offset, rsvd */
#define TH_OFF(th)	(((th)->th_offx2 & 0xf0) >> 4)
    u_char th_flags;
#define TH_FIN 0x01
#define TH_SYN 0x02
#define TH_RST 0x04
#define TH_PUSH 0x08
#define TH_ACK 0x10
#define TH_URG 0x20
#define TH_ECE 0x40
#define TH_CWR 0x80
#define TH_FLAGS (TH_FIN|TH_SYN|TH_RST|TH_ACK|TH_URG|TH_ECE|TH_CWR)
    u_short th_win; /* window */
    u_short th_sum; /* checksum */
    u_short th_urp; /* urgent pointer */
};

typedef struct a {
    struct in_addr ip_src;
    struct in_addr ip_dst;
    unsigned short port_src;
    unsigned short port_dst;
    //    u_short ip_len;
    unsigned short pckt_len;
    unsigned short ip_id;
    tcp_seq th_seq;
    tcp_seq th_ack;
    unsigned short th_sum;
    double total_msec;
    char dst_file_fpath[100];
} packet_timming;


static char errbuf[PCAP_ERRBUF_SIZE];

//static char home[] = "/home/smendoza/NetBeansProjects/ar/analizeReception/trunk/";
static char file_properties[] = "config.properties";
static char file_capture_received[] = "/tmp/receive.txt";
static char file_capture_sendt[] = "/tmp/sendt.txt";
static char config_file[200]; // = "/home/smendoza/NetBeansProjects/analizeReception/config.properties";



/* Functions */
int create_tcpstream_file(packet_timming pt);
char *readProperty(char *configFile, char *pname);
void store_packet(packet_timming pt);
char *getSendingTime(char *file, char *id);
char *getReceiveTime(char *file, char *id);
void callback(u_char *useless, const struct pcap_pkthdr* pkthdr, const u_char* packet);

