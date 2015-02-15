/* 
 * File:   sniffer.c
 * Author: smendoza
 *
 * Created on 5 de agosto de 2013, 12:12
 * 
 */

#include "snifferreceiver.h"
#include "stdio.h"

void generate_filter(char *filter, int ports[], int nports);

/**
 * The sniffing function obtains, from all the filtered packets, the required 
 * information that will be necessary to generate an extrae communication trace
 * @return 
 */
int sniffing(int inbound, int ports[], int nports) {

	// print libpcap version
	printf("%s\n", pcap_lib_version());

	//Extrae_init();

	char *dev = NULL;
	char errbuf[PCAP_ERRBUF_SIZE];
	pcap_t* descr_receiving_in;
	pcap_t* descr_receiving_out;
	pcap_t* descr_receiving_local_in;
	pcap_t* descr_receiving_local_out;
	struct bpf_program fp_in; /* to hold compiled program */
	struct bpf_program fp_out; /* to hold compiled program */
	struct bpf_program fp_local_in; /* to hold compiled program */
	struct bpf_program fp_local_out; /* to hold compiled program */
	bpf_u_int32 pMask; /* subnet mask */
	bpf_u_int32 pNet; /* ip address*/
	pcap_if_t *alldevs, *d;
	//    char dev_buff[64] = {0};
	char *dev_buff;
	int i = 0;
	int *result = NULL;
	char p_interface[] = "interface";
	char p_filter[] = "filter";
	char p_npkts[] = "packets";
	char *my_dev = calloc(100, sizeof (char));
	char *tmp_cmaxp = calloc(100, sizeof (char));
	char *filter_base = calloc(2000, sizeof (char));
	char *filter_remote = calloc(2000, sizeof (char));
	char *filter_local = calloc(2000, sizeof (char));
	int max_packets = 1;

	if (!inbound){
		printf("NOT INBOUND");
		return;
	}

	descr_receiving_in = pcap_create(NULL, errbuf);
	descr_receiving_out = pcap_create(NULL, errbuf);
	descr_receiving_local_in = pcap_create(NULL, errbuf);
	descr_receiving_local_out = pcap_create(NULL, errbuf);
	if (descr_receiving_in == NULL || descr_receiving_out == NULL || descr_receiving_local_in == NULL || descr_receiving_local_out == NULL)
		exit(-1);

	pcap_set_buffer_size(descr_receiving_in, 524288);
	pcap_set_buffer_size(descr_receiving_out, 524288);
	pcap_set_buffer_size(descr_receiving_local_in, 524288);
	pcap_set_buffer_size(descr_receiving_local_out, 524288);

	if(pcap_activate(descr_receiving_in)!=0){
		printf("pcap_activate(): %s\n", pcap_geterr(descr_receiving_in));
                printf("\npcap_activate(): LD_LIBRARY_PATH=%s\n", getenv("LD_LIBRARY_PATH"));
		exit(-1);
	}
	if(pcap_activate(descr_receiving_out)!=0){
                printf("pcap_activate(): %s\n", pcap_geterr(descr_receiving_out));
                printf("\npcap_activate(): LD_LIBRARY_PATH=%s\n", getenv("LD_LIBRARY_PATH"));
		exit(-1);
	}
	if(pcap_activate(descr_receiving_local_in)!=0){
		printf("pcap_activate(): %s\n", pcap_geterr(descr_receiving_local_in));
                printf("\npcap_activate(): LD_LIBRARY_PATH=%s\n", getenv("LD_LIBRARY_PATH"));
		exit(-1);
	}
	if(pcap_activate(descr_receiving_local_out)!=0){
                printf("pcap_activate(): %s\n", pcap_geterr(descr_receiving_local_out));
                printf("\npcap_activate(): LD_LIBRARY_PATH=%s\n", getenv("LD_LIBRARY_PATH"));
		exit(-1);
	}

	char error_prefix[80];
	sprintf(error_prefix,"Error calling pcap_setdirection");
	int setdirection_in_result = pcap_setdirection(descr_receiving_in, PCAP_D_IN);
	if (setdirection_in_result != 0) {
		printf("pcap_setdirection in result: %d\n", setdirection_in_result);
		pcap_perror(descr_receiving_in, error_prefix);
		exit(-1);
	}
	int setdirection_out_result = pcap_setdirection(descr_receiving_out, PCAP_D_OUT);
	if (setdirection_out_result != 0) {
		printf("pcap_setdirection out result: %d\n", setdirection_out_result);
		pcap_perror(descr_receiving_out, error_prefix);
		exit(-1);
	}
	int setdirection_local_in_result = pcap_setdirection(descr_receiving_local_in, PCAP_D_INOUT);
	if (setdirection_local_in_result != 0) {
		printf("pcap_setdirection in result: %d\n", setdirection_local_in_result);
		pcap_perror(descr_receiving_local_in, error_prefix);
		exit(-1);
	}
	int setdirection_local_out_result = pcap_setdirection(descr_receiving_local_out, PCAP_D_INOUT);
	if (setdirection_local_out_result != 0) {
		printf("pcap_setdirection out result: %d\n", setdirection_local_out_result);
		pcap_perror(descr_receiving_local_out, error_prefix);
		exit(-1);
	}


	//TODO: generate a filter with the ports specified by hadoop
	//filter by ports and just unicast packets
	generate_filter(filter_base, ports, nports);
	//    strcpy( filter, "tcp" );
	printf("generate_filter()=%s\n", filter_base);
	sprintf(filter_remote, "(%s) and (ip[12:4] != ip[16:4])", filter_base);
	printf("filter_remote=%s\n", filter_remote);
	sprintf(filter_local, "(%s) and (ip[12:4] == ip[16:4])", filter_base);
	printf("filter_local=%s\n", filter_local);

	// Compile the filter expression
	if (pcap_compile(descr_receiving_in, &fp_in, filter_remote, 0, pNet) == -1) {
		printf("\npcap_compile() failed\n");
		printf("\npcap_compile(): filter used: %s\n", filter_remote);
		printf("pcap_compile(): %s\n", pcap_geterr(descr_receiving_in));
		printf("\npcap_compile(): LD_LIBRARY_PATH=%s", getenv("LD_LIBRARY_PATH"));
		return -1;
	}
	if (pcap_compile(descr_receiving_out, &fp_out, filter_remote, 0, pNet) == -1) {
		printf("\npcap_compile() failed\n");
		printf("\npcap_compile(): filter used: %s\n", filter_remote);
		printf("pcap_compile(): %s\n", pcap_geterr(descr_receiving_out));
		printf("\npcap_compile(): LD_LIBRARY_PATH=%s", getenv("LD_LIBRARY_PATH"));
		return -1;
	}
	if (pcap_compile(descr_receiving_local_in, &fp_local_in, filter_local, 0, pNet) == -1) {
		printf("\npcap_compile() failed\n");
		printf("\npcap_compile(): filter used: %s\n", filter_local);
		printf("pcap_compile(): %s\n", pcap_geterr(descr_receiving_local_in));
		printf("\npcap_compile(): LD_LIBRARY_PATH=%s", getenv("LD_LIBRARY_PATH"));
		return -1;
	}
	if (pcap_compile(descr_receiving_local_out, &fp_local_out, filter_local, 0, pNet) == -1) {
		printf("\npcap_compile() failed\n");
		printf("\npcap_compile(): filter used: %s\n", filter_local);
		printf("pcap_compile(): %s\n", pcap_geterr(descr_receiving_local_out));
		printf("\npcap_compile(): LD_LIBRARY_PATH=%s", getenv("LD_LIBRARY_PATH"));
		return -1;
	}

	// Set the filter compiled above
	if (pcap_setfilter(descr_receiving_in, &fp_in) == -1) {
		printf("\npcap_setfilter() failed\n");
		exit(1);
	}
	if (pcap_setfilter(descr_receiving_out, &fp_out) == -1) {
		printf("\npcap_setfilter() failed\n");
		exit(1);
	}
	if (pcap_setfilter(descr_receiving_local_in, &fp_local_in) == -1) {
		printf("\npcap_setfilter() failed\n");
		exit(1);
	}
	if (pcap_setfilter(descr_receiving_local_out, &fp_local_out) == -1) {
		printf("\npcap_setfilter() failed\n");
		exit(1);
	}
	char in = (char) 1;
	char out = (char) 0;
	printf("sniffing on: %d\n", (int) in);
	fflush(stdout);
	while (1) {
		pcap_dispatch(descr_receiving_out, max_packets, callback, &out);
		pcap_dispatch(descr_receiving_in, max_packets, callback, &in);
		pcap_dispatch(descr_receiving_local_out, max_packets, callback, &out);
		pcap_dispatch(descr_receiving_local_in, max_packets, callback, &in);
	}
	printf("dispatch finished, voy para el Extrae_fini()\n");
	//Extrae_fini(); //Fini absurdo,pq está fuera del while
	printf("Extrae_fini() pasado\n");

}

void generate_filter(char *filter, int ports[], int nports) {

#define FILTER_PROTOCOLS "tcp"
#define FILTER_BCC_MCC_1 "not broadcast and not multicast" // wireshark, but libpcap?
#define FILTER_BCC_MCC_2 "not ether broadcast and not ether multicast"
#define FILTER_BCC_MCC_3 "(not ip broadcast) and (not ip multicast)"
#define FILTER_BCC_MCC_4 "not ip6 broadcast and not ip6 multicast"
	//              True if the packet is an IPv6 multicast packet.


	char fname[2000];
	snprintf(fname, sizeof fname, "%s/sniffer.log", getenv("EXTRAE_DIR"));
	FILE *logfile = fopen(fname, "w");
	fprintf(logfile, "sniffer.c->c.generate_filter()...");
	fflush(logfile);

	char *tmp_msg = calloc(1000, sizeof (char));

	sprintf(tmp_msg, "%s > ports_found = %d", __func__, nports);

	//TODO:create the libpcap filter
	char *filter_protocol = FILTER_PROTOCOLS;
	int i;
	char *str_port = calloc(1000, sizeof (char));
	char *filter_ports = calloc(1000, sizeof (char));
	char *separator = " ";
	for (i = 0; i < nports; ++i) {
		sprintf(str_port, "%s%d", separator, ports[i]);
		strcat(filter_ports, str_port);
		separator = " or ";
	}
	sprintf(tmp_msg, " %s", filter_ports);
	//(tcp) and (not ip broadcast) and (not ip multicast) and (dst port ( 80 or 50010 or 8080 or 50000) or src port ( 80 or 50010 or 8080 or 50000))
	// merge the filter info into a unique string (http://www.manpagez.com/man/7/pcap-filter/)
	sprintf(filter, "(%s) and %s and (dst port (%s) or src port (%s))", filter_protocol, FILTER_BCC_MCC_3, filter_ports, filter_ports);
	//sprintf(filter, "(%s) and (dst port (%s) or src port (%s))", filter_protocol, filter_ports, filter_ports);
	//sprintf(filter, "port 50000");
	printf("filter-generated: %s\n", filter);
	fflush(stdout);

	fprintf(logfile, "filter-generated: %s\n", filter);
	fclose(logfile);

}

