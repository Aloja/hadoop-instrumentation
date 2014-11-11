
#include "snifferreceiver.h"


void processing() {
    double rt = atof(getReceiveTime("", "8743"));
    double st = atof(getSendingTime("", "50349"));

    double transfer_time = rt - st;
    printf("received time: %f\n", rt);
    printf("sending time: %f\n", st);
    printf("transfer_time: %f\n", transfer_time);
}


char *getReceiveTime(char *file, char *id) {
    FILE * fp;
    char * line = NULL;
    size_t len = 0;
    ssize_t read;

    const char delimiters[] = ":";
    //    char *running;
    char *token;

    fp = fopen(file, "r");
    if (fp == NULL)
        return "NO EXISTE EL FICHERO!";

    char *pvalue = "NADA";
    int i;
    //ip_src:ip_dst:ip_len:ip_id:port_src:port_dst:th_seq:th_ack:th_sum:total_msec
    while ((read = getline(&line, &len, fp)) != -1) {
        printf("Retrieved line of length %zu :\n", read);
        printf("line: %s\n", line);
        char * ipid = malloc(24);
        for (i = 0; i < 10; ++i) {
            pvalue = strsep(&line, delimiters);
            //            printf("pvalue: %s\n", pvalue);
            if (i == 3 && strcmp(id, pvalue) == 0) {
                printf("COL[%u], strcmp(%s, %s)=%d\n", i, id, pvalue, strcmp(id, pvalue));
                strcpy(ipid, pvalue);
            } else {
                printf("COL[%u], strcmp(%s, %s)=%d\n", i, id, pvalue, strcmp(id, pvalue));
            }
        }
        if (strcmp(id, ipid) == 0) {
            printf("ipid: %s\n", ipid);
            return pvalue;
        } else {
            printf("ipid: %s\n", ipid);
        }
    }
    printf("***************************\n");
    printf("Packet with id=%s NOT FOUND\n", id);
    printf("***************************\n");

    return "0";
}

char *getSendingTime(char *file, char *id) {
    FILE * fp;
    char * line = NULL;
    size_t len = 0;
    ssize_t read;


    const char delimiters[] = ":";
    //    char *running;
    char *token;

    fp = fopen(file, "r");
    if (fp == NULL)
        return "NO EXISTE EL FICHERO!";

    char *pvalue = "NADA";
    int i;
    //ip_src:ip_dst:ip_len:ip_id:port_src:port_dst:th_seq:th_ack:th_sum:total_msec
    while ((read = getline(&line, &len, fp)) != -1) {
        printf("Retrieved line of length %zu :\n", read);
        printf("line: %s\n", line);
        char * ipid = malloc(24);
        for (i = 0; i < 10; ++i) {
            pvalue = strsep(&line, delimiters);
            //            printf("pvalue: %s\n", pvalue);
            if (i == 3 && strcmp(id, pvalue) == 0) {
                printf("strcmp(%s, %s)=%d", id, pvalue, strcmp(id, pvalue));
                strcpy(ipid, pvalue);
            } else {
                printf("strcmp(%s, %s)=%d", id, pvalue, strcmp(id, pvalue));
            }
        }
        if (strcmp(id, ipid) == 0) {
            printf("ipid: %s\n", ipid);
            return pvalue;
        } else {
            printf("ipid: %s\n", ipid);
        }
    }
    printf("***************************\n");
    printf("Packet with id=%s NOT FOUND\n", id);
    printf("***************************\n");

    return "0";
}
