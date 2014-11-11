#include "snifferreceiver.h"




typedef struct {
    char *ip_src;
    char *ip_dst;
    char *port_src;
    char *port_dst;
    char *packet_id;

} connection;

typedef struct {
    //    char *1:2:3:4:5:6:7:8:9:10:11:12:13:14:15
    char *record_type; //Record Type: COMMUNICATION (STATE/EVENT)
    char *comm_src_cpu; //Communication Source: CPU
    char *comm_src_app; //Communication Source: APPLICATION
    char *comm_src_process; //Communication Source: PROCESS
    char *comm_src_thread; //Communication Source: THREAD
    char *comm_src_time_log_sendt; //Communication Source: Time logical sendt
    char *comm_src_time_phy_sendt; //Communication Source: Time physical sendt
    char *comm_dst_cpu; //Communication Dest: CPU
    char *comm_dst_app; //Communication Dest: APPLICATION
    char *comm_dst_process; //Communication Dest: PROCESS
    char *comm_dst_thread; //Communication Dest: THREAD
    char *comm_dst_time_log_received; //Communication Dest: Time logical received
    char *comm_dst_time_phy_received; //Communication Dest: Time physical received
    char *size;
    char *tag;

} extrae_trace;

/*
1:2:3:4:5:6:7:8:9:10:11:12:13:14:15
Record Type: COMMUNICATION (STATE/EVENT)
Communication Source: CPU
Communication Source: APPLICATION
Communication Source: PROCESS
Communication Source: THREAD
Communication Source: Time logical sendt
Communication Source: Time physical sendt
Communication Dest: CPU
Communication Dest: APPLICATION
Communication Dest: PROCESS
Communication Dest: THREAD
Communication Dest: Time logical received
Communication Dest: Time physical received
Size
Tag
*/


int packets_available();

void gen_trace(){
    

    if(packets_available()){
        //TODO: generate an extrae trace
    }else{
        //TODO: wait till packets available
    }
    
}

int packets_available(){
    //TODO: verify if there are packets available
    return -1;
}

/*

char* makeTrace(extrae_trace strace) {

    char *trace = malloc(512);

    strcpy(trace, strace.record_type); //Record Type: (STATE/EVENT/COMMUNICATION)
    strcat(trace, strace.comm_src_cpu); //Communication Source: CPU
    strcat(trace, strace.comm_src_app); //Communication Source: APPLICATION
    strcat(trace, strace.comm_src_process); //Communication Source: PROCESS
    strcat(trace, strace.comm_src_thread); //Communication Source: THREAD
    strcat(trace, strace.comm_src_time_log_sendt); //Communication Source: Time logical sendt
    strcat(trace, strace.comm_src_time_phy_sendt); //Communication Source: Time physical sendt
    strcat(trace, strace.comm_dst_cpu); //Communication Dest: CPU
    strcat(trace, strace.comm_dst_app); //Communication Dest: APPLICATION
    strcat(trace, strace.comm_dst_process); //Communication Dest: PROCESS
    strcat(trace, strace.comm_dst_thread); //Communication Dest: THREAD
    strcat(trace, strace.comm_dst_time_log_received); //Communication Dest: Time logical received
    strcat(trace, strace.comm_dst_time_phy_received); //Communication Dest: Time physical received
    strcat(trace, strace.size);
    strcat(trace, strace.tag);

    printf("\nstrace: %s\n", trace);
    
    return trace;
}
*/