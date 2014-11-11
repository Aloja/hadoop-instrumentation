#include "snifferreceiver.h"

int sniffing_test() {
#define MAX_SNIFFS 100
    int loops = 0;
            printf("sniffing(1);");
int ports[] = {80,50010,8080,50000};
int nports = 4;
sniffing(1,ports,nports);

/*
    while (loops < MAX_SNIFFS) {
        printf("loop %d\n", loops);
        if (loops & 0x1) {
            printf("sniffing(1);");
            sniffing(1);
        } else {
            printf("sniffing(0);");
        }
        ++loops;

    }
*/
}

/*
int main(){
sniffing_test();
}
*/

