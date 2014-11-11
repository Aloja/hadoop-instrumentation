#include <port_dumper.h>
#include <stdlib.h>
#include <unistd.h>
//#include <glib.h>

#define MAX_COMMAND_LEN 1000

//GArray ephemeral_ports = g_array_new(FALSE, FALSE, sizeof (gint));
//
//void dumpingephports(int ports[], int nports) {
//
//    int i;
//    char command[MAX_COMMAND_LEN];
//
//    for (i = 0; i < nports; ++i)
//        printf("dumpingd: looking for port %d\n", ports[i]);
//
//    while (1) {
//        for (i = 0; i < nports; ++i) {
//            //sprintf(command, "lsof -i:%d | grep -v COMMAND | grep -v LISTEN | grep java | tr -s ' ' | sed -e 's/:/ /g' | sed -e 's/->/ /g' | cut -d ' ' -f 2,8,9 | sed -e 's/ /:/g' | sed -e 's/localhost/\'\"`hostname -i`\"\'/g' >> %s &", ports[i], FNAME);
//            sprintf(command, "lsof -i:%d | grep -v COMMAND | grep -v LISTEN | grep java | tr -s ' ' | sed -e 's/:/ /g' | sed -e 's/->/ /g' | cut -d ' ' -f 2,8,9 | sed -e 's/ /:/g' | sed -e 's/localhost/127.0.0.1/g' >> %s &", ports[i], FNAME);
//            system(command);
//            bzero(command, MAX_COMMAND_LEN);
//            //sprintf(command, "lsof -i:%d | grep -v COMMAND | grep LISTEN | grep java | tr -s ' ' | sed -e 's/:/ /g' | sed -e 's/->/ /g' | cut -d ' ' -f 2,8,9 | sed -e 's/ /:/g' | sed -e 's/*/\'\"`hostname -i`\"\'/g' | sed -e 's/localhost/\'\"`hostname -i`\"\'/g' >> %s &", ports[i], FNAME);
//            sprintf(command, "lsof -i:%d | grep -v COMMAND | grep LISTEN | grep java | tr -s ' ' | sed -e 's/:/ /g' | sed -e 's/->/ /g' | cut -d ' ' -f 2,8,9 | sed -e 's/ /:/g' | sed -e 's/*/127.0.0.1/g' | sed -e 's/localhost/\'\"`hostname -i`\"\'/g' >> %s &", ports[i], FNAME);
//            system(command);
//        }
//        sleep(2);
//    }
//}
//
//void addephports() {
//
//    int i;
//    for (i = 0; i < ephemeral_ports->len; ++i) {
//        printf("%d ", g_array_index(a, int, i));
//    }
//
//
//}



