#include <commons_bsc.h>
#include <snifferreceiver.h>
#include <extrae_types.h>
#include <dirent.h>
#include <unistd.h>
//yolandab
#include <fcntl.h>
//end yolandab


#define IP_HL(ip)		(((ip)->ip_vhl) & 0x0f)
#define TH_OFF(th)	(((th)->th_offx2 & 0xf0) >> 4)
#define SIZE_ETHERNET 14
#define SLL_HDR_LEN     16              /* total header length */

extrae_type_t types[10] = {77770, 77771, 77772, 77773, 77774, 77775, 77776, 77777, 77778, 77779};

// 7770 -> LOCAL IP
// 7771 -> LOCAL PORT
// 7772 -> REMOTE IP
// 7773 -> REMOTE PORT
// 7774 -> RAW PCKT LEN
// 7775 -> APP PAYLOAD LEN
// 7776 -> NUM SEQ
// 7777 -> NUM ACK

void callback(u_char *inbound, const struct pcap_pkthdr* pkthdr, const u_char* packet) {

	const struct sll_header *ethernet;
	const struct sniff_ip *ip; /* The IP header */
	const struct sniff_tcp *tcp; /* The TCP header */
	unsigned int partner;
	unsigned int local;
	unsigned int send;
	unsigned int payload_size;
	u_int size_ip;
	u_int size_tcp;
	extrae_value_t values[10];

	//yolandab
	int fd;
	char c = 'w';
	//end yolandab

	ethernet = (struct sll_header*) (packet);
	ip = (struct sniff_ip*) (packet + SLL_HDR_LEN);
	size_ip = IP_HL(ip)*4;
	if (size_ip < 20) {
		printf("   * Invalid IP header length: %u bytes\n", size_ip);
		return;
	}
	tcp = (struct sniff_tcp*) (packet + SLL_HDR_LEN + size_ip);
	size_tcp = TH_OFF(tcp)*4;
	if (size_tcp < 20) {
		printf("   * Invalid TCP header length: %u bytes\n", size_tcp);
		return;
	}

	packet_timming pt;

	pt.ip_src = ip->ip_src;
	pt.ip_dst = ip->ip_dst;
	pt.port_src = ntohs(tcp->th_sport);
	pt.port_dst = ntohs(tcp->th_dport);
	pt.pckt_len = pkthdr->len; // packet length
	pt.th_seq = ntohl(tcp->th_seq); // sequence number
	pt.th_ack = ntohl(tcp->th_ack); // acknowledgement number
	payload_size = pt.pckt_len - (SLL_HDR_LEN + size_ip + size_tcp);
	char ip_src_str[INET_ADDRSTRLEN];
	char ip_dst_str[INET_ADDRSTRLEN];
	if (*inbound) send = 0;
	else send = 1;
	if (send) {
		values[0] = (extrae_value_t) pt.ip_src.s_addr;
		values[1] = (extrae_value_t) pt.port_src;
		values[2] = (extrae_value_t) pt.ip_dst.s_addr;
		values[3] = (extrae_value_t) pt.port_dst;
		inet_ntop(AF_INET, &pt.ip_src, ip_src_str, INET_ADDRSTRLEN);
		inet_ntop(AF_INET, &pt.ip_dst, ip_dst_str, INET_ADDRSTRLEN);
	} else {
		values[0] = (extrae_value_t) pt.ip_dst.s_addr;
		values[1] = (extrae_value_t) pt.port_dst;
		values[2] = (extrae_value_t) pt.ip_src.s_addr;
		values[3] = (extrae_value_t) pt.port_src;
		inet_ntop(AF_INET, &pt.ip_dst, ip_src_str, INET_ADDRSTRLEN);
		inet_ntop(AF_INET, &pt.ip_src, ip_dst_str, INET_ADDRSTRLEN);
	}
	values[4] = (extrae_value_t) pt.pckt_len;
	values[5] = (extrae_value_t) payload_size;
	values[6] = (extrae_value_t) pt.th_seq;
	values[7] = (extrae_value_t) pt.th_ack;
	values[8] = (extrae_value_t) tcp->th_flags;
	values[9] = (extrae_value_t) send;

	//printf("llego aqui sin problemas! %s:%d (%lu) -> %s:%d (%lu)\n", inet_ntoa(pt.ip_src), values[1], ntohl(pt.ip_src.s_addr), inet_ntoa(pt.ip_dst), values[3], ntohl(pt.ip_dst.s_addr));fflush(stdout);

	//Si es un loopback (ip dst == ip src), no hago nada
	// Cuando hadoop se ejecuta en una sola maquina (mismo master & slave), la
	// condicion anterior hacia que se ignorase todo el trafico (la ip siempre
	// es la misma), por lo que se ha añadido la condicion que la ip sea
	// 127.0.0.1 para ignorar este trafico
	struct in_addr ip_localhost;
	inet_aton("127.0.0.1", &ip_localhost);
	if (values[0] == values[2] && pt.ip_src.s_addr == ip_localhost.s_addr) {
		return;
	}

	//Generate state 
	extrae_type_t types_antes[4] = {5050, 5051, 5052, 5053};  // Aquests events no estan descrits en el paraver
	extrae_value_t values_antes[4]; // = {0,0,0,0};
	values_antes[0] = (extrae_value_t) send;
	values_antes[1] = values[0]; //(extrae_value_t) pt.ip_src.s_addr; //values[0];
	values_antes[2] = values[1]; //(extrae_value_t) pt.port_src; // values[1];
	values_antes[3] = (extrae_value_t) pt.pckt_len;
	// Extrae_nevent(4, types_antes,values_antes);
	Extrae_nevent(10, types, values);
	// Extrae_nevent(4, types_antes,values_antes);
	//Extrae_flush();

	printf("Generating event: %s, #seq: %llu, #ack: %lld, link size: %d, app size: %d, local: %s:%d, remote:  %s:%d\n", (send == 0) ? "rcv" : ((send == 1) ? "snd" : "amb"), pt.th_seq, pt.th_ack, pt.pckt_len, payload_size, ip_src_str, values[1], ip_dst_str, values[3]);

	struct timeval arr_time;
	double tiempo;
	gettimeofday(&arr_time, NULL);   // Instante inicial
	tiempo = arr_time.tv_sec*1000 + arr_time.tv_usec/1000;

	//double secs_acotados

	printf("arr_time.sec: %d\n",arr_time.tv_sec);
	printf("arr_time.usec: %d\n",arr_time.tv_usec);

	char str_nevent[1000];
	sprintf(str_nevent, "2:0:1:%llu:1:%llu:77770:%llu:77771:%llu:77772:%llu:77773:%llu:77774:%llu:77775:%llu:77776:%llu:77777:%llu:77778:%llu:77779:%llu", getpid(),tiempo,values[0],values[1],values[2],values[3],values[4],values[5],values[6],values[7],values[8],values[9]);
	printf("wala-nevent-> %s\n", str_nevent);
	fflush(stdout);

	if (tcp->th_flags & TH_SYN) {
		printf("SYN, %d \n", pt.port_src);
		char *command;
		//        sprintf(command, "lsof -i:%d  >> /tmp/dumping-host-port-pid &", pt.port_src);
		//        system(command);

		//yolandab
		int ret;
		fprintf(stdout, "PCKHANDLER KILLING\n");
		char pipe_name[2000];
		snprintf(pipe_name, sizeof pipe_name, "%s/pipe", getenv("EXTRAE_DIR"));
		fd = open(pipe_name, O_RDWR);
		write(fd, &c, sizeof (char));

		if (ret < 0) fprintf(stdout, "KILLING FROM PCKHANDLER");
		//   FILE *fp;
		//  char lsof_output[1000];
		//         sprintf(command, "lsof -i:%d", pt.port_src);
		//fp = popen(command, "r");
		//if (fp != NULL) {
		// while (fgets(lsof_output, 1000, fp) != NULL)
		//    printf("%s", lsof_output);
		//}
		//pclose(fp);
		//end yolandab
	}

	//<captando pids de conexiones>
	//    char line[1000];
	//    char port_hex[10]; /* two bytes of hex = 4 characters, plus NULL terminator */
	//    char *ptr;
	//#define datcp "/proc/net/tcp"
	//    FILE *fp = NULL;
	//    fp = fopen(datcp, "r");
	//    if (fp != NULL) {
	//        while (fgets(line, sizeof line, fp) != NULL) {
	//            sprintf(port_hex, "%04X", pt.port_src);
	//            //printf("line=%s, port_hex=%s\n", line, port_hex);
	//            if (strstr(line, port_hex) != NULL) {
	//                int i = 0;
	//                char inode[50];
	//                //printf("ptr=%s,", ptr);
	//                ptr = strtok(line, " "); //first token
	//                while (ptr != NULL) {
	//                    if (i == 10) {
	//                        sprintf(inode, "%s", ptr);
	//                        //printf("inode=%s, ", inode);
	//                        //anyadir la busqueda en /proc/[pid]/fd/* del pid:
	//                        DIR *d;
	//                        DIR *d2;
	//                        struct dirent *dir;
	//                        d = opendir("/proc/");
	//
	//                        if (d) {
	//                            while ((dir = readdir(d))) {
	//                                if (dir->d_name[0] == '.' || dir->d_name[1] == '.')
	//                                    continue;
	//                                //printf("readdir=%s,", dir->d_name);
	//                                char x[100];
	//                                struct dirent *dir2;
	//                                sprintf(x, "/proc/%s/fd/", dir->d_name);
	//                                //printf("x=%s, ",x);
	//                                d2 = opendir(x);
	//                                if (d2) {
	//                                    while ((dir2 = readdir(d2))) {
	//                                        if (dir->d_name[0] == '.' || dir->d_name[1] == '.')
	//                                            continue;
	//                                        if (dir2->d_type & DT_LNK) {
	//                                            char buf[1024];
	//                                            ssize_t len;
	//                                            char mylink[100];
	//                                            sprintf(mylink, "/proc/%s/fd/%s", dir->d_name, dir2->d_name);
	//                                            if ((len = readlink(mylink, buf, sizeof (buf) - 1)) != -1) {
	//                                                buf[len] = '\0';
	//                                                if (strstr(buf, inode) != NULL) {
	//                                                    printf("%s->%s, PID=%s\n", dir2->d_name, buf, dir->d_name);
	//                                                }
	//                                            }
	//                                        }
	//                                    }
	//                                }
	//                                closedir(d2);
	//                            }
	//                            closedir(d);
	//                        }
	//                    }
	//                    i++;
	//                    ptr = strtok(NULL, " "); // get next token
	//                }
	//            }
	//        }
	//    }
	//    fclose(fp);
	//</captando pids de conexiones>

}


