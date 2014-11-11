
#include <net_dumper.h>
#include <stdlib.h>

#define MAX_COMMAND_LEN 1000
#define FNAME "/tmp/dumping-host-port-pid.2"

void ports_join(int *ports);
void ports_join(int *ports);
unsigned int pidfinder(int port);

void dumpingd(int ports[], int nports) {

    int port;
    int pid;
    int i;
    int local_run = 0;

    FILE *fd = fopen(FNAME, "a");
    //volcado de las ip:puertos ---> pids relacionados

    printf("DUMPING\n");

    //    pthread_mutex_t *mutexBuffer;
    //    pthread_mutex_init(mutexBuffer, NULL);
    //    pthread_mutex_lock(mutexBuffer);
    //    /* aquí se accede a la estructura de datos */
    //    local_run = run;
    //    pthread_mutex_unlock(mutexBuffer);
    printf("run=%d\n", run);
    printf("nports=%d\n", nports);
    printf("hostname_ip=%d\n", hostname_ip);
    //    printf("local_run=%d\n",local_run);
    while (run) {
        printf("RUN (maybe paused)\n");
        for (i = 0; (!paused) && (i < nports); ++i) {
            printf("ports[%d]=%d\n", i, ports[i]);
            pid = pidfinder(ports[i]);
            printf("pidfinder(ports[%d])=%d\n", i, pid);
            fprintf(fd, "%x:%u:%u\n", hostname_ip, ports[i], pid);
        }
	sleep(2);
    }

    fclose(fd);

    return;
}

unsigned int pidfinder(int port) {
#define MAX_COMMAND_LEN 1000
    char command[MAX_COMMAND_LEN];
    sprintf(command, "lsof -i:%d -t", port);
    unsigned int pid = 0;

    FILE *fp = popen(command, "r");
    char buf[1024];

    while (fgets(buf, 1024, fp)) {
        pid = atoi(buf);
    }

    pclose(fp);

    return pid;
}

void pidfinder_portlist(int *ports) {

    ports_join(ports);

}

void ports_join(int *ports) {
    int i;
    char *port_list = calloc(1000, sizeof (char));
    char *separator = "";
    for (i = 0; i < (sizeof (ports) / sizeof (int)); ++i) {
        char *tmp = calloc(1000, sizeof (char));
        sprintf(port_list, "%s%d", separator, ports[i]);
        strcat(port_list, tmp);
        separator = ",";
    }
}

void readfile() {

    FILE *fd = fopen(FNAME, "r");
    unsigned int l_hostname_ip;
    unsigned int l_port;
    unsigned int l_pid;
    int i;
    for (i = 0; i < 10; ++i) {
        fscanf(fd, "%x:%u:%u\n", &l_hostname_ip, &l_port, &l_pid);
        printf("READ: %x:%u:%u\n", l_hostname_ip, l_port, l_pid);

        add_map_partner_pid(l_hostname_ip, l_port, l_pid);
        int pid = get_pid(l_hostname_ip, l_port);
        printf("%x:%d pid=%d\n",l_hostname_ip, l_port,pid);

    }

    fclose(fd);

    return;
}

void add_map_partner_pid(u_int ip, u_int port, u_int pid) {

    u_int partner;
    char *value_str = calloc(100, sizeof (char));
    char *key_str = calloc(100, sizeof (char));

    partner = (ip & 0xFFFF0000) | (port & 0xFFFF);
    partner = 5;
    sprintf(key_str, "%u", partner);
    sprintf(value_str, "%u", pid);
    //    printf("%s,%s\n", key_str, value_str);

    g_hash_table_insert(ht_map_partner_pid, key_str, value_str);
}

void add_map_pid_partner(u_int ip, u_int port, u_int pid) {


    u_int partner;
    char *value_str = calloc(100, sizeof (char));
    char *key_str = calloc(100, sizeof (char));

    partner = (ip & 0xFFFF0000) | (port & 0xFFFFF);
    sprintf(value_str, "%u", partner);
    sprintf(key_str, "%u", pid);

    g_hash_table_insert(ht_map_pid_partner, key_str, value_str);

}

int get_pid(u_int ip, u_int port) {
    u_int partner;
    char *value_str;
    char *key_str;// = calloc(100, sizeof (char));

    partner = (ip & 0xFFFF0000) | (port & 0xFFFFF);
    sprintf(key_str, "%u", partner);
    printf("%s\n", key_str);
//    value_str = g_hash_table_lookup(ht_map_partner_pid, key_str);

//    return atoi(value_str);
    return 0;
}

