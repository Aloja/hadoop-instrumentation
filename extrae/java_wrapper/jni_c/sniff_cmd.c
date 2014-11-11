#include <snifferreceiver.h>
#include <signal.h>

void * endExtrae(int signum) {
	Extrae_fini();
	exit(0);
}

int main(int argc, char *argv[]){
	//setenv("LD_LIBRARY_PATH","/home/smendoza/lightness/lib:/home/smendoza/lightness/hadoop-apps/extrae-2.5.1-dist/lib",1);
	//setenv("EXTRAE_ON","1",1);
	int inbound=atoi(argv[1]);
	int nports=atoi(argv[argc-1]);
	int ports[nports];
	signal (SIGINT, endExtrae);
	printf("inbound=%d, nports=%d, ",inbound,nports);
	int i=0;
	for(;i<nports;++i){
		ports[i] = atoi(argv[i+2]);
		printf("port[%d]=%d,",i,ports[i]);
	}
	printf("\n");
	sniffing(inbound,ports,nports);
}
