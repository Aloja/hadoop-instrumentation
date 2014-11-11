
#include <net_dumper.h>
#include <glib.h>

//methods
void gethostname(char hostname[MAX_HOSTNAME_SIZE]);
void gethostname_ip(unsigned int *hostname_ip);
void *dump_thr(void *parametro);

//global vars
char run;
char paused;
char hostname[MAX_HOSTNAME_SIZE];
unsigned int hostname_ip = 0;
int ports[101] = {[0 ... 99] = 0};
int nports;
GHashTable* ht_map_partner_pid;
GHashTable* ht_map_pid_partner;


int main(int num_ports, char *input_ports[]) {

    ht_map_partner_pid = g_hash_table_new(g_str_hash, g_str_equal);
    ht_map_pid_partner = g_hash_table_new(g_str_hash, g_str_equal);

    pthread_t tid; //thread id

    run = FALSE;
    paused = FALSE;


    int i;
    //FIXME: i=1 only because still main with program name
    nports=num_ports-1;
    for(i=1; i < num_ports; i++)
	ports[i-1] = atoi(input_ports[i]);

    //TODO: 
    gethostname(hostname);
    gethostname_ip(&hostname_ip);

    int err = pthread_create(&tid, NULL, &dump_thr, NULL);
    if (err != 0) {
        printf("\ncan't create thread :[%s]", strerror(err));
    } else {
        printf("\n Thread created successfully\n");
    }

     //   sleep(5);
    //    pause();
    //    sleep(5);
        start();
        sleep(5);
        stop();


    //readfile();

}

void *dump_thr(void *parametro) {
    start();
    dumpingd(ports, nports);
}

void gethostname(char hostname[250]) {
#define MAX_COMMAND_LEN 1000
    FILE *fp = popen("hostname", "r");
    fgets(hostname, 100, fp);
    hostname = strtok(hostname, "\n");
    pclose(fp);
}

void gethostname_ip(unsigned int *hostname_ip) {
#define MAX_COMMAND_LEN 1000
    struct sockaddr_in hostname_addr;
    char *tmp_hostname = calloc(100, sizeof (char));

    FILE *fp = popen("hostname -i", "r");
    fgets(tmp_hostname, 100, fp);
    inet_aton(tmp_hostname, &(hostname_addr.sin_addr));
    *hostname_ip = (unsigned int) hostname_addr.sin_addr.s_addr;
    pclose(fp);
}
