#include <port_dumper.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>

#define MAX_COMMAND_LEN 1000

void dumpingd(int ports[], int nports) {

	//output files
	char *fname="/tmp/smendoza/dumping-host-port-pid";
	char *flogname="/tmp/smendoza/dumpingd.log";
	int i;
	char command[MAX_COMMAND_LEN];
	char *siesta = getenv("SIESTA");
	int sleeping_time = atoi(siesta);
	printf("siesta=%s , sleeping_time=%d\n", siesta, sleeping_time);

	pid_t pid1, pid2;

	printf("dumpingd: looking for ports[");
	for (i = 0; i < nports; ++i)
		printf("(%d,%d),", i, ports[i]);

	printf("]\n");

	FILE *file;
	file = fopen(flogname, "w+"); /* apend file (add text to a file or create a file if it does not exist.*/
	int loops = 0;
	time_t comienzo, final, init_loop;

	comienzo = time(NULL);

	while (1) {
		//init_loop = time(NULL);
		//                for (i = 0; i < nports; ++i) {
		//
		//            pid1 = fork();
		//            if (pid1 == 0) {
		//                //sprintf(command, "lsof -i:%d | grep -v COMMAND | grep -v LISTEN | grep java | tr -s ' ' | sed -e 's/:/ /g' | sed -e 's/->/ /g' | cut -d ' ' -f 2,8,9 | sed -e 's/ /:/g' | sed -e 's/localhost/\'\"`hostname -i`\"\'/g' >> %s &", ports[i], fname);
		//                sprintf(command, "lsof -i:%d | grep -v COMMAND | grep -v LISTEN | grep java | tr -s ' ' | sed -e 's/:/ /g' | sed -e 's/->/ /g' | cut -d ' ' -f 2,8,9 | sed -e 's/ /:/g' | sed -e 's/localhost/127.0.0.1/g' >> %s &", ports[i], fname);
		//                system(command);
		//                bzero(command, MAX_COMMAND_LEN);
		//                exit(0);
		//            } else {
		//                pid2 = fork();
		//                if (pid2 == 0) {
		//                    //sprintf(command, "lsof -i:%d | grep -v COMMAND | grep LISTEN | grep java | tr -s ' ' | sed -e 's/:/ /g' | sed -e 's/->/ /g' | cut -d ' ' -f 2,8,9 | sed -e 's/ /:/g' | sed -e 's/*/\'\"`hostname -i`\"\'/g' | sed -e 's/localhost/\'\"`hostname -i`\"\'/g' >> %s &", ports[i], fname);
		//                    sprintf(command, "lsof -i:%d | grep -v COMMAND | grep LISTEN | grep java | tr -s ' ' | sed -e 's/:/ /g' | sed -e 's/->/ /g' | cut -d ' ' -f 2,8,9 | sed -e 's/ /:/g' | sed -e 's/*/127.0.0.1/g' | sed -e 's/localhost/\'\"`hostname -i`\"\'/g' >> %s &", ports[i], fname);
		//                    system(command);
		//                    bzero(command, MAX_COMMAND_LEN);
		//                    exit(0);
		//                }
		//            }
		//        sprintf(command, "lsof -i:%d >> %s &", ports[i], fname);
		sprintf(command, "lsof -i >> %s &", fname);
		system(command);
		fprintf(file, "%s", command);

		usleep(sleeping_time); // 100 msec (escala en microsecond)
		loops++;
		//final = time(NULL);
		//fprintf(file, "%d loops with %d ([%u - %u, %u]= %f s)\n", loops, nports, final, comienzo, init_loop, difftime(final, comienzo));
		fprintf(file, "%d loops with %d\n", loops, nports);
		fflush(file);
		//        fclose(file); /*done!*/
	}
	}
