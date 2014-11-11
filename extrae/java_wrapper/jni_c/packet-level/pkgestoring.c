
#include <sys/stat.h>

#include "snifferreceiver.h"

void store_packet(packet_timming pt) {

    // TODO: if not exists: create; else: nothing;
    printf("STORE?\n");
    FILE *fexists;
    if (fexists = fopen(pt.dst_file_fpath, "r")) {
        printf("exists\n");
        // exists
        fclose(fexists);
    } else {
        // TODO: verify directory exists or create it/them otherwise
        // now assuming now assuming directory exists,
        printf("file not exists, assuming direcotry already exists\n");
        mkdir(DST_DIR, DST_DIR_MODE);
        if (create_tcpstream_file(pt) == -1) {
            printf("Error creating the file %s\n", pt.dst_file_fpath);
        }
    }


    /* apend file (add text to a file or create a file if it does not exist.*/
    FILE *file;
    file = fopen(pt.dst_file_fpath, "a+");

    // In a unique long line is non-readable for the developer
    //    fprintf(file, "%s:%s:%d:%d:%u:%u:%u:%u:%u:%f", pt.ip_src, pt.ip_dst, pt.pckt_len, pt.ip_id, pt.port_src, pt.port_dst, pt.th_seq, pt.th_ack, pt.th_sum, pt.total_msec);
    fprintf(file, "%s", pt.ip_src);
    fprintf(file, ":%s", pt.ip_dst);
    fprintf(file, ":%d", pt.pckt_len); // packet total length in bytes
    fprintf(file, ":%d", pt.ip_id); // identification
    fprintf(file, ":%u", pt.port_src); // source port
    fprintf(file, ":%u", pt.port_dst); // destination port
    fprintf(file, ":%u", pt.th_seq); // sequence number
    fprintf(file, ":%u", pt.th_ack); // acknowledgement number
    fprintf(file, ":%u", pt.th_sum); // checksum
    fprintf(file, ":%f", pt.total_msec); // writes
    fprintf(file, "\n");

    fclose(file); /*done!*/
}

/*Creates a file for the tcpstream if not already exists, assuming directory already exist*/
int create_tcpstream_file(packet_timming pt) {
    printf(">>>>>>>>>>>>>>>>>>>>Creating file at %s\n", pt.dst_file_fpath);

    FILE *file;
    file = fopen(pt.dst_file_fpath, "w");
    //    fprintf(file, "ip_src:ip_dst:ip_len:ip_id:port_src:port_dst:th_seq:th_ack:th_sum:total_msec\n");
    fprintf(file, "");
    fclose(file);
    return 0;
}
